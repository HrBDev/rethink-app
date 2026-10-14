# Network sheets native Compose migration

Scope: OrbotBottomSheet, ProxyCountriesBtmSheet, BackupRestoreBottomSheet, RethinkInRethinkWarningBottomSheet. Native content and view-free states live in `ui/compose/networksheets`; no AndroidView, XML content host or RecyclerView presentation remains in these controllers. Bottom-sheet Fragment/window lifecycle remains the framework host.

## Preserved controller behavior

The pinned baseline is `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`, read explicitly with git show. `scripts/compose-migration/check-network-sheet-controllers.py` verifies 64 protected braced bodies with two explicit presentation substitutions: MaterialAlertDialogBuilder to the shared native WgAlertDialogBuilder, and icon clearAnimation to native animation state reset. The evidence JSON records hashes and exclusions. It also verifies that six original XML fixtures remain identical to the baseline.

- All Orbot proxy/VPN/Tor checks, selected-app validation, status observer, event logging, stop/start calls and dispatch order remain in the original controller methods. Radio clicks check before dispatch and never toggle off; row callbacks are separate because their selected-state branches differ. Both mode remains initially GONE, including Android Q and later. HTTP remains hidden before Q. Loading gates clicks without changing the original enabled visual colors.
- Native loading reproduces the original accelerating two-second animation, four Y-axis turns and 20-pixel camera depth. Animation is transient. Independent checked fields save/restore in XML order after onViewStateRestored, without introducing side effects.
- Orbot stop dialog retains its positive/negative/conditional-neutral callbacks and their explicit dismissal/navigation order. The native info dialog keeps no-dim, back/outside dismissal, max-width cap, selectable HTML description and close control.
- OrbotInfo typography resolves actual XML appearances: AppCompat Headline plus heading_font_text_view (18sp) for title, Subhead plus large_font_text_view (14sp) for description. It preserves resolved family/style/spacing/color, includeFontPadding, 5dp extra line spacing, underline and italic spans. Text is not substituted with Material3 headline/body defaults. Warning description's 1.45 line multiplier uses font metrics rather than multiplying the sp font size.
- Country row and radio each invalidate presentation before the original single callback. Existing DOMAIN/IP writes, toasts, dismissal listener and unimplemented APP branch remain unchanged. Country selection is re-read at the native rebind, as with the old adapter; no additional data mutation or completion refresh was added.
- All backup/restore activity-result dispatch, URI grants, extension validation, WorkManager IDs/policies/observation, retries, cancellation-as-failure handling, VPN stop, restart delay and process exit remain byte-token identical after the dialog-builder substitution. Selectable version text freezes/restores; no work is initiated by composition/restoration.
- Warning action callbacks and dismissedByAction ordering are unchanged. Back, outside-tap and swipe still route through the retained framework onDismiss/onCancel behavior; action callbacks still precede dismissAllowingStateLoss.

## Authored verification

Fifteen fake-only Robolectric tests in `NetworkSheetStateTest`, `NetworkSheetContentTest`, and `OrbotInfoTypographyTest` use a plain Application. All specify native graphics. They instantiate only presentation objects, the Compose test host, local callback recorders and offscreen baseline XML TextViews. No production Application, Activity, Fragment, Koin/service, VPN, network/probe, account, permission, database, export, backup, restore or filesystem operation is launched.

Coverage includes radio-before-click ordering, repeat click behavior, row/radio distinction, loading and API visibility, independent restored checks, transient-state exclusion, selectable version restore, rebind selection, backup/restore callback separation, warning actions, light/dark/Frost, RTL/large text, info close, multiline overflow and exact XML typography metrics at two font scales.

## Current evidence and limits

Source guard and diff whitespace checks pass. Gradle/build/test execution is owned serially by the coordinator; this worker has not run Gradle. Compilation and the fifteen authored tests therefore require coordinator confirmation. Pixel-perfect baseline screenshots, actual device TalkBack/focus, physical swipe/back/system-window interaction and production process recreation are not claimed as verified. No live behavior exercise was performed because protected services and operations must remain untouched. Source equality and fake presentation tests do not independently prove full lifecycle/runtime/visual parity.

## Coordinated review follow-up

The coordinator's frozen full-source snapshot compiled main and unit sources. The native-graphics run passed the XML typography comparisons and all sheet state tests; one loading-state semantics fixture incorrectly searched the merged tree for a deliberately non-actionable child. That test now checks the disabled parent and the non-actionable child in the unmerged tree. Separate actionable row and radio callback coverage was already passing and remains unchanged.

Actual narrow/RTL/fontScale2 OrbotInfo PNGs revealed a 35px XML versus 43px native description line step. This is fixed by expressing the physical line step in font-relative em rather than round-tripping through nonlinear sp conversion. The step uses StaticLayout-style integer font metrics and rounded extra spacing. First-strong content direction and SpinnerItem's resolved relative padding are also restored. A new actual-XML/Compose regression covers line steps, line counts, paragraph direction and overflow at nonlinear fontScale2. There are now sixteen sheet tests.

The visual fixture owner is correcting the dismiss target measurement to inspect expanded touch bounds rather than the smaller icon's visual bounds. No button action or target was changed here. These follow-up changes await the coordinator's rerun; prior compilation and source checks are not a claim that the revised visual/runtime parity has passed.
