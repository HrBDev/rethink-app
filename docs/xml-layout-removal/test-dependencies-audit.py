#!/usr/bin/env python3
"""Read-only source census; emits documentation JSON only. Never runs Gradle/Android."""
from pathlib import Path
from collections import defaultdict, Counter
import hashlib, importlib.util, json, re, subprocess, sys
sys.dont_write_bytecode = True
ROOT=Path(__file__).resolve().parents[2]
spec=importlib.util.spec_from_file_location('controller_lex',ROOT/'scripts/compose-migration/check-wireguard-controllers.py')
lexer=importlib.util.module_from_spec(spec);spec.loader.exec_module(lexer)
BASELINE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
FROZEN='803cd4c884243ab9ef9c92cb0c4959d63bc27b00'
def relative(p):return p.relative_to(ROOT).as_posix()
def loc(p,s,m):return {'path':relative(p),'line':s.count('\n',0,m.start())+1,'reference':m[0]}
def source_set(p):return p.relative_to(ROOT/'app/src').parts[0]
def test(p):return source_set(p).lower().startswith(('test','androidtest'))
def layout(p):return '/res/' in relative(p) and p.parent.name.startswith('layout') and p.suffix=='.xml'
layouts=sorted(p for p in (ROOT/'app/src').rglob('*.xml') if layout(p))
defs_layout=defaultdict(list);defs_other=defaultdict(list);xml_refs=defaultdict(list)
for p in sorted((ROOT/'app/src').rglob('*.xml')):
 s=p.read_text()
 for m in re.finditer(r'@\+id/(\w+)',s):(defs_layout if layout(p) else defs_other)[m[1]].append(loc(p,s,m))
 for m in re.finditer(r'<item\b[^>]*\bname="(\w+)"[^>]*\btype="id"|<item\b[^>]*\btype="id"[^>]*\bname="(\w+)"',s):
  (defs_layout if layout(p) else defs_other)[m[1] or m[2]].append(loc(p,s,m))
 if not layout(p):
  for m in re.finditer(r'(?<!\w)@id/(\w+)',s):xml_refs[m[1]].append(loc(p,s,m))
refs=defaultdict(list);layout_refs=[];all_source_hashes={};id_aliases=[];literal_resource_refs=[]
for p in sorted((ROOT/'app/src').rglob('*')):
 if p.suffix not in ('.kt','.java') or not p.is_file():continue
 s=p.read_text();tokens,masked=lexer.lex(s);all_source_hashes[relative(p)]=hashlib.sha256(p.read_bytes()).hexdigest()
 # Own app R is imported unaliased in this tree. Fully-qualified app R supported as well.
 aliases=['R'] if re.search(r'import\s+com\.celzero\.bravedns\.R(?:\s|;|$)',masked) else []
 aliases+=re.findall(r'import\s+com\.celzero\.bravedns\.R\s+as\s+(\w+)',masked)
 for alias in set(aliases):
  for kind in ('id','layout'):
   for m in re.finditer(r'(?<![\w.])'+re.escape(alias)+r'\.'+kind+r'\.(\w+)',masked):
    record=loc(p,s,m)|{'source_set':source_set(p),'test':test(p)}
    if kind=='id':refs[m[1]].append(record)
    else:layout_refs.append(record|{'layout':m[1]})
 for m in re.finditer(r'com\.celzero\.bravedns\.R\.(id|layout)\.(\w+)',masked):
  record=loc(p,s,m)|{'source_set':source_set(p),'test':test(p)}
  if m[1]=='id':refs[m[2]].append(record)
  else:layout_refs.append(record|{'layout':m[2]})
 # Resource references in Kotlin interpolation are executable despite surrounding literals.
 for m in re.finditer(r'\$\{[^}]*?((?:R)\.(id|layout)\.(\w+))[^}]*\}',s):
  if not aliases or masked[m.start():m.end()].strip():continue
  record={'path':relative(p),'line':s.count('\n',0,m.start(1))+1,'reference':m[1],'source_set':source_set(p),'test':test(p),'in_kotlin_string_interpolation':True}
  if m[2]=='id':refs[m[3]].append(record)
  else:layout_refs.append(record|{'layout':m[3]})
 for m in re.finditer(r'import\s+com\.celzero\.bravedns\.R\.(?:id|layout)\.',masked):id_aliases.append(loc(p,s,m))
 for token in tokens:
  if token.startswith(('"',"'")) and re.search(r'R\.(?:layout|id)\.',token):literal_resource_refs.append({'path':relative(p),'literal':token})
lost=set(defs_layout)-set(defs_other)
used_lost=sorted(lost & (set(refs)|set(xml_refs)))
orphans=[]
for name in used_lost:
 r=refs.get(name,[])
 orphans.append({'name':name,'definitions_removed':defs_layout[name],'production_references':[x for x in r if not x['test']],'test_references':[x for x in r if x['test']],'non_layout_xml_references':xml_refs.get(name,[])})
manifest=json.loads((ROOT/'docs/compose-migration/binding-removal-safety-allowlist.json').read_text())
result={
 'kind':'source_only_layout_deletion_dependency_inventory_not_compilation',
 'baseline':BASELINE,'frozen_snapshot':FROZEN,'git_head':subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),
 'counts':{'remaining_layout_files':len(layouts),'unique_layout_names':len({p.stem for p in layouts}),'layout_declared_ids':len(defs_layout),'layout_only_declared_ids':len(lost),'layout_only_referenced_ids':len(used_lost),'layout_only_ids_referenced_in_production':sum(bool(x['production_references']) for x in orphans),'layout_only_ids_referenced_in_tests':sum(bool(x['test_references']) for x in orphans),'layout_only_ids_referenced_only_in_tests':sum(bool(x['test_references']) and not x['production_references'] and not x['non_layout_xml_references'] for x in orphans),'layout_only_ids_referenced_in_other_xml':sum(bool(x['non_layout_xml_references']) for x in orphans),'direct_own_layout_calls':len(layout_refs),'direct_layout_files':len({x['path'] for x in layout_refs}),'direct_layout_resource_names':len({x['layout'] for x in layout_refs})},
 'source_sets':dict(Counter(source_set(p) for p in sorted((ROOT/'app/src').rglob('*')) if p.suffix in ('.kt','.java') and p.is_file())),
 'layouts':[{'path':relative(p),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in layouts],
 'direct_own_layout_references':layout_refs,
 'layout_only_referenced_ids':orphans,
 'layout_only_declared_but_unreferenced_ids':sorted(lost-set(used_lost)),
 'static_imports_requiring_manual_review':id_aliases,
 'resource_reference_literals_requiring_manual_review':literal_resource_refs,
 'safety_fence':{k:manifest[k] for k in ('execution_status','runtime_clearance','source_tree_pin','allowed_class_count','declared_test_method_count')},
 'limitations':['Static own-app reference census, not AAPT/Kotlin compilation. Comments and string literals are masked; simple Kotlin interpolation references are included and all resource-looking literals are separately reported for manual review.','ID declarations are evaluated across current app resource source sets; effective Full/TV source-set merging must still be compiled when authorized.','No resources, production code, tests, existing guards or safety pins were changed; only this documentation script and report are written.']
}
out=Path(__file__).with_name('test-dependencies-inventory.json');out.write_text(json.dumps(result,indent=2)+'\n')
Path(__file__).with_name('test-dependencies-preserve-all-ids.txt').write_text('\n'.join(sorted(defs_layout))+'\n')
Path(__file__).with_name('test-dependencies-preserve-production-ids.txt').write_text('\n'.join(x['name'] for x in orphans if x['production_references'])+'\n')
print(json.dumps(result['counts'],indent=2));print('Output:',relative(out));print('Resource-looking literals:',len(literal_resource_refs));print('Static imports:',len(id_aliases))
for x in orphans:
 print(x['name'],len(x['production_references']),len(x['test_references']),len(x['non_layout_xml_references']))
