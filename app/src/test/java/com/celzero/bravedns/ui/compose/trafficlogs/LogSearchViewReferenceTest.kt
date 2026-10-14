/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.trafficlogs

import android.app.Application
import android.view.ContextThemeWrapper
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Harmless AppCompat widget references only. No Activity, Fragment, application startup or data. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class LogSearchViewReferenceTest {
    @Test fun alreadyVisibleEditorFocusAndClickPreserveRoutedQueryWithoutRootCallback() {
        val reference = referenceSearch()
        val editor = reference.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        val changes = mutableListOf<String>()
        var rootClicks = 0
        reference.setQuery("routed.test", false)
        listen(reference, changes)
        reference.setOnClickListener {
            rootClicks++
            reference.requestFocus()
            reference.onActionViewExpanded()
        }

        reference.clearFocus()
        assertTrue(editor.requestFocus())
        editor.performClick()
        assertTrue(editor.hasFocus())
        assertEquals("routed.test", reference.query.toString())
        assertEquals(0, rootClicks)
        assertTrue(changes.isEmpty())

        editor.append(".edited")
        assertEquals("routed.test.edited", reference.query.toString())
        assertEquals(listOf("routed.test.edited"), changes)
        assertEquals(0, rootClicks)
    }

    @Test fun refocusingAlreadyVisibleEditorPreservesRestoredQuery() {
        val reference = referenceSearch()
        val editor = reference.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        val changes = mutableListOf<String>()
        var rootClicks = 0
        reference.setQuery("routed.test", false)
        listen(reference, changes)
        reference.setQuery("restored.test", false)
        reference.setOnClickListener {
            rootClicks++
            reference.requestFocus()
            reference.onActionViewExpanded()
        }

        assertTrue(editor.requestFocus())
        reference.clearFocus()
        assertTrue(editor.requestFocus())
        editor.performClick()
        assertEquals("restored.test", reference.query.toString())
        assertEquals(listOf("restored.test"), changes)
        assertEquals(0, rootClicks)
    }

    @Test fun rootClickRunsActionExpansionAndClearsOnlyTheFirstTime() {
        val reference = referenceSearch()
        val changes = mutableListOf<String>()
        var rootClicks = 0
        reference.setQuery("routed.test", false)
        listen(reference, changes)
        reference.setOnClickListener {
            rootClicks++
            reference.requestFocus()
            reference.onActionViewExpanded()
        }

        reference.performClick()
        assertEquals("", reference.query.toString())
        assertEquals(1, rootClicks)
        assertEquals(listOf(""), changes)

        reference.setQuery("typed.test", false)
        reference.performClick()
        assertEquals("typed.test", reference.query.toString())
        assertEquals(2, rootClicks)
        assertEquals(listOf("", "typed.test"), changes)
    }

    private fun referenceSearch(): SearchView {
        val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), R.style.AppThemeWhite)
        // Baseline 23cb6dd: fragment_dns_logs.xml and full/fragment_connection_tracker.xml
        // both set iconifiedByDefault=false. The pinned dependency is AppCompat 1.8.0.
        return SearchView(context).apply {
            setIconifiedByDefault(false)
            assertFalse(isIconfiedByDefault)
            assertFalse(isIconified)
            assertEquals(View.GONE, findViewById<View>(androidx.appcompat.R.id.search_button).visibility)
            assertEquals(View.VISIBLE, findViewById<View>(androidx.appcompat.R.id.search_src_text).visibility)
        }
    }

    private fun listen(reference: SearchView, changes: MutableList<String>) {
        reference.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean { changes += newText; return true }
            override fun onQueryTextSubmit(query: String): Boolean = true
        })
    }
}
