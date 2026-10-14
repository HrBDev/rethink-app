# Component-style restoration: independent source review

Review date: 2026-10-14 UTC. Original UI reference: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`. Native Compose comparison: `e2b97de59cc854de74010c096ceffbaed6cb4a55`.

This review is source-only. No Gradle invocation, Kotlin compilation, dependency download, Android runtime, emulator, production Application, VPN, account, permission flow, or application import/export was executed. The unavailable toolchain/cache and explicitly denied Google artifact endpoint remain blockers. The source review does not establish rendered visual parity or passing tests.

## Scope

- Shared original-resource button/chip/label renderer, generated style resources/catalog, representative button and chip integrations, segmented groups, and passive purchase badges.
- Shared field/spinner renderer, plain/outlined/filled/search families, error states, text selection, keyboard actions, and representative DNS, WireGuard, network, rule, support, and ping integrations.
- Additive synthetic test safety: Home/navigation layout tests, passive purchase badges, shared field tests, and subsequent exact frozen source pins.

The target is restoration of original component styling while keeping native Compose and existing actions. Source style resources are useful evidence only when the corresponding live call site uses them. Original group/controller overrides take precedence over a standalone widget template. Cosmetic permission from the earlier migration does not waive these requirements.

## Findings sent to implementers

1. The first button/chip resolver converted an Android dimension to pixels, divided by `scaledDensity`, then handed the result to Compose as SP. Android 14+ nonlinear font scaling can therefore be applied incorrectly. Read raw SP values or convert through the current Compose density exactly once.
2. The first resolver forced normal/bold weights over a resolved Android Typeface, losing original medium-family/native numeric weights. Reuse the existing native-weight resolution policy.
3. The first resolver took the maximum of a fixed XML visual height and the inherited widget minimum, so 32dp mode buttons could regain a larger default silhouette. A fixed source visual height must take precedence while allowing font/content growth and preserving the outer accessible hit area.
4. The first field renderer supplied the normal cursor to `BasicTextField` even when the decoration's colors declared an error cursor. Resolve the actual editor cursor from error state. Preserve an explicit accessibility error message when replacing the stock TextField wrapper.
5. Standalone source styles do not reproduce `MaterialButtonToggleGroup` child corner mutations. The first/middle/last visible segment shapes and logical RTL ends need explicit group integration. Arbitrary migration gaps must not be treated as original styling.
6. Remembered resolved styles must be invalidated by relevant configuration/density changes, including qualified resources and font-scale changes.
7. A generated source style is ineffective when a child supplies unrelated typography or a hard-coded fallback color. The RPN error-button labels retained `RpnText`'s generic primary-text color over original white/error/secondary colors. The About sponsor-again child selected theme typography instead of its explicit source appearance.
8. Automatic source icons can duplicate retained manual child icons. Sponsor, bug-report send, and RPN-log actions were flagged for a single source/override icon path.
9. A manual `hasValue` fallback can resurrect an attribute explicitly cleared with `@null`. Preserve explicit nulls rather than falling back to the default widget style. A remembered Drawable must also lose a prior dynamic tint when the caller clears it.
10. Selected segmented controls must keep single-choice/checkable semantics rather than being universally presented as navigation tabs.

The fixes for these findings were re-read in the frozen source. The final target audit also removed the caller's fixed 44dp outer constraint from the RPN error-reset control, retaining the original 44dp visual specification inside a minimum-48dp outer area. WireGuard's main pair now derives selected semantics from the displayed one/general list, matching its existing controller state rather than an unused checkable field. These source corrections do not establish runtime accessibility or visual acceptance.

### Closed field review

The field cursor/error-semantic finding is corrected in source. The final frozen field primitive, test class, source checker, and mapping are pinned in `original-fields-independent-review.json`. The source checker passes 96 baseline XML records across 18 native files and 21 editor call sites. Independent in-memory negative controls reject ten distinct mutations: value, change callback, enabled, read-only, single-line, maximum/minimum lines, password transformation, IME option, and IME callback. A presentation-only component rename preserves the same behavior records. These are parser/source checks, not Kotlin or runtime tests.

The exact new class is `com.celzero.bravedns.ui.compose.fields.OriginalTextFieldsTest`, containing eight declared methods. Every method was independently read. Theme resolution, state colors, disabled editing, password/IME semantics, retained `TextFieldValue` selection, large-RTL field bounds, error state, and spinner clicks operate only on resources, framework test UI, synthetic values, and local recorders under an explicit plain Application. The Rules search-clear affordance uses the original close icon with the same existing clear callback.

### Controls test and guard review

The exact new class is `com.celzero.bravedns.ui.compose.controls.OriginalControlsTest`, containing eight declared methods. Its plain-Application fixture resolves local styles across six themes, checks family geometry and raw SP/native typeface weights, explicit nulls/checked-icon policy, stateful RPN selector colors, LTR/RTL segment end caps, disabled/passive behavior, and silent/exact-once synthetic selection. The catalog sweep calls only the style resolver; it does not create production controllers or activate actions.

`original-controls-independent-review.json` records the independently checked primitive/catalog/test/guard hashes. The source guard passes 284 source keys, 271 generated styles, 48 checked null overrides, and 117 logged callback expressions. Eight additional in-memory mutations are rejected for state-selector lookup, explicit-null handling, minimum targets, passive semantics, raw-SP conversion, tint reset, View inflation, and plain-Application configuration. The callback check establishes expression presence, not full control-flow equivalence; representative wrappers and controller overrides were separately read.

The coordinator checked the official Material Components 1.14.0 `ChipDrawable` source: `loadFromAttributes` applies `chipCornerRadius` after shape appearance and `setChipCornerRadius` replaces the model's corner sizes. The renderer and test now retain the inherited 12dp radius for `fragment_rethink_list:lb_version`; the previous 1dp precedence assumption was removed. Source: https://raw.githubusercontent.com/material-components/material-components-android/1.14.0/lib/java/com/google/android/material/chip/ChipDrawable.java . No artifact/dependency route was used for that public source lookup.

The separately authored icon/FAB changes were reviewed as presentation-only: the firewall network-action border matches the original 0.1dp `colorOutline` / 12dp drawable, and both original history Activity toolbar setups disable Up, supporting removal of the migration-added navigation icon. Explicit FAB colors/icons/elevations preserve the same visibility and action callbacks. Actual rendering and platform Back behavior remain unexecuted.

## Safety observations

The added renderer entry points read local Android theme/style/drawable/font resources and draw through Compose. They do not inflate production layouts or create Android widget bridges. Their click/edit callbacks remain supplied by callers. The plain field overload retains `TextFieldValue` rather than introducing a String round trip that would discard selection/composition. Error and disabled state changes remain local presentation state.

The new synthetic Home fixture creates `HomePresentation` with local values and recorder callbacks, without HomeScreenActivity/HomeScreenFragment or a live adapter. Root chrome tests create only generic framework hosts, an empty `FragmentContainerView`, or a nested `ComposeView`; no NavHost or production destination is attached. Pixel capture remains in-memory. Snackbar tests retain their pre-existing isolated framework host and cleanup. Purchase badge tests use literal state and no billing/account/repository calls. Field tests use a plain `android.app.Application`, local theme contexts, synthetic strings, and recorder callbacks.

The authoritative cumulative safety gate is `binding-removal-safety-allowlist.json`, not the historical content-test review. Its runtime clearance must remain `BLOCKED`. The checked-in isolated launcher is source preparation only; it does not make missing runtime artifacts available. Historical agent hashes and missing old cache paths are provenance, not current executable verification.

## Frozen source identity and cumulative boundary

The final independent reread covers **84 exact classes / 598 declared test methods**. Relative to the prior cumulative boundary, three existing test classes add eight methods in total (RootChrome +6, Snackbar +1, Purchase +1), and three new classes add 21 methods (Home spacing 5, fields 8, controls 8). Existing fake-host fixtures, non-test helpers, configuration, and checked-in isolated launcher still match their previous pins.

The complete `app/src` envelope contains **1,576 files**, SHA-256 `b99f1104df77f5b3c754fc64a6e4a0ee7c6377193e8f9842795a3e0df1d6a547`. It uses the launcher's documented whole-POSIX-path sort and `path + NUL + file SHA-256 + LF` records. The exact cumulative manifest records 96 changed/added source files relative to `e2b97de59cc854de74010c096ceffbaed6cb4a55`, plus each new/changed class and all current source/configuration/helper/fixture hashes. All pins and the envelope were independently reread after writing. The working-tree HEAD at review is the published Home/navigation group `28435219d19d902862336a84fe734dcb584fa7dd`; it does not by itself identify these later uncommitted component-style bytes.

Execution remains **BLOCKED_TOOLCHAIN_AND_ARTIFACT_REVALIDATION_REQUIRED**, with runtime clearance **BLOCKED**. The overall requested app migration is still unverified at runtime and must not be represented as finished, production-ready, or visually accepted. Any later relevant source change invalidates these exact pins until reviewed.

## Required later validation

- Compile production and exact reviewed test variants with an authorized, independently revalidated toolchain and cached/offline dependencies.
- Execute only exact source-pinned synthetic/host test classes under their existing fake-service, plain-Application, lifecycle, output-path, and no-real-backend restrictions.
- Compare actual phone/wide, LTR/RTL, large-font, light/dark/Plus/Frost renderings with the original UI, including selected, focused, pressed, disabled, and error states.
- Verify Material Components 1.14 corner/shape precedence, segmented stroke overlap, compact silhouette versus minimum hit target, drawable tinting/ripple behavior, field labels/cursor/selection, and keyboard/TalkBack operation on the running app.
- Preserve the reported limitations until those checks actually run; a text/source guard is not a screenshot or UI verification result.
