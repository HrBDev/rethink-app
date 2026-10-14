package com.celzero.bravedns.ui.compose.wireguard

import android.app.Application
import android.content.Context
import android.util.SparseArray
import android.os.Parcelable
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.SearchView
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Verifies the editable child state that the native query-only Bundle replaces. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class WgLegacySearchParityTest {
    @Test fun expandedSearchRestoresItsQueryAndDispatchesTextChange() {
        val context: Context = ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), R.style.AppThemeWhite)
        val original = SearchView(context).apply { id = 40001; isIconifiedByDefault = false; setQuery("synthetic", false) }
        val hierarchy = SparseArray<Parcelable>()
        original.saveHierarchyState(hierarchy)
        val changes = mutableListOf<String>()
        val restored = SearchView(context).apply {
            id = 40001; isIconifiedByDefault = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = true
                override fun onQueryTextChange(newText: String): Boolean { changes.add(newText); return true }
            })
        }
        restored.restoreHierarchyState(hierarchy)
        assertEquals("synthetic", restored.query.toString())
        assertTrue(changes.contains("synthetic"))
    }
}
