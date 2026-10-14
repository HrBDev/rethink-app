# Original native control appearance restoration

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
Previous native snapshot: `e2b97de59cc854de74010c096ceffbaed6cb4a55`.

## Implementation and family coverage

`OriginalControls.kt` is a native Compose renderer. It reads Android theme/style resources and draws original drawable artwork directly on the Compose canvas. It does not instantiate Views, inflate layouts, restore bindings, construct production controllers, or execute service actions.

- Filled, outlined, text, and dialog buttons retain their distinct source style parents. Programmatic AlertDialog actions use separate positive, negative, and neutral aliases.
- `toggleButtonStyle` and `toggleButtonNoStyle` retain their different outline colors and 1dp stroke. WireGuard, app/statistics time choices, blocklist tabs, LAN/probe mode controls, and app/statistics icon selectors use source variants. Segment end caps are direction-aware; adjoining corners are squared, with contiguous 1dp-overlapping row spacing where integrated. Selection callbacks remain with existing owners.
- `ThinnerChip.Large` retains its 18dp corner, 32dp minimum visual height, and 14sp family typography, plus each source's 12sp or state-list overrides. Firewall, rule-type, app selectors, log filters, and summary filters use their source templates.
- `ThinnerChip.Action` retains its 12dp corner, 24dp minimum visual height, 11sp family typography, and 15dp source icon size. Home actions, summary/app detail links, history actions, WireGuard passive chips, and blocklist actions preserve explicit positive/negative/neutral overrides.
- `ActivityTimeSegment` retains its 20dp/38dp/13sp bold default and stateful fill/stroke/text. RPN auto/manual variants retain their explicit 8dp/36dp/12sp overrides.
- `ConnTrackerSegment` retains its 8dp/40dp/13sp outline-only definition. The pinned baseline has no live usages; no screen was newly assigned this style.
- Four Rethink chips preserve both explicit `ShapeAppearance.LargeComponent` 1dp input and inherited `ThinnerChip.Action` corner input. MDC 1.14 applies `chipCornerRadius` after shape appearance and replaces the model's corner size, so inherited 12dp wins. This matches the pinned [ChipDrawable implementation](https://raw.githubusercontent.com/material-components/material-components-android/1.14.0/lib/java/com/google/android/material/chip/ChipDrawable.java), rather than assuming the explicit shape always wins.
- Programmatic RPN filters/frequent-country chips have separate recipes matching the original constructors: 40dp minimum height, 12dp end/start padding, 1dp border, and their original checked/background/text palettes. Filter checkmarks remain explicitly suppressed.
- Framed TextView actions retain their source drawables, typeface, padding, and stateful background tint. Passive badges use `OriginalLabel` without click semantics or an artificial touch target. App notes retain their actual 18dp MaterialCardView-style presentation; they were not a Chip in the baseline.

The catalog is generated from pinned source attributes, including qualified `values-w600dp` overrides. Generated explicit-null metadata prevents `@null` from resurrecting theme defaults. Source text sizes preserve raw SP values for nonlinear font scaling, and typeface weights use the native-family-aware resolver. Clickable compact controls keep a separate minimum interactive target; ripple indication is clipped to the visual control.

The direct-call ledger is `button-chip-integration-ledger.json`. It records 117 transformed direct calls; wrapper fanout and custom labels are separately tracked by the renderer map and equivalence decisions. Equivalent app-detail positive chip recipes are split into their original list 11sp and insight 12sp variants. Catalog counts and planned variant assignments are source linkage evidence, not executed screenshot coverage.

## Verification performed

- `check-original-controls.py`: passed 284 catalog source keys, 271 generated style resources, 48 explicit-null overrides, and 117 logged callback-preservation checks.
- Existing source guards passed: 205 WireGuard controller methods; 221 RPN methods; 67 app methods; 131 DNS/blocklist methods; 19 rules methods plus 9 rule boundaries; 32 statistics methods; 116 utility methods plus 7 protected dependencies; 117 network settings methods; and 522 unchanged protected files.
- Local WireGuard resource-name checks and XML parsing passed. `git diff --check` passed.
- Eight focused `OriginalControlsTest` methods were authored using plain `Application`, synthetic state, and callback recorders. They cover family geometry/typography, six-theme resource resolution, explicit nulls/checkmarks, selected state colors, raw SP/typeface weight, compact bounds/RTL segments, disabled/passive semantics, and exact-once synthetic selection. They were NOT compiled or executed.

## Remaining validation boundary

No Android compilation, Gradle execution, dependency fetching, installed-app testing, or rendered screenshot comparison was possible in this environment. Exact measured geometry, typography under real font scaling, intrinsic drawable tint-clear rendering, icon uniqueness, ripple placement, and complete device/theme behavior still require the approved toolchain and isolated presentation tests. No production Application, VPN, traffic, probe, account, permission, export, or external action was run. The generated resources and source checks must not be represented as runtime or pixel-parity validation.
