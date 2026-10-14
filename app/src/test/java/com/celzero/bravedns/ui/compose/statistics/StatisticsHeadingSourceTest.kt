/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.statistics

import android.app.Application
import android.view.ContextThemeWrapper
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Synthetic source-style regression, authored but uncompiled/unrun. No presenter, Activity,
 * production Application, service, storage, account, or traffic is constructed. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class StatisticsHeadingSourceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun detailHeaderKeepsSourceTitleAndSubtitleSizesAndEmptyMessage() {
        compose.setContent {
            val context = ContextThemeWrapper(LocalContext.current, R.style.AppThemeWhite)
            CompositionLocalProvider(LocalContext provides context) {
                RethinkTheme { PagedStatisticsScreen("Synthetic title", "Synthetic subtitle", true, null) }
            }
        }
        assertEquals(16f, textStyle("Synthetic title").layoutInput.style.fontSize.value, 0f)
        assertEquals(12f, textStyle("Synthetic subtitle").layoutInput.style.fontSize.value, 0f)
        compose.onNodeWithTag("statistics_empty").assertExists()
        compose.onNodeWithTag("statistics_list").assertDoesNotExist()
    }

    private fun textStyle(text: String): TextLayoutResult {
        val values = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(text).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(values)) }
        return values.single()
    }
}
