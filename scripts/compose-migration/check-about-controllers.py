#!/usr/bin/env python3
"""About/diagnostic source guards. Never starts Android, reads real diagnostics or exports files."""
from pathlib import Path
import importlib.util,json,subprocess,sys
ROOT=Path(__file__).resolve().parents[2]
spec=importlib.util.spec_from_file_location('wg_guard',Path(__file__).with_name('check-wireguard-controllers.py'))
g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g)
FILES={
 'ui/fragment/AboutFragment.kt':{
  'onDestroyView','initView','onSingleTapConfirmed','onDoubleTap','showStackTraceDialog','makeTabButton','makeLineRecyclerView',
  'onCreateViewHolder','onBindViewHolder','selectTab','openStatsDialog','onCreateViewHolder#2','onBindViewHolder#2',
  'showProcDialog','makeTabButton#2','makeScrollableSpannable','makeScrollableText','selectTab#2','openDatabaseDumpDialog',
  'showNewFeaturesDialog','showContributors'},
 'ui/bottomsheet/BugReportFilesBottomSheet.kt':{'onCreateView','initView','bind','onCreateViewHolder','onBindViewHolder'},
 'ui/bottomsheet/DnsRecordTypesBottomSheet.kt':{'onCreateView','initView','updateAutoModeUI','bind','onCreateViewHolder','onBindViewHolder'},
}
SUBSTITUTIONS={
 'ui/fragment/AboutFragment.kt':[('MaterialAlertDialogBuilder','AboutDialogBuilder')],
 'ui/bottomsheet/BugReportFilesBottomSheet.kt':[('MaterialAlertDialogBuilder','WgAlertDialogBuilder')],
 'ui/bottomsheet/DnsRecordTypesBottomSheet.kt':[('b.drbsAutoModeBtn.id','R.id.drbs_auto_mode_btn'),('b.drbsManualModeBtn.id','R.id.drbs_manual_mode_btn')],
}
results=[];errors=[]
for rel,excluded in FILES.items():
 path='app/src/main/java/com/celzero/bravedns/'+rel
 original=g.baseline(path)
 for a,b in SUBSTITUTIONS[rel]:original=original.replace(a,b)
 current=(ROOT/path).read_text();old=g.bodies(original);new=g.bodies(current)
 for name,tokens in old.items():
  if name in excluded:continue
  passed=new.get(name)==tokens
  results.append({'path':path,'method':name,'passed':passed,'adaptedBaselineSha256':g.digest(tokens),'currentSha256':g.digest(new.get(name,[]))})
  if not passed:errors.append(path+'::'+name)
 for forbidden in ['Binding.inflate(', 'android.widget.TextView(', 'android.widget.LinearLayout(', 'android.widget.ScrollView(',
                   'android.widget.ListView(', 'android.widget.Button(', 'android.widget.ProgressBar(', 'RecyclerView(', 'AndroidView(', 'setView(']:
  if forbidden in current:errors.append(path+' contains legacy presentation '+forbidden)
# The asynchronous collection section and the Proc formatting are not presentation rewrites.
p='app/src/main/java/com/celzero/bravedns/ui/fragment/AboutFragment.kt';old=g.baseline(p);new=(ROOT/p).read_text()
for label,start,end in [
 ('statistics collection and timeout', '            val (stats, timedOut) = try {','            dismissProgressAndShowResults('),
 ('Proc formatting and complete-copy data','        val colorHint = ContextCompat.getColor','        fun makeTabButton(text: String): android.widget.Button'),
]:
 oldpart=old[old.index(start):old.index(end,old.index(start))]
 if label.startswith('Proc'):
  newpart=new[new.index(start):new.index('        val metricsContent = buildString',new.index(start))]
 else:newpart=new[new.index(start):new.index(end,new.index(start))]
 passed=g.lex(oldpart)[0]==g.lex(newpart)[0]
 results.append({'path':p,'method':label,'passed':passed})
 if not passed:errors.append(label)
# Pure-content test boundary: no production host/service instantiation or side-effect calls.
for path in (ROOT/'app/src/test/java/com/celzero/bravedns/ui/compose/about').glob('*.kt'):
 txt=path.read_text()
 for bad in ['AboutFragment(', 'BugReportFilesBottomSheet(', 'DnsRecordTypesBottomSheet(', 'RethinkDnsApplication', 'VpnController.', 'File(', 'KernelProc.', 'GoVpnAdapter.', 'performMemoryProfileCapture(', 'createCombinedZip(', 'buildTableDump(']:
  if bad in txt:errors.append(str(path)+' unsafe test dependency '+bad)
 if 'application = Application::class' not in txt:errors.append(str(path)+' missing plain Application boundary')
g.self_test()
report={'baseline':g.BASELINE,'kind':'About/diagnostics token equality with explicit presentation substitutions','passed':not errors,'count':len(results),'methods':results,'presentationExclusions':{k:sorted(v) for k,v in FILES.items()}}
(ROOT/'docs/compose-migration/about-controller-source-report.json').write_text(json.dumps(report,indent=2)+'\n')
if errors:print('\n'.join(errors));sys.exit(1)
print('PASS',len(results),'About/diagnostic protected bodies and collection/format sections; no runtime/compilation claim')
