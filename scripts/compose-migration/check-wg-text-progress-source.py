#!/usr/bin/env python3
"""Pinned text/progress resource contracts. --write only regenerates presentation catalogs.
Source-only guard; Android tests remain UNRUN until an approved toolchain is available.
"""
import argparse, json, re, subprocess, xml.etree.ElementTree as ET
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
A='{http://schemas.android.com/apk/res/android}'; P='{http://schemas.android.com/apk/res-auto}'
LAYOUTS=['activity_wireguard_main','activity_wg_config_editor','activity_wg_detail','dialog_wg_add_peer','dialog_wg_ssid','dialog_wg_hop','dialog_wg_apps','list_item_wg_general_interface','list_item_wg_one_interface','list_item_wg_include_apps','list_item_wg_peers','list_item_wg_hop','fragment_wg_nw_stats','list_item_statistics_summary','bottom_sheet_proxies_list','list_item_proxy_cc_wg','activity_rpn_bypass_apps','activity_rpn_config_detail','dialog_rpn_ssid','list_item_rpn_bypass_app','item_ssid']
TEXT_ATTRS={'textAppearance','textSize','textColor','fontFamily','textStyle','textFontWeight','letterSpacing','lineSpacingExtra','lineSpacingMultiplier','lineHeight','includeFontPadding','maxLines','minLines','lines','singleLine','ellipsize','gravity','textAlignment','textAllCaps','alpha'}
PROGRESS_LAYOUTS=['activity_server_order_history','dialog_server_loading','list_item_wg_hop','list_item_statistics_summary','fragment_wg_nw_stats']

def source(layout):
 return ET.fromstring(subprocess.check_output(['git','show',f'{BASE}:app/src/main/res/layout/{layout}.xml'],cwd=ROOT,text=True))
def entries():
 result=[]
 for layout in LAYOUTS:
  for i,e in enumerate(source(layout).iter()):
   if not any(t in e.tag for t in ['TextView','CheckBox','RadioButton','Chip']): continue
   node=e.get(A+'id','').split('/')[-1] or f'anonymous_{i}'
   attrs={('android:' if k.startswith(A) else '')+k.split('}')[-1]:v for k,v in e.attrib.items() if k.split('}')[-1] in TEXT_ATTRS and (k.startswith(A) or k.startswith(P))}
   result.append((layout,node,e.tag,e.get('style',''),attrs))
 return result

def generated():
 rows=entries(); xml=['<?xml version="1.0" encoding="utf-8"?>','<resources>','    <!-- Pinned source '+BASE+'. Typography only; no retained layout inflation. -->']
 kt=['/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */','package com.celzero.bravedns.ui.compose.wireguard','','import com.celzero.bravedns.R','','internal data class WgTextSource(val style: Int, val material: Boolean, val defaultStyleAttr: Int)','','/** Pinned XML typography; generated and checked by check-wg-text-progress-source.py. */','internal val wgTextSources = mapOf(']
 for layout,node,tag,parent,attrs in rows:
  name=f'OriginalWgText_{layout}_{node}'
  xml.append(f'    <style name="{name}" parent="{parent[7:] if parent.startswith("@style/") else ""}">')
  for k,v in attrs.items(): xml.append(f'        <item name="{k}">{v.replace("&", "&amp;").replace("<", "&lt;")}</item>')
  xml.append('    </style>')
  default='com.google.android.material.R.attr.checkboxStyle' if 'MaterialCheckBox' in tag else 'androidx.appcompat.R.attr.checkboxStyle' if 'CheckBox' in tag else 'androidx.appcompat.R.attr.radioButtonStyle' if 'RadioButton' in tag else 'com.google.android.material.R.attr.chipStyle' if 'Chip' in tag else 'android.R.attr.textViewStyle'
  kt.append(f'    "{layout}:{node}" to WgTextSource(R.style.{name}, {str("MaterialTextView" in tag).lower()}, {default}),')
 for layout in PROGRESS_LAYOUTS:
  for e in source(layout).iter():
   if not ('ProgressIndicator' in e.tag or e.tag == 'ProgressBar'): continue
   node=e.get(A+'id','').split('/')[-1]
   name=f'OriginalWgProgress_{layout}_{node}'
   xml.append(f'    <style name="{name}" parent="">')
   for k,v in e.attrib.items():
    if k.split('}')[-1] in {'indicatorSize','indicatorInset','trackThickness','trackCornerRadius','indicatorColor','trackColor','indicatorTrackGapSize','trackStopIndicatorSize','indeterminate','max','progress','secondaryProgress'}:
     key=('android:' if k.startswith(A) else '')+k.split('}')[-1]
     xml.append(f'        <item name="{key}">{v}</item>')
   xml.append('    </style>')
 xml.append('</resources>'); kt.append(')')
 return {'app/src/main/res/values/original_wg_text_styles.xml':'\n'.join(xml)+'\n','app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgTextCatalog.kt':'\n'.join(kt)+'\n'}

def check_text_catalog(contents):
 return [f'{p}: pinned typography mismatch' for p,text in generated().items() if contents.get(p)!=text]

CODE_REQUIREMENTS = {
 'app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgTextStyles.kt': ['node?.textAppearanceStyle', 'fontArray.getResourceId', 'node?.letterSpacingEm', 'node?.lineSpacingExtraDp', 'Typeface.create', 'PlatformTextStyle(includeFontPadding', 'node?.maxLinesOverride', 'value(17).getInt', 'getColorStateList'],
 'app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgControls.kt': ['resolveWgText(', 'style = look.style', 'wgTextStateSet(node, pressed)', 'interactive && node.hasClickListener', 'OriginalHorizontalProgress(', 'OriginalLinearProgress(', 'node.trackThicknessDp, node.trackCornerRadiusDp, node.indicatorTrackGapDp, node.trackStopIndicatorSizeDp'],
 'app/src/main/java/com/celzero/bravedns/ui/compose/controls/OriginalProgress.kt': ['LinearProgressIndicatorSpec(context, null)', 'spec.indicatorTrackGapSize = overrides.gapDp', 'spec.trackStopIndicatorSize = overrides.stopSizeDp', 'spec.useRelativeTrackCornerRadius = false', 'DeterminateDrawable.createLinearDrawable', 'IndeterminateDrawable.createLinearDrawable', 'CircularProgressIndicatorSpec(context, null)', 'android.R.attr.progressBarStyleHorizontal', 'android.R.id.secondaryProgress', 'ProgressBarRangeInfo.Indeterminate', 'handler.removeCallbacksAndMessages(drawable)'],
 'app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgRows.kt': ['WgProgress(b.wgHopListProgress, Modifier.fillMaxWidth().padding(horizontal = 10.dp))', 'WgProgress(b.ssProgress, Modifier.fillMaxWidth().padding(top = 6.dp))'],
 'app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgStatsContent.kt': ['WgProgress(b.fssProgressBar, Modifier.fillMaxWidth().padding(5.dp))'],
 'app/src/main/java/com/celzero/bravedns/ui/compose/purchase/HistoryScreen.kt': ['"activity_server_order_history:fetch_progress"', 'indeterminate = true'],
 'app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnComponents.kt': ['"dialog_server_loading:server_loading_timeout_bar"', 'OriginalServerLoadingSpinner()'],
 'app/src/main/java/com/celzero/bravedns/ui/compose/rpnconfig/RpnSsidContent.kt': ['"dialog_rpn_ssid:dialog_title"', '"dialog_rpn_ssid:description_text_view"'],
 'app/src/main/java/com/celzero/bravedns/ui/compose/rpnconfig/RpnBypassContent.kt': ['"activity_rpn_bypass_apps:rpn_bypass_heading"', 'WgText(b.rpnBypassCount'],
 'app/src/main/java/com/celzero/bravedns/ui/compose/rpnconfig/RpnConfigContent.kt': ['"activity_rpn_config_detail:anonymous_45"', '"activity_rpn_config_detail:anonymous_82"'],
}

def check_code(contents):
 errors=[]
 for path,needles in CODE_REQUIREMENTS.items():
  text=contents.get(path,'')
  for needle in needles:
   if needle not in text:errors.append(f'{path}: missing {needle}')
 controls=contents.get('app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgControls.kt','')
 if 'MaterialTheme.typography' in controls.split('fun WgText(',1)[-1].split('fun wgAnnotatedText',1)[0]:errors.append('WgText regressed to generic Compose typography')
 state_paths=['wireguard/WgScreenState.kt','wireguard/WgProxySelectionContent.kt','rpnconfig/RpnConfigState.kt','rpnconfig/RpnBypassContent.kt']
 keys={f'{layout}:{node}' for layout,node,*_ in entries()}
 for rel in state_paths:
  path='app/src/main/java/com/celzero/bravedns/ui/compose/'+rel
  for key in re.findall(r'textSource = "([\w]+:[\w]+)"', contents.get(path,'')):
   if key not in keys:errors.append(f'{path}: unknown text source {key}')
 return errors

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--write',action='store_true'); ap.add_argument('--negative-controls',action='store_true'); args=ap.parse_args()
 expected=generated()
 if args.write:
  for p,t in expected.items(): (ROOT/p).write_text(t)
 actual={p:(ROOT/p).read_text() if (ROOT/p).exists() else '' for p in expected}
 actual.update({p:(ROOT/p).read_text() for p in CODE_REQUIREMENTS})
 for rel in ['wireguard/WgScreenState.kt','wireguard/WgProxySelectionContent.kt','rpnconfig/RpnConfigState.kt','rpnconfig/RpnBypassContent.kt']:
  p='app/src/main/java/com/celzero/bravedns/ui/compose/'+rel; actual[p]=(ROOT/p).read_text()
 errors=check_text_catalog(actual)+check_code(actual)
 controls=[]
 if args.negative_controls:
  p='app/src/main/res/values/original_wg_text_styles.xml'
  for old,new,label in [('sans-serif-medium','sans-serif','font family'),('<item name="android:letterSpacing">0.01</item>','<item name="android:letterSpacing">0</item>','tracking'),('<item name="android:lineSpacingExtra">3dp</item>','<item name="android:lineSpacingExtra">0dp</item>','line spacing'),('android:textAppearance','android:hint','appearance'),('android:singleLine','android:enabled','line limit')]:
   mutation=dict(actual); mutation[p]=mutation[p].replace(old,new,1); controls.append({'mutation':label,'caught':bool(check_text_catalog(mutation))})
  for path,needle,label in [(next(p for p in CODE_REQUIREMENTS if p.endswith('OriginalProgress.kt')), 'spec.indicatorTrackGapSize = overrides.gapDp', 'progress inherited gap'), (next(p for p in CODE_REQUIREMENTS if p.endswith('OriginalProgress.kt')), 'spec.trackStopIndicatorSize = overrides.stopSizeDp', 'progress inherited stop'), (next(p for p in CODE_REQUIREMENTS if p.endswith('WgRows.kt')), 'WgProgress(b.wgHopListProgress, Modifier.fillMaxWidth().padding(horizontal = 10.dp))', 'hop progress callsite'), (next(p for p in CODE_REQUIREMENTS if p.endswith('WgControls.kt')), 'style = look.style', 'text renderer consumes source')]:
   mutation=dict(actual); mutation[path]=mutation[path].replace(needle,'REMOVED_SOURCE_CONTRACT',1); controls.append({'mutation':label,'caught':bool(check_code(mutation))})
  if not all(x['caught'] for x in controls):errors.append('negative control escaped')
 report={'baseline':BASE,'text_sources':len(entries()),'progress_sources':6,'known_source_absence':{'activity_wireguard_main':'Pinned XML contains no ProgressBar or ProgressIndicator node.'},'errors':errors,'negative_controls':controls,'android_tests':'AUTHORED_UNRUN','verification':'Source guards only; no compile, render, screenshot, TalkBack or runtime claim.'}
 print(json.dumps(report,indent=2))
 if errors:raise SystemExit(1)
if __name__=='__main__':main()
