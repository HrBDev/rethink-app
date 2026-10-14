package com.celzero.bravedns.ui.compose.dnssettings

import org.junit.Assert.*
import org.junit.Test

class DnsControlTest {
    @Test fun initializationBeforeListenersHasNoWrites() {
        val state = DnsControl(); var calls = 0
        state.isChecked = true
        state.onCheckedChange = { _, _ -> calls++ }
        assertEquals(0, calls)
        assertTrue(state.isChecked)
    }
    @Test fun sameValueIsSuppressedAndChangesDispatchOnce() {
        val state = DnsControl(); val calls = mutableListOf<Boolean>()
        state.onCheckedChange = { _, value -> calls += value }
        state.isChecked = false; state.isChecked = true; state.isChecked = true; state.isChecked = false
        assertEquals(listOf(true, false), calls)
    }
    @Test fun sameWidgetReentrantMutationChangesCheckWithoutRedispatch() {
        val state = DnsControl(); val calls = mutableListOf<Boolean>()
        state.onCheckedChange = { control, value -> calls += value; control.isChecked = !value }
        state.isChecked = true
        assertEquals(listOf(true), calls); assertFalse(state.isChecked)
    }
    @Test fun crossWidgetCallbacksRemainNestedInOriginalOrder() {
        val first = DnsControl(); val second = DnsControl(); val calls = mutableListOf<String>()
        first.onCheckedChange = { _, value -> calls += "first-start:$value"; second.isChecked = value; calls += "first-end:$value" }
        second.onCheckedChange = { _, value -> calls += "second:$value"; first.isChecked = false }
        first.isChecked = true
        assertEquals(listOf("first-start:true", "second:true", "first-end:true"), calls)
        assertFalse(first.isChecked); assertTrue(second.isChecked)
    }
    @Test fun disabledControlStillAcceptsProgrammaticWrites() {
        val state = DnsControl(); var calls = 0
        state.isEnabled = false; state.isClickable = false
        state.onCheckedChange = { _, _ -> calls++ }; state.isChecked = true
        assertEquals(1, calls)
    }
    @Test fun callbackFailureReleasesReentrancyGuard() {
        val state = DnsControl(); var calls = 0
        state.onCheckedChange = { _, _ -> calls++; error("fake") }
        repeat(2) { try { state.isChecked = !state.isChecked } catch (_: IllegalStateException) {} }
        assertEquals(2, calls)
    }
    @Test fun stableRestoreOrderMatchesXmlHierarchy() {
        val s = DnsSettingsState()
        assertEquals(listOf(s.networkDnsRb, s.customDnsRb, s.rethinkPlusDnsRb, s.smartDnsRb, s.dcDownloaderSwitch, s.dcCheckUpdateSwitch, s.dcAlgSwitch, s.dcSplitDnsSwitch, s.dcFaviconSwitch, s.dcEnableCacheSwitch, s.dcProxyDnsSwitch, s.dcPreventDnsLeaksSwitch, s.dcBlockUnknownSwitch, s.dcUndelegatedDomainsSwitch), s.restoreOrder)
    }
}
