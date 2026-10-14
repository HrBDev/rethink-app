# Independent source review: notification, Home, statistics and card styling

Verdict: **source review closed for the exact file hashes below**. This is not compilation, runtime or full visual-parity approval.

Four Home issues were found and repaired during review: packed metric-column alignment, missing first-row height while the rule pager has no adapter, missing empty-pager weighted space before dots, and Android 14+ nonlinear px→sp conversion. All four repairs are present in the captured source.

Notification exact-substitution check passed; all dispatch/security/lifecycle/controller code is unchanged beyond its native dialog alias and empty, invisible, lifecycle-disposed Compose shell. Statistics retains independent summary/RPN/blocklist-app/blocklist-domain scalar contracts and unchanged caller data/callbacks. The inspected OriginalCard merge, compat/corner/elevation and physical-RTL padding paths have no additional concrete blocker in this bounded review.

Fresh source-only checks: notification PASS; Home 23 contracts / 15 negative controls PASS; statistics 4 layouts / 13 contracts / 7 negative controls PASS. The resource group separately passed 52 independent guard controls and 40 binding controls. Final gates still fail intentionally on 186 on-disk XML files (plus their 37 include/tools links); 2,516 preserved IDs are verified independently against the immutable baseline. No layout was deleted by this worker.

Remaining limits: Android/Kotlin/resource linking, all authored tests, API34 nonlinear font behavior, actual shadow/clipping/theme-state rendering, RTL/large-font/device/accessibility behavior are unrun. No Application/VPN/account/permission/traffic operation occurred. Other worker edits outside this file list were not independently approved.

## Exact reviewed files

| File | SHA-256 |
| --- | --- |
| `app/src/main/java/com/celzero/bravedns/ui/NotificationHandlerActivity.kt` | `f9137558c155766bc7c754160cd46d5b02e19c92d09d24687916b1d646cb05fb` |
| `scripts/compose-migration/check-notification-ui-boundary.py` | `ff8dae1e0f1a0f6e66ace4d5fa8becf19612aebc44e2099c44fffd5e574b7687` |
| `app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeContent.kt` | `6d026eb39ab73fcb85587270e6790e8e4290a3bee97dab0ed1311d650240a57e` |
| `app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeCardSpacing.kt` | `231e8d63481936168f01484d2d46bc12010e4fe9823e343ffccfebbab02bfa63` |
| `app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeTextContract.kt` | `100353d8755e62e0990fe195873092019bc77765bf1ab6fbf57c3245fb0712ad` |
| `app/src/test/java/com/celzero/bravedns/ui/compose/home/HomeSectionSpacingTest.kt` | `1f3fb7c7bf81db5890e82ce0bfebdbc4f33c4224cb60b805d436655f61cfa6b9` |
| `app/src/main/java/com/celzero/bravedns/ui/compose/statistics/StatisticsRow.kt` | `6c437576a86c29e5784d70d1dcf1ceafa509340d30be4b7d961d02132601c607` |
| `app/src/main/java/com/celzero/bravedns/adapter/BlocklistAppsAdapter.kt` | `1f06ddc12601a331cb79d8870c2f990d687f91575cb6c02aea7076c7dddf88fa` |
| `app/src/main/java/com/celzero/bravedns/ui/bottomsheet/RpnStatsBottomSheet.kt` | `17d8a5af922a3496843b4717b46c960e7d099a1df1f99b19e2f77ee0ff40aa8a` |
| `app/src/test/java/com/celzero/bravedns/ui/compose/statistics/StatisticsContentTest.kt` | `7ee6ecc1f57f75af2a6d39ab075c4350f70393f98769a01a15067a133d8f9d81` |
| `app/src/main/java/com/celzero/bravedns/ui/compose/controls/OriginalCard.kt` | `8d2174cc06c02308758241992d84a7f39d6c21c338567fad12d70d86fae1d5eb` |
| `scripts/compose-migration/check-home-section-spacing.py` | `debeec541e2b66856ede4bef8e83d3583712d52385a0a88871f5f0ed96da5a27` |
| `scripts/compose-migration/check-statistics-row-styles.py` | `ec3fd1e862f219b11e4c34d81289e4fd88a7e533d8690b0625c5c658c4d5a649` |
| `app/src/main/res/values/compose_legacy_ids.xml` | `da9bdbfc1e6b0514eaa386d596db5d51c4f1dcb59adc5026d58f30bd09a7b650` |
| `scripts/compose-migration/check-binding-removal.py` | `7117b55c990c1ed917134bf8624741d4fe92e9d299ad0ace210e66e8515fc4cf` |
| `scripts/compose-migration/check-no-layout-resources.py` | `d984b58df7cef0ab2136c50a5d0ac50cdadaf078e443423ce968725e8387aeea` |

Detailed findings and execution limits: [independent-boundary-review.json](independent-boundary-review.json).

## One-line post-freeze card correction

At 12:33:35 UTC, verified only OriginalCardSurface’s final border expression changed: explicit-or-themed border is resolved first, then width ≤ 0dp becomes no border. Explicit zero therefore suppresses the theme stroke without drawing a Compose hairline. Reversing that exact line reproduces the prior SHA-256 `200337a815fcf0ebb9857c9f39a8b2d29166b8c9dcf63e3505f45c1602b65e74`; no other file byte changed. Current hash is recorded in the table. Source-only approval; no runtime or compile execution.
