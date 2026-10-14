# Utility screens: reconstructed native Compose presentation

Baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`.
This evidence describes the current reconstructed source, not the lost checkpoint.

## Scope and boundaries

- Native content: Alerts, AppLock, Bubble, CustomerSupport, Pause, PingTest,
  TcpProxyMain, Welcome, NetworkReachabilityDialog, Sponsor and SponsorSuccess.
- Bubble allowed/blocked paging and Ping history render through native lists.
- Alerts' original activity business code and AlertAdapter were entirely disabled;
  the empty visible surface remains inactive rather than introducing behavior.
- Framework Activity, Fragment, platform bubble, biometric and Dialog hosts remain.
  The new renderers do not wrap legacy screens, ViewBindings or RecyclerViews.
- No protected service, repository, ViewModel, BubbleHelper, worker, network probe,
  VPN/security setting, account, permission, payment or external action was run.

## Preserved controller contracts

`check-utility-controllers.py` uses the fixed original SHA, not moving HEAD.
The fresh source guard checks 116 exact/narrowly adapted method bodies and seven
byte-identical protected utility dependencies. It also protects inactive AlertAdapter,
rejects legacy renderer boundaries and requires plain-Application/NATIVE-graphics
content test isolation. Negative controls reject changes to literals, arguments,
command order and duplicate callbacks.

The approved substitutions are rendering or input-presentation operations only:
Compose content installation, paging presenter attachment, quick-fix item construction,
keyboard hiding/focus clearing, native dolphin animation-property names, and replacing
old scroll/list wiring. The full original diagnostic generation, redaction, intent
construction, lifecycle collectors, allow/remove refresh ordering, countdown commands,
probe invocation/results, validation and persistence bodies remain guarded.

TcpProxyMain's always-true `isActive` behavior is intentionally preserved.
The observable control adapter changes values only when different, suppresses
synchronous reentrant checked callbacks, and still permits a later asynchronous
reverse callback. No business correction is hidden in the migration.

## Presentation parity decisions

- Welcome advances the requested page synchronously before the pager animation,
  matching rapid Next/Finish behavior. It retains four pages, swipe navigation,
  Skip visibility, indicator state, saved page and original finish navigation.
- Support retains the 180dp-to-56dp collapsing gradient header with nested-scroll,
  direct drag and settling, original primary/theme colors, 16dp horizontal card
  margins and padding, selected-category behavior, description validation, three
  attachment switches, loading state and fixed bottom email action.
- Support and SponsorSuccess resolve native TextAppearance/default TextView values,
  including font family, text style, numeric weight, size and em letter spacing.
  SponsorSuccess body keeps 6dp extra line spacing using native font-spacing metrics,
  with font padding included once. Orbot also reuses this typography helper.
- Bubble retains paging invalidations, all twelve quick fixes, load/error/empty states,
  the two original callbacks and 15-minute expiry/countdown arithmetic. Row actions
  move to their own line for large fonts; labels and selected states are accessible.
- Ping retains history order and result metadata, native hero/status artwork and
  original ObjectAnimator timing through observable transform properties. Signature
  artwork appears at the history end; small-height/large-font layouts scroll the hero.
- Pause keeps the original false-returning long-click release click, UP/CANCEL ordering,
  keyboard repeat handling and cancellation on focus loss/disposal.
- Reachability retains all eight fields, automatic/manual visibility and enablement,
  protocol/status icons, progress/error states, reset/test/save callbacks and original
  default/error drawable borders and padding. Native fields use the EditText default
  appearance; dialog fields/mode are explicitly saved and restored.
- Sponsor retains single-selection chips that can deselect, source slider range and
  label, loading/Play-unavailable states, Stripe action, and success continuation.
- Native form controls use stable keys and explicit field/check/slider restoration;
  layouts use logical start/end padding, scalable text, scrollable content and semantic
  actions/headings/status. No claim of complete screenshot parity is made below.

## Fresh validation and pending proof

Executed without starting the application:

```
python3 scripts/compose-migration/check-utility-controllers.py --self-test \
  --report docs/compose-migration/utility-controller-source-report.json
```

Result: PASS, 116 method bodies, seven protected dependencies, callback negative
controls and native-renderer/test-isolation boundaries.

The coordinator owns serial compilation/test execution. These test sources are ready,
but execution is not claimed here until coordinator results are available:

- UtilityContentTest: 14 strict fake callback/content cases, including rapid Next,
  Bubble identity/cardinality, TCP reentrancy, Support collapse/loading/RTL, Ping,
  Sponsor, Reachability enablement/editing and Pause release ordering.
- UtilityTypographyTest: five offscreen real-XML reference comparisons for Sponsor
  title/body, Support labels and Reachability EditText, with native graphics.
- UtilityStateTest: five changed-only/reentrant, slider, restore and expiry cases.

The content/typography fixtures use Robolectric SDK 35, plain android.app.Application
and NATIVE graphics. They do not instantiate production hosts or start application
services. Separate onboarding integration and visual-reference fixtures are owned by
other workers and must be read with their own execution evidence.

Remaining gates: coordinator compilation and isolated execution of the current bytes;
reference measurements/screenshot comparison across themes, widths, RTL and large
fonts; remaining visual findings must be corrected rather than treated as passed.

## First executed reconstruction findings and corrections (2026-10-14)

The coordinator's frozen `full-source-content-visual-tests.log` passed main and unit
compilation and executed the original 24 utility tests: 22 passed, two failed.
The failures were a duplicated decorative Pause image tag and a TextView/native font
weight mismatch. Both have source corrections pending re-execution.

Actual Support and SponsorSuccess PNG pairs were inspected. Native bold font resolution
now preserves the family-derived weight (sans-serif-medium bold is 800), with robust
fallbacks for zero weights returned by legacy test graphics. Raw SP sizes avoid nonlinear
pixel/SP round-trip drift. Line spacing uses integer native ascent/descent plus the XML
extra spacing in EM units; pinned Compose bytecode demonstrated that SP line height uses
a proportional font-size ratio at nonlinear scales, causing the observed 39px/55px drift.
Content-first text direction is explicit for mixed-direction paragraphs.

Support's 3dp description/privacy spacing, 1000-character counter/LengthFilter and
sentence capitalization were restored. The input limit preserves an unchanged suffix
and never splits surrogate pairs. Sponsor Continue uses the original primary text color
and 16dp/0.75dp border. Reference fixtures are being corrected independently to install
AppCompat/Material inflation and attach offscreen toolbar widgets before their next run.

The updated utility suite has 27 cases (14 content, six typography, seven state).
The extra tests cover native nonlinear font-size/rendered line-step parity, invalid
native-weight fallbacks and length filtering. No assertion tolerance was broadened.
These corrections are not declared runtime or screenshot passes until rerun evidence.
