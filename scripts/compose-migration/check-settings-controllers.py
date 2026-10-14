#!/usr/bin/env python3
"""Source guard for simple settings, menus and mode sheets. Never starts Android."""
from pathlib import Path
import importlib.util,subprocess,json,sys
ROOT=Path(__file__).resolve().parents[2]
spec=importlib.util.spec_from_file_location('wg_guard',Path(__file__).with_name('check-wireguard-controllers.py'));g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g)
FILES={
'ui/activity/AdvancedSettingActivity.kt':{'onCreate'},
'ui/activity/AntiCensorshipActivity.kt':{'onCreate'},
'ui/activity/UniversalFirewallSettingsActivity.kt':{'onCreate'},
'ui/fragment/ConfigureFragment.kt':set(),
'ui/fragment/FirewallSettingsFragment.kt':set(),
'ui/bottomsheet/HomeScreenSettingBottomSheet.kt':{'onCreateView'},
'ui/bottomsheet/BlockFreeDnsModeBottomSheet.kt':{'onCreateView'},
}
SUBSTITUTIONS={
'ui/activity/AdvancedSettingActivity.kt':[('MaterialAlertDialogBuilder','WgAlertDialogBuilder'),('SeekBar.OnSeekBarChangeListener','SettingsSeekBar.OnSeekBarChangeListener'),('seekBar: SeekBar?', 'seekBar: SettingsSeekBar?')],
'ui/activity/AntiCensorshipActivity.kt':[('CompoundButton','WgControlState')],
'ui/activity/UniversalFirewallSettingsActivity.kt':[('CompoundButton','WgControlState'),('MaterialAlertDialogBuilder','WgAlertDialogBuilder')],
'ui/bottomsheet/HomeScreenSettingBottomSheet.kt':[('CompoundButton','WgControlState')],
'ui/fragment/ConfigureFragment.kt':[('b.fhsTitleRethink.setText(R.string.app_name_alpha)','b.fhsTitleRethink.text = getString(R.string.app_name_alpha)')],
}
results=[];errors=[]
for relative,presentation in FILES.items():
 path='app/src/main/java/com/celzero/bravedns/'+relative
 old=subprocess.check_output(['git','show',g.BASELINE+':'+path],cwd=ROOT,text=True)
 for a,b in SUBSTITUTIONS.get(relative,[]):old=old.replace(a,b)
 before=g.bodies(old);after=g.bodies((ROOT/path).read_text())
 for name,body in before.items():
  if name in presentation:continue
  passed=after.get(name)==body
  results.append({'path':path,'method':name,'passed':passed,'baselineAdaptedSha256':g.digest(body),'currentSha256':g.digest(after.get(name,[]))})
  if not passed:errors.append(path+'::'+name)
 # Ensure presentation host functions retained and no protected baseline function vanished.
 for name in presentation:
  if name not in after:errors.append(path+'::'+name+' missing')
g.self_test()
report={'baseline':g.BASELINE,'kind':'settings token equality with explicit UI substitutions','count':len(results),'passed':not errors,'methods':results,'excludedPresentation':{k:sorted(v) for k,v in FILES.items()}}
(ROOT/'docs/compose-migration/settings-controller-source-report.json').write_text(json.dumps(report,indent=2)+'\n')
if errors:print('\n'.join(errors));sys.exit(1)
print('PASS',len(results),'protected settings/menu/mode-sheet bodies; not compilation or runtime parity')
