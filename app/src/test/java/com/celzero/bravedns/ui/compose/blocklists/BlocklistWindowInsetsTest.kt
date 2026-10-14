/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.blocklists

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.paging.PagingData
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.ActivityFragmentContainer
import com.celzero.bravedns.ui.compose.RethinkTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.max

/**
 * AUTHORED / UNRUN. Exercises the real Compose→FragmentContainerView→Compose inset bridge with
 * a synthetic Activity/Fragment, plain Application, empty paging data and recorded callbacks.
 * No production controller/Application, dependency injection, VPN, store, network or real IME.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w400dp-h800dp-mdpi")
class BlocklistWindowInsetsTest {
    @get:Rule val compose = createEmptyComposeRule()
    private var controller: ActivityController<BlocklistInsetsProbeActivity>? = null
    private val activity get() = requireNotNull(controller).get()

    @After fun close() {
        controller?.let { if (!it.get().isDestroyed) it.pause().stop().destroy() }
        controller = null
    }

    @Test fun statusCutoutAndBothNavigationModesAreReservedExactlyOnceThroughFragmentHost() {
        launch()
        // Zero-inset legacy/content-fitted windows must not acquire fixed status/nav spacers.
        listOf(Edges(), Edges(top = 24, bottom = 24), Edges(top = 24, bottom = 48),
            Edges(left = 18, top = 32, right = 10, bottom = 24), Edges()).forEach { edges ->
            dispatch(edges)
            assertSafeBounds(edges)
        }
        compose.runOnIdle { assertTrue(activity.calls.isEmpty()) }
    }

    @Test fun keyboardUsesMaximumBottomInsetRatherThanAddingNavigationAndKeepsActions() {
        launch()
        compose.runOnIdle { activity.screen.showAdvanced = true; activity.screen.selectedAdvanced = true }
        listOf(0, 260, 180, 0).forEach { keyboard ->
            val edges = Edges(top = 24, bottom = 24, ime = keyboard)
            dispatch(edges)
            assertSafeBounds(edges)
            compose.onNodeWithTag("blocklist-tab-advanced").assertIsSelected()
        }
        compose.onNodeWithTag("blocklist-cancel").performClick()
        compose.onNodeWithTag("blocklist-apply").performClick()
        compose.runOnIdle { assertEquals(listOf("discard", "apply"), activity.calls) }
    }

    private fun launch() {
        controller = Robolectric.buildActivity(BlocklistInsetsProbeActivity::class.java).also {
            it.create().start().resume().visible()
        }
        compose.waitForIdle()
        assertFalse(activity.root.consumeWindowInsets)
    }

    private fun dispatch(edges: Edges) {
        val insets = WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, edges.top, 0, 0))
            .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, edges.bottom))
            .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(edges.left, edges.top, edges.right, 0))
            .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, edges.ime))
            .setVisible(WindowInsetsCompat.Type.statusBars(), edges.top > 0)
            .setVisible(WindowInsetsCompat.Type.navigationBars(), edges.bottom > 0)
            .setVisible(WindowInsetsCompat.Type.ime(), edges.ime > 0)
            .build()
        compose.runOnIdle { ViewCompat.dispatchApplyWindowInsets(activity.root, insets) }
        compose.waitForIdle()
    }

    private fun assertSafeBounds(edges: Edges) {
        val rootPosition = IntArray(2)
        compose.runOnIdle { activity.root.getLocationInWindow(rootPosition) }
        val rootLeft = rootPosition[0].toFloat()
        val rootTop = rootPosition[1].toFloat()
        val tabs = compose.onNodeWithTag("blocklist-tabs").fetchSemanticsNode().boundsInWindow
        val actions = compose.onNodeWithTag("blocklist-actions").fetchSemanticsNode().boundsInWindow
        val list = compose.onNodeWithTag("blocklist-list").fetchSemanticsNode().boundsInWindow
        assertEquals("selector below status/cutout, exactly once", rootTop + edges.top, tabs.top, 1f)
        assertEquals(rootLeft + edges.left, tabs.left, 1f)
        assertEquals(rootLeft + activity.root.width - edges.right, tabs.right, 1f)
        assertEquals("actions above gesture/navigation/IME, exactly once",
            rootTop + activity.root.height - max(edges.bottom, edges.ime), actions.bottom, 1f)
        assertTrue("scroll viewport cannot overlap the fixed footer", list.bottom <= actions.top)
        assertEquals(tabs.left, actions.left, 1f)
        assertEquals(tabs.right, actions.right, 1f)
    }

    private data class Edges(val left: Int = 0, val top: Int = 0, val right: Int = 0,
        val bottom: Int = 0, val ime: Int = 0)
}

/** Same pass-through policy as ConfigureRethinkBasicActivity, without its app dependencies. */
class BlocklistInsetsProbeActivity : FragmentActivity() {
    lateinit var root: ComposeView
    val calls = mutableListOf<String>()
    val screen = BlocklistConfigurationState().apply {
        lbBlocklistApplyBtn.text = "Apply synthetic selection"
        lbBlocklistApplyBtn.onClick = { calls += "apply" }
        lbBlocklistCancelBtn.onClick = { calls += "discard" }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(ComposeView(this).apply {
            root = this
            consumeWindowInsets = false
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { RethinkTheme {
                ActivityFragmentContainer(supportFragmentManager, R.id.root_container,
                    modifier = Modifier.fillMaxSize(), createFragment = { BlocklistInsetsProbeFragment() })
            } }
        })
    }
}

class BlocklistInsetsProbeFragment : Fragment() {
    private val pages = MutableStateFlow<PagingData<BlocklistRow>>(PagingData.empty())
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(inflater.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { RethinkTheme {
                val screen = (requireActivity() as BlocklistInsetsProbeActivity).screen
                BlocklistConfigurationScreen(screen, pages, pages, viewLifecycleOwner,
                    onTab = { screen.selectedAdvanced = it; screen.showAdvanced = it },
                    onQueryChange = { true }, onQuerySubmit = { true }, onFilter = {})
            } }
        }
}
