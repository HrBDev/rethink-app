/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.controls

import android.app.Application
import android.view.ContextThemeWrapper
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.data.AllowedAppInfo
import com.celzero.bravedns.data.BlockedAppInfo
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.celzero.bravedns.ui.compose.appdetails.*
import com.celzero.bravedns.ui.compose.utilities.BubbleAllowedRow
import com.celzero.bravedns.ui.compose.utilities.BubbleBlockedRow
import com.google.android.material.tabs.TabLayout
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Authored, NOT RUN. Synthetic renderers and source-style widget references only. No production
 * Application, Activity, controller, VPN, network, database, billing or persistent store is created. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class OriginalRowsAndTabsTest {
    @get:Rule val compose = createComposeRule()
    private val themes = listOf(R.style.AppTheme, R.style.AppThemeWhite, R.style.AppThemeTrueBlack,
        R.style.AppThemeWhitePlus, R.style.AppThemeTrueBlackPlus, R.style.AppThemeTrueBlackFrost)
    private fun context(theme: Int = R.style.AppThemeWhite) =
        ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), theme)

    @Composable private fun Fixture(scale: Float = 1f, rtl: Boolean = false, content: @Composable () -> Unit) {
        val context = remember { context() }
        CompositionLocalProvider(LocalContext provides context, LocalDensity provides Density(1f, scale),
            LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) { RethinkTheme(content) }
    }

    /** The real MDC constructor is an independent oracle for inherited attributes and overlays. */
    @Test fun tabStyleMatchesNativeWidgetAcrossSourceThemes() {
        themes.forEach { theme ->
            val context = context(theme)
            val attrs = Robolectric.buildAttributeSet().setStyleAttribute("@style/TabbedLayoutDefault").build()
            val native = TabLayout(context, attrs)
            val actual = resolveOriginalTabs(context)
            assertEquals(TabLayout.MODE_SCROLLABLE, native.tabMode)
            assertEquals(native.tabMode, actual.mode)
            assertEquals(native.tabGravity, actual.gravity)
            assertEquals(native.isTabIndicatorFullWidth, actual.indicatorFullWidth)
            assertEquals(native.tabIndicatorAnimationMode, actual.animationMode)
            assertEquals(native.tabIndicatorGravity, actual.indicatorGravity)
            assertEquals(native.hasUnboundedRipple(), actual.unboundedRipple)
            assertEquals(native.tabSelectedIndicator.intrinsicHeight, actual.indicator?.intrinsicHeight)
            listOf(intArrayOf(), intArrayOf(android.R.attr.state_enabled), intArrayOf(android.R.attr.state_selected)).forEach { state ->
                val expected = native.tabTextColors!!.getColorForState(state, native.tabTextColors!!.defaultColor)
                val color = if (android.R.attr.state_selected in state) actual.selectedTextColor
                    else actual.textColors!!.getColorForState(state, actual.textColors.defaultColor)
                assertEquals(expected, color)
                assertEquals(native.tabRippleColor!!.getColorForState(state, native.tabRippleColor!!.defaultColor),
                    actual.rippleColors!!.getColorForState(state, actual.rippleColors.defaultColor))
            }
            val px = context.resources.displayMetrics.density
            assertEquals((25 * px + .5f).toInt(), actual.paddingStart)
            assertEquals(actual.paddingStart, actual.paddingEnd)
            assertEquals((30 * px + .5f).toInt(), actual.contentStart)
            assertFalse(actual.allCaps)
        }
    }

    @Test fun centeredTabsEqualizeOnlyInsideSourceGutters() {
        assertEquals(listOf(110, 110), originalTabWidths(listOf(80, 110), 320, 16, 1))
        assertEquals(listOf(80, 150), originalTabWidths(listOf(80, 150), 240, 16, 1))
        assertEquals(listOf(80, 110), originalTabWidths(listOf(80, 110), 320, 16, 2))
        assertEquals(0f to 100f, originalTabIndicatorEdges(0f, 100f, 100f, 220f, 0f, 1))
        assertEquals(100f to 220f, originalTabIndicatorEdges(0f, 100f, 100f, 220f, 1f, 1))
        val forward = originalTabIndicatorEdges(0f, 100f, 100f, 220f, .5f, 1)
        val reverse = originalTabIndicatorEdges(100f, 220f, 0f, 100f, .5f, 1)
        assertTrue(forward.second - forward.first > 120f)
        assertEquals(forward.first, reverse.first, .0001f)
        assertEquals(forward.second, reverse.second, .0001f)
    }

    @Test fun tabCompositionAndRecompositionAreSilentAndClicksKeepSelection() {
        var page by mutableIntStateOf(0)
        val calls = mutableListOf<Int>()
        compose.setContent { Fixture(rtl = true) {
            OriginalTabStrip(page, listOf("IP rules", "Domain rules"), { calls += it; page = it }, Modifier.width(300.dp))
        } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithText("Domain rules").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(listOf(1), calls) }
        compose.onNodeWithText("IP rules").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(listOf(1, 0), calls) }
    }

    private fun textLayout(text: String): TextLayoutResult {
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(text, useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        return layouts.single()
    }

    @Test fun countryAndIpRetainDifferentLineWeightAndSubtitleContracts() {
        val country = AppCountriesRowState().apply { accCountryName.text = "Synthetic country name"; accCount.text = "9999" }
        val ip = AppIpsRowState().apply { acdIpAddress.text = "2001:db8::1"; acdDomainName.text = "a.example, b.example"; acdCount.text = "42" }
        compose.setContent { Fixture { Column(Modifier.width(300.dp)) { AppCountriesRowContent(country); AppIpsRowContent(ip) } } }
        val countryLayout = textLayout("Synthetic country name").layoutInput
        assertEquals(1, countryLayout.maxLines)
        assertEquals(TextOverflow.Ellipsis, countryLayout.overflow)
        assertEquals(FontWeight.Bold, countryLayout.style.fontWeight)
        val ipLayout = textLayout("2001:db8::1").layoutInput
        assertEquals(Int.MAX_VALUE, ipLayout.maxLines)
        assertEquals(TextOverflow.StartEllipsis, ipLayout.overflow)
        assertEquals(3, textLayout("a.example, b.example").layoutInput.maxLines)
        assertEquals(1, textLayout("9999").layoutInput.maxLines)
        assertEquals(16f, textLayout("9999").layoutInput.style.fontSize.value, 0f)
        compose.runOnIdle { ip.acdDomainName.visibility = View.GONE }
        compose.onNodeWithText("a.example, b.example", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test fun bubbleRowsKeepOneLineNamesAndActionsAtLargeFont() {
        val name = "Synthetic application with a deliberately long name"
        val allowed = AllowedAppInfo("fixture.missing.package", name, 7, 0)
        val blocked = BlockedAppInfo("Unknown", "Blocked synthetic application", 8, 3, 0)
        val calls = mutableListOf<Int>()
        compose.setContent { Fixture(scale = 1.5f) { Column(Modifier.width(320.dp)) {
            BubbleAllowedRow(allowed, { calls += it.uid }, now = 1)
            BubbleBlockedRow(blocked, { calls += it.uid }, now = 1)
        } } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        assertEquals(1, textLayout(name).layoutInput.maxLines)
        compose.onNodeWithTag("bubble.remove.7").performClick()
        compose.onNodeWithTag("bubble.allow.8").performClick()
        compose.runOnIdle { assertEquals(listOf(7, 8), calls) }
    }
}
