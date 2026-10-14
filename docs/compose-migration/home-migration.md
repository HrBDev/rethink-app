# Home native Compose reconstruction

Pinned baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

## Reconstructed source

- `HomeScreenFragment`: ComposeView host, retained controller methods, lifecycle observer/result registration order, routing, permissions, local-network hard gate, VPN start/stop/pause and throttles. Presentation state replaces ViewBinding.
- `ui/compose/home/HomePresentation.kt`: observable per-view nodes, required single-choice groups, pager callbacks and rule counts, heatmap/histogram presentation. It invokes no service, repository or persistent setting itself.
- `HomeContent.kt`: native regular/w600 arrangements, protection/uptime status, DNS/throughput, rules pager, proxy health, logs pager, exact heatmap geometry/intensity and hit mapping, top-app stacked histogram and selection, app counts/warning outline, pause/start/settings controls, native animated border.
- `LogActivityIntervalBottomSheet`, `LogIntervalContent.kt` and `AppActivityAdapter`: native exact-window sheet, range selectors/counts, expandable app/domain rows, cache-only favicon fallback and retained per-window query generation/merge/cap behavior. RPN interval shell is owned by the statistics worker and reuses this adapter.
- `HomeDialogs.kt`: six original dialog flows, original labels/cancelability/callbacks, action-before-dismiss ordering and terminal gate per transient dialog instance. HTML emphasis is converted to Compose spans.
- `HomeTourAnchors.kt` and `TourOverlayController`: shared window-bound anchor registry used by Home/RPN/navigation, native tooltip and spotlight, scroll reveal, original seven-step manager order, next/skip/close/touch completion and 180 ms closing callback. Hidden owners and disposed registrations do not remain usable anchors. Native View lookup remains only as compatibility for retained platform/host UI.
- Notification denial uses the shared `ComposeSnackbarPresenter` with Activity-root host, unthrottled stock Material appearance and `LENGTH_LONG` semantics, not an in-fragment snackbar.

The tour business managers, data/services/ViewModels, manifest and VPN logic are untouched.

## Verification

Run `python docs/compose-migration/check_home_controller.py --report docs/compose-migration/home-controller-source-report.json`.
The report records exact protected controller bodies plus narrow allowed native-dialog/snackbar presentation substitutions. This is source-equivalence evidence only, not runtime evidence.

Safe focused tests:
- `HomeDialogTest`: six pure callback tests for terminal dispatch, noncancelability, action/dismiss order, replacements, failure and foreign actions.
- `HomePresentationTest`: ten plain-Application Robolectric tests for selections, per-view resets, histogram selection, pager callback cleanup and tour-anchor ownership/visibility.
- `HomeContentTest`: six plain-Application Compose/Robolectric renderer tests with synthetic callbacks for disabled start, independent navigation, chip selection, semantic heatmap activation, wide/RTL/large text controls and interval expansion.
- `AppActivityAdapterStateTest`: six pure synthetic tests for expansion requests, delayed results after collapse, child-cache reuse, window reset and child caps.

No production Home Activity/Fragment lifecycle is exercised. No test instantiates a production Application, starts a service or loads a backend. Instrumentation is excluded. Current test execution and compiler outcomes are coordinator-owned; test source alone is not a passed result.

## Deliberate parity / review notes

- Presentation caches follow the fragment's baseline in-memory restoration rules; no new persistence or backend policy is introduced.
- Existing disabled-logs behavior hides the logs parent card. Its inline enable control therefore remains hidden with that parent, as in the pinned baseline. This migration does not change that controller behavior.
- Large text can grow content instead of clipping fixed-height card contents; all card actions remain in their original layout groups.
- Home's mode sheet is separately owned. RPN's interval shell must land with the shared native adapter before compiling the complete consumer group.
- XML resources are no longer inflated by the migrated Home/tour renderer, but remain as resource-ID compatibility declarations until repository-wide unused-resource cleanup.
