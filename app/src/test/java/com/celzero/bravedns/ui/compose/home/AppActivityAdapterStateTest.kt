/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.home

import com.celzero.bravedns.ui.adapter.AppActivityAdapter
import com.celzero.bravedns.ui.adapter.AppActivityEntry
import com.celzero.bravedns.ui.adapter.AppActivitySummary
import org.junit.Assert.*
import org.junit.Test

/** Synthetic summaries and callback recorders; no Context, repository or native engine. */
class AppActivityAdapterStateTest {
    private val app = AppActivitySummary(101, "Synthetic", 10, 7, 3)
    private fun child(index: Int) = AppActivityEntry("domain$index.invalid", 2, 1, 0, "")
    @Test fun submitIsSilentAndExpansionRequestsOnceUntilCollapsed() {
        val calls = mutableListOf<Int>()
        val adapter = AppActivityAdapter(false, iconResolver = { _, _ -> error("Unexpected icon lookup") }) { calls += it.uid }
        adapter.submit(listOf(app)); assertTrue(calls.isEmpty()); assertEquals(1, adapter.itemCount)
        adapter.onGroupClicked(app); adapter.onGroupClicked(app)
        assertEquals(listOf(101), calls); assertEquals(1, adapter.itemCount)
    }
    @Test fun lateChildrenCacheWhileCollapsedAndNextExpansionDoesNotReload() {
        val calls = mutableListOf<Int>(); val adapter = AppActivityAdapter(false) { calls += it.uid }
        adapter.submit(listOf(app)); adapter.onGroupClicked(app); adapter.onGroupClicked(app)
        adapter.setChildren(app.uid, listOf(child(1), child(2))); assertEquals(1, adapter.itemCount)
        adapter.onGroupClicked(app)
        assertEquals(3, adapter.itemCount); assertEquals(listOf(101), calls)
        adapter.onGroupClicked(app); assertEquals(1, adapter.itemCount)
    }
    @Test fun newWindowClearsCachedChildrenAndRequestsFreshData() {
        val calls = mutableListOf<Int>(); val adapter = AppActivityAdapter(false) { calls += it.uid }
        adapter.submit(listOf(app)); adapter.onGroupClicked(app); adapter.setChildren(app.uid, listOf(child(1)))
        assertEquals(2, adapter.itemCount)
        adapter.submit(listOf(app.copy(total = 20))); assertEquals(1, adapter.itemCount)
        adapter.onGroupClicked(app)
        assertEquals(listOf(101, 101), calls)
    }
    @Test fun childCapAndReplacementNeverDuplicateOldRows() {
        val adapter = AppActivityAdapter(false) {}
        adapter.submit(listOf(app)); adapter.onGroupClicked(app)
        adapter.setChildren(app.uid, (1..30).map(::child)); assertEquals(21, adapter.itemCount)
        adapter.setChildren(app.uid, listOf(child(99))); assertEquals(2, adapter.itemCount)
    }
    @Test fun absentSummaryCannotRequestAnExpansion() {
        val adapter = AppActivityAdapter(false) { error("Unexpected expansion") }
        adapter.onGroupClicked(app); assertEquals(0, adapter.itemCount)
    }
    @Test fun cachedEmptyGroupDoesNotReloadOnAnotherExpansion() {
        var calls = 0; val adapter = AppActivityAdapter(false) { calls++ }
        adapter.submit(listOf(app)); adapter.onGroupClicked(app)
        adapter.setChildren(app.uid, emptyList()); adapter.onGroupClicked(app); adapter.onGroupClicked(app)
        assertEquals(1, calls); assertEquals(1, adapter.itemCount)
    }
}
