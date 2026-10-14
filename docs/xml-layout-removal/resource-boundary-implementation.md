# Resource identity and zero-layout final gates

## Implemented source boundary

- `app/src/main/res/values/compose_legacy_ids.xml` contains exactly **2,516 unique, empty `type="id"` declarations**. These are every ID declared by the 186 inventoried baseline layouts. No View/layout resource or new behavior is created. No pre-existing non-layout declaration duplicates any name.
- `scripts/compose-migration/check-no-layout-resources.py` independently derives that exact ID set from immutable original git commit `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`, excluding only the four explicitly identified previously removed layouts. It does not trust the new ID file or a mutable generated manifest as its oracle.
- `scripts/compose-migration/check-binding-removal.py` now **defaults to FINAL_ZERO_LAYOUT_GATE**, fails on any remaining source layout XML, and requires the exact reviewed empty/invisible/lifecycle-disposed Compose notification shell. The old `android.R.layout` exception is removed.
- The binding guard has an explicit `--allow-retained-layouts` mode labelled **PRE_REMOVAL_AUDIT_NOT_FINAL**. This only allows the retained-file census for staged audit; it does not excuse binding/inflation/resource references, permit the former framework notification layout, or count as final acceptance.

## Independent guard coverage

The independent gate covers all repository source sets, test/debug/unknown flavors, qualifiers and other modules; XML resource aliases and tools references; Kotlin/Java app or framework `R.layout` references with aliases/static imports; dynamic resource kinds; direct/callable inflation; reflective layout/inflater names; embedded XML strings; renamed XML fixtures in assets/raw/test resources; additional/generated resource source directories; identity omissions, additions, duplicate declarations and wrong resource types. Source symlinks are rejected without reading their targets.

Provenance-only layout path strings are allowed because source pins are evidence, not executable fixtures. Any corresponding source-file/resource loading API in that file makes the path fail. Four previously reviewed lifecycle test hosts may retain their explicit programmatic View containers. They are narrow source-shape exceptions; adding inflation, numeric resource roots or changing the named root assignment still fails.

The ordinary Gradle `srcDir` local used to locate google-services metadata is not a resource source declaration. The only allowed resource wiring remains the existing main/full remapping and Android.bp main-resource directory. No Gradle build configuration was edited.

## Verified staging result

- Python AST parse: passed for both guards.
- Independent gate: **52 in-memory positive/negative controls passed**.
- Binding guard: **40 in-memory positive/negative controls passed**.
- Current explicit binding pre-removal audit: passed with no source violations.
- Default binding gate: intentionally **FAIL**, exit 1, on 186 remaining layout files.
- Independent final gate: intentionally **FAIL**, exit 1, on 186 files and their 37 internal include/tools links. No ID mismatch, executable reference or other source failure remains in the captured report.
- A separately labelled in-memory simulation omitting the 186 layout entries passes the independent gate, retains exactly 2,516 IDs, and confirms **zero files deleted on disk**. This checks dependency closure for the staged snapshot; it is not actual deletion, compilation or runtime evidence.

Reports: `no-layout-gate-before-removal.json`, `binding-final-gate-before-removal.json`, `binding-pre-removal-source-check.json`, and `no-layout-in-memory-removal-check.json`. Logs preserve the expected nonzero final-gate exits. The ID file SHA-256 is `da9bdbfc1e6b0514eaa386d596db5d51c4f1dcb59adc5026d58f30bd09a7b650`.

## Scope and limits

This worker changed only the new values ID resource, these two guards, and its audit documentation. It did not delete any layout, edit test/safety manifests/other guards, stage/commit, or run Android/Gradle/network operations. Notification presentation implementation and test-reference replacement are coordinated separately.

No lexical guard can prove absence of arbitrarily computed reflection or behavior in compiled third-party dependencies. Library/framework internal resources and the retained Android lifecycle/window bridges remain outside app-owned XML deletion. Final resource linking/compilation and runtime/visual validation remain unrun.
