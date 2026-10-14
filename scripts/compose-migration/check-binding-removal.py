#!/usr/bin/env python3
"""Deterministic whole-source renderer/binding census; never builds or launches Android.

This is a conservative lexical guard, not a Kotlin compiler or a whole-program
reachability proof. Every production source set is discovered on each run. Tests
and retained reference XML are inventoried separately, not silently counted as
production screens. See docs/compose-migration/binding-removal-audit.md.
"""
from __future__ import annotations

import argparse
from collections import Counter
import hashlib
import json
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
PKG = "app/src/main/java/com/celzero/bravedns/"
BASELINE = "23cb6dd98805445174bd83b7237ad5f5d2c24ac9"
RECOVERED_COMMIT = "87b3b9975afdb59492bc92cf97b9560cdd725330"
RECOVERED_TREE = "1949435b20c49bb03ec06123c27f913a4974882b"

# Retained historical View implementations have no active construction sites.
# AppHistogramView.Entry remains a controller/presentation DTO. Retaining its
# containing class does not instantiate its View. XML references are historical
# resources, and no app-owned layout resource may be referenced by runtime code.
DORMANT = {
    "AnimatedBorderCardView": PKG + "ui/custom/AnimatedBorderCardView.kt",
    "AppHistogramView": PKG + "ui/custom/AppHistogramView.kt",
    "CountryMapView": PKG + "ui/custom/CountryMapView.kt",
    "DolphinOceanView": PKG + "ui/custom/DolphinOceanView.kt",
    "DonutChartView": PKG + "ui/custom/DonutChartView.kt",
    "EmbeddedDolphinSignature": PKG + "ui/custom/EmbeddedDolphinSignature.kt",
    "FlagWatermarkView": PKG + "ui/custom/FlagWatermarkView.kt",
    "MoneyBackBadgeView": PKG + "ui/custom/MoneyBackBadgeView.kt",
    "RuleStateSwitch": PKG + "ui/custom/RuleStateSwitch.kt",
    "MaxWidthFrameLayout": PKG + "ui/MaxWidthFrameLayout.kt",
    "SpotlightOverlayView": PKG + "ui/tour/SpotlightOverlayView.kt",
}
# Framework window/navigation shells are not app controls. Constructors outside
# these named owners require review, even if they only create another container.
FRAMEWORK_HOSTS = {
    PKG + "ui/HomeScreenActivity.kt",
    PKG + "ui/activity/ConfigureRethinkBasicActivity.kt",
    PKG + "ui/activity/FragmentHostActivity.kt",
    PKG + "ui/activity/FirewallActivity.kt",
    PKG + "ui/activity/DnsDetailActivity.kt",
    PKG + "ui/compose/dnssettings/DnsFragmentHost.kt",
}
FRAMEWORK_TYPES = {
    "android.widget.FrameLayout", "android.widget.LinearLayout",
    "androidx.coordinatorlayout.widget.CoordinatorLayout",
    "androidx.fragment.app.FragmentContainerView",
}
NOTIFICATION = PKG + "ui/NotificationHandlerActivity.kt"
FRAGMENT_BRIDGE = PKG + "ui/compose/ActivityFragmentContainer.kt"
SETTINGS_BRIDGE = PKG + "ui/compose/settings/SingleSettingsFragmentHost.kt"
FRAGMENT_BRIDGES = {
    FRAGMENT_BRIDGE: (
        'AndroidView(factory = { container }, modifier = modifier)',
        'FragmentContainerView(context).apply { id = containerId }',
    ),
    SETTINGS_BRIDGE: (
        'AndroidView( factory = { container }, modifier = Modifier.fillMaxSize().background(themeColor(R.attr.background)), )',
        'FragmentContainerView(context).apply { id = R.id.fragment_container }',
    ),
}
FRAMEWORK_HOSTS.update(FRAGMENT_BRIDGES)

BINDING = re.compile(
    r"\b(?:ViewBinding|ViewDataBinding|DataBindingUtil|DataBinderMapper|DataBindingComponent|"
    r"viewBinding|dataBinding|AndroidViewBinding)\b|\b(?:[\w]+\.)*databinding\."
    r"|\bdev\.androidbroadcast\.vbpd\b|\b\w+Binding\s*(?:::|\.(?:inflate|bind)\s*\()"
)
IMPORT = re.compile(r"^\s*import\s+([\w.]+)(?:\s+as\s+(\w+))?", re.M)
NATIVE_PREFIXES = (
    "android.widget.", "androidx.appcompat.widget.",
    "androidx.recyclerview.widget.", "androidx.viewpager.widget.",
    "androidx.viewpager2.widget.", "androidx.constraintlayout.widget.",
    "androidx.coordinatorlayout.widget.", "android.webkit.WebView",
    "com.google.android.material.button.", "com.google.android.material.card.",
    "com.google.android.material.chip.", "com.google.android.material.textfield.",
    "com.google.android.material.switchmaterial.", "com.google.android.material.tabs.",
    "com.google.android.material.bottomnavigation.", "com.google.android.material.navigation.",
    "com.google.android.material.progressindicator.", "com.google.android.material.slider.",
    "com.google.android.material.checkbox.", "com.google.android.material.radiobutton.",
)
NATIVE_EXACT = {"android.view.View", "android.view.SurfaceView", "android.view.TextureView",
                "androidx.fragment.app.FragmentContainerView"}
NATIVE_DIALOGS = {"com.google.android.material.dialog.MaterialAlertDialogBuilder",
                  "android.app.AlertDialog", "androidx.appcompat.app.AlertDialog",
                  "android.widget.PopupWindow", "android.widget.PopupMenu"}
# Exact MDC model/drawable types, not Views. They paint source styling on a Compose
# Canvas; similarly named ProgressIndicator widgets still fail this guard.
NON_VIEW_MATERIAL_TYPES = {
    "com.google.android.material.progressindicator.LinearProgressIndicatorSpec",
    "com.google.android.material.progressindicator.CircularProgressIndicatorSpec",
    "com.google.android.material.shape.MaterialShapeDrawable",
}


def without_comments(text: str) -> str:
    """Mask nested Kotlin/Java comments, preserving strings and source positions.

    Keeping literals is conservative: a hidden inflater in a Kotlin ${...}
    template remains detectable. References in runtime strings also require
    review rather than providing a loophole for reflection/resource lookup.
    """
    out = list(text)
    i, n = 0, len(text)
    while i < n:
        if text.startswith('//', i):
            end = text.find('\n', i)
            end = n if end < 0 else end
            out[i:end] = ' ' * (end-i)
            i = end
        elif text.startswith('/*', i):
            start, depth = i, 1
            i += 2
            while i < n and depth:
                if text.startswith('/*', i): depth += 1; i += 2
                elif text.startswith('*/', i): depth -= 1; i += 2
                else: i += 1
            out[start:i] = ['\n' if c == '\n' else ' ' for c in text[start:i]]
        elif text.startswith('"""', i):
            end = text.find('"""', i + 3)
            i = n if end < 0 else end + 3
        elif text[i] in '\"\'':
            quote = text[i]
            i += 1
            while i < n:
                if text[i] == '\\': i += 2
                elif text[i] == quote: i += 1; break
                else: i += 1
        else:
            i += 1
    return ''.join(out)


def production(path: str) -> bool:
    parts = Path(path).parts
    return len(parts) > 3 and parts[:2] == ('app', 'src') and not (
        parts[2].lower().startswith('test') or parts[2].lower().startswith('androidtest')
    )


def record(path, code, match, rule, detail=None):
    return {'path': path, 'line': code.count('\n', 0, match.start()) + 1,
            'rule': rule, 'detail': detail or match[0].strip()}


def analyze_code(path: str, text: str):
    code = without_comments(text)
    errors, inventory = [], []
    for pattern, rule in [
        (BINDING, 'binding-api'),
        (re.compile(r'\bAndroidView(?:Binding)?\b'), 'view-in-compose'),
        (re.compile(r'\b(?:inflate|inflateAsync|getLayout)\s*(?:<[^>\n]+>)?\s*\('), 'inflation-entrypoint'),
        (re.compile(r'(?<![\w.])(?:[\w]+\.)*R\.layout\.\w+'), 'layout-resource'),
        (re.compile(r'\b(?:onCreateViewHolder|getDropDownView)\s*\('), 'legacy-adapter-renderer'),
        (re.compile(r'\b(?:RecyclerView\.Adapter|PagingDataAdapter|BaseAdapter|ArrayAdapter)\s*(?:<[^;{]+>)?\s*\('), 'legacy-adapter-construction'),
    ]:
        for match in pattern.finditer(code):
            if rule == 'view-in-compose' and path in FRAGMENT_BRIDGES and match[0] == 'AndroidView':
                inventory.append(record(path, code, match, 'framework-fragment-interop'))
            else:
                errors.append(record(path, code, match, rule))
    if path in FRAGMENT_BRIDGES:
        # Exact interop shape, not a file-wide exemption for wrapping old UI.
        compact = re.sub(r'\s+', ' ', code)
        factory_shape, container_shape = FRAGMENT_BRIDGES[path]
        if (len(re.findall(r'\bAndroidView\b', code)) != 2 or
                factory_shape not in compact or
                len(re.findall(r'\bFragmentContainerView\s*\(', code)) != 1 or
                container_shape not in compact or
                re.search(r'\b(?:TextView|ImageView|WebView|RecyclerView|ViewPager2|addView|addContentView)\s*\(', code)):
            errors.append({'path': path, 'line': 1, 'rule': 'fragment-interop-contract',
                           'detail': 'Only one fixed FragmentContainerView factory is approved per reviewed bridge'})
        root_calls = list(re.finditer(r'\bsetContentView\s*\(', code))
        allowed_root = path == SETTINGS_BRIDGE and len(root_calls) == 1 and 'setContentView(ComposeView(this).apply {' in compact
        if root_calls and not allowed_root:
            errors.append({'path': path, 'line': 1, 'rule': 'fragment-interop-root',
                           'detail': 'Only the reviewed settings ComposeView root is allowed'})
    # Resolve resource aliases as well as conventional R.layout references.
    for imp in IMPORT.finditer(code):
        if imp[2] and imp[1].endswith('.R'):
            for match in re.finditer(r'\b' + re.escape(imp[2]) + r'\.layout\.\w+', code):
                errors.append(record(path, code, match, 'aliased-layout-resource'))
    # Wildcard UI imports and native constructor aliases are intentionally
    # conservative: expanding this boundary must be reviewed explicitly.
    for match in re.finditer(r'^\s*import\s+(?:android\.widget|android\.view|androidx\.appcompat\.widget|com\.google\.android\.material[\w.]*)\.\*', code, re.M):
        errors.append(record(path, code, match, 'wildcard-native-import'))
    # Dynamic layout lookup would evade R.layout scanning. Other resource kinds
    # (the current DNS string lookup) are allowed and recorded for audit.
    for match in re.finditer(r'\bgetIdentifier\s*\([^\n]*', code):
        item = record(path, code, match, 'dynamic-resource-lookup')
        inventory.append(item)
        if not re.search(r',\s*"(?:string|drawable|color|id)"\s*,', match[0]):
            errors.append(item)
    # Resolve simple import aliases before looking for widget constructors.
    for match in IMPORT.finditer(code):
        qualified, alias = match[1], match[2] or match[1].rsplit('.', 1)[-1]
        native = qualified.startswith(NATIVE_PREFIXES) or qualified in NATIVE_EXACT
        dialog = qualified in NATIVE_DIALOGS
        if not native and not dialog and qualified not in NON_VIEW_MATERIAL_TYPES: continue
        constructor = re.compile(r'(?<![\w.])' + re.escape(alias) + r'\s*(?:<[^>\n]+>\s*)?(?:\.Builder\s*)?\(|::' + re.escape(alias) + r'\b')
        for type_alias in re.finditer(r'\btypealias\s+\w+\s*=\s*' + re.escape(alias) + r'\b', code):
            errors.append(record(path, code, type_alias, 'native-type-alias'))
        for call in constructor.finditer(code):
            if qualified in NON_VIEW_MATERIAL_TYPES:
                inventory.append(record(path, code, call, 'nonview-material-drawing-model', qualified))
                continue
            item = record(path, code, call, 'native-constructor', qualified)
            inventory.append(item)
            dormant = path in DORMANT.values()
            framework = (path in FRAMEWORK_HOSTS and qualified in FRAMEWORK_TYPES and
                         (path not in FRAGMENT_BRIDGES or qualified == 'androidx.fragment.app.FragmentContainerView'))
            if not dormant and not framework:
                errors.append(item)
    # Qualified native constructors need no import. Restrict to capitalized type
    # names; constants/method calls and Compose material3 functions are distinct.
    for match in re.finditer(r'\b(?:(?:android\.widget|androidx\.appcompat\.widget|com\.google\.android\.material\.[\w]+|android\.webkit)\.[A-Z]\w*|android\.view\.(?:View|SurfaceView|TextureView))\s*\(', code):
        qualified = match[0].split('(')[0].strip()
        if qualified in NON_VIEW_MATERIAL_TYPES:
            inventory.append(record(path, code, match, 'nonview-material-drawing-model', qualified))
            continue
        item = record(path, code, match, 'qualified-native-constructor', qualified)
        inventory.append(item)
        if path not in DORMANT.values() and not (path in FRAMEWORK_HOSTS and qualified in FRAMEWORK_TYPES and path not in FRAGMENT_BRIDGES):
            errors.append(item)
    # Custom historical widgets cannot become live again through a constructor,
    # factory reference, class literal, imported alias, or reflective class name.
    # The one approved nested DTO does not allocate AppHistogramView itself.
    for symbol, owner in DORMANT.items():
        if path == owner: continue
        alias_names = {symbol}
        for imp in IMPORT.finditer(code):
            if imp[1].endswith('.' + symbol) and imp[2]: alias_names.add(imp[2])
        for alias in alias_names:
            for match in re.finditer(r'\b' + re.escape(alias) + r'\b', code):
                rest = code[match.end():]
                line_start = code.rfind('\n', 0, match.start()) + 1
                if code[line_start:match.start()].lstrip().startswith('import '): continue
                if symbol == 'AppHistogramView' and re.match(r'\s*\.\s*Entry\b', rest):
                    inventory.append(record(path, code, match, 'historical-widget-dto', symbol))
                    continue
                errors.append(record(path, code, match, 'historical-widget-reference', symbol))
    # No opaque integer/factory can sneak a layout into setContentView. Named
    # View roots are allowed only at the two inspected ComposeView definitions.
    for match in re.finditer(r'\b(?:setContentView|addContentView)\s*\(', code):
        tail = code[match.end():].lstrip()
        known = tail.startswith('ComposeView(')
        known |= path in {PKG + 'ui/activity/SmartDnsListActivity.kt', PKG + 'ui/activity/ConfigureOtherDnsActivity.kt'} and tail.startswith('dnsFragmentHost(')
        named_roots = {
            PKG + 'ui/activity/RpnConfigDetailActivity.kt': 'composeRoot',
            PKG + 'ui/compose/wireguard/WgDialogContent.kt': 'view',
        }
        name = named_roots.get(path)
        if name and tail.startswith(name + ')'):
            known |= bool(re.search(r'\bval\s+' + name + r'\s*=\s*ComposeView\s*\(', code))
            known |= bool(re.search(r'\bvar\s+' + name + r'\s*:\s*ComposeView\b', code) and
                          re.search(r'\b' + name + r'\s*=\s*ComposeView\s*\(', code))
        if not known:
            errors.append(record(path, code, match, 'unreviewed-content-root'))
    # Legacy Activity/Fragment XML constructors can otherwise receive a numeric
    # resource ID without spelling R.layout. BaseActivity alone forwards its
    # default-zero parameter; no concrete production host may pass a layout.
    for match in re.finditer(r'\b(?:BaseActivity|AppCompatActivity|Fragment|DialogFragment)\s*\(\s*[^)\s]', code):
        if path != PKG + 'ui/BaseActivity.kt':
            errors.append(record(path, code, match, 'layout-capable-constructor'))
    # These remaining calls are explicitly visible in the report, even though
    # setContentView(ComposeView/root) and ComposeView attachment are legitimate.
    for match in re.finditer(r'\b(?:setContentView|addContentView|addView|AndroidFragment|ComposeView|bindService|unbindService)\s*(?:<[^>\n]+>)?\s*\(', code):
        inventory.append(record(path, code, match, 'host-or-service-boundary'))
    return errors, inventory


def check_build(path: str, text: str):
    code = without_comments(text)
    errors = []
    for match in re.finditer(r'\b(?:viewBinding|dataBinding)\s*(?:=\s*true|\{[^}]*enable\w*\s*=\s*true)', code, re.S):
        errors.append(record(path, code, match, 'enabled-binding-build-feature'))
    for match in re.finditer(r'dev\.androidbroadcast\.vbpd|devAndroidbroadcastVbpd|devAndroidbroadcast\.vbpd|viewbinding|androidx\.databinding|android\.databinding', code, re.I):
        # Explicitly false flags are expected and are not dependency identifiers.
        line_start = code.rfind('\n', 0, match.start()) + 1
        line_end = code.find('\n', match.start())
        line = code[line_start:line_end if line_end >= 0 else len(code)]
        if re.search(r'\b(?:viewBinding|dataBinding)\s*=\s*false\b', line): continue
        errors.append(record(path, code, match, 'binding-build-dependency'))
    return errors


def layout_removal_errors(paths, allow_retained_layouts=False):
    if allow_retained_layouts:
        return []
    return [{'path': path, 'line': 1, 'rule': 'retained-app-owned-layout',
             'detail': 'Final gate requires zero layout XML in production, test, flavor, qualified or alternate resource source sets'}
            for path in sorted(paths)]


def scan(root: Path, allow_retained_layouts=False):
    errors, inventory, hashes = [], [], {}
    all_layout_paths = [file.relative_to(root).as_posix() for file in root.rglob('*.xml')
                        if file.is_file() and file.parent.name.startswith('layout')
                        and file.parent.parent.name == 'res'
                        and not any(part in ('.git', '.gradle', 'build') for part in file.relative_to(root).parts)]
    errors.extend(layout_removal_errors(all_layout_paths, allow_retained_layouts))
    resource_hashes = {}
    source_counts = Counter()
    xml_layouts, xml_roots, resources = [], Counter(), []
    files = sorted((root / 'app' / 'src').rglob('*'))
    for file in files:
        if not file.is_file(): continue
        path = file.relative_to(root).as_posix()
        if file.suffix in ('.kt', '.java') and production(path):
            text = file.read_text()
            hashes[path] = hashlib.sha256(file.read_bytes()).hexdigest()
            source_counts[Path(path).parts[2]] += 1
            failures, findings = analyze_code(path, text)
            errors.extend(failures); inventory.extend(findings)
        elif file.suffix == '.xml' and production(path):
            resource_hashes[path] = hashlib.sha256(file.read_bytes()).hexdigest()
            text = file.read_text()
            try:
                document = ET.fromstring(text)
            except ET.ParseError as exc:
                errors.append({'path': path, 'line': 1, 'rule': 'invalid-xml', 'detail': str(exc)})
                continue
            if file.parent.name.startswith('layout'):
                xml_layouts.append(path); xml_roots[document.tag] += 1
                if document.tag == 'layout' or any('@{' in v or '@={' in v for elem in document.iter() for v in elem.attrib.values()):
                    errors.append({'path': path, 'line': 1, 'rule': 'data-binding-xml', 'detail': 'Data Binding layout/expression'})
            else:
                # Includes manifest, styles and widget metadata. This catches an
                # app widget/alias/theme that indirectly makes old XML live.
                for elem in document.iter():
                    values = list(elem.attrib.values()) + ([elem.text] if elem.text else [])
                    for value in values:
                        for match in re.finditer(r'@(?:\w+:)?layout/\w+', value):
                            errors.append({'path': path, 'line': 1, 'rule': 'nonlayout-layout-reference', 'detail': match[0]})
            for elem in document.iter():
                for symbol in DORMANT:
                    if elem.tag.endswith('.' + symbol):
                        resources.append({'path': path, 'class': symbol})
    build_paths = sorted(set(root.glob('**/*.gradle.kts')) | set(root.glob('**/*.gradle')) | set((root / 'gradle').glob('*.toml')) | {root/'gradle.properties', root/'Android.bp'})
    for file in build_paths:
        if not file.is_file() or any(part in ('.git', 'build', '.gradle') for part in file.relative_to(root).parts): continue
        path = file.relative_to(root).as_posix()
        hashes[path] = hashlib.sha256(file.read_bytes()).hexdigest()
        errors.extend(check_build(path, file.read_text()))
    # Require the reviewed empty Compose notification trampoline, never a layout or visible child.
    notification = root / NOTIFICATION
    if notification.exists():
        code = without_comments(notification.read_text())
        compact = re.sub(r'\s+', ' ', code)
        shell = ('setContentView(ComposeView(this).apply { '
                 'setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed) '
                 'setContent {} }) window.decorView.alpha = 0f')
        if (shell not in compact or len(re.findall(r'\bComposeView\s*\(', code)) != 1 or
                len(re.findall(r'\bsetContentView\s*\(', code)) != 1 or
                len(re.findall(r'\bsetContent\s*\{', code)) != 1 or
                re.search(r'\bR\.layout\.', code)):
            errors.append({'path': NOTIFICATION, 'line': 1, 'rule': 'trampoline-compose-contract',
                           'detail': 'Must retain one lifecycle-disposed empty ComposeView immediately followed by invisible decor alpha'})
    return {
        'status': 'PASS' if not errors else 'FAIL', 'source_only': True,
        'mode': 'PRE_REMOVAL_AUDIT_NOT_FINAL' if allow_retained_layouts else 'FINAL_ZERO_LAYOUT_GATE',
        'all_source_set_layout_paths': sorted(all_layout_paths),
        'guard_sha256': hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
        'baseline': BASELINE, 'recovered_commit': RECOVERED_COMMIT, 'recovered_tree': RECOVERED_TREE,
        'production_source_sets': dict(sorted(source_counts.items())),
        'production_source_files': sum(source_counts.values()),
        'retained_layout_resources': len(xml_layouts), 'retained_layout_root_types': dict(sorted(xml_roots.items())),
        'retained_custom_view_resource_references': resources,
        'errors': sorted(errors, key=lambda x: (x['path'], x['line'], x['rule'], x['detail'])),
        'inventory': sorted(inventory, key=lambda x: (x['path'], x['line'], x['rule'], x['detail'])),
        'source_and_build_sha256': dict(sorted(hashes.items())),
        'resource_sha256': dict(sorted(resource_hashes.items())),
        'limits': ['Lexical source census, not compiler or whole-program reachability proof.',
                   'No Gradle, APK inspection, emulator, Application, VPN, traffic or permission flows executed.',
                   'This guard never deletes resources; its default final mode rejects retained layout XML in every source set. Explicit --allow-retained-layouts is audit-only, not final acceptance.',
                   'Framework Dialog/BottomSheetDialog, two AndroidView FragmentContainerView bridges, navigation containers, ComposeView, platform toast and an empty invisible Compose notification trampoline remain.'],
    }


def self_test():
    p = 'app/src/newFlavor/java/example/Probe.kt'
    mutations = {
        'generated binding import': 'import com.example.databinding.ExampleBinding',
        'binding delegate': 'val b by viewBinding(ExampleBinding::bind)',
        'data binding': 'DataBindingUtil.setContentView(this, 1)',
        'opaque content root': 'setContentView(layoutFromElsewhere())',
        'numeric Activity layout': 'class Probe : BaseActivity(123)',
        'numeric Fragment layout': 'class Probe : Fragment(123)',
        'XML constructor': 'class Probe : BaseActivity(R.layout.old_screen)',
        'framework layout no longer accepted': 'setContentView(android.R.layout.activity_list_item)',
        'inflation across lines': 'inflater\n .inflate\n (layout, parent, false)',
        'AndroidView wrapper': 'AndroidView(factory = { oldView })',
        'AndroidView alias': 'import androidx.compose.ui.viewinterop.AndroidView as Wrapped',
        'resource class alias': 'import example.R as Resources\nsetContentView(Resources.layout.old)',
        'native constructor reference': 'import android.widget.TextView\nval f = ::TextView',
        'native type alias': 'import android.widget.TextView\ntypealias Label = TextView\nLabel(ctx)',
        'wildcard widget import': 'import android.widget.*\nTextView(context)',
        'qualified native View': 'val view = android.view.View(context)',
        'native widget alias': 'import android.widget.TextView as Label\nval label = Label(context)',
        'qualified native widget': 'val label = android.widget.TextView(context)',
        'real Material dialog alias': 'import com.google.android.material.dialog.MaterialAlertDialogBuilder as Builder\nBuilder(context).show()',
        'dormant constructor': 'val chart = DonutChartView(context)',
        'dormant factory reference': 'val chart = ::DonutChartView',
        'dormant alias': 'import com.example.DonutChartView as Chart\nval chart = Chart(context)',
        'dynamic layout name': 'resources.getIdentifier(name, "layout", packageName)',
        'retired adapter renderer': 'override fun onCreateViewHolder(parent: ViewGroup, type: Int) = oldHolder',
        'Kotlin string interpolation': 'val label = "${inflater.inflate(layout, null)}"',
    }
    for label, text in mutations.items():
        assert analyze_code(p, text)[0], f'Negative control escaped: {label}'
    assert check_build('app/build.gradle.kts', 'viewBinding = true')
    assert check_build('gradle/lib.toml', 'vb = { module = "dev.androidbroadcast.vbpd:vbpd" }')
    assert not check_build('app/build.gradle.kts', 'viewBinding = false\ndataBinding = false')
    harmless = '/* outer /* AndroidView { } */ inflate(1) */\n// ViewBinding\nval url = "https://example.test"\nval entry: AppHistogramView.Entry? = null'
    assert not analyze_code(p, harmless)[0], 'Comments or nested DTO were mistaken for a runtime renderer'
    assert production(p) and not production('app/src/testWebsite/java/example/Probe.kt')
    bridge = ('import androidx.compose.ui.viewinterop.AndroidView\n'
              'import androidx.fragment.app.FragmentContainerView\n'
              'val container = FragmentContainerView(context).apply { id = containerId }\n'
              'AndroidView(factory = { container }, modifier = modifier)')
    assert not analyze_code(FRAGMENT_BRIDGE, bridge)[0]
    assert analyze_code(FRAGMENT_BRIDGE, bridge.replace('{ container }', '{ android.widget.TextView(context) }'))[0]
    assert analyze_code(FRAGMENT_BRIDGE, bridge + '\nval old = inflater.inflate(R.layout.old, null)')[0]
    assert analyze_code(FRAGMENT_BRIDGE, bridge + '\ncontainer.addView(existingView)')[0]
    settings = ('import androidx.compose.ui.viewinterop.AndroidView\n'
                'import androidx.fragment.app.FragmentContainerView\n'
                'setContentView(ComposeView(this).apply { setContent { content() } })\n'
                'val container = FragmentContainerView(context).apply { id = R.id.fragment_container }\n'
                'AndroidView( factory = { container }, modifier = Modifier.fillMaxSize().background(themeColor(R.attr.background)), )')
    assert not analyze_code(SETTINGS_BRIDGE, settings)[0]
    assert analyze_code(SETTINGS_BRIDGE, settings.replace('factory = { container }', 'factory = { oldView }'))[0]
    assert analyze_code(SETTINGS_BRIDGE, settings + '\ncontainer.addView(existingView)')[0]
    assert layout_removal_errors(['app/src/testFuture/res/layout-night/old.xml'])
    assert layout_removal_errors(['other/src/debug/res/layout-w600dp/old.xml'])
    assert not layout_removal_errors([])
    assert not layout_removal_errors(['app/src/main/res/layout/old.xml'], allow_retained_layouts=True)
    for name in sorted(NON_VIEW_MATERIAL_TYPES):
        short = name.rsplit('.', 1)[-1]
        assert not analyze_code(p, f'import {name}\nval drawing = {short}(context)')[0]
        assert not analyze_code(p, f'val drawing = {name}(context)')[0]
    for widget in ('LinearProgressIndicator', 'CircularProgressIndicator'):
        name = 'com.google.android.material.progressindicator.' + widget
        assert analyze_code(p, f'import {name}\nval widget = {widget}(context)')[0]
        assert analyze_code(p, f'val widget = {name}(context)')[0]
    return len(mutations) + 25


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--repo', type=Path, default=ROOT)
    parser.add_argument('--report', type=Path)
    parser.add_argument('--self-test', action='store_true')
    parser.add_argument('--allow-retained-layouts', action='store_true',
                        help='Explicit pre-removal source audit only; not final zero-layout acceptance')
    args = parser.parse_args()
    controls = self_test() if args.self_test else None
    if controls is not None:
        print(f'PASS: {controls} in-memory positive/negative controls')
    report = scan(args.repo.resolve(), allow_retained_layouts=args.allow_retained_layouts)
    report['self_test_controls'] = controls
    if args.report:
        output = args.report if args.report.is_absolute() else args.repo / args.report
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(report, indent=2, ensure_ascii=False) + '\n')
    print('Mode:', report['mode'])
    print(f"{report['status']}: {report['production_source_files']} production source files across {len(report['production_source_sets'])} source sets; {report['retained_layout_resources']} retained XML layouts; {len(report['errors'])} violations")
    for error in report['errors']:
        print(f"  {error['path']}:{error['line']}: {error['rule']}: {error['detail']}")
    print('Static source evidence only; no Android build, runtime or visual parity result.')
    return 0 if report['status'] == 'PASS' else 1


if __name__ == '__main__':
    sys.exit(main())
