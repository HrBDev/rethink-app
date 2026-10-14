# XML layout removal: test, resource and safety boundary plan

Planning snapshot before implementation. See `test-dependencies-implementation.md` for the subsequently authorized source-only test changes and exact current verification limits.

Source-only plan, 2026-10-14. No production/test code, existing guards, layout resources, Gradle files, or runtime-clearance manifest were changed for this audit. No Gradle, Android, device, production Application, VPN, traffic, account, permission or network operation was performed.

## Decision and immediate blockers

All 186 remaining app-owned layout XML files can ultimately be removed from source and packaging, but deletion alone is not compile-safe or acceptance-safe. The dependencies requiring deliberate replacement are:

- 12 direct `R.layout` references in five JVM test classes, spanning five baseline resources. Their helper dependency is `VisualParityHarness.inflateBaseline`. These references participate in 13 test methods (the statistics helper serves two methods).
- 2,516 distinct ID declarations currently exist only in those layouts. Preserve **all 2,516 names and `id` resource types in ordinary values XML**, with existing non-layout IDs untouched. Of these, 137 have production source consumers in 24 Kotlin files; 22 are used by tests in 12 Kotlin files; eight are test-only. The union of directly consumed layout-only IDs is 145. Values-only declarations are not Views, layout resources, or retained binding.
- Two guards currently demand physical unchanged baseline layout files. One further guard accepts retained layouts rather than forbidding them.
- Whole-layout original-style equivalence still has independent gaps, notably wide Home's log-bottom barrier, other MaterialCardView compat padding, fixed versus minimum heights, compound controls, progress and layout-wide constraints. Deletion is gated on that separate mapping/correction review; successful test-reference migration does not establish equivalence of all 186 layouts.
- Runtime remains BLOCKED. The exact current manifest records 84 classes / 598 methods and an `app/src` envelope of 1,576 files, SHA-256 `b99f1104df77f5b3c754fc64a6e4a0ee7c6377193e8f9842795a3e0df1d6a547`. None of these are newly executed results.

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
Frozen implementation snapshot: `803cd4c884243ab9ef9c92cb0c4959d63bc27b00`.
The independent inventory confirms every remaining layout byte-identical to the baseline: 114 main/unqualified + 71 full/unqualified + one full/w600dp = 186 files, 185 resource names.

## Reproducible evidence

- `test-dependencies-audit.py`: documentation-only census, no Gradle or Android imports. Reads source and emits only these documentation reports.
- `test-dependencies-inventory.json`: every direct layout call site; every compile-relevant layout-only ID declaration and current consumer; remaining layout hashes; exact existing safety fence; string-interpolation review cases.
- `test-dependencies-preserve-all-ids.txt`: the full 2,516-name preservation target.
- `test-dependencies-preserve-production-ids.txt`: the 137-name compile-critical subset, not a recommendation to delete other IDs.
- `layout-inventory.json`: sibling inventory of every current layout and its original node/style/property data.

The census masks comments and literals, handles the app's actual R imports and fully qualified names, includes simple Kotlin `${R.id...}` interpolation, and separately reports eight resource-looking string literals for manual review. Those eight are navigation tags or the existing BlockFree chip tag, not hidden layout lookups. There are no static imported `R.id.*`/`R.layout.*` members requiring alias resolution. Counts are source facts, not AAPT validation.

## Reference strategy: independent source contract, real Android reference widgets

Use test-only, typed programmatic reference factories for the five resources below. They instantiate the same framework/AppCompat/Material widget classes required by the assertion, apply original defaults and explicit attributes, and construct only the three small reference screen hierarchies that existing capture tests actually need. No factory is callable from production.

Preserve the exact original source as Git history, not as copied resources. For each factory, check in a small immutable **expected-property contract** with baseline commit, original resource path and SHA-256, widget class, source style chain, property values/resource references and relevant ancestor geometry. Typed Kotlin constants or JSON style expectations are legitimate; an XML string, renamed full layout, serialized generic View tree, general-purpose XML-to-View interpreter or Gradle task rebuilding deleted layouts is not.

The test oracle must not call `utilityTextStyle`, `resolveOrbotInfoTypography`, `resolveOriginalControl`, `resolveFieldResources`, current Compose state classes or renderer sizing helpers to compute its expected value. Those remain the implementation under test. Factory construction uses original resource/style contracts, Android widget APIs and independent resource resolution. Do not import the production `originalControlSpecs` catalog to generate both sides of an equality.

Theme/drawable/string/dimension XML are intentionally retained. They remain valid inputs to Android and Compose, but the source-backed oracle must verify the baseline values/style ancestry and selectors it depends on. Otherwise changing a shared resource could move both expected and actual results together. Preserve explicit nulls, disabled/checked/focused/pressed selector precedence and baseline library/framework defaults; mark unresolved defaults for actual runtime comparison rather than inventing hardcoded values.

To reproduce XML style precedence, use widget constructors with independently built original style attributes and explicit overrides, or independent application of original resolved style data. Never use layout inflation. Verify the selected `AttributeSet`/constructor APIs against the approved installed Robolectric/Material artifacts when compilation becomes authorized. Plain `Chip(context)` or plain `TextView(context)` is insufficient when the old XML supplied a style or text appearance.

An important existing distinction must survive: the utility/Orbot/statistics tests call the ordinary LayoutInflater, while the visual harness installs `MaterialComponentsViewInflater`. Unqualified sponsor `TextView` instances are platform reference TextViews in utility tests and Material-compatible TextViews in the capture path. The new factories should make that reference-widget mode explicit rather than silently sharing a different default constructor.

For full-screen captures, programmatic references must retain real framework text rasterization, spacing, card and toolbar behavior. Do not replace a valid reference capture with a blank bitmap, single TextView, Compose screenshot from the same renderer, hand-painted image or a list of tags. If a hierarchy cannot yet be faithfully reproduced, report that evidence as blocked and keep its acceptance gate unresolved.

## Five-resource replacement plan

Proposed helper namespace: `app/src/test/java/com/celzero/bravedns/ui/compose/reference/`. Files/classes below are proposed, not created. Typed return objects avoid dependence on lookup-only resource IDs even though all historical IDs are preserved.

### 1. `app/src/full/res/layout/fragment_sponsor_success.xml`

Factory: `BaselineSponsorSuccessReference.create(context, widgetMode)` returns `root`, `title`, `message`, `continueButton` and `emblem`.

Source contract:
- Vertical, center-gravity root with 32dp padding; original wrap/match sizing and child order.
- 140dp square MaterialCardView with 70dp radius, zero elevation/stroke, `colorSurfaceContainerHigh`; centered 80dp `dns_icon` emblem with `accentGood` tint and decorative accessibility exclusion.
- Title: original HeadlineLarge text appearance, bold, `colorPrimary`, 40dp top margin, original `sponsor_thank_you` text.
- Message: BodyLarge, `colorOnSurfaceVariant`, center gravity, 6dp line-spacing extra, 16dp horizontal padding/top margin, original body text.
- Button container: 56dp top margin; full-width 64dp MaterialButton, original OutlinedButton style, `rectangle_border_background`, TitleMedium appearance and `sponsor_continue` text.
- Do not reinterpret fixed sizes as minimum sizes in the reference. The independent style-equivalence review decides any necessary Compose accessibility treatment.

Consumers to change:
- `UtilityTypographyTest.sponsorSuccessTitleMatchesTheRealXmlTextView` (line 37): reference factory title; retain size/raw-sp, letter-spacing, weight and italic assertions through `assertTypography`.
- `sponsorSuccessBodyMatchesXmlBodySizeAndSpacing` (line 48): reference message; retain font-metrics-derived em line height and 0.002 tolerance.
- `sponsorLargeFontPreservesRawSpAndRenderedLineStep` (line 85): build and measure the complete same reference root at 800x800 and fontScale 2; retain title tolerance 0.51px and line-step tolerance 1px. A detached TextView with a new width is not equivalent.
- `ContentVisualParityTest.sponsorSuccessCapturedAgainstRetainedXmlAcrossConfigurations` (line 104): Material-compatible factory; preserve all eight `visualCases`, attached offscreen rendering, title/message/button text measurements, heading semantic, Continue scroll reachability, action, and 48dp touch-target check.

### 2. `app/src/main/res/layout/activity_customer_support.xml`

Factory: `BaselineSupportReference.create(context, widgetMode, syntheticHeader, syntheticSubtitle)` returns `root`, `collapsingToolbar`, `heroSubtitle` and typed/named section/attachment labels.

The reference capture requires the full original hierarchy, not merely the two typography labels:
- CoordinatorLayout; original background/fitsSystemWindows; AppBarLayout with original hero gradient/elevation.
- 180dp CollapsingToolbarLayout, original expanded/collapsed appearances, scrim, title margins, scroll flags, MaterialToolbar actionBarSize and pin mode. Keep real collapsing-title drawing through offscreen attachment.
- Hero subtitle: original mini dimension, primary text color with 0.7 alpha, sans-serif, one line/end ellipsis, 24dp side and 18dp bottom margins, parallax mode/multiplier.
- NestedScrollView including fillViewport, clipping, overscroll, 160dp bottom clearance, AppBar scrolling behavior; inner padding 16/16/16/112dp.
- Section labels: TitleSmall appearance, bold, 0.08 letter spacing, colorPrimary and original margins.
- Original 16dp cards/padding and zero stroke; all six category chips with their original Filter style and single-selection/optional selection configuration.
- Original outlined description field: 12dp corners, 1,000-character counter/filter limit, 5–10 lines, multiline/capital-sentence input, top gravity and original text size/color.
- All attachment title/description rows, divider dimensions/margins, original three checked MaterialSwitches and their CustomWidget style. Distinguish the unstyled bold subscription attachment title from 14sp sans-serif-medium titles in the other rows.
- Bottom action bar with original surface/elevation and full original send-button/privacy-note properties. Preserve baseline descendant dimensions even for controls never clicked by the visual fixture.

Consumers to change:
- `UtilityTypographyTest.supportSectionUsesItsXmlAppearanceBoldAndLetterSpacing` (line 62): direct reference to Describe section label; preserve `assertTypography` checks.
- `supportUnstyledAttachmentTitleUsesThemeTextViewDefault` (line 70): independent AppCompatTextView reference with theme-default appearance plus bold. Do not supply a new explicit TextAppearance.
- `ContentVisualParityTest.supportCapturedAgainstRetainedXmlAcrossConfigurations` (line 62): programmatic full reference; keep all eight configurations, all eight label measurements and hero subtitle, send display/action/enabled state, 48dp touch target, section heading, attachment content description, and expanded 180dp header review. The synthetic send callback must continue to throw if activated during capture.
- `UtilityTypographyTest.allText` can be removed only if every existing lookup is replaced with a typed handle; its assertions must remain.

### 3. `app/src/main/res/layout/dialog_input_ips.xml`

Factory: `BaselineReachabilityReference.ipv4EditText(context)` returns the actual `AppCompatEditText` reference control used by the original `ipv4_address_1` declaration. It need not recreate the whole unrelated dialog because the current test only reads typography.

Construct the original `androidx.appcompat.widget.AppCompatEditText` with its original default `editTextStyle`, `@drawable/edittext_default` background and text input type, preserving the declaration's 0dp width/weight=1/wrap-content height when placed in a measurement container. Preserve theme-default typeface, letter spacing and text size; no explicit textAppearance, text, hint or textSize was present on this field. Any explicitly present source property must be represented in the expected-property contract. Do not derive this reference from `OriginalTextField` or its resolver.

Consumer: `UtilityTypographyTest.reachabilityInputMatchesXmlEditTextDefaultAppearance` (line 78). Keep all `assertTypography` comparisons; retain synthetic `192.0.2.1` and do not invoke network reachability or a real dialog/controller.

### 4. `app/src/full/res/layout/dialog_info_rules_layout.xml`

Factory: `BaselineOrbotInfoReference.create(context, title, description)` returns `root`, `title`, `description`, `closeImage` and any optional hidden children.

Source contract:
- NestedScrollView, match-parent width/height, 20dp padding; vertical content, header and text rows in original order.
- 30dp close image aligned end/center with platform close drawable. Optional OK button remains GONE; preserve its source style/spacing rather than letting it participate in sizing. Hidden leading/trailing icons/text likewise remain hidden.
- Title: actual AppCompatTextView with `Widget.AppCompat.TextView.SpinnerItem`, AppCompat Headline textAppearance, heading dimension, primaryTextColor, original drawable padding and center-vertical placement.
- Description: AppCompatTextView with the same SpinnerItem widget style, AppCompat Subhead textAppearance, large-font dimension, primaryTextColor, 5dp padding, 5dp line-spacing extra, 10dp top margin and selectable text.
- Include font padding, default FIRST_STRONG/content text direction, inherited font family/style/letter spacing and original width/height measurement behavior. Do not replace a wrap-content width with an arbitrary width.

Consumers to change:
- `OrbotInfoTypographyTest.actualXmlTypographyMatchesAtNormalAndLargeScalesAcrossThemes` (line 22): compare typed title/description against `resolveOrbotInfoTypography` across the same three themes x two font scales. Keep exact text-size, letter-spacing, color, typeface style, sample width, font-spacing, includeFontPadding, directional-padding, 5dp line-extra and selectability assertions. Retain the separate underline/italic/paragraph/no-link-action test unchanged.
- `NetworkSheetContentTest.infoUsesBaselineLineStepAndFirstStrongDirectionAtNonlinearFontScale` (line 128): complete reference panel measured at 360*density x 800*density, locale English with RTL layout and fontScale 2. Keep exact baseline step and line-count comparisons, resolved LTR paragraph direction and no visual overflow. Do not relax these to nonempty-text checks.
- `ContentVisualParityTest.orbotInfoCapturedAgainstRetainedXmlAcrossConfigurations` (line 136): same eight reference/Compose capture pairs and title/description metrics, dismiss display/action, 48dp target, and text-layout/selection semantics. The fixture's dismiss callback continues to reject activation.
- `NetworkSheetContentTest.informationUsesXmlTypographyAndCloseIsOnlyCallback` has no inflation but its historical method name should be revised along with the pin if desired. Its 18sp/14sp and exact zero-to-one callback checks remain.

### 5. `app/src/main/res/layout/bottom_sheet_summary_stats_filter.xml`

Factory: `BaselineStatisticsFilterReference.group(context, seed, callbacks)` constructs a real Material `ChipGroup` with exactly six real `Chip` children in APPS, COUNTRIES, PROVIDERS, BLOCKLISTS, DOMAINS, IPS order. No other screen scaffold is needed for the current behavioral tests.

Keep selectionRequired=true, singleLine=false, multiple selection, original 6dp horizontal/4dp vertical spacing, 6dp top padding; original ThinnerChip.Large child styling, 3dp margins, 2dp padding, checkable state, original initially-checked state, hidden checked icon, original text size and `bg_chip_state_list` fill. Keep PROVIDERS as a GONE child, never remove it. IDs remain the existing `SectionFilter.chipId` resource symbols. Assign a stable test reference group ID to each recreated group (the existing retained `ssfc_chip_group` symbol is valid).

Creation order is significant: build all six initially-checked children, hide provider, apply seed in the original order, then register listener. Do not register listeners earlier or simplify expected callback sequences. Preserve real framework hierarchy save/restore and Material callback behavior.

Change `StatisticsMaterialReferenceTest.group` (line 28), preserving these methods:
- `realSixChipHierarchyRestorationMatchesNativeReplayIncludingHiddenProvider`: all 4 targets x 3 seeds, real SparseArray/Parcelable save/restore, final checked-ID sets and complete callback sequence equality against native selection replay.
- `actualSelectionRequiredLastUncheckIsSilentAndSelectAllIncludesHiddenChild`: last-chip uncheck silence, select-all callback sequence equality, inclusion of hidden provider.
- `actualBlocklistListenerRechecksLastTagAndRefetchesExactlyOnce` is already programmatic and stays unchanged.

## Shared harness and transitive impacts

In `VisualParityHarness.kt`:
- Retire `inflateBaseline(context, layout)` and its LayoutInflater.Factory2 implementation. Do not replace it with a generic hidden-layout interpreter.
- Keep `VisualCase`, all eight configurations, `descendants`, measurement, physical offscreen attachment/detachment, capture validity, text metrics, required content/no-ellipsis gates, contrast and workspace-only output behavior.
- Change misleading `baseline-xml.png`/`XML=` descriptions to `baseline-reference.png`/`reference=` and add explicit provenance: baseline commit/path/hash, `referenceKind=source-derived-programmatic-widgets` (or `material-library-widget`), factory hash, theme, density, fontScale, locale/direction and platform/Material version. Do not label new reference pictures actual historical XML captures.
- `VisualEvidence` is also used by `WgAlertVisualParityTest`. Its Material library dialog reference legitimately uses the dependency's internal layout; it has no app `R.layout` dependency. Preserve both default/no-dim methods, actual-window capture, action contrast, no-dim assertion and lifecycle cleanup. Updating shared output metadata requires its helper/source safety review too.
- `ContentVisualParityTest.rootNavigationCapturedAgainstBaselineWidgetAcrossConfigurations` already constructs BottomNavigationView programmatically. Keep it; its source-defined menu/drawable/theme defaults, selected indicator measurement, all five tabs' text/roles/actions/selection/targets, RTL order and height review remain independent evidence.

Rename methods that explicitly claim retained/real XML if their implementation changes, with an explicit old-to-new mapping in the method manifest. Prefer maintaining the same 84 classes / 598 methods for this replacement; added verification tests are allowed only after independent scope/pin review. Method-count parity alone is insufficient: preserve the listed assertions and configuration loops.

## Generated-resource and build impact

Add a values-only file, e.g. `app/src/main/res/values/compose_legacy_ids.xml`, containing exactly one `<item type="id" name="..."/>` for every name in `test-dependencies-preserve-all-ids.txt`. Keep existing ID declarations/navigation/menu IDs unchanged. Check exact set equality and duplicate declarations. No placeholder Views, legacy layout inflation, generated binding classes or dynamic layout resources are needed.

Keeping all 2,516 names is preferable in this narrowly scoped cleanup: it avoids unrelated removal of the legacy ID namespace and protects names used by restore/tour hooks and potential consumers not proven by lexical analysis. The 137 compile-critical subset includes HomeNode/tour IDs, navigation container/heading IDs, chip selection enums, RPN anchors, Snackbar container and sponsor callbacks. Keeping only the 137 could compile after test changes, but it would unnecessarily combine resource-namespace pruning with layout removal.

**Resource names/types are preserved, not guaranteed numeric values.** AAPT resource number assignment may change when layouts disappear or toolchain/merge inputs change. Neither `values/ids.xml` nor matching names prove stable cross-version integer IDs. Do not invent numeric constants or promise cross-version saved View-state compatibility. If exact numeric stability is a requirement, it needs separate approved resource-linker/stable-ID work and build evidence. Existing Fragment/SavedState restoration tests remain gates.

The eight test-only IDs are `collapsing_toolbar`, `info_rules_dialog_rules_desc`, `info_rules_dialog_rules_title`, `ipv4_address_1`, `ssfc_chip_group`, `success_message`, `success_title`, and `tv_hero_subtitle`. They remain harmless values symbols under full-namespace preservation even when factories return typed handles.

`app/build.gradle.kts:358–360` adds `src/full/res` into main and clears full's separate resource directory. Thus resource removal affects both Full and TV, not just Full. Keep Compose build features and test Android-resource inclusion. No new resource sourceSet, debug XML, generated-res task or hidden assets fixture is required. Generated `R.layout` members for the 185 removed names disappear; none may remain in production or tests. Library/platform resources are separate.

No direct layout references were found in `app/src/androidTest`, `testPlay`, `testWebsite` or the benchmark source, and there is currently no debug source set. This does not authorize executing those suites. Other LayoutInflater imports in `SafeRootHostFixture`, `ActivityFragmentContainerTest`, and `SingleSettingsFragmentHostTest` are framework `onCreateView` parameter types only; their programmatic Compose/FragmentContainerView fixtures need no XML replacement.

`Android.bp` references only main resources and already has distinct legacy headless/source/dependency assumptions; no Soong validation was performed. Do not claim its build is repaired by this cleanup. Later approved variant compilation must cover the configured project variants, especially F-Droid Full and TV; Play/Website remain subject to their existing missing-artifact blockers.

## Guard changes, without weakened assertions

1. `scripts/compose-migration/check-network-settings-layouts.py`, `main` lines 22–48: replace current-file glob discovery/byte equality with exact original paths from the immutable baseline tree or a pinned expected-property contract. Still verify all six `activity_misc_settings`, `activity_tunnel_settings`, `fragment_proxy_configure`, `dialog_set_proxy`, `dialog_lockdown_check`, `item_lockdown_check` baseline hashes and every native tag/label/hint/icon/size/TextAppearance/switch-overlay mapping. Assert the current layout files are absent. Do not simply remove the six checks.
2. `check-network-sheet-controllers.py`, final resource loop lines 37–43: read the six baseline files through Git objects or verified property expectations, verify source provenance and new reference contracts, and assert current layouts absent. Keep all protected controller-body comparisons and exact Orbot HTML extraction. The six paths cover dialog_info_rules_layout, bottom_sheet_orbot, activity_backup_restore, bottomsheet_rinr_warning, bottom_sheet_proxies_list, list_item_proxy_cc_wg.
3. `check-binding-removal.py`, `scan` lines 285–353 and reporting: promote app-owned layout presence from inventory to failure across **all source sets**, including test/debug/flavor/qualified resources; keep current production renderer checks, alias/dynamic lookup checks, non-layout `@layout` checks, exact FragmentContainerView boundaries and notification exception. Report zero app-owned layout files and zero own layout references, not merely binding-free. Add negative controls for a layout reintroduced in test/debug/new flavor, renamed/generated fixture, aliased app R reference and indirect resource sourceSet. Preserve existing mutation controls.
4. Add an explicit no-layout-escape audit covering res sourceSets/generated resource declarations and layout XML copied into assets/test resources/docs as executable fixtures. Historical source paths in documents and scalar expected-style contracts are allowed, executable raw XML reconstruction is not. Git history is expected to retain deleted source.
5. `check-home-section-spacing.py` already reads baseline layout/w600dp through `git show`; no live-layout dependency. Preserve its card-style checks and mutation tests.
6. `check-navigation-chrome.py` already uses Git baseline and requires three older shell layouts absent; retain. `check-purchase-controllers.py` parses baseline Checkout via Git and requires obsolete Konfetti layout absent; retain. `check-wireguard-resources.py` scans current values/file resources and should continue to find ID/style symbols after preservation; not a substitute for AAPT.
7. `inventory_component_styles.py`, `generate_original_control_styles.py`, renderer/variant/fixture scripts and original-control/field guards consume pinned baseline Git objects or expected-style JSON. No current layout retention is required. Retain values/shape/selector coverage and explicit controller/callback identity checks. Update historical report wording only when new evidence exists.
8. `check-original-test-relocation.py` protects original test declarations; retain its relocation ledger. Deleting XML is not permission to remove original navigation/behavior/accessibility coverage.

New guard negative controls should reject changed reference font size, omitted selector state, wrong ChipGroup child order/hidden provider, wrong support padding, blank capture, removed label/action assertion, generated XML escape and missing one preserved ID. Source mutation checks are supplementary; they do not replace executing widgets and Compose later.

## Safety fence and staged acceptance

### Source-only implementation phase, after authorization

1. Freeze layout/property and ID inventories; preserve all 2,516 ID declarations and immutable source provenance.
2. Implement independent factories/contracts and replace the 12 test resource lookups. Preserve the 13 affected methods' assertion sets and shared reference consumers. No production behavior changes are needed for the factories.
3. Update source guards without weakening protected controller/resource checks. Audit factory constructors to plain framework/material references only; no production Application, real Activity host/controller, Koin graph, service, database, network, user files, billing or permission flow.
4. Complete the separate whole-layout style/equivalence corrections and review; only then delete all 186 layouts. No layout copies or generated layouts.
5. Recompute the entire `app/src` source envelope and exact reviewed class/method/helper/fixture/production/build pins **after** all authorized source changes. Explain method renames and assertion parity. Add every new helper/data contract to pinning. Preserve launcher offline/exact-class/sanitized-agent/synthetic-home/workspace-output conditions. `runtime_clearance.status` stays `BLOCKED`; source review cannot clear it.
6. Update current status documents honestly while retaining historical reports as historical. The old 48 capture-pair success report was pre-reset, its actual images were lost, and it does not validate these new factories or styling changes.

### Compilation/runtime phase, currently prohibited and unavailable

Java 17, Android SDK and caches were lost. Only system Java 21 remains; the earlier explicit policy denial was access to `https://dl.google.com:443`. No retry, mirror, alternate download route, Gradle/device/runtime execution or production operation is part of this plan.

When authorized toolchain/artifact access is genuinely restored, independent runtime revalidation must first check the exact approved local Java/Gradle/Android/Robolectric/Mockito/Byte Buddy artifacts and process environment, then compile current resource/Kotlin production and test variants, then run exact reviewed isolated filters. Do not loosen `isolated-tests.init.gradle`, use package wildcards, add dynamic attachment, or claim a generic unit-test command is currently cleared.

For final evidence, confirm zero app layout source/generation, all expected IDs, no unresolved own `R.layout`, and unchanged protected controllers; compile Full/TV and relevant tests; execute the preserved synthetic behavior/content/navigation/a11y tests; regenerate honest reference/Compose captures; inspect geometry, original control styles, line wrapping, RTL/large text and action visibility. Physical-device TalkBack/IME/OEM/window/performance checks remain separately unverified until expressly supported and authorized.

This audit and eventual source deletion are not a claim of successful compilation, complete visual parity, runtime safety clearance or production readiness.
