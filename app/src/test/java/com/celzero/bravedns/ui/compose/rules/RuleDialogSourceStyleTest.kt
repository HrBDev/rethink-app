/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.rules

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.reference.RuleDialogReferenceFactories
import com.celzero.bravedns.service.DomainRulesManager
import com.celzero.bravedns.service.IpRulesManager.IpRuleStatus
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Authored UNRUN: plain Application, synthetic presentation and independent programmatic native-widget oracles only.
 * Does not construct production hosts, managers, storage, VPN, billing or network traffic. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h800dp")
class RuleDialogSourceStyleTest {
    @get:Rule val compose = createComposeRule()

    @Test fun sourceFrameStaysTransparentInsideAndMatchesBaselineFactoryForEveryTheme() {
        val themes = listOf(R.style.AppThemeWhite, R.style.AppTheme, R.style.AppThemeTrueBlack,
            R.style.AppThemeWhitePlus, R.style.AppThemeTrueBlackPlus, R.style.AppThemeTrueBlackFrost)
        themes.forEach { theme ->
            val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), theme)
            val badge = RuleDialogReferenceFactories.listBadge(context)
            listOf(AndroidColor.RED, AndroidColor.GREEN, AndroidColor.GRAY).forEach { tint ->
                val reference = badge.background.constantState!!.newDrawable(context.resources).mutate().apply { setTint(tint) }
                val actual = pixels(ruleStatusBadgeFrame(context, tint))
                assertArrayEquals(pixels(reference), actual)
                assertEquals("The source drawable has a stroke, no solid fill", 0, AndroidColor.alpha(actual[16 * 32 + 16]))
                assertTrue(actual.any { AndroidColor.alpha(it) != 0 })
            }
        }
    }

    @Test fun listAndDetailBadgesRetainSeparateMetricsAtLargeRtlFont() {
        compose.setContent {
            val context = ContextThemeWrapper(LocalContext.current, R.style.AppThemeWhite)
            val density = LocalDensity.current
            CompositionLocalProvider(LocalContext provides context, LocalDensity provides Density(density.density, 2f),
                LocalLayoutDirection provides LayoutDirection.Rtl) {
                RethinkTheme {
                    Column {
                        RuleStatusBadge(true, DomainRulesManager.Status.BLOCK.id, modifier = Modifier.testTag("list"))
                        RuleStatusBadge(true, DomainRulesManager.Status.BLOCK.id, detail = true, modifier = Modifier.testTag("detail"))
                        RuleStatusBadge(false, IpRuleStatus.BYPASS_UNIVERSAL.id, active = false, detail = true, modifier = Modifier.testTag("bypass"))
                    }
                }
            }
        }
        compose.onNodeWithTag("list").assertWidthIsAtLeast(25.dp)
        for (tag in listOf("detail", "bypass")) {
            compose.onNodeWithTag(tag).assertWidthIsEqualTo(32.dp).assertHeightIsEqualTo(32.dp)
            assertEquals(FontWeight.Bold, descendantText(tag).layoutInput.style.fontWeight)
        }
        assertEquals(12f, descendantText("list").layoutInput.style.fontSize.value, 0f)
        assertEquals(12f, descendantText("detail").layoutInput.style.fontSize.value, 0f)
        assertEquals(11f, descendantText("bypass").layoutInput.style.fontSize.value, 0f)
    }

    @Test fun editorHasFourteenElevenTwelveSpAndDismissDoesNotSubmit() {
        val state = RulesPageState().apply { openEditor(); error = R.string.ci_dialog_error_invalid_ip }
        var submissions = 0
        compose.setContent {
            val context = ContextThemeWrapper(LocalContext.current, R.style.AppThemeWhite)
            CompositionLocalProvider(LocalContext provides context) {
                RethinkTheme { if (state.editorOpen) RuleEditor(false, 42, state) { submissions++ } }
            }
        }
        assertEquals(14f, textByTag("rule_editor_title").layoutInput.style.fontSize.value, 0f)
        assertEquals(11f, textByTag("rule_editor_description").layoutInput.style.fontSize.value, 0f)
        assertEquals(12f, textByTag("rule_error").layoutInput.style.fontSize.value, 0f)
        compose.onNodeWithTag("rule_editor_done").performScrollTo().performClick()
        compose.runOnIdle { assertFalse(state.editorOpen); assertEquals(0, submissions) }
    }

    private fun descendantText(tag: String): TextLayoutResult {
        val result = mutableListOf<TextLayoutResult>()
        compose.onNode(hasAnyAncestor(hasTestTag(tag)) and SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult), useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(result)) }
        return result.single()
    }
    private fun textByTag(tag: String): TextLayoutResult {
        val result = mutableListOf<TextLayoutResult>()
        compose.onNodeWithTag(tag).performScrollTo().performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(result)) }
        return result.single()
    }
    private fun pixels(drawable: Drawable): IntArray {
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, 32, 32); drawable.draw(Canvas(bitmap))
        return IntArray(32 * 32).also { bitmap.getPixels(it, 0, 32, 0, 0, 32, 32); bitmap.recycle() }
    }
}
