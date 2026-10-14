#!/usr/bin/env python3
"""Narrow source guard, not a Kotlin build, Android run, or pixel-equivalence claim.

Pins the original app's search/card contracts and checks the Compose paths that carry them.
The independent native-widget oracle lives in OriginalSearchStyleReferenceTest (unexecuted).
"""
import json
import pathlib
import re
import subprocess
import xml.etree.ElementTree as ET

ROOT = pathlib.Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
ANDROID = '{http://schemas.android.com/apk/res/android}'
APP = '{http://schemas.android.com/apk/res-auto}'
errors = []
checks = []


def require(name, value):
    checks.append({'check': name, 'passed': bool(value)})
    if not value:
        errors.append(name)


def baseline(path):
    return subprocess.check_output(['git', 'show', f'{BASE}:{path}'], cwd=ROOT, text=True)


def by_id(xml, name):
    return next(n for n in ET.fromstring(xml).iter() if n.get(ANDROID + 'id') == '@+id/' + name)


for kind, prefix in [('domain', 'cda'), ('ip', 'cip')]:
    xml = baseline(f'app/src/full/res/layout/fragment_custom_{kind}.xml')
    card = by_id(xml, f'{prefix}_search_container')
    require(f'{kind}: baseline MaterialCardView / 16dp radius / 4dp margin',
            card.tag.endswith('MaterialCardView') and card.get(APP + 'cardCornerRadius') == '16dp'
            and card.get(ANDROID + 'layout_margin') == '4dp')
    require(f'{kind}: baseline search 90 percent weight', by_id(xml, f'{prefix}_search_view').get(ANDROID + 'layout_weight') == '0.9')

xml = baseline('app/src/main/res/layout/fragment_server_selection.xml')
card = by_id(xml, 'search_card')
require('server: baseline ContentCard source style', card.get('style') == '@style/RethinkPlus.ContentCard')
row = next(iter(card))
require('server: baseline row 8dp horizontal / 6dp vertical padding', all(row.get(ANDROID + a) == v for a, v in
        [('paddingStart', '8dp'), ('paddingEnd', '8dp'), ('paddingTop', '6dp'), ('paddingBottom', '6dp')]))
require('server: baseline explicit actionSearch', by_id(xml, 'search_bar').get(ANDROID + 'imeOptions') == 'actionSearch')
require('server: baseline SearchView exact 40dp / native close icon',
        by_id(xml, 'search_bar').get(ANDROID + 'layout_height') == '40dp'
        and by_id(xml, 'search_bar').get(APP + 'closeIcon') == '@drawable/ic_cross')
styles = ET.fromstring(baseline('app/src/main/res/values/styles.xml'))
content_card = next(n for n in styles if n.get('name') == 'RethinkPlus.ContentCard')
items = {n.get('name'): n.text for n in content_card}
require('server: baseline card fill / corner / elevation', all(items.get(a) == v for a, v in
        [('cardBackgroundColor', '?attr/cardBackgroundColor'), ('cardCornerRadius', '16dp'), ('cardElevation', '0dp')]))
controller = baseline('app/src/main/java/com/celzero/bravedns/ui/fragment/ServerSelectionFragment.kt')
setup = controller.split('private fun setupSearchBar()', 1)[1].split('setOnQueryTextListener', 1)[0]
require('server: baseline 14sp / no font padding / zero editor padding; plate retained',
        all(x in setup for x in ['textSize = 14f', 'includeFontPadding = false', 'setPadding(0, 0, 0, 0)'])
        and 'background' not in setup)

compose = ROOT / 'app/src/main/java/com/celzero/bravedns/ui/compose'
fields = (compose / 'fields/OriginalTextFields.kt').read_text()
resolver = fields.split('private fun resolveSearchFieldResources', 1)[1].split('/** Resource lookup only.', 1)[0]
require('search: isolated SearchAutoComplete resource style, not editTextStyle',
        'autoCompleteTextViewStyle' in resolver and 'editTextStyle' not in resolver)
require('search: AppCompat tint loader preserves literal/null fallback',
        'AppCompatResources.getDrawable(context, id)' in resolver and 'else search.getDrawable(index)' in resolver)
require('search: server editor zero padding and root 40dp',
        'OriginalFieldStyle.ServerSearch) Rect(0, 0, 0, 0)' in resolver
        and 'OriginalFieldStyle.ServerSearch) 40f' in resolver)
require('search: typography uses autoCompleteTextViewStyle without hardcoded 16sp',
        'originalSearchTextStyle(style)' in fields
        and 'size = style.textSizeSp, defaultStyleAttribute = androidx.appcompat.R.attr.autoCompleteTextViewStyle' in fields
        and 'else if (style.search) 16f' not in fields)
require('search: standard field minimum does not override server 40dp',
        'if (style.search) resources.minHeight else maxOf' in fields)
decoration = fields.split('private fun OriginalSearchDecoration', 1)[1].split('/** The baseline DNS/proxy', 1)[0]
require('search: magnifier precedes separate weighted query plate background',
        decoration.index('search.icon != null') < decoration.index('Row(Modifier.weight(1f).fillMaxHeight().drawBehind')
        < decoration.index('resources.background?.let'))
require('search: plate follows SearchView focused/empty state',
        'drawable.state = searchPlateStateSet(focused)' in decoration
        and 'if (focused) intArrayOf(android.R.attr.state_focused) else intArrayOf()' in fields)
require('search: native edit frame margin / editor36dp / clear width; no added M3 min target',
        all(x in decoration for x in ['.padding(horizontal = 8.dp)', '.heightIn(min = 36.dp)',
                                     'LocalMinimumInteractiveComponentSize provides Dp.Unspecified', 'width(search.clearWidth.dp)']))
require('search: no production View creation or inflation', not re.search(r'AndroidView\s*\(|LayoutInflater|\.inflate\(|SearchView\s*\(', fields))
require('search: existing lint corrections preserved', all(x in fields for x in
        ['val textSize =', 'val invalidInputMessage = errorMessage ?: stringResource(',
         'if (isError) error(invalidInputMessage)', 'colors = colors, shape = resources.shape', 'LocalConfiguration.current']))
rules = (compose / 'rules/RulesContent.kt').read_text()
require('rules: source card radius / margin / weights / inherited ordinary typography', all(x in rules for x in
        ['OriginalCardSurface(Modifier.fillMaxWidth().padding(4.dp), cornerRadius = 16.dp)',
         'Modifier.weight(.9f).testTag("rules_search")', 'Modifier.weight(.1f).fillMaxHeight()',
         'if (name != null) 14.sp else TextUnit.Unspecified']))
rpn = (compose / 'rpn/RpnSelectionScreen.kt').read_text().split('if (ui.searchCard.isVisible)', 1)[1].split('if (ui.serverCountLayout.isVisible)', 1)[0]
require('server: source card style / padding / exact 40dp SearchView', all(x in rpn for x in
        ['OriginalCardSurface(', 'style = R.style.RethinkPlus_ContentCard', 'padding(horizontal = 8.dp, vertical = 6.dp)',
         'Modifier.weight(1f).height(40.dp)', 'style = OriginalFieldStyle.ServerSearch']))
require('server: clear remains inside search plate; no substituted fill or external magnifier',
        'trailingIcon =' in rpn and 'colorSurfaceVariant' not in rpn and 'R.drawable.ic_search' not in rpn)
# Keyboard defaults are verified in the primitive, which the aggregate callback guard skips.
# These source mutation controls prove this guard rejects the specific omissions/regressions;
# they do not execute Kotlin. The real dependency oracle remains an unrun independent test.
def keyboard_contract_errors(source):
    required = [
        ('non-search options must pass through untouched', 'if (!style.search) return options'),
        ('only unspecified IME becomes Search', 'imeAction = if (options.imeAction == ImeAction.Unspecified) ImeAction.Search else options.imeAction'),
        ('only unset autocorrection gets false', 'autoCorrectEnabled = options.autoCorrectEnabled ?: false'),
        ('copy must preserve explicit focus preference', 'showKeyboardOnFocus = options.showKeyboardOnFocus'),
        ('copy must preserve explicit locale hints', 'hintLocales = options.hintLocales'),
    ]
    found = [name for name, token in required if token not in source]
    dispatch = 'originalFieldKeyboardOptions(style, keyboardOptions), keyboardActions, singleLine, maxLines, minLines,'
    if source.count(dispatch) != 2:
        found.append('both editor overloads must apply defaults while retaining caller KeyboardActions')
    return found

require('search: primitive defaults honor unset values and preserve both callback paths', not keyboard_contract_errors(fields))
mutations = [
    ('unwired editor overload', 'originalFieldKeyboardOptions(style, keyboardOptions)', 'keyboardOptions'),
    ('explicit Default action overwritten', 'options.imeAction == ImeAction.Unspecified', 'options.imeAction == ImeAction.Default'),
    ('explicit autocorrection overwritten', 'options.autoCorrectEnabled ?: false', 'false'),
    ('non-search options changed', 'if (!style.search) return options', ''),
    ('explicit focus preference dropped', 'showKeyboardOnFocus = options.showKeyboardOnFocus', 'showKeyboardOnFocus = null'),
    ('explicit locale hints dropped', 'hintLocales = options.hintLocales', 'hintLocales = null'),
    ('caller KeyboardActions replaced', 'originalFieldKeyboardOptions(style, keyboardOptions), keyboardActions,',
     'originalFieldKeyboardOptions(style, keyboardOptions), KeyboardActions.Default,'),
]
for label, before, after in mutations:
    require('negative control rejects ' + label, before in fields and bool(keyboard_contract_errors(fields.replace(before, after, 1))))
proof = json.loads((ROOT / 'docs/compose-migration/search-keyboard-source-contract.json').read_text())
require('native default XML proof pins historical blob and exact Search/no-suggestions values',
        proof['baseline'] == BASE and proof['upstream_blob_sha'] == '33d25d9ec40b10fc7a1b6cf5b615252dba1b802c'
        and proof['editor_attributes'] == {'imeOptions': 'actionSearch', 'inputType': 'text|textAutoComplete|textNoSuggestions'})
reference = (ROOT / 'app/src/test/java/com/celzero/bravedns/ui/compose/fields/OriginalSearchStyleReferenceTest.kt').read_text()
require('independent native oracle checks actual editor action and flags, not helper-derived expectations', all(x in reference for x in
        ['editor.imeOptions and EditorInfo.IME_MASK_ACTION', 'editor.inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS',
         'editor.inputType and InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE', 'editor.inputType and InputType.TYPE_TEXT_FLAG_AUTO_CORRECT']))
require('unrun Compose semantics cover default Search, explicit Done, both overloads and callback counts', all(x in reference for x in
        ['SemanticsProperties.ImeAction, ImeAction.Search', 'SemanticsProperties.ImeAction, ImeAction.Done',
         'OriginalTextField(TextFieldValue("synthetic")', 'assertEquals(0, changes)', 'assertSame(KeyboardOptions.Default']))
report = {'baseline': BASE, 'verification': 'static source only; no Android/toolchain/runtime execution',
          'independent_reference_tests': 'OriginalSearchStyleReferenceTest authored and UNRUN',
          'checks': checks, 'passed': not errors, 'errors': errors}
(ROOT / 'docs/compose-migration/search-source-style-report.json').write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps(report, indent=2))
raise SystemExit(bool(errors))
