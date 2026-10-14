# Custom rules reconstruction: native source and verification ledger

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
This note describes the reconstructed files in this checkout, not the lost earlier workspace. No earlier compilation/test counts are reused.

## Included source

- `CustomRulesActivity`: native tabs and swipe pager, same tab order, incoming UID/rule/tab extras, theme selection and paused-state redirect.
- `CustomIpFragment` and `CustomDomainFragment`: retained public fragment routes with native Compose content.
- `ui/compose/rules`: paging lists, group headers, app icons/names, IP flags, status badges, selection, add/edit forms, delete confirmations, DEBUG document import confirmation/summary and rule-detail content.
- `CustomIpRulesBtmSheet` and `CustomDomainRulesBtmSheet`: framework dialog/lifecycle hosts with native content, original serializable entity arguments, status guards and rule manager calls.
- `CustomLanIpDialog`: unchanged constructor/callsite contract; Compose field rendering backed by the original LAN controller methods in `CustomLanIpState`.
- Both `CustomIpAdapter` and `CustomDomainAdapter` are retired after a Kotlin callsite scan. Legacy XML is retained until full resource/variant cleanup is authorized.

AppInfo/app-wise rule sheets and adapters are outside this group. No shared theme/build/manifest/backend/ViewModel changes belong to this group.

## Behavior retained

| Boundary | Implementation |
| --- | --- |
| Data | Same custom/app/all LiveData paging sources, UID initialization and unfiltered count sources. No client-side sort. Group headers use index-zero/UID-transition behavior. |
| Search | Immediate changed-text filter callbacks; deduplicated unchanged text; initial empty callback; ASCII-blank submission suppression. The resumed settled pager page resets the query before clearing focus/hiding the keyboard. Hidden pages do not receive a fabricated resume. Obsolete SearchView IME-restart mechanics are removed. |
| Restoration | `RulesPageState.Saver` saves only the baseline query. Ordinary editor/draft, validation error, selection, delete confirmation, import preview and summary state are transient. Pager/list scrolling retain Compose saveable state. Detail DialogFragments retain their original entity argument bundle. |
| IP add/edit | Original normalization, IO parsing, invalid-IP and CIDR validation; global allow maps to BYPASS_UNIVERSAL and per-app allow to TRUST. Add/edit use the same UID/entity, parsed port, status and blank proxy values. Add clears input; edit retains it; both dialogs remain open. |
| Domain add/edit | Existing extraction and domain/wildcard validation. Only edit trims before extraction. Prefix `*`/`.` changes the selected type. Same add/update parameters and keep-open behavior. |
| Selection/delete | Legacy entity-equality selection behavior. Selected items override UID/all scope regardless of filter. Domain selection clears before write; IP selection clears after write. Cancel clears bulk selection; outside/Back only dismisses. |
| Details | Seven mutation-method bodies are baseline-identical, including IP deep copies and event logging. Status no-op guards, app/global choices, DomainRulesManager initial lookup and picker-dismiss callback contracts remain. Baseline GONE/unwired country/proxy cards remain absent. |
| Import | DEBUG-only OpenDocument picker accepts `text/plain`; cancel is a no-op. The original import helper is unchanged. Parse does not insert; confirmation chooses original UID-dependent status; one confirmation dismisses the pending preview before scheduling writes. Same count labels, summary and event logging. No baseline export UI existed here. |
| LAN | Original reset flag initialization, saved/default value loading, auto/manual transitions, enabled fields, reset/save callbacks, trim/validation, private IPv4/ULA constraints, prefix bounds, blank pairs, preference write ordering and change flag logic. AUTO does not erase stored manual address strings, matching actual baseline code. |
| Rendering | User-resolved Android theme, no-dim ordinary dialogs, bounded scrollable forms, RTL-relative layout, scalable text, FlowRow actions, accessible edit/detail labels and validation live region. Every baseline field and visible action remains reachable. |

## Fresh checks executed

`python scripts/compose-migration/check-custom-rules-contracts.py` passed on this reconstruction:

- 19 controller method bodies byte-identical: 7 detail-sheet mutations and 12 LAN initialization/loading/action/validation methods.
- 9 manager/DAO/ViewModel/import/entity source files unchanged from baseline.
- No legacy binding inflation, AndroidView renderer, MaterialAlertDialogBuilder or RecyclerView rendering in these hosts/helpers.
- No retired custom-rule adapter references.
- Query-only saver and transient ordinary detail-confirmation guards.

Fresh hashes and method names are recorded in `custom-rules-source-report.json`. Focused `git diff --check` and a new-file trailing-whitespace check passed. These are source checks, not compilation or runtime/visual parity.

The coordinator owns all Gradle and isolated test execution. The coordinator reported that no custom-rule errors appeared in its second-snapshot Kotlin pass. This is a limited coordinator report, not a claim that all variants or this group's tests passed; test execution remains pending. No Gradle command or production app/service/network workflow was run by this worker.

## Fresh isolated JVM test sources

All five test classes use a plain `android.app.Application`, not the production application or activities:

- `RulesPageStateTest`: 8 tests for header/key/selection logic, UID statuses, query/reset/submit ordering, and query-only save restoration dropping transient state.
- `CustomRuleActionsTest`: 8 tests using the actual UI action controller with a fake port and mock logger. Covers invalid IP/CIDR/domain/wildcard input, parser/UID/status arguments, keep-open behavior, trim distinctions, selected/UID/all deletion and order, import read-vs-confirm and release guards.
- `CustomLanIpStateTest`: 6 tests with mocked preferences for initialization order, AUTO behavior, six-pair save ordering, invalid/incomplete input, mode reload/blank pairs, and private/ULA/prefix validation.
- `RulesContentTest`: 5 native Compose tests with local callbacks for errors/draft, wildcard transitions/transient recreation, cancel, 200% RTL action reachability and global detail options.
- `CustomLanIpContentTest`: 2 native Compose tests with local callbacks for all 12 fields, reset/save and enabled states.

Total: 29 tests written. Execution status remains pending until the coordinator runs these exact classes against the current snapshot. Tests do not activate VPN, access live traffic/credentials, alter permissions/network, import actual files, or execute production account/export operations.

## Remaining gates

Coordinator compilation and isolated test execution; all required full/TV/channel variants; screenshot comparison across selected themes, normal/wide widths, RTL/large text; lifecycle, pager/search, Back/cancel, document picker interruption, paging/filter transitions and fake-service repeated-action verification. Source completion does not imply release readiness.
