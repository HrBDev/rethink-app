/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.about

import android.app.Application
import android.view.ContextThemeWrapper
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.celzero.bravedns.ui.compose.utilities.utilityTextStyle
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Synthetic Compose bounds/callback fixtures only. No production host, service or live account. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class AboutReferenceLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun pairedActionsKeepEqualColumnsAndIndependentRecordedCallbacks() {
        val calls = mutableListOf<String>()
        compose.setContent { ReferenceTheme {
            val context = LocalContext.current
            val state = remember { AboutState(context).apply {
                aboutTelegram.setOnClickListener { calls += "telegram" }
                aboutCrashLog.setOnClickListener { calls += "report" }
            } }
            Column(Modifier.width(412.dp)) { AboutQuickActions(state) }
        } }
        assertPairedColumns(LayoutDirection.Ltr)
        compose.onNodeWithTag("about.telegram").performClick()
        compose.onNodeWithTag("about.report").performClick()
        compose.runOnIdle { assertEquals(listOf("telegram", "report"), calls) }
    }

    @Test fun rtlLargeTextKeepsPairedActionsAndTheirOwnSubtitles() {
        compose.setContent { ReferenceTheme(rtl = true, fontScale = 1.3f) {
            val context = LocalContext.current
            val state = remember { AboutState(context) }
            Column(Modifier.width(412.dp)) { AboutQuickActions(state) }
        } }
        assertPairedColumns(LayoutDirection.Rtl)
    }

    @Test fun reportProgressStaysInTheRightColumnAndDoesNotDispatch() {
        lateinit var state: AboutState
        val calls = mutableListOf<String>()
        compose.setContent { ReferenceTheme {
            val context = LocalContext.current
            state = remember { AboutState(context).apply {
                aboutCrashLog.setOnClickListener { calls += "report" }
            } }
            Column(Modifier.width(412.dp)) { AboutQuickActions(state) }
        } }
        val before = bounds("about.telegram-column")
        compose.runOnIdle {
            state.progressLayout.visibility = View.VISIBLE
            state.aboutCrashLog.visibility = View.GONE
        }
        compose.onNodeWithTag("about.report").assertDoesNotExist()
        compose.onNodeWithTag("about.report-progress").assertIsDisplayed()
        assertEquals(before, bounds("about.telegram-column"))
        val progress = bounds("about.report-progress")
        val reportColumn = bounds("about.report-column")
        assertTrue(progress.left >= reportColumn.left && progress.right <= reportColumn.right)
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
    }

    @Test fun introductionPreservesDynamicUsageAndConditionalSponsorActions() {
        lateinit var state: AboutState
        val calls = mutableListOf<String>()
        compose.setContent { ReferenceTheme {
            val context = LocalContext.current
            state = remember { AboutState(context).apply {
                sponsorInfoUsage.text = "Synthetic usage value A"
                aboutSponsor.setOnClickListener { calls += "sponsor" }
                aboutManageRpn.setOnClickListener { calls += "manage" }
            } }
            Column(Modifier.width(412.dp)) { AboutIntroduction(state) }
        } }
        compose.onNodeWithText("Synthetic usage value A").assertIsDisplayed()
        compose.onNodeWithTag("about.manage-rpn").assertDoesNotExist()
        compose.onNodeWithTag("about.sponsor").performClick()
        val intro = bounds("about.description.intro")
        val authors = bounds("about.description.authors")
        val usage = bounds("about.description.usage")
        assertEquals(intro.left, authors.left, .5f)
        assertEquals(authors.left, usage.left, .5f)
        assertTrue(authors.top > intro.bottom)
        assertTrue(usage.top > authors.bottom)
        compose.runOnIdle {
            state.sponsorInfoUsage.text = "Synthetic usage value B"
            state.aboutSponsor.visibility = View.GONE
            state.aboutManageRpn.visibility = View.VISIBLE
        }
        compose.onNodeWithText("Synthetic usage value B").assertIsDisplayed()
        compose.onNodeWithText("Synthetic usage value A").assertDoesNotExist()
        compose.onNodeWithTag("about.sponsor").assertDoesNotExist()
        compose.onNodeWithTag("about.manage-rpn").performClick()
        compose.runOnIdle { assertEquals(listOf("sponsor", "manage"), calls) }
    }

    @Test fun updateVisibilityRemainsOwnedByPresentationState() {
        lateinit var state: AboutState
        var updates = 0
        compose.setContent { ReferenceTheme {
            val context = LocalContext.current
            state = remember { AboutState(context).apply {
                aboutAppUpdate.text = "Synthetic update action"
                aboutAppContributors.text = "Synthetic authors"
                aboutAppUpdate.visibility = View.GONE
                aboutAppUpdate.setOnClickListener { updates++ }
            } }
            AboutContent(state)
        } }
        compose.onNodeWithTag("about").performScrollToNode(hasText("Synthetic authors"))
        compose.onNodeWithText("Synthetic update action").assertDoesNotExist()
        compose.runOnIdle { state.aboutAppUpdate.visibility = View.VISIBLE }
        compose.onNodeWithTag("about").performScrollToNode(hasText("Synthetic update action"))
        compose.onNodeWithText("Synthetic update action").performClick()
        compose.runOnIdle { assertEquals(1, updates) }
    }

    @Test fun headerUsesSourceRelativeTrackingAndFontPadding() {
        compose.setContent { ReferenceTheme {
            val style = utilityTextStyle(appearance = R.style.TextAppearance_Heading_HomeScreen)
            assertEquals(.25f, style.letterSpacing.value, .001f)
            assertEquals(24f, style.fontSize.value, .001f)
            assertTrue(style.platformStyle?.paragraphStyle?.includeFontPadding == true)
        } }
    }

    private fun bounds(tag: String): Rect = compose.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot

    private fun assertPairedColumns(direction: LayoutDirection) {
        val telegram = bounds("about.telegram-column")
        val report = bounds("about.report-column")
        assertEquals(telegram.width, report.width, 1f)
        assertEquals(telegram.top, report.top, .5f)
        val left = if (direction == LayoutDirection.Ltr) telegram else report
        val right = if (direction == LayoutDirection.Ltr) report else telegram
        assertTrue("Equal columns must have a center gap", right.left > left.right)
        for (name in listOf("telegram", "report")) {
            val action = bounds("about.$name")
            val subtitle = bounds("about.$name-subtitle")
            assertTrue("Subtitle must remain below its own action", subtitle.top >= action.bottom)
            assertEquals(action.center.x, subtitle.center.x, .5f)
        }
    }
}

@Composable
private fun ReferenceTheme(rtl: Boolean = false, fontScale: Float = 1f, content: @Composable () -> Unit) {
    val base = LocalContext.current
    val context = remember(base) { ContextThemeWrapper(base, R.style.AppThemeTrueBlack) }
    val density = LocalDensity.current
    CompositionLocalProvider(LocalContext provides context,
        LocalDensity provides Density(density.density, fontScale),
        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        RethinkTheme(content)
    }
}
