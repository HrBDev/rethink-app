# Native RPN server/account/settings reconstruction

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
Reconstructed on 2026-10-14 after the previous execution workspace and checkpoint were lost. The prior implementation summary guided API/timing decisions; all source was rebuilt from the baseline checkout. The earlier workspace's test results are not current evidence.

## Owned source scope

- `ServerSelectionFragment`: original controller, native search/filter/location selection, AUTO/start/stop/relay actions, account/settings/support navigation, capacity and refresh animations, subscription/error/shimmer states, and native 24 × 6 heatmap.
- `RethinkPlusDashboardFragment`: original entitlement observers, account/purchase/test/support/restore routes; native account renderer and reset progress content.
- `CountryServerAdapter`: native country/city presenter preserving alphabetical sections, expansion retention, favourite mutation/callback, and stopped selection redirect.
- `VpnServerAdapter`: native selected-grid presenter preserving rendered-field invalidation, AUTO reconnect/removal, route/app/relay destinations, cached UI replay, per-key loading and three lifecycle-bound polling loops. The stable custom two-column layout keeps item keys in one composition scope. A hidden selected section retains already-composed polling presenters; new binding is deferred until visible.
- `ServerSettingsBottomSheet`, `AutoExcludeCountriesBottomSheet`, `ServerRemovalNotificationBottomSheet`: original framework sheet ownership and callbacks with native content. Dialog Compose roots use `inflater.context` and stable resource IDs.
- `ui/compose/rpn`: native rendering and UI-only snapshot state. No `AndroidView`, layout inflation, or custom legacy View renderer in this group. Android property animators target snapshot properties, never Views.

RPN configuration, purchase/payment, statistics sheets, shared generic-hop, navigation chrome and tour host remain separately owned. This group shares `RpnControlState`, `RpnChoiceState`, `RpnSignatureState`, `RpnSignature`, `RpnText`, and `rpnHeroBackground`; consumes `WgAlertDialogBuilder`, `Dialog.setWgContent`, and `HomeTourAnchors`.

## Preservation details

- `checkedValue` backing field avoids the JVM `setChecked` collision. Same-value updates are silent and reentrant listener broadcasts are suppressed. Controllers restore state before installing listeners; native saved search is delivered after setup. Required AUTO/manual selection dispatches old false, then new true; persistence remains guarded by true in the controller.
- Actual selection, registration/reset timeouts, manager/service/Room calls, URI/extras, coroutine dispatch and status poll loops remain in their controllers. Tests never invoke them.
- Filter changes commit only Apply or Reset; Cancel/outside/Back do not commit. Unknown load/speed handling and exact tier boundaries are preserved.
- Exclusion DONE sets its duplicate-dismiss guard before persistence, listener and dismissal. Dismiss-before-load guard and initial asynchronous `applyFilter("")` remain. Limit-dimmed rows remain tappable to show the existing limit toast. The XML's hidden permanent-config row remains hidden.
- Heatmap level is zero for empty, otherwise min(4, floor(log10(count)) + 1); 5dp diameter fractions `.30/.78/.78/1/1`; original light/dark alphas; RTL drawing and original click-to-time mapping. Native Canvas draws the activity dots.
- Pull threshold/slingshot are 220dp. Native nested scrolling accumulates unconsumed distance directly; release must exceed the threshold, followed by a 200ms settle before notifying the original handler. The `.5` touch fallback multiplier is not applied to nested scrolling. Exact device gesture parity is still a runtime gate.
- Signature entrance: 520ms curved swim, 220ms fade, quote delay360ms/duration240ms. Cycle: guarded140ms out/200ms in, original offsets/rotation keys. System reduced-motion setting is read only. Account overlay retains25% remaining-scroll threshold,200ms fade and96dp bottom offset.
- Navigation measurement uses `findViewById<View>(R.id.nav_view)`, never a BottomNavigationView cast. Native tour anchors register the original status/settings/relay/bypass/selected-list/add-tile IDs through the shared Activity registry.

## Fresh verification

Executed against this reconstruction using existing approved local Kotlin2.4.10 cache and JDK17, with no downloads by this worker:

1. `check-rpn-controller-parity.py`: **221 fixed named braced controller/helper bodies passed** against the pinned Git baseline. Guard names are frozen in `rpn-controller-functions.json`. Normalization is limited to comments/whitespace, UI-state field prefixes, control callback type, property-name animation targets, native dialog builder name, explicit full-list presentation revision, lifecycle-owner/attachment presentation lookup, and obsolete nested-list layout posts. This is not a claim of full-program equivalence; changed renderer/lifecycle setup functions are separately reviewed.
2. `check-rpn-control-state.py`: **8 actual JUnit test bodies passed** with actual state classes compiled against isolated Android/Compose stubs. Includes JVM accessor generation, quiet initialization, reentrant suppression, exactly-once row click, disabled taps, no implicit toggle, and required choice order.
3. `check-rpn-filter-host.py`: **30 actual extracted filter/link-format cases passed** with a plain data fixture: query fields, unknowns, exact load boundaries, speed equality, favourite conjunction and units.
4. Direct native RPN resource-reference scan: **18 renderer/state files passed**. Owned `git diff --check` passed.
5. Coordinator's actual migration compile reported two non-transitive `colorPrimary` references; both were corrected to `androidx.appcompat.R.attr.colorPrimary`. Final all-project compiler outcome belongs to the coordinator and is not inferred here.

Example commands (repository root):

```sh
python scripts/compose-migration/check-rpn-controller-parity.py
python scripts/compose-migration/check-rpn-control-state.py --gradle-cache ../toolchain/gradle-user-home --java-home ../toolchain/java/jdk-17.0.20.1+1
python scripts/compose-migration/check-rpn-filter-host.py --gradle-cache ../toolchain/gradle-user-home --java-home ../toolchain/java/jdk-17.0.20.1+1
```

Three content-only instrumentation fixtures were added in `RpnContentTest`; **not run**. They must only execute under an explicitly isolated test application. They never construct RPN controllers or the real polling presenter.

## Remaining gates

- Coordinator's final actual Android/Compose typecheck, variants, and test compilation.
- No device, screenshot, interaction, TalkBack/D-pad, navigation, IME, pull gesture, animation or runtime parity verification has run.
- Compare seven themes, Frost alpha, compact/expanded widths, font scaling/RTL, native dialog geometry, and motion timing with baseline captures.
- Exercise mocked lifecycle/recreation/hidden-list/reset/refresh overlaps and cancellation cardinality. Verify focus/query restoration and native tour reveal/coordinates.
- Exact visual parity remains provisional until those checks; source reconstruction and isolated host tests do not establish it.

No VPN, probe, user traffic, account, purchase, network/security setting, remote write, commit or publication was executed by this worker.
