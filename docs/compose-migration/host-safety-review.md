# Actual-host UI test safety review

Source-only review: 2026-10-14 UTC. Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
Reviewer executed **no Gradle task, application, test, service, emulator, device action, network probe, permission flow, or native application backend**. Read-only source inspection, SHA-256 hashing, and `javap` inspection of already-downloaded test libraries were used. This is a pre-execution boundary review, not a passing test result or a complete parity certification.

## Decision and execution limits

The exact source-pinned 18 classes below (157 currently declared test methods) are allowlisted for an **isolated local JVM Robolectric run with synthetic data only**. Use fully qualified class filters; never run the unfiltered unit-test task or a package-wide wildcard on the strength of this report. The suffix `.*` below means only the methods present in that class at its listed hash; adding a method invalidates that class's clearance.

Conditions:

- Plain `android.app.Application` is explicitly selected on every allowlisted class. The manifest's production `RethinkDnsApplication`, its Koin modules, and real repository constructors must remain excluded. No device/instrumented execution is covered.
- A fresh isolated test process must not inherit a previously running production app, installed Koin graph, attached VPN/service, real database, account, keys, tokens, or test state from unreviewed classes. Only the listed classes may share this run. The synthetic `user.home` and current isolated launcher are documented below.
- Retain exact fixture ordering and exact API/lifecycle bounds. Changes to tests, fixture helpers, dependencies, relevant production paths, or the launcher require review before execution. Compilation corrections do not authorize new branches automatically.
- No populated paging data may be introduced into the root Summary, NetworkLogs, or AppList fixtures. AppInfo is the separately reviewed exception with strict synthetic DAO paging.
- Native graphics refers to Robolectric's existing Android graphics runtime. It does not permit `com.celzero.firestack.backend.Backend`, Go JNI, tunnel, resolver, native IP/radix trie, or application traffic code.
- Test dependencies may use process-local instrumentation and Android shadows. Nothing here permits changing OS/network/security settings, installing trust roots, attaching to another process, disabling sandboxing, or changing a user's device.

## Allowlist (exact current test source)

| Fully qualified method pattern | Methods | Test-source SHA-256 |
|---|---:|---|
| `com.celzero.bravedns.ui.activity.WelcomeActivityTest.*` | 20 | `eabf2b329388619e3263176b3e6396a65bd722ec0f074be6d93bb0c31681c3fa` |
| `com.celzero.bravedns.ui.CoreNavigationTest.*` | 2 | `66393337ff858232d1c4bea2e5ce115374378c96299e5eb0ccb7fc003f3e8d5d` |
| `com.celzero.bravedns.ui.HomeScreenActivityDialogInstrumentedTest.*` | 7 | `d8d3a68aee76cdc09fcaaeee58052d0b8db9e3dd78b23c6f183f28e0af72d90e` |
| `com.celzero.bravedns.ui.HomeScreenActivityDialogTest.*` | 8 | `5c5234701bd15205a970df15d9d32056076b0c439b4489446a488c67964355bd` |
| `com.celzero.bravedns.ui.activity.ConfigChangeTest.*` | 2 | `b7a4c8d6cbf62a24b3aeee21f646caa6075adbc9105ecb90d38a82c30ad44cc1` |
| `com.celzero.bravedns.ui.activity.ThemeChangeTest.*` | 1 | `79c5215fd64ab2bb1ad56c07ee07c15f1adfa7998d4ade128b5c378bd983372d` |
| `com.celzero.bravedns.ui.fragment.SummaryStatisticsFragmentTest.*` | 2 | `a31eed9550f160b76b2ac59f65e5b6e05d3d9d6630b5efa086d9b4373f749521` |
| `com.celzero.bravedns.ui.activity.NetworkLogsActivityTest.*` | 4 | `8afd8ce518bdbf9943a6b1552222c1fb6d5a8ddb6f6e0b1e54419ec33afd1801` |
| `com.celzero.bravedns.ui.activity.DnsDetailAccessibilityTest.*` | 1 | `0d2f49e4da71197460d79afd7f99187385f4c4ac70d1ba20647365de469056dd` |
| `com.celzero.bravedns.ui.activity.DnsDetailNavigationTest.*` | 3 | `8aea78ecba2b4b4d51405a797b9cb9f5012c94c8fce9fb6320ae6a4842b903b0` |
| `com.celzero.bravedns.ui.activity.DnsListActivityTest.*` | 3 | `414274c92f7280362ee9d2e7cb349aca2935d636f159f388fba66c2c07f6e423` |
| `com.celzero.bravedns.ui.fragment.DnsSettingsFragmentTest.*` | 3 | `c7fe2e4534fa003791da8724113edae561c478a516db983b01f7bc69e3591b5b` |
| `com.celzero.bravedns.ui.activity.FirewallActivityTest.*` | 2 | `4f35738aca624a0568a2d8ea5ba5ed23dea650705dda10bbbc50f86262fd88ae` |
| `com.celzero.bravedns.ui.fragment.FirewallSettingsFragmentTest.*` | 2 | `8ac52a5b9c2ce6d23440e7dd8f0768000142483dfb5a54ff9fdd19659b3e6870` |
| `com.celzero.bravedns.ui.activity.AppListActivityTest.*` | 3 | `551d0fe9bc2c8391a8a98c3225a7f00c02a2de759a923e8c9b79fc023af84b4e` |
| `com.celzero.bravedns.ui.activity.ProxySettingsActivityTest.*` | 1 | `ecc584559015acc671c94e5e72b446acde1c4bc68656156bcc5d977c1da905dc` |
| `com.celzero.bravedns.ui.activity.AppInfoActivityTest.*` | 29 | `5eda39bd46d5e23fb4b98e1a808571d449823a7d63a0a85003eafca26577e6df` |
| `com.celzero.bravedns.ui.activity.MiscSettingsActivityRobolectricTest.*` | 64 | `02107b7a4bba1d8282df312bd55c01498e5df7b404cc351bca5b73ad7c4b7efa` |

Test files correspond to their package/class names under `app/src/test/java/`. No similarly named `androidTest` class is authorized.

## Inspected boundary rationale

### Welcome

`WelcomeActivityTest` installs a strict constructor-free `PersistentState`, then intercepts `Logger.d/v`, then creates the actual Activity. `WelcomeActivity`/`BaseActivity` read only the fake theme; `WelcomeUiState`, `WelcomeScreen`, resource drawables, and `RethinkTheme` are presentation state. Frost operations affect the shadow window and their logger sinks are intercepted. Completion records the fake `firstTimeLaunch=false`, records exactly one explicit Home Intent in Robolectric, and finishes; it never constructs Home. Configuration/theme tests restore only Bundles. No backend, repository, permission, account, download, or service branch is entered.

### Root navigation, configuration, theme, Home dialogs, Summary

`SafeRootHostTest` checks the plain Application, installs strict settings/config/statistics/updater/message/event fakes, then intercepts Logger and VpnController, then assigns the Activity FragmentFactory **before `create()`**. The original graph and NavHost run; Home/About/Configure are substituted. Only SummaryStatisticsFragment is a real application destination. Unknown fragments fail closed. The synthetic Home switch changes a local integer only.

The root startup gates are effective in the actual source: `firstTimeLaunch=false`; `appVersion=BuildConfig.BASE_VERSION_CODE` prevents upgrade/migration/storage/firewall initialization; update checks and Firebase reporting are false; the default Intent has no restore scheme/restart extras. `NewSettingsManager` does have an eager initializer, but its only reached dependency is the already-installed fake `appUpdateTimeTs`, answered with the current time; the expiration-write branch is not entered. `InAppMessageProvider.showMessages`, updater completion/unregister, and postpone writes are recorded sinks.

`VpnController` initializes scalar/null service fields, an elapsed-time scalar, a lazy preference reference, and LiveData only. Its empty status stream cannot launch Pause; start/stop/pause/resume/close entry points are rejected. No service is constructed. Summary uses a strict fake ViewModel, nonemitting/empty paging and zero synthetic total usage. Adapter construction creates paging/state objects only. Empty data prevents app-identity/icon resolution and domain favicons. View-lifecycle total-usage jobs call the already-captured fake VM; destruction cancels them. Summary filter static initialization creates only LiveData/enum/state values.

Home update dialog actions terminate at fake `completeUpdate`, fake timestamp writes, or a shadow-recorded browser ACTION_VIEW Intent. No browser/download is launched. This does not exercise production HomeScreenFragment's firewall/account integration.

**Exclusion:** the Rethink+ root click calls `RpnProxyManager.hasValidSubscription()` before navigation. The FragmentFactory is not a fence for this branch. No reviewed test clicks it. Do not add it without fencing/reviewing account/static initialization first.

### DNS detail/settings and protocol chooser

`SafeDnsHostTest` installs strict preferences/AppConfig/EventLogger before creating actual DnsDetailActivity/DnsSettingsFragment. False, stable switches plus `proxyDns=true` match the default unchecked proxy control, avoiding onResume listener writes. `splitDns=false` avoids WireguardManager. No connected-DNS value is emitted. Therefore `updateConnectedStatus` and its `updateLatency` are not invoked. **Emitting even a synthetic connected-DNS string would resolve native Backend fields before `VpnController.p50`; mocking p50 alone is insufficient.**

The only tested actions are display/semantics, actual recreation, custom-DNS Intent capture, restore-dialog cancellation, and selected-system-DNS information from intercepted `getSystemDns()` returning `192.0.2.53`. The positive system-info button only dismisses; the clipboard secondary action is not clicked. No restore is accepted, setting toggle/refresh invoked, WorkManager/scheduler used, DNS endpoint changed, or local blocklist sheet opened. Stable LIGHT theme avoids Frost logging in these fixtures.

`DnsListActivityTest` deliberately stops at CREATED/STARTED/visible, never RESUMED. Its strict AppConfig is verified unused. `onResume` would read native Backend constants and probe resolver status, so it remains excluded. Protocol card clicks use the inspected pure `ConfigureOtherDnsActivity.getIntent`, and Robolectric captures Intent extras without launching destination fragments.

### Firewall menu, AppList, NetworkLogs

SafeFirewallHostTest installs strict theme settings and intercepted VpnController status/start/stop before the actual FirewallActivity/FirewallSettingsFragment. The page is a menu with no manager initializer; tested navigation is only a captured UniversalFirewallSettingsActivity Intent.

AppList uses a strict mocked ViewModel and strict unused RefreshDatabase/EventLogger; actual adapter construction only obtains the shadow PackageManager and allocates paging/collections. Nonemitting/empty data means no row binding, icon lookup, FirewallManager, or ProxyManager. Search/recreation affect local state and fake filter callbacks; refresh, bulk rules, and filter sheets are not invoked.

NetworkLogs uses strict fake connection/DNS VMs, fake AppConfig/settings, and strict lazy repository definitions. Logger.v and VpnController status/start/stop are intercepted before launch. The only mounted pages are actual ConnectionTrackerFragment and DnsLogFragment. No wireguard/RPN query prefixes, rethink-log page, record rows, or deletion actions occur. Both adapter initializers are memory-only; row-dependent service/native/icon/favicon work is unreachable. Empty emission and search/recreation/debounce operate only on fake streams/filter callbacks. **DNS delete confirmation is excluded:** its production callback clears Glide disk cache before repository deletion, so a fake repository alone would not make it safe.

### Proxy settings display

ProxySettingsActivityTest installs strict LIGHT preferences and AppConfig whose HTTP/SOCKS enabled reads are false. Actual `onCreate` initializes presentation/animation and returns from both endpoint display routines before any IO launch; listeners are merely registered. The Activity is only CREATED/STARTED/visible, never RESUMED. Only the two headings are inspected. `onResume` accesses Orbot/Firewall/Wireguard/RPN account state and is excluded, along with every toggle/refresh/endpoint/editor action.

### AppInfo actual host with populated synthetic paging

AppInfoActivityTest checks plain Application and an empty Koin graph, sets only Robolectric's simulated process UID to 10001, and creates strict constructor-free settings/config/event/repository/DAO doubles. It installs these before the first FirewallManager reference. Its real AppInfoViewModel/CustomIpViewModel/CustomDomainViewModel/AppConnectionsViewModel receive strict DAOs directly; their initializers create state/Paging configuration, not real storage. Only count LiveData and synthetic in-memory PagingSources return data. AppInfoDAO and RethinkLogDao are verified unused.

FirewallManager is not innocuous: object initialization starts `load()` and `hydrateTempAllowCacheFromDb()`. This fixture first installs strict AppInfoRepository reads returning empty lists and Logger sinks. It then explicitly initializes the object and joins its scope's startup children before object interception. Empty load returns before app map refresh, empty temporary allows never enter the expiration-write path, and `appContext=null` makes scheduler cancellation a no-op. The cache's executor is allocated but has no removal/write task because no entries exist. No real DAO/repository constructor is involved.

Only then are controller-facing Firewall/Proxy/Vpn/DomainRules/IpRules/Utilities/UIUtils/favicon boundaries intercepted. ProxyManager initializes lazy DI, an empty set and a mutex. DomainRulesManager/IpRulesManager native tries are lazy; exact `status`/`getMostSpecificRuleMatch` calls are intercepted before populated row rendering. Empty proxy IDs prevent `fetchProxyNames` from evaluating Backend/RPN fields. The icon is a local ColorDrawable. Favicon failed-cache hits prevent all Glide request/cache creation, and empty target collections mean disposal does not access Glide. OS App Info is a no-op recorded boundary, rule/proxy operations terminate at mocks, and service start/stop throw. Synthetic package/domain/TEST-NET addresses never enter network clients.

No row-rule sheet, notes-save, self-warning Proceed, close-connection confirmation, or real app lookup is tested. Own-UID warning is canceled with Back. All Activity lifecycle jobs are canceled and joined before mocks are removed; startup jobs have already completed. Simulated UID is reset. These safety guarantees do not certify the old device performance thresholds.

### Misc settings actual host

MiscSettingsActivityRobolectricTest requires a plain Application and empty Koin graph, installs strict constructor-free settings/config/refresh/events/scheduler, then intercepts Logger/Firebase/LauncherSwitcher and BiometricManager.from before create. Inspected Logger, FirebaseErrorReporting variants and LauncherSwitcher have no eager external-I/O initializer. Biometric capability is a strict fake; no authentication prompt is invoked. Biometric choice writes only synthetic properties and records the intercepted launcher-alias call.

Most tests use API28, eliminating notification/bubble branches. The two API33 cases toggle only the persistent-notification preference; bubbles remain false and the generic notification control is not clicked. A shadow permission read may occur but no request/grant/security mutation occurs. All token strings are synthetic; Firebase.setUserId is intercepted. Tombstone refresh and event recording terminate at mocks; configuration and scheduler are verified unused.

PCAP, theme, notification-action, Go-log and tasker dialogs are shown/dismissed without selecting side-effectful options. External-file PCAP is a fake display value only. No PCAP file, Go log setting, file trace, locale change, notification permission, bubble, system settings activity, real import/export/backup sheet or account is opened. The import/export tests exercise only finishing/destroyed guards. Teardown destroys the Activity and joins its lifecycle job before removing interception.

## Test infrastructure scope

The inspected opt-in `../validation/isolated-tests.init.gradle` supplies a synthetic user.home, references the existing trusted CA store without changing it, and passes the environment's proxy only to forked Test JVMs. Its Mockito `-javaagent` is attached at JVM startup to these test processes; it does not attach to the Gradle daemon, app, device or another process. It configures every Gradle Test task to which the init script is applied, so **the script itself is not an allowlist**. Explicit class filters remain mandatory.

The baseline already declares `org.mockito:mockito-core:5.23.0` and `mock-maker-inline`. The existing jar manifest names `org.mockito.internal.PremainAttach`; bytecode inspection shows premain only stores the provided Instrumentation reference. No global instrumentation/security setting is modified. Jar hash and launcher hash are pinned below.

### MockK startup-agent addendum (2026-10-14 UTC)

The existing Welcome run in `../validation/safe-welcome-host-tests.log` failed during `WelcomeActivityTest.setUp`, at MockK gateway initialization, with `ByteBuddyAgent.installExternal` reporting an unsupported self-attach. This is infrastructure failure, not a passing host test. The Welcome source still matches its allowlisted SHA-256 above. Mockito's startup agent stores instrumentation in its own holder; it does not populate Byte Buddy's `Installer` holder used by MockK.

**Reviewed permitted launcher delta:** keep the existing Mockito startup agent and append exactly this second JVM argument only within the existing opt-in `tasks.withType(Test).configureEach` block:

```text
-javaagent:/workspace/scratch/a9ec32c7322a/rethink-compose/toolchain/gradle-user-home/caches/modules-2/files-2.1/net.bytebuddy/byte-buddy-agent/1.18.2/62f38a6faf7f069d661b79a07d566f504b0b20c4/byte-buddy-agent-1.18.2.jar
```

The launcher now contains exactly the reviewed additional Byte Buddy argument and its explanatory comment; its resulting SHA-256 is `71495eee566283a2aadf2d00d5736ad5f26fcbfd681776c39a1d4c40af3007db`, pinned below. No other functional launcher change was found. This reviewer did not execute the launcher.

The already-cached artifact is `net.bytebuddy:byte-buddy-agent:1.18.2`, SHA-256 `d91e2d6d1b777687ab14a52b18b08aca32c4c91274131c99907674c661402a76`. The cached MockK 1.14.11 POM explicitly declares this Byte Buddy version. No artifact was downloaded, upgraded, installed or executed for this addendum. Inspection used the actual JAR manifest and `javap -c -p --multi-release 17` against the existing Java 17 toolchain, including the JAR's versioned classes.

- Manifest `Premain-Class` is `net.bytebuddy.agent.Installer`. Its `premain` invokes `doMain`, which stores the JVM-supplied `Instrumentation` reference in a static volatile field. In this unrelocated JAR the alternate-name forwarding branch is not entered. This entry point creates no process, opens no connection, installs no transformer and invokes no attach operation.
- Manifest redefine/retransform/native-method-prefix capabilities apply to the new test JVM. They do not load Rethink's native backend or modify OS permissions. The agent is process-local infrastructure, not a security fence or permission to broaden tested code paths.
- MockK's `JvmMockKAgentFactory.initInstrumentation` invokes `ByteBuddyAgent.install()`. Its synchronized implementation first resolves `Installer.getInstrumentation()` through the system class loader and returns a non-null value before resolving a PID, attempting an attachment provider or entering `installExternal`. The system-loader lookup also supports the test's separate Robolectric class loader. A Java module read edge may be added locally if necessary; there is no OS security-setting change.
- Existing Byte Buddy provider initialization creates provider objects and reads the self-attach property; it does not enable that property. No `jdk.attach.allowAttachSelf`, dynamic-agent enabling flag, global `JAVA_TOOL_OPTIONS` change, Gradle daemon agent or attachment to a running process is covered or needed.
- After obtaining instrumentation, existing MockK code builds its dispatcher bootstrap JAR from packaged class resources, writes a temporary file (or a relative working-directory fallback if temporary-file creation fails), registers it for deletion on JVM exit, and adds it to this JVM's bootstrap search. This ordinary test-library temporary output is within the isolated test workspace/temporary storage boundary; it is not application configuration, user data or a downloaded program. Mocking transformers remain confined to the Test JVM.

**Conclusion:** the exact second startup agent is consistent with the existing allowlist and synthetic plain-Application host boundary. All prior test filters, fixture ordering, lifecycle limits and prohibited production branches remain mandatory. This review establishes the startup path from inspected bytecode; runtime success still must be measured. If instrumentation remains unavailable, stop and diagnose within this boundary instead of enabling dynamic attach or changing host security/network settings.

Robolectric 4.17 AndroidTestEnvironment bytecode was inspected: the selected Application class is constructed and its onCreate is called; there is no automatic installation of application manifest ContentProviders in this path. It sets up its FakeMediaProvider and process-local Android/native/shadow runtime. This is library test infrastructure, not production Rethink application startup. No new dependency/source was downloaded for this review.

## Fixture-correction re-review (2026-10-14 UTC)

Compared the exact current allowlisted test and fixture files with checkpoint `efa4b2ff720c41148230e0af4f28cf08c35fc807` using file-content comparisons, including files that are untracked relative to the local index. The original 18 classes and all 157 method names/order are unchanged. The allowlist and fixture pins above/below now identify these reviewed current files. All previously pinned production files, build files and dependency artifacts remain unchanged. No Gradle task, test or application was run by this reviewer.

**Decision:** these corrections may run under the same isolated synthetic plain-Application scope, exact class filters and exclusions. They do not clear additional business branches or real resumed DNS/Proxy hosts.

- AppInfo (`5eda39bd46d5e23fb4b98e1a808571d449823a7d63a0a85003eafca26577e6df`) replaces asynchronous assertion polling with bounded `compose.runOnIdle` polling. OS App Info remains intercepted and now records the synthetic package in a synchronized list; no system Settings Intent is dispatched. Teardown destroys all hosts, drains the paused main looper until every captured Activity lifecycle job is completed, then joins them before removing any fake boundary. A timeout fails before interception is removed. Startup FirewallManager joining, synthetic populated paging and forbidden confirmation branches are unchanged.
- Misc (`02107b7a4bba1d8282df312bd55c01498e5df7b404cc351bca5b73ad7c4b7efa`) explicitly matches the second, default suspend callback parameter of `RefreshDatabase.refresh`. The strict constructor-free fake returns without invoking that callback or entering the real actor. The refresh action and positive/zero invocation counts remain exact. No test interaction or production code changed.
- Root/Summary, AppList and NetworkLogs mocks receive only an exact no-op for framework `ViewModel.clear$lifecycle_viewmodel`. These constructor-free mock VMs own no real resources; application methods remain strict. This permits framework disposal without claiming to test a real ViewModel's resource cleanup. Root navigation waits drain the already-posted Android/FragmentManager work and require the selected reviewed destination to be RESUMED before asserting or saving it. FragmentFactory fencing, empty Summary data and excluded Rethink+ clicks are unchanged. The synthetic Home assertion now matches its renderer's existing uppercase text.
- Update-dialog outside touches now send an outside DOWN/UP pair. Cached Compose 1.12 `DialogWrapper.onTouchEvent` bytecode confirms that this pair reaches `onDismissRequest` only for dismissible dialogs. `HomeChromeDialog.dismiss` executes an empty action and the existing presentation dismissal, so it does not call the positive/negative update callbacks. Modal and action-cardinality assertions remain intact.
- DNS protocol and Proxy display tests stop the actual Activity at STARTED. `StartedHostComposeOwner` installs a separate `LifecycleRegistry` only as the unattached decor's view-tree owner and advances that registry to RESUMED; it does not call Activity lifecycle callbacks or advance the Activity's own lifecycle/lifecycleScope. Checks enforce STARTED before/after replacement and after rendering. Cached Compose test-registry bytecode registers roots on `ON_RESUME`, explaining this presentation-only adapter. Inspected `DnsProtocolGrid`, `NetworkSettingsScreen`/`NetworkContent` and `RethinkTheme` do not start business work from that lifecycle. The synthetic owner is destroyed before host teardown/recreation. DNS AppConfig remains verified unused; Proxy's false endpoint gates and no-click display-only scope remain unchanged. Actual DNS/Proxy `onResume`, resolver/native/account branches remain excluded.
- NetworkLogs emits an explicitly completed, empty in-memory paging generation. The reached `updateLoadState` branch only changes message/list/search state and removes an observer from the fake LiveData. No row is introduced, no PagingSource/DAO is invoked and no identity/icon/favicon/native work is unlocked. The separately changed `LogScreen` search focus handler was also inspected: editor focus no longer invokes root expansion automatically, and the new explicit search affordance only calls the existing presentation callback, requests Compose focus and shows the shadow IME. The reviewed host tests do not click that new affordance; their query callbacks still terminate at strict fake filters. Its current source is additionally pinned below.

Prior execution evidence remains separate: `../validation/safe-welcome-host-tests-2.log` reports 20 Welcome passes with the reviewed startup agents; `../validation/reviewed-host-tests-1.log` reports 137 tests completed with 51 failures before these fixture corrections. These logs do not establish that the corrected suite passes. A fresh filtered rerun is required.

## Explicit blocks and required corrections before expanding coverage

- Unfiltered unit test tasks, `androidTest`/connected/device tests, original application-startup tests, and all unlisted actual hosts: blocked pending individual boundary review.
- Wireguard actual host: blocked. WireguardManager's eager unstructured IO can load repositories and migrate encrypted config files. A fake repository plus migration-complete flag alone does not establish a join/cleanup fence for its unretained startup job. Provide an observable startup/teardown fence and intercept native/file/account paths first.
- Root Rethink+ and actual HomeScreenFragment: block account/manager initialization before any click/controller construction, not after FragmentFactory dispatch.
- DNS resumed protocol status or connected-DNS emissions: fence native Backend class/constant resolution before the callback, as well as any resolver calls.
- Populated NetworkLogs/AppList/root Summary: fence every row identity, native status, icon/favicon/cache and manager initializer first.
- Misc PCAP/export, permissions/bubbles, Go logging, locale or real launcher/security behavior: add explicit synthetic sinks and re-review before selecting those actions.
- AppInfo additional notes/rule-sheet/self-app/connection-confirmation paths: review new DAO/manager/native boundaries and teardown before adding tests.

## Source pins

Any drift in a relevant pin below requires reviewing that change before relying on this allowlist. These pins describe source at review time; they are not a claim that the suite passed.

```text
6d47f453e2795fd3075152b775ed2d67c4a47380ce1f693c6e0c184fa95b0301  app/src/test/java/com/celzero/bravedns/ui/fixture/SafeRootHostFixture.kt
fcb4931346d3fdb720f64d69ef4f155719165d0b74f84c9479ccc5e5d46d598c  app/src/test/java/com/celzero/bravedns/ui/fixture/SafeDnsHostFixture.kt
79a0ecaba0773a7a8f25e97e7227170ead688a379adc0be3fe17bf42bc3b00a6  app/src/test/java/com/celzero/bravedns/ui/fixture/SafeFirewallHostFixture.kt
c4b235b955ce6db432c203528634061c02c7d013f5182dd9ac3b3e9d0a4255b6  app/src/main/java/com/celzero/bravedns/ui/BaseActivity.kt
2c030a321b7c4d73e9705adfad7e25d6eddd7c0c7f2d5af860f6be868d22e216  app/src/main/java/com/celzero/bravedns/ui/HomeScreenActivity.kt
b25f86f4d82c3845a74893860c4d98d79a8cbbc1513a70b55479e23f805675d2  app/src/main/java/com/celzero/bravedns/ui/activity/WelcomeActivity.kt
3e1d4bf377c91bd9ba37841d3f6d1e04ab4dcbdf3618e98cce12fb05fcf46e7e  app/src/main/java/com/celzero/bravedns/ui/activity/DnsDetailActivity.kt
25e9e811a349a80d325d5a984638b72198ff2c1c04473d7b2096ca466369c2c2  app/src/main/java/com/celzero/bravedns/ui/activity/DnsListActivity.kt
d45ef1328e24914f520f956406d075fea87506529a9430dc1a0461e33dd1d653  app/src/main/java/com/celzero/bravedns/ui/activity/ConfigureOtherDnsActivity.kt
afea56f33f8530adde8c0347543b90f2a868fa115d98f68b0bca55a884f987e5  app/src/main/java/com/celzero/bravedns/ui/activity/FirewallActivity.kt
b4a3fbde5c4366deb894555835f74d3f769b519a969f92103637c73b23b960aa  app/src/main/java/com/celzero/bravedns/ui/activity/NetworkLogsActivity.kt
af67579d59ce78079e3af5ff0fb935829205523ff29fd78e81395d8b604c3ac4  app/src/main/java/com/celzero/bravedns/ui/activity/AppListActivity.kt
9670439254278976c23e85d0c08f2a65ca0a104f48dae314fb1face398bb662a  app/src/main/java/com/celzero/bravedns/ui/activity/ProxySettingsActivity.kt
105d240012e7d208acc6971dc6dbf7a3fd2eada1eb7d5e2ba3b5c108d7b3aac2  app/src/main/java/com/celzero/bravedns/ui/activity/AppInfoActivity.kt
cad2a725eabace5878f1f2d5791210d723749c5c8101b4c04ed927aa43bdb1b6  app/src/main/java/com/celzero/bravedns/ui/activity/MiscSettingsActivity.kt
46ddf37274bf0dd7ff49f5571f803796e0609b666ddc7d46325dd5e0b67abff8  app/src/main/java/com/celzero/bravedns/ui/fragment/SummaryStatisticsFragment.kt
58307303cbc4be8fe1c95ba3b353a21e0d1752c690e610072480f2c4c4ffa19b  app/src/main/java/com/celzero/bravedns/ui/fragment/DnsSettingsFragment.kt
437073980a8d03d45ec30120c44330f3abc0760f10baeffc10050285e8793411  app/src/main/java/com/celzero/bravedns/ui/fragment/FirewallSettingsFragment.kt
955e0606c99f698628377fee83daf2a04e59617d1b4d9bee200d59298924d9a2  app/src/main/java/com/celzero/bravedns/ui/fragment/ConnectionTrackerFragment.kt
9f2fb46efa6cae3141037a698c97d873df7834a0dec1f7b4b47e6ec551e7c23a  app/src/main/java/com/celzero/bravedns/ui/fragment/DnsLogFragment.kt
0ad159d3bfeb3ac0eb1455af0cd264f96e61f3623166c4627fc00917e0822fd4  app/src/main/java/com/celzero/bravedns/service/FirewallManager.kt
b173cc711135c2004bd6331490ca3255d9675e8f02117b61b88391e55279cd0c  app/src/main/java/com/celzero/bravedns/service/VpnController.kt
dee178253cfc48d4a54a9fb9ed597e36df1fa6a43f6c1dcbad3e492bfe558f4b  app/src/main/java/com/celzero/bravedns/service/ProxyManager.kt
4a28e1d9318ac0da5a357651a5bc1172d46c4f7e0285573e03d3d2250cd93212  app/src/main/java/com/celzero/bravedns/service/DomainRulesManager.kt
5cd2de80535c1cfd42b124feee6f00e8a8439534120c0e4ca1c9501b8ba88447  app/src/main/java/com/celzero/bravedns/service/IpRulesManager.kt
7c452be8234120e10b779bcf29eef1be658efbb441f5c45455d1d2fe2b721315  app/src/main/java/com/celzero/bravedns/service/PersistentState.kt
1680259dcb2b0225cf19b13cf803c3605afc074b02837638b27b35fb0c454c7c  app/src/main/java/com/celzero/bravedns/service/EventLogger.kt
29c270ea0419e2deabc057363e845197ee1bce9cd5f0bb14e983140d6e59a9ad  app/src/main/java/com/celzero/bravedns/data/AppConfig.kt
2765940845890339c9d0a9a90f15a531553bc464af044c8915147c2755ff6e93  app/src/main/java/com/celzero/bravedns/glide/FavIconDownloader.kt
8f6b67cd09aa9cb2d6b683395d6346f6b523e4fd0601704f8ca3789bbc253512  app/src/main/java/com/celzero/bravedns/adapter/SummaryStatisticsAdapter.kt
399ed4846851d5862e6202d44bf25c5084bd042ee0d61de78eaa0caf7c6be493  app/src/main/java/com/celzero/bravedns/adapter/AppWiseDomainsAdapter.kt
2cbfb883674deb9f0df443b9c19cecf0e80c5991d36c6b3d907e9389722687e0  app/src/main/java/com/celzero/bravedns/adapter/AppWiseIpsAdapter.kt
879b9feb0bdee3ec82162d66ff5704582ffb4dee947ba652c44710429b311aca  app/src/main/java/com/celzero/bravedns/adapter/AppWiseCountriesAdapter.kt
4e0212ad43d358b5efdaf6d3e86317b1fe8c4bc48ad52b395d52808e0040510f  app/src/main/java/com/celzero/bravedns/adapter/ConnectionLogAdapter.kt
4ff60fb0494b947093b49124a99ef32263c863e7a92414cb654f08f4204a2ad3  app/src/main/java/com/celzero/bravedns/adapter/DnsLogAdapter.kt
d6d01e4d04e225a67d1eeed507c4fc3b9b80434c31e2c305737ce653c0f8963b  app/src/main/java/com/celzero/bravedns/adapter/FirewallAppListAdapter.kt
5e32f5e1fb5370f9ef33c4c0d1a47851a3ba99c125a8700ddafef362227a63d4  app/src/main/java/com/celzero/bravedns/viewmodel/AppInfoViewModel.kt
d5b3d3b7f2f6ce17a891485aeae02b33f8570b188354e2a844c436ed1d4eca1a  app/src/main/java/com/celzero/bravedns/viewmodel/CustomIpViewModel.kt
d578e2c8d683bf6e61b40b294add2160b05ab191b84b198c14703cb49ff49fcf  app/src/main/java/com/celzero/bravedns/viewmodel/CustomDomainViewModel.kt
4193aa1a89e0b08c4a631f61d049f1621e89b542af41cbbaea8907fbd268f984  app/src/main/java/com/celzero/bravedns/viewmodel/AppConnectionsViewModel.kt
c365890e50991e5ce66aa5fd9ee93ea54c53ef537f0faeb6509ae0f36c75ebdc  app/src/main/java/com/celzero/bravedns/util/Logger.kt
f78ad40b2685fbbe9d4fc62315633091a9a76dfc63039f62d43a97377c233d34  app/src/main/java/com/celzero/bravedns/util/NewSettingsManager.kt
cff4ec3a9b63bdde7bacf73619d8fb6b85abf91fbcc2dec32dc279799d523752  app/src/main/java/com/celzero/bravedns/util/Utilities.kt
45327085b495872e28114d9fbeb1266bb4f51e21eee9ba6e071781b696c26bfa  app/src/main/java/com/celzero/bravedns/util/UIUtils.kt
bad5f8ae3610ccd171641b7f5a62bf41eaa932515201e839ae4ec34b46a6891e  app/src/main/java/com/celzero/bravedns/util/WindowExtensions.kt
34dcf8b7c761aefdb5c8e1015f8cfaa8cc401bbf25b14c53b892cf886f797767  app/src/main/java/com/celzero/bravedns/ui/LauncherSwitcher.kt
6ae1084a7fd7bdeb5f088095fdab7f6fbeb00da60f4691a4045a9247b1dc3d72  app/src/test/resources/robolectric.properties
fafc5d3383454d6db81ae934e97b8f980bdb27623acda63a3300ddd7bd279ddc  app/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker
60488169810260378871e4543f86b989957f267160aecccbdb140c7cde5663ed  app/src/full/res/layout/activity_home_screen.xml
16f67b0273939e69f262764bd4340149d2f8ca3cf273e72c6831ec82eba3ee3b  app/build.gradle.kts
75761edad45b67af0cee8e2d55130f8bbbdbfc22db35659b7e1a74a8d9fa56c6  gradle/lib.toml
71495eee566283a2aadf2d00d5736ad5f26fcbfd681776c39a1d4c40af3007db  ../validation/isolated-tests.init.gradle
ae295bebd5d11fab97ab297815dc7617188b86003cbce3dfd5c0d5c3a6cc4a0c  ../toolchain/gradle-user-home/caches/modules-2/files-2.1/org.mockito/mockito-core/5.23.0/a5cf3cf244300e4cd05f779827cf50d373a8460e/mockito-core-5.23.0.jar
d91e2d6d1b777687ab14a52b18b08aca32c4c91274131c99907674c661402a76  ../toolchain/gradle-user-home/caches/modules-2/files-2.1/net.bytebuddy/byte-buddy-agent/1.18.2/62f38a6faf7f069d661b79a07d566f504b0b20c4/byte-buddy-agent-1.18.2.jar
bb3af4f281c8d5d50141cad9d1a1f8dde9d2bd695b68054777830957708ea0b4  ../toolchain/gradle-user-home/caches/modules-2/files-2.1/io.mockk/mockk-agent-jvm/1.14.11/e1e1acc201dc6ec3bfb9d2d8f10e5960eda3c9f0/mockk-agent-jvm-1.14.11.jar
69e5bc128ba8d1fbb09d36bfb8a617018d6a3a418f77d44b36e7aca6747f14ea  ../toolchain/gradle-user-home/caches/modules-2/files-2.1/org.robolectric/robolectric/4.17/31a19773cc1daf1be0f77769497371ddf18a518f/robolectric-4.17.jar
46c00c91138dbef5c570e15c8eba2c0f37a143d53904a065eef90e03a0f50630  app/src/main/java/com/celzero/bravedns/ui/compose/trafficlogs/LogScreen.kt
894a8133c1e039d9bbb125cebef9f42fb1aa572b8e8058101fca2c4c85313c06  app/src/main/java/com/celzero/bravedns/ui/compose/dnssettings/DnsProtocolGrid.kt
c4680396ea8405f2be5dbc585faa3b338d7ee3921105a2ae065ef7ed02fa87d5  app/src/main/java/com/celzero/bravedns/ui/compose/networksettings/NetworkNativeUi.kt
99a7faff9041b0fbf23ac548ad667da549ab2861095796606ff25593fe72b041  app/src/main/java/com/celzero/bravedns/ui/compose/RethinkTheme.kt
4de691784e4111dc11ba6c766ccba4c3ede26c76508d173aca4be732456e56eb  app/src/main/java/com/celzero/bravedns/ui/compose/chrome/HomeChrome.kt
```

Coordinator applied the reviewed second startup agent on 2026-10-14 at 06:58 UTC. Launcher SHA-256: `71495eee566283a2aadf2d00d5736ad5f26fcbfd681776c39a1d4c40af3007db`. No dynamic-attach flags or host settings were changed; runtime result is still pending.

## Coordinator additive review: AppInfo replacement Intent fixture

Reviewed the final single-method correction at 2026-10-14 07:34 UTC against the previously cleared class. Only testActivityStateWithMultipleIntents assertions change. It still sets a synthetic replacement Intent, inspects the already-mounted synthetic app, recreates the same reviewed controller, and reads synthetic metadata. New controller.intent and Activity.intent getters are pure framework state; no new production action or lifecycle branch is introduced. Invalid-UID and rule-command zero-call checks remain. The baseline original case never required an invalid dialog after recreation, and the cached Robolectric controller retains its original launch Intent. AppInfo source pin updated to `5eda39bd46d5e23fb4b98e1a808571d449823a7d63a0a85003eafca26577e6df`; all other clearance conditions remain unchanged. Execution of this correction is pending.
