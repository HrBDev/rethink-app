/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.reference

import android.util.AttributeSet
import org.robolectric.Robolectric
import android.view.View
import org.robolectric.util.ReflectionHelpers

/** Test-only source-property input for actual Android widget constructors.
 * No XML is stored, parsed or inflated; no production Compose resolver is used.
 * The constructor's real theme/default-style precedence remains part of the oracle.
 */
internal fun referenceAttributes(style: String? = null, vararg values: Pair<Int, String>): AttributeSet {
    val builder = Robolectric.buildAttributeSet()
    if (style != null) builder.setStyleAttribute(style)
    values.forEach { (attribute, value) -> builder.addAttribute(attribute, value) }
    return builder.build()
}

/** Match the framework's post-order completion hook without an inflater or XML.
 * The target is only a widget constructed by one of the five fixed reference factories.
 */
internal fun finishReferenceConstruction(view: View) {
    ReflectionHelpers.callInstanceMethod<Unit>(view, "onFinishInflate")
}

/** Ordinary inflation and the previous MaterialComponentsViewInflater reference were distinct. */
internal enum class BaselineWidgetMode { PLATFORM, MATERIAL_COMPAT }

internal data class BaselineReferenceSource(
    val resourcePath: String,
    val baselineSourceSha256: String,
    val factory: String,
    val factorySourceSha256: String,
    val attributeHelperSha256: String,
    val kind: String = "source-derived-programmatic-widgets"
)
