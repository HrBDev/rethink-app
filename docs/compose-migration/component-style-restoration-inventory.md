# Original component-style restoration inventory

## Scope and evidence

- Original source: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
- Native Compose comparison snapshot: `e2b97de59cc854de74010c096ceffbaed6cb4a55`.
- User correction: chips, fields and buttons changed across the app. Original per-component styling is now the target; earlier permission to relax Material 3 presentation does not justify those differences.
- This document is a source audit. No Android compilation, emulator execution, screenshot comparison, account access, SDK installation or network access was performed. The missing toolchain and denied dependency-download route remain unchanged.
- No production files were edited by the inventory worker. Home section-spacing/navbar changes are independently owned. Button/chip restoration owns `controls/OriginalControls.kt` and call-site integration; field restoration owns `fields/OriginalTextFields.kt` and field integration.

### Deliverables

- `component-style-restoration-inventory.json`: full baseline attributes, ancestor layouts, repo-local style inheritance, color/dimension definitions, relevant drawable/selector XML, current native calls and wrapper fanout navigation aids.
- `component-style-restoration-renderer-map.json`: each baseline instance/container/action candidate linked to concrete current renderer-family files and verified function locations; 60 priority direct-call mappings select exact `layout:id` variants; 33 exact wrapper-call fanouts.
- `component-style-variant-groups.json`: 213 distinct normalized source-attribute contracts; both duplicate Home keys across default/w600 qualifiers have matching component attributes. Positional margins/constraints remain in the full census.
- `inventory_component_styles.py`, `map_component_style_renderers.py`, `group_component_style_variants.py`: reproducible Python-standard-library/Git-only source generators, pinned to the two commits above. They do not execute app code.
- `check_component_style_coverage.py`: checks current source for remaining direct Material buttons/chips/fields and unresolved baseline obligations. A generated style's existence alone is never a passing integration result.

## Census

All 190 baseline layout resources were scanned, including `full`, `main`, and `layout-w600dp`.

| Baseline source kind | Count |
| --- | ---: |
| Material Chip | 109 |
| MaterialButton | 124 |
| TextInputLayout | 27 |
| TextInputEditText | 25 |
| AppCompatEditText | 29 |
| EditText | 2 |
| SearchView | 13 |
| MaterialButtonToggleGroup / ChipGroup | 14 / 25 |
| FAB / extended FAB / AppCompatImageButton | 12 / 7 / 3 |
| Additional action-like TextView/ImageView candidates | 39 |

The 27 parent TextInputLayouts and their child editors are separate source records, not 52 independent inputs. Counts are definitions, not visible instances. One dynamic `item_chip_filter` template is inflated at 15 baseline sites and produces arbitrary numbers of chips. Six explicit programmatic widget constructors and 130 targeted runtime style-mutation sites are separately captured.

The migrated phone UI native snapshot has 225 direct calls: 23 FilterChip, 7 AssistChip, 2 SuggestionChip, 24 Button, 9 OutlinedButton, 81 TextButton, 48 IconButton, 1 FilledIconToggleButton, 3 FAB, 2 small FAB, 4 extended FAB, 14 OutlinedTextField, 3 TextField, and 4 BasicTextField. There are also 59 calls to the audited shared wrappers and manually drawn `Surface`/`Text` controls. Searching only `ui/compose/` misses DomainConnectionsActivity, DnsCryptListFragment, CountryServerAdapter, NetworkLogsActivity and TourOverlayController.

## Confirmed shared causes

1. `RethinkTheme.kt` copies host colors and text appearances into a Compose MaterialTheme, but does not transfer Android widget-style defaults, shape attributes, state-list colors, inset geometry or per-layout attributes. Correct palette mapping alone cannot restore component styling.
2. `WgControls.WgAction` collapsed MaterialButton, outlined/text/dialog actions, and framed TextViews into `TextButton`. `WgMainContent.WgToggle` collapsed two different outlined toggle families into filled `Button`.
3. `WgControls.WgField` collapsed outlined inputs, plain/custom-background editors, labels and hint variants into an 8dp `OutlinedTextField`. `DnsEndpointEditors.DnsField` separately collapsed default TextInputLayout inputs and plain AppCompatEditText inputs into the same default outlined control.
4. Screen-local raw FilterChip/AssistChip/TextButton calls bypassed `ThinnerChip.Action`, `ThinnerChip.Large`, drawables, state selectors and instance sizes. Several source buttons became chips, and several source chips became text links.
5. Some manually drawn replacements retained only one attribute: `AppDetailsContent.Chip` uses an 18dp positive surface with a 48dp visible minimum for all source action chips, losing the 12dp/24dp family and negative Close All colors. `WgRows.WgChips` preserves some geometry, but hardcodes a 1.5dp border/default surface while each original instance has its own attrs and dynamic colors. `HistoryChip` substitutes `MaterialTheme.shapes.small` for a 16dp framed badge.
6. Runtime state colors were often collapsed via `ColorStateList.defaultColor`. Checked, selected, enabled, pressed and focused states must be resolved as appropriate; `.defaultColor` is not the selected-state contract.
7. Existing static audits primarily checked IDs, strings, controller behavior and selected sizes. They do not prove shape/fill/stroke/typography or full instance integration. Source-generation counts and changed call counts must not be presented as visual parity.

## Chip families and exceptions

| Source family | Actual baseline contract | Key current families |
| --- | --- | --- |
| `ThinnerChip.Action` (76 XML instances) | Parent `Widget.Material3.Chip.Assist`; 12dp corner, 24dp visual minimum, 30dp minimum-touch attribute, 11sp, 15dp icon, `homeScreenBtnBackground` base. Instance font/fill/text/stroke overrides are common. | AppDetails private `Chip`; SummaryStatistics section actions; WgRows `WgChips`; blocklist actions; HistoryScreen; Reachability reset |
| Positive/negative Action variants | `chipBgColorPositive` + `chipTextPositive`; Close All uses negative equivalents. Some instances use 12sp or 14sp rather than inherited 11sp. Preserve the custom theme attributes across Light/Dark/Black/Plus/Frost. | AppDetails, SummaryStatistics, RethinkList, custom-rule see-more, WG badges |
| `ThinnerChip.Large` (9 XML instances, including dynamic template) | Parent Filter; 18dp corner, 32dp minimum, 40dp minimum-touch attribute, inherited 14sp and `chip_text_color` selector. Template/domain/statistics overrides set `default_font_text_view` = 12sp, `bg_chip_state_list`, and optional 15dp icon. | FirewallApps/Filter, logs/events, blocklist filters, domain tags, RPN bypass, WG apps, custom-domain and stats filter |
| Standard Material3 Filter (17 XML) | Seven BlockFree protocol chips, four sponsor amount chips, six support categories. Their library-backed defaults must stay distinct from Thin Large/Action. Explicit source text appearance or checkability still wins. | BlockFreeDnsContent, SponsorScreens, SupportScreen |
| Home checkable Action | 12dp Action family; layout height32dp/visual minimum24dp; 10sp; `homeScreenBtnBackground`; 1dp `chip_toggle_stroke`; `chip_toggle_text`; no checked icon. Enable-logs uses same family but is not checkable. | `HomeContent` chip row and enable-logs action; coordinate Home owner |
| Rethink-list special shape (4) | `ThinnerChip.Action` plus `app:shapeAppearance=ShapeAppearance.LargeComponent` (1dp). **This is not shapeAppearanceOverlay.** The inherited `chipCornerRadius=12dp` also exists. Resolve Material1.14 attribute precedence; do not assert 1dp rendered corners merely from one attribute. 14sp/positive fill/text. | RethinkListScreen version/update/check/redownload |
| RPN programmatic filter | Checkable; checked icon hidden; 40dp min height; 12dp chip start/end padding; 1dp border accent when selected, transparent otherwise; accent alpha0x33 checked fill; background otherwise; checked accent text and normal primaryText. Shape remains inherited from actual Chip theme. | RpnFilterContent |
| RPN frequent-country | Noncheckable; 40dp min; 12dp start/end padding;13sp; `colorSurfaceVariant` fill;1dp `border`;primaryText. | RpnSelectionScreen |
| Small data badges | BlockFree type:6dp/24dp/10sp/white text/no touch expansion. RPN refresh:8dp/28dp/11sp/background/no stroke. RPN hero stats:8dp/24dp/11sp/background/no stroke. Removal badge:8dp/24dp/11sp bold/negative fill/accentBad/no stroke. Event type:16dp/24dp/LabelSmall. Link-speed:24dp high/11sp/no stroke/8dp start/end/no touch expansion plus runtime palette. | BlockFreeDnsContent, RpnConfigContent, RpnRemovalContent, EventsScreen, CountryServerAdapter |

Selectors are preserved verbatim in JSON. `bg_chip_state_list`: checked → `chipColorBgHighlight`, default → `chipColorBgNormal`. `chip_text_color` distinguishes enabled selected from enabled checked. Do not replace all checked colors with `secondaryContainer`.

## Button families and instance overrides

| Source family | Baseline contract / differences to preserve | Current integration target |
| --- | --- | --- |
| `toggleButtonStyle` (6) | Outlined parent; 16dp horizontal padding;1dp accentGood stroke. Layout usually16dp radius; controller supplies selected background/text state. | WG main, DNS record modes, simple/advanced blocklist |
| `toggleButtonNoStyle` (23) | Same outline family but primaryText stroke, with layout overrides2dp/8dp/16dp, small icon sizes, fonts and container tints. | App log/statistics time controls, LAN/reachability modes, app/stats view modes, bugreport send, RPN enable |
| `ActivityTimeSegment` (5) | Filled MaterialButton parent but source selectors control fill/stroke/text;20dp corner,38dp min,0dp vertical insets,8dp horizontal padding,1dp stroke,13sp bold, no all-caps. RPN config-mode overrides8dp corner/36dp layout/12sp/invertedPrimaryText. | LogIntervalContent and RpnSettingsContent; not generic FilterChip |
| `ConnTrackerSegment` | Declared8dp/40dp/13sp transparent fill,1dp selected outline/accent text; **no baseline usages found**. Do not apply it to an arbitrary current screen. | No active restoration assignment |
| Text/dialog actions | 26 explicit TextButton.Dialog,13 TextButton, plus themed generated dialogs. Default App.Dialog.NoDim positive uses colorPrimary, negative/neutral secondaryText; explicit XML dialog buttons frequently override all to accentGood and14sp. | DnsEditorDialog, Rules dialogs, WgAlertDialog/NetworkAlertDialog/AboutDialog and screen-local actions |
| Ordinary outlined/filled/elevated | Source has9 explicit outlined,7 explicit filled,1 elevated and23 unstylized MaterialButtons. Shape/colors/sizing come from source parent plus instance attrs, not the new semantic name. | Purchase/RPN/support/ping/onboarding utilities |
| Legacy framed text/action | `rectangle_border_background`:16dp corner,0.75dp border attr, no explicit solid fill. Often 10dp content padding; widths and fonts per instance. Applies to both TextViews and MaterialButtons with custom background. | WG edit/delete/dismiss/peer buttons, entitlement restore, reachability Save/Test, about rows, DNS relay add |
| Icon/FAB | Six explicit Material IconButton source layouts plus three AppCompatImageButtons and19FABs. Source sizes32/36/38/40dp and background/icon tints differ. Standard Compose IconButton48dp geometry is not automatically equivalent. | RPN/bugreport/statistics icons, tour, country favourites, WG/DNS/import actions |

Important source-to-new-family mismatches already mapped:

- Stats and app-log time toggles: outlined MaterialButton → FilterChip.
- Premium subscription/one-time selector: TextButton22dp/44dp/14sp bold inside toggle group → FilterChip.
- RPN reset: outlined24dp/48dp/1dp/14sp bold → filled Button.
- RPN SSID Add: filled12dp/48dp/14sp bold → TextButton.
- Purchase processing secondary: outlined28dp/50dp/14sp → TextButton.
- Entitlement Restore: framed TextView16dp/0.75dp → filled Button; Dismiss: filled12dp/52dp → TextButton.
- History Retry: purchase-history source is outlined, server-order-history source is App.AlertDialog.PositiveButton. The shared renderer must select by `server`.
- Device-error support: auth error uses accentBad; unregistered uses accentWarning. Both14dp/56dp/15sp bold. A single default-primary button loses the distinction.
- Ping:26dp radius,52dp high,16sp, serverHeaderGradientEnd tint. Support send:outlined28dp,56dp,16sp,accentGood text. Sponsor/support and success-continuation variants are not interchangeable.

## Field families and instance overrides

| Source family | Baseline contract | Renderer / owner |
| --- | --- | --- |
| Explicit OutlinedBox (17 parent definitions) | WG config/peer, custom domain/IP:8dp. Support:12dp with1000-character counter. Ping:16dp. Parent hint/box attrs and child typography/padding must be merged separately. | OriginalTextField decorated family; WgEditors/Rules/Support/Ping |
| Default TextInputLayout (10 parents) | DoH3/ODoH3/DNSCrypt3 have no explicit widget style and accentGood hint. RPN SSID has12dp corners but default box style. Resolve host textInputStyle; do not silently force outline or assume filled from missing XML. | DnsField and RpnSsidContent |
| Plain AppCompatEditText | DNS proxy fields use BodyLarge and layout-weight0.7; SOCKS proxy14sp monospace; programmatic Tasker multiline retains px padding and horizontal scrolling. No floating outlined label was requested. | DnsProxyEditor, NetworkEditField, NetworkTaskerContent |
| Explicit `edittext_default` | Transparent fill;1dp android:darker_gray stroke;4dp radius;10dp drawable padding. LAN12fields, reachability8fields, WG inline SSID. Error drawable changes stroke; not a generic M3 error-outline presentation. | LanPair, ProbeField, WG inline SSID |
| Notes programmatic EditText | Host editTextStyle;4–10 lines, max-length filter, separate counter. Current outlined replacement changed visual family. | AppDetailsDialogs |
| SearchView (13 definitions) | Search affordance/close button and underlying SearchView query presentation; parent card/pill belongs to layout. No source floating label. Distinct boxed logs/blocklist search versus RPN search-pill parent. | LogSearchBar, AppLogs, FirewallApps, RulesPage, WG apps, RPN bypass/selection |
| Transparent country search | Plain AppCompatEditText14sp/primaryText/hint secondary inside parent search container; not an outline field. | RpnExcludeContent |

LAN baseline address:prefix weights are2:1 with4dp gap; the native snapshot used3:1 with8dp. WG pair layout has0.7:0.3 chain weights and4dp margins. Preserve parent layout alongside the field drawable. Existing NetworkEditField and ProbeField already drew original backgrounds; retain that correct behavior while repairing focus/disabled/touch handling.

## Known missing or unresolved obligations

- `item_removed_server:removal_badge`: source negative badge is absent from current RpnRemovalContent. A new renderer resource does not restore missing content; reintroduce only the existing baseline badge.
- `activity_checkout_proxy:(anonymous)`: live XML MaterialButton at line76, text `plans`, rectangular border,14sp, visible. No corresponding native control in CheckoutContent. Establish its intended passive/action behavior from baseline context; do not invent a callback.
- `dialog_sponsor_info:dialog_sponsor_info_sponsor`: no baseline `R.layout.dialog_sponsor_info` or binding reference was found, and no current renderer. Kept explicit as an unassigned legacy source definition; do not silently count it restored or resurrect an unreachable flow.
- `item_server_group:chip_link_speed` and `list_item_country_card:iv_favourite` are in `ui/adapter/CountryServerAdapter.kt`, outside `ui/compose/`; the speed chip currently uses Text with a8dp background.
- `view_tour_tooltip` is rendered in `ui/tour/TourOverlayController.kt`; the close/skip/next controls need source-specific sizes and typography even though they lie outside the screen directory.
- The four Rethink-list shape/corner attribute conflicts require Material-library precedence, not an invented visual result.

## Implementation and validation contract

1. Use original source-key variants, not a single global pill/outline theme. Resolve local styles, Android theme defaults, layout attrs, then controller runtime overrides in their actual precedence.
2. Keep selection semantics, callback delivery, input filtering, IME handling, focus, disabled behavior, accessibility descriptions and transient state unchanged while changing presentation.
3. Record which source keys each dynamic renderer covers. Broad family mapping below is ownership/navigation, **not proof that all instances are integrated**.
4. Keep platform-dialog actions explicit (`@dialog` style mapping), not an undocumented blanket allowance. Low-level primitive usage is allowed inside the dedicated original-style renderers; other residual raw calls must be mapped or explicitly reviewed.
5. The coverage report must distinguish raw calls remaining, custom drawn controls/manual obligations remaining, resource definitions created, source keys integrated, source checks passed, and Android checks not run.
6. When the approved Android toolchain becomes available, validate every family in light/dark/black/Plus/Frost, enabled/disabled and checked/unchecked, focused/error text fields, long localized text, large font scale, narrow/wide layouts and keyboard-open dialogs. Until then no screenshot/visual-parity claim is warranted.

## Full layout-to-renderer ownership census

Every source instance is in the JSON with its exact attributes. The table below groups source IDs by layout for quick ownership; counts include parent groups and input-layout wrappers. The one duplicate source basename under `layout-w600dp` is retained separately.

| Baseline resource | Instances | Current renderer files | Status |
| --- | ---: | --- | --- |
| `full/res/layout-w600dp/fragment_home_screen.xml` | 2 | `compose/home/HomeContent.kt` | Mapped family |
| `full/res/layout/activity_app_details.xml` | 23 | `compose/appdetails/AppDetailsContent.kt` | Mapped family |
| `full/res/layout/activity_app_list.xml` | 2 | `compose/firewallapps/FirewallAppsContent.kt` | Mapped family |
| `full/res/layout/activity_block_free_dns.xml` | 8 | `compose/dns/BlockFreeDnsContent.kt` | Mapped family |
| `full/res/layout/activity_console_log.xml` | 2 | `compose/trafficlogs/ConsoleLogsScreen.kt` | Mapped family |
| `full/res/layout/activity_tcp_proxy.xml` | 1 | `compose/utilities/TcpScreen.kt` | Mapped family |
| `full/res/layout/activity_tunnel_settings.xml` | 1 | `compose/networksettings/NetworkNativeUi.kt` | Mapped family |
| `full/res/layout/activity_welcome.xml` | 2 | `compose/utilities/OnboardingScreens.kt` | Mapped family |
| `full/res/layout/bottom_sheet_conn_track.xml` | 1 | `compose/trafficlogs/LogDetailsContent.kt` | Mapped family |
| `full/res/layout/bottom_sheet_dns_log.xml` | 1 | `compose/trafficlogs/LogDetailsContent.kt` | Mapped family |
| `full/res/layout/bottom_sheet_firewall_sort_filter.xml` | 5 | `compose/firewallapps/FirewallFilterContent.kt` | Mapped family |
| `full/res/layout/bottom_sheet_log_activity_interval.xml` | 4 | `compose/home/LogIntervalContent.kt` | Mapped family |
| `full/res/layout/bottom_sheet_orbot.xml` | 2 | `compose/networksheets/NetworkSheetContent.kt` | Mapped family |
| `full/res/layout/bottom_sheet_rethink_plus_filter.xml` | 3 | `compose/blocklists/BlocklistFilterScreen.kt` | Mapped family |
| `full/res/layout/dialog_add_custom_domain.xml` | 8 | `compose/rules/RulesContent.kt` | Mapped family |
| `full/res/layout/dialog_custom_lan_ip.xml` | 17 | `compose/rules/CustomLanIpContent.kt` | Mapped family |
| `full/res/layout/dialog_import_confirm.xml` | 3 | `compose/rules/RulesContent.kt` | Mapped family |
| `full/res/layout/dialog_info_rules_layout.xml` | 1 | `compose/networksheets/OrbotInfoContent.kt` | Mapped family |
| `full/res/layout/dialog_set_custom_doh.xml` | 8 | `compose/dns/DnsEndpointEditors.kt` | Mapped family |
| `full/res/layout/dialog_set_custom_odoh.xml` | 8 | `compose/dns/DnsEndpointEditors.kt` | Mapped family |
| `full/res/layout/dialog_set_dns_crypt.xml` | 8 | `compose/dns/DnsEndpointEditors.kt` | Mapped family |
| `full/res/layout/dialog_set_dns_proxy.xml` | 5 | `compose/dns/DnsEndpointEditors.kt` | Mapped family |
| `full/res/layout/fragment_connection_tracker.xml` | 3 | `compose/trafficlogs/LogScreen.kt` | Mapped family |
| `full/res/layout/fragment_custom_domain.xml` | 3 | `compose/rules/RulesContent.kt` | Mapped family |
| `full/res/layout/fragment_custom_ip.xml` | 3 | `compose/rules/RulesContent.kt` | Mapped family |
| `full/res/layout/fragment_dns_crypt_list.xml` | 2 | `compose/dns/DnsEndpointScreen.kt`, `fragment/DnsCryptListFragment.kt` | Mapped family |
| `full/res/layout/fragment_dns_proxy_list.xml` | 1 | `compose/dns/DnsEndpointScreen.kt` | Mapped family |
| `full/res/layout/fragment_doh_list.xml` | 1 | `compose/dns/DnsEndpointScreen.kt` | Mapped family |
| `full/res/layout/fragment_dot_list.xml` | 1 | `compose/dns/DnsEndpointScreen.kt` | Mapped family |
| `full/res/layout/fragment_home_screen.xml` | 2 | `compose/home/HomeContent.kt` | Mapped family |
| `full/res/layout/fragment_odoh_list.xml` | 1 | `compose/dns/DnsEndpointScreen.kt` | Mapped family |
| `full/res/layout/fragment_rethink_list.xml` | 5 | `compose/blocklists/RethinkListScreen.kt` | Mapped family |
| `full/res/layout/fragment_sponsor.xml` | 6 | `compose/utilities/SponsorScreens.kt` | Mapped family |
| `full/res/layout/fragment_sponsor_success.xml` | 1 | `compose/utilities/SponsorScreens.kt` | Mapped family |
| `full/res/layout/item_allowed_app.xml` | 1 | `compose/utilities/BubbleScreen.kt` | Mapped family |
| `full/res/layout/item_blocked_app.xml` | 1 | `compose/utilities/BubbleScreen.kt` | Mapped family |
| `full/res/layout/item_chip_filter.xml` | 1 | `compose/appdetails/AppConnectionRows.kt`, `compose/blocklists/BlocklistConfigurationScreen.kt`, `compose/blocklists/BlocklistFilterScreen.kt`, `compose/firewallapps/FirewallAppsContent.kt`, `compose/firewallapps/FirewallFilterContent.kt`, `compose/rpnconfig/RpnBypassContent.kt`, `compose/trafficlogs/LogScreen.kt`, `compose/wireguard/WgIncludeAppsContent.kt` | Mapped family |
| `full/res/layout/list_item_block_free_dns.xml` | 1 | `compose/dns/BlockFreeDnsContent.kt` | Mapped family |
| `full/res/layout/list_item_custom_all_domain.xml` | 1 | `compose/rules/RulesContent.kt` | Mapped family |
| `full/res/layout/list_item_custom_all_ip.xml` | 1 | `compose/rules/RulesContent.kt` | Mapped family |
| `full/res/layout/rethink_endpoint_list_item.xml` | 1 | `compose/dns/DnsEndpointScreen.kt` | Mapped family |
| `full/res/layout/view_home_logs_activity.xml` | 3 | `compose/home/HomeContent.kt` | Mapped family |
| `full/res/layout/view_home_logs_apps.xml` | 3 | `compose/home/HomeContent.kt` | Mapped family |
| `main/res/layout/activity_app_wise_domain_logs.xml` | 5 | `compose/appdetails/AppLogsContent.kt` | Mapped family |
| `main/res/layout/activity_app_wise_ip_logs.xml` | 5 | `compose/appdetails/AppLogsContent.kt` | Mapped family |
| `main/res/layout/activity_checkout_proxy.xml` | 5 | `compose/purchase/CheckoutContent.kt` | Mapped family |
| `main/res/layout/activity_customer_support.xml` | 10 | `compose/utilities/SupportScreen.kt` | Mapped family |
| `main/res/layout/activity_domain_connections.xml` | 1 | `activity/DomainConnectionsActivity.kt` | Mapped family |
| `main/res/layout/activity_events.xml` | 3 | `compose/trafficlogs/LogScreen.kt` | Mapped family |
| `main/res/layout/activity_ping_test.xml` | 3 | `compose/utilities/PingScreen.kt` | Mapped family |
| `main/res/layout/activity_purchase_history.xml` | 1 | `compose/purchase/HistoryScreen.kt` | Mapped family |
| `main/res/layout/activity_rpn_bypass_apps.xml` | 2 | `compose/rpnconfig/RpnBypassContent.kt` | Mapped family |
| `main/res/layout/activity_rpn_config_detail.xml` | 5 | `compose/rpnconfig/RpnConfigContent.kt` | Mapped family |
| `main/res/layout/activity_rpn_win_proxy_detail.xml` | 1 | `compose/rpnconfig/RpnWinContent.kt` | Mapped family |
| `main/res/layout/activity_server_order_history.xml` | 3 | `compose/purchase/HistoryScreen.kt` | Mapped family |
| `main/res/layout/activity_wg_config_editor.xml` | 15 | `compose/wireguard/WgEditors.kt` | Mapped family |
| `main/res/layout/activity_wg_detail.xml` | 8 | `compose/wireguard/WgDetailContent.kt` | Mapped family |
| `main/res/layout/activity_wireguard_main.xml` | 8 | `compose/wireguard/WgMainContent.kt` | Mapped family |
| `main/res/layout/bottom_sheet_bug_report_files.xml` | 1 | `compose/about/BugReportFilesContent.kt` | Mapped family |
| `main/res/layout/bottom_sheet_dns_record_types.xml` | 3 | `compose/about/DnsRecordTypesContent.kt` | Mapped family |
| `main/res/layout/bottom_sheet_summary_stats_filter.xml` | 9 | `compose/statistics/SummaryFilterSheetContent.kt` | Mapped family |
| `main/res/layout/bottomsheet_auto_exclude_countries.xml` | 3 | `compose/rpn/RpnExcludeContent.kt` | Mapped family |
| `main/res/layout/bottomsheet_device_auth_error.xml` | 2 | `compose/purchase/PurchaseSheets.kt` | Mapped family |
| `main/res/layout/bottomsheet_device_not_registered.xml` | 2 | `compose/purchase/PurchaseSheets.kt` | Mapped family |
| `main/res/layout/bottomsheet_entitlement_detail.xml` | 2 | `compose/purchase/EntitlementContent.kt` | Mapped family |
| `main/res/layout/bottomsheet_purchase_conflict.xml` | 3 | `compose/purchase/PurchaseSheets.kt` | Mapped family |
| `main/res/layout/bottomsheet_purchase_processing.xml` | 2 | `compose/purchase/PurchaseSheets.kt` | Mapped family |
| `main/res/layout/bottomsheet_rinr_warning.xml` | 2 | `compose/networksheets/NetworkSheetContent.kt` | Mapped family |
| `main/res/layout/bottomsheet_rpn_stats.xml` | 1 | `compose/statistics/RpnStatisticsContent.kt` | Mapped family |
| `main/res/layout/bottomsheet_server_removal_notification.xml` | 1 | `compose/rpn/RpnRemovalContent.kt` | Mapped family |
| `main/res/layout/bottomsheet_server_settings.xml` | 6 | `compose/rpn/RpnSettingsContent.kt` | Mapped family |
| `main/res/layout/dialog_add_custom_ip.xml` | 5 | `compose/rules/RulesContent.kt` | Mapped family |
| `main/res/layout/dialog_dnscrypt_relays.xml` | 1 | `compose/dns/DnsEndpointScreen.kt` | Mapped family |
| `main/res/layout/dialog_input_ips.xml` | 14 | `compose/utilities/ReachabilityScreen.kt` | Mapped family |
| `main/res/layout/dialog_rpn_ssid.xml` | 5 | `compose/rpnconfig/RpnSsidContent.kt` | Mapped family |
| `main/res/layout/dialog_set_proxy.xml` | 6 | `compose/networksettings/NetworkNativeUi.kt` | Mapped family |
| `main/res/layout/dialog_sponsor_info.xml` | 1 |  | Needs review |
| `main/res/layout/dialog_wg_add_peer.xml` | 12 | `compose/wireguard/WgEditors.kt` | Mapped family |
| `main/res/layout/dialog_wg_apps.xml` | 2 | `compose/wireguard/WgIncludeAppsContent.kt` | Mapped family |
| `main/res/layout/dialog_wg_hop.xml` | 1 | `compose/wireguard/WgDialogContent.kt` | Mapped family |
| `main/res/layout/dialog_wg_ssid.xml` | 5 | `compose/wireguard/WgSsidContent.kt` | Mapped family |
| `main/res/layout/dns_proxy_list_item.xml` | 1 | `compose/dns/DnsEndpointScreen.kt` | Mapped family |
| `main/res/layout/fragment_about.xml` | 5 | `compose/about/AboutContent.kt` | Mapped family |
| `main/res/layout/fragment_dns_logs.xml` | 2 | `compose/trafficlogs/LogScreen.kt` | Mapped family |
| `main/res/layout/fragment_rethink_blocklist.xml` | 9 | `compose/blocklists/BlocklistConfigurationScreen.kt` | Mapped family |
| `main/res/layout/fragment_rethink_plus_premium.xml` | 9 | `compose/purchase/SubscriptionContent.kt` | Mapped family |
| `main/res/layout/fragment_server_selection.xml` | 10 | `compose/rpn/RpnSelectionScreen.kt` | Mapped family |
| `main/res/layout/fragment_summary_statistics.xml` | 32 | `compose/statistics/SummaryStatisticsScreen.kt` | Mapped family |
| `main/res/layout/fragment_wg_nw_stats.xml` | 4 | `compose/wireguard/WgStatsContent.kt` | Mapped family |
| `main/res/layout/item_bug_report_file.xml` | 2 | `compose/about/BugReportFilesContent.kt` | Mapped family |
| `main/res/layout/item_event.xml` | 2 | `compose/trafficlogs/EventsScreen.kt` | Mapped family |
| `main/res/layout/item_removed_server.xml` | 1 | `compose/rpn/RpnRemovalContent.kt` | Mapped family |
| `main/res/layout/item_server_group.xml` | 1 | `adapter/CountryServerAdapter.kt` | Mapped family |
| `main/res/layout/item_ssid.xml` | 1 | `compose/wireguard/WgSsidContent.kt` | Mapped family |
| `main/res/layout/list_item_country_card.xml` | 1 | `adapter/CountryServerAdapter.kt` | Mapped family |
| `main/res/layout/list_item_rethink_blocklist_adv.xml` | 1 | `compose/blocklists/BlocklistConfigurationScreen.kt` | Mapped family |
| `main/res/layout/list_item_server_order.xml` | 1 | `compose/purchase/HistoryRowContent.kt` | Mapped family |
| `main/res/layout/list_item_vpn_server.xml` | 3 | `compose/rpn/RpnServerTile.kt` | Mapped family |
| `main/res/layout/list_item_wg_general_interface.xml` | 8 | `compose/wireguard/WgRows.kt` | Mapped family |
| `main/res/layout/list_item_wg_hop.xml` | 8 | `compose/wireguard/WgRows.kt` | Mapped family |
| `main/res/layout/list_item_wg_one_interface.xml` | 4 | `compose/wireguard/WgRows.kt` | Mapped family |
| `main/res/layout/view_tour_tooltip.xml` | 3 | `tour/TourOverlayController.kt` | Mapped family |

### Counts are not completion

428 baseline rows have renderer-family locations; one legacy row is explicitly unmatched. This is source-navigation coverage only. It does not certify source-key integration, compile success, behavior or visual parity. The live coverage guard/report supplies the remaining direct-call and manual-obligation list.

## Live worktree progress

The pinned comparison above remains the regression evidence. The current implementation is changing concurrently. Consult `component-style-coverage-report.json` for the exact latest source scan, `button-chip-integration-ledger.json` for button/chip integration, and `original-field-style-map.json` / `original-fields-source-report.json` for field integration. A failing coverage guard means residual unreviewed sites/manual obligations remain; it is not an Android test failure and does not establish that every raw icon/FAB call is visually wrong. No blanket icon or dialog exception has been granted.

### Reviewed representative styles

`component-style-equivalence-decisions.json` records the exact source keys covered by representative styles. Nine AppDetails list positive action chips share the11sp contract; its nine insights chips share the12sp contract. The22 Summary positive action chips share that12sp contract, while its two negative Close All chips form a separate contract. Current renderer selection preserves the AppDetails list/insights split. These equivalences exclude positional margins, text/callbacks and runtime mutations.

### Supplemental apparent chips/actions/selectors

`component-style-supplemental-controls.json` captures the DNS lockdown clickable TextView, DNS/SOCKS AppCompatSpinners, and AppDetails Notes MaterialCardView. Their names do not consistently identify their real widget family. Notes remains18dp; the DNS notice remains plain BodyMedium/secondary text; spinners remain selector affordances rather than newly styled action buttons. These four records are supplemental to the typed-widget census above.

### Fixture assignments

`component-style-test-fixture-plan.json` assigns all213 normalized baseline variants to control, field or icon/FAB fake-content fixtures and lists catalog-only dialog/programmatic recipes. `plan_component_style_fixtures.py` cross-checks all source keys against the generated control catalog or the96-instance field map. These are planned tests, not methods that have run. Use the six exact AppTheme variants recorded in the fixture plan and verify source shape/fill/stroke/typography/visual geometry separately from touch targets; loop the entire catalog for resource resolution.

### Production source-set coverage

The live guard discovers every production source set under `app/src`, including future additions; test-prefixed/androidTest-prefixed sets and build outputs are excluded. Current sets are alpha, fdroid, full, main, play, tv and website. Eight exact TV files already used Compose at the pinned baseline and remain byte-identical:18controls are recorded under per-file SHA256 exemptions in `component-style-unchanged-source-exemptions.json`, not restyled or hidden by a blanket TV exemption. The complete pinned native-call census is therefore243 (225phone +18unchanged TV). Mutating an exempt file or adding a raw control in a future source set fails the guard; both paths are self-tested.

## Final static coverage checkpoint

The current-worktree guard and its mutation self-tests passed after the reviewed icon/FAB exceptions were merged:

- 0 unreviewed raw controls across every discovered production source set
- 0 unresolved manual renderer-linkage obligations, 0 stale/ambiguous call exceptions
- 53 reviewed, exact-path/full-invocation SHA256 icon/FAB allowances
- 8 exact-baseline-identical TV files containing 18 already-Compose controls
- 4 dedicated/retained BasicTextField primitive sites
- 117 button/chip ledger entries; 284 source catalog keys
- 213 normalized baseline variants assigned to planned fixtures, with 0 catalog/field-map gaps

The legacy `dialog_sponsor_info` source definition remains explicitly unassigned because no baseline/current controller reference was identified. It is not silently counted as an integrated UI or reintroduced.

This passing checkpoint establishes exhaustive source disposition and renderer linkage, not successful compilation, rendered styling, behavioral equivalence or pixel parity. All 213 visual-variant fixture assignments remain unexecuted. Run the live guard again after any source change; changed allowed invocations or unchanged-baseline files invalidate their exception automatically.
