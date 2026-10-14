/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.resources

import android.app.Application
import android.content.res.TypedArray
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.google.android.material.R as MaterialR
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Authored / UNRUN. Plain Application and resources only; no app controllers or network. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class OrderedStyledAttributesTest {
    @Test fun orderIsAscendingAndOriginalIndicesIncludingDuplicatesAreRetained() {
        val input = intArrayOf(0x7f040100, 0x01010098, 0x7f040002, 0x01010098)
        val original = input.copyOf()
        val order = AttributeOrder(input)
        assertArrayEquals(intArrayOf(0x01010098, 0x7f040002, 0x7f040100), order.sortedIds)
        input.indices.forEach { assertEquals(input[it], order.sortedIds[order.index(it)]) }
        assertArrayEquals(original, input)
        assertEquals(0, AttributeOrder(intArrayOf()).sortedIds.size)
    }

    @Test fun allQueryOverloadsMatchIndependentSingleAttributePlatformQueries() {
        val ids = intArrayOf(MaterialR.attr.icon, android.R.attr.minHeight, android.R.attr.textColor,
            MaterialR.attr.cornerRadius, android.R.attr.minWidth, MaterialR.attr.iconSize,
            android.R.attr.textAppearance, android.R.attr.minHeight)
        val themes = listOf(R.style.AppTheme, R.style.AppThemeWhite, R.style.AppThemeTrueBlack,
            R.style.AppThemeWhitePlus, R.style.AppThemeTrueBlackPlus, R.style.AppThemeTrueBlackFrost)
        val style = R.style.OriginalControl_fragment_summary_statistics_fss_view_mode_list_btn
        themes.forEach { theme ->
            val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), theme)
            fun compare(actual: OrderedStyledAttributes, oracle: (Int) -> TypedArray) {
                try {
                    ids.forEachIndexed { index, id ->
                        val expected = oracle(id) // one ID is intrinsically sorted; independent of our mapping
                        try {
                            assertEquals(expected.hasValue(0), actual.hasValue(index))
                            assertEquals(expected.hasValueOrEmpty(0), actual.hasValueOrEmpty(index))
                            assertEquals(snapshot(expected.peekValue(0)), snapshot(actual.peekValue(index)))
                            val expectedValue = TypedValue()
                            val actualValue = TypedValue()
                            val found = expected.getValue(0, expectedValue)
                            assertEquals("getValue presence at declared index $index", found, actual.getValue(index, actualValue))
                            if (found) assertEquals(snapshot(expectedValue), snapshot(actualValue))
                        } finally { expected.recycle() }
                    }
                } finally { actual.recycle() }
            }
            compare(context.obtainOrderedStyledAttributes(ids)) { context.obtainStyledAttributes(intArrayOf(it)) }
            compare(context.obtainOrderedStyledAttributes(style, ids)) { context.obtainStyledAttributes(style, intArrayOf(it)) }
            compare(context.obtainOrderedStyledAttributes(null, ids, MaterialR.attr.materialButtonStyle,
                MaterialR.style.Widget_MaterialComponents_Button)) {
                context.obtainStyledAttributes(null, intArrayOf(it), MaterialR.attr.materialButtonStyle,
                    MaterialR.style.Widget_MaterialComponents_Button)
            }
        }
    }

    private fun snapshot(value: TypedValue?): List<Any?>? = value?.let {
        listOf(it.type, it.data, it.resourceId, it.string?.toString())
    }
}
