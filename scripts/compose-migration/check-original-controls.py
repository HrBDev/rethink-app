#!/usr/bin/env python3
"""Static source/resource safeguards only. Does not compile Kotlin or run Android tests."""
from pathlib import Path
import collections, json, re, subprocess, sys, xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2]
DOC=ROOT/'docs/compose-migration'
KOTLIN=ROOT/'app/src/main/java/com/celzero/bravedns/ui/compose/controls'
errors=[]
catalog=(KOTLIN/'OriginalControlCatalog.kt').read_text()
keys=set(re.findall(r'^    "([^"]+)" to',catalog,re.M))
styles=ET.parse(ROOT/'app/src/main/res/values/original_control_styles.xml').getroot()
style_names={e.attrib['name'] for e in styles}
for name in re.findall(r'(?<!\.)R\.style\.(OriginalControl_\w+)',catalog):
 if name not in style_names:errors.append('missing style '+name)
# Literal wrapper sources as well as direct renderer calls must exist, not only a family count.
for p in (ROOT/'app/src/main/java').rglob('*.kt'):
 for match in re.finditer(r'(?:OriginalButton|OriginalChip|OriginalLabel)\("([^"$]+)"\s*,|\bsource\s*=\s*"([^"$]+)"',p.read_text()):
  key=match[1] or match[2]
  if ':' in key and key not in keys:errors.append(f'missing key {key} in {p.relative_to(ROOT)}')

# Validate per-style null metadata, including inherited local style values.
inv=json.loads((DOC/'component-style-restoration-inventory.json').read_text())
nulls_checked=0
for c in inv['baseline_components']:
 key=Path(c['path']).stem+':'+(c['id'] or '(anonymous)')
 if key not in keys:continue
 values={k.removeprefix('app:'):v for k,v in c.get('local_style_plus_layout_attributes',{}).items()}
 values.update({k.removeprefix('app:'):v for k,v in c['attributes'].items()})
 row=next(line for line in catalog.splitlines() if line.startswith(f'    "{key}" to'))
 for name,value in values.items():
  if value=='@null' and name in ('chipSurfaceColor','checkedIcon','chipBackgroundColor','android:backgroundTint'):
   nulls_checked+=1
   attr=('android.R.attr.' if name.startswith('android:') else 'MaterialR.attr.')+name.split(':')[-1]
   if attr not in row:errors.append(f'null metadata missing {key} {name}')
primitive=(KOTLIN/'OriginalControls.kt').read_text()
required=['spec.explicitNulls','hasValueOrEmpty','LocalConfiguration.current','LocalDensity.current',
 'getColorForState','checkedIconVisible','checkedIconTint','minimumInteractiveComponentSize',
 'interactive = false','Role.RadioButton','OriginalSegmentShape','LayoutDirection.Ltr',
 'complexToFloat(sizeValue.data)','utilityResolvedWeight','constantState?.newDrawable',
 'if (spec.visualHeight > 0f) spec.visualHeight','if (chip && has(26))',
 'Modifier.indication(interaction','indication = null']
for token in required:
 if token not in primitive:errors.append('missing safeguard '+token)
if re.search(r'AndroidView\s*\(|LayoutInflater|\.inflate\(',primitive):errors.append('View inflation in control renderer')

# Every logged native onClick expression must still be present verbatim modulo whitespace.
def arguments(s):
 out=[];stack=[];begin=0;quote=None;escape=False
 for i,c in enumerate(s):
  if quote:
   if escape:escape=False
   elif c=='\\':escape=True
   elif c==quote:quote=None
  elif c in ('"',"'"):quote=c
  elif c in '({[':stack.append(c)
  elif c in ')}]':stack.pop()
  elif c==',' and not stack:out.append(s[begin:i].strip());begin=i+1
 out.append(s[begin:].strip());return out
callbacks=[]
ledger=json.loads((DOC/'button-chip-integration-ledger.json').read_text())
for entry in ledger:
 call=entry['original'];kind=call[:call.index('(')];args=arguments(call[len(kind)+1:-1])
 named=next((re.sub(r'^onClick\s*=\s*','',arg) for arg in args if re.match(r'onClick\s*=',arg)),None)
 click=named if named is not None else args[1 if kind=='FilterChip' else 0]
 source=(ROOT/entry['path']).read_text()
 present=re.sub(r'\s+','',click) in re.sub(r'\s+','',source)
 if not present:errors.append('callback changed '+entry['path']+' '+click)
 callbacks.append({'path':entry['path'],'callback_preserved':present})
test=ROOT/'app/src/test/java/com/celzero/bravedns/ui/compose/controls/OriginalControlsTest.kt'
test_source=test.read_text()
if 'application = Application::class' not in test_source:errors.append('test does not use plain Application')
report={'verification':'Static source/resource checks only; Kotlin and synthetic tests NOT compiled or executed',
 'baseline':inv['baseline'],'catalog_source_keys':len(keys),'generated_style_resources':len(style_names),
 'explicit_null_overrides_checked':nulls_checked,'logged_callbacks_checked':len(callbacks),
 'synthetic_test_methods_authored_not_executed':len(re.findall(r'@Test fun ',test_source)),
 'passed':not errors,'errors':errors}
(DOC/'original-controls-source-report.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2));sys.exit(bool(errors))
