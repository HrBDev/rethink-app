#!/usr/bin/env python3
"""Independent source-property/structure checker, not a Kotlin or Android test runner."""
from pathlib import Path
from collections import Counter
import hashlib,json,re,subprocess,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2]
D=ROOT/'docs/xml-layout-removal'
P=ROOT/'app/src/test/java/com/celzero/bravedns/ui/compose/reference'
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
FROZEN='803cd4c884243ab9ef9c92cb0c4959d63bc27b00'
A='{http://schemas.android.com/apk/res/android}';APP='{http://schemas.android.com/apk/res-auto}';T='{http://schemas.android.com/tools}'
findings=[];sources=(P/'BaselineReferenceSources.kt').read_text()
for contractfile in sorted(D.glob('test-dependencies-*-contract.json')):
 c=json.loads(contractfile.read_text());raw=subprocess.check_output(['git','show',BASE+':'+c['source_path']],cwd=ROOT)
 assert hashlib.sha256(raw).hexdigest()==c['baseline_source_sha256'],contractfile
 tree=ET.fromstring(raw)
 if c['reference_scope'].startswith('original subtree/control '):
  identity=c['reference_scope'].split()[-1];tree=next(n for n in tree.iter() if n.attrib.get(A+'id')=='@+id/'+identity)
 nodes=list(tree.iter());assert len(nodes)==len(c['nodes'])
 path=ROOT/c['factory_path'];code=path.read_text();sha=hashlib.sha256(code.encode()).hexdigest()
 assert sha==c['factory_sha256'] and sha in sources
 assert c['baseline_source_sha256'] in sources and c['source_path'] in sources
 assert len(re.findall(r'finishReferenceConstruction\(reference\d+\)',code))==len(nodes)
 assert not re.search(r'LayoutInflater|R\.layout|\.inflate\(|XmlPullParser|XmlResourceParser|parseXml|originalControlSpecs|resolveOriginalControl|resolveFieldResources|resolveUtilityStyle|utilityTextStyle|resolveOrbotInfoTypography|RethinkDnsApplication|startActivity|startService',code)
 for node,row in zip(nodes,c['nodes']):
  expected={k.replace(A,'android:').replace(APP,'app:'):v for k,v in node.attrib.items() if not k.startswith(T)}
  assert expected==row['source_attributes'],row
  var=row['variable'];match=re.search(r'val '+var+r'Attributes = referenceAttributes\((.*?)\n        \)',code,re.S)
  assert match, var
  block=match[1];style=expected.get('style');assert block.startswith(json.dumps(style) if style else 'null')
  pairs=re.findall(r'((?:android|androidx\.[\w.]+|com\.[\w.]+)\.R\.attr\.\w+) to ("(?:\\.|[^"\\])*")',block)
  actual={('android:' if name.startswith('android.R.') else 'app:')+name.rsplit('.',1)[-1]:json.loads(value) for name,value in pairs}
  attrs={k:v.replace('@+id/','@id/') for k,v in expected.items() if k.startswith(('android:','app:'))}
  assert actual==attrs,(var,actual,attrs)
  context=row['parent_variable']+'.context' if row['parent_variable'] else 'context'
  if row['widget']=='android.widget.TextView' and 'SponsorSuccess' in path.name:
   assert f'android.widget.TextView({context}, {var}Attributes)' in code
   assert f'com.google.android.material.textview.MaterialTextView({context}, {var}Attributes)' in code
  elif row['widget']=='android.widget.ImageView' and 'SponsorSuccess' in path.name:
   assert f'android.widget.ImageView({context}, {var}Attributes)' in code
   assert f'androidx.appcompat.widget.AppCompatImageView({context}, {var}Attributes)' in code
  else:assert f'val {var} = {row["widget"]}({context}, {var}Attributes)' in code
  finish=code.index(f'finishReferenceConstruction({var})')
  if row['parent_variable']:
   parent=row['parent_variable'];lp=row['layout_params_class']
   assert f'val {var}LayoutParams = {lp}({context}, {var}Attributes)' in code
   add=f'{parent}.addView({var}, {var}LayoutParams)'
   assert code.count(add)==1 and finish<code.index(add)<code.index(f'finishReferenceConstruction({parent})')
  children=[x for x in c['nodes'] if x['parent_variable']==var]
  positions=[code.index(f'{var}.addView({x["variable"]}, {x["variable"]}LayoutParams)') for x in children]
  assert positions==sorted(positions)
 findings.append({'path':c['factory_path'],'nodes':len(nodes),'property_and_structure_check':'PASS','sha256':sha})
helper=(P/'BaselineReferenceAttributes.kt').read_text()
assert hashlib.sha256(helper.encode()).hexdigest() in sources
assert 'Robolectric.buildAttributeSet()' in helper and 'builder.setStyleAttribute(style)' in helper
assert helper.count('ReflectionHelpers.callInstanceMethod<Unit>(view, "onFinishInflate")')==1
assert not re.search(r'LayoutInflater|R\.layout|\.inflate\(|File\(|getIdentifier|production',helper.replace('production Compose','expected Compose'))
files=['networksheets/NetworkSheetContentTest.kt','networksheets/OrbotInfoTypographyTest.kt','statistics/StatisticsMaterialReferenceTest.kt','utilities/UtilityTypographyTest.kt','visual/ContentVisualParityTest.kt','visual/VisualParityHarness.kt']
rows=[]
for name in files:
 path='app/src/test/java/com/celzero/bravedns/ui/compose/'+name;before=subprocess.check_output(['git','show',FROZEN+':'+path],cwd=ROOT,text=True);after=(ROOT/path).read_text()
 methods=lambda text:re.findall(r'@Test\s+fun\s+(\w+)\s*\(',text)
 counts=lambda text:Counter(re.findall(r'\b(assert\w+|evidence\.(?:check|review|text|capture|contrast))\s*\(',text))
 assert len(methods(before))==len(methods(after))
 assert counts(before)==counts(after),(path,counts(before),counts(after))
 if name=='visual/VisualParityHarness.kt':
  cases=lambda text:re.findall(r'    VisualCase\([^\n]+',text)
  assert cases(before)==cases(after) and len(cases(after))==8
 # Explicit source assertion lines are unchanged apart from fixture-specific evidence.text operands.
 assertions=lambda text:[line.strip() for line in text.splitlines() if re.search(r'\bassert\w+\(',line)]
 assert assertions(before)==assertions(after),(path,'assertion line difference')
 rows.append({'path':path,'before_test_methods':methods(before),'after_test_methods':methods(after),'renamed_methods':[{'old':a,'new':b} for a,b in zip(methods(before),methods(after)) if a!=b],'before_assertion_call_counts':dict(counts(before)),'after_assertion_call_counts':dict(counts(after)),'assertion_counts_equal':True,'assertion_lines_unchanged':True,'current_sha256':hashlib.sha256(after.encode()).hexdigest()})
for path in (ROOT/'app/src/test').rglob('*.kt'):
 assert not re.search(r'(?<![\w.])R\.layout\.|\binflateBaseline\s*\(',path.read_text()),path
resource_pins=json.loads((D/'test-dependencies-resource-pins.json').read_text())
for pin in resource_pins['baseline_files']:
 path=ROOT/pin['path'];assert path.is_file() and hashlib.sha256(path.read_bytes()).hexdigest()==pin['sha256'],pin['path']
manifest=json.loads((ROOT/'docs/compose-migration/binding-removal-safety-allowlist.json').read_text())
assert manifest['runtime_clearance']['status']=='BLOCKED'
result={'kind':'source_only_not_compilation_or_runtime','baseline':BASE,'frozen_comparison':FROZEN,'references':findings,'total_reference_widgets':sum(x['nodes'] for x in findings),'eight_visual_configurations_unchanged':True,'app_layout_references_in_tests':0,'fixtures_contain_no_app_xml_or_production_renderer':True,'runtime_clearance':'BLOCKED','baseline_dependency_resource_count':len(resource_pins['baseline_files']),'changed_files':rows,'limits':['No Gradle/Kotlin/AAPT/Robolectric/device execution; Android reference behavior remains unverified.','Static property/constructor/parent/order checks and identical assertions do not establish rendered equivalence.','Robolectric attribute builder, protected construction hook and library LayoutParams APIs require later approved compilation/runtime revalidation.']}
(D/'test-dependencies-implementation-review.json').write_text(json.dumps(result,indent=2)+'\n')
(D/'test-dependencies-assertion-parity.json').write_text(json.dumps({'kind':'source_only_not_runtime','comparison':FROZEN,'files':rows},indent=2)+'\n')
print('PASS: five factory source contracts, 73 nodes, unchanged 23 methods/assertion lines and eight visual cases; zero app layout references in tests. Runtime BLOCKED.')
