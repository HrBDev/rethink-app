# Root navigation and snackbar reconstruction

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
This is newly reconstructed source. No validation from the lost checkout is reused.
The current implementation is source-ready; Android compilation and actual test results
must come from the coordinator's current build logs. Runtime/visual parity is pending.

## Ownership and retained framework boundaries

This group owns HomeScreenActivity navigation and restore/update dialogs, SnackbarHelper
presentation, native snackbar renderer/queue, their tests, and the FragmentHost shell review.
HomeScreenFragment, the shared HomeTourAnchors registry and TourOverlayController are owned
by the separate Home reconstruction group.

- Home now has a native Compose Activity root. The former XML shell is deleted. A single
  programmatic, framework-only `FragmentContainerView` retains its stable resource ID and
  Activity-owned NavHost/graph state; it renders no visible legacy controls. Native Compose
  owns the background, bottom navigation, inset spacing and transient dialogs. See
  [Native Activity hosts](native-activity-hosts.md) for the exact compatibility exception.
- HomeBottomNavigation draws the five original menu items with their original order,
  resource IDs, labels and icons. Native items register window bounds through the shared
  `HomeTourAnchors.forActivity(activity)` / `homeTourAnchor` API. The bar also registers its
  own bounds under `nav_view`, enabling snackbar positioning without an invisible proxy View.
- FragmentHostActivity and ConfigureRethinkBasicActivity now have native Compose roots and
  stable-ID framework-only fragment containers. Their XML shells are deleted. FragmentManager
  still owns restoration, extras and back stack. All three roots preserve delivery of platform
  insets to their nested Fragment ComposeViews.
- Snackbar ComposeViews attach to the nearest existing CoordinatorLayout or window content
  FrameLayout. The Home fallback respects BaseActivity's capped Compose content width and uses
  native bottom-bar bounds. Other hosts retain generic-View/legacy-nav discovery compatibility;
  discovery never creates a BottomNavigationView.

## Behavior preserved in the reconstruction

- Startup, token regeneration, restore worker policies/observation/restart, app update
  callbacks, preference migrations and service observers remain unchanged. The source
  guard compares 25 complete controller methods against the real pinned Git baseline.
- Navigation reselect remains a no-op. The original already-there check and posted
  navigation body are byte-identical after the item-ID carrier/label substitution.
  Home uses inclusive pop-to-home; other tabs use non-inclusive. Subscription entitlement
  routing and alpha RPN toast/highlight quirks remain. Destination changes force the RPN
  highlight only for its dashboard; unrelated destinations do not resynchronize highlights.
  The selected item is saved/restored independently of the navigation graph destination.
- Restore confirmation still starts the worker then observes it. Update modality remains
  title-based. Store completion and website download callbacks stay separate. Only the
  explicit Later button writes lastAppUpdateCheck. Informational/fallback dialogs are
  dismissible. Button callback precedes dismissal and executes once. The local dialog
  stack retains identity; it is not restored through Activity recreation, like baseline.
- SnackbarHelper retains the process-global 30-second identical-message throttle,
  forceShow behavior, last-message/timestamp retention after dismissal, current identity,
  clear-before-action ordering, and stale-dismissal identity check. Stability enrollment
  and unrelated String helpers remain byte-identical.
- Helper bars and unthrottled stock-appearance bars share one process-global active bar
  and latest pending bar, matching the original Material manager's shared presentation.
  Action callback runs before action dismissal; disposal/timeout/replacement is idempotent.
- Short/long/custom/indefinite durations use 1500/2750/custom/indefinite values. API 29
  recommended accessibility timeout flags and pre-Q touch-exploration indefinite action
  behavior are retained. Touch/drag pauses the timeout and release resets its full duration.
  Start-to-end swipe, RTL direction and accessibility dismiss action are provided.
- Themed bars retain 14sp text, three message lines, primary/accent-good colors, 12dp
  rounded bottom-sheet background, border, 8dp elevation and original 16dp horizontal /
  12dp-or-24dp bottom margins. The old inset listener's additive navigation-bar inset plus
  **80 physical pixels on each dispatch** is intentionally retained, rather than silently
  changing that existing quirk. Stock bars retain two-line content, unthrottled behavior
  and no added automatic nav anchoring; the update-complete action keeps its original color.

## Fresh evidence

Executed in this reconstructed checkout:

- `python scripts/compose-migration/check-navigation-chrome.py --self-test --report
  docs/compose-migration/navigation-chrome-source-report.json`: PASS. Protects 25 controller
  methods, startup/listener/back/navigation behavior, throttle/identity, the complete
  UIUtils prefix/tail (only one additive presenter import is allowed), and graph/native-host contracts.
  Nineteen deliberate startup, restore, navigation, throttle, prefix, identity, viewport,
  opacity, inset and listener mutations are rejected. The JSON records current input hashes and check timestamp.
- `git diff --check`: PASS for the current source check.
- Current direct-callsite scan found no remaining `Snackbar.make` after the Home group
  migrated its separate notification-denial bar through the same native presenter.

Authored, not claimed run by this worker:

- Four `HomeScreenActivityDialogTest` production-state tests replace the old tests that
  merely reconstructed unrelated AlertDialogs.
- Eleven `RootChromeContentTest` native renderer tests: menu ID/order/selection/anchors,
  modal button ordering, restore callback order, informational buttons, silent snackbar
  recomposition, distinct action/dismiss semantics, live region, three-line cap and large RTL.
- `SnackbarContractTest` host/policy tests: native-nav lookup and visibility, durations,
  global duplicate throttle/forceShow, stale identity, action reentry/replacement, manual
  dismissal, shared active/pending queue, additive insets, pre-Q accessibility timeout, and native Home
  anchor/expanded-window capped margins.
- All new renderer/host tests explicitly use plain `android.app.Application`; host tests
  use only a synthetic ComponentActivity. No production Activity, Fragment, services,
  updater, restore worker, preferences, accounts or VPN are launched by these fixtures.

No Gradle was run for the native-host changes: the recovered workspace lacks the required
SDK/cache/toolchain and the denied endpoint was not retried. The earlier successful untouched-baseline build does not validate this patch.
No installation, app launch, VPN/traffic action, permission change, account operation,
commit or remote publication occurred in this reconstruction group.

## Remaining gates

Actual patched-build and isolated-test results; full FragmentManager/graph process restore
and back/rapid-reselection traces with safe startup fixtures; all channel and TV variants;
all user/Plus/Frost/alpha themes; RTL, font scaling and expanded/foldable windows; keyboard,
D-pad, TalkBack focus and announcements; snackbar swipe/timeout/window-inset animations;
rotation/destroy while dialogs, pending bars or tour anchor work are outstanding; baseline
screenshot comparison. Source checks and renderer tests do not replace these integration
and visual gates. Existing live instrumentation tests also require a safe startup fixture
and migrated Compose selectors before activation.

## Measured label typography correction (2026-10-14)

The coordinator's real retained-widget/Compose PNG comparisons measured selected label
weight 800 versus native 500, and a 12px baseline versus 24px native size at fontScale=2.
The local pinned Material 1.14.0 AAR was inspected directly, including its values/layout XML
and javap output for NavigationBarItemView and MaterialResources:

- Widget.Material3.BottomNavigationView selects the theme's active/inactive LabelMedium
  TextAppearance, with active bold enabled and label font scaling disabled by default.
- NavigationBarItemView applies setTextAppearanceWithoutFontScaling, whose SP conversion
  rounds the raw size times physical density to pixels, then uses COMPLEX_UNIT_PX.
- updateActiveLabelBoldness applies Typeface.BOLD to the existing medium family; this
  resolves to weight 800 rather than replacing the family with numeric weight 700.
- design_bottom_navigation_item.xml explicitly disables includeFontPadding for both labels.

HomeChrome.kt now resolves that widget style locally for the labels, carries the actual
Typeface/weight into Compose, converts the widget's pixel size through the current Density,
keeps tracking in em and disables font padding. Other typography remains font-scaled and no
shared UtilityTypography code was changed. Menu IDs, callbacks, selection/reselection,
window anchors, controller bodies and bar/item geometry are untouched.

RootNavigationTypographyTest adds plain-Application, no-Activity reference assertions against
actual Material BottomNavigationView labels across five existing themes at font scales 1/2.
This worker did not run Gradle or these tests. The existing navigation source guard and its
seven negative controls passed after the change. The visual fixture owner is refreshing
indicator/layout-post settling before interpreting the separate PNG geometry difference;
no production indicator removal or speculative geometry adjustment was made.

## Anchored navigation correction (2026-10-14)

The user reported a floating navigation bar and scrolling content visible behind it. Source
inspection confirmed three contributing conditions: the Fragment host filled an overlay Box,
the bar clipped its top corners, and bottom system padding lived outside the bar's painted
surface. The original `nav_menu_background.xml` uses `navBarColor`; most themes resolve it
to an opaque color, but Frost resolves it to translucent `#AA1e1e1e`. The migration's outer
inset wrapper did not paint it.

- `HomeChromeLayout` now measures the destination in the remaining Column height above the
  bar, and clips that viewport. There is no guessed or fixed navigation-height spacer.
- `HomeBottomNavigation` paints the full rectangular area, with platform bottom insets inside
  the Material NavigationBar surface. Its local color keeps the theme RGB and sets alpha to
  one, including Frost, without changing the shared theme token. The removal of exposed corner cut-outs is intentional
  for this user-requested anchored treatment; the baseline screenshot remains an honest
  comparison rather than being silently updated to declare exact visual parity.
- The bar's `nav_view` anchor includes the complete painted inset strip. Tab resource IDs,
  labels, icons, typography, selection callbacks, saved navigation selection, Fragment host
  identity and graph/back behavior are unchanged.
- Home's separate renderer removes its old fixed 80dp bottom clearance. RPN now reserves
  only measured overlap with its own viewport, plus its existing 16dp FAB/24dp scroll gaps.
  It observes both viewport layout and native bar geometry and disposes both observers.
- The snackbar's bounds-based anchoring is unchanged; its whole bar/inset boundary remains
  available through the same registry. The historical additive themed snackbar inset policy
  was not changed as part of this targeted correction.

Added synthetic renderer cases cover no content/bar overlap during scroll; 0/24/48/0 physical
pixel inset changes; full-width opaque top corners and system strip in six themes; RTL
resize, removal and recreation of anchor geometry; framework-only AndroidView/FragmentContainerView
measurement within the reserved viewport; a nested ComposeView's Home-only clearance
policy with standalone fallback; and overlap-only child clearance. A
synthetic ComponentActivity snackbar case checks anchor resize and disposal. None instantiates
production Application, Activity/Fragment controllers, VPN, account or traffic machinery.

Fresh source checks passed: navigation guard with nineteen negative controls; 221 fixed RPN
controller/helper bodies; all 522 protected-core files; and `git diff --check`. These are
source-only results. The new Kotlin/Compose tests are authored but uncompiled and unrun.
No Gradle, network fetch, installation, device/app runtime or remote write was attempted.
