# Finite Kotlin/JVM accessor collision audit

## Result

Only the two reported `RpnFlagWatermarkState` collisions were confirmed. Both are fixed by the coordinator’s private backing-property renames. No additional collision was confirmed in the finite UI/adapter candidate set. This is source reasoning, not a successful compilation.

Baseline: local `969aef531b9f82c7f9cee739734011860ad0c37c` (parent verified tree equivalence to remote `7fd20ed5df65610e450e8b27d84951ef1959e348`).

## Confirmed defects and minimal correction

File: `app/src/main/java/com/celzero/bravedns/ui/compose/rpn/RpnFlagWatermark.kt`.

- Property line 113 and method line 139 both generated `setFlagText(Ljava/lang/String;)V`. Rename private delegated `flagText` to `flagTextValue`.
- Property line 122 and method line 187 both generated `setSpread(Z)V`. Rename private delegated `spread` to `spreadEnabled`.
- The private delegated `flagDrawable` at line 114 does not currently collide: its setter takes one `Drawable`, whereas public `setFlagDrawable` at line 154 takes `Drawable`, boxed `Integer`, boxed `Float`. Default arguments generate a separate `$default` bridge; no `@JvmOverloads` is present.

Independent patch review: reversing exactly the two identifier renames reproduces the complete baseline file byte for byte (SHA-256 `b8188c257508fc4a52b5af91a69952d4cc7654604dfbf5877ae25fb40f87a883`). Public API, comments, callers, draw/reset logic, shader invalidation, geometry, alpha constants and text sizing are unchanged.

## Scope and method

The initial name sweep covered 795 production Kotlin files. Manual candidate dispositions cover 358 production UI Kotlin files, including all 188 Compose files, plus 28 related adapter files. The resulting 79 accessor-shaped method declarations in 34 files were classified by owning class, local versus member scope, property visibility, plain versus delegated/custom accessor, JVM parameter types/boxing/arity, annotations and inheritance. The JSON companion lists each method/property line and its disposition.

Additional checks covered Boolean `isX` getter/setter names, property-to-property aliases, generic/extension declarations, defaults, custom setters, and the migrated base/derived owners below. The name sweep is candidate discovery only; it is not represented as a Kotlin parser or whole-project type check.

## Harmless candidate families

- `HomeNode` (`HomePresentation.kt:16,26–27,54–57`): `CharSequence` versus resource `int`, boxed nullable `Integer` versus primitive `int`, or one- versus two-argument setters. `HomePager.currentItem` (`:78`) versus `setCurrentItem` (`:81`) is one versus two parameters.
- `LogControlState` (`LogControlState.kt:16,19–20,37–39`): nullable `Int?` setters have `Ljava/lang/Integer;`, while helpers use `I`.
- `UtilityControl` (`UtilityControl.kt:20,33,36,71–76`): text setter takes `CharSequence`; helpers take `Object` or `int`. Nullable numeric properties and primitive helpers remain distinct.
- `WgControlState` (`WgControlState.kt:24,54,62,64,67,95–106`): text helpers have differing types/arity; progress helper takes `int,boolean`; color/image helpers use primitive `int` versus nullable boxed property setters.
- `AppSearchState` (`AppLogsContent.kt:33,39`) and `LogSearchState` (`LogScreen.kt:45,51`): generated query setter takes one `String`; explicit helper takes `String,boolean`. The latter’s default argument does not create a one-argument JVM overload.
- `AboutDialogBuilder`, `HomeDialogBuilder`, `NetworkAlertDialogBuilder`, `WgAlertDialogBuilder`, `RpnPropertyAnimator`, `SummaryStatisticsAdapter`, the original `FlagWatermarkView` and other retained View setters use private, non-delegated, default-accessor backing fields. These do not emit the colliding setters that private snapshot delegates do. No broad rename is needed.
- `AppActivityAdapter.kt:104–106`: `@get:JvmName("getNativeItemCount")` makes the property getter distinct from `getItemCount()`.
- Remaining matches are locals or distinct JVM owners, such as `BlocklistRow.groupName` versus `BlocklistSelectionPresenter.getGroupName(String)` and enum instance properties versus companion helpers. See the complete finite census below and in JSON.

## Boolean aliases and inheritance

- `HomeNode`: Reviewed HomeToggleGroup, HomePager, HomeHeatmap, HomeHistogram, IntervalRangeGroup, IntervalRows, IntervalSignature, RpnStatRows. Their properties/methods do not reuse inherited accessor signatures. HomePages.itemCount implementations are explicit property overrides, not duplicate getItemCount methods.
- `RpnControlState`: Reviewed RpnSearchState, RpnRefreshState, AppListControlState. New query/onRefresh/animation members do not duplicate inherited accessors. RpnSearchState.query is read-only; setQuery(String) writes distinct queryValue.
- `WgFormState`: Only inherited member property editableFields plus saveForm/restoreForm; 14 form subclasses do not redeclare colliding property/method names.
- `PagingPresenters`: Reviewed LogPagingPresenter, WgPagingPresenter, AppConnectionsPresenter, FirewallAppsPresenter and repository adapter subclass declarations. No subclass declares getItemCount() against inherited itemCount. AppActivityAdapter has its independent @get:JvmName safeguard and does not inherit these presenters.
- `NetworkNativeDialog`: context and theme constructor parameters are not val/var, so no generated getContext against android.app.Dialog. Its private buttons field and getButton(Int) have distinct names/arity.
- `Boolean_and_custom_accessors`: Reviewed isChecked/isVisible and related isX properties in HomeNode, RpnControlState, WgControlState, UtilityControl, LogControlState, DnsControl. Boolean state backing name checkedValue avoids setChecked(Z)V conflicts; UtilityControl.checked is a plain private Function2 callback field. No additional same-owner Boolean alias collisions found.
- `Defaults_and_annotations`: No @JvmOverloads on any method in the production UI subtree; occurrences are legacy View constructors. AppActivityAdapter @get:JvmName is the sole UI accessor renaming annotation. Compose setters with default parameters use distinct full-arity JVM methods and $default bridges. Reviewed generic/extension declarations do not introduce a matching accessor signature in this finite set.

Two property-to-property name candidates were harmless: `UtilityControl.checked` is a plain private callback field distinct from `isChecked`’s Boolean accessor; `DnsRecordTypesBottomSheet.isAutoMode` and nested `DnsRecordTypesAdapter.autoMode` have separate JVM owners.

## Finite candidate census

Paths below are relative to `app/src/main/java/com/celzero/bravedns/`. Line numbers refer to the audited source; the two private renames do not change line numbering.

- `adapter/OneWgConfigAdapter.kt`: `getStatusText` L294, `getHandshakeTime` L416. harmless_local_variable.
- `adapter/SummaryStatisticsAdapter.kt`: `setUid` L48, `setTimeCategory` L211. harmless_private_plain_field.
- `adapter/WgConfigAdapter.kt`: `getStatusText` L481, `getHumanReadableLastOk` L571. harmless_local_variable.
- `adapter/WgNwStatsAdapter.kt`: `getAppName` L172. harmless_local_variable.
- `ui/activity/AppListActivity.kt`: `setFirewallFilter` L534. harmless_distinct_owner.
- `ui/activity/ConfigureOtherDnsActivity.kt`: `getIntent` L45, `getDnsType` L64. harmless_distinct_owner_or_local.
- `ui/activity/CustomRulesActivity.kt`: `getType` L76. harmless_distinct_owner.
- `ui/activity/NetworkLogsActivity.kt`: `getCount` L148. harmless_local_variable.
- `ui/activity/ProxySettingsActivity.kt`: `getStatusText` L633. harmless_local_variable.
- `ui/activity/WgConfigDetailActivity.kt`: `getStatusText` L577, `getHandshakeTime` L615. harmless_local_variable.
- `ui/adapter/AppActivityAdapter.kt`: `getItemCount` L104. harmless_explicit_jvm_name.
- `ui/adapter/VpnServerAdapter.kt`: `setProxyStopped` L196. harmless_private_plain_field.
- `ui/bottomsheet/LocalBlocklistsBottomSheet.kt`: `setDismissListener` L94. harmless_private_plain_field.
- `ui/bottomsheet/ServerRemovalNotificationBottomSheet.kt`: `setOnDismissCallback` L221. harmless_private_plain_field.
- `ui/compose/about/AboutDialog.kt`: `setTitle` L50, `setTitle` L51, `setMessage` L52, `setMessage` L53, `setIcon` L54, `setCloseButton` L55, `setCancelable` L56, `setContent` L57. harmless_private_plain_field.
- `ui/compose/appdetails/AppLogsContent.kt`: `setQuery` L39. harmless_parameter_arity.
- `ui/compose/blocklists/BlocklistSelectionPresenter.kt`: `getGroupName` L84. harmless_distinct_owner.
- `ui/compose/firewallapps/FirewallAppsPresenter.kt`: `getItem` L33. harmless_local_variable.
- `ui/compose/home/HomeDialogs.kt`: `setTitle` L49, `setTitle` L50, `setMessage` L51, `setMessage` L52, `setCancelable` L53. harmless_private_plain_field.
- `ui/compose/home/HomePresentation.kt`: `setText` L54, `setTextColor` L55, `setTextSize` L57, `setCurrentItem` L81. harmless_parameter_type; harmless_parameter_arity.
- `ui/compose/networksettings/NetworkAlertDialog.kt`: `setContent` L42, `setTitle` L54, `setTitle` L55, `setMessage` L56, `setMessage` L57, `setIcon` L58, `setCancelable` L59. harmless_private_plain_field.
- `ui/compose/rpn/RpnFlagWatermark.kt`: `setFlagText` L139, `setFlagDrawable` L154, `setSpread` L187. confirmed_fixed; harmless_parameter_arity.
- `ui/compose/rpn/RpnMotion.kt`: `setDuration` L30, `setInterpolator` L32. harmless_private_plain_field.
- `ui/compose/trafficlogs/LogControlState.kt`: `setTextColor` L37, `setBackgroundColor` L38, `setImageResource` L39. harmless_nullable_primitive_boxing.
- `ui/compose/trafficlogs/LogScreen.kt`: `setQuery` L51. harmless_parameter_arity.
- `ui/compose/utilities/UtilityControl.kt`: `setText` L71, `setText` L72, `setTextColor` L73, `setColorFilter` L76. harmless_parameter_type.
- `ui/compose/wireguard/WgAlertDialog.kt`: `setTitle` L65, `setTitle` L66, `setMessage` L67, `setMessage` L68, `setIcon` L69, `setCancelable` L70. harmless_private_plain_field.
- `ui/compose/wireguard/WgControlState.kt`: `setText` L95, `setText` L100, `setTextColor` L101, `setImageResource` L104, `setIndicatorColor` L105, `setProgress` L106. harmless_parameter_arity; harmless_nullable_primitive_boxing.
- `ui/custom/AppHistogramView.kt`: `setLightTheme` L77. harmless_private_plain_field.
- `ui/custom/DonutChartView.kt`: `setCenterText` L93. harmless_private_plain_field.
- `ui/custom/FlagWatermarkView.kt`: `setFlagText` L132, `setFlagDrawable` L148, `setSpread` L183. harmless_private_plain_field.
- `ui/fragment/RethinkBlocklistFragment.kt`: `getTag` L106. harmless_distinct_owner.
- `ui/fragment/SummaryStatisticsFragment.kt`: `getType` L69. harmless_local_variable.
- `ui/tour/SpotlightOverlayView.kt`: `setPremiumGlow` L186. harmless_private_plain_field.

## Focused regression review

`scripts/compose-migration/check-watermark-accessors.py` pins the full pre-fix source after exact reverse-renaming, preserves the public method declarations, and checks the authored test contract. Independent execution with `--self-test` passed and rejected all 7 mutations; `git diff --check` passed. The script is intentionally finite and source-only.

`RpnFlagWatermarkStateTest.kt` contains two authored tests: public JVM setter signatures and bitmap parity against the original `FlagWatermarkView`. Source review found no API defect. The fixture uses plain `Application`, synthetic text/drawables/bitmaps, both RTL/LTR directions, repeated and changed spread modes, text/resize/blank/drawable/clear transitions. It does not instantiate a production Activity, controller or service.

Both Kotlin tests remain AUTHORED / UNRUN. Native graphics/toolchain and the existing isolated-test launcher/allowlist need their required validation before execution; this source review grants no runtime clearance.

## Verification boundaries

No Gradle, Kotlin compiler, Android build, lint, unit/instrumentation test, application, VPN/backend, or network/security runtime was executed. No toolchain download was attempted, and the existing artifact denial was not bypassed. The auditor changed no production/test/guard code, only these two audit reports. Compiler/build success remains unverified.
