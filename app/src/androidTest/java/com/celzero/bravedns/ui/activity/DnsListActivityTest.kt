package com.celzero.bravedns.ui.activity

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.celzero.bravedns.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DnsListActivityTest {

    @get:Rule(order = 0)
    var activityRule: ActivityScenarioRule<DnsListActivity> =
        ActivityScenarioRule(DnsListActivity::class.java)

    @get:Rule(order = 1) val compose = createEmptyComposeRule()

    @Test
    fun testDnsOptionsDisplay() {
        compose.onNodeWithTag("dns-protocol-cardDoh").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dns-protocol-cardDnscrypt").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dns-protocol-cardDot").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dns-protocol-cardOdoh").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dns-protocol-cardRethinkDns").performScrollTo().assertIsDisplayed()
    }
}
