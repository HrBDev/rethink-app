# Partial reconstruction checkpoint: 2026-10-14

This branch is work in progress. It is not a completed migration or production-ready release.

## Reproducible observations

- Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9` (`gradle-improvements`).
- Untouched baseline `:app:compileFdroidFullDebugKotlin` passed with the approved Java 17 toolchain and Android SDK 37.
- An earlier frozen partial migration passed main Kotlin/Java and unit-test compilation, and 30 explicitly selected isolated tests passed: RpnControlStateTest (8), SettingsStateParityTest (8), WgControlStateTest (10), WgFormRestoreTest (3), WgLegacySearchParityTest (1). These results apply to that earlier snapshot, not every later group in this checkpoint.
- This larger checkpoint adds protocol DNS, custom rules, app details/list, statistics and traffic-log groups. Its main Kotlin/Java and unit-test compilation passed. A selected batch of 46 isolated Home, navigation/dialog/snackbar and statistics tests passed; content cases used native graphics, including RTL/large-text checks. The batch found and corrected heatmap semantics merging. Other groups remain unexecuted.
- The protected-core source gate currently verifies 522 baseline-identical backend/manifest/TV files and unchanged original version pins. Source gates are supplementary evidence, not behavioural proof.
- No production Application, VPN, real traffic/probe, permission/account operation or device test has been launched.

## Still incomplete

Utility screens, network settings/backup/Orbot, About/diagnostic dialogs, and RPN configuration are under reconstruction in separate working files. These may still be baseline View implementations in this coherent checkpoint. Screen/route ledger coverage, complete safe host tests, baseline-vs-Compose visual/accessibility/RTL/theme/large-text comparison, recreation/search equivalence and all flavour compilation are pending. Existing framework navigation/window/container hosts and external QR capture require documented exceptions. Original system/notification/widget resources are retained.

The earlier lost checkpoint is not claimed recovered. This source was reconstructed from the pinned branch. No PR, merge or deployment has been made.
