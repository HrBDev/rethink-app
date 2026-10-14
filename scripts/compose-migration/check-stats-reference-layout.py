#!/usr/bin/env python3
"""Source-only Stats screenshot restoration guard. No Gradle, downloads, app, device, or traffic."""
import argparse
import hashlib
import json
import re
import subprocess
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
SCREEN = 'app/src/main/java/com/celzero/bravedns/ui/compose/statistics/SummaryStatisticsScreen.kt'
TEST = 'app/src/test/java/com/celzero/bravedns/ui/compose/statistics/SummaryStatisticsReferenceLayoutTest.kt'
LAYOUT = 'app/src/main/res/layout/fragment_summary_statistics.xml'
A = '{http://schemas.android.com/apk/res/android}'
P = '{http://schemas.android.com/apk/res-auto}'


def original_xml():
    return ET.fromstring(subprocess.check_output(['git', 'show', f'{BASE}:{LAYOUT}'], cwd=ROOT, text=True))


def function(source, name):
    start = source.index('fun ' + name + '(')
    opening = source.index('{', start)
    depth = 1
    for end in range(opening + 1, len(source)):
        depth += (source[end] == '{') - (source[end] == '}')
        if depth == 0:
            return source[start:end + 1]
    raise AssertionError('Unterminated function: ' + name)


def check(source, test, xml):
    nodes = {n.get(A + 'id', '').replace('@+id/', ''): n for n in xml.iter() if A + 'id' in n.attrib}
    insights = nodes['fss_insights_container']
    card = insights.find('com.google.android.material.card.MaterialCardView')
    assert card.get(P + 'cardCornerRadius') == '16dp'
    assert card.get(P + 'strokeWidth') == '1dp'
    assert card.get(P + 'strokeColor') == '?attr/secondaryTextColor'
    assert card.get(A + 'layout_marginTop') == '12dp'
    row = card.find('LinearLayout/LinearLayout')
    assert card.find('LinearLayout').get(A + 'padding') == '14dp'
    columns, dividers = row.findall('LinearLayout'), row.findall('View')
    assert len(columns) == 3 and len(dividers) == 2
    for column in columns:
        assert column.get(A + 'layout_weight') == '1' and column.get(A + 'gravity') == 'center'
        value, caption = list(column)
        assert value.get(A + 'textStyle') == 'bold'
        assert value.get(A + 'textSize') == '@dimen/heading_font_text_view'
        assert value.get(A + 'singleLine') == 'true'
        assert caption.get(A + 'textSize') == '@dimen/small_font_subheading_text'
        assert caption.get(A + 'layout_marginTop') == '2dp'
    for divider in dividers:
        for attr, expected in [('layout_width', '1dp'), ('layout_height', 'match_parent'),
                               ('layout_marginStart', '6dp'), ('layout_marginEnd', '6dp'),
                               ('layout_marginTop', '4dp'), ('layout_marginBottom', '4dp')]:
            assert divider.get(A + attr) == expected
    for identity in ['tb_recent_toggle_btn', 'tb_daily_toggle_btn', 'tb_weekly_toggle_btn']:
        assert nodes[identity].get(A + 'minWidth') == '100dp'
    for identity, glyph in [('fss_view_mode_list_btn', 'ic_view_list'),
                            ('fss_view_mode_insights_btn', 'ic_view_chart'), ('fss_filter_btn', 'ic_filter')]:
        assert nodes[identity].get(P + 'icon') == '@drawable/' + glyph
    usage = function(source, 'TrafficUsage')
    requirements = [
        'if (usage == null) return',
        'val total = usage.totalDownload + usage.totalUpload',
        'val unmetered = total - usage.meteredDataUsage',
        'val metered = usage.meteredDataUsage',
        'listOf(R.string.ada_app_unmetered to unmetered, R.string.ada_app_metered to metered, R.string.lbl_overall to total)',
        'OriginalCardSurface(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp)',
        'cornerRadius = 16.dp, border = BorderStroke(1.dp, themeColor(R.attr.secondaryTextColor))',
        'if (insights) {',
        'utilityDimensionSp(context, R.dimen.heading_font_text_view)',
        'weight = android.graphics.Typeface.BOLD',
        'utilityDimensionSp(context, R.dimen.small_font_subheading_text)',
        'Row(Modifier.fillMaxWidth().padding(14.dp).height(IntrinsicSize.Min)',
        'Column(Modifier.weight(1f).testTag("stats_usage_column_$index")',
        'horizontalAlignment = Alignment.CenterHorizontally',
        'if (index > 0) Box(Modifier.padding(horizontal = 6.dp, vertical = 4.dp)',
        '.fillMaxHeight().width(1.dp).background(themeColor(R.attr.secondaryTextColor))',
        'Modifier.testTag("stats_usage_value_$index"), maxLines = 1',
        'color = themeColor(R.attr.primaryTextColor), style = valueStyle',
        'Modifier.padding(top = 2.dp).testTag("stats_usage_caption_$index")',
        'color = themeColor(R.attr.primaryLightColorText), style = captionStyle',
        'LinearProgressIndicator(progress = { if (ump + mp == 0) 0f else ump.toFloat() / (ump + mp) }',
        'testTag("stats_usage_progress")',
    ]
    for token in requirements:
        assert token in usage, 'Missing source usage contract: ' + token
    assert usage.index('stats_usage_value_$index') < usage.index('stats_usage_caption_$index')
    assert usage.index('} else {') < usage.index('LinearProgressIndicator'), 'Progress is list-only'
    assert 'FlowRow' not in usage and 'MaterialTheme.shapes' not in usage, 'No wrapping/default-shaped KPI tiles'
    screen = function(source, 'SummaryStatisticsScreen')
    for token in [
        'TrafficUsage(if (state.mode == StatsViewMode.INSIGHTS) state.insightUsage else state.listUsage, state.mode == StatsViewMode.INSIGHTS)',
        'Arrangement.spacedBy(-dimensionResource(R.dimen.default_stroke_width), Alignment.CenterHorizontally)',
        'Modifier.widthIn(min = 100.dp)',
        'segmentIndex = index, segmentCount = 3',
        'selected = state.timeCategory == tc',
        'onClick = { state.onTimeCategory(tc) }',
        'onClick = state.onFilter',
        'WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)',
        'utilityTextStyle(R.style.TextAppearance_Heading_HomeScreen)',
        'utilityTextStyle(R.style.TextAppearance_SubHeading_HomeScreen)',
        'if (state.alpha) appName else appName.uppercase(locale)',
        'ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault()',
        'state.onMode(StatsViewMode.LIST)',
        'state.onMode(StatsViewMode.INSIGHTS)',
    ]:
        assert token in screen, 'Missing screen state/layout contract: ' + token
    assert not re.search(r'state\.mode\s*=(?!=)', screen), 'Do not force the screenshot mode over controller state'
    assert 'onClick = { if (!selected) onClick() }' in function(source, 'StatsModeButton')
    for forbidden in ['AndroidView(', 'LayoutInflater', 'Binding.inflate', 'R.layout.']:
        assert forbidden not in source, 'Restoration must stay in native Compose: ' + forbidden
    for token in ['application = Application::class', 'AUTHORED / UNRUN',
                  'insightsKeepsOneCardThreeEqualColumnsVerticalRulesAndValueFirstTypography',
                  'periodControlIsCenteredFullHeightAndKeepsThreeRadioActions',
                  'suppliedModeAndIndependentUsageValuesSurviveSelectionAndRecomposition',
                  'listChartAndFilterActuallyPaintGlyphsInsideTheirControls',
                  'captureToImage()', 'fontScale = 1.5f', 'rtl = true', 'density = 1.8f']:
        assert token in test, 'Missing synthetic regression coverage: ' + token
    return len(requirements)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--self-test', action='store_true')
    parser.add_argument('--report', type=Path)
    args = parser.parse_args()
    source, test, xml = (ROOT / SCREEN).read_text(), (ROOT / TEST).read_text(), original_xml()
    requirements = check(source, test, xml)
    rejected = 0
    if args.self_test:
        mutations = [
            ('Alignment.CenterHorizontally)) {', 'Alignment.Start)) {'),
            ('OriginalCardSurface(Modifier.fillMaxWidth()', 'Surface(Modifier.fillMaxWidth()'),
            ('cornerRadius = 16.dp, border', 'cornerRadius = 12.dp, border'),
            ('val total = usage.totalDownload + usage.totalUpload', 'val total = usage.totalDownload'),
            ('val unmetered = total - usage.meteredDataUsage', 'val unmetered = total'),
            ('if (insights) {', 'if (true) {'),
            ('R.dimen.heading_font_text_view)', 'R.dimen.default_font_text_view)'),
            ('R.dimen.heading_font_text_view),\n                weight = android.graphics.Typeface.BOLD)', 'R.dimen.heading_font_text_view),\n                weight = android.graphics.Typeface.NORMAL)'),
            ('height(IntrinsicSize.Min)', 'height(4.dp)'),
            ('Column(Modifier.weight(1f).testTag("stats_usage_column_', 'Column(Modifier.testTag("stats_usage_column_'),
            ('padding(horizontal = 6.dp, vertical = 4.dp)', 'padding(horizontal = 4.dp, vertical = 6.dp)'),
            ('.fillMaxHeight().width(1.dp)', '.fillMaxWidth().height(1.dp)'),
            ('Modifier.padding(top = 2.dp).testTag("stats_usage_caption_', 'Modifier.padding(top = 0.dp).testTag("stats_usage_caption_'),
            ('state.insightUsage else state.listUsage', 'state.listUsage else state.listUsage'),
            ('onClick = { state.onTimeCategory(tc) }', 'onClick = {}'),
            ('onClick = state.onFilter', 'onClick = {}'),
            ('utilityTextStyle(R.style.TextAppearance_Heading_HomeScreen)', 'MaterialTheme.typography.headlineLarge'),
            ('WindowInsetsSides.Top + WindowInsetsSides.Horizontal', 'WindowInsetsSides.Bottom'),
            ('onClick = { if (!selected) onClick() }', 'onClick = { onClick() }'),
        ]
        for old, new in mutations:
            assert old in source, 'Invalid mutation fixture: ' + old
            try:
                check(source.replace(old, new, 1), test, xml)
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError('Negative mutation escaped: ' + old)
    result = dict(status='SOURCE_ONLY_PASS', baseline=BASE, source_layout=LAYOUT,
                  usage_contracts=requirements, mutations_rejected=rejected,
                  files={path: hashlib.sha256((ROOT / path).read_bytes()).hexdigest() for path in [SCREEN, TEST]},
                  runtime='UNRUN', visual='UNRUN',
                  limitation='Source/mutation guard only. No compilation, synthetic test execution, render, device, VPN, or traffic verification.')
    if args.report:
        args.report.write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps(result, indent=2))


if __name__ == '__main__':
    main()
