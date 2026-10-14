/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.home

import android.app.Application
import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Pure native geometry registry. No Activity, Fragment, DI, managers or services. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class HomeTourBoundsTest {
    @Test fun reportsInitialAndChangedBoundsButSkipsUnchangedGeometry() {
        val anchors = HomeTourAnchors()
        val owner = Any()
        val heights = mutableListOf<Float?>()
        val stop = anchors.observeBounds(7) { heights += it?.height() }
        anchors.put(7, owner, RectF(0f, 700f, 360f, 800f), { true }, {})
        anchors.put(7, owner, RectF(0f, 700f, 360f, 800f), { true }, {})
        anchors.put(7, owner, RectF(0f, 680f, 360f, 800f), { true }, {})
        assertEquals(listOf(null, 100f, 120f), heights)
        stop()
        anchors.remove(7, owner)
        assertEquals(listOf(null, 100f, 120f), heights)
    }

    @Test fun staleOwnerCannotClearNewBoundsAndRemovalReportsAbsence() {
        val anchors = HomeTourAnchors()
        val oldOwner = Any()
        val newOwner = Any()
        anchors.put(7, oldOwner, RectF(0f, 700f, 360f, 800f), { true }, {})
        val heights = mutableListOf<Float?>()
        val stop = anchors.observeBounds(7) { heights += it?.height() }
        anchors.put(7, newOwner, RectF(0f, 680f, 360f, 800f), { true }, {})
        anchors.remove(7, oldOwner)
        assertEquals(120f, checkNotNull(anchors.find(7)).height(), 0f)
        anchors.remove(7, newOwner)
        assertNull(anchors.find(7))
        assertEquals(listOf(100f, 120f, null), heights)
        stop()
    }
}
