# AppInfo and MiscSettings actual-host test migration

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9` (`gradle-improvements`).

Status: authored and source-audited; **not compiled or executed by this worker**. The coordinator must review the boundaries below before a serial, class-filtered Robolectric run. No Gradle, emulator/device, application, VPN, probe, network, permission, export, account or remote operation was executed to author this change. Do not count authored tests as passing coverage.

## Original test ownership and relocation

- `app/src/androidTest/java/com/celzero/bravedns/ui/activity/AppInfoActivityTest.kt` is intentionally relocated to `app/src/test/java/com/celzero/bravedns/ui/activity/AppInfoActivityTest.kt`, retaining the package, class, and all **24 original method names**. The coordinator authorized this relocation so the host can run with a plain Application instead of the production device Application. Five additional cases cover the self-UID warning, unknown-package exclusion, empty paging data, live count emissions and rule-navigation extras. Current authored count: **29**.
- `app/src/test/java/com/celzero/bravedns/ui/activity/MiscSettingsActivityRobolectricTest.kt` retains its location and all **62 original method names**. Two additional cases cover absent biometric hardware and a native biometric choice through a fake launcher-alias sink. Current authored count: **64**.
- The pure `AppInfoActivityUnitTest` and `MiscSettingsActivityUnitTest` files are untouched. They remain supplementary, not substitutes for these mounted hosts.
- The machine-readable [case manifest](app-settings-host-tests-manifest.json) lists every original source path, replacement path, method name and authored/unrun status. The count is **86 original cases**, mapped individually. No blanket `@Ignore`, nullable launch, conditional return, swallowed test exception, `assertTrue(true)`, relaxed mock, View-ID or RecyclerView selector remains in either host class.

## Safety boundary, installed before host lifecycle

### Common

Both suites explicitly declare `@Config(application = android.app.Application::class)` and assert the actual Application type. They own an initially empty Koin graph and register constructor-free strict mocks for every injected external dependency. They create the actual production Activity with `Robolectric.buildActivity(...).create().start().resume().visible()`, then query the real native semantics through `createEmptyComposeRule()`. They do not replace `onCreate`, `initView`, content, listeners, navigation callbacks or lifecycle methods.

Production `Logger` has lazy Koin properties but no eager I/O initializer. Its `v`/`vv`/`d`/`i`/`w`/`e` sinks used by each host are intercepted before lifecycle, preventing fallback console/database/file writes. Teardown dismisses test windows, destroys host Activities, and joins their cancelled lifecycle jobs before unmocking external sinks or closing Koin. Timeouts fail the test; they are not permissive catches.

Strict MockK instances fail on unstubbed calls. Kotlin object interception is narrower: unlisted object methods can call their originals. The specific allowed object methods and no-entry branches below are therefore part of the run review, not a claim that every method on every singleton is globally mocked. Unexpected production source changes require re-auditing before execution.

### AppInfo

1. Before the first `FirewallManager` reference, Koin supplies a strict `AppInfoRepository` double whose only startup stubs are `getAppInfo() = emptyList()` and `getTempAllowedApps() = emptyList()`. No repository constructor or Room database is called.
2. `FirewallManager` is the only touched manager with eager startup work. Its current initializer launches `load()` and `hydrateTempAllowCacheFromDb()`. Empty synthetic results prevent cache population, expiry writes, observer reloads and work scheduling. Its `appContext` stays null. The fixture joins both child jobs before installing object interception. This order is deliberate: `mockkObject(FirewallManager)` alone would initialize the singleton too early.
3. Then every controller-facing FirewallManager operation exercised here is intercepted: app lookup (UID/package), package/name lists, firewall/connection status, temporary-allow read, unknown-package read, proxy-exclusion write, temporary-allow write and firewall-status command. Writes only record synthetic arguments in memory. No DAO-backed rule change executes.
4. Real lifecycle-owned `AppInfoViewModel`, `CustomIpViewModel`, `CustomDomainViewModel` and `AppConnectionsViewModel` are constructed with strict fake DAOs: AppInfoDAO, CustomIpDao, CustomDomainDAO, ConnectionTrackerDAO, RethinkLogDao and StatsSummaryDao. Only count LiveData and the exact UID-scoped paging queries are stubbed. A tiny in-memory PagingSource emits reserved `192.0.2.x` IPs, `.example` names and synthetic labels; it performs no I/O. AppInfoDAO and RethinkLogDao are asserted unused. The original database and all repository implementations remain unconstructed.
5. `ProxyManager.getProxyIdForApp` returns an empty list. Consequently `fetchProxyNames` never enters its RPN/Backend branches; no RpnProxyManager initialization is needed.
6. `VpnController.uptimeMs`, `isVpnLockdown`, and connection-close callback are fakes. `start` and `stop` are explicit fail-fast traps and verified absent. The singleton itself only creates null state, a clock value and LiveData; it does not create a BraveVPNService.
7. Mounted domain/IP row rendering can otherwise initialize native tries. `DomainRulesManager.status` and `IpRulesManager.getMostSpecificRuleMatch` are intercepted before host creation; their Backend trees remain lazy and unevaluated.
8. `Utilities.getIcon` returns a synthetic ColorDrawable. `FavIconDownloader.isUrlAvailableInFailedCache` always returns a cache-failure hit, so no Glide request, application cache or download is constructed. `UIUtils.openAndroidAppInfo` is a recorded sink. No real package inventory or OS app-settings screen is consulted/opened.
9. Rule-navigation tests inspect Robolectric's recorded Intents, including exact class/tab/UID. They never construct the destination Activity. Custom rule sheets, note saves, RPN mappings and connection-close confirmation execution are outside this suite's current executed workload.

### MiscSettings

- All five injected dependencies are strict constructor-free doubles: PersistentState, AppConfig, RefreshDatabase, EventLogger and WorkScheduler. The baseline fixture omitted WorkScheduler; this fixture includes it. Settings setters update local variables and are verified with exact cardinality. RefreshDatabase receives only the synthetic `ACTION_REFRESH_FORCE` call; no refresh implementation executes. AppConfig and WorkScheduler are asserted entirely unused.
- BiometricManager's static `from()` returns a strict capability fake. The added enabled-biometric test writes only fake persistence and records `LauncherSwitcher.switchLauncherAlias`; no fingerprint prompt, key store or PackageManager alias write runs.
- Firebase `setUserId` is explicitly intercepted for both original token cases. No Firebase account or service is initialized. The fixture's token is nonblank/fresh, preventing token-generation/persistence branches during startup.
- Most original cases run at API 28, preserving the original hidden notification/bubble contract. The two original persistent-notification control cases run at API 33, where those controls actually exist; they never click a permission request. Bubbles remain false, so resume cannot reach a tunnel or BubbleHelper. Notification permission results are Robolectric values and any bookkeeping setter is fake.
- PCAP tests open/dismiss the options or render persisted descriptions. They never choose a new PCAP mode, construct a file, or call AppConfig.setPcap. Go-log tests only open the options: they never invoke GoVpnAdapter. The import/export tests invoke only the Activity's finishing/destroyed guards and assert no fragment/window appeared. No BackupRestoreBottomSheet is created.
- Reflection is narrowly retained where a destroyed/finishing Activity cannot accept UI input or where the original contract directly concerned a saved private flag. Ordinary row/switch/dialog interactions use mounted native semantics.

## Meaningful assertion mapping

All named methods are retained; the detailed names are in the JSON manifest. The important replacements are:

| Original contract | Actual-host replacement |
|---|---|
| AppInfo valid UID and metadata | Synthetic UID with exact name/package/UID text, icon state, enabled native rule controls, rule counts and lifecycle |
| AppInfo invalid/missing/negative/empty UID | Actual noncancelable native no-app dialog, dismissal/finish and no rule command |
| AppInfo root/system/maximum UID | Explicit synthetic records for 0/1000/Int.MAX_VALUE, exact UID propagation to fake count queries |
| AppInfo six setting controls | Native clicks on all six rules, exact UID/status/connection command tuple, one command per click |
| AppInfo proxy/info/close interactions | Native proxy toggle and exact fake write, recorded OS-info sink, close confirmation cancel with zero closes |
| AppInfo old RecyclerView sections | Real lifecycle paging from fake DAOs to all five native sections, populated presenter item counts, UID query assertions |
| AppInfo recreation/multiple-intent/lifecycle/trim/error | Actual ActivityController recreation and state transitions, invalid lookup dialog, fresh recovery host and observer cleanup |
| Misc lifecycle/Koin | Actual create→destroy state and identity of every fake binding |
| Misc saved flag/Back result/recreation | Real save/recreate/Back dispatcher and exact result `24` or canceled |
| Misc row and child-switch delegation | Native `network.<original_xml_id>` tags, actual toggle state, exact one setter/event/refresh callback |
| Misc dialogs/cancel/debounce | Native dialog window and title, cancel callback absence, disabled native row, exactly one window for rapid repeat |
| Misc finishing/destroyed import/export | Actual controller guard and zero fragment/window creation |
| Misc API/flavor visibility | Native semantics absent/present for the actual API/flavor, without silently skipping assertions |
| Misc persisted PCAP/notification/theme metadata | Exact or resource-derived text in real mounted native controls |
| Misc destruction with dialog | Actual destroyed lifecycle and no events; test window explicitly dismissed during cleanup |
| Misc Firebase methods | Exact fake sink argument/cardinality, including empty token |

The baseline destruction cases only asserted absence of an exception. These tests verify destroyed lifecycle and explicitly clean up their native dialog windows; they **do not claim automatic dialog dismissal on owner destruction**. That separate leak/window behavior remains an acceptance item.

## Device performance acceptance remains unrun

Robolectric is not a valid measurement of device performance. The original seven workload methods now execute deterministic UI/lifecycle assertions, while all original numeric requirements remain open below. This is not 7/7 performance equivalence or a performance pass.

| Original method | Original device metric retained, unrun |
|---|---|
| testActivityLaunchPerformance | Launch < 5,000 ms |
| testUiRenderingPerformance | Metadata render checks < 3,000 ms |
| testMultipleActivityInstancesPerformance | Three concurrent instances, average launch < 5,000 ms |
| testConfigurationChangePerformance | Three recreations < 10,000 ms |
| testMemoryUsageStability | Ten UI operations, heap increase < 50 MiB |
| testScrollingPerformance | Four list scroll interactions < 2,000 ms |
| testInteractionResponseTimes | Each proxy/app-info/close interaction < 1,000 ms |

A later device harness must use the same complete fake boundaries and native selectors before those measurements can be run. The old tests' tautological visible-or-not-visible assertions, live current-app UID, swallowed interaction failures, GC calls and sleeps are not reliable benchmark evidence and were not copied into this host harness.

## Verification remaining

1. Coordinator source safety review, then serial compilation and class-filtered execution of these two suites.
2. Fix any compiler/runtime failures without reintroducing permissive catches or weakening assertions. Source-only authorship is not an execution result.
3. Separately review the original pure unit tests and the untouched baseline test suite's safe run eligibility. Do not blanket-run application-backed tests.
4. Device benchmark metrics above, full seven-theme visual comparison/Frost, RTL/accessibility traversal, process death, notes/shared-UID confirmation, active connection confirmation, and OS permission/export interactions remain separate unrun obligations. No live action is authorized by this test migration.
