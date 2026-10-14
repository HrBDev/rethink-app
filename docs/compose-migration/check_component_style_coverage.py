#!/usr/bin/env python3
"""Source-only restoration guard. Intentionally fails on unreviewed direct widgets.
No Gradle, Android runtime, dependency downloads, accounts, or network access.
"""
import argparse, collections, hashlib, json, pathlib, re, sys, tempfile
ROOT=pathlib.Path(__file__).resolve().parents[2]
DOC=ROOT/'docs/compose-migration'
SYMBOLS={'Button','OutlinedButton','TextButton','FilledTonalButton','ElevatedButton',
 'FilterChip','AssistChip','SuggestionChip','InputChip','TextField','OutlinedTextField',
 'BasicTextField','IconButton','IconToggleButton','FilledIconButton','OutlinedIconButton',
 'FilledIconToggleButton','OutlinedIconToggleButton','FilledTonalIconToggleButton',
 'FloatingActionButton','SmallFloatingActionButton','LargeFloatingActionButton','ExtendedFloatingActionButton','SegmentedButton'}
INTERNAL_FILES={
 'app/src/main/java/com/celzero/bravedns/ui/compose/controls/OriginalControls.kt',
 'app/src/main/java/com/celzero/bravedns/ui/compose/fields/OriginalTextFields.kt'}
# These two original-drawable field primitives predate the restoration and are separately
# audited by check-original-fields.py. This is not a blanket exemption for their files.
FIELD_PRIMITIVES={
 ('app/src/main/java/com/celzero/bravedns/ui/compose/networksettings/NetworkNativeUi.kt','NetworkEditField'),
 ('app/src/main/java/com/celzero/bravedns/ui/compose/utilities/ReachabilityScreen.kt','ProbeField')}
MANUAL=[
 {'id':'app-details-action-chips','path':'app/src/main/java/com/celzero/bravedns/ui/compose/appdetails/AppDetailsContent.kt','required_renderer':'OriginalButton','source_keys':['activity_app_details:','aad_close_conns_chip'],'reason':'Custom Surface formerly flattened all source Action chips into18dp positive48dp controls; negative Close All must remain distinct.'},
 {'id':'wireguard-passive-chips','path':'app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgRows.kt','required_renderer':'OriginalLabel','source_keys':[],'reason':'Custom WgChips Surface must retain per-source family and runtime stroke/fill rather than hardcoded border.'},
 {'id':'event-type-badge','path':'app/src/main/java/com/celzero/bravedns/ui/compose/trafficlogs/EventsScreen.kt','required_renderer':'OriginalLabel','source_keys':['item_event:event_type_chip'],'reason':'Source Assist Chip16dp24dp LabelSmall was custom Surface.'},
 {'id':'blockfree-type-badge','path':'app/src/main/java/com/celzero/bravedns/ui/compose/dns/BlockFreeDnsContent.kt','required_renderer':'OriginalLabel','source_keys':['list_item_block_free_dns:bfd_item_type_chip'],'reason':'Source6dp24dp10sp noninteractive Chip has no minimum-touch expansion.'},
 {'id':'country-speed-chip','path':'app/src/main/java/com/celzero/bravedns/ui/adapter/CountryServerAdapter.kt','required_renderer':'OriginalLabel','source_keys':['item_server_group:chip_link_speed'],'reason':'Source24dp11sp passive Chip collapsed into Text with arbitrary8dp background.'},
 {'id':'removed-server-badge','path':'app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnRemovalContent.kt','required_renderer':'OriginalLabel','source_keys':['item_removed_server:removal_badge'],'reason':'Baseline negative removal badge was entirely absent.'},
 {'id':'checkout-plans-control','path':'app/src/main/java/com/celzero/bravedns/ui/compose/purchase/CheckoutContent.kt','required_renderer':'OriginalLabel','source_keys':['activity_checkout_proxy:(anonymous)'],'reason':'Visible anonymous plans MaterialButton was entirely absent; preserve baseline passive/nonlistener behavior rather than inventing a click.'},
]

def mask_noncode(s):
 """Preserve offsets/newlines while masking comments and string/char bodies."""
 out=list(s);i=0
 def blank(a,b):
  for k in range(a,b):
   if out[k]!='\n':out[k]=' '
 while i<len(s):
  if s.startswith('//',i):
   end=s.find('\n',i);end=len(s) if end<0 else end;blank(i,end);i=end
  elif s.startswith('/*',i):
   start=i;depth=1;i+=2
   while i<len(s) and depth:
    if s.startswith('/*',i):depth+=1;i+=2
    elif s.startswith('*/',i):depth-=1;i+=2
    else:i+=1
   blank(start,i)
  elif s.startswith('"""',i):
   end=s.find('"""',i+3);end=len(s) if end<0 else end+3;blank(i,end);i=end
  elif s[i] in ['"',"'"]:
   q=s[i];start=i;i+=1
   while i<len(s):
    if s[i]=='\\':i+=2
    elif s[i]==q:i+=1;break
    else:i+=1
   blank(start,min(i,len(s)))
  else:i+=1
 return ''.join(out)

def scan(path,s):
 clean=mask_noncode(s)
 pattern=re.compile(r'\b('+'|'.join(sorted(SYMBOLS,key=len,reverse=True))+r')\s*\(')
 out=[]
 functions=list(re.finditer(r'\bfun\s+(?:<[^>]+>\s*)?(?:[\w.]+\.)?([A-Za-z_]\w*)\s*\(',clean))
 for m in pattern.finditer(clean):
  if re.search(r'\bfun\s*$',clean[max(0,m.start()-12):m.start()]):continue
  preceding=[f for f in functions if f.start()<m.start()]
  fn=preceding[-1][1] if preceding else None
  start=m.end()-1;depth=1;end=start+1
  while end<len(clean) and depth:
   if clean[end]=='(':depth+=1
   elif clean[end]==')':depth-=1
   end+=1
  trailing=end
  while trailing<len(clean) and clean[trailing].isspace():trailing+=1
  if trailing<len(clean) and clean[trailing]=='{':
   depth=1;end=trailing+1
   while end<len(clean) and depth:
    if clean[end]=='{':depth+=1
    elif clean[end]=='}':depth-=1
    end+=1
  invocation=s[m.start():end]
  out.append({'path':path,'line':s[:m.start()].count('\n')+1,'component':m[1],
   'nearest_function':fn,'call':invocation,'call_sha256':hashlib.sha256(invocation.encode()).hexdigest()})
 return out

def discover_production_sources(root):
 base=root/'app/src'
 if not base.exists():return []
 return sorted(p for source_set in base.iterdir()
  if source_set.is_dir() and not source_set.name.lower().startswith(('test','androidtest')) and source_set.name not in {'build','.gradle'}
  for p in source_set.rglob('*.kt') if not any(part in {'build','.gradle'} for part in p.relative_to(source_set).parts))

def unchanged_file_matches(path,source,entry):
 return (entry.get('path')==path and entry.get('baseline_revision')=='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
  and entry.get('baseline_sha256')==entry.get('current_sha256')==hashlib.sha256(source.encode()).hexdigest()
  and bool(entry.get('reason')))

def exception_matches(call,entry):
 return (entry.get('path')==call['path'] and entry.get('component')==call['component']
  and entry.get('source_contains','~') in call['call']
  and entry.get('call_sha256')==call['call_sha256']
  and bool(entry.get('baseline_evidence')) and bool(entry.get('source_instance')) and bool(entry.get('reason')))

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--report-only',action='store_true');ap.add_argument('--self-test',action='store_true');args=ap.parse_args()
 if args.self_test:
  fixture='// Button()\nval a="FilterChip()"\n/* Button() /* TextField() */ */\n@Composable fun Button() {}\nfun Content() { TextButton(onClick = {}) { Icon(Modifier.size(24.dp)) } }'
  got=scan('fixture',fixture);assert len(got)==1 and got[0]['component']=='TextButton',got
  e={**got[0],'source_contains':'TextButton','baseline_evidence':'fixture.xml:id attrs','source_instance':'fixture:id','reason':'Fixture only'}
  assert exception_matches(got[0],e)
  changed={**got[0],'call':got[0]['call'].replace('onClick','changedClick')}
  changed['call_sha256']=hashlib.sha256(changed['call'].encode()).hexdigest()
  assert not exception_matches(changed,e), 'Changed invocation must invalidate exception'
  visual={**got[0],'call':got[0]['call'].replace('24.dp','28.dp')}
  visual['call_sha256']=hashlib.sha256(visual['call'].encode()).hexdigest()
  assert '24.dp' in got[0]['call'], 'Trailing content must be included in exception hash'
  assert not exception_matches(visual,e), 'Changed trailing icon size must invalidate exception'
  assert not exception_matches({**got[0],'path':'other'},e), 'Exception cannot migrate files'
  with tempfile.TemporaryDirectory(prefix='component-source-set-test-') as temporary:
   root=pathlib.Path(temporary)
   for relative in ['app/src/futureBrand/java/Unexpected.kt','app/src/testFuture/java/Ignore.kt','app/src/androidTestTv/java/Ignore.kt','app/src/futureBrand/build/generated/Ignore.kt']:
    p=root/relative;p.parent.mkdir(parents=True,exist_ok=True);p.write_text('import androidx.compose.material3.Button\nfun Content() { Button(onClick = {}) {} }')
   discovered=discover_production_sources(root)
   assert [str(p.relative_to(root)) for p in discovered]==['app/src/futureBrand/java/Unexpected.kt'], discovered
   assert scan(str(discovered[0]),discovered[0].read_text())[0]['component']=='Button'
  unchanged={'path':'fixture','baseline_revision':'23cb6dd98805445174bd83b7237ad5f5d2c24ac9','baseline_sha256':hashlib.sha256(fixture.encode()).hexdigest(),'current_sha256':hashlib.sha256(fixture.encode()).hexdigest(),'reason':'Exact baseline fixture'}
  assert unchanged_file_matches('fixture',fixture,unchanged)
  assert not unchanged_file_matches('fixture',fixture+' ',unchanged), 'Changed baseline-exempt file must be rescanned'
  print('Scanner, invocation/content mutation, moved-file, future source-set and unchanged-file mutation self-tests passed')
 exceptions_path=DOC/'component-style-coverage-exceptions.json'
 exceptions=json.loads(exceptions_path.read_text()) if exceptions_path.exists() else []
 all_sources={str(p.relative_to(ROOT)):p.read_text() for p in discover_production_sources(ROOT)}
 unchanged_path=DOC/'component-style-unchanged-source-exemptions.json'
 unchanged_entries=json.loads(unchanged_path.read_text()) if unchanged_path.exists() else []
 unchanged_used=set();unchanged_allowed=[];invalid_unchanged=[]
 calls=[];allow=[];unused=set(range(len(exceptions)));exception_hits=collections.Counter()
 for path,s in all_sources.items():
  if 'androidx.compose.' not in s:continue
  unchanged=next((entry for entry in unchanged_entries if entry.get('path')==path),None)
  if unchanged is not None:
   unchanged_used.add(path)
   if unchanged_file_matches(path,s,unchanged):
    unchanged_allowed.append({**unchanged,'raw_calls':scan(path,s)});continue
   invalid_unchanged.append(path)
  for c in scan(path,s):
   if path in INTERNAL_FILES:
    c['allow_reason']='Dedicated source-style renderer internals';allow.append(c);continue
   if c['component']=='BasicTextField' and (path,c['nearest_function']) in FIELD_PRIMITIVES:
    c['allow_reason']='Original Android drawable field primitive; checked separately by check-original-fields.py';allow.append(c);continue
   accepted=False
   for i,e in enumerate(exceptions):
    if exception_matches(c,e):
     c['allow_reason']=e['reason'];c['baseline_evidence']=e['baseline_evidence'];allow.append(c);unused.discard(i);exception_hits[i]+=1;accepted=True;break
   if not accepted:calls.append(c)
 manual=[]
 for o in MANUAL:
  s=all_sources.get(o['path'],'')
  renderer=bool(re.search(r'\b'+o['required_renderer']+r'\s*\(',mask_noncode(s)))
  refs=all(k in s for k in o['source_keys'])
  manual.append({**o,'status':'renderer_and_source_reference_present_needs_visual_check' if renderer and refs else 'unresolved_custom_renderer_or_missing_source_key'})
 unresolved=[x for x in manual if x['status'].startswith('unresolved')]
 ledger_path=DOC/'button-chip-integration-ledger.json'
 ledger=json.loads(ledger_path.read_text()) if ledger_path.exists() else []
 ambiguous={i:n for i,n in exception_hits.items() if n!=1}
 stale_unchanged=[e.get('path') for e in unchanged_entries if e.get('path') not in unchanged_used]
 source_sets=sorted(p.name for p in (ROOT/'app/src').iterdir() if p.is_dir() and not p.name.lower().startswith(('test','androidtest')) and p.name not in {'build','.gradle'})
 report={'kind':'Current-source residual widget guard; not compilation, runtime or visual validation',
  'passed':not calls and not unresolved and not unused and not ambiguous and not invalid_unchanged and not stale_unchanged,
  'production_source_sets_discovered':source_sets,'production_files_by_source_set':dict(sorted(collections.Counter(path.split('/')[2] for path in all_sources).items())),
  'baseline_unchanged_file_exemptions':unchanged_allowed,'invalid_unchanged_file_exemptions':invalid_unchanged,'stale_unchanged_file_exemptions':stale_unchanged,
  'raw_unreviewed_calls':calls,'raw_unreviewed_counts':dict(sorted(collections.Counter(c['component'] for c in calls).items())),
  'allowed_primitive_calls':allow,'manual_obligations':manual,'unused_exception_indexes':sorted(unused),'ambiguous_exception_matches':ambiguous,
  'button_chip_ledger_entries':len(ledger),
  'known_legacy_unassigned_definition':{'source_key':'dialog_sponsor_info:dialog_sponsor_info_sponsor','status':'No baseline/current controller reference identified; not treated as integrated live UI or resurrected'},
  'limits':['Explicit source key/renderer references prove linkage only.','Per-instance state colors, geometry and callback semantics require source review and later approved runtime checks.','Icon/FAB calls remain unreviewed unless a specific baseline-backed exception is recorded.','The static legacy sponsor-info layout has no identified controller reference and is not resurrected.']}
 (DOC/'component-style-coverage-report.json').write_text(json.dumps(report,indent=2)+'\n')
 print(json.dumps({k:report[k] for k in ['passed','raw_unreviewed_counts','button_chip_ledger_entries']},indent=2))
 print('Manual obligations unresolved:',', '.join(x['id'] for x in unresolved) or 'none')
 return 0 if args.report_only or report['passed'] else 1
if __name__=='__main__':sys.exit(main())
