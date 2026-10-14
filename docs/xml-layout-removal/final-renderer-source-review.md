# Final renderer source review

## Result

One interaction-state defect was found and corrected in source during this finite independent review. No additional actionable Kotlin/API or callback/disabled-state defect was established in the six reviewed files. This is not a compile or runtime pass.

- Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`
- Checkout HEAD: `ff0510e327efb5f65c4aa8f958c4b1650bd8d569` (reviewed production files include working-tree changes)
- Dependencies remain MDC `1.14.0`, Compose BOM `2026.08.00`; minSdk 23, compileSdk 37
- No production code was edited by the reviewer; no Gradle, binary download, or device execution was attempted.

## Corrected finding: FR-01 (P2)

The original slider renderer observed only `PressInteraction`. Compose Slider cancels its tap press when dragging takes over and separately emits `DragInteraction`. That made the pressed colors, narrow Material handle, adjusted gap and halo revert during an active drag. MDC 1.14 keeps its pressed thumb state throughout `ACTION_MOVE`.

The final `OriginalSliders.kt` now uses `originalSliderActiveInteraction` with `collectIsPressedAsState` and `collectIsDraggedAsState`, returning `pressed || dragged`. All four visual-state branches use the result. Value and finished callbacks remain forwarded unchanged. The final source was re-read; the authored Press → DragStart + PressCancel → DragStop test remains uncompiled and unrun.

## Reviewed boundaries

- Compound controls: nullable/passive callbacks, enabled selectable/toggleable actions, disabled drag removal, current-state/callback capture, changed-only switch release, cancellation cleanup, tint reset, drawable scheduling/disposal, final thumb-following ripple change
- Sliders: callback/range/steps forwarding, custom-slot call signature, active/disabled state inputs, RTL and label resource access
- Progress: public MDC spec fields/factories, clamped range semantics, visibility/disposal, direction and secondary progress mapping
- Tabs: empty-label handling, bounded interpolation indexes, host selection callback, selectable semantics and RTL scroll/indicator positioning
- WireGuard text: default/source/live resolution paths, state-color inputs, min/max-line validity, font API guards and catalog references; all 186 keys are unique and their style resources exist. All five referenced progress value styles also exist.

The exact guard exemptions are non-View types: `LinearProgressIndicatorSpec` and `CircularProgressIndicatorSpec` extend `BaseProgressIndicatorSpec`; `MaterialShapeDrawable` extends `android.graphics.drawable.Drawable`. Official MDC 1.14 source confirms those inheritance chains. This does not justify a package-wide exemption for actual widgets.

## Final production pins

Paths are relative to `app/src/main/java/com/celzero/bravedns/ui/compose/`.

- `controls/OriginalCompoundControls.kt`: `5e3d3f1377ad72f46f4e239bae8438730c227db87760257a6fb24683fdf37e00`
- `controls/OriginalSliders.kt`: `012dd26051058ddf0c1a43c1bbe4e58b351be4c003f7a35e50217222c2a10157`
- `controls/OriginalProgress.kt`: `611f1dccfc21681a00b84bed4b175ada13b9fcb3114bd6dc79df457c7186ee60`
- `controls/OriginalTabs.kt`: `0f0890d5762ebd45a9e764a4c49e327260d7626a4a51b8786dcecefab48f1d53`
- `wireguard/WgTextStyles.kt`: `dc4c542042c3f0fb26a97d8ee7c84fa3480394c01266c0a523b59c05371e57b4`
- `wireguard/WgTextCatalog.kt`: `cec647211a7616ca9892c2fd2c4d04beea7af55c2ef5def8a4fda8d463fa3323`

## Source evidence and remaining gates

Official upstream source was read through the GitHub connector. Exact returned blob hashes and local supporting-file hashes are recorded in the JSON companion. Key references:

- [MDC 1.14 slider pressed-state behavior](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/slider/BaseSlider.java)
- [AndroidX slider interaction and custom-slot implementation, pinned pre-BOM commit](https://github.com/androidx/androidx/blob/00750b0544d71f1267b80da4ded1c1b8322ae273/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/Slider.kt)
- [MDC 1.14 progress base spec](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/progressindicator/BaseProgressIndicatorSpec.java)
- [MDC 1.14 MaterialShapeDrawable](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/shape/MaterialShapeDrawable.java)

No Kotlin/AAPT/lint, Robolectric, instrumentation, screenshots, actual-host measurement or TalkBack execution was performed. The exact resolved Compose artifact graph and its API/resource linkage remain unverified; the custom-slot call shape matches the official 1.4 API and inspected pre-BOM source. Newer AndroidX main has a different 1.5 Slider API and was not treated as the declared dependency. Gesture ordering, disabled mid-gesture behavior, animation lifecycle and visual/theme/font-scale parity still require runtime validation. The review does not establish whole-app business-logic equivalence.
