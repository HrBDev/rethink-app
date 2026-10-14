/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.rules
import android.app.Application
import androidx.compose.runtime.saveable.SaverScope
import com.celzero.bravedns.database.CustomDomain
import com.celzero.bravedns.database.CustomIp
import com.celzero.bravedns.service.DomainRulesManager
import com.celzero.bravedns.service.IpRulesManager
import com.celzero.bravedns.ui.fragment.RulesImportHelper
import com.celzero.bravedns.util.Constants.Companion.UID_EVERYBODY
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class RulesPageStateTest {
    @Test fun headersFollowPagingUidTransitionsWithoutClientSorting() {
        assertFalse(showRuleHeader(false, 0, null, 10))
        assertTrue(showRuleHeader(true, 0, null, 10))
        assertFalse(showRuleHeader(true, 1, 10, 10))
        assertTrue(showRuleHeader(true, 2, 10, 20))
        assertTrue(showRuleHeader(true, 3, null, 20))
    }
    @Test fun selectionStaysActiveWhenLastItemIsUnchecked() {
        val state = RulesPageState()
        val ip = CustomIp().apply { uid = 42; ipAddress = "192.0.2.1" }
        state.longSelect(ip); state.longSelect(ip)
        assertEquals(listOf(ip), state.selected)
        state.toggleSelection(ip)
        assertTrue(state.selectionMode); assertTrue(state.selected.isEmpty())
        state.clearSelection(); assertFalse(state.selectionMode)
    }
    @Test fun rowKeysIncludeUidPortProtocolButNotMutableStatus() {
        val ip = CustomIp().apply { uid = 42; ipAddress = "192.0.2.1"; port = 80; protocol = "tcp" }
        assertEquals("ip:42:192.0.2.1:80:tcp", ruleKey(ip))
        val key = ruleKey(ip); ip.status++; ip.modifiedDateTime++
        assertEquals(key, ruleKey(ip))
        val domain = CustomDomain(null).apply { uid = 99; this.domain = "example.test" }
        assertEquals("domain:99:example.test", ruleKey(domain))
    }
    @Test fun allowMappingRetainsGlobalBypassAndPerAppTrust() {
        assertEquals(IpRulesManager.IpRuleStatus.BYPASS_UNIVERSAL, ipActionStatus(false, UID_EVERYBODY))
        assertEquals(IpRulesManager.IpRuleStatus.TRUST, ipActionStatus(false, 42))
        assertEquals(IpRulesManager.IpRuleStatus.BLOCK, ipActionStatus(true, UID_EVERYBODY))
        assertEquals(IpRulesManager.IpRuleStatus.BLOCK, ipActionStatus(true, 42))
    }
    @Test fun resumeResetsBeforeFocusAndSuppressesSubsequentUnchangedEmptyCallbacks() {
        val state = RulesPageState().apply { query = "fixture" }
        val trace = mutableListOf<String>()
        resumeRulesSearch(state, { trace += "query:$it" }, { trace += "focus:${state.query}" })
        resumeRulesSearch(state, { trace += "query:$it" }, { trace += "focus:${state.query}" })
        assertEquals(listOf("query:", "focus:", "focus:"), trace)
    }
    @Test fun firstEmptyResumeDispatchesLikeNewSearchView() {
        val state = RulesPageState()
        val trace = mutableListOf<String>()
        repeat(2) { resumeRulesSearch(state, { trace += "query:$it" }, { trace += "focus" }) }
        assertEquals(listOf("query:", "focus", "focus"), trace)
    }
    @Test fun queryChangesAreImmediateAndOnlySubmitRejectsAsciiBlank() {
        val state = RulesPageState(); val trace = mutableListOf<String>()
        updateRulesSearch(state, "a", trace::add); assertEquals(listOf("a"), trace)
        updateRulesSearch(state, "a", trace::add)
        updateRulesSearch(state, " \t", trace::add)
        submitRulesSearch(state, trace::add)
        assertEquals(listOf("a", " \t"), trace)
        state.query = "  fixture  "; submitRulesSearch(state, trace::add)
        state.query = "\u00a0"; submitRulesSearch(state, trace::add)
        assertEquals(listOf("a", " \t", "  fixture  ", "\u00a0"), trace)
    }
    @Test fun saverRetainsOnlyQueryAndDropsAllOrdinaryDialogSelectionState() {
        val state = RulesPageState().apply {
            query = "fixture"
            openEditor(CustomDomain(null).apply { uid = 42; domain = "example.test" })
            input = "*.example.test"; domainType = DomainRulesManager.DomainType.WILDCARD; error = 77
            longSelect(CustomIp().apply { uid = 42 })
            deleteOpen = true; importUri = "content://fixture/rules.txt"
            parsedImport = RulesImportHelper.ParsedFile("rules.txt", listOf("example.test"), 0)
            importBlock = false; importSummary = "fixture summary"
        }
        val saved = with(RulesPageState.Saver) { object : SaverScope { override fun canBeSaved(value: Any) = true }.save(state) }!!
        assertEquals("fixture", saved)
        val restored = RulesPageState.Saver.restore(saved)!!
        assertEquals("fixture", restored.query); assertNull(restored.lastQueryText)
        assertFalse(restored.editorOpen); assertNull(restored.editing); assertEquals("", restored.input)
        assertEquals(DomainRulesManager.DomainType.DOMAIN, restored.domainType); assertNull(restored.error)
        assertFalse(restored.selectionMode); assertTrue(restored.selected.isEmpty()); assertFalse(restored.deleteOpen)
        assertNull(restored.importUri); assertNull(restored.parsedImport); assertTrue(restored.importBlock); assertNull(restored.importSummary)
    }
}
