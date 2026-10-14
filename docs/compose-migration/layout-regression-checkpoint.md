# Layout regression checkpoint — uncompiled WIP

This checkpoint addresses the reported floating/translucent bottom navigation and
lost Home section separation. It does not complete the broader component-style
restoration or establish runtime UI correctness.

- A measured sibling viewport reserves the full navigation bar and bottom system
  inset. The bar paints opaque RGB even in the Frost theme, without corner holes.
- Home card compatibility gutters and section edge spacing are expressed as native
  Compose padding. Home's duplicated 80dp navigation spacer is removed.
- Home-hosted About, Configure, Statistics and RPN subscription content remove only
  the legacy navigation allowance; standalone padding remains. Configure retains
  safe-drawing handling and Statistics/Configure retain their 10dp content gap.
- RPN FAB/list clearance uses actual overlap rather than adding a second bar height.
- Existing navigation IDs, callbacks, FragmentManager state and business logic are
  retained. No ViewBinding, XML UI inflation or new service/permission use is added.

The navigation and Home spacing source guards pass, including deliberate negative
mutations. Synthetic geometry, opacity, nested ComposeView, lifecycle and RTL/large
text test cases are added but have not been compiled or run. Android tooling and
cached dependencies were lost during workspace restart; the existing artifact
access denial remains unresolved. No download retry or alternate route was used.

The older binding-removal safety manifest describes its own previous source
snapshot. Its source hashes are stale for this checkpoint and its runtime gate
remains BLOCKED. A new independent source/artifact review and regenerated exact
pins are required before any isolated test execution. Do not use prior historical
test passes or source guards as evidence that this layout has rendered correctly.

The ongoing original chip/button/field restoration is checkpointed separately.
