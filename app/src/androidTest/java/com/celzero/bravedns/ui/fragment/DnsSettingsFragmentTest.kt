package com.celzero.bravedns.ui.fragment

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.activity.DnsDetailActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DnsSettingsFragmentTest {

    @get:Rule(order = 0)
    var activityRule: ActivityScenarioRule<DnsDetailActivity> =
        ActivityScenarioRule(DnsDetailActivity::class.java)

    @get:Rule(order = 1) val compose = createEmptyComposeRule()

    @Test
    fun testDnsSettingsDisplay() {
        compose.onNodeWithTag("dns-choice-network").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dns-choice-custom").performScrollTo().assertIsDisplayed()
    }
}
