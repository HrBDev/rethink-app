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
pins=tomllib.loads(git('show',BASE+':gradle/lib.toml').decode())['versions']==tomllib.loads((ROOT/'gradle/lib.toml').read_text())['versions']
report={'baseline':BASE,'count':len(results),'unchanged':not changed,'changed':changed,'versionPinsUnchanged':pins,'files':results}
(ROOT/'docs/compose-migration/protected-core-report.json').write_text(json.dumps(report,indent=2)+'\n')
print('Protected',len(results),'files; changed:',changed,'; dependency version pins unchanged:',pins)
sys.exit(0 if not changed and pins else 1)
