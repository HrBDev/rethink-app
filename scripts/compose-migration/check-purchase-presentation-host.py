#!/usr/bin/env python3
"""Execute extracted UI display/gating bodies using strict value-only JVM stubs.
No Android, Compose, application startup, billing, identity, repository, worker or network.
Requires existing toolchains; never installs them. Not the Android/pinned app build.
"""
import argparse,importlib.util,os,subprocess,tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
p=argparse.ArgumentParser();p.add_argument('--kotlin-home',required=True,type=Path);p.add_argument('--java-home',required=True,type=Path);a=p.parse_args()
compiler=a.kotlin_home/'bin/kotlinc';java=a.java_home/'bin/java'
if not compiler.is_file() or not java.is_file():p.error('Existing Kotlin and full JDK required; nothing is installed')
spec=importlib.util.spec_from_file_location('purchase_guard',Path(__file__).with_name('check-purchase-controllers.py'));g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g)
P=ROOT/'app/src/main/java/com/celzero/bravedns'
source='''
private const val TAG = "test"
private const val LOG_TAG_UI = "test"
object Logger { fun e(tag: String, message: String) { error("Unexpected display error: $message") } }
object SubscriptionStatus { enum class SubscriptionState(val id: Int) { STATE_ACTIVE(1) } }
object SubscriptionStateMachineV2 {
 data class SubscriptionData(val subscriptionStatus: Status, val purchaseDetail: Detail?)
 data class Status(val purchaseTime: Long, val status: Int)
 data class Detail(val productId: String, val planId: String)
}
object InAppBillingHandler {
 const val ONE_TIME_PRODUCT_2YRS = "two"
 const val ONE_TIME_PRODUCT_5YRS = "five"
 const val ONE_TIME_PRODUCT_ID = "legacy"
 const val ONE_TIME_TEST_PRODUCT_ID = "test"
 const val SUBS_PRODUCT_YEARLY = "year"
 const val SUBS_PRODUCT_MONTHLY = "month"
 const val REVOKE_WINDOW_SUBS_MONTHLY_DAYS = 3
 const val REVOKE_WINDOW_SUBS_YEARLY_DAYS = 7
 const val REVOKE_WINDOW_ONE_TIME_2YRS_DAYS = 14
 const val REVOKE_WINDOW_ONE_TIME_5YRS_DAYS = 28
}
'''
for sig,rel,name in [
 ('fun maskCid(id: String): String','ui/bottomsheet/DeviceAuthErrorBottomSheet.kt','maskCid'),
 ('fun maskEntitlement(cid: String): String','ui/bottomsheet/DeviceNotRegisteredBottomSheet.kt','maskCid'),
 ('fun valuesEqual(a: String?, b: String?): Boolean','ui/bottomsheet/EntitlementDetailBottomSheet.kt','valuesEqual'),
 ('fun heroIdentityLine(token: String, accountId: String, deviceId: String, expiry: String): String','ui/fragment/RethinkPlusManagePurchaseFragment.kt','heroIdentityLine'),
 ('fun isInAppProduct(productId: String, planId: String): Boolean','ui/fragment/RethinkPlusManagePurchaseFragment.kt','isInAppProduct'),
 ('fun canRevoke(subscriptionData: SubscriptionStateMachineV2.SubscriptionData?): Boolean','ui/fragment/RethinkPlusManagePurchaseFragment.kt','canRevoke')]:source+='\n'+sig+' '+g.body((P/rel).read_text(),name)+'\n'
subs=ROOT/'app/src/play/java/com/celzero/bravedns/adapter/GooglePlaySubsAdapter.kt'
source+='\nfun calculateSavings(originalPrice: String, discountedPrice: String): Int '+g.body(subs.read_text(),'calculateSavings')+'\n'
source+='''
fun main() {
 var count = 0
 fun expect(value: Boolean) { count++; check(value) { "Purchase host assertion $count failed" } }
 for (size in 0..48) {
  val id = "a".repeat(size)
  val expected = if (size <= 16) id else id.take(12)
  expect(maskCid(id) == expected); expect(maskEntitlement(id) == expected)
 }
 for (left in listOf(null, "", " ", "\\n")) for (right in listOf(null, "", " ", "\\n")) expect(valuesEqual(left, right))
 for (left in listOf(null, "", " ")) { expect(!valuesEqual(left, "a")); expect(!valuesEqual("a", left)) }
 expect(valuesEqual("abc", "abc")); expect(!valuesEqual("abc", "ABC")); expect(!valuesEqual("abc", "abc "))
 expect(heroIdentityLine("", "", "", "") == "")
 expect(heroIdentityLine("abcdefghijklmnop", "1234567890123456", "devicelong", "abcd") == "abcdefghijkl · 123456789012 • devi · abcd")
 expect(heroIdentityLine("", "account", "", "") == "account")
 expect(heroIdentityLine("token", "", "device", "") == "token · devi")
 expect(calculateSavings("$10.00", "$8.00") == 20)
 expect(calculateSavings("unknown", "$8") == 0)
 expect(calculateSavings("$0", "$1") == 0)
 expect(calculateSavings("$10", "$20") == -100)
 expect(calculateSavings("$10", "free") == 0)
 expect(!canRevoke(null))
 val now = System.currentTimeMillis()
 fun sub(daysAgo: Long, product: String, status: Int = 1, plan: String = "") = SubscriptionStateMachineV2.SubscriptionData(
    SubscriptionStateMachineV2.Status(now - daysAgo * 86_400_000L, status), SubscriptionStateMachineV2.Detail(product, plan))
 for ((id, window) in listOf("month" to 3L, "year" to 7L, "two" to 14L, "five" to 28L, "legacy" to 14L, "unknown" to 3L)) {
   expect(canRevoke(sub(window - 1, id))); expect(!canRevoke(sub(window + 1, id))); expect(!canRevoke(sub(0, id, status = 99)))
 }
 expect(canRevoke(sub(20, "", plan = "five")))
 expect(!canRevoke(SubscriptionStateMachineV2.SubscriptionData(SubscriptionStateMachineV2.Status(0,1), null)))
 expect(!canRevoke(SubscriptionStateMachineV2.SubscriptionData(SubscriptionStateMachineV2.Status(-1,1), null)))
 expect(isInAppProduct("legacy", "")); expect(isInAppProduct("", "test")); expect(!isInAppProduct("unknown", "month"))
 println("PASS: $count extracted purchase display/refund-eligibility cases with value-only stubs")
}
'''
with tempfile.TemporaryDirectory(prefix='purchase-host-') as directory:
 d=Path(directory);(d/'PurchaseHost.kt').write_text(source);jar=d/'tests.jar';env=dict(os.environ,JAVA_HOME=str(a.java_home))
 subprocess.run([str(compiler),str(d/'PurchaseHost.kt'),'-include-runtime','-d',str(jar)],env=env,check=True)
 subprocess.run([str(java),'-jar',str(jar)],env=env,check=True)
print('LIMIT: fresh extracted functions/value-only stubs, not Android/pinned compilation, Compose, lifecycle, account, billing, identity, storage or network tests.')
