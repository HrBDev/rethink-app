# Compose regression follow-up (2026-10-14)

Compared the WIP migration series beginning at `8d4cd8258` with its pre-migration
parent `23cb6dd98`, including the subsequent control/chrome restorations and
`34b99756e` (About, Stats and DNS), `dedd5f473` (inset tests), and the two latest
compilation corrections. Existing restoration work is retained; no XML UI is
reintroduced and no controllers, persistence, VPN or account behavior is changed.

## Findings and fixes

- **About crash:** the emulator crash buffer records `Resources.NotFoundException`
  in `resolveWgText`, called by `AboutActionSubtitle`. Framework font families can
  be string resources. Only resources of type `font` now go to `getFont`; family
  strings still resolve through `Typeface.create`. Tests compare default text
  appearance against native TextViews across six app themes and scroll the whole
  About screen through the actions to its version/license footer.
- **About styling:** restored the XML footer's font padding, 3dp sponsor-caption
  line spacing, 4dp version/token line spacing, and source dimension/monospace
  typography instead of inherited Compose body typography.
- **Insets:** Home reserves status/cutout edges above its navigation viewport;
  navigation also reserves horizontal safe edges. The shared Activity/Fragment
  bridge reserves remaining safe drawing edges before AndroidView dispatches
  reduced insets to child Compose content. Merely enabling child padding had
  failed the real bridge regression test. About/Stats handle all remaining edges
  in standalone hosts. DNS settings, the protocol grid, the DNS heading/Fragment
  host, and console/event log screens now reserve their safe areas.
- **Progress runtime failure:** explicitly type both Material drawable branches
  as public `Drawable`. Kotlin otherwise generates a cast to their package-private
  common superclass and throws `IllegalAccessError`. Source indicator styling and
  animation behavior remain intact.
- **Event row runtime failure:** replace intrinsic-height measurement around a
  subcomposed label with an overlay stripe measured against the content. Preserve
  the 2dp stripe, 12dp gap, wrapping, expansion and long-press copy behavior.
- **Previously unrun test fixtures:** fix nullable native API usage, inaccessible
  CoordinatorLayout.LayoutParams construction, a missing ProgressBar qualifier,
  dialog host theme setup, and expectations for maxLines/INVISIBLE semantics.

## Verification

Passed `git diff --check`, Kotlin/resource compilation, and
`:app:assembleFdroidFullDebug` using the installed toolchain and offline cache.
The following targeted `:app:testFdroidFullDebugUnitTest` classes pass: **60 tests,
zero failures, zero skipped**.

| Class | Tests |
| --- | ---: |
| AboutPresentationTest | 13 |
| AboutReferenceLayoutTest | 7 |
| BlocklistWindowInsetsTest | 2 |
| RootChromeContentTest | 13 |
| DnsProtocolGridComposeTest | 1 |
| DnsSettingsComposeTest | 7 |
| SummaryStatisticsReferenceLayoutTest | 5 |
| TrafficLogsComposeTest | 5 |
| WgTextProgressSourceTest | 7 |

Inset coverage includes zero insets, status bars, asymmetric cutouts, gesture and
three-button navigation sizes, keyboard changes, and exactly-once padding through
the Compose/Fragment/Compose boundary. Other fixtures exercise RTL, larger fonts,
theme variants, navigation geometry, paging and event-row interactions.

Installed the final x86_64 debug APK over the existing emulator app without clearing
data. Android API 37 smoke checks verified Home and About rendering, scrolling
through About's footer, and opening Event Logs with populated rows. About content
starts below the status bar and ends above the anchored bottom navigation.

## Limits

The original XML appearance was compared through Git source and native reference
fixtures; a separate pre-migration APK was not installed for pixel-by-pixel image
comparison. This is targeted validation, not a full app test suite or all-flavor
release build. Physical devices, older Android versions, and every theme/window
configuration still need wider visual QA. Existing Gradle/test API deprecation
warnings remain. Historical source-only reports and their hash manifests are not
new runtime evidence and were not regenerated.

Pre-existing changes to root Gradle repositories, the staged tun2socks AAR and
untracked `.serena` content were preserved.
