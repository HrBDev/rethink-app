package com.celzero.bravedns.ui.compose.appdetails

import android.app.Application
import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AppConnectionProgressTest {
    @get:Rule val compose = createComposeRule()
    private val themes = listOf(R.style.AppTheme, R.style.AppThemeWhite, R.style.AppThemeTrueBlack,
        R.style.AppThemeWhitePlus, R.style.AppThemeTrueBlackPlus, R.style.AppThemeTrueBlackFrost)

    @Test fun progressGeometryMatchesNativeMaterialAcrossThemes() {
        themes.forEach { theme ->
            val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), theme)
            val density = Density(context.resources.displayMetrics.density)
            val native = LinearProgressIndicator(context)
            val (thickness, radius) = appConnectionProgressMetrics(context, density, country = false)
            // The native radius getter reports only the absolute field (zero for a
            // percentage). Its drawing delegate multiplies the fraction by thickness.
            val nativeRadius = if (native.trackCornerRadiusFraction > 0f)
                (native.trackThickness * native.trackCornerRadiusFraction).toInt() else native.trackCornerRadius
            with(density) {
                assertEquals(native.trackThickness.toFloat(), thickness.toPx(), .01f)
                assertEquals(nativeRadius.toFloat(), radius.toPx(), .01f)
            }
            assertEquals(3.dp to 2.dp, appConnectionProgressMetrics(context, density, country = true))
        }
    }

    @Test fun populatedDomainIpAndCountryRowsRenderAndUpdateProgressAcrossThemes() {
        var theme by mutableIntStateOf(themes.first())
        val domain = AppDomainsRowState().apply { acdDomain.text = "example.test"; progress.max = 100; progress.progress = 25 }
        val ip = AppIpsRowState().apply { acdIpAddress.text = "192.0.2.1"; progress.max = 100; progress.progress = 25 }
        val country = AppCountriesRowState().apply { accCountryName.text = "Test country"; accProgress.max = 100; accProgress.progress = 25 }
        compose.setContent {
            val base = LocalContext.current
            val context = remember(theme) { ContextThemeWrapper(base, theme) }
            CompositionLocalProvider(LocalContext provides context) {
                RethinkTheme { Column {
                    AppDomainsRowContent(domain)
                    AppIpsRowContent(ip)
                    AppCountriesRowContent(country)
                } }
            }
        }
        themes.forEach { next ->
            compose.runOnIdle { theme = next }
            compose.onNodeWithText("example.test").assertIsDisplayed()
            compose.onNodeWithText("192.0.2.1").assertIsDisplayed()
            compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo(.25f, 0f..1f)), useUnmergedTree = true).assertCountEquals(3)
        }
        compose.runOnIdle { domain.progress.progress = 50; ip.progress.progress = 50; country.accProgress.progress = 50 }
        compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo,
            ProgressBarRangeInfo(.5f, 0f..1f)), useUnmergedTree = true).assertCountEquals(3)
    }
}
