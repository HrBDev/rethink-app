#!/usr/bin/env python3
"""Run only the explicit source-only final migration gates. No build or Android execution.

The pinned baseline Git objects must be present. Individual gates may refresh
their source reports. A passing result is not Kotlin/resource/runtime validation.
"""
from pathlib import Path
import argparse
import datetime
import hashlib
import json
import os
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[2]
CHECKS = [
    ('check-about-controllers.py', []),
    ('check-apps-controller-parity.py', []),
    ('check-binding-removal.py', ['--self-test']),
    ('check-button-overlay-regression.py', ['--self-test']),
    ('check-custom-rules-contracts.py', []),
    ('check-dns-blocklists-parity.py', []),
    ('check-dns-retired-renderers.py', []),
    ('check-dns-statistics-controls.py', ['--self-test']),
    ('check-history-rpn-styling.py', []),
    ('check-home-section-spacing.py', ['--self-test']),
    ('check-navigation-chrome.py', ['--self-test']),
    ('check-network-settings-controllers.py', []),
    ('check-network-settings-layouts.py', []),
    ('check-network-sheet-controllers.py', []),
    ('check-no-layout-resources.py', ['--self-test']),
    ('check-notification-ui-boundary.py', []),
    ('check-original-controls.py', []),
    ('check-original-fields.py', []),
    ('check-original-test-relocation.py', ['--self-test']),
    ('check-protected-core.py', []),
    ('check-protocol-dns-parity.py', []),
    ('check-purchase-controllers.py', ['--self-test']),
    ('check-rows-tabs-source-styling.py', []),
    ('check-rpn-configuration-controllers.py', ['--self-test']),
    ('check-rpn-controller-parity.py', []),
    ('check-search-ime-source.py', ['--self-test']),
    ('check-search-source-styling.py', []),
    ('check-settings-controllers.py', []),
    ('check-statistics-contracts.py', ['--self-test']),
    ('check-statistics-row-styles.py', ['--self-test']),
    ('check-surrounding-text-styles.py', ['--self-test']),
    ('check-traffic-log-controllers.py', []),
    ('check-utility-controllers.py', ['--self-test']),
    ('check-wg-text-progress-source.py', ['--negative-controls']),
    ('check-wireguard-controllers.py', ['--self-test']),
    ('check-wireguard-resources.py', []),
]
DOC_CHECKS = [
    'check_compound_control_sources.py',
    'check_dns_source_styles.py',
    'check_rule_dialog_source_styles.py',
    'check_shared_card_source_styles.py',
    'test-dependencies-check-fixtures.py',
]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--report', type=Path, default=ROOT / 'docs/xml-layout-removal/final-source-checks.json')
    args = parser.parse_args()
    commands = [[sys.executable, 'scripts/compose-migration/' + name, *flags] for name, flags in CHECKS]
    commands += [[sys.executable, 'docs/xml-layout-removal/' + name] for name in DOC_CHECKS]
    commands += [['git', 'diff', '--check']]
    def source_hashes():
        paths = sorted(p for p in (ROOT / 'app/src').rglob('*') if p.is_file() and p.suffix in ('.kt', '.java', '.xml'))
        return {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest() for p in paths}
    before = source_hashes()
    rows = []
    for command in commands:
        proc = subprocess.run(command, cwd=ROOT, text=True, capture_output=True,
                              env={**os.environ, 'PYTHONDONTWRITEBYTECODE': '1'}, timeout=180)
        rows.append({'command': command, 'exit_code': proc.returncode,
                     'stdout': proc.stdout, 'stderr': proc.stderr})
        print(('PASS' if proc.returncode == 0 else 'FAIL') + ' ' + ' '.join(command[1:]), flush=True)
    after = source_hashes()
    unchanged = before == after
    report = {
        'status': 'PASS_SOURCE_ONLY' if unchanged and all(r['exit_code'] == 0 for r in rows) else 'FAIL_SOURCE_ONLY',
        'generated_at_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(),
        'baseline': '23cb6dd98805445174bd83b7237ad5f5d2c24ac9',
        'checkpoint_parent': subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip(),
        'checks': rows,
        'source_unchanged_through_checks': unchanged,
        'source_sha256': after,
        'android_compilation': 'UNRUN_TOOLCHAIN_ARTIFACT_ACCESS_BLOCKED',
        'android_tests': 'UNRUN_ON_CURRENT_SOURCE',
        'limits': 'Source/mutation checks do not establish compilation, resource linking, runtime, visual or accessibility parity.',
    }
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, indent=2) + '\n')
    print(f"{report['status']}: {sum(r['exit_code'] == 0 for r in rows)}/{len(rows)} source commands", flush=True)
    return 0 if report['status'] == 'PASS_SOURCE_ONLY' else 1


if __name__ == '__main__':
    sys.exit(main())
