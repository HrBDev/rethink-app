#!/usr/bin/env python3
"""Source-only Home gutter regression guard; never builds or launches Android."""
import argparse
import json
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
BASE = "23cb6dd98805445174bd83b7237ad5f5d2c24ac9"
CONTENT = "app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeContent.kt"
SPACING = "app/src/main/java/com/celzero/bravedns/ui/compose/home/HomeCardSpacing.kt"
TEST = "app/src/test/java/com/celzero/bravedns/ui/compose/home/HomeSectionSpacingTest.kt"
STYLES = "app/src/main/res/values/styles.xml"


def check(overrides=None):
    overrides = overrides or {}
    read = lambda path: overrides[path] if path in overrides else (ROOT / path).read_text()
    baseline = lambda path: subprocess.check_output(["git", "show", f"{BASE}:{path}"], cwd=ROOT, text=True)
    for source in (baseline(STYLES), read(STYLES)):
        styles = {node.attrib["name"]: node for node in ET.fromstring(source).findall("style")}
        for name, elevation in [("Widget.Rethink.MaterialCardView", "2dp"), ("Widget.Rethink.MaterialCardView.Frost", "0dp")]:
            values = {node.attrib["name"]: node.text for node in styles[name].findall("item")}
            assert values["cardUseCompatPadding"] == "true", f"Missing baseline outside padding: {name}"
            assert values["cardElevation"] == elevation, f"Baseline elevation changed: {name}"
    android = "{http://schemas.android.com/apk/res/android}"
    app = "{http://schemas.android.com/apk/res-auto}"
    for folder in ("layout", "layout-w600dp"):
        root = ET.fromstring(baseline(f"app/src/full/res/{folder}/fragment_home_screen.xml"))
        nodes = {node.attrib.get(android + "id", "").split("/")[-1]: node for node in root.iter()}
        assert nodes["fhs_card_dns_ll"].attrib[app + "cardCornerRadius"] == "24dp"
        assert nodes["fhs_throughput_ll"].attrib[app + "cardCornerRadius"] == "24dp"
        assert nodes["fhs_card_firewall_rules_card"].attrib[app + "cardElevation"] == "0dp"
        assert nodes["fhs_card_logs_ll"].attrib[android + "layout_marginStart"] == "2dp"
    content, spacing, tests = read(CONTENT), read(SPACING), read(TEST)
    required_content = [
        "modifier.homeTourAnchor(node.id).padding(rememberHomeCardPadding(radius, elevationOverride))",
        'wide = wide, elevationOverride = 0.dp',
        "ThroughputCard(b, Modifier.weight(.35f).padding(end = 2.dp).fillMaxHeight(), false)",
        "Row(Modifier.fillMaxWidth().padding(start = 2.dp).heightIn(min = 120.dp)",
        "ProxyCard(b, Modifier.weight(1f).padding(end = 2.dp), false)",
        "LogsCard(b, false, Modifier.fillMaxWidth().padding(horizontal = 2.dp))",
        "AppsCard(b, false, Modifier.fillMaxWidth().padding(horizontal = 2.dp))",
        "DnsCard(b, Modifier.weight(.7f).padding(start = 2.dp), true)",
        "LogsCard(b, true, Modifier.fillMaxWidth().padding(start = 2.dp, end = 8.dp))",
        "Column(Modifier.weight(.42f).padding(start = 10.dp, end = 2.dp)",
        "Row(Modifier.fillMaxWidth().padding(top = 12.dp))",
        "verticalArrangement = Arrangement.spacedBy(12.dp)",
    ]
    for token in required_content:
        assert token in content, f"Home gutter/group contract missing: {token}"
    assert ".padding(bottom = 80.dp)" not in content, "Activity-owned navigation clearance must not be added twice"
    assert content.index(".padding(rememberHomeCardPadding") < content.index(".clip(shape).background"), "Gutters must sit outside the painted surface"
    for token in ["cardUseCompatPadding", "cardPreventCornerOverlap", "cardElevation", "cardMaxElevation", "materialCardViewStyle",
                  "(1.0 - cos(Math.PI / 4.0)) * radiusPx", "ceil(maxElevationPx + cornerPadding)",
                  "ceil(1.5 * maxElevationPx + cornerPadding)", "elevationOverride?.toPx()", "values.recycle()"]:
        assert token in spacing, f"Baseline card-inset calculation missing: {token}"
    for token in ["AndroidView(", "LayoutInflater", "MaterialCardView(", "R.layout."]:
        assert token not in content + spacing, f"Legacy View renderer introduced: {token}"
    for token in ["application = Application::class", "phoneSectionsKeepOutsideGuttersAndOriginalGroups",
                  "phoneRtlLargeTextKeepsOutsideGuttersAndOriginalGroups", "frostCardsStillHaveCornerClearanceAtZeroElevation",
                  "wideRtlLargeTextKeepsConnectionLogsAndRulesProxyAppsGroups", "baselineInsetsKeepRadiusAndElevationDifferences",
                  "assertHorizontalGap", "assertVerticalGap", "calls.isEmpty()"]:
        assert token in tests, f"Synthetic coverage source missing: {token}"
    for token in ["RethinkDnsApplication", "HomeScreenActivity(", "HomeScreenFragment(", "VpnController.", "launchFragment", "startService("]:
        assert token not in tests, f"Production execution in synthetic test: {token}"
    return len(required_content)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    contracts = check()
    rejected = 0
    if args.self_test:
        mutations = [
            (CONTENT, ".padding(rememberHomeCardPadding(radius, elevationOverride))", ""),
            (CONTENT, "elevationOverride = 0.dp", "elevationOverride = 2.dp"),
            (CONTENT, "padding(start = 10.dp, end = 2.dp)", "padding(start = 0.dp, end = 0.dp)"),
            (CONTENT, "padding(horizontal = 2.dp)", "padding(horizontal = 0.dp)"),
            (CONTENT, "Modifier.fillMaxSize().background(themeColor(R.attr.background))", "Modifier.fillMaxSize().background(themeColor(R.attr.background)).padding(bottom = 80.dp)"),
            (SPACING, "(1.0 - cos(Math.PI / 4.0)) * radiusPx", "0.0"),
            (SPACING, "ceil(1.5 * maxElevationPx + cornerPadding)", "ceil(maxElevationPx + cornerPadding)"),
            (STYLES, '<item name="cardUseCompatPadding">true</item>', '<item name="cardUseCompatPadding">false</item>'),
            (TEST, "application = Application::class", "application = RethinkDnsApplication::class"),
        ]
        for path, old, new in mutations:
            source = (ROOT / path).read_text()
            assert old in source, f"Invalid self-test mutation: {old}"
            try:
                check({path: source.replace(old, new)})
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError(f"Guard missed regression: {old}")
    report = {"baseline": BASE, "kind": "source_only_not_compile_or_runtime", "passed": True,
              "layout_contracts": contracts, "self_test_mutations_rejected": rejected,
              "android_compilation": "not_run", "compose_bounds_tests": "not_run"}
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2) + "\n")
    print(f"Home section spacing source guard passed: {contracts} layout contracts; {rejected} mutations rejected. Not compile/runtime evidence.")


if __name__ == "__main__":
    main()
