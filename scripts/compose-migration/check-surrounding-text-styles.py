#!/usr/bin/env python3
"""Pinned source checks for S30-S33. No compilation, Android, traffic or rendering."""
from pathlib import Path
import argparse,json,subprocess,xml.etree.ElementTree as ET,hashlib
ROOT=Path(__file__).resolve().parents[2]
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9';PREVIOUS='e17aaecf8185f06f1888d50cd2ad250f085d5374'
P='app/src/main/java/com/celzero/bravedns/ui/compose/'
FILES={'dns':P+'dns/BlockFreeDnsContent.kt','firewall':P+'firewallapps/FirewallFilterContent.kt','blocklist':P+'blocklists/BlocklistFilterScreen.kt','interval':P+'home/LogIntervalContent.kt','stats':P+'statistics/PagedStatisticsScreen.kt'}

def git(rev,path):return subprocess.check_output(['git','show',rev+':'+path],cwd=ROOT,text=True)
def check(overrides=None):
 overrides=overrides or {};s={k:overrides.get(k,(ROOT/p).read_text()) for k,p in FILES.items()}
 expected={
 'dns':['TextAppearance_Material3_TitleSmall_Emphasized','bottom = 8.dp), color = themeColor(R.attr.primaryLightColorText), fontSize = 12.sp','OriginalCardSurface(','containerColor = themeColor(R.attr.chipBgColorPositive)','Text("Selected:", color = themeColor(R.attr.primaryTextColor), fontSize = 12.sp)','Modifier.weight(1f, false), color = themeColor(R.attr.primaryTextColor), fontSize = 14.sp','Modifier.padding(top = 10.dp), color = themeColor(R.attr.primaryLightColorText), fontSize = 12.sp','fontSize = 12.sp, textAlign = TextAlign.Center'],
 'firewall':['padding(10.dp).padding(bottom = 40.dp)','padding(top = 10.dp, bottom = 20.dp).size(60.dp, 3.dp).background(themeColor(R.attr.border))','color=themeColor(R.attr.secondaryTextColor), fontSize=14.sp','padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 2.dp)','padding(horizontal = 12.dp, vertical = 7.dp)'],
 'blocklist':['color = themeColor(R.attr.secondaryTextColor), fontSize = 14.sp'],
 'interval':['.copy(alpha = .6f), fontSize = 11.sp)','.copy(alpha = .7f), fontSize = 11.sp)','Text(name, color = themeColor(R.attr.primaryTextColor), fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)','SelectionContainer(Modifier.weight(1f)) { Text(row.label, color = themeColor(R.attr.primaryLightColorText), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)'],
 'stats':['Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically)', 'Modifier.fillMaxWidth().padding(10.dp), textAlign = TextAlign.Center', 'R.style.TextAppearance_Heading_HomeScreen','uppercase(LocalLocale.current.platformLocale)','family = "sans-serif-smallcaps", lineExtraDp = 5f','SelectionContainer(Modifier.weight(1f)) { Text(title, Modifier.padding(10.dp), color = themeColor(R.attr.primaryLightColorText), style = titleStyle)','utilityDimensionSp(context, R.dimen.default_font_text_view)','utilityDimensionSp(context, R.dimen.heading_font_text_view)']}
 count=0
 for k,tokens in expected.items():
  for token in tokens:assert token in s[k],(k,token);count+=1
 assert s['firewall'].count('color=themeColor(R.attr.secondaryTextColor), fontSize=14.sp')==3
 for k,start,end in [('dns','internal class BlockFreeChipGroup','@Composable internal fun BlockFreeDnsContent'),('firewall','class FirewallFilterState','@Composable fun FirewallFilterContent'),('blocklist','class BlocklistFilterChip','/** Subgroup mutation'),('interval','class IntervalRangeGroup','@Composable\nfun LogIntervalContent')]:
  old=git(PREVIOUS,FILES[k]);assert old[old.index(start):old.index(end)]==s[k][s[k].index(start):s[k].index(end)],k+' state behavior changed'
 old=git(PREVIOUS,FILES['interval']);start='private fun CachedActivityIcon';assert old[old.index(start):]==s['interval'][s['interval'].index(start):],'cached icon behavior changed'
 for k,token in [('dns','list.onItemSelected(item)'),('firewall','b.selectCategory(category,category !in b.selectedCategories)'),('blocklist','chip.change(chip.checked)'),('interval','onGroupClicked(row)'),('stats','presenter?.let { rows -> items(rows.itemCount) { rows.RowAt(it) } }')]:assert token in s[k],k+' callback changed'
 # Independently pin the subtle URL nesting: 8dp container +2dp child, NOT an unwanted10dp gap.
 raw=git(BASE,'app/src/full/res/layout/list_item_block_free_dns.xml');xml=ET.fromstring(raw);a='{http://schemas.android.com/apk/res/android}'
 nodes={n.get(a+'id','').split('/')[-1]:n for n in xml.iter()}
 assert nodes['bfd_item_text_container'].get(a+'layout_marginTop')=='8dp'
 assert nodes['bfd_item_url'].get(a+'layout_marginTop')=='2dp'
 for name in ['activity_detailed_statistics','activity_domain_connections']:
  x=ET.fromstring(git(BASE,'app/src/main/res/layout/'+name+'.xml'))
  assert any(n.get('style')=='@style/TextAppearance.Heading.HomeScreen' for n in x.iter())
  assert any(n.get(a+'lineSpacingExtra')=='5dp' and n.get(a+'fontFamily')=='sans-serif-smallcaps' for n in x.iter())
 return count

def main():
 p=argparse.ArgumentParser();p.add_argument('--self-test',action='store_true');p.add_argument('--report',type=Path);a=p.parse_args();count=check();negative=0
 if a.self_test:
  for k,old,new in [('dns','TextAppearance_Material3_TitleSmall_Emphasized','TextAppearance_Material3_TitleSmall'),('dns','Modifier.padding(top = 10.dp)','Modifier.padding(top = 2.dp)'),('firewall','fontSize=14.sp','fontSize=16.sp'),('blocklist','fontSize = 14.sp','fontSize = 16.sp'),('interval','maxLines = 1, overflow = TextOverflow.Ellipsis','maxLines = 2'),('interval','onlyRetrieveFromCache(true)','onlyRetrieveFromCache(false)'),('stats','lineExtraDp = 5f','lineExtraDp = 0f'),('stats','rows.RowAt(it)','rows.RowAt(0)'),('firewall','onParent(id)','onParent(0)')]:
   text=(ROOT/FILES[k]).read_text();assert old in text
   try:check({k:text.replace(old,new)})
   except AssertionError:negative+=1
   else:raise AssertionError('mutation survived '+old)
 report={'passed':True,'source_only':True,'source_contracts':count,'negative_controls':negative,'baseline':BASE,'compile':'not_run','runtime':'not_run','url_spacing_note':'8dp source container margin plus 2dp URL margin =10dp; retained intentionally','sha256':{p:hashlib.sha256((ROOT/p).read_bytes()).hexdigest() for p in FILES.values()}}
 if a.report:a.report.write_text(json.dumps(report,indent=2)+'\n')
 print(json.dumps(report))
if __name__=='__main__':main()
