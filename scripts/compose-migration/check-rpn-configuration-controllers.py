#!/usr/bin/env python3
"""Pinned controller token equality. No build, rendering or runtime equivalence is inferred."""
import argparse, hashlib, importlib.util, json, pathlib, subprocess
ROOT = pathlib.Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
PREFIX = 'app/src/main/java/com/celzero/bravedns/'
spec = importlib.util.spec_from_file_location('wg_guard', pathlib.Path(__file__).with_name('check-wireguard-controllers.py'))
g = importlib.util.module_from_spec(spec); spec.loader.exec_module(g)
METHODS = {
 'ui/activity/RpnConfigDetailActivity.kt': ['onPermissionsGranted','onPermissionsDenied','onPermissionsRationale','setDolphinSignature','onResume','onPause','init','resolveClientIps','serverEndpoint','stripPort','buildHopIpSpan','buildIpDetailSpan','cancelStatsJob','startStatsPolling','fetchAndApplyStats','applyStats','buildStatusText','buildStatusColor','isFailing','shrinkEmoji','buildLoadSpeedText','observeAppCount','loadConfigSettings','setupClickListeners','applyHop','setHopCheckSilently','applyMobileOnly','setMobileCheckSilently','setSsidCheckSilently','initiateRefresh','showInvalidConfigDialog','showRecoveryDialog','openHopDialog','openLogsDialog','invokeNetworkLogs','openAppsDialog','onIndividualAppModified','collapsedHeaderTitle','setupSsidSection','setupSsidErrorActionListeners','updateErrorLayouts','hideErrorLayouts','showPermissionDeniedDialog','refreshSsidSection','openSsidDialog','showLocationDisclosureDialog','showLocationEnableDialog','onRequestPermissionsResult','io','ui','uiCtx'],
 'ui/activity/RpnBypassAppsActivity.kt': ['onDestroy','loadApps','appTypeRank','categoryRank','applyFilter','updateCountLabel','syncBulkToggleState','confirmBulkAction','performBulkAction','setBulkControlsEnabled','onAppToggled','onQueryTextSubmit','onQueryTextChange','io','uiCtx'],
 'ui/activity/RpnWinProxyDetailsActivity.kt': ['initViews','initClickListeners','showNoProxyFoundDialog','io','uiCtx'],
 'ui/dialog/RpnSsidDialog.kt': ['saveSsids'],
 'ui/dialog/RpnProxyHopDialog.kt': ['create'],
}
ADAPTED = {
 ('ui/activity/RpnConfigDetailActivity.kt','populateHeroBanner'): [('b.collapsingToolbar.title','b.headerTitle')],
 ('ui/activity/RpnConfigDetailActivity.kt','showServerInfo'): [('b.collapsingToolbar.title','b.headerTitle')],
 ('ui/activity/RpnConfigDetailActivity.kt','setupSsidSectionUI'): [(': LinearLayout',': WgControlState'),(': AppCompatImageView',': WgControlState'),('b.root','composeRoot')],
 ('ui/activity/RpnConfigDetailActivity.kt','applyClientIps'): [('b.shimmerIpv4.stopShimmer() b.shimmerIpv4.visibility = View.GONE','b.clientIpShimmer = false')],
 ('ui/activity/RpnConfigDetailActivity.kt','showClientIpShimmer'): [('b.shimmerIpv4.visibility = View.VISIBLE b.shimmerIpv4.startShimmer()','b.clientIpShimmer = true')],
 ('ui/activity/RpnBypassAppsActivity.kt','setupClickListeners'): [('b.rpnBypassSearchView.setOnQueryTextListener(this) b.rpnBypassSearchView.setOnCloseListener { searchText = "" applyFilter() false }',''),('b.rpnBypassRefreshList.animation = animation b.rpnBypassRefreshList.startAnimation(animation)','b.startRefresh()'),('b.rpnBypassRefreshList.clearAnimation()','b.refreshSpinning = false')],
 ('ui/dialog/RpnSsidDialog.kt','loadCurrentSsids'): [('ssidAdapter.notifyDataSetChanged()',''),('updateEmptyState()','')],
 ('ui/dialog/RpnSsidDialog.kt','showDeleteConfirmation'): [('ssidAdapter.notifyItemRemoved(position)',''),('updateEmptyState()','')],
}
def substitute(tokens, source, replacement):
    source, replacement = g.lex(source)[0], g.lex(replacement)[0]
    result=[];i=0;count=0
    while i < len(tokens):
        if tokens[i:i+len(source)] == source:result += replacement;i+=len(source);count+=1
        else:result.append(tokens[i]);i+=1
    if not count:raise ValueError('Expected substitution absent: '+repr(source))
    return result

def check():
    files=set(METHODS)|{p for p,n in ADAPTED}
    originals={p:g.bodies(subprocess.check_output(['git','show',BASE+':'+PREFIX+p],cwd=ROOT,text=True)) for p in files}
    current={p:g.bodies((ROOT/PREFIX/p).read_text()) for p in files}
    records=[];errors=[]
    for p,names in METHODS.items():
        for n in names:
            old=originals[p].get(n);new=current[p].get(n);passed=old is not None and old == new
            records.append({'path':PREFIX+p,'method':n,'exact':True,'passed':passed,'baselineHash':g.digest(old or []),'currentHash':g.digest(new or [])})
            if not passed:errors.append(p+'::'+n)
    for (p,n),changes in ADAPTED.items():
        old=originals[p].get(n);new=current[p].get(n)
        try:
            if old is None:raise ValueError('Missing baseline method')
            for source,replacement in changes:old=substitute(old,source,replacement)
            passed=old==new
        except ValueError as e:passed=False;errors.append(str(e))
        records.append({'path':PREFIX+p,'method':n,'exact':False,'substitutions':changes,'passed':passed,'adaptedHash':g.digest(old or []),'currentHash':g.digest(new or [])})
        if not passed:errors.append(p+'::'+n)
    for p in ['ui/dialog/RpnProxyHopDialog.kt','ui/dialog/WgHopDialog.kt']:
        if subprocess.check_output(['git','show',BASE+':'+PREFIX+p],cwd=ROOT) != (ROOT/PREFIX/p).read_bytes():errors.append('Hop wrapper changed: '+p)
    for retired in ['SsidAdapter','ServerWgPeersAdapter']:
        for p in (ROOT/'app/src').rglob('*.kt'):
            if retired in p.read_text():errors.append('Retired renderer remains referenced: '+str(p))
    return {'baseline':BASE,'passed':not errors,'exactBodies':sum(r['exact'] for r in records),'adaptedBodies':sum(not r['exact'] for r in records),'errors':errors,'methods':records,'limitation':'Static literal-preserving selected-token equality; not Android compilation, runtime/dispatch/lifecycle or rendering parity.'}
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--self-test',action='store_true');p.add_argument('--report',type=pathlib.Path);a=p.parse_args()
    if a.self_test:
        g.self_test();assert substitute(g.lex('{ b.header.title = "A  B"; save(1) }')[0],'b.header.title','title') == g.lex('{ title = "A  B"; save(1) }')[0]
        assert substitute(g.lex('{ b.header.title = "A  B"; save(1) }')[0],'b.header.title','title') != g.lex('{ title = "A B"; save(2) }')[0]
    result=check()
    if a.report:a.report.write_text(json.dumps(result,indent=2)+'\n')
    print(('PASS' if result['passed'] else 'FAIL')+f": {result['exactBodies']} exact and {result['adaptedBodies']} narrowly adapted controller bodies; hop wrappers retained; retired renderers unreferenced")
    for e in result['errors']:print(e)
    raise SystemExit(not result['passed'])
