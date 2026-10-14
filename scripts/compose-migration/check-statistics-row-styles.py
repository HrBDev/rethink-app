#!/usr/bin/env python3
"""Source-only checks for distinct summary, blocklist and RPN row contracts."""
from pathlib import Path
import argparse, json, subprocess, xml.etree.ElementTree as E
ROOT=Path(__file__).resolve().parents[2]
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
PREVIOUS='f07b51b6fb2528d55b396aed62bbb9b700893c70'
ROW='app/src/main/java/com/celzero/bravedns/ui/compose/statistics/StatisticsRow.kt'
APP='app/src/main/java/com/celzero/bravedns/adapter/BlocklistAppsAdapter.kt'
RPN='app/src/main/java/com/celzero/bravedns/ui/bottomsheet/RpnStatsBottomSheet.kt'
TEST='app/src/test/java/com/celzero/bravedns/ui/compose/statistics/StatisticsContentTest.kt'
def git(ref,path):return subprocess.check_output(['git','show',ref+':'+path],cwd=ROOT,text=True)
def check(overrides=None):
 overrides=overrides or {};read=lambda p:overrides.get(p,(ROOT/p).read_text())
 contracts={
 'list_item_statistics_summary':{'ss_data_usage':{'textSize':'@dimen/large_font_text_view','singleLine':'true','ellipsize':'end','textStyle':'bold'},'ss_name':{'textSize':'@dimen/small_font_subheading_text','layout_marginTop':'2dp','singleLine':'true'},'ss_count':{'textSize':'@dimen/default_font_text_view','alpha':'0.85'}},
 'list_item_blocklist_app_row':{'bl_app_icon':{'layout_width':'32dp'},'bl_app_name':{'textSize':'@dimen/large_font_text_view','ellipsize':'end'},'bl_app_count':{'textSize':'@dimen/extra_large_font_text_view','minWidth':'32dp','fontFamily':'sans-serif-light'}},
 'list_item_blocklist_domain_row':{'bl_domain_row':{'minHeight':'40dp','paddingStart':'52dp','paddingEnd':'46dp'},'bl_domain_name':{'textSize':'@dimen/default_font_text_view','ellipsize':'middle'},'bl_domain_count':{'minWidth':'32dp','layout_marginStart':'8dp'}},
 'list_item_rpn_stat_app':{'rpn_stat_app_icon':{'layout_width':'32dp'},'rpn_stat_app_name':{'textSize':'14sp','maxLines':'1'},'rpn_stat_app_meta':{'textSize':'11sp','alpha':'0.7'},'rpn_stat_app_total':{'textSize':'13sp','fontFamily':'sans-serif-medium'}}}
 for name,nodes in contracts.items():
  xml=E.fromstring(git(BASE,'app/src/main/res/layout/'+name+'.xml'))
  actual={n.get('{http://schemas.android.com/apk/res/android}id','').split('/')[-1]:{k.split('}')[-1]:v for k,v in n.attrib.items()} for n in xml.iter()}
  for node,attrs in nodes.items():
   for key,value in attrs.items():assert actual[node][key]==value,(name,node,key)
 source=read(ROW)
 tokens=['size = 14f, weight = Typeface.BOLD','Modifier.padding(top = 2.dp), style = utilityTextStyle(size = 11f)','Modifier.alpha(.85f), style = utilityTextStyle(size = 12f)',
  'padding(horizontal = 20.dp, vertical = 7.dp)','size = 13f, family = "sans-serif-medium"','Modifier.alpha(.7f)',
  'heightIn(min = 52.dp)','TextAppearance_AppCompat_Subhead','size = 16f, family = "sans-serif-light", weight = Typeface.BOLD','R.drawable.ic_expand_more_24',
  'heightIn(min = 40.dp).padding(start = 52.dp, end = 46.dp, top = 4.dp, bottom = 4.dp)','TextOverflow.MiddleEllipsis','Modifier.padding(start = 8.dp).widthIn(min = 32.dp)']
 for token in tokens:assert token in source,'missing row contract '+token
 assert source[source.index('internal fun statisticsLabel'):]=='internal fun statisticsLabel'+git(PREVIOUS,ROW).split('internal fun statisticsLabel',1)[1]
 app=git(PREVIOUS,APP).replace('import com.celzero.bravedns.ui.compose.statistics.StatisticsRow','import com.celzero.bravedns.ui.compose.statistics.BlocklistStatisticsAppRow\nimport com.celzero.bravedns.ui.compose.statistics.BlocklistStatisticsDomainRow').replace('StatisticsRow(identity?.first ?: app.appOrDnsName ?: stringResource(R.string.network_log_app_name_unnamed, app.uid.toString()),\n                null, "", app.count.toString(), icon = identity?.second ?: Utilities.getDefaultIcon(context),\n                downArrow = true, arrowRotation = if (group.expanded) 180f else 0f, tag = "blocklist_app_${app.uid}") { toggle(group) }','BlocklistStatisticsAppRow(identity?.first ?: app.appOrDnsName ?: stringResource(R.string.network_log_app_name_unnamed, app.uid.toString()),\n                app.count.toString(), icon = identity?.second ?: Utilities.getDefaultIcon(context),\n                expanded = group.expanded, tag = "blocklist_app_${app.uid}") { toggle(group) }').replace('Row(Modifier.fillMaxWidth().padding(start = 54.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {\n                    Text(domain.appOrDnsName.orEmpty(), Modifier.weight(1f)); Text(domain.count.toString())\n                }','BlocklistStatisticsDomainRow(domain.appOrDnsName.orEmpty(), domain.count.toString())')
 assert read(APP)==app,'blocklist changed beyond renderer calls'
 rpn=git(PREVIOUS,RPN).replace('import com.celzero.bravedns.ui.compose.statistics.StatisticsRow','import com.celzero.bravedns.ui.compose.statistics.RpnStatisticsAppRow').replace('StatisticsRow(item.appOrDnsName ?: context.getString(R.string.lbl_unknown), meta, "",','RpnStatisticsAppRow(item.appOrDnsName ?: context.getString(R.string.lbl_unknown), meta,')
 # Preserve the user's 154065c lint fix as two exact presentation-only substitutions.
 # Keep whole-file equality; unrelated controller or renderer changes still fail.
 rpn=rpn.replace('import androidx.compose.runtime.Composable', 'import androidx.compose.ui.res.stringResource\nimport androidx.compose.runtime.Composable').replace('context.getString(R.string.lbl_unknown)', 'stringResource(R.string.lbl_unknown)')
 assert read(RPN)==rpn,'RPN changed beyond renderer call and exact stringResource lint fix'
 for token in ['sourceSummaryRowRetainsTextSizesAndOneLineTruncation','rpnAndBlocklistRowsKeepIndependentOriginalTextContracts','GetTextLayoutResult','TextOverflow.MiddleEllipsis','application = Application::class']:
  assert token in read(TEST),'missing regression test '+token
 return len(tokens)
def main():
 p=argparse.ArgumentParser();p.add_argument('--self-test',action='store_true');p.add_argument('--report',type=Path);a=p.parse_args();count=check();rejected=0
 if a.self_test:
  for path,old,new in [(ROW,'size = 13f, family = "sans-serif-medium"','size = 14f'),(ROW,'TextOverflow.MiddleEllipsis','TextOverflow.Ellipsis'),(ROW,'padding(start = 52.dp, end = 46.dp','padding(start = 54.dp, end = 16.dp'),(ROW,'Modifier.padding(top = 2.dp)','Modifier.padding(top = 0.dp)'),(APP,'toggle(group)','error("removed toggle")'),(RPN,'loadIconForUid(context, item.uid)','loadIconForUid(context, 0)'),(RPN,'stringResource(R.string.lbl_unknown)','context.getString(R.string.lbl_unknown)'),(RPN,'import androidx.compose.ui.res.stringResource\n',''),(TEST,'application = Application::class','application = RethinkDnsApplication::class')]:
   text=(ROOT/path).read_text();assert old in text
   try:check({path:text.replace(old,new)})
   except AssertionError:rejected+=1
   else:raise AssertionError('mutation escaped: '+old)
 result={'passed':True,'source_only':True,'baseline':BASE,'layouts':4,'source_contracts':count,'negative_controls':rejected,'compile':'not_run','runtime':'not_run'}
 if a.report:a.report.write_text(json.dumps(result,indent=2)+'\n')
 print(json.dumps(result))
if __name__=='__main__':main()
