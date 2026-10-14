/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.statistics

import android.app.Application
import android.content.Context
import android.os.Parcelable
import android.util.SparseArray
import android.view.ContextThemeWrapper
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.reference.BaselineStatisticsFilterReference
import com.celzero.bravedns.ui.bottomsheet.SummaryStatsFilterBottomSheet.SectionFilter as Filter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Reference widgets only, constructed offscreen with plain Application. No app host or service. */
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class StatisticsMaterialReferenceTest {
    private val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext<Context>(), R.style.AppThemeWhite)
    private fun group(seed: Set<Filter>, callbacks: MutableList<Set<Filter>>): ChipGroup {
        val group = BaselineStatisticsFilterReference.create(context).group
        group.findViewById<Chip>(Filter.PROVIDERS.chipId).visibility = View.GONE
        Filter.entries.forEach { group.findViewById<Chip>(it.chipId).isChecked = it in seed }
        group.setOnCheckedStateChangeListener { _, ids -> callbacks += Filter.entries.filter { it.chipId in ids }.toSet() }
        return group
    }
    @Test fun realSixChipHierarchyRestorationMatchesNativeReplayIncludingHiddenProvider() {
        val targets = listOf(setOf(Filter.APPS, Filter.COUNTRIES, Filter.PROVIDERS), setOf(Filter.IPS), setOf(Filter.PROVIDERS), Filter.entries.toSet())
        val seeds = listOf(setOf(Filter.APPS), setOf(Filter.IPS), Filter.entries.toSet())
        for (target in targets) for (seed in seeds) {
            val saved = SparseArray<Parcelable>()
            group(target, mutableListOf()).saveHierarchyState(saved)
            val actual = mutableListOf<Set<Filter>>()
            val restored = group(seed, actual)
            restored.restoreHierarchyState(saved)
            val nativeEvents = mutableListOf<Set<Filter>>()
            val native = SummaryFilterSelection(seed) { nativeEvents += it }
            native.restore(target)
            assertEquals("checks $seed/$target", restored.checkedChipIds.toSet(), native.selected.map { it.chipId }.toSet())
            assertEquals("callbacks $seed/$target", actual, nativeEvents)
        }
    }
    @Test fun actualSelectionRequiredLastUncheckIsSilentAndSelectAllIncludesHiddenChild() {
        val actual = mutableListOf<Set<Filter>>()
        val reference = group(setOf(Filter.IPS), actual)
        reference.findViewById<Chip>(Filter.IPS.chipId).performClick()
        assertTrue(actual.isEmpty())
        val nativeEvents = mutableListOf<Set<Filter>>()
        val native = SummaryFilterSelection(setOf(Filter.IPS)) { nativeEvents += it }
        Filter.entries.forEach { reference.check(it.chipId) }
        native.selectAll()
        assertEquals(actual, nativeEvents)
        assertTrue(Filter.PROVIDERS in native.selected)
    }
    @Test fun actualBlocklistListenerRechecksLastTagAndRefetchesExactlyOnce() {
        val group = ChipGroup(context)
        val chip = Chip(context).apply { id = View.generateViewId(); isCheckable = true; isChecked = true }
        group.addView(chip)
        var lastCheckedId = group.checkedChipIds.lastOrNull()
        val events = mutableListOf<List<Int>>()
        group.setOnCheckedStateChangeListener { owner, checkedIds ->
            if (checkedIds.isEmpty()) {
                lastCheckedId?.let { owner.check(it) }
                return@setOnCheckedStateChangeListener
            }
            lastCheckedId = checkedIds.last()
            events += checkedIds
        }
        chip.performClick()
        assertEquals(listOf(listOf(chip.id)), events)
        assertTrue(chip.isChecked)
        assertEquals(setOf("fake"), selectStatisticsTags(listOf("fake"), setOf("fake"), "fake"))
    }
}
