#!/usr/bin/env python3
"""Finite source regression audit, not Kotlin compilation or UI/runtime verification.

The checked-in census is the reviewable expected render graph. This script independently
rebuilds the baseline XML census and requires each live node's concrete call-site and
wrapper/delegate evidence. It never regenerates expectations from changed production
sources. Negative controls mutate copies of source text only, never production files.
"""
import argparse
import hashlib
import functools
from datetime import datetime, timezone
import json
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
DOC = ROOT / 'docs/xml-layout-removal'
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
UI = 'app/src/main/java/com/celzero/bravedns/ui/'
P = UI + 'compose/'
CAT = P + 'controls/OriginalCompoundCatalog.kt'
CONTROL = P + 'controls/OriginalCompoundControls.kt'
SLIDER = P + 'controls/OriginalSliders.kt'
STYLES = 'app/src/main/res/values/original_compound_styles.xml'
KINDS = {'MaterialSwitch', 'MaterialCheckBox', 'AppCompatCheckBox', 'CheckBox',
         'MaterialRadioButton', 'AppCompatRadioButton', 'RadioButton',
         'AppCompatSeekBar', 'SeekBar', 'Slider'}
A = '{http://schemas.android.com/apk/res/android}'
NS = {A: 'android:', '{http://schemas.android.com/apk/res-auto}': 'app:',
      '{http://schemas.android.com/tools}': 'tools:'}
# Style attributes intentionally excludes layout/constraint, content, visibility, and
# mutable state. They remain exact in the independently compared baseline attrs.
VISUAL = {'android:button', 'app:buttonCompat', 'android:buttonTint', 'app:buttonTint',
          'app:buttonIcon', 'app:buttonIconTint', 'android:thumb', 'app:thumbTint',
          'app:track', 'app:trackTint', 'app:thumbIcon', 'app:thumbIconTint',
          'app:trackDecoration', 'app:trackDecorationTint', 'android:minWidth',
          'android:minHeight', 'android:padding', 'android:paddingStart',
          'android:paddingEnd', 'android:paddingTop', 'android:paddingBottom',
          'android:paddingHorizontal', 'android:paddingVertical', 'android:drawableBottom',
          'app:switchMinWidth', 'app:materialThemeOverlay', 'android:theme',
          'android:background', 'android:foregroundTint', 'android:progressDrawable',
          'android:thumbTint', 'android:progressTint', 'android:progressBackgroundTint',
          'app:tickMark', 'app:tickMarkTint', 'app:thumbColor', 'app:trackColorActive',
          'app:trackColorInactive', 'app:tickColorActive', 'app:tickColorInactive',
          'app:haloColor', 'app:thumbRadius', 'app:haloRadius', 'app:trackHeight',
          'app:tickVisible', 'app:thumbElevation'}
EXCEPTIONS = {
 'activity_universal_firewall_settings:firewall_check_ipv4_check',
 'list_item_play_subs:selection_indicator', 'item_server:checkbox_server'}


def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT, text=True)


@functools.lru_cache(maxsize=4096)
def uncomment(text):
    """Remove line/block comments, preserving strings and positions; no Kotlin execution."""
    out = list(text); i = 0; quote = None
    while i < len(text):
        if quote:
            if text.startswith(quote, i): i += len(quote); quote = None; continue
            if text[i] == '\\' and quote != '"""': i += 2; continue
            i += 1; continue
        if text.startswith('"""', i): quote = '"""'; i += 3; continue
        if text[i] in '\"\'': quote = text[i]; i += 1; continue
        if text.startswith('//', i):
            j = text.find('\n', i); j = len(text) if j < 0 else j
            out[i:j] = ' ' * (j-i); i = j; continue
        if text.startswith('/*', i):
            j = i+2; depth = 1
            while j < len(text) and depth:
                if text.startswith('/*', j): depth += 1; j += 2
                elif text.startswith('*/', j): depth -= 1; j += 2
                else: j += 1
            out[i:j] = ['\n' if c == '\n' else ' ' for c in text[i:j]]; i = j; continue
        i += 1
    return ''.join(out)


@functools.lru_cache(maxsize=4096)
def compact(s):
    # Preserve spaces inside strings. Tokenized source allows harmless whitespace edits
    # without allowing a changed literal source key to pass.
    return re.findall(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|[A-Za-z_$][\w$]*|\d+(?:\.\d+)?|[^\s]', uncomment(s))


def contains(source, snippet):
    a, b = compact(source), compact(snippet)
    return bool(b) and any(a[i:i+len(b)] == b for i in range(len(a)-len(b)+1))


@functools.lru_cache(maxsize=8192)
def calls(text, name):
    """Return balanced named argument lists with optional immediately trailing lambda."""
    clean = uncomment(text); result=[]
    for m in re.finditer(r'\b' + re.escape(name) + r'\s*\(', clean):
        if re.search(r'\bfun\s*$', clean[max(0,m.start()-15):m.start()]): continue
        i = clean.index('(', m.start()); start = i; depth = 0; quote = None
        while i < len(clean):
            c = clean[i]
            if quote:
                if c == '\\': i += 2; continue
                if c == quote: quote = None
            elif c in '\"\'': quote = c
            elif c == '(': depth += 1
            elif c == ')':
                depth -= 1
                if not depth: break
            i += 1
        result.append(clean[m.start():i+1])
    return result


def args(call):
    text = call[call.index('(')+1:-1]; result = []; start = 0; stack = []; quote = None; i = 0
    while i < len(text):
        c = text[i]
        if quote:
            if c == '\\': i += 2; continue
            if c == quote: quote = None
        elif c in '\"\'': quote = c
        elif c in '([{': stack.append(c)
        elif c in ')]}': stack.pop()
        elif c == ',' and not stack: result.append(text[start:i].strip()); start = i+1
        i += 1
    result.append(text[start:].strip())
    return result


def baseline():
    paths = git('ls-tree', '-r', '--name-only', BASE).splitlines()
    layouts = [p for p in paths if re.fullmatch(r'app/src/[^/]+/res/layout[^/]*/[^/]+\.xml', p)]
    nodes = {}; xml = {}; hashes = {}
    for path in layouts:
        raw = git('show', BASE + ':' + path)
        tree = ET.fromstring(raw)
        found = False
        for node in tree.iter():
            kind = node.tag.rsplit('.', 1)[-1]
            if kind not in KINDS: continue
            found = True
            attrs = dict(node.attrib)
            attrs = {next((p+k[len(n):] for n,p in NS.items() if k.startswith(n)), k): v for k,v in attrs.items()}
            key = Path(path).stem + ':' + attrs['android:id'].split('/')[-1]
            assert key not in nodes, 'Duplicate independently reconstructed source key: ' + key
            nodes[key] = {'path':path, 'key':key, 'type':kind, 'attrs':attrs}
        if found:
            xml[path] = tree; hashes[path] = hashlib.sha256(raw.encode()).hexdigest()
    return nodes, xml, hashes, paths


def verify(sources, census, base_nodes):
    errors = []
    def check(ok, reason):
        if not ok: errors.append(reason)
    ns = census['nodes']; expected = {n['key']:n for n in ns}
    check(len(expected)==len(ns)==154 and len({n['path'] for n in ns})==52, 'census cardinality must be 154 nodes / 52 layouts')
    check(set(expected)==set(base_nodes), 'baseline/census node set mismatch')
    for key,n in expected.items():
        check(all(n.get(x)==base_nodes.get(key,{}).get(x) for x in ('path','type','attrs')), 'baseline exact attributes: '+key)
    style_tree = ET.fromstring(sources[STYLES])
    styles = {s.attrib['name']:s for s in style_tree.findall('style')}
    check(len(styles)==154, '154 independent style resources required')
    cat_entries = re.findall(r'"([^"\n]+:[^"\n]+)"\s+to\s+OriginalCompoundSpec\(R\.style\.(\w+),\s*OriginalCompoundKind\.(\w+),\s*([\d.]+)f,\s*([\d.]+)f,\s*(true|false)\)', uncomment(sources[CAT]))
    catalog = {c[0]:c[1:] for c in cat_entries}
    check(len(cat_entries)==len(catalog)==154 and set(catalog)==set(expected), 'finite catalog covers exactly baseline nodes')
    for key,n in expected.items():
        name = 'OriginalCompound_' + key.replace(':','_')
        check(n['style_resource']==name and name in styles, 'exact style resource identity: '+key)
        if name in styles:
            s = styles[name]; a = n['attrs']
            wanted = {(k[4:] if k.startswith('app:') else k):v for k,v in a.items() if k in VISUAL}
            items = {i.attrib['name']:i.text for i in s.findall('item')}
            check(s.attrib.get('parent','')==a.get('style','').replace('@style/',''), 'style parent: '+key)
            check(items==wanted and len(items)==len(s.findall('item')), 'exact style projection: '+key)
        if key in catalog:
            style,kind,width,height,cleared = catalog[key]
            wanted_kind = {'AppCompatSeekBar':'SeekBar','RadioButton':'MaterialRadioButton','CheckBox':'MaterialCheckBox'}.get(n['type'],n['type'])
            a=n['attrs']
            size = lambda attr: float(a.get(attr,'0dp')[:-2]) if a.get(attr,'').endswith('dp') else 0.0
            check((style,kind,float(width),float(height),cleared)==(name,wanted_kind,size('android:layout_width'),size('android:layout_height'),'true' if a.get('android:button')=='@null' else 'false'), 'catalog family/dimension/cleared-button contract: '+key)
        mapping=n.get('render_mapping',{})
        if key in EXCEPTIONS:
            check(mapping.get('status')=='inactive_baseline_exception' and not mapping.get('paths'), 'inactive exception disposition: '+key)
            continue
        check(mapping.get('status')=='mapped' and bool(mapping.get('paths')), 'missing actual render mapping: '+key)
        for index,path in enumerate(mapping.get('paths',[])):
            check(path.get('resolved_source_key')==key and path.get('source_expression'), 'finite source expression: '+key)
            check(path.get('native_renderer') in ('OriginalSwitch','OriginalCheckbox','OriginalRadioButton','OriginalSlider'), 'native renderer identity: '+key)
            evidence=path.get('evidence',[])
            check(bool(evidence) and any(e.get('role')=='native-render-call' for e in evidence), 'native call evidence: '+key)
            check(any(path.get('native_renderer','')+'(' in e.get('snippet','').replace(' ','') for e in evidence), 'renderer invocation evidence: '+key)
            expected_renderer = 'OriginalSwitch' if n['type']=='MaterialSwitch' else 'OriginalSlider' if n['type'] in ('AppCompatSeekBar','SeekBar','Slider') else 'OriginalRadioButton' if 'RadioButton' in n['type'] else 'OriginalCheckbox'
            check(path.get('native_renderer')==expected_renderer, 'renderer family matches baseline widget: '+key)
            primary = next((e for e in evidence if e['role'] in ('wrapper-render-call','native-render-call')), None)
            if primary:
                invocation=primary['snippet']; values=args(invocation)
                expression = next((v.split('=',1)[1].strip() for v in values if v.startswith('compoundSource =')), None) if invocation.startswith('WgCheck(') else values[0]
                check(expression is not None and compact(expression)==compact(path['source_expression']), 'source expression must equal actual invocation argument: '+key)
            for e in evidence:
                check(e['path'] in sources and contains(sources[e['path']],e['snippet']), 'render path %s #%s %s: %s' % (key,index,e['role'],e['path']))
    for c in census['source_contracts']:
        for entry in [c]+c.get('related_sources',[]):
            for snippet in entry['snippets']:
                check(entry['path'] in sources and contains(sources[entry['path']],snippet), 'source contract: '+c['name']+' ['+entry['path']+']')
    # Every actual compound/slider network node is finite and uniquely ID-addressable.
    network=[]
    for path,text in sources.items():
        if '/networksettings/' not in path or not path.endswith('State.kt'): continue
        for call in calls(text,'NetworkNode'):
            values=args(call)
            if values[0] not in ('NetworkKind.SWITCH','NetworkKind.CHECK','NetworkKind.RADIO','NetworkKind.SLIDER'): continue
            m=re.search(r'key\s*=\s*"network\.([^"\n]+)"',call)
            if m: network.append((path,m.group(1),call))
    check(len(network)==33, 'network tree must enumerate 33 compound/slider nodes')
    for path,id_,call in network:
        keys=[k for k in expected if k.split(':')[-1]==id_]
        check(len(keys)==1, 'network ID must have exactly one catalog key: '+id_)
        if len(keys)==1:
            paths=expected[keys[0]].get('render_mapping',{}).get('paths',[])
            check(any(any(e['path']==path and compact(e['snippet'])==compact(call) for e in p['evidence']) for p in paths), 'actual network node lacks bounded render evidence: '+id_)
    # Coverage is checked in both directions: a newly added native invocation must be
    # accounted for rather than silently escaping the finite render-path audit.
    accounted={(e['path'],tuple(compact(e['snippet']))) for n in ns for p in n.get('render_mapping',{}).get('paths',[]) for e in p['evidence'] if e['role'] in ('wrapper-render-call','native-render-call')}
    uninstantiated=census.get('uninstantiated_native_routes',[])
    accounted.update((e['path'],tuple(compact(e['snippet']))) for e in uninstantiated)
    for e in uninstantiated:
        check(contains(sources[e['path']],e['snippet']), 'uninstantiated generic route contract: '+e['path'])
    for path,text in sources.items():
        if not path.startswith(UI) or not path.endswith('.kt') or '/controls/' in path: continue
        for name in ('OriginalSwitch','OriginalCheckbox','OriginalRadioButton','OriginalSlider','WgCheck'):
            for invocation in calls(text,name):
                check((path,tuple(compact(invocation))) in accounted, 'unmapped actual native/wrapper invocation: '+path+' '+invocation[:90])
    # Constructors/inflation cannot silently replace native drawing.
    for p in (CONTROL,SLIDER):
        check(not re.search(r'\b(?:AndroidView|LayoutInflater|MaterialSwitch|MaterialCheckBox|MaterialRadioButton|AppCompatCheckBox|AppCompatRadioButton|AppCompatSeekBar)\s*\(',uncomment(sources[p])), 'no View/inflation bridge in native renderer: '+p)
    return sorted(set(errors))


def exception_evidence(base_nodes,xml,paths):
    report={}
    key='activity_universal_firewall_settings:firewall_check_ipv4_check'; tree=xml[base_nodes[key]['path']]
    parent={c:p for p in tree.iter() for c in p}; node=next(n for n in tree.iter() if n.attrib.get(A+'id','').endswith('/firewall_check_ipv4_check'))
    ancestors=[]
    while node in parent:
        node=parent[node]
        if node.attrib.get(A+'visibility')=='gone': ancestors.append(node.attrib.get(A+'id',node.tag))
    owner='app/src/main/java/com/celzero/bravedns/ui/activity/UniversalFirewallSettingsActivity.kt'
    raw=git('show',BASE+':'+owner)
    assert ancestors and 'firewallCheckIpv4' not in uncomment(raw)
    assert '/* includeView.firewallCheckIpv4Check.setOnCheckedChangeListener' in raw
    report[key]={'gone_ancestors':ancestors,'baseline_controller':owner,'active_controller_references':0,'listener_setup':'Block commented; initial isChecked assignment line commented.'}
    key='list_item_play_subs:selection_indicator'; assert base_nodes[key]['attrs']['android:visibility']=='gone'
    owners=[p for p in paths if p.endswith('/GooglePlaySubsAdapter.kt')]
    assert len(owners)==2
    for owner in owners:
        raw=git('show',BASE+':'+owner)
        assert 'selectionIndicator' not in uncomment(raw)
        if '/play/' in owner:
            assert 'selection via card stroke only (no radio button)' in raw
            assert 'applySelectionStyle(isSelected)' in uncomment(raw)
    report[key]={'baseline_visibility':'gone','size':['0dp','0dp'],'baseline_owners':owners,'active_selectionIndicator_references':0,'selection_route':'Existing card selection styling; hidden radio was not used.'}
    key='item_server:checkbox_server'
    proc=subprocess.run(['git','grep','-n','-E',r'ItemServerBinding|R\.layout\.item_server([^[:alnum:]_]|$)|@layout/item_server([^[:alnum:]_]|$)|"item_server"',BASE,'--','app/src'],cwd=ROOT,text=True,capture_output=True)
    assert proc.returncode==1, proc.stdout or proc.stderr
    report[key]={'baseline_search_scope':'all tracked app/src files','patterns':['ItemServerBinding','R.layout.item_server (exact resource boundary)','@layout/item_server (exact resource boundary)','literal resource name "item_server" (including getIdentifier)'],'matches':0,'limitation':'Static reference absence only; cannot disprove untracked external reflection. item_server_group is a distinct live layout.'}
    return report


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--report',default=str(DOC/'compound-control-source-results.json'));opt=parser.parse_args()
    census=json.loads((DOC/'compound-control-census.json').read_text())
    base_nodes,xml,hashes,paths=baseline()
    assert len(base_nodes)==154 and len(hashes)==52, (len(base_nodes),len(hashes))
    sources={str(p.relative_to(ROOT)):p.read_text() for p in (ROOT/'app/src').rglob('*.kt')}
    sources[STYLES]=(ROOT/STYLES).read_text()
    for contract in census['source_contracts']:
        for entry in [contract]+contract.get('related_sources',[]):
            path=entry['path']
            if path not in sources: sources[path]=(ROOT/path).read_text()
    errors=verify(sources,census,base_nodes)
    exceptions=exception_evidence(base_nodes,xml,paths)
    negative=[]
    for mutation in census['negative_controls']:
        changed=dict(sources); path=mutation['path']; old=mutation['old'];new=mutation['new']
        assert old in changed[path], 'Negative control no longer applicable: '+mutation['name']
        changed[path]=changed[path].replace(old,new,1)
        rejected=verify(changed,census,base_nodes)
        negative.append({'name':mutation['name'],'rejected':bool(rejected),'detected_by':rejected[:5]})
        if not rejected: errors.append('Negative control incorrectly passed: '+mutation['name'])
    report={'generated_at_utc':datetime.now(timezone.utc).isoformat(),'guard_sha256':hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),'result':'PASS_SOURCE_ONLY' if not errors else 'FAIL_SOURCE_ONLY','baseline':BASE,'scope':'Static text/XML source contracts only. No Kotlin compilation, Gradle, emulator, APK, runtime reference, or screenshots executed.','nodes':len(base_nodes),'layouts':len(hashes),'mapped_nodes':sum(n['render_mapping']['status']=='mapped' for n in census['nodes']),'inactive_baseline_exceptions':exceptions,'network_nodes':33,'uninstantiated_native_routes':census.get('uninstantiated_native_routes',[]),'render_paths':sum(len(n['render_mapping'].get('paths',[])) for n in census['nodes']),'source_contracts':len(census['source_contracts']),'negative_controls':negative,'errors':errors,'baseline_layout_sha256':hashes,'production_file_sha256':{p:hashlib.sha256(sources[p].encode()).hexdigest() for p in sorted({e['path'] for n in census['nodes'] for r in n['render_mapping'].get('paths',[]) for e in r['evidence']}|{entry['path'] for c in census['source_contracts'] for entry in [c]+c.get('related_sources',[])}|{CAT,CONTROL,SLIDER,STYLES})},'remaining_gates':['Kotlin compilation and Android resource resolution are unverified.','Runtime checked/unchecked/disabled/pressed/focused/indeterminate and gesture behavior is unverified.','Visual dimensions, theme/night overlays, RTL, accessibility, screenshot comparison and full interactive flow parity remain unverified.','Finite call-site and delegate guards are regression tripwires, not a general Kotlin semantic or whole-business-logic proof.']}
    Path(opt.report).write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps({k:report[k] for k in ('result','nodes','layouts','mapped_nodes','render_paths','source_contracts','errors')},indent=2))
    print('Negative controls rejected: %s/%s' % (sum(n['rejected'] for n in negative),len(negative)))
    return 1 if errors else 0

if __name__=='__main__':raise SystemExit(main())
