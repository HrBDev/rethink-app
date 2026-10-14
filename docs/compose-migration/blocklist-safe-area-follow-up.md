# Blocklist safe-area follow-up

The supplied current blocklist image visibly places Simple/Advanced in the status
bar's clock/icon band and puts the bottom actions next to the gesture handle.
The corresponding target keeps those controls in the usable content rectangle.
The same existing images were re-inspected; no replacement screenshot was needed.

The production correction is already in remote commit
`34b99756e21f8c150fcff9f84d613c2174d7440f`: the full-screen configuration Box has
one `safeDrawingPadding()` owner around tabs, list, fixed footer and download UI.
This follow-up **does not change production padding or behavior**.

## Ownership traced

- `ConfigureRethinkBasicActivity` REMOTE and LOCAL select `RethinkBlocklistFragment`.
  The Activity's outer ComposeView sets `consumeWindowInsets=false`.
- `ActivityFragmentContainer` bridges a full-size, unpadded FragmentContainerView.
- The fragment's leaf ComposeView renders `BlocklistConfigurationScreen`, whose
  safe-drawing owner covers status/navigation bars, cutouts and IME.
- DB_LIST renders `RethinkListScreen` with non-null state and its standalone inset
  modifier. Sheet reuse passes null and has a separate Material dialog window.
- BaseActivity changes system-icon colors and maximum content width; it does not
  consume insets or introduce a new decor-fitting override. Its relevant baseline
  window/theme behavior is unchanged.

No additional consumed-before-child defect is established by source inspection.
A second host pad, fixed status-bar spacer, or redundant IME pad would be
speculative. Separate ComposeView instances do not by themselves establish lost
inset dispatch or lost composition context.

## New verification coverage

`BlocklistWindowInsetsTest` uses a synthetic FragmentActivity and Fragment with
the actual `ActivityFragmentContainer` bridge and actual blocklist content. It
uses a plain Application, empty paging data, and callback recorders. It does not
start production controllers, stores, VPN, network, or a real keyboard.

Two authored tests dispatch nonzero platform insets through the outer host and
check:

- Zero insets, status top, gesture/button navigation bottom, and side cutouts
- Exactly one safe-area reservation, measured in window coordinates
- Simulated IME opening/height changes/closing: bottom uses max(nav, IME), not sum
- The scroll viewport remains above the fixed action row
- Advanced-tab state and distinct Apply/Discard callbacks survive inset changes

Dispatch begins at the outer ComposeView, so this fixture does not exercise
production DecorView/AppCompat/initial-theme fitting. It does not cover recreation,
rotation/resize, or actual IME animation.

These tests are **UNRUN**, including compilation. They do not constitute a device
or corrected-render pass. The source guard protects the host/leaf ownership and
fixture assertions with deliberate negative mutations, but it cannot prove
Android dispatch behavior.

```sh
python3 scripts/compose-migration/check-blocklist-inset-boundary.py --self-test
python3 scripts/compose-migration/check-final-source.py --report docs/compose-migration/blocklist-safe-area-source-checks.json
# Required once the approved toolchain is available:
./gradlew :app:testFdroidFullDebugUnitTest --tests '*BlocklistWindowInsetsTest'
```

Device verification still needs the rebuilt commit, matching viewport, initial
attachment/recreation, rotation/resize, gesture and button navigation, a cutout,
and actual IME opening/closing. Existing artifact-access restrictions remain;
no download or alternate execution route was used.

## Separate finding, unchanged

The endpoint-list bottom sheet originally had 60dp bottom clearance in
`app/src/full/res/layout/bottom_sheet_rethink_list.xml`. Its current null-state
reuse does not retain that clearance. This is a different route from the supplied
full-screen selector image. Lost source spacing is established; whether the
Material sheet window overlaps system bars is unverified. No blanket sheet or
Home inset changes are included in this checkpoint.
