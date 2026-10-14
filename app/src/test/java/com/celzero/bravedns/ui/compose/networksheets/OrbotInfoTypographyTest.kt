/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.networksheets

import android.app.Application
import android.content.res.Configuration
import android.text.TextPaint
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.TextView
import com.celzero.bravedns.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Offscreen baseline XML TextViews only. No production Application, Activity or controller. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class OrbotInfoTypographyTest {
    @Test fun actualXmlTypographyMatchesAtNormalAndLargeScalesAcrossThemes() {
        val app = RuntimeEnvironment.getApplication<Application>()
        for (theme in listOf(R.style.AppThemeWhite, R.style.AppTheme, R.style.AppThemeTrueBlackFrost)) {
            for (scale in listOf(1f, 2f)) {
                val config = Configuration(app.resources.configuration).apply { fontScale = scale }
                val context = ContextThemeWrapper(app.createConfigurationContext(config), theme)
                val baseline = LayoutInflater.from(context).inflate(R.layout.dialog_info_rules_layout, null, false)
                for ((description, id) in listOf(false to R.id.info_rules_dialog_rules_title, true to R.id.info_rules_dialog_rules_desc)) {
                    val view = baseline.findViewById<TextView>(id)
                    val typography = resolveOrbotInfoTypography(context, description)
                    val label = "theme=$theme scale=$scale description=$description"
                    assertEquals(label, view.textSize, typography.sizePx, .01f)
                    assertEquals(label, view.letterSpacing, typography.letterSpacing, .0001f)
                    assertEquals(label, view.currentTextColor, typography.color)
                    assertEquals(label, view.typeface.style, typography.typeface.style)
                    val paint = TextPaint().apply { textSize = typography.sizePx; typeface = typography.typeface; letterSpacing = typography.letterSpacing }
                    val sample = "Orbot WWW iii 0123"
                    assertEquals(label, view.paint.measureText(sample), paint.measureText(sample), .1f)
                    assertEquals(label, view.paint.fontSpacing, paint.fontSpacing, .1f)
                    assertTrue(label, view.includeFontPadding)
                    if (description) {
                        assertEquals(5f * context.resources.displayMetrics.density, view.lineSpacingExtra, .01f)
                        assertTrue(view.isTextSelectable)
                    }
                }
            }
        }
    }
    @Test fun infoTextPreservesUnderlineItalicAndParagraphTextWithoutMakingLinksActions() {
        val styled = androidx.core.text.HtmlCompat.fromHtml("A <u>secure</u> <i>included</i><br /><br />B", androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY)
        val native = orbotInfoText(styled)
        assertEquals(styled.toString(), native.text)
        assertTrue(native.spanStyles.any { it.item.textDecoration == androidx.compose.ui.text.style.TextDecoration.Underline })
        assertTrue(native.spanStyles.any { it.item.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic })
    }
}
