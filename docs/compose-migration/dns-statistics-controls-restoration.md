# DNS and Stats control callsite restoration

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
This is a bounded source correction, not Android build, screenshot, accessibility, or whole-screen parity certification. No XML was removed and no production controller, VPN, service, user traffic, persistence, or network action was executed.

## Verified causes and corrections

- The DNS custom editors used the generic application alert-button aliases. The original DoH/DoT, ODoH, DNSCrypt and DNS proxy layouts instead specify their own `Widget.Material3.Button.TextButton.Dialog` instances with `accentGood` foreground. Those eight exact generated source keys now drive the actions; their text inherits the source typography and color rather than bypassing the resolver. DoT still intentionally uses the DoH layout's button variants. The DNSCrypt relay dismiss action also uses its original catalog instance and 10dp start/end/bottom margins.
- Stats list/insights mode controls were separate button nodes, with custom 22dp icons. The original controls are joined 32×32dp source instances, outer 2dp corner radii and a shared 1dp outline. They now live in a selectable group with negative source-stroke overlap and use catalog icons plus only the original controller's selected/unselected icon tint override. The shared control renderer owns compact visual bounds and joined-control handling.
- The Stats time group keeps its original three source instances, 100dp minimum widths, 16dp outer corner radii, source 1dp shared outline and selected/unselected foreground/fill. A group semantic and resource-derived overlap replace the unqualified hardcoded spacing. The controller deliberately used solid `ColorStateList.valueOf` overrides, so those solid colors are retained.
- All 22 Stats list/insights detail actions now resolve their own source instance. All remain `ThinnerChip.Action`, with positive chip foreground/fill and explicitly null chip surface. Close-all keeps its negative variant, and the source 6dp separation between close-all and detail is restored. Destination callbacks are unchanged.
- Each of the six Stats sheet filters now uses its exact `ThinnerChip.Large` source instance, with original state-list background/foreground, hidden checked icon, 3dp child margins, 6dp group top padding and 6/4dp horizontal/vertical spacing. Select-all and Apply retain their distinct original text/custom-background variants. Required selection, saved/restored order and provider visibility are untouched.
- The generic Material3 count badge was replaced with the original 16dp-minimum red oval, white 9sp bold text and 4dp horizontal padding. Its original 2dp bottom/end visual margin is positioned relative to the centered 32dp filter control within the separate 48dp touch target.

The separate shared-renderer correction restores Material theme-overlay handling. These callsites depend on that correction and do not replace it with hardcoded fill colors.

## Source-only verification

Passed after edits:

- `check-dns-statistics-controls.py --self-test`: 47 source instances, 22 baseline-controller-derived action mappings, 13 rejected deliberate mutations. Generated appearance resources are compared with the original layout attributes.
- `check-statistics-contracts.py --self-test`: 32 baseline-equivalent methods, route/query contracts, filter restoration and original test obligations.
- `check-statistics-row-styles.py --self-test`: 13 source contracts across four layouts, nine rejected mutations.
- `check-protocol-dns-parity.py`: original inserts/URL validation, transient editors, lifecycle reads and callback boundaries.
- `check-dns-retired-renderers.py` and the pre-existing `check_dns_source_styles.py`: passed; earlier dirty DNS flag/header/mode corrections were preserved.
- `git diff --check`: passed when checked.

`dns-statistics-controls-source-report.json` records scoped production hashes and every checked source identity. Android compilation, Robolectric, screenshots, actual large-font/RTL rendering, keyboard/TalkBack order and touch geometry were not run because the approved Android toolchain remains unavailable. No denied dependency host was retried or substituted. Existing 48dp standalone touch-area policy is distinct from source chip visual heights; full surrounding dialog/screen layout and floating-action-button fidelity are outside this bounded correction.
