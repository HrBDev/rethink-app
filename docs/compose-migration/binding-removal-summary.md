# Binding-removal checkpoint

**Source implementation and static review are complete for this checkpoint. Compilation, isolated runtime verification and release acceptance remain blocked.**

## Changes from `87b3b9975afdb59492bc92cf97b9560cdd725330`

- FirewallActivity and DnsDetailActivity no longer use generated binding, hidden TabLayout or ViewPager2. Their native Compose roots retain the existing Compose settings fragments. A narrowly matched legacy `f0` adoption path preserves the prior pager fragment's identity and saved state.
- HomeScreenActivity, FragmentHostActivity and ConfigureRethinkBasicActivity no longer inflate their XML shells. Stable resource IDs and Activity-owned FragmentManager/NavHost state remain. The three old shell XML files are removed.
- Home's native bottom-bar bounds now drive snackbar anchoring and RPN content/FAB clearance. Geometry subscriptions are disposed with their owning view/window lifecycle. Theme, insets, argument routing, delayed navigation, back-stack and highlight rules are preserved at source level.
- View Binding generation, phone/TV `vbpd` dependencies and the dedicated catalog alias/version are removed. Every other original dependency pin/alias/plugin and all 522 protected backend/manifest/TV files remain unchanged.
- Unused FirewallStatusSpinnerAdapter and SponsorBadgeView, the fully commented AlertAdapter/old Alerts implementation, three unattached ArrayAdapter allocations and unused legacy builder imports are removed.

## Explicit interoperability

Visible app content is Compose; this does not eliminate Android's framework window and Fragment containers. Exactly two application `AndroidView` sites remain, each restricted to a programmatic FragmentContainerView with no XML inflation or native app control: the fixed-ID navigation bridge and the settings legacy-state bridge. NetworkLogs retains AndroidX AndroidFragment; protocol DNS hosts retain their programmatic header/Fragment container shell. Dialog/BottomSheetDialog windows, the invisible notification trampoline, external QR capture and system permission/document/billing surfaces remain framework boundaries. Older XML visual references and dormant custom View definitions are not runtime screen renderers.

Plain observable state objects retain some old widget-shaped property/listener names to preserve controller logic. The audit traces their actual types and Compose consumers; they are not generated binding objects or hidden inflated Views.

## Verification

- Whole-source census: 779 production Kotlin/Java files across five source sets; zero binding APIs/dependencies, app layout references or app inflation paths; two constrained fragment-container exceptions.
- 24 current static guards pass on an unchanged source envelope, including the independent renderer census and original-test obligation guard. All original 99 test sources remain accounted for, with 1,475 original declarations retained and seven explicitly mapped native replacements.
- New settings, legacy-pager Bundle, fixed-ID restoration, navigation-bound and snackbar regression cases are authored and independently source/safety-reviewed. The cumulative isolated scope is recorded in the exact current safety allowlist; it is not an execution result.
- The replacement test launcher is fail-closed and source-reviewed. Its runtime clearance remains BLOCKED. No new Gradle/test execution or APK was produced after the reset.

See [validation-status.md](validation-status.md) for historical results and lost evidence, [binding-removal-review.md](binding-removal-review.md) for exact source review and safety conditions, and [verification-commands.md](verification-commands.md) for reproducible static/build/test prerequisites and commands. The final pre-reset 399-test terminal result is unknown; the latest changes must be compiled and retested before completion can be claimed.
