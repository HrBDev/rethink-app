#!/usr/bin/env python3
"""Source-only check for the five retired blocklist renderers.

Protocol renderer retirement waits until their separate migration removes callsites.
"""
from pathlib import Path
import re
ROOT = Path(__file__).resolve().parents[2]
NAMES = ['RethinkEndpointAdapter','LocalSimpleViewAdapter','RemoteSimpleViewAdapter','LocalAdvancedViewAdapter','RemoteAdvancedViewAdapter']

def referenced(text, name):
    return re.search(r'\b' + name + r'\s*\(', text) or re.search(r'import\s+[\w.]+\.' + name + r'\b', text)

assert referenced('val renderer = LocalSimpleViewAdapter(context, host)', 'LocalSimpleViewAdapter')
for path in (ROOT / 'app/src').rglob('*'):
    if not path.is_file(): continue
    assert path.stem not in NAMES, str(path)
    try: source = path.read_text()
    except UnicodeError: continue
    for name in NAMES:
        assert not referenced(source, name), f'{path}: {name}'
print('PASS: five retired blocklist adapter definitions/imports/constructors absent across app/src; negative control passed.')
