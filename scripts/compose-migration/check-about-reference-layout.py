#!/usr/bin/env python3
"""About screenshot-restoration contracts. Source-only; never builds or launches Android."""
import argparse
from functools import lru_cache
import hashlib
import json
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
BASE = "23cb6dd98805445174bd83b7237ad5f5d2c24ac9"
BEFORE = "cbe1a9f3cb0ef6cdac5d0ced561f67a79c68d001"
P = "app/src/main/java/com/celzero/bravedns/"
CONTENT = P + "ui/compose/about/AboutContent.kt"
STATE = P + "ui/compose/about/AboutState.kt"
HOST = P + "ui/fragment/AboutFragment.kt"
TEST = "app/src/test/java/com/celzero/bravedns/ui/compose/about/AboutReferenceLayoutTest.kt"
A = "{http://schemas.android.com/apk/res/android}"

@lru_cache(None)
def baseline(revision, path):
    return subprocess.check_output(["git", "show", f"{revision}:{path}"], cwd=ROOT, text=True)


def check(overrides=None):
    overrides = overrides or {}
    def read(path):
        return overrides[path] if path in overrides else (ROOT / path).read_text()
    old = ET.fromstring(baseline(BASE, "app/src/main/res/layout/fragment_about.xml"))
    nodes = {n.get(A + "id", "").split("/")[-1]: n for n in old.iter()}
    # Independent pinned XML evidence, not screenshot-coordinate constants in production.
    for name in ("about_telegram", "about_crash_log"):
        node = nodes[name]
        for attr, value in {"drawablePadding": "-15dp", "paddingStart": "16dp", "paddingTop": "12dp", "paddingBottom": "12dp"}.items():
            assert node.get(A + attr) == value, (name, attr)
        parent = next(p for p in old.iter() if node in list(p))
        assert parent.get(A + "layout_weight") == "0.5", name + " must retain half-width column"
    for name in ("about_sponsor", "about_manage_rpn"):
        for attr, value in {"layout_marginStart": "16dp", "layout_marginEnd": "16dp", "layout_marginTop": "10dp", "layout_marginBottom": "16dp", "paddingStart": "18dp"}.items():
            assert nodes[name].get(A + attr) == value, (name, attr)
    shape = ET.fromstring(baseline(BASE, "app/src/main/res/drawable/rectangle_border_background.xml"))
    assert shape.find("corners").get(A + "radius") == "16dp"
    assert shape.find("stroke").get(A + "width") == "0.75dp"
    content = read(CONTENT)
    required = [
        "item { AboutHeader(b) }", "item { AboutIntroduction(b) }", "item { AboutQuickActions(b) }",
        "R.style.TextAppearance_Heading_HomeScreen", "R.style.TextAppearance_SubHeading_HomeScreen",
        "WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)",
        "Modifier.padding(horizontal = 16.dp, vertical = 8.dp)",
        "Column(Modifier.padding(horizontal = 6.dp, vertical = 5.dp))",
        'Modifier.padding(top = 20.dp, bottom = 20.dp), "about.description.intro"',
        'Modifier.padding(bottom = 20.dp), "about.description.authors"',
        'if (b.sponsorInfoUsage.isVisible) AboutDescription(b.sponsorInfoUsage.text.toString()',
        'Modifier.padding(bottom = 10.dp), "about.description.usage"',
        "modifier.fillMaxWidth().padding(horizontal = 20.dp).testTag(tag)", "lineExtraDp = 5f",
        "Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 16.dp)",
        'AboutFramedAction(b.aboutSponsor, "about.sponsor"', 'AboutFramedAction(b.aboutManageRpn, "about.manage-rpn"',
        "Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 10.dp)",
        "horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top",
        'Column(Modifier.weight(1f).testTag("about.telegram-column"))',
        'Column(Modifier.weight(1f).testTag("about.report-column"))',
        'AboutFramedAction(b.aboutTelegram, "about.telegram")', 'AboutFramedAction(b.aboutCrashLog, "about.report")',
        'AboutActionSubtitle(R.string.about_bravedns_toreport_text, "about.telegram-subtitle")',
        'AboutActionSubtitle(R.string.about_bug_report_desc, "about.report-subtitle")',
        "if (b.progressLayout.isVisible)", "padding(top = 3.dp, bottom = 10.dp)",
        "resolveWgText(LocalContext.current, LocalDensity.current, source = null)", "color = Color(defaults.colors.defaultColor)",
        "val shape = RoundedCornerShape(16.dp)", ".border(.75.dp, themeColor(R.attr.border), shape)",
        "enabled = node.isEnabled && node.isClickable", "node.performClick()",
        ".heightIn(min = 48.dp)", "start = startPadding, top = 12.dp, end = 16.dp, bottom = 12.dp",
        "start = if (node.imageResource != null) 9.dp else 0.dp", "iconTint = Color.Unspecified",
        "item { AboutSection(R.string.about_app, groupTop = 10.dp) }", "item { Spacer(Modifier.height(15.dp)) }",
        "private fun AboutSection(label: Int, groupTop: Dp = 16.dp)", "item { Spacer(Modifier.height(16.dp)) }",
        "item { AboutRow(b.aboutAppUpdate) }", "if (!node.isVisible) return",
        "utilityDimensionSp(LocalContext.current, R.dimen.large_font_text_view)",
    ]
    for token in required:
        assert token in content, "Missing About source contract: " + token
    assert content.count("if (!node.isVisible) return") == 2, "Both action renderers must honor visibility"
    assert 'fontSize = 14.sp' not in content, "Use raw-sp resource plus source typography"
    assert 'letterSpacing = 7.sp' not in content, "Header spacing must remain font-relative"
    assert 'b.aboutAppUpdate.visibility' not in content, "Do not override channel-conditioned action visibility"
    for bad in ("R.layout.", "AndroidView(", "LayoutInflater", "Binding.inflate(", "121 days", "$3.23"):
        assert bad not in content, "Forbidden presentation regression: " + bad
    for path in (STATE, HOST):
        assert read(path) == baseline(BEFORE, path), "Controller/state changed outside presentation scope: " + path
    tests = read(TEST)
    for token in ("application = Application::class", "pairedActionsKeepEqualColumnsAndIndependentRecordedCallbacks",
                  "rtlLargeTextKeepsPairedActionsAndTheirOwnSubtitles", "reportProgressStaysInTheRightColumnAndDoesNotDispatch",
                  "introductionPreservesDynamicUsageAndConditionalSponsorActions", "updateVisibilityRemainsOwnedByPresentationState",
                  "headerUsesSourceRelativeTrackingAndFontPadding", "boundsInRoot", "calls.isEmpty()"):
        assert token in tests, "Missing synthetic coverage: " + token
    for bad in ("RethinkDnsApplication", "AboutFragment(", "HomeScreenActivity(", "VpnController.", "startService(", "launchFragment", "File(" ):
        assert bad not in tests, "Unsafe synthetic fixture dependency: " + bad
    return len(required)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    count = check()
    rejected = 0
    if args.self_test:
        mutations = [
            (CONTENT, "WindowInsetsSides.Top + WindowInsetsSides.Horizontal", "WindowInsetsSides.Bottom"),
            (CONTENT, "Column(Modifier.padding(horizontal = 6.dp, vertical = 5.dp))", "Column(Modifier.padding(0.dp))"),
            (CONTENT, 'Modifier.padding(top = 20.dp, bottom = 20.dp), "about.description.intro"', 'Modifier.padding(10.dp), "about.description.intro"'),
            (CONTENT, "lineExtraDp = 5f", "lineExtraDp = 0f"),
            (CONTENT, "Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 10.dp)", "Column(Modifier.fillMaxWidth()"),
            (CONTENT, 'Column(Modifier.weight(1f).testTag("about.telegram-column"))', 'Column(Modifier.testTag("about.telegram-column"))'),
            (CONTENT, "Arrangement.spacedBy(10.dp)", "Arrangement.spacedBy(0.dp)"),
            (CONTENT, "RoundedCornerShape(16.dp)", "RoundedCornerShape(4.dp)"),
            (CONTENT, ".border(.75.dp,", ".border(1.dp,"),
            (CONTENT, "if (!node.isVisible) return", "if (false) return"),
            (CONTENT, "b.sponsorInfoUsage.text.toString()", '"121 days, $3.23"'),
            (CONTENT, "node.performClick()", "Unit"),
            (CONTENT, "R.style.TextAppearance_Heading_HomeScreen", "R.style.TextAppearance_SubHeading_HomeScreen"),
            (TEST, "application = Application::class", "application = RethinkDnsApplication::class"),
            (HOST, "b.aboutAppUpdate.visibility = View.GONE", "b.aboutAppUpdate.visibility = View.VISIBLE"),
        ]
        for path, old, new in mutations:
            source = (ROOT / path).read_text()
            assert old in source, "Invalid negative control: " + old
            try:
                check({path: source.replace(old, new)})
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError("Guard missed negative control: " + old)
    result = {"baseline": BASE, "pre_change_controller_commit": BEFORE,
              "kind": "source_only_not_compile_render_or_runtime", "passed": True,
              "contracts": count, "negative_controls_rejected": rejected,
              "authored_compose_fixtures": 6, "android_compile": "UNRUN", "compose_bounds_tests": "UNRUN", "device_rendering": "UNRUN",
              "sha256": {path: hashlib.sha256((ROOT / path).read_bytes()).hexdigest() for path in (CONTENT, TEST)}}
    if args.report:
        args.report.write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps(result, indent=2))

if __name__ == "__main__":
    main()
