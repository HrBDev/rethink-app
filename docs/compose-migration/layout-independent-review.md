# Independent Home/navigation source review

Reviewed 2026-10-14 against recovered baseline
`23cb6dd98805445174bd83b7237ad5f5d2c24ac9` and the local changes after remote checkpoint
`e2b97de59cc854de74010c096ceffbaed6cb4a55`.

**Gate: source review passes for the corrections below; compilation, isolated tests,
visual comparison and device integration remain BLOCKED / UNRUN.** This is not a
complete migration, build, runtime, accessibility or visual-parity approval.

## Findings and corrections

1. The previous overlay Box let the FragmentContainerView occupy the navigation
   rectangle. The new Column measures navigation first and assigns the remaining
   height to a clipped destination viewport. The existing AndroidView bridge receives
   `fillMaxSize()` within that viewport. No fixed navigation-height spacer is used.
2. Navigation's system-bottom padding now belongs to its painted surface, and its
   corner cut-outs are removed. An independent review found that Frost's actual
   `navBarColor` is `#AA1e1e1e`, so directly using the token was still translucent and
   contradicted the new opacity test. The nav-local color now preserves the theme RGB
   while forcing alpha to one. Shared palette tokens and other Frost surfaces are unchanged.
3. Home's missing card gutters are supported by baseline XML and styles, rather than
   guessed uniform margins: compat padding is enabled; standard max elevation is at
   least 2dp, Frost is 0dp, the rules card explicitly overrides elevation to 0dp;
   connection cards use 24dp radii and section cards use 18dp. The new helper rounds
   CardView-style horizontal/vertical outside clearance in physical pixels. Phone
   2dp edge margins and wide 2dp/8dp/10dp margins match the baseline resources.
4. RPN clearance now compares the destination's window-space bottom with the
   navigation anchor's top. An already-reserved navigation rectangle contributes
   zero additional padding. View layout and navigation bounds are observed, and both
   listeners are removed with the view lifecycle. The existing 16dp FAB and 24dp
   scroll gaps remain. Standalone hosts retain their platform navigation inset fallback.
5. The source-only navigation guard still verifies 25 unchanged Home controller
   methods, startup order, selection/reselection, Back, graph and saved host boundaries.
   The geometry change does not recreate or replace the stable Fragment host. This
   does not establish actual FragmentManager restoration or rapid-tap timing behavior.

## Root-destination clearance audit

The Home shell supplies a false-by-default composition policy only within its
destination subtree. The inherited-policy mechanism has a newly added synthetic
AndroidView-to-ComposeView test; that test is **unrun**, so actual propagation through
the Fragment/NavHost hierarchy still requires isolated integration verification.
Only bottom insets already reserved by Home are consumed; standalone safe-drawing
behavior, top/side insets and the navigation surface's own inset remain intact.

- Home: old 80dp nav allowance removed. Baseline Home root reserved 80dp for the
  overlay bar. Its separate 32dp scroll-end space and 20dp controls-bottom margin remain.
- Statistics: Home-hosted 90dp allowance becomes 10dp, retaining the residual content
  gap after the legacy 80dp bar allowance. Standalone 90dp remains unchanged. Evidence:
  baseline `fragment_summary_statistics.xml`, outer content marginBottom=90dp.
- Configure: same 90dp-to-10dp Home-only policy. Its `safeDrawingPadding()` remains;
  Home consumes the already-reserved bottom inset upstream. Evidence: baseline
  `fragment_configure.xml`, outer content paddingBottom=90dp.
- About: Home-hosted 60dp overlay allowance becomes zero; standalone 60dp remains.
  About's footer spacing is untouched. Evidence: baseline `fragment_about.xml`, root
  marginBottom=60dp, separate internal footer margins/padding.
- Premium RPN: only the !hosted 60dp CTA allowance uses the Home policy. The
  `FragmentHostActivity` hosted branch remains zero. Baseline premium XML explicitly
  reserves 80dp under its CTA; standalone setup removes that allowance. Existing
  CTA spacing and terms padding remain unchanged.
- F-Droid RPN: Home-hosted 60dp overlay allowance becomes zero; standalone 60dp and
  the ordinary 24dp content padding remain.
- Manage purchase: 176dp scroll padding and 96dp signature offset were initially
  suspected but are **retained**. Actual launch sites use `FragmentHostActivity`, not
  Home's graph, so this shell does not demonstrate duplication there. Exact baseline
  signature positioning is a separate, pre-existing parity question.
- RPN selection: only actual overlap is removed as described above. Its synthetic
  renderer still retains its additional content/FAB space; no blanket padding deletion.

## Verification and test boundary

Executed source checks:

- `check-navigation-chrome.py --self-test`: PASS, 25 protected Home methods and
  19 rejected negative mutations, including opaque Frost/nav color, viewport
  structure, inherited Home-only policy and bottom-inset consumption.
- `check-home-section-spacing.py --self-test`: PASS, 12 contracts and 9 rejected mutations.
- `check_home_controller.py`: PASS, 145/145 source checks.
- `git diff --check`: PASS.

New/extended tests remain uncompiled and unrun. Root chrome fixtures use plain
`Application`, synthetic Compose content, framework-only FragmentContainerView,
AndroidView/ComposeView and local callback recorders. They do not construct a production
Activity, Fragment controller, NavHost, account, database, VPN or traffic source.
The nested-view test checks the Home policy reaches a second composition while an
ordinary standalone caller keeps its legacy padding. It is not a process-restoration test.
The Home spacing fixtures likewise use synthetic presentation state and no production
controller. These source observations do not approve a test launcher or invalidate its
existing fail-closed runtime gate.

No Gradle, toolchain download, device test, production Application or external/remote
write was performed by this review. The absent approved toolchain and previously denied
download route were not bypassed.

## Remaining integration/visual risks

- Verify the inherited policy and consumed insets through the actual stable
  AndroidView/FragmentContainerView/NavHost/nested ComposeView hierarchy.
- Run zero/gesture/three-button insets, resize/rotation/recreation, repeated navigation
  and Back, RPN FAB/list end visibility, all six themes, RTL and large-font fixtures.
- Home's existing wide two-column renderer still does not implement the baseline
  logs-to-apps bottom barrier. Card minimum heights and contents can also differ from
  fixed baseline XML heights. The reviewed gutter correction does not claim to solve
  that pre-existing full-layout parity gap.
- Compile all affected variants and inspect screenshots/accessibility before declaring
  the regressions visually verified. Static guards cannot prove Kotlin API validity
  or the pixel assertions' runtime results.
