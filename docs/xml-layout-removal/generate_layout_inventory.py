#!/usr/bin/env python3
"""Read-only source census; writes only explicit documentation outputs beside this script.
No Gradle, compiler, device, network, application, or resource deletion is invoked.
Lexical references/call candidates are source evidence, not runtime reachability proof.
"""
from __future__ import annotations
import argparse, collections, hashlib, importlib.util, io, json, re, subprocess, tarfile, xml.parsers.expat
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).resolve().parent
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
FROZEN = '803cd4c884243ab9ef9c92cb0c4959d63bc27b00'
LAYOUT = re.compile(r'(^|/)res/layout[^/]*/[^/]+\.xml$')
R_LAYOUT = re.compile(r'\b(?P<owner>(?:[\w]+\s*\.\s*)*R)\s*\.\s*layout\s*\.\s*(?P<name>\w+)')
XML_LAYOUT = re.compile(r'@(?:(?P<package>[\w.]+):)?layout/(?P<name>\w+)')
NS = {'http://schemas.android.com/apk/res/android':'android', 'http://schemas.android.com/apk/res-auto':'app', 'http://schemas.android.com/tools':'tools'}
def git(*args): return subprocess.check_output(['git',*args],cwd=ROOT)
def sha(data): return hashlib.sha256(data).hexdigest()
def test_path(p): return any(x.lower().startswith(('test','androidtest')) for x in Path(p).parts[2:3]) or p.startswith('benchmark/')
def production_code(p): return p.startswith('app/src/') and p.endswith(('.kt','.java')) and not test_path(p)
def category(p):
    if p.startswith('docs/'): return 'documentation'
    if p.startswith('scripts/'): return 'source_guard_or_generator'
    if test_path(p): return 'test_or_benchmark'
    if LAYOUT.search(p): return 'layout_xml'
    if p.endswith(('.kt','.java')) and p.startswith('app/src/'): return 'production_code'
    if p.endswith('.xml'): return 'nonlayout_xml'
    if p.endswith(('.gradle','.gradle.kts','.toml','.properties')) or p.endswith('Android.bp'): return 'build_configuration'
    return 'other'
def lines(text): return text.splitlines()
def load_baseline():
    data=git('archive',BASE,'app/src','benchmark','app/build.gradle.kts','Android.bp')
    with tarfile.open(fileobj=io.BytesIO(data)) as archive:
        return {m.name:archive.extractfile(m).read() for m in archive if m.isfile()}
def xml_nodes(data):
    parser=xml.parsers.expat.ParserCreate(namespace_separator='|'); nodes=[]; stack=[]
    def norm(key):
        if '|' not in key:return key
        uri,key=key.split('|',1); return NS.get(uri,uri)+':'+key
    def start(tag,attrs):
        node={'index':len(nodes),'line':parser.CurrentLineNumber,'tag':norm(tag),'attributes':{norm(k):v for k,v in attrs.items()},'parent_index':stack[-1] if stack else None}
        nodes.append(node); stack.append(node['index'])
    parser.StartElementHandler=start; parser.EndElementHandler=lambda tag:stack.pop(); parser.Parse(data,True)
    return nodes

def main():
    baseline=load_baseline()
    current={str(p.relative_to(ROOT)):p.read_bytes() for p in ROOT.rglob('*') if p.is_file() and '.git' not in p.parts and not p.is_relative_to(OUT) and '__pycache__' not in p.parts}
    baseline_layouts={p:b for p,b in baseline.items() if LAYOUT.search(p)}
    current_layouts={p:b for p,b in current.items() if LAYOUT.search(p)}
    names=sorted({Path(p).stem for p in baseline_layouts|current_layouts})
    bindings={''.join(s.capitalize() for s in name.split('_'))+'Binding':name for name in names}
    named=re.compile(r'(?<![\w])('+'|'.join(map(re.escape,names))+r')(?![\w])')
    binding_re=re.compile(r'\b('+'|'.join(bindings)+r')\b')
    spec=importlib.util.spec_from_file_location('binding_guard',ROOT/'scripts/compose-migration/check-binding-removal.py')
    guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard)
    without_comments=guard.without_comments
    texts={p:b.decode('utf8') for p,b in current.items() if b'\x00' not in b and p.endswith(('.kt','.java','.xml','.json','.md','.py','.kts','.gradle','.toml','.properties','.pro','.txt','.bp','.yml','.yaml'))}
    base_texts={p:b.decode('utf8') for p,b in baseline.items() if b'\x00' not in b and p.endswith(('.kt','.java','.xml'))}
    refs=collections.defaultdict(list); historical=collections.defaultdict(list)
    for target,corpus in [(refs,texts),(historical,base_texts)]:
        for path,text in sorted(corpus.items()):
            if LAYOUT.search(path):
                mask=re.sub(r'<!--.*?-->',lambda m:re.sub(r'[^\n]',' ',m[0]),text,flags=re.S)
            elif path.endswith(('.kt','.java','.kts')): mask=without_comments(text)
            else:mask=text
            ml=mask.splitlines()
            for num,line in enumerate(text.splitlines(),1):
                active=ml[num-1] if num<=len(ml) else ''
                row_names={m[1] for m in named.finditer(line)}
                row_names|={bindings[m[1]] for m in binding_re.finditer(line)}
                for name in sorted(row_names):
                    binding=''.join(s.capitalize() for s in name.split('_'))+'Binding'
                    r=[m for m in R_LAYOUT.finditer(line) if m['name']==name]
                    xr=[m for m in XML_LAYOUT.finditer(line) if m['name']==name]
                    if r:kind='framework_layout_reference' if all(re.sub(r'\s','',m['owner'])=='android.R' for m in r) else 'application_layout_resource_reference'
                    elif xr:kind='tools_layout_reference' if 'tools:' in line else 'xml_layout_reference'
                    elif re.search(r'\b'+binding+r'\b',line):kind='generated_binding_name'
                    else:kind='literal_layout_name_or_document_path'
                    present=bool(re.search(r'(?<!\w)'+re.escape(name)+r'(?!\w)',active) or re.search(r'\b'+binding+r'\b',active))
                    target[name].append({'path':path,'line':num,'category':category(path),'kind':kind,'active_not_comment':present,'text':line.strip()})
    ledger=json.loads((ROOT/'docs/compose-migration/screen-parity-ledger.json').read_text())
    roots={r['path']:r for r in ledger['screens']}
    renderer_map=json.loads((ROOT/'docs/compose-migration/component-style-restoration-renderer-map.json').read_text())
    styles=collections.defaultdict(list)
    for mapping in renderer_map['instance_mappings']:styles[mapping['baseline_path']].append(mapping)
    current_code={p:without_comments(t) for p,t in texts.items() if production_code(p)}
    current_code_lines={p:t.splitlines() for p,t in current_code.items()}
    composables=collections.defaultdict(list)
    # Annotation to following declaration; declarations with intervening annotations/visibility supported.
    for p,t in current_code.items():
        for m in re.finditer(r'@Composable\b(?:\s+@\w+(?:\([^()]*\))?)*\s+(?:(?:private|internal|public|inline|tailrec|operator|suspend|override|open)\s+)*fun\s+(?:<[^>]+>\s*)?(?:[\w?.<>]+\.)?(\w+)\s*(?:<[^>]*>)?\s*\(',t,re.S):
            composables[m[1]].append({'path':p,'line':t.count('\n',0,m.start())+1,'function':m[1]})
    # File graph uses named type/function tokens. It is an explicit lexical candidate chain, not call-graph proof.
    declared=collections.defaultdict(set)
    for p,t in current_code.items():
        for m in re.finditer(r'\b(?:class|object|interface|typealias)\s+(\w+)',t):declared[m[1]].add(p)
    reverse=collections.defaultdict(set)
    evidence=collections.defaultdict(list)
    for p,t in current_code.items():
        for symbol in set(re.findall(r'\b[A-Za-z_]\w*\b',t))&declared.keys():
            if len(declared[symbol])>3:continue
            for dest in declared[symbol]:
                if dest==p:continue
                # Do not count import/declaration lines as runtime evidence.
                hit=next(((n,l.strip()) for n,l in enumerate(current_code_lines[p],1) if not re.match(r'\s*(import|package)\b',l) and re.search(r'\b'+re.escape(symbol)+r'\b',l)),None)
                if hit:reverse[dest].add(p);evidence[(dest,p)].append({'symbol':symbol,'line':hit[0],'text':hit[1]})
    def root_paths(owner):
        if owner in roots:return [{'root_id':roots[owner]['id'],'path_chain':[owner],'evidence':[],'status':'direct_ledger_owner','ledger_entry_reference':roots[owner]['entry_reference']}]
        found=[];queue=collections.deque([(owner,[owner],[])]);seen={owner}
        while queue:
            node,path,ev=queue.popleft()
            if len(path)>=5:continue
            for caller in sorted(reverse[node]):
                if caller in seen:continue
                seen.add(caller);new=path+[caller];ne=ev+[{'callee_file':node,'caller_file':caller,'references':evidence[(node,caller)]}]
                if caller in roots:found.append({'root_id':roots[caller]['id'],'path_chain':new,'evidence':ne,'status':'lexical_candidate_requires_semantic_review','ledger_entry_reference':roots[caller]['entry_reference']})
                else:queue.append((caller,new,ne))
        return found
    def direct_composable_calls(owner):
        out=[]
        for n,l in enumerate(current_code_lines.get(owner,[]),1):
            if re.match(r'\s*(import|package)\b',l):continue
            for name in re.findall(r'\b(\w+)\s*\(',l):
                if name in composables and not re.search(r'\bfun\s+'+name+r'\s*\(',l):out.append({'caller_path':owner,'caller_line':n,'function':name,'definitions':composables[name],'text':l.strip()})
        return out
    def namespace_resources(nodes):
        found=[]
        for node in nodes:
            for key,value in node['attributes'].items():
                for match in re.finditer(r'[@?](?:\+)?(?:(?P<package>[\w.]+):)?(?P<type>\w+)/(?P<name>[\w.]+)',value):
                    found.append({'node_index':node['index'],'line':node['line'],'attribute':key,'value':match[0],'resource_type':match['type'],'resource_name':match['name'],'package':match['package'],'tools_only':key.startswith('tools:')})
        return found
    rows=[]
    for path in sorted(current_layouts):
        data=current_layouts[path];name=Path(path).stem;nodes=xml_nodes(data);b=baseline_layouts.get(path)
        hist=[r for r in historical[name] if r['category']=='production_code' and r['active_not_comment'] and r['kind'] in ['application_layout_resource_reference','generated_binding_name']]
        owners=sorted({r['path'] for r in hist})
        mappings=[]
        for owner in owners:
            mappings.append({'baseline_owner_path':owner,'current_path_exists':owner in current_code,'baseline_reference_evidence':[r for r in hist if r['path']==owner], 'ledger_paths':root_paths(owner) if owner in current_code else [],'current_direct_composable_calls':direct_composable_calls(owner)})
        runtime=[r for r in refs[name] if r['category']=='production_code' and r['active_not_comment'] and r['kind'] in ['application_layout_resource_reference','generated_binding_name']]
        tests=[r for r in refs[name] if r['category']=='test_or_benchmark' and r['active_not_comment'] and r['kind'] in ['application_layout_resource_reference','generated_binding_name']]
        xml_in=[r for r in refs[name] if r['category'] in ['layout_xml','nonlayout_xml'] and r['active_not_comment'] and r['kind'] in ['xml_layout_reference','tools_layout_reference']]
        xml_runtime=[r for r in xml_in if r['category']=='nonlayout_xml' and r['kind']=='xml_layout_reference']
        disposition='production_live' if runtime or xml_runtime else ('test_baseline_only' if tests else 'dormant_resource')
        rows.append({'path':path,'resource_name':name,'physical_source_set':Path(path).parts[2],'qualifier':Path(path).parent.name.removeprefix('layout') or 'default','effective_gradle_source_set':'main','effective_gradle_variants':'all channel x full/tv variants via app/build.gradle.kts:358-360','android_bp_included':path.startswith('app/src/main/res/'),'ownership':'app-owned tracked source resource','current_sha256':sha(data),'baseline_sha256':sha(b) if b else None,'baseline_git_blob':hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest() if b else None,'baseline_recovery':BASE+':'+path if b else None,'matches_baseline_bytes':data==b,'root_tag':nodes[0]['tag'],'xml_element_count':len(nodes),'resource_attribute_references':namespace_resources(nodes),'custom_view_tags':sorted({node['tag'] for node in nodes if node['tag'].startswith('com.celzero.')}),'library_view_tags':sorted({node['tag'] for node in nodes if '.' in node['tag'] and not node['tag'].startswith(('com.celzero.','android.'))}),'defined_ids':sorted({v.removeprefix('@+id/') for node in nodes for k,v in node['attributes'].items() if v.startswith('@+id/')}),'all_current_xml_nodes':nodes,'all_baseline_xml_nodes':xml_nodes(b) if b!=data and b else None,'baseline_attributes_reference':'all_current_xml_nodes (baseline identical)' if data==b else 'all_baseline_xml_nodes','qualifier_sibling_paths':sorted(p for p in current_layouts if Path(p).stem==name),'runtime_disposition':disposition,'runtime_layout_dependents':runtime,'test_baseline_dependents':tests,'inbound_xml_references':xml_in,'current_name_references':refs[name],'baseline_owner_mappings':mappings,'baseline_other_references':[r for r in historical[name] if r not in hist],'component_renderer_mappings':styles[path],'compose_mapping_status':'source_candidates_present_requires_verified_styling_equivalence' if mappings or styles[path] else 'unresolved_no_direct_baseline_owner_or_component_mapping','deletion_status':'NOT_APPROVED_BY_THIS_AUDIT: reconcile test/ID/tool/style/renderer dependencies before removal','platform_required_exception':None})
    by_path={r['path']:r for r in rows}
    for row in rows:
        row['included_owner_candidates']=[]
        for ref in row['inbound_xml_references']:
            parent=by_path.get(ref['path'])
            if parent and ref['kind']=='xml_layout_reference':
                row['included_owner_candidates'].append({'including_layout':parent['path'],'include_line':ref['line'],'baseline_owner_mappings':parent['baseline_owner_mappings']})
        if row['included_owner_candidates'] and not row['baseline_owner_mappings']:
            row['compose_mapping_status']='included_layout_parent_candidates_require_styling_equivalence'
    platform=[];reflection=[];alternate=[]
    for p,t in sorted(texts.items()):
        if p.startswith(('docs/','scripts/')):continue
        code=without_comments(t) if p.endswith(('.kt','.java','.kts')) else t
        for n,l in enumerate(code.splitlines(),1):
            if re.search(r'\bRemoteViews\b|\bAppWidgetProvider\b|android:initialLayout|previewLayout|setCustom(?:Content|BigContent|HeadsUpContent)View|android\.R\.layout\.',l):platform.append({'path':p,'line':n,'text':l.strip()})
            if re.search(r'getIdentifier\s*\(|Class\.forName|R\s*\.\s*class|getDeclaredField|getField\s*\(|[Rr]esource\w*\s*\([^\n]*[Ll]ayout|"layout"|\'layout\'',l):reflection.append({'path':p,'line':n,'text':l.strip()})
        if p.endswith('.xml') and not LAYOUT.search(p):
            try:ns=xml_nodes(t.encode())
            except Exception:continue
            if ns and (ns[0]['tag'].endswith(('Layout','View','ViewGroup')) or ns[0]['tag'] in ('merge','include','layout')):alternate.append({'path':p,'root_tag':ns[0]['tag']})
    summary={'physical_current_layouts':len(rows),'unique_current_resource_names':len({r['resource_name'] for r in rows}),'baseline_layouts':len(baseline_layouts),'baseline_already_removed':[{'path':p,'baseline_sha256':sha(b),'baseline_recovery':BASE+':'+p} for p,b in sorted(baseline_layouts.items()) if p not in current_layouts],'by_physical_source_set_and_qualifier':dict(collections.Counter(r['physical_source_set']+'/'+r['qualifier'] for r in rows)),'runtime_dispositions':dict(collections.Counter(r['runtime_disposition'] for r in rows)),'test_layout_names':sorted({r['resource_name'] for r in rows if r['test_baseline_dependents']}),'current_layouts_changed_from_baseline':[r['path'] for r in rows if not r['matches_baseline_bytes']],'files_without_baseline_owners':[r['path'] for r in rows if not r['baseline_owner_mappings']],'mapping_status':dict(collections.Counter(r['compose_mapping_status'] for r in rows)),'root_ledger_count':len(roots),'all_xml_nodes':sum(r['xml_element_count'] for r in rows),'unique_defined_ids':len({i for r in rows for i in r['defined_ids']}),'current_production_source_sets':dict(collections.Counter(Path(p).parts[2] for p in current_code)),'source_set_layout_counts_including_zero':{str(p.relative_to(ROOT)):sum(path.startswith(str(p.relative_to(ROOT))+'/') for path in current_layouts) for p in sorted((ROOT/'app/src').iterdir()) if p.is_dir()},'additional_layout_like_xml_outside_resource_layout_dirs':alternate}
    result={'schema_version':1,'baseline_commit':BASE,'frozen_current_commit':FROZEN,'observed_head':git('rev-parse','HEAD').decode().strip(),'status':'READ_ONLY_DELETION_PLAN; source-only evidence, no runtime/compile acceptance','scope':'All filesystem layout* XML directories, all app source sets, benchmark, repository source/build/test/XML/text references. Library dependency internals and generated APK resources unavailable. XML attributes preserved without excluding tools metadata.','method_limits':['References are exact lexical matches with Kotlin/Java/XML comments distinguished; arbitrary assembled names/compiled dependency reflection cannot be proven absent by source scan.','Owner-to-root graph edges are explicitly labelled lexical candidate chains, not a claim of verified runtime reachability.','Component renderer candidates inherit source-only status of existing renderer map; independent styling mapping remains required.','No Android build/device/runtime or network was run.'], 'summary':summary,'build_resource_wiring_evidence':[{'path':'app/build.gradle.kts','line':358,'text':'sourceSets { getByName("main").res.directories.add("src/full/res"); getByName("full").res.directories.clear() }'},{'path':'Android.bp','line':30,'text':'resource_dirs: ["app/src/main/res"]'}], 'platform_reference_evidence':platform,'reflection_and_dynamic_name_candidates':reflection,'files':rows}
    special_roots={
      'main:HomeScreenActivity':{'disposition':'already_removed_application_shell','prior_layout':'app/src/full/res/layout/activity_home_screen.xml','current_evidence':'HomeScreenActivity.kt:147-175 installs ComposeView, HomeChromeLayout and ActivityFragmentContainer'},
      'main:ConfigureRethinkBasicActivity':{'disposition':'already_removed_application_shell','prior_layout':'app/src/full/res/layout/fragment_rethink_basic.xml','current_evidence':'ConfigureRethinkBasicActivity.kt:79-92 installs ComposeView and ActivityFragmentContainer'},
      'main:FragmentHostActivity':{'disposition':'already_removed_application_shell','prior_layout':'app/src/main/res/layout/activity_fragment_host.xml','current_evidence':'FragmentHostActivity.kt:110-125 installs ComposeView and ActivityFragmentContainer'},
      'main:SubscriptionAnimDialog':{'disposition':'already_removed_application_content','prior_layout':'app/src/main/res/layout/dialog_subscription_anim.xml','current_evidence':'SubscriptionAnimDialog.kt:88-90 installs SubscriptionCelebration in ComposeView'},
      'main:NotificationHandlerActivity':{'disposition':'framework_layout_only_no_app_owned_xml','current_evidence':'NotificationHandlerActivity.kt:96 android.R.layout.activity_list_item; invisible trampoline documented separately'},
      'main:TestDialogActivity':{'disposition':'empty_context_host_no_layout','current_evidence':'TestDialogActivity.kt:25-29 onCreate calls only super; no layout in baseline or current'},
      'main:RethinkLogBottomSheet':{'disposition':'retired_unopened_baseline_class','prior_layout':'app/src/full/res/layout/bottom_sheet_conn_track.xml','current_evidence':'Class absent current. Baseline exact class references only its own definition; shared layout remains associated with live ConnTrackerBottomSheet'},
      'main:DnsCryptRelaysDialog':{'disposition':'retired_class_replaced_by_native_dialog','prior_layout':'app/src/main/res/layout/dialog_dnscrypt_relays.xml','current_evidence':'DnsCryptListFragment.kt:89 calls DnsRelaysDialog; DnsEndpointScreen.kt:168 defines it'},
      'main:RpnProxyHopDialog':{'disposition':'inherits_shared_compose_dialog','prior_layout':'app/src/main/res/layout/dialog_wg_hop.xml','current_evidence':'RpnProxyHopDialog.kt:33 extends GenericHopDialog, GenericHopDialog.kt:50 calls WgHopContent; no production opener recorded in existing ledger'},
      'main:WgHopDialog':{'disposition':'inherits_shared_compose_dialog','prior_layout':'app/src/main/res/layout/dialog_wg_hop.xml','current_evidence':'WgHopDialog.kt:34 extends GenericHopDialog, GenericHopDialog.kt:50 calls WgHopContent'},
    }
    root_rows=[]
    for owner,root in roots.items():
        associated=[r['path'] for r in rows if any(m['baseline_owner_path']==owner for m in r['baseline_owner_mappings'])]
        root_rows.append({'root_id':root['id'],'owner_path':owner,'current_owner_exists':owner in current_code,'current_owner_sha256':sha(current[owner]) if owner in current else None,'ledger_entry_reference':root['entry_reference'],'remaining_layouts_owned_in_baseline':associated,'current_composable_call_candidates':direct_composable_calls(owner),'accounting':special_roots.get(root['id'],{'disposition':'direct_baseline_layout_owner_with_current_compose_source','note':'Current call candidates and existing ledger are source evidence; styling and runtime acceptance remain independent.'})})
    (OUT/'root-layout-accounting.json').write_text(json.dumps({'baseline_commit':BASE,'frozen_commit':FROZEN,'count':len(root_rows),'status':'all 115 ledger roots accounted for; no runtime or visual acceptance claim','roots':root_rows},indent=2)+'\n')
    output=OUT/'layout-inventory.json';output.write_text(json.dumps(result,indent=2)+'\n')
    preliminary={r['path']:{'baseline_owners':[{k:m[k] for k in ['baseline_owner_path','current_path_exists','current_direct_composable_calls','ledger_paths']} for m in r['baseline_owner_mappings']],'component_renderer_count':len(r['component_renderer_mappings']),'mapping_status':r['compose_mapping_status']} for r in rows}
    (OUT/'layout-owner-candidates.json').write_text(json.dumps(preliminary,indent=2)+'\n')
    print(json.dumps(summary,indent=2))
    print('Wrote',output.relative_to(ROOT),'; SHA256',sha(output.read_bytes()))
if __name__=='__main__':main()
