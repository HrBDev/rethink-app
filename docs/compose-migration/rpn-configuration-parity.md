# RPN configuration reconstruction

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`. Reconstructed 2026-10-14. No validation results from the lost earlier workspace are reused.

## Native source scope

- RpnConfigDetailActivity: native collapsing hero, flag watermark, reconnect chip, metadata/statistics, app/log actions, relay/lockdown/catch-all and mobile/SSID controls, error actions, native confirmation dialogs and dolphin signature.
- RpnBypassAppsActivity: native query, filter, bulk controls, refresh and application rows. The inner adapter is an observable presenter; the original sort/filter/mutation and bulk coroutine bodies remain unchanged.
- RpnWinProxyDetailsActivity: native count cards, original informational placeholders and existing app-selector toast. The placeholders are baseline behavior, not fabricated live values.
- RpnSsidDialog: native editor, radio groups, add/delete list, save/cancel and confirmation content inside the existing framework Dialog window.
- RpnProxyHopDialog and WgHopDialog are byte-identical to the baseline and already inherit the native GenericHopDialog/GenericHopAdapter. WgHopDialog is reachable from WgConfigDetailActivity and is retained.
- SsidAdapter was retired after RpnSsidDialog became native. ServerWgPeersAdapter had no callers anywhere under app/src and was retired without creating/removing any reachable screen. Peer/config classes and serialization remain untouched.

Only the three Activities, SSID dialog, two adapter deletions and the new `ui/compose/rpnconfig` package are production changes in this group. Existing shared WireGuard/RPN helpers are reused without editing them. No AndroidView or inflated legacy screen is used.

## Behavior boundaries

`check-rpn-configuration-controllers.py` freshly passes 73 exact baseline method bodies and 8 bodies with fixed, per-method presentation substitutions. It uses the current WireGuard guard's literal-preserving lexer, so changes inside string literals are not ignored. Evidence is saved in `rpn-configuration-controller-evidence.json`.

The protected bodies include recovery/register/fork choices, polling and pause cancellation, status/IP formatting, app-count observation, relay/mobile/SSID confirmation and suppression flags, catch-all removal, navigation extras, permission/disclosure callbacks, SSID save ordering, bypass bulk re-entry/write-loop/error handling and app/query/filter sorting. The eight mappings cover header-state replacement, control types, native snackbar host, shimmer state, observable SSID rows and bypass search/refresh presentation only. The hop wrappers are checked byte-for-byte; removed-adapter references are also checked.

RPN SSID defaults remain Connect + Exact. Input is trimmed, empty and case-insensitive duplicate names are rejected regardless of type, and no 32-character limit or conflicting-type replacement is added. Add clears only the editor, preserves both radio choices, appends in order and requests scrolling for every successful add. Radio choices remain enabled on empty input; only Add is disabled. IME Done still reaches validation. Save retains original serialization/callback/dismiss order. Editable dialog fields restore, but unsaved list changes are not newly persisted.

Bypass query clearing keeps the filter and focus. Whitespace-only IME input is not submitted. Restore follows the old hierarchy order: query first, then bulk checked values; generated-ID filter choices still reset to All. Bulk controls still toggle before confirmation and cancellation introduces no rollback. The original 4,000 ms timeout and early load-completion re-enable remain; repeated refresh restarts its 750 ms icon animation. Per-app switch uses the existing custom switch overlay.

Detail checked states restore at the original Activity stage. CompoundButton same-value suppression and reentrant notification are inherited from the tested native WgControlState. Existing Activity resume/pause controller methods remain exact. Native composition replaces app-bar listeners and disposes with the lifecycle.

## Header and visual contract

The header uses the shared verified 135-degree hero gradient, bottom-right to top-left. Its baseline expanded height is 150 dp; collapsed title is pinned at 56 dp. Native exit-until-collapsed nested scrolling handles body scroll and snap/fling. A separate direct-header gesture route is allowed only while the child is at the top. It waits for vertical touch slop, leaves ordinary child taps intact, consumes accepted drags and does not convert cancellation into a fling. Fully invisible hero controls are absent from hit testing/semantics. Hero/flag parallax is 0.5 and fade uses the baseline remaining-fraction / 0.6 calculation. The expanded hero keeps its own measured height while its viewport collapses.

The flag/drawable uses a native Canvas port of the original full-height/end-aligned radial DST_IN mask, including RTL. Content-card background, 16 dp corners, zero elevation, text styling and client-IP skeleton dimensions are taken from the original resources. Larger fonts are accommodated with additional header height and wrapping/scrolling content.

These are source contracts. Exact screenshots, typography, clipping, focus/IME, accessibility, sheet/dialog sizing, saved list positions and full lifecycle parity remain runtime/visual acceptance work.

## Fresh validation and tests

Performed in this reconstruction:

- Controller token/source guard: PASS, 73 exact + 8 narrowly adapted method bodies; lexer negative controls passed; both hop wrappers unchanged and both retired renderers unreferenced.
- Protected-core guard: PASS, all 522 protected files byte-identical; dependency version pins unchanged.
- Owned tracked diff whitespace check: PASS.

Written, not executed by this worker:

- RpnSsidStateTest: 10 fake-only reducer/restore/JSON serialization tests.
- RpnConfigurationComposeTest: 16 isolated tests for recomposition, row/switch dispatch, hidden/disabled actions, direct header drag/release/cancel, sub-slop child tap, search/filter, bulk dispatch, RTL/large-font SSID and WIN callbacks.
- RpnConfigurationDialogTest: 3 synthetic dialog tests covering SSID callback-before-dismiss, cancel without save and a null negative callback.

All three suites are under `app/src/test`, use Robolectric SDK 35 with a plain android.app.Application and never launch a real RPN Activity/controller, Go backend, VPN, account, permission or traffic flow. Original integration tests were not changed. The coordinator owns all Gradle compilation and test runs, including compiler feedback; no concurrent Gradle process was started by this worker. No Kotlin/Android build or runtime pass is asserted here.

No real app/VPN/traffic/probe/account/export/permission action was executed. No dependency versions, manifest, manager, repository, data/peer model or storage code changed. This worker made no commit, remote write, PR, merge or deployment.

## Re-run

```sh
python3 scripts/compose-migration/check-rpn-configuration-controllers.py --self-test \
  --report docs/compose-migration/rpn-configuration-controller-evidence.json
python3 scripts/compose-migration/check-protected-core.py
```

Then let the coordinator compile the pinned variants and run the three named fake-only suites. Native screen runtime/visual parity requires the separate synthetic-data acceptance matrix; source guards do not replace it.
