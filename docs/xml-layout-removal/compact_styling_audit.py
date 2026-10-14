#!/usr/bin/env python3
"""Lossless deterministic storage normalization for the frozen styling audit.

Run after generate_styling_audit.py and annotate_styling_findings.py. Category
contracts reference the authoritative node and attribute names instead of copying
values. Repeated resources, renderer evidence, owner chains and obligations are
content-addressed. expand() reconstructs the exact pre-normalization JSON value.
"""
from __future__ import annotations
import copy, hashlib, json, pathlib
D = pathlib.Path(__file__).resolve().parent
REF = '$styling_catalog'

def canonical(value):
    return json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode()

def digest(value):
    return hashlib.sha256(canonical(value)).hexdigest()

def write_sections(path, obj):
    # Stable sections/records remain independently diffable without 12 MB of indent.
    lines = ['{']
    for pos, (key, value) in enumerate(obj.items()):
        comma = ',' if pos + 1 < len(obj) else ''
        if key == 'layouts':
            lines.append('  "layouts": [')
            lines.extend('    ' + json.dumps(r, ensure_ascii=False, sort_keys=True, separators=(',', ':')) + (',' if i + 1 < len(value) else '') for i, r in enumerate(value))
            lines.append('  ]' + comma)
        elif key == 'entries':
            lines.append('  "entries": {')
            lines.extend('    ' + json.dumps(k) + ':' + json.dumps(v, ensure_ascii=False, sort_keys=True, separators=(',', ':')) + (',' if i + 1 < len(value) else '') for i, (k, v) in enumerate(sorted(value.items())))
            lines.append('  }' + comma)
        else:
            lines.append('  ' + json.dumps(key) + ':' + json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(',', ':')) + comma)
    lines.append('}')
    path.write_text('\n'.join(lines) + '\n')

def expand(ledger, catalog):
    def deref(value):
        if isinstance(value, dict):
            if set(value) == {REF}:
                return deref(catalog['entries'][value[REF]])
            return {k: deref(v) for k, v in value.items()}
        if isinstance(value, list):
            return [deref(v) for v in value]
        return value
    result = deref(copy.deepcopy(ledger))
    result.pop('storage_schema', None)
    for row in result['layouts']:
        nodes = row['nodes']
        by_route = {n['route']: n for n in nodes}
        for name, entries in row['style_contracts'].items():
            restored = []
            for entry in entries:
                if isinstance(entry, dict):  # Read the first compact schema as well.
                    node = by_route[entry['node_route']]
                    names = entry['attribute_names']
                else:
                    node = nodes[entry[0]]
                    names = [sorted(node['attributes'])[i] for i in entry[1]]
                restored.append(dict(node=node['node'], tag=node['tag'], attributes={k: node['attributes'][k] for k in names}))
            row['style_contracts'][name] = restored
    return result

def main():
    path = D / 'styling-equivalence-ledger.json'
    source = json.loads(path.read_text())
    if 'storage_schema' in source:
        source = expand(source, json.loads((D / 'styling-shared-catalog.json').read_text()))
    ledger = copy.deepcopy(source)
    catalog = {'schema': 'styling-shared-catalog-v1', 'baseline': source['baseline'], 'native_snapshot': source['native_snapshot'], 'entries': {}}
    def ref(value):
        key = digest(value)
        existing = catalog['entries'].setdefault(key, value)
        assert existing == value, 'SHA256 content collision'
        return {REF: key}
    for section in ('style_definitions', 'value_definitions', 'drawable_color_xml_definitions', 'renderer_source_evidence'):
        ledger[section] = {k: ref(v) for k, v in ledger[section].items()}
    for row in ledger['layouts']:
        by_node = {n['node']: n for n in row['nodes']}
        positions = {n['node']: i for i, n in enumerate(row['nodes'])}
        assert len(by_node) == len(row['nodes']), row['layout_path']
        for category, entries in row['style_contracts'].items():
            converted = []
            for entry in entries:
                node = by_node[entry['node']]
                assert entry['tag'] == node['tag']
                assert all(node['attributes'][k] == v for k, v in entry['attributes'].items())
                converted.append([positions[node['node']], [sorted(node['attributes']).index(k) for k in entry['attributes']]])
            row['style_contracts'][category] = converted
        for key in ('baseline_owners_and_root_ledger_candidates', 'required_checks', 'implicit_widget_style_obligations', 'root_ledger_warning'):
            if key in row:
                row[key] = ref(row[key])
    ledger['storage_schema'] = {'version': 2, 'shared_catalog': 'styling-shared-catalog.json', 'category_reference': 'Each style_contracts category entry is [node_index, attribute_indices]. node_index indexes this layout nodes; attribute_indices indexes sorted(node.attributes) keys, preserving exact original values.', 'catalog_reference': REF, 'expanded_sha256': digest(source), 'expanded_byte_count': len(canonical(source))}
    assert expand(ledger, catalog) == source, 'Lossless expansion failed'
    write_sections(path, ledger)
    write_sections(D / 'styling-shared-catalog.json', catalog)
    # Reload from disk so serialized references/encoding are checked too.
    assert expand(json.loads(path.read_text()), json.loads((D / 'styling-shared-catalog.json').read_text())) == source
    report = {'result': 'PASS', 'lossless_round_trip': True, 'layouts': len(ledger['layouts']), 'nodes': sum(len(r['nodes']) for r in ledger['layouts']), 'category_entries': sum(len(v) for r in ledger['layouts'] for v in r['style_contracts'].values()), 'shared_entries': len(catalog['entries']), 'expanded_sha256': digest(source), 'ledger_bytes': path.stat().st_size, 'shared_catalog_bytes': (D / 'styling-shared-catalog.json').stat().st_size, 'runtime': 'NOT_RUN'}
    (D / 'styling-storage-check.json').write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps(report, indent=2))

if __name__ == '__main__':
    main()
