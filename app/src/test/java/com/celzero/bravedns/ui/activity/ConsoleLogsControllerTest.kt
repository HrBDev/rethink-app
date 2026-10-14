/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.activity

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemSnapshotList
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.adapter.ConsoleLogAdapter
import com.celzero.bravedns.database.ConsoleLog
import com.celzero.bravedns.database.ConsoleLogRepository
import com.celzero.bravedns.net.go.GoVpnAdapter
import com.celzero.bravedns.scheduler.WorkScheduler
import com.celzero.bravedns.service.PersistentState
import com.celzero.bravedns.service.VpnController
import com.celzero.bravedns.ui.compose.trafficlogs.ConsoleLogsState
import com.celzero.bravedns.util.*
import com.celzero.bravedns.viewmodel.ConsoleLogViewModel
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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ConsoleLogsControllerTest {
    private val main = StandardTestDispatcher()
    private lateinit var controller: ActivityController<ConsoleLogActivity>
    private lateinit var vm: ConsoleLogViewModel
    private lateinit var scheduler: WorkScheduler
    private val trace = mutableListOf<String>()
    private var level = 2L; private var fileTrace = false
    @Before fun setUp() {
        Dispatchers.setMain(main); stopKoin()
        vm = mockk(); scheduler = mockk()
        val prefs = mockk<PersistentState>(); val repo = mockk<ConsoleLogRepository>()
        every { prefs.theme } returns Themes.LIGHT.id
        every { prefs.goLoggerLevel } returns 2L
        every { prefs.includeFileTrace } answers { fileTrace }
        every { prefs.includeFileTrace = any() } answers { fileTrace = firstArg(); trace += "trace:$fileTrace" }
        every { vm.logs } returns MutableLiveData<PagingData<ConsoleLog>>()
        every { vm.setLogLevel(any()) } answers { trace += "level:${firstArg<Long>()}" }
        every { vm.setFilter(any()) } answers { trace += "search:${firstArg<String>()}" }
        every { vm.restartLogStream() } answers { trace += "restart" }
        coEvery { vm.sinceTime() } coAnswers { awaitCancellation() }
        every { repo.setStartTimestamp(any()) } answers { trace += "timestamp" }
        mockkObject(Logger, Utilities, GoVpnAdapter.Companion, VpnController)
        every { Utilities.isAlphaBuild() } returns false
        every { Logger.uiLogLevel } answers { level }
        every { Logger.uiLogLevel = any() } answers { level = firstArg(); trace += "ui:$level" }
        every { Logger.i(any(), any()) } just Runs
        every { GoVpnAdapter.setLogLevel(any(), any(), any()) } answers { trace += "backend:${arg<Int>(0)}:${arg<Int>(1)}:${arg<Boolean>(2)}" }
        coEvery { VpnController.crashTun(any()) } coAnswers { trace += "crash:${firstArg<Long>()}" }
        startKoin { modules(module { single<Application> { ApplicationProvider.getApplicationContext() }; single { prefs }; single { vm }; single { repo }; single { scheduler } }) }
        controller = Robolectric.buildActivity(ConsoleLogActivity::class.java).create()
    }
    @After fun tearDown() { if (::controller.isInitialized) controller.destroy(); unmockkObject(Logger, Utilities, GoVpnAdapter.Companion, VpnController); stopKoin(); Dispatchers.resetMain() }
    @Test fun creationOnlyInitializesLevelAndNeverExportsOrChangesNativeSettings() {
        assertEquals(listOf("level:2"), trace)
        verify(exactly = 0) { GoVpnAdapter.setLogLevel(any(), any(), any()) }
        verify(exactly = 0) { scheduler.scheduleConsoleLogSaveJob(any()) }
    }
    @Test fun pauseKeeps5000NewestRowsAndResumeRestartsExactlyOnce() {
        val activity = controller.get(); val adapter = mockk<ConsoleLogAdapter>()
        every { adapter.snapshot() } returns ItemSnapshotList(0, 0, (6000 downTo 1).map { ConsoleLog(it, "I fake-$it", 3, it.toLong()) })
        activity.recyclerAdapter = adapter; trace.clear(); invoke(activity, "togglePause")
        val rows = field<List<ConsoleLog>>(activity, "pausedSnapshot")
        assertEquals(5000, rows.size); assertEquals(6000, rows.first().id); assertEquals(1001, rows.last().id); assertTrue(trace.isEmpty())
        invoke(activity, "togglePause"); assertEquals(listOf("restart"), trace); assertTrue(field<List<ConsoleLog>>(activity, "pausedSnapshot").isEmpty())
    }
    @Test fun levelAndFileTraceApplyImmediatelyInOriginalOrder() {
        val activity = controller.get(); trace.clear()
        activity.javaClass.getDeclaredMethod("selectLogLevel", Int::class.javaPrimitiveType).apply { isAccessible = true }.invoke(activity, 3)
        assertEquals(listOf("ui:3", "backend:2:3:false", "level:3", "timestamp"), trace)
        field<ConsoleLogsState>(activity, "b").filterOpen = false; assertEquals(3L, level); trace.clear()
        activity.javaClass.getDeclaredMethod("setFileTrace", Boolean::class.javaPrimitiveType).apply { isAccessible = true }.invoke(activity, true)
        assertEquals(listOf("trace:true", "backend:2:3:true", "level:3"), trace)
    }
    @Test fun intermediateSearchNeverReachesTheControllerBeforeOneSecond() = runTest(main.scheduler) {
        val activity = controller.get(); runCurrent(); trace.clear()
        activity.onQueryTextChange("example"); advanceTimeBy(500); activity.onQueryTextChange("example.test")
        advanceTimeBy(999); runCurrent(); assertTrue(trace.isEmpty())
        advanceTimeBy(1); runCurrent(); assertEquals(listOf("search:example.test"), trace)
    }
    private fun invoke(target: Any, name: String) = target.javaClass.getDeclaredMethod(name).apply { isAccessible = true }.invoke(target)
    @Suppress("UNCHECKED_CAST") private fun <T> field(target: Any, name: String): T = target.javaClass.getDeclaredField(name).apply { isAccessible = true }.get(target) as T
}
