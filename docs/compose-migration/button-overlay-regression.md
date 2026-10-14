# Button overlay regression correction (source only)

The previous shared resolver read Material color selectors directly in the Activity theme. MDC 1.14 `Widget.Material3.Button.TextButton` specifies `ThemeOverlay.Material3.Button.TextButton`, whose `colorContainer` is transparent; its `m3_text_button_background_color_selector` reads that attribute. Omitting this overlay incorrectly turns a text button into a filled control. The alpha theme's purple primary accent itself is intentional baseline styling and is retained.

The resolver now applies the Material overlay and button size overlay before reading the source attributes, with explicit Android/app theme overlay last, matching MDC's resolution order. Material buttons resolve Material `backgroundTint` rather than allowing an inherited framework `android:backgroundTint` to win. Selector and drawable resources use AppCompat resource loading in the resulting component context. Source `@null` and `@empty` clear values instead of resurrecting defaults. The generator preserves the user's Material `iconTint` namespace lint fix.

Joined segments no longer insert per-item 48dp layout gaps. Their original compact visuals remain joined; Compose selectable's touch hit-slop is retained. Standalone targets retain explicit minimum interactive sizing without passing its height minimum into the visual. Dynamic icon tint can override the source icon color without replacing its source icon or size.

## Evidence
- https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/button/res/values/styles.xml
- https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/button/res/color/m3_text_button_background_color_selector.xml
- https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/theme/overlay/MaterialThemeOverlay.java
- https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/button/MaterialButton.java
- https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/button/MaterialButtonHelper.java

## Validation limits
Synthetic regression tests were authored using a plain Application and real MDC MaterialButton as the independent property oracle across six themes and both alpha overlays, enabled/disabled/checked/pressed/focused selector states. Separate tests demand transparent unselected text/dialog/outlined fills, nontransparent filled buttons, and compact grouped bounds/callbacks. These tests are **not compiled or executed**. Android tooling remains blocked. No visual, clipping, accessibility hit-target or runtime parity claim is made. The runtime safety manifest remains BLOCKED; this source change must undergo independent source review before test execution is permitted.
