#!/usr/bin/env python3
"""Pinned read-only source audit. Writes only docs/xml-layout-removal artifacts.
No Gradle, Android process, downloads or application execution.
"""
from __future__ import annotations
import collections, hashlib, json, pathlib, re, subprocess, xml.etree.ElementTree as ET
ROOT=pathlib.Path(__file__).resolve().parents[2]
OUT=ROOT/'docs/xml-layout-removal'
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
CURRENT='803cd4c884243ab9ef9c92cb0c4959d63bc27b00'
P='app/src/main/java/com/celzero/bravedns/ui/'
NS={'http://schemas.android.com/apk/res/android':'android','http://schemas.android.com/apk/res-auto':'app','http://schemas.android.com/tools':'tools'}
def git(*a): return subprocess.check_output(['git','-C',str(ROOT),*a],text=True)
def src(rev,path): return git('show',f'{rev}:{path}')
def attrs(n): return {re.sub(r'^\{([^}]+)\}',lambda m:NS.get(m[1],m[1])+':',k):v for k,v in n.attrib.items()}
basefiles=git('ls-tree','-r','--name-only',BASE).splitlines()
curfiles=git('ls-tree','-r','--name-only',CURRENT).splitlines()
layouts=[f for f in curfiles if re.search(r'^app/src/[^/]+/res/layout[^/]*/[^/]+\.xml$',f)]
styles=collections.defaultdict(list); values=collections.defaultdict(list); xml_resources=collections.defaultdict(list)
for f in basefiles:
 if re.search(r'/res/values[^/]*/[^/]+\.xml$',f) and not re.search(r'/values-[a-z]{2}(?:-r[A-Z]{2})?/',f):
  try: r=ET.fromstring(src(BASE,f))
  except ET.ParseError: continue
  for n in r:
   if n.tag=='style': styles[n.get('name')].append({'path':f,'parent':n.get('parent'),'items':{x.get('name'):(x.text or '').strip() for x in n if x.tag=='item'}})
   elif n.get('name') and n.tag in {'color','dimen','integer','bool'}: values['@'+n.tag+'/'+n.get('name')].append({'path':f,'value':(n.text or '').strip()})
 elif re.search(r'/res/(?:drawable|color)[^/]*/[^/]+\.xml$',f):
  typ=pathlib.Path(f).parent.name.split('-')[0]
  xml_resources['@'+typ+'/'+pathlib.Path(f).stem].append({'path':f,'xml':src(BASE,f)})
def chain(name,seen=()):
 name=name.replace('@style/','')
 if not name or name in seen:return []
 ds=styles.get(name)
 if not ds:return [{'name':name,'resolution':'framework/library or theme reference; runtime resolution required'}]
 result=[]
 for d in ds:
  parent=d['parent']
  if parent is None:parent=name.rsplit('.',1)[0] if '.' in name else ''
  result.append({'name':name,**d,'parent_definitions':chain(parent or '',seen+(name,))})
 return result
# Existing component mappings are file/function navigation aids only. Refresh every location.
old=json.loads((ROOT/'docs/compose-migration/component-style-restoration-renderer-map.json').read_text())
MAP=collections.defaultdict(dict)
for x in old['instance_mappings']:
 for r in x['current_renderers']:
  MAP[pathlib.Path(x['baseline_path']).stem][r['path']]=[q['function'] for q in r['verified_function_locations']]
def add(stems,path,functions=''):
 path=P+path+'.kt' if not path.startswith('app/') else path
 for stem in stems.split():MAP[stem][path]=functions.split()
add('activity_advanced_setting','compose/settings/AdvancedSettingsContent','AdvancedSettingsContent SettingsRow')
add('activity_app_lock','compose/utilities/OnboardingScreens','AppLockScreen')
add('activity_alerts','compose/utilities/OnboardingScreens','AlertsScreen')
add('welcome_slide1 welcome_slide2 welcome_slide3 welcome_slide4','compose/utilities/OnboardingScreens','WelcomeScreen')
add('activity_bubble','compose/utilities/BubbleScreen','BubbleScreen')
add('activity_pause','compose/utilities/PauseScreen','PauseScreen')
add('activity_misc_settings fragment_proxy_configure item_lockdown_check dialog_lockdown_check activity_tunnel_settings item_network_policy','compose/networksettings/NetworkNativeUi','NetworkSettingsScreen NetworkContent')
add('bottom_sheet_app_connections domain_item_bottom_sheet','compose/appdetails/AppRuleSheetContent','AppRuleSheetContent DomainRuleRowContent')
add('bottom_sheet_home_screen bottom_sheet_block_free_dns_mode','compose/settings/ModeSheets','HomeModeContent BlockFreeModeContent')
add('bottom_sheet_rethink_list','bottomsheet/RethinkListBottomSheet','onCreateView')
MAP['dialog_info_firewall_rules']={}
add('dialog_info_firewall_rules','compose/firewallapps/FirewallFilterContent','showFirewallInfoDialog')
add('dialog_whatsnew','compose/about/AboutDiagnosticsContent','AboutRichTextContent')
add('fragment_firewall_settings','compose/settings/SettingsMenus','FirewallSettingsMenuContent')
add('fragment_configure','compose/settings/SettingsMenus','ConfigureMenuContent')
add('item_home_firewall_rules_page','compose/home/HomeContent','RulesCard')
add('item_log_activity_app item_log_activity_conn','compose/home/LogIntervalContent','AppActivityNativeRow')
add('list_item_app_domain_details list_item_app_ip_details list_item_app_country_details','compose/appdetails/AppConnectionRows','AppDomainsRowContent AppIpsRowContent AppCountriesRowContent')
add('list_item_conn_track list_item_dns_log','compose/trafficlogs/LogRows','ConnectionLogRow DnsLogRow')
add('list_item_custom_domain list_item_custom_ip','compose/rules/RulesContent','CustomRuleRow')
add('list_item_firewall_app','compose/firewallapps/FirewallAppsContent','FirewallAppRowContent')
add('list_item_rethink_blocklist_simple','compose/blocklists/BlocklistConfigurationScreen','BlocklistSelectionRow')
# FirewallStatusSpinnerAdapter is a baseline self-only dormant owner.
# SponsorBadgeView is a baseline self-only dormant owner; no fabricated renderer.
add('activity_anti_censorship','compose/settings/AntiCensorshipContent','AntiCensorshipContent')
add('activity_backup_restore','compose/networksheets/NetworkSheetContent','BackupRestoreSheetContent')
add('activity_configure_other_dns activity_smart_dns_list','compose/dnssettings/DnsFragmentHost','DnsFragmentHeader')
add('activity_other_dns_list','compose/dnssettings/DnsProtocolGrid','DnsProtocolGrid')
add('activity_custom_rules','activity/CustomRulesActivity','onCreate')
add('activity_detailed_statistics','compose/statistics/PagedStatisticsScreen','PagedStatisticsScreen')
add('activity_dns_detail activity_firewall','compose/settings/SingleSettingsFragmentHost','SingleSettingsFragmentHost')
add('activity_network_logs','activity/NetworkLogsActivity','onCreate')
add('activity_universal_firewall_settings','compose/settings/UniversalFirewallContent','UniversalFirewallContent')
add('bottom_sheet_custom_domains bottom_sheet_custom_ips','compose/rules/RuleDetailsContent','RuleDetailsContent')
add('bottom_sheet_local_blocklists','compose/blocklists/LocalBlocklistsScreen','LocalBlocklistsScreen')
add('bottom_sheet_proxies_list','compose/wireguard/WgProxySelectionContent','WgProxySelectionContent')
add('dialog_server_loading','compose/rpn/RpnComponents','RpnProgressContent')
add('dns_crypt_endpoint_list_item list_item_endpoint','compose/dns/DnsEndpointScreen','DnsEndpointRow')
add('fragment_dns_configure','compose/dnssettings/DnsSettingsScreen','DnsSettingsScreen')
add('fragment_rethink_plus','app/src/fdroid/java/com/celzero/bravedns/ui/fragment/RethinkPlusFragment.kt','onCreateView')
add('fragment_rethink_plus_dashboard','compose/rpn/RpnAccountScreen','RpnAccountScreen')
add('fragment_rethink_plus_manage_purchase','compose/purchase/ManagePurchaseContent','ManagePurchaseContent')
add('fragment_smart_dns_list','compose/dns/SmartDnsContent','SmartDnsContent')
add('item_dns_record_type','compose/about/DnsRecordTypesContent','DnsRecordTypeRow')
add('item_ping_test_history','app/src/main/java/com/celzero/bravedns/adapter/PingTestHistoryAdapter.kt','PingHistoryRow')
add('item_section_header','adapter/CountryServerAdapter','Content')
add('layout_entitlement_row','compose/purchase/EntitlementContent','EntitlementContent')
add('list_item_blocklist_app_row list_item_blocklist_domain_row','app/src/main/java/com/celzero/bravedns/adapter/BlocklistAppsAdapter.kt','GroupAt')
add('list_item_console_log','compose/trafficlogs/ConsoleLogsScreen','ConsoleLogRow')
add('list_item_exclude_country','compose/rpn/RpnExcludeContent','RpnExcludeContent')
add('list_item_play_subs list_item_shimmer_card shimmer_subscription_loading','compose/purchase/PurchasePlans','PurchasePlanCard PurchaseShimmer')
add('list_item_proxy_cc_wg','compose/wireguard/WgProxySelectionContent','WgProxyRow')
add('list_item_proxy_cc_wg','compose/networksheets/NetworkSheetContent','ProxyCountriesSheetContent')
add('list_item_purchase_history','compose/purchase/HistoryRowContent','HistoryRowContent')
add('list_item_rpn_bypass_app','compose/rpnconfig/RpnBypassContent','RpnBypassAppRow')
add('list_item_rpn_stat_app','compose/statistics/StatisticsRow','StatisticsRow')
add('list_item_wg_peers','compose/wireguard/WgRows','WgPeerRow')
# ServerWgPeersAdapter is a baseline self-only dormant owner, distinct from WgPeersAdapter.
add('list_item_statistics_summary','compose/statistics/StatisticsRow','StatisticsRow')
add('list_item_statistics_summary','compose/wireguard/WgRows','WgStatsRow')
add('list_item_vpn_server_add','compose/rpn/RpnServerTile','RpnAddServerTile')
add('list_item_wg_include_apps','compose/wireguard/WgRows','WgAppRow')
add('view_insights_rank_row','compose/statistics/SummaryStatisticsScreen','SummaryInsights')
add('view_insights_rank_row','compose/appdetails/AppDetailsInsights','AppDetailsInsights')
add('list_item_shimmer_server_row','compose/rpn/RpnSelectionScreen','RpnShimmer')
add('list_item_shimmer_server_row','compose/purchase/HistoryScreen','HistoryScreen')
add('list_item_shimmer_server_row','compose/purchase/PurchasePlans','PurchaseShimmer')
add('activity_events','compose/trafficlogs/EventsScreen','EventsScreen')
add('activity_domain_connections','compose/statistics/PagedStatisticsScreen','PagedStatisticsScreen')
add('item_ssid','compose/rpnconfig/RpnSsidContent','RpnSsidContent')
# Baseline definitions without confirmed consumers stay unassigned, never silently equivalent.
UNASSIGNED={'dialog_ip_details_layout','dialog_sponsor_info','item_server','view_sponsor_badge','spinner_item_firewall_status','list_item_server_wg_peers'}
sources={f:src(CURRENT,f) for f in curfiles if f.endswith('.kt') and '/java/' in f and not re.search('/(?:test|androidTest)',f)}
def renderer(path,functions):
 s=sources.get(path,''); entries=[]
 for fn in functions:
  m=re.search(r'\bfun\s+(?:<[^>]+>\s*)?(?:[\w.]+\.)?'+re.escape(fn)+r'\s*\(',s)
  if m:entries.append({'function':fn,'line':s[:m.start()].count('\n')+1})
 return {'path':path,'source_present':bool(s),'source_sha256':hashlib.sha256(s.encode()).hexdigest() if s else None,'verified_functions':entries,'unverified_function_names':[x for x in functions if x not in [e['function'] for e in entries]],'mapping_evidence':'reviewed renderer-family ownership; not per-node or pixel equivalence','source_style_evidence':{cat:[{'line':i,'text':line.strip()} for i,line in enumerate(s.splitlines(),1) if re.search(pat,line) and not line.strip().startswith('import ')] for cat,pat in {'root_container_geometry':r'\b(?:Column|Row|Box|Surface|Card|Layout|Scaffold|LazyColumn|HorizontalPager)\(|\.(?:padding|size|height|width|heightIn|widthIn|weight|fillMax|verticalScroll|safeDrawing|navigationBars)', 'shape_surface':r'RoundedCornerShape|CircleShape|BorderStroke|\.(?:clip|border|shadow|background)|cardElevation|shape\s*=', 'typography':r'fontSize|fontFamily|fontWeight|fontStyle|letterSpacing|lineHeight|typography|TextStyle|maxLines|minLines|overflow|Text\(', 'color_alpha':r'themeColor|colorResource|colorScheme|Color\(|Color\.|\.alpha|[Cc]olor\s*=', 'semantics_interaction':r'semantics|contentDescription|Role\.|clickable|selectable|toggleable|focus|enabled\s*=', 'motion_state':r'animate|tween|shimmer|visibility|isVisible|isChecked|rememberInfinite'}.items()}}
CATS={
 'root_container_geometry':r'(^android:layout_|^android:(?:orientation|gravity|padding|clip|fillViewport|maxWidth|minWidth|minimum|elevation)|^app:(?:layout_|constraint_|flow_|barrier|behavior_|contentInset))',
 'shape_surface':r'(?:[Cc]orner|[Rr]adius|[Ss]hape|[Ss]troke|[Ee]levation|[Bb]ackground|[Cc]ard|[Cc]lip)',
 'typography':r'(?:text|font|letterSpacing|lineSpacing|ellipsize|maxLines|minLines|singleLine|includeFontPadding|autoSize)',
 'color_alpha':r'(?:[Cc]olor|[Tt]int|[Aa]lpha|[Oo]pacity)',
 'fixed_min_max_dimensions':r'(?:layout_width|layout_height|[Mm]inWidth|[Mm]axWidth|[Mm]inHeight|[Mm]axHeight|[Ss]ize|[Tt]hickness)',
 'grouping_order':r'(?:orientation|gravity|layout_|constraint_|flow_|barrier|singleSelection|selectionRequired|chipSpacing)',
 'semantics_interaction':r'(?:contentDescription|importantForAccessibility|accessibility|focusable|clickable|checkable|checked|enabled|selectable|screenReader|inputType|imeOptions|autofill)',
 'content_visibility':r'(?:^layout$|^android:(?:src|text|hint|visibility)|srcCompat|drawable|showText)',
 'motion_state':r'(?:[Aa]nima|transition|animateLayoutChanges|stateListAnimator|indeterminate|progress|shimmer|alpha|rotation|scale)' }
# Full style refs are kept separately; runtime library defaults must be independently resolved.
records=[]; globalcounts=collections.Counter(); used_values=set();used_xml=set();used_styles=set()
for f in layouts:
 raw=src(BASE,f); root=ET.fromstring(raw); stem=pathlib.Path(f).stem; nodes=[]; cats={k:[] for k in CATS}; tagcounts=collections.Counter()
 def walk(n,route,parent):
  a=attrs(n); tag=n.tag.split('.')[-1]; ident=a.get('android:id','').split('/')[-1] or route
  tagcounts[tag]+=1;globalcounts[tag]+=1
  line=None
  if 'android:id' in a:
   marker='android:id="'+a['android:id']+'"';at=raw.find(marker);line=raw[:at].count('\n')+1 if at>=0 else None
  refs=set(v for v in a.values() if v.startswith('@'))
  localstyles=sorted(v for v in refs if v.startswith('@style/'))
  used_styles.update(x.replace('@style/','') for x in localstyles)
  used_values.update(refs&values.keys());used_xml.update(refs&xml_resources.keys())
  item={'node':ident,'route':route,'parent':parent,'tag':tag,'line':line,'attributes':a,'style_references':localstyles,'theme_references':sorted(v for v in a.values() if v.startswith('?'))}
  nodes.append(item)
  for cat,pat in CATS.items():
   aa={k:v for k,v in a.items() if not k.startswith('tools:') and re.search(pat,k)}
   if aa:cats[cat].append({'node':ident,'tag':tag,'attributes':aa})
  for i,c in enumerate(n):walk(c,route+'/'+str(i),ident)
 walk(root,'0',None)
 cardnodes=[n['node'] for n in nodes if n['tag']=='MaterialCardView']
 implicit=[]
 if cardnodes:
  implicit.append({'nodes':cardnodes,'default_style_attribute':'materialCardViewStyle','repo_theme_alternatives':['Widget.Rethink.MaterialCardView','Widget.Rethink.MaterialCardView.Frost'],'obligations':['cardUseCompatPadding','cardPreventCornerOverlap default','cardMaxElevation/elevation','contentPadding versus outer inset','shape/stroke/ripple/state colors']})
  used_styles.update(['Widget.Rethink.MaterialCardView','Widget.Rethink.MaterialCardView.Frost'])
 controls=[n for n in nodes if re.search(r'(Switch|CheckBox|RadioButton|SeekBar|Slider|ProgressIndicator|ProgressBar)$',n['tag'])]
 renderers=[renderer(p,fn) for p,fn in sorted(MAP.get(stem,{}).items())]
 obligations=[]
 if cardnodes:obligations.append('Reconcile every card own explicit geometry, inherited compat padding, elevation, stroke and fill with mapped renderer; a matched color is insufficient.')
 if controls:obligations.append('Resolve source compound-control/progress/slider widget style and theme overlay; default Compose calls do not establish equivalence.')
 if cats['typography']:obligations.append('Reconcile textAppearance/font family/weight/size/letterSpacing/line metrics/maxLines/ellipsize and dynamic styled text per node.')
 if cats['grouping_order']:obligations.append('Verify parent grouping/order, margin versus padding, wrap versus fixed/min constraints, scroll viewport, and hidden-child behavior; ids are not geometry.')
 if cats['shape_surface']:obligations.append('Trace every referenced drawable/color state list and shape overlay including selected/pressed/disabled state, not only the default color.')
 if cats['semantics_interaction']:obligations.append('Verify labels/roles/checkable selection/focus traversal and invisible/decorative semantics with actual-host accessibility tests.')
 status='renderer_family_mapped_equivalence_pending' if renderers else 'unassigned_baseline_definition_reachability_pending'
 records.append({'layout_path':f,'basename':stem,'source_set':f.split('/')[2],'qualifier':pathlib.Path(f).parent.name,'baseline_sha256':hashlib.sha256(raw.encode()).hexdigest(),'current_layout_sha256':hashlib.sha256(src(CURRENT,f).encode()).hexdigest(),'root':{'tag':root.tag,'attributes':attrs(root)},'node_count':len(nodes),'tag_counts':dict(sorted(tagcounts.items())),'renderer_mapping':renderers,'mapping_status':status,'deletion_ready':False,'equivalence_status':'not_accepted_source_or_visual','style_contracts':cats,'implicit_widget_style_obligations':implicit,'original_style_references':sorted(set(x for n in nodes for x in n['style_references'])),'theme_references':sorted(set(x for n in nodes for x in n['theme_references'])),'nodes':nodes,'required_checks':obligations,'specific_findings':[]})
# Include transitive style/value/drawable refs, retaining all resource qualifiers.
queue=list(used_styles); all_style_defs={}
while queue:
 name=queue.pop()
 if name in all_style_defs:continue
 all_style_defs[name]=chain(name)
 for d in styles.get(name,[]):
  if d['parent']:queue.append(d['parent'].replace('@style/',''))
  for value in d['items'].values():
   if value.startswith('@style/'):queue.append(value.replace('@style/',''))
   if value in values:used_values.add(value)
   if value in xml_resources:used_xml.add(value)
resqueue=list(used_xml); resource_defs={}
while resqueue:
 key=resqueue.pop()
 if key in resource_defs:continue
 resource_defs[key]=xml_resources[key]
 for d in resource_defs[key]:
  for ref in re.findall(r'@(?:color|dimen|drawable)/[A-Za-z0-9_]+',d['xml']):
   if ref in values:used_values.add(ref)
   if ref in xml_resources:resqueue.append(ref)
result={'baseline':BASE,'native_snapshot':CURRENT,'audit_kind':'all-remaining-layout styling preservation preflight; no production changes','limitations':['Source-only; no Gradle, runtime, device, screenshot or TalkBack execution.','Mapping a renderer family or identifier never accepts equivalence.','Repo-local style definitions and every qualifier retained; external Material/framework defaults require approved runtime reference measurements.','Source removal is blocked by unresolved source contracts/findings/references. Runtime verification is independently blocked and must remain disclosed; absence of a toolchain is not represented as a source-comparison pass.'],'layout_count':len(records),'mapped_layout_count':sum(bool(r['renderer_mapping']) for r in records),'unmapped_layouts':[r['layout_path'] for r in records if not r['renderer_mapping']],'node_count':sum(r['node_count'] for r in records),'tag_counts':dict(sorted(globalcounts.items())),'layouts':records,'style_definitions':all_style_defs,'value_definitions':{k:values[k] for k in sorted(used_values)},'drawable_color_xml_definitions':resource_defs}
(OUT/'styling-equivalence-ledger.json').write_text(json.dumps(result,indent=2,ensure_ascii=False)+'\n')
print(json.dumps({k:result[k] for k in ['layout_count','mapped_layout_count','unmapped_layouts','node_count','tag_counts']},indent=2))
print('UNVERIFIED FUNCTIONS',[(r['layout_path'],x['path'],x['unverified_function_names']) for r in records for x in r['renderer_mapping'] if x['unverified_function_names']])
