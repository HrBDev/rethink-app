/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.purchase
import android.app.Application
import android.view.ContextThemeWrapper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
/** Pure renderer tests; no production Activity, Fragment, Application, ViewModel, billing,
 * identity store, tunnel, network, worker or repository is constructed. Exact call lists are
 * strict synthetic recorders. CheckoutActivity is deliberately never instantiated. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PurchaseContentTest {
    @get:Rule val compose = createComposeRule()
    @Test fun processingRecompositionIsSilentAndTapsDispatchOnceEach() {
        val state = ProcessingStateUi().apply { progress = false; title = "Pending"; message = "Synthetic timeout"; primaryVisible = true; primaryText = "Check status"; secondaryVisible = true; secondaryText = "Close" }
        val calls = mutableListOf<String>()
        compose.setContent { TestTheme { ProcessingContent(state, { calls += "check" }, { calls += "dismiss" }) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()); state.message = "New synthetic timeout" }
        compose.onNodeWithTag("purchase_primary").performScrollTo().performClick()
        compose.onNodeWithTag("purchase_primary").performClick()
        compose.onNodeWithTag("purchase_secondary").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("check", "check", "dismiss"), calls) }
    }
    @Test fun hiddenProcessingActionsCannotRun() {
        val state = ProcessingStateUi().apply { title = "Processing"; message = "Synthetic" }
        compose.setContent { TestTheme { ProcessingContent(state, { error("Unexpected primary") }, { error("Unexpected secondary") }) } }
        compose.onNodeWithTag("purchase_primary").assertDoesNotExist()
        compose.onNodeWithTag("purchase_secondary").assertDoesNotExist()
        compose.onNodeWithTag("purchase_progress").assertExists()
    }
    @Test fun refundFlightDisablesActionsAndRetryDispatchesOnce() {
        val calls = mutableListOf<String>()
        val state = ConflictState().apply { title = "Synthetic conflict"; refundVisible = true; refundLabel = "Refund"; inFlight = true; refundEnabled = false; supportEnabled = false; dismissEnabled = false }
        compose.setContent { TestTheme { ConflictContent(state, { calls += "refund" }, { calls += "support" }, { calls += "dismiss" }) } }
        compose.onNodeWithTag("conflict_refund").assertIsNotEnabled()
        compose.onNodeWithTag("conflict_dismiss").assertIsNotEnabled()
        compose.runOnIdle { assertTrue(calls.isEmpty()); state.inFlight = false; state.refundEnabled = true; state.dismissEnabled = true; state.supportEnabled = true; state.refundLabel = "Retry" }
        compose.onNodeWithTag("conflict_refund").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("refund"), calls) }
    }
    @Test fun entitlementResetIsExplicitAndDisabledWhileRunning() {
        val calls = mutableListOf<String>()
        val state = EntitlementUiState().apply { restoreVisible = true; restoreLabel = "Restore" }
        compose.setContent { TestTheme { EntitlementContent(state, { calls += "restore" }, { calls += "dismiss" }) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("entitlement_restore").performScrollTo().performClick()
        compose.runOnIdle { state.restoreEnabled = false; state.restoreLabel = "Restoring" }
        compose.onNodeWithTag("entitlement_restore").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(listOf("restore"), calls) }
    }
    @Test fun transientConfirmationCancelIsSilentAndConfirmRunsOnce() {
        val calls = mutableListOf<Boolean>()
        val state = ManagePurchaseState().apply { confirmCancel = true }
        compose.setContent { TestTheme { ManagePurchaseContent(state) { calls += it } } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("manage_confirm_cancel").performClick()
        compose.onNodeWithTag("manage_confirm").assertDoesNotExist()
        compose.runOnIdle { assertTrue(calls.isEmpty()); state.confirmCancel = false }
        compose.onNodeWithTag("manage_confirm").performClick()
        compose.runOnIdle { assertEquals(listOf(false), calls); assertNull(state.confirmCancel) }
    }
    @Test fun planSelectionNeverPurchases() {
        val calls = mutableListOf<String>()
        var selected by mutableStateOf(false)
        val plan = PurchasePlan("synthetic", "Two years", "$12", "One-time", "Save 20%")
        compose.setContent { TestTheme { PurchasePlanCard(plan, selected, { calls += "select"; selected = true }) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("purchase_plan_synthetic").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(listOf("select"), calls) }
    }
    @Test fun checkoutRendererCannotGenerateKeysAndRequiresExplicitPaymentTap() {
        val calls = mutableListOf<String>()
        val state = CheckoutScreenState().apply {
            paymentAwaitingContainer.isVisible = false; paymentSuccessContainer.isVisible = false; paymentFailedContainer.isVisible = false
            plan1MonthButton.text = "Synthetic one month"; plan3MonthsButton.text = "Synthetic three months"; plan6MonthsButton.text = "Synthetic six months"
            paymentButton.setOnClickListener { calls += "payment" }; restoreButton.setOnClickListener { calls += "restore" }
        }
        compose.setContent { TestTheme { CheckoutContent(state) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithText("Synthetic one month").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("checkout_payment").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("payment"), calls) }
    }
    @Test fun largeRtlErrorStillExposesSupport() {
        val calls = mutableListOf<String>()
        val state = DeviceErrorState().apply { accountId = "synthetic-account"; deviceId = "test" }
        compose.setContent { TestTheme {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl, LocalDensity provides Density(density.density, 2f)) {
                DeviceErrorContent(state, true, { calls += "support" }, { calls += "dismiss" })
            }
        } }
        compose.onNodeWithTag("device_support").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("support"), calls) }
    }
    @Test fun pagingOnlyUsesSuppliedRecordsAndPreservesRecordKeys() {
        lateinit var adapter: com.celzero.bravedns.adapter.PurchaseHistoryAdapter
        lateinit var lifecycle: androidx.lifecycle.Lifecycle
        val state = HistoryScreenState().apply { shimmer.isVisible = false; rvHistory.isVisible = true }
        compose.setContent { TestTheme {
            val context = LocalContext.current
            adapter = remember(context) { com.celzero.bravedns.adapter.PurchaseHistoryAdapter(context) }
            lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
            HistoryScreen(state, false, adapter.itemCount, { adapter.key(it) }, {}, {}, { adapter.Row(it) })
        } }
        val first = com.celzero.bravedns.database.SubscriptionStateHistory(1, 101, 0, 1, 1, "Synthetic first")
        val second = com.celzero.bravedns.database.SubscriptionStateHistory(2, 101, 1, 2, 2, "Synthetic second")
        compose.runOnIdle { adapter.submitData(lifecycle, androidx.paging.PagingData.from(listOf(first))) }
        compose.waitUntil { adapter.itemCount == 1 }
        compose.runOnIdle { assertEquals(1, adapter.key(0)); adapter.submitData(lifecycle, androidx.paging.PagingData.from(listOf(second, first))) }
        compose.waitUntil { adapter.itemCount == 2 }
        compose.runOnIdle { assertEquals(2, adapter.key(0)); assertEquals(1, adapter.key(1)) }
        compose.onNodeWithText("Synthetic second").assertExists()
    }
    @Test fun historyRetryIsExplicitAndCannotReadARepository() {
        val calls = mutableListOf<String>()
        val state = HistoryScreenState().apply { shimmer.isVisible = false; errorState.isVisible = true; tvErrorMessage.text = "Synthetic server failure"; btnRetry.setOnClickListener { calls += "reload" } }
        compose.setContent { TestTheme { HistoryScreen(state, true, 0, { error("Unexpected row key") }, {}, {}, { error("Unexpected row") }) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("history_retry").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("reload"), calls) }
    }
    @Composable private fun TestTheme(content: @Composable () -> Unit) {
        val context = LocalContext.current
        val wrapped = remember(context) { ContextThemeWrapper(context, R.style.AppThemeWhite) }
        CompositionLocalProvider(LocalContext provides wrapped) { RethinkTheme(content) }
    }
}
