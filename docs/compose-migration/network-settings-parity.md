# Network settings native Compose reconstruction

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.

## Scope and implementation

- `MiscSettingsActivity`, `ProxySettingsActivity`, and `TunnelSettingsActivity` now install native Compose content. They no longer inflate or bind their XML screen content.
- Proxy SOCKS5/HTTP editors, logger/file-trace options, Tasker package input, network-policy choices and the proxy-lockdown review are native Compose as well. `NetworkAlertDialogBuilder` uses an Android Dialog only as the window/lifecycle host. All visible content is native Compose.
- `ui/compose/networksettings` contains observable presentation state and immutable native layout definitions reconstructed from the six pinned XML layouts. There is no runtime XML screen inflation, AndroidView bridge, service, repository, database, or network access in this renderer.
- Settings, state mutation, event logging, account/permission decisions, app routes, backup/export routing, controller lifetime, proxy validation, proxy insertion/removal, tunnel refreshes and lockdown operations remain in the original controllers.
- Orbot, ProxyCountries, BackupRestore and the Rethink warning sheets are owned by the separate network-sheet reconstruction. NetworkReachabilityDialog and the existing Compose Snackbar presenter are separately owned and reused without modifying their boundaries here.

## Contracts explicitly retained

- Source strings, hints, icons, dimensions and Android TextAppearance resources; native typography resolves the current themed Android appearance instead of substituting a generic type scale. The custom switch theme overlay and explicit checkbox tints are retained.
- XML hierarchy order, sections, cards, invisible/gone states, trailing progress overlays, row/child enablement, alpha, and separate row versus checkbox/switch callbacks. Native tags are `network.<original_xml_id>`.
- CompoundButton changed-value/broadcast-suppression and click ordering use the existing Wg presentation state. Activity restoration replays checks in original hierarchy order after `onRestoreInstanceState`. Tunnel slider restoration preserves its original programmatic callbacks; gesture start/change/stop and keyboard/accessibility changes remain distinct.
- Un-ID'd root ScrollViews did not save their scroll offsets. Native scroll offsets deliberately remain transient, as do modal editors, proxy passwords, popup visibility, selection dialogs and refresh animation state.
- SOCKS5 and HTTP retain separate cancel/apply callback bodies, including their original mutation order and validation behavior. Numeric input also uses the platform DigitsKeyListener filter for pasted input. Password masking, monospace input, max-line settings and lockdown touch interception are preserved.
- Tasker keeps selectable explanatory text, multiline editing and horizontal scrolling. Span conversion retains text styles, underline/strike and URL links.
- Both normal/no-dim dialog choices, cancellability and button callbacks remain distinct. Lockdown positive-button enabled state follows its original checkbox; forced-proceed and cancel remain separate actions. Policy rows retain their own 14sp/11sp typography and original adapter-style dismiss-after-selection behavior.

## Evidence and limitations

- `scripts/compose-migration/check-network-settings-controllers.py` passes for 117 baseline braced method bodies after explicit, reviewable presentation-only substitutions. The sole excluded baseline body is `TunnelSettingsActivity`'s removed XML-only policy `ArrayAdapter.getView` renderer. All controller methods and nested original callback bodies remain covered.
- `scripts/compose-migration/check-network-settings-layouts.py` checks the six pinned XML files are unchanged and their original IDs, text/hint/icon/dimension references and TextAppearance styles appear in the native definitions. This is static coverage, not a screenshot comparison.
- Authored 25 isolated tests: 17 in `NetworkSettingsStateTest` and 8 in `NetworkSettingsComposeTest`. These use a plain Application, native presentation state, themed test contexts and local synthetic callback recorders. They never instantiate a production Activity, production Application, backend, permission prompt, probe, VPN or service.
- The coordinator's first full-source Kotlin check found only 21 resource-owner errors in this group. Those references were corrected to AppCompat/Material resource owners; recompilation and test execution remain pending at the time of this report. No Gradle commands or production application launches were performed by this worker.
- No claim of complete behavioral/visual acceptance is made from source equality alone. Baseline-versus-native screenshots across themes, RTL, large text, modal sizing, TalkBack traversal, slider pointer cancellation, and Android recreation still require coordinated validation. The native layout renderer and framework-style text decorations are new presentation code and must pass that comparison before acceptance.
