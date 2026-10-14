/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.controls

import android.annotation.SuppressLint
import android.app.Application
import android.content.res.ColorStateList
import android.view.ContextThemeWrapper
import android.widget.CompoundButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.slider.Slider
import com.google.android.material.tooltip.TooltipDrawable
import com.google.android.material.R as MaterialR
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Authored, NOT RUN: Android/Robolectric execution remains blocked. A plain Application and
 * synthetic callbacks isolate these tests from real controllers, storage, networking and services. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class OriginalCompoundControlsTest {
    @get:Rule val compose = createComposeRule()
    private val themes = listOf(R.style.AppTheme, R.style.AppThemeWhite, R.style.AppThemeTrueBlack,
        R.style.AppThemeWhitePlus, R.style.AppThemeTrueBlackPlus, R.style.AppThemeTrueBlackFrost)
    private fun context(theme: Int = R.style.AppThemeWhite) =
        ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), theme)
    private val states = listOf(
        originalCompoundStates(true, ToggleableState.Off), originalCompoundStates(true, ToggleableState.On),
        originalCompoundStates(false, ToggleableState.Off), originalCompoundStates(false, ToggleableState.On),
        originalCompoundStates(true, ToggleableState.Indeterminate),
        originalCompoundStates(true, ToggleableState.On, pressed = true),
        originalCompoundStates(true, ToggleableState.On, focused = true))
    private fun sameColors(label: String, expected: ColorStateList?, actual: ColorStateList?) {
        states.forEach { state ->
            assertEquals("$label state=${state.toList()}", expected?.getColorForState(state, expected.defaultColor),
                actual?.getColorForState(state, actual.defaultColor))
        }
    }

    @Test fun all154SourceStylesResolveAcrossSixThemes() {
        assertEquals(154, originalCompoundSpecs.size)
        themes.forEach { theme -> originalCompoundSpecs.forEach { (key, spec) ->
            if (spec.kind == OriginalCompoundKind.Slider || spec.kind == OriginalCompoundKind.SeekBar) {
                val look = resolveOriginalSlider(context(theme), spec)
                assertTrue("$key thumb", look.thumbWidth > 0f && look.thumbHeight > 0f)
                assertTrue("$key track", look.trackHeight > 0f)
            } else {
                val look = resolveOriginalCompound(context(theme), spec)
                assertNotNull("$key button", look.button)
                assertTrue("$key dimensions", look.width > 0f && look.height > 0f)
            }
        } }
    }

    /** Independent reference: real widget constructors, not values copied from this resolver.
     * This specifically detects framework/app tint precedence and lost Material3 icon tint. */
    @Test fun tintLayersAndDrawableDimensionsMatchNativeConstructors() {
        themes.forEach { theme -> originalCompoundSpecs.forEach specLoop@ { (key, spec) ->
            if (spec.kind == OriginalCompoundKind.Slider || spec.kind == OriginalCompoundKind.SeekBar) return@specLoop
            val c = context(theme)
            val attrs = Robolectric.buildAttributeSet().setStyleAttribute("@style/" + c.resources.getResourceEntryName(spec.style)).build()
            val native: CompoundButton = when (spec.kind) {
                OriginalCompoundKind.MaterialSwitch -> MaterialSwitch(c, attrs)
                OriginalCompoundKind.MaterialCheckBox -> MaterialCheckBox(c, attrs)
                OriginalCompoundKind.MaterialRadioButton -> MaterialRadioButton(c, attrs)
                OriginalCompoundKind.AppCompatRadioButton -> AppCompatRadioButton(c, attrs)
                else -> AppCompatCheckBox(c, attrs)
            }
            val look = resolveOriginalCompound(c, spec)
            val density = c.resources.displayMetrics.density
            assertEquals("$key source background", native.background?.javaClass, look.background?.javaClass)
            if (native is MaterialSwitch) {
                sameColors("$key thumb", native.thumbTintList, look.buttonTint)
                sameColors("$key track", native.trackTintList, look.trackTint)
                sameColors("$key decoration", native.trackDecorationTintList, look.trackDecorationTint)
                assertEquals("$key thumb width", native.thumbDrawable.intrinsicWidth / density, look.thumbWidth, .01f)
                assertEquals("$key track width", native.trackDrawable.intrinsicWidth / density, look.width, .01f)
            } else if (spec.buttonCleared) {
                assertNull("$key cleared", native.buttonDrawable)
                assertEquals("$key bottom drawable", native.compoundDrawables[3].intrinsicWidth / density, look.width, .01f)
                assertNull("$key bottom drawable must retain its own tint", look.buttonTint)
            } else {
                sameColors("$key button", native.buttonTintList, look.buttonTint)
                assertEquals("$key width", native.buttonDrawable.intrinsicWidth / density, look.width, .01f)
                if (native is MaterialCheckBox) sameColors("$key icon", native.buttonIconTintList, look.buttonIconTint)
            }
        } }
    }

    @Test fun sponsorInheritedHandleDimensionsAndGapOverrideRadiusFallback() {
        themes.forEach { theme ->
            val c = context(theme)
            val spec = originalCompoundSpecs.getValue("fragment_sponsor:contribution_seekbar")
            val attrs = Robolectric.buildAttributeSet().setStyleAttribute("@style/" + c.resources.getResourceEntryName(spec.style)).build()
            val native = Slider(c, attrs)
            val look = resolveOriginalSlider(c, spec)
            val d = c.resources.displayMetrics.density
            assertEquals(native.thumbWidth / d, look.thumbWidth, .01f)
            assertEquals(native.thumbHeight / d, look.thumbHeight, .01f)
            assertEquals(native.thumbElevation / d, look.thumbElevation, .01f)
            assertEquals(2f, look.thumbElevation, .01f)
            assertEquals(native.thumbTrackGapSize / d, look.thumbTrackGap, .01f)
            assertEquals(native.trackStopIndicatorSize / d, look.stopIndicatorSize, .01f)
            assertEquals(native.trackInsideCornerSize / d, look.trackInsideCorner, .01f)
            assertEquals(native.trackHeight / d, look.trackHeight, .01f)
            assertEquals(native.haloRadius / d, look.haloRadius, .01f)
            sameColors("Sponsor thumb", native.thumbTintList, look.thumbTint)
            sameColors("Sponsor track", native.trackActiveTintList, look.activeTint)
        }
    }

    @SuppressLint("RestrictedApi") // Reference oracle only; production never uses TooltipDrawable.
    @Test fun sponsorFloatingLabelMatchesSourceTooltipGeometryAndColors() {
        themes.forEach { theme ->
            val c = context(theme)
            val spec = originalCompoundSpecs.getValue("fragment_sponsor:contribution_seekbar")
            val a = c.obtainStyledAttributes(spec.style, intArrayOf(MaterialR.attr.labelStyle))
            val native = TooltipDrawable.createFromAttributes(c, null, 0, a.getResourceId(0, 0))
            a.recycle()
            val actual = requireNotNull(resolveOriginalSlider(c, spec).label)
            val text = "$25 / month"
            native.text = text
            assertEquals(native.intrinsicWidth, maxOf(actual.minWidth, actual.padding * 2 + actual.paint.measureText(text)).toInt())
            assertEquals(native.intrinsicHeight, maxOf(actual.minHeight, actual.paint.textSize).toInt())
            sameColors("Sponsor label background", native.fillColor, actual.background.fillColor)
            assertEquals(native.layoutMargin.toFloat(), actual.windowMargin, .01f)
        }
    }

    @Test fun switchReleaseThresholdAndFlingDirectionMatchSwitchCompat() {
        assertFalse(originalSwitchDragResult(.5f, 0f, false, 50f))
        assertTrue(originalSwitchDragResult(.51f, 0f, false, 50f))
        assertTrue(originalSwitchDragResult(.1f, 51f, false, 50f))
        assertFalse(originalSwitchDragResult(.9f, -51f, false, 50f))
        assertFalse(originalSwitchDragResult(.9f, 51f, true, 50f))
        assertTrue(originalSwitchDragResult(.1f, -51f, true, 50f))
        assertFalse(originalSwitchDragResult(.1f, 50f, false, 50f))
    }

    @Test fun checkboxTapsAndSwitchTapsEmitOnceAndDisabledControlsAreSilent() {
        var checks = 0; var switches = 0; var disabled = 0
        compose.setContent { CompositionLocalProvider(LocalContext provides context()) { RethinkTheme { Column {
            var check by remember { mutableStateOf(false) }; var switch by remember { mutableStateOf(false) }
            OriginalCheckbox("dialog_wg_apps:wg_include_app_bulk_check", check, { check = it; checks++ }, Modifier.testTag("check"))
            OriginalSwitch("activity_advanced_setting:dv_experimental_switch", switch, { switch = it; switches++ }, Modifier.testTag("switch"))
            OriginalSwitch("activity_advanced_setting:dv_experimental_switch", false, { disabled++ }, Modifier.testTag("disabled"), enabled = false)
            OriginalRadioButton("dialog_wg_ssid:radioEqual", false, null, Modifier.testTag("indicator"), indicatorOnly = true)
        } } } }
        compose.onNodeWithTag("check").performClick().assertIsOn()
        compose.onNodeWithTag("switch").performClick().assertIsOn()
        compose.onNodeWithTag("disabled").assertIsNotEnabled().performTouchInput { click() }
        compose.onNodeWithTag("indicator").assertHasNoClickAction()
        compose.runOnIdle { assertEquals(1, checks); assertEquals(1, switches); assertEquals(0, disabled) }
    }

    @Test fun switchDragEmitsOnceInEitherDirectionAndCancellationEmitsNothing() {
        var checked by mutableStateOf(false)
        var rtl by mutableStateOf(false)
        var calls = 0
        val c = context()
        val look = resolveOriginalCompound(c, originalCompoundSpecs.getValue("activity_advanced_setting:dv_experimental_switch"))
        val travel = (look.width - look.thumbWidth) * c.resources.displayMetrics.density
        compose.setContent { CompositionLocalProvider(LocalContext provides c,
            LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) { RethinkTheme {
            OriginalSwitch("activity_advanced_setting:dv_experimental_switch", checked, { checked = it; calls++ }, Modifier.testTag("drag"))
        } } }
        val node = compose.onNodeWithTag("drag")
        node.performTouchInput { swipe(Offset(center.x - travel / 2f, center.y), Offset(center.x + travel / 2f, center.y), 300) }
        node.assertIsOn()
        compose.runOnIdle { assertEquals(1, calls); checked = false; rtl = true }
        compose.mainClock.advanceTimeBy(300)
        node.performTouchInput { swipe(Offset(center.x + travel / 2f, center.y), Offset(center.x - travel / 2f, center.y), 300) }
        node.assertIsOn()
        compose.runOnIdle { assertEquals(2, calls); checked = false; rtl = false }
        compose.mainClock.advanceTimeBy(300)
        node.performTouchInput {
            down(Offset(center.x - travel / 2f, center.y))
            moveTo(Offset(center.x + travel / 2f, center.y), 100)
            cancel()
        }
        node.assertIsOff()
        compose.runOnIdle { assertEquals(2, calls) }
    }

    @Test fun sliderDragRetainsActiveVisualsAfterThePressIsCancelled() {
        val interactions = MutableInteractionSource()
        var active = false
        val press = PressInteraction.Press(Offset.Zero)
        val drag = DragInteraction.Start()
        compose.setContent { active = originalSliderActiveInteraction(interactions) }
        compose.runOnIdle { assertFalse(active); assertTrue(interactions.tryEmit(press)) }
        compose.runOnIdle { assertTrue(active); assertTrue(interactions.tryEmit(drag)); assertTrue(interactions.tryEmit(PressInteraction.Cancel(press))) }
        compose.runOnIdle { assertTrue(active); assertTrue(interactions.tryEmit(DragInteraction.Stop(drag))) }
        compose.runOnIdle { assertFalse(active) }
    }

    @Test fun slidersKeepRangeSemanticsAndChangedValueCallback() {
        var value by mutableFloatStateOf(0f)
        var changes = 0
        compose.setContent { CompositionLocalProvider(LocalContext provides context()) { RethinkTheme {
            OriginalSlider("fragment_sponsor:contribution_seekbar", value, { value = it; changes++ },
                Modifier.width(240.dp).testTag("slider"), valueRange = 0f..6f, steps = 5)
        } } }
        compose.onNodeWithTag("slider").performSemanticsAction(SemanticsActions.SetProgress) { assertTrue(it(3f)) }
        compose.runOnIdle { assertEquals(3f, value, 0f); assertEquals(1, changes) }
        compose.onNodeWithTag("slider").performSemanticsAction(SemanticsActions.SetProgress) { assertFalse(it(3f)) }
        compose.runOnIdle { assertEquals(1, changes) }
    }

    @Test fun clearingAnOverrideRecreatesUntaintedDrawables() {
        val c = context()
        val spec = originalCompoundSpecs.getValue("dialog_wg_apps:wg_include_app_bulk_check")
        val template = resolveOriginalCompound(c, spec)
        val overridden = resolveOriginalCompound(c, spec)
        overridden.button!!.setTintList(ColorStateList.valueOf(android.graphics.Color.MAGENTA))
        val restored = resolveOriginalCompound(c, spec)
        assertNotSame(template.button, overridden.button)
        assertNotSame(overridden.button, restored.button)
        sameColors("restored base", template.buttonTint, restored.buttonTint)
        sameColors("independent icon", template.buttonIconTint, restored.buttonIconTint)
    }
}
