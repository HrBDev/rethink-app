#!/usr/bin/env python3
"""Fresh utility source guard against the original commit. Does not execute app code."""
import argparse, hashlib, importlib.util, json, re, subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
P='app/src/main/java/com/celzero/bravedns/'
spec=importlib.util.spec_from_file_location('purchase_guard',Path(__file__).with_name('check-purchase-controllers.py'))
mod=importlib.util.module_from_spec(spec);spec.loader.exec_module(mod)
body=mod.body
EXACT={
'ui/activity/AppLockActivity.kt':['onNewIntent','showBiometricPrompt','startHomeActivity','isBiometricEnabled','isAppRunningOnTv'],
'ui/activity/WelcomeActivity.kt':['launchHomeScreen'],
'ui/activity/PauseActivity.kt':['openHomeScreenAndFinishIfNeeded','initView','observeAppState','observeTimer','initClickListeners','decreaseTimer','increaseTimer','openHomeScreenAndFinish','handleLongPress'],
'ui/activity/BubbleActivity.kt':['onNewIntent','onResume','onStop','onDestroy','startAllowedCollector','startBlockedCollector','allowApp','removeAllowedApp','setupLoadStateListeners','showVpnOffState','showContentState','stopCollectors','io','uiCtx'],
'ui/activity/TcpProxyMainActivity.kt':['init','displayTcpProxyStatus','showTcpErrorLayout','observeTcpProxyApps','setupClickListeners','openAppsDialog','showConfigCreationError','enableTcpProxy','uiCtx','io'],
'ui/activity/PingTestActivity.kt':['initView','setupClickListeners','showReadyState','showTestingState','showSuccessState','showPartialState','showFailureState','showNoProxyState','setInputEnabled','recordHistory','stopDolphinSwim','animateSuccess','animateFailure','onDestroy','uiCtx','io'],
'ui/activity/CustomerSupportActivity.kt':['prefillDescriptionFromExtras','updateHeroSubtitle','heroIdentityLine','loadSubscriptionSummary','setupSendButton','selectedCategory','collectAndSend','buildDiagnosticText','buildEmailBody','prepareWirelogAttachment','readTailBytes','buildSupportZip','zipBytes','trimBugZipIfNeeded','writeDiagFile','getFileUri','launchEmailIntent','buildSubject','redactId','redactDid','formatTs'],
'ui/dialog/NetworkReachabilityDialog.kt':['initViews','setProtocolsUi','setupListeners','updateAutoModeUi','updateManualModeUi','resetToDefaults','testConnections','probeIpOrUrl','updateStatusIcons','getDrawableForProbeResult','setAllStatusIconsVisibility','setAllProgressBarsVisibility','setButtonsEnabled','saveIps','io','uiCtx','isValidIp','isValidUrl','selectToggleBtnUi','unselectToggleBtnUi'],
'sponsor/ui/SponsorFragment.kt':['onViewCreated','configurePaymentOptions','observeUiState','observeSponsorState','updateUi','showSuccessFragment'],
'sponsor/ui/SponsorSuccessFragment.kt':['onViewCreated'],
}
ADAPTED={
('ui/activity/AppLockActivity.kt','onCreate'):[('        super.onCreate(savedInstanceState)','        super.onCreate(savedInstanceState)\n        setContent { RethinkTheme { AppLockScreen() } }')],
('ui/activity/PauseActivity.kt','onCreate'):[('        openHomeScreenAndFinishIfNeeded()','        openHomeScreenAndFinishIfNeeded()\n        setContent { RethinkTheme { PauseScreen(b) } }')],
('ui/activity/BubbleActivity.kt','onCreate'):[('        super.onCreate(savedInstanceState)','        super.onCreate(savedInstanceState)\n        setContent { RethinkTheme { BubbleScreen(b) } }')],
('ui/activity/BubbleActivity.kt','setupQuickFixes'):[('b.bubbleQuickFixesContainer.removeAllViews()','b.quickFixes.clear()')],
('ui/activity/TcpProxyMainActivity.kt','onCreate'):[('        setupClickListeners()','        setupClickListeners()\n        setContent { RethinkTheme { TcpScreen(b) } }')],
('ui/activity/PingTestActivity.kt','onCreate'):[('        setupHistory()','        setupHistory()\n        restoreUtilityControls(savedInstanceState, b.savedControls)\n        setContent { RethinkTheme { PingScreen(b, historyAdapter) } }')],
('ui/activity/PingTestActivity.kt','setupHistory'):[('        b.historyRecycler.layoutManager = LinearLayoutManager(this)\n        b.historyRecycler.adapter = historyAdapter\n        b.historyRecycler.addItemDecoration(historyDividerDecoration())\n','')],
('ui/activity/PingTestActivity.kt','setDolphinSignature'):[('        b.dolphinSignature.revealAtScrollEndOf(b.historyScrollView)\n','')],
('ui/activity/PingTestActivity.kt','performTest'):[('        b.reachInput.clearFocus()\n        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager\n        imm.hideSoftInputFromWindow(b.reachInput.windowToken, 0)','        b.keyboardRequest++')],
('ui/activity/PingTestActivity.kt','animateDolphinSwim'):[('View.TRANSLATION_Y','"translationY"'),('View.ROTATION','"rotation"')],
('ui/activity/CustomerSupportActivity.kt','onCreate'):[('        applyScrollPadding()','        restoreUtilityControls(savedInstanceState, b.savedControls)\n        setContent { RethinkTheme { SupportScreen(b) } }')],
('ui/activity/CustomerSupportActivity.kt','setLoading'):[('        b.nestedScroll.post { updateScrollPadding() }','')],
('ui/dialog/NetworkReachabilityDialog.kt','onCreate'):[('        binding = DialogInputIpsBinding.inflate(layoutInflater)\n        setContentView(binding.root)','        setWgContent(activity) { ReachabilityScreen(binding) }')],
}
def check():
 errors=[];records=[]
 for rel,names in EXACT.items():
  old=subprocess.check_output(['git','show',BASE+':'+P+rel],cwd=ROOT,text=True);new=(ROOT/P/rel).read_text()
  for name in names+[n for f,n in ADAPTED if f==rel]:
   try:
    before=body(old,name);after=body(new,name)
    for a,b in ADAPTED.get((rel,name),[]):
     if a not in before:raise ValueError('expected substitution absent: '+a)
     before=before.replace(a,b)
    if before != after:errors.append(rel+': '+name+' differs beyond approved renderer substitutions')
    else:records.append({'file':P+rel,'method':name,'kind':'adapted' if (rel,name) in ADAPTED else 'exact','sha256':hashlib.sha256(after.encode()).hexdigest()})
   except ValueError as e:errors.append(rel+': '+str(e))
 protected=['util/BubbleHelper.kt','service/TcpProxyHelper.kt','rpnproxy/RpnProxyManager.kt','viewmodel/AllowedAppsBubbleViewModel.kt','viewmodel/BlockedAppsBubbleViewModel.kt','sponsor/viewmodel/SponsorViewModel.kt','service/PauseTimer.kt']
 for rel in protected:
  if subprocess.check_output(['git','show',BASE+':'+P+rel],cwd=ROOT)!=(ROOT/P/rel).read_bytes():errors.append('Protected dependency changed: '+rel)
 # Native utility renderers may draw Android Drawable artwork, but may not host legacy Views.
 native=list((ROOT/P/'ui/compose/utilities').glob('*.kt'))+[ROOT/P/'adapter'/n for n in ['BubbleAllowedAppsAdapter.kt','BubbleBlockedAppsAdapter.kt','PingTestHistoryAdapter.kt']]
 for file in native:
  code=file.read_text()
  for forbidden in ['AndroidView(', 'AndroidViewBinding(', 'ViewBinding', 'androidx.recyclerview.widget.RecyclerView', 'LayoutInflater', 'setContentView(']:
   if forbidden in code:errors.append('Legacy rendering boundary: '+str(file.relative_to(ROOT))+' '+forbidden)
 for file in (ROOT/'app/src/test/java/com/celzero/bravedns/ui/compose/utilities').glob('*.kt'):
  code=file.read_text()
  if not re.search(r'application\s*=\s*Application::class',code):errors.append('Missing plain Application isolation: '+str(file))
  if 'createComposeRule' in code and '@GraphicsMode(GraphicsMode.Mode.NATIVE)' not in code:errors.append('Missing native graphics fixture: '+str(file))
 # The Alerts adapter is wholly inactive in the baseline. Do not turn it into an action source.
 rel='adapter/AlertAdapter.kt'
 if subprocess.check_output(['git','show',BASE+':'+P+rel],cwd=ROOT)!=(ROOT/P/rel).read_bytes():errors.append('Disabled AlertAdapter changed')
 return {'status':'FAIL' if errors else 'PASS','baseline':BASE,'reconstruction':'Fresh source after workspace loss; no old validation reused','verified_methods':len(records),'protected_dependencies':len(protected),'methods':records,'errors':errors,'limitation':'Source preservation only, not compilation, runtime, visual or service parity.'}
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--self-test',action='store_true');p.add_argument('--report');a=p.parse_args()
 if a.self_test:
  source='fun f() { say("two words"); allow(1,true); a.refresh(); b.refresh() }'
  for mutation in [source.replace('two words','twowords'),source.replace('allow(1,true)','allow(1,false)'),source.replace('a.refresh(); b.refresh()','b.refresh(); a.refresh()'),source.replace('allow(1,true)','allow(1,true); allow(1,true)')]:assert body(source,'f')!=body(mutation,'f')
  print('PASS: changed literal, argument, ordering and duplicate callback negative controls')
 r=check()
 if a.report:Path(a.report).write_text(json.dumps(r,indent=2)+'\n')
 print(f"{r['status']}: {r['verified_methods']} exact/narrowly adapted utility controller bodies; {r['protected_dependencies']} protected dependencies")
 for e in r['errors']:print(e)
 raise SystemExit(bool(r['errors']))
