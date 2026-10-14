# Source checkpoint: search and original control regressions

This is an uncompiled, partial WIP checkpoint. It includes the source-reviewed Material theme-overlay/TextButton correction, native button tint precedence, custom-background ownership of ripple/stroke, fixed-size icon controls, search plate/card/default keyboard corrections, DNS dialog variants and Stats custom controls. It also preserves the completed second history/RPN presentation group and DNS flag/header/record-mode corrections.

User lint commits 154065c and c87ee9a remain ancestors of the local checkpoint; their locale, resource and build-ignore fixes remain present. The unfinished compound-control renderer and subsequent surrounding-text/card work are deliberately kept in the working tree, outside this checkpoint. No XML layouts were deleted: 186 remain pending final source/reference closure. No toolchain, Android, AAPT, Kotlin, device, Compose or native-reference tests were executed for these changes.

Source guards check callbacks/resource contracts and reject deliberate mutations. They are not compile or visual evidence. Independent real MaterialButton and SearchView reference tests are authored and unrun; the runtime safety manifest remains BLOCKED. Native search NO_SUGGESTIONS/AUTO_COMPLETE EditorInfo flag parity remains an explicit open limitation despite restoring the public Compose search action/autocorrection defaults. Typography, shape, touch hit targets, large-text clipping and RTL must still be measured at runtime.

Remote publication remains subject to the currently pending confirmation for generated source-check reports. No PR, merge or deployment is part of this checkpoint.
