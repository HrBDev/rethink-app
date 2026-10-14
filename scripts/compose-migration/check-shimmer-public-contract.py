#!/usr/bin/env python3
"""Source-only guard for the Shimmer 0.5.0 package-private-field compile regression.

Uses independently verified upstream values and pinned original XML; no network,
Gradle, Android, app execution, or reflective access. This is not a compile test.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
SOURCE = 'app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnHeaderSkeletons.kt'
CATALOG = 'gradle/lib.toml'
LAYOUT = 'app/src/main/res/layout/fragment_server_selection.xml'
PLACEHOLDER = 'app/src/main/res/drawable/shimmer_placeholder.xml'
ANDROID = '{http://schemas.android.com/apk/res/android}'
APP = '{http://schemas.android.com/apk/res-auto}'
UPSTREAM_COMMIT = '5ec9d4e73ef84696e6807488d7663a5d5ccf766d'
UPSTREAM_ROOT = 'https://github.com/facebookarchive/shimmer-android/blob/' + UPSTREAM_COMMIT
# Verified through GitHub at tag 0.5.0, independently of the Kotlin implementation.
UPSTREAM = {
    'version': '0.5.0',
    'commit': UPSTREAM_COMMIT,
    'Shimmer.java': {
        'url': UPSTREAM_ROOT + '/shimmer/src/main/java/com/facebook/shimmer/Shimmer.java',
        'git_blob_sha': 'a68993e882d0e94d3065cb175042750b2fbc9b4d',
        'declarations': [
            '@ColorInt int baseColor = 0x4cffffff;',
            '@ColorInt int highlightColor = Color.WHITE;',
            'long animationDuration = 1000L;',
        ],
        'contract': 'Package-private fields; no public getters. AlphaHighlightBuilder leaves these defaults unchanged.',
    },
    'ShimmerFrameLayout.java': {
        'url': UPSTREAM_ROOT + '/shimmer/src/main/java/com/facebook/shimmer/ShimmerFrameLayout.java',
        'git_blob_sha': '42c8fbe0b1351ffea6040292672b310229b29469',
        'contract': 'Defaults to AlphaHighlightBuilder when shimmer_colored is absent. Attributes are applied only when supplied.',
    },
    'expected': {'duration_millis': 1000, 'base_alpha_byte': 76, 'highlight_alpha_byte': 255},
}


def git(path):
    return subprocess.check_output(['git', 'show', BASE + ':' + path], cwd=ROOT, text=True)


def compact(text):
    return re.sub(r'\s+', '', text)


def validate(source, catalog, layout, placeholder):
    checks = []

    def check(name, passed):
        checks.append({'name': name, 'passed': bool(passed)})

    check('dependency remains pinned to the verified Shimmer 0.5.0',
          re.search(r'^shimmer\s*=\s*"0\.5\.0"\s*$', catalog, re.M) is not None)
    tree = ET.fromstring(layout)
    banner = next(node for node in tree.iter()
                  if node.get(ANDROID + 'id') == '@+id/shimmer_subscription_banner')
    check('original subscription banner has no shimmer parameter/style overrides',
          not any(key.startswith(APP) or key == 'style' for key in banner.attrib))
    check('original banner geometry remains the reference',
          banner.get(ANDROID + 'layout_marginTop') == '8dp'
          and banner[0].get(ANDROID + 'layout_height') == '40dp'
          and banner[0].get(ANDROID + 'padding') == '8dp'
          and banner[0][0].get(ANDROID + 'layout_width') == '20dp'
          and banner[0][0].get(ANDROID + 'layout_marginEnd') == '8dp'
          and banner[0][1].get(ANDROID + 'layout_height') == '12dp'
          and banner[0][1].get(ANDROID + 'layout_weight') == '1')
    shape = ET.fromstring(placeholder)
    check('original placeholder is primaryTextColor with 12dp corners',
          shape.find('solid').get(ANDROID + 'color') == '?attr/primaryTextColor'
          and shape.find('corners').get(ANDROID + 'radius') == '12dp')
    # Strip comments so source citations cannot accidentally satisfy code checks.
    code = re.sub(r'/\*.*?\*/|//[^\n]*', '', source, flags=re.S)
    for name, value in [('DURATION_MILLIS', '1000'), ('BASE_ALPHA', '0x4c / 255f'),
                        ('HIGHLIGHT_ALPHA', '1f')]:
        check('pinned subscription shimmer constant: ' + name,
              compact('private const val SHIMMER_050_' + name + ' = ' + value)
              in compact(code))
    check('subscription wires pinned defaults and caller running state into native shimmer',
          compact('sourceShimmerModifier(running, SHIMMER_050_DURATION_MILLIS, '
                  'SHIMMER_050_BASE_ALPHA, SHIMMER_050_HIGHLIGHT_ALPHA)') in compact(code))
    check('no inaccessible Shimmer fields, builder dependency or reflection',
          not re.search(r'com\.facebook\.shimmer|\bShimmer\s*\.|\.(?:baseColor|highlightColor|animationDuration)\b'
                        r'|getDeclaredField|isAccessible|java\.lang\.reflect', code))
    check('source-specific header timing and geometry are unchanged', all(
        compact(snippet) in compact(code) for snippet in [
            'sourceShimmerModifier(running, 1200, .12f, .35f)',
            'padding(top = 4.dp)', 'width(80.dp).height(10.dp)',
            'Spacer(Modifier.weight(1f).height(10.dp))', 'width(60.dp).height(26.dp)',
        ]))
    check('subscription placeholder geometry and source theme color are unchanged', all(
        compact(snippet) in compact(code) for snippet in [
            'padding(top = 8.dp).height(40.dp)', '.padding(8.dp)', 'Modifier.size(20.dp)',
            'Spacer(Modifier.width(8.dp))', 'Modifier.weight(1f).height(12.dp)',
            'themeColor(R.attr.primaryTextColor)', 'RoundedCornerShape(12.dp)',
        ]))
    check('native rendering has no XML/View bridge',
          not re.search(r'AndroidView\s*\(|LayoutInflater|\.inflate\s*\(|ShimmerFrameLayout\s*\(', code))
    return checks


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--report')
    args = parser.parse_args()
    source = (ROOT / SOURCE).read_text()
    catalog = (ROOT / CATALOG).read_text()
    layout, placeholder = git(LAYOUT), git(PLACEHOLDER)
    checks = validate(source, catalog, layout, placeholder)
    mutations = [
        ('wrong duration', 'SHIMMER_050_DURATION_MILLIS = 1000', 'SHIMMER_050_DURATION_MILLIS = 1500'),
        ('rounded base alpha', 'SHIMMER_050_BASE_ALPHA = 0x4c / 255f', 'SHIMMER_050_BASE_ALPHA = .3f'),
        ('wrong highlight alpha', 'SHIMMER_050_HIGHLIGHT_ALPHA = 1f', 'SHIMMER_050_HIGHLIGHT_ALPHA = .35f'),
        ('private baseColor access', 'SHIMMER_050_BASE_ALPHA = 0x4c / 255f', 'SHIMMER_050_BASE_ALPHA = defaults.baseColor'),
        ('private highlightColor access', 'SHIMMER_050_HIGHLIGHT_ALPHA = 1f', 'SHIMMER_050_HIGHLIGHT_ALPHA = defaults.highlightColor'),
        ('private animationDuration access', 'SHIMMER_050_DURATION_MILLIS = 1000', 'SHIMMER_050_DURATION_MILLIS = defaults.animationDuration'),
        ('ignores stopped state', 'sourceShimmerModifier(running, SHIMMER_050_DURATION_MILLIS', 'sourceShimmerModifier(true, SHIMMER_050_DURATION_MILLIS'),
        ('placeholder color drift', 'themeColor(R.attr.primaryTextColor)', 'themeColor(R.attr.background)'),
        ('subscription geometry drift', 'padding(top = 8.dp).height(40.dp)', 'padding(top = 8.dp).height(48.dp)'),
        ('header timing drift', 'sourceShimmerModifier(running, 1200, .12f, .35f)', 'sourceShimmerModifier(running, 1000, .12f, .35f)'),
    ]
    negative_controls = []
    for name, old, new in mutations:
        assert old in source, old
        changed = source.replace(old, new, 1)
        negative_controls.append({'mutation': name, 'rejected': not all(
            result['passed'] for result in validate(changed, catalog, layout, placeholder))})
    changed_catalog = catalog.replace('shimmer = "0.5.0"', 'shimmer = "0.6.0"')
    assert changed_catalog != catalog
    negative_controls.append({'mutation': 'unreviewed dependency version', 'rejected': not all(
        result['passed'] for result in validate(source, changed_catalog, layout, placeholder))})
    report = {
        'verification_kind': 'source-only; not Kotlin compilation or visual equivalence',
        'baseline': BASE, 'upstream_evidence': UPSTREAM, 'checks': checks,
        'negative_controls': negative_controls,
        'source_sha256': {SOURCE: hashlib.sha256(source.encode()).hexdigest(),
                          CATALOG: hashlib.sha256(catalog.encode()).hexdigest()},
        'baseline_sha256': {LAYOUT: hashlib.sha256(layout.encode()).hexdigest(),
                            PLACEHOLDER: hashlib.sha256(placeholder.encode()).hexdigest()},
        'unrun': ['Android/Kotlin compilation', 'Compose/Robolectric tests', 'device/screenshots'],
        'remaining_limits': ['Exact shimmer sweep/dropoff/tilt and stopped-frame rendering still require visual comparison.'],
    }
    if args.report:
        path = Path(args.report)
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(report, indent=2) + '\n')
    failed = [item['name'] for item in checks if not item['passed']]
    failed += [item['mutation'] for item in negative_controls if not item['rejected']]
    if failed:
        print('FAIL:\n' + '\n'.join(failed))
        return 1
    print(f'PASS: {len(checks)} Shimmer source assertions; {len(negative_controls)} rejected mutations. Compilation/runtime not run.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
