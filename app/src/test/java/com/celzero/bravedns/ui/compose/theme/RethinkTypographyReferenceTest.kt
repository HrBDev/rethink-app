/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.theme

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.celzero.bravedns.ui.compose.utilities.resolveUtilityStyle
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.google.android.material.R as MaterialR

/** Offscreen TextView references and native fake text only; no app host, store or service. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35], qualifiers = "w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RethinkTypographyReferenceTest {
    @get:Rule val compose = createComposeRule()
    private val attrs = listOf(MaterialR.attr.textAppearanceDisplayLarge, MaterialR.attr.textAppearanceDisplayMedium,
        MaterialR.attr.textAppearanceDisplaySmall, MaterialR.attr.textAppearanceHeadlineLarge,
        MaterialR.attr.textAppearanceHeadlineMedium, MaterialR.attr.textAppearanceHeadlineSmall,
        MaterialR.attr.textAppearanceTitleLarge, MaterialR.attr.textAppearanceTitleMedium, MaterialR.attr.textAppearanceTitleSmall,
        MaterialR.attr.textAppearanceBodyLarge, MaterialR.attr.textAppearanceBodyMedium, MaterialR.attr.textAppearanceBodySmall,
        MaterialR.attr.textAppearanceLabelLarge, MaterialR.attr.textAppearanceLabelMedium, MaterialR.attr.textAppearanceLabelSmall)

    @Test fun allFifteenStylesMatchExistingAppearancesAcrossThemesAndNonlinearFontScaling() {
        var theme by mutableIntStateOf(R.style.AppThemeWhite)
        var scale by mutableFloatStateOf(1f)
        lateinit var context: Context
        compose.setContent {
            val base = LocalContext.current
            val themed = remember(base, theme, scale) { ContextThemeWrapper(base, theme).apply {
                applyOverrideConfiguration(Configuration(base.resources.configuration).apply { fontScale = scale })
            } }
            context = themed
            CompositionLocalProvider(LocalContext provides themed, LocalDensity provides Density(themed)) {
                RethinkTheme {
                    val t = MaterialTheme.typography
                    val styles = listOf(t.displayLarge,t.displayMedium,t.displaySmall,t.headlineLarge,t.headlineMedium,t.headlineSmall,
                        t.titleLarge,t.titleMedium,t.titleSmall,t.bodyLarge,t.bodyMedium,t.bodySmall,t.labelLarge,t.labelMedium,t.labelSmall)
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        styles.forEachIndexed { index, style -> Text("Synthetic Abc", Modifier.testTag("typography_$index"), style = style) }
                    }
                }
            }
        }
        for (styleTheme in listOf(R.style.AppThemeWhite, R.style.AppTheme, R.style.AppThemeTrueBlack,
            R.style.AppThemeWhitePlus, R.style.AppThemeTrueBlackPlus, R.style.AppThemeTrueBlackFrost)) {
            for (fontScale in listOf(1f, 2f)) {
                compose.runOnIdle { theme = styleTheme; scale = fontScale }
                attrs.forEachIndexed { index, attr ->
                    lateinit var result: TextLayoutResult
                    compose.onNodeWithTag("typography_$index").performScrollTo().performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
                        val results = mutableListOf<TextLayoutResult>(); assertTrue(action(results)); result = results.single()
                    }
                    compose.runOnIdle {
                        val reference = AppCompatTextView(context).apply { setTextAppearance(resolveUtilityStyle(context, attr)); text = "Synthetic Abc" }
                        val native = result.layoutInput.style; val density = result.layoutInput.density
                        val nativeSize = with(density) { native.fontSize.toPx() }
                        // TextAppearance rounds its dimension to integer pixels; Compose retains subpixels.
                        // A half-pixel difference is cosmetic and is not an acceptance regression.
                        assertEquals("size theme=$styleTheme scale=$fontScale style=$index", reference.textSize, nativeSize, .51f)
                        val spacing = if (native.letterSpacing.isEm) native.letterSpacing.value * nativeSize else with(density) { native.letterSpacing.toPx() }
                        assertEquals("spacing theme=$styleTheme scale=$fontScale style=$index", reference.letterSpacing * reference.textSize, spacing, .05f)
                        assertEquals("weight theme=$styleTheme scale=$fontScale style=$index", checkNotNull(reference.typeface).weight, native.fontWeight!!.weight)
                    }
                }
            }
        }
    }
}
