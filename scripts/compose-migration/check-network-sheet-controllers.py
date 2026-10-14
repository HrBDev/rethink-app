#!/usr/bin/env python3
"""Four network sheets: pinned-source controller guard, without starting Android or services."""
from pathlib import Path
import importlib.util, subprocess, json, sys
ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location('guard', Path(__file__).with_name('check-wireguard-controllers.py'))
g = importlib.util.module_from_spec(spec); spec.loader.exec_module(g)
FILES = {
    'OrbotBottomSheet.kt': {'onCreateView', 'animateOrbotIcon', 'getCalculatedWidth', 'showDialogForInfo'},
    'ProxyCountriesBtmSheet.kt': {'onCreateView', 'init', 'onCreateViewHolder', 'onBindViewHolder', 'bind'},
    'BackupRestoreBottomSheet.kt': {'onCreateView'},
    'RethinkInRethinkWarningBottomSheet.kt': {'onCreateView'},
}
SUBSTITUTIONS = [('MaterialAlertDialogBuilder', 'WgAlertDialogBuilder'),
                 ('b.orbotIcon.clearAnimation()', 'b.animating = false')]
results = []; errors = []
for filename, presentation in FILES.items():
    path = 'app/src/main/java/com/celzero/bravedns/ui/bottomsheet/' + filename
    old = subprocess.check_output(['git', 'show', g.BASELINE + ':' + path], cwd=ROOT, text=True)
    for before, after in SUBSTITUTIONS: old = old.replace(before, after)
    current = (ROOT / path).read_text()
    baseline_bodies = g.bodies(old); current_bodies = g.bodies(current)
    for name, body in baseline_bodies.items():
        if name in presentation: continue
        passed = body == current_bodies.get(name)
        results.append({'path': path, 'method': name, 'passed': passed,
                        'baselineAdaptedSha256': g.digest(body), 'currentSha256': g.digest(current_bodies.get(name, []))})
        if not passed: errors.append(path + '::' + name)
    if 'onCreateView' not in current_bodies: errors.append(path + '::missing native host')
    for forbidden in ['Binding.inflate(', 'AndroidView(', 'RecyclerView.Adapter', 'MaterialAlertDialogBuilder(']:
        if forbidden in current: errors.append(path + '::legacy presentation ' + forbidden)
# Presentation-only extraction of the existing info HTML must remain exact.
orbot = (ROOT / 'app/src/main/java/com/celzero/bravedns/ui/bottomsheet/OrbotBottomSheet.kt').read_text()
for line in ['var text = getString(R.string.orbot_explanation)', 'text = text.replace("\\n", "<br /><br />")',
             'val styledText = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)']:
    if line not in orbot: errors.append('Orbot info text transform changed: ' + line)
# The offscreen typography oracle must remain the pinned XML, not a second migrated fixture.
for path in ['app/src/full/res/layout/dialog_info_rules_layout.xml', 'app/src/full/res/layout/bottom_sheet_orbot.xml',
             'app/src/main/res/layout/activity_backup_restore.xml', 'app/src/main/res/layout/bottomsheet_rinr_warning.xml',
             'app/src/main/res/layout/bottom_sheet_proxies_list.xml', 'app/src/main/res/layout/list_item_proxy_cc_wg.xml']:
    before = subprocess.check_output(['git', 'show', g.BASELINE + ':' + path], cwd=ROOT)
    if before != (ROOT / path).read_bytes(): errors.append('Baseline XML fixture changed: ' + path)
g.self_test()
report = {'baseline': g.BASELINE, 'kind': 'network-sheet token equality with explicit presentation substitutions',
          'passed': not errors, 'count': len(results), 'methods': results,
          'excludedPresentation': {k: sorted(v) for k, v in FILES.items()}, 'substitutions': SUBSTITUTIONS,
          'limits': ['Source equality does not establish compilation, Android behavior, rendering or accessibility parity.',
                     'Presentation exclusions cover native host creation, country adapter rows, info window and icon animation only.']}
(ROOT / 'docs/compose-migration/network-sheets-controller-source-report.json').write_text(json.dumps(report, indent=2) + '\n')
if errors: print('\n'.join(errors)); sys.exit(1)
print('PASS', len(results), 'protected baseline network-sheet bodies; not runtime or visual verification')
