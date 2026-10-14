# Bounded DNS and shared-card source corrections

Comparison baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
Frozen pre-correction snapshot: `803cd4c884243ab9ef9c92cb0c4959d63bc27b00`.
The frozen 37-finding ledger is unchanged in meaning; this file records later working-tree corrections. Compilation, Android/runtime, screenshots and accessibility execution were not run because the approved toolchain is unavailable. No XML is removed by this work.

## Implemented source corrections

- **S29, DNS record modes:** `DnsRecordTypesContent` now renders the two exact existing OriginalButton catalog instances, selected segments and source tint overrides. Group selection semantics and original `DiagnosticToggleGroup` callback/restore implementation are retained. The catalog is no longer bypassed by plain Surface/Text modes.
- **S28, DNS flag frames:** `DnsEndpointFlag` restores 32dp minimum width/height, centered text and 5dp end spacing. The presenter explicitly distinguishes standard 25sp flags from the DNS proxy's concrete AppCompat Headline appearance.
- **S35, Smart DNS header:** `DnsFragmentHeader` resolves the concrete Material3 TitleMedium resource and original 11sp dimension-derived description through the existing source typography resolver.
- **S06, shared Settings card surface:** `SettingsCard` delegates source style/defaults to `OriginalCardSurface`, preserving its caller-supplied shape/fill/content padding. This adds inherited compat clearance and source card content-padding handling instead of resolving only fill/radius/elevation.
- **S07, Wg card container portion:** `WgCard` delegates the card surface and preserves dynamic source radius/elevation/fill/stroke and click state. All 11 calls now require original per-container padding. The generic 16dp wrapper and universal 8dp wrapper gap are removed. General/One interface uptime and traffic remain horizontal peers.

The container padding contracts are: General/One 10dp; Include Apps 10/10/6/10dp with 6dp horizontal outer margin and 48dp inner row minimum (68dp including the vertical padding); Hop 5dp start plus 7dp top/bottom; Peer 15/15/15/20dp; Detail Interface 15/10/15/20dp; Detail Actions 20dp; Other/Mobile settings bottom20dp only; network-statistics card 20dp (10dp source child margin plus10dp padding); About child padding0 within original16dp horizontal/8dp vertical margins.

## Independent helper review

`OriginalCardSurface` has the correct intended precedence: explicit instance overrides, then concrete style values, then themed default style attribute. It uses `max(cardMaxElevation, elevation)`, the source corner-overlap term, pixel-ceiling horizontal/vertical compat insets, zero tonal elevation and source shadow elevation. Physical source left/right content padding is translated for RTL.

The parent added a final positive-width filter after explicit/default stroke resolution. Explicit `BorderStroke(0.dp)` therefore suppresses a theme border without drawing a Compose hairline; `null` inherits the theme. WgCard keeps this distinction. The report pins the reviewed helper hash.

Scope limits remain: only rounded cards are modeled; shapeAppearance/cut-corner overrides, stateful background/stroke lists, source foreground/ripple/checkable states and exact shadow rasterization are not generally certified by this helper. No affected whole-layout finding is closed solely because the shared wrapper now resolves defaults.

## Source checks

- `check_dns_source_styles.py`: PASS; seven deliberate source mutations rejected; the diagnostic toggle callback/restore classes match the frozen snapshot exactly.
- `check_shared_card_source_styles.py`: PASS; 17 focused checks, eight deliberate mutations rejected, eight independently pinned baseline XML card/container contracts.
- `scripts/compose-migration/check-protocol-dns-parity.py`: PASS (controller/lifecycle source checks).
- `scripts/compose-migration/check-dns-retired-renderers.py`: PASS.
- `git diff --check`: PASS when checked.

JSON reports contain file hashes and source-only limitations. These checks are integration/regression guards, not compilation or screenshot claims. Existing runtime DNS mode callback-order tests remain unexecuted. Concurrent compound-control changes are separately owned and are outside these checks.

## Still open

S06/S07 remain partial: each caller's node-specific typography, line limits, child margins, alignment, selector/ripple state and hidden-child grouping still needs independent closure. Wg General/One and Hop inner generic spaced columns are not accepted as source-exact. Source-only DNS corrections require independent review and actual-host visual/accessibility verification when available. The 62 live layout candidates without a concrete finding also remain pending per-node source reconciliation, never implicitly equivalent.
