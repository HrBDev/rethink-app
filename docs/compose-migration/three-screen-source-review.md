# Independent source review

No remaining concrete production-code blocker found in reviewed source snapshot; acceptance remains conditional on compile/lint/runtime and fresh screenshot verification.

This review checks the current source against `three-screen-reference-audit.md` and the original XML. All six original supplied screenshots were visually inspected in that prior audit. No fresh rendered screenshots exist in this review. The JSON records exact reviewed file hashes.

## Results

- **SRC-01 — Attribute ordering and duplicate mapping (accepted by source inspection):** AttributeOrder creates a sorted unique query array and maps each original index with binarySearch. All 13 exposed indexed accessors delegate through that mapping; recycle is not indexed. Caller arrays are not mutated. Duplicates share the same queried attribute while keeping all declared indices valid.
- **SRC-02 — Overloads, precedence, and resource API (accepted by source inspection):** All three Context overloads preserve style/default-attribute/default-resource inputs. Only query ordering changes; explicit null checks still use the original arrays, and hasValue/hasValueOrEmpty/peekValue retain platform semantics. hasValueOrEmpty is available before project minSdk 23. Class/internal visibility is appropriate for this module. No missing accessor or stale TypedArray-typed consumer was found.
- **SRC-03 — Mechanical routing (accepted by source inspection):** 15 non-control tracked resolver files compare identically to HEAD after only the helper rename/import and removed TypedArray import are normalized. OriginalControls additionally contains the intended icon change; the new blocklist appearance resolver is also routed. No direct production obtainStyledAttributes call remains under compose outside the wrapper.
- **SRC-04 — Intrinsic icon sizing (accepted by source inspection):** Explicit positive icon size stays square in dp. Zero size uses painter intrinsic width and height independently, converts physical pixels to dp, and falls back to 24dp for missing/nonfinite extents. Caller-provided leading icons retain their sizing when no explicit size exists. Icons remain in the original row, with existing tints, checked-icon choice, padding, and handlers.
- **SRC-05 — Stats API23 locale compatibility (fixed during review):** An intermediate snapshot used Configuration.locales (API 24). The re-read reviewed snapshot uses ConfigurationCompat.getLocales(configuration)[0] with Locale.getDefault fallback. This resolves the minSdk 23 compatibility issue; it is not a remaining blocker.
- **SRC-06 — About target contracts and state (accepted by source inspection):** The patch restores nested description padding/5dp line spacing, native text appearances, inset 16dp rounded .75dp actions, equal support columns and individual captions, and original section rhythm. Dynamic usage/sponsor visibility, flavor-controlled updates, progress, and existing click dispatch remain state-driven. Top/horizontal safeDrawing belongs to the destination; Home still reserves bottom navigation.
- **SRC-07 — Stats target contracts and mode (accepted by source inspection):** Source heading/alpha behavior, centered period group, shared INSIGHTS card, three weighted KPI columns/dividers, and native sizes/colors replace the screenshot mismatches. LIST and INSIGHTS remain separate, selected by existing state; totals, filters, time callback, close/detail behavior, and section visibility are retained. No mode was forced to imitate the target.
- **SRC-08 — DNS target contracts and behavior (accepted by source inspection):** Native elevation overlay is applied to the original checked/background fill choice; no screenshot RGB is hard-coded. Selector/group/row geometry remains source-derived, safe area belongs to the full-screen destination, and the standalone list inset change excludes the bottom sheet. Paging, scroll states, selection, query, apply, discard, and confirmation branches remain intact.
- **SRC-09 — Resource and sizing test oracles (verification limitation):** Wrapper tests use independent single-attribute platform queries for all three overloads across six themes, including duplicate mapping and unmodified input. Native MaterialButton tests compare minimumHeight and iconSize from the same generated styles. Those assertions validate those properties only: they cannot prove final measured/painted height, text padding, line baseline, outline joining, or screenshot parity. The fixed 32dp icon controls deliberately skip the minimum-height comparison. Compose bounds/native measured-layout comparison is still needed when execution is authorized.
- **SRC-10 — Compile, lint, API matrix and runtime (verification limitation):** No compiler, Android lint, Robolectric, device, emulator or fresh render was run. ResourceType suppression is localized to remapped TypedArray access; custom wrapper allocation/recycling still requires lint review. Android fixtures currently target API 35, so a minSdk 23 smoke run remains useful. Static string guards detect known changes but do not execute Kotlin or validate all dynamic states.

## Source checks independently rerun

- `python scripts/compose-migration/check-ordered-style-attributes.py --self-test`: {'result': 'PASS_SOURCE_ONLY', 'queries': 62, 'rejected_mutations': 11}
- `python scripts/compose-migration/check-about-reference-layout.py --self-test`: {'result': 'PASS_SOURCE_ONLY', 'contracts': 44, 'rejected_mutations': 15}
- `python scripts/compose-migration/check-stats-reference-layout.py --self-test`: {'result': 'PASS_SOURCE_ONLY', 'contracts': 22, 'rejected_mutations': 19}
- `python scripts/compose-migration/check-blocklist-reference-restoration.py`: {'result': 'PASS_SOURCE_ONLY', 'checks': 15, 'rejected_mutations': 17}
- `python scripts/compose-migration/check-dns-blocklists-parity.py`: {'result': 'PASS_SOURCE_ONLY', 'protected_methods_and_callbacks': 131, 'negative_control': 'PASS'}

## Remaining acceptance work

- Compile Kotlin/resources and run Android lint without claiming that compilation proves visual fidelity.
- Run authored plain-Application resource/native-control and Compose bounds fixtures, including API23 compatibility and all supported themes.
- Compare actual native measured/painted control bounds, intrinsic icon extents, and joining/insets; property-only minimum-height assertions are insufficient for the original flattened-selector symptom.
- Obtain fresh corrected About, Stats INSIGHTS (plus retained LIST behavior), and DNS screenshots at controlled size/theme/flavor/font scale. Check safe insets, text leading, card surfaces, actions, and footer/navigation ownership.

The reviewer edited reports only. No network, downloads, builds, Android execution, device access, business logic, or production source changes were performed.

Separate About, Stats, and DNS geometry/bounds fixtures are authored but remain unrun. The shared helper native tests compare property resolution only.
