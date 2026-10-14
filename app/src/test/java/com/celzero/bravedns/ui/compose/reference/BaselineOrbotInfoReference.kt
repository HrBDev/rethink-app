/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.reference

/** Independent pinned-baseline widget reference; test only, never production UI.
 * These are explicit Android constructors and original scalar attributes, not XML inflation.
 * Review-time generator: docs/xml-layout-removal/test-dependencies-generate-factories.py.
 * Baseline: 23cb6dd98805445174bd83b7237ad5f5d2c24ac9:app/src/full/res/layout/dialog_info_rules_layout.xml
 * Runtime/compilation remain unverified; the reviewed launcher is BLOCKED.
 */
internal data class BaselineOrbotInfoViews(
    val root: android.view.View,
    val title: android.widget.TextView,
    val description: android.widget.TextView,
    val closeImage: android.widget.ImageView,
    val source: BaselineReferenceSource = BaselineReferenceSources.orbot
)

internal object BaselineOrbotInfoReference {
    fun create(context: android.content.Context, title: CharSequence? = null, description: CharSequence? = null): BaselineOrbotInfoViews {
        val reference0Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "match_parent",
            android.R.attr.orientation to "vertical",
            android.R.attr.padding to "20dp",
            androidx.coordinatorlayout.R.attr.layout_behavior to "@string/appbar_scrolling_view_behavior",
            com.google.android.material.R.attr.layout_scrollFlags to "scroll|enterAlways"
        )
        val reference0 = androidx.core.widget.NestedScrollView(context, reference0Attributes)
        val reference1Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "match_parent",
            android.R.attr.orientation to "vertical",
            androidx.coordinatorlayout.R.attr.layout_behavior to "@string/appbar_scrolling_view_behavior"
        )
        val reference1 = android.widget.LinearLayout(reference0.context, reference1Attributes)
        val reference1LayoutParams = android.widget.FrameLayout.LayoutParams(reference0.context, reference1Attributes)
        val reference2Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.orientation to "horizontal"
        )
        val reference2 = android.widget.RelativeLayout(reference1.context, reference2Attributes)
        val reference2LayoutParams = android.widget.LinearLayout.LayoutParams(reference1.context, reference2Attributes)
        val reference3Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/info_rules_dialog_cancel_img",
            android.R.attr.layout_width to "30dp",
            android.R.attr.layout_height to "30dp",
            android.R.attr.layout_centerVertical to "true",
            android.R.attr.layout_alignParentEnd to "true",
            android.R.attr.src to "@android:drawable/ic_menu_close_clear_cancel"
        )
        val reference3 = androidx.appcompat.widget.AppCompatImageView(reference2.context, reference3Attributes)
        val reference3LayoutParams = android.widget.RelativeLayout.LayoutParams(reference2.context, reference3Attributes)
        finishReferenceConstruction(reference3)
        reference2.addView(reference3, reference3LayoutParams)
        val reference4Attributes = referenceAttributes("@style/Widget.Material3.Button.TextButton.Dialog",
            android.R.attr.id to "@id/info_rules_dialog_ok_btn",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_toStartOf to "@id/info_rules_dialog_cancel_img",
            android.R.attr.layout_marginEnd to "10dp",
            android.R.attr.background to "?attr/selectableItemBackground",
            android.R.attr.drawablePadding to "10dp",
            android.R.attr.layout_centerVertical to "true",
            android.R.attr.padding to "10dp",
            android.R.attr.text to "@string/about_bug_report_dialog_positive_btn",
            android.R.attr.visibility to "gone"
        )
        val reference4 = com.google.android.material.button.MaterialButton(reference2.context, reference4Attributes)
        val reference4LayoutParams = android.widget.RelativeLayout.LayoutParams(reference2.context, reference4Attributes)
        finishReferenceConstruction(reference4)
        reference2.addView(reference4, reference4LayoutParams)
        finishReferenceConstruction(reference2)
        reference1.addView(reference2, reference2LayoutParams)
        val reference5Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_gravity to "center",
            android.R.attr.orientation to "horizontal"
        )
        val reference5 = android.widget.LinearLayout(reference1.context, reference5Attributes)
        val reference5LayoutParams = android.widget.LinearLayout.LayoutParams(reference1.context, reference5Attributes)
        val reference6Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/info_rules_dialog_rules_icon",
            android.R.attr.layout_width to "24dp",
            android.R.attr.layout_height to "24dp",
            android.R.attr.layout_gravity to "center_vertical",
            android.R.attr.layout_margin to "5dp",
            android.R.attr.src to "@android:drawable/ic_menu_close_clear_cancel",
            android.R.attr.visibility to "gone"
        )
        val reference6 = androidx.appcompat.widget.AppCompatImageView(reference5.context, reference6Attributes)
        val reference6LayoutParams = android.widget.LinearLayout.LayoutParams(reference5.context, reference6Attributes)
        finishReferenceConstruction(reference6)
        reference5.addView(reference6, reference6LayoutParams)
        val reference7Attributes = referenceAttributes("@style/Widget.AppCompat.TextView.SpinnerItem",
            android.R.attr.id to "@id/info_rules_dialog_rules_title",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_gravity to "center_vertical",
            android.R.attr.drawablePadding to "15dp",
            android.R.attr.text to "@string/firewall_rules",
            android.R.attr.textAppearance to "@style/TextAppearance.AppCompat.Headline",
            android.R.attr.textColor to "?attr/primaryTextColor",
            android.R.attr.textSize to "@dimen/heading_font_text_view"
        )
        val reference7 = androidx.appcompat.widget.AppCompatTextView(reference5.context, reference7Attributes)
        val reference7LayoutParams = android.widget.LinearLayout.LayoutParams(reference5.context, reference7Attributes)
        finishReferenceConstruction(reference7)
        reference5.addView(reference7, reference7LayoutParams)
        finishReferenceConstruction(reference5)
        reference1.addView(reference5, reference5LayoutParams)
        val reference8Attributes = referenceAttributes("@style/Widget.AppCompat.TextView.SpinnerItem",
            android.R.attr.id to "@id/info_rules_dialog_rules_desc",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_marginTop to "10dp",
            android.R.attr.lineSpacingExtra to "5dp",
            android.R.attr.textIsSelectable to "true",
            android.R.attr.padding to "5dp",
            android.R.attr.text to "@string/bsct_conn_rule_explanation",
            android.R.attr.textAppearance to "@style/TextAppearance.AppCompat.Subhead",
            android.R.attr.textColor to "?attr/primaryTextColor",
            android.R.attr.textSize to "@dimen/large_font_text_view"
        )
        val reference8 = androidx.appcompat.widget.AppCompatTextView(reference1.context, reference8Attributes)
        val reference8LayoutParams = android.widget.LinearLayout.LayoutParams(reference1.context, reference8Attributes)
        finishReferenceConstruction(reference8)
        reference1.addView(reference8, reference8LayoutParams)
        val reference9Attributes = referenceAttributes("@style/Widget.AppCompat.TextView.SpinnerItem",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.drawablePadding to "20dp",
            android.R.attr.padding to "10dp",
            android.R.attr.text to "@string/hybrid_mode_info_title",
            android.R.attr.textAppearance to "@style/TextAppearance.AppCompat.Headline",
            android.R.attr.visibility to "gone",
            androidx.appcompat.R.attr.drawableStartCompat to "@drawable/spinner_both"
        )
        val reference9 = androidx.appcompat.widget.AppCompatTextView(reference1.context, reference9Attributes)
        val reference9LayoutParams = android.widget.LinearLayout.LayoutParams(reference1.context, reference9Attributes)
        finishReferenceConstruction(reference9)
        reference1.addView(reference9, reference9LayoutParams)
        finishReferenceConstruction(reference1)
        reference0.addView(reference1, reference1LayoutParams)
        finishReferenceConstruction(reference0)
        if (title != null) reference7.text = title
        if (description != null) reference8.text = description
        return BaselineOrbotInfoViews(reference0, reference7, reference8, reference3)
    }
}
