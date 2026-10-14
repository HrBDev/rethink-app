package com.celzero.bravedns.ui.compose.rpn

import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.celzero.bravedns.database.CountryConfig
import com.celzero.bravedns.ui.adapter.CountryServerAdapter
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Content fixtures only. Never constructs an RPN controller, real polling presenter, or manager.
 * Not executed by the host checks; requires an explicitly isolated test application before use. */
@RunWith(AndroidJUnit4::class)
class RpnContentTest {
    @get:Rule val compose = createComposeRule()
    private fun content(block: @Composable () -> Unit) {
        val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppThemeWhite)
        compose.setContent { CompositionLocalProvider(LocalContext provides context) { RethinkTheme { block() } } }
    }

    @Test fun filterRequiresOneChoiceAndOnlyCommitsOnApply() {
        val writes = mutableListOf<Triple<Int, Int, Boolean>>()
        content {
            RpnFilterContent(listOf("Any load", "Low"), 0, listOf("Any speed", "Fast"), 0, false,
                { load, speed, favourite -> writes.add(Triple(load, speed, favourite)) }, {}, {})
        }
        compose.onNodeWithText("Low").performClick()
        compose.onNodeWithText("Low").performClick().assertIsSelected()
        compose.onNodeWithText("Fast").performClick()
        compose.runOnIdle { assertEquals(emptyList<Triple<Int, Int, Boolean>>(), writes) }
        compose.onNodeWithTag("rpn_filter_apply").performClick()
        compose.runOnIdle { assertEquals(listOf(Triple(1, 1, false)), writes) }
    }

    @Test fun filterCancelDoesNotWritePendingSelections() {
        var writes = 0; var cancels = 0
        content {
            RpnFilterContent(listOf("Any load", "Low"), 0, listOf("Any speed"), 0, false,
                { _, _, _ -> writes++ }, { writes++ }, { cancels++ })
        }
        compose.onNodeWithText("Low").performClick()
        compose.onNodeWithTag("rpn_filter_cancel").performClick()
        compose.runOnIdle { assertEquals(0, writes); assertEquals(1, cancels) }
    }

    @Test fun countryExpansionSurvivesUpdateAndStoppedRowRedirectsOnce() {
        var selections = 0; var redirects = 0
        val config = CountryConfig("fixture-us", "US", city = "Fixture City", key = "fixture", lastModified = 0)
        val item = CountryServerAdapter.CountryItem("US", "Fixture Country", "🇺🇸", listOf(
            CountryServerAdapter.ServerGroup("fixture", listOf(config), "Fixture City", 10, 1000, false)))
        val presenter = CountryServerAdapter(listOf(item), object : CountryServerAdapter.CitySelectionListener {
            override fun onCitySelected(server: CountryConfig, isEnabled: Boolean) { selections++ }
            override fun onProxyStoppedItemTapped() { redirects++ }
            override fun onFavouriteToggled(countryCode: String, countryName: String, isFavourite: Boolean) {}
        })
        content { presenter.Content() }
        compose.onNodeWithText("Fixture Country").performClick()
        compose.onNodeWithText("Fixture City").assertExists()
        compose.runOnIdle { presenter.updateCountries(listOf(item.copy())); presenter.setProxyStopped(true) }
        compose.onNodeWithText("Fixture City").assertExists().performClick()
        compose.runOnIdle { assertEquals(0, selections); assertEquals(1, redirects) }
    }
}
