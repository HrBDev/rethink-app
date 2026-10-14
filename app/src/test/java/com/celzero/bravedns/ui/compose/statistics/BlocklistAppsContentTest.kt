/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.statistics

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.ColorDrawable
import android.view.ContextThemeWrapper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.adapter.BlocklistAppsAdapter
import com.celzero.bravedns.data.AppConnection
import com.celzero.bravedns.ui.compose.RethinkTheme
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Native expanded rows with memory-only child fetches and an explicit fake identity provider. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BlocklistAppsContentTest {
    @get:Rule val compose = createComposeRule()
    private val app = AppConnection(10001, "", 0, 3, "", true, "Synthetic app")
    private val domain = app.copy(appOrDnsName = "example.test", count = 2)
    @Test fun expandedDomainsCacheWhileCollapsedAndSubmissionResetsThem() {
        val owner = FakeOwner(ApplicationProvider.getApplicationContext())
        val calls = CopyOnWriteArrayList<Int>()
        val pending = CompletableDeferred<List<AppConnection>>()
        val adapter = BlocklistAppsAdapter(owner, { uid -> assertEquals(10001, uid); "Fake identity" to ColorDrawable(0xff123456.toInt()) }) { uid -> calls += uid; pending.await() }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED; adapter.submitApps(listOf(app)) }
        compose.setContent { Theme { adapter.GroupAt(0) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("blocklist_app_10001").performClick()
        compose.waitUntil(5_000) { compose.runOnIdle { calls.size == 1 } }
        compose.onNodeWithTag("blocklist_app_10001").performClick()
        compose.runOnIdle { pending.complete(listOf(domain)) }
        // The fake IO result is committed on Main.immediate. Drain Robolectric's paused
        // main queue while polling instead of merely waiting for wall-clock time to pass.
        compose.waitUntil(5_000) { compose.runOnIdle { cachedDomains(adapter) != null } }
        compose.onNodeWithTag("blocklist_app_10001").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("example.test").fetchSemanticsNodes().size == 1 }
        compose.onNodeWithText("example.test").assertIsDisplayed()
        compose.onNodeWithTag("blocklist_app_10001").performClick()
        compose.onNodeWithText("example.test").assertDoesNotExist()
        compose.onNodeWithTag("blocklist_app_10001").performClick()
        compose.onNodeWithText("example.test").assertIsDisplayed()
        compose.runOnIdle { assertEquals(listOf(10001), calls.toList()); adapter.submitApps(listOf(app)) }
        compose.onNodeWithText("example.test").assertDoesNotExist()
        compose.onNodeWithTag("blocklist_app_10001").performClick()
        compose.waitUntil(5_000) { compose.runOnIdle { calls.size == 2 } }
        waitForDomainFetchesToFinish(owner)
        compose.runOnIdle { assertEquals(listOf(10001, 10001), calls.toList()) }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.DESTROYED }
    }
    @Test fun lateFetchFromPreviousTagSubmissionCannotPopulateTheReplacementGroup() {
        val owner = FakeOwner(ApplicationProvider.getApplicationContext())
        val calls = CopyOnWriteArrayList<Int>()
        val pending = CompletableDeferred<List<AppConnection>>()
        val adapter = BlocklistAppsAdapter(owner, { null to ColorDrawable() }) { uid -> calls += uid; pending.await() }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED; adapter.submitApps(listOf(app)) }
        compose.setContent { Theme { adapter.GroupAt(0) } }
        compose.onNodeWithTag("blocklist_app_10001").performClick()
        compose.waitUntil(5_000) { compose.runOnIdle { calls.size == 1 } }
        compose.runOnIdle { adapter.submitApps(listOf(app.copy(count = 99))); pending.complete(listOf(domain)) }
        // Wait through the old fetch's Main delivery before asserting isolation. A single
        // Compose idle observation can otherwise precede the IO coroutine's resumption.
        waitForDomainFetchesToFinish(owner)
        compose.onNodeWithText("example.test").assertDoesNotExist()
        compose.onNodeWithText("99").assertIsDisplayed()
        compose.runOnIdle { assertEquals(listOf(10001), calls.toList()) }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.DESTROYED }
    }
    private fun waitForDomainFetchesToFinish(owner: FakeOwner) {
        compose.waitUntil(5_000) {
            compose.runOnIdle { owner.lifecycleScope.coroutineContext[Job]!!.children.none() }
        }
    }
    private fun cachedDomains(adapter: BlocklistAppsAdapter): List<*>? {
        val groups = BlocklistAppsAdapter::class.java.getDeclaredMethod("getGroups").apply { isAccessible = true }.invoke(adapter) as List<*>
        val group = groups.single()!!
        return group.javaClass.getDeclaredMethod("getDomains").apply { isAccessible = true }.invoke(group) as List<*>?
    }
    private class FakeOwner(base: Context) : ContextWrapper(base), LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
    @Composable private fun Theme(content: @Composable () -> Unit) {
        val base = LocalContext.current
        val context = remember(base) { ContextThemeWrapper(base, R.style.AppThemeWhite) }
        CompositionLocalProvider(LocalContext provides context) { RethinkTheme(content) }
    }
}
