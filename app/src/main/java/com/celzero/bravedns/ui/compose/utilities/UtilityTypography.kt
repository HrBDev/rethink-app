/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.utilities
import android.graphics.Typeface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*

/** Resolve the XML TextView default appearance, then explicit TextAppearance and XML overrides. */
@Composable
fun utilityTextStyle(appearance: Int = 0, size: Float? = null, family: String? = null,
                     weight: Int? = null, spacing: Float? = null, lineExtraDp: Float? = null): TextStyle {
    val context = LocalContext.current
    val density = LocalDensity.current
    return remember(context, density, appearance, size, family, weight, spacing, lineExtraDp) {
        val ids = intArrayOf(android.R.attr.textSize, android.R.attr.fontFamily, android.R.attr.textStyle, android.R.attr.letterSpacing, android.R.attr.textFontWeight)
        val defaultAppearance = context.obtainStyledAttributes(null, intArrayOf(android.R.attr.textAppearance), android.R.attr.textViewStyle, 0).let {
            try { it.getResourceId(0, 0) } finally { it.recycle() }
        }
        val inherited = context.obtainStyledAttributes(defaultAppearance, ids)
        val widget = context.obtainStyledAttributes(null, ids, android.R.attr.textViewStyle, 0)
        val explicit = if (appearance != 0) context.obtainStyledAttributes(appearance, ids) else null
        fun values(index: Int) = when { explicit?.hasValue(index) == true -> explicit; widget.hasValue(index) -> widget; else -> inherited }
        val textSize = size ?: with(density) { values(0).getDimension(0, 14f * density.density * density.fontScale).toSp().value }
        val name = family ?: values(1).getString(1) ?: "sans-serif"
        val flags = weight ?: values(2).getInt(2, Typeface.NORMAL)
        val letter = spacing ?: values(3).getFloat(3, 0f)
        val fontWeight = if (weight != null) (if (weight and Typeface.BOLD != 0) 700 else 400) else values(4).getInt(4, -1)
        var typeface = Typeface.create(name, flags)
        if (android.os.Build.VERSION.SDK_INT >= 28 && fontWeight in 1..1000) typeface = Typeface.create(typeface, fontWeight, flags and Typeface.ITALIC != 0)
        val resolvedWeight = if (android.os.Build.VERSION.SDK_INT >= 28) typeface.weight else if (flags and Typeface.BOLD != 0) 700 else if (name.contains("medium")) 500 else 400
        explicit?.recycle(); widget.recycle(); inherited.recycle()
        val lineHeight = if (lineExtraDp == null) TextUnit.Unspecified else {
            val paint = android.graphics.Paint().apply { this.typeface = typeface; this.textSize = textSize * density.density * density.fontScale }
            with(density) { (paint.fontSpacing + lineExtraDp.dp.toPx()).toSp() }
        }
        TextStyle(fontSize = textSize.sp, fontFamily = FontFamily(typeface), fontWeight = FontWeight(resolvedWeight),
            fontStyle = if (flags and Typeface.ITALIC != 0) FontStyle.Italic else FontStyle.Normal,
            letterSpacing = letter.em, lineHeight = lineHeight, platformStyle = PlatformTextStyle(includeFontPadding = true))
    }
}
fun resolveUtilityStyle(context: android.content.Context, attribute: Int): Int {
    val value = android.util.TypedValue()
    return if (context.theme.resolveAttribute(attribute, value, true)) value.resourceId else 0
}
