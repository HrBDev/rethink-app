#!/usr/bin/env python3
"""Source-only DNS screenshot-restoration guard. No toolchain, app, device or traffic."""
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BASE = "23cb6dd98805445174bd83b7237ad5f5d2c24ac9"
P = "app/src/main/java/com/celzero/bravedns/ui/compose/blocklists/"
FILES = {
    "screen": P + "BlocklistConfigurationScreen.kt",
    "appearance": P + "BlocklistCardAppearance.kt",
    "list": P + "RethinkListScreen.kt",
    "tests": "app/src/test/java/com/celzero/bravedns/ui/compose/blocklists/BlocklistsComposeTest.kt",
}
A = "{http://schemas.android.com/apk/res/android}"
APP = "{http://schemas.android.com/apk/res-auto}"


def baseline(path):
    return subprocess.check_output(["git", "show", f"{BASE}:{path}"], cwd=ROOT, text=True)


def nodes(path):
    return {n.get(A + "id", "").removeprefix("@+id/"): n
            for n in ET.fromstring(baseline(path)).iter() if A + "id" in n.attrib}


def baseline_evidence():
    layouts = ["app/src/full/res/layout/list_item_rethink_blocklist_simple.xml",
               "app/src/main/res/layout/list_item_rethink_blocklist_adv.xml"]
    for path in layouts:
        n = nodes(path)
        assert n["crp_title_ll"].get(A + "paddingHorizontal") == "16dp"
        assert n["crp_title_ll"].get(A + "paddingVertical") == "12dp"
        assert n["crp_blocktype_heading_tv"].get(A + "textStyle") == "bold"
        assert n["crp_card"].get("style") == "?attr/materialCardViewElevatedStyle"
        assert n["crp_card"].get(A + "layout_marginHorizontal") == "12dp"
        assert n["crp_card"].get(A + "layout_marginVertical") == "6dp"
        assert n["crp_card"].get(APP + "cardCornerRadius") == "16dp"
        assert n["crp_level_indicator"].get(A + "layout_width") == "6dp"
        assert n["crp_label_tv"].get(A + "layout_marginStart") == "16dp"
        assert n["crp_label_tv"].get(A + "layout_marginTop") == "16dp"
        assert n["crp_check_box"].get(A + "minWidth") == "48dp"
        assert n["crp_check_box"].get(A + "minHeight") == "48dp"
    n = nodes("app/src/main/res/layout/fragment_rethink_blocklist.xml")
    for key in ("lb_simple_toggle_btn", "lb_adv_toggle_btn"):
        assert n[key].get("style") == "@style/toggleButtonStyle"
        assert n[key].get(A + "minWidth") == "125dp"
        assert n[key].get(APP + "cornerRadius") == "16dp"
    for key, weight in (("lb_blocklist_cancel_btn", "0.4"), ("lb_blocklist_apply_btn", "0.6")):
        assert n[key].get(A + "layout_weight") == weight
        assert n[key].get("style") == "@style/Widget.Material3.Button.TextButton.Dialog"
    assert n["lb_blocklist_cancel_btn"].get(A + "layout_marginEnd") == "1dp"
    for adapter in ("LocalSimpleViewAdapter", "RemoteSimpleViewAdapter", "LocalAdvancedViewAdapter", "RemoteAdvancedViewAdapter"):
        code = baseline(f"app/src/main/java/com/celzero/bravedns/adapter/{adapter}.kt")
        assert "setCardBackgroundColor(fetchColor(context, R.attr.selectedCardBg))" in code
        assert "setCardBackgroundColor(fetchColor(context, R.attr.background))" in code


def checks(s):
    result = {}

    def has(key, label, *tokens):
        result[label] = all(token in s[key] for token in tokens)

    has("screen", "screen owns system bar safe area",
        'Box(Modifier.fillMaxSize().safeDrawingPadding().testTag("blocklist-content"))')
    has("list", "standalone list owns insets while bottom sheet keeps existing inset owner",
        "if (state != null) Modifier.safeDrawingPadding() else Modifier")
    has("screen", "centered joined source selector",
        '.selectableGroup().testTag("blocklist-tabs")',
        "Arrangement.spacedBy(-dimensionResource(R.dimen.default_stroke_width), Alignment.CenterHorizontally)",
        "Modifier.widthIn(min = 125.dp)", "segmentCount = 2, grouped = true",
        '"lb_simple_toggle_btn" else "lb_adv_toggle_btn"',
        "R.attr.accentGood else R.attr.defaultToggleBtnBg",
        "R.attr.homeScreenHeaderTextColor else R.attr.primaryTextColor")
    has("screen", "original tab callbacks and separate scroll states",
        '"simple") { onTab(false) }', '"advanced") { onTab(true) }',
        "val packScroll = rememberLazyListState()", "val tagScroll = rememberLazyListState()",
        "val scroll = if (b.showAdvanced) tagScroll else packScroll",
        "tagScroll.scrollToItem(0)")
    has("screen", "only list is weighted and scrollable above fixed footer",
        "Box(Modifier.weight(1f))", '.testTag("blocklist-list"), state = scroll)',
        '.heightIn(min = 48.dp).testTag("blocklist-actions")',
        'horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically',
        'onClick = b.lbBlocklistCancelBtn.onClick, Modifier.weight(.4f)',
        'onClick = b.lbBlocklistApplyBtn.onClick, Modifier.weight(.6f)')
    has("screen", "source section grouping and native bold family preserved",
        "val previousGroup = if (index > 0) rows.peek(index - 1)?.group else null",
        "index == 0 || previousGroup != row.group",
        "padding(horizontal = 16.dp, vertical = 12.dp)",
        "textAppearanceTitleLarge), weight = android.graphics.Typeface.BOLD)",
        "padding(top = 4.dp).alpha(.75f)")
    has("screen", "source card spacing radius label and description preserved",
        "padding(horizontal = 12.dp, vertical = 6.dp), cornerRadius = 16.dp",
        "styleAttribute = com.google.android.material.R.attr.materialCardViewElevatedStyle",
        "containerColor = blocklistCardColor(checked)",
        "Modifier.width(6.dp).fillMaxHeight()",
        "padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp)",
        "style = MaterialTheme.typography.titleMedium",
        "padding(top = if (row.entries == null) 4.dp else 8.dp)",
        "Modifier.weight(1f).alpha(.7f)")
    has("screen", "selection state direct checkbox and row callbacks preserved",
        "remember(row.key, persisted, selectedTags)",
        "checked = it; row.onSelected(it)",
        "clickable(role = Role.Checkbox) { select(!checked) }",
        '"list_item_rethink_blocklist_adv:crp_check_box", checked, select',
        "row.onEntries?.invoke()")
    has("screen", "query submit and apply/discard remain distinct",
        "if (b.query.isNotBlank() && !onQuerySubmit(b.query))",
        "b.prompt = null; prompt.cancel()", "b.prompt = null; prompt.confirm()")
    has("appearance", "baseline fill attributes retained and native overlay restored",
        "themeColor(if (checked) R.attr.selectedCardBg else R.attr.background)",
        "MaterialThemeOverlay.wrap(context, null,",
        "MaterialR.attr.materialCardViewElevatedStyle, MaterialR.style.Widget_MaterialComponents_CardView",
        "intArrayOf(androidx.cardview.R.attr.cardElevation)",
        "attributes.getDimension(0, 0f)",
        "ElevationOverlayProvider(cardContext).compositeOverlayIfNeeded(fill.toArgb(), elevationPx)")
    result["no screenshot RGB or unscaled pixel geometry"] = not re.search(r"0x[fF]{2}[0-9a-fA-F]{6}|#[0-9a-fA-F]{6}", s["appearance"])
    for key in ("screen", "appearance", "list"):
        code = re.sub(r"/\*.*?\*/|//[^\n]*", "", s[key], flags=re.S)
        result[key + " remains native Compose presentation"] = not any(x in code for x in
            ("AndroidView(", "LayoutInflater", "R.layout.", "Binding.inflate", "MaterialCardView("))
    has("tests", "synthetic dark-card native oracle and selector/footer regressions authored",
        "application = Application::class", "darkUncheckedCardRetainsNativeElevatedTealSurface",
        "MaterialCardView(context, null, MaterialR.attr.materialCardViewElevatedStyle)",
        "native.draw(Canvas(bitmap))", "assertNotEquals",
        "selectorKeepsCenteredFullHeightJoinedSegments", "assertHeightIsAtLeast(48.dp)",
        "fullHeightFooterStaysBelowScrollViewportAndDispatchesDistinctActions",
        "assertEquals(listOf(\"discard\", \"apply\"), calls)",
        "rowAndCheckboxDispatchOnceEachAndSelectedFillMatchesTheme",
        "persistedTagEmissionRefreshesOptimisticPackCheck")
    return result


baseline_evidence()
source = {key: (ROOT / path).read_text() for key, path in FILES.items()}
results = checks(source)
assert all(results.values()), {key: value for key, value in results.items() if not value}
mutations = [
    ("screen", ".safeDrawingPadding()", ""),
    ("list", "if (state != null) Modifier.safeDrawingPadding() else Modifier", "Modifier.safeDrawingPadding()"),
    ("screen", "Alignment.CenterHorizontally", "Alignment.Start"),
    ("screen", "min = 125.dp", "min = 32.dp"),
    ("screen", "segmentCount = 2, grouped = true", "segmentCount = 2, grouped = false"),
    ("screen", '"simple") { onTab(false) }', '"simple") { onTab(true) }'),
    ("screen", "heightIn(min = 48.dp)", "heightIn(min = 16.dp)"),
    ("screen", "Arrangement.spacedBy(1.dp), verticalAlignment", "Arrangement.spacedBy(0.dp), verticalAlignment"),
    ("screen", "lbBlocklistApplyBtn.onClick", "lbBlocklistCancelBtn.onClick"),
    ("screen", "weight = android.graphics.Typeface.BOLD", "weight = android.graphics.Typeface.NORMAL"),
    ("screen", "containerColor = blocklistCardColor(checked)", "containerColor = themeColor(R.attr.background)"),
    ("screen", "horizontal = 12.dp, vertical = 6.dp", "horizontal = 16.dp, vertical = 8.dp"),
    ("screen", "row.onSelected(it)", "row.onSelected(!it)"),
    ("appearance", "compositeOverlayIfNeeded(fill.toArgb(), elevationPx)", "compositeOverlayIfNeeded(fill.toArgb(), 0f)"),
    ("appearance", "R.attr.selectedCardBg else R.attr.background", "R.attr.background else R.attr.background"),
    ("appearance", "androidx.cardview.R.attr.cardElevation", "androidx.cardview.R.attr.cardMaxElevation"),
    ("tests", "native.draw(Canvas(bitmap))", "// native oracle removed"),
]
for key, old, new in mutations:
    altered = dict(source)
    assert old in altered[key], (key, old)
    altered[key] = altered[key].replace(old, new, 1)
    assert not all(checks(altered).values()), ("negative control escaped", key, old)

report = {
    "result": "PASS_SOURCE_ONLY", "baseline": BASE,
    "checks": results, "negative_controls_rejected": len(mutations),
    "source_sha256": {FILES[key]: hashlib.sha256(value.encode()).hexdigest() for key, value in source.items()},
    "reference_images": ["current image3 (345x747)", "original image4 (756x1688)"],
    "reference_findings": [
        "Selector collapsed into status bar in image3; image4 has a centered full capsule.",
        "Unchecked cards in image3 lose the dark teal elevation overlay visible in image4.",
        "Baseline dp/sp sizes retained; differently sized screenshot pixels are not used as dp."
    ],
    "unrun": ["Android compilation", "Robolectric synthetic tests", "device rendering", "pixel parity", "TalkBack"],
    "limitation": "Source/resource and mutation checks only. Runtime/rendering parity is not established. No toolchain downloads or production execution."
}
output = ROOT / "docs/compose-migration/blocklist-reference-restoration-results.json"
output.write_text(json.dumps(report, indent=2) + "\n")
print(json.dumps({"result": report["result"], "checks": len(results),
                  "negative_controls_rejected": len(mutations), "runtime": "UNRUN"}, indent=2))
