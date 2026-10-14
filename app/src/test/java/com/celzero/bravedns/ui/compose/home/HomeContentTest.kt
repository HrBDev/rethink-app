/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.home

import android.app.Application
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
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
import org.robolectric.annotation.GraphicsMode

/** Renders only synthetic presentation. All actions are strict local recorders;
 * no production Activity/Fragment, repository, Koin, permission or tunnel is created. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h800dp")
class HomeContentTest {
    @get:Rule val compose = createComposeRule()
    @Test fun disabledStartDoesNothingAndEnabledTapDispatchesExactlyOnce() {
        val calls = mutableListOf<String>(); lateinit var state: HomePresentation
        compose.setContent { TestTheme {
            val context = LocalContext.current
            state = remember { HomePresentation(context).apply {
                fhsDnsOnOffBtn.isEnabled = false
                fhsDnsOnOffBtn.setOnClickListener { calls += "start" }
            } }
            HomeContent(state)
        } }
        compose.onNodeWithTag("fhs_dns_on_off_btn").assertIsNotEnabled()
        compose.runOnIdle { assertTrue(calls.isEmpty()); state.fhsDnsOnOffBtn.isEnabled = true }
        compose.onNodeWithTag("fhs_dns_on_off_btn").performClick()
        compose.runOnIdle { assertEquals(listOf("start"), calls) }
    }
    @Test fun cardCallbacksStayIndependentAndRecompositionIsSilent() {
        val calls = mutableListOf<String>(); lateinit var state: HomePresentation
        compose.setContent { TestTheme {
            val context = LocalContext.current
            state = remember { HomePresentation(context).apply {
                fhsCardDnsLl.setOnClickListener { calls += "dns" }
                fhsThroughputLl.setOnClickListener { calls += "mode" }
                homeFragmentPauseIcon.setOnClickListener { calls += "pause" }
                homeFragmentBottomSheetIcon.setOnClickListener { calls += "settings" }
            } }
            HomeContent(state)
        } }
        compose.runOnIdle { state.fhsCardDnsConnectedDns.text = "Synthetic DNS"; assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("fhs_card_dns_ll").performClick()
        compose.onNodeWithTag("fhs_throughput_ll").performClick()
        compose.onNodeWithTag("home_fragment_pause_icon").performClick()
        compose.onNodeWithTag("home_fragment_bottom_sheet_icon").performClick()
        compose.runOnIdle { assertEquals(listOf("dns", "mode", "pause", "settings"), calls) }
    }
    @Test fun logsToggleOnlyDispatchesChangedSelection() {
        val events = mutableListOf<List<Int>>()
        compose.setContent { TestTheme {
            val context = LocalContext.current
            val state = remember { HomePresentation(context).apply {
                fhsLogsPager.adapter = object : HomePages { override val itemCount = 1 }
                logsPage.fhsLogsToggleGroup.setOnCheckedStateChangeListener { _, ids -> events += ids }
            } }
            HomeContent(state)
        } }
        compose.onNodeWithTag("fhs_logs_blocked_chip").performScrollTo().performClick()
        compose.onNodeWithTag("fhs_logs_blocked_chip").performClick()
        compose.runOnIdle { assertEquals(listOf(listOf(R.id.fhs_logs_blocked_chip)), events) }
    }
    @Test fun gridSemanticClickUsesLatestWindowRatherThanStaleTouchCoordinates() {
        val calls = mutableListOf<String>()
        compose.setContent { TestTheme {
            val context = LocalContext.current
            val state = remember { HomePresentation(context).apply {
                fhsLogsPager.adapter = object : HomePages { override val itemCount = 1 }
                logsPage.fhsLogsGrid.onKeyboardClick = { calls += "latest" }
                logsPage.fhsLogsGrid.setOnClickListener { calls += "touch" }
            } }
            HomeContent(state)
        } }
        compose.onNodeWithTag("home_activity_heatmap").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.runOnIdle { assertEquals(listOf("latest"), calls) }
    }
    @Test
    @Config(qualifiers = "w700dp-h900dp")
    fun wideRtlLargeTextKeepsPauseAndModeActionsAccessible() {
        val calls = mutableListOf<String>()
        compose.setContent { TestTheme {
            val context = LocalContext.current
            val configuration = Configuration(LocalConfiguration.current).apply { screenWidthDp = 700 }
            val density = LocalDensity.current
            val state = remember { HomePresentation(context).apply {
                homeFragmentPauseIcon.setOnClickListener { calls += "pause" }
                homeFragmentBottomSheetIcon.setOnClickListener { calls += "mode" }
                fhsProtectionLevelTxt.text = "Synthetic protected status with a long descriptive label"
            } }
            CompositionLocalProvider(LocalConfiguration provides configuration, LocalLayoutDirection provides LayoutDirection.Rtl, LocalDensity provides Density(density.density, 2f)) { HomeContent(state) }
        } }
        compose.onNodeWithTag("home_fragment_pause_icon").assertIsDisplayed().performClick()
        compose.onNodeWithTag("home_fragment_bottom_sheet_icon").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(listOf("pause", "mode"), calls) }
    }
    @Test fun intervalAppExpansionUsesOnlySuppliedRecordsAndCallbacks() {
        val calls = mutableListOf<Int>()
        lateinit var adapter: com.celzero.bravedns.ui.adapter.AppActivityAdapter
        val app = com.celzero.bravedns.ui.adapter.AppActivitySummary(202, "Synthetic activity app", 10, 10, 0)
        compose.setContent { TestTheme {
            val context = LocalContext.current
            val state = remember { LogIntervalPresentation(context).apply {
                bsLaiRangeCard.isVisible = false
                adapter = com.celzero.bravedns.ui.adapter.AppActivityAdapter(false, iconResolver = { _, _ -> null }) { calls += it.uid }
                adapter.submit(listOf(app)); bsLaiRecycler.adapter = adapter
            } }
            LogIntervalContent(state)
        } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("activity_app_202").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(listOf(202), calls)
            adapter.setChildren(202, listOf(com.celzero.bravedns.ui.adapter.AppActivityEntry("synthetic.invalid", 10, 0, 0, "")))
        }
        compose.onNodeWithText("synthetic.invalid").assertExists()
        compose.onNodeWithTag("activity_app_202").performClick()
        compose.onNodeWithText("synthetic.invalid").assertDoesNotExist()
        compose.runOnIdle { assertEquals(listOf(202), calls) }
    }
    @Composable private fun TestTheme(content: @Composable () -> Unit) {
        val context = LocalContext.current
        val wrapper = remember(context) { ContextThemeWrapper(context, R.style.AppThemeWhite) }
        CompositionLocalProvider(LocalContext provides wrapper) { RethinkTheme(content) }
    }
}
