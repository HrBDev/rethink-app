/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.about

import android.app.Application
import android.content.DialogInterface
import android.view.ContextThemeWrapper
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.celzero.bravedns.ui.compose.wireguard.WgControlState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Uses only Compose's empty test host and plain Application, never production Activities. */
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AboutContentComposeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun stackTabsSwapLargeLazyContentAndRetappingResetsToTheFirstLine() {
        val state = DiagnosticTabsState(listOf("JVM Stack" to List(2000) { "JVM line $it" }.joinToString("\n"), "Go Stack" to "Go first\nGo second"))
        compose.setContent { AboutTestTheme { DiagnosticTabsContent(state) } }
        compose.onNodeWithText("JVM line 0").assertIsDisplayed()
        compose.onNodeWithText("Go Stack").performClick().assertIsSelected()
        compose.onNodeWithText("Go first").assertIsDisplayed()
        compose.onNodeWithText("JVM line 0").assertDoesNotExist()
        compose.onNodeWithText("JVM Stack").performClick().assertIsSelected()
        compose.onNodeWithText("JVM line 0").assertIsDisplayed()
    }

    @Test fun metricAndProcTabsKeepTextAndInitialSelection() {
        val state = DiagnosticTabsState(listOf("Metrics" to "Memory Metrics\nFake values", "Threads / Proc / Mem" to "THREADS\nFake thread"), selectableText = true)
        compose.setContent { AboutTestTheme { DiagnosticTabsContent(state) } }
        compose.onNodeWithText("Metrics").assertIsSelected()
        compose.onNodeWithText("Memory Metrics").assertIsDisplayed()
        compose.onNodeWithText("Threads / Proc / Mem").performClick()
        compose.onNodeWithText("Fake thread").assertIsDisplayed()
    }

    @Test fun statsRowTapHighlightsAndCopyUsesOnlyRecordedSelection() {
        val state = DiagnosticSelectionState(listOf("one", "two", "three"), "one\ntwo\nthree")
        compose.setContent { AboutTestTheme { DiagnosticSelectionContent(state) } }
        compose.onNodeWithTag("diagnostic.line.2").performClick().assertIsSelected()
        compose.onNodeWithTag("diagnostic.line.0").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals("one\nthree", state.textToCopy()) }
        compose.onNodeWithTag("diagnostic.line.0").performClick().assertIsNotSelected()
        compose.runOnIdle { assertEquals("three", state.textToCopy()) }
    }

    @Test fun databaseTableClicksOnlyInvokeFakeCallbacksAndProgressTextRecomposes() {
        val dump = DatabaseDumpState()
        val calls = mutableListOf<Int>()
        compose.setContent { AboutTestTheme { DatabaseDumpContent(listOf("FakeA", "FakeB"), dump) { calls += it; dump.append("\nLoading fake ...\n") } } }
        compose.onNodeWithText("Select a table to load its dump").assertIsDisplayed()
        compose.onNodeWithText("FakeB").performClick()
        compose.onNodeWithText("Loading fake ...").assertIsDisplayed()
        compose.runOnIdle { assertEquals(listOf(1), calls); dump.text = "Fake completed dump" }
        compose.onNodeWithText("Fake completed dump").assertIsDisplayed()
    }

    @Test fun fileTrailingActionsNeverToggleSelectionOrPerformActualFileWork() {
        var opened = 0; var deletes = 0; var checks = 0
        val check = WgControlState(checkable = true).apply { isChecked = true; setOnCheckedChangeListener { _, _ -> checks++ } }
        val row = BugReportRowState("fake", "fake.txt", "fake details", R.drawable.ic_logs, check, { opened++ }, { deletes++ })
        compose.setContent { AboutTestTheme { BugReportFileRow(row) } }
        compose.onNodeWithTag("bugreport.delete.fake").performClick()
        compose.onNodeWithTag("bugreport.open.fake").performClick()
        compose.runOnIdle { assertEquals(1, opened); assertEquals(1, deletes); assertEquals(0, checks); assertTrue(check.isChecked) }
        compose.onNodeWithTag("bugreport.file.fake").performClick().assertIsOff()
        compose.runOnIdle { assertEquals(1, checks) }
    }

    @Test fun disabledSendAndProgressDoNotDispatchUntilEnabled() {
        lateinit var state: BugReportFilesState
        var sends = 0
        compose.setContent { AboutTestTheme {
            val context = LocalContext.current
            state = remember { BugReportFilesState(context).apply {
                brbsSendButton.isEnabled = false
                brbsProgressLayout.visibility = View.VISIBLE
                brbsProgressText.text = "Creating fake ZIP"
                brbsSendButton.setOnClickListener { sends++ }
            } }
            BugReportFilesContent(state)
        } }
        compose.onNodeWithTag("bugreport.send").assertIsNotEnabled()
        compose.onNodeWithText("Creating fake ZIP").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, sends); state.brbsSendButton.isEnabled = true; state.brbsProgressLayout.visibility = View.GONE }
        compose.onNodeWithTag("bugreport.send").performClick()
        compose.runOnIdle { assertEquals(1, sends) }
        compose.onNodeWithText("Creating fake ZIP").assertDoesNotExist()
    }

    @Test fun autoDnsRowsAreCheckedLockedAndCannotInvokeSelection() {
        var clicks = 0
        compose.setContent { AboutTestTheme { DnsRecordTypeRow(DnsRecordRowState("A", "Fake record", true, true) { clicks++ }) } }
        compose.onNodeWithTag("dnsrecords.A").assertIsOn().assertIsNotEnabled()
        compose.onNodeWithTag("dnsrecords.A").performClick()
        compose.runOnIdle { assertEquals(0, clicks) }
    }

    @Test fun manualDnsRowDispatchesExactlyOneSelectionCallback() {
        var clicks = 0
        var selected by mutableStateOf(false)
        compose.setContent { AboutTestTheme { DnsRecordTypeRow(DnsRecordRowState("AAAA", "Fake record", selected, false) { clicks++; selected = !selected }) } }
        compose.onNodeWithTag("dnsrecords.AAAA").performClick().assertIsOn()
        compose.runOnIdle { assertEquals(1, clicks) }
    }

    @Test fun modeButtonsUseRecordingListenerWithoutPersistence() {
        lateinit var state: DnsRecordTypesState
        val calls = mutableListOf<Boolean>()
        compose.setContent { AboutTestTheme {
            val context = LocalContext.current
            state = remember { DnsRecordTypesState(context).apply {
                drbsModeToggleGroup.addOnButtonCheckedListener { _, id, checked -> if (checked) calls += (id == R.id.drbs_auto_mode_btn) }
            } }
            DnsRecordTypesContent(state)
        } }
        compose.onNodeWithTag("dnsrecords.manual").performClick().assertIsSelected()
        compose.onNodeWithTag("dnsrecords.manual").performClick()
        compose.onNodeWithTag("dnsrecords.auto").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(listOf(false, true), calls) }
    }

    @Test fun ordinaryDiagnosticSelectionAndTabsAreNotSavedAcrossRecreation() {
        val restore = StateRestorationTester(compose)
        restore.setContent { AboutTestTheme {
            val tabs = remember { DiagnosticTabsState(listOf("JVM Stack" to "fake jvm", "Go Stack" to "fake go")) }
            DiagnosticTabsContent(tabs)
        } }
        compose.onNodeWithText("Go Stack").performClick()
        restore.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("JVM Stack").assertIsSelected()
    }

    @Test fun diagnosticButtonsDispatchExactIdentifiersAndLabels() {
        val calls = mutableListOf<Int>()
        compose.setContent { AboutTestTheme {
            AboutDialogContent("Fake dialog", "Fake message", buttons = mapOf(
                DialogInterface.BUTTON_POSITIVE to "OK", DialogInterface.BUTTON_NEGATIVE to "Copy", DialogInterface.BUTTON_NEUTRAL to "Refresh"), onButton = { calls += it })
        } }
        compose.onNodeWithText("Copy").performClick()
        compose.onNodeWithText("Refresh").performClick()
        compose.onNodeWithText("OK").performClick()
        compose.runOnIdle { assertEquals(listOf(-2, -3, -1), calls) }
    }

    @Test fun tokenTapAndDoubleTapUseSeparateFakeActions() {
        var single = 0; var double = 0
        compose.setContent { AboutTestTheme {
            val context = LocalContext.current
            val state = remember { AboutState(context).apply {
                tokenTextView.text = "fake-token"
                tokenSingleTap = { single++ }; tokenDoubleTap = { double++ }
            } }
            AboutContent(state)
        } }
        compose.onNodeWithTag("about").performScrollToNode(hasTestTag("about.token"))
        compose.onNodeWithTag("about.token").performClick()
        compose.runOnIdle { assertEquals(1, single); assertEquals(0, double) }
        compose.onNodeWithTag("about.token").performTouchInput { doubleClick() }
        compose.mainClock.advanceTimeBy(600)
        compose.runOnIdle { assertEquals(1, single); assertEquals(1, double) }
    }

    @Test fun contributorsCloseControlDoesNotDispatchDialogButtons() {
        var closes = 0
        compose.setContent { AboutTestTheme {
            AboutDialogContent("Contributors", content = { AboutRichTextContent("Fake contributor", centered = true) },
                buttons = emptyMap(), closeAction = { closes++ }, onButton = { fail("unexpected button") })
        } }
        compose.onNodeWithContentDescription("Close").performClick()
        compose.runOnIdle { assertEquals(1, closes) }
    }

    @Test fun descriptiveCopyRendersUnderWhiteBlackPlusAndFrostThemes() {
        var theme by mutableIntStateOf(R.style.AppThemeWhite)
        compose.setContent { AboutTestTheme(theme) {
            val context = LocalContext.current
            AboutContent(remember(context) { AboutState(context) })
        } }
        val text = org.robolectric.RuntimeEnvironment.getApplication().getString(R.string.about_bravedns_explantion)
        for (id in listOf(R.style.AppThemeWhite, R.style.AppThemeTrueBlack, R.style.AppThemeWhitePlus,
            R.style.AppThemeTrueBlackPlus, R.style.AppThemeTrueBlackFrost)) {
            compose.runOnIdle { theme = id }
            compose.onNodeWithTag("about").performScrollToNode(hasText(text))
            compose.onNodeWithText(text).assertIsDisplayed()
        }
    }

    @Test fun aboutShowsDebugAndSponsorVisibilityFromObservableState() {
        lateinit var state: AboutState
        compose.setContent { AboutTestTheme {
            val context = LocalContext.current
            state = remember { AboutState(context).apply { aboutMemoryProfile.visibility = View.GONE } }
            AboutContent(state)
        } }
        compose.onNodeWithTag("about").performScrollToNode(hasText("Stacktrace"))
        compose.onNodeWithText("Stacktrace").assertIsDisplayed()
        compose.onNodeWithText("Profile Memory").assertDoesNotExist()
        compose.runOnIdle { state.aboutMemoryProfile.visibility = View.VISIBLE }
        compose.onNodeWithTag("about").performScrollToNode(hasText("Profile Memory"))
        compose.onNodeWithText("Profile Memory").assertIsDisplayed()
    }
}

@Composable
private fun AboutTestTheme(themeRes: Int = R.style.AppThemeWhite, content: @Composable () -> Unit) {
    val base = LocalContext.current
    val context = remember(base, themeRes) { ContextThemeWrapper(base, themeRes) }
    CompositionLocalProvider(LocalContext provides context) { RethinkTheme(content) }
}
