/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.activity

import android.app.Application
import androidx.compose.ui.test.*
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.fixture.SafeFirewallHostTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp")
class FirewallActivityTest : SafeFirewallHostTest() {
    @Test fun testFirewallActivityDisplay() {
        val pager = activity.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.firewall_act_viewpager)
        assertNotNull(pager)
        assertEquals(1, pager.adapter!!.itemCount)
        assertEquals(android.view.View.VISIBLE, pager.visibility)
        compose.onNodeWithText(activity.getString(R.string.firewall_act_universal_tab)).assertIsDisplayed()
        compose.onNodeWithText(activity.getString(R.string.univ_firewall_heading)).assertIsDisplayed().assertHasClickAction()
    }
    @Test fun actualFirewallHostRecreationRetainsItsNativePage() {
        host.recreate()
        compose.onNodeWithText(activity.getString(R.string.univ_firewall_heading)).assertIsDisplayed()
        assertTrue(activity.supportFragmentManager.fragments.any { it is com.celzero.bravedns.ui.fragment.FirewallSettingsFragment })
    }
}
