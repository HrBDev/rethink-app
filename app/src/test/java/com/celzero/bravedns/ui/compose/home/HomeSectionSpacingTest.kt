/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.home

import android.app.Application
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Synthetic presentation only: no Home controller, production Application or service. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h1600dp")
class HomeSectionSpacingTest {
    @get:Rule val compose = createComposeRule()
    private val calls = mutableListOf<String>()

    @Test fun baselineInsetsKeepRadiusAndElevationDifferences() {
        assertEquals(HomeCardInsets(10, 11), homeCardInsets(24f, 2f, true))
        assertEquals(HomeCardInsets(8, 9), homeCardInsets(18f, 2f, true))
        assertEquals(HomeCardInsets(6, 6), homeCardInsets(18f, 0f, true))
        // Round in physical pixels, rather than rounding dp before scaling.
        assertEquals(HomeCardInsets(15, 17), homeCardInsets(36f, 4f, true))
        assertEquals(HomeCardInsets(2, 3), homeCardInsets(18f, 2f, false))
    }

    @Test fun phoneSectionsKeepOutsideGuttersAndOriginalGroups() {
        render()
        assertPhoneGroups()
    }

    @Test fun phoneRtlLargeTextKeepsOutsideGuttersAndOriginalGroups() {
        render(direction = LayoutDirection.Rtl, fontScale = 2f)
        assertPhoneGroups()
    }

    @Test fun frostCardsStillHaveCornerClearanceAtZeroElevation() {
        render(theme = R.style.AppThemeTrueBlackFrost)
        assertPhoneGroups()
    }

    @Test
    @Config(qualifiers = "w700dp-h1600dp")
    fun wideRtlLargeTextKeepsConnectionLogsAndRulesProxyAppsGroups() {
        render(width = 700, direction = LayoutDirection.Rtl, fontScale = 2f)
        val dns = bounds("fhs_card_dns_ll")
        val throughput = bounds("fhs_throughput_ll")
        val rules = bounds("fhs_card_firewall_rules_card")
        val proxy = bounds("fhs_card_proxy_ll")
        val logs = bounds("fhs_card_logs_ll")
        val apps = bounds("fhs_card_apps_cv")
        assertHorizontalGap(dns, throughput)
        assertHorizontalGap(throughput, rules)
        assertVerticalGap(dns, logs)
        assertVerticalGap(throughput, logs)
        assertVerticalGap(rules, proxy)
        assertVerticalGap(proxy, apps)
        assertHorizontalGap(logs, proxy)
        assertHorizontalGap(logs, apps)
        assertEquals(proxy.left, apps.left, 1f)
        assertEquals(proxy.right, apps.right, 1f)
        assertControlsAndNoDispatch()
    }

    private fun assertPhoneGroups() {
        val dns = bounds("fhs_card_dns_ll")
        val throughput = bounds("fhs_throughput_ll")
        val rules = bounds("fhs_card_firewall_rules_card")
        val proxy = bounds("fhs_card_proxy_ll")
        val logs = bounds("fhs_card_logs_ll")
        val apps = bounds("fhs_card_apps_cv")
        assertHorizontalGap(dns, throughput)
        assertEquals(dns.top, throughput.top, 1f)
        assertEquals(dns.bottom, throughput.bottom, 1f)
        assertHorizontalGap(rules, proxy)
        assertVerticalGap(dns, rules)
        assertVerticalGap(throughput, proxy)
        assertVerticalGap(rules, logs)
        assertVerticalGap(proxy, logs)
        assertVerticalGap(logs, apps)
        assertEquals(logs.left, apps.left, 1f)
        assertEquals(logs.right, apps.right, 1f)
        assertControlsAndNoDispatch()
    }

    private fun assertControlsAndNoDispatch() {
        compose.onNodeWithTag("home_fragment_pause_icon").assertIsDisplayed()
        compose.onNodeWithTag("fhs_dns_on_off_btn").assertIsDisplayed()
        compose.onNodeWithTag("home_fragment_bottom_sheet_icon").assertIsDisplayed()
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
    }

    private fun bounds(tag: String): Rect {
        compose.onNodeWithTag(tag).assertIsDisplayed()
        return compose.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
    }

    private fun assertHorizontalGap(first: Rect, second: Rect) {
        val gap = maxOf(first.left, second.left) - minOf(first.right, second.right)
        assertTrue("Card surfaces must have a visible horizontal gutter: $first / $second", gap >= 8f)
    }

    private fun assertVerticalGap(above: Rect, below: Rect) {
        assertTrue("Card sections must have a visible vertical gutter: $above / $below", below.top - above.bottom >= 8f)
    }

    private fun render(width: Int = 360, direction: LayoutDirection = LayoutDirection.Ltr, fontScale: Float = 1f, theme: Int = R.style.AppThemeWhite) {
        compose.setContent {
            val base = LocalContext.current
            val context = remember(base, theme) { ContextThemeWrapper(base, theme) }
            val configuration = Configuration(LocalConfiguration.current).apply { screenWidthDp = width }
            val density = LocalDensity.current
            val state = remember(context) {
                HomePresentation(context).apply {
                    fhsProtectionLevelTxt.text = "Synthetic protected status"
                    fhsCardDnsConnectedDns.text = "Synthetic DNS"
                    fhsCardDnsLatency.text = "10 ms"
                    fhsInternetSpeed.text = "0.00"
                    fhsInternetSpeedUnit.text = "B/S"
                    fhsFirewallRulesPager.adapter = HomeRulesPages(listOf("Apps", "Domains", "IPs"))
                    fhsCardOtherProxyCount.text = "WireGuard"
                    fhsProxyHealthContainer.isVisible = false
                    fhsLogsPager.adapter = object : HomePages { override val itemCount = 1 }
                    fhsLogsDisabledRow.isVisible = false
                    fhsLogsDots.isVisible = false
                    fhsCardAllowedApps.text = "12"
                    fhsCardAppsAllApps.text = "/ 12"
                    listOf(fhsCardDnsLl, fhsThroughputLl, fhsCardFirewallRulesCard, fhsCardProxyLl, fhsCardLogsLl, fhsCardAppsCv).forEach { node ->
                        node.setOnClickListener { calls += node.id.toString() }
                    }
                }
            }
            CompositionLocalProvider(
                LocalContext provides context,
                LocalConfiguration provides configuration,
                LocalLayoutDirection provides direction,
                LocalDensity provides Density(density.density, fontScale)
            ) { RethinkTheme { HomeContent(state) } }
        }
    }
}
