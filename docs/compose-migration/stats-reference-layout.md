# Stats reference layout restoration

Status: source changes and source/mutation checks only. Android compilation, Robolectric execution, new rendering, and device verification are **UNRUN**. No downloads, Gradle invocations, production application, VPN, or network traffic were used.

## Evidence and interpretation

The supplied current image 2 and target image 5 were visually inspected. The target has the right chart button selected, while the current image selects the left list button. This is a presentation-mode difference: the original source expressly has separate List and Insights usage containers. The restoration keeps the controller's persisted mode and both sets of usage totals rather than forcing the reference's selected mode.

Pinned source: commit `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`, `app/src/main/res/layout/fragment_summary_statistics.xml`, and `SummaryStatisticsFragment.kt`. No deleted XML layout or binding was restored to production.

## Restored source contracts

- Header: original `TextAppearance.Heading.HomeScreen` and `TextAppearance.SubHeading.HomeScreen`, half-opacity colorOnSurface, uppercasing for ordinary builds, and the original alpha-build case exception. Locale lookup supports the application's API 23 minimum. Source header margins remain 16dp start and 12dp end.
- Insets: Stats consumes top/horizontal safeDrawing only. The existing home navigation clearance remains responsible for bottom space.
- Period selector: centered, joined three-segment group with three original 100dp-minimum controls, negative one-stroke spacing, original style keys, selected colors, radio semantics, and callbacks. Shared OriginalControl owns the outline, height, and native-style drawing. LazyRow keeps controls reachable if text or available width causes overflow.
- Usage card: shared OriginalCardSurface restores theme fill, elevation, and compatible shadow/corner clearance, with this source instance's explicit 16dp radius, 1dp secondaryTextColor border, 12dp top margin, and 16dp side margins.
- Insights: one card containing three equally weighted columns. Each has a single-line 18sp bold value above an 11sp caption with 2dp spacing. Two 1dp vertical separators retain 6dp horizontal and 4dp vertical margins. Content padding remains 14dp.
- List: its existing progress/label presentation remains list-only. Total = download + upload; unmetered = total - metered. Rendering does not write controller, persistence, or query state.

The source dimensions are density-independent Android resources and dp values. Screenshot pixels were not used as layout dp.

## Regression coverage

`SummaryStatisticsReferenceLayoutTest.kt` adds five synthetic tests covering centered period geometry/radio actions, equal KPI columns and vertical rules across widths/density/font scale/RTL/themes, original header typography and alpha exception, preservation of mode/period/independent totals and callback counts, and nonblank center glyph painting for list/chart/filter. Plain Application and fake state/callbacks only. All five tests are authored but **UNRUN**.

`check-stats-reference-layout.py --self-test` checks the pinned XML, current Compose structure, state routing, source spacing/typography, and test coverage. It passed 22 usage contracts and rejected 19 negative mutations. Its JSON report is `stats-reference-layout-source-results.json`.

The existing `check-dns-statistics-controls.py --self-test` was narrowly updated to require centered time segments while retaining two joined groups. It passed 47 source instances, 22 action mappings, and 14 negative mutations. These are source checks and do not establish visual equivalence.

## Shared dependencies

The screen intentionally reuses OriginalControl, OriginalCardSurface, utilityTextStyle, and themeColor. Blank icons, missing strokes, and collapsed style heights require the shared resource-resolution fix; there is no duplicate Stats-only renderer. Screenshot matching still requires the allowed Android toolchain and new render/device evidence.
