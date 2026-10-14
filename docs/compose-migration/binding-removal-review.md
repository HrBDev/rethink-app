# Binding-removal behavior and isolated-test review

Source-only review, 2026-10-14 UTC. Original baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`. Recovered pre-removal Compose WIP: `87b3b9975afdb59492bc92cf97b9560cdd725330` (tree `1949435b20c49bb03ec06123c27f913a4974882b`). Current changes are compared with that WIP, while original resources and controllers are checked against the baseline where relevant.

This review executes no Gradle task, test, application, service, emulator/device operation, network request, installer, permission/account flow, VPN, traffic, or application import/export. It is not a build result or functional/accessibility/visual acceptance certificate.

## Review findings

### Legacy single-page restoration needs an explicit compatibility path

The first binding-free version used `AndroidFragment` for the Firewall and DNS settings page. Its generated Compose container identity cannot adopt the previous ViewPager2 `FragmentStateAdapter` page, restored under tag `f0` with no container ID. A pre-migration Activity Bundle could therefore retain the old Fragment alongside a newly created page, losing visible state and leaving duplicate lifecycle observers. New-host-only recreation tests do not cover this transition.

This issue was reported and corrected before runtime. The final `setSingleSettingsContent` host uses a fixed resource container and retains tag `f0`. It only adopts the expected exact Fragment class; the legacy migration additionally requires `id == 0`, tag `f0`, and an already-added Fragment. A single reordered remove/add transaction rebinds that same instance, then `onContainerAvailable` attaches the restored view. It does not create a second page, change its tag, or replace its saved state/ViewModels. New FragmentManager state uses the stable container directly. Max lifecycle remains RESUMED, capped by the Activity lifecycle.

`SingleSettingsFragmentHostTest` produces a genuine one-page ViewPager2/FragmentStateAdapter Bundle using a synthetic Fragment. It checks the same object captured after restoration, saved marker state, one page/view, RESUMED lifecycle, one create/view-create/resume callback, no intermediate destruction, and another recreation after migration. This is a source-reviewed regression fixture, not an observed passing result.

### Fixed-ID Activity-owned Fragment bridge

The Home, generic FragmentHost, and Rethink configuration shells replace XML rendering with Compose content around a framework `FragmentContainerView`. The bridge retains resource container IDs and FragmentManager ownership. `onContainerAvailable` reconnects a matching restored Fragment view; `commitNow` makes a newly created host available before its ready callback. Home retains its primary NavHost and original navigation graph. Composition disposal removes only the bridge's lifecycle observer; Activity FragmentManager destruction owns Fragment cleanup and saved state.

The implementation intentionally requires the fixed host to remain in composition for the entire Activity lifetime. It is not a general conditional/reorderable Fragment composable. Unknown destinations in isolated root tests remain rejected by their preinstalled FragmentFactory.

Home installs the selection callback only once after both the original startup request and the actual NavHost are available. Existing posted navigation, reselection suppression, Back-to-Home behavior, selected-item saving, and navigation options remain present. Actual initial Fragment creation now occurs when composition attaches, rather than during XML inflation inside `super.onCreate`. First-run, upgrade/restore, rapid lifecycle, and restored-back-stack timing still require runtime verification; source ordering alone cannot certify them.

`ConfigureRethinkBasicActivity` now retains a FragmentManager-restored instance instead of replacing it unconditionally. This is an intentional saved-state preservation correction, not proof that every real blocklist/controller path has been exercised.

### Insets, theme, and transient chrome

User theme application still precedes `super.onCreate`; status/navigation-bar icon treatment and Frost calls retain their positions. BaseActivity's max-width code is unchanged apart from its obsolete binding explanation. All five new Activity roots explicitly preserve platform inset dispatch to nested Fragment ComposeViews. Home still adds bottom system-bar padding only at API 27+.

The snackbar now anchors to native bottom-bar window bounds, listens for later geometry changes, and retains the capped Compose root's horizontal boundaries when the parent is the window content FrameLayout. It removes geometry/layout observers on dismissal. A synthetic margin test covers the measured 600-pixel example without launching Home.

A final cross-reference audit found two ServerSelectionFragment reads of the removed navigation View. They now observe the native geometry registry and preserve the old formulas: FAB clearance is system-navigation bottom plus full navigation-bar height plus 16dp; list clearance is full bar height plus 24dp. Initial/changed/removed geometry recalculates local UI state, native insets recalculate the FAB, and a view-lifecycle observer removes the listener on destruction. Geometry notifications suppress identical bounds and protect against a stale owner removing a newer anchor. The two pure `HomeTourBoundsTest` cases cover registry notifications/disposal; they do not launch ServerSelection or touch its account/backend dependencies.

Actual inset delivery, snackbar placement, wide-window/large-text bounds, predictive/platform Back, TalkBack traversal, contrast, and real window screenshots remain validation gates. Native Compose content and source-level semantics assertions are not substitutes for those checks.

### Removal-only changes

The build change removes generated ViewBinding and its phone/TV delegate dependency/version alias. Dead Alert/Firewall spinner adapters, the unused sponsor view, a wholly commented Alerts implementation, unused Material imports, and unused ArrayAdapter allocations are removed. The retained dialog item arrays/callbacks and security/backend controller statements remain unchanged in those cleanup hunks. This review does not reinterpret ordinary local state variables named `binding` as generated ViewBinding.

## Test-safety decision

The binding-removal checkpoint covered **81 exact classes / 569 declared test methods**. The later independent component-style addendum extends the current cumulative source-only allowlist to **84 exact classes / 598 declared methods**; see `component-style-restoration-review.md`. The machine-readable `binding-removal-safety-allowlist.json` carries the current exact class names, method names/counts, class SHA-256 values, changed relevant source pins, all-app/src identity, build/configuration pins and the replacement launcher pin. The original checkpoint carried forward the separately reviewed 60 content classes and 18 actual-host classes, with its delta review, plus three framework/geometry classes. The current addendum adds Home spacing, field and control fixtures and the reviewed existing-class methods. It does not allow a package wildcard, an arbitrary future method, or an unreviewed class.

This is conditional source-safety clearance only. The manifest's execution status and runtime clearance remain BLOCKED until independent toolchain/artifact/launcher execution-environment review. No test execution is permitted by the current handoff manifest.

The existing `host-safety-review.md` and `content-test-safety-allowlist.json` remain historical evidence. Initially all 60 content test class files and their listed notable boundary/helper files matched their prior pins; the subsequent one-method SnackbarContractTest addition and shared geometry changes were independently reviewed above. Changed host/configuration pins are reviewed as a delta; an old hash is never treated as matching changed bytes.

The changed Firewall/DNS tests use the existing strict fake dependencies under explicit plain `android.app.Application`. New pause/resume, repeated recreation, and Back paths only exercise those same synthetic reads and framework lifecycle. The Firewall PAUSED test emits to the intercepted memory-only LiveData and inspects a Robolectric-recorded PauseActivity Intent; it does not construct PauseActivity, BraveVPNService, or a tunnel. The DNS connected-name LiveData remains nonemitting, which is essential because its actual callback can resolve native Backend constants before latency interception.

The two new Fragment-host fixtures use plain Application, a generic FragmentActivity, synthetic Fragment/Compose content, local arguments, and Bundle values. It does not construct a real app Activity, Koin graph, database, manager, backend, account, or network client. The stable-ID fixture’s pre-Compose host is an inline framework container with the same resource ID; the settings fixture uses a genuine framework pager. Neither inflates a production NavHost layout or constructs an application controller.

No broad `app/src/test` or package wildcard is covered. Existing fences still prohibit real Application startup; real Home/WireGuard/account/service integration; populated root/Summary/AppList/log paging; actual DNS restore/refresh or setting changes; unreviewed dialog confirmation; permission changes; backup/export; and third-party communication. Existing actual-host API/lifecycle caps, coroutine/observer cleanup, fake factories, synthetic user.home, and workspace-only visual output rules remain mandatory.

## Current execution blocker

The prior Java 17/Android SDK/cached dependency environment is unavailable. The reviewed `../validation/isolated-tests.init.gradle` is also missing, as are the historical Mockito/Byte Buddy/MockK/Robolectric artifact paths. Their old hashes establish provenance only; they do not establish that an executable harness exists now. The previously reported Google artifact-endpoint denial must not be bypassed or retried through another route.

The replacement `scripts/compose-migration/isolated-tests.init.gradle` is source-reviewed and pinned in the cumulative manifest, but has not been compiled or executed. It is not a substitute for missing runtime artifacts or authorization. Before any later isolated run, restore an authorized toolchain and reverify all relevant source, dependency, agent, configuration, test-resource, and launcher pins. The Gradle wrapper distribution must already exist and be verified: wrapper bootstrap can download before Gradle processes `--offline` or an init script. Do not run or install anything on the strength of this document while these conditions are unmet. No changed test has been compiled or run in this review.


## Replacement launcher review

The checked-in manifest requires an independent runtime review before its `runtime_clearance.status` can become `REVIEWED`; it currently says `BLOCKED` with no approved Android runtime jars. The launcher's before-settings hook rejects that status before project/plugin evaluation. It also requires offline execution, disabled SDK/JDK downloads, disabled configuration cache, the single F-Droid JVM test task, an explicitly supplied matching manifest SHA-256, exact source/configuration/launcher identity, and only exact reviewed class filters. The task-graph gate rejects unexpected test tasks, agents, self-attach overrides, or a non-Java-17 test launcher.

After future runtime clearance, it will require the exact historical Mockito 5.23.0 and Byte Buddy Agent 1.18.2 premain hashes, a fully pinned local Robolectric Android-jar directory with no extra jar files, a fresh workspace-local synthetic home, and forced workspace-local visual output. It adds no proxy configuration, trust/security mutation, installer, download fallback, production Application, or service launch. Inherited Java-agent/options environment variables must be removed before wrapper startup and are also rejected in the script. These are source-inspected controls, not an observed successful execution.

The reproducible compilation and test commands are in [verification-commands.md](verification-commands.md). Those commands are explicitly blocked on verified preinstalled tools/artifacts and runtime clearance. The current manifest must not be changed simply to make a command run. A failed compile or fixture requires diagnosis and a new source review for changed paths, not removal of the safety fences.

## Frozen review identity and exact changed/new classes

- Review completion: 2026-10-14 UTC; no Kotlin, Groovy, Gradle, Robolectric, app, device or emulator execution.
- Full `app/src` envelope: `d185fb5cc04a8932c77baf2b2f8eadc440a67294b58ecdca731479b192630a36` across 1,562 files. The exact sorting/record algorithm is stored in the manifest.
- Manifest SHA-256: `c096c30e2fd7077197927f4500ac15e755b14f29fe5c751877ce9671192a31cf`.
- Replacement launcher SHA-256: `55a6aaff0dd45eb8f54c0b3d5f10faf496259a1d0c912f777da2c139892f354c`.
- Exact cumulative scope: 81 classes / 569 declared test methods. All 81 exact class names and method lists are in the manifest, with no wildcard authority.

| Changed/new exact class | Declared methods | Test-source SHA-256 |
|---|---:|---|
| `com.celzero.bravedns.ui.activity.DnsDetailAccessibilityTest` | 1 | `8653b7d8640b1472e262e07269d86b6aec1daa6aaf0608e554fc64b70e24b6c1` |
| `com.celzero.bravedns.ui.activity.DnsDetailNavigationTest` | 5 | `b0acc8ce8eef564f242af03e69bc947dea636f07eccf62fae0098a55ea970112` |
| `com.celzero.bravedns.ui.activity.FirewallActivityTest` | 5 | `343197808360ae3617fdb2014b82a70b4a4eb683cf63adfbb7f94afafb43107b` |
| `com.celzero.bravedns.ui.compose.chrome.SnackbarContractTest` | 10 | `894bfec521be7a648a23d1fb47f3c038cd7b91523234e3682c46ebc73e28c169` |
| `com.celzero.bravedns.ui.compose.home.HomeTourBoundsTest` | 2 | `26df0bf0290e72a73b0f97a654ffc581ab13baf186fe10f3048ca613332db556` |
| `com.celzero.bravedns.ui.compose.hosts.ActivityFragmentContainerTest` | 3 | `522817bd39124e786a6491f5325e6e989d2719f7d20a5a7c762104a782fdb25d` |
| `com.celzero.bravedns.ui.compose.settings.SingleSettingsFragmentHostTest` | 2 | `735ae394b53d8915a03118fb25776cafcb2dc4bdf24a3a01858bea9fcd244f45` |

The remaining carried-forward classes have unchanged test bodies; their current relevant production/configuration deltas are covered above. The whole source envelope and configuration pins supersede older byte identities for this exact snapshot while preserving every original safety restriction. New files, modified bytes, different dependencies, a changed launcher, or broader execution invalidate the corresponding clearance until reviewed.

Conclusion: the identified legacy-page restoration and native-navigation measurement hazards have source-level fixes and isolated regression fixtures. No remaining source-level test-safety blocker was found in the pinned delta. Compilation, runtime behavior, true saved-state migration, real window/inset behavior, visual evidence, accessibility/contrast and performance remain unverified. Runtime execution remains blocked.
