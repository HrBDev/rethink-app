# Native visual parity fixtures

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

## Acceptance

On 2026-10-14, the user clarified that changes caused by newer Material components (for example an updated text-field appearance) are acceptable and 100% visual parity is not required. Accordingly, differences in raster output, font size/weight/color/spacing, decorative shape, and fixed header/bar geometry are recorded for review rather than treated as failures solely because they differ from the old widgets. This does not permit lost text, unreachable actions, clipped essential content, broken state/RTL behavior, or unusable contrast/accessibility.

`measurements.json` separates cosmetic `parityMismatches` from required `requiredFailures`. The tests continue to enforce actual non-flat captures, unchanged literal text, no newly introduced ellipsis where the reference exposes full text, visible enabled actions, 48 dp expanded touch targets, heading/control semantics, selected tab/order/RTL, and silent callbacks during capture. The screenshots and recorded overflow/geometry are also reviewed for clipping and contrast; these fixtures are not a comprehensive automated accessibility audit.

## Status

The fixture sources compiled and the first real coordinator run executed all six visual test methods on 2026-10-14. It wrote 48 artifact groups (96 PNG files and 48 JSON reports). All six visual methods failed: measured differences remain, and the run also exposed capture-fixture defects described below. Corrections to the fixtures and scoped production fixes are pending a serial rerun. This document does **not** claim that visual parity passes or that all UI has been compared.

First-run artifacts are under `validation/full-source/app/build/outputs/compose-visual-parity` in the task workspace, with the full runner log at `validation/full-source-content-visual-tests.log`. The PNGs were opened and inspected, rather than counting generated files as proof of correctness.

The first run rejected all 16 Wg native captures as flat-color images. `captureToImage` selected the Activity window because a plain ComposeView hosted in a platform Dialog does not expose Compose's DialogWindowProvider. The fixture now requests PixelCopy from the **actual Dialog window**. The reference's action labels were also absent in the first detached raster despite measured text layouts; its actual XML panel is now attached offscreen and allowed to settle before rasterization. These action regions must be checked in the rerun. Other fixture corrections use MaterialComponentsViewInflater for baseline AppCompat widgets, attach baseline content outside the visible test window so the real CollapsingToolbar title painter runs, put the same theme-resolved opaque `android:colorBackground` backing behind transparent Frost content, and measure actual expanded touch bounds rather than visual icon/button bounds. Those corrections fix the capture mechanism. Separately, the later user acceptance above changes cosmetic equality from a hard gate to recorded review evidence.

Initial measured findings, to be reconciled against the corrected rerun:

- Support: four bold labels resolved to XML weight 800 versus native 700; attachment/privacy paragraph steps lost the XML 3 dp extra spacing.
- SponsorSuccess at font scale 2: title 40 px versus 39.771427 px; message baseline step 39 px versus 55 px. Initial enabled Continue text colors also differed, and that comparison is now asserted explicitly.
- Orbot information at font scale 2: description baseline step 35 px versus 43 px.
- Shared Wg alert message: XML 14 px versus native 16 px at normal scale, with color/letter-spacing differences. Typography measurements were real even though the original native screenshot selected the wrong window.
- Root navigation: XML selected-label weight 800 versus native 500; XML labels stay 12 px at font scale 2 while native labels grew to 24 px. The installed Material 1.14.0 implementation explicitly uses `setTextAppearanceWithoutFontScaling`/`MaterialResources.getUnscaledTextSize`, so this is a real reference contract, not an assumed screenshot-size normalization.

The fixtures never start the production Application, Activity, Fragment, service, VPN, traffic or reachability probe. They use `@Config(application = Application::class, sdk = [35])`, `@GraphicsMode(NATIVE)`, and the Compose test `ComponentActivity`. All screen input is default or explicitly synthetic. No permission request, account operation, checkout, logging export, email, or external navigation is invoked. Capture callbacks throw or count accidental activation.

## What is rendered

`app/src/test/java/com/celzero/bravedns/ui/compose/visual/` contains:

- `VisualParityHarness.kt`: retained-XML inflation, real Android View raster capture, real Compose `captureToImage` capture (and explicit actual-window PixelCopy for the shared platform Dialog), PNG output and measured JSON evidence. PNGs are framework-rendered, not reconstructed drawings.
- `ContentVisualParityTest.kt`: Support, SponsorSuccess, Orbot information, and root bottom navigation.
- `WgAlertVisualParityTest.kt`: the actual shared `WgAlertDialogBuilder`, in default and `App_Dialog_NoDim` modes, compared with the Material XML confirmation dialog it replaces.

The baseline inflater uses `MaterialComponentsViewInflater`, as the original AppCompat/Material hosts do, so standard ImageView/TextView tags receive their original compatible implementations. Baseline screen views are attached to a test-only FrameLayout positioned outside the visible ComponentActivity window; this is required for CollapsingToolbarLayout's real title-painting path. No production host is created. Both reference and native screen captures have the same theme-resolved opaque `android:colorBackground` backing behind any transparent theme background; Frost blur or wallpaper compositing is not represented.

The three complete reference content layouts remain byte-identical to the pinned baseline. Their SHA-256 values, checked on 2026-10-14:

| Retained reference | SHA-256 |
| --- | --- |
| `app/src/main/res/layout/activity_customer_support.xml` | `d5f2f1cbd55c5b767ebbf5759e23b6360bfc518ed33edc80d5c5d86b3c898e1c` |
| `app/src/full/res/layout/fragment_sponsor_success.xml` | `194217acae20a8d206a9e10277b8a71eed5e099704a1271d034dd4a22ac581f3` |
| `app/src/full/res/layout/dialog_info_rules_layout.xml` | `003bbf0ab1175cec5a36f616e5d6f210ff74589beb685a1255cd4226a47e502b` |

Root navigation is deliberately isolated from `activity_home_screen.xml`: inflating that entire baseline would instantiate a real navigation host. The fixture constructs only `BottomNavigationView` with its theme's Material3 defaults, labeled mode, baseline `nav_menu_background`, baseline `bottom_nav_menu`, and Home selected. This is a reference-widget comparison, not a capture of the full baseline Activity. The menu SHA-256 is `1b951b5ba7d37d20173d9aa8ad886eed6b126913a077321c467bf18b81673d0e`; the drawable SHA-256 is `4ac5cd6c707597b3b32a104e8b1297c40073c43f5fc2f128e4c749c306851901`.

The shared-dialog reference invokes the installed Material library's actual XML view construction with synthetic title/message/buttons. It creates but never shows the reference Dialog; only its content panel, attached to the offscreen test reference host to settle layout/drawable state, is rasterized. The Compose replacement is shown in the test ComponentActivity. Both use the same theme and a content viewport at most 600 dp wide; reference measurement uses the native root's measured size. This normalizes outer platform-window sizing and does not validate screen dimming visually, platform insets, shadow, window animations, or dialog-width policy. The NoDim fixture also checks actual `dimAmount == 0`.

## Configuration matrix

Each of the six test methods runs the following eight cases. This is targeted pairwise risk coverage, **not** a full cross product.

| Case | Theme | Width | Direction | Font scale |
| --- | --- | ---: | --- | ---: |
| light_narrow_ltr_f1 | AppThemeWhite | 360 dp | LTR | 1 |
| light_wide_rtl_f2 | AppThemeWhite | 800 dp | RTL | 2 |
| dark_wide_ltr_f1 | AppTheme | 800 dp | LTR | 1 |
| dark_narrow_rtl_f2 | AppTheme | 360 dp | RTL | 2 |
| frost_narrow_rtl_f1 | AppThemeTrueBlackFrost | 360 dp | RTL | 1 |
| frost_wide_ltr_f2 | AppThemeTrueBlackFrost | 800 dp | LTR | 2 |
| plus_light_wide_rtl_f1 | AppThemeWhitePlus | 800 dp | RTL | 1 |
| plus_dark_narrow_ltr_f2 | AppThemeTrueBlackPlus | 360 dp | LTR | 2 |

Screen captures use an 800 dp-high content viewport at mdpi, without production system-bar or IME state. Root navigation uses its natural height. The content fixture uses `Density(context)` so Android 35 non-linear font scaling is preserved rather than replacing it with a linear test-only density. English strings are kept stable while layout direction changes; this tests RTL mirroring, not Arabic/Hebrew translation or bidi text shaping. Shared-dialog width is capped at 600 dp by the fixture as described above.

## Evidence and assertions

Every completed case writes explicitly labeled `baseline-xml.png`, `native-compose.png`, and `measurements.json`. The JSON includes:

- baseline commit and exact configuration;
- actual image dimensions and diagnostic changed-overlap-pixel count / mean absolute channel delta;
- actual XML `TextView` size, resolved typeface weight, color, letter spacing, measured text width, height, line count and baseline-to-baseline step;
- actual Compose `GetTextLayoutResult` values and semantic bounds, including visual overflow;
- explicit parity mismatches and tested layout/accessibility conditions.

Typography review flags font-size differences over 0.1 px, letter-spacing differences over 0.05 px, resolved-typeface-weight/color differences, and multi-line baseline-step differences over 1 px (Android baseline rounding). These measurements do not fail a run solely for a cosmetic change. There is no screenshot-match-percentage gate. XML/Compose raster differences, including antialiasing and platform insets, are reported rather than misrepresented as pixel-identical.

The fixture records all typography differences within a case and continues all cases before asserting the required rendering/content/accessibility checks. Capture or accessibility-tool errors still fail immediately; they must not be reported as successful comparisons.

Targeted assertions include Support expanded-header height, heading semantics, named attachment switch, and a visible 48 dp Send target; SponsorSuccess heading and a 48 dp Continue target reachable after scroll; Orbot named 48 dp dismiss action and description text-layout semantics; five root navigation tabs with exact selected state and 48 dp targets; shared-dialog visible enabled 48 dp actions with zero callback dispatch. The enabled SponsorSuccess button's resolved text color is compared as well as its typography. Target-size checks use `touchBoundsInRoot`, which includes Compose's minimum-target expansion; visual bounds alone are not an accessibility touch-target measurement.

The separate utility/network-sheet typography tests resolve XML appearances and native fonts. These fixtures add actual composed text-layout measurement and image evidence rather than replacing those tests.

## Running and reviewing

Run only through the coordinator's serial Gradle/test queue, using the existing project toolchain and dependencies. No dependency upgrade is required.

```sh
RETHINK_VISUAL_OUTPUT="$PWD/app/build/outputs/compose-visual-parity" \
  ./gradlew :app:testFdroidFullDebugUnitTest \
  --tests 'com.celzero.bravedns.ui.compose.visual.ContentVisualParityTest' \
  --tests 'com.celzero.bravedns.ui.compose.visual.WgAlertVisualParityTest'
```

`RETHINK_VISUAL_OUTPUT` or JVM property `rethink.visual.output` can select a different local output directory. Otherwise the test process writes under `build/outputs/compose-visual-parity`. It is intentionally not a checked-in screenshot golden. A complete run is 48 comparisons, 96 PNGs and 48 JSON reports. Measured cosmetic differences are review evidence, while rendering/content/accessibility failures require investigation.

After execution, inspect the PNGs, distinguish acceptable modern-Material cosmetics from lost content/actions or clipping/contrast/accessibility regressions, report actual findings with file/case identifiers and measured values, and rerun after scoped fixes when needed. Artifacts in build directories are not automatically persistent PR artifacts; copy/archive them explicitly if the coordinator's deliverable requires it.

## Exclusions and limits

No full Home screen, settings root, app list, app details, traffic log, DNS editor, purchase flow, network sheet, RPN screen, or other migrated surface is covered by these image fixtures. Root bottom navigation is the only root/Home surface in this set. Wg single-choice/items/icon/neutral-button variants are excluded. Support is captured in its initial expanded state; lower attachment layout can be measured outside the initial viewport, but no second scrolled screenshot is claimed. SponsorSuccess is captured before the accessibility scroll. Keyboard, system bars, edge-to-edge insets, production host lifecycle/recreation, live status transitions, focus traversal, screen-reader announcements, OEM rendering, and on-device graphics are not validated.

These limitations must remain visible in migration status and the final handoff. Passing these isolated fixtures, if established, would not establish a blanket full-UI visual, behavioral, or accessibility pass.
