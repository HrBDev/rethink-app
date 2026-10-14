package com.celzero.bravedns.ui.compose.wireguard

import com.celzero.bravedns.data.SsidItem
import com.celzero.bravedns.data.SsidItem.SsidType
import org.junit.Assert.*
import org.junit.Test

class WgSsidStateTest {
    private fun item(name: String, type: SsidType = SsidType.EQUAL_WILDCARD) = SsidItem(name, type)
    @Test fun appendPreservesOrder() {
        val state = WgSsidPresenter(mutableListOf(item("a"))) { }
        state.addSsidItem(item("b")); state.addSsidItem(item("c"))
        assertEquals(listOf("a", "b", "c"), state.getSsidItems().map { it.name })
    }
    @Test fun exactDuplicateIsIgnored() {
        val state = WgSsidPresenter(mutableListOf(item("a"))) { }
        state.addSsidItem(item("a"))
        assertEquals(1, state.getSsidItems().size)
    }
    @Test fun presenterKeepsDifferentTypeUntilControllerResolvesIt() {
        val state = WgSsidPresenter(mutableListOf(item("a"))) { }
        state.addSsidItem(item("a", SsidType.NOTEQUAL_EXACT))
        assertEquals(2, state.getSsidItems().size)
    }
    @Test fun removeOnlyFirstEqualEntry() {
        val state = WgSsidPresenter(mutableListOf(item("a"), item("b"), item("a"))) { }
        state.removeSsidItem(item("a"))
        assertEquals(listOf("b", "a"), state.getSsidItems().map { it.name })
    }
    @Test fun missingRemovalDoesNotChangeList() {
        val state = WgSsidPresenter(mutableListOf(item("a"))) { }
        state.removeSsidItem(item("b"))
        assertEquals(listOf(item("a")), state.getSsidItems())
    }
    @Test fun returnedListIsAnIndependentSnapshot() {
        val state = WgSsidPresenter(mutableListOf(item("a"))) { }
        val snapshot = state.getSsidItems()
        state.addSsidItem(item("b"))
        assertEquals(listOf(item("a")), snapshot)
    }
}
