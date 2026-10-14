/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.activity
import android.app.Application
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.data.AppConfig
import com.celzero.bravedns.service.*
import com.celzero.bravedns.ui.fragment.*
import com.celzero.bravedns.util.*
import io.mockk.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.*

/** Actual CREATED host only: child pages never start and all service state is a strict fake. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NetworkLogsRoutingTest {
    private var mode = AppConfig.BraveMode.DNS_FIREWALL
    private var controller: ActivityController<NetworkLogsActivity>? = null
    @Before fun setup() {
        stopKoin(); val prefs = mockk<PersistentState>(); val config = mockk<AppConfig>()
        every { prefs.theme } returns Themes.LIGHT.id; every { prefs.routeRethinkInRethink } returns true; every { config.getBraveMode() } answers { mode }
        mockkObject(VpnController, Utilities)
        every { VpnController.connectionStatus } returns MutableLiveData<BraveVPNService.State?>()
        every { Utilities.isAlphaBuild() } returns false
        startKoin { modules(module { single<Application> { ApplicationProvider.getApplicationContext() }; single { prefs }; single { config } }) }
    }
    @After fun teardown() { controller?.destroy(); unmockkObject(VpnController, Utilities); stopKoin() }
    @Test fun ordinaryModesKeepOriginalCountAndPageOrder() {
        val host = create("")
        assertEquals(2, count(host)); assertTrue(page(host, 0) is ConnectionTrackerFragment); assertTrue(page(host, 1) is DnsLogFragment)
        mode = AppConfig.BraveMode.DNS; assertEquals(1, count(host)); assertTrue(page(host, 0) is DnsLogFragment)
        mode = AppConfig.BraveMode.FIREWALL; assertEquals(1, count(host)); assertTrue(page(host, 0) is ConnectionTrackerFragment)
    }
    @Test fun wireguardPassesExactArgumentsToAllThreePages() {
        val host = create("W:wg123"); assertEquals(3, count(host))
        assertTrue(page(host, 0) is ConnectionTrackerFragment); assertTrue(page(host, 1) is DnsLogFragment); assertTrue(page(host, 2) is WgNwStatsFragment)
        (0..2).forEach { assertEquals("W:wg123", page(host, it).arguments?.getString(Constants.SEARCH_QUERY)) }
    }
    @Test fun rpnPreservesItsModeDependentCountAndUniversalRuleTakesPrecedence() {
        mode = AppConfig.BraveMode.DNS; val host = create("RPN:rpn0")
        assertEquals(1, count(host)); assertTrue(page(host, 0) is ConnectionTrackerFragment)
        controller?.destroy(); controller = null
        val query = UniversalFirewallSettingsActivity.RULES_SEARCH_ID + "synthetic-rule W:wg123"
        val universal = create(query); assertEquals(1, count(universal)); assertEquals(query, page(universal, 0).arguments?.getString(Constants.SEARCH_QUERY))
    }
    private fun create(query: String): NetworkLogsActivity = Robolectric.buildActivity(NetworkLogsActivity::class.java, Intent(ApplicationProvider.getApplicationContext<Application>(), NetworkLogsActivity::class.java).putExtra(Constants.SEARCH_QUERY, query)).create().also { controller = it }.get()
    private fun count(host: NetworkLogsActivity) = host.javaClass.getDeclaredMethod("getCount").apply { isAccessible = true }.invoke(host) as Int
    private fun page(host: NetworkLogsActivity, position: Int) = host.javaClass.getDeclaredMethod("getFragment", Int::class.javaPrimitiveType!!).apply { isAccessible = true }.invoke(host, position) as Fragment
}
