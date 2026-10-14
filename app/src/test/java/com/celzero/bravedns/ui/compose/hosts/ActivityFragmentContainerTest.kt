/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.hosts

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.ActivityFragmentContainer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Isolated framework bridge tests. Only a plain Application, FragmentActivity and synthetic
 * Compose Fragment are constructed. No production Activity, DI, database, managers, backend,
 * permissions, networking, accounts, VPN or exports can be reached by this fixture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ActivityFragmentContainerTest {
    @get:Rule val compose = createEmptyComposeRule()
    private var controller: ActivityController<SyntheticComposeHostActivity>? = null
    private val activity get() = checkNotNull(controller).get()
    private val fragment get() = activity.supportFragmentManager
        .findFragmentById(R.id.fragment_container) as SyntheticStateFragment

    private fun launch(saved: Bundle? = null, legacy: Boolean = false) {
        check(ApplicationProvider.getApplicationContext<Application>().javaClass == Application::class.java)
        val intent = Intent(ApplicationProvider.getApplicationContext(), SyntheticComposeHostActivity::class.java)
            .putExtra("synthetic.legacy", legacy)
        controller = Robolectric.buildActivity(SyntheticComposeHostActivity::class.java, intent)
            .create(saved).start().resume().visible()
        compose.waitForIdle()
        activity.supportFragmentManager.executePendingTransactions()
    }

    private fun saveAndClose(): Bundle {
        val state = Bundle()
        checkNotNull(controller).pause().saveInstanceState(state).stop().destroy()
        controller = null
        return state
    }

    @After fun close() {
        controller?.let { if (!it.get().isDestroyed) it.pause().stop().destroy() }
        controller = null
    }

    @Test fun sameResourceIdAndPrimaryFragmentSurviveRecomposition() {
        launch()
        val original = fragment
        compose.runOnIdle { activity.recompositions.intValue++ }
        compose.waitForIdle()
        assertSame(original, fragment)
        assertSame(fragment, activity.supportFragmentManager.primaryNavigationFragment)
        assertEquals(R.id.fragment_container, fragment.id)
        assertTrue(activity.findViewById<View>(android.R.id.content) is ViewGroup)
        val root = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        assertTrue(root is ComposeView)
    }

    @Test fun activityRecreationRestoresFragmentStateAndArguments() {
        launch()
        compose.runOnIdle { fragment.savedSelection = 42 }
        val state = saveAndClose()
        launch(state)
        assertEquals(42, fragment.savedSelection)
        assertEquals("synthetic argument", fragment.requireArguments().getString("label"))
        assertSame(fragment, activity.supportFragmentManager.primaryNavigationFragment)
        assertSame(activity.findViewById<View>(R.id.fragment_container), fragment.requireView().parent)
        assertEquals(1, activity.supportFragmentManager.fragments.size)
    }

    @Test fun savedStateFromPreComposeStableIdHostReconnectsWithoutReplacement() {
        launch(legacy = true)
        compose.runOnIdle { fragment.savedSelection = 73 }
        val state = saveAndClose()
        // Mirrors an upgrade from the previous View-based Activity shell. The IDs and saved
        // FragmentManager state are deliberately identical; no production layout is inflated.
        launch(saved = state, legacy = false)
        assertEquals(73, fragment.savedSelection)
        assertSame(activity.findViewById<View>(R.id.fragment_container), fragment.requireView().parent)
        assertEquals(1, activity.supportFragmentManager.fragments.size)
    }
}

class SyntheticComposeHostActivity : FragmentActivity() {
    val recompositions = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra("synthetic.legacy", false)) {
            val container = FragmentContainerView(this).apply { id = R.id.fragment_container }
            setContentView(container)
            if (savedInstanceState == null) {
                val fragment = newFragment()
                supportFragmentManager.beginTransaction().add(container, fragment, null)
                    .setPrimaryNavigationFragment(fragment).commitNow()
            }
        } else {
            setContent {
                MaterialTheme {
                    // Reading state forces an ordinary recomposition without changing host identity.
                    Text("Synthetic recomposition ${recompositions.intValue}")
                    ActivityFragmentContainer(
                        fragmentManager = supportFragmentManager,
                        containerId = R.id.fragment_container,
                        modifier = Modifier.fillMaxSize(),
                        primaryNavigationHost = true,
                        createFragment = ::newFragment,
                    )
                }
            }
        }
    }

    private fun newFragment() = SyntheticStateFragment().apply {
        arguments = Bundle().apply { putString("label", "synthetic argument") }
    }
}

class SyntheticStateFragment : Fragment() {
    var savedSelection = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedSelection = savedInstanceState?.getInt("synthetic.selection") ?: 0
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { MaterialTheme { Text("Synthetic fragment") } }
        }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("synthetic.selection", savedSelection)
        super.onSaveInstanceState(outState)
    }
}
