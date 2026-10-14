# App list, app details and per-app logs: reconstructed Compose surfaces

Base: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`. Current checkout: the reconstructed `rethink-compose/source` tree. This note supersedes claims about the lost pre-restart app-details checkpoint. Source implementation is present; Android compilation and runtime acceptance must come from the coordinator's new build of this tree.

## Owned scope

Four Activities (`AppListActivity`, `AppInfoActivity`, `AppWiseIpLogsActivity`, `AppWiseDomainLogsActivity`), three bottom sheets (`FirewallAppFilterBottomSheet`, `AppIpRulesBottomSheet`, `AppDomainRulesBottomSheet`), five adapters/controllers (`FirewallAppListAdapter`, `AppWiseIpsAdapter`, `AppWiseDomainsAdapter`, `AppWiseCountriesAdapter`, `DomainRulesBtmSheetAdapter`), dedicated Compose presentation/state files under `ui/compose/appdetails` and `ui/compose/firewallapps`, and separate safe tests.

Statistics owns the shared `AppDetailsCharts.kt` implementations (DrawableImage, appDonutSweeps, AppCountryMap) and the compatible SummaryStatisticsAdapter used by the blocklist section. Framework Activities/fragments/dialog windows remain lifecycle and route hosts, but these owned surfaces render native Compose content. Existing layout resources remain inert reference material. No AndroidView wrapper is used.

## Preserved behavior

- App UID/package routes, per-package lookup fallback, invalid/tombstoned app handling, self-warning acknowledgment and Back, and system app-info intents remain.
- Six per-app firewall controls retain their original guard/write/log bodies. Shared-UID confirmation remains noncancelable; positive/neutral choice ordering remains. Unknown packages disable exclusion and lockdown blocks its controller action.
- Proxy exclusion and temporary allowance still use the original checked-listener semantics. WgControlState's equality/broadcast guard preserves initial asynchronous assignments and one callback per actual checked-value change.
- Notes retain draft Bundle keys, reopen-after-initialization behavior, original in-flight save guard, trimmed save, success/error event handling and event logging. Native edit filtering preserves the original 500 UTF-16-unit limit including surrogate pairs and middle-insertion suffix preservation. Opening/rendering does not save.
- IP/domain rule sheets preserve the original rule methods, CIDR rejection, null port/blank proxy metadata, event logging and framework dismissal refresh notification. Embedded domain rows retain addDomainRule, distinct from the standalone domain sheet's changeStatus. The proxy picker remains hidden exactly as the baseline shared layout specifies.
- All nine app-detail list/insights sections retain original query branches/order, optional ASN gating, seven-day blocklist scoping, distinct Rethink sources, persisted presentation mode and drill-down intents. Snapshot updates and observer removal are explicit; Compose rendering cannot call writes.
- AppWise controllers preserve Paging diffs, snapshot math and load hints. Countries use current-snapshot logarithmic scaling; IP/domain bars keep their adapter-lifetime scaling. Native favicon targets remain cache-only and are cleared on row disposal. Active-row confirmation captures the clicked connection and retains the exact close reason/UID/domain arguments.
- Per-app log source precedence remains country → Rethink → ASN → IP. Optional blocked filter is set before observation. Rethink active uptime, disabled ASN/country search, conditional time/delete controls, delayed query callbacks, onResume query clearing and keyboard restart remain.
- Time selection initializes to seven days without a listener command. Restoring a different saved time dispatches once after host restoration; unchanged seven days does not dispatch. This mirrors the checked-value guard previously provided by MaterialButton.
- AppList retains its exact initial order: reset filters, initialize Paging/query observation, initialize filter observation, then action listeners. Search text is restored only after super.onRestoreInstanceState; AppSearchState rejects equal-text change dispatch just as SearchView does. The debounced/distinct flow and filter update bodies are unchanged. AppList resume preserves query and clears focus, while AppWise resume intentionally clears query.
- AppList bulk operations, their per-icon tags and icon reset behavior, confirmation gates, event details and refresh timeout remain. The first DNS/firewall bypass tap still displays its explanation instead of applying a rule. Native tooltip, rotation and fast-scroll gestures retain those controls.
- Filter sheets keep an independent draft; selected unavailable categories are not deleted merely by changing top-level type. Apply publishes the draft. Clear resets only categories/top-level/sort and preserves current search/firewall filters. Top-level initialization still reproduces the existing possible second category query when selecting a nondefault parent.

## Executed checks

- `check-apps-controller-parity.py`: PASS. 67 complete command methods compare byte-for-byte against pinned Git objects. Its fresh per-method hash report is `apps-controller-source-report.json`. This proves selected method text, not runtime invocation order/cardinality.
- `check-apps-state-host.py`: PASS. 12 isolated checks compile exact current search/note helper bodies using the approved cached Kotlin 2.4.10 compiler and JDK17, against tiny host-only type stubs. Checks cover initial/equal/change/submit query dispatch, restore/clear behavior, UTF-16 limit, middle paste and cursor/composition handling. This is not Android or Compose runtime evidence.
- Owned direct app-resource scan: no missing named resources found. This is not AAPT linking.
- Owned-path whitespace checks: PASS at the reconstruction checkpoint. No whole-tree claim is made while other feature groups change.

## Authored but not yet executed

15 separate plain-Application Robolectric/Compose tests in AppDetailsReconstructionTest and FirewallAppsReconstructionTest. They use synthetic presentation state and strict callback lists; no production Activity/Application, Koin, VPN, network client, manager, repository, export or permission is launched.

The original 20-test FirewallAppListAdapterTest remains present. Only four obsolete RecyclerView-specific cases were changed to exercise real native row construction, empty snapshots, bounds-safe section lookup and native presenter inheritance; its old business assertions remain. An explicit plain Application override prevents production startup. This legacy suite has not been executed here, and its pre-existing mocked-update checks are not treated as evidence of real business behavior.

## Outstanding acceptance

Coordinator Android builds/typechecking, safe selected JVM tests, instrumentation compilation, mocked controller trace coverage, and device visual/RTL/large-font/TalkBack/rotation/process-death/focus/predictive-Back/TV parity remain required. In particular native keyboard focus, scroll restoration, fast-scroll affordance and window sizes need runtime review. No source-marker count or byte-identical method check is a substitute. This worker has not run Gradle concurrently, activated protection, used real traffic/accounts/credentials, changed permissions, exported data, committed or published anything.

### Coordinator compile feedback

The coordinator's fresh second snapshot Android compilation reported three owned nullability errors in native row text assignments. These are repaired with `orEmpty()` at `AppWiseDomainsAdapter` domain/flag assignments and `AppWiseIpsAdapter` ASN-title assignment. Baseline TextView.setText(null) displayed an empty string; the nonnullable presentation field now receives that same display value explicitly. No rule, repository, intent or manager method changed. A coordinator recompile is still required before an Android build pass can be claimed.
