# Compose UI migration: partial work in progress

Base: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9` (`gradle-improvements`).

This branch contains incremental reconstruction of the native Compose UI migration. It is incomplete and is not production-ready. Earlier local-only work did not survive a workspace restart; no old validation result is claimed for these reconstructed bytes.

## Current validation

- Untouched pinned baseline: `:app:compileFdroidFullDebugKotlin` passed with the branch dependency versions, Gradle 9.6, Android SDK 37.0 and Java 17 project toolchain.
- Migration: compiler triage is active. Individual source-preservation guards and isolated host tests are documented by feature. Source checks do not establish complete migration, compilation, runtime or visual parity.
- Runtime app/security/network flows have not been executed. No permissions, VPN, DNS, rule storage, networking or account behavior change is intended.

## Remaining work

Complete all remaining phone routes, dialogs and settings; rerun all protected-core/source guards; compile every relevant flavor and TV; compile isolated tests; execute plain-Application content/callback tests; compare baseline and Compose rendering for normal/wide widths, font scales, RTL and themes; review restoration, search and accessibility. Keep XML used by retained framework hosts or system components until reference checks justify removal.

Incremental Git checkpoints and a separate source archive protect the work while this proceeds. A checkpoint is not a release or a pull request.
