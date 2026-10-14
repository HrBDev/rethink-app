/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.rules
import android.app.Application
import android.view.ContextThemeWrapper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class CustomLanIpContentTest {
    @get:Rule val compose = createComposeRule()
    private val fields = LanFields(ApplicationProvider.getApplicationContext<Application>())
    @Test fun allSixPairsAndBothActionsArePresentWithFakeCallbacks() {
        var saves = 0; var resets = 0
        fields.saveButton.setOnClickListener { saves++ }
        fields.resetButton.setOnClickListener { resets++ }
        compose.setContent { Theme { CustomLanIpContent(fields) } }
        listOf("gateway_v4", "gateway_v6", "router_v4", "router_v6", "dns_v4", "dns_v6").forEach {
            compose.onNodeWithTag("lan_$it").assertExists()
            compose.onNodeWithTag("lan_${it}_prefix").assertExists()
        }
        compose.onNodeWithTag("lan_reset").performScrollTo().performClick()
        compose.onNodeWithTag("lan_save").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, saves); assertEquals(1, resets) }
    }
    @Test fun disabledAutoFieldsDoNotEditAndManualCallbackEnablesThem() {
        fields.gatewayIpv4.isEnabled = false; fields.gatewayIpv4Prefix.isEnabled = false; fields.resetButton.isEnabled = false
        fields.manualToggleBtn.setOnClickListener {
            fields.gatewayIpv4.isEnabled = true; fields.gatewayIpv4Prefix.isEnabled = true; fields.resetButton.isEnabled = true
        }
        compose.setContent { Theme { CustomLanIpContent(fields) } }
        compose.onNodeWithTag("lan_gateway_v4").assertIsNotEnabled()
        compose.onNodeWithTag("lan_reset").assertIsNotEnabled()
        compose.onNodeWithTag("lan_manual").performClick()
        compose.onNodeWithTag("lan_gateway_v4").performTextReplacement("10.2.0.1")
        compose.onNodeWithTag("lan_gateway_v4_prefix").performTextReplacement("24")
        compose.runOnIdle { assertEquals("10.2.0.1", fields.gatewayIpv4.text); assertEquals("24", fields.gatewayIpv4Prefix.text) }
    }
    @Composable private fun Theme(content: @Composable () -> Unit) {
        val context = LocalContext.current
        val themed = remember(context) { ContextThemeWrapper(context, R.style.AppThemeWhite) }
        CompositionLocalProvider(LocalContext provides themed) { RethinkTheme(content) }
    }
}
