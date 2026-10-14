> Historical checkpoint audit. The later binding-removal source changes supersede its framework-host inventory; see [binding-removal-audit.md](binding-removal-audit.md), [binding-removal-review.md](binding-removal-review.md), and [validation-status.md](validation-status.md). This document does not validate the newer, uncompiled host changes.

# Independent migration coverage audit 2

Initial audit: 2026-10-14 07:12–07:31 UTC; scoped F1/F2 follow-up: 07:41–07:48 UTC. **Independent source audit completed; final migration acceptance is not established.**

- Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
- Immutable inspected snapshot: `ecf024b519f1be45c6f104c2e9c3ce41d1591e47`; tree `9a74f9ce57fa2624da52e114a12fec945dc9422c`, matching the coordinator-supplied verified remote snapshot `efa4b2ff720c41148230e0af4f28cf08c35fc807` tree.
- This auditor verified the local commit/tree relationship, not the remote transport independently. The active checkout has concurrent later edits; any later fix is separately identified.
- No Gradle, test execution, app/device/runtime, service, network, account, VPN, permission, installation, or remote-write operation was performed. Git blob/archive reads and static source inspection only.
- User acceptance allows newer Material component cosmetics and padding/margin changes to conform to Material 3 guidance (coordinator/user clarification received 07:25 UTC). Content, actions, theme/contrast, clipping, large fonts, RTL, accessibility, state and controller behavior remain required.

## Findings reported immediately

### F1 — Domain-connections title lost selection/copy (P2 in pinned snapshot; later source repair verified)

Baseline `app/src/main/res/layout/activity_domain_connections.xml:39` declares `dc_title` as `android:textIsSelectable="true"`. `DomainConnectionsActivity` assigns the domain/IP/ASN/country/blocklist title to that control. The migrated Activity calls `PagedStatisticsScreen` at line 155; `ui/compose/statistics/PagedStatisticsScreen.kt:29` renders an ordinary `Text(title)` without `SelectionContainer` or an equivalent copy action. Thus users lose selection/copy for the displayed value. This is an interaction regression, not permitted Material styling variation. The file is unchanged in the active checkout at 07:17 UTC. Reported to coordinator at 07:18 UTC.

Later active-checkout verification at 07:31 UTC: the coordinator added `selectableTitle: Boolean = false` to the shared renderer and passes `selectableTitle = true` only from DomainConnectionsActivity. The selected title is wrapped in `SelectionContainer`, restoring the missing source path without changing the other caller. Six new `PagedStatisticsTitleSelectionTest` cases are present. On follow-up at 07:43 UTC this auditor read the retained `validation/evidence/reviewed-content-1/TEST-com.celzero.bravedns.ui.compose.statistics.PagedStatisticsTitleSelectionTest.xml`: tests=6, failures=0, errors=0, skipped=0, timestamp 07:35:26 UTC. `reviewed-content-tests-1.log` records all six passing. F1 is source-repaired and has a passing isolated selection/copy regression suite in that later run; it remains present in the immutable original audit snapshot.

### F2 — Protocol-DNS row lifecycle added flag lookups/restarted polling (P2 in pinned snapshot; later source repair verified, runtime pending)

`ui/compose/dns/DnsEndpointScreen.kt:95–107` wraps both row-description polling and flag lookup in `repeatOnLifecycle(STARTED)`. For an unchanged row that remains composed, every STOP → START transition starts these blocks again. `latest.flag()` in `DnsEndpointPresenter` can call `VpnController.getDnsIps`, `Utilities.getIpForUrl`, and `IpInfoDownloader.getIpInfo`; this is more than a cosmetic redraw.

The baseline `DohEndpointAdapter.displayDetails` invokes `io { updateFlag(endpoint) }` on row bind only. Its `keepSelectedStatusUpdated` loop checks lifecycle once per one-second tick and cancels its job when below STARTED, with no automatic restart on resume; it restarts on a later bind. A STOP long enough to cross that tick therefore has different native/baseline status-poll traces. Even a shorter STOP now causes an extra native flag lookup on START. DoT/ODoH/DNSCrypt and other shared endpoint rows need their own original guard comparison rather than assuming a uniform contract.

The native files were unchanged in the active checkout at 07:26 UTC. Reported to coordinator at 07:26 UTC; a separate repair worker was assigned at 07:27 UTC. Independent source closure at 07:47 UTC verifies the later repair described below, including the additional bind-branch race found during review. Seventeen fake lifecycle/branch cases are authored; their runtime results remain pending. Do not call production network or VPN to validate this.

### Previously reported log-search focus defect

The pinned snapshot still associates editor focus with SearchView-root expansion in `LogScreen.kt`, which clears seeded query text. A separate worker has reported a source repair and seven regression/reference tests in the active checkout. Follow-up read of the retained reviewed-content-1 XML at 07:43 UTC finds LogSearchViewReferenceTest 3/3 passing, but LogSearchBarComposeTest has 4 cases with 1 failure (editorRefocusAfterResumePreservesRestoredAndTypedQueries). The defect must not be closed from source alone while that interaction failure remains unresolved. This audit does not apply any later fix retroactively to `ecf024b`.

## Fresh independently reproduced scope and protected boundaries

- Enumerated original root class inheritance from original Git blobs, including indirect bottom-sheet and fully qualified dialog bases; excluded the two abstract/shared host base classes. Result: **115 = 45 Activities + 28 flavor-specific Fragments + 32 bottom sheets + 10 standalone dialogs**.
- Compared that fresh root set with the current 115-row ledger: exact path-set match, no missing or extra original roots. This corrects the previous audit's outdated 137-row-ledger warning.
- All 31 original TV production source files are byte-identical by Git blob ID. This does not cover changed common Activities' behavior when reached from TV.
- All 522 paths selected by the documented protected-core rule are byte-identical; changed paths: []. The count includes some support/tests and is not a count of production business classes.
- All 6 original Android manifests are byte-identical. All existing catalog versions, libraries and plugin aliases are unchanged; additions are separate UI/test build integration.
- Every retained root has native content or an explicit framework/native-child delegate. No `AndroidView`/`AndroidViewBinding` screen wrapper was found. `AndroidFragment` remains only the NetworkLogs fragment lifecycle boundary.

## Exhaustive original-root inventory

Root presence/native entry-point review does not certify every interaction or runtime state. Every row retains pending lifecycle/navigation/accessibility/visual acceptance unless separate current evidence explicitly discharges a gate.

| Original root | Category | Static root result |
|---|---|---|
| `fdroid:RethinkPlusFragment` | fragment | Native renderer/delegate present |
| `main:SponsorFragment` | fragment | Native renderer/delegate present |
| `main:SponsorSuccessFragment` | fragment | Native renderer/delegate present |
| `main:HomeScreenActivity` | activity | Explicit framework host; native visible children |
| `main:NotificationHandlerActivity` | activity | Explicit framework host; native visible children |
| `main:TestDialogActivity` | activity | Intentionally empty baseline test context |
| `main:AdvancedSettingActivity` | activity | Native renderer/delegate present |
| `main:AlertsActivity` | activity | Native empty baseline surface |
| `main:AntiCensorshipActivity` | activity | Native renderer/delegate present |
| `main:AppInfoActivity` | activity | Native renderer/delegate present |
| `main:AppListActivity` | activity | Native renderer/delegate present |
| `main:AppLockActivity` | activity | Native renderer/delegate present |
| `main:AppWiseDomainLogsActivity` | activity | Native renderer/delegate present |
| `main:AppWiseIpLogsActivity` | activity | Native renderer/delegate present |
| `main:BlockFreeDnsActivity` | activity | Native renderer/delegate present |
| `main:BubbleActivity` | activity | Native renderer/delegate present |
| `main:CheckoutActivity` | activity | Native renderer/delegate present |
| `main:ConfigureOtherDnsActivity` | activity | Native renderer/delegate present |
| `main:ConfigureRethinkBasicActivity` | activity | Explicit framework host; native visible children |
| `main:ConsoleLogActivity` | activity | Native renderer/delegate present |
| `main:CustomRulesActivity` | activity | Native renderer/delegate present |
| `main:CustomerSupportActivity` | activity | Native renderer/delegate present |
| `main:DetailedStatisticsActivity` | activity | Native renderer/delegate present |
| `main:DnsDetailActivity` | activity | Explicit framework host; native visible children |
| `main:DnsListActivity` | activity | Native renderer/delegate present |
| `main:DomainConnectionsActivity` | activity | Native renderer/delegate present |
| `main:EventsActivity` | activity | Native renderer/delegate present |
| `main:FirewallActivity` | activity | Explicit framework host; native visible children |
| `main:FragmentHostActivity` | activity | Explicit framework host; native visible children |
| `main:MiscSettingsActivity` | activity | Native renderer/delegate present |
| `main:NetworkLogsActivity` | activity | Native renderer/delegate present |
| `main:PauseActivity` | activity | Native renderer/delegate present |
| `main:PingTestActivity` | activity | Native renderer/delegate present |
| `main:ProxySettingsActivity` | activity | Native renderer/delegate present |
| `main:PurchaseHistoryActivity` | activity | Native renderer/delegate present |
| `main:RpnBypassAppsActivity` | activity | Native renderer/delegate present |
| `main:RpnConfigDetailActivity` | activity | Native renderer/delegate present |
| `main:RpnWinProxyDetailsActivity` | activity | Native renderer/delegate present |
| `main:ServerOrderHistoryActivity` | activity | Native renderer/delegate present |
| `main:SmartDnsListActivity` | activity | Native renderer/delegate present |
| `main:TcpProxyMainActivity` | activity | Native renderer/delegate present |
| `main:TunnelSettingsActivity` | activity | Native renderer/delegate present |
| `main:UniversalFirewallSettingsActivity` | activity | Native renderer/delegate present |
| `main:WelcomeActivity` | activity | Native renderer/delegate present |
| `main:WgConfigDetailActivity` | activity | Native renderer/delegate present |
| `main:WgConfigEditorActivity` | activity | Native renderer/delegate present |
| `main:WgIncludeAppsActivity` | activity | Native renderer/delegate present |
| `main:WgMainActivity` | activity | Native renderer/delegate present |
| `main:AppDomainRulesBottomSheet` | sheet | Native renderer/delegate present |
| `main:AppIpRulesBottomSheet` | sheet | Native renderer/delegate present |
| `main:AutoExcludeCountriesBottomSheet` | sheet | Native renderer/delegate present |
| `main:BackupRestoreBottomSheet` | sheet | Native renderer/delegate present |
| `main:BlockFreeDnsModeBottomSheet` | sheet | Native renderer/delegate present |
| `main:BugReportFilesBottomSheet` | sheet | Native renderer/delegate present |
| `main:ConnTrackerBottomSheet` | sheet | Native renderer/delegate present |
| `main:CustomDomainRulesBtmSheet` | sheet | Native renderer/delegate present |
| `main:CustomIpRulesBtmSheet` | sheet | Native renderer/delegate present |
| `main:DeviceAuthErrorBottomSheet` | sheet | Native renderer/delegate present |
| `main:DeviceNotRegisteredBottomSheet` | sheet | Native renderer/delegate present |
| `main:DnsBlocklistBottomSheet` | sheet | Native renderer/delegate present |
| `main:DnsRecordTypesBottomSheet` | sheet | Native renderer/delegate present |
| `main:EntitlementDetailBottomSheet` | sheet | Native renderer/delegate present |
| `main:FirewallAppFilterBottomSheet` | sheet | Native renderer/delegate present |
| `main:HomeScreenSettingBottomSheet` | sheet | Native renderer/delegate present |
| `main:LocalBlocklistsBottomSheet` | sheet | Native renderer/delegate present |
| `main:LogActivityIntervalBottomSheet` | sheet | Native renderer/delegate present |
| `main:OrbotBottomSheet` | sheet | Native renderer/delegate present |
| `main:ProxyCountriesBtmSheet` | sheet | Native renderer/delegate present |
| `main:PurchaseConflictBottomSheet` | sheet | Native renderer/delegate present |
| `main:PurchaseProcessingBottomSheet` | sheet | Native renderer/delegate present |
| `main:RethinkInRethinkWarningBottomSheet` | sheet | Native renderer/delegate present |
| `main:RethinkListBottomSheet` | sheet | Native renderer/delegate present |
| `main:RethinkLogBottomSheet` | sheet | Reference-retired; replacement or unused route checked |
| `main:RethinkPlusFilterBottomSheet` | sheet | Native renderer/delegate present |
| `main:RpnLogActivityIntervalBottomSheet` | sheet | Native renderer/delegate present |
| `main:RpnStatsBottomSheet` | sheet | Native renderer/delegate present |
| `main:ServerRemovalNotificationBottomSheet` | sheet | Native renderer/delegate present |
| `main:ServerSettingsBottomSheet` | sheet | Native renderer/delegate present |
| `main:SummaryStatsFilterBottomSheet` | sheet | Native renderer/delegate present |
| `main:WireguardListBtmSheet` | sheet | Native renderer/delegate present |
| `main:CustomLanIpDialog` | dialog | Native renderer/delegate present |
| `main:DnsCryptRelaysDialog` | dialog | Reference-retired; replacement or unused route checked |
| `main:GenericHopDialog` | dialog | Native renderer/delegate present |
| `main:NetworkReachabilityDialog` | dialog | Native renderer/delegate present |
| `main:RpnProxyHopDialog` | dialog | Inherits native GenericHopDialog content |
| `main:RpnSsidDialog` | dialog | Native renderer/delegate present |
| `main:SubscriptionAnimDialog` | dialog | Native renderer/delegate present |
| `main:WgAddPeerDialog` | dialog | Native renderer/delegate present |
| `main:WgHopDialog` | dialog | Inherits native GenericHopDialog content |
| `main:WgSsidDialog` | dialog | Native renderer/delegate present |
| `main:AboutFragment` | fragment | Native renderer/delegate present |
| `main:ConfigureFragment` | fragment | Native renderer/delegate present |
| `main:ConnectionTrackerFragment` | fragment | Native renderer/delegate present |
| `main:CustomDomainFragment` | fragment | Native renderer/delegate present |
| `main:CustomIpFragment` | fragment | Native renderer/delegate present |
| `main:DnsCryptListFragment` | fragment | Native renderer/delegate present |
| `main:DnsLogFragment` | fragment | Native renderer/delegate present |
| `main:DnsProxyListFragment` | fragment | Native renderer/delegate present |
| `main:DnsSettingsFragment` | fragment | Native renderer/delegate present |
| `main:DoTListFragment` | fragment | Native renderer/delegate present |
| `main:DohListFragment` | fragment | Native renderer/delegate present |
| `main:FirewallSettingsFragment` | fragment | Native renderer/delegate present |
| `main:HomeScreenFragment` | fragment | Native renderer/delegate present |
| `main:ODoHListFragment` | fragment | Native renderer/delegate present |
| `main:RethinkBlocklistFragment` | fragment | Native renderer/delegate present |
| `main:RethinkListFragment` | fragment | Native renderer/delegate present |
| `main:RethinkLogFragment` | fragment | Native renderer/delegate present |
| `main:RethinkPlusDashboardFragment` | fragment | Native renderer/delegate present |
| `main:RethinkPlusManagePurchaseFragment` | fragment | Native renderer/delegate present |
| `main:ServerSelectionFragment` | fragment | Native renderer/delegate present |
| `main:SmartDnsListFragment` | fragment | Native renderer/delegate present |
| `main:SummaryStatisticsFragment` | fragment | Native renderer/delegate present |
| `main:WgNwStatsFragment` | fragment | Native renderer/delegate present |
| `play:RethinkPlusFragment` | fragment | Native renderer/delegate present |
| `website:RethinkPlusFragment` | fragment | Native renderer/delegate present |

## Nested renderer and reachability audit

A string/comment-aware scan of all main/fdroid/play/website production Kotlin/Java found **zero real MaterialAlertDialogBuilder or AppCompat/Android AlertDialog.Builder construction sites and zero AndroidView/AndroidViewBinding render wrappers**. Alias imports to `WgAlertDialogBuilder` and `NetworkAlertDialogBuilder` were resolved before counting. Unused legacy imports were not counted as active renderers.

Exactly two active `.inflate(...)`-containing source files remain in those source sets: `FirewallStatusSpinnerAdapter` and `SponsorBadgeView`. Fresh source/resource caller searches find neither instantiated or referenced outside its own declaration. The old `AlertAdapter` body is commented out. The only active ViewBinding delegates are `DnsDetailActivity` and `FirewallActivity`, whose XML has an explicitly GONE TabLayout and a single native-fragment ViewPager2. These are the documented lifecycle shells, not settings content hidden to avoid migration.

`EmbeddedDolphinSignature`, histogram/donut/map/ocean/badge/switch custom Views remain as original compatibility/reference classes and in uninflated legacy XML. Their visible production replacements use Canvas, Compose drawing, drawable rasterization, and native text. `AppHistogramView.Entry` is used solely as a data type. Native rendering does not instantiate the old View. Retained original source/resources are not evidence of a reachable screen.

Twenty original production Kotlin files are deleted in the snapshot: eighteen renderer adapters plus the two retired root classes. A fresh constructor/import scan finds no remaining production references to the retired class names. Active endpoint, custom-rule, peer, SSID and blocklist functionality is delegated to native presenters/content; unused RethinkLogBottomSheet and WgHopAdapter are not silently reintroduced. `SubscriptionAnimDialog` now hosts native confetti and no longer references the deleted `dialog_subscription_anim.xml`.

### Traced groups and boundaries

| Group | Nested presentation/callback checks | Remaining risk |
|---|---|---|
| Home/root navigation | Native chrome and six graph destinations; tour anchors; snackbar presenter; prompt actions; old custom charts replaced by Canvas | Real-host Back/reselection, system insets, snackbar queue/timeout and tour geometry need current lifecycle/device evidence |
| Settings menus/DNS settings | Native rows, independent controls, switch-vs-row dispatch, DNS protocol routes, framework pager children | Cross-widget restoration order and disabled-state accessibility need actual-host coverage |
| DNS protocol editors/lists | Add/edit/error/cancel fields, selection/delete/info/copy, relay checks, selected status and flag lookups | **F2** identifies a real divergence in the pinned snapshot; later source repair reviewed, runtime pending |
| Blocklists | Native pack/advanced rows and filters, Apply/Cancel/Back, prompt callbacks, subgroup sheet | Async download/cancel/reentry and retained paging require full isolated-host comparison |
| App list/details/app logs | Native bulk icon row, list/paging presenters, shared-UID prompts, six rule controls, notes, nine insight sections and routes | Touch/focus/restored paging and all rule error branches remain unproven globally |
| Traffic/DNS/console/events | Native rows/detail sheets, rule cycling, metadata copy/selection, filters, long-press event copy, console choices/share dispatch | Previously reported log-search focus regression remains in the pinned snapshot; separate fix pending aggregate verification |
| Statistics | Native list/insights, maps/donuts/ranks, tag chips, domain groups, detailed routes | **F1** title selection/copy repair has six passing isolated cases; broader heading/content semantics not accepted |
| WireGuard | Native config/peer/SSID/hop/include-app renderers, shared alert bridge, field/controller callbacks | Form restoration, dialog return, rapid/repeated actions and focus need current mounted-host traces |
| RPN server/account/settings/config | Native grids/chips/lists, visibility layout, switch guards, filters, signatures, dialogs and overlays | Original service/poll operations remain reachable; lifecycle/event cardinality needs isolated fake-manager verification |
| Purchase/channel-specific screens | Play/website/fdroid roots, plans/checkout/history, refund/support/processing/entitlement, native confetti | Billing/return/cancel lifecycle remains an external-boundary test obligation; no payment/account flow was executed |
| Network settings/sheets | All 294 original IDs across six network-settings/editor/lockdown layouts occur exactly once as native descriptor tags; proxy editors, choice dialogs, Orbot/backup/RINR sheets | Shared-dialog focus/IME, disabled and outside/back behavior need broader interaction evidence |
| Utilities/sponsor | Welcome page controls, Bubble nested rows, Ping history, Pause long/release callbacks, reachability fields, support and Sponsor/Success | User-facing clocks/animations/state restore and keyboard/D-pad/RTL still require runtime coverage |
| About/diagnostics | Native debug dialogs, selectable/link text, token tap/double-tap/selection bridge, file/type lists | Gesture conflict and accessibility behavior need actual mounted-content traces |

No additional missing screen or newly reachable legacy renderer was found. This is a source-based reachability conclusion, not a proof that every visible state renders correctly.

## Controller, state and protected-scope conclusions

- Navigation graphs and `FragmentHostActivity` are byte-identical. All six manifest files preserve component declarations, launch modes, extras ownership and aliases. This proves entry metadata, not runtime navigation behavior.
- The protected-core comparison also checks original TV resources/manifests where selected by the guard. No newly added non-UI/non-adapter production Kotlin/Java core path was found. Existing guards enumerate original paths, so this independent addition check matters.
- `UIUtils` is excluded by the generic protected rule; its actual changes are the Compose snackbar import/rendering region. Selected non-rendering helpers and fixed startup/navigation routines have dedicated source guards, but this audit does not promote another worker's historical guard result into a fresh runtime pass.
- Root Gradle/settings/properties/wrapper, main security XML/raw/assets show no baseline diff. `app/build.gradle.kts` changes are native UI dependency scope, Fragment Compose, Compose confetti and Android-resource/test wiring. The removed redundant TV BOM used the same unchanged `composeBom` version. All channel/TV compile and runtime effects must still be validated by the coordinator.
- Snapshot state replaces Views rather than moving business policy into the renderers. However, adapters/controllers are themselves permitted UI paths containing real operations, and native effects determine when those operations run. **F2 demonstrates why unchanged core bytes and selected-method hashes cannot prove global logic or callback equivalence.**
- The audit traced original selectable-text declarations and programmatic selection/click/long-click routes in addition to root markers; this found F1. Source root counts alone would miss it.
- No whole-program assertion of preserved controller semantics is justified while F1/F2 and the known log focus defect remain in the audited snapshot (later F1/log source fixes are separately noted). Fixes need exact-current-source regression evidence.

## Original test preservation and execution limits

Fresh baseline enumeration finds **99 original test source files**, including `testPlay` and `testWebsite`. Every original file has a retained or relocated/merged destination in the pinned snapshot; no source file lacks a destination mapping. This does not by itself prove that every original behavioral obligation is preserved. Sixteen original AndroidTest files have moved to test source paths, with the two original Welcome suites intentionally merged into one 20-case fixture. Seven old ViewHolder-specific method names across FirewallAppListAdapterTest/WgConfigAdapterTest have documented native presenter replacements. Original method-name/count preservation is structural evidence, not a guarantee that each assertion still proves the original behavior.

The previous audit's statement that all original files remain at their original paths is obsolete. The new `original-test-relocation-map.json` records the explicit mapping. The former ignored NetworkLogs/Statistics host obligations are now authored as actual-host fake fixtures; authored does not mean passing.

No tests were executed by this auditor. Coordinator-provided compile/test status must be attached to its exact snapshot and cannot be inferred from source presence. In particular, incomplete/failing host batches must remain incomplete/failing until the corrected suite has a terminal verified result. Targeted visual fixtures cover only their documented surfaces and configurations. They do not establish full 115-root large-font/RTL/TalkBack/contrast acceptance.

## Final disposition and concrete next gates

**Implementation coverage is source-present across the complete original root inventory. Full-scope acceptance is not complete.**

1. Run the 17 new F2 fake lifecycle/branch traces and resolve the remaining log-search interaction failure. F1 now has six passing isolated selection/copy cases. Record the new source snapshot; do not mark the old snapshot fixed.
2. Finish the coordinator's actual-host failures without replacing navigation/recreation assertions by content-only smoke tests. Preserve the original test-to-fixture map and strict service/network/account boundaries.
3. Verify Back/outside/swipe cancellation, focus/IME, restored query/draft/selection/scroll, repeated callbacks, async stop/resume and stale-result behavior across the changed lifecycle bridges.
4. Complete the approved all-channel/full/TV compiler/test matrix. TV byte preservation is a narrow positive, not TV integration acceptance.
5. Review required content/action reachability, contrast, touch targets, RTL/bidi and large fonts on the remaining high-risk screens. Cosmetic Material 3 shapes, typography, padding and margins alone are not blockers under the user's updated acceptance criteria.

Only this report was written in the application checkout by this auditor. No production source, tests, other ledger, service or external state was changed.

## Follow-up verification requested at 07:41 UTC

This follow-up is read-only source/log review; no tests or runtime operations were executed by this auditor. The six title-selection cases and six visual methods are independently confirmed passing from retained test XML and the coordinator log. The surrounding batch is **not a pass**: `reviewed-content-tests-1.log` records 382 tests, 12 failures, and BUILD FAILED. The visual methods cover only their documented six surfaces, eight configurations each, including the updated shared-dialog contrast gate.

Initial F2 repair review confirmed removal of automatic STOP/START replay, owner-bound work, one lookup per row binding, identity-keyed protocol row memoization, and updated SmartDNS callbacks. It also found a remaining bind-branch race: the default description lambda re-evaluated tunnel/smart-DNS guards on each tick, whereas original adapters chose poll-versus-static at bind. A static bind could also become a backend read before its asynchronous callback ran. The repair worker subsequently captured that branch and supplied static labels synchronously; independent final source review below closes this additional source defect.

### F2 final source closure, 07:47 UTC

The frozen patch was read again and each reported SHA-256 was independently recomputed. Its final source structure now provides:

- DoH/DoT/ODoH/DNSCrypt capture empty/static/poll classification in `bindDnsEndpointDescription`. The selected/tunnel/smart-DNS branch is evaluated only when the row factory binds. Once polling is selected, its callback is directly `::dnsStatus`, with no per-tick tunnel/smart-DNS guard re-evaluation. A static bind cannot become a status read later.
- Static selected text is stored as `initialDescription` and installed directly in remembered row state, with `description = null`; no IO must finish to display it while the owner is CREATED. SmartDNS uses this same synchronous static path. Unselected rows do not evaluate the poll guard or selected label.
- Relay captures its distinct selected-and-not-smart condition outside the callback, runs at most one asynchronous status read for that bind, and publishes only while STARTED. Proxy retains its one-shot description callback and STARTED publication guard. Both remain distinct from the four polling protocols.
- A row's `DisposableEffect(item, owner, ioDispatcher)` launches owner-lifetime jobs once. Polling checks STARTED on the original one-second cadence, terminates on an inactive tick, and does not automatically restart at the next START. Flag lookup runs once per binding and is not replayed at START. Pending reads can finish across STOP, but their result publication checks current STARTED state. Replacement/removal/destroy cancels outgoing work.
- `rememberDnsEndpointRow` keys the incoming entity by reference identity plus the row factory. A parent redraw of the same source instance does not manufacture new effect callbacks; a replacement entity with id-only equality or a replacement factory binds its current callbacks. SmartDNS's full-value entity equality is appropriate for its static content, and `rememberUpdatedState` keeps its select/info callbacks fresh.
- Direct comparison with `ecf024b` shows no change to `dnsStatus`, flag lookup internals, metadata/copy, select/delete/lockdown operations, helpers or service/business implementations. The change chooses and owns presentation reads; it does not replace their service bodies.
- The updated source guard rejects the old `repeatOnLifecycle` row structure and checks identity/factory keys, inactive tick termination, initial static text and captured branch selection. The worker reports twelve negative controls passed; this auditor reviewed their source but did not execute them.

The seventeen new `DnsEndpointLifecycleComposeTest` methods use a plain Application, an empty Compose test Activity, synthetic lifecycle owners, fake row factories, deferred callbacks and test dispatchers. They cover inactive ticks, short STOP/START, one-shot rows, static CREATED labels before IO dispatch, bound guards changing afterward, unselected short-circuiting, late results, replacement/factory identity, parent redraw, row disposal and owner destruction. No production presenter/service/lookup is instantiated by these cases. Their presence is not a passing-runtime claim.

| Frozen file | Independently verified SHA-256 |
|---|---|
| `ui/compose/dns/DnsEndpointScreen.kt` | `a20f897cbe3e00a6716a771aa5bec42dcc1cad6cfb1a3bbfd414cc446c543793` |
| `ui/compose/dns/DnsEndpointPresenter.kt` | `6dfa2cb56b9665eb2d7dcf88254c0180264d3ab504fd276adf83d2555cfd2a02` |
| `ui/compose/dns/SmartDnsContent.kt` | `cef42b33148dcad8be01c45c46bf56bc43999413382514ba144d597ecee76931` |
| `app/src/test/java/com/celzero/bravedns/ui/compose/dns/DnsEndpointLifecycleComposeTest.kt` | `fe6cf1e8e11f5228c5207b443a64b3d7806792222c5e402a6b0113464dea53e7` |
| `scripts/compose-migration/check-protocol-dns-parity.py` | `96e2a2098b337d1a79b059b277cdae3008b1006892a0889e3caaff4a2c98c7f0` |

The first three paths are under `app/src/main/java/com/celzero/bravedns/`. F2 is **closed at source-review level for these exact later bytes, with runtime regression execution pending**. This does not retroactively change the old snapshot, certify all original RecyclerView recycling behavior, or establish full migration acceptance.
