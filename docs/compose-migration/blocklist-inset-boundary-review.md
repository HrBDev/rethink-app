# Blocklist inset fixture source review

Reviewed 2026-10-14. Scope: the new isolated blocklist inset fixture and its source-only guard; no production edits or broad-screen fixes.

## Result

- No source-proven Kotlin/API, lifecycle, or assertion blocker found in the reviewed fixture. This is a source review, not compilation approval.
- `python3 scripts/compose-migration/check-blocklist-inset-boundary.py --self-test`: **PASS_SOURCE_ONLY**, 17 contracts and 10 deliberately broken inputs rejected.
- Android compilation: **UNRUN**. Both new Robolectric tests: **UNRUN**. Actual inset dispatch, rendering, native IME, screenshots and accessibility: **UNRUN**. Toolchain artifact access remains blocked; no Gradle, artifact download, device, app service or VPN was invoked.

## Boundary and assertion review

The test uses a plain Application, synthetic FragmentActivity and synthetic Fragment, real ActivityFragmentContainer, the real BlocklistConfigurationScreen, empty PagingData and recording callbacks. It does not construct the production application, activity, fragment controller, dependency injection, persistent stores or network-facing managers. The synthetic host explicitly sets consumeWindowInsets=false before composition, matching the production host's forwarding declaration. Disposal follows the view-tree lifecycle and the ActivityController is closed after each test.

Native WindowInsetsCompat dispatch begins at the outer ComposeView and must cross the actual AndroidView/FragmentContainerView boundary to reach the leaf. Insets and measured window coordinates are physical pixels; the assertions do not confuse pixels and dp. The selector's top/sides and fixed footer bottom constrain a single reservation; max(navigation, IME) rejects additive bottom padding, and the list must stop above the footer. The advanced selector remains selected through inset changes and the recorded discard/apply actions stay distinct.

## Limits and resolved report caveat

1. This fixture bypasses DecorView/AppCompat by dispatching directly to the ComposeView. It sets AppTheme and explicitly disables decor fitting, whereas production starts with AppThemeInitial and applies the chosen style. It therefore does not establish production theme/decor fitting, the inherited v27 fitsSystemWindows value, BaseActivity's width cap, or device/OEM behavior.
2. The cases exercise repeated static inset dispatch after attachment. They do not test recreation, window resize, native keyboard visibility/focus/animation, active download state, or any bottom-sheet window. A synthetic IME inset is not an actual IME run.
3. Top status and cutout heights share the same value in the current sample. Horizontal cutout behavior is independently exercised, but a cutout taller than the status bar is not independently checked.
4. The Python guard is an exact-source/mutation guard, not a Kotlin parser or runtime oracle. The initial hardcoded metadata weakness is **RESOLVED**: `production_padding_changed` was removed, and `authored_test_methods` is now computed from the fixture source (currently 2). This is a source count, not evidence that either test ran.

## Final delta review

The synthetic Activity now constructs its ComposeView directly inside setContentView and assigns the same object to its `root` field before setting content. This preserves the fixture's view identity, forwarding policy and lifecycle while making direct Compose construction visible to the existing zero-layout source gate; no new gate exception is introduced. The corrected source guard was rerun: **PASS_SOURCE_ONLY**, 17 contracts, 10 rejected mutations and 2 authored tests. The follow-up document now explicitly excludes production DecorView/AppCompat/initial-theme fitting, recreation/resize and actual IME animation. No additional source-proven blocker was found in these deltas. Android compilation and runtime tests remain **UNRUN**.

## Production finding

No additional missing inset owner is proven on the current full-screen blocklist path. ConfigureRethinkBasicActivity forwards insets; ActivityFragmentContainer is unpadded; RethinkBlocklistFragment renders the leaf safeDrawingPadding owner. REMOTE and LOCAL share that owner. Do not add a second activity/root padding based solely on this unrun fixture. No production padding was changed by this review.

## Reviewed source identity

HEAD at review: `36443811c11722faffa249ee63973834c56e7cae` (the new fixture and guard were working-tree additions).
The hashes below bind this review to the exact source read. Any edit requires reviewing/re-hashing that file.

- `app/src/test/java/com/celzero/bravedns/ui/compose/blocklists/BlocklistWindowInsetsTest.kt`
  SHA256 `ca2ce5fe7b828c413774d6455b48d2c9800de60196375ee44a3f4b1b8afbf051`
- `scripts/compose-migration/check-blocklist-inset-boundary.py`
  SHA256 `fbb662a89fb855169a7580d4a3f70d14e8d0de68c753d7dfde9ce8fa52e20c72`
- `scripts/compose-migration/check-final-source.py`
  SHA256 `c72eaf0120691dcecb11581d57934d4aa84e580b99205dc86a04ea43cf1f2ce0`
- `app/src/main/java/com/celzero/bravedns/ui/activity/ConfigureRethinkBasicActivity.kt`
  SHA256 `a45caa909cdf132763ecc8e98160f5f6055be9ba8b29e2ae58948ccab934e517`
- `app/src/main/java/com/celzero/bravedns/ui/compose/ActivityFragmentContainer.kt`
  SHA256 `385c1d86ad894bd5430e2d6359811baed77e9307cb22e758d3e59ba99581bba9`
- `app/src/main/java/com/celzero/bravedns/ui/fragment/RethinkBlocklistFragment.kt`
  SHA256 `24b964e86dcda01776550d6d17d8d375fcdeeadd24155b9b194c584781b28967`
- `app/src/main/java/com/celzero/bravedns/ui/compose/blocklists/BlocklistConfigurationScreen.kt`
  SHA256 `1b0682cbe183b272eedf841ac8ceed38faa5020f03e118c0ae31515a72b9b653`
- `docs/compose-migration/blocklist-safe-area-follow-up.md` (coverage-limit wording reviewed in final delta)
  SHA256 `e6e08a91def1508ec55dec79ff5dfdbca4df48d7cead023509e6d1924948399a`
