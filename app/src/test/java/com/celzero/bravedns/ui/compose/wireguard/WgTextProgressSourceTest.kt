/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.wireguard

import android.app.Application
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.ContextThemeWrapper
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.celzero.bravedns.ui.compose.controls.*
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** AUTHORED_UNRUN. Plain Application and isolated renderer/widget fixtures only. Does not launch
 * a production Activity/Application, controller, VPN, service, database, network or persistent store.
 * Source guards passing do not establish these runtime/layout assertions. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class WgTextProgressSourceTest {
    @get:Rule val compose = createComposeRule()
    private val themes = listOf(R.style.AppTheme, R.style.AppThemeWhite, R.style.AppThemeTrueBlack,
        R.style.AppThemeWhitePlus, R.style.AppThemeTrueBlackPlus, R.style.AppThemeTrueBlackFrost)
    private fun context(theme: Int = R.style.AppThemeWhite) =
        ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), theme)

    @Test fun mediumFamilyTrackingAndSingleLineMatchIndependentNativeFixture() {
        themes.forEach { theme ->
            val context = context(theme)
            val attrs = Robolectric.buildAttributeSet()
                .addAttribute(android.R.attr.fontFamily, "sans-serif-medium")
                .addAttribute(android.R.attr.textSize, "16sp")
                .addAttribute(android.R.attr.letterSpacing, "0.01")
                .addAttribute(android.R.attr.singleLine, "true")
                .addAttribute(android.R.attr.ellipsize, "end").build()
            val native = AppCompatTextView(context, attrs)
            val metrics = context.resources.displayMetrics
            val actual = resolveWgText(context, Density(metrics.density, metrics.scaledDensity / metrics.density),
                "list_item_wg_include_apps:wg_include_app_list_apk_label_tv")
            assertEquals(native.textSize / metrics.scaledDensity, actual.style.fontSize.value, .01f)
            assertEquals(native.letterSpacing, actual.style.letterSpacing.value, .0001f)
            assertEquals(native.typeface.weight, actual.style.fontWeight!!.weight)
            assertEquals(native.maxLines, actual.maxLines)
            assertTrue(actual.singleLine)
        }
    }

    @Test fun materialAppearanceAndLineSpacingComeFromSourceBeforeLiveOverrides() {
        val context = context()
        val density = Density(context.resources.displayMetrics.density)
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.textSize, "14sp")
            .addAttribute(android.R.attr.lineSpacingExtra, "4dp").build()
        val native = MaterialTextView(context, attrs)
        val actual = resolveWgText(context, density, "dialog_rpn_ssid:description_text_view")
        val expectedPixels = native.paint.fontMetricsInt.run { descent - ascent } + native.lineSpacingExtra
        assertEquals(expectedPixels / native.textSize, actual.style.lineHeight.value, .01f)
        val node = WgControlState(context, textSource = "list_item_wg_include_apps:wg_include_app_list_apk_label_tv")
        node.textSizeSp = 21f; node.letterSpacingEm = .07f; node.fontFamilyName = "monospace"
        node.textStyle = Typeface.ITALIC; node.maxLines = 3; node.ellipsize = false
        val override = resolveWgText(context, density, node.textSource, node)
        assertEquals(21f, override.style.fontSize.value, 0f)
        assertEquals(.07f, override.style.letterSpacing.value, 0f)
        assertEquals(3, override.maxLines)
        assertEquals(androidx.compose.ui.text.style.TextOverflow.Clip, override.overflow)
    }

    @Test fun defaultAppearanceDoesNotUseComposeBodyMedium() {
        themes.forEach { theme ->
            val context = context(theme); val native = AppCompatTextView(context)
            val metrics = context.resources.displayMetrics
            val actual = resolveWgText(context, Density(metrics.density), null)
            assertEquals(native.textSize / metrics.scaledDensity, actual.style.fontSize.value, .01f)
            assertEquals(native.letterSpacing, actual.style.letterSpacing.value, .0001f)
            assertEquals(native.includeFontPadding, actual.style.platformStyle!!.paragraphStyle!!.includeFontPadding)
        }
    }

    @Test fun sourceProgressMatchesMdcIncludingInheritedGapStopAndDirection() {
        themes.forEach { theme ->
            val context = context(theme)
            originalLinearProgressSources.forEach { (source, style) ->
                // Source styles are checked independently against pinned XML by the Python guard.
                val attrs = Robolectric.buildAttributeSet().setStyleAttribute("@style/" + context.resources.getResourceEntryName(style)).build()
                val native = LinearProgressIndicator(context, attrs)
                val actual = resolveOriginalLinearProgress(context, source)
                assertEquals(source, native.trackThickness, actual.trackThickness)
                assertEquals(source, native.trackCornerRadius, actual.getTrackCornerRadiusInPx())
                assertEquals(source, native.trackColor, actual.trackColor)
                assertArrayEquals(source, native.indicatorColor, actual.indicatorColors)
                assertEquals(source, native.indicatorTrackGapSize, actual.indicatorTrackGapSize)
                assertEquals(source, native.trackStopIndicatorSize, actual.trackStopIndicatorSize)
                assertEquals(source, native.indicatorDirection, actual.indicatorDirection)
                assertEquals(source, native.indeterminateAnimationType, actual.indeterminateAnimationType)
            }
        }
    }

    @Test fun liveProgressOverridesDoNotReplaceOtherInheritedValues() {
        val context = context()
        val base = resolveOriginalLinearProgress(context, "list_item_statistics_summary:ss_progress")
        val actual = resolveOriginalLinearProgress(context, "list_item_statistics_summary:ss_progress",
            OriginalProgressOverrides(indicator = android.graphics.Color.RED, gapDp = 0f, stopSizeDp = 0f))
        assertArrayEquals(intArrayOf(android.graphics.Color.RED), actual.indicatorColors)
        assertEquals(0, actual.indicatorTrackGapSize); assertEquals(0, actual.trackStopIndicatorSize)
        assertEquals(base.trackThickness, actual.trackThickness); assertEquals(base.trackColor, actual.trackColor)
    }

    @Test fun longRtlTextLargeFontLiveStateAndAccessibleRanges() {
        val context = context()
        val text = WgControlState(context, textSource = "list_item_wg_include_apps:wg_include_app_list_apk_label_tv").apply {
            this.text = "یک نام طولانی برنامه برای بررسی چیدمان و کوتاه‌سازی متن"
        }
        val progress = WgControlState(context).apply {
            progressSource = "list_item_statistics_summary:ss_progress"; this.progress = 25; max = 100
        }
        compose.setContent {
            CompositionLocalProvider(LocalContext provides context, LocalDensity provides Density(1f, 2f),
                LocalLayoutDirection provides LayoutDirection.Rtl) {
                RethinkTheme { Column(Modifier.width(160.dp)) {
                    WgText(text, Modifier.testTag("source-text"))
                    WgProgress(progress, Modifier.testTag("source-progress"))
                } }
            }
        }
        compose.onNodeWithTag("source-text").assertTextEquals(text.text.toString())
        compose.onNodeWithTag("source-progress").assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(.25f, 0f..1f)))
        compose.runOnIdle { progress.setProgress(150, true); text.text = "updated"; text.letterSpacingEm = .03f }
        assertTrue(progress.animateProgress)
        compose.onNodeWithTag("source-text").assertTextEquals("updated")
        compose.onNodeWithTag("source-progress").assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(1f, 0f..1f)))
        compose.runOnIdle { progress.visibility = View.INVISIBLE }
        compose.onNodeWithTag("source-progress").assertDoesNotExist()
        compose.runOnIdle { progress.visibility = View.GONE; text.visibility = View.GONE }
        compose.onNodeWithTag("source-text").assertDoesNotExist()
    }
}
