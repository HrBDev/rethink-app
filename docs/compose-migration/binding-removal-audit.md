# Binding removal and remaining renderer audit

## Verdict and scope

The audited source has no active Android View Binding or Data Binding API, generated-binding import, binding delegate dependency, app-owned `R.layout` reference, or application layout inflation entry point. Visible application content is Compose. This **does not mean that all Android Views, XML resources, adapter names, or compatibility state objects have been deleted**.

There are two explicit `AndroidView` calls in narrowly scoped framework fragment-container bridges. AndroidX `AndroidFragment`, `ComposeView`, platform dialog windows, the Material bottom-sheet shell, and an invisible notification trampoline remain. None is described as a fully Compose-only Android runtime.

The reproducible census is in [binding-removal-report.json](binding-removal-report.json). Its file hashes identify exactly what was checked. The guard discovers every production Kotlin/Java file under every `app/src` source set on each run; it does not rely on the existing 115-screen ledger. Current inventory:

- 779 production Kotlin/Java files: main 657, play 35, website 33, fdroid 23, TV 31
- 186 retained layout XML resources, including older screen references and test/visual-reference resources
- No Data Binding `<layout>` wrapper or `@{...}`/`@={...}` expression in production resources
- No layout references from manifest, navigation metadata, styles, or other non-layout XML
- No production `ViewBinding`, `ViewDataBinding`, `DataBindingUtil`, generated `databinding` package or `vbpd` dependency
- No application-owned `R.layout` reference or explicit `inflate`/`getLayout` call in production Kotlin/Java

Pinned source provenance supplied for this work:

- Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`
- Recovered work commit: `87b3b9975afdb59492bc92cf97b9560cdd725330`
- Recovered source tree: `1949435b20c49bb03ec06123c27f913a4974882b`

This is static source evidence. No Gradle, Kotlin compilation, APK inspection, emulator, real Application, VPN, network traffic, purchase, or permission flow was run for this audit. Compilation, restoration behavior and screenshots remain separate verification requirements.

Final frozen-source check (2026-10-14): PASS, zero violations; 35 in-memory positive/negative controls and Python syntax validation passed. Two consecutive complete reports were byte-identical. Report SHA-256: `1d6ce17f9db9eaef7b979a1346459da6017d095f2bf7a04d5254e422dd0f4f92`. Guard SHA-256: `61226ba66482eb09f34a837c31ee26682ccb19c8924d32bc85afb2997401a5e4`.


## Inflation paths traced

### Binding hosts removed

`FirewallActivity` and `DnsDetailActivity` previously used generated `ActivityFirewallBinding` / `ActivityDnsDetailBinding`, a hidden TabLayout and a one-page ViewPager2. They now install Compose presentation for the existing Compose settings fragments. The old tab/pager widget renderer and binding delegates are gone. Fragment/lifecycle ownership remains an interoperability boundary; upgrade-time restoration of the former pager fragment must be reviewed and tested independently.

`HomeScreenActivity`, `ConfigureRethinkBasicActivity` and `FragmentHostActivity` previously inflated app XML shells. Their current presentation roots no longer reference those XML layouts. The Home navigation graph and fragment controllers are retained; the home bottom bar and dialog content are Compose. The generic fragment host still resolves its requested fragment using FragmentManager rather than replacing controller logic with a separate navigation implementation.

### Explicit AndroidView exceptions

`ui/compose/ActivityFragmentContainer.kt` contains one `AndroidView(factory = { container }, modifier = modifier)` call. Its `container` is exactly a `FragmentContainerView(context)` with the existing resource ID. It has no XML inflation, TextView, ImageView, RecyclerView, ViewPager2 or other application widget construction.

This bridge is used by Home and the two generic fixed-ID fragment hosts. It preserves the Activity-owned FragmentManager, primary navigation host, fixed container IDs and restored fragment views. Those fragments return ComposeView content.

`ui/compose/settings/SingleSettingsFragmentHost.kt` contains the second `AndroidView(factory = { container }, ...)`, again wrapping only a FragmentContainerView. It adopts the old one-page ViewPager2 fragment tagged `f0` so a restored page is not silently abandoned when the pager is removed. The Activity root is ComposeView, and the retained settings fragment supplies ComposeView content. The guard permits one exact reviewed factory/container shape per file, allows only the settings helper's reviewed ComposeView root, and rejects attaching extra children. Adding an XML inflater, native control or another AndroidView call fails the guard.

`AndroidFragment` also remains in the network log pager. The final settings host uses the explicit fixed-identity bridge above instead of AndroidFragment so it can retain the legacy `f0` page. This library API itself uses framework fragment/View-container interoperability. Its existence must not be omitted when describing the migration. A lack of application `AndroidView` wrappers is not evidence that AndroidX has no transitive View containers.

### Other intentionally retained boundaries

- `ui/compose/dnssettings/DnsFragmentHost.kt`: programmatic `LinearLayout` containing a ComposeView header and a FragmentContainerView, used by `SmartDnsListActivity` and `ConfigureOtherDnsActivity`. The child DNS-list fragments return ComposeView. This is a native layout/navigation shell with Compose visible content, not an old screen wrapped in Compose.
- `NotificationHandlerActivity`: an empty ComposeView shell, immediately made invisible with `window.decorView.alpha = 0f`, replaces the former platform list-item XML. Notification/security routing and lifecycle remain unchanged; its user-visible confirmation dialogs use `WgAlertDialogBuilder` Compose content. The exact source-boundary guard permits only this renderer substitution. There is no platform-layout exception in the current trampoline.
- `BaseActivity`: window/status-bar/theme logic and maximum-content-width handling still use the Android decor/content FrameLayout. These are window integration, not generated UI binding.
- Fragment and bottom-sheet `onCreateView` signatures still receive `LayoutInflater`; their implementations create ComposeView rather than calling it. An imported type or lifecycle parameter is not evidence of XML rendering.
- Compose snackbar/tour overlays attach ComposeView to a window/ViewGroup. Their content is composed; window attachment, insets, anchors and teardown still use platform APIs.
- Ordinary platform Toasts, Android permissions, system settings/biometric/notification flows and service interfaces are outside removal of UI binding and have not been replaced by this audit.

## Dialogs, adapters and compatibility names

### Dialogs

All live calls named `MaterialAlertDialogBuilder` in migrated production code resolve to imports aliased from `WgAlertDialogBuilder` or `NetworkAlertDialogBuilder`. The unused imports of the actual Material View dialog builder were removed. No live construction of the real builder was found.

`WgAlertDialogBuilder`, `NetworkAlertDialogBuilder` and `AboutDialogBuilder` retain builder-shaped APIs to preserve controller callback/dismissal ordering. Their visible title, text, rows, choices and buttons are Compose. They create platform Dialog windows and install Compose content through `setWgContent`.

The custom WireGuard peer/hop/SSID, RPN SSID, LAN-IP and network-reachability dialog classes likewise retain Dialog/AppCompatDialog window and lifecycle ownership but install Compose content. Other transient Compose dialogs use `androidx.compose.ui.window.Dialog` or Compose Material dialogs. `BaseBottomSheetDialogFragment` retains Material BottomSheetDialog behavior (width, drag/dismiss, expanded state); each app sheet supplies ComposeView content. Material's own internal shell resources are not application screen XML and are not removed.

### Adapters and state shims

Old names alone are insufficient to classify a renderer:

- `WgPagingPresenter`, `LogPagingPresenter` and `AppConnectionsPresenter` keep `AsyncPagingDataDiffer`, DiffUtil, snapshots, access hints and lifecycle-bound submission. They render Compose rows/content and are not RecyclerView adapters.
- App/traffic/summary/WireGuard classes still named `*Adapter` or `*ViewHolder` hold presentation state and controller actions. No active `onCreateViewHolder`, `getDropDownView`, RecyclerView.Adapter construction or PagingDataAdapter construction remains in the source census.
- `WgControlState`, `RpnControlState`, `UtilityControl`, `HomeNode`, `HomePager`, search/slider/spinner state and `NetworkNode` deliberately mirror former widget properties/listeners. They are ordinary observable Kotlin state objects, not Android View subclasses and not generated ViewBinding. They have no XML inflation.
- `binding`/`dialogBinding` variables can refer to `ReachabilityUiState`, `ProxyEditorState`, `EntitlementRowState` or other ordinary Compose presentation state. `launchBinding`/`cancelBinding` in the log adapter govern per-row asynchronous work. These terms are not Android UI binding APIs and should not be blindly renamed or deleted.
- Three obsolete ArrayAdapter allocations using `android.R.layout.simple_list_item_activated_1` were removed from FirewallAppListAdapter, AppInfoActivity and WgIncludeAppsAdapter. The active paths already deliver their labels to the Compose builder's `setItems`; the old adapters were never attached.

### Concrete review of binding-shaped state holders

This audit traced the values behind the remaining names rather than treating a renamed import as sufficient:

- `CustomLanIpState.binding` creates `LanFields`. Every LanFields member is `WgControlState`, whose properties are Compose `mutableStateOf` values and callbacks. `CustomLanIpContent` consumes them in Compose Button/Text/OutlinedTextField. Neither LanFields nor WgControlState extends View, owns a root View, looks up View IDs, inflates XML or implements a binding API. The state/controller still performs the original validation and persistence; removing it would remove behavior rather than eliminate binding.
- `NetworkReachabilityDialog.binding` creates `ReachabilityUiState`, which owns `UtilityControl` values. `ReachabilityScreen` consumes them in BasicTextField, Compose text/buttons/progress and drawable-backed Compose drawing. Android Drawables and `View.VISIBLE/GONE` integer constants are data/style compatibility, not View construction.
- Both `ProxySettingsActivity.dialogBinding` locals create `ProxyEditorState`. Its fields are WgControlState / NetworkSpinnerState and a NetworkNode data tree. `NetworkContent` dispatches that tree to Compose Column/Row/Box/Text/BasicTextField/Slider/dropdown/Checkbox/RadioButton/progress. The `.layout` property is a Kotlin tree, not an XML layout ID or inflated View.
- `EntitlementDetailBottomSheet.setupRow(rowBinding: EntitlementRowState)` assigns ordinary observable label/value/divider fields. EntitlementContent renders those values as Compose rows.
- Twelve nullable `_binding` properties are typed `BackupRestoreSheetState`, `BlockFreeModeState`, `BugReportFilesState`, `DnsRecordTypesState`, `HomeModeState`, `LogIntervalPresentation` (two hosts), `OrbotSheetState`, `ProxyCountriesSheetState`, `RethinkWarningSheetState`, `RpnStatisticsPresentation`, or `WgProxySelectorState`. They preserve presentation lifetime while the fragment view exists. Their state classes contain ordinary state/control objects; their child Views are not hidden inside binding-shaped properties.

A mechanical rename of these plain properties to `fields`, `uiState` or `presentation` would improve readability and avoid confusion during future audits. It is a clarity cleanup, not evidence of an unresolved binding framework dependency. Any such cleanup should preserve controller expressions/callback order, null/lifecycle semantics and tests. Android network/socket “binding” and per-row coroutine binding scopes have unrelated meanings and should remain outside the UI migration.

### Dormant renderers retained as source/reference material

The following historical View implementations still exist but have no active production construction, class-literal/factory reference or reflective-name use outside their own class. Their old XML references are retained resources with no application layout inflation path:

- AnimatedBorderCardView
- CountryMapView
- DolphinOceanView
- DonutChartView
- EmbeddedDolphinSignature
- FlagWatermarkView
- MoneyBackBadgeView
- RuleStateSwitch
- SpotlightOverlayView
- MaxWidthFrameLayout
- AppHistogramView

`AppHistogramView.Entry` is the specific exception: Home controller/presentation code still uses its nested data class. That does not instantiate AppHistogramView or run its drawing code. Removing that class would require moving the data type and updating references; this audit does not call it a live View renderer.

`RecyclerViewSpacingDecoration` and `SelectionIndicator` remain unused helper definitions with no production callers. Historical utility imports/extensions may also remain without constructing or mounting widgets. They are cleanup opportunities, not evidence of a visible XML screen.

The unused FirewallStatusSpinnerAdapter and SponsorBadgeView were removed. The entirely commented AlertAdapter and commented pre-Compose AlertsActivity implementation were removed. Their disappearance is verified by the whole-tree source scan, not just excluded from a screen ledger.

## Build generation

`app/build.gradle.kts` no longer enables View Binding or includes the `vbpd` delegate dependency in either phone or TV branches. The version-catalog binding alias and its dedicated version were removed. No Data Binding feature is enabled, and no source resource declares Data Binding expressions.

The guard inspects Gradle scripts, the version catalog, gradle.properties and Android.bp for binding flags/dependencies. It records their hashes. This establishes removal of generation requests and compile-time source use, not a built APK's contents. No generated/build output was available in this recovered checkout to inspect; a clean build must still establish that no stale generated source is being consumed by a different environment.

## Reproducible guard

Run from the source repository:

```sh
python3 scripts/compose-migration/check-binding-removal.py --self-test --report docs/compose-migration/binding-removal-report.json
```

The script uses only the Python standard library. It performs no build, Android launch, network request or production state mutation. The only optional output write is the requested JSON report.

The guard checks:

1. All discovered production source sets, including newly added flavors, while excluding test/androidTest source sets from runtime claims.
2. Generated binding imports/delegates/APIs, app layout references (including aliased R imports), all explicit inflation entry points, dynamic layout-name lookup, opaque/numeric Activity/Fragment content roots, AndroidView/AndroidViewBinding imports and calls.
3. Native widget constructors with resolved import aliases, fully qualified constructors, callable constructor references, wildcard native imports and native type aliases.
4. Reintroduction of dormant custom View constructors/references, permitting only the known nested histogram DTO.
5. Data Binding XML and external XML-to-layout links, so a new manifest/widget/style alias cannot silently make a retained legacy layout live.
6. Binding build flags/dependencies, two narrow FragmentContainerView interop exceptions and the invisible platform notification exception.
7. In-memory negative controls for bindings, inflaters, aliased imports, native widgets/dialogs, old adapters, dynamic layouts, Kotlin string interpolation and changes to the approved framework factory.

The report preserves deterministic source/build/resource/guard hashes, the self-test count, and per-file findings. It is intentionally stronger than searching a selected list of roots for `viewBinding`. It is still a conservative lexical guard, not a compiler, runtime call graph, dependency-bytecode audit or proof against arbitrary reflection. Passing it does not establish behavior, accessibility, visual parity or successful compilation.
