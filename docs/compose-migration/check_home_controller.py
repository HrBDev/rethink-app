#!/usr/bin/env python3
"""Fail closed when protected Home controller bodies differ from the pinned baseline.

This is a source-equivalence guard, not a substitute for runtime/UI testing. It
never imports the production Application, loads gojni, or starts a VPN service.
"""
import argparse
import json
from pathlib import Path
import re
import subprocess
import sys

BASELINE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
SOURCE = 'app/src/main/java/com/celzero/bravedns/ui/fragment/HomeScreenFragment.kt'
ROOT = Path(__file__).resolve().parents[2]
PROTECTED = '''onAttach initializeValues initializeClickListeners openRethinkAppInfoIfNeeded
openRpnDashboardScreen openRpnServerSelection logEvent handlePause openPauseActivity
updateCardsUi observeVpnState enableFirewallCardIfNeeded enableAppsCardIfNeeded
enableDnsCardIfNeeded enableProxyCardIfNeeded enableLogsCardIfNeeded observeProxyStates
startProxyPollingForStatus startDnsStatePolling updateUiWithDnsStatusPolled getConnectedDnsId
startProxyStatePolling updateUiWithProxyStates showRpnStopped getProxyStatus isSplitDns
isDnsError unobserveProxyStates isStoppedSlotVisible isRpnStoppedEarly toggleLogsView
updateLogsDotsVisibility showLogsDotsTemporarily pagerPageCount openNetworkLogs
refreshTopAppsHistogram loadTopAppsEntries isUnknownAppName iconForUid showTopAppsEntry
formatBytesCompact observeLogActivity renderLogsHourAxisLabels renderLogsHeaderCount
tappedInterval openIntervalDetails observeDnsStates renderDnsHeadline updateUiWithDnsStates
withBlocklistCountSuffix observeLogsCount formatDecimal unobserveDnsStates
observeUniversalStates observeCustomRulesCount updateTotalRuleCount updateRulesPageCounts
openRulesScreen compactRuleCount unObserveCustomRulesCount unobserveUniversalStates
observeAppStates unobserveAppStates handleMainScreenBtnClickEvent handleVpnActivation
batteryOptimizationActive isRestrictBackgroundActive openBottomSheet handleAlwaysOnVpn
onResume handleRethinkAppStatus startTrafficStats displayProtos updateActiveSinceUi
displayTrafficStatsBW stopTrafficStats displayTrafficStatsRate getCommonUnit
convertToCommonUnit maybeAutoStartVpn triggerBugReport handleLockdownModeIfNeeded
handleShimmer onPause onDestroyView startDnsActivity canStartRethinkActivity
startAppsActivity startActivity prepareAndStartVpn proceedWithVpnStart stopShimmer
startShimmer stopVpnService startVpnService getNotificationPermissionIfNeeded
shouldShowRethinkWarning prepareVpnService hasLocalNetworkPermission
requestLocalNetworkPermission handleLocalNetworkPermissionDenied syncDnsStatus
isAnotherVpnActive fetchTextColor isLightTheme isDarkThemeOn io uiCtx ui'''.split()
DIALOGS = '''showBatteryOptimizationDialog showAlwaysOnStopDialog showAlwaysOnDisableDialog
showPrivateDnsDialog showFirstTimeVpnDialog showLocalNetworkPermissionDialog'''.split()


def mask_literals(source):
    """Mask strings/comments while retaining positions and source whitespace."""
    chars = list(source)
    i = 0
    while i < len(source):
        end = None
        if source.startswith('//', i):
            end = source.find('\n', i)
            if end < 0: end = len(source)
        elif source.startswith('/*', i):
            nesting, end = 1, i + 2
            while nesting and end < len(source):
                if source.startswith('/*', end): nesting += 1; end += 2
                elif source.startswith('*/', end): nesting -= 1; end += 2
                else: end += 1
        elif source.startswith('"""', i):
            end = source.find('"""', i + 3)
            end = len(source) if end < 0 else end + 3
        elif source[i] in '\"\'':
            quote, end = source[i], i + 1
            while end < len(source):
                if source[end] == '\\': end += 2
                elif source[end] == quote: end += 1; break
                else: end += 1
        if end is not None:
            for j in range(i, end):
                if chars[j] != '\n': chars[j] = ' '
            i = end
        else: i += 1
    return ''.join(chars)


def body(source, name):
    masked = mask_literals(source)
    match = re.search(r'\bfun\s+' + re.escape(name) + r'\s*\(', masked)
    if not match: raise ValueError(f'Missing method {name}')
    depth, i = 1, match.end()
    while depth:
        depth += (masked[i] == '(') - (masked[i] == ')')
        i += 1
    start = masked.find('{', i)
    if start < 0: raise ValueError(f'Missing body for {name}')
    depth, end = 1, start + 1
    while depth:
        depth += (masked[end] == '{') - (masked[end] == '}')
        end += 1
    return source[start:end]


def migrated_dialog_baseline(value):
    return value.replace('MaterialAlertDialogBuilder(requireContext(), R.style.App_Dialog_NoDim)', 'HomeDialogBuilder(requireContext(), b)').replace(
        '        val dialog = builder.create()\n        dialog.show()\n        UIUtils.capDialogWidth(dialog)', '        builder.show()')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--report', type=Path)
    args = parser.parse_args()
    baseline = subprocess.check_output(['git', 'show', f'{BASELINE}:{SOURCE}'], cwd=ROOT, text=True)
    current = (ROOT / SOURCE).read_text()
    results = []
    for name in PROTECTED:
        results.append({'method': name, 'check': 'exact_body', 'passed': body(baseline, name) == body(current, name)})
    # Stock Material notification denial is unthrottled and Activity-root scoped.
    registration = body(baseline, 'registerForActivityResult').replace('Snackbar.make(', 'ComposeSnackbarPresenter(').replace('Snackbar.LENGTH_LONG\n', 'ComposeSnackbarPresenter.LENGTH_LONG,\n                        appearance = ComposeSnackbarPresenter.Appearance.Material\n')
    results.append({'method': 'registerForActivityResult', 'check': 'exact_body_except_native_activity_root_material_snackbar', 'passed': registration == body(current, 'registerForActivityResult')})
    for name in DIALOGS:
        results.append({'method': name, 'check': 'callbacks_and_order_with_presentation_substitution', 'passed': migrated_dialog_baseline(body(baseline, name)) == body(current, name)})
    interval_path = 'app/src/main/java/com/celzero/bravedns/ui/bottomsheet/LogActivityIntervalBottomSheet.kt'
    interval_baseline = subprocess.check_output(['git', 'show', f'{BASELINE}:{interval_path}'], cwd=ROOT, text=True)
    interval_current = (ROOT / interval_path).read_text()
    for name in ['isDarkThemeOn', 'onDestroyView', 'selectChipForPreset', 'applyPreset', 'load', 'onAppExpandRequested', 'mergeDomains', 'render', 'sumCounts', 'mergeApps', 'showTimeRange']:
        results.append({'method': 'LogActivityIntervalBottomSheet.' + name, 'check': 'exact_body', 'passed': body(interval_baseline, name) == body(interval_current, name)})
    interval_view = body(interval_baseline, 'onViewCreated').replace('        b.bsLaiRecycler.layoutManager = LinearLayoutManager(requireContext())\n', '')
    results.append({'method': 'LogActivityIntervalBottomSheet.onViewCreated', 'check': 'exact_body_except_recyclerview_layout_manager', 'passed': interval_view == body(interval_current, 'onViewCreated')})
    range_setup = body(interval_baseline, 'setupRangeChips').replace('MaterialButtonToggleGroup.OnButtonCheckedListener', 'IntervalRangeGroup.OnButtonCheckedListener')
    results.append({'method': 'LogActivityIntervalBottomSheet.setupRangeChips', 'check': 'native_listener_type_only', 'passed': range_setup == body(interval_current, 'setupRangeChips')})
    adapter_path = 'app/src/main/java/com/celzero/bravedns/ui/adapter/AppActivityAdapter.kt'
    adapter_baseline = subprocess.check_output(['git', 'show', f'{BASELINE}:{adapter_path}'], cwd=ROOT, text=True)
    adapter_current = (ROOT / adapter_path).read_text()
    for name in ['submit', 'rebuildRows', 'setChildren', 'formatCount', 'visibleEntries', 'insertChildRows', 'removeStaleChildRows', 'removeChildRows', 'resolveIconForUid']:
        results.append({'method': 'AppActivityAdapter.' + name, 'check': 'exact_body', 'passed': body(adapter_baseline, name) == body(adapter_current, name)})
    expansion = body(adapter_baseline, 'onGroupClicked').replace('                b.laaChevron.animate().rotation(0f).setDuration(150).start()\n', '').replace('            b.laaChevron.animate().rotation(180f).setDuration(150).start()', '            notifyItemChanged(headerPos) // native chevron animation, including cached empty groups')
    results.append({'method': 'AppActivityAdapter.onGroupClicked', 'check': 'native_chevron_presentation_only', 'passed': expansion == body(adapter_current, 'onGroupClicked')})
    # Keep production startup paths out of callback-only Home tests.
    tests = list((ROOT / 'app/src/test/java/com/celzero/bravedns/ui/compose/home').glob('*.kt'))
    for test in tests:
        text = test.read_text()
        forbidden = ['Robolectric.buildActivity(HomeScreenActivity', 'Robolectric.buildActivity(HomeScreenFragment', 'launchFragment', 'VpnController.start(', 'VpnController.stop(', 'RethinkDnsApplication']
        results.append({'file': str(test.relative_to(ROOT)), 'check': 'no_production_startup_in_tests', 'passed': not any(token in text for token in forbidden)})
    report = {'baseline': BASELINE, 'kind': 'source_equivalence_only', 'results': results, 'passed': all(r['passed'] for r in results)}
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2) + '\n')
    for result in results:
        if not result['passed']: print('FAIL', result)
    print(f"Home source guard: {sum(r['passed'] for r in results)}/{len(results)} checks passed; not runtime evidence")
    return 0 if report['passed'] else 1

if __name__ == '__main__': sys.exit(main())
