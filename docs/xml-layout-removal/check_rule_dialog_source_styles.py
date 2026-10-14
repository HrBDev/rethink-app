#!/usr/bin/env python3
"""S19/S20/S34 bounded source guard. No Android, network, toolchain or artifact invocation."""
from pathlib import Path
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
P = ROOT / 'app/src/main/java/com/celzero/bravedns/ui/compose'
paths = {'rules': P/'rules/RulesContent.kt', 'details': P/'rules/RuleDetailsContent.kt', 'dns': P/'dns/DnsEndpointEditors.kt'}
sources = {key: path.read_text() for key, path in paths.items()}
A = '{http://schemas.android.com/apk/res/android}'
APP = '{http://schemas.android.com/apk/res-auto}'
evidence = []

def xml(path):
    original = subprocess.check_output(['git', '-C', str(ROOT), 'show', BASE + ':' + path])
    # Deleted application layouts remain evidence in the pinned Git tree, never runtime fixtures.
    if '/res/layout' not in path:
        assert (ROOT/path).read_bytes() == original, 'Retained non-layout resource changed: ' + path
    root = ET.fromstring(original)
    evidence.append({'path': path, 'sha256': hashlib.sha256(original).hexdigest()})
    return root

def node(root, name):
    return next(n for n in root.iter() if n.get(A+'id', '').split('/')[-1] == name)

def attrs(n, **expected):
    for key, value in expected.items():
        assert n.get(A+key) == value, (n.tag, key, n.get(A+key), value)

# Independent baseline XML/controller evidence, including hierarchy and empty-state visibility.
frame = xml('app/src/main/res/drawable/rectangle_border_background.xml')
attrs(frame.find('stroke'), width='0.75dp', color='?attr/border')
attrs(frame.find('corners'), radius='16dp')
assert frame.find('solid') is None
for variant in ('domain', 'ip'):
    root = xml(f'app/src/full/res/layout/list_item_custom_{variant}.xml')
    attrs(node(root, f'custom_{variant}_status_icon'), background='@drawable/rectangle_border_background',
        minWidth='25dp', padding='5dp', layout_marginStart='5dp', layout_marginEnd='10dp', textSize='@dimen/default_font_text_view')
    root = xml(f'app/src/main/res/layout/bottom_sheet_custom_{variant}s.xml')
    for badge in [n for n in root.iter() if f'custom_{variant}_badge_' in n.get(A+'id', '')]:
        attrs(badge, layout_width='32dp', layout_height='32dp', textStyle='bold',
            background='@drawable/rectangle_border_background', textSize='11sp' if badge.get(A+'id').endswith('bypass_univ') else '12sp')
for prefix, path in [('dacd', 'app/src/full/res/layout/dialog_add_custom_domain.xml'), ('daci', 'app/src/main/res/layout/dialog_add_custom_ip.xml')]:
    root = xml(path)
    attrs(root, padding='10dp')
    attrs(node(root, prefix + ('_url_title' if prefix == 'dacd' else '_ip_title')), padding='10dp', textSize='@dimen/large_font_text_view')
    attrs(node(root, prefix + ('_url_desc' if prefix == 'dacd' else '_ip_desc')), layout_marginStart='5dp', padding='5dp', textSize='@dimen/small_font_subheading_text', textColor='?attr/primaryLightColorText')
    attrs(node(root, prefix + ('_failure_text' if prefix == 'dacd' else '_failure_text_view')), layout_margin='10dp', padding='5dp', textSize='@dimen/default_font_text_view', visibility='gone')
    failure = node(root, prefix + ('_failure_text' if prefix == 'dacd' else '_failure_text_view'))
    assert failure.get(APP+'layout_constraintTop_toBottomOf') is not None
    assert not any(k.startswith(APP+'layout_constraintBottom_') for k in failure.attrib)
root = xml('app/src/full/res/layout/dialog_import_confirm.xml')
attrs(root, padding='10dp')
attrs(node(root, 'dic_title'), padding='10dp', textSize='@dimen/large_font_text_view')
attrs(node(root, 'dic_file_name'), layout_marginStart='8dp', maxLines='1', ellipsize='middle', textSize='@dimen/default_font_text_view')
for ident in ('dic_file_row', 'dic_valid_row', 'dic_ignored_row'):
    attrs(node(root, ident), paddingStart='10dp', paddingEnd='10dp', paddingTop='4dp')
attrs(node(root, 'dic_action_label'), paddingTop='12dp', textSize='@dimen/default_font_text_view')
for variant in ('doh', 'odoh'):
    root = xml(f'app/src/full/res/layout/dialog_set_custom_{variant}.xml')
    attrs(root, padding='20dp')
    title = node(root, 'dialog_custom_url_top')
    attrs(title, padding='10dp')
    assert title in list(root), 'DoH/ODoH heading is outside ScrollView'
    attrs(node(root, 'dialog_custom_url_failure_text'), visibility='invisible', text='@string/settings_http_proxy_error_text4',
        layout_marginStart='10dp', layout_marginEnd='10dp', layout_marginTop='10dp', layout_marginBottom='10dp', padding='5dp')
    button = node(root, 'dialog_custom_url_ok_btn')
    assert button.get(APP+'layout_constraintBottom_toBottomOf') == 'parent'
    assert button.get(APP+'layout_constraintEnd_toEndOf') == 'parent'
    assert not any(k.startswith(APP+'layout_constraintTop_') or k.startswith(APP+'layout_constraintStart_') for k in button.attrib)
    for field in [n for n in root.iter() if n.tag.endswith('TextInputLayout')]:
        attrs(field, layout_marginTop='10dp')
root = xml('app/src/full/res/layout/dialog_set_dns_crypt.xml')
attrs(root, padding='20dp')
attrs(node(root, 'dialog_dns_crypt_heading'), padding='10dp')
scroll = node(root, 'dialog_dns_crypt_scroll')
assert node(root, 'dialog_dns_crypt_heading') in list(scroll.iter())
attrs(node(root, 'dialog_dns_proxy_radio_group'), orientation='horizontal', padding='10dp')
error = node(root, 'dialog_dns_crypt_error_txt')
attrs(error, padding='2dp')
assert error.get(A+'visibility') is None and error.get(A+'text') is None
dims = xml('app/src/main/res/values/dimens.xml')
for name, value in [('large_font_text_view', '14sp'), ('small_font_subheading_text', '11sp'), ('default_font_text_view', '12sp')]:
    assert next(n.text for n in dims if n.get('name') == name) == value
for controller in ('adapter/CustomDomainAdapter.kt', 'adapter/CustomIpAdapter.kt', 'ui/bottomsheet/CustomDomainRulesBtmSheet.kt', 'ui/bottomsheet/CustomIpRulesBtmSheet.kt'):
    text = subprocess.check_output(['git', '-C', str(ROOT), 'show', BASE + ':app/src/main/java/com/celzero/bravedns/' + controller], text=True)
    assert 'backgroundTintList = ColorStateList.valueOf(' in text

required = {
'rules': [
 'detail: Boolean = false, modifier: Modifier = Modifier',
 'ruleStatusBadgeFrame(context, bg.toArgb())',
 'if (detail) Modifier.size(32.dp) else Modifier.widthIn(min = 25.dp)',
 'if (detail) Modifier else Modifier.padding(5.dp)',
 'if (detail && !domain && status == IpRulesManager.IpRuleStatus.BYPASS_UNIVERSAL.id) 11f else 12f',
 'weight = if (detail) android.graphics.Typeface.BOLD else null',
 'AppCompatResources.getDrawable(context, R.drawable.rectangle_border_background)).mutate().apply { setTint(tint) }',
 'RuleStatusBadge(domain, status, modifier = Modifier.padding(start = 5.dp, end = 10.dp))',
 'padding(if (sourceLayout) 10.dp else 20.dp)',
 'if (sourceLayout) Arrangement.Top else Arrangement.spacedBy(10.dp)',
 'Modifier.fillMaxWidth().padding(10.dp).testTag("rule_editor_title")',
 'utilityTextStyle(androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead, size = 14f)',
 'Modifier.fillMaxWidth().padding(start = 5.dp).padding(5.dp).testTag("rule_editor_description")',
 'utilityTextStyle(size = 11f), color = themeColor(R.attr.primaryLightColorText)',
 'Modifier.fillMaxWidth().padding(start = 10.dp, top = 10.dp, end = 10.dp).padding(5.dp)',
 'utilityTextStyle(androidx.appcompat.R.style.TextAppearance_AppCompat_Body1, size = 12f)',
 'padding(horizontal = if (domain) 0.dp else 20.dp)',
 'horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)',
 'padding(start = 10.dp, end = 10.dp, top = 4.dp)',
 'padding(start = 10.dp, end = 10.dp, top = 12.dp)',
 'overflow = if (fileName) TextOverflow.MiddleEllipsis else TextOverflow.Clip',
 'OriginalRadioButton(compoundSource, selected, null, indicatorOnly = true)',
],
'details': ['RuleStatusBadge(domain, status, selected == status, detail = true)'],
'dns': [
 'DnsError(error, DnsErrorLayout.Doh)', 'DnsError(error, DnsErrorLayout.Crypt)',
 '"dns_name", modifier = Modifier.padding(top = 10.dp)',
 '"dns_url", KeyboardType.Uri, modifier = Modifier.padding(top = 10.dp)',
 '"dns_proxy", KeyboardType.Uri, modifier = Modifier.padding(top = 10.dp)',
 '"dns_ip", modifier = Modifier.padding(top = 10.dp)',
 'Row(Modifier.fillMaxWidth().padding(10.dp).selectableGroup(), horizontalArrangement = Arrangement.Center)',
 'if (buttons != DnsEditorButtons.CRYPT) DnsEditorTitle(title, legacyProxy = buttons == DnsEditorButtons.PROXY)',
 'if (buttons == DnsEditorButtons.CRYPT) DnsEditorTitle(title)',
 'if (buttons == DnsEditorButtons.PROXY) Arrangement.spacedBy(8.dp) else Arrangement.Top',
 'else Modifier.fillMaxWidth().padding(10.dp)',
 'DnsEditorButtons.DOH, DnsEditorButtons.ODOH -> Modifier.padding(end = 10.dp, bottom = 10.dp)',
 'DnsEditorButtons.CRYPT -> Modifier.padding(bottom = 10.dp)',
 'if (layout == DnsErrorLayout.Proxy && !visible) return',
 'if (layout == DnsErrorLayout.Doh) stringResource(R.string.settings_http_proxy_error_text4) else ""',
 'if (layout == DnsErrorLayout.Doh) Modifier.padding(10.dp) else Modifier',
 'DnsErrorLayout.Doh -> 5.dp; DnsErrorLayout.Crypt -> 2.dp',
 '.alpha(if (visible || layout == DnsErrorLayout.Crypt) 1f else 0f)',
 'else Modifier.clearAndSetSemantics {}',
 'liveRegion = LiveRegionMode.Polite',
 'OriginalRadioButton(compoundSource, selected, null, indicatorOnly = true)',
]}

def valid(s):
    if not all(all(token in s[k] for token in tokens) for k, tokens in required.items()): return False
    badge = s['rules'][s['rules'].index('@Composable internal fun RuleStatusBadge'):]
    if 'Surface(' in badge or '.background(' in badge: return False
    d = s['dns']; scroll = d.index('Column(Modifier.weight(1f, false).verticalScroll(rememberScrollState())')
    if not d.index('if (buttons == DnsEditorButtons.CRYPT) DnsEditorTitle(title)') > scroll: return False
    return True

assert valid(sources)
mutations = [
 ('rules', 'Modifier.size(32.dp)', 'Modifier.size(30.dp)'),
 ('rules', 'Modifier.widthIn(min = 25.dp)', 'Modifier.widthIn(min = 0.dp)'),
 ('rules', 'else Modifier.padding(5.dp)', 'else Modifier.padding(8.dp)'),
 ('rules', 'IpRulesManager.IpRuleStatus.BYPASS_UNIVERSAL.id) 11f', 'IpRulesManager.IpRuleStatus.BYPASS_UNIVERSAL.id) 12f'),
 ('rules', 'if (detail) android.graphics.Typeface.BOLD else null', 'if (detail) null else null'),
 ('rules', 'size = 14f)', 'size = 20f)'),
 ('rules', 'size = 11f)', 'size = 14f)'),
 ('rules', 'TextAppearance_AppCompat_Body1, size = 12f', 'TextAppearance_AppCompat_Body1, size = 14f'),
 ('rules', 'padding(if (sourceLayout) 10.dp else 20.dp)', 'padding(20.dp)'),
 ('rules', 'TextOverflow.MiddleEllipsis', 'TextOverflow.Ellipsis'),
 ('details', 'detail = true', 'detail = false'),
 ('dns', 'DnsErrorLayout.Doh)', 'DnsErrorLayout.Proxy)'),
 ('dns', 'DnsErrorLayout.Crypt -> 2.dp', 'DnsErrorLayout.Crypt -> 5.dp'),
 ('dns', 'else Modifier.clearAndSetSemantics {}', 'else Modifier'),
 ('dns', 'if (buttons == DnsEditorButtons.CRYPT) DnsEditorTitle(title)', 'if (false) DnsEditorTitle(title)'),
 ('dns', '"dns_url", KeyboardType.Uri, modifier = Modifier.padding(top = 10.dp)', '"dns_url", KeyboardType.Uri'),
]
for key, old, new in mutations:
    changed = dict(sources); changed[key] = changed[key].replace(old, new, 1)
    assert changed[key] != sources[key] and not valid(changed), (key, old)
# Independent typed native-widget factories replace all application-layout test dependencies.
factory_path = ROOT / 'app/src/test/java/com/celzero/bravedns/ui/compose/reference/RuleDialogReferenceFactories.kt'
factory_sha256 = '0a22c2637adb3268b6891b6e9b4558408b0cade12746d785a1702b333ded5741'
reference_paths = [factory_path,
    ROOT/'app/src/test/java/com/celzero/bravedns/ui/compose/rules/RuleDialogSourceStyleTest.kt',
    ROOT/'app/src/test/java/com/celzero/bravedns/ui/compose/dns/DnsEditorSourceStyleTest.kt']
references = {str(path.relative_to(ROOT)): path.read_text() for path in reference_paths}
forbidden_reference_tokens = ['R.layout', 'LayoutInflater', '.inflate(', 'getLayout(', 'getXml(',
    'XmlPullParser', 'Base64', 'AndroidView(', 'OriginalLayoutReferenceFactories.inflate']
def valid_references(items):
    factory = items[str(factory_path.relative_to(ROOT))]
    return (hashlib.sha256(factory.encode()).hexdigest() == factory_sha256
        and all(not any(token in source for token in forbidden_reference_tokens) for source in items.values())
        and factory.count('AppCompatTextView(context).apply') == 3
        and all(token in factory for token in ['cornerRadius = 16f * context.resources.displayMetrics.density',
            'setStroke(dp(context, .75f).coerceAtLeast(1), border.getColorStateList(0))',
            'minimumWidth = dp(context, 25f)', 'val inset = dp(context, 5f)', 'val inset = dp(context, 2f)',
            'setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)', 'visibility = View.INVISIBLE', 'visibility = View.VISIBLE',
            'com.google.android.material.R.attr.textAppearanceBodyMedium', 'androidx.appcompat.R.attr.colorError']))
assert valid_references(references)
reference_mutations = [
    (str(factory_path.relative_to(ROOT)), '.75f', '1f'),
    (str(factory_path.relative_to(ROOT)), 'val inset = dp(context, 2f)', 'val inset = dp(context, 5f)'),
    (str(factory_path.relative_to(ROOT)), 'visibility = View.INVISIBLE', 'visibility = View.GONE'),
    (str(reference_paths[1].relative_to(ROOT)), 'RuleDialogReferenceFactories.listBadge(context)', 'LayoutInflater.from(context).inflate(R.layout.list_item_custom_domain, null)'),
    (str(reference_paths[2].relative_to(ROOT)), 'RuleDialogReferenceFactories.dohError(context)', 'LayoutInflater.from(context).inflate(R.layout.dialog_set_custom_doh, null)'),
]
for key, old, new in reference_mutations:
    changed = dict(references); changed[key] = changed[key].replace(old, new, 1)
    assert changed[key] != references[key] and not valid_references(changed), (key, old)

# Search bytes are frozen independently by the compound-control worker.
freeze = json.loads((ROOT/'docs/xml-layout-removal/RulesContent-search-freeze.json').read_text())
start = sources['rules'].index(freeze['start']); end = sources['rules'].index('LazyColumn', start)
search_hash = hashlib.sha256(sources['rules'][start:end].encode()).hexdigest()
assert search_hash == freeze['sha256']
# Explicit mutation/business lambdas remain identical to the staged pre-compound snapshot.
callback_patterns = [r'\{ error = onApply\([^\n]+?\) \}', r'\{ onSubmit\((?:false|true)\) \}',
    r'\{ state\.editorOpen = false \}', r'\{ state\.domainType = DomainRulesManager\.DomainType\.\w+ \}',
    r'\{ relay = (?:false|true); name = resources\.getString\([^\n]+? \}',
    r'\{ (?:name|url|ip|proxy|insecure|stamp|description|forwardApps) = it \}']
for key in ('rules', 'dns'):
    staged = subprocess.check_output(['git', '-C', str(ROOT), 'show', ':' + str(paths[key].relative_to(ROOT))], text=True)
    for pattern in callback_patterns:
        assert re.findall(pattern, staged) == re.findall(pattern, sources[key]), (key, pattern)
result = {'status': 'passed_source_only', 'baseline': BASE, 'findings': ['S19', 'S20', 'S34'],
    'negative_mutations_rejected': len(mutations) + len(reference_mutations), 'baseline_resources': evidence,
    'baseline_layouts_loaded_from_git_only': True, 'application_layout_references_in_test_oracles': 0,
    'reference_factory_sha256': factory_sha256,
    'frozen_search_sha256': search_hash, 'callback_literals': 'unchanged_from_staged_snapshot',
    'tests': {'RuleDialogSourceStyleTest': 'authored_UNRUN', 'DnsEditorSourceStyleTest': 'authored_UNRUN'},
    'runtime': 'NOT_RUN; no toolchain/artifact fetch or production execution',
    'limits': ['Dialog window shell dimensions/background/insets remain unverified.',
        'Inner TextInputLayout/EditText metrics and native compound-label min-height are outside this bounded surrounding-layout fix.',
        'FlowRow large-font wrapping remains an accessibility adaptation requiring actual-host comparison.',
        'No claim of pixel, TalkBack or full layout equivalence; this source guard is not runtime clearance.'],
    'production_sha256': {str(paths[k].relative_to(ROOT)): hashlib.sha256(v.encode()).hexdigest() for k,v in sources.items()}}
(ROOT/'docs/xml-layout-removal/rule-dialog-source-style-results.json').write_text(json.dumps(result, indent=2)+'\n')
print(json.dumps(result, indent=2))
