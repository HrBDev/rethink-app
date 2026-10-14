/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.activity

import android.app.Application
import androidx.compose.ui.test.*
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.fixture.SafeDnsHostTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp")
class DnsDetailNavigationTest : SafeDnsHostTest() {
    @Test fun testDnsTabsDisplay() {
        val pager = activity.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.dns_detail_act_viewpager)
        assertNotNull(pager)
        assertEquals(1, pager.adapter!!.itemCount)
        assertEquals(android.view.View.VISIBLE, pager.visibility)
        // The XML TabLayout is intentionally hidden; the real native page replaces its heading.
        compose.onNodeWithTag("dns-choice-network").performScrollTo().assertIsDisplayed()
    }
    @Test fun customDnsChoicePreservesActivityRouteWithoutLaunchingDestination() {
        compose.onNodeWithTag("dns-choice-custom").performScrollTo().performClick()
        val shadow = org.robolectric.Shadows.shadowOf(activity)
        assertEquals(DnsListActivity::class.java.name, shadow.nextStartedActivity?.component?.className)
        assertNull(shadow.nextStartedActivity)
    }
    @Test fun actualHostRecreationRestoresNativeDnsPage() {
        host.recreate()
        compose.onNodeWithTag("dns-choice-network").performScrollTo().assertIsDisplayed()
        assertTrue(activity.supportFragmentManager.fragments.any { it is com.celzero.bravedns.ui.fragment.DnsSettingsFragment })
    }
}
