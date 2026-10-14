# Styling-preservation source plan

## What is complete

- All 186 remaining physical layouts / 185 names / 4,344 original nodes have root, hierarchy, shape/surface, typography, color/alpha, dimension, grouping, semantics, content/visibility and motion contracts.
- Explicit style inheritance, theme alternatives, dimension/color definitions and referenced XML drawables are retained, including the wide Home qualifier.
- 180 layouts have verified current renderer-family file/function mappings. Six baseline definitions have no live render path; exact retirement evidence is in the inventory artifacts.
- 115 root accounting is supplied by `root-layout-accounting.json`; these are not 115 layout files. Includes/shared rows/qualified variants are separate obligations.
- 37 findings identify 32 source-confirmed visual mismatches, 3 source style omissions whose resolved effect needs runtime measurement, 1 unchecked-checkbox-style mapping gate, and 1 fixed/min/touch measurement gate. Findings attach to118 layouts.

## What is not claimed

- This is complete inventory/contract coverage, not complete equivalence acceptance. The62 remaining live layouts have an explicit mapped family and original contract but no new specific mismatch identified in this audit; they still require independent source closure. They are not silently green.
- No new spacing/design deviation is accepted. Earlier tolerance for generic Material changes does not override the new original-style preservation target. Existing semantic/accessibility expansions must be documented and checked separately.
- No Android compilation, rendering, screenshots, TalkBack or production execution occurred. A source comparison is not a pixel, contrast or accessibility pass.
- No XML, production file or source guard was changed by this audit. Later small corrective patches have their own report.

## Source correction order

1. Home cross-column barrier, qualifier grid, outer/fixed/min constraints, text contracts and elevation (S01–S05). Keep opaque nav anchoring and previously restored gutters.
2. Shared original-card/defaults plus per-card overrides (S06–S07), source switch/radio/checkbox/slider/progress variants (S08–S11/S25). Avoid broad new default changes without per-layout mapping.
3. Row families: statistics, RPN stats, domain expansions (S12–S14/S27), history, skeletons, add tile and country/header grouping (S15–S18).
4. Remaining text/framed badges/dialogs/bubbles, source vs minimum control measurement (S19–S26), DNS flag/record-mode controls and nearby header/text contracts (S28–S37).
5. Independently close all per-layout contracts; migrate test baselines without weakening assertions; preserve required ID symbols, then remove only source-proven unreferenced definitions. Source-removal and unavailable runtime validation are separate gates.

## Family dispositions

| Family | Layouts | Specific finding IDs | Source disposition |
| --- | ---: | --- | --- |
| about | 6 | S25, S26, S29, S37 | known_gaps_and_independent_source_reconciliation_pending |
| appdetails | 10 | S08, S26, S27 | known_gaps_and_independent_source_reconciliation_pending |
| blocklists | 6 | S06, S25, S31 | known_gaps_and_independent_source_reconciliation_pending |
| dns | 17 | S06, S09, S25, S28, S30, S34 | known_gaps_and_independent_source_reconciliation_pending |
| dnssettings | 4 | S06, S08, S35 | known_gaps_and_independent_source_reconciliation_pending |
| firewallapps | 4 | S06, S31 | known_gaps_and_independent_source_reconciliation_pending |
| home | 8 | S01, S02, S03, S04, S05, S26, S32 | known_gaps_and_independent_source_reconciliation_pending |
| native_host_or_adapter | 12 | S14, S18, S24, S25, S26, S33, S36 | known_gaps_and_independent_source_reconciliation_pending |
| networksettings | 7 | S06, S10, S25 | known_gaps_and_independent_source_reconciliation_pending |
| networksheets | 5 | S09 | known_gaps_and_independent_source_reconciliation_pending |
| purchase | 17 | S09, S11, S15, S16, S26 | known_gaps_and_independent_source_reconciliation_pending |
| retired | 6 | S25 | retired_no_live_baseline_renderer |
| rpn | 10 | S06, S08, S11, S16, S17, S23, S25, S26, S36 | known_gaps_and_independent_source_reconciliation_pending |
| rpnconfig | 6 | S09, S22, S25, S26 | known_gaps_and_independent_source_reconciliation_pending |
| rules | 12 | S06, S19, S20, S25 | known_gaps_and_independent_source_reconciliation_pending |
| settings | 9 | S06, S08, S09, S10 | known_gaps_and_independent_source_reconciliation_pending |
| statistics | 6 | S12, S13, S26, S33 | known_gaps_and_independent_source_reconciliation_pending |
| trafficlogs | 10 | S08 | known_gaps_and_independent_source_reconciliation_pending |
| utilities | 17 | S08, S10, S21, S25, S26 | known_gaps_and_independent_source_reconciliation_pending |
| wireguard | 14 | S07, S09, S11, S22, S25 | known_gaps_and_independent_source_reconciliation_pending |

## Required verification matrix

Every live family needs baseline-theme (light/dark/black/Plus/Frost) property comparison, default/wide qualifiers, LTR/RTL, default/large text, selected/disabled/error/loading/empty states, long content, hidden children and repeated navigation/state restoration. Use immutable baseline expectations independent from production resolvers; no app-owned XML inflation after final removal. Track compile/isolated render/device/actual-host/accessibility stages independently, with not-run stages explicit.

For actual source-removal acceptance, require: exact live/dead consumer disposition; no unresolved source visual/behavior mismatches; required ID/resource preservation; safe test oracle relocation; source guards updated without narrowing their meaning; independent review of the resulting immutable snapshot. Runtime gates remain open when unavailable.

## Storage and reproducibility

`python3 docs/xml-layout-removal/refresh_styling_audit.py` rebuilds the pinned source audit, manual finding annotations, and compact storage deterministically. The ledger keeps all 186 contracts and all 4,344 node attribute dictionaries. Category entries reference those authoritative nodes/attributes instead of repeating values. Shared resource/style/color/dimension/drawable definitions, renderer evidence, owner links and obligations are content-addressed by SHA-256 in `styling-shared-catalog.json`. The schema and hash are recorded in the ledger; `compact_styling_audit.expand(ledger, catalog)` reconstructs the exact original audit JSON value. `styling-storage-check.json` records a serialized lossless round-trip check. This changes storage only, never any finding or equivalence status.
