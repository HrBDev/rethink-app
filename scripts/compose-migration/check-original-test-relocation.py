#!/usr/bin/env python3
"""Original test path/name coverage only. Never imports or executes Kotlin test code."""
import argparse
from collections import Counter
import importlib.util
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
spec = importlib.util.spec_from_file_location('test_source_lexer', Path(__file__).with_name('check-wireguard-controllers.py'))
lexer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(lexer)
mask = lambda source: lexer.lex(source)[1]


def test_methods(source):
    code = mask(source)
    names = re.findall(r'@(?:org\.junit\.)?Test\b[^{};]*?\bfun\s+(`[^`]+`|\w+)\s*\(', code)
    assert len(names) == len(re.findall(r'@(?:org\.junit\.)?Test\b', code)), 'Unparsed @Test declaration'
    return Counter(names)


def verify_file(original_path, current_path, replacements):
    original = subprocess.check_output(['git', 'show', BASE + ':' + original_path], cwd=ROOT, text=True)
    destination = ROOT / current_path
    assert destination.is_file(), 'Missing original test destination: ' + current_path
    before, after = test_methods(original), test_methods(destination.read_text())
    missing = before - after
    assert set(replacements) == set(missing), 'Undocumented or stale method replacement: ' + original_path
    for old, new in replacements.items():
        assert after[new] >= missing[old], 'Replacement @Test missing: ' + current_path + ':' + new
    assert len(set(replacements.values())) == len(replacements), 'Multiple original obligations share a replacement'
    return {
        'original_path': original_path, 'current_path': current_path,
        'original_test_count': sum(before.values()), 'current_test_count': sum(after.values()),
        'retained_original_method_count': sum((before & after).values()),
        'missing_original_method_names': sorted(missing), 'documented_replacements': replacements,
        'original_method_names': sorted(before), 'current_method_names': sorted(after),
    }


def check():
    mapping = json.loads((ROOT / 'docs/compose-migration/original-test-relocation-map.json').read_text())
    by_path = {row['original_path']: row for row in mapping}
    assert len(by_path) == len(mapping), 'Duplicate original test path in map'
    paths = subprocess.check_output(['git', 'ls-tree', '-r', '--name-only', BASE], cwd=ROOT, text=True).splitlines()
    paths = [path for path in paths if re.fullmatch(r'app/src/(?:androidTest\w*|test\w*)/.*\.kt', path)]
    assert set(by_path) <= set(paths), 'Relocation map contains a non-original test source'
    records = []
    for path in paths:
        entry = by_path.get(path)
        current = entry['current_path'] if entry else path
        replacements = entry.get('prior_worker_native_method_replacements', {}) if entry else {}
        if current != path:
            assert '/androidTest/' in path and current == path.replace('/androidTest/', '/test/'), 'Unexpected relocation: ' + path
        row = verify_file(path, current, replacements)
        if entry:
            for key in ['original_test_count', 'current_test_count', 'missing_original_method_names']:
                assert entry[key] == row[key], 'Stale relocation-map field ' + key + ': ' + path
            assert entry['unmapped_original_method_names'] == [], 'Unmapped original test: ' + path
        records.append(row)
    manifest = json.loads((ROOT / 'docs/compose-migration/app-settings-host-tests-manifest.json').read_text())
    assert manifest['baseline'] == BASE
    manifest_cases = [(case['baseline_path'], case['method']) for case in manifest['case_mapping']]
    assert len(manifest_cases) == len(set(manifest_cases)) == manifest['original_cases']
    expected = {(row['original_path'], name) for row in records if row['original_path'] in {
        'app/src/androidTest/java/com/celzero/bravedns/ui/activity/AppInfoActivityTest.kt',
        'app/src/test/java/com/celzero/bravedns/ui/activity/MiscSettingsActivityRobolectricTest.kt',
    } for name in row['original_method_names']}
    assert set(manifest_cases) == expected, 'App/settings manifest does not cover exactly the original cases'
    for case in manifest['case_mapping']:
        row = next(row for row in records if row['original_path'] == case['baseline_path'])
        assert case['current_path'] == row['current_path'] and case['method'] in row['current_method_names']
    return {
        'baseline': BASE, 'status': 'PASS', 'source_only': True,
        'original_source_files': len(records), 'relocation_map_entries': len(mapping),
        'relocated_sources': sum(row['current_path'] != row['original_path'] for row in records),
        'original_test_declarations': sum(row['original_test_count'] for row in records),
        'retained_original_declarations': sum(row['retained_original_method_count'] for row in records),
        'documented_native_method_replacements': sum(len(row['documented_replacements']) for row in records),
        'app_settings_manifest_original_cases': len(manifest_cases), 'sources': records,
        'limitation': 'Path/name/declaration coverage only; no test execution or behavioral equivalence claim.',
    }


def self_test():
    source = '// @Test fun removed() {}\n@Test @Config(sdk = [35]) fun live() {}\n@Test fun `named case`() {}'
    assert test_methods(source) == Counter({'live': 1, '`named case`': 1})
    assert 'live' not in test_methods(source.replace('@Test @Config', '@Config'))
    assert 'live' not in test_methods(source.replace('fun live()', 'fun renamed()'))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--self-test', action='store_true')
    parser.add_argument('--report', type=Path)
    args = parser.parse_args()
    if args.self_test:
        self_test()
    report = check()
    if args.report:
        args.report.write_text(json.dumps(report, indent=2) + '\n')
    print(f"PASS: {report['original_source_files']} original test sources, {report['relocated_sources']} relocated; "
          f"{report['retained_original_declarations']} original @Test declarations retained and "
          f"{report['documented_native_method_replacements']} explicitly mapped native replacements; "
          f"{report['app_settings_manifest_original_cases']} app/settings manifest cases. Source coverage only, no tests executed.")
