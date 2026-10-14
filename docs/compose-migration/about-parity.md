# About and diagnostics Compose reconstruction

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

## Ownership and rendering

- `AboutFragment`: native `ComposeView` and observable `AboutState`; all link, update, contributor, translation, OS settings, sponsor, RPN dashboard, diagnostics, logs and token routes remain wired to the existing controller.
- `BugReportFilesBottomSheet`: native full-height Compose sheet with lazy file rows, independent view/delete actions, selection, process-info inclusion, totals and asynchronous ZIP progress. The existing platform bottom-sheet/window/system-bar host remains.
- `DnsRecordTypesBottomSheet`: native Auto/Manual controls and lazy record rows; Auto retains locked checked rows, parent/row dimming and 200 ms transition. Manual selection writes, selected-first sorting, saved manual selection, mode events and fragment-result notification remain in the controller.
- All formerly programmatic TextViews, Buttons, ProgressBars, RecyclerViews, ListViews, LinearLayouts and ScrollViews in these hosts are removed. No `AndroidView` wrapper is used.

## Diagnostic surfaces

- JVM/Go stack tabs use bounded lazy line rendering. JVM is initially selected; either tab click resets that tab to its first line. Stack lines remain deliberately non-selectable, matching baseline; Copy retains the complete JVM/Go text.
- Proc keeps the exact thread/status/smaps/auxv span formatter and complete-copy text. Metrics remains the actual initial tab, matching `selectTab(false)` in the baseline despite its contradictory comment. Proc/metrics text remains selectable, with per-line spans retained and lazy rendering for large data.
- Stats preserves collection, timeout, progress-dismiss-before-results and liveness guards. Selected rows are copied in source-position order; no selection copies the complete formatted text, including timeout headers.
- Database table selection keeps duplicate-load prevention, loading text, asynchronous append order and copy payload. Query/schema/row-limit/export methods are unchanged. Compose receives only text, table labels and a callback.
- Contributors retains its close icon, centered content, author icon and HTML links. What's New retains rich links, original version slicing and email/close actions.
- Ordinary dialogs remain transient platform Dialog windows with native Compose content and non-dim flags. Button dispatch is action-before-dismiss, guarded against duplicate terminal taps. Refresh explicitly dismisses before requesting a new diagnostic capture.

## Theme, visibility and gestures

- Descriptive About text explicitly resolves `primaryTextColor`, including Frost and Plus. Header, secondary, footer, sponsor and icon colors resolve the same baseline theme attributes rather than assuming light/dark colors.
- Alpha title, F-Droid update/token visibility, error-reporting token visibility, debug-only memory profiling, sponsor/community/RPN alternatives and collection progress are still controlled by the original conditions.
- Token single tap copies; double tap regenerates only through the original controller callback. A native Compose Initial-pass pointer adapter reuses the baseline platform GestureDetector, ensuring the recognized second press wins before selectable Text begins word selection. Unconsumed long presses retain native text selection. Pending confirmation is cancelled when this UI leaves composition. The seven-tap title action is unchanged.
- Bug-report checkbox and DNS mode state replay remains changed-only and follows original listener ordering. Diagnostic text, selections, dialogs, profiles and file payloads are not persisted as UI state.

## Evidence and safety boundary

- `check-about-controllers.py`: passes 81 protected method/section equality checks, allowing only named presentation substitutions. It separately guards statistics collection/timeout and Proc formatting/copy sections, and rejects legacy rendering and production-controller dependencies in the new tests.
- `run-about-presentation-harness.py`: passes six exact-source model scenarios using cached Kotlin 2.4.10 and tiny framework fakes. This does not compile Compose or Android production hosts.
- Added 13 plain-Application Robolectric state/callback tests and 16 native Compose content tests. They use recording callbacks and fake metadata, never instantiate production Fragments/Activities/Application, never invoke VPN/network/profiling/accounts/SAF/permission/export operations, and never open real files or databases.
- Content fixtures cover tabs, bounded large stack data, selected-row copy, table callback/progress text, independent file-row actions, disabled send/progress, locked/manual DNS records, mode callback order, transient state, button identifiers, token gestures, close affordance, theme variants and debug visibility.
- Existing integration tests were not edited. Production compilation and Robolectric execution are owned by the coordinator and are not claimed by this source/harness evidence.

### Fresh coordinator runtime evidence

The coordinator reported all 13 state tests and 14 of the original 15 content tests passing in the isolated batch. The physical token test exposed gesture/selection arbitration and a Robolectric magnifier disposal limitation. The token adapter was corrected to prioritize the baseline recognizer; two physical-token tests now cover tap counts, performClick ordering, exclusion of double-tap word selection, and preservation of long-press selection. These updated fixtures await the next coordinator run. A method-scoped test-only Magnifier.dismiss shadow handles missing compositor surfaces; no production selection behavior is disabled.

The next coordinator full-source run compiled production and unit sources and passed the original physical single/double-tap test. The stronger fixture also passed all double-tap count/priority assertions. Its long-press check initially advanced Android's handler clock only after batched pointer release. The fixture now holds the pointer down while advancing both the Android and Compose clocks, asserts selection while held, then releases and asserts that selection persists without a copy/regeneration callback. This is a test-clock correction; production gesture logic is unchanged. The updated hold/release assertions await rerun.
