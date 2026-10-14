/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.dns

import com.celzero.bravedns.R
import com.celzero.bravedns.data.BlockFreeDnsItem
import com.celzero.bravedns.data.BlockFreeDnsType
import org.junit.Assert.*
import org.junit.Test

/** Pure presentation state and recording callbacks. No Android host, preferences or services. */
class BlockFreeChipGroupTest {
    @Test fun sevenStableIdsHaveExactlyTheXmlOrder() {
        assertEquals(listOf(R.id.bfd_chip_all, R.id.bfd_chip_rethink, R.id.bfd_chip_doh, R.id.bfd_chip_dot,
            R.id.bfd_chip_dnscrypt, R.id.bfd_chip_odoh, R.id.bfd_chip_dns_proxy), BlockFreeChipGroup().ids)
    }
    @Test fun initializationDoesNotDispatchAndSelectionRemainsRequired() {
        val group = BlockFreeChipGroup(); val calls = mutableListOf<List<Int>>()
        group.setOnCheckedStateChangeListener { _, values -> calls += values }
        assertEquals(R.id.bfd_chip_all, group.checkedId)
        group.check(R.id.bfd_chip_all); group.check(-1)
        assertTrue(calls.isEmpty()); assertEquals(R.id.bfd_chip_all, group.checkedId)
    }
    @Test fun eachDistinctClickDispatchesExactlyOneSingleSelection() {
        val group = BlockFreeChipGroup(); val calls = mutableListOf<List<Int>>()
        group.setOnCheckedStateChangeListener { _, values -> calls += values }
        group.ids.drop(1).forEach { group.check(it); group.check(it) }
        group.check(R.id.bfd_chip_all)
        assertEquals(group.ids.drop(1).plus(R.id.bfd_chip_all).map { listOf(it) }, calls)
    }
    @Test fun recreationRestorationDispatchesTheInstalledCallbackOnce() {
        val before = BlockFreeChipGroup().apply { check(R.id.bfd_chip_odoh) }
        val after = BlockFreeChipGroup(); val calls = mutableListOf<List<Int>>()
        after.setOnCheckedStateChangeListener { _, values -> calls += values }
        after.restore(before.checkedId); after.restore(before.checkedId)
        assertEquals(listOf(listOf(R.id.bfd_chip_odoh)), calls)
        assertEquals(R.id.bfd_chip_odoh, after.checkedId)
    }
    @Test fun restoredAllDoesNotAddAnInitializationCallback() {
        val group = BlockFreeChipGroup(); var calls = 0
        group.setOnCheckedStateChangeListener { _, _ -> calls++ }
        group.restore(R.id.bfd_chip_all)
        assertEquals(0, calls)
    }
    @Test fun rowSubmissionAndKeyChangeDoNotDispatchSelectionCallbacks() {
        var calls = 0
        val state = BlockFreeDnsListState("") { calls++ }
        val item = BlockFreeDnsItem("DOH::https://example.org/", BlockFreeDnsType.DOH, "Fake DNS", "https://example.org/")
        state.submitList(listOf(item)); state.updateSelectedKey(item.key)
        assertEquals(0, calls); assertEquals(item.key, state.selectedKey)
        state.onItemSelected(item); assertEquals(1, calls)
    }
}
