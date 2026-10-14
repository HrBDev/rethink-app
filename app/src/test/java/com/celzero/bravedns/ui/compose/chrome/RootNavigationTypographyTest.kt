/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.chrome

import android.app.Application
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.unit.Density
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.R as MaterialR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Harmless widget/style references only. No Activity, Fragment, navigation or service startup. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RootNavigationTypographyTest {
    @Test fun labelsMatchPinnedWidgetFaceSizeSpacingAndPaddingAcrossThemesAndFontScales() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val themes = listOf(R.style.AppThemeWhite, R.style.AppTheme, R.style.AppThemeTrueBlackFrost,
            R.style.AppThemeWhitePlus, R.style.AppThemeTrueBlackPlus)
        for (theme in themes) for (fontScale in listOf(1f, 2f)) {
            val context = ContextThemeWrapper(application, theme).apply {
                applyOverrideConfiguration(Configuration(application.resources.configuration).apply { this.fontScale = fontScale })
            }
            val density = Density(context)
            val reference = BottomNavigationView(context).apply {
                labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
                inflateMenu(R.menu.bottom_nav_menu)
                selectedItemId = R.id.homeScreenFragment
            }
            for (selected in listOf(true, false)) {
                val id = if (selected) R.id.homeScreenFragment else R.id.summaryStatisticsFragment
                val labelId = if (selected) MaterialR.id.navigation_bar_item_large_label_view
                    else MaterialR.id.navigation_bar_item_small_label_view
                val label = reference.findViewById<ViewGroup>(id).findViewById<TextView>(labelId)
                val actual = rootNavigationLabelTextStyle(context, density, selected)
                val pixels = with(density) { actual.fontSize.toPx() }
                assertEquals("size selected=$selected, fontScale=$fontScale", label.textSize, pixels, 0.01f)
                assertEquals("face selected=$selected, fontScale=$fontScale", label.typeface.weight, actual.fontWeight!!.weight)
                assertTrue(actual.letterSpacing.isEm)
                assertEquals(label.letterSpacing * label.textSize, actual.letterSpacing.value * pixels, 0.01f)
                assertFalse(label.includeFontPadding)
                assertEquals(false, actual.platformStyle?.paragraphStyle?.includeFontPadding)
            }
        }
    }
}
