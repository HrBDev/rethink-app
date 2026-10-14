/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.rules
import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.RethinkDnsApplication
import com.celzero.bravedns.database.CustomDomain
import com.celzero.bravedns.database.CustomIp
import com.celzero.bravedns.service.DomainRulesManager
import com.celzero.bravedns.service.EventLogger
import com.celzero.bravedns.service.IpRulesManager
import com.celzero.bravedns.ui.activity.CustomRulesActivity.RULES
import com.celzero.bravedns.ui.fragment.RulesImportHelper
import com.celzero.bravedns.util.Constants.Companion.UID_EVERYBODY
import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Real UI controller, fake port. No manager/native trie/database/document/VPN is used. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class CustomRuleActionsTest {
    private val dispatcher = StandardTestDispatcher()
    private val owner = object : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
    private val state = RulesPageState()
    private val port = FakePort()
    private var originalDebug = false
    @Before fun before() {
        Dispatchers.setMain(dispatcher); owner.registry.currentState = Lifecycle.State.STARTED
        originalDebug = RethinkDnsApplication.DEBUG; RethinkDnsApplication.DEBUG = true
    }
    @After fun after() {
        owner.registry.currentState = Lifecycle.State.DESTROYED
        RethinkDnsApplication.DEBUG = originalDebug; Dispatchers.resetMain()
    }
    private fun actions(domain: Boolean = false, uid: Int = UID_EVERYBODY, rules: RULES = RULES.APP_SPECIFIC_RULES) = CustomRuleActions(
        ApplicationProvider.getApplicationContext(), owner, mock(EventLogger::class.java), domain, uid, rules, state, port, dispatcher, dispatcher)
    @Test fun invalidAndNonCidrIpsNeverWriteAndKeepDraftOpen() = runTest(dispatcher) {
        state.openEditor(); state.input = "bad"; port.ip = null
        actions().submit(true); advanceUntilIdle()
        assertEquals(R.string.ci_dialog_error_invalid_ip, state.error); assertEquals("bad", state.input)
        assertTrue(state.editorOpen); assertTrue(port.writes.isEmpty())
        port.ip = IPAddressString("192.0.2.1").address; port.cidr = false
        actions().submit(false); advanceUntilIdle()
        assertEquals(R.string.ci_dialog_error_invalid_cidr, state.error); assertTrue(port.writes.isEmpty())
    }
    @Test fun globalAndAppAllowKeepDifferentStatusesAndParserPort() = runTest(dispatcher) {
        state.openEditor(); state.input = ".192.0.2.1."
        actions().submit(false); advanceUntilIdle()
        assertEquals("192.0.2.1", port.parsedInput)
        assertEquals(listOf("add-ip:$UID_EVERYBODY:443:BYPASS_UNIVERSAL"), port.writes)
        assertEquals("", state.input); assertTrue(state.editorOpen)
        port.writes.clear(); state.input = "192.0.2.1"
        actions(uid = 42).submit(false); advanceUntilIdle()
        assertEquals(listOf("add-ip:42:443:TRUST"), port.writes)
    }
    @Test fun ipEditUsesOriginalEntityUidAndRetainsText() = runTest(dispatcher) {
        state.openEditor(CustomIp().apply { uid = 42; ipAddress = "192.0.2.1"; port = 0 })
        state.input = "192.0.2.2"
        actions().submit(false); advanceUntilIdle()
        assertEquals(listOf("replace-ip:42:443:TRUST"), port.writes)
        assertEquals("192.0.2.2", state.input); assertTrue(state.editorOpen)
    }
    @Test fun invalidDomainsAndWildcardsHaveSeparateErrorsWithoutWrites() = runTest(dispatcher) {
        state.openEditor(); state.input = "example.test"; port.host = null
        actions(true).submit(true); assertEquals(R.string.cd_dialog_error_invalid_domain, state.error)
        port.host = "*.example.test"; port.wildcard = false; state.domainType = DomainRulesManager.DomainType.WILDCARD
        actions(true).submit(true); assertEquals(R.string.cd_dialog_error_invalid_wildcard, state.error)
        state.domainType = DomainRulesManager.DomainType.DOMAIN; port.domain = false
        actions(true).submit(true); advanceUntilIdle()
        assertEquals(R.string.cd_dialog_error_invalid_domain, state.error); assertTrue(port.writes.isEmpty())
    }
    @Test fun onlyDomainEditTrimsBeforeExtractionAndBothDialogsStayOpen() = runTest(dispatcher) {
        state.openEditor(); state.input = "  example.test  "; port.host = ".example.test."
        actions(true).submit(false); advanceUntilIdle()
        assertEquals("  example.test  ", port.extractedInput)
        assertEquals(listOf("add-domain:example.test:$UID_EVERYBODY:DOMAIN:TRUST"), port.writes)
        assertEquals("  example.test  ", state.input); assertTrue(state.editorOpen)
        port.writes.clear(); state.editing = CustomDomain(null).apply { uid = 77; domain = "example.test" }
        actions(true).submit(true); advanceUntilIdle()
        assertEquals("example.test", port.extractedInput)
        assertEquals(listOf("replace-domain:example.test:77:DOMAIN:BLOCK"), port.writes)
    }
    @Test fun deleteSelectionOverridesUidOrAllAndClearOrderMatchesBaseline() = runTest(dispatcher) {
        state.longSelect(CustomIp().apply { uid = 55 }); state.query = "filtered"
        port.onWrite = { if (it.startsWith("delete-ips:")) assertFalse(state.selected.isEmpty()) }
        actions(uid = 42, rules = RULES.ALL_RULES).deleteConfirmed(); advanceUntilIdle()
        assertEquals(listOf("delete-ips:55"), port.writes); assertFalse(state.selectionMode)
        port.writes.clear(); actions(uid = 42).deleteConfirmed(); advanceUntilIdle()
        actions(rules = RULES.ALL_RULES).deleteConfirmed(); advanceUntilIdle()
        assertEquals(listOf("delete-ip-uid:42", "delete-all-ips"), port.writes)
        port.writes.clear(); state.longSelect(CustomDomain(null).apply { uid = 66; domain = "example.test" })
        port.onWrite = { if (it.startsWith("delete-domains:")) assertTrue(state.selected.isEmpty()) }
        actions(true).deleteConfirmed(); advanceUntilIdle()
        actions(true, 77).deleteConfirmed(); advanceUntilIdle()
        actions(true, rules = RULES.ALL_RULES).deleteConfirmed(); advanceUntilIdle()
        assertEquals(listOf("delete-domains:66", "delete-domain-uid:77", "delete-all-domains"), port.writes)
    }
    @Test fun importOnlyWritesAfterConfirmAndDuplicateClickIsIgnored() = runTest(dispatcher) {
        val actions = actions(); state.importUri = "content://fixture/rules.txt"
        actions.parseImport(state.importUri!!)
        assertNotNull(state.parsedImport); assertTrue(port.writes.isEmpty())
        state.importBlock = false; actions.importConfirmed(); actions.importConfirmed(); advanceUntilIdle()
        assertEquals(listOf("import:IP:$UID_EVERYBODY:BYPASS_UNIVERSAL:BLOCK"), port.writes)
        assertNull(state.importUri); assertNotNull(state.importSummary)
        port.writes.clear(); state.parsedImport = port.parsed
        actions(true, 42).importConfirmed(); advanceUntilIdle()
        assertEquals(listOf("import:DOMAIN:42:BLOCK:TRUST"), port.writes)
    }
    @Test fun releaseImportEntryPointsCannotReadOrWrite() = runTest(dispatcher) {
        RethinkDnsApplication.DEBUG = false; state.importUri = "content://fixture/rules.txt"; state.parsedImport = port.parsed
        actions().parseImport(state.importUri!!); actions().importConfirmed(); advanceUntilIdle()
        assertEquals(0, port.reads); assertTrue(port.writes.isEmpty())
    }
    private class FakePort : CustomRulePort {
        val writes = mutableListOf<String>(); var onWrite: (String) -> Unit = {}
        private fun write(value: String) { onWrite(value); writes += value }
        var reads = 0; var parsedInput = ""; var extractedInput = ""
        var ip: IPAddress? = IPAddressString("192.0.2.1").address
        var cidr = true; var host: String? = "example.test"; var wildcard = true; var domain = true
        val parsed = RulesImportHelper.ParsedFile("rules.txt", listOf("192.0.2.1"), 2)
        override fun normalize(input: String) = input.trim('.')
        override fun parseIp(input: String): Pair<IPAddress?, Int> { parsedInput = input; return ip to 443 }
        override fun isCidrEnforceable(ip: IPAddress?) = cidr
        override fun extractHost(input: String): String? { extractedInput = input; return host }
        override fun isWildcard(host: String) = wildcard
        override fun isDomain(host: String) = domain
        override suspend fun addIp(uid: Int, ip: IPAddress, port: Int, status: IpRulesManager.IpRuleStatus) { write("add-ip:$uid:$port:$status") }
        override suspend fun replaceIp(previous: CustomIp, ip: IPAddress, port: Int, status: IpRulesManager.IpRuleStatus) { write("replace-ip:${previous.uid}:$port:$status") }
        override suspend fun addDomain(domain: String, status: DomainRulesManager.Status, type: DomainRulesManager.DomainType, uid: Int) { write("add-domain:$domain:$uid:$type:$status") }
        override suspend fun replaceDomain(domain: String, status: DomainRulesManager.Status, type: DomainRulesManager.DomainType, previous: CustomDomain) { write("replace-domain:$domain:${previous.uid}:$type:$status") }
        override suspend fun deleteIps(items: List<CustomIp>) { write("delete-ips:" + items.joinToString { it.uid.toString() }) }
        override suspend fun deleteIpsByUid(uid: Int) { write("delete-ip-uid:$uid") }
        override suspend fun deleteAllIps() { write("delete-all-ips") }
        override suspend fun deleteDomains(items: List<CustomDomain>) { write("delete-domains:" + items.joinToString { it.uid.toString() }) }
        override suspend fun deleteDomainsByUid(uid: Int) { write("delete-domain-uid:$uid") }
        override suspend fun deleteAllDomains() { write("delete-all-domains") }
        override suspend fun parseImport(uri: Uri, type: RulesImportHelper.ImportType): RulesImportHelper.ParsedFile { reads++; return parsed }
        override suspend fun importRules(entries: List<String>, importType: RulesImportHelper.ImportType, uid: Int, ipStatus: IpRulesManager.IpRuleStatus, domainStatus: DomainRulesManager.Status): RulesImportHelper.ImportSummary {
            write("import:$importType:$uid:$ipStatus:$domainStatus"); return RulesImportHelper.ImportSummary(1, 0, 2)
        }
    }
}
