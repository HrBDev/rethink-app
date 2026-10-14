# Button and search regression review

**Verdict: source-only approval for the bounded corrections at the exact hashes below.**

## Confirmed source corrections

- Material 1.14 component overlay precedes size overlay; Android/app overlay is last. Selectors resolve in this context, restoring transparent TextButton containers.
- Material backgroundTint drives managed and overwritten Material buttons; AppCompat color/drawable loading preserves component theme resolution.
- Explicit null/empty clears are retained; cleared cornerRadius falls back to shapeAppearance.
- Real custom-button backgrounds suppress managed stroke/ripple and extra shape clipping, following the native overwritten-background path.
- Eleven generated fixed-width icon-only source keys have hard source heights and centered icons without inherited text padding/icon-gap collapse; joined controls omit per-item 48dp layout minima.
- Search resolves SearchAutoComplete style and a separate tint-aware query plate, restores native frame/editor/icon dimensions and source 40dp server caller, and omits AppLog magnifiers.
- Existing positional OriginalButton calls remain compatible; query/focus/clear source callbacks are preserved; invalid LazyColumn style argument is absent.

The exact MDC 1.14 overlay helper was independently read through GitHub, blob `08b189c6c432d10023f6699d6a2e8a90e0220394`. Its order and early-return behavior match the corrected resolver. [Official source](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/theme/overlay/MaterialThemeOverlay.java).

## Checks and limits

Source checks passed: button 21 contracts plus 11 rejected mutations; search 34 contracts including 7 rejected keyboard mutations; original-controls; original-fields 21 calls/96 baseline instances; diff whitespace check. The exact user lint commit `154065c0467f9f47be5acf8b58fc15e6bdca8a31` remains preserved without a behavior waiver.

- Kotlin and synthetic tests are uncompiled/unrun; no runtime, screenshot, accessibility, clipping, touch-hit-region or actual-host parity claim.
- The independent MDC widget oracle validates color-state resolution and custom-background properties, not general typography or shape parity. It shares generated source style inputs, so pinned-baseline generation checks remain necessary.
- The native SearchView oracle is independent but unexecuted. Large fonts, RTL, source shadow/ripple pixels, Compose measurement and interaction behavior remain runtime gates.
- The whole-layout styling ledger remains independently open; this bounded review does not close all source-removal or runtime gates.
- Runtime safety manifest remains BLOCKED; no production app/VPN/account/permissions/traffic was executed.

## Keyboard follow-up

Both field overloads now fill only absent search defaults: Unspecified action becomes Search and null autocorrection becomes false. Explicit actions, autocorrection, keyboard type, capitalization, platform options, showKeyboardOnFocus, hintLocales and caller callbacks remain preserved; other families return their original options object. Source review and 34-check guard including seven negative mutations pass. Compose false-autocorrection does not set native NO_SUGGESTIONS or AUTO_COMPLETE bits, so identical EditorInfo/suggestion behavior is explicitly unverified and not approved.

## Checkpoint provenance

The 20 artifact hashes below pin the Git index used for this bounded checkpoint. RulesContent.kt uses the indexed pre-compound version, SHA256 `ebedf52662db0b0bb594fb55b657df7d3918c56733a1935aeb44cf4191cd4300`, rather than the later working version `08ebec904ad1b2fd4fd8a0122c80d15757bec870cd9ced029e08675864d6d285`. Direct comparison confirms the search region and first 146 lines are byte-identical; the differences are later radio/checkbox compound integrations outside this review. All other 19 hashes independently match the index. The JSON records the indexed Git blob and unchanged search-region hash. This provenance update does not add runtime evidence or review newer RulesDialog changes.

## Reviewed artifacts

Any subsequent source change requires hash refresh and review. The JSON companion contains the eleven fixed-icon source keys and official evidence links.

- `app/src/main/java/com/celzero/bravedns/ui/compose/controls/OriginalControls.kt`: `7125329c6beaa215072dc395978a3c11149ffe76a59770051c40de8789bceba9`
- `app/src/main/java/com/celzero/bravedns/ui/compose/controls/OriginalControlCatalog.kt`: `5bc4fe34fd8ea03aacd4c994d406d2f904f1cd95c7ab0c77d9526841eb80a940`
- `app/src/main/res/values/original_control_styles.xml`: `ffd59f1d00692d730233001029beebba42388204da74848a18622b03e96994bb`
- `app/src/main/res/values-w600dp/original_control_styles.xml`: `d04e99799f56d49ab21f4153247aa53e1913d91082fa996ab1c33075d2dea176`
- `docs/compose-migration/generate_original_control_styles.py`: `54af241c0137ce4a6aa7312790ec8d56da5e944f6fd6cdff3aff6af18be93626`
- `app/src/test/java/com/celzero/bravedns/ui/compose/controls/OriginalControlsTest.kt`: `8522461b1a19db067b4e2d3a26bd4ea3bd505d5b74ef06f8b3b93f3091f855d6`
- `app/src/main/java/com/celzero/bravedns/ui/compose/fields/OriginalTextFields.kt`: `6621814654d70d4de2ee71db7164d2d4cd2ac402afb798da976d6e17f79c15db`
- `app/src/main/java/com/celzero/bravedns/ui/compose/rules/RulesContent.kt`: `ebedf52662db0b0bb594fb55b657df7d3918c56733a1935aeb44cf4191cd4300`
- `app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnSelectionScreen.kt`: `a7a30a214604c99c3d34195329d028709b32cf509117543e62cad7f04ce3fb69`
- `app/src/main/java/com/celzero/bravedns/ui/compose/appdetails/AppLogsContent.kt`: `09ac5d45f6fb027a5259ddfc26215f247707575e4578b550529f8518812eed19`
- `app/src/main/java/com/celzero/bravedns/ui/compose/trafficlogs/LogScreen.kt`: `c004145ae4c08356edc27d619668fcdc163ed9e5d3f242725d2500bee7e59103`
- `app/src/test/java/com/celzero/bravedns/ui/compose/fields/OriginalSearchStyleReferenceTest.kt`: `a50957801360d94f870d0e2fa8a8fc35038de7f5df3e81f1e9a10f820f5ef046`
- `app/src/test/java/com/celzero/bravedns/ui/compose/fields/OriginalTextFieldsTest.kt`: `7b2ca88ad707e37b5651b0f52935f43c1e1c2f77ff563ce1b8753b211404c729`
- `scripts/compose-migration/check-button-overlay-regression.py`: `7e811a08636f5a03583a89de3a2d0ede5cb747cda9ae5b19dfcff8f8756b8549`
- `scripts/compose-migration/check-original-controls.py`: `166772238fb38b3f6bc713bcc6c599f03253ca616ec74974157fcb7d56c25d22`
- `scripts/compose-migration/check-search-source-styling.py`: `a116bc648ffaf34ba0d2407e8c877dbc2d54a11a26e6e48da9aaf0fb76c7169f`
- `scripts/compose-migration/check-original-fields.py`: `6063fe4190884c2bdf3b47452a48578894e6383eaaf32db1b40241b598723d91`
- `docs/compose-migration/button-overlay-regression.md`: `d6e9c3b5fd5d55c43f6f37831d071f81ba2f935751dbe45f79b356f9894dec49`
- `docs/compose-migration/search-source-style-corrections.md`: `a7e15ad66b93868339fbab25eb9847b2f68864c1e96ee6fad8d54d03a5d53fba`
- `docs/compose-migration/search-keyboard-source-contract.json`: `c89a1e49a0023e4759f63bf41057c4f0908c82067caa410fe9cbfac6018e9890`
