/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.statistics

import com.celzero.bravedns.data.AppConnection
import com.celzero.bravedns.ui.bottomsheet.SummaryStatsFilterBottomSheet.SectionFilter as Filter
import com.celzero.bravedns.ui.compose.appdetails.appDonutSweeps
import com.celzero.bravedns.ui.fragment.SummaryStatisticsFragment.SummaryStatisticsType as Type
import org.junit.Assert.*
import org.junit.Test

/** Pure fake state. No Android host, service, repository, network, or application initialization. */
class StatisticsSelectionParityTest {
    private val order = listOf(Filter.APPS, Filter.COUNTRIES, Filter.PROVIDERS, Filter.BLOCKLISTS, Filter.DOMAINS, Filter.IPS)
    private fun subset(mask: Int) = order.filterIndexed { index, _ -> mask and (1 shl index) != 0 }.toSet()

    // Independent boolean-child oracle: XML starts every child checked. Each restored child can
    // change only its own boolean; a request to uncheck the final checked child is rejected.
    private fun replay(initial: Set<Filter>, target: Set<Filter>): Pair<Set<Filter>, List<Set<Filter>>> {
        val children = order.map { it in initial }.toMutableList()
        val events = mutableListOf<Set<Filter>>()
        for (index in children.indices) {
            val checked = order[index] in target
            if (children[index] == checked || (!checked && children.count { it } == 1)) continue
            children[index] = checked
            events += order.filterIndexed { position, _ -> children[position] }.toSet()
        }
        return order.filterIndexed { index, _ -> children[index] }.toSet() to events
    }

    @Test fun all4096SeedAndSavedStatePairsReplayEveryChildInXmlOrder() {
        for (seedMask in 0 until 64) for (savedMask in 0 until 64) {
            val events = mutableListOf<Set<Filter>>()
            val selection = SummaryFilterSelection(subset(seedMask)) { events += it }
            val seeded = replay(order.toSet(), subset(seedMask)).first
            assertEquals("initial $seedMask", seeded, selection.selected)
            assertTrue("initial seeding never notifies", events.isEmpty())
            val (restored, expectedEvents) = replay(seeded, subset(savedMask))
            selection.restore(subset(savedMask))
            assertEquals("restored $seedMask/$savedMask", restored, selection.selected)
            assertEquals("callback order $seedMask/$savedMask", expectedEvents, events)
            assertTrue(selection.selected.isNotEmpty())
        }
    }
    @Test fun selectAllPublishesEachNewCheckIncludingHiddenProviderAndStopsWhenAlreadyChecked() {
        val events = mutableListOf<Set<Filter>>()
        val selection = SummaryFilterSelection(setOf(Filter.IPS)) { events += it }
        selection.selectAll(); selection.selectAll()
        assertEquals(5, events.size)
        assertEquals(setOf(Filter.APPS, Filter.IPS), events.first())
        assertEquals(setOf(Filter.APPS, Filter.COUNTRIES, Filter.PROVIDERS, Filter.IPS), events[2])
        assertEquals(order.toSet(), events.last())
        assertEquals(order, selection.selected.toList())
    }
    @Test fun lastSelectedSummaryChipDoesNotPublishAndIndependentChipsRemainSelected() {
        val events = mutableListOf<Set<Filter>>()
        val selection = SummaryFilterSelection(setOf(Filter.APPS)) { events += it }
        selection.toggle(Filter.APPS); assertTrue(events.isEmpty())
        selection.toggle(Filter.COUNTRIES); selection.toggle(Filter.BLOCKLISTS)
        assertEquals(setOf(Filter.APPS, Filter.COUNTRIES, Filter.BLOCKLISTS), selection.selected)
        assertEquals(2, events.size)
    }
    @Test fun hiddenProviderAffectsRestorationButNotTheVisibleBadgeDenominator() {
        assertNull(statisticsFilterBadge(order.toSet() - Filter.PROVIDERS, false))
        assertEquals(5, statisticsFilterBadge(order.toSet() - Filter.PROVIDERS, true))
        assertEquals(0, statisticsFilterBadge(setOf(Filter.PROVIDERS), false))
        val selection = SummaryFilterSelection(setOf(Filter.PROVIDERS)) {}
        selection.restore(setOf(Filter.APPS))
        assertEquals(setOf(Filter.APPS), selection.selected)
    }
    @Test fun blocklistTagFallbackPreservesLastSelectionAndOriginalTagOrder() {
        val tags = listOf("b", "a", "c")
        assertEquals(setOf("a"), selectStatisticsTags(tags, setOf("a"), "a"))
        assertEquals(listOf("b", "a"), selectStatisticsTags(tags, setOf("a"), "b").toList())
        assertEquals(setOf("b"), selectStatisticsTags(tags, setOf("a", "b"), "a"))
    }
    @Test fun sectionOrderFeatureGatesAndAllElevenMetricMappingsRemainDistinct() {
        assertEquals(listOf(9, 0, 1, 5, 2, 3, 10, 4, 6, 7, 8), summarySectionOrder.map { it.tid })
        val state = SummaryStatisticsState()
        assertEquals(setOf(Type.TOP_ACTIVE_CONNS, Type.MOST_CONNECTED_APPS, Type.MOST_BLOCKED_APPS), Type.entries.filter { state.sectionAvailable(it) }.toSet())
        state.providersAvailable = true; state.dnsActive = true; state.firewallActive = true
        assertTrue(Type.entries.all { state.sectionAvailable(it) })
        state.filters = setOf(Filter.COUNTRIES)
        assertEquals(listOf(Type.MOST_CONTACTED_COUNTRIES), Type.entries.filter { state.sectionAvailable(it) })
        val item = AppConnection(10001, "192.0.2.1", 443, 7, "", false, "example.test", 100, 30)
        Type.entries.forEach { assertEquals(if (it == Type.MOST_CONNECTED_APPS) 130L else 7L, statisticsMetric(item, it)) }
        assertEquals(0L, statisticsMetric(item.copy(downloadBytes = null, uploadBytes = null), Type.MOST_CONNECTED_APPS))
    }
    @Test fun donutRenormalizesTheVisibleFiveSlicesAndHandlesZero() {
        assertEquals(listOf(180f, 90f, 90f), appDonutSweeps(listOf(.2f, .1f, .1f)))
        assertEquals(listOf(0f, 0f), appDonutSweeps(listOf(0f, 0f)))
        assertTrue(appDonutSweeps(emptyList()).isEmpty())
        assertEquals(360f, appDonutSweeps(listOf(.4f, .2f, .1f, .1f, .05f)).sum(), .001f)
    }
}
