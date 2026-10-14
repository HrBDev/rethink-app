#!/usr/bin/env python3
"""Pinned-baseline source equality after explicit presentation-only substitutions.
No Android, services, network, production data, or Gradle is executed.
"""
from pathlib import Path
import re, subprocess, json, importlib.util, sys
ROOT = Path(__file__).resolve().parents[2]
BASELINE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
spec = importlib.util.spec_from_file_location('controller_lexer', Path(__file__).with_name('check-wireguard-controllers.py'))
g = importlib.util.module_from_spec(spec); spec.loader.exec_module(g)

def adapt_baseline(name, source):
 state, layout = {
  'MiscSettingsActivity': ('MiscSettingsState', 'activity_misc_settings'),
  'ProxySettingsActivity': ('ProxySettingsState', 'fragment_proxy_configure'),
  'TunnelSettingsActivity': ('TunnelSettingsState', 'activity_tunnel_settings'),
 }[name]
 s = source
 s=re.sub(r'import com.celzero.bravedns.databinding.\w+\n','',s)
 s=s.replace('import dev.androidbroadcast.vbpd.viewBinding\n','')
 s=s.replace('import com.google.android.material.dialog.MaterialAlertDialogBuilder','import com.celzero.bravedns.ui.compose.networksettings.NetworkAlertDialogBuilder as MaterialAlertDialogBuilder')
 s=s.replace('import android.widget.CompoundButton','import com.celzero.bravedns.ui.compose.wireguard.WgControlState as CompoundButton')
 s=s.replace('import android.widget.SeekBar','import com.celzero.bravedns.ui.compose.networksettings.NetworkSeekState as SeekBar')
 s=s.replace('package com.celzero.bravedns.ui.activity','package com.celzero.bravedns.ui.activity\n\nimport androidx.activity.compose.setContent\nimport com.celzero.bravedns.ui.compose.RethinkTheme\nimport com.celzero.bravedns.ui.compose.networksettings.*\nimport com.celzero.bravedns.ui.compose.wireguard.WgControlState')
 s=s.replace(f'BaseActivity(R.layout.{layout})','BaseActivity()')
 s=re.sub(r'private val b by viewBinding\(\w+::bind\)',f'private val b by lazy {{ {state}(this) }}',s)
 s=s.replace('super.onCreate(savedInstanceState)','super.onCreate(savedInstanceState)\n        setContent { RethinkTheme { NetworkSettingsScreen(b.layout'+(', b.refreshing.toSet()' if name.startswith('Proxy') else '')+') } }',1)
 s=s.replace('vararg views: View','vararg views: WgControlState')
 s=s.replace('SnackbarHelper.showStabilityProgram(b.root, persistentState)','SnackbarHelper.showStabilityProgram(window.decorView, persistentState)')
 if 'override fun onSaveInstanceState' in s:s=s.replace('super.onSaveInstanceState(outState)','super.onSaveInstanceState(outState)\n        b.saveChecks(outState)',1)
 else:
  at=s.index('    private fun ',s.index('class '+name))
  s=s[:at]+'''    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        b.saveChecks(outState)
    }

'''+s[at:]
 at=s.index('    private fun ',s.index('class '+name))
 s=s[:at]+'''    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        b.restoreChecks(savedInstanceState)
    }

'''+s[at:]
 if name.startswith('Misc'):
  s=s.replace('?.replaceFirstChar(Char::titlecase)?.replace("_", " ")','?.replaceFirstChar(Char::titlecase)?.replace("_", " ").orEmpty()')
  s=s.replace('val cb = MaterialCheckBox(this)','val cb = WgControlState(this, checkable = true)')
  start=s.index('        val density = resources.displayMetrics.density',s.index('private fun showGoLoggerDialog'))
  end=s.index('\n        alertBuilder.create()',start)
  s=s[:start]+'        alertBuilder.setContent { NetworkCheckRow(cb) }\n'+s[end:]
  start=s.index('          val editText = AppCompatEditText(context)',s.index('fun showAppTriggerPackageDialog'))
  end=s.index('          MaterialAlertDialogBuilder(context',start)
  s=s[:start]+'''          val editText = WgControlState(context).apply {
              hint = context.getString(R.string.adv_tasker_dialog_edit_hint)
              if (persistentState.appTriggerPackages.isNotEmpty()) {
                  setText(persistentState.appTriggerPackages)
              }
          }

'''+s[end:]
  s=s.replace('.setView(scrollView)','.setContent { NetworkTaskerContent(context, editText) }')
 if name.startswith('Proxy'):
  s=s.replace('private fun refresh(iv: AppCompatImageView)','private fun refresh(iv: WgControlState)')
  s=s.replace('        iv.animation = animation\n        iv.startAnimation(animation)','        b.startRefresh(iv)')
  s=s.replace('iv.clearAnimation()','b.stopRefresh(iv)')
  s=s.replace('val dialogBinding = DialogSetProxyBinding.inflate(layoutInflater)','val dialogBinding = ProxyEditorState(this)')
  s=s.replace('.setView(dialogBinding.root)','.setContent { NetworkContent(dialogBinding.layout, scrollContainer = false) }')
  for typ in ['TextView','EditText','LinearLayout','CheckBox']:
   s=s.replace(': '+typ+' =',': WgControlState =')
  s=s.replace(': Spinner =',': NetworkSpinnerState =')
  s=s.replace('        val proxySpinnerAdapter =\n            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, appNames)\n        appNameSpinner.adapter = proxySpinnerAdapter','        appNameSpinner.items = appNames')
 if name.startswith('Tunnel'):
  start=s.index('        val adapter = object : ArrayAdapter<NetworkPolicyOption>(')
  end=s.index('\n\n        val builder = ',start)
  s=s[:start]+'        val adapter = NetworkPolicyAdapter(options.map { it.title to it.description }) { currentSelection }'+s[end:]
  start=s.index('                val dialogView = LayoutInflater.from(this@TunnelSettingsActivity)')
  end=s.index('\n                subtitle.text',start)
  s=s[:start]+'''                val dialogView = LockdownDialogState(this@TunnelSettingsActivity)
                val items = mutableListOf<NetworkNode>()
                val subtitle = dialogView.lockdownCheckSubtitle
                val allOkText = dialogView.lockdownCheckAllOk
                val divider = dialogView.lockdownCheckDivider
                val disableRow = dialogView.lockdownCheckDisableRow
                val disableLabel = dialogView.lockdownCheckDisableLabel
                val disableDesc = dialogView.lockdownCheckDisableDesc
                val disableSwitch = dialogView.lockdownCheckDisableSwitch
'''+s[end:]
  start=s.index('                    val itemView = LayoutInflater.from(this@TunnelSettingsActivity)')
  end=s.index('\n                    label.text',start)
  s=s[:start]+'''                    val itemView = LockdownItemState(this@TunnelSettingsActivity)
                    val icon = itemView.lockdownCheckItemIcon
                    val label = itemView.lockdownCheckItemLabel
                    val desc = itemView.lockdownCheckItemDesc
                    val status = itemView.lockdownCheckItemStatus
'''+s[end:]
  s=s.replace('icon.imageTintList = ColorStateList.valueOf(redColor)','icon.textColor = redColor').replace('icon.imageTintList = ColorStateList.valueOf(greenColor)','icon.textColor = greenColor')
  s=s.replace('status.background = createChipBackground(redColor)','status.backgroundColor = redColor').replace('status.background = createChipBackground(greenColor)','status.backgroundColor = greenColor')
  s=s.replace('itemsContainer.addView(itemView)','items.add(itemView.layout)')
  s=s.replace('.setView(dialogView)','.setContent { NetworkContent(dialogView.layout, extraChildren = mapOf(dialogView.lockdownCheckItemsContainer to items), scrollContainer = false) }')
 return s

def main():
 results=[]; failures=[]
 for name in ['MiscSettingsActivity', 'ProxySettingsActivity', 'TunnelSettingsActivity']:
  path='app/src/main/java/com/celzero/bravedns/ui/activity/'+name+'.kt'
  baseline=subprocess.check_output(['git','show',BASELINE+':'+path],cwd=ROOT,text=True)
  original=g.bodies(baseline); expected=g.bodies(adapt_baseline(name, baseline)); current=g.bodies((ROOT/path).read_text())
  for method in original:
   if name == 'TunnelSettingsActivity' and method == 'getView': continue # Removed XML ArrayAdapter renderer only.
   before=expected.get(method); after=current.get(method)
   passed=before is not None and before==after
   results.append({'path':path,'method':method,'passed':passed,'baselineAdaptedSha256':g.digest(before or []),'currentSha256':g.digest(after or [])})
   if not passed:failures.append(path+'::'+method)
  for new_method in ['onRestoreInstanceState', 'onSaveInstanceState']:
   if current.get(new_method)!=expected.get(new_method):failures.append(path+'::'+new_method+' restoration differs')
  source=(ROOT/path).read_text()
  for forbidden in ['viewBinding(', 'Binding.inflate(', '.setView(', 'findViewById<', '.inflate(R.layout', 'AndroidView(']:
   if forbidden in source:failures.append(path+' forbidden legacy content: '+forbidden)
 g.self_test()
 report={'baseline':BASELINE,'kind':'all baseline braced method token equality after enumerated presentation-only substitutions','passed':not failures,'count':len(results),'methods':results,'excludedPresentation':{'TunnelSettingsActivity.kt':['getView: removed XML-only policy row inflation/binding']},'limitations':['Not Kotlin compilation, runtime, screenshots, Android lifecycle, service behavior, accessibility, or exhaustive rendering verification.','UI adapter equivalence is covered separately by isolated synthetic fixtures. No production application or services launched.']}
 (ROOT/'docs/compose-migration/network-settings-controller-source-report.json').write_text(json.dumps(report,indent=2)+'\n')
 if failures:print('FAIL\n'+'\n'.join(failures));return 1
 print('PASS',len(results),'pinned baseline method bodies; only the XML policy row renderer excluded. Not runtime or rendering validation.')
 return 0
if __name__=='__main__':sys.exit(main())
