#!/usr/bin/env python3
"""Pinned-source controls guard; no Android toolchain, network, production app, or traffic."""
import argparse
import functools
import hashlib
import json
import re
import subprocess
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
PREFIX = 'app/src/main/java/com/celzero/bravedns/'
FILES = {
    'stats': PREFIX + 'ui/compose/statistics/SummaryStatisticsScreen.kt',
    'filters': PREFIX + 'ui/compose/statistics/SummaryFilterSheetContent.kt',
    'dns': PREFIX + 'ui/compose/dns/DnsEndpointEditors.kt',
    'relays': PREFIX + 'ui/compose/dns/DnsEndpointScreen.kt',
}
ANDROID = '{http://schemas.android.com/apk/res/android}'
APP = '{http://schemas.android.com/apk/res-auto}'

@functools.lru_cache(None)
def baseline(path):
    return subprocess.check_output(['git', 'show', f'{BASE}:{path}'], cwd=ROOT, text=True)

@functools.lru_cache(None)
def nodes(path):
    return {e.get(ANDROID + 'id', '').removeprefix('@+id/'): e for e in ET.fromstring(baseline(path)).iter() if ANDROID + 'id' in e.attrib}

def snake(value):
    return re.sub(r'([A-Z])', lambda m: '_' + m[1].lower(), value)

def body(text, name, end):
    return text[text.index(name):text.index(end, text.index(name))]

def check(sources):
    stats, filters, dns, relays = (sources[k] for k in FILES)
    catalog = (ROOT / (PREFIX + 'ui/compose/controls/OriginalControlCatalog.kt')).read_text()
    styles = {e.get('name'): e for e in ET.parse(ROOT / 'app/src/main/res/values/original_control_styles.xml').getroot()}
    checked = set()
    def source_contract(layout, identity, source_set='main'):
        key = layout + ':' + identity
        assert f'"{key}" to OriginalControlSpec(' in catalog, 'Missing catalog key: ' + key
        original = nodes(f'app/src/{source_set}/res/layout/{layout}.xml')[identity]
        generated = styles['OriginalControl_' + layout + '_' + identity]
        parent = original.get('style')
        if parent:
            assert generated.get('parent') == parent, 'Wrong variant: ' + key
        values = {item.get('name'): item.text for item in generated}
        # Layout position is caller-owned. Native paints must retain every concrete appearance override.
        for ns, name in [(ANDROID, 'textColor'), (ANDROID, 'textSize'), (ANDROID, 'insetTop'),
                         (ANDROID, 'insetBottom'), (ANDROID, 'insetLeft'), (ANDROID, 'insetRight'),
                         (APP, 'cornerRadius'), (APP, 'icon'), (APP, 'iconTint'), (APP, 'iconPadding'),
                         (APP, 'chipBackgroundColor'), (APP, 'chipSurfaceColor'), (APP, 'checkedIconVisible')]:
            if ns + name in original.attrib:
                item = ('android:' if ns == ANDROID else '') + name
                assert values.get(item) == original.get(ns + name), 'Changed source appearance: ' + key + ':' + item
        checked.add(key)
        return original

    controller = baseline(PREFIX + 'ui/fragment/SummaryStatisticsFragment.kt')
    routes = re.findall(r'b\.(fss\w+)\.setOnClickListener\s*\{\s*openDetailedStatsUi\(SummaryStatisticsType\.(\w+)\)', controller)
    expected = {'list': {}, 'insights': {}}
    for view, type_name in routes:
        identity = snake(view)
        expected['insights' if identity.startswith('fss_ia_') else 'list'][type_name] = identity
    mapping = body(stats, 'internal fun statisticsDetailSource', '@Composable\nprivate fun StatsFilterBadge')
    insight, listing = mapping.split('} else when (type)')
    parse_mapping = lambda s: dict(re.findall(r'Type\.(\w+) -> "([^"]+)"', s))
    assert parse_mapping(insight) == expected['insights'], 'Insights action source no longer matches controller destination'
    assert parse_mapping(listing) == expected['list'], 'List action source no longer matches controller destination'
    assert len(routes) == 22
    for view in [snake(v) for v, _ in routes] + ['fss_close_conns_chip', 'fss_ia_close_conns_chip']:
        e = source_contract('fragment_summary_statistics', view)
        assert e.get('style') == '@style/ThinnerChip.Action'
    for token in ['OriginalButton(statisticsDetailSource(type, state.mode)',
                  'onClick = { state.onDetail(type) }', 'onClick = state.onClose',
                  'Spacer(Modifier.width(6.dp))', '"fss_ia_close_conns_chip" else "fss_close_conns_chip"']:
        assert token in stats, 'Missing action integration: ' + token

    mode = body(stats, 'private fun StatsModeButton', '/** Each action')
    for token in ['segmentCount = 2, grouped = true', 'iconTint = themeColor(if (selected) R.attr.homeScreenHeaderTextColor else R.attr.defaultToggleBtnTxt)',
                  'containerColor = themeColor(if (selected) R.attr.accentGood else R.attr.defaultToggleBtnBg)',
                  'onClick = { if (!selected) onClick() }', 'contentDescription = label']:
        assert token in mode, 'Mode control contract changed: ' + token
    assert 'leadingIcon' not in mode and 'Modifier.size(22.dp)' not in mode, 'Catalog icons must retain source size'
    assert stats.count('.selectableGroup()') >= 2
    assert stats.count('Arrangement.spacedBy(-dimensionResource(R.dimen.default_stroke_width))') == 2
    for identity in ['fss_view_mode_list_btn', 'fss_view_mode_insights_btn', 'fss_filter_btn']:
        e = source_contract('fragment_summary_statistics', identity)
        assert e.get(ANDROID + 'layout_width') == '32dp' and e.get(ANDROID + 'layout_height') == '32dp'
        assert e.get(APP + 'cornerRadius') == ('16dp' if identity == 'fss_filter_btn' else '2dp')
    for identity in ['tb_recent_toggle_btn', 'tb_daily_toggle_btn', 'tb_weekly_toggle_btn']:
        e = source_contract('fragment_summary_statistics', identity)
        assert e.get(ANDROID + 'minWidth') == '100dp' and e.get(APP + 'cornerRadius') == '16dp'
    for token in ['onClick = { state.onTimeCategory(tc) }', 'segmentIndex = index, segmentCount = 3',
                  'R.attr.homeScreenHeaderTextColor else R.attr.primaryTextColor', 'R.attr.accentGood else R.attr.defaultToggleBtnBg']:
        assert token in stats, 'Time group changed: ' + token

    filter_map = dict(re.findall(r'SectionFilter\.(\w+) -> "(fss_filter_\w+)"', filters))
    expected_filters = {name: 'fss_filter_' + suffix + '_chip' for name, suffix in
                        [('APPS', 'apps'), ('COUNTRIES', 'countries'), ('PROVIDERS', 'providers'), ('BLOCKLISTS', 'blocklist'), ('DOMAINS', 'domains'), ('IPS', 'ips')]}
    assert filter_map == expected_filters
    for identity in expected_filters.values():
        e = source_contract('bottom_sheet_summary_stats_filter', identity)
        assert e.get(ANDROID + 'layout_margin') == '3dp'
        assert e.get('style') == '@style/ThinnerChip.Large' and e.get(APP + 'checkedIconVisible') == 'false'
    for identity in ['ssfc_select_all', 'ssfc_apply']:
        source_contract('bottom_sheet_summary_stats_filter', identity)
    for token in ['OriginalChip(statisticsFilterSource(filter)', 'Modifier.padding(3.dp)',
                  'FlowRow(Modifier.fillMaxWidth().padding(top = 6.dp)',
                  'Arrangement.spacedBy(6.dp)', 'Arrangement.spacedBy(4.dp)',
                  'selection::selectAll', 'onClick = { selection.toggle(filter) }',
                  'filter != SectionFilter.PROVIDERS || providers']:
        assert token in filters, 'Filter integration changed: ' + token

    badge = nodes('app/src/main/res/layout/fragment_summary_statistics.xml')['fss_filter_badge']
    assert badge.get(ANDROID + 'textSize') == '9sp' and badge.get(ANDROID + 'minWidth') == '16dp'
    drawable = ET.fromstring(baseline('app/src/main/res/drawable/bg_filter_badge.xml'))
    assert drawable.get(ANDROID + 'shape') == 'oval' and drawable.find('solid').get(ANDROID + 'color') == '@color/accentBad'
    for token in ['drawOval(badgeColor)', 'colorResource(R.color.accentBad)', 'minWidth = 16.dp, minHeight = 16.dp',
                  'padding(horizontal = 4.dp)', 'size = 9f, weight = android.graphics.Typeface.BOLD',
                  'color = Color.White', 'padding(end = 10.dp, bottom = 10.dp)']:
        assert token in stats, 'Badge source contract changed: ' + token
    assert 'Badge(Modifier' not in stats

    variants = {
        'DOH': ('dialog_set_custom_doh', 'dialog_custom_url_cancel_btn', 'dialog_custom_url_ok_btn'),
        'ODOH': ('dialog_set_custom_odoh', 'dialog_custom_url_cancel_btn', 'dialog_custom_url_ok_btn'),
        'CRYPT': ('dialog_set_dns_crypt', 'dialog_dns_crypt_cancel_btn', 'dialog_dns_crypt_ok_btn'),
        'PROXY': ('dialog_set_dns_proxy', 'dialog_dns_proxy_cancel_btn', 'dialog_dns_proxy_apply_btn'),
    }
    for name, (layout, cancel, apply) in variants.items():
        assert f'{name}("{layout}:{cancel}", "{layout}:{apply}")' in dns
        for identity in [cancel, apply]:
            e = source_contract(layout, identity, 'full')
            assert e.get('style') == '@style/Widget.Material3.Button.TextButton.Dialog'
            assert e.get(ANDROID + 'textColor') == '?attr/accentGood'
    assert 'DialogSetCustomDohBinding.inflate(layoutInflater)' in baseline(PREFIX + 'ui/fragment/DoTListFragment.kt')
    assert 'if (kind == DnsServerKind.ODOH) DnsEditorButtons.ODOH else DnsEditorButtons.DOH' in dns
    dialog = body(dns, '@Composable private fun DnsEditorDialog', '@Composable private fun DnsField')
    assert 'OriginalButton(buttons.cancelSource, onDismiss' in dialog and 'OriginalButton(buttons.applySource, onApply' in dialog
    assert '@dialog_' not in dialog and 'color = themeColor' not in dialog
    assert 'val resources = LocalResources.current' in dns, 'Preserve lint154 localization fix'
    source_contract('dialog_dnscrypt_relays', 'custom_dialog_ok_button')
    assert 'OriginalButton("dialog_dnscrypt_relays:custom_dialog_ok_button", onDismiss' in relays
    return sorted(checked)

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--self-test', action='store_true')
    parser.add_argument('--report', type=Path)
    args = parser.parse_args()
    sources = {name: (ROOT / path).read_text() for name, path in FILES.items()}
    contracts = check(sources)
    rejected = 0
    if args.self_test:
        mutations = [
            ('stats', 'Type.MOST_BLOCKED_APPS -> "fss_ia_blocked_apps_chip"', 'Type.MOST_BLOCKED_APPS -> "fss_ia_allowed_apps_chip"'),
            ('stats', 'segmentCount = 2, grouped = true', 'segmentCount = 2, grouped = false'),
            ('stats', 'iconTint = themeColor(if (selected)', 'contentColor = themeColor(if (selected)'),
            ('stats', 'Arrangement.spacedBy(-dimensionResource(R.dimen.default_stroke_width))', 'Arrangement.spacedBy(4.dp)'),
            ('stats', 'onClick = { if (!selected) onClick() }', 'onClick = { onClick() }'),
            ('stats', 'drawOval(badgeColor)', 'drawRect(badgeColor)'),
            ('stats', 'size = 9f, weight = android.graphics.Typeface.BOLD', 'size = 14f, weight = android.graphics.Typeface.BOLD'),
            ('filters', 'SectionFilter.PROVIDERS -> "fss_filter_providers_chip"', 'SectionFilter.PROVIDERS -> "fss_filter_apps_chip"'),
            ('filters', 'Modifier.padding(3.dp)', 'Modifier.padding(0.dp)'),
            ('filters', 'onClick = { selection.toggle(filter) }', 'onClick = {}'),
            ('dns', 'OriginalButton(buttons.cancelSource, onDismiss', 'OriginalButton("@dialog_negative", onDismiss'),
            ('dns', 'DnsEditorButtons.ODOH else DnsEditorButtons.DOH', 'DnsEditorButtons.ODOH else DnsEditorButtons.ODOH'),
            ('relays', '"dialog_dnscrypt_relays:custom_dialog_ok_button", onDismiss', '"@dialog_negative", onDismiss'),
        ]
        for name, old, new in mutations:
            altered = dict(sources)
            assert old in altered[name], 'Invalid mutation: ' + old
            altered[name] = altered[name].replace(old, new, 1)
            try:
                check(altered)
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError('Negative mutation was accepted: ' + old)
    result = {'status': 'PASS', 'baseline': BASE, 'source_only': True,
              'source_instances_checked': len(contracts), 'negative_mutations_rejected': rejected,
              'contracts': contracts, 'file_sha256': {FILES[k]: hashlib.sha256(v.encode()).hexdigest() for k, v in sources.items()},
              'unrun': ['Android compilation', 'Robolectric', 'rendered screenshots', 'TalkBack/touch geometry'],
              'limitation': 'Pinned source/resource and callback guard only. No runtime or pixel equivalence claim.'}
    if args.report:
        args.report.write_text(json.dumps(result, indent=2) + '\n')
    print(f'PASS: {len(contracts)} source instances; 22 baseline destination/style mappings; {rejected} negative mutations rejected.')
    print(result['limitation'])

if __name__ == '__main__':
    main()
