#!/usr/bin/env python3
"""Fresh source-only purchase parity guard. No app, billing, account or network execution."""
import argparse,hashlib,json,re,subprocess,xml.etree.ElementTree as ET
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
P='app/src/main/java/com/celzero/bravedns/'
def body(text,name,occurrence=0):
 matches=list(re.finditer(r'\bfun\s+'+re.escape(name)+r'\s*\(',text))
 if len(matches)<=occurrence:raise ValueError('missing '+name)
 start=text.find('{',matches[occurrence].end());i=start;depth=0;quote=None;comment=None
 while i<len(text):
  c=text[i];pair=text[i:i+2]
  if comment=='line':
   if c=='\n':comment=None
  elif comment=='block':
   if pair=='*/':comment=None;i+=1
  elif quote:
   if c=='\\':i+=1
   elif c==quote:quote=None
  elif pair=='//':comment='line';i+=1
  elif pair=='/*':comment='block';i+=1
  elif c in '\"\'':quote=c
  elif c=='{':depth+=1
  elif c=='}':
   depth-=1
   if depth==0:return text[start:i+1]
  i+=1
 raise ValueError('unterminated '+name)
def original(path):return subprocess.check_output(['git','show',BASE+':'+path],cwd=ROOT,text=True)
EXACT={
'ui/activity/CheckoutActivity.kt':['init','handlePaymentStatusUi','observePaymentStatus','handleKeys','setupClickListeners','setSpannablePricing','generateRandomHexToken','io','uiCtx'],
'ui/activity/PurchaseHistoryActivity.kt':['setDolphinSignature','onSaveInstanceState','observeTotalCount','loadHeroSubtitle','buildHeroSubtitle','onPause','onResume','uiCtx','io'],
'ui/activity/ServerOrderHistoryActivity.kt':['setDolphinSignature','setupClickListeners','openBillingHistory','showShimmer','loadHeroSubtitle','buildHeroSubtitle','heroIdentityLine','onPause','onResume','uiCtx','io'],
'ui/fragment/RethinkPlusManagePurchaseFragment.kt':['onViewCreated','initView','onResume','loadSubscriptionDetails','populateView','formatToken','heroIdentityLine','gateActionRows','gateManageOnPlayRow','setRowAvailability','setupClickListeners','copyWhoToClipboard','showCancelOrRevokeButton','canRevoke','observeOperationState','hideProgressOverlay','observeAckFailureState','updateAckFailureBanner','observeSubscriptionState','setupServerErrorObserver','showDeviceNotRegisteredBottomSheet','showDeviceAuthErrorBottomSheet','showConflictBottomSheet','managePlayStoreSubs','openServerOrderHistory','isInAppProduct'],
'ui/bottomsheet/DeviceAuthErrorBottomSheet.kt':['newInstance','onCreate','onViewCreated','maskCid','openCustomerSupport','dismiss','dismissAllowingStateLoss'],
'ui/bottomsheet/DeviceNotRegisteredBottomSheet.kt':['newInstance','onCreate','onViewCreated','maskCid','openEmailClient','dismiss','dismissAllowingStateLoss'],
'ui/bottomsheet/PurchaseConflictBottomSheet.kt':['newInstance','onCreate','onViewCreated','dismiss','dismissAllowingStateLoss','titleFor','descriptionFor'],
'ui/bottomsheet/PurchaseProcessingBottomSheet.kt':['newInstance','onCreate','onViewCreated','updateState','dismiss','dismissAllowingStateLoss'],
'ui/bottomsheet/EntitlementDetailBottomSheet.kt':['onViewCreated','dismiss','dismissAllowingStateLoss','observeEntitlementState','handleResetResult','valuesEqual','newInstance'],
'ui/dialog/SubscriptionAnimDialog.kt':['newInstance','festive'],
'adapter/ServerOrderHistoryAdapter.kt':['bind','bindTypeBadge','bindStatusChip','statusStyle','bindProductInfo','formatProductName','bindDates','bindOrderId','bindTestBadge'],
'adapter/PurchaseHistoryAdapter.kt':['stateStringRes','friendlyStateName'],
}
SUBS=['onViewCreated','onResume','onPause','onDestroyView','setupUI','applyButtonTheme','setupProductTypeToggle','setupClickListeners','openHelpAndSupport','setupObservers','setupIab','handleUiState','showLoading','showConnectionInfo','showProcessing','showPendingPurchase','showPendingTimeout','showNoInternet','showServerAckPending','showSuccess','showError','showAcknowledgementFailed','showTransactionError','hideAllContainers','navigateToDashboard','navigateBackToDashboard','initializeBilling','reconnectBilling','purchaseSubscription','showProcessingBottomSheet','dismissProcessingBottomSheet','startProcessingTimeout','cancelProcessingTimeout','friendlyBillingMessage','isOnline','restorePurchases','onConnectionResult','purchasesResult','productResult','onSubscriptionSelected','updateCancelPolicyText']
ADAPTED={
('ui/bottomsheet/PurchaseConflictBottomSheet.kt','initiateRefund'):[('binding.btnRefund.text','ui.refundLabel')],
('ui/bottomsheet/PurchaseConflictBottomSheet.kt','setRefundInFlight'):[('binding.progressRefund.isVisible','ui.inFlight'),('binding.btnRefund.isEnabled','ui.refundEnabled'),('binding.btnContactSupport.isEnabled','ui.supportEnabled'),('binding.btnDismiss.isEnabled','ui.dismissEnabled')],
('ui/fragment/RethinkPlusManagePurchaseFragment.kt','setDolphinSignature'):[('        b.dolphinSignature.revealAtScrollEndOf(b.managePurchaseScroll)\n','')],
('ui/bottomsheet/EntitlementDetailBottomSheet.kt','observeResetState'):[('b.btnRestore.isEnabled','ui.restoreEnabled'),('b.btnRestore.alpha','ui.restoreAlpha'),('b.btnRestore.text','ui.restoreLabel')],
('ui/bottomsheet/EntitlementDetailBottomSheet.kt','initView'):[('        b.btnDismiss.setOnClickListener { dismiss() }\n',''),('        b.btnRestore.setOnClickListener { viewModel.reset() }\n','')],
('ui/bottomsheet/EntitlementDetailBottomSheet.kt','displayEntitlement'):[('LayoutEntitlementRowBinding','EntitlementRowState'),('b.row','ui.row'),('b.restoreCv.visibility = if (canRestore) View.VISIBLE else View.GONE','ui.restoreVisible = canRestore')],
('adapter/PurchaseHistoryAdapter.kt','bind'):[('bindingAdapterPosition == itemCount - 1','isLast')],
('ui/activity/PurchaseHistoryActivity.kt','observeHistory'):[('                        adapter.stateRestorationPolicy =\n                            RecyclerView.Adapter.StateRestorationPolicy.ALLOW\n',''),('                        b.rvHistory.alpha = 0f\n                        b.rvHistory.animate().alpha(1f).setDuration(300).start()','                        b.listEpoch++')],
('ui/activity/ServerOrderHistoryActivity.kt','observeUiState'):[('                            b.rvOrders.alpha = 0f\n                            b.rvOrders.animate().alpha(1f).setDuration(280).start()','                            b.listEpoch++')],
}
PROCESSING_PROPERTIES={'progressIndicator.isVisible':'progress','statusIcon.isVisible':'iconVisible','titleText.text':'title','messageText.text':'message','actionButton.isVisible':'primaryVisible','secondaryButton.isVisible':'secondaryVisible','actionButton.text':'primaryText','secondaryButton.text':'secondaryText'}
def check():
 errors=[];exact=[];adapted=[]
 def record(path,name,value,target):target.append({'file':path,'method':name,'sha256':hashlib.sha256(value.encode()).hexdigest()})
 def compare(path,name,changes=(),occurrence=0):
  try:
   old=body(original(path),name,occurrence);new=body((ROOT/path).read_text(),name,occurrence)
   for a,b in changes:
    if a not in old:raise ValueError('missing approved substitution '+a)
    old=old.replace(a,b)
   if old!=new:errors.append(path+': '+name+' changed beyond approved presentation substitution')
   else:record(path,name,new,adapted if changes else exact)
  except Exception as e:errors.append(path+': '+str(e))
 for relative,names in EXACT.items():
  for name in names:compare(P+relative,name)
 for (relative,name),changes in ADAPTED.items():compare(P+relative,name,changes)
 for flavor in ('play','website'):
  prefix=f'app/src/{flavor}/java/com/celzero/bravedns/'
  for name in SUBS:compare(prefix+'ui/fragment/RethinkPlusFragment.kt',name)
  compare(prefix+'ui/fragment/RethinkPlusFragment.kt','showReady',[('        b.ctaContainer.doOnNextLayout { syncScrollBottomPaddingWithCta() }\n',''),('            b.subscriptionPlans.adapter = adapter\n','')])
  for name in ['getInAppDurationMonths','formatDurationLabel','calculateSavings','getBillingText']:
   path=prefix+'adapter/GooglePlaySubsAdapter.kt';old=body(original(path),name);new=body((ROOT/path).read_text(),name)
   old='\n'.join(line[4:] if line.startswith('    ') else line for line in old.splitlines())
   if old!=new:errors.append(path+': '+name+' changed')
   else:record(path,name,new,adapted)
 path=P+'ui/bottomsheet/PurchaseProcessingBottomSheet.kt';oldsource=original(path);newsource=(ROOT/path).read_text()
 for name in ['showProcessing','showPendingVerification','showSuccess','showError','showServerAckPending','showPendingTimeout','showPaymentDeclined']:
  old=body(oldsource,name)
  for a,b in PROCESSING_PROPERTIES.items():old=old.replace('binding.'+a,'ui.'+b)
  old=old.replace('binding.actionButton.setOnClickListener {','primaryAction = {').replace('        binding.secondaryButton.setOnClickListener {\n            dismiss()\n        }','')
  old=re.sub(r'binding.statusIcon.setImageResource\(([^\n]*)\)',r'ui.icon = \1',old)
  if old!=body(newsource,name):errors.append(path+': '+name+' presentation changed')
  else:record(path,name,body(newsource,name),adapted)
 for occurrence in (0,1):compare(P+'ui/fragment/RethinkPlusManagePurchaseFragment.kt','resolvePlanName',occurrence=occurrence)
 old=original(P+'ui/custom/DolphinOceanView.kt');new=(ROOT/(P+'ui/compose/purchase/DolphinOceanRenderer.kt')).read_text()
 constants=old[old.index('    // Trajectory phases'):old.index('    private val density')]
 if constants not in new:errors.append('Dolphin geometry constants, paths or particle pools changed')
 olddraw=body(old,'onDraw').replace('        super.onDraw(canvas)\n','').replace('        postInvalidateOnAnimation()\n','')
 if olddraw!=body(new,'draw'):errors.append('Dolphin frame body changed beyond View scheduling')
 for name in re.findall(r'private fun (\w+)\(',old):
  if name=='ensureRunning':continue
  if body(old,name)!=body(new,name):errors.append('Dolphin drawing/math changed: '+name)
  else:record(P+'ui/compose/purchase/DolphinOceanRenderer.kt',name,body(new,name),exact)
 old=original(P+'ui/custom/MoneyBackBadgeView.kt');new=(ROOT/(P+'ui/compose/purchase/MoneyBackBadgeRenderer.kt')).read_text()
 for name in ['drawSeal','drawCurvedText','drawCenterText']:
  if body(old,name)!=body(new,name):errors.append('Money-back badge drawing changed: '+name)
  else:record(P+'ui/compose/purchase/MoneyBackBadgeRenderer.kt',name,body(new,name),exact)
 for relative in EXACT:
  path=ROOT/(P+relative)
  if re.search(r'AndroidView\(|Binding\.inflate|viewBinding\(',path.read_text()):errors.append('Legacy active renderer: '+relative)
 if (ROOT/'app/src/main/res/layout/dialog_subscription_anim.xml').exists():errors.append('Obsolete Konfetti XML remains')
 # Full legacy privacy notice must match, with only Android's escaped newline interpretation.
 xml=ET.fromstring(original('app/src/main/res/layout/activity_checkout_proxy.xml'))
 notices=[v.get('{http://schemas.android.com/apk/res/android}text','') for v in xml.iter()]
 notice=next(v for v in notices if v.startswith('Rethink DNS + Firewall are anti-surveillance'))
 expected=notice.replace('\\n','\n')
 actual=re.search(r'val termsCopy = """([\s\S]*?)"""',(ROOT/(P+'ui/compose/purchase/CheckoutContent.kt')).read_text()).group(1)
 if expected!=actual:errors.append('Checkout privacy notice altered')
 return {'status':'FAIL' if errors else 'PASS','baseline':BASE,'reconstruction':'Freshly reimplemented and rechecked after lost-workspace restart; no prior test result reused','exact_body_count':len(exact),'narrowly_adapted_body_count':len(adapted),'privacy_notice_sha256':hashlib.sha256(actual.encode()).hexdigest(),'errors':errors,'exact_bodies':exact,'adapted_bodies':adapted,'limitation':'Source equality only; not Android compilation, Compose, lifecycle, billing execution, device or visual parity.'}
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--self-test',action='store_true');p.add_argument('--report');a=p.parse_args()
 if a.self_test:
  sample='fun f() { val label = "space retained }"; /* } */ call(1) }'
  for altered in [sample.replace('call(1)','call(2)'),sample.replace('space retained','spaceretained'),sample.replace('call(1)','call(1); call(1)')]:assert body(sample,'f')!=body(altered,'f')
  print('PASS: changed argument, string and duplicate-callback negative controls')
 result=check()
 if a.report:Path(a.report).write_text(json.dumps(result,indent=2)+'\n')
 print(f"{result['status']}: {result['exact_body_count']} exact and {result['narrowly_adapted_body_count']} narrowly adapted purchase/graphics method bodies")
 for e in result['errors']:print(e)
 raise SystemExit(bool(result['errors']))
