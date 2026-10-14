/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.home

import android.app.Application
import android.view.ContextThemeWrapper
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.custom.AppHistogramView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class HomePresentationTest {
    private fun context() = ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), R.style.AppThemeWhite)
    @Test fun requiredChipSelectionDispatchesOnceAndDoesNotDeselect() {
        val b = HomePresentation(context()); val page = b.logsPage
        val events = mutableListOf<List<Int>>()
        page.fhsLogsToggleGroup.setOnCheckedStateChangeListener { _, ids -> events += ids }
        page.fhsLogsToggleGroup.select(page.fhsLogsAllowedChip)
        assertTrue(events.isEmpty())
        page.fhsLogsToggleGroup.select(page.fhsLogsBlockedChip)
        page.fhsLogsToggleGroup.select(page.fhsLogsBlockedChip)
        assertEquals(listOf(listOf(R.id.fhs_logs_blocked_chip)), events)
        assertTrue(page.fhsLogsBlockedChip.isChecked); assertFalse(page.fhsLogsAllowedChip.isChecked)
    }
    @Test fun restoredHistogramChipIsSelectedBeforeListenerWithoutReload() {
        val page = HomeTopAppsPage(context()); val events = mutableListOf<List<Int>>()
        page.fhsTopAppsBlockedChip.isChecked = true
        page.fhsTopAppsToggleGroup.setOnCheckedStateChangeListener { _, ids -> events += ids }
        assertTrue(page.fhsTopAppsBlockedChip.isChecked); assertFalse(page.fhsTopAppsAllowedChip.isChecked)
        assertTrue(events.isEmpty())
    }
    @Test fun reentrantCheckedAssignmentUpdatesStateWithoutRecursiveBroadcast() {
        val node = HomeNode(context()); val events = mutableListOf<Boolean>()
        node.onChecked = { value -> events += value; node.isChecked = false }
        node.isChecked = true
        assertEquals(listOf(true), events); assertFalse(node.isChecked)
    }
    @Test fun disabledNodeCannotDispatchAndClickDoesNotToggle() {
        val node = HomeNode(context()); var calls = 0
        node.setOnClickListener { calls++ }; node.isEnabled = false
        node.performClick(); assertEquals(0, calls)
        node.isEnabled = true; node.performClick()
        assertEquals(1, calls); assertFalse(node.isChecked)
    }
    @Test fun freshPresentationResetsPerViewWarningsAndDialogs() {
        val context = context(); val old = HomePresentation(context)
        old.fhsCardAppsCv.strokeWidth = 12; old.fhsDnsOnOffBtn.isEnabled = false
        old.dialogs += HomeDialog("synthetic", "", false, emptyList()) {}
        val fresh = HomePresentation(context)
        assertEquals(0, fresh.fhsCardAppsCv.strokeWidth); assertTrue(fresh.fhsDnsOnOffBtn.isEnabled)
        assertTrue(fresh.dialogs.isEmpty()); assertFalse(fresh.borderAnimating)
    }
    @Test fun visibilityOnlyChangesPresentation() {
        val node = HomeNode(context()); node.text = "saved label"
        node.isVisible = false; assertEquals(View.GONE, node.visibility)
        node.isVisible = true; assertEquals(View.VISIBLE, node.visibility)
        assertEquals("saved label", node.text)
    }
    @Test fun histogramSubmitClearsSelectionBeforeNewTap() {
        val histogram = HomeHistogram(context(), 0)
        val entry = AppHistogramView.Entry(123, "Synthetic app", 100, 200, 3, null)
        val events = mutableListOf<AppHistogramView.Entry?>()
        histogram.onEntrySelected = { events += it }
        histogram.submit(listOf(entry), false); histogram.select(0); histogram.select(0)
        assertEquals(listOf(null, entry, null), events)
        histogram.select(0); histogram.submit(listOf(entry), true)
        assertEquals(-1, histogram.selectedIndex); assertNull(events.last()); assertTrue(histogram.blocked)
    }
    @Test fun pagerDispatchIsBoundedAndCallbackCanBeRemoved() {
        val pager = HomePager(context(), 0)
        pager.adapter = object : HomePages { override val itemCount = 2 }
        val events = mutableListOf<Int>()
        val callback = object : HomePagerCallback() { override fun onPageSelected(position: Int) { events += position } }
        pager.registerOnPageChangeCallback(callback)
        pager.setCurrentItem(1, false); pager.setCurrentItem(1, true); pager.setCurrentItem(9, true)
        assertEquals(listOf(1), events)
        pager.unregisterOnPageChangeCallback(callback); pager.setCurrentItem(0, true)
        assertEquals(listOf(1), events)
    }
    @Test fun rangeSelectionEmitsOldFalseThenNewTrueAndIgnoresSameSelection() {
        val group = IntervalRangeGroup(context()); val events = mutableListOf<Pair<Int, Boolean>>()
        group.check(R.id.bs_lai_chip_10m)
        group.addOnButtonCheckedListener { _, id, checked -> events += id to checked }
        group.check(R.id.bs_lai_chip_10m); assertTrue(events.isEmpty())
        group.check(R.id.bs_lai_chip_1h)
        assertEquals(listOf(R.id.bs_lai_chip_10m to false, R.id.bs_lai_chip_1h to true), events)
    }
    @Test fun hiddenTourOwnerIsNotAnAnchorAndStaleDisposalDoesNotRemoveReplacement() {
        val anchors = HomeTourAnchors(); val first = Any(); val second = Any(); var shown = true
        anchors.put(1, first, android.graphics.RectF(1f, 2f, 3f, 4f), { shown }) {}
        assertNotNull(anchors.find(1)); shown = false; assertNull(anchors.find(1))
        anchors.put(1, second, android.graphics.RectF(5f, 6f, 7f, 8f), { true }) {}
        anchors.remove(1, first); assertEquals(5f, anchors.find(1)!!.left)
        anchors.remove(1, second); assertNull(anchors.find(1))
    }
}
