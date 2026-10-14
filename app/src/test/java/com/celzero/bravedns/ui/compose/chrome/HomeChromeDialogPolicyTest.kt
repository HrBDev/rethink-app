/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.chrome

import com.celzero.bravedns.ui.compose.chrome.HomeChromeDialog
import org.junit.Assert.assertEquals
import org.junit.Test

/** Production dialog state, not a reconstructed platform AlertDialog. No Android startup. */
class HomeChromeDialogPolicyTest {
    @Test fun modalDismissDoesNothingUntilExplicitAction() {
        val events = mutableListOf<String>()
        val dialog = HomeChromeDialog("Update", "Available", "Download", "Later", false,
            onPositive = { events += "download" }, onNegative = { events += "postpone" })
        dialog.dismiss { events += "dismiss" }
        assertEquals(emptyList<String>(), events)
        dialog.negative { events += "dismiss" }
        assertEquals(listOf("postpone", "dismiss"), events)
        dialog.positive { events += "dismiss" }
        assertEquals(listOf("postpone", "dismiss"), events)
    }
    @Test fun positiveRunsBeforeDismissAndOnlyOnce() {
        val events = mutableListOf<String>()
        val dialog = HomeChromeDialog("Restore", "Confirm", "Restore", "Cancel",
            onPositive = { events += "start worker"; events += "observe worker" })
        assertEquals(emptyList<String>(), events)
        repeat(2) { dialog.positive { events += "dismiss" } }
        assertEquals(listOf("start worker", "observe worker", "dismiss"), events)
    }
    @Test fun informationDismissDoesNotPerformButtonAction() {
        val events = mutableListOf<String>()
        val dialog = HomeChromeDialog("Info", "Current", "OK", onPositive = { events += "positive" })
        dialog.dismiss { events += "dismiss" }
        dialog.positive { events += "dismiss" }
        assertEquals(listOf("dismiss"), events)
    }
    @Test fun stackedDialogsKeepIndependentIdentity() {
        val events = mutableListOf<String>()
        val restore = HomeChromeDialog("Restore", "Confirm", "Restore", onPositive = { events += "restore" })
        val update = HomeChromeDialog("Update", "Available", "Download", onPositive = { events += "update" })
        val stack = mutableListOf(restore, update)
        update.positive { stack.remove(update) }
        assertEquals(listOf(restore), stack)
        assertEquals(listOf("update"), events)
        restore.dismiss { stack.remove(restore) }
        assertEquals(emptyList<HomeChromeDialog>(), stack)
        assertEquals(listOf("update"), events)
    }
}
