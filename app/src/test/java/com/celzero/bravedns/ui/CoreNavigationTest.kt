/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui

import android.app.Application
import androidx.compose.ui.test.*
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.fixture.SafeRootHostTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp")
class CoreNavigationTest : SafeRootHostTest() {
    @Test fun testBottomNavigation() {
        launch()
        for (id in listOf(R.id.summaryStatisticsFragment, R.id.configureFragment, R.id.aboutFragment, R.id.homeScreenFragment)) {
            compose.onNodeWithTag("root_nav_$id").performClick().assertIsSelected()
            compose.runOnIdle { assertEquals(id, destination) }
        }
        compose.onNodeWithTag("fhs_dns_on_off_btn").assertIsDisplayed()
    }
    @Test fun reselectionDoesNotReplaceCurrentFragmentAndBackReturnsHomeThenFinishes() {
        launch()
        compose.onNodeWithTag("root_nav_${R.id.summaryStatisticsFragment}").performClick()
        val current = navHost.childFragmentManager.primaryNavigationFragment
        compose.onNodeWithTag("root_nav_${R.id.summaryStatisticsFragment}").performClick()
        compose.runOnIdle { assertSame(current, navHost.childFragmentManager.primaryNavigationFragment) }
        compose.runOnIdle { activity.onBackPressedDispatcher.onBackPressed() }
        compose.runOnIdle { assertEquals(R.id.homeScreenFragment, destination); assertFalse(activity.isFinishing) }
        compose.runOnIdle { activity.onBackPressedDispatcher.onBackPressed() }
        assertTrue(activity.isFinishing)
    }
}
