package com.celzero.bravedns.ui.activity

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real host gate retained separately from fake-content tests; production startup is unsafe here. */
@Ignore("Pending isolated instrumentation Application and synthetic Paging fixture; never run against live traffic/VPN")
@RunWith(AndroidJUnit4::class)
class NetworkLogsActivityTest {
    @get:Rule val compose = createAndroidComposeRule<NetworkLogsActivity>()
    @Test fun testNetworkLogsDisplay() { compose.onNodeWithTag("log-list").assertIsDisplayed() }
}
