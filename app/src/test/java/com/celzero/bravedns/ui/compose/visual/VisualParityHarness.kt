/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.visual

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.graphics.isSpecified
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.reference.BaselineReferenceSource
import org.robolectric.RuntimeEnvironment
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertTrue

/** References are real source-derived or library-owned widgets, never production controllers. */
internal const val PINNED_BASELINE = "23cb6dd98805445174bd83b7237ad5f5d2c24ac9"
internal data class VisualCase(val name: String, val theme: Int, val width: Int, val fontScale: Float, val rtl: Boolean) {
    fun context(base: Context): ContextThemeWrapper = ContextThemeWrapper(base, theme).apply {
        applyOverrideConfiguration(Configuration(base.resources.configuration).apply {
            this.fontScale = this@VisualCase.fontScale
            setLocale(Locale.ENGLISH)
            setLayoutDirection(if (rtl) Locale("ar") else Locale.ENGLISH)
        })
    }
}

/** Pairwise risk coverage, deliberately not a claim of full-factorial theme/configuration coverage. */
internal val visualCases = listOf(
    VisualCase("light_narrow_ltr_f1", R.style.AppThemeWhite, 360, 1f, false),
    VisualCase("light_wide_rtl_f2", R.style.AppThemeWhite, 800, 2f, true),
    VisualCase("dark_wide_ltr_f1", R.style.AppTheme, 800, 1f, false),
    VisualCase("dark_narrow_rtl_f2", R.style.AppTheme, 360, 2f, true),
    VisualCase("frost_narrow_rtl_f1", R.style.AppThemeTrueBlackFrost, 360, 1f, true),
    VisualCase("frost_wide_ltr_f2", R.style.AppThemeTrueBlackFrost, 800, 2f, false),
    VisualCase("plus_light_wide_rtl_f1", R.style.AppThemeWhitePlus, 800, 1f, true),
    VisualCase("plus_dark_narrow_ltr_f2", R.style.AppThemeTrueBlackPlus, 360, 2f, false),
)

internal fun Context.color(attribute: Int): Int = obtainStyledAttributes(intArrayOf(attribute)).let {
    try { check(it.hasValue(0)); it.getColor(0, 0) } finally { it.recycle() }
}
internal fun descendants(view: View): List<View> = listOf(view) +
    if (view is ViewGroup) (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) } else emptyList()
internal fun View.textView(text: String): TextView = descendants(this).filterIsInstance<TextView>()
    .first { it.text.toString() == text }
internal fun attachBaselineOffscreen(activity: Activity, view: View, width: Int, height: Int) {
    // CollapsingToolbarLayout's real title painter requires an attached toolbar dummy view.
    // This test-only reference host is physically outside the window and cannot obscure Compose.
    val holder = FrameLayout(activity).apply { tag = "visual.baseline.holder" }
    activity.findViewById<ViewGroup>(android.R.id.content).addView(holder,
        FrameLayout.LayoutParams(width, height).apply { leftMargin = 10000; topMargin = 10000 })
    holder.addView(view)
}
internal fun detachBaseline(view: View) {
    val holder = view.parent as? ViewGroup ?: return
    (holder.parent as? ViewGroup)?.removeView(holder)
}
internal fun layoutBaseline(view: View, width: Int, height: Int?) {
    view.layoutDirection = view.context.resources.configuration.layoutDirection
    view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(height ?: 0, if (height == null) View.MeasureSpec.UNSPECIFIED else View.MeasureSpec.EXACTLY))
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
}
internal fun captureBaseline(view: View, background: Int): Bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).also {
    Canvas(it).apply { drawColor(view.context.color(android.R.attr.colorBackground)); drawColor(background); view.draw(this) }
}
internal fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
    val result = mutableListOf<TextLayoutResult>()
    performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(result)) }
    return result.single()
}

/** Cosmetic Material differences are review evidence. Lost content/actions/captures remain gates. */
internal class VisualEvidence(
    private val surface: String,
    private val scenario: VisualCase,
    private val referenceSource: BaselineReferenceSource? = null
) {
    private val directory = File(System.getProperty("rethink.visual.output")
        ?: System.getenv("RETHINK_VISUAL_OUTPUT") ?: "build/outputs/compose-visual-parity", "$surface/${scenario.name}").apply { mkdirs() }
    private val referenceContext = scenario.context(RuntimeEnvironment.getApplication())
    private val report = JSONObject().put("schemaVersion", 2).put("baselineCommit", PINNED_BASELINE).put("surface", surface)
        .put("scenario", scenario.name).put("widthDp", scenario.width).put("fontScale", scenario.fontScale)
        .put("rtl", scenario.rtl).put("baseline", "baseline-reference.png").put("native", "native-compose.png")
        .put("referenceKind", referenceSource?.kind ?: "material-library-widget")
        .put("referenceSourcePath", referenceSource?.resourcePath ?: JSONObject.NULL)
        .put("baselineSourceSha256", referenceSource?.baselineSourceSha256 ?: JSONObject.NULL)
        .put("referenceFactory", referenceSource?.factory ?: JSONObject.NULL)
        .put("referenceFactorySourceSha256", referenceSource?.factorySourceSha256 ?: JSONObject.NULL)
        .put("referenceAttributeHelperSha256", referenceSource?.attributeHelperSha256 ?: JSONObject.NULL)
        .put("theme", referenceContext.resources.getResourceName(scenario.theme))
        .put("density", referenceContext.resources.displayMetrics.density)
        .put("locale", "en").put("layoutDirection", if (scenario.rtl) "rtl" else "ltr")
        .put("androidSdk", android.os.Build.VERSION.SDK_INT)
        .put("declaredMaterialVersion", "1.14.0").put("declaredRobolectricVersion", "4.17")
        .put("referenceIsHistoricalScreenshot", false)
    private val measurements = JSONArray()
    val failures = mutableListOf<String>()
    private val differences = mutableListOf<String>()
    fun capture(baseline: Bitmap, native: SemanticsNodeInteraction) = capture(baseline, native.captureToImage().asAndroidBitmap())
    fun capture(baseline: Bitmap, native: Bitmap) {
        File(directory, "baseline-reference.png").outputStream().use { baseline.compress(Bitmap.CompressFormat.PNG, 100, it) }
        File(directory, "native-compose.png").outputStream().use { native.compress(Bitmap.CompressFormat.PNG, 100, it) }
        report.put("baselineWidthPx", baseline.width).put("baselineHeightPx", baseline.height)
            .put("nativeWidthPx", native.width).put("nativeHeightPx", native.height)
        var totalDelta = 0L; var changed = 0L
        val width = minOf(baseline.width, native.width); val height = minOf(baseline.height, native.height)
        val baselinePixels = IntArray(width * height)
        val nativePixels = IntArray(width * height)
        baseline.getPixels(baselinePixels, 0, width, 0, 0, width, height)
        native.getPixels(nativePixels, 0, width, 0, 0, width, height)
        for (i in baselinePixels.indices) {
            val a = baselinePixels[i]; val b = nativePixels[i]
            val delta = abs((a and 255) - (b and 255)) + abs((a ushr 8 and 255) - (b ushr 8 and 255)) +
                abs((a ushr 16 and 255) - (b ushr 16 and 255)) + abs((a ushr 24 and 255) - (b ushr 24 and 255))
            totalDelta += delta
            if (delta != 0) changed++
        }
        check("baseline capture contains rendered content", baselinePixels.any { it != baselinePixels.first() }, "A flat-color reference is not screenshot evidence")
        check("native capture contains rendered content", nativePixels.any { it != nativePixels.first() }, "A flat-color native image is not screenshot evidence")
        report.put("overlapPixelCount", width * height).put("changedOverlapPixels", changed)
            .put("meanAbsoluteChannelDelta", totalDelta.toDouble() / max(1, width * height * 4))
            .put("pixelComparisonIsDiagnosticOnly", true)
        flush()
    }
    fun text(label: String, baseline: TextView, native: SemanticsNodeInteraction, compareColor: Boolean = true, compareLineStep: Boolean = true) {
        val result = native.textLayout()
        val style = result.layoutInput.style
        val nativeSize = with(result.layoutInput.density) { style.fontSize.toPx() }
        val nativeLetter = when {
            style.letterSpacing.isEm -> style.letterSpacing.value * nativeSize
            style.letterSpacing.isSp -> with(result.layoutInput.density) { style.letterSpacing.toPx() }
            else -> 0f
        }
        val oldLayout = requireNotNull(baseline.layout)
        val baselineStep = if (oldLayout.lineCount > 1) oldLayout.getLineBaseline(1) - oldLayout.getLineBaseline(0) else null
        val nativeStep = if (result.lineCount > 1) result.getLineBaseline(1) - result.getLineBaseline(0) else null
        val bounds = native.fetchSemanticsNode().boundsInRoot
        var baselineX = baseline.left; var baselineY = baseline.top
        var ancestor = baseline.parent as? View
        while (ancestor != null && ancestor.tag != "visual.baseline.holder") {
            baselineX += ancestor.left - ancestor.scrollX
            baselineY += ancestor.top - ancestor.scrollY
            ancestor = ancestor.parent as? View
        }
        measurements.put(JSONObject().put("label", label).put("text", baseline.text.toString())
            .put("baselineSizePx", baseline.textSize).put("nativeSizePx", nativeSize)
            .put("baselineLetterSpacingPx", baseline.letterSpacing * baseline.textSize).put("nativeLetterSpacingPx", nativeLetter)
            .put("baselineColor", baseline.currentTextColor).put("nativeColor", if (style.color.isSpecified) style.color.toArgb() else JSONObject.NULL)
            .put("baselineTypefaceWeight", baseline.typeface.weight).put("nativeFontWeight", style.fontWeight?.weight)
            .put("baselineLineCount", oldLayout.lineCount).put("nativeLineCount", result.lineCount)
            .put("baselineLineStepPx", baselineStep).put("nativeLineStepPx", nativeStep)
            .put("baselineBoundsInContent", "$baselineX,$baselineY,${baselineX + baseline.width},${baselineY + baseline.height}")
            .put("baselineFirstLineWidthPx", oldLayout.getLineWidth(0))
            .put("nativeFirstLineWidthPx", result.getLineRight(0) - result.getLineLeft(0))
            .put("baselineContentWidthPx", baseline.width - baseline.compoundPaddingLeft - baseline.compoundPaddingRight)
            .put("nativeTextWidthPx", result.size.width).put("baselineHeightPx", baseline.height).put("nativeTextHeightPx", result.size.height)
            .put("nativeBoundsInRoot", bounds.toString()).put("nativeVisualOverflow", result.hasVisualOverflow))
        close(label, "font size", baseline.textSize, nativeSize, .1f)
        if (style.fontWeight != null && baseline.typeface.weight != style.fontWeight!!.weight) {
            differences += "$label font weight: reference=${baseline.typeface.weight}, Compose=${style.fontWeight!!.weight}"
        }
        close(label, "letter spacing", baseline.letterSpacing * baseline.textSize, nativeLetter, .05f)
        if (compareColor && style.color.isSpecified && baseline.currentTextColor != style.color.toArgb()) {
            differences += "$label color: reference=${baseline.currentTextColor}, Compose=${style.color.toArgb()}"
        }
        check("$label retains literal content", result.layoutInput.text.text == baseline.text.toString(), "No content may be dropped for cosmetic parity")
        if (baseline.ellipsize == null) {
            check("$label is not ellipsized", (0 until result.lineCount).none { result.isLineEllipsized(it) }, "No ellipsis where the reference exposes full text")
        }
        // Android TextView rounds baseline positions to px. A 1 px tolerance is only for rounding.
        if (compareLineStep && baselineStep != null && nativeStep != null) close(label, "line step", baselineStep.toFloat(), nativeStep, 1f)
        flush()
    }
    fun contrast(label: String, native: SemanticsNodeInteraction, baselineColor: Int, background: Int, backing: Int) {
        val color = native.textLayout().layoutInput.style.color
        check("$label explicit enabled text color", color.isSpecified, "Contrast requires a resolved foreground color")
        if (!color.isSpecified) return
        val opaqueBackground = androidx.core.graphics.ColorUtils.compositeColors(background, backing)
        val nativeForeground = androidx.core.graphics.ColorUtils.compositeColors(color.toArgb(), opaqueBackground)
        val baselineForeground = androidx.core.graphics.ColorUtils.compositeColors(baselineColor, opaqueBackground)
        val ratio = androidx.core.graphics.ColorUtils.calculateContrast(nativeForeground, opaqueBackground)
        val baselineRatio = androidx.core.graphics.ColorUtils.calculateContrast(baselineForeground, opaqueBackground)
        measurements.put(JSONObject().put("contrastLabel", label).put("nativeContrastRatio", ratio)
            .put("baselineContrastRatioAgainstNativeSurface", baselineRatio).put("minimumRatio", 4.5)
            .put("compositedSurfaceArgb", opaqueBackground).put("nativeForegroundArgb", nativeForeground)
            .put("scope", "Enabled synthetic button text against its known surface; no disabled alpha or wallpaper backdrop"))
        check("$label contrast", ratio >= 4.5, "native=$ratio:1; baseline foreground=$baselineRatio:1; required normal-label contrast=4.5:1")
    }
    fun check(label: String, condition: Boolean, details: String) {
        measurements.put(JSONObject().put("check", label).put("passed", condition).put("details", details))
        if (!condition) failures += "$label: $details"
        flush()
    }
    fun review(label: String, condition: Boolean, details: String) {
        measurements.put(JSONObject().put("review", label).put("matchesBaseline", condition).put("details", details))
        if (!condition) differences += "$label: $details"
        flush()
    }
    private fun close(label: String, property: String, baseline: Float, native: Float, tolerance: Float) {
        if (abs(baseline - native) > tolerance) differences += "$label $property: reference=$baseline, Compose=$native (tolerance=$tolerance)"
    }
    fun finish(): List<String> { flush(); return failures.map { "$surface/${scenario.name}: $it" } }
    private fun flush() {
        report.put("measurements", measurements).put("parityMismatches", JSONArray(differences))
            .put("requiredFailures", JSONArray(failures))
            .put("acceptance", "Reference metrics and cosmetic differences are recorded; capture validity, content and accessibility checks remain required. Original-style acceptance requires the separate styling gates.")
        File(directory, "measurements.json").writeText(report.toString(2))
    }
}
