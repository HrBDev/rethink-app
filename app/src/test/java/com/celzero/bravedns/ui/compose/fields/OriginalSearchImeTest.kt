/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.fields

import android.app.Application
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Authored, UNRUN. Does not open an IME/session, invoke a service or inspect real input. */
@OptIn(ExperimentalComposeUiApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class OriginalSearchImeTest {
    @Test fun editorFlagsMatchNativeSearchWhileConnectionAndOtherMetadataAreUnchanged() {
        val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), R.style.AppThemeWhite)
        val native = SearchView(context).findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        val connection = BaseInputConnection(View(context), true)
        var calls = 0
        val request = PlatformTextInputMethodRequest { info ->
            calls++
            info.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            info.imeOptions = EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI
            info.initialSelStart = 2; info.initialSelEnd = 5
            info.privateImeOptions = "synthetic-only"
            connection
        }
        val info = EditorInfo()
        assertSame(connection, originalSearchInputRequest(request).createInputConnection(info))
        val mask = InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        assertEquals(native.inputType and mask, info.inputType and mask)
        assertEquals(mask, info.inputType and mask)
        assertEquals(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, info.inputType and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        assertEquals(EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI, info.imeOptions)
        assertEquals(2, info.initialSelStart); assertEquals(5, info.initialSelEnd)
        assertEquals("synthetic-only", info.privateImeOptions); assertEquals(1, calls)
    }

    @Test fun numberAndPasswordInputTypesAreNeverRewritten() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val connection = BaseInputConnection(View(context), true)
        listOf(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD).forEach { type ->
            val request = PlatformTextInputMethodRequest { info -> info.inputType = type; connection }
            val info = EditorInfo()
            assertSame(connection, originalSearchInputRequest(request).createInputConnection(info))
            assertEquals(type, info.inputType)
        }
    }
}
