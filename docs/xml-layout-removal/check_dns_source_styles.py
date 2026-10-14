#!/usr/bin/env python3
"""Bounded source guard only; never invokes Android or downloads dependencies."""
from pathlib import Path
import json,re,subprocess,hashlib
ROOT=Path(__file__).resolve().parents[2]
P=ROOT/'app/src/main/java/com/celzero/bravedns/ui/compose'
paths={'modes':P/'about/DnsRecordTypesContent.kt','screen':P/'dns/DnsEndpointScreen.kt','presenter':P/'dns/DnsEndpointPresenter.kt','header':P/'dnssettings/DnsFragmentHost.kt'}
sources={k:p.read_text() for k,p in paths.items()}
def check(s):
 m=s['modes']; f=s['screen']; h=s['header']; p=s['presenter']
 return all([
  'OriginalButton(' in m,
  '"bottom_sheet_dns_record_types:" + if (automatic) "drbs_auto_mode_btn" else "drbs_manual_mode_btn"' in m,
  'selected = selected, segmentIndex = if (automatic) 0 else 1, segmentCount = 2' in m,
  'contentColor = node.textColor?.let(::Color), backgroundTint = node.backgroundTintList' in m,
  'onClick = { b.drbsModeToggleGroup.check(id) }' in m,
  'Surface(Modifier.weight(1f).selectable' not in m,
  'DnsEndpointFlag(it, item.flagStyle)' in f,
  'sizeIn(minWidth = 32.dp, minHeight = 32.dp), contentAlignment = Alignment.Center' in f,
  'utilityTextStyle(appearance = androidx.appcompat.R.style.TextAppearance_AppCompat_Headline)' in f,
  'else utilityTextStyle(size = 25f)' in f,
  'flagStyle = if (directIp) DnsEndpointFlagStyle.Proxy else DnsEndpointFlagStyle.Standard' in p,
  'utilityTextStyle(appearance = com.google.android.material.R.style.TextAppearance_Material3_TitleMedium)' in h,
  'utilityDimensionSp(LocalContext.current, R.dimen.small_font_subheading_text)' in h,
 ])
assert check(sources)
mutations=[('modes','OriginalButton(','Surface('),('modes','segmentCount = 2','segmentCount = 1'),('modes','backgroundTint = node.backgroundTintList','backgroundTint = null'),('screen','minWidth = 32.dp','minWidth = 0.dp'),('screen','TextAppearance_AppCompat_Headline','TextAppearance_AppCompat_Body1'),('presenter','if (directIp) DnsEndpointFlagStyle.Proxy','if (false) DnsEndpointFlagStyle.Proxy'),('header','R.dimen.small_font_subheading_text','R.dimen.large_font_text_view')]
for key,old,new in mutations:
 altered=dict(sources); altered[key]=altered[key].replace(old,new,1)
 assert altered[key]!=sources[key] and not check(altered),(key,old)
# The callback/restore state machine is unchanged from the frozen source snapshot.
base=subprocess.check_output(['git','-C',str(ROOT),'show','803cd4c884243ab9ef9c92cb0c4959d63bc27b00:'+str(paths['modes'].relative_to(ROOT))],text=True)
def state_classes(s):return s[s.index('class DiagnosticToggleGroup'):s.index('@Composable\nfun DnsRecordTypesContent')]
assert state_classes(base)==state_classes(sources['modes'])
result={'status':'passed_source_only','checks':'exact catalog integration, callback state unchanged, source flag variants and header typography','negative_mutations_rejected':len(mutations),'runtime':'not_run_toolchain_unavailable','production_file_sha256':{str(paths[k].relative_to(ROOT)):hashlib.sha256(v.encode()).hexdigest() for k,v in sources.items()}}
(ROOT/'docs/xml-layout-removal/dns-source-style-results.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result,indent=2))
