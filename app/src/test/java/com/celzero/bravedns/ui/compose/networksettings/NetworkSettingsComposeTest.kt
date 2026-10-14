/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.networksettings

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Synthetic native presentation only. No production Activity, application, service, or repository. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class NetworkSettingsComposeTest {
    @get:Rule val compose = createComposeRule()
    private val context: Context get() = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppThemeWhite)

    @Test fun rowClickAndSwitchClickEachDispatchOneOriginalTransition() {
        val state = MiscSettingsState(context); val calls = mutableListOf<Boolean>()
        state.settingsActivityEnableLogsRl.setOnClickListener { state.settingsActivityEnableLogsSwitch.isChecked = !state.settingsActivityEnableLogsSwitch.isChecked }
        state.settingsActivityEnableLogsSwitch.setOnCheckedChangeListener { _, checked -> calls += checked }
        compose.setContent { Theme { NetworkSettingsScreen(state.layout) } }
        compose.onNodeWithTag("network.settings_activity_enable_logs_rl").performScrollTo().performClick()
        compose.onNodeWithTag("network.settings_activity_enable_logs_switch").performClick()
        compose.runOnIdle { assertEquals(listOf(true, false), calls) }
    }
    @Test fun disabledProxyRowsAndControlsDoNotInvokeCallbacks() {
        val state = ProxySettingsState(context); var calls = 0
        state.settingsActivitySocks5Rl.isEnabled = false; state.settingsActivitySocks5Switch.isEnabled = false
        state.settingsActivitySocks5Rl.setOnClickListener { calls++ }
        state.settingsActivitySocks5Switch.setOnCheckedChangeListener { _, _ -> calls++ }
        compose.setContent { Theme { NetworkSettingsScreen(state.layout) } }
        compose.onNodeWithTag("network.settings_activity_socks5_rl").performScrollTo().assertIsNotEnabled().performClick()
        compose.onNodeWithTag("network.settings_activity_socks5_switch").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(0, calls) }
    }
    @Test fun httpEditorHidesSocksOnlyFieldsAndKeepsHostEditable() {
        val state = ProxyEditorState(context)
        state.dialogProxyPortHeader.visibility = View.GONE; state.dialogProxyUsernameHeader.visibility = View.GONE
        state.dialogProxyPasswordHeader.visibility = View.GONE; state.dialogProxyUdpHeader.visibility = View.GONE
        compose.setContent { Theme { NetworkContent(state.layout) } }
        compose.onNodeWithTag("network.dialog_proxy_edit_port").assertDoesNotExist()
        compose.onNodeWithTag("network.dialog_proxy_edit_password").assertDoesNotExist()
        compose.onNodeWithTag("network.dialog_proxy_udp_check").assertDoesNotExist()
        compose.onNodeWithTag("network.dialog_proxy_edit_ip").performScrollTo().performTextReplacement("https://synthetic.invalid:8443")
        compose.runOnIdle { assertEquals("https://synthetic.invalid:8443", state.dialogProxyEditIp.text.toString()) }
    }
    @Test fun hiddenFeatureRowsDoNotLeaveInteractiveChildren() {
        val state = MiscSettingsState(context)
        state.settingsFirebaseErrorReportingRl.visibility = View.GONE
        state.settingsActivityCheckUpdateRl.visibility = View.GONE
        compose.setContent { Theme { NetworkSettingsScreen(state.layout) } }
        compose.onNodeWithTag("network.settings_firebase_error_reporting_switch").assertDoesNotExist()
        compose.onNodeWithTag("network.settings_activity_check_update_switch").assertDoesNotExist()
    }
    @Test fun restoredDescriptionsCanChangeWithoutRecreatingControllerListeners() {
        val state = ProxySettingsState(context); var calls = 0
        state.settingsActivityWireguardContainer.setOnClickListener { calls++ }
        compose.setContent { Theme { NetworkSettingsScreen(state.layout) } }
        compose.runOnIdle { state.settingsActivityWireguardDesc.text = "Synthetic connected state" }
        compose.onNodeWithText("Synthetic connected state").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("network.settings_activity_wireguard_container").performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }
    @Test fun rtlLargeTextKeepsLastTunnelRouteReachable() {
        val state = TunnelSettingsState(context); var calls = 0
        state.settingsCustomLanIpRl.setOnClickListener { calls++ }
        compose.setContent { Theme(largeRtl = true) { NetworkSettingsScreen(state.layout) } }
        compose.onNodeWithTag("network.settings_custom_lan_ip_rl").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }
    @Test fun nativeLockdownReviewIncludesSyntheticConflictAndConditionalControls() {
        val state = LockdownDialogState(context)
        val item = LockdownItemState(context)
        item.lockdownCheckItemLabel.text = "Synthetic conflict"
        item.lockdownCheckItemDesc.text = "A fake option for rendering only"
        item.lockdownCheckItemStatus.text = "Conflict"
        state.lockdownCheckDisableLabel.text = "Disable synthetic options"
        compose.setContent { Theme {
            NetworkContent(state.layout, extraChildren = mapOf(state.lockdownCheckItemsContainer to listOf(item.layout)))
        } }
        compose.onNodeWithText("Synthetic conflict").assertIsDisplayed()
        compose.onNodeWithTag("network.lockdown_check_disable_switch").performClick()
        compose.runOnIdle { assertTrue(state.lockdownCheckDisableSwitch.isChecked) }
    }
    @Test fun selectorInterceptionShowsOneAttemptAndDoesNotOpenMenu() {
        val state = ProxyEditorState(context); var attempts = 0
        state.dialogProxySpinnerAppname.items = listOf("None", "Synthetic app")
        state.dialogProxySpinnerAppname.setOnTouchListener { _, _ -> attempts++; true }
        compose.setContent { Theme { NetworkContent(state.layout) } }
        compose.onNodeWithText("None").performScrollTo().performClick()
        compose.onNodeWithText("Synthetic app").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, attempts) }
    }
    @Composable private fun Theme(largeRtl: Boolean = false, content: @Composable () -> Unit) {
        val base = LocalContext.current
        val context = remember(base) { ContextThemeWrapper(base, R.style.AppThemeWhite) }
        val density = LocalDensity.current
        CompositionLocalProvider(LocalContext provides context,
            LocalDensity provides if (largeRtl) Density(density.density, 2f) else density,
            LocalLayoutDirection provides if (largeRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) { RethinkTheme(content) }
    }
}
