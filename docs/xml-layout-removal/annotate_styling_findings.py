#!/usr/bin/env python3
"""Attach manually reviewed findings to the exhaustive source-contract ledger."""
import collections,json,pathlib,re,subprocess
ROOT=pathlib.Path(__file__).resolve().parents[2]; D=ROOT/'docs/xml-layout-removal'; P='app/src/main/java/com/celzero/bravedns/ui/'
j=json.loads((D/'styling-equivalence-ledger.json').read_text()); rows=j['layouts']; findings=[]
def evidence(path,needle,baseline=False):
 p=path if path.startswith('app/') else P+path+'.kt'
 rev=j['baseline'] if baseline else j['native_snapshot']
 s=subprocess.check_output(['git','-C',str(ROOT),'show',f'{rev}:{p}'],text=True)
 at=s.find(needle)
 if at<0:raise ValueError((p,needle))
 return {'path':p,'line':s[:at].count('\n')+1,'excerpt':needle,'snapshot':rev}
def add(code,title,stems,source_contract,current_difference,remediation,refs=(),kind='confirmed_source_mismatch',priority='P1',pred=None):
 targets=[r for r in rows if (r['basename'] in stems.split() if stems else pred and pred(r))]
 f={'id':code,'priority':priority,'status':kind,'title':title,'layout_paths':[r['layout_path'] for r in targets],'baseline_contract':source_contract,'current_source_difference':current_difference,'required_before_deletion':remediation,'source_evidence':[evidence(*ref) for ref in refs],'verification':'Source only; screenshots/measurement/TalkBack/actual-host execution not run.'}
 if not targets:raise ValueError(code)
 findings.append(f)
 for r in targets:r['specific_findings'].append(code)
add('S01','Wide Home logs card no longer reaches the shared bottom barrier','fragment_home_screen',
 'w600dp logs is 0dp height, top below throughput, bottom constrained to fhs_row2_bottom_barrier over proxy/apps; outer gutters remain 2dp/8dp/10dp and 12dp vertical separation.',
 'HomeContent uses independent .58/.42 Column heights. LogsCard has content-driven height and cannot share the proxy/apps bottom boundary. This is separate from the already-restored compat gutters.',
 'Restore the cross-column measurement relationship in native Compose, and assert logs/apps outer bottom bounds for normal/long/hidden-content states. Preserve existing navigation-clearance and gutter fixes.',
 [('compose/home/HomeContent','Column(Modifier.weight(.58f)'),('app/src/full/res/layout-w600dp/fragment_home_screen.xml','app:layout_constraintBottom_toBottomOf="@id/fhs_row2_bottom_barrier"',True)],priority='P0',pred=None)
# Only wide variant is affected by the barrier.
findings[-1]['layout_paths']=[p for p in findings[-1]['layout_paths'] if '/layout-w600dp/' in p]
for r in rows:
 if r['basename']=='fragment_home_screen' and r['qualifier']=='layout':r['specific_findings'].remove('S01')
add('S02','Home apps metric grid and qualifier-specific grouping collapsed','fragment_home_screen',
 'The wide layout puts count/title above an aligned Flow with maxElementsWrap=2, horizontal gap 12dp and vertical gap 6dp. ConstraintFlow owns blocked, isolated, bypassed, excluded order. Phone and wide have separate geometry.',
 'AppsCard shares one FlowRow for both qualifiers and nests two Columns in a Row; count/title and metrics may become siblings on one row, and row alignment/wrap is not the original 2×2 flow.',
 'Implement explicit phone and wide grid grouping from the two XML contracts. Assert row/column placement, wrap and reading order with long translated metrics.',
 [('compose/home/HomeContent','FlowRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)'),('app/src/full/res/layout-w600dp/fragment_home_screen.xml','app:flow_maxElementsWrap="2"',True)],priority='P0')
add('S03','Home fixed heights are now minimums with different padding constraints','fragment_home_screen',
 'Protection bar, firewall rules card and logs pager have fixed XML dimensions (40dp, wide rules 80dp, pager 168dp wide/190dp phone); card clearance consumes part of outer card constraints. NestedScrollView fillViewport=true is also explicit.',
 'ProtectionRow, RulesCard and NativePager use heightIn(min=...), while parent wrapping and padding order differ. Rules minimum is applied inside HomeCard after outside clearance, not to the original outer card. Scrollable Column does not explicitly reproduce fillViewport.',
 'Record outer allocated height versus visible surface versus content minimum for each state, then match default-font geometry without accidental double insets. Decide/validate large-text growth explicitly rather than call min-height equivalent to fixed height.',
 [('compose/home/HomeContent','heightIn(min = if (wide) 80.dp else 84.dp)'),('compose/home/HomeContent','heightIn(min = if (wide) 168.dp else 190.dp)')],priority='P0')
add('S04','Home card elevation space restored but shadow is not rendered','fragment_home_screen',
 'Default Widget.Rethink.MaterialCardView has 2dp elevation and compat padding; Frost has zero elevation; rules explicitly zero.',
 'HomeCard uses calculated padding then clip/background/border. It does not draw a shadow or an elevated Surface/Card. The restored gap is not the full elevation treatment.',
 'Preserve theme-dependent shadow rendering as well as measured outside clearance. Avoid duplicating compat padding or adding shadows to zero-elevation rules/Frost.',
 [('compose/home/HomeContent','var m = modifier.homeTourAnchor(node.id).padding(rememberHomeCardPadding'),('compose/home/HomeCardSpacing','fun rememberHomeCardPadding')])
add('S05','Home typography loses source tracking, truncation and autosize','fragment_home_screen item_home_firewall_rules_page view_home_logs_activity view_home_logs_apps',
 'Source labels use letterSpacing (.02/.05), maxLines/ellipsize and several uniform autosize ranges (rules 8–12sp; proxy labels 6–8sp). Home text size/color/alpha are not the complete contract.',
 'HomeText accepts size/color/monospace/allCaps but no letterSpacing, maxLines, ellipsize or source font weight; RulesCard and proxy labels use unbounded Text with fixed sizes.',
 'Carry actual baseline text contracts into presentation-only text rendering, retaining default-font alignment and source-specific overflow. Add narrow/RTL/large-font fixtures for rules, proxy legend and apps labels.',
 [('compose/home/HomeContent','private fun HomeText('),('compose/home/HomeContent','Text(pages.labels[page]'),('app/src/full/res/layout/item_home_firewall_rules_page.xml','app:autoSizeMinTextSize="8sp"',True)])
add('S06','Settings/DNS cards still omit inherited compat-padding geometry','',
 'MaterialCardView inherits Widget.Rethink.MaterialCardView/Frost, including cardUseCompatPadding=true; explicit per-card radius/stroke/content padding and margins apply on top.',
 'SettingsCard resolves fill/radius/elevation only, then draws a Compose Card to full allocated bounds. It does not resolve compat padding, prevent-corner-overlap, max elevation, card stroke or explicit Android foreground.',
 'Create a source-style-aware native card primitive and map each call to its actual style plus per-card overrides. Reconcile outer clearance separately from content padding, including zero-elevation Frost.',
 [('compose/settings/SettingsComponents','intArrayOf(CardR.attr.cardBackgroundColor, CardR.attr.cardCornerRadius, CardR.attr.cardElevation)'),('compose/settings/SettingsComponents','Card(modifier.fillMaxWidth()')],pred=lambda r:any('SettingsCard(' in subprocess.check_output(['git','-C',str(ROOT),'show',j['native_snapshot']+':'+x['path']],text=True) for x in r['renderer_mapping']) and bool(r['implicit_widget_style_obligations']))
add('S07','WireGuard card wrapper flattens per-layout padding and row grouping','list_item_wg_general_interface list_item_wg_one_interface list_item_wg_hop list_item_wg_include_apps list_item_wg_peers activity_wireguard_main activity_wg_detail activity_wg_config_editor dialog_wg_add_peer',
 'WireGuard cards and their children have individually specified radii, margins, padding and constrained subrows; uptime/rx-tx are peers in a RelativeLayout in interface rows.',
 'WgCard always creates an inner Column with 16dp padding and 8dp spacing and no compat padding. WgRows stacks uptime and rx-tx separately instead of their original shared row. WgAppRow omits its source icon-container geometry.',
 'Use per-card contracts rather than one content-padding default. Restore explicit subrow grouping, hidden-child spacing and modifier order for each WireGuard layout.',
 [('compose/wireguard/WgControls','Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)'),('compose/wireguard/WgRows','if (b.interfaceActiveLayout.isVisible) { WgText(b.interfaceActiveUptime); WgText(b.interfaceActiveRxTx) }')])
add('S08','Raw switches bypass the original theme overlays','activity_advanced_setting activity_customer_support activity_tcp_proxy bottomsheet_server_settings fragment_dns_configure activity_app_details bottom_sheet_conn_track',
 'These XML controls specify CustomWidget.Material3.CompoundButton.MaterialSwitch, whose CustomCompoundButtonSwitch overlay overrides colorSurface/colorOnSurface/colorControlActivated and secondary colors; checked/unchecked/disabled states matter.',
 'Their native calls use raw Switch defaults without the source overlay. RethinkTheme global colors do not apply an individual widget materialThemeOverlay.',
 'Route through a native switch renderer that resolves the particular XML style/overlay and its state colors/dimensions. Existing wgSwitchColors is useful partial infrastructure but not a blanket equivalence exemption.',
 [('compose/settings/AdvancedSettingsContent','Switch(b.dvExperimentalSwitch.isChecked'),('compose/utilities/SupportScreen','Switch(state.isChecked'),('compose/rpn/RpnSettingsContent','Switch(toggle.isChecked')])
add('S09','Radio controls have unreviewed source-style/default differences','activity_anti_censorship bottom_sheet_orbot dialog_set_dns_proxy dialog_rpn_ssid dialog_wg_ssid activity_checkout_proxy',
 'Baseline mixes MaterialRadioButton, AppCompatRadioButton and RadioButton, sometimes with explicit CustomCompoundButtonSwitch overlay, tint or padding. These are not a single global Material3 radio style.',
 'Native renderers mostly use RadioButton defaults. AntiCensorshipRow does not resolve the explicit overlay applied to all nine XML radios.',
 'Resolve source widget defaults/overlays and checked/unchecked/disabled states per family. Preserve selection group semantics and avoid duplicate targets when both row and control are clickable.',
 [('compose/settings/AntiCensorshipContent','RadioButton(radio.isChecked'),('compose/purchase/CheckoutContent','RadioButton(selected == index, onClick = null)')],kind='confirmed_source_omission_runtime_effect_pending')
add('S10','SeekBar/Slider replacement does not preserve the source widget appearance','activity_advanced_setting activity_tunnel_settings activity_misc_settings fragment_sponsor',
 'Baseline uses AppCompatSeekBar for several settings and a separately styled Slider for sponsorship; thumb/track/ticks/min-height arise from different widget defaults.',
 'Advanced/Network sources use generic Compose Slider, including its visual track/thumb/tick system, without source style/drawable resolution; callback preservation does not preserve visual form.',
 'Map each source slider type/attributes to an explicit native visual contract. Resolve external widget defaults in independent reference fixtures before deleting XML.',
 [('compose/settings/AdvancedSettingsContent','Slider(b.goMaxMemorySeekbar.progress.toFloat()'),('compose/networksettings/NetworkNativeUi','NetworkKind.SLIDER -> Slider')],kind='confirmed_source_omission_runtime_effect_pending')
add('S11','Progress defaults replace explicit source track/color contracts','activity_server_order_history dialog_server_loading list_item_wg_hop activity_wireguard_main fragment_wg_nw_stats',
 'Server-order progress is 3dp, square-cornered, accentGood on chipBgColorPositive. Server-loading timeout is 3dp with 4dp corners. Other XML progress nodes retain per-instance tints and thickness.',
 'HistoryScreen and WgProgress use default LinearProgressIndicator parameters; server-loading does not set source track corner/gap/stop behavior. Current Compose defaults are not verified against original MDC defaults.',
 'Map determinate/indeterminate progress per original node, including track, thickness, corner shape, gap/stop decorations, insets and accessible range.',
 [('compose/purchase/HistoryScreen','if (state.fetchProgress.isVisible) LinearProgressIndicator'),('compose/wireguard/WgControls','if (node.isIndeterminate) LinearProgressIndicator'),('compose/rpn/RpnComponents','androidx.compose.material3.LinearProgressIndicator(progress')])
add('S12','StatisticsRow no longer matches the source summary row text contract','list_item_statistics_summary',
 'Summary title 14sp bold single-line ellipsis; subtitle 11sp single-line ellipsis with 2dp top margin; count 12sp with .85 alpha; icon22dp in 40dp frame.',
 'StatisticsRow uses bodySmall (12sp) for subtitle and bodyMedium (14sp) for count; text has no maxLines/ellipsis; subtitle loses 2dp top margin. WgStatsRow also reorders data/name and has its own incompatible geometry.',
 'Preserve distinct summary/variant row contracts. Reintroduce exact sizes, line limits and baseline margins; do not use the same generic row for layouts with different metrics.',
 [('compose/statistics/StatisticsRow','if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall'),('compose/statistics/StatisticsRow','Text(count, Modifier.alpha(.85f), style = MaterialTheme.typography.bodyMedium)'),('compose/wireguard/WgRows','Column(Modifier.weight(1f)) { WgText(b.ssName); WgText(b.ssDataUsage) }')])
add('S13','RPN statistics app row is incorrectly collapsed into StatisticsRow','list_item_rpn_stat_app',
 'RPN app row has horizontal20dp/vertical7dp padding, 32dp icon, 14sp normal title, 11sp .7-alpha metadata and 13sp sans-serif-medium total; line limits are one.',
 'RpnStatsBottomSheet uses StatisticsRow with 16dp/8dp padding, 22dp icon in 40dp frame, bold title, bodySmall metadata and bodyMedium total. This is a different layout contract.',
 'Give RPN app statistics its own source-backed row variant and check long-name/large-font ellipsis rather than inherit summary-row defaults.',
 [('app/src/main/java/com/celzero/bravedns/ui/bottomsheet/RpnStatsBottomSheet.kt','StatisticsRow('),('compose/statistics/StatisticsRow','Box(Modifier.size(40.dp)')])
add('S14','Expanded blocklist-domain row loses geometry, type and overflow','list_item_blocklist_domain_row list_item_blocklist_app_row',
 'Domain row is minHeight40dp; padding start52/end46/top4/bottom4; 12sp primaryLightColorText single-line middle ellipsis; count minWidth32dp with 8dp start margin.',
 'BlocklistAppsAdapter.GroupAt uses start54/end16/top8/bottom8, raw Text with no explicit 12sp/color/single-line/middle ellipsis/min count width. App headers also inherit generic StatisticsRow.',
 'Restore app and expanded-domain variants separately, including row minimum, count alignment and middle-ellipsis behavior.',
 [('app/src/main/java/com/celzero/bravedns/adapter/BlocklistAppsAdapter.kt','Row(Modifier.fillMaxWidth().padding(start = 54.dp, end = 16.dp, top = 8.dp, bottom = 8.dp))')])
add('S15','Purchase/history grouping and icon backgrounds changed','list_item_purchase_history list_item_server_order activity_purchase_history activity_server_order_history',
 'History rows are horizontal icon-frame plus text stack within a shared parent list; payment icon24dp in 44dp neutral circle, server icon24dp in 32dp positive circle; per-node order/margins/dividers differ.',
 'HistoryRowContent renders a vertical Column with 28dp icon then all content, loses circle backgrounds and timestamp timer drawable, reorders badge/title/timestamp content and uses a generic divider. HistoryScreen wraps each item in an individual Card.',
 'Restore each row hierarchy and the shared parent-card/list grouping. Preserve control restoration inside it. tv_purchase_token is always GONE in baseline adapter; its non-rendering is not a missing-visible-content bug.',
 [('compose/purchase/HistoryRowContent','Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp))'),('compose/purchase/HistoryScreen','items(count, key = key) { index -> Card(')],priority='P0')
add('S16','Loading skeleton layouts replaced by generic shapes','list_item_shimmer_server_row list_item_shimmer_card shimmer_subscription_loading activity_server_order_history fragment_server_selection fragment_rethink_plus_premium',
 'Server-row include is a 16dp-corner card with icon36dp, text120×14/70×10 separated by6dp, trailing20dp square and explicit margins. Subscription/card skeletons have their own detailed arrangements.',
 'History uses PurchaseShimmer with four 120dp 2×2 blocks; RpnShimmer uses fractional-width lines. A running shimmer animation alone does not preserve source placeholder composition.',
 'Create native source-specific skeleton variants; keep each include fanout and shimmer timing/direction/alpha. Cover loading before final data, including stopped shimmer state.',
 [('compose/purchase/PurchasePlans','repeat(2) { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { repeat(2)'),('compose/rpn/RpnSelectionScreen','private fun RpnShimmer(')],priority='P0')
add('S17','Add-server tile lost its dashed stroke and label typography','list_item_vpn_server_add',
 'bg_vpn_server_add_tile uses 1dp border with dashWidth6dp/dashGap4dp and radius16dp; label11sp sans-serif-medium .02em tracking, one line. Tile minHeight80dp excludes outer margin6dp.',
 'RpnAddServerTile uses a solid border and plain Text; heightIn(80dp) precedes padding6dp, so margin consumes minimum allocation rather than remaining outside it.',
 'Paint the actual dashed drawable/shape in Compose and restore label contract and outer-margin/min-height relationship.',
 [('compose/rpn/RpnServerTile','heightIn(min = 80.dp).padding(6.dp).border(1.dp'),('app/src/main/res/drawable/bg_vpn_server_add_tile.xml','android:dashWidth="6dp"',True)])
add('S18','Country selector and alphabet headers changed text metrics','list_item_country_card item_section_header item_server_group',
 'Country flag12sp with includeFontPadding=false; server count11sp; code has mini size/bold/tracking. Header is11sp bold sans-serif-medium, .1em tracking, start/end4dp top14/bottom4dp.',
 'CountryServerAdapter.Country draws flag26sp and code10sp; headers use labelSmall with start8/bottom8 and omit source weight/family/tracking. Country card/title/count and group layouts share generic Text defaults.',
 'Restore exact country/header/group variants and source drawable/background geometry. Keep the already-restored favourite artwork and selection callbacks.',
 [('adapter/CountryServerAdapter','Text(item.flagEmoji, fontSize = 26.sp)'),('adapter/CountryServerAdapter','if (letter != previous) Text(letter, Modifier.padding(start = 8.dp, top = 14.dp, bottom = 8.dp)')])
add('S19','Rule status badges are generic filled surfaces instead of framed source badges','list_item_custom_domain list_item_custom_ip bottom_sheet_custom_domains bottom_sheet_custom_ips',
 'Source badge uses rectangle_border_background: radius16dp, .75dp border; list badges have minWidth25dp/padding5dp; detail badges are fixed32×32 with bold text.',
 'RuleStatusBadge uses MaterialTheme.shapes.small, no border, horizontal8/vertical4 padding and no 32dp detail variant or bold weight.',
 'Use source-specific list/detail badge variants with original drawable, min/fixed constraints and dynamic tint state. A badge is not covered merely because buttons/chips are restored.',
 [('compose/rules/RulesContent','Surface(color = bg, contentColor = fg, shape = MaterialTheme.shapes.small)')])
add('S20','Rule editor title/body/error geometry still uses generic dialog text','dialog_add_custom_domain dialog_add_custom_ip dialog_import_confirm',
 'Editor title14sp plus XML10dp padding, description11sp and specified margins; error12sp with margin10dp/padding5dp. Dialog window background/theme is separate from layout content.',
 'RuleDialog defaults title20sp/titleLarge, padding20dp and spacedBy10dp; RuleEditor body/error texts use generic inherited sizing instead of XML sizes/paddings. Original fields/buttons do not restore surrounding text.',
 'Map dialog content and window separately; preserve original title/body/error typography and spacing. Independently compare actual themed window shape rather than assume extraLarge is correct.',
 [('compose/rules/RulesContent','titleSize: TextUnit = 20.sp'),('compose/rules/RulesContent','Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp))')])
add('S21','Bubble app rows lose source icon frame and name marquee','item_allowed_app item_blocked_app',
 'Rows use CardView with compat padding; allowed icon28dp within44dp circular frame; name one-line marquee and row child constraints; source content vertical padding10dp.',
 'BubbleRow uses generic Material3 Card, icon32dp with no44dp frame, content padding12dp and unbounded name text. Large-font button reflow is deliberate current behavior but is not baseline equivalence.',
 'Restore original card/icon/name constraints while preserving accessible actions. Document any authorized large-font alternate layout independently; default-font equality is still required.',
 [('compose/utilities/BubbleScreen','UtilityImage(icon, Modifier.size(32.dp))'),('compose/utilities/BubbleScreen','Column(Modifier.padding(12.dp))')])
add('S22','WireGuard text helper cannot carry several explicit text properties','list_item_wg_include_apps list_item_wg_hop activity_rpn_bypass_apps activity_rpn_config_detail activity_wg_detail dialog_rpn_ssid',
 'Examples: app include label sans-serif-medium16sp/.01em; description sans-serif12sp/.02em; RPN bypass heading sans-serif-medium18sp; SSID dialog title .01em.',
 'WgText handles color/size/maxLines/bold/italic but has no fontFamily/letterSpacing/textAppearance/line-height input. Shared renderer cannot reproduce those properties solely from IDs.',
 'Extend source-specific text style metadata and route every affected TextView to it, resolving textAppearance before explicit overrides. Keep state and callback code untouched.',
 [('compose/wireguard/WgControls','@Composable fun WgText(')])
add('S23','RPN progress dialog status/hint text grew from original sizes','dialog_server_loading',
 'Status12sp, hint10sp, title17sp bold sans-serif-medium; explicit margins and minLines2.',
 'RpnProgressContent status has no fontSize (inherits bodyLarge), hint uses bodySmall (12sp), and title does not resolve sans-serif-medium. Most geometry is present but typography differs.',
 'Use explicit source text style values for these nodes and retain independent progress decoration verification.',
 [('compose/rpn/RpnComponents','Text(state.status.text.toString()'),('compose/rpn/RpnComponents','style = MaterialTheme.typography.bodySmall, textAlign')])
add('S24','Tabbed hosts do not resolve TabbedLayoutDefault','activity_custom_rules activity_network_logs',
 'Both TabLayouts explicitly use @style/TabbedLayoutDefault; selected text, unselected text, indicator/background, ripple and label appearance are part of that style.',
 'CustomRulesActivity and NetworkLogsActivity call raw TabRow/Tab. The style resource is not consumed by their native tab components.',
 'Map all TabbedLayoutDefault properties to a shared source-backed native tab strip and preserve tab height/selection/scrolling/indicator in both hosts.',
 [('activity/CustomRulesActivity','TabRow(pager.currentPage)'),('activity/NetworkLogsActivity','TabRow(')],kind='confirmed_source_omission_runtime_effect_pending')
add('S25','Checkbox/selection visuals need a separate source-style census','',
 'Baseline contains 20 AppCompatCheckBox, 8 MaterialCheckBox and multiple explicit tints, minimum sizes and widget theme choices; source selection semantics and duplicate row/control targets also differ.',
 'Current native implementations include CheckboxDefaults/default Checkbox and some custom tint helpers. The previous 213-variant control census covered button/chip/field/icon families, not full checkbox parity.',
 'Account for every checkbox node and its actual native invocation; compare source-resolved checked/unchecked/indeterminate/disabled colors, shape/size and semantic grouping. Do not broadly replace the tint-preserving cases.',
 [('compose/wireguard/WgControls','} ?: CheckboxDefaults.colors()'),('compose/blocklists/BlocklistConfigurationScreen','Checkbox(checked, select')],kind='unresolved_source_style_mapping',pred=lambda r:any(n['tag'] in {'AppCompatCheckBox','MaterialCheckBox','CheckBox'} for n in r['nodes']))
add('S26','Fixed visual size versus minimum/touch size must be proven for restored controls','',
 'XML layout_height=fixed, minHeight, style/chipMinHeight, inset and parent constraints have distinct precedence. Some source chips are compact and some opt out of minimum touch target.',
 'OriginalControl stores fixed visualHeight but renders it with heightIn(min=look.height); interactive Box adds minimumInteractiveComponentSize and propagation. The surrounding layout can therefore allocate/grow differently even when colors and radius are correct.',
 'Keep all existing per-instance style keys. Test outer/touch/visual/content bounds independently at default and large fonts; resolve fixed/min/parent precedence and source touch-target rules before accepting a layout.',
 [('compose/controls/OriginalControls','val visual = Modifier.wrapContentHeight().heightIn(min = look.height.dp)'),('compose/controls/OriginalControls','Modifier.minimumInteractiveComponentSize()')],kind='confirmed_constraint_difference_runtime_measurement_required',pred=lambda r:any(n['tag'] in {'MaterialButton','Chip','AppCompatButton','Button'} and re.match(r'\d+(?:\.\d+)?dp$',n['attributes'].get('android:layout_height','')) for n in r['nodes']))
add('S27','App connection rows omit source line/weight/font distinctions','list_item_app_domain_details list_item_app_ip_details list_item_app_country_details',
 'Each layout supplies distinct line limits, ellipsis, font family, source sizes and count/flag/progress alignment. Country is not identical to domain/IP.',
 'AppConnectionRow flattens three types into shared14sp title/12sp subtitle/16sp count; Text does not apply node textStyle/maxLines/ellipsize. Generic count/font and row padding replace original attrs.',
 'Make per-layout row variants or pass complete source contracts; preserve current progress explicit colors and source-specific icon/flag states.',
 [('compose/appdetails/AppConnectionRows','Text(wgAnnotatedText(title.text), color = themeColor(R.attr.primaryTextColor), fontSize = 14.sp)'),('compose/appdetails/AppConnectionRows','Text(count.text.toString(), Modifier.padding(5.dp), fontSize = 16.sp)')])
add('S28','Endpoint flag minimum frame was lost','list_item_endpoint dns_proxy_list_item dns_crypt_endpoint_list_item',
 'list_item_endpoint endpoint_flag_text has minWidth32dp/minHeight32dp, centered25sp text, marginEnd5dp; endpoint rows also inherit card compat styling.',
 'DnsEndpointRow flag is raw Text with end5dp and25sp size, without the source minimum frame or centered text. Protocol layouts need separate attribute comparison rather than a generic endpoint equivalence assumption.',
 'Preserve flag slot constraints per protocol variant and independently resolve card/tint/selection artwork contracts.',
 [('compose/dns/DnsEndpointScreen','flag?.let { Text(it, Modifier.padding(end = 5.dp), fontSize = 25.sp)')])
add('S29','DNS record-mode controls bypass their existing original-control catalog','bottom_sheet_dns_record_types',
 'Both buttons use toggleButtonStyle, minWidth125dp and cornerRadius16dp within a single-selection MaterialButtonToggleGroup; source style adds accent stroke and state fill/text.',
 'DnsRecordTypesContent draws two independent Surface/Text controls, with no source stroke/minWidth/fused inner-corner semantics. Keys already exist in OriginalControlCatalog but are unused by these nodes.',
 'Use exact existing catalog variants with segmentIndex/count, original dynamic tints, selection callbacks and test tags. Extend integration guard to catch manually painted source controls.',
 [('compose/about/DnsRecordTypesContent','Surface(Modifier.weight(1f).selectable(selected'),('compose/controls/OriginalControlCatalog','bottom_sheet_dns_record_types:drbs_auto_mode_btn')],priority='P0')
add('S30','Block Free DNS surrounding text and URL spacing differ','activity_block_free_dns list_item_block_free_dns',
 'Description/selected/empty text12sp, item name14sp bold, item URL12sp with2dp top margin; heading uses explicit TitleSmall.Emphasized style.',
 'BlockFreeDnsContent uses bodyMedium14sp for description and default Text for selected/empty/name/URL (bodyLarge16sp), and URL top10dp. Cards are manually flattened surfaces.',
 'Retain restored filter/type-chip keys, but restore explicit surrounding text styles and URL margin; map the original MaterialCard shapes/elevation/insets separately.',
 [('compose/dns/BlockFreeDnsContent','Modifier.padding(top = 10.dp), color = themeColor(R.attr.primaryLightColorText)')])
add('S31','Filter-sheet heading style and drag-handle geometry differ','bottom_sheet_firewall_sort_filter bottom_sheet_rethink_plus_filter',
 'Filter headings14sp secondaryTextColor with original margin/padding; handle60×3dp border color and10dp/20dp vertical margins.',
 'FirewallFilterContent uses16sp default-color headings, a generic HorizontalDivider color and shared20dp/spacedBy10dp arrangement. BlocklistFilterScreen heading has no explicit14sp size.',
 'Restore exact heading and handle variants while retaining original-control chip/button selection behavior.',
 [('compose/firewallapps/FirewallFilterContent','Text(stringResource(R.string.fapps_filter_filter_heading),fontSize=16.sp)'),('compose/blocklists/BlocklistFilterScreen','Text(stringResource(R.string.bsrf_sub_group_heading)')])
add('S32','Interval sheet captions and expandable row overflow differ','bottom_sheet_log_activity_interval item_log_activity_app item_log_activity_conn',
 'Count labels and empty message11sp; app name maxLines1/end ellipsis; connection label maxLines1/end ellipsis/selectable. Card styles/elevation are independent of text.',
 'IntervalCount and empty message use12sp. AppActivityNativeRow Text for name and entry label does not constrain maxLines/overflow, allowing a longer row than XML.',
 'Restore source11sp captions and line limits/overflow; preserve source row reading/selection and original range buttons.',
 [('compose/home/LogIntervalContent','fontSize = 12.sp)'),('compose/home/LogIntervalContent','Text(name, color = themeColor(R.attr.primaryTextColor), fontSize = 15.sp)')])
add('S33','Detailed-statistics/domain headers lose original typography','activity_detailed_statistics activity_domain_connections',
 'Brand Heading.HomeScreen is all-caps sans-serif-smallcaps with .25em tracking and rethink_header_text size. Title16sp smallcaps with5dp extra line spacing; subtitle12sp primaryLightColorText.',
 'PagedStatisticsScreen uses headlineLarge for brand, titleLarge for heading and bodyMedium for subtitle, dropping source family/tracking/caps/colors and sizes. Selectable domain title behavior is preserved.',
 'Resolve the original named style and explicit title/subtitle overrides; keep header constraints/empty-state alignment and existing selection behavior.',
 [('compose/statistics/PagedStatisticsScreen','style = MaterialTheme.typography.headlineLarge'),('compose/statistics/PagedStatisticsScreen','Text(title, style = MaterialTheme.typography.titleLarge)')])
add('S34','DNS editor layout loses source invisible error reservation and insets','dialog_set_custom_doh dialog_set_custom_odoh dialog_set_dns_crypt',
 'DoH/ODoH failure TextView starts INVISIBLE, retaining measurement; its10dp margins/5dp padding differ from DNSCrypt2dp error padding. Titles have10dp padding and each layout has own field arrangement.',
 'Shared DnsEditorDialog uses20dp content padding/spacedBy8; DnsError emits nothing without error and has no layout-specific padding. Field restoration alone does not preserve parent/error geometry.',
 'Model invisible versus gone error states and per-editor content insets without changing validation/callback behavior. Compare dialog window background separately from layout content.',
 [('compose/dns/DnsEndpointEditors','if (!error.isNullOrEmpty()) Text(error'),('compose/dns/DnsEndpointEditors','Column(Modifier.padding(20.dp))')])
add('S35','Smart DNS header description is 14sp instead of11sp','activity_smart_dns_list activity_configure_other_dns',
 'Shared title references the concrete TextAppearance.Material3.TitleMedium resource; Smart DNS description is small_font_subheading_text=11sp with5dp padding.',
 'DnsFragmentHeader uses theme titleMedium (not exact direct style resource) and explicit14sp for Smart DNS description. Margins/padding are otherwise closely matched.',
 'Resolve the explicit title style and11sp description with existing typography helper; retain native framework host and fixed IDs.',
 [('compose/dnssettings/DnsFragmentHost','description?.let { Text(it, Modifier.padding(5.dp), fontSize = 14.sp')])
add('S36','Removed-server rows and ping-history rows lose text metrics','item_removed_server item_ping_test_history',
 'Removed-server flag24sp in40dp frame, name16sp bold/maxLines1, location13sp/maxLines1. Ping-history targets14.5sp medium, latency13sp bold medium, outcome11.5sp medium; all are single-line.',
 'RpnRemovalContent flag28sp with no frame and generic name/bodySmall location; PingHistoryRow resolves only numeric sizes and leaves family/weight/line limits unset.',
 'Give each row its own complete source text/frame contract; preserve current data and callback logic and disclose large-font alternate layout separately.',
 [('compose/rpn/RpnRemovalContent','Text(server.flagEmoji, fontSize = 28.sp)'),('app/src/main/java/com/celzero/bravedns/adapter/PingTestHistoryAdapter.kt','style = utilityTextStyle(size = 14.5f)')])
add('S37','About brand title uses fixed SP tracking instead of source EM/style','fragment_about',
 'TextAppearance.Heading.HomeScreen specifies sans-serif-smallcaps, .25em tracking, all-caps and rethink_header_text dimension.',
 'AboutContent uses SansSerif and letterSpacing7.sp; dimensionResource(...).value.sp is not a source text-style resolver and can double-apply scaled values under nonlinear font scaling.',
 'Resolve original named TextAppearance and explicit dynamic text/click state through the existing typography resolver; keep content and source accessibility behavior.',
 [('compose/about/AboutContent','fontSize = dimensionResource(R.dimen.rethink_header_text).value.sp, letterSpacing = 7.sp')])

# Six definitions have no live baseline rendering path. Do not invent a Compose counterpart.
retired={'dialog_ip_details_layout':'No baseline source or include consumer found.','dialog_sponsor_info':'No baseline source or include consumer found; the prior button census explicitly left it unassigned.','item_server':'No baseline source or include consumer found.','view_sponsor_badge':'SponsorBadgeView is referenced only by its own baseline declaration and inflater.','spinner_item_firewall_status':'FirewallStatusSpinnerAdapter is referenced only by its own baseline declaration.','list_item_server_wg_peers':'ServerWgPeersAdapter is referenced only by its own declaration/type self-reference, distinct from live WgPeersAdapter.'}
for r in rows:
 if r['basename'] in retired:
  r['mapping_status']='baseline_dormant_or_unreferenced_no_live_renderer_required'
  r['retirement_evidence']=retired[r['basename']]
  r['equivalence_status']='not_applicable_if_reachability_retirement_accepted'
  r['required_checks']=['Use inventory worker exact reference/retired-owner evidence; preserve resource-id/test dependencies before removal. No invented Compose screen is necessary.']
# Existing controls are source-corrected candidates, not a restart request.
j['reviewed_findings']=findings
j['finding_counts']=dict(collections.Counter(f['status'] for f in findings))
j['dormant_definition_count']=len(retired)
owners=json.loads((D/'layout-owner-candidates.json').read_text())
for r in rows:
 r['baseline_owners_and_root_ledger_candidates']=owners.get(r['layout_path'],{}).get('baseline_owners',[])
 # Remove noisy direct-call lexical candidates from this styling ledger; inventory owns those.
 for o in r['baseline_owners_and_root_ledger_candidates']:o.pop('current_direct_composable_calls',None)
 r['source_removal_status']='pending_independent_source_equivalence_and_reference_gate' if r['basename'] not in retired else 'pending_retirement_reference_gate'
 r['runtime_validation_status']='blocked_toolchain_not_run'
 r['accepted_spacing_deviations']=[]
 r['root_ledger_warning']='Direct owner links are exact; transitive ledger paths are source candidates requiring semantic review. No new pass status is inferred.'
# Deduplicate renderer snippets/hashes across per-layout entries.
registry={}
for r in rows:
 for x in r['renderer_mapping']:
  p=x['path']; registry.setdefault(p,{'path':p,'source_present':x['source_present'],'source_sha256':x['source_sha256'],'source_style_evidence':x.pop('source_style_evidence')})
  x.pop('source_sha256',None)
  x['renderer_source_evidence_key']=p
j['renderer_source_evidence']=registry
j['acceptance_policy']='No new generic Material spacing/design deviations are accepted by this audit. Source-equivalence/reference cleanup and runtime validation are separate gates; a parent-reviewed source-safe deletion can proceed with runtime still explicitly unverified.'
(D/'styling-equivalence-ledger.json').write_text(json.dumps(j,indent=2,ensure_ascii=False)+'\n')
(D/'styling-findings.json').write_text(json.dumps({'baseline':j['baseline'],'native_snapshot':j['native_snapshot'],'findings':findings},indent=2)+'\n')
lines=['# Whole-layout styling preservation audit','',f"Baseline `{j['baseline']}`; native snapshot `{j['native_snapshot']}`.",'','Status: source-only preflight; all 186 XML layouts remain in place. No production edits, compilation, runtime, screenshot, TalkBack, downloads, VPN, account or traffic execution.','', '## Findings','']
for f in findings:
 lines+=['### '+f['id']+' — '+f['title'],'',f"Priority: {f['priority']}. Evidence: `{f['status']}`.",'','Affected: '+', '.join('`'+pathlib.Path(p).parent.name+'/'+pathlib.Path(p).name+'`' for p in f['layout_paths'])+'.','','Original: '+f['baseline_contract'],'','Current: '+f['current_source_difference'],'','Before removal: '+f['required_before_deletion'],'','Evidence: '+', '.join('`'+e['path']+':'+str(e['line'])+'`' for e in f['source_evidence'])+'.','']
(D/'styling-findings.md').write_text('\n'.join(lines)+'\n')
lines=['# Per-layout styling register','', 'All 186 physical source-set/qualifier files are listed. Each row links to explicit original root/container/shape/typography/color/dimension/semantics contracts in `styling-equivalence-ledger.json`. Mapped families and extracted source properties are evidence locations, never equivalence approvals. No row is deletion-ready.','', '| Layout | Root | Nodes | Native family | Findings / remaining disposition |','| --- | --- | ---: | --- | --- |']
for r in rows:
 fam='<br>'.join(x['path'].replace(P,'')+':'+','.join(str(v['line']) for v in x['verified_functions']) for x in r['renderer_mapping']) or 'No live baseline renderer required; retirement evidence pending final reference gate'
 lines.append('| `'+r['layout_path'].replace('app/src/','')+'` | '+r['root']['tag'].split('.')[-1]+' | '+str(r['node_count'])+' | '+fam+' | '+(', '.join(r['specific_findings']) or ('Retirement check' if r['basename'] in retired else 'Per-node reconciliation and runtime fixtures pending; not accepted'))+' |')
(D/'styling-layout-register.md').write_text('\n'.join(lines)+'\n')
print(json.dumps({'layouts':len(rows),'nodes':j['node_count'],'renderer_files':len(registry),'findings':len(findings),'statuses':j['finding_counts'],'layouts_with_specific_findings':sum(bool(r['specific_findings']) for r in rows),'retired':len(retired)},indent=2))
# Content-address resources and reference authoritative node attributes losslessly.
from compact_styling_audit import main as compact_styling_audit
compact_styling_audit()
