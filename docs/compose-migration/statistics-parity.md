# Statistics native Compose reconstruction

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
This is evidence for the current reconstructed workspace. Results from the lost workspace are not claimed.

## Implemented scope

All content is native Compose in these retained routing/lifecycle hosts:

- `SummaryStatisticsFragment`: List/Insights modes, eleven sections, three time categories, section filters/badge, usage/KPI cards, native donut/map/rank charts, detailed routes, close-all confirmation.
- `DetailedStatisticsActivity`: intent and time decoding, paged native rows, original empty illustration and terminal-empty behavior.
- `DomainConnectionsActivity`: domain/IP/flag/ASN/blocklist routes, scoped/global blocklists, multi-tag filters, per-app expandable domain groups.
- `SummaryStatsFilterBottomSheet`: six stable multi-select chip identities, selectionRequired, hidden-provider behavior, immediate filter callbacks, Select All, Apply and checked-child restoration.
- `RpnLogActivityIntervalBottomSheet`: shared native Home interval presentation, same query windows, grouping, expand limits/generation handling, and signature.
- `RpnStatsBottomSheet`: loading/content/error branches, transferred bytes, connection/blocked/app metrics, native top-app rows, log route and signature.
- `SummaryStatisticsAdapter`, `DomainConnectionsAdapter`, `BlocklistAppsAdapter`: non-View native presenters, preserved dispatch/query/expansion semantics.

Helpers live in `ui/compose/statistics`. `ui/compose/appdetails/AppDetailsCharts.kt` supplies the shared native country map, drawable glyph rendering and exact legacy donut renormalization. `StatsInsightsMath`, `CountryInsightsMapper`, `WorldMapPaths`, and `LogActivityWindow` remain byte-identical to baseline.

The Home worker owns `LogActivityIntervalBottomSheet`, shared `AppActivityAdapter`, and `LogIntervalPresentation/Content`. The app-details worker owns `AppConnectionsPresenter` and per-app screens. Statistics reuses their native APIs without replacing their files.

## Behavior details protected

- Summary query references retain all eleven sources, feature gates, first load-more priming before lazy-source observation, snapshot/load-state listener detachment, time-category propagation, and persisted mode writes only on change. Recreated views reattach their observers without re-priming the cached sources.
- Detailed/Domain intent extras and enum IDs retain original values and setter order. Direct row routes retain their destination, payload, blocked bit, scoped UID and time category. AppInfo's blocklist caller keeps the original Summary adapter constructor/setter API.
- RPN interval query/load/expansion/render bodies and RPN stats query/log/loading/error bodies are compared against baseline. Only renderer assignments are replaced.
- Summary chips start from six checked XML-equivalent children. Initial seeding is silent. Saved checks replay in XML child order, including a hidden provider, enforcing at least one selection at each transition. Select All emits each actual newly checked child. A final-child uncheck emits nothing.
- Domain blocklist chips retain their different last-selection rule: the legacy listener rechecks a cleared final tag and performs one nested nonempty refetch. The native callback also refetches exactly once even if the effective selection remains unchanged.
- Native country polygons use the original quantization, fit/centering, intensity alpha and fixed-pixel outline. Donuts retain top-five slices, full-ring renormalization, 0.14 ring width, -90 start, 1.5-degree gaps, >2-degree drawing threshold and original stepped alpha values. Summary rank bars retain all rows and blocked-per-row coloring.
- Content uses current theme resources, start/end padding, wrap-capable filter groups, scalable text and native semantics. Filters, rows, actions and chart summaries are available to accessibility. Visual/device parity is not claimed from source checks.

## Current evidence

Executed locally:

```
python3 scripts/compose-migration/check-statistics-contracts.py --self-test --report docs/compose-migration/statistics-controller-source-report.json
```

PASS: 32 baseline-equivalent controller methods; exact row dispatch, intent/setter ordering, query references, enum IDs, protected math/assets, six-chip restoration and native-renderer boundaries. Negative controls deliberately reverse a query window and change an AppInfo UID, and are rejected. The JSON records the current owned-source hashes.

`git diff --check` on the nine owned baseline source files passed. The coordinator reported no production Kotlin errors for this group in its second compiler snapshot; the coordinator owns full compilation and test execution, and reports those results separately.

### Authored safe regression tests (execution pending coordinator)

Under `app/src/test/java/com/celzero/bravedns/ui/compose/statistics`:

- `StatisticsSelectionParityTest`: 4,096 seed/saved-state combinations against an independent checked-child oracle, callback order, selectionRequired, hidden providers, tag fallback/order, all eleven section/metric mappings and donut normalization.
- `StatisticsContentTest`: fake native content/callbacks for modes, three time tabs, filter selection/apply, close cancel/confirm, RTL and large font controls, RPN loading/content/error/log action, and paged empty/content transitions.
- `SummaryStatisticsRouteTest`: captured Intents for eight aggregate routes across all three time categories, scoped blocklist UID, and top-active AppInfo UID. Strict config/store mocks must remain untouched.
- `BlocklistAppsContentTest`: injected fake identity and in-memory child queries, expansion/collapse, cached late results, tag-submission reset and old-result isolation.
- `StatisticsMaterialReferenceTest`: offscreen Material widget reference fixtures, using the existing six-chip XML resource to compare actual checked-child hierarchy restoration and callback traces; verifies the distinct blocklist final-tag recheck cardinality. These fixtures never launch a production host.

All Android-dependent fixtures explicitly use Robolectric SDK35 with plain `android.app.Application`. They do not initialize the production application, databases, repositories, VPN engine, real traffic, exports or permissions. Pure state/math tests do not create an Android host.

## Explicitly pending integration and visual evidence

The original `SummaryStatisticsFragmentTest.testStatisticsDisplay` remains an explicitly ignored integration contract, with the root Compose route `root_nav_${R.id.summaryStatisticsFragment}` and native time-chip assertion. Its original class/method identity is preserved. Running it still requires an isolated fake-service/database host fixture; no fake content test silently replaces that integration contract.

Production-host lifecycle/recreation, paging-driven route interactions with safe service/database fakes, device screenshots, large-font/RTL visual inspection and screen-reader traversal remain pending coordinator validation. Source equivalence and offscreen content tests are not substitutes for those checks. No production startup, VPN activation, network probes, real-data mutation, permission setting, dependency update or external write was performed by this worker.
