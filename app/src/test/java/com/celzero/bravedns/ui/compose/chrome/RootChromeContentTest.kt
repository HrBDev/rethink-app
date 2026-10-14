/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.chrome

import android.app.Application
import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.celzero.bravedns.ui.compose.home.HomeTourAnchors
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Plain Application + synthetic callback recorders only. No production Activity/Fragment,
 * service, updater, restore worker, preferences, account, repository or VPN is constructed. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class RootChromeContentTest {
    @get:Rule val compose = createComposeRule()

    @Test fun fiveTabsRetainOrderIdsSelectedStateAndWindowAnchors() {
        val selected = mutableIntStateOf(R.id.homeScreenFragment)
        val calls = mutableListOf<Int>()
        val anchors = HomeTourAnchors()
        val ids = listOf(R.id.homeScreenFragment, R.id.summaryStatisticsFragment, R.id.rethinkPlus,
            R.id.configureFragment, R.id.aboutFragment)
        assertEquals(ids, rootNavigationItems.map { it.id })
        compose.setContent { Theme { HomeBottomNavigation(selected.intValue, anchors) { calls += it; selected.intValue = it } } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("root_nav_${ids.first()}").assertIsSelected()
        ids.drop(1).forEach { id -> compose.onNodeWithTag("root_nav_$id").performClick().assertIsSelected() }
        compose.runOnIdle {
            assertEquals(ids.drop(1), calls)
            ids.forEach { assertNotNull(anchors.find(it)) }
        }
    }

    @Test fun updateModalRendersExplicitChoicesAndPostponesBeforeDismissal() {
        val calls = mutableListOf<String>()
        val visible = mutableStateOf(true)
        val dialog = HomeChromeDialog("Synthetic update", "Available", "Download", "Later", false,
            onPositive = { calls += "download" }, onNegative = { calls += "postpone" })
        compose.setContent { Theme { if (visible.value) HomeChromeDialogContent(dialog) {
            calls += "dismiss"; visible.value = false
        } } }
        compose.onNodeWithTag("root_dialog_positive").assertIsDisplayed()
        compose.onNodeWithTag("root_dialog_negative").performClick()
        compose.onNodeWithText("Available").assertDoesNotExist()
        compose.runOnIdle { assertEquals(listOf("postpone", "dismiss"), calls) }
    }

    @Test fun restoreIsSilentUntilExplicitPositiveAndEmitsOrderedCallbacksOnce() {
        val calls = mutableListOf<String>()
        val visible = mutableStateOf(true)
        val dialog = HomeChromeDialog("Synthetic restore", "Confirm", "Restore", "Cancel", onPositive = {
            calls += "start"; calls += "observe"
        })
        compose.setContent { Theme { if (visible.value) HomeChromeDialogContent(dialog) {
            calls += "dismiss"; visible.value = false
        } } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("root_dialog_positive").performClick()
        compose.runOnIdle { assertEquals(listOf("start", "observe", "dismiss"), calls) }
    }

    @Test fun informationHasOnlyOk() {
        val calls = mutableListOf<String>()
        val visible = mutableStateOf(true)
        val dialog = HomeChromeDialog("Current", "Synthetic current version", "OK")
        compose.setContent { Theme { if (visible.value) HomeChromeDialogContent(dialog) {
            calls += "dismiss"; visible.value = false
        } } }
        compose.onNodeWithTag("root_dialog_negative").assertDoesNotExist()
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("root_dialog_positive").performClick()
        compose.runOnIdle { assertEquals(listOf("dismiss"), calls) }
    }

    @Test fun snackbarRecompositionIsSilentAndActionIsDistinctFromDismiss() {
        val message = mutableStateOf("Synthetic message")
        val calls = mutableListOf<String>()
        compose.setContent { Theme { NativeSnackbarContent(message.value, "Run", ComposeSnackbarPresenter.Appearance.Themed,
            null, { calls += "action" }, { calls += "dismiss" }) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()); message.value = "Updated synthetic message" }
        compose.onNodeWithTag("native_snackbar").assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        compose.onNodeWithTag("native_snackbar_action").performClick()
        compose.onNodeWithTag("native_snackbar").performSemanticsAction(SemanticsActions.Dismiss) { it() }
        compose.runOnIdle { assertEquals(listOf("action", "dismiss"), calls) }
    }

    @Test fun actionlessSnackbarHasNoActionAndClampsToThreeMessageLines() {
        val message = "A deliberately long synthetic message. ".repeat(30)
        compose.setContent { Theme { Box(Modifier.width(200.dp)) {
            NativeSnackbarContent(message, null, ComposeSnackbarPresenter.Appearance.Themed, null,
                { error("No action should exist") }, {})
        } } }
        compose.onNodeWithTag("native_snackbar_action").assertDoesNotExist()
        compose.onNodeWithText(message).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { read ->
            val layouts = mutableListOf<TextLayoutResult>()
            assertTrue(read(layouts)); assertEquals(3, layouts.single().lineCount)
        }
    }

    @Test fun largeRtlSnackbarKeepsActionAvailableWithoutWritesDuringComposition() {
        val calls = mutableListOf<String>()
        compose.setContent { Theme {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalDensity provides Density(density.density, 2f)) {
                NativeSnackbarContent("Synthetic RTL", "Run", ComposeSnackbarPresenter.Appearance.Themed,
                    null, { calls += "action" }, { calls += "dismiss" })
            }
        } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("native_snackbar_action").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(listOf("action"), calls) }
    }

    @Composable private fun Theme(content: @Composable () -> Unit) {
        val context = LocalContext.current
        val wrapped = remember(context) { ContextThemeWrapper(context, R.style.AppThemeWhite) }
        CompositionLocalProvider(LocalContext provides wrapped) { RethinkTheme(content) }
    }
}
