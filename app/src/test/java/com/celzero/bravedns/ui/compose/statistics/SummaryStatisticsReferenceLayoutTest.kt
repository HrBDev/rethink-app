/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.statistics

import android.app.Application
import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.data.DataUsageSummary
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.celzero.bravedns.ui.stats.StatsViewMode
import com.celzero.bravedns.util.Utilities
import com.celzero.bravedns.viewmodel.SummaryStatisticsViewModel.TimeCategory
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs

/**
 * AUTHORED / UNRUN. Synthetic Compose content only, plain Application, recorded callbacks.
 * No production Activity/Fragment/Application, VPN, database, network, or real traffic.
 * Source oracle: 23cb6dd:fragment_summary_statistics.xml and supplied reference image 5.
 * These geometry/glyph checks require Android/Robolectric; no pixel-equivalence claim is made.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w800dp-h1000dp-mdpi")
class SummaryStatisticsReferenceLayoutTest {
    @get:Rule val compose = createComposeRule()
    private data class Scenario(val width: Int, val density: Float = 1f, val fontScale: Float = 1f,
        val rtl: Boolean = false, val theme: Int = R.style.AppTheme)

    @Test fun insightsKeepsOneCardThreeEqualColumnsVerticalRulesAndValueFirstTypography() {
        var scenario by mutableStateOf(Scenario(412))
        val state = SummaryStatisticsState().apply { insightUsage = DataUsageSummary(0, 0, 0, 0) }
        compose.setContent { Scene(scenario) { SummaryStatisticsScreen(state) } }
        listOf(Scenario(360), Scenario(412), Scenario(640), Scenario(412, density = 1.8f),
            Scenario(412, fontScale = 1.5f, rtl = true), Scenario(412, theme = R.style.AppThemeWhite),
            Scenario(412, theme = R.style.AppThemeTrueBlackFrost)).forEach { next ->
            compose.runOnIdle { scenario = next }
            compose.onNodeWithTag("stats_usage_insights").assertIsDisplayed()
            compose.onNodeWithTag("stats_usage_list").assertDoesNotExist()
            compose.onNodeWithTag("stats_usage_progress").assertDoesNotExist()
            val columns = (0..2).map { bounds("stats_usage_column_$it") }
            columns.zipWithNext().forEach { (left, right) ->
                assertEquals("source weights remain equal", left.width, right.width, 1.1f)
                assertEquals("KPIs remain on the same row", left.top, right.top, 1.1f)
            }
            (0..2).forEach { index ->
                val value = bounds("stats_usage_value_$index")
                val caption = bounds("stats_usage_caption_$index")
                assertTrue("value above caption", value.bottom <= caption.top + 1f)
                assertEquals("value centered in its column", columns[index].center.x, value.center.x, 1.1f)
                val label = bounds("stats_usage_label_$index")
                assertEquals("label centered in its column", columns[index].center.x, label.center.x, 1.1f)
                assertEquals("caption centered in its column", columns[index].center.x, caption.center.x, 1.1f)
                compose.onNodeWithTag("stats_usage_icon_$index", useUnmergedTree = true).assertDoesNotExist()
                compose.onNodeWithTag("stats_usage_value_$index", useUnmergedTree = true).assertTextEquals("0 B")
                val valueLayout = layout("stats_usage_value_$index")
                assertEquals(18f, valueLayout.layoutInput.style.fontSize.value, 0f)
                assertTrue(requireNotNull(valueLayout.layoutInput.style.fontWeight).weight >= 700)
                assertEquals(1, valueLayout.layoutInput.maxLines)
                assertEquals(11f, layout("stats_usage_caption_$index").layoutInput.style.fontSize.value, 0f)
            }
            (1..2).forEach { index ->
                val divider = bounds("stats_usage_divider_$index")
                assertEquals("1dp source vertical stroke", next.density, divider.width, 1.1f)
                assertTrue("divider cannot regress to a horizontal rule", divider.height > divider.width * 4f)
                val x = divider.center.x
                assertTrue(x > minOf(columns[index - 1].center.x, columns[index].center.x))
                assertTrue(x < maxOf(columns[index - 1].center.x, columns[index].center.x))
            }
        }
    }

    @Test fun headerKeepsOriginalSmallcapsSpacingAndAlphaCaseException() {
        val state = SummaryStatisticsState()
        compose.setContent { Scene(Scenario(412)) { SummaryStatisticsScreen(state) } }
        compose.onNodeWithTag("stats_header_title", useUnmergedTree = true).assertTextEquals("RETHINK")
        val title = layout("stats_header_title").layoutInput.style
        assertEquals(24f, title.fontSize.value, 0f)
        assertEquals(.25f, title.letterSpacing.value, 0f)
        assertEquals(11f, layout("stats_header_subtitle").layoutInput.style.fontSize.value, 0f)
        compose.runOnIdle { state.alpha = true }
        val application = androidx.test.core.app.ApplicationProvider.getApplicationContext<Application>()
        compose.onNodeWithTag("stats_header_title", useUnmergedTree = true)
            .assertTextEquals(application.getString(R.string.app_name_alpha))
    }

    @Test fun periodControlIsCenteredFullHeightAndKeepsThreeRadioActions() {
        val calls = mutableListOf<TimeCategory>()
        val state = SummaryStatisticsState().apply {
            onTimeCategory = { timeCategory = it; calls += it }
        }
        var scenario by mutableStateOf(Scenario(412))
        compose.setContent { Scene(scenario) { SummaryStatisticsScreen(state) } }
        listOf(Scenario(360), Scenario(412), Scenario(640), Scenario(412, rtl = true)).forEach { next ->
            compose.runOnIdle { scenario = next }
            val segments = TimeCategory.entries.map { bounds("stats_time_${it.value}") }
            val group = bounds("stats_time_group")
            val center = (segments.minOf { it.left } + segments.maxOf { it.right }) / 2f
            assertEquals("joined segments centered in available width", group.center.x, center, 1.1f)
            TimeCategory.entries.forEach { time ->
                val node = compose.onNodeWithTag("stats_time_${time.value}")
                node.assertHeightIsAtLeast(40.dp).assertWidthIsAtLeast(100.dp)
                node.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            }
        }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        TimeCategory.entries.forEach { time ->
            compose.onNodeWithTag("stats_time_${time.value}").performClick().assertIsSelected()
        }
        compose.runOnIdle { assertEquals(TimeCategory.entries.toList(), calls) }
    }

    @Test fun suppliedModeAndIndependentUsageValuesSurviveSelectionAndRecomposition() {
        val calls = mutableListOf<String>()
        val state = SummaryStatisticsState().apply {
            mode = StatsViewMode.LIST
            timeCategory = TimeCategory.SEVEN_DAYS
            listUsage = DataUsageSummary(200, 100, 0, 50)
            insightUsage = DataUsageSummary(600, 200, 0, 100)
            onMode = { mode = it; calls += "mode:$it" }
            onFilter = { calls += "filter" }
        }
        compose.setContent { Scene(Scenario(412)) { SummaryStatisticsScreen(state) } }
        compose.onNodeWithTag("stats_usage_list").assertIsDisplayed()
        compose.onNodeWithTag("stats_usage_progress").assertIsDisplayed()
        compose.onNodeWithTag("stats_usage_insights").assertDoesNotExist()
        compose.onNodeWithTag("stats_mode_list").assertIsSelected().performClick()
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("stats_mode_insights").performClick().assertIsSelected()
        compose.onNodeWithTag("stats_time_2").assertIsSelected()
        listOf(700L, 100L, 800L).forEachIndexed { index, value ->
            compose.onNodeWithTag("stats_usage_value_$index", useUnmergedTree = true)
                .assertTextEquals(Utilities.humanReadableByteCount(value, true))
        }
        compose.onNodeWithTag("stats_mode_insights").performClick()
        compose.onNodeWithTag("stats_filter").performClick()
        compose.runOnIdle { assertEquals(listOf("mode:INSIGHTS", "filter"), calls); state.insightUsage = null }
        compose.onNodeWithTag("stats_usage_insights").assertDoesNotExist()
        compose.onNodeWithTag("stats_mode_insights").assertIsSelected()
        compose.runOnIdle { assertEquals(StatsViewMode.INSIGHTS, state.mode); assertEquals(TimeCategory.SEVEN_DAYS, state.timeCategory) }
    }

    @Test fun listChartAndFilterActuallyPaintGlyphsInsideTheirControls() {
        compose.setContent { Scene(Scenario(412)) { SummaryStatisticsScreen(SummaryStatisticsState()) } }
        listOf("stats_mode_list", "stats_mode_insights", "stats_filter").forEach { tag ->
            val node = compose.onNodeWithTag(tag).assertIsDisplayed().assertHasClickAction()
            val pixels = node.captureToImage().toPixelMap()
            // Center crop excludes the outline and outer inset: a blank filled block has no
            // foreground variation here, while list/chart/funnel source glyphs do.
            val colors = mutableListOf<androidx.compose.ui.graphics.Color>()
            for (y in (pixels.height * .35f).toInt() until (pixels.height * .65f).toInt())
                for (x in (pixels.width * .35f).toInt() until (pixels.width * .65f).toInt()) colors += pixels[x, y]
            assertTrue("$tag must paint a glyph, not only its background", colors.any { a -> colors.any { b ->
                abs(a.red - b.red) + abs(a.green - b.green) + abs(a.blue - b.blue) > .25f
            } })
        }
    }

    private fun bounds(tag: String): Rect = compose.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
    private fun layout(tag: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithTag(tag, useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(results)) }
        return results.single()
    }
    @Composable private fun Scene(scenario: Scenario, content: @Composable () -> Unit) {
        val base = LocalContext.current
        val context = remember(base, scenario.theme) { ContextThemeWrapper(base, scenario.theme) }
        CompositionLocalProvider(LocalContext provides context, LocalDensity provides Density(scenario.density, scenario.fontScale),
            LocalLayoutDirection provides if (scenario.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
            RethinkTheme { Box(Modifier.requiredSize(scenario.width.dp, 700.dp)) { content() } }
        }
    }
}
