# Synthetic Compose/content JVM test safety review

Source-only review: 2026-10-14 UTC. Original checkpoint: `f84a752925cfd424db8bbf8de8760e873ae914c8`. Latest comparison snapshot: `1f5c96e9642d893cd3371bf19058b50b4b92df10`; final frozen working-tree identity is pinned below, with coordinator snapshot pending.

## Decision

Allow the **exact 60 classes / 399 declared `@Test` methods** listed below, under the execution conditions in this review. The initial review covered 56 classes / 369 methods. The successive addenda below bring the current directory to 62 Kotlin files: 60 test classes and two non-test helpers. No current class needed exclusion after fixture and reached-call-path review. This is **not** permission for a package wildcard, arbitrary future tests, real application startup or real security/network/account operations.

No Gradle task, test, application, service, native application backend, network probe, permission flow, emulator or device operation was executed for this review. Source reading, Git-object comparison, hashing and cached-JAR manifest reading were used. Prior pass reports were not substituted for source review. This document does not claim tests pass, that all UI paths are covered, or that visual parity is complete.

Machine-readable exact filters, per-class SHA-256, method names/counts, helper pins, configuration and artifact pins: [`content-test-safety-allowlist.json`](content-test-safety-allowlist.json). Use each `allowed_classes` entry as one exact Gradle class filter. The `class_details` entries define the current reviewed methods; do not append package wildcards. No additional execution class is implied by a referenced production class or helper.

## Mandatory execution boundary

- Use only exact fully qualified class names in allowed_classes; no package wildcard or unfiltered Test task.
- Fresh isolated local JVM process(es), plain android.app.Application for every Robolectric class; no device/instrumented execution or production RethinkDnsApplication construction/onCreate.
- Do not mix with any unreviewed classes, existing production Koin graph, actual services, real database, account, credentials, tunnel, user traffic or persisted user data. The separately source-pinned host review must be revalidated independently before combining it.
- Retain all current synthetic callbacks, constructor-free mocks, dispatchers, lifecycle bounds, empty/no-value paging fixtures, ordering and cleanup. Test/helper/production/dependency/launcher drift requires re-review.
- UtilityContentTest may reach Logger.e -> VpnController.writeConsoleLog only with the initial null rvpn service. Logger state/Koin must be fresh; this is a null-safe no-op, never permission for a service or real database.
- Use the exact reviewed Mockito and Byte Buddy startup-agent arguments in the opt-in launcher; no dynamic attach flags, OS/security/network changes, native application backend or unreviewed agent.
- Visual output must be workspace-local. Unset rethink.visual.output and RETHINK_VISUAL_OUTPUT or set the effective value to an explicit directory beneath this task workspace; no user-data or external output path.
- Robolectric native graphics, shadowed package/keyboard/window/clipboard/toast/accessibility operations and ordinary test reports/PNG/JSON/temp output are permitted within the isolated test workspace. No real export/permission/account/network operation is covered.

The 18 actual-host classes in `host-safety-review.md` are a **separate clearance**, not additional entries in this manifest. This review does not revalidate their older pins or authorize the entire `app/src/test` tree. Combining the two sets is permissible only after separately checking every applicable host pin and preserving both sets of conditions; otherwise use a separate fresh run.

## Fixture and reached-call-path review

### Presentation-only state and Compose content

About, application details, firewall rows/filters, DNS settings/protocol editors, home content/chrome, miscellaneous/proxy/tunnel settings, network sheets, RPN controls/configuration, purchase, settings and most utility tests construct presentation state and attach recorder lambdas. `createComposeRule` and explicit `createAndroidComposeRule<ComponentActivity>` use synthetic framework hosts under plain Application. Resource IDs, class literals and nested enums do not construct the named production controller.

- About diagnostics use supplied strings and fake file/table rows. Database dump, token regeneration/copy, bug report open/delete/send and DNS record actions stop at recorders. No database, file archive, stacktrace service or token store is queried. `NoSurfaceMagnifierShadow` overrides only framework `Magnifier.dismiss`; it adds no app behavior.
- DNS editors forward synthetic text to supplied callbacks. `SmartDnsEndpoint`, block-free rows and endpoint row state are data objects. Tests never construct `DnsEndpointPresenter`, bind `RethinkEndpointPresenter.BoundEndpoint`, poll real service status, resolve DNS IPs or follow URL links. New tests entering those paths require review because they can evaluate Backend constants, managers and network helpers.
- Home tests do not instantiate `HomeScreenFragment`. `HomePresentation`/HomeNode state and Home dialog actions are local. The one populated activity-group fixture supplies `iconResolver = { _, _ -> null }` and `favIconEnabled=false`. `CachedActivityIcon` therefore creates no Glide target/request/cache and its disposer does not call Glide. Pure `AppActivityAdapterStateTest` never renders rows or invokes its default icon resolver. Formatting reaches only local time/number helpers. Default RPN signatures are null, so the renderer returns before animation-settings lookup.
- Application-details reconstruction uses empty presenters and recorder callbacks. It exercises warning Proceed and notes Save only as synthetic UI actions, not actual AppInfo controller/database actions. The firewall reconstruction uses nested `AppListActivity.SortOption` values without constructing that Activity.
- Network/settings/sheet state reads resources and uses WgControlState/NetworkSeekState/NetworkSpinnerState. Proxy strings, password-field synthetic text, lockdown switches, backup/restore, Orbot and country selection are local state/recorders. No proxy, Tor, backup worker, document picker, permission request or system settings is started. Link-span conversion tests inspect annotations and never activate a link.
- PurchaseContentTest renders synthetic processing/refund/entitlement/device/checkout data. Its populated `PurchaseHistoryAdapter` uses AsyncPagingDataDiffer fed only by `PagingData.from` synthetic history, date formatting, enum labels and resource colors. No billing/account identity, key creation, repository or CheckoutActivity is constructed. Action labels do not imply corresponding real operations.
- RPN/SSID state uses Bundles, strings, in-memory lists and local `SsidItem` JSON parsing. No Wi-Fi discovery, location permission, account or WireguardManager occurs.

### Blocklist presenter tests

`BlocklistSelectionPresenterTest` uses constructor-free strict Context and RethinkBlocklistFragment mocks. RethinkBlocklistManager object construction installs lazy Koin delegates and immutable group metadata only; it has no eager repository load. All six reached repository read/write entry points are intercepted before actions, with unexpected calls failing closed. Local/remote selections run on test dispatchers drained with `advanceUntilIdle`. Fragment publishing is a mock. The presenter does retain independent coroutine scopes, so the current explicit drain and dispatcher wiring are part of the clearance. `onEntries`/`openUrl` is never invoked.

`RethinkEndpointPresenterTest` uses strict AppConfig/Context doubles and test lifecycle/dispatchers. VpnController/Logger/UIUtils/Utilities are intercepted before presenter actions. Its reached VpnController initializer creates only null/scalar/lazy/LiveData state. Selection reaches only the fake AppConfig; copy/Toast are recorded boundaries; edit constructs an Intent but `Context.startActivity` is a capture stub. No destination is launched. The dangerous `bind`/BoundEndpoint status/flag jobs are not entered, so imports of Backend and IP-download classes do not execute those paths.

### Real framework UI helpers and synthetic dialogs

`SnackbarContractTest` constructs only ComponentActivity and FrameLayout, then the existing SnackbarHelper/ComposeSnackbarPresenter. The helper's global queue/throttle state is explicitly reset. Timeouts, accessibility reads, simulated touch exploration and view/inset operations are process-local Robolectric shadows. SnackbarHelper also calls Logger.d; its fresh default suppresses DEBUG console forwarding and an unavailable lazy settings lookup falls back to ERROR. No real preference store, service or external communication is reached.

`RpnConfigurationDialogTest` constructs the real RpnSsidDialog around synthetic SSID text and a fake onSave, or WgAlertDialogBuilder with recorder actions. RpnSsidDialog parses text locally, calls `setWgContent`, and changes only its shadow window. Its Save path logs through Logger.i, invokes the supplied recorder, then dismisses. Fresh Logger defaults suppress INFO database forwarding; neither a real repository nor service is available. This is genuine dialog-helper coverage, not production Activity or RPN manager execution.

WgAlertDialogBuilder's current code resolves Android/Material theme attributes, builds Dialog/Compose content, dispatches only supplied callbacks, caps the shadow window width and optionally removes dimming on that window. `setWgContent` only installs the synthetic host's lifecycle/saved-state owners and disposes composition on detachment. No VPN, import/export, account, storage or permission path is inside this builder.

### Rules, statistics and traffic boundaries

- CustomRuleActionsTest always supplies `FakePort`, both test dispatchers and constructor-free Mockito EventLogger. Every mutation/import call terminates in a list/counter recorder. `content://fixture/rules.txt` is parsed as a Uri and passed to FakePort; no ContentResolver document read occurs. Successful operations may show a shadow Toast. The test saves/sets/restores `RethinkDnsApplication.DEBUG`; the companion initializes only that Boolean. It never constructs or calls onCreate on RethinkDnsApplication. Manager references are independent nested enums. The edited CustomIp has port=0, excluding `IpRulesManager.joinIpNetPort` from editor initialization.
- CustomLanIpStateTest uses fake preferences and Java inet.ipaddr parsing, not networking. Its normal INFO/WARN Logger calls are below fresh `uiLogLevel=ERROR`, so dbWrite returns before VpnController. Rule content/state tests avoid production RulesPage/StoredRuleDetails/rememberRuleApp/rememberRuleFlag entry points.
- SummaryStatisticsRouteTest constructs the actual adapter's memory-only paging presenter, but never renders a production row. It reflects only onRowClick for the pinned aggregate/top-active branches. RecordingContext.startActivity stores Intent copies. Strict settings/config dependencies are verified unused. Destination class literals/extras do not create the Activity. App rows and scoped blocked-domain sheet branches are not tested.
- BlocklistAppsContentTest supplies a non-null identity resolver and memory-only domain-fetch callback. That excludes FirewallManager identity lookup. Its fallback icon is a resource drawable. Other statistics content fixtures have null presenters/no production rows; selection/math tests are scalar/list operations. The retained filter XML contains only framework/AndroidX/Material widgets.
- TrafficLogFragmentControllerTest is the important exception to “content-only”: it runs real ConnectionTrackerFragment/DnsLogFragment lifecycle methods in synthetic FragmentActivity. Before creation it installs only plain Application, strict constructor-free preferences/config/viewmodels/repositories and empty/no-value LiveData in Koin. The existing adapter constructors create paging state/cache only. No PagingData is emitted, so no real row identity, favicon, rule, status or backend branch can run. Logger.v is intercepted. Filter/search/restore methods terminate at mocked viewmodel recorders. Keyboard restart is a shadow operation. Teardown destroys the host, removes Logger interception, stops the test Koin graph and restores Main. Do not add populated paging, bottom-sheet/delete actions or production viewmodels on this clearance.
- TrafficLogsComposeTest uses synthetic Event data through FakePresenter and local copy/trace callbacks. WgLegacySearchParityTest instantiates only an offscreen SearchView. Other WireGuard state/rich-text/SSID tests use callbacks, local formatting and Bundles; no actual WireGuard host or manager is instantiated.

### UtilityContentTest: reachable logging deserves an explicit exception

The synthetic blocked-app package is intentionally absent from Robolectric's package manager. `BubbleScreen.AppIcon` catches that shadow NameNotFoundException and calls real `Logger.e`. Logger may attempt its lazy settings lookup, which falls back to ERROR if no test Koin value exists; it then formats a synthetic ConsoleLog and reaches `VpnController.writeConsoleLog`. The inspected method is exactly `rvpn?.writeConsoleLog(log)`, and fresh VpnController initializes `rvpn=null`. Thus it performs no real DB/service action in the required fresh process. This class **does initialize app logging/VpnController state**; calling it “no singleton ever executes” would be inaccurate. Do not mix in a class that installs a real Koin graph, assigns the private service, or starts a VPN.

The remainder of utilities uses synthetic callbacks. The empty PingTestHistoryAdapter has only an empty list; no RpnProxyManager method or ping entry is used. Support email, Stripe, sponsor billing, TCP, reachability and pause callbacks are recorders, never actual operations.

### Visual output and widget references

ContentVisualParityTest has four methods and WgAlertVisualParityTest has two; each iterates eight fixed visual scenarios. These are six JUnit methods, not 48 separate JUnit cases. They capture only synthetic ComponentActivity/Dialog content and detached/offscreen XML references; PixelCopy requests a Robolectric window. Baseline layouts inspected here are `activity_customer_support`, `fragment_sponsor_success`, `dialog_info_rules_layout`, `dialog_input_ips`, and the statistics-filter layout. They contain framework/AndroidX/Material widgets, no production Fragment/Activity/custom app startup. Root navigation constructs only BottomNavigationView; it deliberately does not inflate the NavHost-containing root Activity layout.

VisualParityHarness writes PNGs and measurements.json. This is permitted test evidence generation, not an application backup/export. Its path override uses the `rethink.visual.output` system property, then `RETHINK_VISUAL_OUTPUT`, then relative `build/outputs/compose-visual-parity`. Verify the effective destination is under this task workspace before running. Synthetic callback guards reject navigation/sending during captures. Typography-reference tests resolve local resources/fonts and inspect styles without production hosts.

## Search-focus and typography addendum

The initial 56-class/369-method review is preserved separately in `content-test-safety-baseline-allowlist.json` for its exact earlier source snapshot. It is historical and does not clear later source by itself. The current main JSON supersedes it for the current 60-class source.

- LogSearchBarComposeTest adds four tests of synthetic routed/restored/typed query strings, recorder listeners, local expansion state and editor focus. It renders LogSearchBar in the Compose test host under explicit plain Application.
- LogSearchViewReferenceTest adds three offscreen AppCompat SearchView tests with synthetic queries and callback counters. No searchable provider, suggestion adapter, search Intent, production Activity, repository or service is configured or invoked.
- The changed LogScreen binds a local FocusRequester to the editor and moves the root expansion callback to an explicit search IconButton. Its reached focus clear, InputMethodManager.restartInput and software-keyboard show are shadow UI operations. No permission, exported Intent or backend path is added. Existing traffic fixtures retain their reviewed data/lifecycle boundaries.
- UtilityTypographyTest now builds the override Configuration from base.resources before touching the wrapper. RethinkTypographyReferenceTest now accepts a 0.51 px font-size difference for integer-pixel TextAppearance rounding. These are source-only safety observations, not approval of visual acceptance criteria or a test-pass assertion. Neither change adds a consequential path.

## Final title-selection and dialog-contrast addendum

Source-only additive review: 2026-10-14 UTC. At this earlier 59-class review, app/src bytes and file set were compared with local Git snapshot `5aaca3b1a7b55c38bd1fe5e59ac298bda4c906d6` and matched exactly. That checkpoint contained 59 classes / 382 declared methods; the subsequent DNS/fixture addendum below supersedes the current source pins. This addendum ran no Gradle task or runtime test. Final execution must use already-cached dependencies/offline mode; it does not permit retrying a denied artifact-download destination.

- `PagedStatisticsTitleSelectionTest` adds six exact methods. Four test domain/IP/ASN/blocklist synthetic headings; another tests updated text after the empty-to-list transition; the sixth checks that a caller omitting the opt-in remains nonselectable. Every PagedStatisticsScreen call supplies `presenter=null`; the only extra row/chip content is literal Text. No adapter, repository, viewmodel, rule, favicon, native backend or actual DomainConnectionsActivity is constructed.
- The test explicitly selects plain Application and Compose's synthetic host. A long press/drag and Ctrl-C exercise the real Compose SelectionContainer copy handler, but its ClipboardManager is Robolectric's simulated Android service. Each method seeds a synthetic sentinel and `@After` clears the simulated primary clip. This neither reads nor changes the dot host's or user's actual clipboard. The method-scoped StatisticsNoSurfaceMagnifierShadow replaces only Magnifier.dismiss, matching the earlier About fixture boundary; it does not replace selection or copy behavior. Device magnifier and visual copy-toolbar behavior remain unverified.
- `PagedStatisticsScreen` adds a default-false `selectableTitle` parameter and wraps only title Text in SelectionContainer when true. Subtitle/chips/rows are outside that selection boundary. No side-effectful call was added.
- `DomainConnectionsActivity` changes only its shared-screen call to `selectableTitle=true`. It is source-pinned because route tests name the destination, but none of these 59 tests instantiate or launch it. This change does not add actual-host clearance.
- `WgAlertDialog` composites its known surface against an opaque theme background, calculates primary-text contrast with AndroidX ColorUtils, and selects primary for contrast >=4.5 or onSurface otherwise. These are in-process color calculations/resource reads; button callbacks and lifecycle are unchanged.
- `WgAlertVisualParityTest` now calls VisualEvidence.contrast on the actual rendered synthetic button text. The helper reads the existing text-layout color and theme colors, composites them, calculates ratios, records JSON measurements and invokes the existing required-check function. It introduces no new output destination, callback activation or external operation. The previous workspace-only PNG/JSON output restriction still applies.

Exact additional/updated pins are in the JSON `additive_review.source_pins`; the original historical 56-class baseline file is retained unchanged.

## Final DNS lifecycle and fixture-correction addendum

Source-only delta review: 2026-10-14 UTC. The frozen working tree adds `DnsEndpointLifecycleComposeTest` with 17 methods, bringing the exact allowlist to **60 classes / 399 declared methods**, with no exclusions. Relative to snapshot `5aaca3b1a7b55c38bd1fe5e59ac298bda4c906d6`, there are nine changed existing files and that one new test in this review's production/content-test scope. The last bound-branch correction was separately compared with the intervening 60-class/396-method snapshot `1f5c96e9642d893cd3371bf19058b50b4b92df10`: exactly five changed files and no added/missing files. There is no configuration, launcher or cached-agent drift. A coordinator snapshot of these final bytes is pending; the exact SHA-256 envelope and individual pins identify the cleared source. No Gradle/runtime execution was performed here.

### Reached DNS lifecycle callbacks

The lifecycle test explicitly uses plain Application, native Robolectric graphics, the generic Compose test host, a synthetic LifecycleRegistry owner, CopyOnWriteArrayList traces and CompletableDeferred synthetic results. Every DnsEndpointRow call injects `Dispatchers.Unconfined`, except the static-label test which injects StandardTestDispatcher and explicitly runs its synthetic scheduler. Description/flag callbacks record/return literal strings or await in-memory deferred values; select/info callbacks are empty. `rememberDnsEndpointRow` receives only FakeEndpoint/FakeRowFactory. No real DnsEndpointPresenter, production row factory, repository, AppConfig, DNS/IP lookup, network client, service, account or native backend is created or invoked.

The row uses `DisposableEffect(item, owner, ioDispatcher)`, two lifecycle-owned jobs, child status-read jobs, lifecycle-state checks and cancellation on disposal. Polling can terminate on an inactive tick; one-shot flag work can complete while stopped without publishing; replacing/removing the row cancels its jobs; owner destruction cancels its scope. Its nullable description callback is skipped when absent. Static `initialDescription` is ordinary remembered Compose state; asynchronous description results publish only at STARTED. Existing DnsProtocolContentComposeTest still supplies null-returning description/flag lambdas or SmartDnsContent's local selected-label/null values. SmartDnsContent remembers row state, supplies static resource text without an async description read, and uses the latest supplied selection/info recorders.

### Final bound-branch closure and three additional regressions

`bindDnsEndpointDescription` is a top-level helper with no eager initialization or reference to an app singleton. It returns empty data for an unselected row, evaluates the supplied guard once for a selected row, and returns either the supplied status callback with poll=true or a supplied static label. The helper does not execute the status callback itself. The production caller chooses its real tunnel/smart-DNS branch at bind time and keeps that branch for subsequent status reads; relay selection similarly captures its one-shot description branch at bind. Those real presenter entry points remain outside this test clearance.

The three new tests pass synthetic booleans and callbacks that only record strings, return literal text, or throw AssertionError if a forbidden branch is evaluated. They check static/polling branch retention after changing the fake guard and the unselected branch's lack of callback evaluation. A prior static-label test is strengthened/replaced by `staticSelectedLabelIsRenderedBeforeAnyIoDispatchWhileStopped`: StandardTestDispatcher proves initial text is rendered before any fake IO callback runs, then checks the existing flag lifecycle gate. It adds no real dispatcher work or external effect. A final compiler-only correction removes the unresolved assertDoesNotExist import; its SemanticsNodeInteraction member calls and every method body remain unchanged.

The production presenter itself is **not executed by these 60 classes**. Its real callbacks can still reach VpnController, Backend constants, FirewallManager or IP/network helpers in production. This clearance must not be extended to real presenter-backed populated DNS rows or production DNS host lifecycle tests. A complete suite search confirms the row/helper entry points occur only in the synthetic lifecycle/content classes and SmartDnsContent with local callbacks. Consequently, **no reviewed DNS lifecycle or bound-branch callback reaches a real service or native/network backend**.

### Six fixture corrections

- BlocklistsComposeTest adds Robolectric native graphics and waits until the replacement synthetic dialog is displayed before Back. Its empty paging and recorder callbacks are unchanged.
- BlocklistAppsContentTest drains the paused main queue during bounded waits and waits until the synthetic owner's lifecycleScope has no children. The existing non-null identity resolver, synthetic domain fetches/deferred values and resource-only icons remain unchanged; no FirewallManager lookup or new IO source is introduced.
- LogSearchBarComposeTest calls Robolectric Instrumentation.setInTouchMode(true) in an ordered ExternalResource before its generic Activity attaches. This changes only simulated process UI input state, not the dot host's device settings or real Android instrumentation.
- TrafficLogFragmentControllerTest allows only framework ViewModel.clear disposal on its constructor-free strict fake viewmodels. The reflected call is recorded by MockK and returns null; constructors, production onCleared logic, repositories and application methods remain unexecuted. Unlisted application calls still fail closed.
- TrafficLogsComposeTest applies AppThemeWhite to the already-created generic test Activity before rendering. Its final two existing item-count waits use a 5000 ms bound and `compose.runOnIdle` to drain the paused main queue. Fake Event rows, in-memory PagingData, exact order/cardinality and display assertions are retained. It does not construct a production Activity or change persistence.
- UtilityTypographyTest changes a font-size assertion's local tolerance from 0.1 to 0.51 px for integer TextAppearance versus fractional Compose rounding. Existing methods/content assertions remain; this is not a new effect or a test-pass claim.

The exact current hashes and comparison lists are in `dns_lifecycle_delta_review` and `dns_bound_branch_delta_review`. All existing fresh-Application/no-production-graph, workspace-output, exact-class and cached/offline restrictions remain mandatory. The historical 56-class baseline is unchanged.

## Test launcher and instrumentation review

The current `../validation/isolated-tests.init.gradle` SHA-256 is `71495eee566283a2aadf2d00d5736ad5f26fcbfd681776c39a1d4c40af3007db`. It matches the applied launcher pin recorded by the separate host review after its Byte Buddy addendum. It sets a workspace-local synthetic user.home, refers to the unchanged existing Java CA store, forwards the environment proxy only to forked Test JVMs, and uses `maxParallelForks=1`.

The exact existing cached artifacts were rehashed and their manifests reread:

- Mockito `5.23.0`: `ae295bebd5d11fab97ab297815dc7617188b86003cbce3dfd5c0d5c3a6cc4a0c`, Premain-Class `org.mockito.internal.PremainAttach`
- Byte Buddy agent `1.18.2`: `d91e2d6d1b777687ab14a52b18b08aca32c4c91274131c99907674c661402a76`, Premain-Class `net.bytebuddy.agent.Installer`

The prior source/bytecode review in host-safety-review.md applies to these identical artifacts: premain stores the JVM's Instrumentation holder; MockK finds the existing Byte Buddy Instrumentation before any external/self-attach branch. Mocking and its temporary bootstrap support JAR remain process-local. This review did not rerun that bytecode experiment or execute the agents. No new artifact, dynamic attach flag, global JAVA_TOOL_OPTIONS, OS security/network setting or Go backend is authorized. The init script applies to every selected Gradle Test task and **is not an allowlist**; exact filters remain mandatory. A proxy/trust-store argument is test-infrastructure configuration, not a network sandbox or permission to invoke app networking.

## Source identity and invalidation

The initial source review used checkpoint `f84a752925cfd424db8bbf8de8760e873ae914c8` plus three compiler corrections, preserved in the historical baseline. A later 59-class checkpoint matched `5aaca3b1a7b55c38bd1fe5e59ac298bda4c906d6`. The final 60-class working tree is now pinned by its exact class/helper/production/configuration/artifact hashes and the conservative source envelope. Coordinator snapshot creation is pending; this review does not label changed working-tree bytes as identical to the earlier Git object.

Within review scope, the current tree differs from the original f84 checkpoint in 16 existing files and adds 4 test files. The precise list is `reviewed_source_deltas` plus `source_tree_pin.added_since_checkpoint`. The final DNS/fixture delta relative to 5a is separately recorded in `dns_lifecycle_delta_review`: nine existing files and one new test. The final five-file bound-branch/paging-wait delta relative to 1f5 is recorded in `dns_bound_branch_delta_review`; its 1460-file set is unchanged. All those exact changes were inspected for safety. Unrelated actual-host/device test changes are outside this content clearance.

Git HEAD alone does not identify this working tree. The conservative source digest covers production source/resources plus content tests and test resources, excluding unrelated host/device test source. Its canonical ordering is lexicographic POSIX path-component tuple order (`PurePosixPath.parts`), matching the generator used for earlier envelopes; it is not whole-path string order. It is a change detector, not approval of non-reached production methods. Any relevant source, test/helper, resource, dependency, launcher, output-path or fixture-order change invalidates reliance on the affected clearance until reviewed. Other tests, new methods and broader production paths remain outside scope.

## Exact class allowlist

Counts are declared @Test methods, not assertions, loop iterations or parameter expansions. SHA-256 identifies the full current test file, including helpers declared in it.

| Exact fully qualified class | Methods | Test source SHA-256 |
|---|---:|---|
| `com.celzero.bravedns.ui.compose.about.AboutContentComposeTest` | 16 | `d5eebe93fba3b7c42a55d2528a56d1b3e20c6757853b3a6c939c6ab3cdc6d497` |
| `com.celzero.bravedns.ui.compose.about.AboutPresentationTest` | 13 | `b5974d39c5c88add07daf2081bf6b7b60c7c2d9d7d4471ac1f9b2d11baada60b` |
| `com.celzero.bravedns.ui.compose.appdetails.AppDetailsReconstructionTest` | 9 | `0c5ac305e41434fe619e66f9f1d12c0261b61a6a96b78d945f9e710153e49c2d` |
| `com.celzero.bravedns.ui.compose.blocklists.BlocklistSelectionPresenterTest` | 6 | `df5028fd6008414e490572657b9494e1bffa4942c1e9966b46b6bbd72aa265ae` |
| `com.celzero.bravedns.ui.compose.blocklists.BlocklistsComposeTest` | 11 | `22ce2034655c66fc0c43081d10cb702e883adbed048a38abbf6f9a5ee4880334` |
| `com.celzero.bravedns.ui.compose.blocklists.RethinkEndpointPresenterTest` | 7 | `50560781f9e011e231efaea366b02a5aa2fce406d7134be3f85d48e3f569570d` |
| `com.celzero.bravedns.ui.compose.chrome.HomeChromeDialogPolicyTest` | 4 | `08f349f781d431a6ed8b141a7efdf46dae212356723d61b7fc80e23c303bff97` |
| `com.celzero.bravedns.ui.compose.chrome.RootChromeContentTest` | 7 | `8edffab80cf2ce0a987d797d5e0d4a56b0d55542f8996e424f3e2ad8d1f644b6` |
| `com.celzero.bravedns.ui.compose.chrome.RootNavigationTypographyTest` | 1 | `5b4737c1642e017f581a6be2cf65063c7d4c18816ee6dac205cdbcd2a487e02d` |
| `com.celzero.bravedns.ui.compose.chrome.SnackbarContractTest` | 9 | `73de009d0667aea3b03f3475beb5085648d46de0a08296987c569c7fa5ac7bf1` |
| `com.celzero.bravedns.ui.compose.dns.BlockFreeChipGroupTest` | 6 | `873afc784cf951d33847a128790c67494cb81a7d27335701aca644e907c10c88` |
| `com.celzero.bravedns.ui.compose.dns.DnsEndpointLifecycleComposeTest` | 17 | `54cdbc8968dc95e14059667c1952d491b22d34b08265eb0b309ffd2550564549` |
| `com.celzero.bravedns.ui.compose.dns.DnsProtocolContentComposeTest` | 10 | `f5b30445328588f592ec2db78d9c51614a4e8972ac6abe1ccdfb24dcc296ab43` |
| `com.celzero.bravedns.ui.compose.dnssettings.DnsControlTest` | 7 | `d76e3f3a123fd118f07c033b54971c86804739ad7c3542324ea8e40a025b382c` |
| `com.celzero.bravedns.ui.compose.dnssettings.DnsProtocolGridComposeTest` | 1 | `ecd61e966a7d3d87e8a5cbb099b6551397f091ea9621de18505c44491844e2f8` |
| `com.celzero.bravedns.ui.compose.dnssettings.DnsSettingsChoicesComposeTest` | 1 | `aa57b10058965c9b72f971966e9a9ad8dcf3096c85fabfee0ec2c8a31331d3c4` |
| `com.celzero.bravedns.ui.compose.dnssettings.DnsSettingsComposeTest` | 7 | `03093cf4aab6b6dec60dfc30f1545090735a24b926d598980b1104df601eb780` |
| `com.celzero.bravedns.ui.compose.firewallapps.FirewallAppsReconstructionTest` | 6 | `14e020aed0efb41d28ff8f3158974a5097cac27f0d1940a2ef7f55472bc047d8` |
| `com.celzero.bravedns.ui.compose.home.AppActivityAdapterStateTest` | 6 | `272ab2899b723fc66c702aa58d68d7743d693f9068a42cb33cb32d6e79af6918` |
| `com.celzero.bravedns.ui.compose.home.HomeContentTest` | 6 | `f684b0e6297a934bd1573bb6a787c58cb4ad72f62d159c61b90e48761dc81220` |
| `com.celzero.bravedns.ui.compose.home.HomeDialogTest` | 6 | `2c07dce9867e1487310bdf59607a8103c38ede07e93a5dc1549253b2fb122935` |
| `com.celzero.bravedns.ui.compose.home.HomePresentationTest` | 10 | `042fdba7eb62ff7eed02dbf517a88667a00649589994b4a35a0ca6c18bd069d7` |
| `com.celzero.bravedns.ui.compose.networksettings.NetworkSettingsComposeTest` | 8 | `bc5cfe5ef476bd7a8cdbb13a910b8d6eec56658d6bf7387e2e5d13fb48e02668` |
| `com.celzero.bravedns.ui.compose.networksettings.NetworkSettingsStateTest` | 17 | `e28ca8283e9335451c8ad52cf648b72772ddafe3c606d943ff7dbc2f0a6419a9` |
| `com.celzero.bravedns.ui.compose.networksheets.NetworkSheetContentTest` | 8 | `2a6b76da674bce79bb01e639b044687fe9ec7a88f0a7a47ac8abc2978a706c36` |
| `com.celzero.bravedns.ui.compose.networksheets.NetworkSheetStateTest` | 6 | `b1e67587b67ea29f667ce6fce843fd298162aa580fafb52244156bb1fec58ba0` |
| `com.celzero.bravedns.ui.compose.networksheets.OrbotInfoTypographyTest` | 2 | `f7e4320912dd731d54592c4978a9c77723888907ccf26a485dfb24438ce4cbb6` |
| `com.celzero.bravedns.ui.compose.purchase.PurchaseContentTest` | 10 | `13928579a8dd5d4f8978bf30bfa7c3ac0174695be5dc5a1d5cd5df173c27e244` |
| `com.celzero.bravedns.ui.compose.rpn.RpnControlStateTest` | 8 | `f5390b9cf2bc32dd937123ee869a62ad76690b49ee895a6cabe22046767eabae` |
| `com.celzero.bravedns.ui.compose.rpnconfig.RpnConfigurationComposeTest` | 16 | `85e2f430af874c403bfced822db4342d5da2235155e620ee8ce94363dc1cbc37` |
| `com.celzero.bravedns.ui.compose.rpnconfig.RpnConfigurationDialogTest` | 3 | `34e86b9d068083820987f1e1d803a4b5562c4e5503c0c5f49606062a1ec795c7` |
| `com.celzero.bravedns.ui.compose.rpnconfig.RpnSsidStateTest` | 10 | `378dfd1f89c0bc65de40bf97d9578efd78588544affb708b50e14f523fc90d31` |
| `com.celzero.bravedns.ui.compose.rules.CustomLanIpContentTest` | 2 | `1051b73ec8ac751e41f9f0f7d18c193627d234b691d996c6eb075e5475cde4f4` |
| `com.celzero.bravedns.ui.compose.rules.CustomLanIpStateTest` | 6 | `640a1dda72ec267919e2f8ccb3ac9bc850daaf127df859d2c3b52e05037f3a69` |
| `com.celzero.bravedns.ui.compose.rules.CustomRuleActionsTest` | 8 | `a2d19eb18bc198a0651862e1f2c1d21b6717aa1d9ad928069d0b44a880e615f4` |
| `com.celzero.bravedns.ui.compose.rules.RulesContentTest` | 5 | `ae90ccf3bb1f7a9f5c6c2d10b5dbfbca9dd976ef3bf463c93bfc703dbca488fb` |
| `com.celzero.bravedns.ui.compose.rules.RulesPageStateTest` | 8 | `178bb90a401701a2d63eb99c506de8b9e3f41762e46e126d3b03a395c80d4887` |
| `com.celzero.bravedns.ui.compose.settings.SettingsStateParityTest` | 8 | `0bfc5a1e66bd0a16c28f1dc66b8b4c62e916ee0601a8ab7af6da45b3ab8d9863` |
| `com.celzero.bravedns.ui.compose.statistics.BlocklistAppsContentTest` | 2 | `857d0d3aa24108aeecbe696b579063983a9244a647dfee654ddea610d7a044b8` |
| `com.celzero.bravedns.ui.compose.statistics.PagedStatisticsTitleSelectionTest` | 6 | `b07c417c207cfacdc5abc51c85db571ccd4d274b179cb781cda80b4418ae23cd` |
| `com.celzero.bravedns.ui.compose.statistics.StatisticsContentTest` | 6 | `1ce7fca9cf8fa6e1b8a9dadf87adac139cbed7fe17ee3e9c407428fca5f93cc4` |
| `com.celzero.bravedns.ui.compose.statistics.StatisticsMaterialReferenceTest` | 3 | `3f276bc6f15d4296562502aae9ef72d08c60f7cb92bba06cd750b4c15d19f41c` |
| `com.celzero.bravedns.ui.compose.statistics.StatisticsSelectionParityTest` | 7 | `8ba6cc98ad3a8c6215c8fd32ff061d80745f97b5fa40697e62bdf7bfa7953098` |
| `com.celzero.bravedns.ui.compose.statistics.SummaryStatisticsRouteTest` | 3 | `2b4bb2fbef9a72974a2027b3b4fd3840a092f87eae34305bbb625abd8c0509a3` |
| `com.celzero.bravedns.ui.compose.theme.RethinkTypographyReferenceTest` | 1 | `90c5dad36f3537f8382c9ae6f80bbb54e0836181e8cd9db684c19b9ef880323e` |
| `com.celzero.bravedns.ui.compose.trafficlogs.LogSearchBarComposeTest` | 4 | `5c31d3dc4416d7c89217c0a8f4db06417c843413d8edc5c3f53976b7f00adf8f` |
| `com.celzero.bravedns.ui.compose.trafficlogs.LogSearchStateTest` | 5 | `0ca883b943365d5f67584a603b08b57cf02546eaf829a0ff50687440e972fe5c` |
| `com.celzero.bravedns.ui.compose.trafficlogs.LogSearchViewReferenceTest` | 3 | `e7ce0c894ee9b17fa78f4911cfc6096356da5b6871712ece4221895070fd5e05` |
| `com.celzero.bravedns.ui.compose.trafficlogs.TrafficLogFragmentControllerTest` | 7 | `f240c99d7a32e3d1a021d985cac0df3ff92869d9077c0a64d01c9d831d600c47` |
| `com.celzero.bravedns.ui.compose.trafficlogs.TrafficLogsComposeTest` | 5 | `449bf78c96e757981a4eb4d59b735bd0bc60d8c1daf9ca047a65495922012849` |
| `com.celzero.bravedns.ui.compose.utilities.UtilityContentTest` | 14 | `59d0b660e36af9e48d305753649b4a0eaa2e3ad3b86ab76ed494d759d5a02234` |
| `com.celzero.bravedns.ui.compose.utilities.UtilityStateTest` | 7 | `0ea548ce3a253decc8303c37bab00230ab79a5948a49542d4c845e8af968a4e3` |
| `com.celzero.bravedns.ui.compose.utilities.UtilityTypographyTest` | 6 | `5380cad5e68acf9e5ae7c11be1570d23eaf479d800deb3a6d555f21f7a8e4eca` |
| `com.celzero.bravedns.ui.compose.visual.ContentVisualParityTest` | 4 | `eb41ec45aec45bc5dcd65a11d3406ef759a42dd941bfdb4f0993494366546ba8` |
| `com.celzero.bravedns.ui.compose.visual.WgAlertVisualParityTest` | 2 | `9610d5d2cd85c0fea25d1c3c5fcca51a086be36351b624628cb3513a3754596f` |
| `com.celzero.bravedns.ui.compose.wireguard.WgControlStateTest` | 10 | `9860f03260929a42cfb186f56563b272fd2bb7a27dfd73f822b0b6e215887ba9` |
| `com.celzero.bravedns.ui.compose.wireguard.WgFormRestoreTest` | 3 | `7f2216402183079d759b2cfc66043b61298e2a702dabb9743b8ec6f8eb0df768` |
| `com.celzero.bravedns.ui.compose.wireguard.WgLegacySearchParityTest` | 1 | `55d45978af092a597ad8eac8b0291079030494cee430ca628c5878872034f946` |
| `com.celzero.bravedns.ui.compose.wireguard.WgRichTextTest` | 3 | `1e85e3462cbf35e4415237df0f9335eb5706982f74b130f30ce7fe63bb34ba4f` |
| `com.celzero.bravedns.ui.compose.wireguard.WgSsidStateTest` | 6 | `da35d881e7e040fd2b43c08cf3f4be28fd07371ddb818c0c57b4733198e3588b` |

### Non-test helper pins

- `app/src/test/java/com/celzero/bravedns/ui/compose/dnssettings/DnsUiTestTheme.kt`: `ffb2455eb76e6cf523a83f45fc6135a04856b936e96b73048ea7329058923b73`
- `app/src/test/java/com/celzero/bravedns/ui/compose/visual/VisualParityHarness.kt`: `46100c282c62e52f558268ed1a7ee050ec51c13a27e1db97244194278dd52181`

Conservative production/content-test source envelope: 1460 files, SHA-256 `e7b89a0d41f650816063ca2c39b57963bfb3d9a4a4b6f79e5c8cf081411164fb` using the JSON's documented canonical algorithm.
