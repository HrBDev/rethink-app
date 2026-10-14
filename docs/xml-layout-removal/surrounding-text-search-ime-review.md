# Surrounding text and search IME source review

**Verdict: approved for the bounded source changes at the hashes below.** No actionable defect remains in this reviewed diff.

## Reviewed corrections

- S30 source text sizes/colors, emphasized title appearance and card clearances are restored; the URL spacing false positive is resolved by source nesting: 8dp plus 2dp equals retained 10dp.
- S31 filter headings and reviewed firewall container/group insets match source attributes; S32 labels and one-line ellipsis preserve selection and callbacks.
- S33 heading appearance, title/subtitle/empty font sizes, selected title behavior, centered header row and empty text alignment match the reviewed source contracts.
- Both field overloads wrap the existing BasicTextField without substituting value callbacks, KeyboardActions, focus callbacks or keyboard options.
- The search interceptor invokes the underlying request once per connection request and returns the same InputConnection. It changes only the autocomplete/no-suggestions/autocorrect hint mask; session cancellation and all other EditorInfo metadata remain with Compose.
- Non-search fields and explicitly enabled autocorrection bypass the interceptor; non-text classes and all three password variations keep their inputType unchanged.

## Review findings resolved

- Centered empty text had wrap-content width. The empty Text now fills the parent width before its source padding and TextAlign.Center.
- FlowRow top-aligned and could wrap the subtitle, unlike the source RelativeLayout. A vertically centered Row reserves the subtitle width; title and selectable title wrapper use weight(1f). Selection remains intact.
- All firewall groups shared one inset despite different source parent/group margins and padding. Parent group has horizontal 10dp, top 5dp, bottom 2dp. Category/sort groups have combined horizontal 12dp and vertical 7dp.

## Checks and limits

Source checks pass: surrounding text 26 contracts / 9 rejected mutations; IME 18 contracts / 6 rejected mutations; search 34 checks; field callbacks 21 call sites / 96 baseline instances; bounded whitespace check. The two historical review artifacts still exactly match ff0510.

- Source-only approval for the bounded changes, not whole-layout or pixel-equivalence clearance.
- Kotlin, AAPT, Compose and Robolectric tests remain uncompiled/unrun. No actual IME or production application was opened.
- The authored IME tests independently inspect native SearchView flag masks and verify connection identity, request count, selected metadata and input-type exclusions. They have not executed and do not establish device IME behavior or cancellation/restart behavior.
- Font metrics, scaling, RTL, clipping, accessibility, hit regions and actual keyboard behavior remain runtime gates.
- Official upstream source and API documentation support API use; this is not a resolved-dependency build of the pinned BOM.
- The historical ff0510 button/search checkpoint and its 20-hash review remain immutable; this artifact independently reviews the later source changes.

## Official API evidence

- [Public interceptor forwards a request through the existing cancellable input session.](https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/platform/PlatformTextInputModifierNode.kt) Blob `0c76792823ec1a7520e380c60f3e4344c7d08aa3`.
- [Android request is a public fun interface returning InputConnection from EditorInfo.](https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/platform/PlatformTextInputMethodRequest.android.kt) Blob `92cb2064c6a16c70a8339eb2d11ad8e8101e5fa7`.
- [Legacy BasicTextField adapter forwards its request through startInputMethod and returns its own recording connection.](https://github.com/androidx/androidx/blob/androidx-main/compose/foundation/foundation/src/androidMain/kotlin/androidx/compose/foundation/text/input/internal/LegacyPlatformTextInputServiceAdapter.android.kt) Blob `57b710b24cc6324168196c8174cf4582b6664ae8`.
- [Public API reference records introduction in Compose 1.7.0, before the configured 2026.08.00 BOM.](https://developer.android.com/reference/kotlin/androidx/compose/ui/platform/PlatformTextInputInterceptor)

## Reviewed SHA256s

- `app/src/main/java/com/celzero/bravedns/ui/compose/dns/BlockFreeDnsContent.kt`: `0354d7086b9383e960940001da77503bebd0c2e23ee4fb8403456a1920b5080b`
- `app/src/main/java/com/celzero/bravedns/ui/compose/blocklists/BlocklistFilterScreen.kt`: `ab6d926cec96802228f71a142a0b039b97c793411cd271dd1b484bc6834d9e7a`
- `app/src/main/java/com/celzero/bravedns/ui/compose/firewallapps/FirewallFilterContent.kt`: `96b6b3bb760979c0333edc4ef1cd9fbb7d9b0bb8a948e61a70c447412e930fae`
- `app/src/main/java/com/celzero/bravedns/ui/compose/home/LogIntervalContent.kt`: `5d2e1d8ca4ada5d3d12e67133a7a0cc9cb908e8e7e7615813ca4eee209456d86`
- `app/src/main/java/com/celzero/bravedns/ui/compose/statistics/PagedStatisticsScreen.kt`: `059e66124a35c50ce40f0f748dcb07b0add147c7cc2c9abe1e05b63445183f87`
- `app/src/main/java/com/celzero/bravedns/ui/compose/fields/OriginalSearchIme.kt`: `a9778cff53ee2eb40a77af5d2024909d5d1d9067aaeaf201917414772e3ead63`
- `app/src/main/java/com/celzero/bravedns/ui/compose/fields/OriginalTextFields.kt`: `1f9a8c2966b4d3e5da58cbda26971d9548834e8c8048de0975923cb4a87a44ec`
- `app/src/test/java/com/celzero/bravedns/ui/compose/fields/OriginalSearchImeTest.kt`: `4279ff23b3be4d1390d3d4e94b66adff12a010f7955278202ea4167bb66e1eed`
- `app/src/test/java/com/celzero/bravedns/ui/compose/statistics/StatisticsHeadingSourceTest.kt`: `50dd22dfb645918d97400a7d2fde0ba6e1c02e0235d690a0ee6258e19df54c93`
- `scripts/compose-migration/check-surrounding-text-styles.py`: `79dda2b0a5642a7521b60c4db5e63c74746164a851ebaa4f95fd0491f4fb6571`
- `scripts/compose-migration/check-search-ime-source.py`: `360761df16d345924cf9f7aad723c738bbe12a6800ca0953b60196af87763a8d`
- `docs/xml-layout-removal/surrounding-text-corrections.md`: `23deb05a5dda8ff09f9b53a3b8346f625e3e8df793d052d2557716899f827397`
- `docs/compose-migration/search-ime-native-flags.md`: `27c5af3627a6e5628eec5446b7ce93a74163a1b8cfee095467b6191b8f75123b`
