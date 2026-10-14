# Compose migration validation status — 2026-10-14

**Work in progress. Not production ready. Binding removal and the latest layout/component-style corrections have not been compiled or executed.**

## Reported compiler errors after the source checkpoint

The user's F-Droid Full build screenshot reports 15 Kotlin errors in checkpoint
`02b826718295f054411b99912b4a00938ace263a`. They identify incorrect resource-owner
namespaces, one positional/named Compose `Layout` argument collision, and access
to package-private Shimmer fields. The targeted source fixes and regression
checks are documented in `screenshot-compiler-errors.md`. The earlier passing
source gates did not establish compilation and missed these errors. A new
Android build is still required; it cannot run here under the existing artifact
access blocker.

## Current source checkpoint: final XML removal and styling reconciliation

The latest source work removes **all 186 remaining app-owned XML layout files**. Together with the four earlier shell removals, no app-owned layout resource remains. `docs/xml-layout-removal/layout-removal-manifest.json` records each deleted file, original Git hash, original styling contract and Compose family or verified retired-owner disposition. All 2,516 layout-declared ID names/types remain as values-only resources. Manifest, drawables, colors, styles, strings and dependency-internal resources are retained.

Source corrections now cover anchored opaque navigation, Home section gutters and wide grouping, original chips/buttons/fields/search variants, transparent TextButton overlays, DNS/Stats custom controls, compound controls, slider interaction states, rule/DNS dialogs, cards/rows/tabs, WireGuard/RPN typography and source progress drawables. Search input requests preserve the native no-suggestions/auto-complete flags without constructing an Android editor. See `docs/xml-layout-removal/final-source-handoff.md` for exact boundaries and remaining validation.

The final source gates run with `python3 scripts/compose-migration/check-final-source.py`. Their current results and exact source hashes are in `docs/xml-layout-removal/final-source-checks.json`. These are source/mutation checks, not compiled tests. The protected-core guard separately checks 522 unchanged logic files and the original dependency pins. The user's lint commits remain ancestors of the checkpoint; their locale/resource fixes are retained.

The original 115-phone-root ledger and exhaustive layout/style inventory retain their runtime-unverified status. The old inventory/findings documents are historical preflight evidence; the current removal manifest and final handoff supersede their pending-deletion descriptions without retroactively claiming runtime equivalence. New tests and helpers are authored but unrun; previous runtime safety manifests are historical exact-source envelopes and must not be repinned or enabled without review.

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
