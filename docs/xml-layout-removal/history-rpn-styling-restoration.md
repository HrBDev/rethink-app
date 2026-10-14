# History and RPN row styling restoration

Reference: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`; pre-edit Compose checkpoint: `f07b51b6fb2528d55b396aed62bbb9b700893c70`.

This is an uncompiled source restoration. It neither authorizes XML deletion nor establishes visual/runtime parity.

## Restored source contracts

- S15: payment/server history rows use their separate horizontal icon-circle/text columns, original text order, timer compound icon, source font dimensions/colors/max lines, alpha, margins, and distinct inset divider widths. One weighted shared card contains the list; the payment summary is horizontal and server action chips are end-aligned. Empty/error panels recover source padding, alignment, icon tint/alpha and text metrics. Server progress is 3dp; signature alpha remains 1 for server and .8 for payment.
- S16, bounded history/server-list portion: five vertical payment-card skeletons; four server-history skeletons; five RPN list skeletons. Server skeletons have 36dp icon, 120x14/70x10 bars, 6dp bar gap, trailing 20dp square, 16dp padding/corners and source outer margins. Source duration/base/highlight alpha values are explicit; stopped state does not run a Compose infinite transition.
- S17: add-server tile is transparent with 1dp stroke, 6dp dash/4dp gap, 16dp radius and an 80dp minimum inside its 6dp outer margin. Label recovers medium 11sp, .02em, one-line source contract.
- S18: plain country sections recover 36dp circular avatars, 12sp flags without font padding, resource-sized code with .02em, original text/count typography, 53dp-inset divider and original favourite/arrow frames. Alphabet headers recover 4/14/4/4dp padding and 11sp bold/.1em. Expanded city rows place load/speed/checkbox to the side, preserve source padding/minimum height, and keep callbacks unchanged.
- Further tile findings: explicit RethinkPlus.ContentCard style replaces invented 1dp border. Grid has no invented 12dp decoration; equal-height two-column rows use one composition scope. Recent-app icons restore source ordering/offsets 2@0, 1@10, 0@20 and circular backgrounds. Status follows title rather than remove-icon height. Source font tracking and label alignment are explicit.
- Missing watermark: plain observable state plus native Compose Canvas reproduce the original FlagWatermarkView drawing math. AUTO receives the same resource/tint/.25 peak; manual receives the same group.flagEmoji expression. No View, inflation, network or business controller is introduced.

All source MaterialCardView wrappers consume the shared OriginalCardSurface helper, including explicit zero-stroke overrides where XML specifies them. Implicit normal/Frost defaults are not replaced by generic Material3 Card assumptions.

## Changed source files

- ui/compose/purchase/HistoryRowContent.kt
- ui/compose/purchase/HistoryScreen.kt
- ui/compose/rpn/RpnServerTile.kt
- ui/compose/rpn/RpnServerRowState.kt
- ui/compose/rpn/RpnSelectionScreen.kt (country-list padding and server-list skeleton call only)
- ui/compose/rpn/RpnSourceTypography.kt (new)
- ui/compose/rpn/RpnLoadingSkeletons.kt (new)
- ui/compose/rpn/RpnFlagWatermark.kt (new)
- ui/adapter/CountryServerAdapter.kt (presentation/imports only)
- ui/adapter/VpnServerAdapter.kt (two source-equivalent watermark assignments only)

Paths above are relative to app/src/main/java/com/celzero/bravedns/.

## Fresh verification

- `python3 scripts/compose-migration/check-history-rpn-styling.py --report docs/xml-layout-removal/history-rpn-styling-source-report.json`: 37 source assertions and 5 rejected negative-control mutations. The guard reads its XML references from the pinned Git baseline, checks source hashes, compares three watermark drawing bodies byte-exact, and verifies the entire VPN adapter differs from the pre-edit checkpoint by only the two watermark assignments.
- `python3 scripts/compose-migration/check-rpn-controller-parity.py`: 221 fixed named controller/helper bodies pass.
- `python3 scripts/compose-migration/check-purchase-controllers.py --self-test --report /tmp/history-purchase-controller-check.json`: 211 exact and 26 narrowly adapted bodies pass, including argument/string/duplicate-callback negative controls.
- Scoped `git diff --check`: passed.

## Explicit unrun and remaining gates

Android/Kotlin compilation, Compose/Robolectric execution, device/screenshot tests, TalkBack, D-pad, font-scale and RTL visual comparison have not run. The toolchain is unavailable and the coordinator has an explicit download-policy denial; no workaround, download, Gradle, device, app, VPN, account or production operation was attempted.

The shared Material-card helper and intrinsic grid measurement require approved runtime reference comparison. History collapsing toolbar title placement, scrim/parallax/snap and insets remain separate source/runtime gates. Shimmer composition, cadence and alpha are source-guarded, but the library's exact sweep/dropoff/tilt and stopped-frame appearance remain visual gates. RPN header/subscription skeletons and premium subscription loading are outside this first bounded checkpoint. No XML resource or shared test/safety manifest was changed by this work.
