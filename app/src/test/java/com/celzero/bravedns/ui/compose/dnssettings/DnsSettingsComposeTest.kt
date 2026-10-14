package com.celzero.bravedns.ui.compose.dnssettings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso
import android.app.Application
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/** No production Fragment/Activity is launched; every effect is a local counter. */
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class DnsSettingsComposeTest {
    @get:Rule val compose = createComposeRule()
    @Test fun renderingDoesNotWriteAndTenSwitchesDispatchExactlyOnce() {
        val s = DnsSettingsState(); val calls = mutableListOf<String>()
        val controls = listOf("dcDownloaderRl" to s.dcDownloaderSwitch, "dcCheckUpdateRl" to s.dcCheckUpdateSwitch, "dcAlgRl" to s.dcAlgSwitch, "dcSplitDnsRl" to s.dcSplitDnsSwitch, "dcFaviconRl" to s.dcFaviconSwitch, "dcEnableCacheRl" to s.dcEnableCacheSwitch, "dcProxyDnsRl" to s.dcProxyDnsSwitch, "dcPreventDnsLeaksRl" to s.dcPreventDnsLeaksSwitch, "dcBlockHeadingRl" to s.dcBlockUnknownSwitch, "dcUndelegatedDomainsRl" to s.dcUndelegatedDomainsSwitch)
        controls.forEach { (key, control) -> control.onCheckedChange = { _, _ -> calls += key } }
        compose.setContent { DnsUiTestTheme { DnsSettingsScreen(s, {}, {}) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        controls.forEach { (key, _) -> compose.onNodeWithTag("dns-switch-$key").performScrollTo().performClick() }
        compose.runOnIdle { assertEquals(controls.map { it.first }, calls) }
    }
    @Test fun proxyRowAndDirectSwitchKeepSeparateLogCardinality() {
        val s = DnsSettingsState(); var writes = 0; var logs = 0
        s.dcProxyDnsSwitch.onCheckedChange = { _, _ -> writes++ }
        s.dcProxyDnsRl.onClick = { s.dcProxyDnsSwitch.isChecked = !s.dcProxyDnsSwitch.isChecked; logs++ }
        compose.setContent { DnsUiTestTheme { DnsSettingsScreen(s, {}, {}) } }
        compose.onNodeWithTag("dns-switch-dcProxyDnsRl").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, writes); assertEquals(0, logs) }
        compose.onNodeWithTag("dns-row-dcProxyDnsRl").performClick()
        compose.runOnIdle { assertEquals(2, writes); assertEquals(1, logs) }
    }
    @Test fun temporarilyDisabledSwitchDoesNotDisableEnclosingRow() {
        val s = DnsSettingsState(); var calls = 0
        s.dcAlgSwitch.isEnabled = false; s.dcAlgRl.onClick = { calls++ }
        compose.setContent { DnsUiTestTheme { DnsSettingsScreen(s, {}, {}) } }
        compose.onNodeWithTag("dns-switch-dcAlgRl").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag("dns-row-dcAlgRl").performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }
    @Test fun allFourRadioRoutesRemainIndependentActionsAndReselects() {
        val s = DnsSettingsState(); val calls = mutableListOf<String>()
        listOf("network" to s.networkDnsRb, "custom" to s.customDnsRb, "rethink" to s.rethinkPlusDnsRb, "smart" to s.smartDnsRb).forEach { (key, c) -> c.onClick = { calls += key } }
        compose.setContent { DnsUiTestTheme { DnsSettingsScreen(s, {}, {}) } }
        listOf("network", "custom", "rethink", "smart", "smart").forEach { compose.onNodeWithTag("dns-choice-$it").performScrollTo().performClick() }
        compose.runOnIdle { assertEquals(listOf("network", "custom", "rethink", "smart", "smart"), calls); assertTrue(s.networkDnsRb.isChecked); assertTrue(s.smartDnsRb.isChecked) }
    }
    @Test fun restoreBackAndCancelDoNotConfirm() {
        val s = DnsSettingsState().apply { restorePrompt = true }; var restores = 0
        compose.setContent { DnsUiTestTheme { DnsSettingsScreen(s, { restores++; s.restorePrompt = false }, {}) } }
        Espresso.pressBack()
        compose.runOnIdle { assertFalse(s.restorePrompt); s.restorePrompt = true }
        compose.onNodeWithTag("dns-dialog-secondary").performClick()
        compose.runOnIdle { assertEquals(0, restores); s.restorePrompt = true }
        compose.onNodeWithTag("dns-dialog-positive").performClick()
        compose.runOnIdle { assertEquals(1, restores) }
    }
    @Test fun systemDnsCopyDoesNotSelectAndRefreshCanBeDisabled() {
        val s = DnsSettingsState().apply { systemDns = "192.0.2.1"; dcRefresh.isEnabled = false }; val copies = mutableListOf<String>()
        compose.setContent { DnsUiTestTheme { DnsSettingsScreen(s, {}, { copies += it; s.systemDns = null }) } }
        compose.onNodeWithTag("dns-dialog-secondary").performClick()
        compose.onNodeWithTag("dns-refresh").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(listOf("192.0.2.1"), copies); assertFalse(s.networkDnsRb.isChecked) }
    }
    @Test fun rtlLargeFontAndHiddenLocalFlavorRemainReachable() {
        val s = DnsSettingsState().apply { dcLocalBlocklistRl.visible = false }
        compose.setContent { DnsUiTestTheme { val d = LocalDensity.current; CompositionLocalProvider(LocalDensity provides Density(d.density, 2f), LocalLayoutDirection provides LayoutDirection.Rtl) { DnsSettingsScreen(s, {}, {}) } } }
        compose.onNodeWithTag("dns-row-dcLocalBlocklistRl").assertDoesNotExist()
        compose.onNodeWithTag("dns-switch-dcUndelegatedDomainsRl").performScrollTo().assertIsDisplayed()
    }
}
