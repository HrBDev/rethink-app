# Per-file layout ownership and native source candidates

This is the complete 186-file census at local commit `803cd4c884243ab9ef9c92cb0c4959d63bc27b00`. All files are byte-identical to original commit `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`. Full hashes, nodes/attributes, exact references, candidate call definitions and include edges are in `layout-inventory.json`. Styling equivalence and runtime acceptance are separate gates. Function lists below are source candidates, not a claim that every function replaces every original element.

| Physical layout file | Current resource use | Baseline owner(s) | Current Compose call candidates / disposition |
| --- | --- | --- | --- |
| `app/src/full/res/layout-w600dp/fragment_home_screen.xml` | dormant_resource | HomeScreenFragment.kt | HomeContent |
| `app/src/full/res/layout/activity_advanced_setting.xml` | dormant_resource | AdvancedSettingActivity.kt | AdvancedSettingsContent |
| `app/src/full/res/layout/activity_app_details.xml` | dormant_resource | AppInfoActivity.kt | AppDetailsContent |
| `app/src/full/res/layout/activity_app_list.xml` | dormant_resource | AppListActivity.kt | FirewallAppsContent |
| `app/src/full/res/layout/activity_app_lock.xml` | dormant_resource | AppLockActivity.kt | AppLockScreen |
| `app/src/full/res/layout/activity_block_free_dns.xml` | dormant_resource | BlockFreeDnsActivity.kt | BlockFreeDnsContent |
| `app/src/full/res/layout/activity_bubble.xml` | dormant_resource | BubbleActivity.kt | BubbleScreen |
| `app/src/full/res/layout/activity_console_log.xml` | dormant_resource | ConsoleLogActivity.kt | ConsoleLogsScreen |
| `app/src/full/res/layout/activity_misc_settings.xml` | dormant_resource | MiscSettingsActivity.kt | NetworkCheckRow, NetworkSettingsScreen, NetworkTaskerContent |
| `app/src/full/res/layout/activity_pause.xml` | dormant_resource | PauseActivity.kt | PauseScreen |
| `app/src/full/res/layout/activity_tcp_proxy.xml` | dormant_resource | TcpProxyMainActivity.kt | TcpScreen |
| `app/src/full/res/layout/activity_tunnel_settings.xml` | dormant_resource | TunnelSettingsActivity.kt | NetworkContent, NetworkSettingsScreen |
| `app/src/full/res/layout/activity_welcome.xml` | dormant_resource | WelcomeActivity.kt | WelcomeScreen |
| `app/src/full/res/layout/bottom_sheet_app_connections.xml` | dormant_resource | AppDomainRulesBottomSheet.kt; AppIpRulesBottomSheet.kt | AppRuleSheetContent |
| `app/src/full/res/layout/bottom_sheet_conn_track.xml` | dormant_resource | ConnTrackerBottomSheet.kt; RethinkLogBottomSheet.kt | ConnectionDetailsContent |
| `app/src/full/res/layout/bottom_sheet_dns_log.xml` | dormant_resource | DnsBlocklistBottomSheet.kt | DnsDetailsContent |
| `app/src/full/res/layout/bottom_sheet_firewall_sort_filter.xml` | dormant_resource | FirewallAppFilterBottomSheet.kt | FirewallFilterContent |
| `app/src/full/res/layout/bottom_sheet_home_screen.xml` | dormant_resource | HomeScreenSettingBottomSheet.kt | HomeModeContent |
| `app/src/full/res/layout/bottom_sheet_log_activity_interval.xml` | dormant_resource | LogActivityIntervalBottomSheet.kt; RpnLogActivityIntervalBottomSheet.kt | LogIntervalContent |
| `app/src/full/res/layout/bottom_sheet_orbot.xml` | dormant_resource | OrbotBottomSheet.kt | OrbotSheetContent |
| `app/src/full/res/layout/bottom_sheet_rethink_list.xml` | dormant_resource | RethinkListBottomSheet.kt | RethinkListScreen |
| `app/src/full/res/layout/bottom_sheet_rethink_plus_filter.xml` | dormant_resource | RethinkPlusFilterBottomSheet.kt | BlocklistFilterScreen |
| `app/src/full/res/layout/dialog_add_custom_domain.xml` | dormant_resource | CustomDomainAdapter.kt; CustomDomainFragment.kt | RulesPage |
| `app/src/full/res/layout/dialog_custom_lan_ip.xml` | dormant_resource | CustomLanIpDialog.kt | CustomLanIpContent |
| `app/src/full/res/layout/dialog_import_confirm.xml` | dormant_resource | CustomDomainFragment.kt; CustomIpFragment.kt | RulesPage |
| `app/src/full/res/layout/dialog_info_firewall_rules.xml` | dormant_resource | AppListActivity.kt | FirewallAppsContent |
| `app/src/full/res/layout/dialog_info_rules_layout.xml` | test_baseline_only | AboutFragment.kt; ConnTrackerBottomSheet.kt; DnsBlocklistBottomSheet.kt; OrbotBottomSheet.kt | AboutContent, AboutProgressContent, AboutRichTextContent, ConnectionDetailsContent, DatabaseDumpContent, DiagnosticSelectionContent, DiagnosticTabsContent, DnsDetailsContent, OrbotSheetContent |
| `app/src/full/res/layout/dialog_ip_details_layout.xml` | dormant_resource | include-only / no direct owner | Verified baseline orphan: no live equivalent invented (see retired-layout-owners.json) |
| `app/src/full/res/layout/dialog_lockdown_check.xml` | dormant_resource | TunnelSettingsActivity.kt | NetworkContent, NetworkSettingsScreen |
| `app/src/full/res/layout/dialog_set_custom_doh.xml` | dormant_resource | DoTListFragment.kt; DohListFragment.kt | DnsEndpointScreen, DnsServerEditor |
| `app/src/full/res/layout/dialog_set_custom_odoh.xml` | dormant_resource | ODoHListFragment.kt | DnsEndpointScreen, DnsServerEditor |
| `app/src/full/res/layout/dialog_set_dns_crypt.xml` | dormant_resource | DnsCryptListFragment.kt | DnsCryptEditor, DnsEndpointScreen, DnsRelaysDialog, OriginalButton, Row |
| `app/src/full/res/layout/dialog_set_dns_proxy.xml` | dormant_resource | DnsProxyListFragment.kt | DnsEndpointScreen, DnsProxyEditor |
| `app/src/full/res/layout/dialog_whatsnew.xml` | dormant_resource | AboutFragment.kt | AboutContent, AboutProgressContent, AboutRichTextContent, DatabaseDumpContent, DiagnosticSelectionContent, DiagnosticTabsContent |
| `app/src/full/res/layout/domain_item_bottom_sheet.xml` | dormant_resource | DomainRulesBtmSheetAdapter.kt | DomainRuleRowContent |
| `app/src/full/res/layout/fragment_connection_tracker.xml` | dormant_resource | ConnectionTrackerFragment.kt; RethinkLogFragment.kt | Content, LogAction, LogFilterChips, LogList, LogSearchBar, themeColor |
| `app/src/full/res/layout/fragment_custom_domain.xml` | dormant_resource | CustomDomainFragment.kt | RulesPage |
| `app/src/full/res/layout/fragment_custom_ip.xml` | dormant_resource | CustomIpFragment.kt | RulesPage |
| `app/src/full/res/layout/fragment_dns_crypt_list.xml` | dormant_resource | DnsCryptListFragment.kt | DnsCryptEditor, DnsEndpointScreen, DnsRelaysDialog, OriginalButton, Row |
| `app/src/full/res/layout/fragment_dns_proxy_list.xml` | dormant_resource | DnsProxyListFragment.kt | DnsEndpointScreen, DnsProxyEditor |
| `app/src/full/res/layout/fragment_doh_list.xml` | dormant_resource | DohListFragment.kt | DnsEndpointScreen, DnsServerEditor |
| `app/src/full/res/layout/fragment_dot_list.xml` | dormant_resource | DoTListFragment.kt | DnsEndpointScreen, DnsServerEditor |
| `app/src/full/res/layout/fragment_firewall_settings.xml` | dormant_resource | FirewallSettingsFragment.kt | FirewallSettingsMenuContent |
| `app/src/full/res/layout/fragment_home_screen.xml` | dormant_resource | HomeScreenFragment.kt | HomeContent |
| `app/src/full/res/layout/fragment_odoh_list.xml` | dormant_resource | ODoHListFragment.kt | DnsEndpointScreen, DnsServerEditor |
| `app/src/full/res/layout/fragment_proxy_configure.xml` | dormant_resource | ProxySettingsActivity.kt | NetworkContent, NetworkSettingsScreen |
| `app/src/full/res/layout/fragment_rethink_list.xml` | dormant_resource | RethinkListFragment.kt | RethinkListScreen |
| `app/src/full/res/layout/fragment_sponsor.xml` | dormant_resource | SponsorFragment.kt | SponsorScreen |
| `app/src/full/res/layout/fragment_sponsor_success.xml` | test_baseline_only | SponsorSuccessFragment.kt | SponsorSuccessScreen |
| `app/src/full/res/layout/item_allowed_app.xml` | dormant_resource | BubbleAllowedAppsAdapter.kt | BubbleAllowedRow, Row |
| `app/src/full/res/layout/item_blocked_app.xml` | dormant_resource | BubbleBlockedAppsAdapter.kt | BubbleBlockedRow, Row |
| `app/src/full/res/layout/item_chip_filter.xml` | dormant_resource | AppListActivity.kt; ConnectionTrackerFragment.kt; DnsLogFragment.kt; DomainConnectionsActivity.kt; EventsActivity.kt; FirewallAppFilterBottomSheet.kt; RethinkBlocklistFragment.kt; RethinkPlusFilterBottomSheet.kt; RpnBypassAppsActivity.kt; WgIncludeAppsActivity.kt | BlocklistConfigurationScreen, BlocklistFilterScreen, Content, EventsScreen, FirewallAppsContent, FirewallFilterContent, GroupAt, LogAction, LogFilterChips, LogList, LogSearchBar, OriginalChip, PagedStatisticsScreen, RpnBypassAppRow, RpnBypassContent, WgIncludeAppsContent, themeColor |
| `app/src/full/res/layout/item_home_firewall_rules_page.xml` | dormant_resource | HomeScreenFragment.kt | HomeContent |
| `app/src/full/res/layout/item_lockdown_check.xml` | dormant_resource | TunnelSettingsActivity.kt | NetworkContent, NetworkSettingsScreen |
| `app/src/full/res/layout/item_log_activity_app.xml` | dormant_resource | AppActivityAdapter.kt | AppActivityNativeRow, Row |
| `app/src/full/res/layout/item_log_activity_conn.xml` | dormant_resource | AppActivityAdapter.kt | AppActivityNativeRow, Row |
| `app/src/full/res/layout/list_item_app_domain_details.xml` | dormant_resource | AppWiseDomainsAdapter.kt | AppDomainsRowContent |
| `app/src/full/res/layout/list_item_app_ip_details.xml` | dormant_resource | AppWiseIpsAdapter.kt | AppIpsRowContent |
| `app/src/full/res/layout/list_item_block_free_dns.xml` | dormant_resource | BlockFreeDnsAdapter.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/full/res/layout/list_item_conn_track.xml` | dormant_resource | ConnectionLogAdapter.kt; ConnectionTrackerAdapter.kt; RethinkLogAdapter.kt | ConnectionLogRow |
| `app/src/full/res/layout/list_item_custom_all_domain.xml` | dormant_resource | CustomDomainAdapter.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/full/res/layout/list_item_custom_all_ip.xml` | dormant_resource | CustomIpAdapter.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/full/res/layout/list_item_custom_domain.xml` | dormant_resource | CustomDomainAdapter.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/full/res/layout/list_item_custom_ip.xml` | dormant_resource | CustomIpAdapter.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/full/res/layout/list_item_dns_log.xml` | dormant_resource | DnsLogAdapter.kt | DnsLogRow |
| `app/src/full/res/layout/list_item_firewall_app.xml` | dormant_resource | FirewallAppListAdapter.kt | FirewallAppRowContent |
| `app/src/full/res/layout/list_item_rethink_blocklist_simple.xml` | dormant_resource | LocalSimpleViewAdapter.kt; RemoteSimpleViewAdapter.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/full/res/layout/rethink_endpoint_list_item.xml` | dormant_resource | RethinkEndpointAdapter.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/full/res/layout/spinner_item_firewall_status.xml` | dormant_resource | FirewallStatusSpinnerAdapter.kt | Verified baseline orphan: no live equivalent invented (see retired-layout-owners.json) |
| `app/src/full/res/layout/view_home_logs_activity.xml` | dormant_resource | HomeScreenFragment.kt | HomeContent |
| `app/src/full/res/layout/view_home_logs_apps.xml` | dormant_resource | HomeScreenFragment.kt | HomeContent |
| `app/src/full/res/layout/view_sponsor_badge.xml` | dormant_resource | SponsorBadgeView.kt | Verified baseline orphan: no live equivalent invented (see retired-layout-owners.json) |
| `app/src/main/res/layout/activity_alerts.xml` | dormant_resource | AlertsActivity.kt | AlertsScreen |
| `app/src/main/res/layout/activity_anti_censorship.xml` | dormant_resource | AntiCensorshipActivity.kt | AntiCensorshipContent |
| `app/src/main/res/layout/activity_app_wise_domain_logs.xml` | dormant_resource | AppWiseDomainLogsActivity.kt | AppLogsContent |
| `app/src/main/res/layout/activity_app_wise_ip_logs.xml` | dormant_resource | AppWiseIpLogsActivity.kt | AppLogsContent |
| `app/src/main/res/layout/activity_backup_restore.xml` | dormant_resource | BackupRestoreBottomSheet.kt | BackupRestoreSheetContent |
| `app/src/main/res/layout/activity_checkout_proxy.xml` | dormant_resource | CheckoutActivity.kt | CheckoutContent |
| `app/src/main/res/layout/activity_configure_other_dns.xml` | dormant_resource | ConfigureOtherDnsActivity.kt | DnsScreen |
| `app/src/main/res/layout/activity_custom_rules.xml` | dormant_resource | CustomRulesActivity.kt | RulesPage, Surface |
| `app/src/main/res/layout/activity_customer_support.xml` | test_baseline_only | CustomerSupportActivity.kt | SupportScreen |
| `app/src/main/res/layout/activity_detailed_statistics.xml` | dormant_resource | DetailedStatisticsActivity.kt | PagedStatisticsScreen |
| `app/src/main/res/layout/activity_dns_detail.xml` | dormant_resource | DnsDetailActivity.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/main/res/layout/activity_domain_connections.xml` | dormant_resource | DomainConnectionsActivity.kt | GroupAt, OriginalChip, PagedStatisticsScreen |
| `app/src/main/res/layout/activity_events.xml` | dormant_resource | EventsActivity.kt | EventsScreen |
| `app/src/main/res/layout/activity_firewall.xml` | dormant_resource | FirewallActivity.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/main/res/layout/activity_network_logs.xml` | dormant_resource | NetworkLogsActivity.kt | Row, themeColor |
| `app/src/main/res/layout/activity_other_dns_list.xml` | dormant_resource | DnsListActivity.kt | DnsProtocolGrid |
| `app/src/main/res/layout/activity_ping_test.xml` | dormant_resource | PingTestActivity.kt | PingScreen |
| `app/src/main/res/layout/activity_purchase_history.xml` | dormant_resource | PurchaseHistoryActivity.kt | HistoryScreen, Row |
| `app/src/main/res/layout/activity_rpn_bypass_apps.xml` | dormant_resource | RpnBypassAppsActivity.kt | Content, RpnBypassAppRow, RpnBypassContent |
| `app/src/main/res/layout/activity_rpn_config_detail.xml` | dormant_resource | RpnConfigDetailActivity.kt | RpnConfigContent |
| `app/src/main/res/layout/activity_rpn_win_proxy_detail.xml` | dormant_resource | RpnWinProxyDetailsActivity.kt | RpnWinContent |
| `app/src/main/res/layout/activity_server_order_history.xml` | dormant_resource | ServerOrderHistoryActivity.kt | HistoryScreen, Row |
| `app/src/main/res/layout/activity_smart_dns_list.xml` | dormant_resource | SmartDnsListActivity.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/main/res/layout/activity_universal_firewall_settings.xml` | dormant_resource | UniversalFirewallSettingsActivity.kt | UniversalFirewallContent |
| `app/src/main/res/layout/activity_wg_config_editor.xml` | dormant_resource | WgConfigEditorActivity.kt | WgEditorContent |
| `app/src/main/res/layout/activity_wg_detail.xml` | dormant_resource | WgConfigDetailActivity.kt | WgDetailContent |
| `app/src/main/res/layout/activity_wireguard_main.xml` | dormant_resource | WgMainActivity.kt | WgMainContent |
| `app/src/main/res/layout/bottom_sheet_block_free_dns_mode.xml` | dormant_resource | BlockFreeDnsModeBottomSheet.kt | BlockFreeModeContent |
| `app/src/main/res/layout/bottom_sheet_bug_report_files.xml` | dormant_resource | BugReportFilesBottomSheet.kt | BugReportFilesContent |
| `app/src/main/res/layout/bottom_sheet_custom_domains.xml` | dormant_resource | CustomDomainRulesBtmSheet.kt | StoredRuleDetails |
| `app/src/main/res/layout/bottom_sheet_custom_ips.xml` | dormant_resource | CustomIpRulesBtmSheet.kt | StoredRuleDetails |
| `app/src/main/res/layout/bottom_sheet_dns_record_types.xml` | dormant_resource | DnsRecordTypesBottomSheet.kt | DnsRecordTypesContent |
| `app/src/main/res/layout/bottom_sheet_local_blocklists.xml` | dormant_resource | LocalBlocklistsBottomSheet.kt | LocalBlocklistsScreen |
| `app/src/main/res/layout/bottom_sheet_proxies_list.xml` | dormant_resource | ProxyCountriesBtmSheet.kt; WireguardListBtmSheet.kt | ProxyCountriesSheetContent, WgProxyRow, WgProxySelectionContent |
| `app/src/main/res/layout/bottom_sheet_summary_stats_filter.xml` | test_baseline_only | SummaryStatsFilterBottomSheet.kt | SummaryFilterSheetContent |
| `app/src/main/res/layout/bottomsheet_auto_exclude_countries.xml` | dormant_resource | AutoExcludeCountriesBottomSheet.kt | RpnExcludeContent |
| `app/src/main/res/layout/bottomsheet_device_auth_error.xml` | dormant_resource | DeviceAuthErrorBottomSheet.kt | DeviceErrorContent |
| `app/src/main/res/layout/bottomsheet_device_not_registered.xml` | dormant_resource | DeviceNotRegisteredBottomSheet.kt | DeviceErrorContent |
| `app/src/main/res/layout/bottomsheet_entitlement_detail.xml` | dormant_resource | EntitlementDetailBottomSheet.kt | EntitlementContent |
| `app/src/main/res/layout/bottomsheet_purchase_conflict.xml` | dormant_resource | PurchaseConflictBottomSheet.kt | ConflictContent |
| `app/src/main/res/layout/bottomsheet_purchase_processing.xml` | dormant_resource | PurchaseProcessingBottomSheet.kt | ProcessingContent |
| `app/src/main/res/layout/bottomsheet_rinr_warning.xml` | dormant_resource | RethinkInRethinkWarningBottomSheet.kt | RethinkWarningSheetContent |
| `app/src/main/res/layout/bottomsheet_rpn_stats.xml` | dormant_resource | RpnStatsBottomSheet.kt | RpnStatisticsContent, StatisticsRow |
| `app/src/main/res/layout/bottomsheet_server_removal_notification.xml` | dormant_resource | ServerRemovalNotificationBottomSheet.kt | RpnRemovalContent |
| `app/src/main/res/layout/bottomsheet_server_settings.xml` | dormant_resource | ServerSettingsBottomSheet.kt | RpnSettingsContent |
| `app/src/main/res/layout/dialog_add_custom_ip.xml` | dormant_resource | CustomIpAdapter.kt; CustomIpFragment.kt | RulesPage |
| `app/src/main/res/layout/dialog_dnscrypt_relays.xml` | dormant_resource | DnsCryptRelaysDialog.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/main/res/layout/dialog_input_ips.xml` | test_baseline_only | NetworkReachabilityDialog.kt | ReachabilityScreen |
| `app/src/main/res/layout/dialog_rpn_ssid.xml` | dormant_resource | RpnSsidDialog.kt | RpnSsidContent |
| `app/src/main/res/layout/dialog_server_loading.xml` | dormant_resource | RethinkPlusDashboardFragment.kt; ServerSelectionFragment.kt | RpnAccountScreen, RpnSelectionScreen |
| `app/src/main/res/layout/dialog_set_proxy.xml` | dormant_resource | ProxySettingsActivity.kt | NetworkContent, NetworkSettingsScreen |
| `app/src/main/res/layout/dialog_sponsor_info.xml` | dormant_resource | include-only / no direct owner | Verified baseline orphan: no live equivalent invented (see retired-layout-owners.json) |
| `app/src/main/res/layout/dialog_wg_add_peer.xml` | dormant_resource | WgAddPeerDialog.kt | WgPeerContent |
| `app/src/main/res/layout/dialog_wg_apps.xml` | dormant_resource | WgIncludeAppsActivity.kt | WgIncludeAppsContent |
| `app/src/main/res/layout/dialog_wg_hop.xml` | dormant_resource | GenericHopDialog.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/main/res/layout/dialog_wg_ssid.xml` | dormant_resource | WgSsidDialog.kt | WgSsidContent |
| `app/src/main/res/layout/dns_crypt_endpoint_list_item.xml` | dormant_resource | DnsCryptEndpointAdapter.kt; DnsCryptRelayEndpointAdapter.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/main/res/layout/dns_proxy_list_item.xml` | dormant_resource | DnsProxyEndpointAdapter.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/main/res/layout/fragment_about.xml` | dormant_resource | AboutFragment.kt | AboutContent, AboutProgressContent, AboutRichTextContent, DatabaseDumpContent, DiagnosticSelectionContent, DiagnosticTabsContent |
| `app/src/main/res/layout/fragment_configure.xml` | dormant_resource | ConfigureFragment.kt | ConfigureMenuContent |
| `app/src/main/res/layout/fragment_dns_configure.xml` | dormant_resource | DnsSettingsFragment.kt | DnsSettingsScreen |
| `app/src/main/res/layout/fragment_dns_logs.xml` | dormant_resource | DnsLogFragment.kt | Content, LogAction, LogFilterChips, LogList, LogSearchBar, themeColor |
| `app/src/main/res/layout/fragment_rethink_blocklist.xml` | dormant_resource | RethinkBlocklistFragment.kt | BlocklistConfigurationScreen |
| `app/src/main/res/layout/fragment_rethink_plus.xml` | dormant_resource | RethinkPlusFragment.kt | Surface, homeNavigationClearance |
| `app/src/main/res/layout/fragment_rethink_plus_dashboard.xml` | dormant_resource | RethinkPlusDashboardFragment.kt | RpnAccountScreen |
| `app/src/main/res/layout/fragment_rethink_plus_manage_purchase.xml` | dormant_resource | RethinkPlusManagePurchaseFragment.kt | ManagePurchaseContent |
| `app/src/main/res/layout/fragment_rethink_plus_premium.xml` | dormant_resource | RethinkPlusFragment.kt | Content, SubscriptionContent |
| `app/src/main/res/layout/fragment_server_selection.xml` | dormant_resource | ServerSelectionFragment.kt | RpnSelectionScreen |
| `app/src/main/res/layout/fragment_smart_dns_list.xml` | dormant_resource | SmartDnsListFragment.kt | SmartDnsContent |
| `app/src/main/res/layout/fragment_summary_statistics.xml` | dormant_resource | SummaryStatisticsFragment.kt | SummaryStatisticsScreen |
| `app/src/main/res/layout/fragment_wg_nw_stats.xml` | dormant_resource | WgNwStatsFragment.kt | WgStatsContent |
| `app/src/main/res/layout/item_bug_report_file.xml` | dormant_resource | BugReportFilesBottomSheet.kt | BugReportFilesContent |
| `app/src/main/res/layout/item_dns_record_type.xml` | dormant_resource | DnsRecordTypesBottomSheet.kt | DnsRecordTypesContent |
| `app/src/main/res/layout/item_event.xml` | dormant_resource | EventsAdapter.kt | EventLogRow |
| `app/src/main/res/layout/item_network_policy.xml` | dormant_resource | TunnelSettingsActivity.kt | NetworkContent, NetworkSettingsScreen |
| `app/src/main/res/layout/item_ping_test_history.xml` | dormant_resource | PingTestHistoryAdapter.kt | Row, Surface, themeColor, utilityImage, utilityTextStyle |
| `app/src/main/res/layout/item_removed_server.xml` | dormant_resource | ServerRemovalNotificationBottomSheet.kt | RpnRemovalContent |
| `app/src/main/res/layout/item_section_header.xml` | dormant_resource | CountryServerAdapter.kt | Country, OriginalLabel, Row, themeColor |
| `app/src/main/res/layout/item_server.xml` | dormant_resource | include-only / no direct owner | Verified baseline orphan: no live equivalent invented (see retired-layout-owners.json) |
| `app/src/main/res/layout/item_server_group.xml` | dormant_resource | CountryServerAdapter.kt | Country, OriginalLabel, Row, themeColor |
| `app/src/main/res/layout/item_ssid.xml` | dormant_resource | SsidAdapter.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/main/res/layout/layout_entitlement_row.xml` | dormant_resource | EntitlementDetailBottomSheet.kt | EntitlementContent |
| `app/src/main/res/layout/list_item_app_country_details.xml` | dormant_resource | AppWiseCountriesAdapter.kt | AppCountriesRowContent |
| `app/src/main/res/layout/list_item_blocklist_app_row.xml` | dormant_resource | BlocklistAppsAdapter.kt | Row, StatisticsRow |
| `app/src/main/res/layout/list_item_blocklist_domain_row.xml` | dormant_resource | BlocklistAppsAdapter.kt | Row, StatisticsRow |
| `app/src/main/res/layout/list_item_console_log.xml` | dormant_resource | ConsoleLogAdapter.kt | ConsoleLogRow |
| `app/src/main/res/layout/list_item_country_card.xml` | dormant_resource | CountryServerAdapter.kt | Country, OriginalLabel, Row, themeColor |
| `app/src/main/res/layout/list_item_endpoint.xml` | dormant_resource | DoTEndpointAdapter.kt; DohEndpointAdapter.kt; ODoHEndpointAdapter.kt; SmartDnsEndpointAdapter.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/main/res/layout/list_item_exclude_country.xml` | dormant_resource | AutoExcludeCountriesBottomSheet.kt | RpnExcludeContent |
| `app/src/main/res/layout/list_item_play_subs.xml` | dormant_resource | GooglePlaySubsAdapter.kt | PurchasePlanCard, PurchaseShimmer, Row |
| `app/src/main/res/layout/list_item_proxy_cc_wg.xml` | dormant_resource | ProxyCountriesBtmSheet.kt; WireguardListBtmSheet.kt | ProxyCountriesSheetContent, WgProxyRow, WgProxySelectionContent |
| `app/src/main/res/layout/list_item_purchase_history.xml` | dormant_resource | PurchaseHistoryAdapter.kt | HistoryRowContent |
| `app/src/main/res/layout/list_item_rethink_blocklist_adv.xml` | dormant_resource | LocalAdvancedViewAdapter.kt; RemoteAdvancedViewAdapter.kt | Indirect/replaced owner: component mapping or independent styling ledger must resolve; no automatic acceptance |
| `app/src/main/res/layout/list_item_rpn_bypass_app.xml` | dormant_resource | RpnBypassAppsActivity.kt | Content, RpnBypassAppRow, RpnBypassContent |
| `app/src/main/res/layout/list_item_rpn_stat_app.xml` | dormant_resource | RpnStatsBottomSheet.kt | RpnStatisticsContent, StatisticsRow |
| `app/src/main/res/layout/list_item_server_order.xml` | dormant_resource | ServerOrderHistoryAdapter.kt | HistoryRowContent |
| `app/src/main/res/layout/list_item_server_wg_peers.xml` | dormant_resource | ServerWgPeersAdapter.kt | Verified baseline orphan: no live equivalent invented (see retired-layout-owners.json) |
| `app/src/main/res/layout/list_item_shimmer_card.xml` | dormant_resource | GooglePlaySubsAdapter.kt | PurchasePlanCard, PurchaseShimmer, Row |
| `app/src/main/res/layout/list_item_shimmer_server_row.xml` | dormant_resource | include-only / no direct owner | Included by activity_server_order_history, fragment_server_selection; see parent owner and styling map |
| `app/src/main/res/layout/list_item_statistics_summary.xml` | dormant_resource | DomainConnectionsAdapter.kt; SummaryStatisticsAdapter.kt; WgNwStatsAdapter.kt | StatisticsRow, WgStatsRow, statisticsFavIcon, themeColor |
| `app/src/main/res/layout/list_item_vpn_server.xml` | dormant_resource | VpnServerAdapter.kt | RpnAddServerTile, RpnServerGrid, RpnServerTile |
| `app/src/main/res/layout/list_item_vpn_server_add.xml` | dormant_resource | VpnServerAdapter.kt | RpnAddServerTile, RpnServerGrid, RpnServerTile |
| `app/src/main/res/layout/list_item_wg_general_interface.xml` | dormant_resource | WgConfigAdapter.kt | WgConfigRow |
| `app/src/main/res/layout/list_item_wg_hop.xml` | dormant_resource | GenericHopAdapter.kt; WgHopAdapter.kt | WgHopRow |
| `app/src/main/res/layout/list_item_wg_include_apps.xml` | dormant_resource | WgIncludeAppsAdapter.kt | WgAppRow |
| `app/src/main/res/layout/list_item_wg_one_interface.xml` | dormant_resource | OneWgConfigAdapter.kt | WgOneConfigRow |
| `app/src/main/res/layout/list_item_wg_peers.xml` | dormant_resource | WgPeersAdapter.kt | WgPeerRow |
| `app/src/main/res/layout/shimmer_subscription_loading.xml` | dormant_resource | include-only / no direct owner | Included by fragment_rethink_plus_premium; see parent owner and styling map |
| `app/src/main/res/layout/view_insights_rank_row.xml` | dormant_resource | AppInfoActivity.kt; SummaryStatisticsFragment.kt | AppDetailsContent, SummaryStatisticsScreen |
| `app/src/main/res/layout/view_tour_tooltip.xml` | dormant_resource | TourOverlayController.kt | OriginalButton, Row, TourOverlay, themeColor |
| `app/src/main/res/layout/welcome_slide1.xml` | dormant_resource | WelcomeActivity.kt | WelcomeScreen |
| `app/src/main/res/layout/welcome_slide2.xml` | dormant_resource | WelcomeActivity.kt | WelcomeScreen |
| `app/src/main/res/layout/welcome_slide3.xml` | dormant_resource | WelcomeActivity.kt | WelcomeScreen |
| `app/src/main/res/layout/welcome_slide4.xml` | dormant_resource | WelcomeActivity.kt | WelcomeScreen |
