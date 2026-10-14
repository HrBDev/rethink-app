/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.trafficlogs

import org.junit.Assert.*
import org.junit.Test

/** No Android application, backend or real traffic needed for query-delivery contracts. */
class LogSearchStateTest {
    @Test fun argumentBeforeListenerDoesNotSeedFlowAndEqualRestoreDoesNotDeliver() {
        val search = LogSearchState(); val calls = mutableListOf<String>()
        search.setQuery("argument.test", true)
        search.listener = { calls += "change:$it" }; search.submitListener = { calls += "submit:$it" }
        search.setQuery("argument.test")
        assertEquals("argument.test", search.query)
        assertTrue(calls.isEmpty())
        search.setQuery("restored.test")
        assertEquals(listOf("change:restored.test"), calls)
    }
    @Test fun dnsArgumentAfterListenerDeliversChangeThenSubmitAndRestoredDraftLast() {
        val search = LogSearchState(); val calls = mutableListOf<String>()
        search.listener = { calls += "change:$it" }; search.submitListener = { calls += "submit:$it" }
        search.setQuery("argument.test", true); search.setQuery("restored.test")
        assertEquals(listOf("change:argument.test", "submit:argument.test", "change:restored.test"), calls)
    }
    @Test fun disabledListenerStillTracksOldQueryAndEmptySubmissionsAreSuppressed() {
        val search = LogSearchState(); val calls = mutableListOf<String>()
        search.setQuery("disabled.test")
        search.listener = { calls += it }; search.submitListener = { calls += "submit:$it" }
        search.setQuery("disabled.test")
        assertTrue(calls.isEmpty())
        search.setQuery("", true); search.submit(); search.setQuery("  ", true); search.submit()
        assertEquals(listOf("", "  "), calls)
    }
    @Test fun expandResetsOnlyOnceAndResumeClearDoesNotEmitTwice() {
        val search = LogSearchState(); val calls = mutableListOf<String>()
        search.listener = { calls += it }
        search.setQuery("argument.test"); search.expand(); search.setQuery("draft.test"); search.expand()
        assertEquals("draft.test", search.query)
        search.setQuery(""); search.setQuery("")
        assertEquals(listOf("argument.test", "", "draft.test", ""), calls)
    }
    @Test fun requiredCheckCallbackIsNotRedeliveredForEqualValuesAndClickTogglesBeforeListener() {
        val state = LogControlState(checkable = true); val calls = mutableListOf<String>()
        state.setOnCheckedChangeListener { _, checked -> calls += "changed:$checked" }
        state.setOnClickListener { calls += "clicked:${it.isChecked}" }
        state.isChecked = true; state.isChecked = true; state.performClick()
        assertEquals(listOf("changed:true", "changed:false", "clicked:false"), calls)
    }
}
