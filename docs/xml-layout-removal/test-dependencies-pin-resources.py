#!/usr/bin/env python3
"""Source-only baseline dependency closure for the five independent test references."""
from pathlib import Path
from collections import defaultdict
import hashlib,json,re,subprocess,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2];D=ROOT/'docs/xml-layout-removal';BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
files=subprocess.check_output(['git','ls-tree','-r','--name-only',BASE,'app/src'],cwd=ROOT,text=True).splitlines()
paths=[x for x in files if re.search(r'/res/(?:values|drawable|color|font|mipmap)(?:-[^/]+)?/[^/]+$',x)]
raws={};symbols=defaultdict(list)
for path in paths:
 raw=subprocess.check_output(['git','show',BASE+':'+path],cwd=ROOT);raws[path]=raw
 kind=path.split('/res/')[1].split('/')[0].split('-')[0]
 if kind=='values' and path.endswith('.xml'):
  for node in ET.fromstring(raw):
   name=node.attrib.get('name');typ=node.attrib.get('type',node.tag)
   if name:symbols[(typ,name)].append((path,node))
 else:
  try:node=ET.fromstring(raw) if path.endswith('.xml') else None
  except ET.ParseError:node=None
  symbols[(kind,Path(path).stem)].append((path,node))
refs=lambda value:[(m[1],m[2]) for m in re.finditer(r'@(?:\+)?(style|string|dimen|color|drawable|font|mipmap|integer|bool)/(\w[\w.]*)',value)]
queue=[('style',name) for name in ['AppThemeWhite','AppTheme','AppThemeTrueBlackFrost','AppThemeWhitePlus','AppThemeTrueBlackPlus']]
for path in D.glob('test-dependencies-*-contract.json'):
 c=json.loads(path.read_text())
 for node in c['nodes']:
  for value in node['source_attributes'].values():queue.extend(refs(value))
seen=set();pinpaths=set();unresolved=[]
while queue:
 symbol=queue.pop()
 if symbol in seen:continue
 seen.add(symbol)
 if symbol not in symbols:unresolved.append('/'.join(symbol));continue
 for path,node in symbols[symbol]:
  pinpaths.add(path)
  if node is None:continue
  if symbol[0]=='style':
   parent=node.attrib.get('parent')
   if parent:queue.append(('style',parent.removeprefix('@style/')))
   elif '.' in symbol[1]:queue.append(('style',symbol[1].rsplit('.',1)[0]))
  for item in node.iter():
   for value in list(item.attrib.values())+([item.text] if item.text else []):queue.extend(refs(value))
# Attribute declarations are part of runtime resource interpretation and must not drift silently.
pinpaths.update(p for p in paths if p.endswith('/attrs.xml'))
result={'kind':'immutable_baseline_resource_dependency_pins_not_android_resolution','baseline':BASE,'seed_themes':['AppThemeWhite','AppTheme','AppThemeTrueBlackFrost','AppThemeWhitePlus','AppThemeTrueBlackPlus'],'baseline_files':[{'path':p,'sha256':hashlib.sha256(raws[p]).hexdigest()} for p in sorted(pinpaths)],'unresolved_library_or_framework_resource_names':sorted(unresolved),'limits':['The Android/Material resource resolver is not executed here. Library/default behavior requires exact approved artifacts and later runtime checks.','Pins protect shared expected/actual inputs from moving together silently. Current drift fails instead of being accepted as a new baseline.','Source styles added in separate new files are permitted; the referenced original resources remain immutable.']}
(D/'test-dependencies-resource-pins.json').write_text(json.dumps(result,indent=2)+'\n')
print('Pinned',len(pinpaths),'baseline resource files;',len(unresolved),'library/framework symbols require actual approved resource resolution.')
for pin in result['baseline_files']:
 p=ROOT/pin['path'];assert p.is_file() and hashlib.sha256(p.read_bytes()).hexdigest()==pin['sha256'],pin['path']
print('PASS: all pinned referenced original resources match baseline bytes.')
