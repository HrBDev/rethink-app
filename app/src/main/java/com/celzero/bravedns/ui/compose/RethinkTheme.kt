/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose

import androidx.annotation.AttrRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.celzero.bravedns.R
import com.google.android.material.R as MaterialR

/** Uses the host's selected Activity or Dialog theme, including Plus/Frost/alpha. */
@Composable
fun themeColor(@AttrRes attribute: Int): Color {
    LocalConfiguration.current
    val values = LocalContext.current.obtainStyledAttributes(intArrayOf(attribute))
    return try {
        check(values.hasValue(0)) { "Required Rethink theme color is missing: $attribute" }
        Color(values.getColor(0, android.graphics.Color.TRANSPARENT))
    } finally { values.recycle() }
}

@Composable
fun RethinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = themeColor(androidx.appcompat.R.attr.colorPrimary),
            onPrimary = themeColor(MaterialR.attr.colorOnPrimary),
            primaryContainer = themeColor(MaterialR.attr.colorPrimaryContainer),
            onPrimaryContainer = themeColor(MaterialR.attr.colorOnPrimaryContainer),
            secondary = themeColor(MaterialR.attr.colorSecondary),
            onSecondary = themeColor(MaterialR.attr.colorOnSecondary),
            secondaryContainer = themeColor(R.attr.colorSecondaryContainer),
            onSecondaryContainer = themeColor(R.attr.colorOnSecondaryContainer),
            tertiary = themeColor(MaterialR.attr.colorTertiary),
            onTertiary = themeColor(MaterialR.attr.colorOnTertiary),
            tertiaryContainer = themeColor(R.attr.colorTertiaryContainer),
            onTertiaryContainer = themeColor(R.attr.colorOnTertiaryContainer),
            error = themeColor(androidx.appcompat.R.attr.colorError),
            onError = themeColor(MaterialR.attr.colorOnError),
            errorContainer = themeColor(R.attr.colorErrorContainer),
            onErrorContainer = themeColor(R.attr.colorOnErrorContainer),
            background = themeColor(R.attr.background),
            onBackground = themeColor(R.attr.primaryTextColor),
            surface = themeColor(MaterialR.attr.colorSurface),
            onSurface = themeColor(R.attr.colorOnSurface),
            surfaceVariant = themeColor(R.attr.colorSurfaceVariant),
            onSurfaceVariant = themeColor(R.attr.colorOnSurfaceVariant),
            surfaceTint = themeColor(androidx.appcompat.R.attr.colorPrimary),
            inverseSurface = themeColor(MaterialR.attr.colorSurfaceInverse),
            inverseOnSurface = themeColor(MaterialR.attr.colorOnSurfaceInverse),
            inversePrimary = themeColor(MaterialR.attr.colorPrimaryInverse),
            outline = themeColor(R.attr.colorOutline),
            outlineVariant = themeColor(R.attr.border),
            scrim = Color.Black,
            surfaceDim = themeColor(R.attr.colorSurfaceContainerLowest),
            surfaceBright = themeColor(R.attr.colorSurfaceContainerHighest),
            surfaceContainer = themeColor(R.attr.colorSurfaceContainer),
            surfaceContainerLow = themeColor(R.attr.colorSurfaceContainerLow),
            surfaceContainerHigh = themeColor(R.attr.colorSurfaceContainerHigh),
            surfaceContainerHighest = themeColor(R.attr.colorSurfaceContainerHighest),
            surfaceContainerLowest = themeColor(R.attr.colorSurfaceContainerLowest),
        ),
        typography = rethinkTypography,
        content = content,
    )
}

// No fixed line heights: match the branch's OEM-safe TextAppearance.Rethink scale.
private fun textStyle(size: Int, spacing: Float, medium: Boolean = false) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = if (medium) FontWeight.Medium else FontWeight.Normal,
    fontSize = size.sp,
    letterSpacing = spacing.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = true),
)
private val rethinkTypography = Typography(
    displayLarge = textStyle(57, -.25f), displayMedium = textStyle(45, 0f), displaySmall = textStyle(36, 0f),
    headlineLarge = textStyle(32, 0f), headlineMedium = textStyle(28, 0f), headlineSmall = textStyle(24, 0f),
    titleLarge = textStyle(22, 0f, true), titleMedium = textStyle(16, .15f, true), titleSmall = textStyle(14, .1f, true),
    bodyLarge = textStyle(16, .5f), bodyMedium = textStyle(14, .25f), bodySmall = textStyle(12, .4f),
    labelLarge = textStyle(14, .1f, true), labelMedium = textStyle(12, .5f, true), labelSmall = textStyle(11, .5f, true),
)
