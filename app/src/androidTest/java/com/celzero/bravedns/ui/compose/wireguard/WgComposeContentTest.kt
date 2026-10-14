package com.celzero.bravedns.ui.compose.wireguard

import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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

/** Isolated native content: synthetic state/callbacks, no manager or VPN/service invocation. */
class WgComposeContentTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppThemeWhite)
    private fun content(block: @Composable () -> Unit) {
        val themedContext = context
        compose.setContent { CompositionLocalProvider(LocalContext provides themedContext) { RethinkTheme { block() } } }
    }
    @Test fun editorCompositionDoesNotSaveAndSaveClickDispatchesOnce() {
        val b = WgEditorState(context)
        var saves = 0
        b.saveTunnel.setOnClickListener { saves++ }
        content { WgEditorContent(b) }
        compose.runOnIdle { assertEquals(0, saves) }
        compose.onNodeWithText(context.getString(R.string.lbl_save)).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, saves) }
    }
    @Test fun peerSaveDisabledWhileDismissRemainsAvailable() {
        val b = WgPeerState(context)
        var dismissed = 0
        b.customDialogOkButton.isEnabled = false
        b.customDialogDismissButton.setOnClickListener { dismissed++ }
        content { WgPeerContent(b) }
        compose.onNodeWithText(context.getString(R.string.lbl_save)).performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText(context.getString(R.string.lbl_dismiss)).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, dismissed) }
    }
    @Test fun switchAndCardDispatchSeparately() {
        val b = WgConfigRowState(context)
        b.interfaceNameText.text = "Synthetic tunnel"
        var cards = 0; var switches = 0
        b.interfaceDetailCard.setOnClickListener { cards++ }
        b.interfaceSwitch.setOnClickListener { switches++ }
        content { WgConfigRow(b) }
        compose.onNodeWithContentDescription("Synthetic tunnel").performClick()
        compose.runOnIdle { assertEquals(0, cards); assertEquals(1, switches) }
        compose.onNodeWithText("Synthetic tunnel").performClick()
        compose.runOnIdle { assertEquals(1, cards); assertEquals(1, switches) }
    }
    @Test fun disabledApplicationCheckboxBlocksUserDispatch() {
        val b = WgAppRowState(context)
        b.wgIncludeAppListApkLabelTv.text = "Synthetic app"
        b.wgIncludeAppListCheckbox.isEnabled = false
        var clicks = 0
        b.wgIncludeAppListCheckbox.setOnClickListener { clicks++ }
        content { WgAppRow(b) }
        compose.onNodeWithContentDescription("Synthetic app").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(0, clicks) }
    }
    @Test fun mobileOnlyRowRetainsCallOnClickWithoutToggle() {
        val b = WgDetailState(context)
        b.useMobileTitleTv.text = "Synthetic mobile"
        var checked: Boolean? = null
        b.useMobileCheck.setOnClickListener { checked = it.isChecked }
        b.useMobileRl.setOnClickListener { b.useMobileCheck.callOnClick() }
        content { WgDetailContent(b) }
        compose.onNodeWithText("Synthetic mobile").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(false, checked); assertFalse(b.useMobileCheck.isChecked) }
    }
    @Test fun ssidControlsRemainReachableAtLargeRtlFont() {
        val b = WgSsidState(context)
        val presenter = WgSsidPresenter(mutableListOf()) { }
        content {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl, LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                WgSsidContent(b, presenter)
            }
        }
        compose.onNodeWithText(context.getString(R.string.fapps_info_dialog_positive_btn)).performScrollTo().assertIsDisplayed()
    }
    @Test fun searchClearPreservesSelectedFilter() {
        val b = WgIncludeAppsState(context)
        val selected = WgControlState(checkable = true).apply { text = "Selected"; isChecked = true }
        b.filters.add(selected)
        b.wgIncludeAppDialogSearchView.setText("synthetic")
        var changed = "synthetic"
        b.wgIncludeAppDialogSearchView.onTextChanged = { changed = it.toString() }
        content { WgIncludeAppsContent(b) { true } }
        compose.onNodeWithContentDescription(context.getString(R.string.fapps_filter_clear_btn)).performClick()
        compose.runOnIdle { assertEquals("", changed); assertTrue(selected.isChecked) }
    }
    @Test fun asciiWhitespaceSearchDoesNotSubmit() {
        val b = WgIncludeAppsState(context)
        b.wgIncludeAppDialogSearchView.setText(" \t ")
        var submitted = 0
        content { WgIncludeAppsContent(b) { submitted++; true } }
        compose.onNode(hasSetTextAction()).performImeAction()
        compose.runOnIdle { assertEquals(0, submitted) }
    }
}
