/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.networksheets

import android.app.Application
import android.os.Bundle
import android.view.View
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Only pure presentation state and local fake traces. No controller, services or persistence. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class NetworkSheetStateTest {
    private val context get() = RuntimeEnvironment.getApplication()
    @Test fun radioChecksBeforeClickAndDoesNotToggleOffOrSetSelected() {
        val b = OrbotSheetState(context)
        val calls = mutableListOf<String>()
        b.bsOrbotRadioSocks5.setOnCheckedChangeListener { _, checked -> calls += "checked:$checked" }
        b.bsOrbotRadioSocks5.setOnClickListener { calls += "click:${it.isChecked}:${it.isSelected}" }
        clickSheetRadio(b.bsOrbotRadioSocks5)
        clickSheetRadio(b.bsOrbotRadioSocks5)
        assertEquals(listOf("checked:true", "click:true:false", "click:true:false"), calls)
    }
    @Test fun rowClickDoesNotPretoggleButRadioClickDoes() {
        val b = OrbotSheetState(context)
        val calls = mutableListOf<Boolean>()
        b.bsSocks5OrbotRl.setOnClickListener { calls += b.bsOrbotRadioSocks5.isChecked }
        b.bsOrbotRadioSocks5.setOnClickListener { calls += it.isChecked }
        b.bsSocks5OrbotRl.performClick()
        clickSheetRadio(b.bsOrbotRadioSocks5)
        assertEquals(listOf(false, true), calls)
    }
    @Test fun loadingClickGateAndPermanentlyHiddenBothRemainDistinct() {
        val b = OrbotSheetState(context)
        assertEquals(View.GONE, b.bsOrbotBothRl.visibility)
        b.bsOrbotRadioHttp.setOnClickListener { fail("loading radio dispatched") }
        b.bsOrbotRadioHttp.isClickable = false
        clickSheetRadio(b.bsOrbotRadioHttp)
        assertFalse(b.bsOrbotRadioHttp.isChecked)
        assertTrue(b.bsOrbotRadioHttp.isEnabled)
    }
    @Test fun restoresIndependentChecksInXmlOrderWithoutStartingAnimation() {
        val old = OrbotSheetState(context)
        old.bsOrbotRadioHttp.isChecked = true; old.bsOrbotRadioSocks5.isChecked = true
        old.bsOrbotRadioBoth.isChecked = true; old.bsOrbotRadioNone.isChecked = true
        old.animating = true; old.orbotStatus.text = "transient"
        val saved = Bundle().also(old::saveChecks)
        val restored = OrbotSheetState(context)
        val calls = mutableListOf<Int>()
        listOf(restored.bsOrbotRadioHttp, restored.bsOrbotRadioSocks5, restored.bsOrbotRadioBoth, restored.bsOrbotRadioNone).forEachIndexed { i, radio ->
            radio.setOnCheckedChangeListener { _, _ -> calls += i }
        }
        restored.restoreChecks(saved)
        restored.restoreChecks(saved)
        assertEquals(listOf(0, 1, 2, 3), calls)
        assertFalse(restored.animating)
        assertNotEquals("transient", restored.orbotStatus.text.toString())
    }
    @Test fun backupRestoresOnlySelectableVersionAndNeverCallsActions() {
        val original = BackupRestoreSheetState(context).apply { brbsAppVersion.text = "synthetic 1.0" }
        val restored = BackupRestoreSheetState(context)
        restored.brbsBackup.setOnClickListener { fail("restoration dispatched backup") }
        restored.brbsRestore.setOnClickListener { fail("restoration dispatched restore") }
        restored.restoreText(Bundle().also(original::saveText))
        assertEquals("synthetic 1.0", restored.brbsAppVersion.text.toString())
    }
    @Test fun countrySelectionIsReadAtRebindAndEveryExplicitClickDispatchesOnce() {
        val state = ProxyCountriesSheetState(context)
        var source = "AA"
        val calls = mutableListOf<String>()
        state.selection = { source }
        state.onClick = { calls += it; source = it }
        assertEquals("AA", state.selectedCountry())
        state.click("BB"); state.click("BB")
        assertEquals(listOf("BB", "BB"), calls)
        assertEquals("BB", state.selectedCountry())
    }
}
