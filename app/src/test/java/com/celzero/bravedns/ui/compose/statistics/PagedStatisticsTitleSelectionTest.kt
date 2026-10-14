/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.statistics

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Looper
import android.view.ContextThemeWrapper
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Duration

/**
 * Presentation-only: Compose's empty test activity, a plain Application and synthetic headings.
 * Real Compose long-press/drag selection and its copy-key handler write only to Robolectric's
 * simulated clipboard. No production activity, presenter, repository, backend or network is used.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
@OptIn(ExperimentalTestApi::class)
class PagedStatisticsTitleSelectionTest {
    @get:Rule val compose = createComposeRule()

    private val clipboard: ClipboardManager
        get() = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    @After fun clearSyntheticClipboard() { clipboard.clearPrimaryClip() }

    @Test
    @Config(shadows = [StatisticsNoSurfaceMagnifierShadow::class])
    fun domainHeadingSupportsNativeSelectionAndCopy() = assertHeadingCopies("example.test")

    @Test
    @Config(shadows = [StatisticsNoSurfaceMagnifierShadow::class])
    fun ipHeadingSupportsNativeSelectionAndCopy() = assertHeadingCopies("192.0.2.1")

    @Test
    @Config(shadows = [StatisticsNoSurfaceMagnifierShadow::class])
    fun asnHeadingSupportsNativeSelectionAndCopy() = assertHeadingCopies("AS64500")

    @Test
    @Config(shadows = [StatisticsNoSurfaceMagnifierShadow::class])
    fun blocklistHeadingCopiesWithoutSubtitleChipsOrRows() = assertHeadingCopies("Synthetic blocklist")

    @Test
    @Config(shadows = [StatisticsNoSurfaceMagnifierShadow::class])
    fun copyingUpdatedHeadingAfterEmptyTransitionUsesCurrentText() {
        var title by mutableStateOf("before.test")
        var empty by mutableStateOf(true)
        seedClipboard()
        compose.setContent { Theme {
            PagedStatisticsScreen(title, "Synthetic range", empty, null,
                selectableTitle = true, extraCount = 1, extraRow = { Text("Synthetic row") })
        } }
        selectAndCopy("before.test")
        compose.runOnIdle {
            assertEquals("before.test", clipboardText())
            title = "after.test"
            empty = false
            seedClipboard()
        }
        compose.onNodeWithTag("statistics_empty").assertDoesNotExist()
        compose.onNodeWithText("Synthetic row").assertIsDisplayed()
        selectAndCopy("after.test")
        compose.runOnIdle { assertEquals("after.test", clipboardText()) }
    }

    @Test
    @Config(shadows = [StatisticsNoSurfaceMagnifierShadow::class])
    fun sharedScreenTitleRemainsNonSelectableUnlessCallerOptsIn() {
        seedClipboard()
        compose.setContent { Theme {
            // Deliberately omit selectableTitle, as DetailedStatisticsActivity does.
            PagedStatisticsScreen("Ordinary heading", "Synthetic range", false, null)
        } }
        selectAndCopy("Ordinary heading")
        compose.runOnIdle { assertEquals(SENTINEL, clipboardText()) }
    }

    private fun assertHeadingCopies(title: String) {
        seedClipboard()
        compose.setContent { Theme {
            PagedStatisticsScreen(title, "Synthetic range", false, null,
                chips = { Text("Synthetic chip") }, extraCount = 1,
                extraRow = { Text("Synthetic row") }, selectableTitle = true)
        } }
        selectAndCopy(title)
        compose.runOnIdle { assertEquals(title, clipboardText()) }
        compose.onNodeWithText("Synthetic range").assertIsDisplayed()
        compose.onNodeWithText("Synthetic chip").assertIsDisplayed()
        compose.onNodeWithText("Synthetic row").assertIsDisplayed()
    }

    private fun selectAndCopy(title: String) {
        val node = compose.onNodeWithText(title)
        val layouts = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val layout = layouts.single()
        val first = layout.getBoundingBox(0).center
        val last = layout.getBoundingBox(title.lastIndex)
        node.performTouchInput { down(first) }
        // Keep the pointer down while both the Android and Compose long-press timers expire.
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(600))
        compose.mainClock.advanceTimeBy(600)
        node.performTouchInput {
            advanceEventTime(600)
            moveTo(Offset(last.right - .1f, last.center.y))
            up()
        }
        compose.waitForIdle()
        // Exercise SelectionContainer's real copy handler; no test callback writes the result.
        node.performKeyInput { withKeyDown(Key.CtrlLeft) { pressKey(Key.C) } }
        compose.waitForIdle()
    }

    private fun seedClipboard() {
        clipboard.setPrimaryClip(ClipData.newPlainText("synthetic fixture", SENTINEL))
    }

    private fun clipboardText(): String? = clipboard.primaryClip?.getItemAt(0)?.text?.toString()

    @Composable private fun Theme(content: @Composable () -> Unit) {
        val base = LocalContext.current
        val themed = remember(base) { ContextThemeWrapper(base, R.style.AppThemeWhite) }
        CompositionLocalProvider(LocalContext provides themed) { RethinkTheme(content) }
    }

    private companion object { const val SENTINEL = "synthetic clipboard unchanged" }
}

/** Same method-scoped limitation as the About fixture: Robolectric has no compositor-backed
 * magnifier surface. Suppress only its disposal; selection and clipboard code stay real.
 * This does not validate device magnifier rendering or the platform's visual copy toolbar. */
@org.robolectric.annotation.Implements(android.widget.Magnifier::class, minSdk = 28)
class StatisticsNoSurfaceMagnifierShadow {
    @org.robolectric.annotation.Implementation
    fun dismiss() = Unit
}
