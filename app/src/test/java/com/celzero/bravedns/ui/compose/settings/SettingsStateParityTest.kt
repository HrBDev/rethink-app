/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.settings

import android.app.Application
import android.os.Bundle
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Pure presentation and synthetic callback traces. No production controllers or services. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class SettingsStateParityTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Test fun advancedRestoresChecksAfterListenerRegistrationWithoutPersistingTransientLabels() {
        val old = AdvancedSettingsState(context)
        old.dvExperimentalSwitch.isChecked = true
        old.dvAutoDialSwitch.isChecked = true
        old.goMaxMemorySeekbar.progress = 2
        old.settingsResetRpnTourDesc.text = "transient success"
        val saved = Bundle().also(old::saveChecks)
        val restored = AdvancedSettingsState(context)
        val calls = mutableListOf<String>()
        restored.dvExperimentalSwitch.setOnCheckedChangeListener { _, checked -> calls += "experimental:$checked" }
        restored.dvAutoDialSwitch.setOnCheckedChangeListener { _, checked -> calls += "dial:$checked" }
        restored.restoreChecks(saved)
        assertEquals(listOf("experimental:true", "dial:true"), calls)
        assertEquals(2, restored.goMaxMemorySeekbar.progress)
        assertNotEquals("transient success", restored.settingsResetRpnTourDesc.text)
    }
    @Test fun unchangedRestoredChecksDoNotDispatch() {
        val state = AdvancedSettingsState(context)
        val saved = Bundle().also(state::saveChecks)
        state.dvExperimentalSwitch.setOnCheckedChangeListener { _, _ -> fail("equal selection dispatched") }
        state.restoreChecks(saved)
    }
    @Test fun sliderProgrammaticRestoreIsSilentAndUserChangesAreBounded() {
        val slider = SettingsSeekBar()
        val calls = mutableListOf<Int>()
        slider.setOnSeekBarChangeListener(object : SettingsSeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SettingsSeekBar?, progress: Int, fromUser: Boolean) { assertTrue(fromUser); calls += progress }
            override fun onStartTrackingTouch(seekBar: SettingsSeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SettingsSeekBar?) {}
        })
        slider.progress = 2
        assertTrue(calls.isEmpty())
        slider.userProgress(2); slider.userProgress(99); slider.userProgress(-1)
        assertEquals(listOf(5, 0), calls)
    }
    @Test fun homeRadiosRestoreIndependentCheckedValuesInXmlOrder() {
        val state = HomeModeState(context)
        // Test only state replay; callbacks are local fakes and cannot change VPN mode.
        val calls = mutableListOf<String>()
        state.bsHomeScreenRadioDns.setOnCheckedChangeListener { _, v -> calls += "dns:$v" }
        state.bsHomeScreenRadioFirewall.setOnCheckedChangeListener { _, v -> calls += "firewall:$v" }
        state.bsHomeScreenRadioDnsFirewall.setOnCheckedChangeListener { _, v -> calls += "both:$v" }
        val saved = Bundle().apply {
            putBoolean("compose.homemode.0", true)
            putBoolean("compose.homemode.1", true)
            putBoolean("compose.homemode.2", true)
        }
        state.restoreChecks(saved)
        assertEquals(listOf("dns:true", "firewall:true", "both:true"), calls)
    }
    @Test fun homeNestedUnchecksPreserveCompoundButtonBroadcasting() {
        val state = HomeModeState(context)
        val calls = mutableListOf<String>()
        state.bsHomeScreenRadioDns.isChecked = true
        state.bsHomeScreenRadioDns.setOnCheckedChangeListener { _, v -> calls += "dns:$v" }
        state.bsHomeScreenRadioFirewall.setOnCheckedChangeListener { control, v ->
            calls += "firewall:$v"
            if (v) { state.bsHomeScreenRadioDns.isChecked = false; control.isChecked = false }
        }
        state.bsHomeScreenRadioFirewall.isChecked = true
        assertEquals(listOf("firewall:true", "dns:false"), calls)
        assertFalse(state.bsHomeScreenRadioFirewall.isChecked)
    }
    @Test fun blockFreeRestoreDoesNotPersistOrInvokeRows() {
        val state = BlockFreeModeState(context)
        state.bfdmOptionFallbackRb.isChecked = true
        val saved = Bundle().also(state::saveChecks)
        val restored = BlockFreeModeState(context)
        restored.bfdmOptionFallbackRl.setOnClickListener { fail("state restore clicked row") }
        restored.restoreChecks(saved)
        assertTrue(restored.bfdmOptionFallbackRb.isChecked)
    }
    @Test fun universalBackgroundSwitchUserClickTogglesBeforeCallback() {
        val state = UniversalFirewallState()
        val calls = mutableListOf<Boolean>()
        state.firewallBackgroundModeCheck.setOnClickListener { calls += it.isChecked }
        state.firewallBackgroundModeCheck.performClick()
        state.firewallBackgroundModeCheck.performClick()
        assertEquals(listOf(true, false), calls)
    }
    @Test fun antiCensorshipIndependentRadiosPreserveNestedCallbacks() {
        val state = AntiCensorshipState()
        val calls = mutableListOf<String>()
        state.acRadioNeverSplit.isChecked = true
        state.acRadioNeverSplit.setOnCheckedChangeListener { _, v -> calls += "never:$v" }
        state.acRadioSplitAuto.setOnCheckedChangeListener { _, v -> calls += "auto:$v"; if (v) state.acRadioNeverSplit.isChecked = false }
        state.acRadioSplitAuto.isChecked = true
        state.acRadioSplitAuto.isChecked = true
        assertEquals(listOf("auto:true", "never:false"), calls)
    }
}
