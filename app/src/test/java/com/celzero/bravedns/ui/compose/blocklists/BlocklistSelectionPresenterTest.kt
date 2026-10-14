package com.celzero.bravedns.ui.compose.blocklists

import android.content.Context
import com.celzero.bravedns.database.*
import com.celzero.bravedns.service.RethinkBlocklistManager
import com.celzero.bravedns.ui.fragment.RethinkBlocklistFragment
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/** Strict doubles only. Never creates Koin, a database, a downloader, or a tunnel. */
@OptIn(ExperimentalCoroutinesApi::class)
class BlocklistSelectionPresenterTest {
    private val context = mockk<Context>()
    private val fragment = mockk<RethinkBlocklistFragment>()
    @Before fun prepare() {
        mockkObject(RethinkBlocklistManager)
        every { context.getString(any()) } returns "label"
        every { context.getString(any(), *anyVararg()) } returns "count"
        every { fragment.isAdded } returns true
        every { fragment.updateFileTagList(any()) } just Runs
        every { fragment.getSelectedFileTags() } returns emptySet()
    }
    @After fun cleanup() { unmockkAll() }
    private fun presenter(scope: TestScope) = StandardTestDispatcher(scope.testScheduler).let { BlocklistSelectionPresenter(context, fragment, it, it) }

    @Test fun localPackDeduplicatesWritesThenReadsThenPublishesExactlyOnce() = runTest {
        val events = mutableListOf<String>()
        coEvery { RethinkBlocklistManager.updateFiletagsLocal(setOf(1, 2), 1) } coAnswers { events += "write" }
        coEvery { RethinkBlocklistManager.getSelectedFileTagsLocal() } coAnswers { events += "read"; listOf(1, 2) }
        every { fragment.updateFileTagList(setOf(1, 2)) } answers { events += "publish" }
        presenter(this).localPack(LocalBlocklistPacksMap("fake", 0, listOf(1, 2, 2), "")).onSelected(true)
        advanceUntilIdle(); assertEquals(listOf("write", "read", "publish"), events)
        coVerify(exactly = 1) { RethinkBlocklistManager.updateFiletagsLocal(setOf(1, 2), 1) }
    }
    @Test fun remotePackDeselectionUsesZeroAndRemoteStore() = runTest {
        coEvery { RethinkBlocklistManager.updateFiletagsRemote(setOf(3), 0) } just Runs
        coEvery { RethinkBlocklistManager.getSelectedFileTagsRemote() } returns emptyList()
        presenter(this).remotePack(RemoteBlocklistPacksMap("fake", 1, listOf(3), "")).onSelected(false)
        advanceUntilIdle()
        coVerifyOrder { RethinkBlocklistManager.updateFiletagsRemote(setOf(3), 0); RethinkBlocklistManager.getSelectedFileTagsRemote() }
        verify(exactly = 1) { fragment.updateFileTagList(emptySet()) }
    }
    @Test fun detachedHostStillPersistsAndReadsWithoutPublishing() = runTest {
        every { fragment.isAdded } returns false
        coEvery { RethinkBlocklistManager.updateFiletagsLocal(setOf(1), 1) } just Runs
        coEvery { RethinkBlocklistManager.getSelectedFileTagsLocal() } returns listOf(1)
        presenter(this).localPack(LocalBlocklistPacksMap("fake", 0, listOf(1), "")).onSelected(true)
        advanceUntilIdle()
        coVerify(exactly = 1) { RethinkBlocklistManager.getSelectedFileTagsLocal() }
        verify(exactly = 0) { fragment.updateFileTagList(any()) }
    }
    @Test fun packCheckRequiresEveryTagWithoutWriting() = runTest {
        every { fragment.getSelectedFileTags() } returnsMany listOf(setOf(1), setOf(1, 2))
        val row = presenter(this).localPack(LocalBlocklistPacksMap("fake", 0, listOf(1, 2), ""))
        assertFalse(row.selected()); assertTrue(row.selected())
        coVerify(exactly = 0) { RethinkBlocklistManager.updateFiletagsLocal(any(), any()) }
    }
    @Test fun localAdvancedMutatesOriginalEntityBeforeWrite() = runTest {
        val tag = RethinkLocalFileTag(null).apply { uname = "fake" }
        coEvery { RethinkBlocklistManager.updateFiletagLocal(tag) } coAnswers { assertTrue(tag.isSelected) }
        coEvery { RethinkBlocklistManager.getSelectedFileTagsLocal() } returns listOf(8)
        presenter(this).localTag(tag).onSelected(true)
        advanceUntilIdle(); assertTrue(tag.isSelected)
        verify(exactly = 1) { fragment.updateFileTagList(setOf(8)) }
    }
    @Test fun remoteAdvancedRetainsDetachedCallbackGuard() = runTest {
        val tag = RethinkRemoteFileTag(null).apply { uname = "fake"; isSelected = true }
        every { fragment.isAdded } returns false
        coEvery { RethinkBlocklistManager.updateFiletagRemote(tag) } coAnswers { assertFalse(tag.isSelected) }
        coEvery { RethinkBlocklistManager.getSelectedFileTagsRemote() } returns emptyList()
        presenter(this).remoteTag(tag).onSelected(false)
        advanceUntilIdle(); assertFalse(tag.isSelected)
        verify(exactly = 0) { fragment.updateFileTagList(any()) }
    }
}
