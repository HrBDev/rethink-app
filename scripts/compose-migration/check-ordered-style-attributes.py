#!/usr/bin/env python3
"""Source-only safeguards for ordered Android resource queries and intrinsic icon sizing.
Does not compile Kotlin, run Android, or establish screenshot parity.
"""
from pathlib import Path
import argparse
import json
import re

ROOT = Path(__file__).resolve().parents[2]
BASE = ROOT / 'app/src/main/java/com/celzero/bravedns/ui/compose'
WRAPPER = BASE / 'resources/OrderedStyledAttributes.kt'
CONTROL = BASE / 'controls/OriginalControls.kt'
TEST = ROOT / 'app/src/test/java/com/celzero/bravedns/ui/compose/resources/OrderedStyledAttributesTest.kt'

def check(overrides=None):
    overrides = overrides or {}
    def read(path): return overrides.get(path, path.read_text())
    wrapper, control, test = read(WRAPPER), read(CONTROL), read(TEST)
    assert 'attributes.distinct().sorted().toIntArray()' in wrapper, 'query IDs must be sorted and deduplicated'
    assert 'sortedIds.binarySearch(attributes[it])' in wrapper, 'preserve declared indices'
    assert 'fun index(declaredIndex: Int): Int = declaredToSorted[declaredIndex]' in wrapper
    assert '.sort()' not in wrapper, 'do not mutate caller arrays'
    for method in ('hasValue', 'hasValueOrEmpty', 'peekValue', 'getResourceId', 'getDimension',
                   'getDimensionPixelSize', 'getBoolean', 'getInt', 'getFloat', 'getColor',
                   'getColorStateList', 'getDrawable', 'getString'):
        assert f'values.{method}(order.index(index)' in wrapper, f'unmapped {method}'
    for query in ('obtainStyledAttributes(order.sortedIds)',
                  'obtainStyledAttributes(style, order.sortedIds)',
                  'obtainStyledAttributes(set, order.sortedIds, defaultStyleAttribute, defaultStyleResource)'):
        assert query in wrapper, 'unsorted overload: ' + query
    assert 'fun recycle() = values.recycle()' in wrapper
    calls = 0
    for path in BASE.rglob('*.kt'):
        if path == WRAPPER: continue
        source = read(path)
        assert not re.search(r'\bobtainStyledAttributes\s*\(', source), f'raw query in {path.name}'
        calls += source.count('.obtainOrderedStyledAttributes(')
    assert calls >= 61, 'existing resolver queries must remain routed'
    for token in ('look.iconSize > 0f -> Modifier.size(look.iconSize.dp)',
                  'originalIconExtent(iconPainter.intrinsicSize.width, density)',
                  'originalIconExtent(iconPainter.intrinsicSize.height, density)',
                  'else -> Modifier', 'intrinsicPixels.toDp()',
                  'iconSize = if (chip) dim(36, 18f) else dim(38)',
                  'if (leadingIcon != null) leadingIcon()', 'Modifier.fillMaxSize(), tint = resolvedIconTint'):
        assert token in control, 'intrinsic icon contract: ' + token
    for token in ('application = Application::class', 'allQueryOverloadsMatchIndependentSingleAttributePlatformQueries',
                  'context.obtainStyledAttributes(intArrayOf(it))', 'assertArrayEquals(original, input)',
                  'hasValueOrEmpty', 'snapshot(expected.peekValue(0))'):
        assert token in test, 'independent resource test: ' + token
    return calls

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--self-test', action='store_true')
    args = parser.parse_args()
    calls = check()
    mutations = [
        (WRAPPER, 'attributes.distinct().sorted().toIntArray()', 'attributes.copyOf()'),
        (WRAPPER, 'sortedIds.binarySearch(attributes[it])', 'it'),
        (WRAPPER, 'declaredToSorted[declaredIndex]', 'declaredIndex'),
        (WRAPPER, 'values.hasValueOrEmpty(order.index(index))', 'values.hasValueOrEmpty(index)'),
        (WRAPPER, 'values.getDimension(order.index(index), fallback)', 'values.getDimension(index, fallback)'),
        (WRAPPER, 'obtainStyledAttributes(style, order.sortedIds)', 'obtainStyledAttributes(style, attributes)'),
        (CONTROL, 'context.obtainOrderedStyledAttributes(null, attrs', 'context.obtainStyledAttributes(null, attrs'),
        (CONTROL, 'originalIconExtent(iconPainter.intrinsicSize.width, density)', 'look.iconSize.dp'),
        (CONTROL, 'originalIconExtent(iconPainter.intrinsicSize.height, density)', 'look.iconSize.dp'),
        (CONTROL, 'intrinsicPixels.toDp()', 'intrinsicPixels.dp'),
        (TEST, 'context.obtainStyledAttributes(intArrayOf(it))', 'context.obtainOrderedStyledAttributes(intArrayOf(it))'),
    ]
    rejected = 0
    if args.self_test:
        for path, old, new in mutations:
            source = path.read_text()
            assert old in source, old
            try: check({path: source.replace(old, new)})
            except AssertionError: rejected += 1
            else: raise AssertionError('mutation escaped: ' + old)
    report = {'status': 'PASS_SOURCE_ONLY', 'style_queries_routed': calls, 'negative_controls_rejected': rejected,
              'compile': 'UNRUN', 'android_resource_tests': 'UNRUN', 'rendering': 'UNRUN'}
    (ROOT / 'docs/compose-migration/ordered-style-attributes-source-report.json').write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps(report))

if __name__ == '__main__': main()
