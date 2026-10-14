# Original integration tests: isolated host migration

Source handoff: 2026-10-14. Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

## Status and limits

**Authored, not executed.** This worker ran no Gradle, emulator, application, service, native backend, permission flow, probe, account operation, export, or external write. Compilation and exact-pattern execution require coordinator approval after independent boundary review. Passing a source/name check is not a runtime pass.

`original-test-relocation-map.json` records all 24 paths from `final-coverage-audit.md` and their current locations. Original class and test-method names are preserved in this worker's migrations. The two original Welcome classes shared a package/class name in separate source sets, so their 16 + 2 cases are merged in the existing unit-test class, with two extra recreation tests. Method-counts do not imply assertion equivalence; the contracts and remaining gaps are described below. AppInfo/Misc changes belong to the separate app-settings worker and its `app-settings-host-tests.md`.

Tests were relocated from `androidTest` to `test` rather than executed with the production AndroidJUnitRunner/Application. The two temporary ignores (NetworkLogs and SummaryStatistics) are removed through that relocation. No ignore or pass-on-error catch was added to make the suite green. The actual-host suites are additional to the isolated native-content suites and do not silently replace them.

## Reviewed boundaries required before execution

### Root shell and statistics (`SafeRootHostFixture.kt`)

- Explicit plain `Application`; check before any host launch. Koin contains only strict memory-only doubles.
- The actual `HomeScreenActivity`, original XML navigation graph, real NavHost, production bottom-navigation callbacks and native Home chrome are created.
- A fail-closed FragmentFactory is installed before `onCreate`. Home/Configure/About controllers are replaced by test fragments. Home's production `HomeContent` renders a synthetic local state/callback. Its toggle cannot start a VPN. Unlisted destinations throw at fragment creation. This is not an account boundary: Rethink+ selection calls RpnProxyManager before navigation reaches the factory, so none of these tests may click Rethink+ without a separate review.
- The actual `SummaryStatisticsFragment` is retained. Its ViewModel, AppConfig, EventLogger and preferences are strict doubles. Paging sources contain no rows, so app identity lookup cannot initialize FirewallManager or fetch favicons. `totalUsage` returns four zero fields in memory.
- Fake version equals the current build, avoiding migrations. First launch is false; updates/reporting disabled. NewSettingsManager's inspected eager initializer can only read the fake current `appUpdateTimeTs`, which is not expired. Its persistent writes are not stubbed/allowed.
- VpnController's inspected initializer has scalar state, lazy injection and LiveData only. Its status stream is fake and start/stop/pause/resume/close entry points throw. Logger entry points are stubbed. No Firebase, restore Worker, repository or production manager is constructed.
- Fake AppUpdater records only completion/unregister; fake billing-message provider records the host lifecycle call without contacting billing.
- Home dialog tests invoke production `showDownloadDialog`, including title/source policy and actual native dialog. Website actions are captured Robolectric intents; no browser starts.

**Scope boundary:** this proves root-shell wiring only once run. It does not prove production HomeScreenFragment's manager observers, protection toggle, About/Configure content or account navigation. The original device Home test and original HomeScreenFragment tests remain separate, unapproved execution obligations.

### DNS (`SafeDnsHostFixture.kt`, `DnsListActivityTest`)

- Actual DnsDetailActivity and actual DnsSettingsFragment, strict fake preferences/AppConfig/EventLogger, fake status stream.
- Connected-DNS LiveData has no emissions, so the latency callback, Backend constants and backend probe are never entered. Resume setting values are chosen to match mounted control values and do not issue writes.
- System-DNS info returns only documentation address `192.0.2.53` from a strict fake. Restore dialog is canceled; its positive restore/configuration/worker operations are not invoked.
- DnsListActivity is intentionally mounted at STARTED, with its production native grid and listeners. `onResume` status probing is outside this fixture. Protocol intents are captured, not launched.

### Firewall (`SafeFirewallHostFixture.kt`)

- Actual FirewallActivity, retained one-page host and actual FirewallSettingsFragment.
- Only strict theme preferences and fake VPN status are installed. Universal-settings route is captured as an Intent. Destination is never launched.

### Network logs (`NetworkLogsActivityTest`)

- Actual Activity, AndroidFragment lifecycle boundary and actual network/DNS fragments.
- Strict fake ViewModels and repositories; no repository constructors. Unemitted/empty memory-only paging intentionally avoids all row identity/proxy/native manager lookups.
- Tests exercise native initial list, empty transition, DNS tab and saved query restoration. WG-stat/RPN/account/populated-row paths are not covered by this fixture; existing isolated content/controller/routing cases remain complementary.

### App list (`AppListActivityTest`)

- Actual resumed AppListActivity and actual native adapter/list, strict fake AppInfoViewModel and no populated rows.
- Strict EventLogger/RefreshDatabase mocks must remain uncalled. Original list display, empty PagingData and query recreation are tested.
- Populated UID/rule/proxy actions still belong to adapter/AppInfo fixtures and require the independently reviewed FirewallManager startup isolation.

### Proxy visibility (`ProxySettingsActivityTest`)

- Actual STARTED Activity only, fake disabled HTTP/SOCKS5 configuration, no endpoint fetch or toggle.
- Never resume this fixture: Orbot/WireguardManager/RPN startup requires separate isolation. The single original control-display assertion is retained, not the resumed account/proxy lifecycle.

### Welcome (`WelcomeActivityTest`)

- Actual resumed WelcomeActivity with strict in-memory PersistentState and mocked Logger, including seven stored themes.
- All page transitions, Skip, Finish and Back invoke real production callbacks. Completion records the exact Home Intent and one first-launch write. Robolectric does not launch Home.
- Old resource-error catches, optional View checks, mocked interaction self-tests and pass-on-error fallbacks are replaced with concrete native assertions and exact callbacks.

## All 24 original contracts

Paths below are relative to `app/src`; “relocated” means the same package/file under `test/java` instead of `androidTest/java`.

| Original class/path group | Current execution layer and preserved obligation | Remaining gate |
|---|---|---|
| androidTest/ui/CoreNavigationTest | Relocated; actual root Activity/graph transitions Statistics → Configure → About → Home; selected semantics; real reselection/Back behavior | Production destination contents except Summary are synthetic |
| androidTest/ui/activity/HomeScreenActivityTest | Still device-only; stale View selectors changed to native tags; original display/toggle cases preserved | Strict full Home controller/Application/permission/VPN setup absent; never blanket-run |
| androidTest/ui/activity/ConfigChangeTest | Relocated; actual saved-state host recreation across portrait/landscape; synthetic Home state and actual selected Statistics route retained | Real Home controller state and physical rotation timing |
| androidTest/ui/activity/ThemeChangeTest | Relocated; original DARK → LIGHT writes/recreation, extended to seven themes | Device screenshots/system bars, full Home contents |
| androidTest/ui/activity/AppInfoActivityTest | Separate worker relocated actual Activity, 24 names retained | See app-settings-host-tests.md; no execution assumed |
| androidTest/ui/activity/AppListActivityTest | Relocated; actual Activity/list, empty memory paging, restored search | Populated manager-backed rows |
| androidTest/ui/activity/ProxySettingsActivityTest | Relocated; actual STARTED host, strict disabled-proxy config, native SOCKS5/HTTP headings | Full resumed proxy/RPN/WG startup excluded |
| androidTest/ui/activity/WelcomeActivityTest | Its two named tests merged into unit Welcome suite; complete sequence and captured Skip completion | Device onboarding visuals/accessibility |
| androidTest/ui/fragment/FirewallSettingsFragmentTest | Relocated; actual native fragment in actual host, Universal row and one captured route | Physical device accessibility |
| androidTest/ui/activity/NetworkLogsActivityTest | Relocated; original list assertion plus actual tab/empty/query recreation; no Ignore | Populated rows/WG stats/real timing |
| androidTest/ui/fragment/SummaryStatisticsFragmentTest | Relocated; original root-nav click then actual Summary fragment/time controls; no Ignore; category recreation | Populated row/service-derived state |
| androidTest/ui/activity/DnsListActivityTest | Relocated; actual STARTED Activity; all six native cards, exact five protocol route extras, saved instance | Resumed DNS/backend status path |
| androidTest/ui/fragment/DnsSettingsFragmentTest | Relocated; actual DNS host + fragment, network/custom choices, cancel/info dialogs | Restore-positive/config mutations outside this fixture |
| androidTest/ui/activity/WgMainActivityTest | Existing native FAB assertion retained, device-only | WireguardManager eager unstructured IO loads/migrates config files; mockkObject alone is unsafe |
| androidTest/ui/activity/DnsDetailAccessibilityTest | Relocated; retained real pager visibility plus actual native RadioButton role/click semantics | Device AccessibilityChecks/TalkBack, traversal, contrast, large font and target-size audit |
| androidTest/ui/activity/DnsDetailNavigationTest | Relocated; real one-page pager and native page, captured custom-DNS route/recreation | Physical device Back/rotation |
| androidTest/ui/activity/FirewallActivityTest | Relocated; real pager and native heading/control, actual recreation | Device AccessibilityChecks/TalkBack |
| androidTest/ui/HomeScreenActivityDialogInstrumentedTest | Relocated; all seven names; actual Home controller/native dialog modal/outside/positive/negative cases | Physical device window/input/accessibility behavior |
| test/ui/HomeScreenActivityDialogTest | All eight names; actual Home controller/native window, channel policy and exact callback/Intent traces | Runtime pass pending |
| test/ui/activity/MiscSettingsActivityRobolectricTest | Separate worker; actual host fixtures and preserved original obligations | See app-settings-host-tests.md |
| test/ui/activity/WelcomeActivityTest | All 16 original names, plus two merged device cases and two recreation cases | Runtime pass pending |
| test/ui/fragment/HomeScreenFragmentTest | Original 65 tests preserved, not claimed mounted-Compose evidence | Existing permissive mocks and eager manager init require isolation before execution |
| test/adapter/FirewallAppListAdapterTest | Prior worker retained 20 cases, four renderer obligations adapted | Named source migrations in original-test-relocation-map.json; initializer/real controller review required |
| test/adapter/WgConfigAdapterTest | Prior worker retained 28 cases, row constructors adapted | Existing permissive catches/mocked updates and WG init review required |

## Important independent gaps

- The default instrumented runner still starts production Application. Updating selectors is not authorization to execute those files. No safe whole-suite runner is claimed.
- WireguardManager initializer starts unstructured IO and can migrate/delete config files. It must not be referenced just to install an object mock without strict preinstalled migration/repository fakes and an independently accepted completion boundary.
- FirewallManager similarly starts eager IO. The separate AppInfo fixture installs empty strict repositories and joins startup jobs before mocking; do not assume unrelated tests have that ordering.
- Empty data is deliberate safety scope, not coverage of real traffic, installed packages or logged-in account data.
- No broad production bypass, fake flag, service switch or dependency-construction hook was added. The only production change by this worker is a stable `ffa_app_list` semantics tag on the existing native LazyColumn.
- Existing audit/parity documents that describe the temporary ignores are historical snapshots; this document and relocation map describe the current authored source only, not a pass.
