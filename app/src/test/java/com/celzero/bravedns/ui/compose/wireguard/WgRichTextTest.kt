/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.wireguard

import android.app.Application
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Text conversion only: no application controller, I/O, repository or platform action. */
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class WgRichTextTest {
    @Test fun nullAndPlainTextRemainLiteral() {
        assertEquals("", wgAnnotatedText(null).text)
        assertEquals("<literal>", wgAnnotatedText("<literal>").text)
    }
    @Test fun boldItalicPreservesBothFlagsAndRange() {
        val source = SpannableString("fake text").apply { setSpan(StyleSpan(Typeface.BOLD_ITALIC), 1, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
        val range = wgAnnotatedText(source).spanStyles.single()
        assertEquals(1, range.start); assertEquals(5, range.end)
        assertEquals(FontWeight.Bold, range.item.fontWeight)
        assertEquals(FontStyle.Italic, range.item.fontStyle)
    }
    @Test fun explicitColoursAndDecorationsSurvive() {
        val source = SpannableString("synthetic").apply {
            setSpan(ForegroundColorSpan(0xff123456.toInt()), 0, 3, 0)
            setSpan(BackgroundColorSpan(0xfffedcba.toInt()), 2, 6, 0)
            setSpan(UnderlineSpan(), 0, 4, 0)
            setSpan(StrikethroughSpan(), 4, 8, 0)
        }
        val styles = wgAnnotatedText(source).spanStyles
        assertTrue(styles.any { it.start == 0 && it.end == 3 && it.item.color == Color(0xff123456) })
        assertTrue(styles.any { it.start == 2 && it.end == 6 && it.item.background == Color(0xfffedcba) })
        assertTrue(styles.any { it.item.textDecoration == TextDecoration.Underline })
        assertTrue(styles.any { it.item.textDecoration == TextDecoration.LineThrough })
    }
}
