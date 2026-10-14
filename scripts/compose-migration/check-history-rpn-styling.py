#!/usr/bin/env python3
"""Source-only geometry/controller regression guard. Never loads an app or fetches tools.

Source assertions and mutation controls are not compilation, pixel parity, or device tests.
All XML evidence is read from pinned Git, so layout resource removal cannot erase the reference.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
BEFORE = 'f07b51b6fb2528d55b396aed62bbb9b700893c70'
UI = 'app/src/main/java/com/celzero/bravedns/ui/'
FILES = {
    'row': UI + 'compose/purchase/HistoryRowContent.kt',
    'screen': UI + 'compose/purchase/HistoryScreen.kt',
    'tile': UI + 'compose/rpn/RpnServerTile.kt',
    'loading': UI + 'compose/rpn/RpnLoadingSkeletons.kt',
    'country': UI + 'adapter/CountryServerAdapter.kt',
    'selection': UI + 'compose/rpn/RpnSelectionScreen.kt',
    'watermark': UI + 'compose/rpn/RpnFlagWatermark.kt',
    'vpn': UI + 'adapter/VpnServerAdapter.kt',
    'row_state': UI + 'compose/rpn/RpnServerRowState.kt',
    'type': UI + 'compose/rpn/RpnSourceTypography.kt',
}
NS = {'a': 'http://schemas.android.com/apk/res/android', 'm': 'http://schemas.android.com/apk/res-auto'}

def git(revision, path):
    return subprocess.check_output(['git', 'show', revision + ':' + path], cwd=ROOT, text=True)

def compact(s):
    # Preserve strings and numerals; only source whitespace is irrelevant to these checks.
    return re.sub(r'\s+', '', s)

def block(s, prefix):
    start = s.index(prefix)
    start = s.index('{', start)
    depth = 1
    end = start + 1
    # These pinned callback/drawing blocks contain no braces inside string literals.
    while depth:
        depth += (s[end] == '{') - (s[end] == '}')
        end += 1
    return s[start:end]

XML = [
    'activity_purchase_history', 'activity_server_order_history', 'list_item_purchase_history',
    'list_item_server_order', 'list_item_shimmer_card', 'list_item_shimmer_server_row',
    'list_item_country_card', 'item_server_group', 'item_section_header', 'list_item_vpn_server_add',
    'list_item_vpn_server', 'fragment_server_selection'
]

def validate(src):
    results = []
    def check(name, ok):
        results.append({'name': name, 'passed': bool(ok)})
    def contains(key, name, *snippets):
        check(name, all(compact(x) in compact(src[key]) for x in snippets))
    contains('row', 'history icon beside text, original circle/icon sizes and spacing',
             'Row(Modifier.fillMaxWidth().padding(horizontal = if (server) 10.dp else 20.dp, vertical = 16.dp)',
             'Modifier.size(if (server) 32.dp else 44.dp)', 'CircleShape', 'Modifier.size(24.dp)',
             'Column(Modifier.weight(1f).padding(start = 14.dp))')
    contains('row', 'history divider thickness/inset/theme',
             'start = if (server) 55.dp else 78.dp', 'thickness = if (server) 1.dp else .5.dp', 'color = themeColor(R.attr.border)')
    order = block(src['row'], 'private fun ServerOrderText')
    check('server order title/status then type/test then date/order/token sequence',
          [order.index(x) for x in ('state.tvProductName', 'state.tvStatusChip', 'state.tvTypeBadge', 'state.tvTestBadge',
                                  'state.tvDatePrimary', 'state.tvDateSecondary', 'state.tvOrderId', 'state.tvToken')] ==
          sorted(order.index(x) for x in ('state.tvProductName', 'state.tvStatusChip', 'state.tvTypeBadge', 'state.tvTestBadge',
                                         'state.tvDatePrimary', 'state.tvDateSecondary', 'state.tvOrderId', 'state.tvToken')))
    purchase = block(src['row'], 'private fun PurchaseHistoryText')
    check('purchase badge/transition precede timestamp, reason and hidden token',
          purchase.index('state.tvStateBadge') < purchase.index('state.tvTransition') < purchase.index('state.tvTimestamp') < purchase.index('state.tvReason') < purchase.index('state.tvPurchaseToken'))
    contains('row', 'timestamp timer and source maximum lines/fades/monospace',
             'painterResource(R.drawable.ic_timer)', 'Modifier.padding(start = 4.dp)', 'maxLines = 2',
             'Modifier.padding(top = 4.dp).alpha(.7f)', 'Modifier.padding(top = 2.dp).alpha(.5f)', 'FontFamily.Monospace')
    contains('screen', 'shared weighted card wraps uncarded lazy items',
             'OriginalCardSurface(Modifier.fillMaxWidth().weight(1f)', 'items(count, key = key) { index -> row(index) }',
             'contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp)', 'Column(Modifier.fillMaxSize().padding(bottom = 100.dp))')
    check('history rows are not individually carded', 'index -> Card(' not in src['screen'])
    contains('screen', 'summary horizontal grouping and XML metrics',
             'private fun HistorySummary', 'Row(Modifier.fillMaxWidth().padding(16.dp)',
             'Modifier.size(36.dp).padding(4.dp)', 'Column(Modifier.weight(1f).padding(start = 12.dp))', 'fontSize = 22.sp')
    contains('screen', 'empty/error centered paddings and original opacity/type metrics',
             'padding(horizontal = 32.dp, vertical = 56.dp)', 'Modifier.size(72.dp).alpha(.5f)', 'fontSize = 16',
             'Modifier.padding(top = 8.dp), fontSize = 13', 'Modifier.size(64.dp).alpha(.7f)', 'fontSize = 14.sp')
    contains('screen', 'server fetch progress and distinct signature alpha',
             'height(3.dp)', 'trackColor = themeColor(R.attr.chipBgColorPositive)', 'alpha(if (server) 1f else .8f)')
    contains('loading', 'server skeleton exact icon/text/trailing shape dimensions',
             'size(36.dp)', 'padding(start = 12.dp)', 'width(120.dp).height(14.dp)',
             'padding(top = 6.dp).width(70.dp).height(10.dp)', 'size(20.dp)',
             'padding(start = 4.dp, end = 4.dp, bottom = 8.dp)', 'RoundedCornerShape(12.dp)')
    contains('loading', 'purchase skeleton five vertical source cards',
             'repeat(5)', 'padding(14.dp)', 'width(72.dp).height(12.dp)',
             'padding(top = 8.dp).width(96.dp).height(24.dp)', 'padding(top = 6.dp).width(80.dp).height(10.dp)')
    contains('selection', 'RPN five server skeleton rows and original list padding',
             'RpnServerLoadingRows(count = 5, running = ui.shimmerServerList.shimmerRunning',
             'padding(start = 16.dp, end = 16.dp, bottom = 12.dp)')
    contains('screen', 'server history four server rows and purchase-specific variant',
             'RpnServerLoadingRows(count = 4, running = state.shimmerRunning',
             'durationMillis = 1500', 'baseAlpha = .85f, highlightAlpha = .35f', 'PurchaseHistoryLoadingRows(state.shimmerRunning)')
    contains('loading', 'RPN shimmer timing/alpha and no live animation when stopped',
             'durationMillis: Int = 1200', 'baseAlpha: Float = .10f', 'highlightAlpha: Float = .30f',
             'val phase = if (running)', '} else 0f', 'BlendMode.DstIn')
    contains('tile', 'add-tile dash pattern with outer margin before minimum height',
             'padding(6.dp).heightIn(min = 80.dp)', 'val stroke = 1.dp.toPx()',
             'PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))', 'CornerRadius(16.dp.toPx())')
    contains('tile', 'add-tile original single-line medium tracked label',
             'tileTextStyle(11.sp).copy(fontWeight = FontWeight.Medium, letterSpacing = .02.em)', 'maxLines = 1')
    contains('tile', 'plain two-column source grid with equal-height rows and no invented gap',
             'val cell = (constraints.maxWidth / 2)', 'it.maxIntrinsicHeight(cell)',
             'Constraints.fixed(cell, heights[index / 2])', 'child.placeRelative(column * cell, y)',
             'layout(constraints.maxWidth, heights.sum())')
    contains('tile', 'source content card style, watermark and recent-app icon layering',
             'style = R.style.RethinkPlus_ContentCard', 'RpnFlagWatermark(ui.flagWatermark, Modifier.matchParentSize())',
             'listOf(ui.routedAppIcon2 to 0, ui.routedAppIcon1 to 10, ui.routedAppIcon0 to 20)',
             'background(themeColor(R.attr.vpnHeaderGradientStart), CircleShape)')
    contains('country', 'country avatar source metrics and font padding',
             'size(36.dp).background(themeColor(R.attr.colorSurfaceVariant), CircleShape)',
             'countryTextStyle(12.sp, includeFontPadding = false)', 'rpnDimensionSp(R.dimen.mini_font_text_view)',
             'letterSpacing = .02.em', 'padding(start = 13.dp, end = 10.dp)')
    contains('country', 'alphabet section original typography and padding',
             'padding(start = 4.dp, end = 4.dp, top = 14.dp, bottom = 4.dp)',
             'countryTextStyle(11.sp).copy(fontWeight = FontWeight.Bold, letterSpacing = .1.em)')
    contains('country', 'plain country grouping, inset divider and nested rows',
             'HorizontalDivider(Modifier.padding(start = 53.dp)',
             'padding(start = 14.dp, end = 4.dp, bottom = 6.dp)',
             'heightIn(min = 56.dp)', 'padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)',
             'Row(Modifier.padding(start = 12.dp)', 'padding(end = 5.dp).size(7.dp)', 'Modifier.size(28.dp)')
    check('country rows have no added card borders/background', '.border(' not in src['country'])
    old_country = git(BEFORE, FILES['country'])
    check('favourite action callback unchanged', compact(block(old_country, 'IconButton({')) == compact(block(src['country'], 'IconButton({')))
    check('city selection/stopped callback unchanged',
          compact('if (proxyStoppedValue) listener.onProxyStoppedItemTapped() else listener.onCitySelected(group.servers.first(), !group.isEnabled)') in compact(src['country']))
    for name in ['speedInfo', 'loadInfo', 'sectionLetter']:
        check('country ' + name + ' body unchanged', compact(block(old_country, 'fun '+name)) == compact(block(src['country'], 'fun '+name)))
    added = ['''                ui.flagWatermark.setFlagDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_rpn_auto),
                    R.attr.primaryTextColor,
                    peakAlpha = 0.25f
                )\n''', '                ui.flagWatermark.setFlagText(group.flagEmoji)\n']
    unchanged = src['vpn']
    for text in added:
        check('watermark bind exactly one source assignment: '+text.strip().splitlines()[0], unchanged.count(text) == 1)
        unchanged = unchanged.replace(text, '')
    check('entire VPN adapter unchanged except the two watermark presentation assignments', unchanged == git(BEFORE, FILES['vpn']))
    old_watermark = git(BASE, UI + 'custom/FlagWatermarkView.kt')
    for name in ['flagCenter', 'buildMask', 'drawFlag']:
        check('watermark '+name+' drawing body byte-exact from baseline',
              block(src['watermark'], 'fun '+name) == block(old_watermark, 'fun '+name))
    contains('watermark', 'watermark source constants, native drawing and RTL',
             'CONTENT_SIZE_DP = 20f', 'CARD_CONTENT_SCALE = 4f', 'CARD_END_BLEED_FRACTION = 0.3f',
             'CARD_TILT_DEGREES = -35f', 'CARD_MASK_RADIUS_FACTOR = 5f', 'if (rtl) 0f else w',
             'canvas.saveLayer(0f, 0f, w, h, null)', 'drawContext.canvas.nativeCanvas')
    contains('type', 'dimension references retain qualifier and font scaling',
             'LocalContext.current.resources.getDimension(id)', 'with(LocalDensity.current) { pixels.toSp() }')
    check('owned renderers do not inflate XML or instantiate a View',
          all(not re.search(r'AndroidView\s*\(|LayoutInflater|\.inflate\s*\(', src[x]) for x in ['row','screen','tile','loading','watermark','type']))
    return results


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--report')
    args = parser.parse_args()
    src = {key: (ROOT/path).read_text() for key, path in FILES.items()}
    results = validate(src)
    evidence = {}
    for name in XML:
        path = 'app/src/main/res/layout/' + name + '.xml'
        xml = git(BASE, path)
        ET.fromstring(xml)
        evidence[path] = hashlib.sha256(xml.encode()).hexdigest()
    mutations = [('tile', 'floatArrayOf(6.dp.toPx(), 4.dp.toPx())', 'floatArrayOf(1.dp.toPx(), 1.dp.toPx())'),
                 ('country', 'countryTextStyle(12.sp, includeFontPadding = false)', 'countryTextStyle(26.sp, includeFontPadding = false)'),
                 ('screen', 'index -> row(index)', 'index -> Card { row(index) }'),
                 ('loading', 'width(120.dp).height(14.dp)', 'width(100.dp).height(14.dp)'),
                 ('vpn', 'ui.flagWatermark.setFlagText(group.flagEmoji)', 'ui.flagWatermark.setFlagText("")')]
    mutation_results = []
    for key, old, new in mutations:
        changed = dict(src)
        assert old in changed[key], old
        changed[key] = changed[key].replace(old, new, 1)
        mutation_results.append({'file': FILES[key], 'mutation': new, 'rejected': not all(x['passed'] for x in validate(changed))})
    report = {'baseline': BASE, 'pre_edit_checkpoint': BEFORE, 'verification_kind': 'source-only, not Kotlin compilation or visual equivalence',
              'checks': results, 'negative_controls': mutation_results, 'baseline_layout_sha256': evidence,
              'source_sha256': {FILES[k]: hashlib.sha256(v.encode()).hexdigest() for k,v in src.items()},
              'unrun': ['Android/Kotlin compilation', 'Compose/Robolectric tests', 'device/screenshots', 'TalkBack/RTL/font-scale visual checks'],
              'remaining_limits': ['Material and framework implicit defaults require approved runtime reference comparison.',
                  'History collapsing toolbar scrim/parallax/title placement remains a separate source/runtime gate.',
                  'Shimmer placeholder composition/timing/alpha are guarded, but exact library sweep/dropoff/tilt and stopped-frame appearance require visual comparison.',
                  'RPN header/subscription skeletons and premium subscription loading layout are outside this bounded row/history fix.']}
    if args.report:
        output = Path(args.report)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(report, indent=2)+'\n')
    failed = [x['name'] for x in results if not x['passed']]
    failed += [x['mutation'] for x in mutation_results if not x['rejected']]
    if failed:
        print('FAIL:\n'+'\n'.join(failed)); return 1
    print(f'PASS: {len(results)} source styling/controller assertions; {len(mutation_results)} rejected mutations. Compilation/runtime not run.')
    return 0

if __name__ == '__main__':
    sys.exit(main())
