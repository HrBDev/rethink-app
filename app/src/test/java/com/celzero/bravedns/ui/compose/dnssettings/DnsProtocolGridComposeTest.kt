package com.celzero.bravedns.ui.compose.dnssettings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.celzero.bravedns.ui.compose.dnssettings.*
import android.app.Application
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

/** Isolated native content and recording callbacks; original integration tests remain separate. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class DnsProtocolGridComposeTest {
    @get:Rule val compose = createComposeRule()
    @Test fun allSixProtocolDestinationsRemainReachable() {
        val s = DnsProtocolState(); val calls = mutableListOf<String>()
        val cards = listOf("cardDoh" to s.cardDoh, "cardDot" to s.cardDot, "cardDnscrypt" to s.cardDnscrypt, "cardDnsproxy" to s.cardDnsproxy, "cardOdoh" to s.cardOdoh, "cardRethinkDns" to s.cardRethinkDns)
        cards.forEach { (key, card) -> card.onClick = { calls += key } }
        compose.setContent { DnsUiTestTheme { DnsProtocolGrid(s) } }
        cards.forEach { (key, _) -> compose.onNodeWithTag("dns-protocol-$key").performScrollTo().performClick() }
        compose.runOnIdle { assertEquals(cards.map { it.first }, calls) }
    }
}
