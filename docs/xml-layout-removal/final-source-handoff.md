# Compose source migration and XML removal: validation-limited handoff

**Current source is uncompiled and not production-ready.** No current APK, device,
rendering or accessibility result is claimed. The finite source implementation,
original-style reconciliation and app-owned XML-removal work are recorded here;
runtime acceptance remains a separate blocked step.

## Scope completed in source

- Original baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9` on
  `gradle-improvements`. Incremental destination: `wip/compose-ui-migration`.
- The final stage removes 186 original app layout files (185 resource names),
  including the wide Home qualifier. The four previously removed host/loading
  layouts bring the original total to 190. Six remaining-layout entries were
  verified original orphans/unopened owners; no fictitious screen replaces them.
- Every removed layout retains its original hash, recoverable Git path, complete
  node/style contract and concrete renderer-family or retired-owner mapping.
  `layout-removal-manifest.json` is the current disposition. The exhaustive
  preflight inventory and styling ledger remain historical evidence, including
  their explicit limitations; family mapping is not per-node runtime proof.
- Generated ViewBinding/DataBinding usage and build features are removed.
  Strict gates scan all source sets, tests, qualifiers, reflective/inflation
  entry points and non-layout resources. Values-only IDs preserve all 2,516
  original layout-declared names/types; numerical AAPT IDs are not promised stable.
- Original chip, button and field styles are resolved from the pinned source,
  including explicit nulls, theme overlays and state selectors. The purple
  TextButton regression came from skipping MaterialThemeOverlay, which supplies
  the transparent container; it was not an intentional purple redesign.
- Search geometry, hint/plate variants and input flags; DNS and Stats custom
  grouped controls; anchored opaque navigation; Home section spacing/wide grouping;
  original cards, rows, tabs, rule/DNS dialog details, compound widgets, sliders,
  WireGuard/RPN text and progress contracts have targeted source corrections.
- Source compound census: 154 nodes in 52 layouts, 151 active nodes mapped to
  152 render paths, three independently checked inactive exceptions. Native
  drawables paint on Compose Canvas without constructing widget Views. The
  slider drag visual-state correction keeps original callbacks unchanged.
- Independent programmatic baseline reference factories replace all app-layout
  inflations in tests. They use original widget/value expectations rather than
  the Compose implementation as its own oracle. Newly authored tests remain UNRUN.

## Remaining interoperability

FragmentContainerView/AndroidView bridges retain fragment identity, saved state
and navigation ownership while their visible child UI is Compose. AndroidFragment,
programmatic navigation/window containers, ComposeView attachment, Dialog and
BottomSheet window shells remain. System-owned permission, notification, toast,
biometric and settings flows are Android framework boundaries. The invisible
notification trampoline uses an empty Compose root. No app-owned platform layout
exception remains. MDC progress specifications and MaterialShapeDrawable are
non-View models/drawables, explicitly distinguished from native widget constructors.

Dormant original custom View declarations still retained by the repository are
not instantiated by app rendering; the source guard rejects new references to
them, except the reviewed histogram Entry data object. Dependency-internal Android
layouts are not app-owned files and were not removed.

## Evidence and limits

Run `python3 scripts/compose-migration/check-final-source.py` for the complete
explicit source-only matrix. `final-source-checks.json` records each command and
source hash. Separate final binding and zero-layout reports retain their mutation
controls. Targeted review documents identify the exact files and remaining gaps.
The final matrix passes **42/42 commands**, including all strict removal gates.
The binding scan covers 798 production source files in five source sets, with
zero retained layouts and zero binding/rendering violations. Its 50 in-memory
controls and the independent layout gate's 52 controls pass.
The protected-core check verifies 522 unchanged logic files and original dependency
pins; controller guards compare bounded method bodies and boundary calls. These
checks are regression tripwires, not a compiler or universal business-logic proof.

Source-layout obligations and fixed-size/minimum-size accessibility differences
remain observable runtime questions. Card shadows/stateful paints, text metrics,
slider animations/gestures, IME flags/focus, RTL, enlarged fonts, back/state
recreation, all-route content/actions and TalkBack require execution. No source
gate is presented as a rendered or interaction pass. Earlier test/capture results
belong to older source and do not validate this checkpoint.

## Blocking prerequisite and finite next validation plan

The workspace reset removed Java17/Android SDK/Gradle caches. The explicit policy
denial for `https://dl.google.com:443` remains; no retry or alternate artifact route
was attempted. Current build/resource/Android-test execution is blocked. No VPN,
real traffic, billing, production Application or account/permission flow was run.

When toolchain/artifact access is legitimately available:
1. Verify the original pinned toolchain/dependencies locally; compile baseline
   and this exact source for F-Droid Full and TV, then Play and Website variants.
   Run resource linking and lint without dependency upgrades.
2. Independently refresh the exact isolated test-source/launcher/artifact safety
   manifest. Preserve plain Application, synthetic data/fakes, no production
   backend, exact class filters and teardown. Existing BLOCKED manifests must
   remain blocked until reviewed; do not turn them on merely to make tests run.
3. Run the complete host/content regression suites to a captured terminal result.
   The historical final content run lost its terminal result and must be rerun.
4. Render and inspect original-source/Compose comparisons for navigation/Home,
   search, TextButtons, DNS/Stats custom controls and final renderer families in
   light/dark, supported themes, RTL and large text. Verify content/actions,
   contrast, clipping, disabled/selected/pressed states and gesture behavior.
5. Exercise safe synthetic navigation, back/search/filter/state recreation and
   accessibility flows. Keep real VPN, user traffic, account and security-sensitive
   actions outside this test work. Save reports/captures with the exact commit.

This handoff stops at the attainable source checkpoint. It does not authorize a
PR, merge or deployment, or describe the migration as fully runtime-validated.
