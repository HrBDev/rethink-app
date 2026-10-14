/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.controls

import android.app.Application
import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.google.android.material.button.MaterialButton
import org.robolectric.Robolectric
import com.google.android.material.R as MaterialR
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Authored synthetic tests only. Plain Application; no production controller, service,
 * tunnel, traffic, account, billing, network, persistent store, or real Activity is constructed.
 * These tests require the unavailable Android/Robolectric toolchain and have NOT been executed. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class OriginalControlsTest {
    @get:Rule val compose = createComposeRule()
    private val density = Density(1f, 1f)
    private val themes = listOf(R.style.AppTheme, R.style.AppThemeWhite, R.style.AppThemeTrueBlack,
        R.style.AppThemeWhitePlus, R.style.AppThemeTrueBlackPlus, R.style.AppThemeTrueBlackFrost)
    private fun context(theme: Int = R.style.AppThemeWhite) =
        ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), theme)
    private fun look(key: String, theme: Int = R.style.AppThemeWhite) =
        resolveOriginalControl(context(theme), originalControlSpecs.getValue(key), density)
    private fun radius(key: String) =
        (look(key).shape.createOutline(Size(160f, 48f), LayoutDirection.Ltr, density) as Outline.Rounded).roundRect.topLeftCornerRadius.x

    @Test fun screenshotSegmentHeightsAndIconSizesMatchTheNativeSourceWidgets() {
        val keys = listOf("fragment_summary_statistics:tb_recent_toggle_btn",
            "fragment_summary_statistics:tb_daily_toggle_btn", "fragment_summary_statistics:tb_weekly_toggle_btn",
            "fragment_summary_statistics:fss_view_mode_list_btn", "fragment_summary_statistics:fss_view_mode_insights_btn",
            "fragment_summary_statistics:fss_filter_btn", "fragment_rethink_blocklist:lb_simple_toggle_btn",
            "fragment_rethink_blocklist:lb_adv_toggle_btn")
        themes.forEach { theme -> keys.forEach { key ->
            val themed = context(theme)
            val spec = originalControlSpecs.getValue(key)
            val attrs = Robolectric.buildAttributeSet()
                .setStyleAttribute("@style/" + themed.resources.getResourceEntryName(spec.style)).build()
            val native = MaterialButton(themed, attrs)
            val actual = resolveOriginalControl(themed, spec, density)
            val pixelsPerDp = themed.resources.displayMetrics.density
            if (spec.visualHeight == 0f) assertEquals("$key minimum height",
                native.minimumHeight / pixelsPerDp, actual.height, .01f)
            assertEquals("$key icon size (zero means intrinsic)", native.iconSize / pixelsPerDp, actual.iconSize, .01f)
            native.icon?.let { icon ->
                assertTrue("$key must retain its source icon", actual.icon != 0)
                assertTrue(icon.intrinsicWidth > 0)
                assertTrue(icon.intrinsicHeight > 0)
            }
        } }
    }

    @Test fun intrinsicIconBoundsSurviveDensityAndNonSquareDrawables() {
        assertEquals(24.dp, originalIconExtent(48f, Density(2f)))
        assertEquals(16.dp, originalIconExtent(32f, Density(2f)))
        assertEquals(24.dp, originalIconExtent(Float.NaN, Density(2f)))
    }

    @Test fun distinctFamiliesKeepOriginalCornersHeightsAndTextSizes() {
        assertEquals(12f, radius("@action"), 0f)
        assertEquals(24f, look("@action").height, 0f)
        assertEquals(11f, look("@action").textStyle.fontSize.value, 0f)
        assertEquals(18f, radius("@large"), 0f)
        assertEquals(32f, look("@large").height, 0f)
        assertEquals(14f, look("@large").textStyle.fontSize.value, 0f)
        assertEquals(20f, radius("@activity_time"), 0f)
        assertEquals(38f, look("@activity_time").height, 0f)
        assertEquals(8f, radius("@connection_rule"), 0f)
        assertEquals(40f, look("@connection_rule").height, 0f)
        // MDC 1.14 applies inherited chipCornerRadius after this source's explicit shapeAppearance.
        assertEquals(12f, radius("fragment_rethink_list:lb_version"), 0f)
    }

    @Test fun everyCatalogStyleResolvesAcrossSixThemesWithoutInvokingCallbacks() {
        themes.forEach { theme -> originalControlSpecs.forEach { (key, spec) ->
            val result = resolveOriginalControl(context(theme), spec, density)
            assertTrue("$key height", result.height >= 0f)
            assertTrue("$key text", result.textStyle.fontSize.value > 0f)
            assertTrue("$key stroke", result.strokeWidth >= 0f)
        } }
    }

    /** Independent oracle: the real MDC widget constructor applies its own overlays and
     * selectors. It does not call the Compose resolver or use a snapshot of its output. */
    @Test fun materialButtonSelectorsMatchNativeWidgetsIncludingAlphaThemes() {
        val states = listOf(
            intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_enabled, -android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_enabled, android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused))
        val overlays = listOf(0, R.style.ThemeOverlay_App_AlphaLight, R.style.ThemeOverlay_App_AlphaDark)
        themes.forEach { theme -> overlays.forEach { overlay ->
            val themed = context(theme)
            if (overlay != 0) themed.theme.applyStyle(overlay, true)
            originalControlSpecs.filterValues { it.kind == OriginalControlKind.Button }.forEach { (key, spec) ->
                val attrs = Robolectric.buildAttributeSet()
                    .setStyleAttribute("@style/" + themed.resources.getResourceEntryName(spec.style)).build()
                val native = MaterialButton(themed, attrs)
                val actual = resolveOriginalControl(themed, spec, density)
                if (actual.background != null) {
                    assertNotNull("$key custom background must survive", native.background)
                    assertEquals("$key custom background class", native.background.javaClass, actual.background.javaClass)
                    assertNull("$key must not add a managed ripple over a custom background", actual.ripple)
                    assertNull("$key must not add a managed stroke over a custom background", actual.stroke)
                    assertEquals(0f, actual.strokeWidth, 0f)
                }
                states.forEach { state ->
                    fun color(list: android.content.res.ColorStateList?) = list?.getColorForState(state, list.defaultColor) ?: 0
                    assertEquals("$key fill theme=$theme overlay=$overlay state=${state.toList()}",
                        color(native.backgroundTintList), color(actual.fill))
                    assertEquals("$key text", color(native.textColors), color(actual.textColor))
                    assertEquals("$key stroke", color(native.strokeColor), color(actual.stroke))
                    assertEquals("$key ripple", color(native.rippleColor), color(actual.ripple))
                }
            }
        } }
    }

    @Test fun textAndDialogButtonsStayTransparentWhileFilledButtonsKeepTheirSourceFill() {
        val normal = intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_checked)
        val disabled = intArrayOf(-android.R.attr.state_enabled, -android.R.attr.state_checked)
        themes.forEach { theme ->
            listOf("@text", "@dialog", "@dialog_positive", "@dialog_negative", "@dialog_neutral", "@outlined").forEach { key ->
                listOf(normal, disabled).forEach { state ->
                    val fill = look(key, theme).fill
                    assertEquals("$key must not become a filled purple button", 0,
                        android.graphics.Color.alpha(fill?.getColorForState(state, fill.defaultColor) ?: 0))
                }
            }
            val filled = requireNotNull(look("@filled", theme).fill)
            assertTrue(android.graphics.Color.alpha(filled.getColorForState(normal, filled.defaultColor)) > 0)
        }
    }

    @Test fun compactGroupedButtonsKeepSourceBoundsAndActions() {
        var calls = 0
        compose.setContent { TestTheme {
            OriginalButton("fragment_summary_statistics:fss_view_mode_list_btn", { calls++ },
                Modifier.testTag("grouped"), selected = true, grouped = true) {}
        } }
        compose.onNodeWithTag("grouped").assertWidthIsEqualTo(32.dp)
            .assertHeightIsEqualTo(32.dp).assertIsSelected().performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test fun nativeTypefaceWeightAndRawSpSurviveLargerFontScale() {
        val key = "view_tour_tooltip:tour_next_btn"
        val normal = look(key)
        val larger = resolveOriginalControl(context(), originalControlSpecs.getValue(key), Density(1f, 2f))
        assertEquals(13f, normal.textStyle.fontSize.value, 0f)
        assertEquals(normal.textStyle.fontSize.value, larger.textStyle.fontSize.value, 0f)
        val expected = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL).weight
        assertEquals(expected, normal.textStyle.fontWeight?.weight)
    }

    @Test fun sourceNullsAndCheckedIconVisibilityDoNotResurrectThemeDefaults() {
        val key = "fragment_summary_statistics:fss_active_apps_chip"
        assertTrue(MaterialR.attr.chipSurfaceColor in originalControlSpecs.getValue(key).explicitNulls)
        assertNull(look(key).surface)
        assertTrue(look("item_chip_filter:(anonymous)").checkedIconVisible)
        assertFalse(look("bottom_sheet_summary_stats_filter:fss_filter_apps_chip").checkedIconVisible)
        assertFalse(look("@rpn_filter").checkedIconVisible)
    }

    @Test fun programmaticRpnSelectorsPreserveCheckedAndUncheckedColors() {
        val checked = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked)
        val unchecked = intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_checked)
        themes.forEach { theme ->
            val look = look("@rpn_filter", theme)
            val fill = requireNotNull(look.fill)
            val stroke = requireNotNull(look.stroke)
            assertEquals(0x33, android.graphics.Color.alpha(fill.getColorForState(checked, 0)))
            assertEquals(0, android.graphics.Color.alpha(stroke.getColorForState(unchecked, 0)))
            assertEquals(1f, look.strokeWidth, 0f)
        }
    }

    @Test fun fixedCompactBoundsAndSegmentEndCapsRespectRtl() {
        val spec = originalControlSpecs.getValue("fragment_summary_statistics:fss_filter_btn")
        assertEquals(32f, spec.visualWidth, 0f)
        assertEquals(32f, look("fragment_summary_statistics:fss_filter_btn").height, 0f)
        val base = look("@activity_time").shape
        val ltr = (OriginalSegmentShape(base, 0, 3).createOutline(Size(100f, 38f), LayoutDirection.Ltr, density) as Outline.Rounded).roundRect
        val rtl = (OriginalSegmentShape(base, 0, 3).createOutline(Size(100f, 38f), LayoutDirection.Rtl, density) as Outline.Rounded).roundRect
        assertEquals(20f, ltr.topLeftCornerRadius.x, 0f)
        assertEquals(0f, ltr.topRightCornerRadius.x, 0f)
        assertEquals(0f, rtl.topLeftCornerRadius.x, 0f)
        assertEquals(20f, rtl.topRightCornerRadius.x, 0f)
    }

    @Test fun disabledActionsAreSilentAndPassiveLabelsHaveNoClickSemantics() {
        val calls = mutableListOf<String>()
        var enabled by mutableStateOf(false)
        compose.setContent { TestTheme {
            Column {
                OriginalButton("@outlined", { calls += "action" }, Modifier.testTag("action"), enabled = enabled) { Text("Synthetic action") }
                OriginalLabel("item_removed_server:removal_badge", Modifier.testTag("passive")) { Text("Synthetic badge") }
            }
        } }
        compose.onNodeWithTag("action").assertIsNotEnabled().performTouchInput { click() }
        compose.onNodeWithTag("passive").assertHasNoClickAction()
        compose.runOnIdle { assertTrue(calls.isEmpty()); enabled = true }
        compose.onNodeWithTag("action").performClick()
        compose.runOnIdle { assertEquals(listOf("action"), calls) }
    }

    @Test fun selectionRecompositionIsSilentAndSyntheticTapDispatchesExactlyOnce() {
        val calls = mutableListOf<String>()
        var selected by mutableStateOf(false)
        compose.setContent { TestTheme {
            OriginalChip("@large", selected, { calls += "select"; selected = true }, { Text("Synthetic filter") }, Modifier.testTag("filter"))
        } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("filter").assertIsNotSelected().performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(listOf("select"), calls) }
    }

    @Composable private fun TestTheme(content: @Composable () -> Unit) {
        val current = LocalContext.current
        val themed = remember(current) { ContextThemeWrapper(current, R.style.AppThemeWhite) }
        CompositionLocalProvider(LocalContext provides themed) { RethinkTheme(content) }
    }
}
