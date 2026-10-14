package com.celzero.bravedns.ui.compose.rpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure callback fixtures: no Android application, manager, database, tunnel, or account. */
class RpnControlStateTest {
    @Test fun initializationBeforeListenerIsSilent() {
        val state = RpnControlState()
        state.isChecked = true
        var calls = 0
        state.setOnCheckedChangeListener { _, _ -> calls++ }
        state.isChecked = true
        assertEquals(0, calls)
        state.isChecked = false
        assertEquals(1, calls)
    }
    @Test fun quietListenerRemovalDoesNotWrite() {
        val state = RpnControlState(); var calls = 0
        state.setOnCheckedChangeListener { _, _ -> calls++ }
        state.setOnCheckedChangeListener(null)
        state.isChecked = true
        assertEquals(0, calls)
    }
    @Test fun reentrantAssignmentDoesNotBroadcastTwice() {
        val state = RpnControlState(); val values = mutableListOf<Boolean>()
        state.setOnCheckedChangeListener { node, checked -> values.add(checked); node.isChecked = false }
        state.isChecked = true
        assertEquals(listOf(true), values)
        assertFalse(state.isChecked)
    }
    @Test fun rowDispatchTogglesExactlyOnce() {
        val row = RpnControlState(); val box = RpnControlState(); var writes = 0
        box.setOnCheckedChangeListener { _, _ -> writes++ }
        row.setOnClickListener { box.isChecked = !box.isChecked }
        assertTrue(row.performClick()); assertTrue(box.isChecked); assertEquals(1, writes)
    }
    @Test fun disabledAndNonClickableControlsIgnoreTaps() {
        val state = RpnControlState(); var calls = 0
        state.setOnClickListener { calls++ }
        state.isEnabled = false; assertFalse(state.performClick())
        state.isEnabled = true; state.isClickable = false; assertFalse(state.performClick())
        assertEquals(0, calls)
    }
    @Test fun clickDoesNotImplicitlyToggleCheckedState() {
        val state = RpnControlState(); state.setOnClickListener {}
        state.performClick(); assertFalse(state.isChecked)
    }
    @Test fun requiredChoiceDoesNotDeselectSameOption() {
        val state = RpnChoiceState(); state.check(7); var calls = 0
        state.addOnButtonCheckedListener { _, _, _ -> calls++ }
        state.check(7); assertEquals(7, state.checkedId); assertEquals(0, calls)
    }
    @Test fun configurationCallbackWritesOnlyForNewTrueSelection() {
        val state = RpnChoiceState(); state.check(7)
        val events = mutableListOf<Pair<Int, Boolean>>(); val writes = mutableListOf<Int>()
        state.addOnButtonCheckedListener { _, id, checked -> events.add(id to checked); if (checked) writes.add(id) }
        state.check(9)
        assertEquals(listOf(7 to false, 9 to true), events)
        assertEquals(listOf(9), writes)
    }
}
