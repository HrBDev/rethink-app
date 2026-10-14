#!/usr/bin/env python3
"""Bounded source integration check. Never compiles, launches or downloads anything."""
from pathlib import Path
import hashlib, json, re, subprocess, xml.etree.ElementTree as ET
ROOT = Path(__file__).resolve().parents[2]
D = ROOT / 'docs/xml-layout-removal'
P = ROOT / 'app/src/main/java/com/celzero/bravedns/ui/compose'
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
paths = {k: P / p for k, p in {'helper':'controls/OriginalCard.kt','settings':'settings/SettingsComponents.kt','wg':'wireguard/WgControls.kt','rows':'wireguard/WgRows.kt','detail':'wireguard/WgDetailContent.kt','stats':'wireguard/WgStatsContent.kt','about':'about/AboutContent.kt'}.items()}
sources = {k: p.read_text() for k, p in paths.items()}
def compact(s): return re.sub(r'\s+', '', s)
def block(s, prefix):
    pos = s.index('{', s.index(prefix)); end = pos + 1; depth = 1
    while depth:
        depth += (s[end] == '{') - (s[end] == '}'); end += 1
    return s[pos:end]
def checks(s):
    out = {}
    def has(key, name, *parts): out[name] = all(compact(p) in compact(s[key]) for p in parts)
    has('helper','instance/style/default precedence', 'explicit?.hasValue(index) == true', 'cornerRadius?.toPx() ?: values(1).getDimension(1, 0f)', 'containerColor ?: appearance.fill')
    has('helper','compat/corner/max-elevation clearance', 'max(elevationPx, values(3).getDimension(3, elevationPx))', '(1.0 - cos(Math.PI / 4.0)) * radiusPx', 'ceil(maxElevationPx + corner)', 'ceil(1.5 * maxElevationPx + corner)')
    has('helper','physical source padding mapped for RTL', 'start = if (rtl) appearance.right else appearance.left', 'end = if (rtl) appearance.left else appearance.right')
    has('helper','zero border suppresses inherited stroke without a hairline', '(border ?: appearance.stroke?', ')?.takeIf { it.width > 0.dp }')
    has('helper','source shadow without tonal overlay', 'tonalElevation = 0.dp', 'shadowElevation = appearance.elevation')
    has('settings','SettingsCard keeps parameters and delegates source style', 'OriginalCardSurface(', 'styleAttribute = styleAttribute', 'cornerRadius = cornerRadius', 'horizontal = horizontalContentPadding, vertical = contentPadding')
    card = block(s['wg'], 'fun WgCard(')
    out['WgCard removes universal child insets and gaps'] = 'Column(Modifier.padding(contentPadding), content = content)' in card and 'spacedBy' not in card and '16.dp' not in card
    has('wg','WgCard keeps explicit radius/elevation/fill/zero-border and callback', 'cornerRadius = node?.radiusDp?.dp', 'elevation = node?.elevationDp?.dp', 'containerColor = node?.backgroundColor?.let(::Color)', 'border = node?.let { BorderStroke(width, Color(it.strokeColor)) }', 'node.performClick()')
    has('rows','general/one original ten-dp containers', 'WgCard(b.interfaceDetailCard, contentPadding = PaddingValues(10.dp))')
    out['general/one each uses ten-dp container'] = s['rows'].count('WgCard(b.interfaceDetailCard, contentPadding = PaddingValues(10.dp))') == 2
    has('rows','include original margins/minimum/content padding', 'Modifier.padding(horizontal = 6.dp)', 'PaddingValues(start = 10.dp, top = 10.dp, end = 6.dp, bottom = 10.dp)', 'Row(Modifier.heightIn(min = 48.dp)')
    has('rows','hop/peer source container insets', 'PaddingValues(start = 5.dp, top = 7.dp, bottom = 7.dp)', 'PaddingValues(start = 15.dp, top = 15.dp, end = 15.dp, bottom = 20.dp)')
    out['general/one uptime and traffic restored as peers'] = s['rows'].count('if (b.interfaceActiveLayout.isVisible) Row(Modifier.fillMaxWidth()') == 2
    has('detail','detail four source container paddings', 'b.interfaceDetailCard, contentPadding = PaddingValues(start = 15.dp, top = 10.dp, end = 15.dp, bottom = 20.dp)', 'b.actionsCard, contentPadding = PaddingValues(20.dp)', 'b.otherSettingsCard, contentPadding = PaddingValues(bottom = 20.dp)', 'b.mobileSsidSettingsCard, contentPadding = PaddingValues(bottom = 20.dp)')
    has('stats','stats combines original ten-dp margin and ten-dp child padding', 'contentPadding = PaddingValues(20.dp)')
    has('about','About retains zero child padding within original outer margins', 'WgCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), contentPadding = PaddingValues(0.dp))')
    calls=[]
    for key in ('rows','detail','stats','about'):
        for match in re.finditer(r'\bWgCard\(', s[key]):
            start=match.end(); end=start; depth=1
            while depth:
                depth+=(s[key][end]=='(')-(s[key][end]==')'); end+=1
            calls.append(s[key][start:end])
    out['all eleven WgCard calls require explicit child padding'] = len(calls)==11 and all('contentPadding =' in c for c in calls)
    return out
results=checks(sources)
assert all(results.values()), results
mutations=[('helper','it.width > 0.dp','it.width >= 0.dp'),('helper','ceil(1.5 * maxElevationPx + corner)','0f'),('helper','start = if (rtl) appearance.right else appearance.left','start = appearance.left'),('settings','OriginalCardSurface(','Card('),('wg','Column(Modifier.padding(contentPadding)','Column(Modifier.padding(16.dp)'),('rows','min = 48.dp','min = 68.dp'),('detail','PaddingValues(20.dp)','PaddingValues(16.dp)'),('about','contentPadding = PaddingValues(0.dp)','contentPadding = PaddingValues(16.dp)')]
for key, old, new in mutations:
    changed=dict(sources);changed[key]=changed[key].replace(old,new,1)
    assert changed[key] != sources[key] and not all(checks(changed).values()), (key,old)
# Independent baseline evidence is obtained from immutable Git, not current layouts.
xml_names=['activity_wg_detail','list_item_wg_general_interface','list_item_wg_one_interface','list_item_wg_include_apps','list_item_wg_hop','list_item_wg_peers','fragment_wg_nw_stats','fragment_about']
base_paths=subprocess.check_output(['git','ls-tree','-r','--name-only',BASE],cwd=ROOT,text=True).splitlines()
pins={};contracts={};A='{http://schemas.android.com/apk/res/android}'
for name in xml_names:
    path=next(p for p in base_paths if p.endswith('/'+name+'.xml'))
    raw=subprocess.check_output(['git','show',BASE+':'+path],cwd=ROOT,text=True)
    pins[path]=hashlib.sha256(raw.encode()).hexdigest()
    contracts[name]=[{'card':n.attrib,'direct_child':list(n)[0].attrib} for n in ET.fromstring(raw).iter() if n.tag.endswith('MaterialCardView')]
assert contracts['list_item_wg_general_interface'][0]['direct_child'][A+'padding']=='10dp'
assert contracts['list_item_wg_one_interface'][0]['direct_child'][A+'padding']=='10dp'
assert contracts['list_item_wg_include_apps'][0]['direct_child'][A+'minHeight']=='68dp'
assert contracts['fragment_wg_nw_stats'][0]['direct_child'][A+'layout_margin']=='10dp'
assert contracts['fragment_wg_nw_stats'][0]['direct_child'][A+'padding']=='10dp'
assert not any('padding' in k for k in contracts['fragment_about'][0]['direct_child'])
result={'result':'PASS_SOURCE_ONLY','checks':results,'negative_mutations_rejected':len(mutations),'baseline':BASE,'baseline_layout_sha256':pins,'baseline_card_container_contracts':contracts,'production_file_sha256':{str(paths[k].relative_to(ROOT)):hashlib.sha256(v.encode()).hexdigest() for k,v in sources.items()},'runtime':'NOT_RUN_TOOLCHAIN_UNAVAILABLE','remaining_gates':['S06/S07 child-by-child typography, spacing, grouping and original ripple/selector contracts remain open.','OriginalCardSurface models rounded cards and default state colors only; it is not a general shapeAppearance/state-list/checkable-card equivalence claim.','Compound controls are independently owned; this guard does not certify them.']}
(D/'shared-card-source-style-results.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps({'result':result['result'],'checks':len(results),'negative_mutations_rejected':len(mutations),'runtime':result['runtime']},indent=2))
