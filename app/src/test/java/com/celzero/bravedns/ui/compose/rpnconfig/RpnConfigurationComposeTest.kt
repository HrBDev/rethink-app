/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.celzero.bravedns.ui.compose.rpnconfig

import android.app.Application
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Fake state/content under a plain Application: no real RPN Activity or controller is launched. */
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class RpnConfigurationComposeTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppThemeWhite)
    private fun state() = RpnConfigState(context).apply { clientIpShimmer = false }

    @Test fun recompositionDoesNotDispatch() {
        val state = state(); var calls = 0
        state.refreshChip.setOnClickListener { calls++ }; state.catchAllCheck.setOnCheckedChangeListener { _, _ -> calls++ }
        compose.setContent { RpnTestTheme { RpnConfigContent(state) } }
        compose.runOnIdle { state.valueRx.text = "Fake download"; state.headerTitle = "Fake city" }
        compose.runOnIdle { assertEquals(0, calls) }
    }
    @Test fun catchAllRowAndSwitchDispatchExactlyOnceEach() {
        val state = state(); val values = mutableListOf<Boolean>()
        state.catchAllCheck.setOnCheckedChangeListener { _, value -> values += value }
        state.catchAllRl.setOnClickListener { state.catchAllCheck.performClick() }
        compose.setContent { RpnTestTheme { RpnConfigContent(state) } }
        compose.onNodeWithTag("rpn.config.catchAll.row").performScrollTo().performClick()
        compose.onNodeWithTag("rpn.config.catchAll.switch").performClick()
        compose.runOnIdle { assertEquals(listOf(true, false), values) }
    }
    @Test fun hopRowIsInertWhileItsSwitchWorks() {
        val state = state(); var calls = 0
        state.hopCheck.setOnCheckedChangeListener { _, _ -> calls++ }
        compose.setContent { RpnTestTheme { RpnConfigContent(state) } }
        compose.onNodeWithTag("rpn.config.hop.row").performScrollTo().assertHasNoClickAction()
        compose.onNodeWithTag("rpn.config.hop.switch").performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }
    @Test fun disabledRefreshAndHiddenHopHaveNoActiveActions() {
        val state = state(); var calls = 0
        state.refreshChip.isEnabled = false; state.refreshChip.setOnClickListener { calls++ }
        compose.setContent { RpnTestTheme { RpnConfigContent(state) } }
        compose.onNodeWithTag("rpn.config.refresh").assertIsNotEnabled(); compose.onNodeWithTag("rpn.config.hopAction").assertDoesNotExist()
        compose.runOnIdle { assertEquals(0, calls) }
    }
    @Test fun headerCanCollapseAndExpandByDirectDragAtTop() {
        val state = state(); lateinit var header: TopAppBarState
        compose.setContent { RpnTestTheme { header = rememberTopAppBarState(); RpnConfigContent(state, header) } }
        compose.onNodeWithTag("rpn.config.header").performTouchInput { swipe(Offset(5f, height - 5f), Offset(5f, 2f), 300) }
        compose.runOnIdle { assertTrue("Direct header drag must collapse", header.heightOffset < 0f) }
        compose.onNodeWithTag("rpn.config.header").performTouchInput { swipe(Offset(5f, 2f), Offset(5f, height + 200f), 400) }
        compose.runOnIdle { assertEquals(0f, header.heightOffset, .1f) }
    }
    @Test fun headerDragCancellationDoesNotSnapOrDispatchRefresh() {
        val state = state(); lateinit var header: TopAppBarState; var calls = 0; var cancelledOffset = 0f
        state.refreshChip.setOnClickListener { calls++ }
        compose.setContent { RpnTestTheme { header = rememberTopAppBarState(); RpnConfigContent(state, header) } }
        compose.onNodeWithTag("rpn.config.header").performTouchInput { down(Offset(5f, height - 5f)); moveBy(Offset(0f, -35f)); cancel() }
        compose.runOnIdle { cancelledOffset = header.heightOffset; assertTrue(cancelledOffset < 0f) }
        compose.mainClock.advanceTimeBy(600)
        compose.runOnIdle { assertEquals(cancelledOffset, header.heightOffset, .1f); assertEquals(0, calls) }
    }
    @Test fun headerChildTapSurvivesSubSlopMovement() {
        val state = state(); var calls = 0
        state.refreshChip.setOnClickListener { calls++ }
        compose.setContent { RpnTestTheme { RpnConfigContent(state) } }
        compose.onNodeWithTag("rpn.config.refresh").performTouchInput { down(center); moveBy(Offset(0f, 1f)); up() }
        compose.runOnIdle { assertEquals(1, calls) }
    }
    @Test fun fullyCollapsedHeroRemovesInvisibleTouchTargets() {
        val state = state(); lateinit var header: TopAppBarState
        compose.setContent { RpnTestTheme { header = rememberTopAppBarState(); RpnConfigContent(state, header) } }
        compose.runOnIdle { header.heightOffset = header.heightOffsetLimit }
        compose.onNodeWithTag("rpn.config.refresh").assertDoesNotExist()
        compose.onNodeWithTag("rpn.config.public").assertDoesNotExist()
    }
    @Test fun bypassRowAndSwitchDoNotDoubleDispatch() {
        var excluded by mutableStateOf(false); val values = mutableListOf<Boolean>()
        compose.setContent { RpnTestTheme { RpnBypassAppRow("Fake", "example.invalid", excluded, null) { values += it; excluded = it } } }
        compose.onNodeWithTag("rpn.bypass.switch.example.invalid").performClick()
        compose.runOnIdle { assertEquals(listOf(true), values) }
        compose.onNodeWithTag("rpn.bypass.app.example.invalid").performClick()
        compose.runOnIdle { assertEquals(listOf(true, false), values) }
    }
    @Test fun bulkTogglePrecedesConfirmationAndProgrammaticSyncDoesNotClick() {
        val state = RpnBypassState(context); val values = mutableListOf<Boolean>()
        state.rpnBypassBulkCheck.setOnClickListener { values += it.isChecked }
        compose.setContent { RpnTestTheme { RpnBypassContent(state, "", 0, {}, {}, {}) {} } }
        compose.onNodeWithTag("rpn.bypass.all").performClick()
        compose.runOnIdle { assertEquals(listOf(true), values); state.rpnBypassBulkCheck.isChecked = true }
        compose.runOnIdle { assertEquals(1, values.size) }
    }
    @Test fun searchClearKeepsFilterAndDoesNotSubmit() {
        val state = RpnBypassState(context); var query by mutableStateOf("needle"); var queries = 0; var filters = 0; var submits = 0
        compose.setContent { RpnTestTheme { RpnBypassContent(state, query, 1, { query = it; queries++ }, { submits++ }, { filters++ }) {} } }
        compose.onNodeWithTag("rpn.bypass.clear").performClick(); compose.onNodeWithTag("rpn.bypass.filter.1").assertIsSelected()
        compose.runOnIdle { assertEquals("", query); assertEquals(1, queries); assertEquals(0, filters); assertEquals(0, submits) }
    }
    @Test fun whitespaceSearchImeDoesNotSubmit() {
        val state = RpnBypassState(context); var submits = 0
        compose.setContent { RpnTestTheme { RpnBypassContent(state, "   ", 0, {}, { submits++ }, {}) {} } }
        compose.onNodeWithTag("rpn.bypass.search").performImeAction(); compose.runOnIdle { assertEquals(0, submits) }
    }
    @Test fun ssidRadioOptionsRemainEnabledOnEmptyInput() {
        val state = RpnSsidState(); var adds = 0
        compose.setContent { RpnTestTheme { RpnSsidContent(state, "Fake Country", { adds++ }, {}, {}, {}) } }
        compose.onNodeWithTag("rpn.ssid.notEqual").performScrollTo().performClick(); compose.onNodeWithTag("rpn.ssid.add").assertIsNotEnabled()
        compose.runOnIdle { assertFalse(state.equal); assertEquals(0, adds) }
    }
    @Test fun rtlLargeFontSsidSaveAndCancelDispatchSeparately() {
        val state = RpnSsidState(); var saves = 0; var cancels = 0
        compose.setContent { RpnTestTheme {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f), LocalLayoutDirection provides LayoutDirection.Rtl) {
                RpnSsidContent(state, "Fake Country", { state.add() }, {}, { cancels++ }, { saves++ })
            }
        } }
        compose.onNodeWithTag("rpn.ssid.input").performScrollTo().performTextInput("Cafe")
        compose.onNodeWithTag("rpn.ssid.add").performScrollTo().performClick()
        compose.onNodeWithTag("rpn.ssid.save").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("Cafe", state.items.single().name); assertEquals(1, saves); assertEquals(0, cancels) }
        compose.onNodeWithTag("rpn.ssid.cancel").performClick(); compose.runOnIdle { assertEquals(1, saves); assertEquals(1, cancels) }
    }
    @Test fun winActionRemainsRepeatable() {
        val state = RpnWinState(); var calls = 0; state.openAppSelector.setOnClickListener { calls++ }
        compose.setContent { RpnTestTheme { RpnWinContent(state) } }
        compose.onNodeWithTag("rpn.win.apps").performClick().performClick(); compose.runOnIdle { assertEquals(2, calls) }
    }
    @Test fun detailRestoreUsesSameValueSuppression() {
        val state = state(); var calls = 0; state.hopCheck.isChecked = true
        val saved = Bundle().also(state::save); state.hopCheck.setOnCheckedChangeListener { _, _ -> calls++ }
        state.restore(saved); assertEquals(0, calls); state.hopCheck.isChecked = false; state.restore(saved)
        assertEquals(2, calls); assertTrue(state.hopCheck.isChecked)
    }
}

@Composable
private fun RpnTestTheme(content: @Composable () -> Unit) {
    val base = LocalContext.current
    val context = remember(base) { ContextThemeWrapper(base, R.style.AppThemeWhite) }
    CompositionLocalProvider(LocalContext provides context) { RethinkTheme(content) }
}
