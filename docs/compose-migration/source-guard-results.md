# Recovered static source-guard checkpoint

## Recovery status: 2026-10-14 09:34 UTC

This Markdown was reconstructed from the surviving `source-guard-results.json` after the workspace reset. Its aggregate 23/23 result belongs to the **07:39 UTC historical checkpoint**. Binding and remaining XML-host removal work is now changing the source. The historical aggregate must not be treated as a current-source pass or migration acceptance. The surviving JSON was left byte-for-byte unchanged.

The two lost guards were restored byte-for-byte to their recorded historical SHA-256s:

- `scripts/compose-migration/check-original-test-relocation.py`: `fdbc6cbe5ad9db47fe017b20e73696c9ff5b97dccc1175982638cfc1d171f0c7`
- `scripts/compose-migration/check-statistics-contracts.py`: `fe24347bfa36456fd6c58f3163e866205ac8f1103ed48a62e786c382abac316e`

Only these two guards were rerun during this restoration, including their existing self-tests. **2/2 passed at the restoration checkpoint**:

- Original-test guard: 99 original source destinations, 16 relocations, 1,475 retained annotated declarations, seven explicitly documented native replacements, and all 86 AppInfo/Misc manifest cases.
- Statistics guard: 32 baseline-equivalent methods plus its existing route/query, enum, restoration, rendering-boundary and relocated-test obligations.
- The three additional read-only negative controls again rejected a missing relocated file, a removed original method and a removed original `@Test` annotation. No source was changed for these controls.

The local git database does not contain the original baseline commit. For this limited rerun, a temporary runner redirected only the guards' exact baseline `git show` and `git ls-tree` requests to the parent-supplied recovered `../recovery/baseline` files. It read 110 baseline files. The restored guards themselves were not adapted or weakened; they continue to require the pinned baseline in normal use. All other guard results below remain historical, and the full source suite needs another run once current edits settle.

No production source was changed by this restoration. DNS protocol guard recovery belongs to its separate owner. No Gradle, Kotlin harness, Android test, network, install, production Application, backend, database, account, VPN, security operation or remote write was executed.

## Historical aggregate

Checked at 2026-10-14T07:39:16.491941+00:00; baseline `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

**23/23 static guard scripts passed at that historical checkpoint.** Five isolated Kotlin harness scripts were intentionally not run. These counts do not describe the changing recovered source.

### Boundaries

The statistics restoration changed only the obsolete androidTest/@Ignore expectation to the relocated JVM-host path, retained original annotated names, active status and existing native route/time selector expectations. All original controller comparison functions, normalizers and mutation controls are preserved. The added relocation guard checks actual baseline/current declarations and explicit replacement mappings, rather than skipping missing old paths.

Source equality and annotated-name preservation do not prove runtime, rendering, lifecycle, accessibility or behavioral equivalence. Per-script counts overlap and must not be totaled as unique behavior coverage. Historical per-area reports were captured within the aggregate rather than overwritten.

### Historically executed guards

| Guard | Historical result | Exact reported scope |
| --- | --- | --- |
| `check_home_controller.py` | PASS | Home source guard: 143/143 checks passed; not runtime evidence |
| `check-about-controllers.py` | PASS | PASS 81 About/diagnostic protected bodies and collection/format sections; no runtime/compilation claim |
| `check-apps-controller-parity.py` | PASS | PASS: 67 complete protected command methods byte-identical to pinned baseline. No invocation/runtime claim. |
| `check-custom-rules-contracts.py` | PASS | PASS: 19 unchanged controller methods, 9 unchanged rule data/manager boundaries; native-source and transient-state guards passed. |
| `check-dns-blocklists-parity.py` | PASS | PASS: 131 reconstructed DNS/blocklist protected methods and callbacks match baseline after explicit UI aliases; negative control passed. Source-only. |
| `check-dns-retired-renderers.py` | PASS | PASS: five retired blocklist adapter definitions/imports/constructors absent across app/src; negative control passed. |
| `check-navigation-chrome.py` | PASS | PASS: seven source guard negative controls; PASS: 25 unchanged Home controller methods; startup/listener/back/navigation/throttle/prefix and framework boundaries checked |
| `check-network-settings-controllers.py` | PASS | PASS 117 pinned baseline method bodies; only the XML policy row renderer excluded. Not runtime or rendering validation. |
| `check-network-settings-layouts.py` | PASS | PASS 6 pinned layouts; 294 native tagged controls. Not runtime or screenshot validation. |
| `check-network-sheet-controllers.py` | PASS | PASS 64 protected baseline network-sheet bodies; not runtime or visual verification |
| `check-notification-ui-boundary.py` | PASS | PASS: notification trampoline byte-identical except two dialog renderer substitutions; all routing, security, permission and lifecycle code preserved. |
| `check-original-test-relocation.py` | PASS | PASS: 99 original test sources, 16 relocated; 1475 original @Test declarations retained and 7 explicitly mapped native replacements; 86 app/settings manifest cases. Source coverage only, no tests executed. |
| `check-protected-core.py` | PASS | Protected 522 files; changed: [] ; dependency version pins unchanged: True |
| `check-protocol-dns-parity.py` | PASS | 19 protected bodies in 6 hosts; 6 method-mutation and 9 row-lifecycle negative controls; PASS: protocol insert/URL validators and BlockFree controller bodies exactly match pinned base; transient dialogs, lifecycle sources, action entry points, chip restoration, retired renderers and isolated test scope checked. |
| `check-purchase-controllers.py` | PASS | PASS: changed argument, string and duplicate-callback negative controls; PASS: 211 exact and 26 narrowly adapted purchase/graphics method bodies |
| `check-rpn-configuration-controllers.py` | PASS | PASS: 73 exact and 8 narrowly adapted controller bodies; hop wrappers retained; retired renderers unreferenced |
| `check-rpn-controller-parity.py` | PASS | PASS: 221 fixed named controller/helper bodies match pinned baseline after documented presentation-only normalization. |
| `check-settings-controllers.py` | PASS | PASS 79 protected settings/menu/mode-sheet bodies; not compilation or runtime parity |
| `check-statistics-contracts.py` | PASS | PASS: 32 baseline-equivalent methods, exact route/query contracts, six-chip multi-select restoration, native renderer boundary and relocated original @Test obligation. |
| `check-traffic-log-controllers.py` | PASS | PASS: 161 pinned callback/helper bodies and native-rendering boundaries; not runtime parity. |
| `check-utility-controllers.py` | PASS | PASS: changed literal, argument, ordering and duplicate callback negative controls; PASS: 116 exact/narrowly adapted utility controller bodies; 7 protected dependencies |
| `check-wireguard-controllers.py` | PASS | PASS: 205 unchanged selected WireGuard controller bodies. Not typechecking, Android compilation, lifecycle/rendering or service tests. |
| `check-wireguard-resources.py` | PASS | PASS: local resource names in 30 WG Kotlin files; XML parse successful. Not Android resource linking or compilation. |

### Historical additional comparisons

- 522 selected protected backend/manifest/TV/helper files were unchanged.
- All five original style/color/dimension XML resources, including qualified styles, were byte-identical.
- All 62 original version entries, 93 original library aliases and four plugin aliases were unchanged; four UI/test library aliases were additive.
- All 99 original test sources had destinations, with 16 relocations and seven documented native-method replacements.

### Not run in the historical source-only pass

- `check-apps-state-host.py`
- `check-purchase-presentation-host.py`
- `check-rpn-control-state.py`
- `check-rpn-filter-host.py`
- `check-wireguard-control-state.py`

### Historical source fingerprint

The historical checkpoint covered 1598 source/resource/test/build/guard files, aggregate SHA-256 `9e7e0852950f00a98d87ea921eae58958d2d604d6d8b06e14a77f7c0e134744d`. Individual hashes and pre/post-run stability are retained in the surviving JSON. This fingerprint is historical; it does not identify the current working source.

## Precise restoration differences

1. Recreated the missing original-test relocation guard with identical historical bytes.
2. Reapplied the statistics test-tail restoration with identical historical bytes; no original controller guard or normalization changes.
3. Recreated this Markdown from the historical aggregate, adding the recovery warning and limited two-guard rerun evidence. It intentionally no longer presents the old aggregate as a fresh final-source result.
4. Left `source-guard-results.json` unchanged; SHA-256 `acbca83ec981d2ad2f614a587e9ca1243de89ba6c1f31c00dc20c17ae3d021a2`.
