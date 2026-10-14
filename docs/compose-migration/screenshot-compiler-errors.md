# Corrections for the reported 15 Kotlin errors

The user's F-Droid Full compiler screenshot identifies five affected files in
`02b826718295f054411b99912b4a00938ace263a`. These are actual source defects missed
by the earlier source-only checks. This correction is **not a successful build
claim**: local Android compilation remains blocked by the existing toolchain and
artifact-access restriction.

| File | Diagnostics shown | Root cause and correction |
| --- | ---: | --- |
| OriginalCompoundControls.kt | 4 | `thumbTint`, `track`, `trackTint` and `switchMinWidth` belong to AppCompat, not Material's non-transitive R class. Use `androidx.appcompat.R.attr`; preserve `android.R.attr.thumb`, Material icon/decoration attrs, array order and all resolution indices. |
| HomeContent.kt | 3 | Compose `Layout` takes `content` first. Passing `modifier` positionally and also naming `content` bound the first parameter twice and caused cascading composable-context diagnostics. Name `modifier = modifier`; leave the content and measure policy unchanged. |
| RpnHeaderSkeletons.kt | 3 | Shimmer 0.5.0 exposes no public getters for its package-private `baseColor`, `highlightColor` and `animationDuration` fields. Use documented private constants from the pinned source: base alpha `76 / 255f`, highlight alpha `1f`, duration `1000ms`. No reflection/private access is used. |
| RpnRemovalContent.kt | 1 | `colorSurface` is Material-owned; the app's non-transitive R class does not declare it. Resolve the same theme attribute through `MaterialR.attr.colorSurface`. |
| WgTextCatalog.kt | 4 | Four MaterialCheckBox text entries used the wrong `checkboxStyle` resource owner. Use AppCompat's attr, matching the MaterialCheckBox constructor; update the catalog generator so regeneration retains the correction. |

Only the five affected production files change. The edits retain callback
delegates, measurement/layout bodies, control indices, default style values,
placeholder geometry, animation-running state and dependency versions.

## Reproducible source checks

```sh
python3 scripts/compose-migration/check-screenshot-call-errors.py
python3 scripts/compose-migration/check-screenshot-attribute-errors.py --self-test
python3 scripts/compose-migration/check-shimmer-public-contract.py
python3 scripts/compose-migration/check-final-source.py --report docs/compose-migration/screenshot-error-fix-source-checks.json
```

The focused checks reject the reported argument/namespace/private-field
regressions. The full matrix also retains controller, protected-core,
zero-layout/binding and original-style checks. They inspect source only; no
Kotlin/AAPT/Gradle/Android test or UI execution is represented as passed.
The original 42-command report remains historical for its own source hashes;
the new screenshot-fix report covers this patch and its 45-command matrix.

## Source evidence

- [Compose Layout signature](https://github.com/androidx/androidx/blob/00750b0544d71f1267b80da4ded1c1b8322ae273/compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/layout/Layout.kt)
- [MaterialSwitch 1.14.0 superclass and inherited drawables/tints](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/materialswitch/MaterialSwitch.java)
- [MaterialCheckBox 1.14.0 default-style owner](https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/checkbox/MaterialCheckBox.java)
- [Shimmer 0.5.0 immutable source](https://github.com/facebookarchive/shimmer-android/blob/5ec9d4e73ef84696e6807488d7663a5d5ccf766d/shimmer/src/main/java/com/facebook/shimmer/Shimmer.java)

`shimmer-public-contract-source-report.json` preserves the tag/commit/blob and
original-layout evidence. `screenshot-error-fix-review.json` pins the bounded
independent review. A fresh F-Droid build is still required to confirm the full
compiler result and expose any later-stage errors; broader variant/runtime
acceptance remains unverified.
