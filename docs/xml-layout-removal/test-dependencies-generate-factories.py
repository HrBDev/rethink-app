#!/usr/bin/env python3
"""Review-time typed test source generation from immutable Git baseline.

Writes Kotlin widget constructors and scalar property contracts, never XML or Android
resources. Not connected to Gradle or runtime. Factories do not load these contracts.
"""
from pathlib import Path
import hashlib,json,subprocess,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2]
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
OUT=ROOT/'app/src/test/java/com/celzero/bravedns/ui/compose/reference'
DOC=ROOT/'docs/xml-layout-removal'
A='{http://schemas.android.com/apk/res/android}'
APP='{http://schemas.android.com/apk/res-auto}'
CLASSES={'LinearLayout':'android.widget.LinearLayout','RelativeLayout':'android.widget.RelativeLayout','FrameLayout':'android.widget.FrameLayout','View':'android.view.View','TextView':'android.widget.TextView','ImageView':'android.widget.ImageView'}
LAYOUT_PARAMS={
'androidx.coordinatorlayout.widget.CoordinatorLayout':'androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams',
'com.google.android.material.appbar.AppBarLayout':'com.google.android.material.appbar.AppBarLayout.LayoutParams',
'com.google.android.material.appbar.CollapsingToolbarLayout':'com.google.android.material.appbar.CollapsingToolbarLayout.LayoutParams',
'androidx.core.widget.NestedScrollView':'android.widget.FrameLayout.LayoutParams',
'android.widget.LinearLayout':'android.widget.LinearLayout.LayoutParams',
'android.widget.RelativeLayout':'android.widget.RelativeLayout.LayoutParams',
'com.google.android.material.card.MaterialCardView':'android.widget.FrameLayout.LayoutParams',
'com.google.android.material.chip.ChipGroup':'com.google.android.material.chip.ChipGroup.LayoutParams',
'com.google.android.material.textfield.TextInputLayout':'android.widget.LinearLayout.LayoutParams',
}
def quote(v):return json.dumps(v,ensure_ascii=False).replace('$','\\$')
def attr(k):
 if k.startswith(A):return 'android.R.attr.'+k[len(A):]
 if k.startswith(APP):
  name=k[len(APP):]
  if name in ('cardCornerRadius','cardElevation','cardBackgroundColor'):return 'androidx.cardview.R.attr.'+name
  if name=='layout_behavior':return 'androidx.coordinatorlayout.R.attr.'+name
  if name in ('srcCompat','tint','drawableStartCompat'):return 'androidx.appcompat.R.attr.'+name
  return 'com.google.android.material.R.attr.'+name
 return None

def build(name,path,kind,select_id=None):
 raw=subprocess.check_output(['git','show',BASE+':'+path],cwd=ROOT)
 tree=ET.fromstring(raw)
 if select_id:tree=next(n for n in tree.iter() if n.attrib.get(A+'id')=='@+id/'+select_id)
 nodes=[]; ids={}; lines=[]
 def visit(node,parent=None,parentclass=None):
  index=len(nodes);var='reference'+str(index);cls=CLASSES.get(node.tag,node.tag)
  context = parent+'.context' if parent else 'context'
  attributes={k.replace(A,'android:').replace(APP,'app:'):v for k,v in node.attrib.items() if not k.startswith('{http://schemas.android.com/tools}')}
  nodes.append({'variable':var,'widget':cls,'source_attributes':attributes,'parent_variable':parent,'layout_params_class':LAYOUT_PARAMS.get(parentclass)})
  id_=node.attrib.get(A+'id','').split('/')[-1]
  if id_:ids[id_]=var
  props=[(attr(k),v.replace('@+id/','@id/')) for k,v in node.attrib.items() if attr(k)]
  style=node.attrib.get('style')
  lines.append(f'        val {var}Attributes = referenceAttributes({quote(style) if style else "null"}'+(',' if props else ')'))
  if props:
   lines.extend(f'            {k} to {quote(v)}'+(',' if i<len(props)-1 else '') for i,(k,v) in enumerate(props))
   lines.append('        )')
  if node.tag=='TextView' and kind=='sponsor':
   lines.extend([f'        val {var}: android.widget.TextView = when (widgetMode) {{',f'            BaselineWidgetMode.PLATFORM -> android.widget.TextView({context}, {var}Attributes)',f'            BaselineWidgetMode.MATERIAL_COMPAT -> com.google.android.material.textview.MaterialTextView({context}, {var}Attributes)','        }'])
  elif node.tag=='ImageView' and kind=='sponsor':
   lines.extend([f'        val {var}: android.widget.ImageView = when (widgetMode) {{',f'            BaselineWidgetMode.PLATFORM -> android.widget.ImageView({context}, {var}Attributes)',f'            BaselineWidgetMode.MATERIAL_COMPAT -> androidx.appcompat.widget.AppCompatImageView({context}, {var}Attributes)','        }'])
  else:lines.append(f'        val {var} = {cls}({context}, {var}Attributes)')
  if parent:
   lp=LAYOUT_PARAMS[parentclass]
   lines.append(f'        val {var}LayoutParams = {lp}({context}, {var}Attributes)')
  for child in node:visit(child,var,cls)
  lines.append(f'        finishReferenceConstruction({var})')
  if parent:lines.append(f'        {parent}.addView({var}, {var}LayoutParams)')
  return var
 root=visit(tree)
 sources={
 'sponsor':('BaselineSponsorSuccessViews',[('root','android.view.View',root),('title','android.widget.TextView',ids['success_title']),('message','android.widget.TextView',ids['success_message']),('continueButton','com.google.android.material.button.MaterialButton',ids['continue_button']),('emblem','android.widget.ImageView',ids['success_emblem'])],'context: android.content.Context, widgetMode: BaselineWidgetMode = BaselineWidgetMode.PLATFORM'),
 } if kind=='sponsor' else {}
 if kind=='support':
  def textvar(resource):return next(n['variable'] for n in nodes if n['source_attributes'].get('android:text')=='@string/'+resource)
  sources[kind]=('BaselineSupportViews',[('root','android.view.View',root),('collapsingToolbar','com.google.android.material.appbar.CollapsingToolbarLayout',ids['collapsing_toolbar']),('heroSubtitle','android.widget.TextView',ids['tv_hero_subtitle']),('sectionDescribe','android.widget.TextView',textvar('support_section_describe')),('attachmentSubscriptionTitle','android.widget.TextView',textvar('support_attach_sub_status'))],'context: android.content.Context, title: String = "", subtitle: String = ""')
  lines += [f'        {ids["collapsing_toolbar"]}.title = title',f'        {ids["tv_hero_subtitle"]}.text = subtitle']
 if kind=='orbot':
  sources[kind]=('BaselineOrbotInfoViews',[('root','android.view.View',root),('title','android.widget.TextView',ids['info_rules_dialog_rules_title']),('description','android.widget.TextView',ids['info_rules_dialog_rules_desc']),('closeImage','android.widget.ImageView',ids['info_rules_dialog_cancel_img'])],'context: android.content.Context, title: CharSequence? = null, description: CharSequence? = null')
  lines += [f'        if (title != null) {ids["info_rules_dialog_rules_title"]}.text = title',f'        if (description != null) {ids["info_rules_dialog_rules_desc"]}.text = description']
 if kind=='reachability':sources[kind]=('BaselineReachabilityViews',[('ipv4EditText','androidx.appcompat.widget.AppCompatEditText',root)],'context: android.content.Context')
 if kind=='statistics':sources[kind]=('BaselineStatisticsFilterViews',[('group','com.google.android.material.chip.ChipGroup',root)],'context: android.content.Context')
 result,fields,args=sources[kind]
 header='''/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.reference

/** Independent pinned-baseline widget reference; test only, never production UI.
 * These are explicit Android constructors and original scalar attributes, not XML inflation.
 * Review-time generator: docs/xml-layout-removal/test-dependencies-generate-factories.py.
 * Baseline: '''+BASE+':'+path+'''\n * Runtime/compilation remain unverified; the reviewed launcher is BLOCKED.
 */
'''
 text=header+f'internal data class {result}(\n'+',\n'.join(f'    val {field}: {typ}' for field,typ,var in fields)+f',\n    val source: BaselineReferenceSource = BaselineReferenceSources.{kind}\n)\n\n'
 text+=f'internal object {name} {{\n    fun create({args}): {result} {{\n'+'\n'.join(lines)+'\n'
 text+=f'        return {result}('+', '.join(var for field,typ,var in fields)+')\n    }\n}\n'
 target=OUT/(name+'.kt');target.write_text(text)
 contract={'baseline':BASE,'source_path':path,'baseline_source_sha256':hashlib.sha256(raw).hexdigest(),'factory_path':str(target.relative_to(ROOT)),'factory_sha256':hashlib.sha256(text.encode()).hexdigest(),'reference_scope':'complete original hierarchy' if not select_id else 'original subtree/control '+select_id,'nodes':nodes,'runtime_xml_inflation':False,'runtime_json_loading':False,'production_renderer_dependencies':False,'execution':'not compiled or run; runtime clearance BLOCKED'}
 (DOC/f'test-dependencies-{kind}-contract.json').write_text(json.dumps(contract,indent=2)+'\n')
 return kind,contract
OUT.mkdir(parents=True,exist_ok=True)
contracts=[build('BaselineSponsorSuccessReference','app/src/full/res/layout/fragment_sponsor_success.xml','sponsor'),
 build('BaselineSupportReference','app/src/main/res/layout/activity_customer_support.xml','support'),
 build('BaselineOrbotInfoReference','app/src/full/res/layout/dialog_info_rules_layout.xml','orbot'),
 build('BaselineReachabilityReference','app/src/main/res/layout/dialog_input_ips.xml','reachability','ipv4_address_1'),
 build('BaselineStatisticsFilterReference','app/src/main/res/layout/bottom_sheet_summary_stats_filter.xml','statistics','ssfc_chip_group')]
helperhash=hashlib.sha256((OUT/'BaselineReferenceAttributes.kt').read_bytes()).hexdigest()
provenance='''/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.reference

/** Source pins, not a runtime-validation claim. Updated only with reviewed fixture source. */
internal object BaselineReferenceSources {
'''
for kind,c in contracts:
 provenance+=f'    val {kind} = BaselineReferenceSource(\n'+''.join('        '+quote(v)+',\n' for v in (c['source_path'],c['baseline_source_sha256'],c['factory_path'],c['factory_sha256']))+'        '+quote(helperhash)+'\n    )\n'
provenance+='}\n';(OUT/'BaselineReferenceSources.kt').write_text(provenance)
print('Created five explicit programmatic test references; no XML/resource output or runtime executed.')
for kind,c in contracts:print(kind,len(c['nodes']),c['factory_sha256'])
