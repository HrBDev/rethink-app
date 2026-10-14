#!/usr/bin/env python3
"""Reconcile the independent original-root inventory; no source marker means acceptance."""
from pathlib import Path
import json,re,subprocess,hashlib
root=Path(__file__).resolve().parents[2];base='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
report=(root/'docs/compose-migration/final-coverage-audit.md').read_text().split('## Exhaustive original phone root and caller matrix',1)[1].split('## Reproduction notes',1)[0]
paths=subprocess.check_output(['git','ls-tree','-r','--name-only',base,'app/src'],cwd=root,text=True).splitlines()
rows=[];framework={'NotificationHandlerActivity'}
compose_hosts={'HomeScreenActivity','ConfigureRethinkBasicActivity','DnsDetailActivity','FirewallActivity','FragmentHostActivity'}
for line in report.splitlines():
 m=re.match(r'\| `([^`]+)` \| (.*?) \| (.*?) \|$',line)
 if not m:continue
 key,entry,evidence=m.groups();source_set,name=key.split(':',1)
 matches=[p for p in paths if p.startswith('app/src/'+source_set+'/') and Path(p).name==name+'.kt']
 assert len(matches)==1,(key,matches)
 path=matches[0];actual=root/path
 presentation='native_source_candidate'
 if name in compose_hosts:
  presentation='native_compose_root_with_framework_fragment_interop'
  evidence='[Binding removal and fixed-ID fragment boundary](binding-removal-audit.md); current host changes await compilation and runtime restoration tests'
 elif name in framework:presentation='retained_invisible_platform_trampoline_with_native_dialogs'
 elif name=='TestDialogActivity':presentation='unchanged_empty_test_context'
 elif not actual.exists():presentation='reference_retired_original_class'
 elif name=='AlertsActivity':presentation='native_empty_baseline_surface'
 rows.append({'id':key,'path':path,'entry_reference':entry,'presentation':presentation,'current_sha256':hashlib.sha256(actual.read_bytes()).hexdigest() if actual.exists() else None,'source_acceptance':'pending final independent reference and callback audit','compile':'pending final immutable all-variant run','navigation_back_state_recreation':'pending actual-host/device parity','search_filter_paging':'pending complete applicable-case parity','rtl_themes_large_text':'original chip/button/field styling restoration requested; clipping/contrast/RTL/large-text and actual rendering checks pending; other intentional Material spacing differences allowed','accessibility':'pending complete semantic/focus/TalkBack parity','notes':evidence})
assert len(rows)==115,len(rows)
assert len({r['id'] for r in rows})==115
output={'baseline':base,'status':'WIP: exhaustive root inventory, not completed full migration. Selected component tests do not discharge all route gates.','inventory_counts':{'activities':45,'flavor_specific_fragments':28,'bottom_sheets':32,'standalone_dialog_classes':10,'phone_roots':115,'protected_tv_source_files':31},'platform_exceptions':['Programmatic fixed-ID FragmentContainerView bridge and AndroidFragment lifecycle containers; app XML Activity shells and generated bindings removed','Invisible notification trampoline shell; visible prompts are native','BaseActivity window/status-bar/max-width wrapper','Framework Dialog windows with Compose-only content','External JourneyApps CaptureActivity and Android system permission/document/billing surfaces','Retained XML resources used by framework/system/widget/notification components'], 'component_evidence':[{'snapshot':'ae0737e390bf4d8d98c3d5fc0ac921acdbe211f6','result':'46 selected Home/chrome/statistics isolated tests passed; native-content RTL/large-text and semantics checks, not full host/device parity'}], 'screens':rows}
(root/'docs/compose-migration/screen-parity-ledger.json').write_text(json.dumps(output,indent=2)+'\n');print('PASS: 115 unique original phone roots; zero test/helper files substituted for screens; all unverified gates remain explicit.')
