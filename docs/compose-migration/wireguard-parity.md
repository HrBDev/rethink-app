# WireGuard native Compose migration: recovered implementation

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
Recovery evidence: 2026-10-14 UTC, in the current scratch checkout.

The previous shared filesystem and checkpoints were lost. This document describes newly reconstructed files, not recovered byte-for-byte source or surviving test outputs. The retained review summary supplied the original boundaries and behavior requirements. Controllers were reconstructed against the pinned baseline; rendering/helpers were reimplemented and reviewed again. The earlier 97-body/19-test reports are not results for this checkout.

## Current scope

Native Compose now renders:

- `WgMainActivity`: simple and advanced configuration lists, original mode initialization, live state/chips, DNS disclaimer, empty illustration, FAB actions, Back handling, import/create/QR/delete-all prompts.
- `WgConfigDetailActivity`: interface/client metadata, peer list, edit/delete, application inclusion, relay, logs, lockdown, catch-all, mobile-only, SSID and the existing permission/location prompt callbacks.
- `WgConfigEditorActivity`: every interface field, masked private key, key generation, public-key copying, defaults, Amnezia display, save/dismiss.
- `WgIncludeAppsActivity`: search, three mutually exclusive filters, bulk include/exclude, refresh, membership and shared-UID confirmation.
- `WgAddPeerDialog`, `WgSsidDialog`, `GenericHopDialog` and its unchanged `WgHopDialog` subclass.
- `WireguardListBtmSheet`: supplied order/cardinality, nullable default row, domain/IP assignment and the unchanged inert APP branch.
- `WgNwStatsFragment`: one-hour/day/week periods, totals, metered/unmetered chart and per-app usage.

The six active dedicated adapters are now Compose presenters: `WgConfigAdapter`, `OneWgConfigAdapter`, `WgIncludeAppsAdapter`, `WgPeersAdapter`, `GenericHopAdapter`, `WgNwStatsAdapter`. The original comparison callbacks feed `AsyncPagingDataDiffer`; native lazy lists retain paging access hints and original item order. Row controllers own their prior callback/status calculations. Visible-row effects bind state and cancel polling on disposal. `WgHopAdapter` was removed only after confirming no callers under `app/src`. `SsidAdapter` remains unchanged for other users.

No migrated WireGuard screen inflates its old binding or uses `AndroidView`. Existing Activity, Fragment, Dialog and bottom-sheet windows remain hosts. Old XML remains as a baseline/reference and may still be shared by unmigrated destinations.

## Shared helper contracts

- `WgControlState`: observable presentation state and original listener dispatch semantics. It does not own a manager, service or persistent setting. `checkedValue` deliberately avoids the JVM setter collision previously found between private `checked` and public `isChecked`.
- `WgPagingPresenter<T>` / `WgListContent`: differ-backed presentation without RecyclerView rendering.
- `Dialog.setWgContent(Activity)`: ComposeView with host Lifecycle/SavedStateRegistry owners and disposal on window detachment.
- `WgAlertDialogBuilder(Context, theme = 0)`: native confirmations. Default prompts resolve their normal Material/AppCompat alert theme; explicit `App.Dialog.NoDim` remains undimmed with secondary negative/neutral labels. Terminal buttons dispatch once, then dismiss. Single-choice rows only dismiss when the caller does, and ignore further dispatch after dismissal.
- `wgAnnotatedText(CharSequence?)`: preserves foreground-color, relative-size, bold and typeface spans. RPN purchase code also uses this.

`GenericHopDialog` and `GenericHopAdapter` retain their constructor/callback contracts for RPN hop users, including the pre-existing unimplemented RPN-hop branch. Other RPN destinations also consume the alert/dialog helpers. Shared-helper consumers must be included in integration builds/tests.

## Retained behavior and reviewed fixes

- Interface defaults are still MTU `-1`, port `0`, DNS `1.1.1.1`; parser/persistence order is unchanged. Interface Save remains repeatable, as before.
- Peer Save disables before validation. Parsing remains allowed IPs → endpoint → keepalive → preshared key → public key → build. Editing deletes the old peer before adding its replacement. Synchronous failures restore Save.
- Compound-button state equality suppresses checked callbacks; checked change precedes click; reentrant rollback does not recursively redispatch. `callOnClick` does not toggle. The mobile-only row deliberately retains that quirk. Programmatic `performClick` does not consult enabled/clickable; native controls separately block disabled user input.
- Editable `setText` notifies text listeners even for identical content. Resource-Int overloads resolve through Context and preserve spans. Search independently suppresses equal-query change callbacks.
- Include-app bulk guards, exception handling, result marking, shared-UID loop order, callback cardinality and the 4,000 ms refresh delay are preserved.
- Always-expanded SearchView clearing did not call its close listener. Native clear therefore keeps the filter, focuses the editor and requests the IME. ASCII-whitespace-only submit remains ignored. Query-only Bundle restoration replaces the editable child's saved hierarchy state; generated filter-chip IDs/filter state are not newly persisted. A legacy comparison test is authored but unrun.
- Main mode resets from the manager on recreation. Detail's restored-state name-prefill skip is retained. Interface/peer editable drafts replace their former editable-view state. SSID saves editor/radio fields but not unsaved list mutations. Activity-owned dialogs are not silently recreated after Activity destruction.
- SSID validation, four action/match combinations, case-insensitive duplicate check, first-conflict removal, append order and connect/wildcard reset remain in the original controller. IME handling hides the keyboard, yields, clears focus, then invokes add.
- Rule selection still does not auto-dismiss, preserves duplicate/null entries and uses the baseline reused “Choose country” title.
- Statistics retain logarithmic per-app scaling, totals, initial-period behavior and the existing invalid-dialog callback that accesses the Back dispatcher without invoking it.

## Theme, rendering and accessibility review

Native content uses the selected `RethinkTheme`. Card background/elevation resolve the existing MaterialCardView style, including Frost; explicit XML radii, elevation, stroke colors, text appearance/size/style, alpha, ID max length and ellipsizing are carried into state. Runtime status stroke widths remain pixels. Switch colors resolve the original theme overlay's Material tint selectors. Status chips retain original background, rounded outline and alpha. Private/preshared keys remain password-masked; no-suggestions and Next/Done IME behavior are retained. Metadata spans remain annotated native text.

Editors and dialogs are scrollable, rows use logical start/end layout, and field pairs stack at large font scale. Switch/card callbacks remain separate. Invisible text is excluded from semantics. Labels and disabled states are native semantics.

These source-level choices are not screenshot evidence. Exact spacing, animations, text measurement, TalkBack/focus, window/inset behavior, large-font/RTL rendering, rotation and process restoration still require comparison on a device/emulator with synthetic data. No visual acceptance claim is made here.

## Verification in this checkout

- `check-wireguard-controllers.py --self-test`: **PASS, 205 retained controller bodies across 14 files**. The manifest is `wireguard-controller-selection.json`; hashes are in `wireguard-controller-evidence.json`. Tokens ignore comments/whitespace but retain literal contents. Negative controls cover mutations, quoted/comment braces, literal spaces, missing methods and abstract declarations. This is a selected-source guard, not proof of complete equivalence.
- `check-wireguard-control-state.py`: **PASS, 10 actual `WgControlStateTest` bodies**, compiled with existing cached **Kotlin 2.4.10 / JDK 17**. Minimal Android/Compose-state stubs make this an isolated host test, not Android compilation, real Compose snapshots/recomposition, a Gradle suite or service regression.
- `check-wireguard-resources.py`: **PASS**, local resource identifiers in 30 WireGuard Kotlin files and XML parsing. This does not replace Android resource linking.
- Scoped `git diff --check`: **PASS** at the final feature check. Repeat after integration edits.
- 20 JVM/Robolectric tests are authored: ten control-event cases, six SSID presenter cases, three form-state cases, one legacy SearchView comparison.
- Eight isolated Compose tests are authored, plus an updated native `WgMainActivityTest` smoke assertion. They use synthetic presentation state/callbacks. **The JVM/Robolectric Gradle suite and instrumentation tests have not been run by this feature worker.**
- The coordinator's first integration compiler pass reported four WireGuard resource-owner errors for inherited `colorPrimary`; all four are corrected to AppCompat's R owner. This worker does not claim a subsequent full app compile/lint/APK pass. Consult coordinator build logs for the current aggregate result.

The earlier stub harness caught a real JVM accessor collision that syntax parsing missed. The recovered implementation preserves that fix and reruns actual Kotlin compilation of the model. Syntax/resource scans remain useful but cannot substitute for compilation or runtime validation.

Commands (existing toolchain only):

```sh
python3 scripts/compose-migration/check-wireguard-controllers.py --self-test \
  --report docs/compose-migration/wireguard-controller-evidence.json
python3 scripts/compose-migration/check-wireguard-control-state.py \
  --gradle-cache ../toolchain/gradle-user-home/caches \
  --java-home ../toolchain/java/jdk-17.0.20.1+1
python3 scripts/compose-migration/check-wireguard-resources.py
git diff --check
```

## Outstanding acceptance and safety

JDK/SDK provisioning and aggregate Gradle execution are coordinated separately. This worker installed no tooling, accepted no agreements, launched no app/service, exercised no VPN or user traffic, and changed no OS network/security/permission setting. No backend, manifest, ViewModel, database or policy source was edited by this group. No remote writes or commits were performed.

Before acceptance, obtain the pinned app compile, JVM test, lint, APK and AndroidTest compilation results, then run approved synthetic-device checks for navigation, cancellation/repetition, lifecycle/restoration, all selected themes, RTL, large fonts and accessibility. Keep network/service effects mocked or otherwise explicitly authorized; UI source parity does not authorize live VPN operation.
