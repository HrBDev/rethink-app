# DNS settings and blocklists reconstruction

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

The prior filesystem checkpoint was lost. This source was reconstructed from that baseline and retained review requirements on 2026-10-14. **Earlier build/test/parser results are not evidence for this checkout.** This document records implementation and bounded checks, not visual or runtime sign-off.

## Persisted scope

Nine controllers now render native Compose:

- `DnsSettingsFragment`: four DNS choices, connected name/type/latency, refresh/restore, system-DNS copy prompt, local blocklist status, ten settings switches, allowed record types and block-free mode navigation.
- `DnsListActivity`: all six protocol destinations, two/three columns at the original 480dp boundary, auto-size ranges, dot legends, status colors and a two-physical-pixel selected border.
- `ConfigureOtherDnsActivity` and `SmartDnsListActivity`: native headings/description with the original root/header/fragment-container IDs and transactions.
- `RethinkListFragment` and `RethinkListBottomSheet`: paging, metadata/copy/edit, Sky/Max choices, endpoint selection/status/flag reads and update/check/redownload states.
- `LocalBlocklistsBottomSheet`: every existing action, download state and confirmation/lockdown prompt.
- `RethinkBlocklistFragment`: local/remote packs and advanced lists, headings, entry links, level/selection colors, search/stamp submission, All/Selected/subgroup filtering, download states, Apply/Cancel/Back and all retry/downloader prompts.
- `RethinkPlusFilterBottomSheet`: distinct nonblank subgroup chips, Apply and Clear, native bottom-sheet content and themed inflater context.

There are 14 new Kotlin presentation/state/presenter files under `ui/compose/dnssettings` and `ui/compose/blocklists`, plus dedicated accessibility strings. Existing protocol list/editor fragments, Smart DNS child fragment and BlockFreeDnsActivity belong to the separate endpoint migration and were not overwritten.

### Framework boundary

`DnsFragmentHost` uses a minimal `LinearLayout` and `FragmentContainerView` to preserve FragmentManager lifecycle, stable IDs and the original replacement transactions. Labels are Compose. No legacy content is wrapped in AndroidView. `ConfigureRethinkBasicActivity` and the single-page `DnsDetailActivity` framework hosts remain outside this change.

### Retired rendering code

Five adapters were removed only after checking all `app/src` source sets, tests and resources for live imports/constructors: `RethinkEndpointAdapter`, `LocalSimpleViewAdapter`, `RemoteSimpleViewAdapter`, `LocalAdvancedViewAdapter`, `RemoteAdvancedViewAdapter`. Their real persistence bodies are retained in the presenters and compared against Git baseline source. Layout resources remain for the global resource audit.

The eight protocol/Smart/block-free adapters and `DnsCryptRelaysDialog` were not retired in this reconstruction because their separate baseline endpoint controllers still reference them. Their previous retirement is not assumed to apply here.

## Preserved contracts and quirks

- DNS initialization reads state before checked listeners are attached. Same-value assignments are suppressed; same-widget reentrant changes update the check without redispatch. Cross-widget callbacks remain nested. Restore-defaults and resume still perform the original programmatic callbacks, including the ALG/split-DNS intermediate check and no-op assignment.
- Proxy switch clicks only write; enclosing-row clicks additionally log. Temporarily disabling an individual switch does not disable its row. Stable DNS checks restore after listener installation in XML hierarchy order.
- Four DNS radio checks remain independently checked until the existing status update resets them. Reselects remain actions. The system-DNS info action does not change selection; lockdown still blocks network DNS selection.
- Original fragment-result, WorkManager, preference, logging and navigation method bodies are preserved. Download and delete scopes and teardown guards are retained. The local download start still outlives sheet dismissal, while local deletion stays view-lifecycle bound.
- Download chips preserve independent progress/enabled/visibility outcomes, including failure branches that only clear progress. The Rethink FAB remains hidden because its baseline starts GONE and has no show path.
- Endpoint selection mutates the same entity before the original manager call. Editability remains name-based and edit still checks for a tunnel. Flag lookup and status use the existing inputs. Bind-time polling guards are not re-evaluated on every tick, and a resume alone does not initiate another flag lookup/polling cycle. Actual paging/rebinding cadence still requires runtime comparison.
- Packs retain contains-all semantics and deduplicated integer IDs. Advanced rows mutate the original entity before writing. Persistence outlives row disappearance; detached-host guards remain. Entry links use `url[0]` and do not toggle selection.
- Search change and submit remain separate; stamps restore only on submission. Blank submission is ignored. An unhandled ordinary submit hides the keyboard. Stable tabs/query restore after listener setup; anonymous generated filter-chip IDs do not gain new persistence. Main Cancel reverts before finishing, while Back-dialog Discard only finishes.
- The subgroup sheet still edits the existing mutable Filters object immediately. Back/dismiss is not silently changed into a rollback. Apply dismisses before posting; Clear posts a fresh Filters object before dismissing. The host reference is still not restored from arguments after recreation.
- Local download/delete prompts remain noncancelable via Back/outside. Lockdown Cancel still proceeds with the existing Android downloader, whereas Back/outside only dismisses. Error downloader changes retain cancel → preference → reset → retry ordering. Ordinary prompts remain transient on recreation.

## Styling, accessibility and unresolved visual gates

The shared `RethinkTheme`, `SettingsCard`, and NoDim window helper are used with existing theme attributes. Only baseline NoDim prompts use the NoDim helper. Dialog secondary/neutral actions use secondaryTextColor; positive actions use the AppCompat colorPrimary attribute. DialogFragment content uses `inflater.context`, retaining the themed window context. Pack/tag cards request the original elevated-card style and selectedCardBg/background fill explicitly.

The reconstruction includes original row/card dimensions, protocol auto-size limits, selected shapes, basic selection fade/scale, progress cues, RTL-aware start/end layout, scrolling/wrapping text and accessible action labels. These are implementation choices reviewed against source, **not screenshot equivalence evidence**. Font metrics, Frost/Plus/alpha backgrounds, shadows/compat padding, button positioning, large-font clipping, TalkBack/focus order, swipe behavior and platform insets still need isolated device comparisons. Native polling/rebinding and interrupted paging teardown also remain runtime gates.

## Fresh checks

- `python3 scripts/compose-migration/check-dns-blocklists-parity.py`: **PASS**, 131 protected methods and checked callbacks compared to baseline after explicit presentation-property aliases and formatting/comments only. Includes all four actual adapter write bodies and seven endpoint methods. A changed-write negative control is rejected. Additional source assertions cover elevated selected cards, blank search and subgroup dismissal/post ordering.
- `python3 scripts/compose-migration/check-dns-retired-renderers.py`: **PASS**, five retired definitions/imports/constructors absent in every `app/src` source set, with a reintroduced-constructor negative control.
- Scoped direct app-resource check: all references in the 14 new native Kotlin files resolve; this is not AAPT linking and does not check dependency aliases or dynamic drawable types.
- Owned existing-file `git diff --check`: **PASS**.
- The coordinator's first current migration Kotlin compilation reported seven non-transitive `R.attr.colorPrimary` errors in this group. Those were fixed to `androidx.appcompat.R.attr.colorPrimary`. A later compilation found the private checked/isChecked JVM setter collision; the private backing property was renamed checkedValue without changing dispatch. A successful final post-fix compilation is not asserted here; the coordinator owns the exact build log/result.

No Android or JVM tests below were executed by this worker during reconstruction. No historical test pass is reused.

## Tests authored

20 JVM methods:

- `DnsControlTest`: seven cases for initialization, equal assignments, reentrancy, nested order, disabled programmatic updates, exception recovery and stable restore order.
- `BlocklistSelectionPresenterTest`: six strict mocked cases for local/remote selection, write/read/publish order, detached host, contains-all and original entity mutation. Test dispatchers are injected; production dispatcher defaults are unchanged.
- `RethinkEndpointPresenterTest`: seven strict mocked cases for select/reselect, inert metadata, name-based editability, copy order and edit guard/extras. Robolectric explicitly uses plain `android.app.Application`, not the production Application.

20 new plain-Application Robolectric native-content methods in `src/test`:

- `DnsSettingsComposeTest`: seven cases covering all controls, callback separation, radio reselects, prompt dismissal, copy, refresh disabling, RTL and large font.
- `BlocklistsComposeTest`: eleven cases covering row/checkbox cardinality, selected fill, entry links, external selection refresh, tabs/search/filter, progress, three-way prompts, noncancelable deletion, lockdown Back versus Cancel and subgroup actions.
- `DnsProtocolGridComposeTest` and `DnsSettingsChoicesComposeTest` add two separate counter-only fixtures. The original `DnsListActivityTest` and `DnsSettingsFragmentTest` remain genuine ActivityScenario integration tests; only their View-ID assertions were adapted to native semantics tags.

All new content tests explicitly use plain `android.app.Application` under Robolectric. They do not launch production Activities. The retained integration tests and all connected/instrumentation execution remain blocked because the default runner starts the production Application. Only the coordinator may run individually reviewed isolated JVM suites. No blanket production instrumentation run is authorized. Main compilation and test compilation can be coordinated separately from execution.

## Remaining gates

Use the coordinator's approved toolchain and baseline-first build process. Compile the final sources and test assets, then execute only explicitly reviewed isolated suites. Compare full event traces for rapid/repeated taps, programmatic restore/resume, sticky downloads, prompt Back/outside/Cancel, callback order, paging refresh/rebinding, recreation and in-flight teardown. Keep runtime/visual parity unchecked until those gates pass.

No backend, DAO, repository, service, scheduler, validation, manifest, dependency or toolchain file was edited by this group. No VPN activation, live user traffic, permission change, credential access, installation, agreement acceptance, app launch, commit or remote publication was performed by this worker.
