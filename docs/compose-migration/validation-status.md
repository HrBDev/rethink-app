# Compose migration validation status — 2026-10-14

**Work in progress. Not production ready. Binding removal and the latest layout/component-style corrections have not been compiled or executed.**

## Current regression corrections

- Anchored opaque navigation and restored Home card gutters are durably checkpointed at `28435219d19d902862336a84fe734dcb584fa7dd`, tree `f219b14ab7767f5b1b4704c69612d280aa02c83d`. Its GitHub Actions run list was empty; there is no CI build result for that commit.
- This subsequent source checkpoint restores original chip/button/field families and stateful resources, segmented controls, passive badges, icon/FAB overrides, and the omitted Checkout plans/removal-badge labels. It retains native Compose, callbacks and business logic. See `original-controls-restoration.md`, `original-fields-restoration.md`, `component-style-restoration-review.md` and `icon-affordance-audit.json`.
- All 27 source/controller guards passed against an unchanged 1,576-file `app/src` envelope, SHA-256 `b99f1104df77f5b3c754fc64a6e4a0ee7c6377193e8f9842795a3e0df1d6a547`. The separate whole-production component coverage guard passes: no unreviewed direct calls or unresolved manual linkage obligations; 53 exact reviewed icon/FAB calls and eight byte-identical original TV files are explicit exceptions.
- Coverage describes source linkage, not visual equivalence. The 284 source-style keys, 271 generated styles, 96 XML field mappings, 117 preserved callback expressions, and 213 planned variant fixtures must not be counted as rendered passes.
- The cumulative safety manifest now contains 84 exact classes / 598 methods and keeps runtime clearance BLOCKED. New layout, field, control and passive-badge tests are authored only. The full current allowlist still requires toolchain/artifact revalidation, compilation and execution.
- The 115-original-phone-root ledger continues to mark unverified gates explicitly. Actual Fragment/NavHost inset-policy propagation, all-route visual/accessibility acceptance, and the existing wide Home logs-to-apps bottom-barrier layout gap remain unresolved.


## Durable source recovery

- Pinned upstream base: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9` (`gradle-improvements`).
- Historical recovery checkpoint: `87b3b9975afdb59492bc92cf97b9560cdd725330`, tree `1949435b20c49bb03ec06123c27f913a4974882b`.
- After a workspace reset, all 1,782 source files were recovered and checked against their Git blob hashes. The recovered 1,460-file production/content-test envelope is `e7b89a0d41f650816063ca2c39b57963bfb3d9a4a4b6f79e5c8cf081411164fb`.
- Library version 6 archive was independently verified as SHA-256 `1275dd10f406ab2605eade25a2754077e50d9fea2f1c529ebfc64df903fb6441`. It retains earlier source and logs, while the newer source was recovered from GitHub's completed tree upload.
- The last upload's final documentation/script chunk never ran. Three missing guard scripts have since been restored to their recorded exact hashes. Historical reports are retained as historical, not current acceptance results.

## Results observed before the reset

These results do **not** validate the newer binding-removal, layout or component-style edits.

- Pinned baseline F-Droid Full main compilation passed.
- The final pre-reset F-Droid Full production, unit-test and Android-test Kotlin compilation, plus F-Droid TV production Kotlin compilation, passed. Earlier successful build logs are retained in Library version 6; the final build log itself was lost.
- Welcome's 20 isolated actual-host tests passed. The larger actual-host run passed 156 of 157 cases; the remaining AppInfo fixture expectation was corrected and its focused 29-case class passed, alongside six utility tests. A single final all-host run on the latest source is still required.
- A 382-case content run passed 370 cases. All twelve failures were addressed as fixture issues. A 35-case rerun passed 34 cases, exposing one paging-wait timeout; that wait was corrected without changing ordering/cardinality assertions.
- The final 399-case content run started with its exact reviewed source envelope. Its terminal result was lost during the reset. It is **unknown**, not a pass, and must be rerun.
- Six rendering methods passed their content/action/accessibility assertions across 48 baseline/native capture pairs. Thirty-two WireGuard action-label contrast checks passed at 5.099–19.121:1. The final PNGs were not durably uploaded before the reset and are no longer available; regenerate them before presenting downloadable rendering evidence.

## Binding removal and completeness

The previous checkpoint still had two generated-binding Activity shells: DNS Detail and Firewall, each containing a hidden TabLayout and a single-page ViewPager2 whose child rendered Compose. The other remaining XML shells were Home, FragmentHost, and ConfigureRethinkBasic. They were documented framework exceptions, so a statement that the entire source was already binding-free would have been incorrect.

The user explicitly requested removal of binding. The current work replaces those shells, removes unused binding dependencies and dormant binding code, and performs a whole-source renderer audit in addition to the 115-original-phone-root ledger. Framework-only Fragment containers and system-owned window/notification resources are identified separately. See `binding-removal-audit.md` and `binding-removal-review.md` for the current source checks and any unresolved findings.

The user subsequently requested restoration of original chip, button and field styling; that overrides the earlier broad acceptance of newer Material appearance for these controls. Other intentional Material spacing changes are not automatically failures. Missing content/actions, clipping, unreadable contrast, RTL/large-text/accessibility, navigation/state and business-logic regressions remain failures.

## Build blockers and remaining acceptance

- Java 17, the Android SDK and Gradle caches were lost in the workspace reset. Only system Java 21 remains available.
- The prior explicit policy denial was: `Network access to "https://dl.google.com:443" was blocked by policy.` No retry or alternate artifact source has been used.
- Offline Play configuration was missing Google Services 4.4.4, Firebase Crashlytics Gradle/build tools 3.0.7 and strict-version-matcher 1.2.4. Play and Website builds remain unverified.
- Offline F-Droid APK assembly was missing `desugar_jdk_libs` and its configuration artifact, both 2.1.5. No APK was produced.
- Restore authorized toolchain/dependency access; compile the current F-Droid Full/TV production and test variants; rerun the reviewed content and actual-host allowlists, including new binding-removal restoration tests; regenerate and inspect rendering evidence. Revalidate the exact test safety/source hashes first.
- Physical-device TalkBack, IME/keyboard, magnifier, OEM/system-window and performance behavior remain unverified. No production Application, actual VPN, user traffic/probe, account, permission or export operation is part of these isolated tests.

No PR, merge or deployment has been performed. Static source guards are supplementary evidence, not runtime or full-migration completion proof.
