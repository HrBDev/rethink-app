#!/usr/bin/env python3
"""Freeze baseline presentation attributes for native Compose controls, never inflate layouts."""
import json,re,subprocess,xml.etree.ElementTree as ET
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
DATA=json.loads((ROOT/'docs/compose-migration/component-style-restoration-inventory.json').read_text())
KEEP={'textAppearance','textSize','textColor','fontFamily','textStyle','letterSpacing','minWidth','minHeight','padding','paddingHorizontal','paddingVertical','paddingStart','paddingEnd','paddingLeft','paddingRight','paddingTop','paddingBottom','insetLeft','insetRight','insetTop','insetBottom','background','backgroundTint','cornerRadius','strokeWidth','strokeColor','rippleColor','shapeAppearance','shapeAppearanceOverlay','chipCornerRadius','chipMinHeight','chipMinTouchTargetSize','chipBackgroundColor','chipSurfaceColor','chipStrokeWidth','chipStrokeColor','chipStartPadding','chipEndPadding','textStartPadding','textEndPadding','chipIconSize','chipIconVisible','chipIcon','chipIconTint','icon','iconSize','iconPadding','iconTint','checkedIcon','checkedIconVisible','checkedIconTint','textAllCaps','elevation','checkable'}
# The unqualified baseline layout owns styling; wide layout changes are layout worker scope.
components={}
for c in DATA['baseline_components']+DATA['baseline_action_like_text_image_candidates']:
 if c['tag'] not in ('Chip','MaterialButton','AppCompatTextView','MaterialTextView','TextView','AppCompatButton','Button'): continue
 key=Path(c['path']).stem+':'+(c['id'] or '(anonymous)')
 if key in components and '/layout/' not in c['path']: continue
 components[key]=c
# Passive purchase badges were not action candidates in the census; preserve their source styles too.
for layout, ids in {"list_item_server_order": {"tv_type_badge", "tv_status_chip", "tv_test_badge"}, "list_item_purchase_history": {"tv_state_badge"}, "activity_wg_config_editor": {"save_tunnel"}, "fragment_sponsor": {"pay_with_stripe_link"}, "dialog_set_dns_proxy": {"dialog_dns_proxy_lockdown_desc"}}.items():
 path=f"app/src/main/res/layout/{layout}.xml"
 if layout in ("fragment_sponsor", "dialog_set_dns_proxy"):path=path.replace("src/main", "src/full")
 xml=ET.fromstring(subprocess.check_output(["git", "show", DATA["baseline"]+":"+path],cwd=ROOT,text=True))
 for element in xml.iter():
  attributes={k.replace("{http://schemas.android.com/apk/res/android}","android:").replace("{http://schemas.android.com/apk/res-auto}","app:"):v for k,v in element.attrib.items()}
  id=attributes.get("android:id","").split("/")[-1]
  if id in ids:components[layout+":"+id]={"tag":"TextView","attributes":attributes,"style":attributes.get("style"),"path":path,"id":id}
resources=ET.Element('resources'); lines=[]
for key,c in sorted(components.items()):
 name='OriginalControl_'+re.sub('[^a-zA-Z0-9_]','_',key)
 style=ET.SubElement(resources,'style',{'name':name,'parent':c['style'] or ''})
 for k,v in c['attributes'].items():
  if ':' not in k or k.startswith('tools:') or k.split(':')[-1] not in KEEP: continue
  nameattr=k[4:] if k.startswith('app:') else k
  ET.SubElement(style,'item',{'name':nameattr}).text=v
 kind='Chip' if c['tag']=='Chip' else 'Button' if c['tag'] in ('MaterialButton','Button','AppCompatButton') else 'Text'
 height=c['attributes'].get('android:layout_height','')
 # A fixed visual height is not a touch target. Native controls retain >=48dp touch area.
 h=float(height[:-2]) if height.endswith('dp') else 0
 width=c['attributes'].get('android:layout_width','')
 w=float(width[:-2]) if width.endswith('dp') else 0
 inherited={k.removeprefix('app:'):v for k,v in c.get('local_style_plus_layout_attributes',{}).items()}
 inherited.update({k.removeprefix('app:'):v for k,v in c['attributes'].items()})
 nulls=[('android.R.attr.'+k.split(':')[-1]) if k.startswith('android:') else ('MaterialR.attr.'+k) for k,v in inherited.items() if v=='@null' and k.split(':')[-1] in KEEP and not k.startswith('tools:')]
 null_expression='setOf('+', '.join(sorted(set(nulls)))+')' if nulls else 'emptySet()'
 lines.append(f'    "{key}" to OriginalControlSpec(R.style.{name}, OriginalControlKind.{kind}, {h}f, {str('app:shapeAppearance' in c['attributes'] or 'app:shapeAppearanceOverlay' in c['attributes']).lower()}, {w}f, {null_expression}),')
# Programmatic Chip(context) constructions in baseline ServerSelectionFragment:3262,3610.
for key,items in {
 "@rpn_filter": {"chipMinHeight":"40dp", "chipStartPadding":"12dp", "chipEndPadding":"12dp", "chipStrokeWidth":"1dp", "checkedIconVisible":"false", "chipBackgroundColor":"@color/original_rpn_filter_fill", "chipStrokeColor":"@color/original_rpn_filter_stroke", "android:textColor":"@color/original_rpn_filter_text"},
 "@rpn_frequent": {"chipMinHeight":"40dp", "chipStartPadding":"12dp", "chipEndPadding":"12dp", "chipStrokeWidth":"1dp", "chipStrokeColor":"?attr/border", "chipBackgroundColor":"?attr/colorSurfaceVariant", "android:textColor":"?attr/primaryTextColor", "android:textSize":"13sp", "checkedIconVisible":"false"}
}.items():
 name="OriginalControl_"+key[1:]
 style=ET.SubElement(resources,"style",{"name":name,"parent":""})
 for k,v in items.items():ET.SubElement(style,"item",{"name":k}).text=v
 lines.append(f'    "{key}" to OriginalControlSpec(R.style.{name}, OriginalControlKind.Chip),')
for qualifier in sorted({Path(c['path']).parent.name.replace('layout','values',1) for c in DATA['baseline_components'] if '/layout-' in c['path']}):
 qualified=ET.Element('resources')
 for c in DATA['baseline_components']:
  if Path(c['path']).parent.name.replace('layout','values',1)!=qualifier or c['tag'] not in ('Chip','MaterialButton'):continue
  key=Path(c['path']).stem+':'+(c['id'] or '(anonymous)')
  name='OriginalControl_'+re.sub('[^a-zA-Z0-9_]','_',key)
  style=ET.SubElement(qualified,'style',{'name':name,'parent':c['style'] or ''})
  for k,v in c['attributes'].items():
   if ':' in k and not k.startswith('tools:') and k.split(':')[-1] in KEEP:
    ET.SubElement(style,'item',{'name':k[4:] if k.startswith('app:') else k}).text=v
 ET.indent(qualified,space='    ')
 directory=ROOT/'app/src/main/res'/qualifier;directory.mkdir(exist_ok=True)
 (directory/'original_control_styles.xml').write_text('<?xml version="1.0" encoding="utf-8"?>\n'+ET.tostring(qualified,encoding='unicode')+'\n')
ET.indent(resources,space='    ')
(ROOT/'app/src/main/res/values/original_control_styles.xml').write_text('<?xml version="1.0" encoding="utf-8"?>\n<!-- Generated from 23cb6dd; style attributes only. No layouts are inflated. -->\n'+ET.tostring(resources,encoding='unicode')+'\n')
kt='''/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.controls

import com.celzero.bravedns.R
import com.google.android.material.R as MaterialR

internal enum class OriginalControlKind { Button, Chip, Text }
internal data class OriginalControlSpec(val style: Int, val kind: OriginalControlKind, val visualHeight: Float = 0f, val localShapeOverride: Boolean = false, val visualWidth: Float = 0f, val explicitNulls: Set<Int> = emptySet())

/** Generated baseline source keys; style resources resolve in the current Android theme. */
internal val originalControlSpecs = mapOf(
'''+ '\n'.join(lines)+'''
    "@dialog_positive" to OriginalControlSpec(R.style.App_AlertDialog_PositiveButton, OriginalControlKind.Button),
    "@dialog_negative" to OriginalControlSpec(R.style.App_AlertDialog_NegativeButton, OriginalControlKind.Button),
    "@dialog_neutral" to OriginalControlSpec(R.style.App_AlertDialog_NeutralButton, OriginalControlKind.Button),
    "@filled" to OriginalControlSpec(com.google.android.material.R.style.Widget_Material3_Button, OriginalControlKind.Button),
    "@outlined" to OriginalControlSpec(com.google.android.material.R.style.Widget_Material3_Button_OutlinedButton, OriginalControlKind.Button),
    "@text" to OriginalControlSpec(com.google.android.material.R.style.Widget_Material3_Button_TextButton, OriginalControlKind.Button),
    "@dialog" to OriginalControlSpec(com.google.android.material.R.style.Widget_Material3_Button_TextButton_Dialog, OriginalControlKind.Button),
    "@filter" to OriginalControlSpec(com.google.android.material.R.style.Widget_Material3_Chip_Filter, OriginalControlKind.Chip),
    "@assist" to OriginalControlSpec(com.google.android.material.R.style.Widget_Material3_Chip_Assist, OriginalControlKind.Chip),
    "@action" to OriginalControlSpec(R.style.ThinnerChip_Action, OriginalControlKind.Chip),
    "@large" to OriginalControlSpec(R.style.ThinnerChip_Large, OriginalControlKind.Chip),
    "@activity_time" to OriginalControlSpec(R.style.ActivityTimeSegment, OriginalControlKind.Button),
    "@connection_rule" to OriginalControlSpec(R.style.ConnTrackerSegment, OriginalControlKind.Button),
)
'''
(ROOT/'app/src/main/java/com/celzero/bravedns/ui/compose/controls/OriginalControlCatalog.kt').write_text(kt)
print(f'Generated {len(components)} source control styles.')
