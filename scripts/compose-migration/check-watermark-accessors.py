#!/usr/bin/env python3
"""Bounded source/mutation regression for the reported JVM clashes; not Kotlin compilation."""
from pathlib import Path
import argparse
import hashlib
import json
import re

ROOT = Path(__file__).resolve().parents[2]
FILE = ROOT/'app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnFlagWatermark.kt'
TEST = ROOT/'app/src/test/java/com/celzero/bravedns/ui/compose/rpn/RpnFlagWatermarkStateTest.kt'
BEFORE_SHA256 = 'b8188c257508fc4a52b5af91a69952d4cc7654604dfbf5877ae25fb40f87a883'


def rename_code(source, replacements):
    # Comments and string contents must stay byte-identical too, not be renamed as code.
    pieces = re.split(r'(/\*[\s\S]*?\*/|//[^\n]*|"(?:\\.|[^"\\])*")', source)
    for index in range(0, len(pieces), 2):
        for old, new in replacements.items():
            pieces[index] = re.sub(r'\b'+old+r'\b', new, pieces[index])
    return ''.join(pieces)


def check(source=None, test=None):
    source = FILE.read_text() if source is None else source
    test = TEST.read_text() if test is None else test
    assert 'private var flagTextValue: String? by mutableStateOf(null)' in source
    assert 'private var spreadEnabled by mutableStateOf(false)' in source
    assert not re.search(r'\bvar\s+(?:flagText|spread)\b', source)
    restored = rename_code(source, {'flagTextValue': 'flagText', 'spreadEnabled': 'spread'})
    assert hashlib.sha256(restored.encode()).hexdigest() == BEFORE_SHA256, 'only two private identifier renames are permitted'
    for token in ('fun setFlagText(text: String?)', 'fun setSpread(enabled: Boolean)',
                  'fun setFlagDrawable(drawable: Drawable?, tintAttr: Int? = null, peakAlpha: Float? = null)'):
        assert token in source, 'public API changed'
    for token in ('application = Application::class', 'controllerSettersHaveOnePublicJvmSignatureEach',
                  'renamedBackingStateRetainsNativeDrawingAndResetBehavior', 'declaredMethods',
                  'native.draw(Canvas(expected))', 'state.draw(Canvas(actual), width, height, rtl)',
                  'expected.sameAs(actual)', 'listOf(false, true)', 'listOf(true, true, false)',
                  'state.setFlagText("  ")', 'native.clear(); state.clear()'):
        assert token in test, 'missing synthetic regression: '+token
    return True


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--self-test', action='store_true')
    args = parser.parse_args()
    check()
    source, test = FILE.read_text(), TEST.read_text()
    mutations = [
        (rename_code(source, {'flagTextValue': 'flagText'}), test),
        (rename_code(source, {'spreadEnabled': 'spread'}), test),
        (source.replace('fun setFlagText(text:', 'fun updateFlagText(text:'), test),
        (source.replace('maskShader = null', 'maskShader = maskShader', 1), test),
        (source.replace('CARD_PEAK_ALPHA = 0.4f', 'CARD_PEAK_ALPHA = 0.2f'), test),
        (source, test.replace('expected.sameAs(actual)', 'true')),
        (source, test.replace('application = Application::class', 'application = RethinkDnsApplication::class')),
    ]
    rejected = 0
    if args.self_test:
        for candidate, fixture in mutations:
            try: check(candidate, fixture)
            except AssertionError: rejected += 1
            else: raise AssertionError('mutation escaped')
    result = {'status': 'PASS_SOURCE_ONLY', 'reported_jvm_signature_collisions': 2,
              'reported_compiler_diagnostics': 4, 'private_identifier_renames': 2,
              'exact_reverse_rename_sha256': BEFORE_SHA256, 'rejected_mutations': rejected,
              'authored_test_methods': len(re.findall(r'@Test\s+fun', test)),
              'kotlin_compilation': 'UNRUN', 'android_tests_and_rendering': 'UNRUN'}
    (ROOT/'docs/compose-migration/watermark-accessor-source-report.json').write_text(json.dumps(result, indent=2)+'\n')
    print(json.dumps(result))

if __name__ == '__main__': main()
