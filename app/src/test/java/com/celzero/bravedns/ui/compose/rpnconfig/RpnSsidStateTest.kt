/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.rpnconfig

import android.app.Application
import android.os.Bundle
import com.celzero.bravedns.data.SsidItem
import com.celzero.bravedns.data.SsidItem.SsidType.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Synthetic SSIDs only. No manager, permissions, traffic, repositories or Go initialization. */
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class RpnSsidStateTest {
    @Test fun defaultsAreConnectAndExact() {
        val state = RpnSsidState(); assertTrue(state.equal); assertTrue(state.exact); assertTrue(state.items.isEmpty())
    }
    @Test fun emptyInputDoesNotChangeDraftOrSelections() {
        val state = RpnSsidState().apply { name = " \t "; equal = false; exact = false }
        assertEquals(RpnSsidState.AddResult.EMPTY, state.add())
        assertEquals(" \t ", state.name); assertFalse(state.equal); assertFalse(state.exact); assertEquals(-1, state.scrollTo)
    }
    @Test fun duplicateNamesIgnoreCaseEvenWithDifferentTypes() {
        val state = RpnSsidState().apply { items.add(SsidItem("Cafe", EQUAL_EXACT)); name = "  cAFE  "; equal = false; exact = false }
        assertEquals(RpnSsidState.AddResult.DUPLICATE, state.add())
        assertEquals(listOf(SsidItem("Cafe", EQUAL_EXACT)), state.items.toList()); assertEquals("  cAFE  ", state.name)
        assertFalse(state.equal); assertFalse(state.exact)
    }
    @Test fun exactDuplicateDoesNotClearEditorOrScroll() {
        val state = RpnSsidState().apply { items.add(SsidItem("Cafe", EQUAL_EXACT)); name = "Cafe" }
        assertEquals(RpnSsidState.AddResult.DUPLICATE, state.add()); assertEquals("Cafe", state.name); assertEquals(-1, state.scrollTo)
    }
    @Test fun allFourCombinationsKeepRadioSelectionsAfterAdd() {
        val pairs = listOf(true to true, true to false, false to true, false to false)
        val actual = pairs.map { (equal, exact) ->
            val state = RpnSsidState().apply { name = "wifi"; this.equal = equal; this.exact = exact }
            assertEquals(RpnSsidState.AddResult.ADDED, state.add())
            assertEquals(equal, state.equal); assertEquals(exact, state.exact); assertEquals("", state.name)
            state.items.single().type
        }
        assertEquals(listOf(EQUAL_EXACT, EQUAL_WILDCARD, NOTEQUAL_EXACT, NOTEQUAL_WILDCARD), actual)
    }
    @Test fun trimAndAppendDoNotAddWireGuardLengthLimit() {
        val state = RpnSsidState().apply { items.add(SsidItem("First", EQUAL_EXACT)); name = "  " + "x".repeat(100) + "  " }
        assertEquals(RpnSsidState.AddResult.ADDED, state.add()); assertEquals(listOf("First", "x".repeat(100)), state.items.map { it.name }); assertEquals(1, state.scrollTo)
    }
    @Test fun repeatingAddOnClearedTextNeverInsertsAgain() {
        val state = RpnSsidState().apply { name = "Test" }
        assertEquals(RpnSsidState.AddResult.ADDED, state.add()); assertEquals(RpnSsidState.AddResult.EMPTY, state.add()); assertEquals(1, state.items.size)
    }
    @Test fun reusingAnIndexStillRequestsScrolling() {
        val state = RpnSsidState().apply { name = "one"; add(); items.clear(); name = "two"; add() }
        assertEquals(0, state.scrollTo); assertEquals(2, state.scrollRequest)
    }
    @Test fun restoresEditableFieldsButDoesNotNewlyPersistUnsavedItems() {
        val source = RpnSsidState().apply { name = "unfinished"; equal = false; exact = false; items.add(SsidItem("new", EQUAL_EXACT)) }
        val restored = RpnSsidState().apply { restore(Bundle().also(source::save)) }
        assertEquals("unfinished", restored.name); assertFalse(restored.equal); assertFalse(restored.exact)
        assertTrue(restored.items.isEmpty()); assertEquals(-1, restored.scrollTo)
    }
    @Test fun storageRoundTripRetainsActualDataFormatAndListOrder() {
        val source = listOf(SsidItem("Wifi", EQUAL_EXACT), SsidItem("Wifi", NOTEQUAL_EXACT), SsidItem("Last", EQUAL_WILDCARD))
        assertEquals(source, SsidItem.parseStorageList(SsidItem.toStorageList(source)))
    }
}
