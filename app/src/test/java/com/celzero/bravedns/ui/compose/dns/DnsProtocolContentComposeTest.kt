/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.dns

import android.app.Application
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import com.celzero.bravedns.R
import com.celzero.bravedns.data.BlockFreeDnsItem
import com.celzero.bravedns.data.BlockFreeDnsType
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Fake content only: the rule hosts a ComponentActivity under a plain Application. */
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class DnsProtocolContentComposeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun trailingInfoDoesNotSelectTheRow() {
        var selections = 0; var infos = 0
        var selected by mutableStateOf(false)
        compose.setContent { DnsProtocolTestTheme {
            DnsEndpointRow(DnsEndpointRowState(7, "Fake resolver", selected, true, "", description = { null }, flag = { null },
                onSelect = { selections++; selected = true }, onInfo = { infos++ }))
        } }
        compose.onNodeWithTag("dns_endpoint_info_7").performClick()
        compose.runOnIdle { assertEquals(0, selections); assertEquals(1, infos) }
        compose.onNodeWithTag("dns_endpoint_7").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(1, selections) }
    }

    @Test fun dohFieldsReachTheCallbackWithoutExtraTrimmingOrValidation() {
        var submitted: List<Any>? = null
        compose.setContent { DnsProtocolTestTheme {
            DnsServerEditor(DnsServerKind.DOH, { "DoH7" }, { name, url, ip, proxy, secure ->
                submitted = listOf(name, url, ip, proxy, secure); "Fake validation error"
            }, {})
        } }
        awaitText("DoH7")
        compose.onNodeWithTag("dns_name").performTextReplacement("  Name  ")
        compose.onNodeWithTag("dns_url").performTextReplacement("https://example.org")
        compose.onNodeWithTag("dns_ip").performTextReplacement("  192.0.2.1, 2001:db8::1  ")
        compose.onNodeWithTag("dns_insecure").performScrollTo().performClick()
        compose.onNodeWithTag("dns_editor_add").performClick()
        compose.onNodeWithText("Fake validation error").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(listOf("  Name  ", "https://example.org", "192.0.2.1, 2001:db8::1", "", false), submitted) }
    }

    @Test fun dotKeepsTheBlankInitialUrlAndOptionalIpField() {
        var input: String? = null
        compose.setContent { DnsProtocolTestTheme { DnsServerEditor(DnsServerKind.DOT, { "DoT2" }, { _, url, _, _, _ -> input = url; null }, {}) } }
        awaitText("DoT2")
        compose.onNodeWithTag("dns_ip").assertExists()
        compose.onNodeWithTag("dns_editor_add").performClick()
        compose.runOnIdle { assertEquals("", input) }
    }

    @Test fun odohDoesNotRequireOrTransformTheOptionalProxy() {
        var values: Pair<String, String>? = null
        compose.setContent { DnsProtocolTestTheme { DnsServerEditor(DnsServerKind.ODOH, { "ODoH2" }, { _, url, _, proxy, _ -> values = proxy to url; null }, {}) } }
        awaitText("ODoH2")
        compose.onNodeWithTag("dns_insecure").assertDoesNotExist()
        compose.onNodeWithTag("dns_url").performTextReplacement("https://example.org/dns-query")
        compose.onNodeWithTag("dns_editor_add").performClick()
        compose.runOnIdle { assertEquals("" to "https://example.org/dns-query", values) }
    }

    @Test fun ordinaryEditorDismissesOnRecreationInsteadOfSavingDraftPayload() {
        val restore = StateRestorationTester(compose)
        restore.setContent { DnsProtocolTestTheme {
            var adding by remember { mutableStateOf(false) }
            Button({ adding = true }) { Text("Open fake editor") }
            if (adding) DnsServerEditor(DnsServerKind.DOH, { "DoH7" }, { _, _, _, _, _ -> null }, { adding = false })
        } }
        compose.onNodeWithText("Open fake editor").performClick(); awaitText("DoH7")
        compose.onNodeWithTag("dns_name").performTextReplacement("Transient draft")
        restore.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("dns_name").assertDoesNotExist()
        compose.onNodeWithText("Open fake editor").assertIsDisplayed()
    }

    @Test fun proxyLockdownDisablesForwarderToggleAndKeepsInputUnnormalized() {
        var submitted: List<Any>? = null
        compose.setContent { DnsProtocolTestTheme {
            DnsProxyEditor(listOf("Nobody", "Fake app"), "Proxy3", true, true, {}, { name, app, ip, port, checked ->
                submitted = listOf(name, app, ip, port, checked); null
            }, {})
        } }
        compose.onNodeWithTag("dns_forward_apps").performScrollTo().assertIsNotEnabled().assertIsOn()
        compose.onNodeWithTag("dns_app").performScrollTo().performClick()
        compose.onNodeWithText("Fake app").performClick()
        compose.onNodeWithTag("dns_ip").performTextReplacement(" 127.0.0.1, ::1 ")
        compose.onNodeWithTag("dns_port").performTextReplacement("5353")
        compose.onNodeWithTag("dns_editor_add").performClick()
        compose.runOnIdle { assertEquals(listOf("Proxy3", "Fake app", " 127.0.0.1, ::1 ", "5353", true), submitted) }
    }

    @Test fun blockFreeChipsAndRowsDispatchOnlyTheirRecordingCallbacks() {
        lateinit var b: BlockFreeDnsPresentation
        val calls = mutableListOf<Int>(); var rowCalls = 0
        compose.setContent { DnsProtocolTestTheme {
            val context = androidx.compose.ui.platform.LocalContext.current
            b = remember { BlockFreeDnsPresentation(context).apply { bfdFilterChipGroup.setOnCheckedStateChangeListener { _, ids -> calls += ids.single() } } }
            val list = remember { BlockFreeDnsListState("") { rowCalls++ }.apply {
                submitList(listOf(BlockFreeDnsItem("fake-key", BlockFreeDnsType.DOH, "Fake DNS", "https://example.org/")))
            } }
            BlockFreeDnsContent(b, list)
        } }
        compose.onNodeWithTag("blockfree-filter-${R.id.bfd_chip_dot}").performScrollTo().performClick()
        compose.onNodeWithTag("blockfree-item-fake-key").performClick()
        compose.runOnIdle { assertEquals(listOf(R.id.bfd_chip_dot), calls); assertEquals(1, rowCalls) }
    }

    @Test fun dnsCryptModeChangeUsesTheMatchingIndexWithoutDroppingTheStamp() {
        var relayLabel = ""; var serverName = ""; var relayName = ""
        var submitted: List<Any>? = null
        compose.setContent { DnsProtocolTestTheme {
            val context = androidx.compose.ui.platform.LocalContext.current
            relayLabel = context.getString(R.string.cd_dns_crypt_relay_heading)
            serverName = context.getString(R.string.cd_dns_crypt_name, "3")
            relayName = context.getString(R.string.cd_dns_crypt_relay_name, "5")
            DnsCryptEditor({ 3 to 5 }, { name, stamp, desc, relay ->
                submitted = listOf(name, stamp, desc, relay); null
            }, {})
        } }
        awaitText(serverName)
        compose.onNodeWithTag("dns_stamp").performTextReplacement("sdns://fake")
        compose.onNodeWithTag("dns_description").performTextReplacement("Fake description")
        compose.onNodeWithText(relayLabel).performScrollTo().performClick()
        compose.onNodeWithTag("dns_name").assertTextContains(relayName)
        compose.onNodeWithTag("dns_editor_add").performClick()
        compose.runOnIdle { assertEquals(listOf(relayName, "sdns://fake", "Fake description", true), submitted) }
    }

    @Test fun relayRowIsIndependentlyToggleable() {
        var selected by mutableStateOf(true); var calls = 0
        compose.setContent { DnsProtocolTestTheme {
            DnsEndpointRow(DnsEndpointRowState(9, "Fake relay", selected, false, "", relay = true,
                description = { null }, flag = { null }, onSelect = { selected = !selected; calls++ }, onInfo = {}))
        } }
        compose.onNodeWithTag("dns_endpoint_9").assertIsOn().performClick().assertIsOff()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test fun smartSelectionRequiresBothTheStoredSelectionAndActiveSmartMode() {
        var active by mutableStateOf(false); var selectCalls = 0; var infoCalls = 0
        val entry = com.celzero.bravedns.database.SmartDnsEndpoint(8, "Fake smart DNS", 0, "Fake explanation", true)
        compose.setContent { DnsProtocolTestTheme { SmartDnsContent(listOf(entry), active, { selectCalls++ }, { infoCalls++ }) } }
        compose.onNodeWithTag("dns_endpoint_8").assertIsNotSelected()
        compose.runOnIdle { active = true }
        compose.onNodeWithTag("dns_endpoint_8").assertIsSelected()
        compose.onNodeWithTag("dns_endpoint_info_8").performClick()
        compose.runOnIdle { assertEquals(0, selectCalls); assertEquals(1, infoCalls) }
        compose.onNodeWithTag("dns_endpoint_8").performClick()
        compose.runOnIdle { assertEquals(1, selectCalls) }
    }

    private fun awaitText(text: String) { compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() } }
}

@Composable private fun DnsProtocolTestTheme(content: @Composable () -> Unit) {
    val base = androidx.compose.ui.platform.LocalContext.current
    val context = remember(base) { android.view.ContextThemeWrapper(base, R.style.AppThemeWhite) }
    CompositionLocalProvider(androidx.compose.ui.platform.LocalContext provides context) {
        com.celzero.bravedns.ui.compose.RethinkTheme(content)
    }
}
