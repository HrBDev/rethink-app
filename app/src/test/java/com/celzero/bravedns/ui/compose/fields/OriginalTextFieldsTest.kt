/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.fields

import android.app.Application
import android.content.res.ColorStateList
import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Authored synthetic resource/Compose tests. No production Application, controller or VPN.
 * Execution requires the project's Android/Robolectric toolchain; source audits do not run these.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h800dp")
class OriginalTextFieldsTest {
    @get:Rule val compose = createComposeRule()
    private val themes = listOf(R.style.AppThemeWhite, R.style.AppTheme, R.style.AppThemeTrueBlack,
        R.style.AppThemeWhitePlus, R.style.AppThemeTrueBlackPlus, R.style.AppThemeTrueBlackFrost)

    @Test fun xmlFamiliesResolveDistinctModesShapesAndPaddingInEveryTheme() {
        themes.forEach { theme ->
            val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), theme)
            val density = context.resources.displayMetrics.density
            val lan = resolveFieldResources(context, OriginalFieldStyle.Lan)
            val inline = resolveFieldResources(context, OriginalFieldStyle.WgInlineSsid)
            val notes = resolveFieldResources(context, OriginalFieldStyle.Notes)
            val tasker = resolveFieldResources(context, OriginalFieldStyle.Tasker)
            assertEquals(0, lan.mode); assertEquals(0, notes.mode)
            assertNotNull(lan.background)
            val nativeNotes = context.obtainStyledAttributes(null, intArrayOf(android.R.attr.background), android.R.attr.editTextStyle, 0)
            val notesHasBackground = try { nativeNotes.getDrawable(0) != null } finally { nativeNotes.recycle() }
            assertEquals(notesHasBackground, notes.background != null)
            assertEquals((10 * density).toInt(), lan.padding.left)
            assertEquals((8 * density).toInt(), inline.padding.left)
            assertEquals(android.graphics.Rect(50, 40, 50, 40), tasker.padding)
            assertNull(resolveFieldResources(context, OriginalFieldStyle.TransparentSearch).background)
            listOf(OriginalFieldStyle.WgEditor to 8f, OriginalFieldStyle.Rule to 8f,
                OriginalFieldStyle.Support to 12f, OriginalFieldStyle.Ping to 16f).forEach { (style, radius) ->
                val resources = resolveFieldResources(context, style)
                assertEquals(2, resources.mode)
                assertEquals(radius, resources.shape.topStart.toPx(Size(300f, 56f), Density(1f)), .01f)
                assertEquals(radius, resources.shape.bottomEnd.toPx(Size(300f, 56f), Density(1f)), .01f)
                assertTrue(resources.focusedStrokeWidth >= resources.strokeWidth)
            }
            // DNS and country SSID inherit the selected Android style instead of forcing outline.
            val attr = context.obtainStyledAttributes(intArrayOf(com.google.android.material.R.attr.textInputStyle))
            val styleId = try { attr.getResourceId(0, 0) } finally { attr.recycle() }
            val box = context.obtainStyledAttributes(styleId, intArrayOf(com.google.android.material.R.attr.boxBackgroundMode))
            val expectedMode = try { box.getInt(0, 1) } finally { box.recycle() }
            assertEquals(expectedMode, resolveFieldResources(context, OriginalFieldStyle.Dns).mode)
            assertEquals(expectedMode, resolveFieldResources(context, OriginalFieldStyle.RpnSsid).mode)
        }
    }

    @Test fun stateSelectorsKeepDisabledFocusedAndUnfocusedColorsSeparate() {
        val selector = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_focused), intArrayOf()),
            intArrayOf(android.graphics.Color.GRAY, android.graphics.Color.RED, android.graphics.Color.BLUE))
        assertEquals(Color.Gray, selector.fieldColor(false, false))
        assertEquals(Color.Gray, selector.fieldColor(false, true))
        assertEquals(Color.Red, selector.fieldColor(true, true))
        assertEquals(Color.Blue, selector.fieldColor(true, false))
    }

    @Test fun resourceRestylingDoesNotDispatchUntilAnEditAndDisabledFieldRemainsDisabled() {
        var value by mutableStateOf("synthetic")
        var changes = 0
        render {
            Column {
                OriginalTextField(value, { value = it; changes++ }, Modifier.testTag("editable"),
                    style = OriginalFieldStyle.DnsProxy, singleLine = true)
                OriginalTextField("disabled", { changes++ }, Modifier.testTag("disabled"),
                    style = OriginalFieldStyle.Lan, enabled = false)
            }
        }
        compose.runOnIdle { assertEquals(0, changes) }
        compose.onNodeWithTag("disabled").assertIsNotEnabled()
        compose.onNodeWithTag("editable").assert(SemanticsMatcher.expectValue(OriginalFieldFamily, "DnsProxy"))
            .performTextReplacement("changed")
        compose.runOnIdle { assertEquals("changed", value); assertEquals(1, changes) }
    }

    @Test fun passwordAndDoneSemanticsSurviveOutlinedRestyling() {
        var done = 0
        render {
            OriginalTextField("synthetic-secret", {}, Modifier.testTag("password"), style = OriginalFieldStyle.WgEditor,
                visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { done++ }))
        }
        compose.onNodeWithTag("password").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
            .performImeAction()
        compose.runOnIdle { assertEquals(1, done) }
    }

    @Test fun textFieldValueSelectionIsNotResetDuringComposition() {
        val initial = TextFieldValue("Synthetic note", TextRange(2, 6))
        var value by mutableStateOf(initial)
        var changes = 0
        render {
            OriginalTextField(value, { value = it; changes++ }, Modifier.testTag("notes"),
                style = OriginalFieldStyle.Notes, minLines = 4, maxLines = 10)
        }
        compose.runOnIdle { assertEquals(initial, value); assertEquals(0, changes) }
        compose.onNodeWithTag("notes").assert(SemanticsMatcher.expectValue(SemanticsProperties.TextSelectionRange, TextRange(2, 6)))
    }

    @Test fun allFamiliesRemainEditableAndReachableAtLargeRtlFont() {
        val families = OriginalFieldStyle.entries.filter { it != OriginalFieldStyle.Network }
        render(largeRtl = true) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                families.forEach { style ->
                    OriginalTextField("Synthetic text", {}, Modifier.fillMaxWidth().testTag(style.name), style = style,
                        label = { Text("Synthetic label") }, minLines = 1, maxLines = 4)
                }
            }
        }
        families.forEach { style ->
            compose.onNodeWithTag(style.name).performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(if (style == OriginalFieldStyle.ServerSearch) 40.dp else 48.dp)
                .assert(SemanticsMatcher.expectValue(OriginalFieldFamily, style.name))
                .assert(hasSetTextAction())
        }
    }

    @Test fun errorCursorAndAccessibilityUseTheExistingValidationState() {
        assertEquals(Color.Red, fieldCursorColor(Color.Blue, Color.Red, true))
        assertEquals(Color.Blue, fieldCursorColor(Color.Blue, Color.Red, false))
        render {
            OriginalTextField("invalid", {}, Modifier.testTag("error"), style = OriginalFieldStyle.Ping,
                isError = true, errorMessage = "Synthetic validation message")
        }
        compose.onNodeWithTag("error").assert(SemanticsMatcher.expectValue(SemanticsProperties.Error, "Synthetic validation message"))
    }

    @Test fun spinnerRemainsAnAccessibleSelectorAndDispatchesOnlyOnClick() {
        var clicks = 0
        render {
            Column {
                OriginalSpinner("Synthetic app", { clicks++ }, Modifier.testTag("spinner"))
                OriginalSpinner("Disabled app", { clicks++ }, Modifier.testTag("disabled-spinner"), enabled = false)
            }
        }
        compose.runOnIdle { assertEquals(0, clicks) }
        compose.onNodeWithTag("disabled-spinner").assertIsNotEnabled()
        compose.onNodeWithTag("spinner").assert(SemanticsMatcher.expectValue(OriginalFieldFamily, "Spinner"))
            .assertHeightIsAtLeast(48.dp).performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }

    private fun render(largeRtl: Boolean = false, block: @Composable () -> Unit) {
        compose.setContent {
            val base = LocalContext.current
            val context = remember(base) { ContextThemeWrapper(base, R.style.AppThemeWhite) }
            val density = LocalDensity.current
            CompositionLocalProvider(LocalContext provides context,
                LocalDensity provides if (largeRtl) Density(density.density, 2f) else density,
                LocalLayoutDirection provides if (largeRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                RethinkTheme(block)
            }
        }
    }
}
