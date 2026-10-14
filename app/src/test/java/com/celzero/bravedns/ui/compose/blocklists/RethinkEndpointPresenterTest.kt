package com.celzero.bravedns.ui.compose.blocklists

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.celzero.bravedns.R
import com.celzero.bravedns.data.AppConfig
import com.celzero.bravedns.database.RethinkDnsEndpoint
import com.celzero.bravedns.service.RethinkBlocklistManager
import com.celzero.bravedns.service.VpnController
import com.celzero.bravedns.ui.activity.ConfigureRethinkBasicActivity
import com.celzero.bravedns.util.Logger
import com.celzero.bravedns.util.UIUtils
import com.celzero.bravedns.util.Utilities
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Plain Application + strict fake boundaries. No BraveApp, service, IP lookup or tunnel. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
@OptIn(ExperimentalCoroutinesApi::class)
class RethinkEndpointPresenterTest {
    private val context = mockk<Context>()
    private val config = mockk<AppConfig>()
    private lateinit var owner: LifecycleOwner
    private lateinit var registry: LifecycleRegistry
    private val dispatcher = StandardTestDispatcher()
    @Before fun prepare() {
        Dispatchers.setMain(dispatcher)
        owner = object : LifecycleOwner { override val lifecycle: Lifecycle get() = registry }
        registry = LifecycleRegistry.createUnsafe(owner).apply { currentState = Lifecycle.State.STARTED }
        mockkObject(VpnController, Logger, UIUtils, Utilities)
        every { Logger.d(any(), any()) } just Runs
        every { context.getString(R.string.rdns_plus) } returns "Rethink Plus"
        every { context.getString(R.string.copy_clipboard_label) } returns "copy"
        every { context.getString(R.string.info_dialog_url_copy_toast_msg) } returns "copied"
        every { context.getString(R.string.ssv_toast_start_rethink) } returns "start"
        every { Utilities.showToastUiCentered(context, any(), any()) } just Runs
    }
    @After fun cleanup() { registry.currentState = Lifecycle.State.DESTROYED; unmockkAll(); Dispatchers.resetMain() }
    private fun endpoint(active: Boolean = false) = RethinkDnsEndpoint("Rethink Plus", "https://example.invalid/1:fake", 1, "fake description", active, true, 0, 3, 0L)
    private fun presenter() = RethinkEndpointPresenter(context, config, owner, dispatcher)

    @Test fun selectionMutatesBeforeExactManagerCall() = runTest(dispatcher) {
        val endpoint = endpoint()
        coEvery { config.handleRethinkChanges(endpoint) } coAnswers { assertTrue(endpoint.isActive) }
        presenter().select(endpoint); advanceUntilIdle()
        coVerify(exactly = 1) { config.handleRethinkChanges(endpoint) }
        verify(exactly = 0) { VpnController.hasTunnel() }
    }
    @Test fun reselectIsStillAnActionEachTime() = runTest(dispatcher) {
        val endpoint = endpoint(true)
        coEvery { config.handleRethinkChanges(endpoint) } just Runs
        val presenter = presenter(); presenter.select(endpoint); presenter.select(endpoint)
        advanceUntilIdle(); coVerify(exactly = 2) { config.handleRethinkChanges(endpoint) }
    }
    @Test fun constructionAndMetadataOpenHaveNoServiceReads() {
        val presenter = presenter(); val endpoint = endpoint()
        presenter.metadata = endpoint
        assertSame(endpoint, presenter.metadata)
        verify(exactly = 0) { VpnController.hasTunnel() }
        coVerify(exactly = 0) { config.handleRethinkChanges(any()) }
    }
    @Test fun editabilityUsesNameRatherThanCustomFlag() {
        val endpoint = endpoint().apply { isCustom = false }
        assertTrue(presenter().editable(endpoint))
        endpoint.name = "Built in"; endpoint.isCustom = true
        assertFalse(presenter().editable(endpoint))
    }
    @Test fun copyRetainsUrlLabelAndToastOrder() {
        val endpoint = endpoint()
        every { UIUtils.clipboardCopy(context, endpoint.url, "copy") } just Runs
        presenter().copy(endpoint)
        verifyOrder { UIUtils.clipboardCopy(context, endpoint.url, "copy"); Utilities.showToastUiCentered(context, "copied", any()) }
    }
    @Test fun editWithoutTunnelOnlyShowsGuardToast() {
        every { VpnController.hasTunnel() } returns false
        presenter().edit(endpoint())
        verify(exactly = 1) { Utilities.showToastUiCentered(context, "start", any()) }
        verify(exactly = 0) { context.startActivity(any()) }
    }
    @Test fun editWithFakeTunnelKeepsDestinationAndExactExtras() {
        every { VpnController.hasTunnel() } returns true
        every { context.packageName } returns "com.celzero.bravedns"
        val intent = slot<Intent>()
        every { context.startActivity(capture(intent)) } just Runs
        val endpoint = endpoint(); presenter().edit(endpoint)
        assertEquals(ConfigureRethinkBasicActivity::class.java.name, intent.captured.component?.className)
        assertEquals(endpoint.name, intent.captured.getStringExtra(ConfigureRethinkBasicActivity.RETHINK_BLOCKLIST_NAME))
        assertEquals(endpoint.url, intent.captured.getStringExtra(ConfigureRethinkBasicActivity.RETHINK_BLOCKLIST_URL))
        @Suppress("DEPRECATION")
        assertEquals(RethinkBlocklistManager.RethinkBlocklistType.REMOTE, intent.captured.getSerializableExtra(ConfigureRethinkBasicActivity.RETHINK_BLOCKLIST_TYPE))
    }
}
