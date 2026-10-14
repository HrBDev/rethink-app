# Compound control source audit

## Result and scope

`check_compound_control_sources.py` is a finite, source-only regression guard. It independently reads immutable baseline `23cb6dd98805445174bd83b7237ad5f5d2c24ac9` with read-only Git commands and reparses all baseline layout XML. It does not rely on current layouts remaining present.

The current inventory contains 154 compound/slider nodes in 52 layouts. There are 151 active baseline nodes mapped to 152 concrete render paths; `list_item_proxy_cc_wg:proxy_radio_cc` has two actual native Compose consumers. Three inactive baseline exceptions are recorded separately. The 33 instantiated Network tree compound/slider nodes are individually mapped. The generic `NetworkKind.RADIO` branch has no current tree instance and is explicitly recorded without counting it as an instance.

The checked-in census retains every original attribute and now includes the native renderer, exact source-key expression, finite input/branch explanation, and bounded evidence for each path. Evidence includes actual renderer calls, wrapper caller inputs, state tag declarations, key-map entries, and Network node kind/control/key declarations. Checks compare tokenized, comment-stripped source snippets rather than accepting a catalog key merely because it occurs somewhere in the repository. Every actual native renderer/WgCheck invocation is also checked against the finite mapping inventory.

The guard checks 30 bounded source contracts, including:

- Independent baseline node/path/type/attribute equality, including all 154 nodes and 52 layouts
- Exactly 154 source style resources, exact visual-attribute projections and parent styles, and exact catalog style/kind/dimension/cleared-button mapping
- The native renderer family and source expression on each real invocation
- All 33 Network node IDs resolving uniquely through the catalog and dispatching through their concrete native path
- Source callbacks, changed-only/reentrancy-protected controller setters, slider user-change provenance, and start/finish delegates
- Source overlays, drawable/tint layers, tint-override removal, current drag callback capture, RTL drawing and gesture direction
- Native app:buttonTint precedence over framework android:buttonTint, and MaterialCheckBox replacement gated to a Material3 theme, the legacy framework button, no custom compat button, and an uncleared button
- An independent MaterialCheckBox icon tint/drawing layer and the four Wg/RPN bulk accentGood/accentBad source style entries
- Sponsor thumbWidth/thumbHeight precedence over radius fallback, thumbElevation, source labelStyle resolved through the overlay, scalar Tooltip padding, and the original sliding/userValue/finished callback order
- Press-or-drag activity drives the slider pressed-state tint, painted handle width, and enabled halo; the helper remains active after Compose cancels PressInteraction during a drag
- Passive glyphs inside owning selectable/toggleable rows, including one action for Wg/RPN bulk checkboxes and passive labels
- No AndroidView/widget-constructor/inflater bridge in the two new renderers

## Independently verified exceptions

1. `activity_universal_firewall_settings:firewall_check_ipv4_check`: an XML ancestor is `gone`. The baseline initial checked assignment is line-commented, and listener/title-click setup is block-commented in `UniversalFirewallSettingsActivity.kt`. The guard checks that no active `firewallCheckIpv4` reference remains in that baseline controller after stripping comments.
2. `list_item_play_subs:selection_indicator`: baseline visibility is `gone` and dimensions are `0dp × 0dp`. The play adapter explicitly describes selection through card styling, calls `applySelectionStyle(isSelected)`, and does not use a `selectionIndicator` binding. Both play and website adapter variants are checked for that absent binding usage.
3. `item_server:checkbox_server`: a read-only baseline search across all tracked `app/src` files finds no `ItemServerBinding`, exact `R.layout.item_server`, exact `@layout/item_server`, or literal `getIdentifier(..."item_server")` reference. The distinct `item_server_group` layout remains live and mapped. This is static absence evidence; it cannot disprove untracked external reflection.

These exceptions retain their baseline census and style entries. They are not silently counted as rendered controls or used to excuse an unmatched live call site.

## Negative controls

The guard modifies copied source strings in memory and requires every negative control to fail. No mutated Kotlin/XML file is written. The 20 cases cover removed catalog mapping, wrong direct/wrapper/network source key, removed source overlay/resource overlay, lost click/assignment/slider delegate, lost slider callback forwarding, wrong switch/seek RTL behavior, lost tint reset dependency, stale drag callback capture, duplicate bulk glyph action, lost controller listener dispatch, reversed app/framework button-tint precedence, lost accentBad bulk icon tint, and replacing Sponsor source thumb width with the radius fallback, and losing active drag state when Compose cancels its press interaction.

## Additional source observations

The accompanying implementation removes four migration-added scalar accentGood checkbox-tint initializers in Wg screen state / proxy editor state so that the native resolver's app/framework tint precedence can apply. The guard snapshots the concrete callback routes independently of those styling-only changes. It does not assert that unrelated business logic is semantically unchanged.

`OriginalCompoundControlsTest.kt` contains ten authored tests covering all 154 source styles across six themes, native tint/dimension comparison, Sponsor inherited thumb dimensions, switch release/fling behavior, tap/disabled behavior, drag/cancellation behavior, slider semantics/callbacks, clearing tint overrides, a TooltipDrawable label-style oracle against the native MaterialShapeDrawable/TextPaint label, and synthetic Press → DragStart + PressCancel → DragStop active-state continuity. The test class uses a plain Application, avoiding the production application/bootstrap. These tests were only inspected as source; they were not executed.

## Remaining gates

No Gradle, Kotlin compiler, Android toolchain, emulator, APK, screenshot, or runtime-reference execution was performed. No binary downloads, XML deletion, remote writes, commits, resets, or staging were performed by this audit.

`PASS_SOURCE_ONLY` means the finite source checks and their negative controls passed. It does not certify resource resolution, compilation, runtime state behavior, accessibility, gestures, RTL screenshots, complete widget parity, or whole-business-logic equivalence. Those gates remain open.

Run the permitted source check with `python3 docs/xml-layout-removal/check_compound_control_sources.py`. It writes `compound-control-source-results.json`, including exact source hashes, baseline layout hashes, exception evidence, negative-control detection, and any failures. The normal check never refreshes its expected mapping from changed production code.
