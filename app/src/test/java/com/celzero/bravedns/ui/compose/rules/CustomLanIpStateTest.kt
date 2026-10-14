/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.rules
import android.app.Application
import android.view.ContextThemeWrapper
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.service.PersistentState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Mocked preferences only; no real tunnel, network, Activity or service. */
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class CustomLanIpStateTest {
    private lateinit var preferences: PersistentState
    private val values = mutableMapOf<String, Any>()
    private var dismissals = 0
    @Before fun before() {
        preferences = Mockito.mock(PersistentState::class.java, Answer { call ->
            val key = call.method.name.removePrefix("get").removePrefix("set")
            when {
                call.method.name.startsWith("set") -> { values[key] = call.arguments[0]; null }
                values.containsKey(key) -> values[key]
                call.method.returnType == String::class.java -> ""
                else -> Mockito.RETURNS_DEFAULTS.answer(call)
            }
        })
    }
    private fun state() = CustomLanIpState(ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), R.style.AppThemeWhite), preferences) { dismissals++ }
    @Test fun openingResetsChangedFlagBeforeReadingModeAndDoesNotSaveMode() {
        values["CustomLanIpMode"] = true
        val state = state(); val order = Mockito.inOrder(preferences)
        order.verify(preferences).customModeOrIpChanged = false
        order.verify(preferences).customLanIpMode
        assertTrue(state.binding.gatewayIpv4.isEnabled)
        Mockito.verify(preferences, Mockito.never()).customLanIpMode = true
        assertEquals(0, dismissals)
    }
    @Test fun savingAutoPreservesSavedAddressesAndSignalsOnlyModeChange() {
        values["CustomLanIpMode"] = true; values["CustomLanGatewayIpv4"] = "10.3.4.1/24"
        val state = state(); state.binding.autoToggleBtn.performClick(); Mockito.clearInvocations(preferences)
        state.binding.saveButton.performClick()
        val order = Mockito.inOrder(preferences)
        order.verify(preferences).customLanIpMode = false
        order.verify(preferences).customModeOrIpChanged = true
        assertEquals("10.3.4.1/24", values["CustomLanGatewayIpv4"]); assertEquals(1, dismissals)
    }
    @Test fun manualSaveTrimsAndPersistsSixPairsInOriginalOrder() {
        val state = state(); state.binding.manualToggleBtn.performClick()
        state.binding.gatewayIpv4.setText(" 10.8.0.1 "); state.binding.gatewayIpv4Prefix.setText(" 24 ")
        Mockito.clearInvocations(preferences); state.binding.saveButton.performClick()
        val order = Mockito.inOrder(preferences)
        order.verify(preferences).customLanIpMode = true
        order.verify(preferences).customLanGatewayIpv4 = "10.8.0.1/24"
        order.verify(preferences).customLanGatewayIpv6 = "fd66:f83a:c650::1/120"
        order.verify(preferences).customLanRouterIpv4 = "10.111.222.2/32"
        order.verify(preferences).customLanRouterIpv6 = "fd66:f83a:c650::2/128"
        order.verify(preferences).customLanDnsIpv4 = "10.111.222.3/32"
        order.verify(preferences).customLanDnsIpv6 = "fd66:f83a:c650::3/128"
        order.verify(preferences).customModeOrIpChanged = true
        assertEquals(1, dismissals)
    }
    @Test fun invalidOrIncompletePairKeepsDraftAndDoesNotWrite() {
        val state = state(); state.binding.manualToggleBtn.performClick(); state.binding.gatewayIpv4.setText("8.8.8.8")
        Mockito.clearInvocations(preferences); state.binding.saveButton.performClick()
        assertEquals(View.VISIBLE, state.binding.errorText.visibility); assertEquals("8.8.8.8", state.binding.gatewayIpv4.text)
        Mockito.verifyNoInteractions(preferences); assertEquals(0, dismissals)
        state.binding.gatewayIpv4.setText("10.0.0.1"); state.binding.gatewayIpv4Prefix.setText("")
        state.binding.saveButton.performClick(); Mockito.verifyNoInteractions(preferences)
    }
    @Test fun switchingBackToManualReloadsSavedValuesAndBlankPairsRemainAllowed() {
        values["CustomLanGatewayIpv4"] = "192.168.7.1/24"
        val state = state(); state.binding.manualToggleBtn.performClick(); state.binding.gatewayIpv4.setText("10.2.0.1")
        state.binding.autoToggleBtn.performClick(); state.binding.manualToggleBtn.performClick()
        assertEquals("192.168.7.1", state.binding.gatewayIpv4.text)
        state.binding.gatewayIpv4.setText(""); state.binding.gatewayIpv4Prefix.setText(""); state.binding.saveButton.performClick()
        Mockito.verify(preferences).customLanGatewayIpv4 = ""
        assertEquals(1, dismissals)
    }
    @Test fun validatorsKeepPrivateUlaAndInclusivePrefixBounds() {
        val state = state()
        fun valid(name: String, ip: String, prefix: String): Boolean = state.javaClass.getDeclaredMethod(name, String::class.java, String::class.java).apply { isAccessible = true }.invoke(state, ip, prefix) as Boolean
        listOf("10.0.0.1", "172.16.0.1", "172.31.255.255", "192.168.0.1").forEach { assertTrue(valid("validateIpv4WithPrefix", it, "0")); assertTrue(valid("validateIpv4WithPrefix", it, "32")) }
        listOf("172.15.0.1", "172.32.0.1", "127.0.0.1", "8.8.8.8").forEach { assertFalse(valid("validateIpv4WithPrefix", it, "24")) }
        assertFalse(valid("validateIpv4WithPrefix", "10.0.0.1", "33"))
        assertTrue(valid("validateIpv6WithPrefix", "fc00::1", "0")); assertTrue(valid("validateIpv6WithPrefix", "fd00::1", "128"))
        assertFalse(valid("validateIpv6WithPrefix", "fd00::1", "129")); assertFalse(valid("validateIpv6WithPrefix", "fe80::1", "64"))
        assertFalse(valid("validateIpv6WithPrefix", "2001:db8::1", "64"))
    }
}
