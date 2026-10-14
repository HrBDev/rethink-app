# Whole-layout styling preservation audit

Baseline `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`; native snapshot `803cd4c884243ab9ef9c92cb0c4959d63bc27b00`.

Status: source-only preflight; all 186 XML layouts remain in place. No production edits, compilation, runtime, screenshot, TalkBack, downloads, VPN, account or traffic execution.

## Findings

### S01 — Wide Home logs card no longer reaches the shared bottom barrier

Priority: P0. Evidence: `confirmed_source_mismatch`.

Affected: `layout-w600dp/fragment_home_screen.xml`.

Original: w600dp logs is 0dp height, top below throughput, bottom constrained to fhs_row2_bottom_barrier over proxy/apps; outer gutters remain 2dp/8dp/10dp and 12dp vertical separation.

Current: HomeContent uses independent .58/.42 Column heights. LogsCard has content-driven height and cannot share the proxy/apps bottom boundary. This is separate from the already-restored compat gutters.

Before removal: Restore the cross-column measurement relationship in native Compose, and assert logs/apps outer bottom bounds for normal/long/hidden-content states. Preserve existing navigation-clearance and gutter fixes.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeContent.kt:82`, `app/src/full/res/layout-w600dp/fragment_home_screen.xml:699`.

### S02 — Home apps metric grid and qualifier-specific grouping collapsed

Priority: P0. Evidence: `confirmed_source_mismatch`.

Affected: `layout-w600dp/fragment_home_screen.xml`, `layout/fragment_home_screen.xml`.

Original: The wide layout puts count/title above an aligned Flow with maxElementsWrap=2, horizontal gap 12dp and vertical gap 6dp. ConstraintFlow owns blocked, isolated, bypassed, excluded order. Phone and wide have separate geometry.

Current: AppsCard shares one FlowRow for both qualifiers and nests two Columns in a Row; count/title and metrics may become siblings on one row, and row alignment/wrap is not the original 2×2 flow.

Before removal: Implement explicit phone and wide grid grouping from the two XML contracts. Assert row/column placement, wrap and reading order with long translated metrics.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeContent.kt:203`, `app/src/full/res/layout-w600dp/fragment_home_screen.xml:611`.

### S03 — Home fixed heights are now minimums with different padding constraints

Priority: P0. Evidence: `confirmed_source_mismatch`.

Affected: `layout-w600dp/fragment_home_screen.xml`, `layout/fragment_home_screen.xml`.

Original: Protection bar, firewall rules card and logs pager have fixed XML dimensions (40dp, wide rules 80dp, pager 168dp wide/190dp phone); card clearance consumes part of outer card constraints. NestedScrollView fillViewport=true is also explicit.

Current: ProtectionRow, RulesCard and NativePager use heightIn(min=...), while parent wrapping and padding order differ. Rules minimum is applied inside HomeCard after outside clearance, not to the original outer card. Scrollable Column does not explicitly reproduce fillViewport.

Before removal: Record outer allocated height versus visible surface versus content minimum for each state, then match default-font geometry without accidental double insets. Decide/validate large-text growth explicitly rather than call min-height equivalent to fixed height.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeContent.kt:158`, `app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeContent.kt:231`.

### S04 — Home card elevation space restored but shadow is not rendered

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout-w600dp/fragment_home_screen.xml`, `layout/fragment_home_screen.xml`.

Original: Default Widget.Rethink.MaterialCardView has 2dp elevation and compat padding; Frost has zero elevation; rules explicitly zero.

Current: HomeCard uses calculated padding then clip/background/border. It does not draw a shadow or an elevated Surface/Card. The restored gap is not the full elevation treatment.

Before removal: Preserve theme-dependent shadow rendering as well as measured outside clearance. Avoid duplicating compat padding or adding shadows to zero-elevation rules/Frost.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeContent.kt:126`, `app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeCardSpacing.kt:34`.

### S05 — Home typography loses source tracking, truncation and autosize

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout-w600dp/fragment_home_screen.xml`, `layout/fragment_home_screen.xml`, `layout/item_home_firewall_rules_page.xml`, `layout/view_home_logs_activity.xml`, `layout/view_home_logs_apps.xml`.

Original: Source labels use letterSpacing (.02/.05), maxLines/ellipsize and several uniform autosize ranges (rules 8–12sp; proxy labels 6–8sp). Home text size/color/alpha are not the complete contract.

Current: HomeText accepts size/color/monospace/allCaps but no letterSpacing, maxLines, ellipsize or source font weight; RulesCard and proxy labels use unbounded Text with fixed sizes.

Before removal: Carry actual baseline text contracts into presentation-only text rendering, retaining default-font alignment and source-specific overflow. Add narrow/RTL/large-font fixtures for rules, proxy legend and apps labels.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeContent.kt:478`, `app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeContent.kt:162`, `app/src/full/res/layout/item_home_firewall_rules_page.xml:39`.

### S06 — Settings/DNS cards still omit inherited compat-padding geometry

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/activity_app_list.xml`, `layout/activity_misc_settings.xml`, `layout/activity_tunnel_settings.xml`, `layout/fragment_firewall_settings.xml`, `layout/fragment_proxy_configure.xml`, `layout/list_item_firewall_app.xml`, `layout/list_item_rethink_blocklist_simple.xml`, `layout/rethink_endpoint_list_item.xml`, `layout/activity_anti_censorship.xml`, `layout/activity_other_dns_list.xml`, `layout/bottom_sheet_custom_domains.xml`, `layout/bottom_sheet_custom_ips.xml`, `layout/bottomsheet_server_settings.xml`, `layout/dns_crypt_endpoint_list_item.xml`, `layout/dns_proxy_list_item.xml`, `layout/fragment_configure.xml`, `layout/fragment_dns_configure.xml`, `layout/fragment_rethink_blocklist.xml`, `layout/list_item_endpoint.xml`, `layout/list_item_rethink_blocklist_adv.xml`.

Original: MaterialCardView inherits Widget.Rethink.MaterialCardView/Frost, including cardUseCompatPadding=true; explicit per-card radius/stroke/content padding and margins apply on top.

Current: SettingsCard resolves fill/radius/elevation only, then draws a Compose Card to full allocated bounds. It does not resolve compat padding, prevent-corner-overlap, max elevation, card stroke or explicit Android foreground.

Before removal: Create a source-style-aware native card primitive and map each call to its actual style plus per-card overrides. Reconcile outer clearance separately from content padding, including zero-elevation Frost.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/settings/SettingsComponents.kt:59`, `app/src/main/java/com/celzero/bravedns/ui/compose/settings/SettingsComponents.kt:68`.

### S07 — WireGuard card wrapper flattens per-layout padding and row grouping

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/activity_wg_config_editor.xml`, `layout/activity_wg_detail.xml`, `layout/activity_wireguard_main.xml`, `layout/dialog_wg_add_peer.xml`, `layout/list_item_wg_general_interface.xml`, `layout/list_item_wg_hop.xml`, `layout/list_item_wg_include_apps.xml`, `layout/list_item_wg_one_interface.xml`, `layout/list_item_wg_peers.xml`.

Original: WireGuard cards and their children have individually specified radii, margins, padding and constrained subrows; uptime/rx-tx are peers in a RelativeLayout in interface rows.

Current: WgCard always creates an inner Column with 16dp padding and 8dp spacing and no compat padding. WgRows stacks uptime and rx-tx separately instead of their original shared row. WgAppRow omits its source icon-container geometry.

Before removal: Use per-card contracts rather than one content-padding default. Restore explicit subrow grouping, hidden-child spacing and modifier order for each WireGuard layout.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgControls.kt:169`, `app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgRows.kt:42`.

### S08 — Raw switches bypass the original theme overlays

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/activity_advanced_setting.xml`, `layout/activity_app_details.xml`, `layout/activity_tcp_proxy.xml`, `layout/bottom_sheet_conn_track.xml`, `layout/activity_customer_support.xml`, `layout/bottomsheet_server_settings.xml`, `layout/fragment_dns_configure.xml`.

Original: These XML controls specify CustomWidget.Material3.CompoundButton.MaterialSwitch, whose CustomCompoundButtonSwitch overlay overrides colorSurface/colorOnSurface/colorControlActivated and secondary colors; checked/unchecked/disabled states matter.

Current: Their native calls use raw Switch defaults without the source overlay. RethinkTheme global colors do not apply an individual widget materialThemeOverlay.

Before removal: Route through a native switch renderer that resolves the particular XML style/overlay and its state colors/dimensions. Existing wgSwitchColors is useful partial infrastructure but not a blanket equivalence exemption.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/settings/AdvancedSettingsContent.kt:101`, `app/src/main/java/com/celzero/bravedns/ui/compose/utilities/SupportScreen.kt:121`, `app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnSettingsContent.kt:101`.

### S09 — Radio controls have unreviewed source-style/default differences

Priority: P1. Evidence: `confirmed_source_omission_runtime_effect_pending`.

Affected: `layout/bottom_sheet_orbot.xml`, `layout/dialog_set_dns_proxy.xml`, `layout/activity_anti_censorship.xml`, `layout/activity_checkout_proxy.xml`, `layout/dialog_rpn_ssid.xml`, `layout/dialog_wg_ssid.xml`.

Original: Baseline mixes MaterialRadioButton, AppCompatRadioButton and RadioButton, sometimes with explicit CustomCompoundButtonSwitch overlay, tint or padding. These are not a single global Material3 radio style.

Current: Native renderers mostly use RadioButton defaults. AntiCensorshipRow does not resolve the explicit overlay applied to all nine XML radios.

Before removal: Resolve source widget defaults/overlays and checked/unchecked/disabled states per family. Preserve selection group semantics and avoid duplicate targets when both row and control are clickable.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/settings/AntiCensorshipContent.kt:92`, `app/src/main/java/com/celzero/bravedns/ui/compose/purchase/CheckoutContent.kt:85`.

### S10 — SeekBar/Slider replacement does not preserve the source widget appearance

Priority: P1. Evidence: `confirmed_source_omission_runtime_effect_pending`.

Affected: `layout/activity_advanced_setting.xml`, `layout/activity_misc_settings.xml`, `layout/activity_tunnel_settings.xml`, `layout/fragment_sponsor.xml`.

Original: Baseline uses AppCompatSeekBar for several settings and a separately styled Slider for sponsorship; thumb/track/ticks/min-height arise from different widget defaults.

Current: Advanced/Network sources use generic Compose Slider, including its visual track/thumb/tick system, without source style/drawable resolution; callback preservation does not preserve visual form.

Before removal: Map each source slider type/attributes to an explicit native visual contract. Resolve external widget defaults in independent reference fixtures before deleting XML.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/settings/AdvancedSettingsContent.kt:112`, `app/src/main/java/com/celzero/bravedns/ui/compose/networksettings/NetworkNativeUi.kt:272`.

### S11 — Progress defaults replace explicit source track/color contracts

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/activity_server_order_history.xml`, `layout/activity_wireguard_main.xml`, `layout/dialog_server_loading.xml`, `layout/fragment_wg_nw_stats.xml`, `layout/list_item_wg_hop.xml`.

Original: Server-order progress is 3dp, square-cornered, accentGood on chipBgColorPositive. Server-loading timeout is 3dp with 4dp corners. Other XML progress nodes retain per-instance tints and thickness.

Current: HistoryScreen and WgProgress use default LinearProgressIndicator parameters; server-loading does not set source track corner/gap/stop behavior. Current Compose defaults are not verified against original MDC defaults.

Before removal: Map determinate/indeterminate progress per original node, including track, thickness, corner shape, gap/stop decorations, insets and accessible range.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/purchase/HistoryScreen.kt:64`, `app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgControls.kt:148`, `app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnComponents.kt:78`.

### S12 — StatisticsRow no longer matches the source summary row text contract

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/list_item_statistics_summary.xml`.

Original: Summary title 14sp bold single-line ellipsis; subtitle 11sp single-line ellipsis with 2dp top margin; count 12sp with .85 alpha; icon22dp in 40dp frame.

Current: StatisticsRow uses bodySmall (12sp) for subtitle and bodyMedium (14sp) for count; text has no maxLines/ellipsis; subtitle loses 2dp top margin. WgStatsRow also reorders data/name and has its own incompatible geometry.

Before removal: Preserve distinct summary/variant row contracts. Reintroduce exact sizes, line limits and baseline margins; do not use the same generic row for layouts with different metrics.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/statistics/StatisticsRow.kt:38`, `app/src/main/java/com/celzero/bravedns/ui/compose/statistics/StatisticsRow.kt:43`, `app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgRows.kt:106`.

### S13 — RPN statistics app row is incorrectly collapsed into StatisticsRow

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/list_item_rpn_stat_app.xml`.

Original: RPN app row has horizontal20dp/vertical7dp padding, 32dp icon, 14sp normal title, 11sp .7-alpha metadata and 13sp sans-serif-medium total; line limits are one.

Current: RpnStatsBottomSheet uses StatisticsRow with 16dp/8dp padding, 22dp icon in 40dp frame, bold title, bodySmall metadata and bodyMedium total. This is a different layout contract.

Before removal: Give RPN app statistics its own source-backed row variant and check long-name/large-font ellipsis rather than inherit summary-row defaults.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/bottomsheet/RpnStatsBottomSheet.kt:276`, `app/src/main/java/com/celzero/bravedns/ui/compose/statistics/StatisticsRow.kt:33`.

### S14 — Expanded blocklist-domain row loses geometry, type and overflow

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/list_item_blocklist_app_row.xml`, `layout/list_item_blocklist_domain_row.xml`.

Original: Domain row is minHeight40dp; padding start52/end46/top4/bottom4; 12sp primaryLightColorText single-line middle ellipsis; count minWidth32dp with 8dp start margin.

Current: BlocklistAppsAdapter.GroupAt uses start54/end16/top8/bottom8, raw Text with no explicit 12sp/color/single-line/middle ellipsis/min count width. App headers also inherit generic StatisticsRow.

Before removal: Restore app and expanded-domain variants separately, including row minimum, count alignment and middle-ellipsis behavior.

Evidence: `app/src/main/java/com/celzero/bravedns/adapter/BlocklistAppsAdapter.kt:58`.

### S15 — Purchase/history grouping and icon backgrounds changed

Priority: P0. Evidence: `confirmed_source_mismatch`.

Affected: `layout/activity_purchase_history.xml`, `layout/activity_server_order_history.xml`, `layout/list_item_purchase_history.xml`, `layout/list_item_server_order.xml`.

Original: History rows are horizontal icon-frame plus text stack within a shared parent list; payment icon24dp in 44dp neutral circle, server icon24dp in 32dp positive circle; per-node order/margins/dividers differ.

Current: HistoryRowContent renders a vertical Column with 28dp icon then all content, loses circle backgrounds and timestamp timer drawable, reorders badge/title/timestamp content and uses a generic divider. HistoryScreen wraps each item in an individual Card.

Before removal: Restore each row hierarchy and the shared parent-card/list grouping. Preserve control restoration inside it. tv_purchase_token is always GONE in baseline adapter; its non-rendering is not a missing-visible-content bug.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/purchase/HistoryRowContent.kt:47`, `app/src/main/java/com/celzero/bravedns/ui/compose/purchase/HistoryScreen.kt:96`.

### S16 — Loading skeleton layouts replaced by generic shapes

Priority: P0. Evidence: `confirmed_source_mismatch`.

Affected: `layout/activity_server_order_history.xml`, `layout/fragment_rethink_plus_premium.xml`, `layout/fragment_server_selection.xml`, `layout/list_item_shimmer_card.xml`, `layout/list_item_shimmer_server_row.xml`, `layout/shimmer_subscription_loading.xml`.

Original: Server-row include is a 16dp-corner card with icon36dp, text120×14/70×10 separated by6dp, trailing20dp square and explicit margins. Subscription/card skeletons have their own detailed arrangements.

Current: History uses PurchaseShimmer with four 120dp 2×2 blocks; RpnShimmer uses fractional-width lines. A running shimmer animation alone does not preserve source placeholder composition.

Before removal: Create native source-specific skeleton variants; keep each include fanout and shimmer timing/direction/alpha. Cover loading before final data, including stopped shimmer state.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/purchase/PurchasePlans.kt:57`, `app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnSelectionScreen.kt:232`.

### S17 — Add-server tile lost its dashed stroke and label typography

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/list_item_vpn_server_add.xml`.

Original: bg_vpn_server_add_tile uses 1dp border with dashWidth6dp/dashGap4dp and radius16dp; label11sp sans-serif-medium .02em tracking, one line. Tile minHeight80dp excludes outer margin6dp.

Current: RpnAddServerTile uses a solid border and plain Text; heightIn(80dp) precedes padding6dp, so margin consumes minimum allocation rather than remaining outside it.

Before removal: Paint the actual dashed drawable/shape in Compose and restore label contract and outer-margin/min-height relationship.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnServerTile.kt:104`, `app/src/main/res/drawable/bg_vpn_server_add_tile.xml:8`.

### S18 — Country selector and alphabet headers changed text metrics

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/item_section_header.xml`, `layout/item_server_group.xml`, `layout/list_item_country_card.xml`.

Original: Country flag12sp with includeFontPadding=false; server count11sp; code has mini size/bold/tracking. Header is11sp bold sans-serif-medium, .1em tracking, start/end4dp top14/bottom4dp.

Current: CountryServerAdapter.Country draws flag26sp and code10sp; headers use labelSmall with start8/bottom8 and omit source weight/family/tracking. Country card/title/count and group layouts share generic Text defaults.

Before removal: Restore exact country/header/group variants and source drawable/background geometry. Keep the already-restored favourite artwork and selection callbacks.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/adapter/CountryServerAdapter.kt:102`, `app/src/main/java/com/celzero/bravedns/ui/adapter/CountryServerAdapter.kt:89`.

### S19 — Rule status badges are generic filled surfaces instead of framed source badges

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/list_item_custom_domain.xml`, `layout/list_item_custom_ip.xml`, `layout/bottom_sheet_custom_domains.xml`, `layout/bottom_sheet_custom_ips.xml`.

Original: Source badge uses rectangle_border_background: radius16dp, .75dp border; list badges have minWidth25dp/padding5dp; detail badges are fixed32×32 with bold text.

Current: RuleStatusBadge uses MaterialTheme.shapes.small, no border, horizontal8/vertical4 padding and no 32dp detail variant or bold weight.

Before removal: Use source-specific list/detail badge variants with original drawable, min/fixed constraints and dynamic tint state. A badge is not covered merely because buttons/chips are restored.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/rules/RulesContent.kt:278`.

### S20 — Rule editor title/body/error geometry still uses generic dialog text

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/dialog_add_custom_domain.xml`, `layout/dialog_import_confirm.xml`, `layout/dialog_add_custom_ip.xml`.

Original: Editor title14sp plus XML10dp padding, description11sp and specified margins; error12sp with margin10dp/padding5dp. Dialog window background/theme is separate from layout content.

Current: RuleDialog defaults title20sp/titleLarge, padding20dp and spacedBy10dp; RuleEditor body/error texts use generic inherited sizing instead of XML sizes/paddings. Original fields/buttons do not restore surrounding text.

Before removal: Map dialog content and window separately; preserve original title/body/error typography and spacing. Independently compare actual themed window shape rather than assume extraLarge is correct.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/rules/RulesContent.kt:180`, `app/src/main/java/com/celzero/bravedns/ui/compose/rules/RulesContent.kt:184`.

### S21 — Bubble app rows lose source icon frame and name marquee

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/item_allowed_app.xml`, `layout/item_blocked_app.xml`.

Original: Rows use CardView with compat padding; allowed icon28dp within44dp circular frame; name one-line marquee and row child constraints; source content vertical padding10dp.

Current: BubbleRow uses generic Material3 Card, icon32dp with no44dp frame, content padding12dp and unbounded name text. Large-font button reflow is deliberate current behavior but is not baseline equivalence.

Before removal: Restore original card/icon/name constraints while preserving accessible actions. Document any authorized large-font alternate layout independently; default-font equality is still required.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/utilities/BubbleScreen.kt:84`, `app/src/main/java/com/celzero/bravedns/ui/compose/utilities/BubbleScreen.kt:99`.

### S22 — WireGuard text helper cannot carry several explicit text properties

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/activity_rpn_bypass_apps.xml`, `layout/activity_rpn_config_detail.xml`, `layout/activity_wg_detail.xml`, `layout/dialog_rpn_ssid.xml`, `layout/list_item_wg_hop.xml`, `layout/list_item_wg_include_apps.xml`.

Original: Examples: app include label sans-serif-medium16sp/.01em; description sans-serif12sp/.02em; RPN bypass heading sans-serif-medium18sp; SSID dialog title .01em.

Current: WgText handles color/size/maxLines/bold/italic but has no fontFamily/letterSpacing/textAppearance/line-height input. Shared renderer cannot reproduce those properties solely from IDs.

Before removal: Extend source-specific text style metadata and route every affected TextView to it, resolving textAppearance before explicit overrides. Keep state and callback code untouched.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgControls.kt:55`.

### S23 — RPN progress dialog status/hint text grew from original sizes

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/dialog_server_loading.xml`.

Original: Status12sp, hint10sp, title17sp bold sans-serif-medium; explicit margins and minLines2.

Current: RpnProgressContent status has no fontSize (inherits bodyLarge), hint uses bodySmall (12sp), and title does not resolve sans-serif-medium. Most geometry is present but typography differs.

Before removal: Use explicit source text style values for these nodes and retain independent progress decoration verification.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnComponents.kt:76`, `app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnComponents.kt:80`.

### S24 — Tabbed hosts do not resolve TabbedLayoutDefault

Priority: P1. Evidence: `confirmed_source_omission_runtime_effect_pending`.

Affected: `layout/activity_custom_rules.xml`, `layout/activity_network_logs.xml`.

Original: Both TabLayouts explicitly use @style/TabbedLayoutDefault; selected text, unselected text, indicator/background, ripple and label appearance are part of that style.

Current: CustomRulesActivity and NetworkLogsActivity call raw TabRow/Tab. The style resource is not consumed by their native tab components.

Before removal: Map all TabbedLayoutDefault properties to a shared source-backed native tab strip and preserve tab height/selection/scrolling/indicator in both hosts.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/activity/CustomRulesActivity.kt:127`, `app/src/main/java/com/celzero/bravedns/ui/activity/NetworkLogsActivity.kt:127`.

### S25 — Checkbox/selection visuals need a separate source-style census

Priority: P1. Evidence: `unresolved_source_style_mapping`.

Affected: `layout/activity_tcp_proxy.xml`, `layout/dialog_set_custom_doh.xml`, `layout/dialog_set_dns_proxy.xml`, `layout/list_item_custom_all_domain.xml`, `layout/list_item_custom_all_ip.xml`, `layout/list_item_custom_domain.xml`, `layout/list_item_custom_ip.xml`, `layout/list_item_rethink_blocklist_simple.xml`, `layout/activity_rpn_bypass_apps.xml`, `layout/bottom_sheet_bug_report_files.xml`, `layout/bottomsheet_server_settings.xml`, `layout/dialog_set_proxy.xml`, `layout/dialog_wg_apps.xml`, `layout/item_bug_report_file.xml`, `layout/item_dns_record_type.xml`, `layout/item_server.xml`, `layout/item_server_group.xml`, `layout/list_item_exclude_country.xml`, `layout/list_item_rethink_blocklist_adv.xml`, `layout/list_item_wg_hop.xml`, `layout/list_item_wg_include_apps.xml`, `layout/list_item_wg_one_interface.xml`.

Original: Baseline contains 20 AppCompatCheckBox, 8 MaterialCheckBox and multiple explicit tints, minimum sizes and widget theme choices; source selection semantics and duplicate row/control targets also differ.

Current: Current native implementations include CheckboxDefaults/default Checkbox and some custom tint helpers. The previous 213-variant control census covered button/chip/field/icon families, not full checkbox parity.

Before removal: Account for every checkbox node and its actual native invocation; compare source-resolved checked/unchecked/indeterminate/disabled colors, shape/size and semantic grouping. Do not broadly replace the tint-preserving cases.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgControls.kt:104`, `app/src/main/java/com/celzero/bravedns/ui/compose/blocklists/BlocklistConfigurationScreen.kt:166`.

### S26 — Fixed visual size versus minimum/touch size must be proven for restored controls

Priority: P1. Evidence: `confirmed_constraint_difference_runtime_measurement_required`.

Affected: `layout-w600dp/fragment_home_screen.xml`, `layout/activity_app_details.xml`, `layout/fragment_home_screen.xml`, `layout/fragment_sponsor.xml`, `layout/fragment_sponsor_success.xml`, `layout/item_allowed_app.xml`, `layout/item_blocked_app.xml`, `layout/view_home_logs_activity.xml`, `layout/view_home_logs_apps.xml`, `layout/activity_customer_support.xml`, `layout/activity_ping_test.xml`, `layout/bottom_sheet_summary_stats_filter.xml`, `layout/bottomsheet_auto_exclude_countries.xml`, `layout/bottomsheet_device_auth_error.xml`, `layout/bottomsheet_device_not_registered.xml`, `layout/bottomsheet_entitlement_detail.xml`, `layout/bottomsheet_purchase_conflict.xml`, `layout/bottomsheet_purchase_processing.xml`, `layout/bottomsheet_rpn_stats.xml`, `layout/bottomsheet_server_removal_notification.xml`, `layout/bottomsheet_server_settings.xml`, `layout/dialog_rpn_ssid.xml`, `layout/fragment_rethink_plus_premium.xml`, `layout/fragment_server_selection.xml`, `layout/fragment_summary_statistics.xml`, `layout/item_bug_report_file.xml`, `layout/item_server_group.xml`, `layout/view_tour_tooltip.xml`.

Original: XML layout_height=fixed, minHeight, style/chipMinHeight, inset and parent constraints have distinct precedence. Some source chips are compact and some opt out of minimum touch target.

Current: OriginalControl stores fixed visualHeight but renders it with heightIn(min=look.height); interactive Box adds minimumInteractiveComponentSize and propagation. The surrounding layout can therefore allocate/grow differently even when colors and radius are correct.

Before removal: Keep all existing per-instance style keys. Test outer/touch/visual/content bounds independently at default and large fonts; resolve fixed/min/parent precedence and source touch-target rules before accepting a layout.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/controls/OriginalControls.kt:124`, `app/src/main/java/com/celzero/bravedns/ui/compose/controls/OriginalControls.kt:122`.

### S27 — App connection rows omit source line/weight/font distinctions

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/list_item_app_domain_details.xml`, `layout/list_item_app_ip_details.xml`, `layout/list_item_app_country_details.xml`.

Original: Each layout supplies distinct line limits, ellipsis, font family, source sizes and count/flag/progress alignment. Country is not identical to domain/IP.

Current: AppConnectionRow flattens three types into shared14sp title/12sp subtitle/16sp count; Text does not apply node textStyle/maxLines/ellipsize. Generic count/font and row padding replace original attrs.

Before removal: Make per-layout row variants or pass complete source contracts; preserve current progress explicit colors and source-specific icon/flag states.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/appdetails/AppConnectionRows.kt:28`, `app/src/main/java/com/celzero/bravedns/ui/compose/appdetails/AppConnectionRows.kt:34`.

### S28 — Endpoint flag minimum frame was lost

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/dns_crypt_endpoint_list_item.xml`, `layout/dns_proxy_list_item.xml`, `layout/list_item_endpoint.xml`.

Original: list_item_endpoint endpoint_flag_text has minWidth32dp/minHeight32dp, centered25sp text, marginEnd5dp; endpoint rows also inherit card compat styling.

Current: DnsEndpointRow flag is raw Text with end5dp and25sp size, without the source minimum frame or centered text. Protocol layouts need separate attribute comparison rather than a generic endpoint equivalence assumption.

Before removal: Preserve flag slot constraints per protocol variant and independently resolve card/tint/selection artwork contracts.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/dns/DnsEndpointScreen.kt:140`.

### S29 — DNS record-mode controls bypass their existing original-control catalog

Priority: P0. Evidence: `confirmed_source_mismatch`.

Affected: `layout/bottom_sheet_dns_record_types.xml`.

Original: Both buttons use toggleButtonStyle, minWidth125dp and cornerRadius16dp within a single-selection MaterialButtonToggleGroup; source style adds accent stroke and state fill/text.

Current: DnsRecordTypesContent draws two independent Surface/Text controls, with no source stroke/minWidth/fused inner-corner semantics. Keys already exist in OriginalControlCatalog but are unused by these nodes.

Before removal: Use exact existing catalog variants with segmentIndex/count, original dynamic tints, selection callbacks and test tags. Extend integration guard to catch manually painted source controls.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/about/DnsRecordTypesContent.kt:67`, `app/src/main/java/com/celzero/bravedns/ui/compose/controls/OriginalControlCatalog.kt:84`.

### S30 — Block Free DNS surrounding text and URL spacing differ

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/activity_block_free_dns.xml`, `layout/list_item_block_free_dns.xml`.

Original: Description/selected/empty text12sp, item name14sp bold, item URL12sp with2dp top margin; heading uses explicit TitleSmall.Emphasized style.

Current: BlockFreeDnsContent uses bodyMedium14sp for description and default Text for selected/empty/name/URL (bodyLarge16sp), and URL top10dp. Cards are manually flattened surfaces.

Before removal: Retain restored filter/type-chip keys, but restore explicit surrounding text styles and URL margin; map the original MaterialCard shapes/elevation/insets separately.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/dns/BlockFreeDnsContent.kt:100`.

### S31 — Filter-sheet heading style and drag-handle geometry differ

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/bottom_sheet_firewall_sort_filter.xml`, `layout/bottom_sheet_rethink_plus_filter.xml`.

Original: Filter headings14sp secondaryTextColor with original margin/padding; handle60×3dp border color and10dp/20dp vertical margins.

Current: FirewallFilterContent uses16sp default-color headings, a generic HorizontalDivider color and shared20dp/spacedBy10dp arrangement. BlocklistFilterScreen heading has no explicit14sp size.

Before removal: Restore exact heading and handle variants while retaining original-control chip/button selection behavior.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/firewallapps/FirewallFilterContent.kt:40`, `app/src/main/java/com/celzero/bravedns/ui/compose/blocklists/BlocklistFilterScreen.kt:33`.

### S32 — Interval sheet captions and expandable row overflow differ

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/bottom_sheet_log_activity_interval.xml`, `layout/item_log_activity_app.xml`, `layout/item_log_activity_conn.xml`.

Original: Count labels and empty message11sp; app name maxLines1/end ellipsis; connection label maxLines1/end ellipsis/selectable. Card styles/elevation are independent of text.

Current: IntervalCount and empty message use12sp. AppActivityNativeRow Text for name and entry label does not constrain maxLines/overflow, allowing a longer row than XML.

Before removal: Restore source11sp captions and line limits/overflow; preserve source row reading/selection and original range buttons.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/home/LogIntervalContent.kt:113`, `app/src/main/java/com/celzero/bravedns/ui/compose/home/LogIntervalContent.kt:155`.

### S33 — Detailed-statistics/domain headers lose original typography

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/activity_detailed_statistics.xml`, `layout/activity_domain_connections.xml`.

Original: Brand Heading.HomeScreen is all-caps sans-serif-smallcaps with .25em tracking and rethink_header_text size. Title16sp smallcaps with5dp extra line spacing; subtitle12sp primaryLightColorText.

Current: PagedStatisticsScreen uses headlineLarge for brand, titleLarge for heading and bodyMedium for subtitle, dropping source family/tracking/caps/colors and sizes. Selectable domain title behavior is preserved.

Before removal: Resolve the original named style and explicit title/subtitle overrides; keep header constraints/empty-state alignment and existing selection behavior.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/statistics/PagedStatisticsScreen.kt:30`, `app/src/main/java/com/celzero/bravedns/ui/compose/statistics/PagedStatisticsScreen.kt:34`.

### S34 — DNS editor layout loses source invisible error reservation and insets

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/dialog_set_custom_doh.xml`, `layout/dialog_set_custom_odoh.xml`, `layout/dialog_set_dns_crypt.xml`.

Original: DoH/ODoH failure TextView starts INVISIBLE, retaining measurement; its10dp margins/5dp padding differ from DNSCrypt2dp error padding. Titles have10dp padding and each layout has own field arrangement.

Current: Shared DnsEditorDialog uses20dp content padding/spacedBy8; DnsError emits nothing without error and has no layout-specific padding. Field restoration alone does not preserve parent/error geometry.

Before removal: Model invisible versus gone error states and per-editor content insets without changing validation/callback behavior. Compare dialog window background separately from layout content.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/dns/DnsEndpointEditors.kt:169`, `app/src/main/java/com/celzero/bravedns/ui/compose/dns/DnsEndpointEditors.kt:137`.

### S35 — Smart DNS header description is 14sp instead of11sp

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/activity_configure_other_dns.xml`, `layout/activity_smart_dns_list.xml`.

Original: Shared title references the concrete TextAppearance.Material3.TitleMedium resource; Smart DNS description is small_font_subheading_text=11sp with5dp padding.

Current: DnsFragmentHeader uses theme titleMedium (not exact direct style resource) and explicit14sp for Smart DNS description. Margins/padding are otherwise closely matched.

Before removal: Resolve the explicit title style and11sp description with existing typography helper; retain native framework host and fixed IDs.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/dnssettings/DnsFragmentHost.kt:40`.

### S36 — Removed-server rows and ping-history rows lose text metrics

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/item_ping_test_history.xml`, `layout/item_removed_server.xml`.

Original: Removed-server flag24sp in40dp frame, name16sp bold/maxLines1, location13sp/maxLines1. Ping-history targets14.5sp medium, latency13sp bold medium, outcome11.5sp medium; all are single-line.

Current: RpnRemovalContent flag28sp with no frame and generic name/bodySmall location; PingHistoryRow resolves only numeric sizes and leaves family/weight/line limits unset.

Before removal: Give each row its own complete source text/frame contract; preserve current data and callback logic and disclose large-font alternate layout separately.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnRemovalContent.kt:32`, `app/src/main/java/com/celzero/bravedns/adapter/PingTestHistoryAdapter.kt:35`.

### S37 — About brand title uses fixed SP tracking instead of source EM/style

Priority: P1. Evidence: `confirmed_source_mismatch`.

Affected: `layout/fragment_about.xml`.

Original: TextAppearance.Heading.HomeScreen specifies sans-serif-smallcaps, .25em tracking, all-caps and rethink_header_text dimension.

Current: AboutContent uses SansSerif and letterSpacing7.sp; dimensionResource(...).value.sp is not a source text-style resolver and can double-apply scaled values under nonlinear font scaling.

Before removal: Resolve original named TextAppearance and explicit dynamic text/click state through the existing typography resolver; keep content and source accessibility behavior.

Evidence: `app/src/main/java/com/celzero/bravedns/ui/compose/about/AboutContent.kt:46`.
