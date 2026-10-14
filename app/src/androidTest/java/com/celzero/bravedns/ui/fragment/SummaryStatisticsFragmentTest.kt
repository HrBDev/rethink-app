package com.celzero.bravedns.ui.fragment

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.HomeScreenActivity
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Original root navigation/display contract, pending a disposable fake-service/database fixture. */
@Ignore("Production HomeScreenActivity startup is outside safe UI validation; restore this integration check on an isolated fake-service/database device fixture")
@RunWith(AndroidJUnit4::class)
class SummaryStatisticsFragmentTest {
    @get:Rule val compose = createAndroidComposeRule<HomeScreenActivity>()
    @Test fun testStatisticsDisplay() {
        compose.onNodeWithTag("root_nav_${R.id.summaryStatisticsFragment}").performClick()
        compose.onNodeWithTag("stats_time_0").assertIsDisplayed()
    }
}
