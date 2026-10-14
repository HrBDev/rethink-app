#!/usr/bin/env python3
"""Assign baseline variants to offline fake-content/style-resolution fixtures. Does not run Android."""
import collections,json,pathlib,re
ROOT=pathlib.Path(__file__).resolve().parents[2];D=ROOT/'docs/compose-migration'
variants=json.loads((D/'component-style-variant-groups.json').read_text())
field=json.loads((D/'original-field-style-map.json').read_text())
field_map={(x['path'],x['id']):x for x in field['baseline_instances']}
catalog=(ROOT/'app/src/main/java/com/celzero/bravedns/ui/compose/controls/OriginalControlCatalog.kt').read_text()
keys=set(re.findall(r'"([^"\n]+)"\s+to\s+OriginalControlSpec\(',catalog))
rows=[]
for group in variants['groups']:
 tag=group['contract']['tag'];instances=group['instances'];source_keys=sorted({x['source_key'] for x in instances})
 if tag in ['TextInputLayout','TextInputEditText','AppCompatEditText','EditText','SearchView']:
  family='OriginalTextField';records=[field_map.get((x['path'],x['source_key'].split(':',1)[1])) for x in instances]
  assignments=[{'source_key':x['source_key'],'field_family':r['family'],'renderer_file':r['renderer_file'],'renderer_function':r['renderer_function']} for x,r in zip(instances,records) if r]
  test_class='OriginalFieldStyleContractTest';unassigned=[x['source_key'] for x,r in zip(instances,records) if not r]
 elif tag in ['Chip','MaterialButton','AppCompatTextView','MaterialTextView','TextView']:
  family='OriginalControl source-spec';assignments=[{'source_key':key,'catalog_entry_present':key in keys} for key in source_keys]
  test_class='OriginalControlStyleContractTest';unassigned=[key for key in source_keys if key not in keys]
 else:
  family='Icon/FAB source-instance fixture';assignments=[{'source_key':key,'renderer':'per-instance icon audit'} for key in source_keys]
  test_class='OriginalIconStyleContractTest';unassigned=[]
 rows.append({'variant':group['variant'],'baseline_tag':tag,'source_instances':instances,'expected_source_contract':group['contract'],
  'fixture_family':family,'test_class':test_class,'assignments':assignments,'missing_mapping_or_catalog_entries':unassigned,
  'states':['enabled-unselected','enabled-selected-or-checked','disabled','pressed','focused'],
  'layout_cases':['fontScale1.0/narrow','fontScale2.0/narrow/long-text','fontScale1.0/w600dp','RTL/long-text'],
  'test_status':'planned_not_executed'})
result={'baseline':variants['baseline'],'status':'Fixture assignment plan only. No Android test methods created or executed by this script.',
 'theme_matrix':['AppTheme','AppThemeWhite','AppThemeTrueBlack','AppThemeWhitePlus','AppThemeTrueBlackPlus','AppThemeTrueBlackFrost'],
 'safety':'Pure fake-content hosts only; no real Application startup, managers, services, network, accounts or permissions.',
 'required_assertions':['Resolve every catalog key, not just one sample per family.','Assert expected source shape/fill/stroke/text appearance and visual size separately from touch target.','Cover every original selector state and per-layout override; compare before/after resources using the same themed context.','Capture representative213variant screenshots once approved toolchain is available; preserve immutable baseline references.','Assert onClick/change/IME callback counts using fakes; no real services.'],
 'variant_count':len(rows),'catalog_key_count':len(keys),'fixtures':rows,
 'catalog_only_keys':sorted(k for k in keys if k.startswith('@')),
 'mapping_gaps':[{'variant':r['variant'],'keys':r['missing_mapping_or_catalog_entries']} for r in rows if r['missing_mapping_or_catalog_entries']]}
(D/'component-style-test-fixture-plan.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps({k:result[k] for k in ['variant_count','catalog_key_count','mapping_gaps']},indent=2))
