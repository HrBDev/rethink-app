# About reference-layout restoration

Evidence: the supplied current About screenshot (image 1) and original About screenshot
(image 6) were visually inspected before editing. Their different raster dimensions do
not justify scaling the whole screen or hardcoding their system-bar offsets. Original
layout/style evidence is pinned to `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

## Corrected source regressions

- The introduction card had flattened its inner `6dp` horizontal / `5dp` vertical wrapper,
  `20dp` paragraph inset, and paragraph-specific margins into generic `10dp` padding.
  Those groups and the original `5dp` extra line spacing are restored.
- The sponsor control was flush against the card with a `4dp` outline radius. Its original
  `16dp` outer horizontal margins, `10dp` top / `16dp` bottom margins, `16dp` outline radius,
  `0.75dp` stroke and `12dp` vertical control padding are restored. The original compound
  drawable alignment is reproduced: a `24dp` start icon with `-15dp` drawable gap leaves a
  `9dp` text-area offset. The text remains centered within that area.
- Telegram and Bug report were stacked vertically. They now occupy the original equal-width
  columns with `20dp` outer margins, `10dp` center gap and their own captions immediately below.
  Report progress remains in the report column, honoring the controller's visibility state.
- The heading had a forced bold face and `7sp` tracking. It now resolves the original heading
  TextAppearance, including smallcaps family, `24sp` size and `0.25em` tracking. The subtitle,
  body and row text use their original source typography and include-font-padding behavior.
  The two action captions retain their themed TextView default color/face.
- Section container top/bottom padding is restored. App has `10dp` above and `15dp` below;
  subsequent sections have `16dp` above/below. List icons retain their source drawable tint
  and alpha. Stacktrace's original `21dp` start margin / `22dp` drawable gap is retained.
- About adds only real top and horizontal safe-drawing insets. The Home shell continues to
  own bottom-navigation clearance. No screenshot-derived fixed top spacer was introduced.

`AboutFragment.kt` and `AboutState.kt` remain byte-identical to pre-change
`cbe1a9f3cb0ef6cdac5d0ced561f67a79c68d001`. Usage days/cost remain live controller text.
Update visibility remains flavor-conditioned; no missing screenshot action was forced on.
All changes are Compose presentation only. No layout XML, binding or production service
startup was introduced.

## Validation and limits

- `python scripts/compose-migration/check-about-reference-layout.py --self-test --report docs/compose-migration/about-reference-layout-source-report.json`
  checks the pinned XML geometry, new source contracts, unchanged controllers/state, and the
  safe fixture boundary. Negative controls deliberately mutate geometry, grouping, typography,
  insets, callbacks, usage values, visibility and the test host, and must be rejected.
- Existing `check-about-controllers.py`: 81 protected controller/collection/format checks pass.
- Six authored plain-Application Compose fixtures cover paired equal-width controls and
  recording callbacks, RTL/large text, report progress containment, live usage/sponsor changes,
  channel-conditioned update visibility, and source header typography.
- Android compilation: UNRUN. Compose bounds/callback execution: UNRUN. Device rendering and
  screenshot comparison after the patch: UNRUN. Toolchain execution remains prohibited for
  this task; no Gradle, runtime harness, binary or dependency download was attempted.

Static checks are source evidence, not proof of pixel-perfect rendering or runtime success.
