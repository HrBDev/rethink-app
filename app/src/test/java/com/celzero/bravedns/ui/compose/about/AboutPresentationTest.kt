/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.about

import android.app.Application
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.wireguard.WgControlState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Presentation state and recording callbacks only. Never instantiates an app controller. */
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AboutPresentationTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Test fun initialAboutVisibilityMatchesXmlAndDoesNotProbeServices() {
        val state = AboutState(context)
        assertTrue(state.aboutSponsor.isVisible)
        assertFalse(state.aboutManageRpn.isVisible)
        assertFalse(state.aboutCommunitySponsor.isVisible)
        assertFalse(state.progressLayout.isVisible)
        assertTrue(state.fhsTitleRethink.isAllCaps)
    }

    @Test fun titleAndTokenDispatchOnlyTheirAssignedCallbacks() {
        val state = AboutState(context)
        val calls = mutableListOf<String>()
        state.fhsTitleRethink.setOnClickListener { calls += "title" }
        state.tokenSingleTap = { calls += "copy" }
        state.tokenDoubleTap = { calls += "regenerate" }
        repeat(7) { state.fhsTitleRethink.performClick() }
        state.tokenSingleTap(); state.tokenDoubleTap()
        assertEquals(List(7) { "title" } + listOf("copy", "regenerate"), calls)
    }

    @Test fun allAboutActionsRemainIndependentlyAddressable() {
        val s = AboutState(context)
        val actions = listOf(s.aboutSponsor, s.aboutSponsorAgain, s.aboutManageRpn, s.aboutWebsite,
            s.aboutTwitter, s.aboutGithub, s.aboutBlog, s.aboutPrivacyPolicy, s.aboutTermsOfService,
            s.aboutLicense, s.aboutMail, s.aboutTelegram, s.aboutReddit, s.aboutMastodon, s.aboutElement,
            s.aboutFaq, s.mozillaImg, s.fossImg, s.flossFundsImg, s.aboutAppUpdate, s.aboutAppInfo,
            s.aboutAppNotification, s.aboutVpnProfile, s.aboutCrashLog, s.aboutAppVersion,
            s.aboutAppContributors, s.aboutAppTranslate, s.aboutStats, s.aboutProc, s.aboutStackTrace,
            s.aboutMemoryProfile, s.aboutDbStats, s.tokenTextView, s.aboutConsoleLogs, s.aboutEventLogs)
        val calls = mutableListOf<Int>()
        actions.forEachIndexed { i, control -> control.setOnClickListener { calls += i } }
        actions.forEach { it.performClick() }
        assertEquals(actions.indices.toList(), calls)
    }

    @Test fun stackTabsStartAtJvmWhileProcTabsStartAtMetricsAndRetapResetsScrollEpoch() {
        val stack = DiagnosticTabsState(listOf("JVM Stack" to "jvm", "Go Stack" to "go"))
        val proc = DiagnosticTabsState(listOf("Metrics" to "metrics", "Threads / Proc / Mem" to "proc"), selectableText = true)
        assertEquals("JVM Stack", stack.tabs[stack.selectedTab].first)
        assertEquals("Metrics", proc.tabs[proc.selectedTab].first)
        stack.select(1); stack.select(1)
        assertEquals(1, stack.selectedTab)
        assertEquals(2, stack.selectionEpoch)
        assertFalse(stack.selectableText)
        assertTrue(proc.selectableText)
    }

    @Test fun statsCopyIncludesAllByDefaultAndSelectedRowsInSourceOrder() {
        val state = DiagnosticSelectionState(listOf("first", "second", "third"), "TIMED OUT\nfirst\nsecond\nthird")
        assertEquals("TIMED OUT\nfirst\nsecond\nthird", state.textToCopy())
        state.toggle(2); state.toggle(0)
        assertEquals("first\nthird", state.textToCopy())
        state.toggle(0); state.toggle(2)
        assertEquals("TIMED OUT\nfirst\nsecond\nthird", state.textToCopy())
    }

    @Test fun databaseDumpTextRemainsAppendableWithoutOpeningAnyDatabase() {
        val state = DatabaseDumpState()
        assertEquals("Select a table to load its dump", state.text)
        state.append("\nLoading FakeTable ...\n")
        state.text = state.text.toString().replace("Loading FakeTable ...", "")
        state.append("\n===== TABLE: FakeTable =====\n")
        state.append("fake | values\n[rows: 1]\n")
        assertFalse(state.text.contains("Loading"))
        assertTrue(state.text.endsWith("fake | values\n[rows: 1]\n"))
    }

    @Test fun dialogActionPrecedesDismissAndCannotBeRepeated() {
        val calls = mutableListOf<String>()
        val dialog = fakeDialog(calls)
        val gate = AboutDialogActionGate()
        gate.dispatch(dialog, DialogInterface.BUTTON_POSITIVE) { _, which -> calls += "action:$which" }
        gate.dispatch(dialog, DialogInterface.BUTTON_NEGATIVE) { _, _ -> fail("second action") }
        assertEquals(listOf("action:-1", "dismiss"), calls)
    }

    @Test fun refreshCanExplicitlyDismissBeforeItsCallbackStartsReplacement() {
        val calls = mutableListOf<String>()
        val dialog = fakeDialog(calls)
        AboutDialogActionGate().dispatch(dialog, DialogInterface.BUTTON_NEUTRAL) { d, _ -> d.dismiss(); calls += "refresh" }
        assertEquals(listOf("dismiss", "refresh", "dismiss"), calls)
    }

    @Test fun cancelDoesNotInvokeAnyPositiveAction() {
        val calls = mutableListOf<String>()
        AboutDialogActionGate().dispatch(fakeDialog(calls), DialogInterface.BUTTON_NEGATIVE) { d, _ -> d.dismiss() }
        assertEquals(listOf("dismiss", "dismiss"), calls)
    }

    @Test fun fileRowsContainOnlyDisplayMetadataAndExplicitRecordingCallbacks() {
        val calls = mutableListOf<String>()
        val check = WgControlState(checkable = true).apply {
            isChecked = true
            setOnCheckedChangeListener { _, value -> calls += "selected:$value" }
        }
        val row = BugReportRowState("fake", "fake.txt", "12 KB - fake date", R.drawable.ic_logs, check,
            onOpen = { calls += "open" }, onDelete = { calls += "request-delete" })
        row.onDelete(); row.onOpen(); row.checkbox.performClick()
        assertEquals(listOf("request-delete", "open", "selected:false"), calls)
    }

    @Test fun fileChecksRestoreChangedOnlyInLayoutOrderWithoutRestoringFilePayloads() {
        val first = BugReportFilesState(context).apply {
            brbsProcInfoCheckbox.isChecked = false; brbsSelectAllCheckbox.isChecked = false
        }
        val saved = Bundle().also(first::saveChecks)
        val second = BugReportFilesState(context)
        val calls = mutableListOf<String>()
        second.brbsProcInfoCheckbox.setOnCheckedChangeListener { _, v -> calls += "proc:$v" }
        second.brbsSelectAllCheckbox.setOnCheckedChangeListener { _, v -> calls += "all:$v" }
        second.restoreChecks(saved); second.restoreChecks(saved)
        assertEquals(listOf("proc:false", "all:false"), calls)
        assertTrue(second.rows.isEmpty())
    }

    @Test fun dnsToggleDispatchesOldUncheckedBeforeNewCheckedAndIgnoresRetap() {
        val group = DiagnosticToggleGroup(1)
        val calls = mutableListOf<String>()
        group.addOnButtonCheckedListener { _, id, checked -> calls += "$id:$checked" }
        group.check(2); group.check(2); group.check(1)
        assertEquals(listOf("1:false", "2:true", "2:false", "1:true"), calls)
    }

    @Test fun dnsModeRestoreRoutesThroughSameChangedOnlyListener() {
        val state = DnsRecordTypesState(context)
        state.drbsModeToggleGroup.check(R.id.drbs_manual_mode_btn)
        val saved = Bundle().also(state::saveMode)
        val restored = DnsRecordTypesState(context)
        val calls = mutableListOf<Int>()
        restored.drbsModeToggleGroup.addOnButtonCheckedListener { _, id, selected -> if (selected) calls += id }
        restored.restoreMode(saved); restored.restoreMode(saved)
        assertEquals(listOf(R.id.drbs_manual_mode_btn), calls)
    }

    private fun fakeDialog(calls: MutableList<String>) = object : DialogInterface {
        override fun cancel() { calls += "cancel" }
        override fun dismiss() { calls += "dismiss" }
    }
}
