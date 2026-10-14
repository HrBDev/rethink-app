# XML layout removal: exhaustive source inventory

## Result and boundaries

The frozen application source contains **186 app-owned XML layout files**, representing **185 resource names**, **4,344 XML elements** and **2,516 distinct `@+id` declarations**. All 186 files are byte-identical to the original baseline. There are **no app-owned production-live or platform-required XML layouts identified by the source audit**. Five resources are still inflated by tests; the other 181 have no active resource inflation owner.

This is a deletion plan, not a deletion approval or a build/runtime pass. No application files, layouts, tests, Gradle configuration, manifest, drawable or values XML were changed by this inventory worker. No Gradle/download/device/Application/VPN/account/permission/traffic operation was run.

- Original baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`
- Frozen source: `803cd4c884243ab9ef9c92cb0c4959d63bc27b00`
- Full per-file machine record: [layout-inventory.json](layout-inventory.json)
- Human per-file ownership/candidate matrix: [layout-ownership-matrix.md](layout-ownership-matrix.md)
- Complete existing 115-root accounting: [root-layout-accounting.json](root-layout-accounting.json)
- Exact dormant-owner evidence: [retired-layout-owners.json](retired-layout-owners.json)
- Current full production source guard: [current-binding-reference-check.json](current-binding-reference-check.json)

## All source sets and qualifiers

| Physical location | XML layouts | Effective Gradle resource ownership |
| --- | ---: | --- |
| `app/src/main/res/layout` | 114 | main, all phone/TV channel variants |
| `app/src/full/res/layout` | 71 | main, all phone/TV channel variants |
| `app/src/full/res/layout-w600dp` | 1 | main, all phone/TV channel variants |
| `alpha`, `androidTest`, `fdroid`, `play`, `test`, `testPlay`, `testWebsite`, `tv`, `website` | 0 each | No layout directories/files |
| benchmark and all other repository locations | 0 | No alternate app-owned layout XML found |

The `full/` path does **not** mean these resources are full-phone-only. `app/build.gradle.kts:358–360` adds `src/full/res` to main and clears the full source set's own resource directories. `Android.bp:30` separately includes only `app/src/main/res`; it must not be conflated with the Gradle resource set.

Home is the sole qualified duplicate: `fragment_home_screen` in default and `w600dp`. Each is independently hashed and retains all node attributes in the inventory. Qualifier equality is not assumed. The original baseline had 190 XML layouts; four had already been removed before this audit: `activity_home_screen`, `fragment_rethink_basic`, `activity_fragment_host`, and `dialog_subscription_anim`. Their baseline hashes/recovery references and native host ownership remain in the accounting.

## Exact test dependencies

There are 12 active `R.layout` occurrences, five names, five test source files:

| Resource | Test references |
| --- | --- |
| `activity_customer_support` | `UtilityTypographyTest.kt:64,72`; `ContentVisualParityTest.kt:72` |
| `fragment_sponsor_success` | `UtilityTypographyTest.kt:39,50,90`; `ContentVisualParityTest.kt:113` |
| `dialog_input_ips` | `UtilityTypographyTest.kt:80` |
| `dialog_info_rules_layout` | `NetworkSheetContentTest.kt:134`; `OrbotInfoTypographyTest.kt:30`; `ContentVisualParityTest.kt:144` |
| `bottom_sheet_summary_stats_filter` | `StatisticsMaterialReferenceTest.kt:29` |

Full paths and source text are preserved per layout. The independent test-dependency audit confirms the same 2,516 layout IDs and identifies 137 production-used symbols that require explicit values ID declarations or separately reviewed removal, plus eight test-only disappearing IDs. Merely deleting XML would break those resource references even though no production `R.layout` remains. Reference tests must be replaced with honest pinned-baseline expectations before layout deletion; substituting the Compose implementation as its own oracle does not preserve the test.

## Production, platform, reflection and XML references

The refreshed whole-source binding/inflation guard passed all 35 in-memory controls and scanned **784 production Kotlin/Java files** across `main` (662), `play` (35), `website` (33), `fdroid` (23), and `tv` (31), with zero violations. It checks aliases/fully qualified resource references, inflation APIs, dynamic layout lookups and non-layout XML references beyond the 115-root ledger.

Independent scans find:

- No app-owned `RemoteViews`, AppWidgetProvider initial/preview layout, or custom notification layout reference.
- No non-layout XML link to an app layout, including manifest, navigation, styles/resource aliases, metadata and tools references.
- No separate layout-like XML stored outside `res/layout*`.
- `NotificationHandlerActivity.kt:96` uses `android.R.layout.activity_list_item`, a framework-owned invisible trampoline shell. It is not one of the 186 app XML files and must not be deleted or misreported as an app-owned exception.
- The only production `getIdentifier` is `DnsEndpointPresenter.kt:202`, resolving type `string`. FdHelper reflection accesses Android OS/file-descriptor internals, not layouts. Other reflection hits are test field access. No reflective app layout-name lookup was found.
- Internal layout-to-layout includes and `tools:listitem` preview references are preserved in the per-file index. They belong to dormant XML parents; removing only children before parents would still create broken XML resource references. Remove the approved set together after its dependencies are reconciled.
- Custom view class tags and library view tags are preserved per layout. Current Compose/framework containers, ComposeView, AndroidFragment, material bottom-sheet/dialog windows and third-party library-internal XML are separate interoperability boundaries. Removing app-owned layouts does not delete library resources or establish a View-free Android runtime.

Compiled dependency bytecode and generated APK resources are unavailable. The audit is conservative source evidence and cannot prove the absence of arbitrary dynamically assembled names in uninspected dependencies.

## Reachability accounting and legitimate dormant resources

`root-layout-accounting.json` accounts for every one of the existing ledger's 115 roots. 105 have direct baseline ownership of remaining layouts; the other ten are explicitly accounted for as four previously removed Compose shells/content, the framework-only notification trampoline, empty TestDialog host, two inherited shared hop dialogs, a retired unopened RethinkLogBottomSheet, and the retired-but-replaced DnsCryptRelaysDialog.

Owner-to-root reverse-reference chains are explicitly labelled **lexical candidates requiring semantic review**, not executable call-graph proof. Current direct Compose calls record both the call site and candidate definitions. Included-only skeletons retain their parent owners. The independent styling ledger must provide element/style equivalence and review ambiguous/retired owner mappings; source-family matches alone do not authorize removal.

Six layouts are verified baseline orphans rather than missing live Compose screens:

- `dialog_ip_details_layout`, `dialog_sponsor_info`, `item_server`: no baseline source resource/binding/name reference or XML include/tools reference.
- `list_item_server_wg_peers`: its only owner was ServerWgPeersAdapter, whose name appears only inside its own baseline definition.
- `view_sponsor_badge`: its only owner was the self-referenced, otherwise unopened SponsorBadgeView.
- `spinner_item_firewall_status`: its only owner was the self-referenced, otherwise unopened FirewallStatusSpinnerAdapter.

No fictitious native screen should be introduced solely to justify deleting these unused resources. Their bytes/attributes/hashes remain archived by the inventory and original git baseline. By contrast, `list_item_wg_hop` is shared with live GenericHopAdapter and `bottom_sheet_conn_track` with live ConnTrackerBottomSheet, so their unused sibling owners do not make the resources unmapped. `dialog_dnscrypt_relays` was live at baseline and is explicitly replaced by `DnsRelaysDialog` in `DnsCryptListFragment.kt:89`, defined in `DnsEndpointScreen.kt:168`.

The two included-only skeleton layouts are not orphan resources: `list_item_shimmer_server_row` is included from server-order history and server selection; `shimmer_subscription_loading` is included from the premium fragment. Their grouping/loading presentation needs explicit equivalence review, including both parent contexts.

## Safe next sequence

1. Finish per-layout and element-level styling/renderer decisions, preserving default/w600dp distinctions and any unresolved mismatch. Keep dormant-orphan decisions explicit.
2. Preserve required production ID symbols in a values resource without adding placeholder Views. Replace the five layout-based baseline tests using independently pinned baseline data; retain their behavioral assertions.
3. Update source guards/generators that currently assume production XML remains. Keep original evidence recoverable from the immutable baseline; do not silently weaken coverage to zero.
4. Remove only the reconciled app-owned layout XML set. Preserve unrelated manifest/navigation/drawable/value XML and framework/library-owned layout resources.
5. Re-run static checks and exact resource-reference/ID-resolution checks after deletion. Compile/resource-link and device/visual checks remain **blocked/not run** under the current unavailable-toolchain and explicit network constraints; a source pass is not build acceptance.

## Reproduction

From the repository root, with Python standard library only:

    PYTHONDONTWRITEBYTECODE=1 python3 docs/xml-layout-removal/generate_layout_inventory.py
    PYTHONDONTWRITEBYTECODE=1 python3 docs/xml-layout-removal/audit_retired_layout_owners.py
    PYTHONDONTWRITEBYTECODE=1 python3 scripts/compose-migration/check-binding-removal.py --self-test --report docs/xml-layout-removal/current-binding-reference-check.json

The inventory scripts read git baseline and filesystem contents; they only write their documentation artifacts. They do not invoke Gradle or Android.
