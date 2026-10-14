#!/usr/bin/env python3
"""Pinned controller-body guard for reconstructed traffic logs; not a runtime or compilation test."""
import hashlib, importlib.util, json, pathlib, subprocess, sys
ROOT = pathlib.Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
spec = importlib.util.spec_from_file_location('wireguard_source_lexer', ROOT/'scripts/compose-migration/check-wireguard-controllers.py')
lexer = importlib.util.module_from_spec(spec); spec.loader.exec_module(lexer)
PREFIX = 'app/src/main/java/com/celzero/bravedns/'
CHECKS = {
'ui/activity/ConsoleLogActivity.kt': ['setQueryFilter','observeLog','matchesLocalFilter','applyLocalFilter','reapplyLocalFilter','setupClickListener','togglePause','handleShareLogs','shareZipFileViaEmail','makeConsoleLogFile','getVersionName','showFileCreationErrorToast','io','uiCtx','onQueryTextSubmit','onQueryTextChange','crashTun'],
'ui/activity/EventsActivity.kt': ['showSourceChipsIfNeeded','applySeverityFilter','applySourceFilter','setQueryFilter','onQueryTextSubmit','onQueryTextChange','showDeleteDialog','io','ui'],
'ui/activity/NetworkLogsActivity.kt': ['getCount','getFragment','getTabText','observeAppState','openConsoleLogActivity'],
'ui/fragment/ConnectionTrackerFragment.kt': ['applyParentFilter','applyChildFilter','unselectParentsChipsUi','showChildChipsIfNeeded','setQueryFilter','onQueryTextChange','onQueryTextSubmit','showDeleteDialog','io'],
'ui/fragment/DnsLogFragment.kt': ['setQueryFilter','onQueryTextChange','onQueryTextSubmit','showDnsLogsDeleteDialog','io'],
'ui/fragment/RethinkLogFragment.kt': ['setQueryFilter','onQueryTextChange','onQueryTextSubmit','showDeleteDialog','io'],
'ui/bottomsheet/ConnTrackerBottomSheet.kt': ['isInvalidProxyDetails','updateDnsIfAvailable','updateConnDetailsChip','updateBlockedRulesChip','updateAppDetails','displaySummaryDetails','lightenUpChip','handleNonApp','setupClickListeners','onAppRuleSelected','showAppRuleOptionsDialog','appRuleLabelIndex','updateFirewallRulesUi','renderRuleState','ipRuleSwitchState','updateIpRulesUi','firewallStatusText','openAppDetailActivity','cycleIpRule','cycleDomainRule','renderDomainRuleState','formatText','applyFirewallRule','applyIpRule','applyDomainRule','logEvent','applyFirewallRuleWithLifecycle'],
'ui/bottomsheet/DnsBlocklistBottomSheet.kt': ['getResponseIp','updateAppDetails','displayRecordTypeChip','setupClickListeners','cycleAppRule','cycleGlobalRule','nextDomainRuleStatus','applyAppRuleStatus','applyDnsRuleStatus','statusText','statusColor','domainRuleSwitchState','renderGlobalRuleState','renderAppRuleState','setupAppSpecificDomainRule','displayDnsTransactionDetails','handleFilterReasonChip','handleBlocklistChip','renderBlocklistRow','showBlocklistChip','startDomainConnectionsActivity','formatText','displayDescription','showResolvedState','showBlockedState','showResolverDetails','showHeaderFlag','hideHeaderFlag','displayFavIcon','lookupForImageDuckduckgo','logEvent','io','uiCtx'],
}
HELPERS = ['openBottomSheet','displayTransactionDetails','displayProtocolDetails','displayAppDetails','displayFirewallRulesetHint','displaySummaryDetails','isRoundTripShorter','containsRelayProxy','isConnectionProxied','isRpnProxy','isConnectionHeavier','isConnectionSlower']
for name in ['ConnectionLog','ConnectionTracker','RethinkLog']: CHECKS[f'adapter/{name}Adapter.kt'] = HELPERS
CHECKS['adapter/DnsLogAdapter.kt'] = ['openBottomSheet','displayLogEntryHint','determineMaybeBlocked','displayTransactionDetails','displayUnicodeIfNeeded','dnssecIndicatorRequired','isRoundTripShorter','displayAppDetails','displayIcon','displayDnsType','displayDuckduckgoFavIcon','showFavIcon','hideFavIcon','showFlag','hideFlag']

def normalize(source):
    # Only presentation type/lifetime renames and nullable presentation getter normalization.
    return (source.replace('MaterialAlertDialogBuilder', 'WgAlertDialogBuilder')
        .replace('RuleStateSwitch.SwitchState', 'LogRuleGlyph').replace('_binding', 'presentation')
        .replace('.text.isEmpty()', '.text.isNullOrEmpty()'))

def main():
    lexer.self_test()
    rows=[]; failed=[]
    for path,names in CHECKS.items():
        before=subprocess.check_output(['git','show',f'{BASE}:{PREFIX}{path}'],cwd=ROOT,text=True)
        after=(ROOT/PREFIX/path).read_text()
        old=lexer.bodies(normalize(before)); new=lexer.bodies(normalize(after))
        for name in names:
            passed = name in old and name in new and old[name] == new[name]
            rows.append({'path':PREFIX+path,'method':name,'passed':passed,'baselineSha256':lexer.digest(old.get(name,[])), 'currentSha256':lexer.digest(new.get(name,[]))})
            if not passed: failed.append(f'{path}::{name}')
    assert not (ROOT/PREFIX/'ui/bottomsheet/RethinkLogBottomSheet.kt').exists()
    for path in CHECKS:
        text=(ROOT/PREFIX/path).read_text()
        assert not any(token in text for token in ['Binding.inflate(', 'androidx.paging.PagingDataAdapter', 'import com.google.android.material.dialog.MaterialAlertDialogBuilder', 'AndroidView(']), path
    report={'baseline':BASE,'kind':'selected-controller-token-equality-with-explicit-presentation-renames','count':len(rows),'passed':not failed,'checks':rows,'limitations':'Source comparison only. No compilation, rendering, callback execution, lifecycle or live operations.'}
    (ROOT/'docs/compose-migration/traffic-logs-controller-evidence.json').write_text(json.dumps(report,indent=2)+'\n')
    if failed: print('FAIL: '+'\n'.join(failed)); return 1
    print(f'PASS: {len(rows)} pinned callback/helper bodies and native-rendering boundaries; not runtime parity.');return 0
if __name__=='__main__':sys.exit(main())
