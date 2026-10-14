/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.visual

import android.app.Application
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import kotlin.math.roundToInt
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.wireguard.WgAlertDialogBuilder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Only a ComponentActivity, an offscreen Material XML dialog and synthetic Compose dialog. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w800dp-h1000dp-mdpi")
class WgAlertVisualParityTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun defaultConfirmationCapturesAndMeasuresTheActualSharedBuilder() = compare(0, "wg-alert-default")
    @Test fun noDimConfirmationCapturesAndMeasuresTheActualSharedBuilder() = compare(R.style.App_Dialog_NoDim, "wg-alert-no-dim")

    private fun compare(dialogTheme: Int, surface: String) {
        val failures = mutableListOf<String>()
        var callbacks = 0
        val title = "Synthetic confirmation"
        val message = "Synthetic information only.\nNo network or account operation occurs.\nThis line measures paragraph spacing."
        visualCases.forEach { scenario ->
            val context = scenario.context(compose.activity)
            lateinit var native: Dialog
            lateinit var reference: androidx.appcompat.app.AlertDialog
            compose.runOnUiThread {
                reference = MaterialAlertDialogBuilder(context, dialogTheme).setTitle(title).setMessage(message)
                    .setPositiveButton("Continue") { _, _ -> callbacks++ }
                    .setNegativeButton("Cancel") { _, _ -> callbacks++ }.create()
                reference.create() // Inflate detached platform XML; never show the reference window.
                native = WgAlertDialogBuilder(context, dialogTheme).setTitle(title).setMessage(message)
                    .setPositiveButton("Continue") { _, _ -> callbacks++ }
                    .setNegativeButton("Cancel") { _, _ -> callbacks++ }.create()
                native.window!!.decorView.layoutDirection = if (scenario.rtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
                native.show()
                // Standardized content width isolates the content from platform dialog width policy.
                native.window!!.setLayout(minOf(scenario.width, 600), ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            val baseline = reference.findViewById<View>(androidx.appcompat.R.id.parentPanel)!!
            try {
                compose.waitForIdle()
                val root = compose.onRoot()
                val nativeBounds = root.fetchSemanticsNode().boundsInRoot
                compose.runOnUiThread {
                    (baseline.parent as ViewGroup).removeView(baseline)
                    attachBaselineOffscreen(compose.activity, baseline, nativeBounds.width.toInt(), nativeBounds.height.toInt())
                    layoutBaseline(baseline, nativeBounds.width.toInt(), nativeBounds.height.toInt())
                }
                compose.waitForIdle()
                compose.runOnUiThread { baseline.jumpDrawablesToCurrentState() }
                val evidence = VisualEvidence(surface, scenario)
                val windowBounds = root.fetchSemanticsNode().boundsInWindow
                val nativeBitmap = Bitmap.createBitmap(nativeBounds.width.toInt(), nativeBounds.height.toInt(), Bitmap.Config.ARGB_8888)
                var pixelCopyResult = PixelCopy.ERROR_UNKNOWN
                compose.runOnUiThread {
                    // captureToImage cannot identify a platform Dialog holding a plain ComposeView:
                    // it falls back to the Activity window. Capture this exact Window instead.
                    PixelCopy.request(native.window!!, Rect(windowBounds.left.roundToInt(), windowBounds.top.roundToInt(),
                        windowBounds.right.roundToInt(), windowBounds.bottom.roundToInt()), nativeBitmap,
                        { pixelCopyResult = it }, Handler(Looper.getMainLooper()))
                }
                compose.waitForIdle()
                assertEquals("Actual Dialog window PixelCopy", PixelCopy.SUCCESS, pixelCopyResult)
                evidence.capture(captureBaseline(baseline, reference.context.color(R.attr.colorSurfaceContainerHigh)), nativeBitmap)
                evidence.text("title", baseline.findViewById(androidx.appcompat.R.id.alertTitle)!!, text(title))
                evidence.text("message", baseline.findViewById(android.R.id.message)!!, text(message))
                listOf(DialogInterface.BUTTON_NEGATIVE to "Cancel", DialogInterface.BUTTON_POSITIVE to "Continue").forEach { (id, label) ->
                    evidence.text("button $label", reference.getButton(id), text(label))
                    evidence.contrast("button $label", text(label), reference.getButton(id).currentTextColor,
                        native.context.color(R.attr.colorSurfaceContainerHigh), native.context.color(android.R.attr.colorBackground))
                    val button = compose.onNodeWithText(label)
                    button.assertIsDisplayed().assertHasClickAction().assertIsEnabled()
                    evidence.check("$label touch target", button.fetchSemanticsNode().touchBoundsInRoot.height >= 48f, "minimum 48 dp")
                }
                evidence.check("synthetic callbacks remain silent", callbacks == 0, "callbacks=$callbacks")
                if (dialogTheme == R.style.App_Dialog_NoDim) {
                    evidence.check("no dim amount", native.window!!.attributes.dimAmount == 0f,
                        "dimAmount=${native.window!!.attributes.dimAmount}")
                }
                failures += evidence.finish()
            } finally {
                compose.runOnUiThread { detachBaseline(baseline); native.dismiss(); reference.dismiss() }
                compose.waitForIdle()
            }
        }
        assertEquals(0, callbacks)
        assertTrue("Required shared-dialog rendering/content/accessibility failures (cosmetic differences recorded separately):\n" + failures.joinToString("\n"), failures.isEmpty())
    }
    private fun text(value: String): SemanticsNodeInteraction = compose.onAllNodes(
        hasText(value) and SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult), useUnmergedTree = true)[0]
}
