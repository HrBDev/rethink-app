/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.home

import org.junit.Assert.*
import org.junit.Test

/** Strict callback recorders only; these tests cannot reach a VPN/permission controller. */
class HomeDialogTest {
    @Test fun repeatedPositiveThenNegativeDispatchesOnlyTheFirstTerminalAction() {
        val events = mutableListOf<String>()
        val yes = HomeDialogAction("Proceed", -1) { _, _ -> events += "proceed" }
        val no = HomeDialogAction("Cancel", -2) { _, _ -> events += "cancel" }
        val dialog = HomeDialog("title", "message", false, listOf(yes, no)) { events += "dismiss" }
        dialog.choose(yes); dialog.choose(yes); dialog.choose(no); dialog.cancel()
        assertEquals(listOf("proceed", "dismiss"), events)
    }
    @Test fun noncancelableDialogIgnoresOutsideAndBackButRetainsNegativeAction() {
        val events = mutableListOf<String>()
        val no = HomeDialogAction("Dismiss", -2) { _, _ -> events += "negative" }
        val dialog = HomeDialog("title", "message", false, listOf(no)) { events += "dismiss" }
        dialog.cancel(); assertTrue(events.isEmpty())
        dialog.choose(no)
        assertEquals(listOf("negative", "dismiss"), events)
    }
    @Test fun cancellationIsTerminalOnlyForThisDialog() {
        val events = mutableListOf<String>()
        val action = HomeDialogAction("Go", -1) { _, _ -> events += "action" }
        val first = HomeDialog("first", "", true, listOf(action)) { events += "first dismissed" }
        val next = HomeDialog("next", "", false, listOf(action)) { events += "next dismissed" }
        first.cancel(); first.cancel(); first.choose(action); next.choose(action)
        assertEquals(listOf("first dismissed", "action", "next dismissed"), events)
    }
    @Test fun actionRunsBeforeDismissAndReplacementSurvives() {
        val open = mutableListOf<HomeDialog>()
        val replacement = HomeDialog("replacement", "", false, emptyList()) { open.remove(it) }
        val action = HomeDialogAction("Go", -1) { current, _ ->
            assertTrue(open.contains(current)); open += replacement
        }
        val original = HomeDialog("original", "", false, listOf(action)) { open.remove(it) }
        open += original; original.choose(action)
        assertEquals(listOf(replacement), open)
    }
    @Test fun failedActionStillDismissesAndCannotBeRepeated() {
        var calls = 0; var dismissed = 0
        val action = HomeDialogAction("Go", -1) { _, _ -> calls++; error("synthetic") }
        val dialog = HomeDialog("title", "", false, listOf(action)) { dismissed++ }
        assertThrows(IllegalStateException::class.java) { dialog.choose(action) }
        dialog.choose(action)
        assertEquals(1, calls); assertEquals(1, dismissed)
    }
    @Test fun unrelatedButtonCannotInvokeAnAction() {
        val action = HomeDialogAction("Foreign", -1) { _, _ -> error("Unexpected callback") }
        val dialog = HomeDialog("title", "", false, emptyList()) { error("Unexpected dismissal") }
        dialog.choose(action)
    }
}
