/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.statistics

import android.app.Application
import android.content.Context
import android.view.ContextThemeWrapper
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.bottomsheet.SummaryStatsFilterBottomSheet.SectionFilter as Filter
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.celzero.bravedns.ui.fragment.SummaryStatisticsFragment.SummaryStatisticsType as Type
import com.celzero.bravedns.ui.stats.StatsViewMode
import com.celzero.bravedns.viewmodel.SummaryStatisticsViewModel.TimeCategory
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Fake presentations and callback recorders only. Never launches a production activity or sheet. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class StatisticsContentTest {
    @get:Rule val compose = createComposeRule()
    @Test fun summaryModesDispatchOnlyAChangeAndTimeChipsKeepAllThreeRoutes() {
        val state = SummaryStatisticsState(); val calls = mutableListOf<String>()
        state.onMode = { state.mode = it; calls += "mode:$it" }
        state.onTimeCategory = { state.timeCategory = it; calls += "time:${it.value}" }
        state.onFilter = { calls += "filter" }
        compose.setContent { Theme { SummaryStatisticsScreen(state) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("stats_mode_insights").performClick()
        compose.onNodeWithTag("stats_mode_list").performClick()
        compose.onNodeWithTag("stats_mode_list").performClick()
        TimeCategory.entries.forEach { compose.onNodeWithTag("stats_time_${it.value}").performClick().assertIsSelected() }
        compose.onNodeWithTag("stats_filter").performClick()
        compose.runOnIdle { assertEquals(listOf("mode:LIST") + TimeCategory.entries.map { "time:${it.value}" } + "filter", calls) }
    }
    @Test fun summaryDetailAndCloseCancelConfirmHaveSeparateExactCallbacks() {
        val state = SummaryStatisticsState().apply { mode = StatsViewMode.LIST; sections.getValue(Type.TOP_ACTIVE_CONNS).visible = true }
        val calls = mutableListOf<String>()
        state.onDetail = { calls += "detail:${it.tid}" }
        state.onClose = { state.closeDialog = true }
        state.onConfirmClose = { calls += "confirm" }
        compose.setContent { Theme { SummaryStatisticsScreen(state) } }
        compose.onNodeWithTag("stats_detail_9").performScrollTo().performClick()
        compose.onNodeWithTag("stats_close_all").performClick()
        compose.onNodeWithTag("stats_cancel_close").performClick()
        compose.runOnIdle { assertEquals(listOf("detail:9"), calls); assertFalse(state.closeDialog) }
        compose.onNodeWithTag("stats_close_all").performClick()
        compose.onNodeWithTag("stats_confirm_close").performClick()
        compose.onNodeWithTag("stats_confirm_close").assertDoesNotExist()
        compose.runOnIdle { assertEquals(listOf("detail:9", "confirm"), calls); assertFalse(state.closeDialog) }
    }
    @Test fun summaryFilterPreservesIndependentSelectionsAndHiddenProviderOnSelectAll() {
        val calls = mutableListOf<Set<Filter>>(); var applied = 0
        val selection = SummaryFilterSelection(setOf(Filter.APPS, Filter.PROVIDERS)) { calls += it }
        compose.setContent { Theme { SummaryFilterSheetContent(selection, false) { applied++ } } }
        compose.onNodeWithTag("stats_filter_2").assertDoesNotExist()
        compose.onNodeWithTag("stats_filter_0").assertIsSelected()
        compose.onNodeWithTag("stats_filter_1").performClick().assertIsSelected()
        compose.onNodeWithTag("stats_filter_0").assertIsSelected()
        compose.onNodeWithTag("stats_filter_select_all").performClick()
        compose.onNodeWithTag("stats_filter_apply").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(Filter.entries.toSet(), selection.selected); assertEquals(4, calls.size); assertEquals(1, applied) }
    }
    @Test fun selectionRequiredSurvivesClickAndRtlLargeText() {
        val calls = mutableListOf<Set<Filter>>()
        val selection = SummaryFilterSelection(setOf(Filter.IPS)) { calls += it }
        compose.setContent { Theme(rtl = true, fontScale = 1.8f) { SummaryFilterSheetContent(selection, true) {} } }
        compose.onNodeWithTag("stats_filter_5").performScrollTo().performClick().assertIsSelected()
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("stats_filter_2").performScrollTo().performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(listOf(setOf(Filter.PROVIDERS, Filter.IPS)), calls) }
    }
    @Test fun rpnLoadingContentEmptyErrorAndLogActionRemainSeparate() {
        val b = RpnStatisticsPresentation(ApplicationProvider.getApplicationContext<Context>())
        var clicks = 0; b.rpnStatsViewLogs.setOnClickListener { clicks++ }
        compose.setContent { Theme { RpnStatisticsContent(b) } }
        compose.onNodeWithTag("rpn_stats_loading").assertIsDisplayed()
        compose.onNodeWithTag("rpn_stats_logs").assertDoesNotExist()
        compose.runOnIdle {
            b.rpnStatsProgress.isVisible = false; b.rpnStatsContent.isVisible = true
            b.rpnStatsTopAppsEmpty.isVisible = true; b.rpnStatsBlockedCard.isVisible = false
            b.rpnStatsRx.text = "123 MB"; b.rpnStatsTx.text = "45 MB"
        }
        compose.onNodeWithTag("rpn_stats_loading").assertDoesNotExist()
        compose.onNodeWithText("123 MB").assertIsDisplayed()
        compose.onNodeWithTag("rpn_stats_empty").assertIsDisplayed()
        compose.onNodeWithText(string(R.string.lbl_blocked)).assertDoesNotExist()
        compose.onNodeWithTag("rpn_stats_logs").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, clicks); b.rpnStatsContent.isVisible = false; b.rpnStatsError.isVisible = true }
        compose.onNodeWithTag("rpn_stats_logs").assertDoesNotExist()
        compose.onNodeWithText(string(R.string.rpn_stats_no_active_proxy)).assertIsDisplayed()
    }
    @Test fun pagedScreenRetainsHeaderAndFakeRowOrderAcrossEmptyTransition() {
        var empty by mutableStateOf(true)
        compose.setContent { Theme { PagedStatisticsScreen("Synthetic title", "Synthetic range", empty, null, extraCount = 2, extraRow = { Text("Fake row $it") }) } }
        compose.onNodeWithTag("statistics_empty").assertIsDisplayed()
        compose.onNodeWithText("Synthetic title").assertIsDisplayed()
        compose.runOnIdle { empty = false }
        compose.onNodeWithTag("statistics_empty").assertDoesNotExist()
        compose.onNodeWithText("Synthetic range").assertIsDisplayed()
        compose.onNodeWithText("Fake row 0").assertIsDisplayed()
        compose.onNodeWithText("Fake row 1").assertIsDisplayed()
    }
    @Test fun sourceSummaryRowRetainsTextSizesAndOneLineTruncation() {
        compose.setContent { Theme { StatisticsRow("Synthetic summary", "Synthetic subtitle", "", "123") } }
        assertEquals(14f, textLayout("Synthetic summary").layoutInput.style.fontSize.value, 0f)
        assertEquals(11f, textLayout("Synthetic subtitle").layoutInput.style.fontSize.value, 0f)
        assertEquals(12f, textLayout("123").layoutInput.style.fontSize.value, 0f)
        assertEquals(1, textLayout("Synthetic summary").layoutInput.maxLines)
        assertEquals(androidx.compose.ui.text.style.TextOverflow.Ellipsis, textLayout("Synthetic subtitle").layoutInput.overflow)
    }
    @Test fun rpnAndBlocklistRowsKeepIndependentOriginalTextContracts() {
        compose.setContent { Theme { androidx.compose.foundation.layout.Column {
            RpnStatisticsAppRow("Synthetic RPN app", "Synthetic metadata", "255 MB", null)
            BlocklistStatisticsAppRow("Synthetic blocklist app", "13", null, false, "synthetic_blocklist") {}
            BlocklistStatisticsDomainRow("synthetic.example.invalid", "7")
        } } }
        assertEquals(14f, textLayout("Synthetic RPN app").layoutInput.style.fontSize.value, 0f)
        assertEquals(11f, textLayout("Synthetic metadata").layoutInput.style.fontSize.value, 0f)
        assertEquals(13f, textLayout("255 MB").layoutInput.style.fontSize.value, 0f)
        assertEquals(16f, textLayout("13").layoutInput.style.fontSize.value, 0f)
        assertEquals(12f, textLayout("synthetic.example.invalid").layoutInput.style.fontSize.value, 0f)
        assertEquals(androidx.compose.ui.text.style.TextOverflow.MiddleEllipsis, textLayout("synthetic.example.invalid").layoutInput.overflow)
    }
    private fun textLayout(text: String): androidx.compose.ui.text.TextLayoutResult {
        val result = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        compose.onNodeWithText(text, useUnmergedTree = true).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { assertTrue(it(result)) }
        return result.single()
    }
    private fun string(id: Int) = ApplicationProvider.getApplicationContext<Context>().getString(id)
    @Composable private fun Theme(rtl: Boolean = false, fontScale: Float = 1f, content: @Composable () -> Unit) {
        val base = LocalContext.current
        val context = remember(base) { ContextThemeWrapper(base, R.style.AppThemeWhite) }
        CompositionLocalProvider(LocalContext provides context, LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            LocalDensity provides Density(LocalDensity.current.density, fontScale)) { RethinkTheme(content) }
    }
}
