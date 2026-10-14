# User lint integration and frozen checkpoint

This is an uncompiled, partial WIP recovery checkpoint. It preserves user commits `154065c0467f9f47be5acf8b58fc15e6bdca8a31` (lint fixes) and `c87ee9a8df50d82537613a0f123f0f7afec036a4` (benchmark build ignore), with the latter as its direct parent. The XML-preparation and original-layout corrections remain bounded to the previously frozen checkpoint.

## Preservation review

- All 21 lint-change paths were independently checked: 18 are byte-identical to the user's lint commit. Home, country rows and RPN statistics retain both the frozen presentation corrections and the user's exact locale/string-resource changes.
- Country locale integration is restricted to the composable country-code label. `sortCountries` and `sectionLetter` retain their original `Locale.getDefault()` expressions, matching the user's commit; an overly broad earlier conflict resolution was corrected before publication.
- `.gitignore` is byte-identical to `c87ee9a`, including `/benchmark/build/`.
- The statistics-row guard still requires whole-file equality after a closed set of renderer substitutions and the user's exact `stringResource` import/call changes. Two additional negative controls reject removing either lint fix. No controller waiver was added.

## Source-only checks

- Home: 145/145 controller checks; 23 layout contracts and 15 rejected mutations.
- Statistics: 32 controller methods; 13 row contracts and 9 rejected mutations.
- History/RPN: 37 styling/controller assertions and 5 rejected mutations.
- Notification: exact renderer substitutions only, with routing/security/lifecycle preserved.
- Protected core: 522 files unchanged; retained dependency pins and aliases unchanged.
- Reference fixtures: 73 nodes, 23 unchanged test methods/assertion lines and eight visual configurations; zero app layout references in tests.
- Binding staging audit, explicitly using `--allow-retained-layouts`: 40 controls; 789 production source files; 186 retained XML layouts; zero violations. This is not final zero-layout acceptance.

Android, Gradle, Kotlin, AAPT, Robolectric, device and visual execution remain blocked/not run. The source/runtime authorization manifest is unchanged: `source_review_complete=false`, runtime clearance `BLOCKED`. No manifest repinning, toolchain download, production app execution, PR, merge or deployment is part of this checkpoint.
