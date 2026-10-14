/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.networksettings

import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.MotionEvent
import android.view.ContextThemeWrapper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.text.style.StyleSpan
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.wireguard.WgControlState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Only plain-Application presentation state and local callback recorders. No production host/service. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class NetworkSettingsStateTest {
    private val context get() = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.AppThemeWhite)

    @Test fun miscellaneousRestoreReplaysChecksInOriginalHierarchyOrder() {
        val old = MiscSettingsState(context)
        val original = listOf(old.settingsActivityCheckUpdateSwitch, old.tombstoneAppSwitch,
            old.settingsFirewallBubbleSwitch, old.dvIpInfoSwitch, old.settingsActivityEnableLogsSwitch,
            old.settingsActivityAppNotificationSwitch, old.settingsActivityAppNotificationPersistentSwitch,
            old.settingsActivityAutoStartSwitch, old.settingsFirebaseErrorReportingSwitch)
        original.forEach { it.isChecked = true }
        val saved = Bundle().also(old::saveChecks)
        val fresh = MiscSettingsState(context)
        val controls = listOf(fresh.settingsActivityCheckUpdateSwitch, fresh.tombstoneAppSwitch,
            fresh.settingsFirewallBubbleSwitch, fresh.dvIpInfoSwitch, fresh.settingsActivityEnableLogsSwitch,
            fresh.settingsActivityAppNotificationSwitch, fresh.settingsActivityAppNotificationPersistentSwitch,
            fresh.settingsActivityAutoStartSwitch, fresh.settingsFirebaseErrorReportingSwitch)
        val calls = mutableListOf<Int>()
        controls.forEachIndexed { index, control -> control.setOnCheckedChangeListener { _, selected -> assertTrue(selected); calls += index } }
        fresh.restoreChecks(saved)
        assertEquals(controls.indices.toList(), calls)
        fresh.restoreChecks(saved)
        assertEquals(controls.size, calls.size)
    }
    @Test fun restoredNestedCorrectionSuppressesSelfRecursion() {
        val state = MiscSettingsState(context)
        val calls = mutableListOf<Boolean>()
        state.settingsFirewallBubbleSwitch.setOnCheckedChangeListener { control, checked -> calls += checked; if (checked) control.isChecked = false }
        state.settingsFirewallBubbleSwitch.isChecked = true
        assertEquals(listOf(true), calls)
        assertFalse(state.settingsFirewallBubbleSwitch.isChecked)
    }
    @Test fun originalRowAndCompoundButtonListenersRemainIndependent() {
        val state = MiscSettingsState(context)
        val calls = mutableListOf<String>()
        state.settingsActivityEnableLogsRl.setOnClickListener {
            calls += "row"
            state.settingsActivityEnableLogsSwitch.isChecked = !state.settingsActivityEnableLogsSwitch.isChecked
        }
        state.settingsActivityEnableLogsSwitch.setOnCheckedChangeListener { _, checked -> calls += "check:$checked" }
        state.settingsActivityEnableLogsRl.performClick()
        state.settingsActivityEnableLogsSwitch.performClick()
        assertEquals(listOf("row", "check:true", "check:false"), calls)
    }
    @Test fun programmaticToggleInvokesChangeBeforeOriginalClickListener() {
        val state = MiscSettingsState(context)
        val calls = mutableListOf<String>()
        state.settingsActivityAppNotificationSwitch.setOnCheckedChangeListener { _, checked -> calls += "changed:$checked" }
        state.settingsActivityAppNotificationSwitch.setOnClickListener { calls += "click:${it.isChecked}" }
        state.settingsActivityAppNotificationSwitch.performClick()
        assertEquals(listOf("changed:true", "click:true"), calls)
    }
    @Test fun tunnelSliderProgrammaticRestorationRetainsCallbacks() {
        val old = TunnelSettingsState(context)
        old.dvTimeoutSeekbar.progress = 4; old.dvSocketBufferSizeSeekbar.progress = 2
        val saved = Bundle().also(old::saveChecks)
        val fresh = TunnelSettingsState(context)
        val calls = mutableListOf<String>()
        fresh.dvTimeoutSeekbar.setOnSeekBarChangeListener(recorder(calls, "timeout"))
        fresh.dvSocketBufferSizeSeekbar.setOnSeekBarChangeListener(recorder(calls, "buffer"))
        fresh.restoreChecks(saved)
        assertEquals(listOf("timeout:4:false", "buffer:2:false"), calls)
    }
    @Test fun sliderGestureOrderingAndChangedValueSemanticsMatchSeekBar() {
        val slider = NetworkSeekState().apply { max = 7 }
        val calls = mutableListOf<String>(); slider.setOnSeekBarChangeListener(recorder(calls, "s"))
        slider.progress = 2; slider.progress = 2
        slider.startGesture(); slider.userProgress(4); slider.userProgress(4); slider.userProgress(99); slider.finishGesture(); slider.finishGesture()
        assertEquals(listOf("s:2:false", "s:start", "s:4:true", "s:7:true", "s:stop:7"), calls)
    }
    @Test fun keyboardAndAccessibilityProgressDoNotInventTouchCallbacks() {
        val slider = NetworkSeekState(); val calls = mutableListOf<String>()
        slider.setOnSeekBarChangeListener(recorder(calls, "s")); slider.userProgress(10)
        assertEquals(listOf("s:10:true"), calls)
    }
    @Test fun tappingUnchangedSliderStillStartsAndStopsGesture() {
        val slider = NetworkSeekState(); val calls = mutableListOf<String>()
        slider.setOnSeekBarChangeListener(recorder(calls, "s")); slider.startGesture(); slider.userProgress(0); slider.finishGesture()
        assertEquals(listOf("s:start", "s:stop:0"), calls)
    }
    @Test fun proxyChecksRestoreSocksBeforeHttpWithoutPersistingRefreshAnimation() {
        val old = ProxySettingsState(context)
        old.settingsActivitySocks5Switch.isChecked = true; old.settingsActivityHttpProxySwitch.isChecked = true
        old.startRefresh(old.rpnRefresh)
        val saved = Bundle().also(old::saveChecks); val fresh = ProxySettingsState(context); val calls = mutableListOf<String>()
        fresh.settingsActivitySocks5Switch.setOnCheckedChangeListener { _, _ -> calls += "socks" }
        fresh.settingsActivityHttpProxySwitch.setOnCheckedChangeListener { _, _ -> calls += "http" }
        fresh.restoreChecks(saved)
        assertEquals(listOf("socks", "http"), calls); assertTrue(fresh.refreshing.isEmpty())
    }
    @Test fun refreshIsIdentitySpecificAndDuplicateStartsDoNotDuplicateAnimation() {
        val state = ProxySettingsState(context)
        state.startRefresh(state.rpnRefresh); state.startRefresh(state.rpnRefresh); state.startRefresh(state.wgRefresh)
        assertEquals(2, state.refreshing.size); state.stopRefresh(state.rpnRefresh)
        assertEquals(listOf(state.wgRefresh), state.refreshing.toList())
    }
    @Test fun spinnerPreservesDefaultExplicitAndBlockedSelections() {
        val spinner = NetworkSpinnerState(); spinner.items = listOf("None", "Synthetic app")
        assertEquals("None", spinner.selectedItem); spinner.setSelection(1); assertEquals("Synthetic app", spinner.selectedItem)
        var attempts = 0; spinner.setOnTouchListener { _, event -> if (event.actionMasked == MotionEvent.ACTION_DOWN) attempts++; true }
        assertTrue(spinner.intercepted()); assertEquals(1, attempts); assertEquals("Synthetic app", spinner.selectedItem)
    }
    @Test fun dialogEditorDefaultsAreNotPersistedWithActivityState() {
        val editor = ProxyEditorState(context)
        editor.dialogProxyEditIp.text = "synthetic.invalid"; editor.dialogProxyEditPassword.text = "synthetic-input"
        val activity = ProxySettingsState(context); val saved = Bundle().also(activity::saveChecks)
        val newActivity = ProxySettingsState(context); newActivity.restoreChecks(saved)
        val newEditor = ProxyEditorState(context)
        assertEquals("", newEditor.dialogProxyEditIp.text); assertEquals("", newEditor.dialogProxyEditPassword.text)
        assertTrue(newEditor.dialogProxyEditPassword.isPassword)
        assertEquals(1, newEditor.dialogProxyEditIp.maxLines)
    }
    @Test fun originalStyledSwitchOverlayAndHiddenDefaultsAreRetained() {
        val misc = MiscSettingsState(context); val tunnel = TunnelSettingsState(context); val proxy = ProxySettingsState(context)
        assertEquals(R.style.CustomCompoundButtonSwitch, misc.settingsActivityCheckUpdateSwitch.switchThemeOverlay)
        assertEquals(R.style.CustomCompoundButtonSwitch, tunnel.dvWgLockdownSwitch.switchThemeOverlay)
        assertEquals(View.GONE, proxy.wgRefresh.visibility)
        assertEquals(View.GONE, tunnel.settingsActivityAllowBypassProgress.visibility)
    }
    @Test fun originalSettingIdsAppearOnlyOnceInNativeHierarchy() {
        val states = listOf(MiscSettingsState(context).layout, TunnelSettingsState(context).layout, ProxySettingsState(context).layout)
        states.forEach { root -> val tags = flatten(root).mapNotNull { it.key }; assertEquals(tags.size, tags.toSet().size) }
        assertTrue(flatten(states[0]).any { it.key == "network.settings_tasker_rl" })
        assertTrue(flatten(states[1]).any { it.key == "network.settings_custom_lan_ip_rl" })
        assertTrue(flatten(states[2]).any { it.key == "network.settings_activity_rpn_container" })
    }
    @Test fun legacyDimensionsAndNativePolicyEditorKindsRemainDistinct() {
        val editor = ProxyEditorState(context)
        val ip = flatten(editor.layout).single { it.key == "network.dialog_proxy_edit_ip" }
        val port = flatten(editor.layout).single { it.key == "network.dialog_proxy_edit_port" }
        val header = flatten(editor.layout).single { it.key == "network.dialog_proxy_header" }
        assertEquals(NetworkKind.FIELD, ip.kind); assertEquals("monospace", ip.fontFamily); assertTrue(port.numeric)
        assertEquals(R.dimen.large_font_text_view, header.textSizeRes)
    }
    @Test fun clickableLinkAndStyleSpansSurviveNativeTextConversion() {
        val original = SpannableString("Read synthetic guide").apply {
            setSpan(URLSpan("https://example.invalid/guide"), 5, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(android.graphics.Typeface.BOLD), 5, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val converted = networkAnnotatedText(original)
        assertEquals(original.toString(), converted.text)
        assertEquals(1, converted.getLinkAnnotations(0, converted.length).size)
        assertTrue(converted.spanStyles.isNotEmpty())
    }
    private fun recorder(calls: MutableList<String>, name: String) = object : NetworkSeekState.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: NetworkSeekState?, progress: Int, fromUser: Boolean) { calls += "$name:$progress:$fromUser" }
        override fun onStartTrackingTouch(seekBar: NetworkSeekState?) { calls += "$name:start" }
        override fun onStopTrackingTouch(seekBar: NetworkSeekState?) { calls += "$name:stop:${seekBar?.progress}" }
    }
    private fun flatten(node: NetworkNode): List<NetworkNode> = listOf(node) + node.children.flatMap(::flatten)
}
