package com.celzero.bravedns.ui.compose.dnssettings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.celzero.bravedns.ui.compose.dnssettings.*
import android.app.Application
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Rule
import org.junit.Test

/** Content only; never launches DnsDetailActivity or a real VPN controller. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class DnsSettingsChoicesComposeTest {
    @get:Rule val compose = createComposeRule()
    @Test fun allFourChoicesAndSystemInformationAreExposed() {
        compose.setContent { DnsUiTestTheme { DnsSettingsScreen(DnsSettingsState(), {}, {}) } }
        listOf("network", "custom", "rethink", "smart").forEach { compose.onNodeWithTag("dns-choice-$it").performScrollTo().assertIsDisplayed() }
        compose.onNodeWithTag("dns-system-info").performScrollTo().assertIsDisplayed()
    }
}
