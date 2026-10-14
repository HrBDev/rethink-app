package com.celzero.bravedns.ui.compose.wireguard

import org.junit.Assert.*
import org.junit.Test

class WgControlStateTest {
    @Test fun checkedListenerRunsBeforeClick() {
        val node = WgControlState(checkable = true)
        val events = mutableListOf<String>()
        node.setOnCheckedChangeListener { _, checked -> events.add("checked:$checked") }
        node.setOnClickListener { events.add("click:${it.isChecked}") }
        node.performClick()
        assertEquals(listOf("checked:true", "click:true"), events)
    }
    @Test fun settingSameCheckedStateIsSilent() {
        val node = WgControlState(checkable = true)
        var calls = 0
        node.setOnCheckedChangeListener { _, _ -> calls++ }
        node.isChecked = false; node.isChecked = true; node.isChecked = true
        assertEquals(1, calls)
    }
    @Test fun reentrantRollbackNotifiesOnlyOnce() {
        val node = WgControlState(checkable = true)
        var calls = 0
        node.setOnCheckedChangeListener { control, _ -> calls++; control.isChecked = false }
        node.isChecked = true
        assertFalse(node.isChecked)
        assertEquals(1, calls)
    }
    @Test fun callOnClickDoesNotToggleMobileOnlyRow() {
        val node = WgControlState(checkable = true)
        var received = true
        node.setOnClickListener { received = it.isChecked }
        node.callOnClick()
        assertFalse(received)
        assertFalse(node.isChecked)
    }
    @Test fun programmaticClickDoesNotConsultEnabled() {
        val node = WgControlState(checkable = true)
        var calls = 0
        node.isEnabled = false; node.isClickable = false
        node.setOnClickListener { calls++ }
        node.performClick()
        assertTrue(node.isChecked)
        assertEquals(1, calls)
    }
    @Test fun ordinaryActionDoesNotToggle() {
        val node = WgControlState()
        var calls = 0
        node.setOnClickListener { calls++ }
        node.performClick(); node.performClick()
        assertFalse(node.isChecked)
        assertEquals(2, calls)
    }
    @Test fun nullCheckedListenerStopsDispatch() {
        val node = WgControlState(checkable = true)
        var calls = 0
        node.setOnCheckedChangeListener { _, _ -> calls++ }
        node.setOnCheckedChangeListener(null)
        node.isChecked = true
        assertEquals(0, calls)
    }
    @Test fun checkedListenerFailureDoesNotLeaveBroadcastingLatched() {
        val node = WgControlState(checkable = true)
        node.setOnCheckedChangeListener { _, _ -> error("synthetic") }
        try { node.isChecked = true } catch (_: IllegalStateException) { }
        var calls = 0
        node.setOnCheckedChangeListener { _, _ -> calls++ }
        node.isChecked = false
        assertEquals(1, calls)
    }
    @Test fun listenerCanBeReplacedWithoutDispatch() {
        val node = WgControlState()
        val events = mutableListOf<Int>()
        node.setOnClickListener { events.add(1) }
        node.setOnClickListener { events.add(2) }
        assertTrue(events.isEmpty())
        node.performClick()
        assertEquals(listOf(2), events)
    }
    @Test fun editTextSetTextNotifiesForRepeatedContent() {
        val node = WgControlState()
        val values = mutableListOf<String>()
        node.addTextChangedListener { values.add(it.toString()) }
        node.setText("demo"); node.setText("demo"); node.setText(null)
        assertEquals(listOf("demo", "demo", ""), values)
    }
}
