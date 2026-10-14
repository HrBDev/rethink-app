# DNS/blocklist reference restoration

## Evidence

Visually inspected both supplied screenshots before editing:

- Current image 3: `visual-restoration-1558/3/image(20261014-155712).png`, 345 × 747.
- Original image 4: `visual-restoration-1558/4/image(20261014-155714).png`, 756 × 1688.
- Original source: commit `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`, `fragment_rethink_blocklist.xml`, both simple/advanced row layouts, and the four blocklist adapters.

The current selector is flattened against the status bar. The original has a centered joined capsule with a full-height label and outline. Original unchecked cards have a dark teal fill; current unchecked cards nearly disappear into the page. Text and checkbox sizes are much closer when the different screenshot widths are normalized, so there is no blanket scaling or conversion from screenshot pixels to dp.

## Changes

- The full-screen blocklist screen now owns safe drawing insets. Its scrolling list remains between the selector and fixed action bar. The standalone Rethink list also consumes its insets; its bottom-sheet variant retains the existing inset owner.
- The selector retains the original 125dp segment minimum width, 16dp source corner radius, source colors, source stroke overlap, and selection callbacks. It is an explicit selectable group. Default native control height and text/style resolution are supplied by the shared original-control resolver.
- The section heading resolves the native `sans-serif-medium` plus bold face through the original TextAppearance rather than overwriting the typeface with an unrelated numeric Compose weight.
- Cards retain the exact original 12dp/6dp outer margins, 16dp corners, 6dp level strip, 16dp label inset, original descriptions, checked state and callbacks.
- `BlocklistCardAppearance.kt` preserves the original adapter's `background` and `selectedCardBg` inputs. It applies `ElevationOverlayProvider` using the native elevated-card theme and elevation. This restores the original surface tint without hardcoded screenshot colors. Simply replacing the background with a different theme attribute would not reproduce the native adapter.
- The action bar retains the original 40/60 weighting and 1dp separator gap, with a 48dp minimum height. Apply, discard, query, entry-link and download actions are unchanged.

## Verification performed

- `python scripts/compose-migration/check-blocklist-reference-restoration.py`: PASS, 15 source/resource contracts and 17 rejected negative mutations.
- `python scripts/compose-migration/check-dns-blocklists-parity.py`: PASS, 131 protected DNS/blocklist methods and callbacks, plus its existing negative control.
- `git diff --check`: PASS at the time of this review.

Machine-readable result: `blocklist-reference-restoration-results.json`.

Three synthetic regression tests were authored in `BlocklistsComposeTest.kt`:

1. Compare the unchecked dark card to an independently drawn native MaterialCardView, including a negative assertion that its color differs from the flat page.
2. Verify centered, full-height, joined selector geometry and selected-state switching.
3. Verify fixed footer geometry, original weight/gap allocation, and distinct apply/discard callbacks.

These supplement existing tests for row/checkbox callback cardinality, selected fill, optimistic/persisted state reconciliation, independent entry links, tabs, search and dialogs.

## Explicitly unverified

Android compilation, Robolectric execution, device rendering, screenshot/pixel equivalence and TalkBack are **UNRUN**. The existing toolchain-download restriction was respected. No Gradle, device, production Application, VPN, service, traffic, download, staging, commit or remote write was performed for this change. Source checks are not proof of visual/runtime equivalence.
