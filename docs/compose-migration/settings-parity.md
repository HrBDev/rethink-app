# Settings/menu/mode-sheet reconstruction

Partial native UI migration of AdvancedSettingActivity, AntiCensorshipActivity, UniversalFirewallSettingsActivity, ConfigureFragment, FirewallSettingsFragment, HomeScreenSettingBottomSheet and BlockFreeDnsModeBottomSheet. Native content lives in `ui/compose/settings`. All navigation, persistence, event logging, accessibility permission decisions, mode mutations and refresh operations remain in original controllers.

The source gate `scripts/compose-migration/check-settings-controllers.py` protects 79 baseline braced bodies with explicit UI-type substitutions. It excludes only Activity content installation and the two sheet inflation methods. Controller source equality does not prove behavior or rendering parity.

- AppCompat checked controls are observable state, with changed-value and broadcasting semantics from the shared presentation adapter.
- Activity check restoration happens after `onRestoreInstanceState`; sheet restoration happens after `onViewStateRestored`. Home mode radios replay independently in DNS/firewall/both order, including callback-driven nested unchecks.
- Advanced mode selection dialogs and tour-result labels are transient. Slider programmatic restoration never writes preferences.
- Mode-sheet themed contexts come from `inflater.context`. Non-dismissible accessibility confirmation, refresh lifetime/750ms rotation/minimum1500ms action timing, no-dim dialogs and hidden baseline rows remain distinct contracts.
- Native rendering preserves resource strings/icons, original card-style colors, row ordering, debug-only visibility, mode restrictions and theme selection. Large text/RTL/theme screenshot comparison remains pending.
- Eight new plain-Application synthetic presentation tests are authored in SettingsStateParityTest. They do not instantiate production controllers, app, services, storage, permissions or VPN. They are not yet executed.

This group is not accepted as fully verified until coordinated compilation, isolated test execution and baseline visual/accessibility comparison pass.
