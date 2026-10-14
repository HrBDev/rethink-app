#!/usr/bin/env python3
"""Baseline statistics controller guard. Source equivalence is not Android/runtime/visual parity."""
import argparse,hashlib,importlib.util,json,re,subprocess
from pathlib import Path
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
ROOT=Path(__file__).resolve().parents[2]
PREFIX='app/src/main/java/com/celzero/bravedns/'
OWNED=['ui/fragment/SummaryStatisticsFragment.kt','ui/activity/DetailedStatisticsActivity.kt','ui/activity/DomainConnectionsActivity.kt','adapter/SummaryStatisticsAdapter.kt','adapter/DomainConnectionsAdapter.kt','adapter/BlocklistAppsAdapter.kt','ui/bottomsheet/SummaryStatsFilterBottomSheet.kt','ui/bottomsheet/RpnLogActivityIntervalBottomSheet.kt','ui/bottomsheet/RpnStatsBottomSheet.kt']
METHODS={
 'adapter/SummaryStatisticsAdapter.kt':['keyOf','calculatePercentage','progressValue','setTimeCategory','notifyDataset','isUnknownApp','startDomainConnectionsActivity','startAppInfoActivity','showDomainRulesBottomSheet','showDnsLogs','showNetworkLogs','handleVpnState','startActivity','io','uiCtx'],
 'ui/activity/DetailedStatisticsActivity.kt':['setSubTitle','handleStatType'],
 'ui/activity/DomainConnectionsActivity.kt':['setSubTitle'],
 'ui/bottomsheet/RpnLogActivityIntervalBottomSheet.kt':['load','onAppExpandRequested','render','showTimeRange','setDolphinSignature'],
 'ui/bottomsheet/RpnStatsBottomSheet.kt':['loadStats','openConnectionLogs','applyStats','showLoadingState','showContentState','showErrorState','setDolphinSignature'],
 'ui/fragment/SummaryStatisticsFragment.kt':['logEvent','observeAppStart'],
}
def original(path):return subprocess.check_output(['git','show',f'{BASE}:{PREFIX}{path}'],cwd=ROOT,text=True)
def block(source,marker,start=0):
 a=source.index(marker,start);o=source.index('{',a);d=1;e=o+1
 while d:d+=(source[e]=='{')-(source[e]=='}');e+=1
 return source[a:e]
def function(source,name):return block(source,'fun '+name+'(')
def normalize(v):
 for old,new in [('b.dsaTitle.text','screenTitle'),('b.dsaSubtitle.text','subtitle'),('b.dcSubtitle.text','subtitle'),('b.dsaSubtitle.visibility = View.GONE','subtitle = null')]:v=v.replace(old,new)
 v=v.replace('            b.rpnStatsTopApps.layoutManager = LinearLayoutManager(requireContext())\n','').replace('            b.rpnStatsTopApps.itemAnimator = null\n','')
 return v

def check(current):
 records=[]
 for path,names in METHODS.items():
  before=original(path);after=current[path]
  for name in names:
   assert normalize(function(before,name))==normalize(function(after,name)),f'Changed command {path}:{name}'
   records.append({'path':PREFIX+path,'method':name,'baseline_equivalent':True})
 for path,source in current.items():
  for marker in ['viewBinding(', 'Binding.inflate', 'AndroidView(', 'RecyclerView.Adapter<', 'PagingDataAdapter<','MaterialAlertDialogBuilder(']:assert marker not in source,f'Legacy renderer {path}: {marker}'
 before=original('adapter/SummaryStatisticsAdapter.kt');after=current['adapter/SummaryStatisticsAdapter.kt']
 assert block(before,'when (type)',before.index('private fun setupClickListeners'))==block(after,'when (type)',after.index('private fun onRowClick'))
 before=original('ui/activity/DomainConnectionsActivity.kt');after=current['ui/activity/DomainConnectionsActivity.kt']
 assert before[before.index('        val t = intent.getIntExtra'):before.index('        setRecyclerView(timeCategory)')].replace('b.dcTitle.text','screenTitle')==after[after.index('        val t = intent.getIntExtra'):after.index('        setRecyclerView(timeCategory)')]
 before=original('ui/activity/DetailedStatisticsActivity.kt');after=current['ui/activity/DetailedStatisticsActivity.kt']
 assert before[before.index('        val type ='):before.index('        setSubTitle(statType')]==after[after.index('        val type ='):after.index('        setSubTitle(statType')]
 for path in ['ui/fragment/SummaryStatisticsFragment.kt','ui/activity/DomainConnectionsActivity.kt','ui/bottomsheet/SummaryStatsFilterBottomSheet.kt']:
  assert block(original(path),'enum class ')==block(current[path],'enum class '),path+' enum IDs'
 summary=current['ui/fragment/SummaryStatisticsFragment.kt']
 for query in ['getTopActiveConns','getAllowedAppNetworkActivity','getBlockedAppNetworkActivity','getMostConnectedASN','getMostBlockedASN','mcd','mbd','getMostContactedIps','getMostBlockedIps','getMostContactedCountries','mostBlockedBlocklists']:
  assert len(re.findall(r'viewModel\.'+query+r'\b',summary))==1,query
 assert summary.index('viewModel.setLoadMoreClicked(true)')<summary.index('viewModel.mcd')
 for token in ['VpnController.closeConnectionsIfNeeded(Constants.UID_EVERYBODY, "summ-stats-manual-close")','logEvent("close connections", "Closed active connections for all apps from stats screen")']:
  assert summary.count(token)==1,token
 for token in ['adapter.removeSnapshotListener(snapshot)','adapter.removeLoadStateListener(loads)','adaptersByType.values.forEach { it.setTimeCategory(timeCategory) }']:assert token in summary
 for path in ['ui/stats/StatsInsightsMath.kt','ui/stats/CountryInsightsMapper.kt','util/WorldMapPaths.kt','service/LogActivityWindow.kt']:
  assert original(path)==(ROOT/(PREFIX+path)).read_text(),f'Protected math/window/map changed {path}'
 filter=current['ui/bottomsheet/SummaryStatsFilterBottomSheet.kt']
 assert 'getIntArray(SAVED_CHECKED)' in filter and 'putIntArray(SAVED_CHECKED' in filter
 model=(ROOT/(PREFIX+'ui/compose/statistics/SummaryFilterSelection.kt')).read_text()
 assert 'SectionFilter.entries.forEach { setChecked(it, it in target, notify) }' in model
 assert 'if (next.isEmpty()) return' in model
 assert 'SectionFilter.entries.forEach { setChecked(it, true, true) }' in model
 test_path='app/src/test/java/com/celzero/bravedns/ui/fragment/SummaryStatisticsFragmentTest.kt'
 spec=importlib.util.spec_from_file_location('original_test_guard',Path(__file__).with_name('check-original-test-relocation.py'))
 tests=importlib.util.module_from_spec(spec);spec.loader.exec_module(tests)
 obligation=tests.verify_file(test_path.replace('/test/','/androidTest/'),test_path,{})
 assert not obligation['missing_original_method_names'], 'Statistics original @Test obligation missing'
 test=(ROOT/test_path).read_text()
 assert not re.search(r'@(?:org\.junit\.)?Ignore\b',tests.mask(test)), 'Statistics host obligation ignored'
 assert 'root_nav_${R.id.summaryStatisticsFragment}' in test and 'stats_time_0' in test
 return records

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--self-test',action='store_true');p.add_argument('--report',type=Path);a=p.parse_args()
 current={path:(ROOT/(PREFIX+path)).read_text() for path in OWNED};records=check(current)
 if a.self_test:
  mutations=[('ui/bottomsheet/RpnLogActivityIntervalBottomSheet.kt','w.startMs,\n                    w.endMs,','w.endMs,\n                    w.startMs,'),('adapter/SummaryStatisticsAdapter.kt','intent.putExtra(AppInfoActivity.INTENT_UID, appConnection.uid)','intent.putExtra(AppInfoActivity.INTENT_UID, 0)')]
  for path,old,new in mutations:
   broken=dict(current);assert old in broken[path];broken[path]=broken[path].replace(old,new)
   try:check(broken)
   except AssertionError:pass
   else:raise AssertionError('negative control passed')
 result={'baseline':BASE,'status':'PASS','source_only':True,'methods':records,'sha256':{PREFIX+k:hashlib.sha256(v.encode()).hexdigest() for k,v in current.items()}}
 if a.report:a.report.write_text(json.dumps(result,indent=2)+'\n')
 print(f'PASS: {len(records)} baseline-equivalent methods, exact route/query contracts, six-chip multi-select restoration, native renderer boundary and relocated original @Test obligation.')
 print('Not Android typechecking, test execution or visual/runtime parity.')
if __name__=='__main__':main()
