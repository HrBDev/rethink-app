# Reproducing the current checkpoint checks

This is an **uncompiled WIP** checkpoint. Run commands from the repository root. Do not substitute an unfiltered unit-test task or device/instrumentation run for the reviewed filters below.

## Static checks available without Android tooling

```sh
python3 scripts/compose-migration/check-binding-removal.py --self-test --report docs/compose-migration/binding-removal-report.json
python3 scripts/compose-migration/check-protected-core.py
python3 scripts/compose-migration/check-navigation-chrome.py --self-test
python3 scripts/compose-migration/check-protocol-dns-parity.py
python3 scripts/compose-migration/check-original-test-relocation.py --self-test
python3 scripts/compose-migration/check-apps-controller-parity.py
python3 scripts/compose-migration/check-wireguard-controllers.py --self-test
git diff --check
```

The baseline Git object `23cb6dd98805445174bd83b7237ad5f5d2c24ac9` must exist locally for the source-comparison guards. These scripts inspect source and may write their documented JSON reports. They do not execute Kotlin or start the application.

## Current complete source-only gate matrix

```sh
python3 scripts/compose-migration/check-final-source.py
```

This executes only the 44 explicitly listed Python source checks plus `git diff --check`. It does not run Gradle, Kotlin, Android, a native backend, VPN or any live flow. Keep the exact pinned baseline Git object locally; no retained layout files are needed. Individual checks include strict zero-layout/binding/resource-reference gates, original-style contracts, independent reference-fixture checks, targeted negative controls and protected/controller source comparisons. The resulting report pins current `app/src` source hashes and labels all Android tests UNRUN.

## Compilation after toolchain access is restored

Prerequisites: the branch's **already installed and verified Gradle wrapper distribution**, Java 17, Android SDK platform 37, the required build tools and dependency artifacts. A wrapper can download its distribution before Gradle processes `--offline` or an init script. Do not invoke `./gradlew` until its distribution is locally present and verified; an independently verified local Gradle binary matching the wrapper version is another execution prerequisite, not a way to bypass a denied download. Use the existing version catalog; do not upgrade dependencies to make a check pass. The workspace reset removed the installed toolchain and caches, and the explicit `dl.google.com:443` policy denial remains unresolved. The following offline command therefore cannot currently complete here; it does not authorize retrying blocked downloads.

```sh
export JAVA_HOME=/absolute/path/to/approved/java-17
export ANDROID_HOME=/absolute/path/to/approved/android-sdk
env -u JAVA_TOOL_OPTIONS -u _JAVA_OPTIONS -u JDK_JAVA_OPTIONS ./gradlew --offline --no-daemon --no-configuration-cache --max-workers=2 \
  -Pandroid.builder.sdkDownload=false \
  -Porg.gradle.java.installations.auto-download=false \
  -Porg.gradle.java.installations.paths="$JAVA_HOME" \
  :app:compileFdroidFullDebugKotlin \
  :app:compileFdroidFullDebugUnitTestKotlin \
  :app:compileFdroidFullDebugAndroidTestKotlin \
  :app:compileFdroidTvDebugKotlin
```

Compiling Android-test sources does not run them. APK assembly, Play/Website compilation and device testing are separate, still-unverified gates. The exact previously missing artifacts are listed in `validation-status.md`.

## Isolated JVM tests: blocked until launcher revalidation

Read `binding-removal-review.md`, its exact JSON safety allowlist, `host-safety-review.md`, and `content-test-safety-review.md` first. The latest source/hash allowlist supersedes older pins; all original plain-Application, strict fake, empty/synthetic data, no native backend and teardown restrictions still apply.

The old isolated Gradle init script was lost. Its source-only replacement is `scripts/compose-migration/isolated-tests.init.gradle`. It fails closed while the manifest's runtime clearance is BLOCKED, and verifies exact class filters, source/configuration/launcher hashes, two startup agents, offline local Android runtime jars and a fresh synthetic home. It has not been executed here. Its startup-agent, dependency-cache, toolchain and native-graphics setup must be independently revalidated before the manifest can receive runtime clearance. Do not bypass its gate, enable dynamic attachment, or run the production Application to work around a fixture failure. Mockito 5.23.0 and Byte Buddy Agent 1.18.2 are test-process startup agents, not permissions to change host security settings; their exact artifact hashes are enforced by the launcher.

Once those preconditions are satisfied, use the exact reviewed manifest hash and artifact paths below. The current checked-in BLOCKED manifest intentionally makes this command fail; do not change the clearance merely to make it run. The runtime reviewer must supply the updated manifest/hash and local Android runtime artifact pins after verification.

```bash
mapfile -t classes < <(python3 -c 'import json; print("\n".join(json.load(open("docs/compose-migration/binding-removal-safety-allowlist.json"))["allowed_classes"]))')
filters=()
for class in "${classes[@]}"; do filters+=(--tests "$class"); done
env -u JAVA_TOOL_OPTIONS -u _JAVA_OPTIONS -u JDK_JAVA_OPTIONS ./gradlew --offline --no-daemon --no-configuration-cache --max-workers=2 \
  -I scripts/compose-migration/isolated-tests.init.gradle \
  -Pandroid.builder.sdkDownload=false \
  -Porg.gradle.java.installations.auto-download=false \
  -Porg.gradle.java.installations.paths="$JAVA_HOME" \
  -PcomposeMigrationManifestSha256=REVIEWED_MANIFEST_SHA256 \
  -PcomposeMigrationMockitoJar=/absolute/path/to/reviewed/mockito-core-5.23.0.jar \
  -PcomposeMigrationByteBuddyJar=/absolute/path/to/reviewed/byte-buddy-agent-1.18.2.jar \
  -PcomposeMigrationAndroidRuntimeDir=/absolute/path/to/reviewed/android-runtime-jars \
  :app:testFdroidFullDebugUnitTest "${filters[@]}"
```

Start Gradle in the shown sanitized process environment: inherited Java agent/options variables can affect the launcher before an init script has a chance to reject them. Visual output is forcibly scoped to build/outputs/compose-migration-visual. Never use a package wildcard or run an unfiltered task. Verify every listed source, helper and relevant production hash before execution; any drift requires renewed boundary review. Capture the complete terminal log and JUnit XML and store them durably alongside the exact source tree before reporting a pass.

Regenerate visual artifacts with the reviewed rendering classes and inspect required content/actions, clipping, contrast, RTL and large text. The pre-reset captures are historical results; their PNG bytes are no longer available. Physical-device TalkBack, IME/keyboard, native magnifier, OEM/system windows and performance checks remain separate and unrun.
