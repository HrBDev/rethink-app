/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.trafficlogs

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.data.AppConfig
import com.celzero.bravedns.database.*
import com.celzero.bravedns.service.PersistentState
import com.celzero.bravedns.ui.fragment.ConnectionTrackerFragment
import com.celzero.bravedns.ui.fragment.DnsLogFragment
import com.celzero.bravedns.util.Logger
import com.celzero.bravedns.viewmodel.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Actual fragment lifecycle with strict fakes and empty memory-only LiveData. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class TrafficLogFragmentControllerTest {
    private val main = StandardTestDispatcher()
    private lateinit var host: ActivityController<FragmentActivity>
    private lateinit var connectionVm: ConnectionTrackerViewModel
    private lateinit var dnsVm: DnsLogViewModel
    private var enabled = true
    private var containerId = 0
    private val calls = mutableListOf<String>()
    @Before fun setUp() {
        Dispatchers.setMain(main); stopKoin()
        connectionVm = mockk(); dnsVm = mockk()
        val prefs = mockk<PersistentState>(); val config = mockk<AppConfig>()
        every { prefs.logsEnabled } answers { enabled }
        every { prefs.fetchFavIcon } returns false
        every { config.isRethinkDnsConnected() } returns false
        every { connectionVm.connectionTrackerListMerged } returns MutableLiveData<PagingData<MergedConnectionLog>>()
        every { connectionVm.connectionTrackerList } returns MutableLiveData<PagingData<ConnectionTracker>>()
        every { connectionVm.setFilter(any(), any(), any()) } answers { calls += "network:${arg<String>(0)}:${arg<Set<String>>(1).sorted()}:${arg<ConnectionTrackerViewModel.TopLevelFilter>(2)}" }
        every { dnsVm.dnsLogsList } returns MutableLiveData<PagingData<DnsLog>>()
        every { dnsVm.setFilter(any(), any()) } answers { calls += "dns:${arg<String>(0)}:${arg<DnsLogFragment.DnsLogFilter>(1)}" }
        every { dnsVm.setIsWireGuardLogs(any(), any()) } answers { calls += "wg:${arg<Boolean>(0)}:${arg<String>(1)}" }
        every { dnsVm.setIsRpnLogs(any(), any()) } answers { calls += "rpn:${arg<Boolean>(0)}:${arg<String>(1)}" }
        mockkObject(Logger); every { Logger.v(any(), any()) } just Runs
        startKoin { modules(module {
            single<Application> { ApplicationProvider.getApplicationContext() }; single { prefs }; single { config }; single { connectionVm }; single { dnsVm }
            single<ConnectionTrackerRepository> { mockk() }; single<RethinkLogRepository> { mockk() }; single<DnsLogRepository> { mockk() }
        }) }
        host = Robolectric.buildActivity(FragmentActivity::class.java).create()
        host.get().setTheme(R.style.AppThemeWhite)
        containerId = View.generateViewId()
        host.get().setContentView(FrameLayout(host.get()).apply { id = containerId })
        host.start()
    }
    @After fun tearDown() { if (::host.isInitialized) host.stop().destroy(); unmockkObject(Logger); stopKoin(); Dispatchers.resetMain() }
    @Test fun networkArgumentThenInitialEmptyFlowResetIsPreserved() = runTest(main.scheduler) {
        val fragment = ConnectionTrackerFragment.newInstance("argument.test"); add(fragment)
        assertEquals(listOf("network:argument.test:[]:ALL"), calls)
        runCurrent(); advanceTimeBy(1000); runCurrent()
        assertEquals(listOf("network:argument.test:[]:ALL", "network::[]:ALL"), calls)
        assertEquals("argument.test", state(fragment).search.query)
    }
    @Test fun restoredNetworkTextOverridesArgumentOnlyAfterListenerInstallation() = runTest(main.scheduler) {
        val fragment = ConnectionTrackerFragment.newInstance("argument.test"); add(fragment)
        fragment.onViewStateRestored(Bundle().apply { putString("traffic.search", "restored.test") })
        runCurrent(); advanceTimeBy(1000); runCurrent()
        assertEquals(listOf("network:argument.test:[]:ALL", "network:restored.test:[]:ALL"), calls)
    }
    @Test fun equalNetworkRestoreDoesNotAccidentallySeedInitialFlow() = runTest(main.scheduler) {
        val fragment = ConnectionTrackerFragment.newInstance("argument.test"); add(fragment)
        fragment.onViewStateRestored(Bundle().apply { putString("traffic.search", "argument.test") })
        runCurrent(); advanceTimeBy(1000); runCurrent()
        assertEquals(listOf("network:argument.test:[]:ALL", "network::[]:ALL"), calls)
    }
    @Test fun dnsArgumentAndResumeKeepOriginalOrder() = runTest(main.scheduler) {
        val fragment = DnsLogFragment.newInstance("argument.test"); add(fragment); fragment.onResume()
        assertEquals(listOf("dns::ALL"), calls)
        runCurrent(); advanceTimeBy(1000); runCurrent()
        assertEquals(listOf("dns::ALL", "dns:argument.test:ALL"), calls)
    }
    @Test fun disabledDnsRestorationNeverDispatchesThroughAnUninstalledListener() = runTest(main.scheduler) {
        enabled = false
        val fragment = DnsLogFragment.newInstance("argument.test"); add(fragment)
        fragment.onViewStateRestored(Bundle().apply { putString("traffic.search", "restored.test") })
        runCurrent(); advanceTimeBy(2000); runCurrent()
        assertEquals("restored.test", state(fragment).search.query); assertTrue(calls.isEmpty())
    }
    @Test fun wireguardNetworkHasNoSearchCollectorAndDnsRpnSetsOnlyItsScope() = runTest(main.scheduler) {
        val network = ConnectionTrackerFragment.newInstance("W:wg123"); add(network)
        assertFalse(state(network).showSearch)
        network.onQueryTextChange("unreachable.test"); runCurrent(); advanceTimeBy(2000); runCurrent()
        assertEquals(listOf("network:wg123:[]:ALL"), calls)
        host.get().supportFragmentManager.beginTransaction().remove(network).commitNow(); calls.clear()
        val dns = DnsLogFragment.newInstance("RPN:rpn0"); add(dns)
        assertFalse(state(dns).showSearch); assertEquals(listOf("rpn:true:rpn0"), calls)
    }
    @Test fun blockedSelectionRetainsExistingChildQueryCategory() {
        val fragment = ConnectionTrackerFragment.newInstance(""); add(fragment)
        invoke(fragment, "applyParentFilter", arrayOf(Any::class.java), arrayOf(ConnectionTrackerViewModel.TopLevelFilter.ALLOWED.id))
        invoke(fragment, "applyChildFilter", arrayOf(Any::class.java, Boolean::class.javaPrimitiveType!!), arrayOf("synthetic-rule", true))
        invoke(fragment, "applyParentFilter", arrayOf(Any::class.java), arrayOf(ConnectionTrackerViewModel.TopLevelFilter.BLOCKED.id))
        assertEquals("network::[synthetic-rule]:BLOCKED", calls.last())
    }
    private fun add(fragment: Fragment) { host.get().supportFragmentManager.beginTransaction().add(containerId, fragment, "fixture").commitNow() }
    private fun state(fragment: Fragment) = fragment.javaClass.getDeclaredField("state").apply { isAccessible = true }.get(fragment) as LogScreenState
    private fun invoke(target: Any, name: String, types: Array<Class<*>>, args: Array<Any>) = target.javaClass.getDeclaredMethod(name, *types).apply { isAccessible = true }.invoke(target, *args)
}
