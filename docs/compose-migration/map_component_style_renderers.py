#!/usr/bin/env python3
"""Add manually reviewed renderer-family ownership and high-priority native call mappings.
This is an audit aid, not an automatic UI migration or a runtime parity claim.
"""
import collections,json,pathlib,re
ROOT=pathlib.Path(__file__).resolve().parents[2]
D=ROOT/'docs/compose-migration'
j=json.loads((D/'component-style-restoration-inventory.json').read_text())
P='app/src/main/java/com/celzero/bravedns/ui/compose/'
# basename -> concrete native rendering entry points. Per-ID overrides follow below.
MAP={}
def add(layouts,file,functions):
 for l in layouts.split(): MAP[l]=[{'path':P+file+'.kt','functions':functions.split()}]
add('fragment_home_screen view_home_logs_activity view_home_logs_apps','home/HomeContent','HomeContent ChipRow')
add('activity_app_details','appdetails/AppDetailsContent','AppDetailsContent Chip ModeButton')
add('activity_app_list','firewallapps/FirewallAppsContent','FirewallAppsContent')
add('activity_block_free_dns list_item_block_free_dns','dns/BlockFreeDnsContent','BlockFreeDnsContent')
add('activity_console_log','trafficlogs/ConsoleLogsScreen','ConsoleLogsScreen')
add('activity_tcp_proxy','utilities/TcpScreen','TcpScreen')
add('activity_welcome','utilities/OnboardingScreens','WelcomeScreen')
add('bottom_sheet_firewall_sort_filter','firewallapps/FirewallFilterContent','FirewallFilterContent')
add('bottom_sheet_log_activity_interval','home/LogIntervalContent','LogIntervalContent')
add('bottom_sheet_orbot','networksheets/NetworkSheetContent','OrbotSheetContent')
add('bottom_sheet_rethink_plus_filter','blocklists/BlocklistFilterScreen','BlocklistFilterScreen')
add('dialog_add_custom_domain dialog_add_custom_ip','rules/RulesContent','RuleEditor')
add('dialog_custom_lan_ip','rules/CustomLanIpContent','CustomLanIpContent LanPair')
add('dialog_import_confirm','rules/RulesContent','RulesPage')
add('dialog_info_rules_layout','networksheets/OrbotInfoContent','OrbotInfoContent')
add('dialog_set_custom_doh dialog_set_custom_odoh','dns/DnsEndpointEditors','DnsServerEditor DnsField DnsEditorDialog')
add('dialog_set_dns_crypt','dns/DnsEndpointEditors','DnsCryptEditor DnsField DnsEditorDialog')
add('dialog_set_dns_proxy','dns/DnsEndpointEditors','DnsProxyEditor DnsField DnsEditorDialog')
add('fragment_connection_tracker fragment_dns_logs activity_events','trafficlogs/LogScreen','LogFilterChips LogSearchBar')
add('fragment_custom_domain fragment_custom_ip','rules/RulesContent','RulesPage')
add('fragment_dns_crypt_list fragment_dns_proxy_list fragment_doh_list fragment_dot_list fragment_odoh_list dns_proxy_list_item rethink_endpoint_list_item','dns/DnsEndpointScreen','DnsEndpointScreen')
add('fragment_rethink_list','blocklists/RethinkListScreen','RethinkListScreen RethinkActionChip')
add('fragment_sponsor fragment_sponsor_success','utilities/SponsorScreens','SponsorScreen SponsorSuccessScreen')
add('item_allowed_app item_blocked_app','utilities/BubbleScreen','BubbleScreen')
add('list_item_custom_all_domain list_item_custom_all_ip','rules/RulesContent','RuleAppHeader')
add('activity_app_wise_domain_logs activity_app_wise_ip_logs','appdetails/AppLogsContent','AppLogsContent')
add('activity_checkout_proxy','purchase/CheckoutContent','CheckoutContent')
add('activity_customer_support','utilities/SupportScreen','SupportScreen')
add('activity_domain_connections','appdetails/AppConnectionRows','AppDomainConnectionsContent')
add('activity_ping_test','utilities/PingScreen','PingScreen')
add('activity_purchase_history activity_server_order_history','purchase/HistoryScreen','HistoryScreen')
add('activity_rpn_bypass_apps','rpnconfig/RpnBypassContent','RpnBypassContent')
add('activity_rpn_config_detail','rpnconfig/RpnConfigContent','RpnConfigContent')
add('activity_rpn_win_proxy_detail','rpnconfig/RpnWinContent','RpnWinContent')
add('activity_wg_config_editor','wireguard/WgEditors','WgEditorContent')
add('activity_wg_detail','wireguard/WgDetailContent','WgDetailContent')
add('activity_wireguard_main','wireguard/WgMainContent','WgMainContent WgToggle')
add('bottom_sheet_bug_report_files item_bug_report_file','about/BugReportFilesContent','BugReportFilesContent BugReportFileRow')
add('bottom_sheet_dns_record_types','about/DnsRecordTypesContent','DnsRecordTypesContent')
add('bottom_sheet_summary_stats_filter','statistics/SummaryFilterSheetContent','SummaryFilterSheetContent')
add('bottomsheet_auto_exclude_countries','rpn/RpnExcludeContent','RpnExcludeContent')
add('bottomsheet_device_auth_error bottomsheet_device_not_registered','purchase/PurchaseSheets','DeviceErrorContent')
add('bottomsheet_purchase_conflict','purchase/PurchaseSheets','ConflictContent')
add('bottomsheet_purchase_processing','purchase/PurchaseSheets','ProcessingContent')
add('bottomsheet_entitlement_detail','purchase/EntitlementContent','EntitlementContent')
add('bottomsheet_rpn_stats','statistics/RpnStatisticsContent','RpnStatisticsContent')
add('bottomsheet_server_removal_notification item_removed_server','rpn/RpnRemovalContent','RpnRemovalContent')
add('bottomsheet_server_settings','rpn/RpnSettingsContent','RpnSettingsContent')
add('dialog_dnscrypt_relays','dns/DnsEndpointScreen','DnsRelaysDialog')
add('dialog_input_ips','utilities/ReachabilityScreen','ReachabilityScreen ReachabilityMode ReachabilityAction ProbeField')
add('dialog_rpn_ssid','rpnconfig/RpnSsidContent','RpnSsidContent')
add('dialog_set_proxy','networksettings/NetworkNativeUi','NetworkContent NetworkEditField')
add('dialog_wg_add_peer','wireguard/WgEditors','WgPeerContent')
add('dialog_wg_apps','wireguard/WgIncludeAppsContent','WgIncludeAppsContent')
add('dialog_wg_hop','wireguard/WgDialogContent','WgHopContent')
add('dialog_wg_ssid item_ssid','wireguard/WgSsidContent','WgSsidContent')
add('fragment_rethink_blocklist list_item_rethink_blocklist_adv','blocklists/BlocklistConfigurationScreen','BlocklistConfigurationScreen BlocklistModeButton BlocklistRow')
add('fragment_rethink_plus_premium','purchase/SubscriptionContent','SubscriptionContent PurchaseButton')
add('fragment_server_selection item_server_group','rpn/RpnSelectionScreen','RpnSelectionScreen RpnErrorContent RpnIconButton')
add('fragment_summary_statistics','statistics/SummaryStatisticsScreen','SummaryStatisticsScreen StatsModeButton')
add('fragment_wg_nw_stats','wireguard/WgStatsContent','WgStatsContent')
add('item_event','trafficlogs/EventsScreen','EventLogRow')
add('list_item_wg_general_interface list_item_wg_one_interface list_item_wg_hop','wireguard/WgRows','WgChips WgConfigRow WgOneConfigRow WgHopRow')
add('activity_tunnel_settings','networksettings/NetworkNativeUi','NetworkContent')
add('bottom_sheet_conn_track bottom_sheet_dns_log','trafficlogs/LogDetailsContent','ConnectionDetailsContent DnsDetailsContent')
add('bottomsheet_rinr_warning','networksheets/NetworkSheetContent','RethinkWarningSheetContent WarningAction')
add('fragment_about','about/AboutContent','AboutContent AboutRow')
add('list_item_server_order','purchase/HistoryRowContent','HistoryRowContent HistoryChip')
add('list_item_vpn_server','rpn/RpnServerTile','RpnServerTile RpnTileChip')
MAP['activity_domain_connections']=[{'path':'app/src/main/java/com/celzero/bravedns/ui/activity/DomainConnectionsActivity.kt','functions':['onCreate']} ]
MAP['view_tour_tooltip']=[{'path':'app/src/main/java/com/celzero/bravedns/ui/tour/TourOverlayController.kt','functions':['TourOverlay']} ]
MAP['item_server_group']=MAP['list_item_country_card']=[{'path':'app/src/main/java/com/celzero/bravedns/ui/adapter/CountryServerAdapter.kt','functions':['Country']} ]
MAP['fragment_dns_crypt_list'].append({'path':'app/src/main/java/com/celzero/bravedns/ui/fragment/DnsCryptListFragment.kt','functions':['onViewCreated']})
# Dynamic inflater template fans out across these specific families; it has no own ID.
MAP['item_chip_filter']=[{'path':P+p+'.kt','functions':f.split()} for p,f in [
 ('trafficlogs/LogScreen','LogFilterChips'),('firewallapps/FirewallAppsContent','FirewallAppsContent'),('firewallapps/FirewallFilterContent','FirewallFilterContent'),('blocklists/BlocklistFilterScreen','BlocklistFilterScreen'),('blocklists/BlocklistConfigurationScreen','BlocklistConfigurationScreen'),('wireguard/WgIncludeAppsContent','WgIncludeAppsContent'),('rpnconfig/RpnBypassContent','RpnBypassContent'),('appdetails/AppConnectionRows','AppConnectionRow')]]
UNMATCHED={'view_tour_tooltip':'No exact renderer reference identified in pinned current source; review Tour implementation separately, do not silently replace with generic buttons.', 'list_item_country_card':'No matching favourite renderer identified; current RpnSelectionScreen grid differs. Must establish active/unused status from baseline adapter/controller.', 'dialog_sponsor_info':'No exact renderer reference identified; establish whether this legacy sponsor-info dialog remains reachable.'}
# Verify recorded symbol names against pinned source; unknown names are explicitly downgraded.
import subprocess
source_cache={}
def source(path):
 if path not in source_cache:source_cache[path]=subprocess.check_output(['git','-C',str(ROOT),'show',j['native_snapshot']+':'+path],text=True)
 return source_cache[path]
instances=[]
for r in j['baseline_components']+j['baseline_action_like_text_image_candidates']:
 stem=pathlib.Path(r['path']).stem; maps=MAP.get(stem,[]); notes=[]
 rendering=[]
 for m in maps:
  s=source(m['path']); funcs=[]
  for fn in m['functions']:
   match=re.search(r'\bfun\s+(?:<[^>]+>\s*)?(?:[\w.]+\.)?'+re.escape(fn)+r'\s*\(',s)
   if match: funcs.append({'function':fn,'line':s[:match.start()].count('\n')+1})
  hits=[h for h in r['current_identifier_references'] if h['path']==m['path']]
  rendering.append({'path':m['path'],'verified_function_locations':funcs,'exact_identifier_locations':hits})
 if r['tag']=='TextInputLayout':notes.append('Pairs with child editor; preserve parent decoration separately from input text style.')
 if r['tag'] in ['ChipGroup','MaterialButtonToggleGroup']:notes.append('Selection/spacing container; do not create an extra visible component.')
 if stem=='fragment_rethink_list' and r['tag']=='Chip':notes.append('ThinnerChip.Action chipCornerRadius12dp plus explicit ShapeAppearance.LargeComponent1dp; resolve Material1.14 attribute precedence, do not assume shape overlay alone wins.')
 if stem=='item_chip_filter':notes.append('Dynamic instance template; concrete per-screen labels/icons/checkable/selection overrides are set by15baseline inflater sites.')
 if stem=='item_removed_server' and r['id']=='removal_badge':notes.append('Source Chip collapsed to inline Text in RpnRemovalContent; requires restoring badge geometry, not just replacing existing Chip calls.')
 if stem=='item_server_group':notes.append('Source chip_link_speed is flattened into Text with8dp background in CountryServerAdapter.Country; restore the Chip family while preserving speed-dependent palette.')
 if stem=='activity_checkout_proxy' and r['id'] is None:notes.append('Anonymous live XML MaterialButton; inspect surrounding source to establish whether it is a duplicate/static purchase action.')
 status='mapped_renderer_family' if maps else 'unmatched_requires_review'
 if maps and not any(x['verified_function_locations'] for x in rendering):status='mapped_file_symbol_requires_review'
 instances.append({'source_key':stem+':'+(r['id'] or '(anonymous)'), 'baseline_path':r['path'],'baseline_line':r['line'],'original_widget':r['tag'],'original_style':r['style'],'status':status,'current_renderers':rendering,'notes':notes+([UNMATCHED[stem]] if stem in UNMATCHED and not maps else []),'owner':'fields' if r['tag'] in ['AppCompatEditText','EditText','TextInputEditText','TextInputLayout','SearchView'] else 'buttons_chips','inventory_attributes_pointer':{'path':r['path'],'id':r['id'],'tag':r['tag']}})

# Explicit direct-call maps used to choose styles at dynamic call sites. One call can have
# several source keys: select using the existing state/variant, never choose the first key.
priority=[]
def pc(file,line,keys,note=''):
 calls=[c for c in j['native_component_calls']+j['native_wrapper_calls'] if c['path']==P+file+'.kt' and c['line']==line]
 priority.append({'native_path':P+file+'.kt','native_line':line,'snapshot':j['native_snapshot'],'native_calls':calls,'source_keys':keys.split(),'selection_note':note})
pc('firewallapps/FirewallAppsContent',60,'item_chip_filter:(anonymous)','All firewall filters use inflated ThinnerChip.Large; selected state and icon overrides remain per control.')
for line in [42,47,52]:pc('firewallapps/FirewallFilterContent',line,'item_chip_filter:(anonymous)','Parent/category/sort groups all use the same baseline inflated template.')
pc('blocklists/BlocklistFilterScreen',35,'item_chip_filter:(anonymous)')
pc('blocklists/BlocklistConfigurationScreen',80,'item_chip_filter:(anonymous)')
pc('trafficlogs/LogScreen',69,'item_chip_filter:(anonymous)','Preserve optional15dp source icon and source checkedIcon visibility per caller; baseline default_font_text_view overrides14sp to12sp.')
pc('rpnconfig/RpnBypassContent',91,'item_chip_filter:(anonymous)')
pc('wireguard/WgIncludeAppsContent',47,'item_chip_filter:(anonymous)')
pc('statistics/SummaryFilterSheetContent',40,'bottom_sheet_summary_stats_filter:fss_filter_apps_chip bottom_sheet_summary_stats_filter:fss_filter_countries_chip bottom_sheet_summary_stats_filter:fss_filter_providers_chip bottom_sheet_summary_stats_filter:fss_filter_blocklist_chip bottom_sheet_summary_stats_filter:fss_filter_domains_chip bottom_sheet_summary_stats_filter:fss_filter_ips_chip','Select key by filter enum; all suppress checked icon and use12sp.')
pc('rules/RulesContent',158,'dialog_add_custom_domain:dacd_domain_chip')
pc('rules/RulesContent',159,'dialog_add_custom_domain:dacd_wildcard_chip')
pc('home/HomeContent',301,'view_home_logs_activity:fhs_logs_allowed_chip view_home_logs_activity:fhs_logs_blocked_chip view_home_logs_apps:fhs_top_apps_allowed_chip view_home_logs_apps:fhs_top_apps_blocked_chip','Keep12dp Action shape,32dp layout/24dp visual min,10sp and chip_toggle text/stroke selectors. Coordinate Home file owner.')
pc('home/HomeContent',241,'fragment_home_screen:fhs_logs_enable_chip','Source was Action Chip, not TextButton. Same style in both layout qualifiers.')
pc('home/LogIntervalContent',102,'bottom_sheet_log_activity_interval:bs_lai_chip_10m bottom_sheet_log_activity_interval:bs_lai_chip_1h bottom_sheet_log_activity_interval:bs_lai_chip_24h','MaterialButton ActivityTimeSegment family, not FilterChip; select by range.')
pc('appdetails/AppLogsContent',70,'activity_app_wise_domain_logs:tb_recent_toggle_btn activity_app_wise_domain_logs:tb_daily_toggle_btn activity_app_wise_domain_logs:tb_weekly_toggle_btn activity_app_wise_ip_logs:tb_recent_toggle_btn activity_app_wise_ip_logs:tb_daily_toggle_btn activity_app_wise_ip_logs:tb_weekly_toggle_btn','Outlined toggleButtonNoStyle16dp, shared time selection, not FilterChip.')
pc('statistics/SummaryStatisticsScreen',63,'fragment_summary_statistics:tb_recent_toggle_btn fragment_summary_statistics:tb_daily_toggle_btn fragment_summary_statistics:tb_weekly_toggle_btn','Outlined toggleButtonNoStyle16dp/12sp, not FilterChip.')
pc('statistics/SummaryStatisticsScreen',77,'fragment_summary_statistics:fss_close_conns_chip fragment_summary_statistics:fss_ia_close_conns_chip','Negative ThinnerChip.Action variant. Select list/insights mode.')
pc('statistics/SummaryStatisticsScreen',78,'fragment_summary_statistics:fss_active_apps_chip fragment_summary_statistics:fss_app_info_chip fragment_summary_statistics:fss_app_info_chip_second fragment_summary_statistics:fss_countries_logs_chip fragment_summary_statistics:fss_asn_chip fragment_summary_statistics:fss_asn_chip_second fragment_summary_statistics:fss_blocklist_chip fragment_summary_statistics:fss_dns_logs_chip fragment_summary_statistics:fss_dns_logs_chip_second fragment_summary_statistics:fss_network_logs_chip fragment_summary_statistics:fss_network_logs_chip_second fragment_summary_statistics:fss_ia_active_conns_chip fragment_summary_statistics:fss_ia_allowed_apps_chip fragment_summary_statistics:fss_ia_blocked_apps_chip fragment_summary_statistics:fss_ia_countries_chip fragment_summary_statistics:fss_ia_asn_allowed_chip fragment_summary_statistics:fss_ia_asn_blocked_chip fragment_summary_statistics:fss_ia_blocklists_chip fragment_summary_statistics:fss_ia_domains_allowed_chip fragment_summary_statistics:fss_ia_domains_blocked_chip fragment_summary_statistics:fss_ia_ips_allowed_chip fragment_summary_statistics:fss_ia_ips_blocked_chip','Positive Action Chip per section/mode, not plain text link.')
pc('purchase/SubscriptionContent',112,'fragment_rethink_plus_premium:btn_subscription','TextButton22dp44dp14sp bold, not Chip.')
pc('purchase/SubscriptionContent',113,'fragment_rethink_plus_premium:btn_one_time','TextButton22dp44dp14sp bold, not Chip.')
pc('purchase/SubscriptionContent',65,'fragment_rethink_plus_premium:subscribe_button fragment_rethink_plus_premium:retry_button','PurchaseButton generic wrapper selects source key by state.')
pc('purchase/SubscriptionContent',123,'fragment_rethink_plus_premium:btn_contact_support_error','Source outlined28dp1dp.')
pc('purchase/PurchaseSheets',81,'bottomsheet_device_auth_error:btn_email_support bottomsheet_device_not_registered:btn_email_support','Select by unregistered boolean: accentBad versus accentWarning;14dp56dp15sp bold.')
pc('purchase/PurchaseSheets',82,'bottomsheet_device_auth_error:btn_dismiss bottomsheet_device_not_registered:btn_dismiss')
pc('purchase/PurchaseSheets',93,'bottomsheet_purchase_conflict:btn_refund')
pc('purchase/PurchaseSheets',95,'bottomsheet_purchase_conflict:btn_contact_support')
pc('purchase/PurchaseSheets',96,'bottomsheet_purchase_conflict:btn_dismiss')
pc('purchase/PurchaseSheets',105,'bottomsheet_purchase_processing:action_button','Source elevated28dp56dp16sp.')
pc('purchase/PurchaseSheets',106,'bottomsheet_purchase_processing:secondary_button','Source outlined28dp50dp14sp, not TextButton.')
pc('purchase/EntitlementContent',43,'bottomsheet_entitlement_detail:btn_restore','Source framed TextView with rectangle_border_background16dp/.75dp border, not filled Button.')
pc('purchase/EntitlementContent',48,'bottomsheet_entitlement_detail:btn_dismiss','Source filled Button12dp52dp, not TextButton.')
pc('purchase/HistoryScreen',69,'activity_server_order_history:chip_entitlement')
pc('purchase/HistoryScreen',70,'activity_server_order_history:chip_payment_history')
pc('purchase/HistoryScreen',91,'activity_purchase_history:btn_retry activity_server_order_history:btn_retry','Select by server flag: purchase outlined vs server App.AlertDialog.PositiveButton text.')
pc('rpn/RpnSettingsContent',40,'bottomsheet_server_settings:split_dns_enable_btn')
pc('rpn/RpnSettingsContent',56,'bottomsheet_server_settings:btn_config_auto bottomsheet_server_settings:btn_config_manual','ActivityTimeSegment overridden8dp36dp12sp; preserve selection colors.')
pc('rpn/RpnSettingsContent',77,'bottomsheet_server_settings:btn_reset_rpn','Source outlined24dp1dp48dp14sp bold, not filled.')
pc('rpn/RpnSelectionScreen',214,'fragment_server_selection:error_retry_btn')
pc('rpn/RpnSelectionScreen',215,'fragment_server_selection:error_reset_btn')
pc('rpn/RpnSelectionScreen',216,'fragment_server_selection:error_report_btn')
pc('rpnconfig/RpnSsidContent',87,'dialog_rpn_ssid:add_ssid_btn','Source filled12dp48dp14sp bold, not TextButton.')
pc('rpnconfig/RpnSsidContent',103,'dialog_rpn_ssid:cancel_btn')
pc('rpnconfig/RpnSsidContent',104,'dialog_rpn_ssid:save_btn')
pc('wireguard/WgMainContent',19,'activity_wireguard_main:one_wg_toggle_btn activity_wireguard_main:wg_general_toggle_btn fragment_wg_nw_stats:tb_recent_toggle_btn fragment_wg_nw_stats:tb_daily_toggle_btn fragment_wg_nw_stats:tb_weekly_toggle_btn','WgToggle common primitive must receive variant. First pair accent stroke toggleButtonStyle; stats primaryText stroke toggleButtonNoStyle.')
pc('utilities/ReachabilityScreen',60,'dialog_input_ips:auto_toggle_btn dialog_input_ips:manual_toggle_btn','Outlined16dp toggleNoStyle and16sp source text, not borderless filled.')
pc('utilities/ReachabilityScreen',67,'dialog_input_ips:save_button dialog_input_ips:test_button','Framed TextView16dp/.75dp,16sp and10dp padding; current explicit outline already close.')
pc('rules/CustomLanIpContent',44,'dialog_custom_lan_ip:auto_toggle_btn dialog_custom_lan_ip:manual_toggle_btn','Outlined toggleNoStyle16dp, not filled.')
pc('blocklists/BlocklistConfigurationScreen',138,'fragment_rethink_blocklist:lb_simple_toggle_btn fragment_rethink_blocklist:lb_adv_toggle_btn','Outlined toggleStyle16dp, preserve dynamic background tint and stroke.')
pc('blocklists/RethinkListScreen',58,'fragment_rethink_list:lb_version','Passive source Chip and explicit shapeAppearance + inherited corner conflict.')
pc('blocklists/RethinkListScreen',97,'fragment_rethink_list:bslb_update_available_btn fragment_rethink_list:bslb_check_update_btn fragment_rethink_list:bslb_redownload_btn','Select by key/state; positive fill/text and explicit shapeAppearance.')
pc('blocklists/BlocklistConfigurationScreen',160,'list_item_rethink_blocklist_adv:crp_desc_entries_tv','Action Chip; preserve per-instance chipIcon/checkable/inset overrides.')
pc('utilities/ReachabilityScreen',39,'dialog_input_ips:reset_chip')
pc('rpnconfig/RpnConfigContent',57,'activity_rpn_config_detail:refresh_chip','8dp28dp11sp, background attr, zero stroke.')
def external_pc(path,line,keys,note=''):
 calls=[c for c in j['native_component_calls'] if c['path']==path and c['line']==line]
 priority.append({'native_path':path,'native_line':line,'snapshot':j['native_snapshot'],'native_calls':calls,'source_keys':keys.split(),'selection_note':note})
external_pc('app/src/main/java/com/celzero/bravedns/ui/activity/DomainConnectionsActivity.kt',159,'item_chip_filter:(anonymous)','Dynamic source Chip template; scoped blocklist tag selection.')
external_pc('app/src/main/java/com/celzero/bravedns/ui/fragment/DnsCryptListFragment.kt',73,'fragment_dns_crypt_list:add_relay_btn','Legacy rectangle_border_background TextView action.')
external_pc('app/src/main/java/com/celzero/bravedns/ui/tour/TourOverlayController.kt',227,'view_tour_tooltip:tour_close_btn')
external_pc('app/src/main/java/com/celzero/bravedns/ui/tour/TourOverlayController.kt',233,'view_tour_tooltip:tour_skip_btn')
external_pc('app/src/main/java/com/celzero/bravedns/ui/tour/TourOverlayController.kt',237,'view_tour_tooltip:tour_next_btn')
external_pc('app/src/main/java/com/celzero/bravedns/ui/adapter/CountryServerAdapter.kt',108,'list_item_country_card:iv_favourite')
# Shared wrappers have many concrete identifiers; retain an exact-ID fanout to all matching
# baseline variants rather than flattening TextView actions and MaterialButtons together.
wrapper_fanout=[]
for c in j['native_wrapper_calls']:
 matches=[]
 for r in j['baseline_components']+j['baseline_action_like_text_image_candidates']:
  ident=r['id']; stem=pathlib.Path(r['path']).stem
  if not ident:continue
  cam=re.sub(r'_([a-z0-9])',lambda m:m[1].upper(),ident)
  if re.search(r'\b'+re.escape(cam)+r'\b',c['call']) and any(m['path']==c['path'] for m in MAP.get(stem,[])):
   matches.append(stem+':'+ident)
 if matches:wrapper_fanout.append({'native_path':c['path'],'native_line':c['line'],'component':c['component'],'source_keys':matches,'call':c['call']})
result={'baseline':j['baseline'],'native_snapshot':j['native_snapshot'],'status':'source audit only; no compile/render claims','instance_mappings':instances,'priority_native_call_mappings':priority,'exact_wrapper_fanout':wrapper_fanout,'counts':dict(collections.Counter(x['status'] for x in instances)),'unmatched':[x for x in instances if x['status']!='mapped_renderer_family']}
(D/'component-style-restoration-renderer-map.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps({'counts':result['counts'],'priority_calls':len(priority),'wrapper_calls':len(wrapper_fanout),'unmatched':[(x['source_key'],x['status']) for x in result['unmatched']]},indent=2))
