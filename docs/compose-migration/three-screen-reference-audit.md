# Three-screen reference audit

Reviewed 2026-10-14. All six supplied screenshots were opened and visually inspected before findings. Pairing: **About 1 → 6; Stats 2 → 5; DNS/blocklists 3 → 4**. This is an evidence and source review, not a verified corrected rendering.

Original source baseline: `23cb6dd98805445174bd83b7237ad5f5d2c24ac9`. Pre-restoration Compose: `cbe1a9f`. Production code was not edited by this review. No browser, network, download, Gradle, Android, emulator, or device action was performed.

## Scale and state controls

- Current image dimensions: About 325×724; Stats 344×707; DNS 345×747. Targets are each 756×1688. Measurements below normalize current geometry to width 756 when helpful; they are approximate, not Android dp. Density, font scale, and crop are unknown.
- **Stats screenshots show different modes:** current 2 is LIST, target 5 is INSIGHTS. Stacked list summary versus three-column insight summary is not by itself a regression. Preserve persisted mode and restore each mode’s original presentation.
- Current page background #1a1b1e versus target #1a1b1f is a negligible encoding-level difference. About/DNS glyph and checkbox sizes broadly agree after normalization; a blanket font/icon scale-up is unsupported.
- Current DNS selector visibly overlaps status icons. Target absolute top offsets must not be copied mechanically; restore the real host inset and component spacing once.

## Evidence and restoration contracts

| ID | Area | Current → target evidence | Contract |
|---|---|---|---|
| SH-01 | Shared: Segmented-control height and outline | Stats period control: 13px high (28.6 at common width), x14..258 / 344; DNS selector: 15px (32.9), x72..272 / 345. → Stats: 74px high, x104..651 / 756; DNS: 75px, x149..606 / 756. Both have rounded exterior ends and visible outlines/dividers. | Restore source Material3 outlined-button minimum visual height, padding, stroke, and joined end caps; do not globally scale every control. |
| SH-02 | Shared: System-bar overlap and top spacing | DNS top selector occupies y4..19 while status icons occupy the same band. Stats screenshot has no visible status bar; About does. → Targets have a blank upper band before content; no visible status glyph overlap. | Respect real host insets once. Do not hard-code the target absolute Y or add duplicate per-screen status padding. |
| AB-01 | About: Card outer and inner spacing | Card x7..310, y75..290: normalized x16.3..721.1; first text x15 gives ~18.6px inset from card. → Card x38..718, y255..881; first text x86 gives ~48px inset. | Restore source 16dp card margins and original themed card clearance; body inner 6dp container + 20dp text padding. |
| AB-02 | About: Paragraph line spacing and vertical rhythm | Line step approximately 13px (30.2 normalized); three paragraphs packed into card y86..240. → Line step approximately 39px; distinct paragraph gaps and larger top/bottom inset. | Restore original Subhead body styling with 5dp extra line spacing, first-text 10dp top margin/padding, paragraph 20dp bottom margins, and usage 10dp bottom margin. |
| AB-03 | About: Sponsor outline, margins, and icon grouping | Sponsor nearly flush with card: x8..309,y250..289, roughly 4px corner radius. → Sponsor inset x67..688,y762..852, rounded ~32px corners; heart left and centered label. | Use original 16dp-radius/0.75dp-stroke drawable contract, 16dp side/bottom margins, 10dp top margin, and original compound-icon/text padding. |
| AB-04 | About: Contact action grouping | Telegram and Bug report each occupy a full-width row, vertically stacked with captions. → Equal-width adjacent columns x38..369 and x387..719; each caption immediately below its own action. | Restore 0.5/0.5 horizontal columns with 20dp outer and 5dp inner margins. Keep crash progress and handlers in their original column. |
| AB-05 | About: App section alignment and visible extent | Section left x43 (~100 normalized), row text x46 (~107), icons around x12 (~28). Links appears in current viewport. → Section x120, row text x125, icons around x47. App update is visible; Links is below the viewport. | Restore source row/section padding and larger preceding content flow. Do not force app-update visibility or remove Links. |
| ST-01 | Stats: Brand typography | Lowercase rethink with default headline styling. → Spaced uppercase RETHINK, muted, matching About branding. | Restore original HomeScreen heading style, smallcaps family, all-caps behavior, 0.25em spacing, resource size, and alpha; preserve alpha-build text behavior. |
| ST-02 | Stats: Period selector horizontal alignment | Three-segment group begins x14/344 (4.1%); width about244/344 (70.9%). → Group x104/756 (13.8%); width547/756 (72.4%), centered. | Center the source-sized 100dp-minimum segments rather than stretching them or left-aligning their scroll container. |
| ST-03 | Stats: Mode and filter glyphs | Top-right shows blank selected/unselected rectangles and a blank dark circle. → List, chart, and filter glyphs are readable, with outlines. | Restore existing ic_view_list, ic_view_chart, and ic_filter resources and source tints inside original 32dp controls; preserve selected mode. |
| ST-04 | Stats: Traffic summary grouping and surface | Screenshot 2 selects LIST: stacked label/value rows and horizontal usage bar. Pre-restoration Compose INSIGHTS source used independent FlowRow tiles. → Screenshot 5 selects INSIGHTS: one rounded card x42..714,y368..495, three equal columns, bold value above muted caption, two cyan vertical dividers; fill approximately #25262a. | Do not force persisted mode or replace valid LIST presentation. Restore the shared INSIGHTS card, 18sp bold values/11sp captions, and original card fill/compat clearance when INSIGHTS is selected. |
| DN-01 | Dns: Unselected card fill | Unchecked simple rows use near-page #1a1b1e fill, so their bodies almost disappear. → Unchecked simple cards have a clear dark-teal fill, dominant approximate #182629. | Preserve original background/selectedCardBg choice and apply the native MaterialCardView elevation overlay; retain selection behavior. |
| DN-02 | Dns: Row geometry, stripe, and checkbox | First card roughly x13..330,y97..155: normalized width694.6,height127.1. Text begins x31, ~39.4 normalized inset; next-row gap about14px (~30.7). → First card x22..734,y374..510: width712,height136. Text begins x63, inset41; next-row gap23. | Keep source 12dp/6dp card margins, 6dp edge stripe, 16dp text margins, 16dp corners and 48dp checkbox target. Review font padding/line height rather than enlarging all text/icons. |
| DN-03 | Dns: Section grouping and lower visible content | Security title y35 (~76.7 normalized), first card y97 (~212.6); Privacy follows five rows; Extremeprivacy is visible before footer. → Security title around y236, first card y374; Privacy begins after five rows; Apple is last mostly visible row. | Restore selector height/inset and source group padding first. Keep group order, blocklist counts, and offscreen rows; visible row count is not data loss evidence. |

All geometric bounds are approximate visual edges. Detailed classifications, source references, resolution metadata, and limitations are in the adjacent JSON.

## Source-grounded causes

- About substituted a generic card/row and short uniform description padding for the original nested card spacing, 5dp extra line spacing, drawable outline, and horizontal support columns. Original `rectangle_border_background.xml` uses 16dp corners and a 0.75dp stroke.
- Stats substituted default headline text and a left-aligned `LazyRow`; its pre-restoration INSIGHTS branch used wrapping tiles rather than the original shared bordered KPI card. Both mode branches remain behaviorally valid and must survive.
- DNS paints the raw background without the native elevation overlay. Source adapters already set background/selectedCardBg, so retain that choice and restore the overlay rather than changing selection colors. The original row, stripe, text, and checkbox dimensions already explain much of the target geometry.
- Both flattened selectors and missing Stats toolbar glyphs pass through `OriginalControls`. Their source styles are Material3 outlined buttons with explicit stroke/shape/icon contracts; resolve original minimum-size and padding behavior rather than adding screen-specific cosmetic overrides.

## Do not “fix” runtime or flavor differences

- Stats current screenshot is LIST and target is INSIGHTS; mode is persisted runtime state, not a requested logic change.
- About usage days and sponsor cost differ (25/$0.67 versus 121/$3.23); retain live calculation.
- About update-row absence is consistent with existing F-Droid visibility condition; screenshot alone does not establish build flavor.
- Different status/nav bar crops, screenshot aspect ratios, density, and font scale prevent absolute full-frame pixel equivalence claims.
- Both Stats screenshots show zero traffic and otherwise empty content; no missing-data inference is justified.
- Extra About Links or DNS Extremeprivacy content in the current viewport follows geometry/scroll capacity; do not remove content.
- No touch interaction, accessibility tree, semantics, screen-reader behavior, dynamic font size, build result, or corrected runtime rendering was tested.

## Required later verification

1. Render matching build flavor, theme, locale, font scale, window size, and runtime mode, or document unavoidable differences.
2. Compare About 1→6, Stats 2→5, DNS 3→4; for Stats explicitly select INSIGHTS for target comparison and separately retain LIST behavior.
3. Check shared control outlined silhouettes, proper glyphs, text baseline, and safe insets without duplicate padding.
4. Check About card body leading/insets, sponsor frame, two-column support grouping, and correct conditional rows.
5. Check Stats centered period selector and single three-column INSIGHTS card while preserving filter/time/mode handlers and usage calculations.
6. Check DNS unchecked/checked surfaces, stripes, section rhythm, row/checkbox geometry, scrolling and fixed footer; preserve paging/selection/apply/discard behavior.
7. Do not call source checks or a compile success visual verification; a fresh corrected screenshot set remains required.

Status: screenshot evidence complete; source restoration review is recorded in `three-screen-source-review.md`; build and corrected visual verification remain unavailable in this pass. No accessibility-compliance claim can be made from these screenshots.
