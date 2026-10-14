# S11 / S22: finite WireGuard/RPN text and progress source closure

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

## Implemented

- `WgText` resolves Android widget defaults, source textAppearance, explicit source text attributes and live presentation overrides. It preserves font family/resource, actual family weight, italic, tracking, line spacing/multiplier/line height, include-font-padding, alignment, limits/ellipsis, alpha/visibility, live state colors, spans and existing callbacks. No generic Compose body-medium fallback.
- The 186-entry source catalog is generated from the pinned source using typography-only value styles. Named state nodes carry exact source keys. Static affected labels in the WireGuard detail/SSID and RPN bypass/config/SSID families use the same resolver. Source medium-family, .01/.02/.03 tracking, 2/3/4dp line spacing and themed section appearances are retained.
- WgText has a passive-label mode used by the compound-control owner to retain one bulk-checkbox target. Existing WgCard child padding and compound/field changes are preserved.
- Six progress source instances are covered: server-order fetch; server-loading timeout and spinner; hop loading; summary-row progress; WireGuard network stats horizontal progress. The pinned `activity_wireguard_main.xml` contains no progress node; none was invented.
- MDC linear/circular specs and non-View Drawables render in a native Compose Canvas. Explicit thickness/color/radius override the actual source widget theme; inherited gap, stop indicator, direction and disjoint animation remain those of the installed MDC 1.14 dependency. The platform horizontal source keeps its own drawable, secondary layer, tints, measured intrinsic/min/max height and RTL mirroring. No View/AndroidView/layout inflation.
- Source progress margins are explicit: hop 10dp start/end, summary 6dp top within the text column, network-stats 5dp. Live progress/max/indicator and visibility work through existing presentation nodes; the existing `setProgress(value, animate)` setter now retains its presentation-only animation flag.

## Evidence

Official public text, read via GitHub connector only:

- [MDC 1.14 progress styles](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/progressindicator/res/values/styles.xml)
- [BaseProgressIndicatorSpec](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/progressindicator/BaseProgressIndicatorSpec.java)
- [LinearProgressIndicatorSpec](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/progressindicator/LinearProgressIndicatorSpec.java)
- [MaterialTextView textAppearance/line-height precedence](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/textview/MaterialTextView.java)

MDC 1.14 Material3 progress defaults have inherited gap/stop decorations; only the Legacy style explicitly zeroes them. No Legacy style was inferred. Reusing the source drawable avoids approximating those values using current Compose M3 defaults.

## Verification boundary

Run: `python scripts/compose-migration/check-wg-text-progress-source.py --negative-controls`.

Passed: pinned catalog comparison (186 text / 6 progress), required resolver/callsite guards, nine mutation negative controls, generated values XML well-formed check, and `git diff --check`.

Authored **UNRUN**: six `WgTextProgressSourceTest` tests use plain `Application`, isolated synthetic renderer fixtures, independent native attributes or source-style widget references. They cover themes, inherited text appearance/family, tracking/line spacing, live overrides, source progress specs, gap/stop, large-font RTL/long text, range semantics and visibility. No production Application/Activity/controller, VPN, service, network, database or persistent store is initialized.

Compile, Robolectric/runtime, screenshots, actual-host measurement and TalkBack were **not run**. The existing toolchain download restriction remains; no toolchain artifact was downloaded and no alternate route was attempted. Visual equivalence and device/OEM/font-scale rendering remain unverified. This is source completion, not measured parity.
