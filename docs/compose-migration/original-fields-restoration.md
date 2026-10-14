# Original text-field presentation restoration

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
Native behavior checkpoint: `e2b97de59cc854de74010c096ceffbaed6cb4a55`.

## Implementation

`ui/compose/fields/OriginalTextFields.kt` renders native Compose inputs. It reads Android style/color/drawable resources, without creating Views or inflating layouts. There is no universal outlined-field replacement:

- WG interface/peer and custom rules retain their explicit 8dp outlined family. WG SSID dialog inherits its explicit outlined widget's shape. WG inline SSID uses the original 4dp rectangle with its 8dp padding.
- DNS resolver forms and country SSID inherit `textInputStyle`, including box mode, shape, fill, stroke selectors and focused widths. DNS proxy fields retain ordinary edit-text decoration and BodyLarge typography, with the original 30:70 side-label rows.
- LAN fields retain `edittext_default`, 10dp drawable padding, 2:1 address/prefix weights and 4dp separation. Reachability retains its existing original-drawable renderer.
- Ping and support retain 16dp/15sp and 12dp/14sp overrides respectively. Error cursors and accessibility messages use the existing error state.
- Notes keep the `TextFieldValue` path and themed underline; Tasker retains the themed editor background, horizontal scrolling and original pixel padding. The pre-existing network editor's resource/state drawable renderer is retained.
- SearchView families use the original query-background drawable rather than an outlined field. App logs and server selection retain 14sp overrides; server selection retains its no-font-padding override. Auto-exclude search remains transparent inside its 12dp search card.
- DNS/network app selectors resolve the original spinner background and the `spinnerDropDownItemStyle` used by their baseline `simple_spinner_dropdown_item` adapters. Selection/menu ownership stays with the existing caller.

Focused, unfocused and disabled colors resolve through resource selectors. Theme highlight/selection colors are retained. Material input decoration uses native Compose `DecorationBox` with explicitly resolved colors, shape, padding and stroke widths; generic Compose field defaults are not used as the screen's style specification. Minimum targets are 48dp and field heights may expand for large text/multiline content.

## Evidence and limits

- `original-field-style-map.json`: 96 baseline XML field/search instances, two programmatic editors, two auxiliary spinner selectors, and their rendering functions.
- `check-original-fields.py`: source-only guard against stock field call sites or View inflation, complete baseline mapping, and identical value/change/keyboard/editor arguments at 21 editor sites in 18 native files.
- `original-fields-source-report.json`: latest source-check results.
- `OriginalTextFieldsTest.kt`: eight authored isolated Robolectric tests for six themes, distinct families/corners/padding, state colors, disabled/edit dispatch, password/IME, selection, large RTL targets, errors and selectors. Uses `android.app.Application`, synthetic state and no production controller/service.

The source guard and whitespace check pass. Kotlin compilation, resource linking, Robolectric/Compose tests, device execution and screenshot/pixel comparisons have not run because the SDK/Java 17/dependency environment is unavailable. These changes are not runtime or visual-parity proof.
