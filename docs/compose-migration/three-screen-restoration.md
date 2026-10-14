# About, Stats and DNS screenshot restoration

Status: implemented and source-reviewed; **Android compilation, resource linking,
Compose tests, corrected rendering and device accessibility validation are UNRUN**.
The current toolchain/artifact-access restriction is unchanged. Do not infer visual
acceptance from source checks or authored tests.

The six supplied screenshots were visually inspected. Matching by content gives
About 1→6, Stats 2→5 and DNS/blocklists 3→4. Original images are not committed.
The target images have a much larger raster size; measurements are normalized by
viewport width, and live data, build flavor, system-bar crops and selected modes
are distinguished from layout defects in `three-screen-reference-audit.md`.

## Corrections

- About: original introduction wrapper/paragraph spacing and line spacing;
  rounded outlined Sponsor and paired Telegram/Bug report actions; source heading,
  caption and row typography; original section grouping. Live usage text, update
  eligibility and all callbacks remain controlled by existing state.
- Stats: source uppercase/tracked heading (with original alpha exception), centered
  joined period selector, source card padding, equal-width value-over-label KPIs
  and vertical separators in Insights. The current screenshot selects List while
  the target selects Insights; saved mode and independent totals are preserved.
- DNS/blocklists: source-size joined selector, native bold group headings, native
  elevation overlay on the source background/selected fill, fixed full-height
  footer and safe drawing insets. Existing selection, search, grouping, apply,
  discard and paging/scroll behavior are preserved.
- About/Stats own only top and horizontal safe drawing insets because Home owns
  bottom navigation. Standalone DNS owns its full safe drawing insets; embedded
  bottom-sheet content does not add another copy.

## Shared defects corrected

Android requires attribute IDs supplied to style queries in ascending order.
The manual presentation arrays grouped IDs by meaning instead. The new
`OrderedStyledAttributes` adapter sorts/deduplicates the platform query and maps
all reads back to the original declaration indices, without mutating caller
arrays. The 62 Compose queries now use the adapter, including source/default
styles, overlay resolution, explicit-empty/null tests and text/card dimensions.
This is a resource lookup repair, not a new palette or component design.
[Android Theme API contract](https://developer.android.com/reference/android/content/res/Resources.Theme).

MaterialButton treats iconSize=0 as the drawable's intrinsic dimensions. The old
Compose renderer instead created a zero-sized icon box. It now converts the
painter's independent intrinsic width/height from pixels to dp, preserves explicit
sizes, and lets caller-supplied icons keep their dimensions when unspecified.
[MaterialButton 1.14.0 source](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/button/MaterialButton.java).
These are established source/API defects; their corrected device appearance has
not been observed in this environment.

## Verification

Run the source-only matrix:

```sh
python3 scripts/compose-migration/check-final-source.py --report docs/compose-migration/three-screen-source-checks.json
```

It includes the original controller/core/no-layout/no-binding protections and
four focused guards with deliberate negative mutations. The compound-control
census and three existing guards change only their lookup-method expectations;
resource IDs, positions, source style evidence and callbacks are not relaxed.
The machine report records each command and exact app source hashes.

18 new synthetic test methods are authored across About (6), Stats (5), DNS (3),
ordered style queries (2) and original controls (2). Tests use a plain Application,
synthetic state and callbacks. The resource adapter oracle uses independent
single-ID native queries across six themes. The control oracle compares native
minimum-height and icon-size properties; that alone does not prove measured or
painted bounds. Separate Compose geometry/glyph tests are authored, still UNRUN.

When the approved Android toolchain is available, required checks include:

```sh
./gradlew :app:compileFdroidFullDebugKotlin :app:lintFdroidFullDebug
./gradlew :app:testFdroidFullDebugUnitTest --tests '*AboutReferenceLayoutTest' --tests '*SummaryStatisticsReferenceLayoutTest' --tests '*BlocklistsComposeTest' --tests '*OrderedStyledAttributesTest' --tests '*OriginalControlsTest'
```

Then inspect isolated captures at matching viewport density for all three screens,
both Stats modes, six themes, RTL and large text. Check bottom navigation anchoring,
scroll/footer separation, labels/icons, selected/disabled colors, clipping, focus,
back/recreation and callbacks. Play/Website and device behavior remain separately
unverified. No production Application, VPN, traffic, permission or account action
was used in this repair.
