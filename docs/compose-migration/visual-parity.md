# Native visual parity fixtures

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

**Recovery status, 2026-10-14:** This report restores previously verified findings after a workspace reset. The final 96 PNGs, 48 JSON reports and their hash manifest were lost and **cannot currently be delivered or rechecked**. The historical results below do not validate the latest binding-removal or host changes. A fresh safe run and durable export are required before making a current visual-validation claim.

## Acceptance

On 2026-10-14, the user clarified that changes caused by newer Material components (for example an updated text-field appearance) are acceptable and 100% visual parity is not required. The user also explicitly permits changes to padding and margins to conform to Material 3. Accordingly, differences in raster output, font size/weight/color/spacing, decorative shape, and fixed header/bar geometry are recorded for review rather than treated as failures solely because they differ from the old widgets. This does not permit lost text, unreachable actions, clipped essential content, broken state/RTL behavior, or unusable contrast/accessibility.

`measurements.json` separates cosmetic `parityMismatches` from required `requiredFailures`. The tests continue to enforce actual non-flat captures, unchanged literal text, no newly introduced ellipsis where the reference exposes full text, visible enabled actions, 48 dp expanded touch targets, heading/control semantics, selected tab/order/RTL, and silent callbacks during capture. The strengthened shared-dialog tests also require enabled action-label contrast of at least 4.5:1 against their known composited surface. The screenshots and recorded overflow/geometry are also reviewed for clipping and contrast; these fixtures are not a comprehensive automated accessibility audit.

## Historical executed result and review

Before the reset, the contrast-fixed capture run on 2026-10-14 passed all six visual test methods, covering 48 cases. It produced 96 real PNGs and 48 JSON reports, with **zero required failures**. All **32 enabled shared-dialog action-label contrast checks passed**, ranging from 5.099:1 to 19.121:1 against their known composited surfaces. These were real framework renders and measured results, not reconstructed screenshots or inferred passes.

The former output directory was `validation/full-source/app/build/outputs/compose-visual-contrast-fixed/`, and the former runner log was `validation/reviewed-content-tests-1.log`. These identify the historical run; they are **not available artifact links** after the reset. That broader content batch was 370/382, with 12 failures outside these six visual methods. A later 399-test run had started, but its terminal result was lost and is **unknown**. Neither the earlier broader batch nor the later run may be described as an overall pass.

All 48 final reports and all 32 action ratios were inspected before the reset. The preceding corrected checkpoint's review opened 27 native and 8 reference images across all six surfaces, light/dark/Frost/Plus themes, phone/tablet widths, RTL, and font scale 2. A byte-for-byte comparison found only nine changed PNGs in the final contrast-fixed run: all eight default-Wg native images and the Plus-light NoDim native image. **All nine changed PNGs were opened and reviewed**; the other 87 images were byte-identical to the preceding checkpoint.

The verified historical capture visibly included:

- Both title/message and Cancel/Continue labels from the actual Wg Dialog window and its actual Material XML reference.
- The synthetic Support title in both CollapsingToolbar and native header.
- The reference navigation's settled active indicator and correctly mirrored five-tab order.
- Complete SponsorSuccess and Orbot text in the examined scale-2 phone/tablet views, with named/reachable actions. Support's lower content remained scrollable below the fixed action footer; this was not a claim that every lower row was visible in its initial screenshot.

No new essential-content clipping or lost action was found in those inspected views. Changed chip/text-field shapes, spacing, decorative card/button borders, and small title metric differences were within the user's accepted modern-Material cosmetic scope. Four SponsorSuccess scale-2 reports still recorded 40 px XML title size versus 39.771427 px native size; this did not lose text or block the action. The only other recorded final color differences were the deliberately more readable Plus-light dialog actions described below.

### Action-label contrast regression: historically fixed and verified

The preceding corrected screenshot review found default Wg buttons in fallback magenta (`#ff00ff`), giving only 2.826:1 contrast on the light `#f3f3f3` surface. It also exposed an inherited Plus-light limitation: the existing teal action accent on `#eaf7f5` was 2.930:1 in both baseline and NoDim.

The scoped shared-dialog fix used the selected theme's primary color when it satisfied 4.5:1 and its existing onSurface color otherwise. Final images showed the intended accents instead of magenta; both Plus-light variants used black action labels. The change was limited to enabled action-label readability, rather than changing the global palette or business logic.

| Dialog theme | Historically verified enabled-action contrast |
| --- | ---: |
| Light | 5.963:1 |
| Dark | 12.114:1 |
| Frost | 7.167:1 |
| Plus-dark | 5.099:1 |
| Plus-light | 19.121:1 |

The fixture checked each Cancel and Continue label in all eight configurations of each dialog variant: 32 assertions in total, all at or above 4.5:1. Every reported foreground and composited surface color was also verified to be present in its actual native PNG. Foregrounds and surfaces were composited using ColorUtils over the resolved opaque `android:colorBackground`. This was a targeted enabled-action contrast check, not a full-app WCAG audit or a measurement of real wallpaper/blur backgrounds.

### Lost artifact manifest and historical verification

The former `compose-visual-contrast-fixed/artifact-manifest.sha256` contained SHA-256 for every one of the 96 PNGs and 48 JSON reports: 144 files. All 144 entries were checked successfully before the reset. The retained SHA-256 of that manifest is:

`244a85c0728c7bcace654d4586b39904325daa84b8ee4b2d39c8ca27777a979d`

**This is historical verification only.** The manifest file, its individual entries and all 144 final artifacts are now lost. This single aggregate hash cannot recreate the files, serve as a replacement manifest, or establish that current source produces the same output. The PNGs and reports cannot be delivered from this record. No replacement screenshots or manifest entries have been invented.

### Source recovery and current validation boundary

The coordinator recovered the latest application/test source from remote checkpoint `1949435b20c49bb03ec06123c27f913a4974882b`, followed by work-in-progress checkpoint `87b3b9975afdb59492bc92cf97b9560cdd725330`. This report restores the later verified findings that had not been uploaded before the reset; it does not claim that those remote commits contain the lost output files.

New binding-removal and host changes are underway. The historical visual run **does not validate those latest changes**, their integration, or current host behavior. After the relevant changes settle, rerun the isolated fixtures, inspect the new captures, and persist both the artifacts and a newly generated per-file manifest outside the disposable workspace. Record the tested source revision and the actual terminal result. Do not reuse the old manifest hash for new output.

### Superseded capture history

The first `compose-visual-parity` run was not valid final evidence: all 16 Wg native screenshots were flat because Compose captureToImage selected the Activity instead of its platform Dialog. Detached XML references also omitted title/action drawing or posted indicator state, and Frost backdrops differed. Those defects were corrected using explicit actual-window PixelCopy, offscreen attached Material-compatible reference inflation, drained layout/drawable state, matching themed opaque backing, and expanded touch bounds.

The intermediate `compose-visual-corrected` checkpoint contained real images but predated the action-label contrast fix. The contrast-fixed run superseded both earlier checkpoints before the workspace reset. None should be presented as currently deliverable screenshot evidence unless the actual files are recovered and verified.

## Safety boundary

The fixtures never started the production Application, Activity, Fragment, service, VPN, traffic or reachability probe. They used `@Config(application = Application::class, sdk = [35])`, `@GraphicsMode(NATIVE)`, and the Compose test `ComponentActivity`. All screen input was default or explicitly synthetic. No permission request, account operation, checkout, logging export, email, or external navigation was invoked. Capture callbacks threw or counted accidental activation.

## What is rendered

`app/src/test/java/com/celzero/bravedns/ui/compose/visual/` contains:

- `VisualParityHarness.kt`: retained-XML inflation, real Android View raster capture, real Compose `captureToImage` capture (and explicit actual-window PixelCopy for the shared platform Dialog), PNG output and measured JSON evidence. PNGs are framework-rendered, not reconstructed drawings.
- `ContentVisualParityTest.kt`: Support, SponsorSuccess, Orbot information, and root bottom navigation.
- `WgAlertVisualParityTest.kt`: the actual shared `WgAlertDialogBuilder`, in default and `App_Dialog_NoDim` modes, compared with the Material XML confirmation dialog it replaces.

The baseline inflater uses `MaterialComponentsViewInflater`, as the original AppCompat/Material hosts do, so standard ImageView/TextView tags receive their original compatible implementations. Baseline screen views are attached to a test-only FrameLayout positioned outside the visible ComponentActivity window; this is required for CollapsingToolbarLayout's real title-painting path. No production host is created. Both reference and native screen captures have the same theme-resolved opaque `android:colorBackground` backing behind any transparent theme background; Frost blur or wallpaper compositing is not represented.

At the historical verification checkpoint, the three complete reference content layouts were byte-identical to the pinned baseline. Their SHA-256 values, checked before the reset on 2026-10-14, are retained below. This is not a fresh hash check of the latest host changes:

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

Targeted checks include diagnostic Support expanded-header height, required heading semantics, named attachment switch, and a visible 48 dp Send target; SponsorSuccess heading and a 48 dp Continue target reachable after scroll; Orbot named 48 dp dismiss action and description text-layout semantics; five root navigation tabs with exact selected state and 48 dp targets; shared-dialog visible enabled 48 dp actions with zero callback dispatch and at least 4.5:1 label contrast. The enabled SponsorSuccess button's resolved text color is compared as well as its typography. Target-size checks use `touchBoundsInRoot`, which includes Compose's minimum-target expansion; visual bounds alone are not an accessibility touch-target measurement.

The separate utility/network-sheet typography tests resolve XML appearances and native fonts. These fixtures add actual composed text-layout measurement and image evidence rather than replacing those tests.

## Running and reviewing

A fresh execution is required after recovery and the latest host changes. Run only through the coordinator's serial Gradle/test queue, using the existing project toolchain and dependencies. No dependency upgrade is required. The command below documents how to run the fixtures; it was not executed while restoring this report.

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

These limitations must remain visible in migration status and the final handoff. The isolated fixtures passed at the historical checkpoint, but that does not establish a blanket full-UI visual, behavioral, or accessibility pass. With the final artifacts lost and new host changes underway, current visual validation remains pending.
