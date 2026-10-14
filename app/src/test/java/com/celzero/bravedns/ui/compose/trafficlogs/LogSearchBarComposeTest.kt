/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.trafficlogs

import android.app.Application
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextRange
import androidx.test.platform.app.InstrumentationRegistry
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.rules.ExternalResource
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Native presentation only, plain Application and synthetic text. No application host or backend. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class LogSearchBarComposeTest {
    // Compose's injected touch events bypass ViewRoot's touch-mode transition. Establish
    // touch mode before the generic test Activity attaches, otherwise Android clearFocus()
    // immediately assigns focus to the first eligible child again (keyboard-mode behavior).
    @get:Rule(order = 0) val touchMode = object : ExternalResource() {
        override fun before() { InstrumentationRegistry.getInstrumentation().setInTouchMode(true) }
    }
    @get:Rule(order = 1) val compose = createComposeRule()

    @Test fun editorTapPreservesRoutedQueryAndTypingDoesNotExpandSearch() {
        val state = LogScreenState().apply { search.setQuery("routed.test") }
        val changes = mutableListOf<String>()
        var rootClicks = 0
        state.search.listener = { changes += it }
        show(state) { rootClicks++; state.search.expand() }

        val editor = compose.onNodeWithTag("log-search")
        editor.assertIsNotFocused().performTouchInput { click() }
        editor.assertIsFocused().assertTextEquals("routed.test")
        compose.runOnIdle { assertEquals(0, rootClicks); assertTrue(changes.isEmpty()) }

        editor.performTextInputSelection(TextRange("routed.test".length))
        editor.performTextInput(".edited")
        editor.assertTextEquals("routed.test.edited")
        compose.runOnIdle {
            assertEquals(0, rootClicks)
            assertEquals(listOf("routed.test.edited"), changes)
        }
    }

    @Test fun editorRefocusAfterResumePreservesRestoredAndTypedQueries() {
        val state = LogScreenState().apply {
            search.setQuery("routed.test")
            search.setQuery("restored.test")
        }
        val changes = mutableListOf<String>()
        var rootClicks = 0
        state.search.listener = { changes += it }
        show(state) { rootClicks++; state.search.expand() }

        val editor = compose.onNodeWithTag("log-search")
        editor.performTouchInput { click() }
        editor.assertIsFocused().assertTextEquals("restored.test")
        editor.performTextInputSelection(TextRange("restored.test".length))
        editor.performTextInput(".typed")

        compose.runOnIdle { state.resumeGeneration++ }
        editor.assertIsNotFocused().assertTextEquals("restored.test.typed")
        editor.performTouchInput { click() }
        editor.assertIsFocused().assertTextEquals("restored.test.typed")
        compose.runOnIdle {
            assertEquals(0, rootClicks)
            assertEquals(listOf("restored.test.typed"), changes)
        }
    }

    @Test fun typingIntoEmptyEditorDoesNotDispatchRootSearchCallback() {
        val state = LogScreenState()
        val changes = mutableListOf<String>()
        var rootClicks = 0
        state.search.listener = { changes += it }
        show(state) { rootClicks++; state.search.expand() }

        val editor = compose.onNodeWithTag("log-search")
        editor.performTouchInput { click() }
        editor.performTextInput("typed.test")
        editor.assertTextEquals("typed.test")
        compose.runOnIdle {
            assertEquals(0, rootClicks)
            assertEquals(listOf("typed.test"), changes)
        }
    }

    @Test fun explicitSearchAffordanceExpandsOnceAndFocusesEditor() {
        val state = LogScreenState().apply { search.setQuery("routed.test") }
        val changes = mutableListOf<String>()
        var rootClicks = 0
        state.search.listener = { changes += it }
        show(state) { rootClicks++; state.search.expand() }

        val editor = compose.onNodeWithTag("log-search")
        compose.onNodeWithTag("log-search-open").performTouchInput { click() }
        editor.assertIsFocused()
        compose.runOnIdle {
            assertEquals("", state.search.query)
            assertEquals(1, rootClicks)
            assertEquals(listOf(""), changes)
        }

        editor.performTextInput("typed.test")
        compose.onNodeWithTag("log-search-open").performTouchInput { click() }
        editor.assertIsFocused().assertTextEquals("typed.test")
        compose.runOnIdle {
            assertEquals(2, rootClicks)
            assertEquals(listOf("", "typed.test"), changes)
        }
    }

    private fun show(state: LogScreenState, onRootSearchClick: () -> Unit) {
        compose.setContent {
            Theme { LogSearchBar(state, "Search synthetic logs", onRootSearchClick, actions = {}) }
        }
    }

    @Composable private fun Theme(content: @Composable () -> Unit) {
        val base = LocalContext.current
        val themed = remember(base) { ContextThemeWrapper(base, R.style.AppThemeWhite) }
        CompositionLocalProvider(LocalContext provides themed) { RethinkTheme(content) }
    }
}
