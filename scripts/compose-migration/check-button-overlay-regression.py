#!/usr/bin/env python3
"""Closed source-contract and negative-control checks, not Kotlin/Android verification."""
from pathlib import Path
import argparse, json
ROOT=Path(__file__).resolve().parents[2]
CONTROL='app/src/main/java/com/celzero/bravedns/ui/compose/controls/OriginalControls.kt'
TEST='app/src/test/java/com/celzero/bravedns/ui/compose/controls/OriginalControlsTest.kt'
GENERATOR='docs/compose-migration/generate_original_control_styles.py'
STYLE='app/src/main/res/values/original_control_styles.xml'

def check(overrides=None):
 overrides=overrides or {}
 def read(p):return overrides.get(p,(ROOT/p).read_text())
 s=read(CONTROL);test=read(TEST);generator=read(GENERATOR)
 contracts=[
  ('component context precedes attribute reads',s.index('val context = originalControlContext(')<s.index('val base = context.obtainOrderedStyledAttributes')),
  ('material overlay precedes size then client overlay',s.index('ContextThemeWrapper(context, materialOverlay).also')<s.index('wrapped.theme.applyStyle(sizeOverlay, true)')<s.index('wrapped.theme.applyStyle(clientOverlay, true)')),
  ('size overlays apply to buttons', 'if (spec.kind == OriginalControlKind.Button) overlay(1) else 0' in s),
  ('framework theme does not inherit default component style','val androidOverlay = source.getResourceId(2, 0)' in s and 'val appOverlay = source.getResourceId(3, 0)' in s),
  ('Material namespace drives button fill','colors(if (chip) 28 else 19)' in s and 'else if (has(18)) 18 else 19' not in s),
  ('Material namespace drives overwritten button drawable tint','drawableTint = if (spec.kind == OriginalControlKind.Button) colors(19) else colors(18)' in s),
  ('selectors resolved in overlaid AppCompat context','AppCompatResources.getColorStateList(context, resource)' in s),
  ('drawables use AppCompat loader','AppCompatResources.getDrawable(context, resource)' in s),
  ('explicit clears preserved','if (explicitlyCleared(index)) return null' in s and "v in ('@null','@empty')" in generator),
  ('generator preserves user iconTint namespace correction',"if c['tag']=='MaterialButton' and nameattr=='android:iconTint': nameattr='iconTint'" in generator and '<item name="android:iconTint">' not in read(STYLE)),
  ('compact controls avoid minimum propagation','propagateMinConstraints = true' not in s and 'BoxWithConstraints(' in s),
  ('joined segments omit standalone layout minimum','interactive && !grouped && segmentCount <= 1' in s),
  ('dynamic icon tint retains source icon','val resolvedIconTint = iconTintOverride ?:' in s and 'Modifier.size(look.iconSize.dp)' in s),
  ('native widget is independent oracle','val native = MaterialButton(themed, attrs)' in test and 'color(native.backgroundTintList), color(actual.fill)' in test),
  ('alpha/disabled/checked states covered','ThemeOverlay_App_AlphaLight' in test and 'ThemeOverlay_App_AlphaDark' in test and '-android.R.attr.state_enabled' in test and 'android.R.attr.state_checked' in test),
  ('transparent and filled controls both covered','textAndDialogButtonsStayTransparentWhileFilledButtonsKeepTheirSourceFill' in test),
  ('compact group and callbacks covered','compactGroupedButtonsKeepSourceBoundsAndActions' in test and 'assertHeightIsEqualTo(32.dp)' in test and 'assertEquals(1, calls)' in test),
  ('plain test application','application = Application::class' in test),
  ('custom drawable owns stroke and ripple','ripple = if (overwrittenButton) null' in s and 'stroke = if (overwrittenButton) null' in s and 'interactive && look.ripple != null' in s),
  ('fixed icon visuals avoid text padding collapse','if (spec.iconOnly && spec.visualHeight > 0f) Modifier.height(spec.visualHeight.dp)' in s and 'if (spec.iconOnly) Modifier else Modifier.padding' in s and 'if (!spec.iconOnly) Spacer' in s),
  ('view-free production resolver', all(x not in s for x in ['AndroidView(', 'LayoutInflater', '.inflate(', 'MaterialButton(']))]
 for name,ok in contracts:assert ok,name
 return len(contracts)

def main():
 p=argparse.ArgumentParser();p.add_argument('--self-test',action='store_true');p.add_argument('--report',type=Path);a=p.parse_args();count=check();rejected=0
 if a.self_test:
  mutations=[(CONTROL,'ripple = if (overwrittenButton) null else colors(23)','ripple = colors(23)'),(CONTROL,'if (spec.iconOnly) Modifier else Modifier.padding','if (false) Modifier else Modifier.padding'),(CONTROL,'val context = originalControlContext(sourceContext, spec, defaultAttr, defaultStyle)','val context = sourceContext'),(CONTROL,'wrapped.theme.applyStyle(sizeOverlay, true)','Unit'),(CONTROL,'colors(if (chip) 28 else 19)','colors(if (chip) 28 else 18)'),(CONTROL,'AppCompatResources.getColorStateList(context, resource)','values.getColorStateList(index)'),(CONTROL,'interactive && !grouped && segmentCount <= 1','interactive'),(CONTROL,'val resolvedIconTint = iconTintOverride ?:','val resolvedIconTint ='),(GENERATOR,"if c['tag']=='MaterialButton' and nameattr=='android:iconTint': nameattr='iconTint'",'pass'),(TEST,'color(native.backgroundTintList), color(actual.fill)','color(actual.fill), color(actual.fill)'),(TEST,'application = Application::class','application = RethinkDnsApplication::class')]
  for path,old,new in mutations:
   text=(ROOT/path).read_text();assert old in text
   try:check({path:text.replace(old,new)})
   except (AssertionError, ValueError):rejected+=1
   else:raise AssertionError('mutation escaped: '+old)
 result={'passed':True,'source_only':True,'contracts':count,'negative_controls':rejected,'compile':'not_run','runtime':'not_run'}
 if a.report:a.report.write_text(json.dumps(result,indent=2)+'\n')
 print(json.dumps(result))
if __name__=='__main__':main()
