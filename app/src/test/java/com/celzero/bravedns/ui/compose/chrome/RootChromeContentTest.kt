/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.chrome

import android.app.Application
import android.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.celzero.bravedns.ui.compose.themeColor
import com.celzero.bravedns.ui.compose.home.HomeTourAnchors
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Plain Application + synthetic callback recorders only. No production Activity/Fragment,
 * service, updater, restore worker, preferences, account, repository or VPN is constructed. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class RootChromeContentTest {
    @get:Rule val compose = createComposeRule()

    @Test fun fiveTabsRetainOrderIdsSelectedStateAndWindowAnchors() {
        val selected = mutableIntStateOf(R.id.homeScreenFragment)
        val calls = mutableListOf<Int>()
        val anchors = HomeTourAnchors()
        val ids = listOf(R.id.homeScreenFragment, R.id.summaryStatisticsFragment, R.id.rethinkPlus,
            R.id.configureFragment, R.id.aboutFragment)
        assertEquals(ids, rootNavigationItems.map { it.id })
        compose.setContent { Theme { HomeBottomNavigation(selected.intValue, anchors) { calls += it; selected.intValue = it } } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("root_nav_${ids.first()}").assertIsSelected()
        ids.drop(1).forEach { id -> compose.onNodeWithTag("root_nav_$id").performClick().assertIsSelected() }
        compose.runOnIdle {
            assertEquals(ids.drop(1), calls)
            ids.forEach { assertNotNull(anchors.find(it)) }
        }
    }


    @Test fun anchoredViewportAndSystemStripStayContiguousWhileContentScrolls() {
        val inset = mutableIntStateOf(0)
        val anchors = HomeTourAnchors()
        compose.setContent { Theme {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.size(320.dp, 400.dp).testTag("shell")) {
                    HomeChromeLayout(R.id.homeScreenFragment, anchors, {},
                        WindowInsets(0, 0, 0, inset.intValue)) {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("scroll")) {
                            repeat(30) { Box(Modifier.fillMaxWidth().height(60.dp).background(Color.Red)) }
                        }
                    }
                }
            }
        } }
        var initialBarHeight = 0f
        for (bottom in listOf(0, 24, 48, 0)) {
            compose.runOnIdle { inset.intValue = bottom }
            val shell = compose.onNodeWithTag("shell").fetchSemanticsNode().boundsInRoot
            val viewport = compose.onNodeWithTag("root_content_viewport").fetchSemanticsNode().boundsInRoot
            val nav = compose.onNodeWithTag("root_navigation").fetchSemanticsNode()
            val bar = nav.boundsInRoot
            if (initialBarHeight == 0f) initialBarHeight = bar.height
            assertEquals(viewport.bottom, bar.top, 0.01f)
            assertEquals(shell.bottom, bar.bottom, 0.01f)
            assertEquals(shell.left, bar.left, 0.01f)
            assertEquals(shell.right, bar.right, 0.01f)
            assertEquals(initialBarHeight + bottom, bar.height, 0.01f)
            compose.onNodeWithTag("scroll").performTouchInput { swipeUp() }
            val afterScroll = compose.onNodeWithTag("root_navigation").fetchSemanticsNode().boundsInRoot
            assertEquals(bar, afterScroll)
            compose.runOnIdle {
                val registered = checkNotNull(anchors.find(R.id.nav_view))
                assertEquals(nav.boundsInWindow.top, registered.top, 0.01f)
                assertEquals(nav.boundsInWindow.bottom, registered.bottom, 0.01f)
            }
        }
    }

    @Test fun navigationPaintsOpaqueCornersAndInsetInEveryTheme() {
        val selectedTheme = mutableIntStateOf(R.style.AppThemeWhite)
        var navColor = Color.Unspecified
        compose.setContent { Theme(selectedTheme.intValue) {
            navColor = themeColor(R.attr.navBarColor).copy(alpha = 1f)
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.size(320.dp, 400.dp).background(Color.Red)) {
                    HomeChromeLayout(R.id.homeScreenFragment, remember { HomeTourAnchors() }, {},
                        WindowInsets(0, 0, 0, 24)) {
                        Box(Modifier.fillMaxSize().background(Color.Red))
                    }
                }
            }
        } }
        for (theme in listOf(R.style.AppThemeWhite, R.style.AppTheme, R.style.AppThemeTrueBlack, R.style.AppThemeTrueBlackFrost,
            R.style.AppThemeWhitePlus, R.style.AppThemeTrueBlackPlus)) {
            compose.runOnIdle { selectedTheme.intValue = theme }
            val pixels = compose.onNodeWithTag("root_navigation").captureToImage().toPixelMap()
            assertEquals(1f, navColor.alpha, 0f)
            // Neither the former clipped corners nor the system strip may expose content.
            assertEquals(navColor, pixels[0, 0])
            assertEquals(navColor, pixels[pixels.width - 1, 0])
            assertEquals(navColor, pixels[0, pixels.height - 1])
            assertEquals(navColor, pixels[pixels.width / 2, pixels.height - 1])
        }
    }

    @Test fun frameworkContainerReceivesOnlyTheMeasuredDestinationViewport() {
        val height = mutableIntStateOf(400)
        val inset = mutableIntStateOf(24)
        val anchors = HomeTourAnchors()
        var container: FragmentContainerView? = null
        compose.setContent { Theme {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.size(320.dp, height.intValue.dp)) {
                    HomeChromeLayout(R.id.homeScreenFragment, anchors, {},
                        WindowInsets(0, 0, 0, inset.intValue)) {
                        // Exercise the same AndroidView measurement bridge without starting
                        // a production Activity, Fragment, NavHost or controller.
                        AndroidView(
                            factory = { context -> FragmentContainerView(context).also { container = it } },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        } }
        for ((viewportHeight, bottomInset) in listOf(400 to 24, 300 to 48, 400 to 0)) {
            compose.runOnIdle { height.intValue = viewportHeight; inset.intValue = bottomInset }
            val viewport = compose.onNodeWithTag("root_content_viewport").fetchSemanticsNode().boundsInWindow
            compose.runOnIdle {
                val host = checkNotNull(container)
                val location = IntArray(2)
                host.getLocationInWindow(location)
                assertEquals(viewport.width, host.measuredWidth.toFloat(), 0.01f)
                assertEquals(viewport.height, host.measuredHeight.toFloat(), 0.01f)
                assertEquals(viewport.bottom, (location[1] + host.height).toFloat(), 0.01f)
                assertEquals(viewport.bottom, checkNotNull(anchors.find(R.id.nav_view)).top, 0.01f)
            }
        }
    }

    @Test fun homeClearancePolicyReachesNestedComposeViewWithoutChangingStandalonePadding() {
        var standaloneGap: Dp? = null
        var nestedGap: Dp? = null
        compose.setContent { Theme {
            val gap = homeNavigationClearance(90.dp, contentGap = 10.dp)
            SideEffect { standaloneGap = gap }
            Box(Modifier.size(320.dp, 400.dp)) {
                HomeChromeLayout(R.id.homeScreenFragment, remember { HomeTourAnchors() }, {},
                    WindowInsets(0, 0, 0, 24)) {
                    AndroidView(
                        factory = { context -> ComposeView(context).apply {
                            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                            setContent {
                                val embedded = homeNavigationClearance(90.dp, contentGap = 10.dp)
                                SideEffect { nestedGap = embedded }
                                Box(Modifier.fillMaxSize().testTag("nested_destination"))
                            }
                        } },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        } }
        compose.onNodeWithTag("nested_destination").assertExists()
        compose.runOnIdle {
            assertEquals(90.dp, standaloneGap)
            assertEquals(10.dp, nestedGap)
        }
    }

    @Test fun navigationGeometryFollowsViewportResizeAndCompositionLifetime() {
        val anchors = HomeTourAnchors()
        val height = mutableIntStateOf(400)
        val visible = mutableStateOf(true)
        val events = mutableListOf<Float?>()
        val stop = anchors.observeBounds(R.id.nav_view) { events += it?.top }
        compose.setContent { Theme {
            CompositionLocalProvider(LocalDensity provides Density(1f), LocalLayoutDirection provides LayoutDirection.Rtl) {
                if (visible.value) Box(Modifier.size(320.dp, height.intValue.dp)) {
                    HomeChromeLayout(R.id.homeScreenFragment, anchors, {}, WindowInsets(0, 0, 0, 24)) {}
                }
            }
        } }
        compose.waitForIdle()
        val original = checkNotNull(anchors.find(R.id.nav_view)).top
        compose.runOnIdle { height.intValue = 300 }
        compose.runOnIdle { assertEquals(original - 100, checkNotNull(anchors.find(R.id.nav_view)).top, 0.01f) }
        compose.runOnIdle { visible.value = false }
        compose.runOnIdle { assertNull(anchors.find(R.id.nav_view)); assertNull(events.last()) }
        compose.runOnIdle { visible.value = true }
        compose.runOnIdle {
            val restored = checkNotNull(anchors.find(R.id.nav_view))
            assertEquals(original - 100, restored.top, 0.01f)
            stop()
        }
    }

    @Test fun childClearanceCountsOnlyGeometryThatActuallyOverlapsItsViewport() {
        // Anchored Home viewport ends at the bar; no repeated bar/inset padding is owed.
        assertEquals(0, navigationOverlapBottomPx(696, 696f))
        assertEquals(0, navigationOverlapBottomPx(650, 696f))
        // Compatibility with an overlay host still reserves its complete visible overlap.
        assertEquals(104, navigationOverlapBottomPx(800, 696f))
        assertEquals(105, navigationOverlapBottomPx(800, 695.5f))
    }

    @Test fun updateModalRendersExplicitChoicesAndPostponesBeforeDismissal() {
        val calls = mutableListOf<String>()
        val visible = mutableStateOf(true)
        val dialog = HomeChromeDialog("Synthetic update", "Available", "Download", "Later", false,
            onPositive = { calls += "download" }, onNegative = { calls += "postpone" })
        compose.setContent { Theme { if (visible.value) HomeChromeDialogContent(dialog) {
            calls += "dismiss"; visible.value = false
        } } }
        compose.onNodeWithTag("root_dialog_positive").assertIsDisplayed()
        compose.onNodeWithTag("root_dialog_negative").performClick()
        compose.onNodeWithText("Available").assertDoesNotExist()
        compose.runOnIdle { assertEquals(listOf("postpone", "dismiss"), calls) }
    }

    @Test fun restoreIsSilentUntilExplicitPositiveAndEmitsOrderedCallbacksOnce() {
        val calls = mutableListOf<String>()
        val visible = mutableStateOf(true)
        val dialog = HomeChromeDialog("Synthetic restore", "Confirm", "Restore", "Cancel", onPositive = {
            calls += "start"; calls += "observe"
        })
        compose.setContent { Theme { if (visible.value) HomeChromeDialogContent(dialog) {
            calls += "dismiss"; visible.value = false
        } } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("root_dialog_positive").performClick()
        compose.runOnIdle { assertEquals(listOf("start", "observe", "dismiss"), calls) }
    }

    @Test fun informationHasOnlyOk() {
        val calls = mutableListOf<String>()
        val visible = mutableStateOf(true)
        val dialog = HomeChromeDialog("Current", "Synthetic current version", "OK")
        compose.setContent { Theme { if (visible.value) HomeChromeDialogContent(dialog) {
            calls += "dismiss"; visible.value = false
        } } }
        compose.onNodeWithTag("root_dialog_negative").assertDoesNotExist()
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("root_dialog_positive").performClick()
        compose.runOnIdle { assertEquals(listOf("dismiss"), calls) }
    }

    @Test fun snackbarRecompositionIsSilentAndActionIsDistinctFromDismiss() {
        val message = mutableStateOf("Synthetic message")
        val calls = mutableListOf<String>()
        compose.setContent { Theme { NativeSnackbarContent(message.value, "Run", ComposeSnackbarPresenter.Appearance.Themed,
            null, { calls += "action" }, { calls += "dismiss" }) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()); message.value = "Updated synthetic message" }
        compose.onNodeWithTag("native_snackbar").assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        compose.onNodeWithTag("native_snackbar_action").performClick()
        compose.onNodeWithTag("native_snackbar").performSemanticsAction(SemanticsActions.Dismiss) { it() }
        compose.runOnIdle { assertEquals(listOf("action", "dismiss"), calls) }
    }

    @Test fun actionlessSnackbarHasNoActionAndClampsToThreeMessageLines() {
        val message = "A deliberately long synthetic message. ".repeat(30)
        compose.setContent { Theme { Box(Modifier.width(200.dp)) {
            NativeSnackbarContent(message, null, ComposeSnackbarPresenter.Appearance.Themed, null,
                { error("No action should exist") }, {})
        } } }
        compose.onNodeWithTag("native_snackbar_action").assertDoesNotExist()
        compose.onNodeWithText(message).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { read ->
            val layouts = mutableListOf<TextLayoutResult>()
            assertTrue(read(layouts)); assertEquals(3, layouts.single().lineCount)
        }
    }

    @Test fun largeRtlSnackbarKeepsActionAvailableWithoutWritesDuringComposition() {
        val calls = mutableListOf<String>()
        compose.setContent { Theme {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalDensity provides Density(density.density, 2f)) {
                NativeSnackbarContent("Synthetic RTL", "Run", ComposeSnackbarPresenter.Appearance.Themed,
                    null, { calls += "action" }, { calls += "dismiss" })
            }
        } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("native_snackbar_action").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(listOf("action"), calls) }
    }

    @Composable private fun Theme(theme: Int = R.style.AppThemeWhite, content: @Composable () -> Unit) {
        val context = LocalContext.current
        val wrapped = remember(context, theme) { ContextThemeWrapper(context, theme) }
        CompositionLocalProvider(LocalContext provides wrapped) { RethinkTheme(content) }
    }
}
