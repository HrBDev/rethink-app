/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.settings

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
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

/**
 * Plain Application, synthetic Fragment/state, and framework hosts only. No application
 * controllers, dependency injection, settings writes, managers, services or network calls.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp")
class SingleSettingsFragmentHostTest {
    @get:Rule val compose = createEmptyComposeRule()
    private var host: ActivityController<SettingsHostRestoreActivity>? = null
    private val activity get() = requireNotNull(host).get()

    @After fun tearDown() {
        val closing = host
        host = null
        if (closing != null && !closing.get().isDestroyed) closing.pause().stop().destroy()
    }

    @Test fun legacyPagerBundleAdoptsTheSameRestoredPageWithoutDuplicateLifecycleCallbacks() {
        launch(legacyPager = true)
        val legacyPage = page()
        assertEquals("f0", legacyPage.tag)
        assertEquals(0, legacyPage.id)
        compose.runOnIdle { legacyPage.marker = "saved legacy selection" }

        val saved = saveAndClose()
        launch(legacyPager = false, saved = saved)
        val restored = page()
        // Capture occurs immediately after super.onCreate, before Compose installs its host.
        assertSame(activity.restoredBeforeCompose, restored)
        assertEquals("saved legacy selection", restored.marker)
        assertNativeOwnership(restored)
        assertEquals(1, restored.createCount)
        assertEquals(1, restored.viewCreateCount)
        assertEquals(1, restored.resumeCount)
        assertEquals(0, restored.destroyViewCount)
        assertEquals(0, restored.destroyCount)
        compose.onNodeWithText("saved legacy selection").assertIsDisplayed()
    }

    @Test fun migratedPageRetainsStateAndSingleOwnershipOnTheNextRecreation() {
        launch(legacyPager = true)
        compose.runOnIdle { page().marker = "saved across both hosts" }
        launch(legacyPager = false, saved = saveAndClose())
        assertNativeOwnership(page())

        val saved = saveAndClose()
        launch(legacyPager = false, saved = saved)
        val restored = page()
        assertSame(activity.restoredBeforeCompose, restored)
        assertEquals("saved across both hosts", restored.marker)
        assertNativeOwnership(restored)
        assertEquals(1, restored.createCount)
        assertEquals(1, restored.viewCreateCount)
        assertEquals(1, restored.resumeCount)
        compose.onNodeWithText("saved across both hosts").assertIsDisplayed()
    }

    private fun launch(legacyPager: Boolean, saved: Bundle? = null) {
        val intent = Intent(ApplicationProvider.getApplicationContext<Application>(), SettingsHostRestoreActivity::class.java)
            .putExtra(SettingsHostRestoreActivity.LEGACY_PAGER, legacyPager)
        host = Robolectric.buildActivity(SettingsHostRestoreActivity::class.java, intent).also {
            it.create(saved).start().resume().visible()
        }
        compose.waitForIdle()
    }

    private fun saveAndClose(): Bundle {
        val closing = requireNotNull(host)
        host = null
        return Bundle().also { closing.pause().saveInstanceState(it).stop().destroy() }
    }

    private fun page(): SettingsRestoreProbeFragment {
        val fragments = activity.supportFragmentManager.fragments
        assertEquals(1, fragments.size)
        return fragments.single() as SettingsRestoreProbeFragment
    }

    private fun assertNativeOwnership(fragment: SettingsRestoreProbeFragment) {
        val root = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        assertTrue(root is ComposeView)
        assertFalse((root as ComposeView).consumeWindowInsets)
        assertEquals("f0", fragment.tag)
        assertEquals(R.id.fragment_container, fragment.id)
        val container = activity.findViewById<FragmentContainerView>(R.id.fragment_container)
        assertSame(container, fragment.requireView().parent)
        assertEquals(1, container.childCount)
        assertEquals(Lifecycle.State.RESUMED, fragment.lifecycle.currentState)
        assertEquals(Lifecycle.State.RESUMED, fragment.viewLifecycleOwner.lifecycle.currentState)
    }
}

/** Recreates the original one-page FragmentStateAdapter contract without any app controller. */
class SettingsHostRestoreActivity : FragmentActivity() {
    var restoredBeforeCompose: Fragment? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppThemeWhite)
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra(LEGACY_PAGER, false)) {
            setContentView(ViewPager2(this).apply {
                // A stable, synthetic pager ID; the adapter still uses its genuine "f0"
                // fragment tag, generated holder IDs, and zero fragment/container ID.
                id = android.R.id.list
                adapter = object : FragmentStateAdapter(this@SettingsHostRestoreActivity) {
                    override fun getItemCount() = 1
                    override fun createFragment(position: Int): Fragment = SettingsRestoreProbeFragment()
                }
            })
        } else {
            restoredBeforeCompose = supportFragmentManager.findFragmentByTag("f0")
            setSingleSettingsContent(SettingsRestoreProbeFragment::class.java)
        }
    }

    companion object {
        const val LEGACY_PAGER = "legacy_pager"
    }
}

class SettingsRestoreProbeFragment : Fragment() {
    var marker by mutableStateOf("initial selection")
    var createCount = 0
    var viewCreateCount = 0
    var resumeCount = 0
    var destroyViewCount = 0
    var destroyCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createCount++
        marker = savedInstanceState?.getString("marker") ?: marker
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewCreateCount++
        return ComposeView(inflater.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { RethinkTheme { Text(marker) } }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("marker", marker)
    }

    override fun onResume() { super.onResume(); resumeCount++ }
    override fun onDestroyView() { destroyViewCount++; super.onDestroyView() }
    override fun onDestroy() { destroyCount++; super.onDestroy() }
}
