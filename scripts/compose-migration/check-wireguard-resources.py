#!/usr/bin/env python3
"""WG-scoped local resource-name check; does not replace Android resource linking."""
import importlib.util, pathlib, re, sys, xml.etree.ElementTree as ET
ROOT=pathlib.Path(__file__).resolve().parents[2]
spec=importlib.util.spec_from_file_location('wg_controller_gate',pathlib.Path(__file__).with_name('check-wireguard-controllers.py'))
gate=importlib.util.module_from_spec(spec);spec.loader.exec_module(gate)
resources={}
for path in (ROOT/'app/src').glob('*/res/values*/*.xml'):
    for element in ET.parse(path).getroot():
        kind=element.attrib.get('type',element.tag);name=element.attrib.get('name')
        if name:resources.setdefault(kind,set()).add(name.replace('.','_') if kind=='style' else name)
for path in (ROOT/'app/src').glob('*/res/*/*'):
    kind=path.parent.name.split('-')[0]
    if kind!='values':resources.setdefault(kind,set()).add(path.stem)
base=ROOT/'app/src/main/java/com/celzero/bravedns'
paths=[]
for pattern in ['ui/compose/wireguard/*.kt','ui/activity/Wg*.kt','ui/dialog/Wg*.kt','ui/dialog/GenericHopDialog.kt','ui/bottomsheet/WireguardListBtmSheet.kt','ui/fragment/Wg*.kt','adapter/Wg*.kt','adapter/OneWgConfigAdapter.kt','adapter/GenericHopAdapter.kt']:
    paths.extend(base.glob(pattern))
errors=[]
for path in paths:
    _,source=gate.lex(path.read_text())
    for match in re.finditer(r'(?<![\w.])R\.(\w+)\.(\w+)',source):
        if match[2] not in resources.get(match[1],set()):errors.append(f'{path.relative_to(ROOT)}: {match[0]}')
if errors:
    print('\n'.join(errors));sys.exit(1)
print(f'PASS: local resource names in {len(paths)} WG Kotlin files; XML parse successful. Not Android resource linking or compilation.')
