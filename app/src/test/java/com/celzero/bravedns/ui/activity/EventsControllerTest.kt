/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.activity
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.database.*
import com.celzero.bravedns.service.PersistentState
import com.celzero.bravedns.ui.compose.trafficlogs.EventsState
import com.celzero.bravedns.util.Themes
import com.celzero.bravedns.util.Utilities
import com.celzero.bravedns.viewmodel.EventsViewModel
import io.mockk.*
import kotlinx.coroutines.*
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
import org.robolectric.annotation.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class EventsControllerTest {
    private val main = StandardTestDispatcher()
    private lateinit var controller: ActivityController<EventsActivity>
    private lateinit var dao: EventDao
    private val calls = mutableListOf<Triple<String, Set<EventSource>, Severity?>>()
    @Before fun setup() {
        Dispatchers.setMain(main); stopKoin()
        val prefs = mockk<PersistentState>(); val vm = mockk<EventsViewModel>(); dao = mockk()
        every { prefs.theme } returns Themes.LIGHT.id
        every { vm.eventsList } returns MutableLiveData<PagingData<Event>>()
        every { vm.setFilter(any(), any(), any()) } answers { calls += Triple(arg(0), arg<Set<EventSource>>(1).toSet(), arg(2)) }
        mockkObject(Utilities); every { Utilities.isAlphaBuild() } returns false
        startKoin { modules(module { single<Application> { ApplicationProvider.getApplicationContext() }; single { prefs }; single { vm }; single { dao } }) }
        controller = Robolectric.buildActivity(EventsActivity::class.java).create()
    }
    @After fun teardown() { if (::controller.isInitialized) controller.destroy(); unmockkObject(Utilities); stopKoin(); Dispatchers.resetMain() }
    @Test fun severityClearsQuerySourcesWithoutClearingHiddenSourceChecks() {
        state().sourceChips = setOf(EventSource.DNS)
        severity("SOURCE"); source(EventSource.DNS, true); severity("HIGH"); severity("SOURCE")
        assertEquals(listOf(Triple("", emptySet(), null), Triple("", setOf(EventSource.DNS), null), Triple("", emptySet(), Severity.HIGH), Triple("", emptySet(), null)), calls)
        assertEquals(setOf(EventSource.DNS), state().sourceChips)
        coVerify(exactly = 0) { dao.deleteAll() }
    }
    @Test fun lastSourceRemovalKeepsVisibleRowUntilBaselineVisibilityCheck() {
        severity("SOURCE"); source(EventSource.DNS, true); source(EventSource.DNS, false)
        assertTrue(state().logs.showChildFilters)
        controller.get().javaClass.getDeclaredMethod("showSourceChipsIfNeeded").apply { isAccessible = true }.invoke(controller.get())
        assertFalse(state().logs.showChildFilters)
    }
    @Test fun debounceCombinesLatestSearchAndCurrentSeverity() = runTest(main.scheduler) {
        runCurrent(); severity("CRITICAL"); calls.clear()
        controller.get().onQueryTextChange("first.test"); advanceTimeBy(500); controller.get().onQueryTextSubmit("second.test")
        advanceTimeBy(999); runCurrent(); assertTrue(calls.isEmpty())
        advanceTimeBy(1); runCurrent(); assertEquals(listOf(Triple("second.test", emptySet<EventSource>(), Severity.CRITICAL)), calls)
    }
    private fun severity(value: String) = controller.get().javaClass.getDeclaredMethod("applySeverityFilter", Any::class.java).apply { isAccessible = true }.invoke(controller.get(), value)
    private fun source(value: EventSource, enabled: Boolean) = controller.get().javaClass.getDeclaredMethod("applySourceFilter", EventSource::class.java, Boolean::class.javaPrimitiveType!!).apply { isAccessible = true }.invoke(controller.get(), value, enabled)
    private fun state() = controller.get().javaClass.getDeclaredField("state").apply { isAccessible = true }.get(controller.get()) as EventsState
}
