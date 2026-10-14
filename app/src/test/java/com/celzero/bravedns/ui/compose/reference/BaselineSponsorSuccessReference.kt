/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.reference

/** Independent pinned-baseline widget reference; test only, never production UI.
 * These are explicit Android constructors and original scalar attributes, not XML inflation.
 * Review-time generator: docs/xml-layout-removal/test-dependencies-generate-factories.py.
 * Baseline: 23cb6dd98805445174bd83b7237ad5f5d2c24ac9:app/src/full/res/layout/fragment_sponsor_success.xml
 * Runtime/compilation remain unverified; the reviewed launcher is BLOCKED.
 */
internal data class BaselineSponsorSuccessViews(
    val root: android.view.View,
    val title: android.widget.TextView,
    val message: android.widget.TextView,
    val continueButton: com.google.android.material.button.MaterialButton,
    val emblem: android.widget.ImageView,
    val source: BaselineReferenceSource = BaselineReferenceSources.sponsor
)

internal object BaselineSponsorSuccessReference {
    fun create(context: android.content.Context, widgetMode: BaselineWidgetMode = BaselineWidgetMode.PLATFORM): BaselineSponsorSuccessViews {
        val reference0Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "match_parent",
            android.R.attr.gravity to "center",
            android.R.attr.orientation to "vertical",
            android.R.attr.padding to "32dp"
        )
        val reference0 = android.widget.LinearLayout(context, reference0Attributes)
        val reference1Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "140dp",
            android.R.attr.layout_height to "140dp",
            androidx.cardview.R.attr.cardCornerRadius to "70dp",
            androidx.cardview.R.attr.cardElevation to "0dp",
            androidx.cardview.R.attr.cardBackgroundColor to "?attr/colorSurfaceContainerHigh",
            com.google.android.material.R.attr.strokeWidth to "0dp"
        )
        val reference1 = com.google.android.material.card.MaterialCardView(reference0.context, reference1Attributes)
        val reference1LayoutParams = android.widget.LinearLayout.LayoutParams(reference0.context, reference1Attributes)
        val reference2Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "match_parent",
            android.R.attr.gravity to "center"
        )
        val reference2 = android.widget.LinearLayout(reference1.context, reference2Attributes)
        val reference2LayoutParams = android.widget.FrameLayout.LayoutParams(reference1.context, reference2Attributes)
        val reference3Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/success_emblem",
            android.R.attr.layout_width to "80dp",
            android.R.attr.layout_height to "80dp",
            android.R.attr.importantForAccessibility to "no",
            android.R.attr.src to "@drawable/dns_icon",
            androidx.appcompat.R.attr.tint to "?attr/accentGood"
        )
        val reference3: android.widget.ImageView = when (widgetMode) {
            BaselineWidgetMode.PLATFORM -> android.widget.ImageView(reference2.context, reference3Attributes)
            BaselineWidgetMode.MATERIAL_COMPAT -> androidx.appcompat.widget.AppCompatImageView(reference2.context, reference3Attributes)
        }
        val reference3LayoutParams = android.widget.LinearLayout.LayoutParams(reference2.context, reference3Attributes)
        finishReferenceConstruction(reference3)
        reference2.addView(reference3, reference3LayoutParams)
        finishReferenceConstruction(reference2)
        reference1.addView(reference2, reference2LayoutParams)
        finishReferenceConstruction(reference1)
        reference0.addView(reference1, reference1LayoutParams)
        val reference4Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/success_title",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_marginTop to "40dp",
            android.R.attr.text to "@string/sponsor_thank_you",
            android.R.attr.textAppearance to "@style/TextAppearance.Material3.HeadlineLarge",
            android.R.attr.textStyle to "bold",
            android.R.attr.textColor to "?attr/colorPrimary"
        )
        val reference4: android.widget.TextView = when (widgetMode) {
            BaselineWidgetMode.PLATFORM -> android.widget.TextView(reference0.context, reference4Attributes)
            BaselineWidgetMode.MATERIAL_COMPAT -> com.google.android.material.textview.MaterialTextView(reference0.context, reference4Attributes)
        }
        val reference4LayoutParams = android.widget.LinearLayout.LayoutParams(reference0.context, reference4Attributes)
        finishReferenceConstruction(reference4)
        reference0.addView(reference4, reference4LayoutParams)
        val reference5Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/success_message",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_marginTop to "16dp",
            android.R.attr.gravity to "center",
            android.R.attr.lineSpacingExtra to "6dp",
            android.R.attr.text to "@string/sponsor_support_message",
            android.R.attr.textAppearance to "@style/TextAppearance.Material3.BodyLarge",
            android.R.attr.textColor to "?attr/colorOnSurfaceVariant",
            android.R.attr.paddingHorizontal to "16dp"
        )
        val reference5: android.widget.TextView = when (widgetMode) {
            BaselineWidgetMode.PLATFORM -> android.widget.TextView(reference0.context, reference5Attributes)
            BaselineWidgetMode.MATERIAL_COMPAT -> com.google.android.material.textview.MaterialTextView(reference0.context, reference5Attributes)
        }
        val reference5LayoutParams = android.widget.LinearLayout.LayoutParams(reference0.context, reference5Attributes)
        finishReferenceConstruction(reference5)
        reference0.addView(reference5, reference5LayoutParams)
        val reference6Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_marginTop to "56dp",
            android.R.attr.orientation to "vertical"
        )
        val reference6 = android.widget.LinearLayout(reference0.context, reference6Attributes)
        val reference6LayoutParams = android.widget.LinearLayout.LayoutParams(reference0.context, reference6Attributes)
        val reference7Attributes = referenceAttributes("@style/Widget.Material3.Button.OutlinedButton",
            android.R.attr.id to "@id/continue_button",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "64dp",
            android.R.attr.background to "@drawable/rectangle_border_background",
            android.R.attr.text to "@string/sponsor_continue",
            android.R.attr.textAppearance to "@style/TextAppearance.Material3.TitleMedium"
        )
        val reference7 = com.google.android.material.button.MaterialButton(reference6.context, reference7Attributes)
        val reference7LayoutParams = android.widget.LinearLayout.LayoutParams(reference6.context, reference7Attributes)
        finishReferenceConstruction(reference7)
        reference6.addView(reference7, reference7LayoutParams)
        finishReferenceConstruction(reference6)
        reference0.addView(reference6, reference6LayoutParams)
        finishReferenceConstruction(reference0)
        return BaselineSponsorSuccessViews(reference0, reference4, reference5, reference7, reference3)
    }
}
