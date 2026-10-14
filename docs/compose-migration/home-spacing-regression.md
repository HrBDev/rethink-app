# Home section separation correction

Status: source-only correction, **uncompiled and runtime-unverified**. No Gradle,
production Home controller, Application, VPN, network traffic, account, permission
or export flow was executed for this correction.

## Baseline cause

Baseline `23cb6dd98805445174bd83b7237ad5f5d2c24ac9` uses
`Widget.Rethink.MaterialCardView` with `cardUseCompatPadding=true` and 2dp elevation;
the Frost variant retains compat padding with 0dp elevation. Home's connection
cards have 24dp corners; the remaining section cards have 18dp corners, and the
rules card explicitly overrides elevation to 0dp. Phone sections mostly have
zero explicit vertical margins because their card backgrounds are already inset.

The first Compose renderer painted each card to its full allocated bounds and
therefore dropped that separation. The correction reads only those existing
theme attributes, calculates the rounded-card outside clearance in physical pixels,
and adds native Compose padding before drawing the surface. It does not inflate or
instantiate a legacy View. Insets remain independent of font scale; text can grow.

The original phone 2dp row edges and wide 2dp/8dp/10dp column margins are restored,
alongside the existing 12dp wide vertical gaps and the DNS/throughput,
rules/proxy, logs and apps grouping. The Activity shell now owns navigation
clearance, so Home no longer adds a second 80dp bottom gap. Its 32dp scroll-end
space and 20dp control-cluster bottom margin remain.

## Checks and limits

- `python3 scripts/compose-migration/check-home-section-spacing.py --self-test`
  checks the baseline resource evidence, explicit native insets, section margins,
  grouping and absence of a duplicate navigation spacer. Nine negative mutations
  demonstrate the source guard catches the named regressions.
- `python3 docs/compose-migration/check_home_controller.py` checks protected
  controller source equivalence, including the synthetic-test startup fence.
- `HomeSectionSpacingTest` adds five pending tests: radius/elevation/pixel-rounding
  calculations; phone surface gaps/grouping; phone RTL at 2x font scale; Frost
  zero-elevation gutters; wide RTL at 2x font scale. Fixtures use plain Application,
  synthetic presentation and local callback recorders only.

These are not pixel-parity, accessibility, screenshot or runtime pass claims.
The new Kotlin source and bounds tests still require compilation and execution in
the approved isolated toolchain. Existing six HomeContentTest cases are unchanged.
