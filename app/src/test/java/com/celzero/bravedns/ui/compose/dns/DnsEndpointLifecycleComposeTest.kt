/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.dns

import android.app.Application
import android.os.Looper
import android.view.ContextThemeWrapper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Bind-lifetime traces only: a plain Application, Compose's empty test activity, synthetic
 * LifecycleOwner and in-memory callbacks. No endpoint presenter, production host, backend,
 * DNS/IP lookup, repository, account, store or network is created or called.
 * The injected dispatcher keeps fake callbacks on the test thread; Looper time is virtual.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class DnsEndpointLifecycleComposeTest {
    @get:Rule val compose = createComposeRule()
    private val trace = CopyOnWriteArrayList<String>()

    @Test fun inactivePollTickTerminatesUntilAnIncomingRowRebinds() {
        val owner = owner(Lifecycle.State.STARTED)
        var item by mutableStateOf(row("first", polling = true))
        compose.setContent { Theme(owner) { DnsEndpointRow(item, Dispatchers.Unconfined) } }
        assertTrace("first:description", "first:flag")

        move(owner, Lifecycle.State.CREATED)
        advanceBy(1_100)
        move(owner, Lifecycle.State.STARTED)
        advanceBy(2_100)
        assertTrace("first:description", "first:flag")

        // Same id, selected state, address and poll setting; only the incoming callbacks differ.
        compose.runOnIdle { item = row("replacement", polling = true) }
        assertTrace("first:description", "first:flag", "replacement:description", "replacement:flag")
        advanceBy(1_100)
        assertTrace("first:description", "first:flag", "replacement:description", "replacement:flag", "replacement:description")
    }

    @Test fun stopAndStartBetweenTicksDoesNotFetchTheFlagOrAddAnEarlyPoll() {
        val owner = owner(Lifecycle.State.STARTED)
        val item = row("bound", polling = true)
        compose.setContent { Theme(owner) { DnsEndpointRow(item, Dispatchers.Unconfined) } }
        assertTrace("bound:description", "bound:flag")

        move(owner, Lifecycle.State.CREATED)
        advanceBy(100)
        move(owner, Lifecycle.State.STARTED)
        assertTrace("bound:description", "bound:flag")
        advanceBy(1_000)
        assertTrace("bound:description", "bound:flag", "bound:description")
    }

    @Test fun oneShotRowsDoNotWaitForStartOrReplayOnResume() {
        val owner = owner(Lifecycle.State.CREATED)
        var item by mutableStateOf(row("one-shot"))
        compose.setContent { Theme(owner) { DnsEndpointRow(item, Dispatchers.Unconfined) } }
        assertTrace("one-shot:description", "one-shot:flag")
        compose.onNodeWithText("one-shot description").assertDoesNotExist()
        compose.onNodeWithText("one-shot flag").assertDoesNotExist()

        move(owner, Lifecycle.State.STARTED)
        advanceBy(2_100)
        assertTrace("one-shot:description", "one-shot:flag")
        compose.onNodeWithText("one-shot description").assertDoesNotExist()
        compose.onNodeWithText("one-shot flag").assertDoesNotExist()

        compose.runOnIdle { item = row("updated") }
        assertTrace("one-shot:description", "one-shot:flag", "updated:description", "updated:flag")
        compose.onNodeWithText("updated description").assertIsDisplayed()
        compose.onNodeWithText("updated flag").assertIsDisplayed()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun staticSelectedLabelIsRenderedBeforeAnyIoDispatchWhileStopped() {
        val owner = owner(Lifecycle.State.CREATED)
        val dispatcher = StandardTestDispatcher()
        val item = row("static label").copy(description = null, initialDescription = "static label description")
        compose.setContent { Theme(owner) { DnsEndpointRow(item, dispatcher) } }
        assertTrace()
        compose.onNodeWithText("static label description").assertIsDisplayed()
        compose.runOnIdle { dispatcher.scheduler.runCurrent() }
        assertTrace("static label:flag")
        compose.onNodeWithText("static label flag").assertDoesNotExist()
        move(owner, Lifecycle.State.STARTED)
        advanceBy(1_100)
        assertTrace("static label:flag")
    }

    @Test fun staticBranchCannotTurnIntoAStatusReadAfterTheBindGuardChanges() {
        val owner = owner(Lifecycle.State.STARTED)
        var canPoll = false
        val bound = bindDnsEndpointDescription(true,
            canPoll = { trace += "bind:guard"; canPoll },
            selectedLabel = { trace += "bind:label"; "Bound selected label" },
            status = { throw AssertionError("Static bind must not invoke status") })
        canPoll = true
        val item = row("static").copy(description = bound.read, pollStatus = bound.poll,
            initialDescription = bound.initial)
        compose.setContent { Theme(owner) { DnsEndpointRow(item, Dispatchers.Unconfined) } }
        advanceBy(2_100)
        assertTrace("bind:guard", "bind:label", "static:flag")
        compose.onNodeWithText("Bound selected label").assertIsDisplayed()
    }

    @Test fun pollingBranchKeepsReadingStatusWithoutRecheckingTheBindGuard() {
        val owner = owner(Lifecycle.State.STARTED)
        var canPoll = true
        val bound = bindDnsEndpointDescription(true,
            canPoll = { trace += "bind:guard"; canPoll },
            selectedLabel = { throw AssertionError("Polling bind must not evaluate static label") },
            status = { trace += "bound:status"; "Bound status" })
        canPoll = false
        val item = row("polling").copy(description = bound.read, pollStatus = bound.poll,
            initialDescription = bound.initial)
        compose.setContent { Theme(owner) { DnsEndpointRow(item, Dispatchers.Unconfined) } }
        assertTrace("bind:guard", "bound:status", "polling:flag")
        advanceBy(1_100)
        assertTrace("bind:guard", "bound:status", "polling:flag", "bound:status")
        compose.onNodeWithText("Bound status").assertIsDisplayed()
    }

    @Test fun unselectedBindDoesNotEvaluateTheGuardLabelOrStatus() {
        val bound = bindDnsEndpointDescription(false,
            canPoll = { throw AssertionError("Unselected bind must not evaluate poll guard") },
            selectedLabel = { throw AssertionError("Unselected bind must not evaluate static label") },
            status = { throw AssertionError("Unselected bind must not invoke status") })
        assertFalse(bound.poll)
        assertNull(bound.initial)
        assertNull(bound.read)
    }

    @Test fun pollingRowBoundWhileStoppedFetchesOnlyItsFlagAndDoesNotStartLater() {
        val owner = owner(Lifecycle.State.CREATED)
        val item = row("stopped", polling = true)
        compose.setContent { Theme(owner) { DnsEndpointRow(item, Dispatchers.Unconfined) } }
        assertTrace("stopped:flag")
        move(owner, Lifecycle.State.STARTED)
        advanceBy(2_100)
        assertTrace("stopped:flag")
    }

    @Test fun flagInFlightSurvivesStopButItsStoppedResultIsNotPublishedOrReplayed() {
        val owner = owner(Lifecycle.State.STARTED)
        val pending = CompletableDeferred<String?>()
        val item = row("pending").copy(flag = {
            trace += "flag:started"
            try { pending.await() } finally { trace += "flag:finished" }
        })
        compose.setContent { Theme(owner) { DnsEndpointRow(item, Dispatchers.Unconfined) } }
        assertTrace("pending:description", "flag:started")
        move(owner, Lifecycle.State.CREATED)
        advanceBy(1_100)
        assertTrace("pending:description", "flag:started")

        compose.runOnIdle { pending.complete("late fake flag") }
        assertTrace("pending:description", "flag:started", "flag:finished")
        compose.onNodeWithText("late fake flag").assertDoesNotExist()
        move(owner, Lifecycle.State.STARTED)
        advanceBy(1_100)
        assertTrace("pending:description", "flag:started", "flag:finished")
        compose.onNodeWithText("late fake flag").assertDoesNotExist()
    }

    @Test fun pendingDescriptionDoesNotDelayTheInactiveTickOrRestartThePoll() {
        val owner = owner(Lifecycle.State.STARTED)
        val pending = CompletableDeferred<String?>()
        val item = row("slow", polling = true).copy(description = {
            trace += "description:started"
            try { pending.await() } finally { trace += "description:finished" }
        })
        compose.setContent { Theme(owner) { DnsEndpointRow(item, Dispatchers.Unconfined) } }
        assertTrace("description:started", "slow:flag")
        move(owner, Lifecycle.State.CREATED)
        advanceBy(1_100)
        // A status read already dispatched before STOP may finish, like the original adapter's
        // separate IO job. It must not hold up the poll's next inactive-lifecycle check.
        assertTrace("description:started", "slow:flag")
        move(owner, Lifecycle.State.STARTED)
        compose.runOnIdle { pending.complete("completed synthetic status") }
        advanceBy(2_100)
        assertTrace("description:started", "slow:flag", "description:finished")
        compose.onNodeWithText("completed synthetic status").assertIsDisplayed()
    }

    @Test fun selectionAndAddressChangesBindAgainWithRetainedCallbacks() {
        val owner = owner(Lifecycle.State.STARTED)
        var item by mutableStateOf(row("same-callbacks"))
        compose.setContent { Theme(owner) { DnsEndpointRow(item, Dispatchers.Unconfined) } }
        assertTrace("same-callbacks:description", "same-callbacks:flag")
        compose.runOnIdle { item = item.copy(selected = false) }
        compose.runOnIdle { item = item.copy(address = "changed synthetic address") }
        compose.runOnIdle {
            assertEquals(3, trace.count { it == "same-callbacks:description" })
            assertEquals(3, trace.count { it == "same-callbacks:flag" })
        }
    }

    @Test fun unrelatedRecompositionDoesNotRebindAnUnchangedRow() {
        val owner = owner(Lifecycle.State.STARTED)
        val item = row("unchanged")
        var revision by mutableStateOf(0)
        compose.setContent { Theme(owner) {
            androidx.compose.material3.Text("Unrelated revision $revision")
            DnsEndpointRow(item, Dispatchers.Unconfined)
        } }
        assertTrace("unchanged:description", "unchanged:flag")
        compose.runOnIdle { revision++ }
        assertTrace("unchanged:description", "unchanged:flag")
    }

    @Test fun parentRedrawDoesNotManufactureNewCallbacksFromTheSamePagedEntity() {
        val owner = owner(Lifecycle.State.STARTED)
        val endpoint = FakeEndpoint(7, "paged")
        val factory = FakeRowFactory()
        var revision by mutableStateOf(0)
        compose.setContent { Theme(owner) {
            androidx.compose.material3.Text("Parent revision $revision")
            DnsEndpointRow(rememberDnsEndpointRow(endpoint, factory::bind), Dispatchers.Unconfined)
        } }
        assertTrace("paged:description", "paged:flag")
        compose.runOnIdle { revision++ }
        compose.onNodeWithText("Parent revision 1").assertIsDisplayed()
        assertTrace("paged:description", "paged:flag")
        move(owner, Lifecycle.State.CREATED)
        move(owner, Lifecycle.State.STARTED)
        compose.runOnIdle { revision++ }
        assertTrace("paged:description", "paged:flag")
    }

    @Test fun equalIdPagedReplacementStillBindsChangedDataAndCallbacks() {
        val owner = owner(Lifecycle.State.STARTED)
        // DNSCrypt/relay/proxy entity equality compares only id; equality-based remember would
        // silently discard a replacement's changed explanation, address or callback captures.
        var endpoint by mutableStateOf(FakeEndpoint(7, "before"), referentialEqualityPolicy())
        val factory = FakeRowFactory()
        compose.setContent { Theme(owner) {
            DnsEndpointRow(rememberDnsEndpointRow(endpoint, factory::bind), Dispatchers.Unconfined)
        } }
        assertTrace("before:description", "before:flag")
        compose.runOnIdle {
            val replacement = FakeEndpoint(7, "after")
            assertEquals(endpoint, replacement)
            endpoint = replacement
        }
        assertTrace("before:description", "before:flag", "after:description", "after:flag")
        compose.onNodeWithText("after description").assertIsDisplayed()
        compose.onNodeWithText("after flag").assertIsDisplayed()
    }

    @Test fun replacingTheFactoryRebindsTheSameSourceEntityWithItsNewCallbacks() {
        val owner = owner(Lifecycle.State.STARTED)
        val endpoint = FakeEndpoint(7, "source")
        var factory by mutableStateOf(FakeRowFactory("old "))
        compose.setContent { Theme(owner) {
            DnsEndpointRow(rememberDnsEndpointRow(endpoint, factory::bind), Dispatchers.Unconfined)
        } }
        assertTrace("old source:description", "old source:flag")
        compose.runOnIdle { factory = FakeRowFactory("new ") }
        assertTrace("old source:description", "old source:flag", "new source:description", "new source:flag")
    }

    @Test fun replacingOrRemovingARowCancelsItsInFlightWork() {
        val owner = owner(Lifecycle.State.STARTED)
        val pending = CompletableDeferred<String?>()
        var shown by mutableStateOf(true)
        var item by mutableStateOf(row("old").copy(flag = {
            trace += "old:flag-started"
            try { pending.await() } finally { trace += "old:flag-cancelled" }
        }))
        compose.setContent { Theme(owner) { if (shown) DnsEndpointRow(item, Dispatchers.Unconfined) } }
        assertTrace("old:description", "old:flag-started")
        compose.runOnIdle { item = row("new", polling = true) }
        compose.runOnIdle {
            assertTrue(trace.contains("old:flag-cancelled"))
            assertEquals(1, trace.count { it == "new:description" })
            assertEquals(1, trace.count { it == "new:flag" })
            shown = false
        }
        advanceBy(2_100)
        compose.runOnIdle {
            pending.complete("discarded fake flag")
            assertEquals(1, trace.count { it == "new:description" })
        }
        compose.onNodeWithText("discarded fake flag").assertDoesNotExist()
    }

    @Test fun destroyingTheOwnerCancelsPendingFlagWorkWithoutACompositionChange() {
        val owner = owner(Lifecycle.State.STARTED)
        val pending = CompletableDeferred<String?>()
        val item = row("destroyed").copy(flag = {
            trace += "flag:started"
            try { pending.await() } finally { trace += "flag:cancelled" }
        })
        compose.setContent { Theme(owner) { DnsEndpointRow(item, Dispatchers.Unconfined) } }
        assertTrace("destroyed:description", "flag:started")
        move(owner, Lifecycle.State.DESTROYED)
        assertTrace("destroyed:description", "flag:started", "flag:cancelled")
    }

    private fun row(label: String, polling: Boolean = false) = DnsEndpointRowState(
        id = 7, name = "Synthetic endpoint", selected = true, deletable = false,
        address = "synthetic address", pollStatus = polling,
        description = { trace += "$label:description"; "$label description" },
        flag = { trace += "$label:flag"; "$label flag" }, onSelect = {}, onInfo = {}
    )

    private inner class FakeRowFactory(private val prefix: String = "") {
        fun bind(endpoint: FakeEndpoint): DnsEndpointRowState = row(prefix + endpoint.label)
    }
    private class FakeEndpoint(val id: Int, val label: String) {
        override fun equals(other: Any?): Boolean = other is FakeEndpoint && id == other.id
        override fun hashCode(): Int = id
    }

    private fun owner(state: Lifecycle.State) = FakeOwner().also { move(it, state) }
    private fun move(owner: FakeOwner, state: Lifecycle.State) {
        compose.runOnIdle { owner.registry.currentState = state }
    }
    private fun advanceBy(millis: Long) {
        compose.runOnIdle { shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis)) }
        compose.waitForIdle()
    }
    private fun assertTrace(vararg expected: String) {
        compose.runOnIdle { assertEquals(expected.toList(), trace.toList()) }
    }
    private class FakeOwner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
    @Composable private fun Theme(owner: LifecycleOwner, content: @Composable () -> Unit) {
        val base = LocalContext.current
        val themed = remember(base) { ContextThemeWrapper(base, R.style.AppThemeWhite) }
        CompositionLocalProvider(LocalContext provides themed, LocalLifecycleOwner provides owner) {
            RethinkTheme(content)
        }
    }
}
