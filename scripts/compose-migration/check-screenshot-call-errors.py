#!/usr/bin/env python3
"""Exact source-only regression for the reported Home/RPN Kotlin call errors.

Does not compile Kotlin. The screenshot's checkpoint is the immutable oracle;
only the reviewed named-argument/resource-owner corrections are allowed here.
"""
from pathlib import Path
import json
import subprocess

ROOT = Path(__file__).resolve().parents[2]
BASE = '02b826718295f054411b99912b4a00938ace263a'
PREFIX = 'app/src/main/java/com/celzero/bravedns/ui/compose/'
CHANGES = {
    PREFIX + 'home/HomeContent.kt': [('Layout(modifier, content = {', 'Layout(modifier = modifier, content = {')],
    PREFIX + 'rpn/RpnRemovalContent.kt': [
        ('import com.celzero.bravedns.R\n', 'import com.celzero.bravedns.R\nimport com.google.android.material.R as MaterialR\n'),
        ('themeColor(R.attr.colorSurface)', 'themeColor(MaterialR.attr.colorSurface)'),
    ],
}


def expected(path):
    text = subprocess.check_output(['git', 'show', BASE + ':' + path], cwd=ROOT, text=True)
    for old, new in CHANGES[path]:
        assert text.count(old) == 1, 'Unexpected screenshot-baseline shape: ' + old
        text = text.replace(old, new, 1)
    return text


def check(sources):
    for path in CHANGES:
        assert sources[path] == expected(path), 'Unreviewed change or reported error restored: ' + path
    assert 'android.nonTransitiveRClass=true' in (ROOT / 'gradle.properties').read_text()


def main():
    sources = {path: (ROOT / path).read_text() for path in CHANGES}
    check(sources)
    negative = 0
    for path, changes in CHANGES.items():
        for old, new in changes:
            mutated = dict(sources)
            mutated[path] = mutated[path].replace(new, old, 1)
            try:
                check(mutated)
            except AssertionError:
                negative += 1
            else:
                raise AssertionError('Reported error was not rejected: ' + old)
    print(json.dumps({'result': 'PASS_SOURCE_ONLY', 'reported_errors_addressed': 4,
                      'negative_controls': negative, 'scope': list(CHANGES),
                      'compile': 'UNRUN_TOOLCHAIN_BLOCKED'}))


if __name__ == '__main__':
    main()
