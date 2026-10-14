/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.utilities
import android.app.Application
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.*
import com.celzero.bravedns.R
import com.celzero.bravedns.data.AllowedAppInfo
import com.celzero.bravedns.data.BlockedAppInfo
import com.celzero.bravedns.adapter.PingTestHistoryAdapter
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Pure content and strict synthetic callbacks. No production Activity, Fragment or singleton. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UtilityContentTest {
    @get:Rule val compose = createComposeRule()
    @Test fun welcomeRapidNextUsesRequestedIndexBeforeAnimationSettles() {
        val state = WelcomeUiState(); var finishes = 0
        compose.mainClock.autoAdvance = false
        compose.setContent { TestTheme { WelcomeScreen(state) { finishes++ } } }
        compose.onNodeWithTag("welcome.next").performClick().performClick().performClick()
        compose.runOnIdle { assertEquals(3, state.currentItem); assertEquals(0, finishes) }
        compose.onNodeWithTag("welcome.next").performClick()
        compose.runOnIdle { assertEquals(1, finishes) }
    }
    @Test fun welcomeSkipCompletesExactlyOnce() {
        var finishes=0;compose.setContent { TestTheme { WelcomeScreen(remember { WelcomeUiState() }) { finishes++ } } }
        compose.runOnIdle { assertEquals(0,finishes) }
        compose.onNodeWithTag("welcome.skip").performClick()
        compose.runOnIdle { assertEquals(1,finishes) }
    }
    @Test fun bubbleRowActionsRetainTheExactSyntheticObjectAndTapCount() {
        val blocked=BlockedAppInfo("invalid.test.synthetic","Synthetic blocked",101,3,0)
        val allowed=AllowedAppInfo("invalid.test.synthetic","Synthetic allowed",202,0)
        val calls=mutableListOf<String>()
        compose.setContent { TestTheme { Column {
            BubbleBlockedRow(blocked,{assertSame(blocked,it);calls+="allow"},60_000)
            BubbleAllowedRow(allowed,{assertSame(allowed,it);calls+="remove"},60_000)
        } } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("bubble.allow.101").performClick().performClick()
        compose.onNodeWithTag("bubble.remove.202").performClick()
        compose.runOnIdle { assertEquals(listOf("allow","allow","remove"),calls) }
    }
    @Test fun tcpTrueFalseReentrantDispatchIsChangedOnly() {
        lateinit var state:TcpUiState;val calls=mutableListOf<Boolean>()
        compose.setContent { TestTheme {
            val context=LocalContext.current;state=remember { TcpUiState(context).apply { tcpProxySwitch.setOnCheckedChangeListener { row,value -> calls+=value;row.isChecked=false } } }
            TcpScreen(state)
        } }
        compose.onNodeWithTag("activity_tcp_proxy.tcp_proxy_switch").performClick()
        compose.runOnIdle { assertEquals(listOf(true),calls);assertFalse(state.tcpProxySwitch.isChecked) }
        compose.onNodeWithTag("activity_tcp_proxy.tcp_error_tv").assertDoesNotExist()
    }
    @Test fun supportCategoriesAreExclusiveAndCanBeClearedWithoutSending() {
        lateinit var state:SupportUiState;var sends=0
        compose.setContent { TestTheme {
            val context=LocalContext.current;state=remember { SupportUiState(context).apply { btnSendEmail.setOnClickListener { sends++ } } }
            SupportScreen(state)
        } }
        compose.onNodeWithTag("activity_customer_support.chip_payment").performScrollTo().performClick()
        compose.onNodeWithTag("activity_customer_support.chip_activation").performScrollTo().performClick()
        compose.runOnIdle { assertFalse(state.chipPayment.isChecked);assertTrue(state.chipActivation.isChecked);assertEquals(0,sends) }
        compose.onNodeWithTag("activity_customer_support.chip_activation").performClick()
        compose.runOnIdle { assertTrue(state.categories.none{it.isChecked});assertEquals(0,sends) }
    }
    @Test fun supportDirectHeaderDragCollapsesAndLargeRtlBottomActionIsReachable() {
        compose.setContent { TestTheme(largeRtl=true) {
            val context=LocalContext.current;val state=remember { SupportUiState(context).apply { headerTitle="Synthetic support" } };SupportScreen(state)
        } }
        val before=compose.onNodeWithTag("support.header").fetchSemanticsNode().boundsInRoot.height
        compose.onNodeWithTag("support.header").performTouchInput { swipeUp() }
        compose.waitForIdle()
        val after=compose.onNodeWithTag("support.header").fetchSemanticsNode().boundsInRoot.height
        assertTrue("Header must respond to direct drag",after < before)
        compose.onNodeWithTag("activity_customer_support.btn_send_email").assertIsDisplayed()
    }
    @Test fun disabledSupportButtonNeverDispatchesAndLoadingRemainsVisible() {
        var sends=0
        compose.setContent { TestTheme {
            val context=LocalContext.current;val state=remember {SupportUiState(context).apply {btnSendEmail.isEnabled=false;layoutLoading.isVisible=true;btnSendEmail.setOnClickListener{sends++}}};SupportScreen(state)
        } }
        compose.onNodeWithTag("activity_customer_support.btn_send_email").assertIsNotEnabled().performClick()
        compose.onNodeWithTag("support.loading").assertExists()
        compose.runOnIdle {assertEquals(0,sends)}
    }
    @Test fun inactivePingAllowsAutoActionWithoutCustomInput() {
        var runs=0
        compose.setContent {TestTheme {
            val context=LocalContext.current;val state=remember {PingUiState(context).apply {reachInput.isEnabled=false;reachInputLayout.isEnabled=false;rpnInactiveNote.isVisible=true;pingButton.setOnClickListener{runs++}}}
            Box(Modifier.height(600.dp)) {PingScreen(state,PingTestHistoryAdapter())}
        }}
        compose.onNodeWithTag("activity_ping_test.reach_input").assertIsNotEnabled()
        compose.onNodeWithTag("ping.history.list").performScrollToNode(hasTestTag("activity_ping_test.ping_button"))
        compose.onNodeWithTag("activity_ping_test.ping_button").performClick()
        compose.runOnIdle {assertEquals(1,runs)}
    }
    @Test fun sponsorChipDispatchIsSingleAndSameSelectedChipCanClear() {
        lateinit var state:SponsorUiStateModel;val calls=mutableListOf<Int>()
        compose.setContent {TestTheme {
            val context=LocalContext.current;state=remember {SponsorUiStateModel(context).apply {chip5.setOnClickListener{calls+=5};chip10.setOnClickListener{calls+=10}}};SponsorScreen(state)
        }}
        compose.onNodeWithTag("fragment_sponsor.chip_5").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(listOf(5),calls);assertTrue(state.chip5.isChecked)}
        compose.onNodeWithTag("fragment_sponsor.chip_5").performClick()
        compose.runOnIdle {assertEquals(listOf(5,5),calls);assertFalse(state.chip5.isChecked)}
    }
    @Test fun sponsorUnavailableBillingShowsOnlyStripeAction() {
        val calls=mutableListOf<String>()
        compose.setContent {TestTheme {
            val context=LocalContext.current;val state=remember {SponsorUiStateModel(context).apply {contributionCard.isVisible=false;supportButton.isVisible=false;sponsorPlayUnavailableText.isVisible=true;supportButton.setOnClickListener{error("Billing must be hidden")};payWithStripeLink.setOnClickListener{calls+="stripe"}}};SponsorScreen(state)
        }}
        compose.onNodeWithTag("fragment_sponsor.support_button").assertDoesNotExist()
        compose.onNodeWithTag("fragment_sponsor.contribution_seekbar").assertDoesNotExist()
        compose.onNodeWithTag("fragment_sponsor.pay_with_stripe_link").performClick()
        compose.runOnIdle {assertEquals(listOf("stripe"),calls)}
    }
    @Test fun sponsorSuccessOnlyCompletesWhenContinueIsTapped() {
        var finishes=0
        compose.setContent {TestTheme(largeRtl=true) {
            val context=LocalContext.current;val state=remember {SponsorSuccessUiState(context).apply {continueButton.setOnClickListener{finishes++}}};SponsorSuccessScreen(state)
        }}
        compose.runOnIdle {assertEquals(0,finishes)}
        compose.onNodeWithTag("fragment_sponsor_success.continue_button").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(1,finishes)}
    }
    @Test fun reachabilitySaveTestResetAndModesDispatchOnlyTheirOwnCallbacks() {
        val calls=mutableListOf<String>()
        compose.setContent {TestTheme {
            val context=LocalContext.current;val state=remember {ReachabilityUiState(context).apply {
                autoToggleBtn.setOnClickListener{calls+="auto"};manualToggleBtn.setOnClickListener{calls+="manual"};resetChip.setOnClickListener{calls+="reset"};testButton.setOnClickListener{calls+="test"};saveButton.setOnClickListener{calls+="save"}
            }};ReachabilityScreen(state)
        }}
        compose.runOnIdle {assertTrue(calls.isEmpty())}
        compose.onNodeWithTag("dialog_input_ips.manual_toggle_btn").performScrollTo().performClick()
        compose.onNodeWithTag("dialog_input_ips.reset_chip").performScrollTo().performClick()
        compose.onNodeWithTag("dialog_input_ips.test_button").performScrollTo().performClick()
        compose.onNodeWithTag("dialog_input_ips.save_button").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(listOf("manual","reset","test","save"),calls)}
    }
    @Test fun disabledReachabilityModeNeverDispatchesAndFieldsRetainEditedText() {
        lateinit var state:ReachabilityUiState;var modes=0
        compose.setContent {TestTheme {
            val context=LocalContext.current;state=remember {ReachabilityUiState(context).apply {
                manualToggleBtn.isEnabled=false;manualToggleBtn.setOnClickListener{modes++}
            }};ReachabilityScreen(state)
        }}
        compose.onNodeWithTag("dialog_input_ips.manual_toggle_btn").assertIsNotEnabled().performClick()
        compose.onNodeWithTag("dialog_input_ips.ipv4_address_1").performScrollTo().performTextReplacement("192.0.2.1")
        compose.runOnIdle {assertEquals(0,modes);assertEquals("192.0.2.1",state.ipv4Address1.text.toString())}
    }
    @Test fun pauseReleaseAndCancellationStopTheRepeatState() {
        val calls=mutableListOf<String>()
        compose.setContent {TestTheme {
            val context=LocalContext.current;val state=remember {PauseUiState(context).apply {
                pacPlusIv.setOnLongClickListener{calls+="long";false};pacPlusIv.setOnClickListener{calls+="click"};pacPlusIv.setOnTouchListener{_,event->calls+="touch:${event.action}";false}
            }};PauseScreen(state)
        }}
        compose.onNodeWithTag("activity_pause.pac_plus_iv").performScrollTo().performTouchInput {longClick()}
        compose.runOnIdle {assertEquals(listOf("long","touch:${MotionEvent.ACTION_UP}","click"),calls)}
    }
    @Composable private fun TestTheme(largeRtl:Boolean=false,content:@Composable ()->Unit) {
        val base=LocalContext.current;val themed=remember(base){ContextThemeWrapper(base,R.style.AppThemeWhite)};val density=LocalDensity.current
        CompositionLocalProvider(LocalContext provides themed,LocalLayoutDirection provides if(largeRtl)LayoutDirection.Rtl else LayoutDirection.Ltr,
            LocalDensity provides if(largeRtl)Density(density.density,2f) else density) {RethinkTheme(content)}
    }
}
