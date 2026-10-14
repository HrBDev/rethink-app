#!/usr/bin/env python3
"""Group pinned baseline visual attributes; preserve positional overrides in the census."""
import collections,json,pathlib
D=pathlib.Path(__file__).resolve().parent
j=json.loads((D/'component-style-restoration-inventory.json').read_text())
exclude={'android:id','android:text','android:hint','android:contentDescription','android:visibility','android:checked','android:enabled','style'}
placements=('android:layout_margin','android:layout_to','android:layout_align','android:layout_center','app:layout_constraint','android:layout_below','android:layout_above','tools:')
style_attrs={};groups=collections.defaultdict(list)
for r in j['baseline_components']+j['baseline_action_like_text_image_candidates']:
 if r['tag'] in ['ChipGroup','MaterialButtonToggleGroup']:continue
 merged={}
 for d in reversed(r['local_style_chain']):merged.update(d.get('items',{}))
 merged.update({k[4:] if k.startswith('app:') else k:v for k,v in r['attributes'].items()})
 visual={k:v for k,v in merged.items() if k not in exclude and not k.startswith(placements) and not k.startswith('layout_constraint')}
 key=pathlib.Path(r['path']).stem+':'+(r['id'] or '(anonymous)')
 unresolved=[x['name'] for x in r['local_style_chain'] if 'resolution' in x]
 fp=json.dumps({'tag':r['tag'],'external_parent':unresolved,'visual':visual},sort_keys=True)
 groups[fp].append({'source_key':key,'path':r['path'],'line':r['line']})
 style_attrs[(r['path'],r['id'])]=visual
out=[{'variant':f'baseline-variant-{i:03}','contract':json.loads(fp),'instances':instances} for i,(fp,instances) in enumerate(sorted(groups.items()),1)]
duplicates=collections.defaultdict(list)
for r in j['baseline_components']:duplicates[(pathlib.Path(r['path']).stem,r['id'])].append(r)
conflicts=[]
for key,rs in duplicates.items():
 if len(rs)>1 and rs[0]['tag'] not in ['ChipGroup','MaterialButtonToggleGroup']:
  eq=len({json.dumps(style_attrs[(r['path'],r['id'])],sort_keys=True) for r in rs})==1
  conflicts.append({'source_key':':'.join(str(x) for x in key),'same_visual_contract':eq,'paths':[r['path'] for r in rs]})
res={'baseline':j['baseline'],'meaning':'Source attribute equivalence only; local style attrs normalized, positional constraints/margins and labels excluded. Runtime state mutations and unresolved theme/library defaults are not flattened.','groups':out,'duplicate_source_keys':conflicts}
(D/'component-style-variant-groups.json').write_text(json.dumps(res,indent=2)+'\n')
print(f'{len(out)} baseline attribute variants; {len(conflicts)} duplicate keys checked')
