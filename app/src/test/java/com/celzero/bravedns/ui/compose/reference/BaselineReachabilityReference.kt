/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.reference

/** Independent pinned-baseline widget reference; test only, never production UI.
 * These are explicit Android constructors and original scalar attributes, not XML inflation.
 * Review-time generator: docs/xml-layout-removal/test-dependencies-generate-factories.py.
 * Baseline: 23cb6dd98805445174bd83b7237ad5f5d2c24ac9:app/src/main/res/layout/dialog_input_ips.xml
 * Runtime/compilation remain unverified; the reviewed launcher is BLOCKED.
 */
internal data class BaselineReachabilityViews(
    val ipv4EditText: androidx.appcompat.widget.AppCompatEditText,
    val source: BaselineReferenceSource = BaselineReferenceSources.reachability
)

internal object BaselineReachabilityReference {
    fun create(context: android.content.Context): BaselineReachabilityViews {
        val reference0Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/ipv4_address_1",
            android.R.attr.layout_width to "0dp",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_weight to "1",
            android.R.attr.background to "@drawable/edittext_default",
            android.R.attr.inputType to "text"
        )
        val reference0 = androidx.appcompat.widget.AppCompatEditText(context, reference0Attributes)
        finishReferenceConstruction(reference0)
        return BaselineReachabilityViews(reference0)
    }
}
