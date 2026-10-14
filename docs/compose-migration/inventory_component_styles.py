#!/usr/bin/env python3
"""Read-only, pinned Git-source census. Does not execute Android or inspect caches."""
from __future__ import annotations
import collections, json, pathlib, re, subprocess, xml.etree.ElementTree as ET

BASELINE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
CURRENT = 'e2b97de59cc854de74010c096ceffbaed6cb4a55'
ROOT = pathlib.Path(__file__).resolve().parents[2]
OUTPUT = ROOT / 'docs/compose-migration/component-style-restoration-inventory.json'
NS = {'http://schemas.android.com/apk/res/android': 'android', 'http://schemas.android.com/apk/res-auto': 'app', 'http://schemas.android.com/tools': 'tools'}
TAGS = {'Chip','Button','MaterialButton','TextInputLayout','TextInputEditText','EditText','AppCompatEditText','SearchView','AppCompatButton','ImageButton','AppCompatImageButton','FloatingActionButton','ExtendedFloatingActionButton','MaterialButtonToggleGroup','ChipGroup'}
CALLS = {'FilterChip','AssistChip','SuggestionChip','InputChip','TextField','OutlinedTextField','BasicTextField','Button','OutlinedButton','TextButton','FilledTonalButton','ElevatedButton','IconButton','FilledIconButton','OutlinedIconButton','IconToggleButton','FilledIconToggleButton','OutlinedIconToggleButton','FilledTonalIconToggleButton','FloatingActionButton','SmallFloatingActionButton','LargeFloatingActionButton','ExtendedFloatingActionButton','SingleChoiceSegmentedButtonRow','MultiChoiceSegmentedButtonRow','SegmentedButton'}
WRAPPERS = {'WgAction','WgToggle','WgField','DnsField','LanPair','UtilityAction','UtilityButton','ReachabilityButton','ReachabilityOutline','RethinkActionChip','LogFilterChips','RpnFilterChip','HomeChips','ConfigAction','RuleAction','PlanButton','SubscriptionButton','NetworkEditField'}

def git(*args): return subprocess.check_output(['git', '-C', str(ROOT), *args], text=True)
def files(rev): return git('ls-tree','-r','--name-only',rev).splitlines()
def source(rev,path): return git('show',f'{rev}:{path}')
def attrs(node):
    return {re.sub(r'^\{([^}]+)\}',lambda m:NS.get(m[1],m[1])+':',k):v for k,v in node.attrib.items()}
def camel(value): return re.sub(r'_([a-z0-9])',lambda m:m[1].upper(),value)
base_files, current_files = files(BASELINE), files(CURRENT)
values, style_defs = {}, collections.defaultdict(list)
for f in base_files:
    if re.search(r'/res/values[^/]*/[^/]+\.xml$',f) and not re.search(r'/values-[a-z]{2}(-|/)',f):
        try: root=ET.fromstring(source(BASELINE,f))
        except ET.ParseError: continue
        for n in root:
            if n.tag == 'style':
                name=n.get('name'); record={'path':f,'parent':n.get('parent'), 'items':{i.get('name'):(i.text or '').strip() for i in n if i.tag=='item'}}
                style_defs[name].append(record)
            elif n.tag in {'color','dimen'}:
                values.setdefault('@'+n.tag+'/'+n.get('name'),[]).append({'path':f,'value':(n.text or '').strip()})
styles = {k: next((v for v in vs if '/values/' in v['path']),vs[0]) for k,vs in style_defs.items()}
def inherit(style):
    name=(style or '').replace('@style/',''); chain=[]; merged={}; seen=set()
    while name and name not in seen:
        seen.add(name); definition=styles.get(name)
        if definition is None:
            chain.append({'name':name,'resolution':'external/library-or-framework; do not assume Compose default equals this'})
            break
        chain.append({'name':name,**definition})
        name=definition['parent']
        if name is None: name=chain[-1]['name'].rsplit('.',1)[0] if '.' in chain[-1]['name'] else ''
        name=(name or '').replace('@style/','')
    for d in reversed(chain): merged.update(d.get('items',{}))
    return chain,merged

current_sources={f:source(CURRENT,f) for f in current_files if re.search(r'app/src/[^/]+/(java|kotlin)/.*\.kt$',f) and not f.split('/')[2].lower().startswith(('test','androidtest')) and '/build/' not in f}
index=collections.defaultdict(list)
for f,s in current_sources.items():
    for i,line in enumerate(s.splitlines(),1):
        for word in set(re.findall(r'\b[A-Za-z_]\w*\b',line)):
            index[word].append({'path':f,'line':i})
records=[]; candidates=[]; layout_count=0
for f in base_files:
    if not re.search(r'/res/layout[^/]*/[^/]+\.xml$',f): continue
    layout_count+=1; raw=source(BASELINE,f); root=ET.fromstring(raw)
    def walk(n,parents):
        a=attrs(n); tag=n.tag.split('.')[-1]; id_=a.get('android:id','').split('/')[-1]; style=a.get('style')
        if tag in TAGS or ((tag.endswith('TextView') or tag.endswith('ImageView')) and (re.search(r'(chip|button|btn|action)',id_,re.I) or a.get('android:clickable')=='true')):
            chain,merged=inherit(style); merged.update(a)
            references={k:values[v] for k,v in merged.items() if v in values}
            hits=[]
            for token in {id_,camel(id_)}-{''}:
                hits.extend(index.get(token,[]))
            hits=list({(h['path'],h['line']):h for h in hits}.values())
            record={'path':f,'line':raw[:raw.find('android:id="'+a.get('android:id','~')+'"')].count('\n')+1 if id_ else None,'tag':tag,'id':id_ or None,'style':style,'attributes':a,'local_style_chain':chain,'local_style_plus_layout_attributes':merged,'value_references':references,'ancestors':parents,'current_identifier_references':hits,'reference_warning':'Identifier matches are navigation aids, not a proven one-to-one renderer mapping.'}
            (records if tag in TAGS else candidates).append(record)
        for c in n: walk(c,parents+[{'tag':tag,'attributes':a}])
    walk(root,[])

# Every direct native component call (not imports); snippets are lexical source evidence.
native=[]; wrapper_calls=[]
pattern=re.compile(r'\b('+'|'.join(sorted(CALLS|WRAPPERS,key=len,reverse=True))+r')\s*\(')
for f,s in current_sources.items():
    if 'androidx.compose.' not in s: continue
    for m in pattern.finditer(s):
        prefix=s[max(0,m.start()-8):m.start()]
        if re.search(r'fun\s*$',prefix): continue
        start=m.end()-1; depth=0; quote=None; escaped=False; end=start
        for pos in range(start,len(s)):
            ch=s[pos]
            if quote:
                if escaped: escaped=False
                elif ch=='\\': escaped=True
                elif ch==quote: quote=None
                continue
            if ch in '\"\'': quote=ch; continue
            if ch=='(':depth+=1
            elif ch==')':
                depth-=1
                if depth==0: end=pos+1;break
        preceding=list(re.finditer(r'\bfun\s+(?:<[^>]+>\s*)?(?:[\w.]+\.)?([A-Za-z_]\w*)\s*\(',s[:m.start()]))
        rec={'path':f,'line':s[:m.start()].count('\n')+1,'component':m[1],'nearest_preceding_function':preceding[-1][1] if preceding else None,'call':s[m.start():end]}
        (native if m[1] in CALLS else wrapper_calls).append(rec)
programmatic=[]
template_inflations=[]
style_mutations=[]
for f in base_files:
    if not re.search(r'app/src/(main|full)/java/.*\.kt$',f):continue
    s=source(BASELINE,f)
    lines=s.splitlines()
    for i,line in enumerate(lines):
        if 'R.layout.item_chip_filter' in line:
            template_inflations.append({'path':f,'line':i+1,'source_context':'\n'.join(lines[max(0,i-4):i+35])})
        if re.search(r'\.(?:chip(?:BackgroundColor|StrokeColor|StrokeWidth|CornerRadius|MinHeight|StartPadding|EndPadding|IconSize)|setChip\w*|backgroundTintList|strokeColor|strokeWidth|cornerRadius)\s*(?:=|\()',line):
            style_mutations.append({'path':f,'line':i+1,'source_context':'\n'.join(lines[max(0,i-2):i+5])})
    for m in re.finditer(r'\b(Chip|MaterialButton|TextInputLayout|TextInputEditText|AppCompatEditText|EditText|Button|SearchView)\(',s):
        lines=s.splitlines(); line=s[:m.start()].count('\n')+1
        programmatic.append({'path':f,'line':line,'component':m[1],'source_context':'\n'.join(lines[max(0,line-5):line+65])})

resources=[]
for f in base_files:
    if re.search(r'/res/(color|drawable)[^/]*/[^/]+\.xml$',f) and re.search(r'(chip|button|segment|edittext|search_pill|rectangle_border_background)',f):
        resources.append({'path':f,'xml':source(BASELINE,f)})
result={'baseline':BASELINE,'native_snapshot':CURRENT,'scope':'All baseline source-set/qualifier layout resources; all current production source-set native Compose direct calls; baseline programmatic constructors. No runtime verification.', 'method':'Repo-local style inheritance is expanded; unresolved library/framework defaults are explicitly retained. All layout attrs and ancestors are preserved. Counts are source definitions/call sites, not simultaneous rendered instances.', 'layout_files_scanned':layout_count,'counts':{'baseline_component_tags':dict(sorted(collections.Counter(r['tag'] for r in records).items())),'baseline_action_like_text_image_candidates':len(candidates),'native_direct_component_calls':dict(sorted(collections.Counter(r['component'] for r in native).items())),'native_wrapper_calls':len(wrapper_calls),'baseline_programmatic_constructor_sites':len(programmatic)},'baseline_components':records,'baseline_action_like_text_image_candidates':candidates,'native_component_calls':native,'native_wrapper_calls':wrapper_calls,'baseline_programmatic_constructors':programmatic,'baseline_dynamic_chip_template_sites':template_inflations,'baseline_style_mutation_sites':style_mutations,'baseline_styles':dict(sorted(style_defs.items())),'baseline_component_drawables_and_selectors':resources,'baseline_color_and_dimen_values':values}
OUTPUT.write_text(json.dumps(result,indent=2,ensure_ascii=False)+'\n')
print(json.dumps({k:result[k] for k in ['baseline','native_snapshot','layout_files_scanned','counts']},indent=2));print(str(OUTPUT),OUTPUT.stat().st_size)
