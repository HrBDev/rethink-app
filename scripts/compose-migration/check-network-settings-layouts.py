#!/usr/bin/env python3
"""Static resource/control coverage against pinned layouts; never loads Android or user data."""
from pathlib import Path
import subprocess, xml.etree.ElementTree as E, re, hashlib, json, sys, argparse
ROOT=Path(__file__).resolve().parents[2]
BASELINE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
LAYOUTS={
 'activity_misc_settings':'MiscSettingsState',
 'activity_tunnel_settings':'TunnelSettingsState',
 'fragment_proxy_configure':'ProxySettingsState',
 'dialog_set_proxy':'ProxyEditorState',
 'dialog_lockdown_check':'LockdownDialogState',
 'item_lockdown_check':'LockdownItemState',
}
def attrs(element): return {k.split('}')[-1]:v for k,v in element.attrib.items()}
def reference(value):
 if not value.startswith('@') or '/' not in value:return None
 typ,name=value.lstrip('@+').split('/',1)
 return 'R.'+typ+'.'+name.replace('.','_')
def main():
 parser=argparse.ArgumentParser(description=__doc__);parser.add_argument("--allow-retained-layouts",action="store_true");args=parser.parse_args()
 results=[];errors=[]
 baseline_paths=subprocess.check_output(["git","ls-tree","-r","--name-only",BASELINE],cwd=ROOT,text=True).splitlines()
 for layout,state in LAYOUTS.items():
  candidates=[path for path in baseline_paths if path.startswith('app/src/') and path.endswith('/res/layout/'+layout+'.xml')]
  if len(candidates)!=1:errors.append(layout+' ambiguous or absent');continue
  path=candidates[0]
  baseline=subprocess.check_output(['git','show',BASELINE+':'+path],cwd=ROOT)
  retained=(ROOT/path).exists()
  if retained and not args.allow_retained_layouts:errors.append('Final layout-removal gate: '+path+' is still present')
  unchanged=not retained or (ROOT/path).read_bytes()==baseline
  source=(ROOT/'app/src/main/java/com/celzero/bravedns/ui/compose/networksettings'/f'{state}.kt').read_text()
  missing=[];tags=[];labels=[];styles=[]
  for node in E.fromstring(baseline).iter():
   a=attrs(node)
   if 'id' in a:
    tag='network.'+a['id'].split('/')[-1];tags.append(tag)
    if source.count('key = "'+tag+'"')!=1:missing.append(tag)
   for key in ['text','hint','src','srcCompat','textSize','drawableStartCompat']:
    ref=reference(a.get(key,''))
    if ref:
     labels.append(ref)
     if ref not in source:missing.append(key+':'+ref)
   style=a.get('style','')
   if style.startswith('@style/TextAppearance'):
    ref=reference(style);styles.append(ref)
    if ref not in source:missing.append('appearance:'+ref)
   if style=='@style/CustomWidget.Material3.CompoundButton.MaterialSwitch' and 'R.style.CustomCompoundButtonSwitch' not in source:
    missing.append('original switch theme overlay')
  passed=unchanged and not missing
  if not passed:errors.append(layout+': '+', '.join(missing or ['baseline layout changed']))
  results.append({'layout':path,'nativeState':state,'passed':passed,'baselineLayoutSha256':hashlib.sha256(baseline).hexdigest(),'nativeTags':tags,'textHintIconSizeReferences':sorted(set(labels)),'textAppearances':sorted(set(styles)),'missing':missing})
 native=(ROOT/'app/src/main/java/com/celzero/bravedns/ui/compose/networksettings/NetworkNativeUi.kt').read_text()
 for forbidden in ['AndroidView(', 'LayoutInflater', '.inflate(', 'ViewBinding', 'VpnController', 'PersistentState', 'RethinkDnsApplication']:
  if forbidden in native:errors.append('native renderer contains forbidden dependency: '+forbidden)
 report={'mode':'PRE_REMOVAL_AUDIT_NOT_FINAL' if args.allow_retained_layouts else 'FINAL_LAYOUT_REMOVAL','baseline':BASELINE,'passed':not errors,'kind':'static pinned layout IDs, labels, hints, icon/size refs and TextAppearance coverage; not visual/runtime equivalence','layouts':results}
 (ROOT/'docs/compose-migration/network-settings-layout-source-report.json').write_text(json.dumps(report,indent=2)+'\n')
 if errors:print('FAIL\n'+'\n'.join(errors));return 1
 print('PASS',len(results),'pinned layouts;',sum(len(r['nativeTags']) for r in results),'native tagged controls. Not runtime or screenshot validation.')
 return 0
if __name__=='__main__':sys.exit(main())
