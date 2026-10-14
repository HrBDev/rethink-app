/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.rpn

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.ui.custom.FlagWatermarkView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.lang.reflect.Modifier

/** AUTHORED / UNRUN. Plain Application, synthetic bitmap/drawable and the original drawing
 * View as an independent oracle only. No production Activity, controller, service or traffic. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "mdpi")
class RpnFlagWatermarkStateTest {
    @Test fun controllerSettersHaveOnePublicJvmSignatureEach() {
        val methods = RpnFlagWatermarkState::class.java.declaredMethods
        assertEquals(1, methods.count { it.name == "setFlagText" &&
            it.parameterTypes.contentEquals(arrayOf(String::class.java)) && Modifier.isPublic(it.modifiers) })
        assertEquals(1, methods.count { it.name == "setSpread" &&
            it.parameterTypes.contentEquals(arrayOf(java.lang.Boolean.TYPE)) && Modifier.isPublic(it.modifiers) })
        assertEquals(1, methods.count { it.name == "setFlagDrawable" && it.parameterCount == 3 })
    }

    @Test fun renamedBackingStateRetainsNativeDrawingAndResetBehavior() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        for (rtl in listOf(false, true)) {
            val state = RpnFlagWatermarkState(context)
            val native = FlagWatermarkView(context).apply {
                layoutDirection = if (rtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            }
            fun compare(width: Int = 200, height: Int = 100) {
                native.layout(0, 0, width, height)
                val expected = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val actual = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                try {
                    native.draw(Canvas(expected))
                    state.draw(Canvas(actual), width, height, rtl)
                    assertTrue("source drawing/reset parity, rtl=$rtl size=$width/$height", expected.sameAs(actual))
                } finally { expected.recycle(); actual.recycle() }
            }
            compare()
            native.setFlagDrawable(ColorDrawable(Color.RED), peakAlpha = .25f)
            state.setFlagDrawable(ColorDrawable(Color.RED), peakAlpha = .25f)
            compare()
            listOf(true, true, false).forEach { spread ->
                native.setSpread(spread); state.setSpread(spread); compare()
            }
            native.setFlagText("A"); state.setFlagText("A"); compare()
            native.setFlagText("A"); state.setFlagText("A"); compare(160, 80)
            native.setFlagText("  "); state.setFlagText("  "); compare()
            native.setFlagDrawable(ColorDrawable(Color.BLUE)); state.setFlagDrawable(ColorDrawable(Color.BLUE)); compare()
            native.clear(); state.clear(); compare()
            native.clear(); state.clear(); compare()
        }
    }
}
