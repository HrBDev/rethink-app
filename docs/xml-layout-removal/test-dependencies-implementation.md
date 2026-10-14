# Test-reference implementation handoff

2026-10-14. Source-only implementation authorized after the planning audit. No Android/Gradle/device/runtime or network operation was performed. Runtime clearance remains BLOCKED; this work does not clear the manifest or establish compiled/rendered parity.

## Result

- Removed all 12 app-owned `R.layout` references from the five affected JVM test classes, without deleting XML in this worker's scope.
- Added five independent typed native-widget factories with 73 baseline nodes: Sponsor 8, Support 47, Orbot 10, statistics group 7, reachability field 1.
- Preserved full Sponsor, Support and Orbot capture hierarchies. Statistics retains actual Material ChipGroup/six-child state semantics; the reachability test uses the original AppCompatEditText. These are programmatic references, not Compose implementation copies or relocated XML resources.
- The five classes retain all 23 test methods. Every existing line containing an `assert...` invocation is byte-for-byte unchanged after trimming indentation. Existing assertion-call counts also match. The eight `visualCases` declarations are unchanged. The Support label `.single` cardinality checks are retained.
- All original source attributes, styles, native constructors, parent LayoutParams, child ordering, hidden states and source-property hashes pass the independent source checker. Sixty original non-layout resource dependencies also match the baseline, protecting against changes that could otherwise move both reference and implementation together.
- `VisualParityHarness` no longer inflates app layouts. New capture reports use `baseline-reference.png` and explicitly identify source-derived versus Material-library widget provenance, original source/factory/helper hashes, theme, density, direction, SDK and declared dependency versions. These are not labeled historical XML screenshots.
- Shared WgAlert and root-navigation library reference paths remain supported. No WgAlert test source or existing assertion body was changed.

## Files and schema

Kotlin test changes:
- `ui/compose/networksheets/NetworkSheetContentTest.kt`
- `ui/compose/networksheets/OrbotInfoTypographyTest.kt`
- `ui/compose/statistics/StatisticsMaterialReferenceTest.kt`
- `ui/compose/utilities/UtilityTypographyTest.kt`
- `ui/compose/visual/ContentVisualParityTest.kt`
- `ui/compose/visual/VisualParityHarness.kt`

Seven new helpers under `app/src/test/java/com/celzero/bravedns/ui/compose/reference/`:
- `BaselineReferenceAttributes.kt`
- `BaselineReferenceSources.kt`
- `BaselineSponsorSuccessReference.kt`
- `BaselineSupportReference.kt`
- `BaselineOrbotInfoReference.kt`
- `BaselineReachabilityReference.kt`
- `BaselineStatisticsFilterReference.kt`

Immutable scalar/property contracts are `docs/xml-layout-removal/test-dependencies-{sponsor,support,orbot,reachability,statistics}-contract.json`. Each has baseline/source_path/baseline_source_sha256, factory_path/factory_sha256, reference_scope, and nodes containing variable/widget/source_attributes/parent_variable/layout_params_class. They contain no raw XML and are never loaded by Android or test runtime.

`test-dependencies-resource-pins.json` records the exact 60 original resource paths/SHA-256s, five seed themes, and 17 unresolved library/framework resource symbols. These symbols are not claimed resolved by the source checker.

`test-dependencies-implementation-review.json` records current helper and changed-source hashes and check results. `test-dependencies-assertion-parity.json` records exact before/after method lists, rename mapping, assertion counts and unchanged-assertion-line result. `test-dependencies-frozen-files.json` is the final worker-owned file/hash list, excluding itself to avoid circular hashing.

Source-only verification entry point: `python docs/xml-layout-removal/test-dependencies-check-fixtures.py`. It reads pinned baseline Git objects and current source, validates the expected property/structure contracts, baseline dependencies and preserved assertions, and writes only documentation reports. It does not run Gradle, Kotlin, AAPT or Android. The separate review-time factory generator is not wired into Gradle; it emits explicit Kotlin constructors and JSON property contracts, never any layout XML, compiled layout resource or runtime parser.

## Exact method renames

UtilityTypographyTest:
- sponsorSuccessTitleMatchesTheRealXmlTextView -> sponsorSuccessTitleMatchesIndependentBaselineTextView
- sponsorSuccessBodyMatchesXmlBodySizeAndSpacing -> sponsorSuccessBodyMatchesBaselineBodySizeAndSpacing
- supportSectionUsesItsXmlAppearanceBoldAndLetterSpacing -> supportSectionUsesBaselineAppearanceBoldAndLetterSpacing
- reachabilityInputMatchesXmlEditTextDefaultAppearance -> reachabilityInputMatchesBaselineEditTextDefaultAppearance

OrbotInfoTypographyTest:
- actualXmlTypographyMatchesAtNormalAndLargeScalesAcrossThemes -> independentBaselineTypographyMatchesAtNormalAndLargeScalesAcrossThemes

NetworkSheetContentTest:
- informationUsesXmlTypographyAndCloseIsOnlyCallback -> informationUsesBaselineTypographyAndCloseIsOnlyCallback

ContentVisualParityTest:
- supportCapturedAgainstRetainedXmlAcrossConfigurations -> supportCapturedAgainstIndependentBaselineAcrossConfigurations
- sponsorSuccessCapturedAgainstRetainedXmlAcrossConfigurations -> sponsorSuccessCapturedAgainstIndependentBaselineAcrossConfigurations
- orbotInfoCapturedAgainstRetainedXmlAcrossConfigurations -> orbotInfoCapturedAgainstIndependentBaselineAcrossConfigurations

All other method names remain unchanged. The rename map must be applied when the coordinator re-pins the exact safety manifest; method/count identity alone is not runtime clearance.

## Bounded construction review and remaining API uncertainty

The attribute helper uses `Robolectric.buildAttributeSet`, `setStyleAttribute`, and `addAttribute` to feed original properties into real widget constructors. It stores no app-owned layout XML and uses no app layout inflater. Framework, AppCompat, Material and CardView attribute namespaces are explicit; CardView properties use `androidx.cardview.R.attr` because non-transitive R is enabled.

Each child uses its actual parent's context, preserving nested Material theme overlays. Native parent LayoutParams are constructed before descendants but installed only on parent addView after the child's completion hook, matching normal framework construction order. Child addView order and post-order completion are checked against the original source.

The helper calls only the bounded `onFinishInflate` construction hook through `ReflectionHelpers.callInstanceMethod<Unit>` on widgets already constructed by one of these five fixed factories. This is necessary because plain programmatic addView would skip the hook that the original framework inflater invoked. It is not a production class lookup or general method-dispatch API exposed to external input. The existing JSON helper pin and all factory hashes include this code.

The original plain-inflater Sponsor TextView/ImageView mode and the Material-compatible capture mode are kept distinct. Explicit AppCompat/Material widget declarations are unchanged. Native widget defaults, shape/theme resolution and real font rasterization still require the approved runtime artifacts; source inspection cannot verify them.

Uncompiled APIs requiring later approved validation include the Robolectric attribute builder/style setter, bounded protected-hook dispatch, and native LayoutParams constructors. Material/Robolectric declared versions are 1.14.0/4.17; only artifact revalidation can establish the actual runtime. No fallback to a generic, simplified or self-comparing reference is acceptable if compilation/runtime reveals a problem.

## Scope and handoff

This worker changed only the listed JVM tests/helpers and `docs/xml-layout-removal/test-dependencies*` audit artifacts. It did not alter production classes, layout XML, resource IDs, shared guards or the shared safety manifest. Concurrent coordinator/worker changes elsewhere in the checkout are outside this file list.

The coordinator owns global guard updates, the all-2,516-name values-only ID preservation, layout deletion, independent style/layout correction review, final source-envelope/helper/fixture/method pins, and any eventual toolchain/runtime revalidation. Keep the 84-class/598-method runtime fence BLOCKED. These new helpers add no test methods and do not authorize additional suites, production Application startup, VPN, accounts, traffic, permissions or device operations.
