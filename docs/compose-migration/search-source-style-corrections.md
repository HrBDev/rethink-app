# Search source-style corrections

Status: source corrections and static checks only. No Android build, lint, native/Compose test execution, runtime app launch, or screenshot parity is claimed. The existing toolchain/runtime safety gates remain blocked. No XML was deleted. No VPN, account, or user-traffic activity was performed.

## Source defects and corrections

- Shared Search, AppLogSearch and ServerSearch incorrectly used generic `editTextStyle`, forced ordinary search text to 16sp, and imposed the generic 48dp field minimum. They now resolve `autoCompleteTextViewStyle`, including TextAppearance colors and text metrics, with only the app's explicit 14sp overrides. The server path keeps its 40dp source root and removes font/editor padding as the original controller did.
- The original `queryBackground` was loaded with raw TypedArray drawable lookup and drawn under the entire row. Resource-backed values now use `AppCompatResources.getDrawable`, matching AppCompat's TintTypedArray path. The drawable is on a separate inner query plate, after the leading magnifier, and receives SearchView's focused/empty state. The server query background is retained.
- Uniconified search layout restores the 8dp edit-frame margins, native magnifier slot/margin, 36dp editor minimum and editor padding, and native clear width. The app-log search still omits the search icons. Caller query/focus/clear callbacks are preserved.
- Domain/IP Rules restore the baseline MaterialCardView parent, 16dp radius, 4dp margins and 90:10 search/delete width split; ordinary search typography inherits the source style and the app-specific 14sp override remains.
- RPN selection restores `RethinkPlus.ContentCard` (theme cardBackgroundColor, 16dp radius, zero elevation), 8dp horizontal/6dp vertical inner padding, and a 40dp search. The existing server clear control is moved inside the query plate. Unrelated dirty RPN history/skeleton changes are preserved.

## Pinned app evidence

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

- `app/src/full/res/layout/fragment_custom_domain.xml`: cda_search_container, cda_search_view, cda_search_delete_icon.
- `app/src/full/res/layout/fragment_custom_ip.xml`: cip_search_container, cip_search_view, cip_search_delete_icon.
- `app/src/main/res/layout/fragment_server_selection.xml`: search_card, search_bar, search_filter_btn.
- `app/src/main/res/values/styles.xml`: RethinkPlus.ContentCard.
- `app/src/main/java/com/celzero/bravedns/ui/fragment/ServerSelectionFragment.kt`: setupSearchBar.

Official AndroidX source was read through the GitHub connector, not downloaded as executable/runtime artifacts. These are upstream source blobs, not a claim that an AndroidX source build or the pinned dependency was run:

- abc_search_view.xml, blob `33d25d9ec40b10fc7a1b6cf5b615252dba1b802c`: https://github.com/androidx/androidx/blob/androidx-main/appcompat/appcompat/src/main/res/layout/abc_search_view.xml
- SearchView.java, blob `aef35709ec14ac7ab09bf41344610cbe59c6bf56`: https://github.com/androidx/androidx/blob/androidx-main/appcompat/appcompat/src/main/java/androidx/appcompat/widget/SearchView.java
- AppCompatDrawableManager.java, blob `e894c47e26f6d29ee7bf5a89527d8d7d55152b94`: https://github.com/androidx/androidx/blob/androidx-main/appcompat/appcompat/src/main/java/androidx/appcompat/widget/AppCompatDrawableManager.java
- TintTypedArray.java: https://github.com/androidx/androidx/blob/androidx-main/appcompat/appcompat/src/main/java/androidx/appcompat/widget/TintTypedArray.java

## Verification

- `python scripts/compose-migration/check-search-source-styling.py`: PASS, 34 narrow pinned-source checks including seven keyboard-default mutation controls. Report: `search-source-style-report.json`.
- `git diff --check`: PASS at this correction checkpoint.
- `check-custom-rules-contracts.py`: PASS, 19 unchanged controller methods and 9 unchanged rule data/manager boundaries; refreshed custom-rules-source-report.json.
- `check-original-fields.py`: PASS, 21 call sites and 96 pinned-baseline field instances. The exact user lint commit `154065c0467f9f47be5acf8b58fc15e6bdca8a31` resolves the previously reported locale-argument drift; no behavior waiver is used.
- `OriginalSearchStyleReferenceTest`: seven authored, UNRUN tests. Independent real SearchView oracles compare six app themes, source root height, editor colors/padding/highlight, query-plate state/drawable pixels, server overrides and typography. A synthetic Compose test checks 40dp bounds through edit/clear/focus without extra change callbacks. This is not runtime clearance or visual-equivalence evidence.
- Existing `OriginalTextFieldsTest` now expects the source 40dp lower bound for ServerSearch rather than the generic 48dp bound. Other families are unchanged.

The new test class has not been added to any runtime safety allowlist or marked reviewed/executable; it requires the same independent safety/toolchain review as the rest of the suite before execution.

## Bounded keyboard-default correction

The pinned server XML explicitly sets `android:imeOptions="actionSearch"`. The upstream AppCompat default editor XML sets `android:imeOptions="actionSearch"` and `android:inputType="text|textAutoComplete|textNoSuggestions"`. The historical commit `5e1cddb861d7c49fda89354edbcf1637b43af064` (2019-11-11) was read through GitHub, and its XML blob exactly matches the previously read current source: `33d25d9ec40b10fc7a1b6cf5b615252dba1b802c`. The path history has no later source change. Evidence is recorded in `search-keyboard-source-contract.json`; no Android library artifact was fetched or run.

Both OriginalTextField overloads now fill only missing search-family keyboard options: `ImeAction.Unspecified` becomes Search and a null `autoCorrectEnabled` becomes false. Explicit Default/None/Done/Next actions, true/false autocorrection, keyboard type, capitalization, platform IME options, focus preference, locale hints and caller KeyboardActions are preserved. Non-search families receive their original options object unchanged. No controller callbacks or call-site options changed.

Important remaining limit: official Compose Android input translation only omits TYPE_TEXT_FLAG_AUTO_CORRECT when autocorrection is false. This change does not set native TYPE_TEXT_FLAG_NO_SUGGESTIONS or TYPE_TEXT_FLAG_AUTO_COMPLETE, and does not claim identical keyboard suggestion behavior or bit-for-bit EditorInfo parity. Achieving those flags would require separate investigation beyond this bounded public-options correction.

Three additional UNRUN tests independently inspect the real SearchAutoComplete action/input flags, verify explicit overrides/non-search pass-through, and check both Compose overloads' Search/Done semantics and exact caller callback counts. The static guard now checks primitive dispatch directly and rejects seven deliberately mutated source variants; this is source-guard validation, not Kotlin execution.
