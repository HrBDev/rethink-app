# Per-layout styling register

All 186 physical source-set/qualifier files are listed. Each row links to explicit original root/container/shape/typography/color/dimension/semantics contracts in `styling-equivalence-ledger.json`. Mapped families and extracted source properties are evidence locations, never equivalence approvals. No row is deletion-ready.

| Layout | Root | Nodes | Native family | Findings / remaining disposition |
| --- | --- | ---: | --- | --- |
| `full/res/layout-w600dp/fragment_home_screen.xml` | ConstraintLayout | 87 | compose/home/HomeContent.kt:74 | S01, S02, S03, S04, S05, S26 |
| `full/res/layout/activity_advanced_setting.xml` | NestedScrollView | 49 | compose/settings/AdvancedSettingsContent.kt:95,73 | S08, S10 |
| `full/res/layout/activity_app_details.xml` | NestedScrollView | 167 | compose/appdetails/AppDetailsContent.kt:38,128,119 | S08, S26 |
| `full/res/layout/activity_app_list.xml` | RelativeLayout | 20 | compose/firewallapps/FirewallAppsContent.kt:45 | S06 |
| `full/res/layout/activity_app_lock.xml` | CoordinatorLayout | 2 | compose/utilities/OnboardingScreens.kt:81 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/activity_block_free_dns.xml` | CoordinatorLayout | 20 | compose/dns/BlockFreeDnsContent.kt:67 | S30 |
| `full/res/layout/activity_bubble.xml` | CoordinatorLayout | 24 | compose/utilities/BubbleScreen.kt:33 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/activity_console_log.xml` | RelativeLayout | 11 | compose/trafficlogs/ConsoleLogsScreen.kt:59 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/activity_misc_settings.xml` | NestedScrollView | 143 | compose/networksettings/NetworkNativeUi.kt:172,180 | S06, S10 |
| `full/res/layout/activity_pause.xml` | RelativeLayout | 15 | compose/utilities/PauseScreen.kt:24 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/activity_tcp_proxy.xml` | NestedScrollView | 24 | compose/utilities/TcpScreen.kt:17 | S08, S25 |
| `full/res/layout/activity_tunnel_settings.xml` | NestedScrollView | 188 | compose/networksettings/NetworkNativeUi.kt:172,180 | S06, S10 |
| `full/res/layout/activity_welcome.xml` | ConstraintLayout | 6 | compose/utilities/OnboardingScreens.kt:40 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/bottom_sheet_app_connections.xml` | LinearLayout | 21 | compose/appdetails/AppRuleSheetContent.kt:21,20 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/bottom_sheet_conn_track.xml` | NestedScrollView | 54 | compose/trafficlogs/LogDetailsContent.kt:101,139 | S08 |
| `full/res/layout/bottom_sheet_dns_log.xml` | NestedScrollView | 52 | compose/trafficlogs/LogDetailsContent.kt:101,139 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/bottom_sheet_firewall_sort_filter.xml` | LinearLayout | 11 | compose/firewallapps/FirewallFilterContent.kt:37 | S31 |
| `full/res/layout/bottom_sheet_home_screen.xml` | LinearLayout | 28 | compose/settings/ModeSheets.kt:78,121 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/bottom_sheet_log_activity_interval.xml` | NestedScrollView | 23 | compose/home/LogIntervalContent.kt:95 | S32 |
| `full/res/layout/bottom_sheet_orbot.xml` | LinearLayout | 29 | compose/networksheets/NetworkSheetContent.kt:52 | S09 |
| `full/res/layout/bottom_sheet_rethink_list.xml` | LinearLayout | 6 | bottomsheet/RethinkListBottomSheet.kt:59 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/bottom_sheet_rethink_plus_filter.xml` | LinearLayout | 7 | compose/blocklists/BlocklistFilterScreen.kt:30 | S31 |
| `full/res/layout/dialog_add_custom_domain.xml` | ConstraintLayout | 14 | compose/rules/RulesContent.kt:159 | S20 |
| `full/res/layout/dialog_custom_lan_ip.xml` | ScrollView | 31 | compose/rules/CustomLanIpContent.kt:42,67 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/dialog_import_confirm.xml` | ConstraintLayout | 18 | compose/rules/RulesContent.kt:64 | S20 |
| `full/res/layout/dialog_info_firewall_rules.xml` | NestedScrollView | 10 | compose/firewallapps/FirewallFilterContent.kt:61 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/dialog_info_rules_layout.xml` | NestedScrollView | 10 | compose/networksheets/OrbotInfoContent.kt:96 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/dialog_ip_details_layout.xml` | NestedScrollView | 7 | No live baseline renderer required; retirement evidence pending final reference gate | Retirement check |
| `full/res/layout/dialog_lockdown_check.xml` | ScrollView | 11 | compose/networksettings/NetworkNativeUi.kt:172,180 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/dialog_set_custom_doh.xml` | ConstraintLayout | 15 | compose/dns/DnsEndpointEditors.kt:42,148,133 | S25, S34 |
| `full/res/layout/dialog_set_custom_odoh.xml` | ConstraintLayout | 14 | compose/dns/DnsEndpointEditors.kt:42,148,133 | S34 |
| `full/res/layout/dialog_set_dns_crypt.xml` | ConstraintLayout | 17 | compose/dns/DnsEndpointEditors.kt:73,148,133 | S34 |
| `full/res/layout/dialog_set_dns_proxy.xml` | ConstraintLayout | 23 | compose/dns/DnsEndpointEditors.kt:100,148,133 | S09, S25 |
| `full/res/layout/dialog_whatsnew.xml` | ScrollView | 3 | compose/about/AboutDiagnosticsContent.kt:126 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/domain_item_bottom_sheet.xml` | RelativeLayout | 4 | compose/appdetails/AppRuleSheetContent.kt:21,20 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/fragment_connection_tracker.xml` | RelativeLayout | 13 | compose/trafficlogs/LogScreen.kt:71,79 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/fragment_custom_domain.xml` | ConstraintLayout | 14 | compose/rules/RulesContent.kt:64 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/fragment_custom_ip.xml` | ConstraintLayout | 14 | compose/rules/RulesContent.kt:64 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/fragment_dns_crypt_list.xml` | ConstraintLayout | 11 | compose/dns/DnsEndpointScreen.kt:52<br>fragment/DnsCryptListFragment.kt:63 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/fragment_dns_proxy_list.xml` | ConstraintLayout | 8 | compose/dns/DnsEndpointScreen.kt:52 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/fragment_doh_list.xml` | ConstraintLayout | 8 | compose/dns/DnsEndpointScreen.kt:52 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/fragment_dot_list.xml` | ConstraintLayout | 8 | compose/dns/DnsEndpointScreen.kt:52 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/fragment_firewall_settings.xml` | NestedScrollView | 26 | compose/settings/SettingsMenus.kt:110 | S06 |
| `full/res/layout/fragment_home_screen.xml` | ConstraintLayout | 86 | compose/home/HomeContent.kt:74 | S02, S03, S04, S05, S26 |
| `full/res/layout/fragment_odoh_list.xml` | ConstraintLayout | 8 | compose/dns/DnsEndpointScreen.kt:52 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/fragment_proxy_configure.xml` | NestedScrollView | 51 | compose/networksettings/NetworkNativeUi.kt:172,180 | S06 |
| `full/res/layout/fragment_rethink_list.xml` | NestedScrollView | 19 | compose/blocklists/RethinkListScreen.kt:47 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/fragment_sponsor.xml` | ConstraintLayout | 29 | compose/utilities/SponsorScreens.kt:23,75 | S10, S26 |
| `full/res/layout/fragment_sponsor_success.xml` | LinearLayout | 8 | compose/utilities/SponsorScreens.kt:23,75 | S26 |
| `full/res/layout/item_allowed_app.xml` | CardView | 7 | compose/utilities/BubbleScreen.kt:33 | S21, S26 |
| `full/res/layout/item_blocked_app.xml` | CardView | 9 | compose/utilities/BubbleScreen.kt:33 | S21, S26 |
| `full/res/layout/item_chip_filter.xml` | Chip | 1 | compose/appdetails/AppConnectionRows.kt:22<br>compose/blocklists/BlocklistConfigurationScreen.kt:51<br>compose/blocklists/BlocklistFilterScreen.kt:30<br>compose/firewallapps/FirewallAppsContent.kt:45<br>compose/firewallapps/FirewallFilterContent.kt:37<br>compose/rpnconfig/RpnBypassContent.kt:56<br>compose/trafficlogs/LogScreen.kt:71<br>compose/wireguard/WgIncludeAppsContent.kt:25 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/item_home_firewall_rules_page.xml` | LinearLayout | 3 | compose/home/HomeContent.kt:155 | S05 |
| `full/res/layout/item_lockdown_check.xml` | LinearLayout | 6 | compose/networksettings/NetworkNativeUi.kt:172,180 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/item_log_activity_app.xml` | LinearLayout | 10 | compose/home/LogIntervalContent.kt:134 | S32 |
| `full/res/layout/item_log_activity_conn.xml` | LinearLayout | 10 | compose/home/LogIntervalContent.kt:134 | S32 |
| `full/res/layout/list_item_app_domain_details.xml` | LinearLayout | 12 | compose/appdetails/AppConnectionRows.kt:19,18,20 | S27 |
| `full/res/layout/list_item_app_ip_details.xml` | LinearLayout | 10 | compose/appdetails/AppConnectionRows.kt:19,18,20 | S27 |
| `full/res/layout/list_item_block_free_dns.xml` | MaterialCardView | 7 | compose/dns/BlockFreeDnsContent.kt:67 | S30 |
| `full/res/layout/list_item_conn_track.xml` | LinearLayout | 28 | compose/trafficlogs/LogRows.kt:37,68 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/list_item_custom_all_domain.xml` | RelativeLayout | 14 | compose/rules/RulesContent.kt:225 | S25 |
| `full/res/layout/list_item_custom_all_ip.xml` | RelativeLayout | 15 | compose/rules/RulesContent.kt:225 | S25 |
| `full/res/layout/list_item_custom_domain.xml` | RelativeLayout | 9 | compose/rules/RulesContent.kt:241 | S19, S25 |
| `full/res/layout/list_item_custom_ip.xml` | RelativeLayout | 10 | compose/rules/RulesContent.kt:241 | S19, S25 |
| `full/res/layout/list_item_dns_log.xml` | LinearLayout | 29 | compose/trafficlogs/LogRows.kt:37,68 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `full/res/layout/list_item_firewall_app.xml` | MaterialCardView | 11 | compose/firewallapps/FirewallAppsContent.kt:107 | S06 |
| `full/res/layout/list_item_rethink_blocklist_simple.xml` | LinearLayout | 10 | compose/blocklists/BlocklistConfigurationScreen.kt:144 | S06, S25 |
| `full/res/layout/rethink_endpoint_list_item.xml` | MaterialCardView | 11 | compose/dns/DnsEndpointScreen.kt:52 | S06 |
| `full/res/layout/spinner_item_firewall_status.xml` | RelativeLayout | 3 | No live baseline renderer required; retirement evidence pending final reference gate | Retirement check |
| `full/res/layout/view_home_logs_activity.xml` | ConstraintLayout | 26 | compose/home/HomeContent.kt:74 | S05, S26 |
| `full/res/layout/view_home_logs_apps.xml` | ConstraintLayout | 18 | compose/home/HomeContent.kt:74 | S05, S26 |
| `full/res/layout/view_sponsor_badge.xml` | merge | 2 | No live baseline renderer required; retirement evidence pending final reference gate | Retirement check |
| `main/res/layout/activity_alerts.xml` | ConstraintLayout | 2 | compose/utilities/OnboardingScreens.kt:90 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/activity_anti_censorship.xml` | NestedScrollView | 55 | compose/settings/AntiCensorshipContent.kt:53 | S06, S09 |
| `main/res/layout/activity_app_wise_domain_logs.xml` | LinearLayout | 14 | compose/appdetails/AppLogsContent.kt:59 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/activity_app_wise_ip_logs.xml` | LinearLayout | 14 | compose/appdetails/AppLogsContent.kt:59 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/activity_backup_restore.xml` | LinearLayout | 19 | compose/networksheets/NetworkSheetContent.kt:125 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/activity_checkout_proxy.xml` | ScrollView | 32 | compose/purchase/CheckoutContent.kt:69 | S09 |
| `main/res/layout/activity_configure_other_dns.xml` | LinearLayout | 3 | compose/dnssettings/DnsFragmentHost.kt:37 | S35 |
| `main/res/layout/activity_custom_rules.xml` | LinearLayout | 3 | activity/CustomRulesActivity.kt:89 | S24 |
| `main/res/layout/activity_customer_support.xml` | CoordinatorLayout | 47 | compose/utilities/SupportScreen.kt:35 | S08, S26 |
| `main/res/layout/activity_detailed_statistics.xml` | RelativeLayout | 10 | compose/statistics/PagedStatisticsScreen.kt:22 | S33 |
| `main/res/layout/activity_dns_detail.xml` | LinearLayout | 3 | compose/settings/SingleSettingsFragmentHost.kt:45 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/activity_domain_connections.xml` | RelativeLayout | 11 | activity/DomainConnectionsActivity.kt:99<br>compose/statistics/PagedStatisticsScreen.kt:22 | S33 |
| `main/res/layout/activity_events.xml` | RelativeLayout | 17 | compose/trafficlogs/EventsScreen.kt:64<br>compose/trafficlogs/LogScreen.kt:71,79 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/activity_firewall.xml` | LinearLayout | 3 | compose/settings/SingleSettingsFragmentHost.kt:45 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/activity_network_logs.xml` | LinearLayout | 5 | activity/NetworkLogsActivity.kt:87 | S24 |
| `main/res/layout/activity_other_dns_list.xml` | NestedScrollView | 58 | compose/dnssettings/DnsProtocolGrid.kt:33 | S06 |
| `main/res/layout/activity_ping_test.xml` | CoordinatorLayout | 23 | compose/utilities/PingScreen.kt:32 | S26 |
| `main/res/layout/activity_purchase_history.xml` | CoordinatorLayout | 31 | compose/purchase/HistoryScreen.kt:45 | S15 |
| `main/res/layout/activity_rpn_bypass_apps.xml` | LinearLayout | 13 | compose/rpnconfig/RpnBypassContent.kt:56 | S22, S25 |
| `main/res/layout/activity_rpn_config_detail.xml` | CoordinatorLayout | 103 | compose/rpnconfig/RpnConfigContent.kt:37 | S22 |
| `main/res/layout/activity_rpn_win_proxy_detail.xml` | LinearLayout | 38 | compose/rpnconfig/RpnWinContent.kt:34 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/activity_server_order_history.xml` | CoordinatorLayout | 27 | compose/purchase/HistoryScreen.kt:45 | S11, S15, S16 |
| `main/res/layout/activity_smart_dns_list.xml` | LinearLayout | 4 | compose/dnssettings/DnsFragmentHost.kt:37 | S35 |
| `main/res/layout/activity_universal_firewall_settings.xml` | NestedScrollView | 79 | compose/settings/UniversalFirewallContent.kt:89 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/activity_wg_config_editor.xml` | CoordinatorLayout | 22 | compose/wireguard/WgEditors.kt:23 | S07 |
| `main/res/layout/activity_wg_detail.xml` | ConstraintLayout | 91 | compose/wireguard/WgDetailContent.kt:48 | S07, S22 |
| `main/res/layout/activity_wireguard_main.xml` | ConstraintLayout | 15 | compose/wireguard/WgMainContent.kt:31,20 | S07, S11 |
| `main/res/layout/bottom_sheet_block_free_dns_mode.xml` | LinearLayout | 22 | compose/settings/ModeSheets.kt:78,121 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/bottom_sheet_bug_report_files.xml` | ConstraintLayout | 18 | compose/about/BugReportFilesContent.kt:57,91 | S25 |
| `main/res/layout/bottom_sheet_custom_domains.xml` | LinearLayout | 48 | compose/rules/RuleDetailsContent.kt:37 | S06, S19 |
| `main/res/layout/bottom_sheet_custom_ips.xml` | LinearLayout | 54 | compose/rules/RuleDetailsContent.kt:37 | S06, S19 |
| `main/res/layout/bottom_sheet_dns_record_types.xml` | LinearLayout | 9 | compose/about/DnsRecordTypesContent.kt:58 | S29 |
| `main/res/layout/bottom_sheet_local_blocklists.xml` | ConstraintLayout | 24 | compose/blocklists/LocalBlocklistsScreen.kt:28 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/bottom_sheet_proxies_list.xml` | LinearLayout | 5 | compose/wireguard/WgProxySelectionContent.kt:27 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/bottom_sheet_summary_stats_filter.xml` | LinearLayout | 15 | compose/statistics/SummaryFilterSheetContent.kt:21 | S26 |
| `main/res/layout/bottomsheet_auto_exclude_countries.xml` | LinearLayout | 26 | compose/rpn/RpnExcludeContent.kt:23 | S26 |
| `main/res/layout/bottomsheet_device_auth_error.xml` | NestedScrollView | 20 | compose/purchase/PurchaseSheets.kt:68 | S26 |
| `main/res/layout/bottomsheet_device_not_registered.xml` | NestedScrollView | 27 | compose/purchase/PurchaseSheets.kt:68 | S26 |
| `main/res/layout/bottomsheet_entitlement_detail.xml` | NestedScrollView | 22 | compose/purchase/EntitlementContent.kt:39 | S26 |
| `main/res/layout/bottomsheet_purchase_conflict.xml` | NestedScrollView | 23 | compose/purchase/PurchaseSheets.kt:86 | S26 |
| `main/res/layout/bottomsheet_purchase_processing.xml` | ConstraintLayout | 7 | compose/purchase/PurchaseSheets.kt:100 | S26 |
| `main/res/layout/bottomsheet_rinr_warning.xml` | NestedScrollView | 9 | compose/networksheets/NetworkSheetContent.kt:161,179 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/bottomsheet_rpn_stats.xml` | LinearLayout | 33 | compose/statistics/RpnStatisticsContent.kt:45 | S26 |
| `main/res/layout/bottomsheet_server_removal_notification.xml` | ConstraintLayout | 12 | compose/rpn/RpnRemovalContent.kt:21 | S26 |
| `main/res/layout/bottomsheet_server_settings.xml` | LinearLayout | 71 | compose/rpn/RpnSettingsContent.kt:23 | S06, S08, S25, S26 |
| `main/res/layout/dialog_add_custom_ip.xml` | ConstraintLayout | 10 | compose/rules/RulesContent.kt:159 | S20 |
| `main/res/layout/dialog_dnscrypt_relays.xml` | RelativeLayout | 4 | compose/dns/DnsEndpointScreen.kt:168 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/dialog_input_ips.xml` | NestedScrollView | 50 | compose/utilities/ReachabilityScreen.kt:31,62,67,72 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/dialog_rpn_ssid.xml` | ConstraintLayout | 33 | compose/rpnconfig/RpnSsidContent.kt:47 | S09, S22, S26 |
| `main/res/layout/dialog_server_loading.xml` | LinearLayout | 6 | compose/rpn/RpnComponents.kt:69 | S11, S23 |
| `main/res/layout/dialog_set_proxy.xml` | NestedScrollView | 32 | compose/networksettings/NetworkNativeUi.kt:180,329 | S25 |
| `main/res/layout/dialog_sponsor_info.xml` | LinearLayout | 4 | No live baseline renderer required; retirement evidence pending final reference gate | Retirement check |
| `main/res/layout/dialog_wg_add_peer.xml` | ConstraintLayout | 20 | compose/wireguard/WgEditors.kt:43 | S07 |
| `main/res/layout/dialog_wg_apps.xml` | RelativeLayout | 14 | compose/wireguard/WgIncludeAppsContent.kt:25 | S25 |
| `main/res/layout/dialog_wg_hop.xml` | RelativeLayout | 4 | compose/wireguard/WgDialogContent.kt:30 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/dialog_wg_ssid.xml` | FrameLayout | 23 | compose/wireguard/WgSsidContent.kt:48 | S09 |
| `main/res/layout/dns_crypt_endpoint_list_item.xml` | MaterialCardView | 11 | compose/dns/DnsEndpointScreen.kt:106 | S06, S28 |
| `main/res/layout/dns_proxy_list_item.xml` | MaterialCardView | 11 | compose/dns/DnsEndpointScreen.kt:52 | S06, S28 |
| `main/res/layout/fragment_about.xml` | NestedScrollView | 74 | compose/about/AboutContent.kt:39,129 | S37 |
| `main/res/layout/fragment_configure.xml` | NestedScrollView | 78 | compose/settings/SettingsMenus.kt:62 | S06 |
| `main/res/layout/fragment_dns_configure.xml` | NestedScrollView | 117 | compose/dnssettings/DnsSettingsScreen.kt:33 | S06, S08 |
| `main/res/layout/fragment_dns_logs.xml` | RelativeLayout | 11 | compose/trafficlogs/LogScreen.kt:71,79 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/fragment_rethink_blocklist.xml` | RelativeLayout | 30 | compose/blocklists/BlocklistConfigurationScreen.kt:51 | S06 |
| `main/res/layout/fragment_rethink_plus.xml` | ConstraintLayout | 20 | app/src/fdroid/java/com/celzero/bravedns/ui/fragment/RethinkPlusFragment.kt:25 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/fragment_rethink_plus_dashboard.xml` | FrameLayout | 57 | compose/rpn/RpnAccountScreen.kt:24 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/fragment_rethink_plus_manage_purchase.xml` | FrameLayout | 79 | compose/purchase/ManagePurchaseContent.kt:58 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/fragment_rethink_plus_premium.xml` | CoordinatorLayout | 54 | compose/purchase/SubscriptionContent.kt:73,65 | S16, S26 |
| `main/res/layout/fragment_server_selection.xml` | CoordinatorLayout | 101 | compose/rpn/RpnSelectionScreen.kt:59,220,204 | S16, S26 |
| `main/res/layout/fragment_smart_dns_list.xml` | ConstraintLayout | 5 | compose/dns/SmartDnsContent.kt:17 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/fragment_summary_statistics.xml` | FrameLayout | 184 | compose/statistics/SummaryStatisticsScreen.kt:41,95 | S26 |
| `main/res/layout/fragment_wg_nw_stats.xml` | NestedScrollView | 15 | compose/wireguard/WgStatsContent.kt:14 | S11 |
| `main/res/layout/item_bug_report_file.xml` | LinearLayout | 8 | compose/about/BugReportFilesContent.kt:57,91 | S25, S26 |
| `main/res/layout/item_dns_record_type.xml` | RelativeLayout | 5 | compose/about/DnsRecordTypesContent.kt:84 | S25 |
| `main/res/layout/item_event.xml` | MaterialCardView | 13 | compose/trafficlogs/EventsScreen.kt:38 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/item_network_policy.xml` | LinearLayout | 5 | compose/networksettings/NetworkNativeUi.kt:172,180 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/item_ping_test_history.xml` | LinearLayout | 8 | app/src/main/java/com/celzero/bravedns/adapter/PingTestHistoryAdapter.kt:25 | S36 |
| `main/res/layout/item_removed_server.xml` | MaterialCardView | 6 | compose/rpn/RpnRemovalContent.kt:21 | S36 |
| `main/res/layout/item_section_header.xml` | AppCompatTextView | 1 | adapter/CountryServerAdapter.kt:84 | S18 |
| `main/res/layout/item_server.xml` | LinearLayout | 4 | No live baseline renderer required; retirement evidence pending final reference gate | S25 |
| `main/res/layout/item_server_group.xml` | LinearLayout | 9 | adapter/CountryServerAdapter.kt:96 | S18, S25, S26 |
| `main/res/layout/item_ssid.xml` | LinearLayout | 4 | compose/rpnconfig/RpnSsidContent.kt:47<br>compose/wireguard/WgSsidContent.kt:48 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/layout_entitlement_row.xml` | LinearLayout | 4 | compose/purchase/EntitlementContent.kt:39 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/list_item_app_country_details.xml` | LinearLayout | 8 | compose/appdetails/AppConnectionRows.kt:19,18,20 | S27 |
| `main/res/layout/list_item_blocklist_app_row.xml` | LinearLayout | 5 | app/src/main/java/com/celzero/bravedns/adapter/BlocklistAppsAdapter.kt:41 | S14 |
| `main/res/layout/list_item_blocklist_domain_row.xml` | LinearLayout | 3 | app/src/main/java/com/celzero/bravedns/adapter/BlocklistAppsAdapter.kt:41 | S14 |
| `main/res/layout/list_item_console_log.xml` | RelativeLayout | 4 | compose/trafficlogs/ConsoleLogsScreen.kt:50 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/list_item_country_card.xml` | LinearLayout | 12 | adapter/CountryServerAdapter.kt:96 | S18 |
| `main/res/layout/list_item_endpoint.xml` | MaterialCardView | 11 | compose/dns/DnsEndpointScreen.kt:106 | S06, S28 |
| `main/res/layout/list_item_exclude_country.xml` | LinearLayout | 6 | compose/rpn/RpnExcludeContent.kt:23 | S25 |
| `main/res/layout/list_item_play_subs.xml` | ConstraintLayout | 16 | compose/purchase/PurchasePlans.kt:24,47 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/list_item_proxy_cc_wg.xml` | RelativeLayout | 6 | compose/networksheets/NetworkSheetContent.kt:187<br>compose/wireguard/WgProxySelectionContent.kt:37 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/list_item_purchase_history.xml` | LinearLayout | 12 | compose/purchase/HistoryRowContent.kt:46 | S15 |
| `main/res/layout/list_item_rethink_blocklist_adv.xml` | LinearLayout | 11 | compose/blocklists/BlocklistConfigurationScreen.kt:51 | S06, S25 |
| `main/res/layout/list_item_rpn_bypass_app.xml` | LinearLayout | 6 | compose/rpnconfig/RpnBypassContent.kt:102 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/list_item_rpn_stat_app.xml` | LinearLayout | 6 | compose/statistics/StatisticsRow.kt:28 | S13 |
| `main/res/layout/list_item_server_order.xml` | LinearLayout | 16 | compose/purchase/HistoryRowContent.kt:46,34 | S15 |
| `main/res/layout/list_item_server_wg_peers.xml` | LinearLayout | 14 | No live baseline renderer required; retirement evidence pending final reference gate | Retirement check |
| `main/res/layout/list_item_shimmer_card.xml` | ShimmerFrameLayout | 6 | compose/purchase/PurchasePlans.kt:24,47 | S16 |
| `main/res/layout/list_item_shimmer_server_row.xml` | MaterialCardView | 7 | compose/purchase/HistoryScreen.kt:45<br>compose/purchase/PurchasePlans.kt:47<br>compose/rpn/RpnSelectionScreen.kt:232 | S16 |
| `main/res/layout/list_item_statistics_summary.xml` | LinearLayout | 11 | compose/statistics/StatisticsRow.kt:28<br>compose/wireguard/WgRows.kt:102 | S12 |
| `main/res/layout/list_item_vpn_server.xml` | MaterialCardView | 23 | compose/rpn/RpnServerTile.kt:48,84 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/list_item_vpn_server_add.xml` | FrameLayout | 4 | compose/rpn/RpnServerTile.kt:103 | S17 |
| `main/res/layout/list_item_wg_general_interface.xml` | ConstraintLayout | 23 | compose/wireguard/WgRows.kt:19,34,49,74 | S07 |
| `main/res/layout/list_item_wg_hop.xml` | RelativeLayout | 16 | compose/wireguard/WgRows.kt:19,34,49,74 | S07, S11, S22, S25 |
| `main/res/layout/list_item_wg_include_apps.xml` | MaterialCardView | 7 | compose/wireguard/WgRows.kt:64 | S07, S22, S25 |
| `main/res/layout/list_item_wg_one_interface.xml` | ConstraintLayout | 19 | compose/wireguard/WgRows.kt:19,34,49,74 | S07, S25 |
| `main/res/layout/list_item_wg_peers.xml` | LinearLayout | 16 | compose/wireguard/WgRows.kt:87 | S07 |
| `main/res/layout/shimmer_subscription_loading.xml` | LinearLayout | 10 | compose/purchase/PurchasePlans.kt:24,47 | S16 |
| `main/res/layout/view_insights_rank_row.xml` | LinearLayout | 5 | compose/appdetails/AppDetailsInsights.kt:22<br>compose/statistics/SummaryStatisticsScreen.kt:130 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/view_tour_tooltip.xml` | MaxWidthFrameLayout | 16 | tour/TourOverlayController.kt:158 | S26 |
| `main/res/layout/welcome_slide1.xml` | RelativeLayout | 4 | compose/utilities/OnboardingScreens.kt:40 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/welcome_slide2.xml` | RelativeLayout | 4 | compose/utilities/OnboardingScreens.kt:40 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/welcome_slide3.xml` | RelativeLayout | 4 | compose/utilities/OnboardingScreens.kt:40 | Per-node reconciliation and runtime fixtures pending; not accepted |
| `main/res/layout/welcome_slide4.xml` | RelativeLayout | 4 | compose/utilities/OnboardingScreens.kt:40 | Per-node reconciliation and runtime fixtures pending; not accepted |
