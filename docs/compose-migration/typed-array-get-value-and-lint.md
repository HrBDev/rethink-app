# Reported getValue compiler error and lint availability

## Confirmed error and correction

The supplied IDE screenshot shows `HomeChrome.kt`, one visible diagnostic:
`Unresolved reference 'getValue'` at line 128. Its header says four problems, but
the other three diagnostics are not visible. This is a Kotlin compiler error,
not a new safe-area screenshot, and not an Android Lint result.

`rootNavigationLabelTextStyle` calls the ordered attribute adapter to retrieve a
TypedValue for the source navigation label size. The adapter introduced in the
preceding styling repair omitted TypedArray.getValue. Added the exact mapped
operation:

```kotlin
fun getValue(index: Int, outValue: TypedValue): Boolean =
    values.getValue(order.index(index), outValue)
```

It preserves the requested declaration index, populates the caller's output
object, and returns the platform Boolean. This API has existed since Android
API 1. [TypedArray API](https://developer.android.com/reference/android/content/res/TypedArray#getValue(int,%20android.util.TypedValue)).
The existing independent single-attribute resource oracle now checks getValue's
presence result and output fields for all three acquisition overloads across six
themes. These Android tests are authored, **UNRUN**. The source guard rejects a
missing method or a direct, unremapped index. A confirmed unused TextButton import
in HomeChrome was also removed. No business logic, dimensions, palette or lint
suppression was changed.

## Current Android Lint result: unavailable

No current lint-results report or lint baseline was found in the checkout or
recovered validation trees. No Gradle distribution/dependency cache or Android
SDK is present after the environment reset. A Java 21 executable exists; the
project targets Java 17. The pinned build uses Gradle 9.6.0, AGP 9.4.0, Kotlin
2.4.10 and compile SDK 37.

The existing artifact-access policy restriction remains. No Gradle or download
was invoked. In particular, `--offline` does not prevent the Gradle wrapper from
trying to obtain a missing distribution, so it is not safe to treat that flag
as proof that this environment can run lint offline.

There is **no current Android Lint error/warning count, no successful Kotlin
build, and no corrected-device-render result** for this patch. Source checks and
source review are reported separately; they are not a lint pass.

## Configured coverage exclusions, unchanged

- `app/lint.xml` ignores every lint issue in all source-set values*/strings.xml,
  including default English, and globally ignores StringFormatInvalid and
  StringFormatMatches.
- `app/build.gradle.kts` ignores test sources, demotes MissingTranslation, and
  disables FragmentLiveDataObserve, FragmentBackPressedCallback and
  FragmentAddMenuProvider. Errors still abort; warnings are not errors.
- Security lint checks use `com.android.security.lint:lint:1.0.4`.
- Kotlin separately suppresses SENSELESS_COMPARISON.

These are existing exclusions, not newly detected errors or permissions to add
more suppressions. A future default lint pass must be interpreted with these
limits, and compiler/test checks are needed separately.

## Historical warning evidence only

Recovered `full-source-compile-2.log` records an earlier successful F-Droid Full
production compilation with 106 Kotlin warning lines: 39 deprecated API warnings,
39 unnecessary safe calls, eight unnecessary non-null assertions, two Java
nullability type mismatches, and other redundant-expression/cast warnings.
`full-source-compile-7.log` emitted 14 unit-test warnings while production was
UP-TO-DATE; it is not a new production warning total. An earlier TV compile log
has 93 warning lines. Provenance is in `validation-results.json`.

Those logs are historical Kotlin output, not Android Lint and not validation of
the current source. Do not apply their counts to this commit or hide current
errors behind their earlier successful build status.

## Reproducible validation once tooling is available

Only with an authorized, installed and fully cached toolchain:

```sh
./gradlew --offline --no-daemon --warning-mode all -Dorg.gradle.java.installations.auto-download=false -Pandroid.builder.sdkDownload=false :app:compileFdroidFullDebugKotlin :app:lintFdroidFullDebug
./gradlew --offline --no-daemon --warning-mode all -Dorg.gradle.java.installations.auto-download=false -Pandroid.builder.sdkDownload=false :app:testFdroidFullDebugUnitTest --tests '*OrderedStyledAttributesTest'
./gradlew --offline --no-daemon --warning-mode all -Dorg.gradle.java.installations.auto-download=false -Pandroid.builder.sdkDownload=false :app:lintFdroidTvDebug
```

Run Play/Website checks separately because existing build logic selects Firebase
behavior from the complete task-name string. Repository-wide `lint` also covers
the benchmark module. Inspect actual HTML/XML reports and Kotlin diagnostics;
no task above has been run here for this patch.
