# Bounded history/RPN typography and loading follow-up

First checkpoint is retained at `2ed89549ef8a3e526e25cbf128541eb14ba028f3`. Its source report is historical and has not been replaced. Reference XML remains pinned to `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

## Follow-up source corrections

- S23: RPN progress title explicitly resolves 17sp sans-serif-medium plus the native bold face; status/hint retain raw resource SP (12sp/10sp), minLines2 and source spacing. Timeout track is 3dp, rounded and contiguous, with no added Material3 stop marker/gap. Source progress indicator/track colors, circular inset and cap defaults are read from the themed Material widget styles without instantiating Views. Existing dialog ownership/callback methods are unchanged.
- S36: removed-server rows restore 40dp flag frames containing 24sp text; 16sp bold, one-line ellipsized names; 13sp one-line locations at .6 alpha; 16dp padding, 12dp row separation and 16dp-corner/1dp bordered cards. The sheet's original horizontal icon/title/subtitle header, premium gradient, white icon, divider, bounded list and 24dp action margin are restored. Button callback identity is unchanged.
- S36: ping-history rows restore circular32dp icon/8dp padding, horizontal16dp/vertical12dp row padding, original 13dp/8dp columns, medium14.5sp target with middle ellipsis, medium-bold13sp latency, medium11.5sp outcome and single-line limits. The unsupported large-font alternate layout is removed in favor of the source layout; font-scale accessibility remains a required visual gate. Outcome/data mappings and adapter submissions are unchanged.
- Remaining S16 source compositions: premium loading now uses the original 200dp hero, 150x24 title, four80dp feature blocks, 120x24 plans title and two120dp plans with source spacing/12dp placeholder radii. The original themed viewport clips the fixed-height placeholder sequence. RPN header loading uses80x10 and60x26 side-by-side placeholders with original4dp top margin and1200ms/.12/.35 settings; subscription banner has20dp icon,8dp gap and weighted12dp bar inside40dp/8dp padding. Its unspecified shimmer parameters come from the already-pinned Shimmer builder defaults, not guessed constants. All placeholder rendering remains native Compose.
- Typography refinement in the first group: raw SP values now use the existing resource helper; medium+bold uses the native family resolver instead of equating it to generic weight700. This applies to history summary/title/count/product, country alphabet headers and selected tile labels.

## Changed files in this follow-up

Relative to app/src/main/java/com/celzero/bravedns:

- adapter/PingTestHistoryAdapter.kt
- ui/compose/rpn/RpnComponents.kt
- ui/compose/rpn/RpnRemovalContent.kt
- ui/compose/rpn/RpnHeaderSkeletons.kt (new)
- ui/compose/rpn/RpnSelectionScreen.kt (header skeleton calls/removal of obsolete generic skeleton)
- ui/compose/rpn/RpnSourceTypography.kt
- ui/compose/rpn/RpnServerTile.kt
- ui/adapter/CountryServerAdapter.kt
- ui/compose/purchase/HistoryRowContent.kt
- ui/compose/purchase/HistoryScreen.kt
- ui/compose/purchase/PurchasePlans.kt (loading renderer only)
- ui/compose/purchase/SubscriptionContent.kt (loading viewport only)

Also updated scripts/compose-migration/check-history-rpn-styling.py and added history-rpn-styling-source-report-v2.json plus this document.

## Verification and remaining limits

Fresh source guard:57 assertions and10 rejected negative controls. RPN fixed-body guard:221 pass. Purchase guard:211 exact +26 narrowly adapted bodies and its argument/string/duplicate-callback negative controls pass. `git diff --check` passes. The source report is explicitly not compilation or runtime parity.

All Android/Kotlin compilation, Compose/Robolectric tests, device/screenshot checks and accessibility/font-scale/RTL rendering remain UNRUN. Existing toolchain/download-policy blockers remain. Progress dialog window chrome and exact progress animation curves, implicit card/grid measurement, collapsing toolbar placement/parallax/scrim/insets, and exact shimmer sweep/dropoff/tilt/stopped-frame appearance still require approved reference comparisons. Existing Shimmer dependency is intentionally used only for its source default parameter builder; this introduces no View or inflation.

No XML deletion, controller/business/storage/billing change, shared test/safety manifest change, Gradle operation, artifact download, network access, device/app/VPN/account action, remote write or publication was performed by this worker. Work is frozen to let the coordinator prioritize the user's newly reported search/text-button/purple-background regression.
