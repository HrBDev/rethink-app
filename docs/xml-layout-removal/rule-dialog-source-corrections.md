# S19 / S20 / S34 bounded source corrections

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

## Changes

- Rule badges paint the retained `rectangle_border_background` drawable directly. It has a 16dp radius and 0.75dp stroke, no solid. Source controllers' background tint colors therefore recolor the frame only. List badges retain 25dp minimum width, 5dp padding and 5dp/10dp adjacent margins. Detail badges have an exact 32dp frame, bold 12sp text, and the source 11sp universal-IP exception.
- Domain/IP/import custom dialog content uses its original 10dp root and title padding. Source AppCompat Subhead title is 14sp; description is 11sp with start margin 5dp plus padding 5dp; error uses Body1 overridden to 12sp and padding 5dp. Import labels/counts use 12sp with the original 4dp row top padding, 8dp label/value gap, 12dp action-label top padding and middle-ellipsized filename. IP input outer horizontal margins are 20dp. Domain chip group is centered with 4dp top/8dp item spacing and 3dp child margins.
- DoH/ODoH fields have individual 10dp top margins, with no generic 8dp child spacing. Titles have 10dp padding. DNSCrypt keeps its heading inside the scroll content and its horizontal, centered radio group has 10dp padding. DoH/ODoH error reserves the localized original placeholder with 10dp LinearLayout margins and 5dp padding while invisible, and clears semantics until visible. DNSCrypt keeps the empty visible TextView's line reservation and 2dp padding. Existing validation, submit, selection, cancel, and dismiss callbacks are unchanged.
- Compound source keys and null decorative-control callbacks remain intact. The custom-rules search block retains SHA-256 `6d68d298a74016debf8baac405ab4bdacaed51c32b45ec1225d74c6799212587`.

## Constraint nesting matters

The custom rule error's bottom margin is unconnected, so it does not add a second gap before the following buttons' 10dp top margin. Its start/end/top margins apply. The IP input's unconnected bottom margin is likewise not converted to trailing Compose padding. DoH/ODoH/DNSCrypt buttons constrain bottom/end anchors only: their top margins, and the DoH/ODoH add button's start margin, are inert. The implementation applies connected bottom/end margins, avoiding duplicate 20dp gaps. DoH/ODoH error margins are all effective because the error is inside a vertical LinearLayout.

## Verification

`python docs/xml-layout-removal/check_rule_dialog_source_styles.py` passed. It checks pinned historical layout attributes from Git and retained non-layout resource bytes against the baseline, independent XML attributes/hierarchy, controller tint use, source tokens, unchanged callback literals against the staged snapshot, the search freeze, and rejects 21 deliberate regressions, including reintroduced layout dependencies and native-factory mutations. `git diff --check` passed for the three production files.

`RuleDialogSourceStyleTest` and `DnsEditorSourceStyleTest` are authored UNRUN. They use plain `Application`, independent typed AppCompatTextView/GradientDrawable factories derived from the pinned baseline, synthetic strings, and fake callbacks. Coverage includes themed stroke transparency, list/detail bounds, 2x RTL badge text, rule title/body/error sizes, invisible error measurement and semantics, Crypt empty line reservation, and fake apply/cancel behavior. No Android compile or execution occurred; no artifact/toolchain fetch, production host, VPN, database, or network traffic was started.

## Not established by this patch

- Alert-window shell size, shape, color and insets remain unverified. The existing shell was not replaced with another guessed theme.
- Inner TextInputLayout/EditText child metrics and native compound-label minimum heights are outside this surrounding-layout correction. Source domain input margins belong to the nested EditText, and are deliberately not misapplied around the entire Compose field.
- Adaptive FlowRow behavior at large fonts and full screenshots/TalkBack still require actual-host comparison.
- These source checks do not establish full visual equivalence or runtime clearance. The reference factories have no application-layout dependency, and the guard reads removed source layouts exclusively from the pinned Git tree.

## Zero-layout reference-fixture correction

The three former test inflations were replaced by `RuleDialogReferenceFactories`: programmatic AppCompatTextViews with native textAppearance, colors, padding, visibility and margin values, plus an independently built GradientDrawable with the original 16dp radius / 0.75dp stroke / no solid. No production Compose style function is reused by the reference factory. The guard pins the full factory source hash and rejects application layout IDs, inflater calls, encoded XML and production view bridges in these test/reference files. Test assertions are preserved; both test classes remain authored UNRUN. Production source was not changed in this correction.
