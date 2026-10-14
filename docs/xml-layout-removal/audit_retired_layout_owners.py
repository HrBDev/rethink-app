#!/usr/bin/env python3
"""Document baseline orphan layouts and retired classes without modifying application files."""
import hashlib, importlib.util, json, re
from pathlib import Path
HERE=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('inventory',HERE/'generate_layout_inventory.py')
inv=importlib.util.module_from_spec(spec);spec.loader.exec_module(inv)
ROOT=inv.ROOT
base=inv.load_baseline()
text={p:b.decode() for p,b in base.items() if p.endswith(('.kt','.java','.xml'))}
current={str(p.relative_to(ROOT)):p.read_text() for p in (ROOT/'app/src').rglob('*') if p.is_file() and p.suffix in ('.kt','.java','.xml')}
rows=[]
classes={
'ServerWgPeersAdapter':('app/src/main/java/com/celzero/bravedns/adapter/ServerWgPeersAdapter.kt','list_item_server_wg_peers','exclusive_dormant_owner'),
'SponsorBadgeView':('app/src/main/java/com/celzero/bravedns/sponsor/ui/SponsorBadgeView.kt','view_sponsor_badge','exclusive_dormant_owner'),
'FirewallStatusSpinnerAdapter':('app/src/main/java/com/celzero/bravedns/adapter/FirewallStatusSpinnerAdapter.kt','spinner_item_firewall_status','exclusive_dormant_owner'),
'WgHopAdapter':('app/src/main/java/com/celzero/bravedns/adapter/WgHopAdapter.kt','list_item_wg_hop','shared_with_live_GenericHopAdapter'),
'RethinkLogBottomSheet':('app/src/main/java/com/celzero/bravedns/ui/bottomsheet/RethinkLogBottomSheet.kt','bottom_sheet_conn_track','shared_with_live_ConnTrackerBottomSheet'),
'DnsCryptRelaysDialog':('app/src/main/java/com/celzero/bravedns/ui/dialog/DnsCryptRelaysDialog.kt','dialog_dnscrypt_relays','baseline_live_replaced_DnsRelaysDialog'),
}
def hits(corpus,symbol):
    pat=re.compile(r'(?<!\w)'+re.escape(symbol)+r'(?!\w)')
    return [{'path':p,'line':n,'text':l.strip()} for p,t in sorted(corpus.items()) for n,l in enumerate(t.splitlines(),1) if pat.search(l)]
for symbol,(owner,layout,role) in classes.items():
    allhits=hits(text,symbol)
    rows.append({'symbol':symbol,'baseline_definition':owner,'baseline_sha256':hashlib.sha256(base[owner]).hexdigest(),'layout_name':layout,'role':role,'baseline_symbol_occurrences':allhits,'baseline_external_symbol_occurrences':[h for h in allhits if h['path']!=owner],'current_symbol_occurrences':hits(current,symbol),'current_owner_exists':owner in current})
orphans=[]
for name in ['dialog_ip_details_layout','dialog_sponsor_info','item_server']:
    path=next(p for p in base if inv.LAYOUT.search(p) and Path(p).stem==name)
    binding=''.join(s.capitalize() for s in name.split('_'))+'Binding'
    orphans.append({'layout_path':path,'baseline_sha256':hashlib.sha256(base[path]).hexdigest(),'baseline_name_occurrences':hits(text,name),'baseline_binding_occurrences':hits(text,binding),'current_name_occurrences':hits(current,name),'current_binding_occurrences':hits(current,binding),'disposition':'baseline_orphan_resource; no exact source, XML include/tools, binding, reflection literal or current layout inflation owner found; no live equivalent invented'})
out={'baseline_commit':inv.BASE,'frozen_current_commit':inv.FROZEN,'status':'source-only; no runtime execution; bytecode/arbitrary name construction unavailable','orphan_resource_records':orphans,'retired_class_records':rows,'orphan_layout_names':['dialog_ip_details_layout','dialog_sponsor_info','item_server','list_item_server_wg_peers','spinner_item_firewall_status','view_sponsor_badge'],'shared_layout_warning':'Retired WgHopAdapter/RethinkLogBottomSheet do not retire their shared layouts. DnsCryptRelaysDialog was live and has native replacement DnsRelaysDialog; do not treat it as dead.'}
(HERE/'retired-layout-owners.json').write_text(json.dumps(out,indent=2)+'\n')
assert all(not r['baseline_external_symbol_occurrences'] for r in rows if r['role']=='exclusive_dormant_owner')
assert all(not r['baseline_name_occurrences'] and not r['baseline_binding_occurrences'] for r in orphans)
print('Verified 3 baseline orphan resource names and 3 layouts owned only by self-referenced dormant classes; shared/live retirement exceptions retained.')
