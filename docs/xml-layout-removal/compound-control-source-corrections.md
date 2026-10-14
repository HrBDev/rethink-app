# Compound-control source corrections (S08, S09, S10, S25)

## Bounded result

The immutable baseline is `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`. The finite census covers 154 original nodes in 52 layouts: 72 MaterialSwitch, 50 radio nodes, 28 checkbox nodes, three AppCompatSeekBar and one Material Slider. Every node retains a source key and original attributes. 151 active nodes map to 152 actual Compose render paths; three independently checked inactive exceptions remain explicit in the census. See `compound-control-source-audit.md` and the machine-readable source results for the mapping and regression-check outcome.

Production changes are limited to source-widget rendering, presentation metadata, local semantic grouping and their existing callback routes. The helpers construct no Android View, inflate no layout and call no service/controller/storage/billing/VPN/DNS operation. Existing UI callbacks still delegate to the current controller-facing state objects.

## Resolved source contracts

- Each control resolves its own generated source style, default widget style and applicable Material overlay. Plain XML RadioButton is classified through the app's MaterialComponents inflater; explicit AppCompat radio/checkbox classes retain their own family.
- Native button/track/thumb/decoration/icon drawables, source selector tints, explicit dimensions/minimums and padding replace generic Compose control visuals. Source background/ripple drawables receive enabled/checked/indeterminate/pressed/focused state. Drawable callbacks schedule their own animation frames and are detached on disposal.
- Switch gestures retain taps and add thumb dragging with touch slop, midpoint release, minimum-fling threshold, RTL direction and cancellation. The current callback/state is captured without restarting a live drag. A drag emits only a changed state; declarative state changes do not invoke the caller.
- Clearing an optional checkbox tint creates fresh source drawable instances; a prior override cannot permanently recolor a remembered template. Independent Material checkbox icon tint stays intact, including select-all accentGood and deselect-all accentBad.
- Four migration-added scalar checkbox tint defaults were removed from WgScreenState and ProxyEditorState. They were styling approximations, not controller writes. A true runtime tint override remains supported.
- Null callbacks remain passive indicators. Baseline labeled bulk checkboxes now have one owning toggleable row with passive glyph/text. Existing distinct row/radio routes, including Orbot and WG selection, retain separate callbacks where baseline behavior differs.
- Rethink sky/max radios retain the source cleared button and stateful drawable below their label, separated by 5dp. DNS choices retain source-specific radio keys and separate arrow/info content. Checkout, rules, Orbot, network and server groups retain their individual source keys.
- The three AppCompat sliders paint the source thumb/progress/tick drawables while Compose retains range semantics, gesture/keyboard behavior and current value/finish delegates.
- Sponsor resolves inherited Material 1.14 handle width/height, track gap/corners/stop indicator, tick radii, 2dp thumb elevation, 28dp halo and 8dp track. The source floating label is painted from labelStyle using public MaterialShapeDrawable and TextPaint, with source font/color/shape/minimums/window margin, source geometry and RTL. Handle width/gap, selector state and halo stay active across Compose's PressInteraction cancellation when DragInteraction takes over. Existing label strings and `{ sliding = true; userValue(it) }` / `{ sliding = false }` ordering are unchanged.

## Important native precedence findings

These are source-backed behavior, not assumptions from the appearance of XML:

1. AppCompatCompoundButtonHelper applies app:buttonTint after framework inflation, and MaterialRadioButton explicitly reapplies it. Thus a theme/default app tint can override an explicit android:buttonTint. Retaining a scalar accent color for all states would not reproduce that constructor.
2. MaterialCheckBox only replaces the framework legacy checkbox drawable under a Material3 theme when buttonCompat is absent. Custom drawables and AppCompatCheckBox must not receive a blanket M3 drawable replacement.
3. In MDC 1.14, inherited thumbWidth/thumbHeight override Sponsor's thumbRadius fallback. Sponsor's XML `thumbRadius=14dp` does not imply a 28dp round handle in that library version.
4. TooltipDrawable reads scalar android:padding, not the start/end/top/bottom padding entries of its Material3 label style. The Compose label follows the actual constructor contract.

Primary source references, read through the permitted public-source connector:

- [MDC 1.14 MaterialCheckBox](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/checkbox/MaterialCheckBox.java) and [checkbox styles](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/checkbox/res/values/styles.xml)
- [MDC 1.14 MaterialRadioButton](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/radiobutton/MaterialRadioButton.java)
- [MDC 1.14 MaterialSwitch](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/materialswitch/MaterialSwitch.java) and [switch styles](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/materialswitch/res/values/styles.xml)
- [MDC 1.14 BaseSlider](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/slider/BaseSlider.java) and [slider styles](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/slider/res/values/styles.xml)
- [MDC 1.14 TooltipDrawable](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/tooltip/TooltipDrawable.java) and [TextAppearance](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/resources/TextAppearance.java)
- [AndroidX AppCompatCompoundButtonHelper](https://github.com/androidx/androidx/blob/androidx-main/appcompat/appcompat/src/main/java/androidx/appcompat/widget/AppCompatCompoundButtonHelper.java) and [SwitchCompat](https://github.com/androidx/androidx/blob/androidx-main/appcompat/appcompat/src/main/java/androidx/appcompat/widget/SwitchCompat.java). These AndroidX references were read on androidx-main, not from a downloaded 1.8.0 artifact; the authored native-widget oracle remains the version-specific verification gate.

## Verification and remaining limits

The source guard independently reparses pinned baseline Git XML, checks each active mapping and bounded callback/style contract, and rejects deliberately corrupted in-memory negative controls. It does not need the current layout files. Exact source hashes are recorded in its JSON output. Compound-only patches for mixed RulesContent, DnsEndpointEditors and CountryServerAdapter were saved separately; the RulesContent search bytes were frozen and preserved.

Ten tests are authored in `OriginalCompoundControlsTest.kt`, with `Application::class`, synthetic callbacks and native widget constructors as independent property oracles. They cover source families across six themes, tint/dimension layers, Sponsor handle and label, tap/disabled/drag/cancellation, slider range callbacks, press-to-drag visual-state continuity and tint-reset isolation. They have **not been compiled or run**.

No Gradle, Kotlin/Android compilation, resource linking, Robolectric execution, emulator/device testing, APK execution, screenshot comparison or live accessibility audit was performed. Those gates remain blocked. Source checks cannot certify actual resource resolution, exact animation timing, ripple/focus rendering, drag arbitration with scrolling, pointer/keyboard/accessibility behavior, theme/font-scale/RTL pixels or whole-app parity. No runtime or visual completion is claimed.
