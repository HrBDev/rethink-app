#!/usr/bin/env python3
"""Pinned exact-method body guard. It does not claim UI, runtime, or all-controller equivalence."""
import json,sys
from rpn_source import ROOT,UI,baseline,functions,normalize
ledger=json.loads((ROOT/'scripts/compose-migration/rpn-controller-functions.json').read_text())
failed=[];passed=0
for path,names in ledger.items():
    old=functions(baseline(path));new=functions((ROOT/UI/path).read_text())
    for name in names:
        if name not in old or name not in new or normalize(old[name][1]) != normalize(new[name][1]):failed.append(path+':'+name)
        else:passed+=1
if failed:print('FAIL:\n'+'\n'.join(failed));sys.exit(1)
print(f'PASS: {passed} fixed named controller/helper bodies match pinned baseline after documented presentation-only normalization.')
