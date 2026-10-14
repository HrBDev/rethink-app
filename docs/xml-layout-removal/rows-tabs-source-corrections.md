# Bubble, app-connection rows and tab-strip source corrections

Scope: S21, S24, S27. Original source is pinned to `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`; the pre-patch Compose host is `e17aaecf8185f06f1888d50cd2ad250f085d5374`.

## Bounded changes

- `BubbleScreen.kt`, row functions only: outer AndroidX CardView defaults and compat clearance replace default Material3 Card styling. Allowed and blocked margins remain separate (4/3dp versus 6/6dp). Both frames are 44dp circles with 28dp images. Content uses 12dp horizontal / 10dp vertical padding and top anchors. App names retain the selected, forever one-line marquee. Blocked package clipping follows the adapter's unselected package TextView, and count/time are neighboring captions. The existing greater-than-1.3 font-scale action reflow is preserved as an accessibility alternate; it is not claimed to be the original default-font layout. OriginalButton retains the source key, visual dimensions and callback, using its compact grouped layout option so a 48dp spacer does not shift the source top anchor; platform touch hit slop remains.
- `AppConnectionRows.kt`: country remains a separate 4/16dp-inset, 44dp-minimum inner row. Domain/IP share only genuinely matching source contracts. Titles retain source appearance/dimension/start ellipsis without an invented maxLines; subtitles retain three-line end ellipsis. Country names remain bold, one-line, end-ellipsized. Counts retain sans-serif-light bold, 40–60dp bounds, 5dp padding, source dimension and the country-only .85 alpha. Flag/icon, arrow INVISIBLE/GONE reservations, divider and progress insets are source-specific. Original annotated text, state colors, progress colors, enabled state and callbacks are preserved. The country 3dp/2dp-radius progress differs from the inherited domain/IP progress thickness; RTL progress starts at the source start edge.
- `CustomRulesActivity.kt` and `NetworkLogsActivity.kt`: only the tab renderer/import changes. The shared `OriginalTabs.kt` resolves `TabbedLayoutDefault`, its theme overlay, `TabTextDefault`, state colors, ripple, indicator/background drawables, spacing and source scrollable/center behavior. It uses 48dp text-tab height, 25dp label insets and the source content-start adjustment; equal-width centering is used only when source gutters fit. Long labels retain the two-line sizing rule. Indicator drawing uses the original drawable with full-width geometry, source gravity/height and elastic edge motion; pager offset and RTL selection scroll are retained. There is no default Material3 divider. These two hosts are text-only; the source icon tint is resolved but no new tab icon is introduced.

Surrounding BubbleScreen shell and NetworkLogs console-icon geometry are outside this bounded task. No rule dialogs, original button/field helpers, app-details sheet, controllers, layout XML or production runtime were changed.

## Source evidence and validation

`check-rows-tabs-source-styling.py` reads the immutable original layouts/styles directly from Git, asserts their independent contracts, pins their SHA-256 values and checks the corresponding production integration. It verifies the entire activity sources are byte-identical to the pre-patch sources outside the scoped tab strips/imports, and that both Bubble age/content/callback bodies are unchanged. The report is `rows-tabs-source-style-results.json`.

Results:
- 31 focused source checks pass
- 17 in-memory negative mutations are rejected
- Utility controller guard: 116 exact/narrowly adapted bodies and 7 protected dependencies pass; its extractor negative controls pass
- Custom-rules source guard: 19 unchanged methods and 9 rule data/manager boundaries pass
- Traffic-log source guard: 161 pinned bodies and native rendering boundaries pass, including extractor negative controls
- Scoped whitespace check and Python guard syntax check pass

The source-style oracle is the project's preserved resource hierarchy. Inherited MDC layout behavior was checked against the pinned dependency's public upstream source: [MDC 1.14.0 tab styles](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/tabs/res/values/styles.xml) and [MDC 1.14.0 TabLayout](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/tabs/TabLayout.java). Those explain the transparent OnSurface background, inherited indicator resource/elastic mode, source scrollable minimum width, center-width equalization, label sizing and effective content inset. No dependency artifact was retrieved.

`OriginalRowsAndTabsTest.kt` adds five isolated tests: actual MDC style resolution across six source themes; source tab-width/elastic-direction math; silent recomposition and tab callbacks; country/IP text/visibility distinctions; and large-font Bubble callbacks/name limits. All use a plain Application and synthetic state. They are **authored and unrun**, not execution evidence.

## Remaining gates

No Kotlin compilation, Android resource linking, Android lint, isolated runtime tests, screenshots, TalkBack or production-host execution ran. The existing dependency-route policy denial remains; no download, alternate route, VPN, traffic or production runtime was attempted. The existing app-state-host launcher was invoked without required arguments and exited at argparse; it did not run a build or test. Exact font metrics, text wrapping, touch targets, ripple timing, indicator motion and layout bounds remain pending approved isolated execution. Source-only checks do not authorize deleting layouts or claim pixel equivalence.
