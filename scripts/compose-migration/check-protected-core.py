#!/usr/bin/env python3
"""Recompute baseline equality for non-UI production logic, manifests, TV and version pins."""
from pathlib import Path
import json,subprocess,hashlib,tomllib,sys
ROOT=Path(__file__).resolve().parents[2]
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
def git(*args):return subprocess.check_output(['git',*args],cwd=ROOT)
paths=git('ls-tree','-r','--name-only',BASE).decode().splitlines()
helpers=['GuidedTourManager.kt','RpnOnboardingManager.kt','RulesImportHelper.kt','CountryInsightsMapper.kt','StatsInsightsMath.kt','LauncherSwitcher.kt']
results=[];changed=[]
for path in paths:
    if not path.startswith('app/src/'):continue
    protect=(path.endswith('AndroidManifest.xml') or ('/java/' in path and '/ui/' not in path and '/adapter/' not in path and not path.endswith('/util/UIUtils.kt') and not path.endswith('/iab/stripe/PricesAdapter.kt')) or '/tv/' in path or any(path.endswith('/'+h) for h in helpers))
    if not protect:continue
    old=git('show',BASE+':'+path);new=(ROOT/path).read_bytes() if (ROOT/path).exists() else b''
    same=old==new
    results.append({'path':path,'sha256':hashlib.sha256(old).hexdigest(),'unchanged':same})
    if not same:changed.append(path)
baseline_catalog=tomllib.loads(git('show',BASE+':gradle/lib.toml').decode())
current_catalog=tomllib.loads((ROOT/'gradle/lib.toml').read_text())
# The explicit request to remove UI binding also removes its unused dependency.
# Every other original version and library alias must still match exactly.
removed_versions={'viewbinding': '2.0.4'}
removed_libraries={'devAndroidbroadcastVbpd': {'module': 'dev.androidbroadcast.vbpd:vbpd', 'version': {'ref': 'viewbinding'}}}
assert all(baseline_catalog['versions'].get(k)==v for k,v in removed_versions.items())
assert all(baseline_catalog['libraries'].get(k)==v for k,v in removed_libraries.items())
expected_versions={k:v for k,v in baseline_catalog['versions'].items() if k not in removed_versions}
pins=current_catalog['versions']==expected_versions
aliases=all(current_catalog['libraries'].get(k)==v for k,v in baseline_catalog['libraries'].items() if k not in removed_libraries)
binding_removed=not any(k in current_catalog['libraries'] for k in removed_libraries)
plugins=current_catalog['plugins']==baseline_catalog['plugins']
report={'baseline':BASE,'count':len(results),'unchanged':not changed,'changed':changed,'retainedVersionPinsUnchanged':pins,'retainedLibraryAliasesUnchanged':aliases,'pluginAliasesUnchanged':plugins,'authorizedRemovedVersions':removed_versions,'authorizedRemovedLibraries':removed_libraries,'bindingAliasRemoved':binding_removed,'files':results}
(ROOT/'docs/compose-migration/protected-core-report.json').write_text(json.dumps(report,indent=2)+'\n')
print('Protected',len(results),'files; changed:',changed,'; retained dependency pins/aliases unchanged:',pins and aliases and plugins,'; obsolete binding alias removed:',binding_removed)
sys.exit(0 if not changed and pins and aliases and plugins and binding_removed else 1)
