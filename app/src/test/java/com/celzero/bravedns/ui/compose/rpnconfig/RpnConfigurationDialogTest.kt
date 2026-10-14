/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.rpnconfig

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.ContextWrapper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.celzero.bravedns.R
import com.celzero.bravedns.data.SsidItem
import com.celzero.bravedns.ui.dialog.RpnSsidDialog
import com.celzero.bravedns.ui.compose.wireguard.WgAlertDialogBuilder
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Only synthetic dialogs on Compose's ComponentActivity; no production Activity/controller. */
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class RpnConfigurationDialogTest {
    @get:Rule val compose = createComposeRule()

    @Test fun ssidSaveCallsCallbackThenDismissesOnceWithUnchangedOrder() {
        lateinit var host: Activity; lateinit var dialog: Dialog
        val events = mutableListOf<String>(); var saved = ""
        val original = listOf(SsidItem("Fake first", SsidItem.SsidType.EQUAL_EXACT), SsidItem("Fake last", SsidItem.SsidType.NOTEQUAL_WILDCARD))
        compose.setContent { host = generateSequence(LocalContext.current) { (it as? ContextWrapper)?.baseContext }.filterIsInstance<Activity>().first() }
        compose.runOnIdle {
            host.setTheme(R.style.AppThemeWhite)
            dialog = RpnSsidDialog(host, R.style.AppThemeWhite, "fake", "Fake Country", SsidItem.toStorageList(original)) { saved = it; events += "save" }
            dialog.setOnDismissListener { events += "dismiss" }; dialog.show()
        }
        compose.onNodeWithTag("rpn.ssid.save").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("save", "dismiss"), events); assertEquals(original, SsidItem.parseStorageList(saved)); assertFalse(dialog.isShowing) }
    }
    @Test fun ssidCancelNeverSaves() {
        lateinit var host: Activity; lateinit var dialog: Dialog; var saves = 0
        compose.setContent { host = generateSequence(LocalContext.current) { (it as? ContextWrapper)?.baseContext }.filterIsInstance<Activity>().first() }
        compose.runOnIdle {
            host.setTheme(R.style.AppThemeWhite)
            dialog = RpnSsidDialog(host, R.style.AppThemeWhite, "fake", "Fake Country", "[]") { saves++ }; dialog.show()
        }
        compose.onNodeWithTag("rpn.ssid.cancel").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(0, saves); assertFalse(dialog.isShowing) }
    }
    @Test fun nullNegativeCallbackDismissesWithoutPositiveDispatch() {
        lateinit var host: Activity; lateinit var dialog: Dialog; var positives = 0; var dismissals = 0
        compose.setContent { host = generateSequence(LocalContext.current) { (it as? ContextWrapper)?.baseContext }.filterIsInstance<Activity>().first() }
        compose.runOnIdle {
            host.setTheme(R.style.AppThemeWhite)
            dialog = WgAlertDialogBuilder(host, R.style.App_Dialog_NoDim).setTitle("Fake confirmation")
                .setPositiveButton("Proceed") { _, _ -> positives++ }.setNegativeButton("Cancel", null).create()
            dialog.setOnDismissListener { dismissals++ }; dialog.show()
        }
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle { assertEquals(0, positives); assertEquals(1, dismissals); assertFalse(dialog.isShowing) }
    }
}
