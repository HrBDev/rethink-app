/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.chrome

import android.app.Application
import android.graphics.RectF
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.home.HomeTourAnchors
import com.celzero.bravedns.util.SnackbarHelper
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Plain ComponentActivity and Application isolate presentation from production startup. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class SnackbarContractTest {
    private lateinit var controller: org.robolectric.android.controller.ActivityController<ComponentActivity>
    private lateinit var root: FrameLayout
    @Before fun setup() {
        reset()
        controller = Robolectric.buildActivity(ComponentActivity::class.java)
        controller.get().setTheme(R.style.AppThemeWhite)
        val activity = controller.setup().get()
        root = FrameLayout(activity)
        activity.setContentView(root)
    }
    @After fun teardown() { SnackbarHelper.dismiss(); controller.pause().stop().destroy(); reset() }

    @Test fun durationsRetainMaterialAndCustomMilliseconds() {
        assertEquals(1_500L, snackbarBaseTimeout(-1)); assertEquals(2_750L, snackbarBaseTimeout(0))
        assertEquals(Long.MAX_VALUE, snackbarBaseTimeout(-2)); assertEquals(6_123L, snackbarBaseTimeout(6_123))
    }
    @Test fun nativeNavIsFoundWithoutBottomNavigationCastAndHiddenNavIsIgnored() {
        val nav = ComposeView(root.context).apply { id = R.id.nav_view }
        root.addView(nav)
        assertSame(nav, ComposeSnackbarPresenter.findBottomNavView(root))
        nav.visibility = View.GONE
        assertNull(ComposeSnackbarPresenter.findBottomNavView(root))
    }
    @Test fun throttleIsGlobalAcrossViewsAndSurvivesDismissal() {
        val first = View(root.context); val second = View(root.context)
        root.addView(first); root.addView(second)
        SnackbarHelper.show(first, "Duplicate", -2)
        val original = current()
        SnackbarHelper.show(second, "Duplicate", -2)
        assertSame(original, current())
        SnackbarHelper.dismiss()
        SnackbarHelper.show(second, "Duplicate", -2)
        assertNull(current())
        SnackbarHelper.show(second, "Duplicate", -2, forceShow = true)
        assertNotNull(current()); assertNotSame(original, current())
    }
    @Test fun oldDismissalCannotClearNewIdentity() {
        SnackbarHelper.show(root, "First", -2)
        val first = current()!!
        SnackbarHelper.show(root, "Second", -2)
        val second = current()!!
        detach(first)
        invoke(first, "finishDismissal") // a redundant, late callback must be harmless
        assertSame(second, current())
    }
    @Test fun actionClearsCurrentBeforeCallbackAndCannotEraseCallbackReplacement() {
        val calls = mutableListOf<String>()
        SnackbarHelper.show(root, "Action", -2, "Run", {
            assertNull(current()); calls += "action"
            SnackbarHelper.show(root, "Replacement", -2)
        })
        val first = current()!!
        invoke(first, "performAction")
        val replacement = current()!!
        invoke(first, "performAction"); detach(first); invoke(first, "finishDismissal")
        assertEquals(listOf("action"), calls); assertSame(replacement, current())
    }
    @Test fun manualDismissIsIdempotentAndNeverInvokesAction() {
        var actions = 0
        SnackbarHelper.show(root, "Manual", -2, "Run", { actions++ })
        val first = current()!!
        SnackbarHelper.dismiss(); SnackbarHelper.dismiss(); invoke(first, "performAction")
        assertEquals(0, actions); assertNull(current())
    }
    @Test fun helperAndUpdateAppearancesShareSingleActiveLatestPendingQueue() {
        val first = ComposeSnackbarPresenter(root, "Helper", -2)
        val second = ComposeSnackbarPresenter(root, "Update", -2, appearance = ComposeSnackbarPresenter.Appearance.Material)
        first.show(); second.show()
        detach(first)
        assertSame(second, presenterField("active"))
        assertNull(host(first).parent)
        assertNotNull(host(second).parent)
        second.dismiss(); detach(second)
        assertNull(presenterField("active")); assertNull(presenterField("pending"))
    }
    @Test fun nativeHomeAnchorKeepsSnackbarAboveBarAndInsideCappedContent() {
        val parent = controller.get().findViewById<FrameLayout>(android.R.id.content)
        parent.layout(0, 0, 1_000, 800)
        root.layout(0, 0, 1_000, 800)
        val nativeRoot = ComposeView(root.context).apply { id = R.id.container }
        root.addView(nativeRoot)
        nativeRoot.layout(200, 0, 800, 800)
        val registry = HomeTourAnchors.forActivity(controller.get())
        val owner = Any()
        registry.put(R.id.nav_view, owner, RectF(200f, 700f, 800f, 800f), { true }, {})
        try {
            assertNull(ComposeSnackbarPresenter.findBottomNavView(nativeRoot))
            val presenter = ComposeSnackbarPresenter(nativeRoot, "Native anchor", -2)
            presenter.show()
            invoke(presenter, "position")
            val params = host(presenter).layoutParams as android.view.ViewGroup.MarginLayoutParams
            val horizontal = root.resources.getDimensionPixelSize(R.dimen.snackbar_horizontal_margin)
            val vertical = root.resources.getDimensionPixelSize(R.dimen.snackbar_bottom_margin)
            assertEquals(200 + horizontal, params.leftMargin)
            assertEquals(200 + horizontal, params.rightMargin)
            val dispatchedInset = presenter.javaClass.getDeclaredField("insetBottom")
                .apply { isAccessible = true }.getInt(presenter)
            assertEquals(100 + vertical * 2 + dispatchedInset, params.bottomMargin)
            presenter.dismiss()
            detach(presenter)
        } finally {
            registry.remove(R.id.nav_view, owner)
        }
    }
    @Test fun themedInsetsKeepTheOriginalAdditiveEightyPixelLift() {
        SnackbarHelper.show(root, "Insets", -2)
        val host = current()!!.javaClass.getDeclaredField("host").apply { isAccessible = true }.get(current()) as ComposeView
        val before = (host.layoutParams as android.view.ViewGroup.MarginLayoutParams).bottomMargin
        val insets = WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, 20)).build()
        ViewCompat.dispatchApplyWindowInsets(host, insets)
        val first = (host.layoutParams as android.view.ViewGroup.MarginLayoutParams).bottomMargin
        ViewCompat.dispatchApplyWindowInsets(host, insets)
        val second = (host.layoutParams as android.view.ViewGroup.MarginLayoutParams).bottomMargin
        assertEquals(100, first - before); assertEquals(100, second - first)
    }
    @Test @Config(sdk = [28]) fun preQTouchExplorationKeepsActionableMessagesIndefinite() {
        shadowOf(root.context.getSystemService(AccessibilityManager::class.java)).setTouchExplorationEnabled(true)
        val duration = ComposeSnackbarPresenter::class.java.getDeclaredMethod("recommendedTimeout").apply { isAccessible = true }
        assertEquals(Long.MAX_VALUE, duration.invoke(ComposeSnackbarPresenter(root, "Action", -1, "Run", {})))
        assertEquals(1_500L, duration.invoke(ComposeSnackbarPresenter(root, "Plain", -1)))
    }
    private fun current() = SnackbarHelper::class.java.getDeclaredField("current").apply { isAccessible = true }.get(null) as? ComposeSnackbarPresenter
    private fun presenterField(name: String) = ComposeSnackbarPresenter::class.java.getDeclaredField(name).apply { isAccessible = true }.get(null)
    private fun invoke(target: Any, name: String) { target.javaClass.getDeclaredMethod(name).apply { isAccessible = true }.invoke(target) }
    private fun host(presenter: ComposeSnackbarPresenter) = presenter.javaClass.getDeclaredField("host")
        .apply { isAccessible = true }.get(presenter) as ComposeView
    private fun detach(presenter: ComposeSnackbarPresenter) {
        val view = host(presenter)
        (view.parent as? android.view.ViewGroup)?.removeView(view)
    }
    private fun reset() {
        listOf("current" to null, "lastMessage" to "", "lastShownAt" to 0L).forEach { (name, value) ->
            SnackbarHelper::class.java.getDeclaredField(name).apply { isAccessible = true }.set(null, value)
        }
        listOf("active", "pending").forEach { name ->
            ComposeSnackbarPresenter::class.java.getDeclaredField(name).apply { isAccessible = true }.set(null, null)
        }
    }
}
