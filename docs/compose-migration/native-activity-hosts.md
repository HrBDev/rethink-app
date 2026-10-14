# Native Compose Activity hosts

## Implemented boundaries

- `HomeScreenActivity`, `FragmentHostActivity`, and `ConfigureRethinkBasicActivity` use a native `ComposeView` root. None has a layout-resource constructor or inflates an Activity XML layout. Their obsolete XML shells were removed.
- Home renders its bottom navigation, window-inset spacing, background and dialogs directly in Compose. The destination occupies a measured, clipped viewport above the opaque bottom navigation, including its painted system-inset strip. The navigation no longer overlays scrolling content.
- The original `app_navigation` graph, route IDs, primary NavHost, `findNavController()` ancestry, delayed `View.post` navigation, reselection/back behavior and highlights remain. The request to set up navigation remains in the original startup sequence; actual listener installation waits for the Compose-hosted NavHost, and occurs once.
- `FragmentHostActivity` keeps its class/arguments Intent contract, FragmentFactory, invalid-class finish path, back handling and saved class name. Its restored Fragment is reused.
- `ConfigureRethinkBasicActivity` keeps REMOTE/LOCAL/DB_LIST creation and argument contracts. It now reuses a restored instance instead of unconditionally replacing it on recreation.
- Each root disables Compose host consumption of Android window insets, allowing the nested Fragment's ComposeView to retain its own system-bar and IME handling.

## Exact framework-only exception

`ActivityFragmentContainer` uses one programmatic `FragmentContainerView` through `AndroidView`. This container has no visible legacy controls, XML layout, binding, adapter or screen renderer. Existing destination Fragments render their native Compose screens.

The bridge preserves `R.id.fragment_container` / `R.id.root_container`, existing FragmentManager saved state, and Home's primary-navigation relationship. `AndroidFragment` uses a composition-generated container ID and owns Fragment removal; switching these existing Activity hosts to that identity model would lose compatibility with existing FragmentManager state and callers using `findFragmentById`. The bridge uses the same container-aware Fragment APIs (`add(ViewGroup, Fragment, tag)` and `onContainerAvailable`) with stable IDs. Activity FragmentManager owns saving/removal; composition disposal only removes its lifecycle observer. Installation waits until state is not saved and never commits allowing state loss. These hosts must stay mounted for the Activity lifetime.

RPN's FAB/list clearance observes both the native bottom-bar geometry and its own viewport layout, including late first layout and subsequent inset/size changes. Only actual overlap is added, avoiding double clearance when the Home shell already reserves the bar. Standalone hosts retain their system-inset fallback. Both listeners are removed when the Fragment view is destroyed. No RPN service, subscription or connection behavior changes.

Home's bottom bar requires no placeholder View: the existing Activity tour registry supplies its Compose window bounds to the native snackbar presenter. Snackbar placement also tracks the capped Compose root's horizontal bounds so expanded-window content stays inside BaseActivity's 600dp cap.

## Verification and limitations

- Source-only `check-navigation-chrome.py --self-test` passes: 25 unchanged Home controller bodies; unchanged graph, updater, navigation callback, back policy, startup controller ordering and Fragment argument contracts; native host/inset/stable-ID contracts; sixteen negative controls.
- New isolated `ActivityFragmentContainerTest` source covers recomposition identity, primary navigation ownership, arguments and saved state through recreation, and restoration from the old stable-ID View host into the new Compose host. It uses only a plain Application, synthetic FragmentActivity/Fragment and native Compose content.
- `HomeTourBoundsTest` covers initial/changed/unchanged/removed geometry, stale-owner protection and listener disposal without constructing an Activity or Fragment.
- `RootChromeContentTest` adds synthetic viewport/bar adjacency, repeated insets, scrolling, opaque corners/system strip across five themes, resize/disposal and overlap-calculation checks.
- `SnackbarContractTest` includes synthetic native-anchor/capped-width and resize/disposal checks without production startup.
- Existing safe Home host suites continue to exercise the real graph with a restrictive FragmentFactory and fake dependencies. No tests have been run in this recovered workspace: SDK/cache/toolchain access is unavailable and the denied network endpoint was not retried. No claim of Android compilation, runtime, screenshot, contrast or accessibility parity is made.
- Runtime gates still required in an authorized prepared environment: first-frame/lifecycle ordering, NavHost/Fragment recreation, legacy-state adoption, post/rapid-repeat navigation, native snackbar bounds across insets/resizing, TalkBack and theme/contrast checks.
