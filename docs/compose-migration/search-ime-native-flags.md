# Search EditorInfo hints follow-up

The earlier checkpoint ff0510 explicitly recorded that public KeyboardOptions did not emit native SearchAutoComplete's NO_SUGGESTIONS/AUTO_COMPLETE flags. That known source gap is now handled by a small public Compose input-request interceptor, without constructing a View/editor or opening an input session itself. Both field overloads use it. The underlying request is called exactly once and its exact InputConnection is returned; Compose retains session ownership/cancellation. Only inputType hint bits are modified. Other EditorInfo data, actions, selection, locale hints and input callbacks are untouched. Non-search fields, explicitly enabled autocorrection, numeric and password editor types are left alone.

Source evidence:
- Original AppCompat abc_search_view.xml: text|textAutoComplete|textNoSuggestions, historical blob33d25d9ec40b10fc7a1b6cf5b615252dba1b802c.
- https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/platform/PlatformTextInputModifierNode.kt
- https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/platform/PlatformTextInputMethodRequest.android.kt

The source contract is implemented; runtime parity is not verified. Two plain-Application tests compare a real SearchView's flag mask, request call count, connection identity, preserved metadata and excluded input types. They are authored and UNRUN. No actual keyboard, account, service, user input or traffic was used. The safety manifest remains BLOCKED. The older checkpoint's review hashes are historical and are not repinned to imply it reviewed this bridge.
