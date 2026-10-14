#!/usr/bin/env python3
"""Fresh source-only root chrome parity guard. Never launches Android or app services."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET

BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
ROOT = Path(__file__).resolve().parents[2]
HOME = 'app/src/main/java/com/celzero/bravedns/ui/HomeScreenActivity.kt'
UTIL = 'app/src/main/java/com/celzero/bravedns/util/UIUtils.kt'
IMPORT = 'import com.celzero.bravedns.ui.compose.chrome.ComposeSnackbarPresenter\n'
CHROME = 'app/src/main/java/com/celzero/bravedns/ui/compose/chrome/'
HOST = 'app/src/main/java/com/celzero/bravedns/ui/compose/ActivityFragmentContainer.kt'
FRAGMENT_HOST = 'app/src/main/java/com/celzero/bravedns/ui/activity/FragmentHostActivity.kt'
CONFIGURE_HOST = 'app/src/main/java/com/celzero/bravedns/ui/activity/ConfigureRethinkBasicActivity.kt'
HOST_IDS = 'app/src/main/res/values/compose_activity_host_ids.xml'
ANCHORS = 'app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeTourAnchors.kt'
SERVER_SELECTION = 'app/src/main/java/com/celzero/bravedns/ui/fragment/ServerSelectionFragment.kt'
RETIRED_LAYOUTS = [
    'app/src/full/res/layout/activity_home_screen.xml',
    'app/src/main/res/layout/activity_fragment_host.xml',
    'app/src/full/res/layout/fragment_rethink_basic.xml',
]
TOKENS = re.compile(r'"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|/\*[\s\S]*?\*/|//[^\n]*|[{}]')


def block(text, marker):
    start = text.index(marker)
    signature = re.compile(TOKENS.pattern + r'|[()]')
    parens = 0
    opening = None
    for token in signature.finditer(text, start):
        if token[0] == '(': parens += 1
        elif token[0] == ')': parens -= 1
        elif token[0] == '{' and parens == 0:
            opening = token.start()
            break
    assert opening is not None, f'No body for {marker}'
    depth = 1
    for match in TOKENS.finditer(text, opening + 1):
        if match[0] == '{': depth += 1
        elif match[0] == '}':
            depth -= 1
            if depth == 0: return text[start:match.end()]
    raise AssertionError(f'Unterminated {marker}')


def baseline(root, path):
    return subprocess.check_output(['git', '-C', str(root), 'show', f'{BASE}:{path}']).decode()


def check(root, override=None):
    read = lambda path: (override or {}).get(path, (root / path).read_text())
    before, after = baseline(root, HOME), read(HOME)
    protected = [
        'onResume', 'isAppRunningOnTv', 'isInForeground', 'handleIntent',
        'handleRestoreProcess', 'regenerateFirebaseTokenIfNeeded', 'startRestore',
        'observeRestoreWorker', 'restartApp', 'observeAppState', 'removeThisMethod',
        'migrateSmartDnsSelectionIfNeeded', 'setDefaultSmartDnsPrivacyForNewInstalls',
        'moveRemoteBlocklistFileFromAsset', 'launchOnboardActivity', 'updateNewVersion',
        'isNewVersion', 'getLatestVersion', 'initUpdateCheck', 'isUpdateRequired',
        'checkForUpdate', 'isGooglePlayServicesAvailable', 'initiateDownload', 'onStop', 'io',
    ]
    for name in protected:
        assert block(before, f'fun {name}(') == block(after, f'fun {name}('), f'Home controller changed: {name}'
    # Only rendering/inset installation moved into the Compose root. Keep every controller
    # startup call and its order byte-for-byte; this does not establish runtime timing parity.
    old_startup = block(before, 'override fun onCreate(')
    old_insets = block(old_startup, 'if (isAtleastO_MR1())')
    old_startup = old_startup.replace('        ' + old_insets + '\n\n', '')
    new_startup = block(after, 'override fun onCreate(').replace(
        '\n        installComposeChrome(savedInstanceState)', '')
    assert old_startup == new_startup, 'Startup controller call order changed'
    assert block(before, 'object : AppUpdater.InstallStateListener') == block(after, 'object : AppUpdater.InstallStateListener'), 'Updater listener changed'
    old_back = block(before, 'private fun handleOnBackPressed(').replace(
        'val btmNavView = findViewById<BottomNavigationView>(R.id.nav_view)\n                            btmNavView.selectedItemId = homeId',
        'selectBottomNavigationItem(homeId)')
    assert old_back == block(after, 'private fun handleOnBackPressed('), 'Back behavior changed'
    old_selection = block(before, 'btmNavView.setOnItemSelectedListener').replace(
        'btmNavView.setOnItemSelectedListener { item ->', 'selectNavigationItem = selection@ { itemId ->'
    ).replace('item.itemId', 'itemId').replace('return@setOnItemSelectedListener', 'return@selection')
    assert old_selection == block(after, 'selectNavigationItem = selection@'), 'Navigation callback changed'
    for marker in ['private fun bottomNavNavOptions', 'private fun isRpnEntitled']:
        region = lambda text: text[text.index(marker):text.index('\n    private fun ', text.index(marker) + len(marker))]
        assert region(before) == region(after), f'Navigation policy changed: {marker}'
    assert 'if (selectedNavigationItem == itemId) return' in after, 'Reselection behavior changed'
    assert 'if (selectNavigationItem?.invoke(itemId) == true) selectedNavigationItem = itemId' in after
    assert 'HomeTourAnchors.forActivity(this)' in after, 'Activity tour registry disconnected'
    assert 'val workId = startRestore(uri)\n                observeRestoreWorker(workId)' in after
    assert 'onNegative = { persistentState.lastAppUpdateCheck = System.currentTimeMillis() }' in after

    old_util, new_util = baseline(root, UTIL), read(UTIL)
    assert new_util.count(IMPORT) == 1, 'Only the additive presenter import is approved'
    clean_util = new_util.replace(IMPORT, '')
    assert old_util[:old_util.index('object SnackbarHelper')] == clean_util[:clean_util.index('object SnackbarHelper')], 'Protected UIUtils prefix changed'
    helper_end = lambda text: text.index('object SnackbarHelper') + len(block(text, 'object SnackbarHelper'))
    assert old_util[helper_end(old_util):] == clean_util[helper_end(clean_util):], 'Protected UIUtils tail changed'
    for marker in ['fun showStabilityProgram(', 'fun String.capitalizeWords(', 'fun String.italic(']:
        assert block(old_util, marker) == block(new_util, marker), f'Non-rendering helper changed: {marker}'
    throttle = lambda text: text[text.index('        val now = System.currentTimeMillis()', text.index('object SnackbarHelper')):text.index('        current?.dismiss()', text.index('object SnackbarHelper'))]
    assert throttle(old_util) == throttle(new_util), 'Global duplicate throttle changed'
    for expected in ['private const val THROTTLE_MS = 30_000L', 'private var lastMessage: String = ""',
                     'private var lastShownAt: Long = 0L', 'if (current === dismissed) current = null',
                     'current = null\n                    callback()']:
        assert expected in new_util, f'Helper identity/throttle contract missing: {expected}'
    for legacy in ['Snackbar.make', 'findViewById<BottomNavigationView>']:
        assert legacy not in new_util, f'Legacy snackbar rendering/cast remains: {legacy}'
    for legacy in ['MaterialAlertDialogBuilder', 'Snackbar.make', 'BottomNavigationView>']:
        assert legacy not in after, f'Legacy root chrome remains: {legacy}'
    presenter = read(CHROME + 'ComposeSnackbarPresenter.kt')
    for expected in ['findViewById<View>(R.id.nav_view)', 'getRecommendedTimeoutMillis',
                     'isTouchExplorationEnabled', 'insetBottom += navigationBottom + 80',
                     'private var active: ComposeSnackbarPresenter?', 'private var pending: ComposeSnackbarPresenter?']:
        assert expected in presenter, f'Snackbar presentation contract missing: {expected}'
    for path in [CHROME + 'HomeChrome.kt', CHROME + 'ComposeSnackbarPresenter.kt']:
        for legacy in ['AndroidView(', 'LayoutInflater', 'Snackbar.make(']:
            assert legacy not in read(path), f'Legacy renderer remains in {path}: {legacy}'
    for path in [HOME, FRAGMENT_HOST, CONFIGURE_HOST]:
        host_source = read(path)
        assert ' : BaseActivity()' in host_source, f'XML constructor remains: {path}'
        assert 'R.layout.' not in host_source, f'XML Activity rendering remains: {path}'
        assert 'ActivityFragmentContainer(' in host_source, f'Compose Fragment boundary missing: {path}'
        assert 'consumeWindowInsets = false' in host_source, f'Nested Fragment insets consumed: {path}'
    for required in ['id = R.id.container', 'containerId = R.id.fragment_container',
                     'NavHostFragment.create(R.navigation.app_navigation)', 'primaryNavigationHost = true',
                     'homeTourAnchor(R.id.nav_view, anchors)', 'consumeWindowInsets = false',
                     'WindowInsets.systemBars.only(WindowInsetsSides.Bottom)',
                     'if (navigationListenerInstalled) return',
                     'if (navigationSetupRequested) setupNavigationItemSelectedListener()']:
        assert required in after, f'Native Home host contract missing: {required}'
    host_source = read(HOST)
    for required in ['FragmentContainerView(context).apply { id = containerId }',
                     'fragmentManager.findFragmentById(containerId) ?: createFragment()',
                     '.add(container, it, fragmentTag(it))', 'fragmentManager.onContainerAvailable(container)',
                     'setPrimaryNavigationFragment(fragment)', 'fragmentManager.isStateSaved',
                     'lifecycle.removeObserver(observer)']:
        assert required in host_source, f'Stable FragmentManager host contract missing: {required}'
    assert 'commitNowAllowingStateLoss' not in host_source
    assert 'LayoutInflater' not in host_source and 'R.layout.' not in host_source
    for path in RETIRED_LAYOUTS:
        assert not (root / path).exists(), f'Retired Activity XML shell remains: {path}'
    ids = ET.fromstring(read(HOST_IDS))
    assert {'fragment_container', 'root_container', 'nav_view'} <= {e.attrib['name'] for e in ids}
    for path in ['app/src/main/res/navigation/app_navigation.xml', 'app/src/full/res/navigation/app_navigation.xml']:
        if (root / path).exists(): assert baseline(root, path) == read(path), f'Framework graph changed: {path}'
    for marker in ['fun createIntent(', 'private fun handleOnBackPressed(', 'override fun onSaveInstanceState(']:
        assert block(baseline(root, FRAGMENT_HOST), marker) == block(read(FRAGMENT_HOST), marker), f'FragmentHost contract changed: {marker}'
    for marker in ['private fun fragment(', 'private fun createBundle(', 'private fun updateBundle(']:
        assert block(baseline(root, CONFIGURE_HOST), marker) == block(read(CONFIGURE_HOST), marker), f'Blocklist argument contract changed: {marker}'
    assert 'supportFragmentManager.findFragmentById(R.id.root_container) ?: fragment(fl)' in read(CONFIGURE_HOST)
    assert 'composeAnchors?.find(R.id.nav_view)' in presenter, 'Native snackbar anchor disconnected'
    assert 'composeActivityRoot?.addOnLayoutChangeListener(reposition)' in presenter
    assert 'params.setMargins(left, params.topMargin, right, bottom)' in presenter
    assert 'stopObservingNavigation?.invoke()' in presenter
    selection = read(SERVER_SELECTION)
    assert 'findViewById<View>(R.id.nav_view)' not in selection, 'RPN still depends on a removed navigation View'
    for expected in ['tourAnchors.observeBounds(R.id.nav_view)', 'stopObservingNavigation()',
                     'owner.lifecycle.removeObserver(this)',
                     'ui.fabBottomPx = navBarBottom + nativeNavHeight + dpPx(16)',
                     'ui.scrollBottomPx = if (bounds != null) nativeNavHeight + dpPx(24) else 0']:
        assert expected in selection, f'RPN native navigation-clearance contract missing: {expected}'
    anchors = read(ANCHORS)
    assert 'boundsListeners[id]?.toList()?.forEach' in anchors
    assert 'if (anchors[id]?.owner !== owner) return' in anchors
    return len(protected)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--repo', type=Path, default=ROOT)
    parser.add_argument('--self-test', action='store_true')
    parser.add_argument('--report', type=Path)
    args = parser.parse_args()
    count = check(args.repo)
    if args.self_test:
        for path, old, new in [
            (HOME, 'regenerateFirebaseTokenIfNeeded()', 'regenerateFirebaseTokenIfNeeded(); regenerateFirebaseTokenIfNeeded()'),
            (HOME, 'ExistingWorkPolicy.KEEP', 'ExistingWorkPolicy.REPLACE'),
            (HOME, 'return@selection false', 'return@selection true'),
            (HOME, 'bottomNavNavOptions(homeId, true)', 'bottomNavNavOptions(homeId, false)'),
            (UTIL, 'private const val THROTTLE_MS = 30_000L', 'private const val THROTTLE_MS = 1L'),
            (UTIL, 'object UIUtils {', 'object UIUtils { /* unrelated change */'),
            (UTIL, 'if (current === dismissed)', 'if (current != null)'),
            (HOST, 'fragmentManager.findFragmentById(containerId) ?: createFragment()', 'createFragment()'),
            (HOME, 'primaryNavigationHost = true', 'primaryNavigationHost = false'),
            (CONFIGURE_HOST, 'supportFragmentManager.findFragmentById(R.id.root_container) ?: fragment(fl)', 'fragment(fl)'),
            (SERVER_SELECTION, 'stopObservingNavigation()', 'Unit'),
        ]:
            source = (args.repo / path).read_text()
            assert old in source
            try: check(args.repo, {path: source.replace(old, new, 1)})
            except AssertionError: continue
            raise AssertionError(f'Negative control escaped: {old}')
        print('PASS: eleven source guard negative controls')
    if args.report:
        files = [HOME, UTIL, CHROME + 'HomeChrome.kt', CHROME + 'ComposeSnackbarPresenter.kt', HOST, FRAGMENT_HOST, CONFIGURE_HOST, HOST_IDS, ANCHORS, SERVER_SELECTION]
        args.report.write_text(json.dumps({'baseline': BASE, 'checked_at': datetime.now(timezone.utc).isoformat(),
            'status': 'PASS', 'source_only': True, 'unchanged_home_methods': count, 'retired_xml_shells': RETIRED_LAYOUTS,
            'files_sha256': {path: hashlib.sha256((args.repo / path).read_bytes()).hexdigest() for path in files}}, indent=2)+'\n')
    print(f'PASS: {count} unchanged Home controller methods; startup/listener/back/navigation/throttle/prefix and framework boundaries checked')
    print('Source preservation only. Not Android compilation, runtime, visual or accessibility parity.')

if __name__ == '__main__': main()
