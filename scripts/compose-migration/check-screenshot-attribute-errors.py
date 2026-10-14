#!/usr/bin/env python3
"""Source-only guard for the October 14 screenshot's resource-namespace errors.

This checks the owning resource package and existing indexed resolution contracts.
It does not compile Kotlin, resolve an AAR, run Android, or establish visual parity.
"""
import argparse
import ast
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
COMPOUND = "app/src/main/java/com/celzero/bravedns/ui/compose/controls/OriginalCompoundControls.kt"
CATALOG = "app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgTextCatalog.kt"
GENERATOR = "scripts/compose-migration/check-wg-text-progress-source.py"

# MaterialSwitch 1.14.0 extends SwitchCompat and reads core drawables/tints from super:
# https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/materialswitch/MaterialSwitch.java
# AppCompat owns unprefixed switch attrs; only the thumb drawable uses android:thumb:
# https://github.com/androidx/androidx/blob/androidx-main/appcompat/appcompat/src/main/res/values/attrs.xml
# MaterialCheckBox 1.14.0 explicitly uses androidx.appcompat.R.attr.checkboxStyle:
# https://github.com/material-components/material-components-android/blob/1.14.0/lib/java/com/google/android/material/checkbox/MaterialCheckBox.java
SWITCH_ATTRS = {
    6: "android.R.attr.thumb",
    7: "androidx.appcompat.R.attr.thumbTint",
    8: "androidx.appcompat.R.attr.track",
    9: "androidx.appcompat.R.attr.trackTint",
    10: "MaterialR.attr.thumbIcon",
    11: "MaterialR.attr.thumbIconTint",
    12: "MaterialR.attr.trackDecoration",
    13: "MaterialR.attr.trackDecorationTint",
    22: "androidx.appcompat.R.attr.switchMinWidth",
}
MATERIAL_CHECKBOX_KEYS = (
    "dialog_wg_apps:wg_include_app_bulk_check",
    "dialog_wg_apps:wg_include_app_deselect_all_check",
    "activity_rpn_bypass_apps:rpn_bypass_bulk_check",
    "activity_rpn_bypass_apps:rpn_bypass_deselect_all_check",
)


def check(overrides=None):
    overrides = overrides or {}
    read = lambda path: overrides[path] if path in overrides else (ROOT / path).read_text()
    compound, catalog, generator = map(read, (COMPOUND, CATALOG, GENERATOR))
    match = re.search(r"val attrs = intArrayOf\((.*?)\)\s*fun overlay", compound, re.S)
    assert match, "Compound resolver attribute array missing"
    attrs = [item.strip() for item in match[1].split(",")]
    assert len(attrs) == 29, "Compound resolver attribute indices changed"
    for index, expected in SWITCH_ATTRS.items():
        assert attrs[index] == expected, f"Attribute {index} must resolve {expected}"
    assert "if (switch) MaterialR.attr.materialSwitchStyle" in compound, "Material switch default changed"
    assert "OriginalCompoundKind.MaterialSwitch -> MaterialR.style.Widget_Material3_CompoundButton_MaterialSwitch" in compound, "Material switch fallback changed"

    rows = dict(re.findall(r'^\s*"([^"]+)" to WgTextSource\(([^\n]+)\),$', catalog, re.M))
    for key in MATERIAL_CHECKBOX_KEYS:
        expected = f"R.style.OriginalWgText_{key.replace(':', '_')}, false, androidx.appcompat.R.attr.checkboxStyle"
        assert rows.get(key) == expected, f"MaterialCheckBox default attribute mismatch: {key}"

    # Evaluate only the generator's literal tag-to-attribute conditional, independently
    # of its expected-output comparison. This also catches matching bad generator/output.
    defaults = [node.value for node in ast.walk(ast.parse(generator))
                if isinstance(node, ast.Assign)
                and any(isinstance(target, ast.Name) and target.id == "default" for target in node.targets)]
    assert len(defaults) == 1, "Expected one generator default-attribute mapping"
    expression = compile(ast.Expression(defaults[0]), GENERATOR, "eval")
    for tag in ("com.google.android.material.checkbox.MaterialCheckBox", "androidx.appcompat.widget.AppCompatCheckBox"):
        actual = eval(expression, {"__builtins__": {}}, {"tag": tag})
        assert actual == "androidx.appcompat.R.attr.checkboxStyle", f"Generator changed checkbox owner: {tag}"
    return len(SWITCH_ATTRS) + len(MATERIAL_CHECKBOX_KEYS) + 5


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    contracts = check()
    rejected = 0
    if args.self_test:
        mutations = []
        for attr in ("thumbTint", "track", "trackTint", "switchMinWidth"):
            for namespace in ("MaterialR", "android.R"):
                mutations.append((COMPOUND, f"androidx.appcompat.R.attr.{attr},", f"{namespace}.attr.{attr},"))
        mutations.append((COMPOUND, "android.R.attr.thumb,", "MaterialR.attr.thumb,"))
        for key in MATERIAL_CHECKBOX_KEYS:
            old = f'"{key}" to WgTextSource(R.style.OriginalWgText_{key.replace(":", "_")}, false, androidx.appcompat.R.attr.checkboxStyle)'
            mutations.append((CATALOG, old, old.replace("androidx.appcompat.R", "com.google.android.material.R")))
        mutations.append((GENERATOR, "default='androidx.appcompat.R.attr.checkboxStyle'", "default='com.google.android.material.R.attr.checkboxStyle'"))
        for path, old, new in mutations:
            text = (ROOT / path).read_text()
            assert old in text, f"Invalid self-test mutation: {old}"
            try:
                check({path: text.replace(old, new, 1)})
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError(f"Namespace regression escaped: {old} -> {new}")
    report = {"passed": True, "source_only": True, "contracts": contracts,
              "negative_controls_rejected": rejected, "kotlin_compile": "not_run", "android_runtime": "not_run"}
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2) + "\n")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
