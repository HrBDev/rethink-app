/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.rules
import android.app.Application
import android.view.ContextThemeWrapper
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.celzero.bravedns.R
import com.celzero.bravedns.service.DomainRulesManager
import com.celzero.bravedns.service.IpRulesManager.IpRuleStatus
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.celzero.bravedns.util.Constants.Companion.UID_EVERYBODY
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Isolated native content: plain Application, fake callbacks, no production host/service. */
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class RulesContentTest {
    @get:Rule val compose = createComposeRule()
    @Test fun invalidIpCallbackKeepsDraftOpenAndTypingClearsError() {
        val state = RulesPageState().apply { openEditor() }; val trace = mutableListOf<Boolean>()
        compose.setContent { Theme { if (state.editorOpen) RuleEditor(false, UID_EVERYBODY, state) { trace += it; state.error = R.string.ci_dialog_error_invalid_cidr } } }
        compose.onNodeWithTag("rule_input").performTextReplacement("192.0.2.1-55")
        compose.onNodeWithTag("rule_allow").performScrollTo().performClick()
        compose.onNodeWithTag("rule_error").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("rule_input").assertTextContains("192.0.2.1-55")
        compose.runOnIdle { assertEquals(listOf(false), trace); assertTrue(state.editorOpen) }
        compose.onNodeWithTag("rule_input").performTextReplacement("192.0.2.1")
        compose.onNodeWithTag("rule_error").assertDoesNotExist()
        compose.onNodeWithTag("rule_editor_done").performScrollTo().performClick()
        compose.onNodeWithTag("rule_input").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, trace.size) }
    }
    @Test fun wildcardPrefixSelectsTypeButOrdinaryEditorDoesNotSurviveRecreation() {
        val restoration = StateRestorationTester(compose)
        var latest: RulesPageState? = null
        restoration.setContent {
            val state = rememberSaveable(saver = RulesPageState.Saver) { RulesPageState() }
            latest = state
            Theme { if (state.editorOpen) RuleEditor(true, 42, state) {} }
        }
        compose.runOnIdle { latest!!.openEditor() }
        compose.onNodeWithTag("rule_input").performTextReplacement("*.example.test")
        compose.runOnIdle { assertEquals(DomainRulesManager.DomainType.WILDCARD, latest!!.domainType); latest!!.query = "fixture" }
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("rule_input").assertDoesNotExist()
        compose.runOnIdle { assertEquals("fixture", latest!!.query); assertEquals("", latest!!.input); assertFalse(latest!!.editorOpen) }
    }
    @Test fun cancelNeverDispatchesDelete() {
        var confirms = 0; var cancels = 0
        compose.setContent {
            var open by remember { mutableStateOf(true) }
            Theme { if (open) RuleConfirmDialog("Fixture", "Delete fixture", "Delete", { confirms++; open = false }, { cancels++; open = false }, { open = false }) }
        }
        compose.onNodeWithTag("rule_delete_cancel").performClick()
        compose.onNodeWithTag("rule_delete_confirm").assertDoesNotExist()
        compose.runOnIdle { assertEquals(0, confirms); assertEquals(1, cancels) }
    }
    @Test fun largeRtlEditorExposesAllActions() {
        val state = RulesPageState().apply { openEditor() }; val trace = mutableListOf<Boolean>()
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f), LocalLayoutDirection provides LayoutDirection.Rtl) {
                Theme { RuleEditor(true, 42, state) { trace += it } }
            }
        }
        compose.onNodeWithTag("rule_allow").performScrollTo().performClick()
        compose.onNodeWithTag("rule_block").performScrollTo().performClick()
        compose.onNodeWithTag("rule_editor_done").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(listOf(false, true), trace) }
    }
    @Test fun globalIpDetailsExposeBypassAndHideAppTrust() {
        val trace = mutableListOf<Int>()
        compose.setContent { Theme { RuleDetailsContent(false, UID_EVERYBODY, "192.0.2.1", RuleAppMetadata("Universal"), "", IpRuleStatus.NONE.id, IpRuleStatus.NONE.id, 0, trace::add, {}) } }
        compose.onNodeWithTag("rule_status_${IpRuleStatus.TRUST.id}").assertDoesNotExist()
        compose.onNodeWithTag("rule_status_${IpRuleStatus.BYPASS_UNIVERSAL.id}").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf(IpRuleStatus.BYPASS_UNIVERSAL.id), trace) }
    }
    @Composable private fun Theme(content: @Composable () -> Unit) {
        val context = LocalContext.current
        val themed = remember(context) { ContextThemeWrapper(context, R.style.AppThemeWhite) }
        CompositionLocalProvider(LocalContext provides themed) { RethinkTheme(content) }
    }
}
