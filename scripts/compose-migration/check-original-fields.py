#!/usr/bin/env python3
"""Static-only evidence: pinned field families, no stock field calls, identical editor callbacks.
Does not compile Kotlin, render a screen, or initialize an Android application.
"""
import collections
import json
import pathlib
import re
import subprocess

ROOT = pathlib.Path(__file__).resolve().parents[2]
CHECKPOINT = 'e2b97de59cc854de74010c096ceffbaed6cb4a55'
BASELINE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
COMPOSE = ROOT / 'app/src/main/java/com/celzero/bravedns/ui/compose'
PRIMITIVE = COMPOSE / 'fields/OriginalTextFields.kt'


BEHAVIOR_ARGS = {'enabled', 'readOnly', 'singleLine', 'maxLines', 'minLines', 'visualTransformation', 'keyboardOptions', 'keyboardActions'}


def call_arguments(source, start):
    """Balanced Kotlin call arguments, preserving quoted strings and lambda bodies."""
    args, begin, depth, quote, escaped = [], start, [], None, False
    i = start
    while i < len(source):
        c = source[i]
        if quote:
            if escaped: escaped = False
            elif c == '\\': escaped = True
            elif c == quote: quote = None
        elif c in ('"', "'"): quote = c
        elif c in '({[': depth.append(c)
        elif c in ')}]':
            if not depth:
                args.append(source[begin:i].strip())
                return args
            depth.pop()
        elif c == ',' and not depth:
            args.append(source[begin:i].strip())
            begin = i + 1
        i += 1
    return args


def call_records(source):
    records = []
    for match in re.finditer(r'\b(?:OutlinedTextField|BasicTextField|TextField|OriginalTextField)\s*\(', source):
        args = call_arguments(source, match.end())
        behavior = []
        for arg in args[2:]:
            named = re.match(r'(\w+)\s*=\s*(.*)', arg, re.S)
            if named and named.group(1) in BEHAVIOR_ARGS:
                behavior.append((named.group(1), re.sub(r'\s+', '', named.group(2))))
        records.append((tuple(re.sub(r'\s+', '', arg) for arg in args[:2]), tuple(sorted(behavior))))
    return records


def calls(source):
    return collections.Counter(record[0] for record in call_records(source))


def behavior_calls(source):
    return collections.Counter(call_records(source))


def main():
    errors, audited = [], []
    for path in sorted(COMPOSE.rglob('*.kt')):
        if path == PRIMITIVE: continue
        rel = str(path.relative_to(ROOT))
        current = path.read_text()
        if re.search(r'(?<!\w)(?:OutlinedTextField|TextField)\s*\(', current): errors.append(f'stock field: {rel}')
        old = subprocess.run(['git', 'show', f'{CHECKPOINT}:{rel}'], cwd=ROOT, text=True, capture_output=True)
        if old.returncode or not calls(old.stdout): continue
        if calls(old.stdout) != calls(current): errors.append(f'value/onValueChange drift: {rel}')
        if behavior_calls(old.stdout) != behavior_calls(current): errors.append(f'keyboard/editor behavior argument drift: {rel}')
        audited.append({'path': rel, 'call_sites': sum(calls(current).values()), 'value_and_change_callbacks_unchanged': calls(old.stdout) == calls(current),
                        'keyboard_and_editor_arguments_unchanged': behavior_calls(old.stdout) == behavior_calls(current)})
    primitive = PRIMITIVE.read_text()
    for required in ['getColorForState', 'textInputStyle', 'editTextStyle', 'queryBackground',
                     'Widget_Material3_TextInputLayout_OutlinedBox', 'TextFieldValue', 'heightIn(min =',
                     'resources.strokeWidth.dp', 'resources.focusedStrokeWidth.dp', 'includeFontPadding',
                     'fieldCursorColor(normalCursor, errorColor, isError)', 'if (isError) error(', 'spinnerDropDownItemStyle']:
        if required not in primitive: errors.append(f'missing resource/behavior safeguard: {required}')
    if re.search(r'AndroidView\s*\(|LayoutInflater|\.inflate\(', primitive): errors.append('View inflation in field renderer')
    mapping_path = ROOT / 'docs/compose-migration/original-field-style-map.json'
    mapping = json.loads(mapping_path.read_text())
    inventory = json.loads((ROOT / 'docs/compose-migration/component-style-restoration-inventory.json').read_text())
    expected = {(n['path'], n['id']) for n in inventory['baseline_components'] if n['tag'] in ('TextInputLayout', 'TextInputEditText', 'AppCompatEditText', 'EditText', 'SearchView')}
    actual = {(n['path'], n['id']) for n in mapping['baseline_instances']}
    if expected != actual: errors.append(f'incomplete baseline map: missing={expected-actual}; extra={actual-expected}')
    for row in mapping['baseline_instances']:
        if not (ROOT / row['renderer_file']).exists(): errors.append(f'missing renderer {row}')
        elif not re.search(r'fun\s+(?:<[^>]+>\s+)?' + row['renderer_function'] + r'\s*\(', (ROOT / row['renderer_file']).read_text()): errors.append(f'missing renderer function {row["renderer_function"]}')
    report = {'baseline': BASELINE, 'native_checkpoint': CHECKPOINT, 'verification': 'static source only; no build, runtime or screenshot verification',
              'mapped_baseline_instances': len(actual), 'audited_native_files': len(audited),
              'audited_native_calls': sum(x['call_sites'] for x in audited), 'callback_checks': audited,
              'passed': not errors, 'errors': errors}
    (ROOT / 'docs/compose-migration/original-fields-source-report.json').write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps({k:v for k,v in report.items() if k != 'callback_checks'}, indent=2))
    raise SystemExit(bool(errors))


if __name__ == '__main__': main()
