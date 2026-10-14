# Reconstructed purchase/subscription Compose source

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
New workspace: `/workspace/scratch/a9ec32c7322a/rethink-compose/source`.

## Recovery status, 2026-10-14

The previous `/workspace/shared/rethink-compose` tree and checkpoints were absent. These files were **reimplemented from retained task context against the pinned baseline**, not recovered byte-for-byte from an old artifact. Previous parser/build/host-test results are not reused. Fresh source evidence is in `purchase-controller-source-report.json`.

All 18 assigned existing purchase UI sources now use native Compose content, plus 12 new state/renderer sources in `ui/compose/purchase`:

- Play/Website RethinkPlusFragment and GooglePlaySubsAdapter: both product types, two-column plan selection, trial/discount/billing copy, extend mode, availability, HTML terms/privacy links, CTA, loading/error/retry/support, all UI-state dispatch and processing orchestration.
- F-Droid RethinkPlusFragment coming-soon and dormant PricesAdapter presentation. No billing initialization was added to F-Droid's placeholder.
- RethinkPlusManagePurchaseFragment: plan/status/identity, entitlement-dependent row gating, history, Play management, support, cancel/refund confirmation and operation overlay.
- CheckoutActivity: all status panels, six-month default choice, original two-color price spans, complete literal copy and privacy notice, original four button callbacks.
- PurchaseHistoryActivity/adapter: PagingData, count/loading/error/empty/retry, state badges/icons, reason truncation and first-show fade.
- ServerOrderHistoryActivity/adapter: type/status/test chips, order/product identity, dates/renewal/expiry, entitlement/history actions, no-credentials/empty/error/retry/loading and fetch fade.
- DeviceAuthError, DeviceNotRegistered, EntitlementDetail, PurchaseConflict and PurchaseProcessing sheets.
- SubscriptionAnimDialog: optional title/message, original Party configuration and 2-second safe auto-dismiss.

No active renderer uses AndroidView, old layout inflation, ViewBinding or RecyclerView. Observable presentation controls are plain state objects. Activity/Fragment/bottom-sheet hosts remain explicit lifecycle, selected-theme, arguments, window, navigation and platform-action boundaries. Sheets/dialogs construct ComposeView with inflater.context. Confirmation state is transient and not saveable.

## Fresh source evidence

Run:

```
python3 scripts/compose-migration/check-purchase-controllers.py --self-test \
  --report docs/compose-migration/purchase-controller-source-report.json
```

Fresh result: **211 byte-exact method bodies and 26 narrowly listed presentation substitutions pass**. Mutation controls reject changed arguments, strings and duplicate callbacks. The full checkout privacy notice is also checked against the original XML text after Android's escaped-newline interpretation, with its SHA-256 in the report. This is source evidence only, not compilation or runtime parity.

Preserved controller contracts include billing initialization/listeners/reconnect, selected-product dispatch, unresolved-state replay, one-shot error consumption, purchase-in-flight and connectivity guards, extension handling, refund identity/error handling and callback order, all state/event observers, product-specific revoke windows, cancellation truth, entitlement full-value comparisons/reset/load/consume order, original navigation, 60-second processing timeout and 1.5-second success navigation. The original Checkout startup/key-generation/worker/payment methods remain unchanged. **Never instantiate CheckoutActivity during these tests**: its pre-existing startup path generates keys/tokens.

The server-ack-pending sheet action intentionally keeps the original support label with a dismiss callback. Pending-timeout/payment-declined primary actions call the supplied status/retry callback; their secondary action dismisses. No product decision was silently corrected.

Local history uses AsyncPagingDataDiffer with original PagingData/load states/retry/access hints and stable integer record IDs. Native LazyList saved state replaces RecyclerView state restoration; runtime equivalence remains unverified. Server rows use positional keys to tolerate duplicate/blank tokens and avoid serializing raw purchase tokens as saved keys.

## Graphics and shared interfaces

- DolphinOceanRenderer retains original geometry constants, vector paths, particle pools and drawing/math functions; the guard checks these plus the frame body. Compose frame driving replaces View invalidation, with the engine remembered at screen scope so hiding Ready content does not reset it.
- MoneyBackBadgeRenderer retains exact seal, curved text and center-text drawing methods. Original resource text and days policy remain.
- Shared RpnSignature/RpnSignatureState and rpnHeroBackground come from the reconstructed server group. Entitlement uses an inline signature; management retains the 25%-viewport end threshold and 200ms reveal; histories use bottom-12dp/0.8-alpha overlay.
- Plan border/pulse timings are 3000ms/120ms; button pulse 100ms; content/history and fetch fades keep original durations. Loading sweeps retain 2000ms subscription/1500ms history cadence and .85/.35 alpha values.
- Selected Activity/sheet theme is resolved by RethinkTheme. Resource copy, vectors, native enabled/selected semantics, headings, live error announcements, scrollable sheets and wrapping rows are present. Large-text, RTL, dark/frost/Plus/alpha rendering and motion still require device tests.
- Native 180dp Material collapsing history app bars replace CoordinatorLayout chrome. Exact scrim/parallax/frost/text metrics remain visual-validation gates, not verified parity.
- Checkout calls the shared wgAnnotatedText helper for the original ForegroundColorSpan pricing; it does not change price logic or copy.

## Konfetti dependency boundary

SubscriptionCelebration uses the official native `nl.dionsegijn:konfetti-compose:2.0.5` API. The original festive() configuration is byte-identical. Only its obsolete dialog_subscription_anim.xml was removed after a fresh search found no other XML Konfetti consumers. The parent owns the approved two full/tv alias substitutions and unchanged 2.0.5 version reference; this worker did not edit build wiring or install dependencies.

Reference: https://github.com/DanielMartinus/Konfetti . Coexistence issue: https://github.com/DanielMartinus/Konfetti/issues/337 .

## Tests rebuilt; execution status

- `PurchaseContentTest.kt`: 10 renderer-only tests under src/test, RobolectricTestRunner, SDK35, explicit plain android.app.Application. Tests use strict in-memory action recorders or supplied PagingData.from records. No production Activity/Fragment/ViewModel/backend/identity/tunnel is constructed.
- Coverage: initial/recomposition silence, repeat primary actions, hidden controls, disabled refund/restore, explicit retry, transient confirm/cancel, plan selection without purchase, fake checkout selection/payment, large RTL support, supplied paging replacement/record keys, fake server-history retry.
- `check-purchase-presentation-host.py`: reconstructed 157-case harness for extracted masking, blank equality, identity truncation, savings parsing and revoke eligibility. It uses value-only stand-ins and requires existing Kotlin/JDK arguments. **Not yet executed in this new workspace.** Previous execution evidence does not apply.
- Android compilation, Compose/Robolectric execution, screenshots and runtime/device parity: **not performed by this reconstruction worker**. Await the parent's safe toolchain/test commands.

## Remaining gates

Compile all requested Play/Website/F-Droid full and TV variants without changing version pins. Run only isolated renderer/controller tests with strict fakes, not broad instrumentation or actual billing/VPN/accounts. Exercise all seven processing states and subscription states; repeated taps, invalid identity, errors/retries, interrupted/recreated views, Back/outside dismiss, timeout, restored processing sheet and reverify. Verify cancellation/refund and entitlement reset order with fakes; test paging append/invalidation/error/scroll restore. Compare baseline visual captures across themes, RTL, large fonts, narrow/wide, TalkBack/D-pad, insets, CTA reachability, header/signature animations and animator scaling. Payment sheets, URL/custom-tab navigation, email composer, account/security and activity-result boundaries remain explicit platform actions, never automatically activated for validation.
