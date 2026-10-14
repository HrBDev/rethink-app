#!/usr/bin/env python3
"""Source/mutation safeguards only; cannot establish Android inset dispatch or rendering."""
from pathlib import Path
import argparse
import json
import re

ROOT = Path(__file__).resolve().parents[2]
PATHS = {
    'host': 'app/src/main/java/com/celzero/bravedns/ui/activity/ConfigureRethinkBasicActivity.kt',
    'bridge': 'app/src/main/java/com/celzero/bravedns/ui/compose/ActivityFragmentContainer.kt',
    'fragment': 'app/src/main/java/com/celzero/bravedns/ui/fragment/RethinkBlocklistFragment.kt',
    'screen': 'app/src/main/java/com/celzero/bravedns/ui/compose/blocklists/BlocklistConfigurationScreen.kt',
    'test': 'app/src/test/java/com/celzero/bravedns/ui/compose/blocklists/BlocklistWindowInsetsTest.kt',
}

def check(overrides=None):
    overrides = overrides or {}
    source = {key: overrides.get(key, (ROOT/path).read_text()) for key, path in PATHS.items()}
    host, bridge, fragment, screen, test = (source[k] for k in PATHS)
    contracts = [
        ('host forwards rather than consumes', 'consumeWindowInsets = false' in host),
        ('no second host padding', 'modifier = Modifier.fillMaxSize(),' in host and 'safeDrawingPadding' not in host and 'systemBarsPadding' not in host),
        ('framework bridge remains unpadded', 'AndroidView(factory = { container }, modifier = modifier)' in bridge),
        ('leaf uses actual screen', 'setContent { RethinkTheme { BlocklistConfigurationScreen(' in fragment),
        ('single leaf owns safe drawing', screen.count('.safeDrawingPadding()') == 1 and 'Box(Modifier.fillMaxSize().safeDrawingPadding().testTag("blocklist-content"))' in screen),
        ('safe drawing is not added to IME again', '.imePadding()' not in screen),
        ('synthetic plain application', 'application = Application::class' in test),
        ('test uses actual host bridge', 'ActivityFragmentContainer(supportFragmentManager, R.id.root_container,' in test),
        ('test uses actual screen', 'BlocklistConfigurationScreen(screen, pages, pages, viewLifecycleOwner,' in test),
        ('test sends real platform inset dispatch', 'ViewCompat.dispatchApplyWindowInsets(activity.root, insets)' in test),
        ('zero, gesture, button and cutout fixtures', all(s in test for s in ['Edges()', 'Edges(top = 24, bottom = 24)', 'Edges(top = 24, bottom = 48)', 'Edges(left = 18, top = 32, right = 10, bottom = 24)'])),
        ('keyboard opening and closing', 'listOf(0, 260, 180, 0)' in test and 'WindowInsetsCompat.Type.ime()' in test),
        ('maximum not additive bottom', 'max(edges.bottom, edges.ime)' in test),
        ('top and sides reserved once', all(s in test for s in ['rootTop + edges.top, tabs.top', 'rootLeft + edges.left, tabs.left', 'activity.root.width - edges.right, tabs.right'])),
        ('footer separate from list', 'list.bottom <= actions.top' in test),
        ('recorded actions remain distinct', 'assertEquals(listOf("discard", "apply"), activity.calls)' in test),
        ('no production dependencies in fixture', all(s not in test for s in ['RethinkDnsApplication::class', 'inject<', 'startService(', 'VpnController', 'RethinkBlocklistFragment()'])),
    ]
    for name, valid in contracts: assert valid, name
    return len(contracts)

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--self-test', action='store_true')
    args = parser.parse_args()
    count = check()
    mutations = [
        ('host', 'consumeWindowInsets = false', 'consumeWindowInsets = true'),
        ('host', 'modifier = Modifier.fillMaxSize(),', 'modifier = Modifier.fillMaxSize().safeDrawingPadding(),'),
        ('bridge', 'AndroidView(factory = { container }, modifier = modifier)', 'AndroidView(factory = { container }, modifier = modifier.safeDrawingPadding())'),
        ('screen', '.safeDrawingPadding()', ''),
        ('screen', '.safeDrawingPadding()', '.safeDrawingPadding().imePadding()'),
        ('test', 'ViewCompat.dispatchApplyWindowInsets(activity.root, insets)', 'Unit'),
        ('test', 'max(edges.bottom, edges.ime)', 'edges.bottom + edges.ime'),
        ('test', 'rootTop + edges.top, tabs.top', 'rootTop, tabs.top'),
        ('test', 'list.bottom <= actions.top', 'true'),
        ('test', 'application = Application::class', 'application = RethinkDnsApplication::class'),
    ]
    rejected = 0
    if args.self_test:
        for key, old, new in mutations:
            source = (ROOT/PATHS[key]).read_text()
            assert old in source
            try: check({key: source.replace(old, new)})
            except AssertionError: rejected += 1
            else: raise AssertionError('mutation escaped: ' + old)
    result = {'status': 'PASS_SOURCE_ONLY', 'contracts': count, 'rejected_mutations': rejected,
              'authored_test_methods': len(re.findall(r'@Test\s+fun', (ROOT/PATHS['test']).read_text())), 'android_compilation': 'UNRUN', 'android_tests': 'UNRUN',
              'inset_dispatch_and_rendering': 'UNRUN'}
    (ROOT/'docs/compose-migration/blocklist-inset-boundary-source-report.json').write_text(json.dumps(result, indent=2)+'\n')
    print(json.dumps(result))

if __name__ == '__main__': main()
