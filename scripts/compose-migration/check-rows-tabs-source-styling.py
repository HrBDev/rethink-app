#!/usr/bin/env python3
"""S21/S24/S27 source-contract regression guard. No Gradle, app, network or runtime execution."""
from pathlib import Path
import hashlib, json, re, subprocess, xml.etree.ElementTree as ET
ROOT = Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
BEFORE = 'e17aaecf8185f06f1888d50cd2ad250f085d5374'
P = 'app/src/main/java/com/celzero/bravedns/'
FILES = {
    'bubble': P+'ui/compose/utilities/BubbleScreen.kt',
    'rows': P+'ui/compose/appdetails/AppConnectionRows.kt',
    'tabs': P+'ui/compose/controls/OriginalTabs.kt',
    'rules': P+'ui/activity/CustomRulesActivity.kt',
    'logs': P+'ui/activity/NetworkLogsActivity.kt',
}
A = '{http://schemas.android.com/apk/res/android}'
APP = '{http://schemas.android.com/apk/res-auto}'
def original(path, commit=BASE): return subprocess.check_output(['git', 'show', commit+':'+path], cwd=ROOT, text=True)
def compact(text): return re.sub(r'\s+', '', text)
def block(source, prefix):
    start = source.index('{', source.index(prefix)); end = start+1; depth=1
    while depth:
        depth += (source[end]=='{') - (source[end]=='}'); end += 1
    return source[start:end]
def checks(s):
    results={}
    def has(key, name, *parts): results[name] = all(compact(p) in compact(s[key]) for p in parts)
    has('bubble', 'CardView family, compat clearance, distinct margins', 'OriginalCardSurface(', 'androidx.cardview.R.attr.cardViewStyle', 'androidx.cardview.R.style.CardView', 'useCompatPadding = true', 'horizontal = if (blocked) 6.dp else 4.dp', 'vertical = if (blocked) 6.dp else 3.dp')
    has('bubble', 'source icon frame and image', 'Surface(Modifier.size(44.dp), shape = CircleShape', 'UtilityImage(icon, Modifier.size(28.dp))', 'tonalElevation = 0.dp, shadowElevation = 0.dp')
    has('bubble', 'source top anchors and inner padding', 'Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp))', 'Row(verticalAlignment = Alignment.Top)', 'Column(Modifier.weight(1f).padding(start = 12.dp))')
    has('bubble', 'selected app-name marquee and one-line constraints', 'basicMarquee(iterations = Int.MAX_VALUE)', 'maxLines = 1, softWrap = false', 'TextAppearance_AppCompat_Subhead, weight = android.graphics.Typeface.BOLD')
    has('bubble', 'source caption groups and overflow', 'Row(Modifier.fillMaxWidth().padding(top = 4.dp)', 'Modifier.weight(1f).padding(start = 10.dp)', 'Text(pkg, Modifier.fillMaxWidth().padding(top = 2.dp)', 'Text(line, Modifier.fillMaxWidth().padding(top = 2.dp), maxLines = 1', 'overflow = TextOverflow.Ellipsis')
    has('bubble', 'existing action identity and large-font reflow preserved', 'LocalDensity.current.fontScale > 1.3f', '"bubble.remove.${app.uid}"', '"bubble.allow.${app.uid}"', '{ action(app) }', 'item_blocked_app:blocked_app_allow_btn', 'item_allowed_app:allowed_remove_btn', 'grouped = true', 'contentDescription = "$action $name"')
    for name in ('BubbleAllowedRow(', 'BubbleBlockedRow('):
        results[name+' age/content/callback body unchanged'] = block(s['bubble'], 'fun '+name) == block(original(FILES['bubble'], BEFORE), 'fun '+name)
    has('rows', 'country keeps independent outer and row geometry', 'fun AppCountriesRowContent', 'heightIn(min = 50.dp).padding(start = 4.dp, end = 16.dp)', 'heightIn(min = 44.dp)', 'padding(start = 2.dp, end = 8.dp)', 'Column(Modifier.weight(1f).padding(start = 8.dp))', 'Modifier.padding(start = 10.dp), country = true')
    has('rows', 'country bold one-line end ellipsis', 'R.dimen.large_font_text_view, weight = Typeface.BOLD, lines = 1, overflow = TextOverflow.Ellipsis')
    has('rows', 'detail title and subtitle source attributes', 'R.dimen.large_font_text_view, appearance = android.R.style.TextAppearance_Material_Body1', 'overflow = TextOverflow.StartEllipsis', 'R.dimen.default_font_text_view, lines = 3, overflow = TextOverflow.Ellipsis', 'padding(start = 5.dp).padding(2.dp)')
    has('rows', 'count source family, weight, clipping and width', 'widthIn(min = 40.dp, max = 60.dp)', '.alpha(if (country) .85f else 1f)', 'R.dimen.extra_large_font_text_view', 'android.R.style.TextAppearance_Material_Subhead', 'family = "sans-serif-light", weight = Typeface.BOLD, lines = 1')
    has('rows', 'source icon and flag state-specific alpha/frame', 'if (icon != null) Row(Modifier.alpha(.9f)', 'icon.visibility != View.GONE && icon.drawable != null', 'AppConnectionFlag(flag, Modifier.padding(5.dp), 1f)', 'AppConnectionFlag(flag, Modifier.padding(5.dp), .75f)', 'modifier.size(32.dp)', 'size = node.textSizeSp ?: 26f')
    has('rows', 'arrow retains invisible geometry and resource tint', 'arrow.visibility != View.GONE', 'Modifier.padding(5.dp).size(26.dp).padding(5.dp).alpha(.3f)', 'Modifier.connectionState(arrow)', 'tint = Color.Unspecified')
    has('rows', 'state typography and annotated spans retained', 'wgAnnotatedText(node.text)', 'node.textSizeSp ?: utilityDimensionSp', 'node.textStyle.takeIf', 'minOf(lines, node.maxLines)', 'if (node.ellipsize) TextOverflow.Ellipsis', 'node.textColor?.let(::Color)', 'node.visibility == View.INVISIBLE', 'Modifier.clearAndSetSemantics {}')
    has('rows', 'source progress variants and explicit controller colors', 'PaddingValues(top = 5.dp, bottom = 2.dp)', 'PaddingValues(start = 7.dp, top = 5.dp, end = 5.dp, bottom = 5.dp)', 'if (country) 3.dp to 2.dp', 'MaterialR.attr.linearProgressIndicatorStyle', 'node.indicatorColor?.let(::Color)', 'if (country) Modifier else Modifier.padding(3.dp)', 'if (rtl) size.width - width else 0f')
    has('rows', 'row callbacks and separator preserved', 'container.performClick()', 'container.isEnabled && container.isClickable', 'thickness = .75.dp', 'Modifier.padding(vertical = 3.dp)')
    has('tabs', 'original style hierarchy and overlay resolved', 'base.obtainOrderedStyledAttributes(R.style.TabbedLayoutDefault', 'context.obtainOrderedStyledAttributes(R.style.TabbedLayoutDefault', 'MaterialR.attr.materialThemeOverlay', 'ContextThemeWrapper(base, overlay)', 'CompositionLocalProvider(LocalContext provides look.context)', 'utilityTextStyle(look.textAppearance)', 'utilityTextStyle(look.selectedTextAppearance)', 'androidx.appcompat.R.attr.textAllCaps')
    has('tabs', 'scrollable centered source geometry', 'check(look.mode == 0)', 'horizontalScroll(scroll)', 'height(48.dp)', 'look.contentStart - look.paddingStart', 'originalTabWidths(naturalWidths, viewport', 'largest * natural.size <= viewport - 2 * gutter', 'natural.map { largest }', 'look.paddingStart.toDp()', 'look.paddingEnd.toDp()')
    has('tabs', 'source selected/unselected/ripple/background colors', 'MaterialR.attr.tabSelectedTextColor', 'MaterialR.attr.tabTextColor', 'MaterialR.attr.tabRippleColor', 'MaterialR.attr.tabIconTint', 'drawTabDrawable(look.background)', 'drawTabDrawable(look.tabBackground, states)', 'ripple(bounded = !look.unboundedRipple', 'look.selectedTextColor')
    has('tabs', 'source full-width indicator drawable, height, gravity and elastic movement', 'if (look.indicatorFullWidth) widths[index]', 'indicator.intrinsicHeight', 'look.indicatorGravity', 'originalTabIndicatorEdges(', 'cos(amount * Math.PI / 2.0)', 'sin(amount * Math.PI / 2.0)', 'indicator.draw(it.nativeCanvas)')
    has('tabs', 'original overflow sizing and RTL scroll selection', 'MaterialR.dimen.design_tab_text_size_2line', 'MaterialR.dimen.design_tab_scrollable_min_width', 'maxLines = 2', 'TextOverflow.Ellipsis', 'look.multiLineSize.sp', 'if (rtl) total - starts[index] - widths[index]', 'scroll.scrollTo(', 'Role.Tab', 'onClick = { onSelect(index) }')
    for key in ('rules', 'logs'):
        has(key, key+' consumes source tab style and pager offset', 'OriginalTabStrip(pager.currentPage', 'pager.currentPageOffsetFraction', 'scope.launch { pager.animateScrollToPage(page) }')
        results[key+' has no raw TabRow/Tab fallback'] = not re.search(r'\b(?:PrimaryTabRow|TabRow|Tab)\(', s[key])
        # Verify the entire host is unchanged except the explicitly scoped tab renderer/import.
        before = original(FILES[key], BEFORE)
        old_start = before.index('                        TabRow(' if key=='rules' else '                    PrimaryTabRow(')
        end_marker = '                        HorizontalPager(' if key=='rules' else '                    IconButton('
        old_end = before.index(end_marker, old_start)
        new_start = s[key].index('                        OriginalTabStrip(' if key=='rules' else '                    OriginalTabStrip(')
        new_end = s[key].index(end_marker, new_start)
        current = s[key].replace('import com.celzero.bravedns.ui.compose.controls.OriginalTabStrip\n', '')
        new_start = current.index('                        OriginalTabStrip(' if key=='rules' else '                    OriginalTabStrip(')
        new_end = current.index(end_marker, new_start)
        results[key+' whole-host outside tab strip byte-identical'] = before[:old_start] + before[old_end:] == current[:new_start] + current[new_end:]
    for key in ('bubble','rows','tabs'):
        code=re.sub(r'/\*.*?\*/|//[^\n]*','',s[key],flags=re.S)
        results[key+' native rendering only'] = not any(x in code for x in ('AndroidView(', 'LayoutInflater', 'R.layout.', 'setContentView(', 'TabLayout('))
    return results

source={k:(ROOT/p).read_text() for k,p in FILES.items()}
results=checks(source)
assert all(results.values()), {k:v for k,v in results.items() if not v}
mutations=[
 ('bubble','useCompatPadding = true','useCompatPadding = false'),
 ('bubble','Modifier.size(28.dp)','Modifier.size(32.dp)'),
 ('bubble','horizontal = if (blocked) 6.dp else 4.dp','horizontal = 4.dp'),
 ('bubble','basicMarquee(iterations = Int.MAX_VALUE)','basicMarquee(iterations = 1)'),
 ('bubble','padding(top = 4.dp)','padding(top = 12.dp)'),
 ('rows','family = "sans-serif-light"','family = "sans-serif"'),
 ('rows','max = 60.dp','max = 80.dp'),
 ('rows','lines = 3, overflow','lines = 8, overflow'),
 ('rows','heightIn(min = 44.dp)','heightIn(min = 50.dp)'),
 ('rows','Modifier.padding(3.dp)','Modifier.padding(0.dp)'),
 ('tabs','R.style.TabbedLayoutDefault','R.style.TabTextDefault'),
 ('tabs','largest * natural.size <= viewport - 2 * gutter','largest * natural.size <= viewport'),
 ('tabs','height(48.dp)','height(64.dp)'),
 ('tabs','if (look.indicatorFullWidth) widths[index]','if (false) widths[index]'),
 ('tabs','Role.Tab','Role.Button'),
 ('rules','pager.animateScrollToPage(page)','pager.animateScrollToPage(0)'),
 ('logs','OriginalTabStrip(pager.currentPage','PrimaryTabRow(pager.currentPage'),
]
for key, old, new in mutations:
    changed=dict(source);changed[key]=changed[key].replace(old,new,1)
    assert changed[key]!=source[key], (key,old)
    try: rejected=not all(checks(changed).values())
    except ValueError: rejected=True
    assert rejected, ('mutation escaped',key,old)
paths=['app/src/full/res/layout/item_allowed_app.xml','app/src/full/res/layout/item_blocked_app.xml',
 'app/src/full/res/layout/list_item_app_domain_details.xml','app/src/full/res/layout/list_item_app_ip_details.xml',
 'app/src/main/res/layout/list_item_app_country_details.xml','app/src/main/res/layout/activity_custom_rules.xml',
 'app/src/main/res/layout/activity_network_logs.xml','app/src/main/res/values/styles.xml']
raw={path:original(path) for path in paths}
xml={path:ET.fromstring(text) for path,text in raw.items()}
def node(path,id): return next(n for n in xml[path].iter() if n.get(A+'id')=='@+id/'+id)
allowed,blocked,domain,ip,country,rules,logs,styles=paths
assert xml[allowed].get(A+'layout_marginHorizontal')=='4dp'
assert xml[blocked].get(A+'layout_marginHorizontal')=='6dp'
for path,prefix in ((allowed,'allowed'),(blocked,'blocked')):
    assert xml[path].get(APP+'cardUseCompatPadding')=='true'
    assert list(xml[path])[0].get(A+'paddingVertical')=='10dp'
    assert node(path,prefix+'_icon_container').get(A+'layout_width')=='44dp'
    assert node(path,prefix+'_app_icon').get(A+'layout_width')=='28dp'
    assert node(path,prefix+'_app_name').get(A+'ellipsize')=='marquee'
assert node(blocked,'blocked_app_time').get(APP+'layout_constraintStart_toEndOf')=='@id/blocked_app_count'
for path,title,subtitle in ((domain,'acd_domain','acd_ip_address'),(ip,'acd_ip_address','acd_domain_name')):
    assert node(path,title).get(A+'ellipsize')=='start' and A+'maxLines' not in node(path,title).attrib
    assert node(path,subtitle).get(A+'maxLines')=='3'
    assert node(path,'acd_count').get(A+'fontFamily')=='sans-serif-light'
    assert node(path,'acd_count').get(A+'maxWidth')=='60dp'
assert node(country,'acc_country_name').get(A+'textStyle')=='bold'
assert xml[country].get(A+'paddingStart')=='4dp' and xml[country].get(A+'paddingEnd')=='16dp'
assert node(country,'acc_progress').get(APP+'trackThickness')=='3dp'
for path in (rules,logs): assert node(path,'logs_act_tabLayout').get('style')=='@style/TabbedLayoutDefault'
style={n.get('name'):{i.get('name'):i.text for i in n} for n in xml[styles].findall('style')}
assert style['TabbedLayoutDefault']['tabMode']=='scrollable'
assert style['TabbedLayoutDefault']['tabPaddingStart']=='25dp'
assert style['TabbedLayoutDefault']['tabContentStart']=='30dp'
assert style['TabTextDefault']['android:textSize']=='16sp'
assert style['TabTextDefault']['textAllCaps']=='false'
report={'result':'PASS_SOURCE_ONLY','findings':['S21','S24','S27'],'baseline':BASE,'pre_patch_compose':BEFORE,
 'checks':results,'negative_mutations_rejected':len(mutations),'baseline_sha256':{p:hashlib.sha256(v.encode()).hexdigest() for p,v in raw.items()},
 'source_sha256':{FILES[k]:hashlib.sha256(v.encode()).hexdigest() for k,v in source.items()},
 'independent_reference':'MDC 1.14.0 source-style inheritance and source-only widget-oracle test authored, not executed.',
 'runtime':'NOT_RUN: existing dl.google.com policy denial; no dependency retrieval, build, app or production runtime attempted.',
 'limitations':['Source integration, not compiled or visual equivalence. No layout deletion authorized by this guard.',
 'Existing >1.3 fontScale bubble action reflow is preserved as a current accessibility alternate, not claimed original default geometry.',
 'Synthetic native-style/layout/callback tests are authored but unrun; exact pixels, font metrics, ripple animation and TalkBack need approved isolated execution.',
 'NetworkLogs console action geometry and surrounding BubbleScreen shell are outside the bounded row/tab work.']}
output=ROOT/'docs/xml-layout-removal/rows-tabs-source-style-results.json'
output.write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps({'result':report['result'],'checks':len(results),'negative_mutations_rejected':len(mutations),'runtime':'NOT_RUN'},indent=2))
