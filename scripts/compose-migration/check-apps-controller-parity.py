#!/usr/bin/env python3
"""Pinned-body equality for UI-host command boundaries, not runtime invocation proof."""
import subprocess,re,json,hashlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
PREFIX='app/src/main/java/com/celzero/bravedns/'
SELECTED={
'ui/activity/AppInfoActivity.kt':['shouldShowRethinkWarning','fetchProxyNames','openCustomIpScreen','openCustomDomainScreen','openDetailedStatsUi','openDetailedBlocklistsUi','openBlockedDomainsUi','updateExcludeProxyStatus','updateTempAllowStatus','openAppWiseDomainLogsActivity','openAppWiseIpLogsActivity','toggleMobileData','toggleWifi','updateFirewallStatus','completeFirewallChanges','getFirewallText','appName','guardAppInfoInitialized','logEvent','saveNotesToDatabase'],
'ui/bottomsheet/AppIpRulesBottomSheet.kt':['applyIpRule','showWgListBtmSheet','onDismiss','logEvent','onDismissWg'],
'ui/bottomsheet/AppDomainRulesBottomSheet.kt':['applyDomainRule','showWgListBtmSheet','onDismiss','logEvent','onDismissWg'],
'ui/activity/AppListActivity.kt':['initObserver','onQueryTextSubmit','onQueryTextChange','setQueryFilter','addQueryToFilters','getBulkActionDialogTitle','getBulkActionDialogMessage','updateBulkRules','applyFirewallFilter','resetFirewallIcons','updateMeteredBulk','updateUnmeteredBulk','updateBypassBulk','updateBypassDnsFirewallBulk','updateExcludedBulk','updateLockdownBulk','refreshDatabase','logEvent'],
'adapter/FirewallAppListAdapter.kt':['toggleMobileData','toggleWifi','openAppDetailActivity','logEvent','uiCtx','io','ioCtx'],
'ui/activity/AppWiseIpLogsActivity.kt':['onQueryTextSubmit','onQueryTextChange','deleteAppLogs','appName'],
'ui/activity/AppWiseDomainLogsActivity.kt':['onQueryTextSubmit','onQueryTextChange','deleteAppLogs','appName'],
'ui/bottomsheet/FirewallAppFilterBottomSheet.kt':['applyParentFilter','applyChildFilter','initClickListeners'],
'adapter/DomainRulesBtmSheetAdapter.kt':['applyDomainRule'],
}
def method(source,name):
 match=re.search(r'fun '+name+r'\(',source);assert match,name
 opening=source.index('{',match.end());end=opening+1;depth=1
 while depth:
  depth+=(source[end]=='{')-(source[end]=='}');end+=1
 return source[match.start():end]
rows=[]
for relative,names in SELECTED.items():
 path=PREFIX+relative
 original=subprocess.check_output(['git','show',BASE+':'+path],cwd=ROOT,text=True)
 current=(ROOT/path).read_text()
 for name in names:
  expected=method(original,name);actual=method(current,name)
  rows.append({'file':path,'method':name,'equal':expected==actual,'baseline_sha256':hashlib.sha256(expected.encode()).hexdigest(),'current_sha256':hashlib.sha256(actual.encode()).hexdigest()})
report={'base':BASE,'source_only':True,'methods':rows,'pass':all(x['equal'] for x in rows)}
(ROOT/'docs/compose-migration/apps-controller-source-report.json').write_text(json.dumps(report,indent=2)+'\n')
failed=[x for x in rows if not x['equal']]
for row in failed:print('FAIL',row['file'],row['method'])
assert not failed
print(f'PASS: {len(rows)} complete protected command methods byte-identical to pinned baseline. No invocation/runtime claim.')
