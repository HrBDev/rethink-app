# Traffic, DNS, console and event logs reconstruction

Pinned baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

This is a fresh reconstruction after the prior workspace was lost. No result from the lost tree applies to these bytes. Native source and isolated test contracts are present; coordinator compilation and test execution, real-host lifecycle comparison and visual/accessibility checks remain pending.

## Ownership and reachable scope

- Activities: `NetworkLogsActivity`, `ConsoleLogActivity`, `EventsActivity`.
- Pages: `ConnectionTrackerFragment`, `DnsLogFragment`, `RethinkLogFragment`.
- Paging presenters: `ConnectionLogAdapter`, `ConnectionTrackerAdapter`, `DnsLogAdapter`, `RethinkLogAdapter`, `ConsoleLogAdapter`, `EventsAdapter`.
- Detail sheets: `ConnTrackerBottomSheet`, `DnsBlocklistBottomSheet`; app-rule choices, IP/per-app/global domain rule cycling, unknown-app switch, verdict explanations, matched blocklist dialog, app/domain navigation and all metadata.
- Search, filters, parent/child and severity/source chips, clear confirmations, empty/loading/disabled states, timestamps/scroll header/fast-scroll thumb, console level/file-trace dialog, debug pause/resume and both share actions are reachable through native content.
- `ui/compose/trafficlogs/` contains actual Compose layouts and observable presentation state. It does not inflate or embed XML layouts. `LogControlState` is a value/callback object, not an Android View. The six former adapters retain Paging's differ and controller algorithms but render native rows.
- Per-app logs, WireGuard statistics and DomainConnections are owned by their separate groups. Their existing routes and arguments remain intact.

### Unused route retirement

A fresh whole-repository reference search found `RethinkLogBottomSheet` only at its own declaration. The baseline RethinkLogAdapter opens ConnTrackerBottomSheet instead. The unused Kotlin source was retired under the original approved scope; its XML/resources are retained for global reference analysis. The source guard checks that it remains absent.

## Preserved controller contracts

### Search/restoration

`LogSearchState` is deliberately separate from generic text setters. It tracks the previous query even before a listener exists, suppresses equal-text change callbacks, submits only nonblank queries and expands/clears only once. This retains SearchView's listener-installation and `mOldQueryText` behavior.

Connection arguments run before listener installation: the visible argument does not seed the initial empty flow, so the baseline one-second empty-query reset still occurs. DNS installs its listener/collector before ordinary arguments. Scoped WG/RPN/universal routing retains the original split/substring methods, precedence and argument values. DNS `onResume` still reapplies the current filter.

Fragment text restoration occurs in `onViewStateRestored`, after `onViewCreated` argument setup and listener installation. Equal restored text does not deliver a new query; different text does. Disabled-logging restoration does not invoke a listener that was never installed. A view snapshot carries hierarchy text over same-fragment view recreation. Activities deliver text in `onRestoreInstanceState`. Console and Rethink still clear search in `onResume`; filters/pause/dialog drafts were not made newly persistent.

The native search field currently maps first focus to the old root SearchView click/expansion action. Baseline root versus inner text-field tap behavior needs an explicit integration comparison: the source-level query-order checks alone do not prove touch-target equivalence.

### Filters, paging and lifecycle

- The original mode/page count, page choice/tab labels, universal/WG/RPN precedence and VPN-status observation bodies remain unchanged.
- Native HorizontalPager retains a `FragmentState` per page outside lazy composition. The approved pinned Fragment Compose 1.9.0 `AndroidFragment` is only a lifecycle boundary; child content is Compose. The settled page is capped RESUMED and other composed pages STARTED. Runtime offscreen residency, restoration and query-call cardinality are pending verification.
- Original DiffUtil identity/content callbacks, generation handling, snapshots and access hints remain in `AsyncPagingDataDiffer`. Merged keys include source plus ID. No query, sort order, retention, DAO, repository, ViewModel, service or network implementation changed.
- Original view/fragment lifecycle arguments for `submitData` and the baseline `removeObservers(this)` owner mismatch are retained. Merged row jobs remain owner-bound and cancel on row disposal/rebinding; legacy row background jobs remain Activity-bound.
- Required-selection parents cannot be deselected. ALLOWED clears connection query categories; BLOCKED preserves them. Child chips rebuild with the original visual reset and horizontal scrolling. The selected ALL chip still receives its original external deselection callback after the required-selection check is restored.
- Event query sources remain separate from visual source-chip checks. Severity selection clears query sources but does not uncheck hidden chips. Last-source removal retains the existing visibility quirk.
- Connection scroll headers retain the 100 ms throttle; all original timestamps use the existing relative formatter. Native thumb hide delay is 1500 ms. Console retains disabled item prefetch and no fast-scroll/header.

### Console/events

Console keeps newest-first frozen snapshots capped at 5000, local paused filtering, one restart on resume, prefix-based colors, selectable ten-line messages and debug ID timestamps. Level/file-trace choices apply immediately in the original order; Cancel/Done only dismiss. The eight-choice list is capped at 40% of screen height.

Both share handlers, double-tap guard, file naming, WorkManager tag observation, success/failure/cancel/prune ordering and chooser URI grants remain in the original source bodies. No file export was executed. Event timestamp format, severity/source order and ARGB values, detail expansion, message-only clipboard/toast, four-second refresh disabling and delete-then-refresh ordering remain. Animation uses the original accelerate/decelerate curve.

### Details/rendering

Rule cycles, app/global IDs, excluded-app/lockdown checks, in-flight row disabling, rollback/finally paths and persistence-before-live-UI-update remain in the original controllers. `applyFirewallRuleWithLifecycle` is unchanged. DNS retains `view.post`, its 150 ms deferred icon read, failure-cache handling and cache-only fallback sequence. CustomTarget replaces only the ImageView target; no new icon download is introduced.

Native rows/sheets use the existing resource sizes, font families, theme colors, glyph geometry, borders/radii, 65/20/15 row weighting and ellipsis direction. The info dialog preserves selectable spans, separate close row, heading/body dimensions, extra 5dp line spacing and full-width 600dp cap; the blocklist-only dialog retains default width. Its framework close selector is replaced by a supported native vector icon.

Exact font baselines, platform inset/touch differences, RecyclerView recycling artifacts (including stale arrow visibility or empty console row contents), offscreen caching and fast-scroll/access-hint cadence need baseline/runtime review; this document does not mark those checks passed.

## Fresh evidence and test contracts

Run `python scripts/compose-migration/check-traffic-log-controllers.py`.

The fresh guard passes **161** selected baseline callback/helper bodies using a literal-preserving lexer, negative controls and explicit presentation-only normalizations: native dialog builder name, native glyph enum, binding-lifetime field name and nullable presentation getter empty check. It also rejects legacy XML/PagingDataAdapter/AndroidView rendering entry points in guarded files. Results are in `traffic-logs-controller-evidence.json`. This is source equality, not compilation or semantic/runtime proof.

New test classes (authored, not yet executed by this group):

- `LogSearchStateTest`: pre-listener/equal restore, DNS argument order, disabled-listener gate, blank submit, expansion/resume resets and check callback ordering.
- `TrafficLogFragmentControllerTest`: real Fragment view lifecycle in a plain-Application bare host, strict fake VMs/repositories, argument→empty reset, restored text timing/equality, disabled DNS restoration, scoped routing and BLOCKED category retention.
- `ConsoleLogsControllerTest`: actual CREATED Activity with strict mocked collaborators, initial no-write/no-export contract, 5000-row pause cap/order, immediate level/file-trace order and debounce.
- `EventsControllerTest`: strict fake DAO/VM, source/severity transitions, stale visual checks, last-source visibility and debounce.
- `NetworkLogsRoutingTest`: actual CREATED host without STARTED child pages, strict fake service state, mode counts/order, WG exact arguments and universal precedence.
- `TrafficLogsComposeTest`: plain-Application native content with only synthetic records/callbacks; immediate dialog writes/cancel, event expansion/copy, disabled-rule no-write, paging order/cardinality and hidden source selection.

The existing `FirewallRuleApplierLifecycleTest` remains untouched. No new test uses production Application startup, real databases, VPN, probes, permissions, accounts or exports. `NetworkLogsActivityTest` keeps the real host gate with native selectors, explicitly ignored until an isolated Application/synthetic Paging fixture exists. No live instrumentation test was executed; its result remains separate from fake-content coverage.

## Acceptance still required

1. Coordinator compiles current stable sources and test sources, then runs only reviewed plain-Application fake fixtures. No old workspace pass can be reused.
2. Run real host/pager interactions only after an isolated application/service fixture is approved: swipe/tap/back, exact resume counts, argument/restoration timing, paging placeholders/errors/empty states and keyboard behavior.
3. Compare pinned baseline/native rendering for light/dark/black, RTL, narrow/wide/folded windows and large font; include all detail/rule dialogs, text selection, TalkBack order, disabled controls and native fast-scroll gestures.
4. Check rotation/process recreation/offscreen page disposal against baseline, including deliberate draft loss and query reset quirks.
5. Exercise rule errors/cancellation/re-entry and export success/failure/double-tap solely through strict fake managers, WorkManager and chooser/file boundaries. Never activate a real VPN or export live traffic to validate UI.
