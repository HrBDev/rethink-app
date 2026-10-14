package com.celzero.bravedns.ui.compose.blocklists

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.paging.PagingData
import androidx.test.espresso.Espresso
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.dnssettings.DnsUiTestTheme
import com.celzero.bravedns.ui.compose.themeColor
import kotlinx.coroutines.flow.MutableStateFlow
import android.app.Application
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/** Native content only, with empty paging flows and recording callbacks. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BlocklistsComposeTest {
    @get:Rule val compose = createComposeRule()
    private fun row(entries: String? = null, select: (Boolean) -> Unit = {}, link: () -> Unit = {}) = BlocklistRow("pack:fake:1", "security", "Security", "Description", "Fake pack", "Two lists", 1, { false }, select, entries, link)
    @Composable private fun Configuration(s: BlocklistConfigurationState, change: (String) -> Boolean = { false }, submit: (String) -> Boolean = { false }, filter: (Any) -> Unit = {}) {
        val pages = remember { MutableStateFlow<PagingData<BlocklistRow>>(PagingData.empty()) }
        BlocklistConfigurationScreen(s, pages, pages, LocalLifecycleOwner.current, { s.selectedAdvanced = it; s.showAdvanced = it }, change, submit, filter)
    }
    @Test fun rowAndCheckboxDispatchOnceEachAndSelectedFillMatchesTheme() {
        val writes = mutableListOf<Boolean>(); val row = row(select = writes::add); var selectedColor = Color.Unspecified
        compose.setContent { DnsUiTestTheme { selectedColor = themeColor(R.attr.selectedCardBg); BlocklistSelectionRow(row, true, emptySet()) } }
        compose.runOnIdle { assertTrue(writes.isEmpty()) }
        compose.onNodeWithTag("blocklist-row-pack:fake:1").performClick()
        compose.onNodeWithTag("blocklist-check-pack:fake:1").assertIsOn()
        val pixels = compose.onNodeWithTag("blocklist-row-pack:fake:1").captureToImage().toPixelMap()
        assertEquals(selectedColor, pixels[pixels.width / 2, pixels.height - 2])
        compose.onNodeWithTag("blocklist-check-pack:fake:1").performClick()
        compose.runOnIdle { assertEquals(listOf(true, false), writes) }
    }
    @Test fun entryLinkIsIndependentOfSelection() {
        var links = 0; val writes = mutableListOf<Boolean>()
        compose.setContent { DnsUiTestTheme { BlocklistSelectionRow(row("12 entries", writes::add) { links++ }, false, emptySet()) } }
        compose.onNodeWithTag("blocklist-entries-pack:fake:1").performClick()
        compose.runOnIdle { assertEquals(1, links); assertTrue(writes.isEmpty()) }
    }
    @Test fun persistedTagEmissionRefreshesOptimisticPackCheck() {
        var tags by mutableStateOf(emptySet<Int>())
        compose.setContent { DnsUiTestTheme { BlocklistSelectionRow(row(), false, tags) } }
        compose.onNodeWithTag("blocklist-check-pack:fake:1").performClick().assertIsOn()
        compose.runOnIdle { tags = setOf(2) }
        compose.onNodeWithTag("blocklist-check-pack:fake:1").assertIsOff()
    }
    @Test fun tabsShowSearchOnlyInAdvanced() {
        val s = BlocklistConfigurationState()
        compose.setContent { DnsUiTestTheme { Configuration(s) } }
        compose.onNodeWithTag("blocklist-query").assertDoesNotExist()
        compose.onNodeWithTag("blocklist-tab-advanced").performClick()
        compose.onNodeWithTag("blocklist-query").assertIsDisplayed()
        compose.onNodeWithTag("blocklist-tab-simple").performClick()
        compose.onNodeWithTag("blocklist-query").assertDoesNotExist()
    }
    @Test fun queryChangeAndSubmissionAreSeparateAndBlankSubmitIgnored() {
        val s = BlocklistConfigurationState().apply { showAdvanced = true; selectedAdvanced = true }
        val changes = mutableListOf<String>(); val submits = mutableListOf<String>()
        compose.setContent { DnsUiTestTheme { Configuration(s, { changes += it; false }, { submits += it; true }) } }
        compose.onNodeWithTag("blocklist-query").performImeAction()
        compose.onNodeWithTag("blocklist-query").performTextInput("https://example.invalid/1:fake")
        compose.runOnIdle { assertEquals(listOf("https://example.invalid/1:fake"), changes); assertTrue(submits.isEmpty()) }
        compose.onNodeWithTag("blocklist-query").performImeAction()
        compose.runOnIdle { assertEquals(listOf("https://example.invalid/1:fake"), submits) }
    }
    @Test fun filterReselectDoesNotDispatchTwice() {
        val s = BlocklistConfigurationState().apply { showAdvanced = true }; val calls = mutableListOf<Any>()
        compose.setContent { DnsUiTestTheme { Configuration(s, filter = calls::add) } }
        compose.onNodeWithTag("blocklist-selection-0").performClick()
        compose.onNodeWithTag("blocklist-selection-1").performClick().performClick()
        compose.runOnIdle { assertEquals(listOf(1), calls) }
    }
    @Test fun inFlightDownloadDisablesStartButNotCancel() {
        val s = BlocklistConfigurationState().apply { lbConfigureLayout.visible = false; lbDownloadLayout.visible = true; lbDownloadBtn.isEnabled = false; lbDownloadProgress.visible = true }; var cancels = 0
        s.lbCancelDownloadBtn.onClick = { cancels++ }
        compose.setContent { DnsUiTestTheme { Configuration(s) } }
        compose.onNodeWithTag("blocklist-download").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag("blocklist-cancel-download").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, cancels) }
    }
    @Test fun threeWayPromptAndBackRemainDistinct() {
        val s = BlocklistConfigurationState(); val calls = mutableListOf<String>()
        fun prompt() = BlocklistPrompt("Apply changes", "Fake prompt", "Apply", { calls += "apply" }, "Discard", { calls += "discard" }, "Continue", { calls += "continue" })
        s.prompt = prompt()
        compose.setContent { DnsUiTestTheme { Configuration(s) } }
        compose.onNodeWithTag("blocklist-dialog-neutral").performClick()
        compose.runOnIdle { s.prompt = prompt() }
        compose.onNodeWithTag("blocklist-dialog-negative").performClick()
        compose.runOnIdle { s.prompt = prompt() }
        compose.onNodeWithTag("blocklist-dialog-positive").performClick()
        compose.runOnIdle { s.prompt = prompt() }
        Espresso.pressBack()
        compose.runOnIdle { assertNull(s.prompt); assertEquals(listOf("continue", "discard", "apply"), calls) }
    }
    @Test fun localDeleteCannotDismissByBack() {
        val s = LocalBlocklistsState(); var deletes = 0; var cancels = 0
        fun prompt() = LocalBlocklistPrompt("Delete", "Fake deletion", "Delete", false, { deletes++ }, { cancels++ })
        s.prompt = prompt()
        compose.setContent { DnsUiTestTheme { LocalBlocklistsScreen(s) } }
        Espresso.pressBack()
        compose.onNodeWithTag("local-dialog-cancel").assertIsDisplayed().performClick()
        compose.runOnIdle { s.prompt = prompt() }
        compose.onNodeWithTag("local-dialog-confirm").performClick()
        compose.runOnIdle { assertEquals(1, deletes); assertEquals(1, cancels) }
    }
    @Test fun lockdownBackDoesNotRunNegativeDownloadAction() {
        val s = LocalBlocklistsState(); var proceeds = 0
        fun prompt() = LocalBlocklistPrompt("Lockdown", "Fake guard", "Enable", true, {}, { proceeds++ })
        s.prompt = prompt()
        compose.setContent { DnsUiTestTheme { LocalBlocklistsScreen(s) } }
        Espresso.pressBack()
        compose.runOnIdle { assertEquals(0, proceeds); s.prompt = prompt() }
        compose.onNodeWithTag("local-dialog-cancel").performClick()
        compose.runOnIdle { assertEquals(1, proceeds) }
    }
    @Test fun subgroupMutationAndExplicitApplyClearAreIndependent() {
        val s = BlocklistFilterState(); val calls = mutableListOf<String>()
        s.chips += BlocklistFilterChip("fake subgroup", false) { calls += "check:$it" }
        s.apply = { calls += "apply" }; s.clear = { calls += "clear" }
        compose.setContent { DnsUiTestTheme { BlocklistFilterScreen(s) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("blocklist-subgroup-fake subgroup").performClick()
        compose.onNodeWithTag("blocklist-filter-apply").performClick()
        compose.onNodeWithTag("blocklist-filter-clear").performClick()
        compose.runOnIdle { assertEquals(listOf("check:true", "apply", "clear"), calls) }
    }
}
