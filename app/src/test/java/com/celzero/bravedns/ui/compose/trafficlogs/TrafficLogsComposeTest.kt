/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.trafficlogs

import android.app.Application
import android.content.Context
import android.view.ContextThemeWrapper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.database.*
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Plain Application and memory-only rows; no production startup, DB, native VPN or export. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class TrafficLogsComposeTest {
    @get:Rule val compose = createComposeRule()
    @Test fun levelAndTraceApplyImmediatelyAndCancelOnlyDismisses() {
        var level by mutableIntStateOf(2); var fileTrace by mutableStateOf(false); val calls = mutableListOf<String>()
        compose.setContent { Theme { ConsoleLevelDialog(level, fileTrace, { level = it; calls += "level:$it" }, { fileTrace = it; calls += "trace:$it" }, { calls += "dismiss" }) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("console-level-3").performScrollTo().performClick()
        compose.onNodeWithTag("console-file-trace").performClick()
        compose.onNodeWithText(string(R.string.lbl_cancel)).performClick()
        compose.runOnIdle { assertEquals(listOf("level:3", "trace:true", "dismiss"), calls); assertEquals(3, level); assertTrue(fileTrace) }
    }
    @Test fun fakeEventExpandsCollapsesAndCopiesOnlyItsMessage() {
        val copied = mutableListOf<String>()
        val event = Event(1, 0, EventType.DNS_QUERY, Severity.LOW, "example.test requested", "Synthetic detail", EventSource.DNS)
        compose.setContent { Theme { EventLogRow(event) { copied += it } } }
        compose.onNodeWithText("Synthetic detail").assertDoesNotExist()
        compose.onNodeWithTag("event-1").performClick()
        compose.onNodeWithText("Synthetic detail").assertIsDisplayed()
        compose.onNodeWithTag("event-1").performTouchInput { longClick() }
        compose.runOnIdle { assertEquals(listOf("example.test requested"), copied) }
        compose.onNodeWithTag("event-1").performClick()
        compose.onNodeWithText("Synthetic detail").assertDoesNotExist()
    }
    @Test fun disabledDomainRowCannotDispatchAndCompositionNeverWrites() {
        val state = ConnectionDetailsState(ApplicationProvider.getApplicationContext()).apply {
            bsConnRuleRowDomain.visibility = android.view.View.VISIBLE; bsConnDomainRuleAddress.text = "example.test"; bsConnRuleRowDomain.isEnabled = false
        }
        var writes = 0; state.bsConnRuleRowDomain.setOnClickListener { writes++ }
        compose.setContent { Theme { ConnectionDetailsContent(state) } }
        compose.onNodeWithText("example.test").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(0, writes) }
    }
    @Test fun pagingKeepsOrderAndCardinalityAfterInsertion() {
        val presenter = FakePresenter(); lateinit var lifecycle: Lifecycle
        compose.setContent { Theme { lifecycle = LocalLifecycleOwner.current.lifecycle; LogList(LogScreenState(), presenter, timestamp = { it.timestamp }) } }
        val first = Event(1, 1, EventType.DNS_QUERY, Severity.LOW, "first.test", source = EventSource.DNS)
        val second = Event(2, 2, EventType.DNS_QUERY, Severity.LOW, "second.test", source = EventSource.DNS)
        compose.runOnIdle { presenter.submitData(lifecycle, PagingData.from(listOf(first))) }; compose.waitUntil(5000) { compose.runOnIdle { presenter.itemCount == 1 } }
        compose.runOnIdle { presenter.submitData(lifecycle, PagingData.from(listOf(second, first))) }; compose.waitUntil(5000) { compose.runOnIdle { presenter.itemCount == 2 } }
        compose.runOnIdle { assertEquals(listOf(second, first), presenter.snapshot().items) }
        compose.onNodeWithText("second.test").assertIsDisplayed(); compose.onNodeWithText("first.test").assertIsDisplayed()
    }
    @Test fun hiddenSourceChecksRemainSeparateFromSeveritySelection() {
        val state = EventsState().apply { logs.showParentFilters = true; logs.showChildFilters = true; severityChip = "SOURCE"; sourceChips = setOf(EventSource.DNS) }
        val selected = mutableListOf<String>()
        compose.setContent { Theme { EventsScreen(state, FakePresenter(), {}, {}, { selected += it }, { _, _ -> }, {}, {}) } }
        compose.onNodeWithTag("log-filter-HIGH").performClick(); compose.onNodeWithTag("log-filter-HIGH").performClick()
        compose.runOnIdle { assertEquals(listOf("HIGH"), selected); assertEquals(setOf(EventSource.DNS), state.sourceChips) }
    }
    private class FakePresenter : LogPagingPresenter<Event>(object : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Event, newItem: Event) = oldItem == newItem
    }) {
        override fun itemKey(value: Event): Any = value.id
        @Composable override fun Row(value: Event, generation: Int) { EventLogRow(value) {} }
    }
    private fun string(resource: Int) = ApplicationProvider.getApplicationContext<Context>().getString(resource)
    @Composable private fun Theme(content: @Composable () -> Unit) {
        val base = LocalContext.current
        // Dialog creates its own Compose root from the generic test Activity's context.
        remember(base) {
            generateSequence(base) { (it as? android.content.ContextWrapper)?.baseContext }
                .filterIsInstance<android.app.Activity>().first().setTheme(R.style.AppThemeWhite)
        }
        val themed = remember(base) { ContextThemeWrapper(base, R.style.AppThemeWhite) }
        CompositionLocalProvider(LocalContext provides themed) { RethinkTheme(content) }
    }
}
