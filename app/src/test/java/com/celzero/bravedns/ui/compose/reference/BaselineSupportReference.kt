/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.reference

/** Independent pinned-baseline widget reference; test only, never production UI.
 * These are explicit Android constructors and original scalar attributes, not XML inflation.
 * Review-time generator: docs/xml-layout-removal/test-dependencies-generate-factories.py.
 * Baseline: 23cb6dd98805445174bd83b7237ad5f5d2c24ac9:app/src/main/res/layout/activity_customer_support.xml
 * Runtime/compilation remain unverified; the reviewed launcher is BLOCKED.
 */
internal data class BaselineSupportViews(
    val root: android.view.View,
    val collapsingToolbar: com.google.android.material.appbar.CollapsingToolbarLayout,
    val heroSubtitle: android.widget.TextView,
    val sectionDescribe: android.widget.TextView,
    val attachmentSubscriptionTitle: android.widget.TextView,
    val source: BaselineReferenceSource = BaselineReferenceSources.support
)

internal object BaselineSupportReference {
    fun create(context: android.content.Context, title: String = "", subtitle: String = ""): BaselineSupportViews {
        val reference0Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/root",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "match_parent",
            android.R.attr.background to "?attr/background",
            android.R.attr.fitsSystemWindows to "true"
        )
        val reference0 = androidx.coordinatorlayout.widget.CoordinatorLayout(context, reference0Attributes)
        val reference1Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/app_bar",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.background to "@drawable/bg_plus_hero_gradient",
            android.R.attr.fitsSystemWindows to "true",
            com.google.android.material.R.attr.elevation to "4dp"
        )
        val reference1 = com.google.android.material.appbar.AppBarLayout(reference0.context, reference1Attributes)
        val reference1LayoutParams = reference0.generateLayoutParams(reference1Attributes)
        val reference2Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/collapsing_toolbar",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "180dp",
            android.R.attr.fitsSystemWindows to "true",
            com.google.android.material.R.attr.layout_scrollFlags to "scroll|exitUntilCollapsed|snap",
            com.google.android.material.R.attr.contentScrim to "@drawable/bg_plus_hero_gradient",
            com.google.android.material.R.attr.expandedTitleTextAppearance to "@style/TextAppearance.Material3.HeadlineMedium",
            com.google.android.material.R.attr.collapsedTitleTextAppearance to "@style/TextAppearance.Material3.TitleLarge",
            com.google.android.material.R.attr.expandedTitleMarginStart to "24dp",
            com.google.android.material.R.attr.expandedTitleMarginBottom to "48dp",
            com.google.android.material.R.attr.titleEnabled to "true"
        )
        val reference2 = com.google.android.material.appbar.CollapsingToolbarLayout(reference1.context, reference2Attributes)
        val reference2LayoutParams = com.google.android.material.appbar.AppBarLayout.LayoutParams(reference1.context, reference2Attributes)
        val reference3Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/toolbar",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "?attr/actionBarSize",
            com.google.android.material.R.attr.layout_collapseMode to "pin"
        )
        val reference3 = com.google.android.material.appbar.MaterialToolbar(reference2.context, reference3Attributes)
        val reference3LayoutParams = com.google.android.material.appbar.CollapsingToolbarLayout.LayoutParams(reference2.context, reference3Attributes)
        finishReferenceConstruction(reference3)
        reference2.addView(reference3, reference3LayoutParams)
        val reference4Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/tv_hero_subtitle",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_gravity to "bottom",
            android.R.attr.layout_marginStart to "24dp",
            android.R.attr.layout_marginEnd to "24dp",
            android.R.attr.layout_marginBottom to "18dp",
            android.R.attr.textSize to "@dimen/mini_font_text_view",
            android.R.attr.textColor to "?attr/primaryTextColor",
            android.R.attr.alpha to "0.7",
            android.R.attr.ellipsize to "end",
            android.R.attr.maxLines to "1",
            android.R.attr.fontFamily to "sans-serif",
            com.google.android.material.R.attr.layout_collapseMode to "parallax",
            com.google.android.material.R.attr.layout_collapseParallaxMultiplier to "0.6"
        )
        val reference4 = androidx.appcompat.widget.AppCompatTextView(reference2.context, reference4Attributes)
        val reference4LayoutParams = com.google.android.material.appbar.CollapsingToolbarLayout.LayoutParams(reference2.context, reference4Attributes)
        finishReferenceConstruction(reference4)
        reference2.addView(reference4, reference4LayoutParams)
        finishReferenceConstruction(reference2)
        reference1.addView(reference2, reference2LayoutParams)
        finishReferenceConstruction(reference1)
        reference0.addView(reference1, reference1LayoutParams)
        val reference5Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/nested_scroll",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "match_parent",
            android.R.attr.clipToPadding to "false",
            android.R.attr.fillViewport to "true",
            android.R.attr.overScrollMode to "never",
            android.R.attr.layout_marginBottom to "160dp",
            androidx.coordinatorlayout.R.attr.layout_behavior to "@string/appbar_scrolling_view_behavior"
        )
        val reference5 = androidx.core.widget.NestedScrollView(reference0.context, reference5Attributes)
        val reference5LayoutParams = reference0.generateLayoutParams(reference5Attributes)
        val reference6Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.orientation to "vertical",
            android.R.attr.paddingStart to "16dp",
            android.R.attr.paddingTop to "16dp",
            android.R.attr.paddingEnd to "16dp",
            android.R.attr.paddingBottom to "112dp"
        )
        val reference6 = android.widget.LinearLayout(reference5.context, reference6Attributes)
        val reference6LayoutParams = android.widget.FrameLayout.LayoutParams(reference5.context, reference6Attributes)
        val reference7Attributes = referenceAttributes("@style/TextAppearance.Material3.TitleSmall",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_section_describe",
            android.R.attr.textStyle to "bold",
            android.R.attr.textColor to "?attr/colorPrimary",
            android.R.attr.letterSpacing to "0.08",
            android.R.attr.layout_marginStart to "4dp",
            android.R.attr.layout_marginBottom to "8dp"
        )
        val reference7 = androidx.appcompat.widget.AppCompatTextView(reference6.context, reference7Attributes)
        val reference7LayoutParams = android.widget.LinearLayout.LayoutParams(reference6.context, reference7Attributes)
        finishReferenceConstruction(reference7)
        reference6.addView(reference7, reference7LayoutParams)
        val reference8Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_marginBottom to "20dp",
            androidx.cardview.R.attr.cardCornerRadius to "16dp",
            com.google.android.material.R.attr.strokeWidth to "0dp"
        )
        val reference8 = com.google.android.material.card.MaterialCardView(reference6.context, reference8Attributes)
        val reference8LayoutParams = android.widget.LinearLayout.LayoutParams(reference6.context, reference8Attributes)
        val reference9Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.orientation to "vertical",
            android.R.attr.padding to "16dp"
        )
        val reference9 = android.widget.LinearLayout(reference8.context, reference9Attributes)
        val reference9LayoutParams = android.widget.FrameLayout.LayoutParams(reference8.context, reference9Attributes)
        val reference10Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_issue_category_label",
            android.R.attr.textSize to "13sp",
            android.R.attr.textColor to "?attr/primaryLightColorText",
            android.R.attr.layout_marginBottom to "8dp"
        )
        val reference10 = androidx.appcompat.widget.AppCompatTextView(reference9.context, reference10Attributes)
        val reference10LayoutParams = android.widget.LinearLayout.LayoutParams(reference9.context, reference10Attributes)
        finishReferenceConstruction(reference10)
        reference9.addView(reference10, reference10LayoutParams)
        val reference11Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/chip_group_category",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            com.google.android.material.R.attr.singleSelection to "true",
            com.google.android.material.R.attr.selectionRequired to "false",
            android.R.attr.layout_marginBottom to "8dp"
        )
        val reference11 = com.google.android.material.chip.ChipGroup(reference9.context, reference11Attributes)
        val reference11LayoutParams = android.widget.LinearLayout.LayoutParams(reference9.context, reference11Attributes)
        val reference12Attributes = referenceAttributes("@style/Widget.Material3.Chip.Filter",
            android.R.attr.id to "@id/chip_payment",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_category_payment",
            android.R.attr.checkable to "true"
        )
        val reference12 = com.google.android.material.chip.Chip(reference11.context, reference12Attributes)
        val reference12LayoutParams = com.google.android.material.chip.ChipGroup.LayoutParams(reference11.context, reference12Attributes)
        finishReferenceConstruction(reference12)
        reference11.addView(reference12, reference12LayoutParams)
        val reference13Attributes = referenceAttributes("@style/Widget.Material3.Chip.Filter",
            android.R.attr.id to "@id/chip_activation",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_category_activation",
            android.R.attr.checkable to "true"
        )
        val reference13 = com.google.android.material.chip.Chip(reference11.context, reference13Attributes)
        val reference13LayoutParams = com.google.android.material.chip.ChipGroup.LayoutParams(reference11.context, reference13Attributes)
        finishReferenceConstruction(reference13)
        reference11.addView(reference13, reference13LayoutParams)
        val reference14Attributes = referenceAttributes("@style/Widget.Material3.Chip.Filter",
            android.R.attr.id to "@id/chip_connectivity",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_category_connectivity",
            android.R.attr.checkable to "true"
        )
        val reference14 = com.google.android.material.chip.Chip(reference11.context, reference14Attributes)
        val reference14LayoutParams = com.google.android.material.chip.ChipGroup.LayoutParams(reference11.context, reference14Attributes)
        finishReferenceConstruction(reference14)
        reference11.addView(reference14, reference14LayoutParams)
        val reference15Attributes = referenceAttributes("@style/Widget.Material3.Chip.Filter",
            android.R.attr.id to "@id/chip_refund",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_category_refund",
            android.R.attr.checkable to "true"
        )
        val reference15 = com.google.android.material.chip.Chip(reference11.context, reference15Attributes)
        val reference15LayoutParams = com.google.android.material.chip.ChipGroup.LayoutParams(reference11.context, reference15Attributes)
        finishReferenceConstruction(reference15)
        reference11.addView(reference15, reference15LayoutParams)
        val reference16Attributes = referenceAttributes("@style/Widget.Material3.Chip.Filter",
            android.R.attr.id to "@id/chip_moneyback",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_category_moneyback",
            android.R.attr.checkable to "true"
        )
        val reference16 = com.google.android.material.chip.Chip(reference11.context, reference16Attributes)
        val reference16LayoutParams = com.google.android.material.chip.ChipGroup.LayoutParams(reference11.context, reference16Attributes)
        finishReferenceConstruction(reference16)
        reference11.addView(reference16, reference16LayoutParams)
        val reference17Attributes = referenceAttributes("@style/Widget.Material3.Chip.Filter",
            android.R.attr.id to "@id/chip_other",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/category_name_others",
            android.R.attr.checkable to "true"
        )
        val reference17 = com.google.android.material.chip.Chip(reference11.context, reference17Attributes)
        val reference17LayoutParams = com.google.android.material.chip.ChipGroup.LayoutParams(reference11.context, reference17Attributes)
        finishReferenceConstruction(reference17)
        reference11.addView(reference17, reference17LayoutParams)
        finishReferenceConstruction(reference11)
        reference9.addView(reference11, reference11LayoutParams)
        val reference18Attributes = referenceAttributes("@style/Widget.Material3.TextInputLayout.OutlinedBox",
            android.R.attr.id to "@id/til_description",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.hint to "@string/support_description_hint",
            com.google.android.material.R.attr.boxCornerRadiusTopStart to "12dp",
            com.google.android.material.R.attr.boxCornerRadiusTopEnd to "12dp",
            com.google.android.material.R.attr.boxCornerRadiusBottomStart to "12dp",
            com.google.android.material.R.attr.boxCornerRadiusBottomEnd to "12dp",
            com.google.android.material.R.attr.counterEnabled to "true",
            com.google.android.material.R.attr.counterMaxLength to "1000"
        )
        val reference18 = com.google.android.material.textfield.TextInputLayout(reference9.context, reference18Attributes)
        val reference18LayoutParams = android.widget.LinearLayout.LayoutParams(reference9.context, reference18Attributes)
        val reference19Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/et_description",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.minLines to "5",
            android.R.attr.maxLines to "10",
            android.R.attr.gravity to "top",
            android.R.attr.inputType to "textMultiLine|textCapSentences",
            android.R.attr.textSize to "14sp",
            android.R.attr.textColor to "?attr/primaryTextColor",
            android.R.attr.maxLength to "1000"
        )
        val reference19 = com.google.android.material.textfield.TextInputEditText(reference18.context, reference19Attributes)
        val reference19LayoutParams = android.widget.LinearLayout.LayoutParams(reference18.context, reference19Attributes)
        finishReferenceConstruction(reference19)
        reference18.addView(reference19, reference19LayoutParams)
        finishReferenceConstruction(reference18)
        reference9.addView(reference18, reference18LayoutParams)
        finishReferenceConstruction(reference9)
        reference8.addView(reference9, reference9LayoutParams)
        finishReferenceConstruction(reference8)
        reference6.addView(reference8, reference8LayoutParams)
        val reference20Attributes = referenceAttributes("@style/TextAppearance.Material3.TitleSmall",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_section_attachments",
            android.R.attr.textStyle to "bold",
            android.R.attr.textColor to "?attr/colorPrimary",
            android.R.attr.letterSpacing to "0.08",
            android.R.attr.layout_marginStart to "4dp",
            android.R.attr.layout_marginBottom to "8dp"
        )
        val reference20 = androidx.appcompat.widget.AppCompatTextView(reference6.context, reference20Attributes)
        val reference20LayoutParams = android.widget.LinearLayout.LayoutParams(reference6.context, reference20Attributes)
        finishReferenceConstruction(reference20)
        reference6.addView(reference20, reference20LayoutParams)
        val reference21Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_marginBottom to "20dp",
            androidx.cardview.R.attr.cardCornerRadius to "16dp",
            com.google.android.material.R.attr.strokeWidth to "0dp"
        )
        val reference21 = com.google.android.material.card.MaterialCardView(reference6.context, reference21Attributes)
        val reference21LayoutParams = android.widget.LinearLayout.LayoutParams(reference6.context, reference21Attributes)
        val reference22Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.orientation to "vertical",
            android.R.attr.padding to "16dp"
        )
        val reference22 = android.widget.LinearLayout(reference21.context, reference22Attributes)
        val reference22LayoutParams = android.widget.FrameLayout.LayoutParams(reference21.context, reference22Attributes)
        val reference23Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_attachments_desc",
            android.R.attr.textSize to "13sp",
            android.R.attr.textColor to "?attr/primaryLightColorText",
            android.R.attr.lineSpacingExtra to "3dp",
            android.R.attr.layout_marginBottom to "12dp"
        )
        val reference23 = androidx.appcompat.widget.AppCompatTextView(reference22.context, reference23Attributes)
        val reference23LayoutParams = android.widget.LinearLayout.LayoutParams(reference22.context, reference23Attributes)
        finishReferenceConstruction(reference23)
        reference22.addView(reference23, reference23LayoutParams)
        val reference24Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.orientation to "horizontal",
            android.R.attr.gravity to "center_vertical",
            android.R.attr.layout_marginBottom to "10dp"
        )
        val reference24 = android.widget.LinearLayout(reference22.context, reference24Attributes)
        val reference24LayoutParams = android.widget.LinearLayout.LayoutParams(reference22.context, reference24Attributes)
        val reference25Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "0dp",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_weight to "1",
            android.R.attr.orientation to "vertical"
        )
        val reference25 = android.widget.LinearLayout(reference24.context, reference25Attributes)
        val reference25LayoutParams = android.widget.LinearLayout.LayoutParams(reference24.context, reference25Attributes)
        val reference26Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_attach_sub_status",
            android.R.attr.textStyle to "bold",
            android.R.attr.textColor to "?attr/primaryTextColor"
        )
        val reference26 = androidx.appcompat.widget.AppCompatTextView(reference25.context, reference26Attributes)
        val reference26LayoutParams = android.widget.LinearLayout.LayoutParams(reference25.context, reference26Attributes)
        finishReferenceConstruction(reference26)
        reference25.addView(reference26, reference26LayoutParams)
        val reference27Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_marginTop to "2dp",
            android.R.attr.text to "@string/support_attach_sub_status_desc",
            android.R.attr.textSize to "12sp",
            android.R.attr.textColor to "?attr/primaryLightColorText"
        )
        val reference27 = androidx.appcompat.widget.AppCompatTextView(reference25.context, reference27Attributes)
        val reference27LayoutParams = android.widget.LinearLayout.LayoutParams(reference25.context, reference27Attributes)
        finishReferenceConstruction(reference27)
        reference25.addView(reference27, reference27LayoutParams)
        finishReferenceConstruction(reference25)
        reference24.addView(reference25, reference25LayoutParams)
        val reference28Attributes = referenceAttributes("@style/CustomWidget.Material3.CompoundButton.MaterialSwitch",
            android.R.attr.id to "@id/switch_attach_status",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.checked to "true"
        )
        val reference28 = com.google.android.material.materialswitch.MaterialSwitch(reference24.context, reference28Attributes)
        val reference28LayoutParams = android.widget.LinearLayout.LayoutParams(reference24.context, reference28Attributes)
        finishReferenceConstruction(reference28)
        reference24.addView(reference28, reference28LayoutParams)
        finishReferenceConstruction(reference24)
        reference22.addView(reference24, reference24LayoutParams)
        val reference29Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "1dp",
            android.R.attr.background to "?attr/divider",
            android.R.attr.layout_marginBottom to "10dp"
        )
        val reference29 = android.view.View(reference22.context, reference29Attributes)
        val reference29LayoutParams = android.widget.LinearLayout.LayoutParams(reference22.context, reference29Attributes)
        finishReferenceConstruction(reference29)
        reference22.addView(reference29, reference29LayoutParams)
        val reference30Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.orientation to "horizontal",
            android.R.attr.gravity to "center_vertical"
        )
        val reference30 = android.widget.LinearLayout(reference22.context, reference30Attributes)
        val reference30LayoutParams = android.widget.LinearLayout.LayoutParams(reference22.context, reference30Attributes)
        val reference31Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "0dp",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_weight to "1",
            android.R.attr.orientation to "vertical"
        )
        val reference31 = android.widget.LinearLayout(reference30.context, reference31Attributes)
        val reference31LayoutParams = android.widget.LinearLayout.LayoutParams(reference30.context, reference31Attributes)
        val reference32Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_attach_vpn_stats",
            android.R.attr.textSize to "14sp",
            android.R.attr.textStyle to "bold",
            android.R.attr.fontFamily to "sans-serif-medium",
            android.R.attr.textColor to "?attr/primaryTextColor"
        )
        val reference32 = androidx.appcompat.widget.AppCompatTextView(reference31.context, reference32Attributes)
        val reference32LayoutParams = android.widget.LinearLayout.LayoutParams(reference31.context, reference32Attributes)
        finishReferenceConstruction(reference32)
        reference31.addView(reference32, reference32LayoutParams)
        val reference33Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_marginTop to "2dp",
            android.R.attr.text to "@string/support_attach_vpn_stats_desc",
            android.R.attr.textSize to "12sp",
            android.R.attr.textColor to "?attr/primaryLightColorText"
        )
        val reference33 = androidx.appcompat.widget.AppCompatTextView(reference31.context, reference33Attributes)
        val reference33LayoutParams = android.widget.LinearLayout.LayoutParams(reference31.context, reference33Attributes)
        finishReferenceConstruction(reference33)
        reference31.addView(reference33, reference33LayoutParams)
        finishReferenceConstruction(reference31)
        reference30.addView(reference31, reference31LayoutParams)
        val reference34Attributes = referenceAttributes("@style/CustomWidget.Material3.CompoundButton.MaterialSwitch",
            android.R.attr.id to "@id/switch_attach_stats",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.checked to "true"
        )
        val reference34 = com.google.android.material.materialswitch.MaterialSwitch(reference30.context, reference34Attributes)
        val reference34LayoutParams = android.widget.LinearLayout.LayoutParams(reference30.context, reference34Attributes)
        finishReferenceConstruction(reference34)
        reference30.addView(reference34, reference34LayoutParams)
        finishReferenceConstruction(reference30)
        reference22.addView(reference30, reference30LayoutParams)
        val reference35Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "1dp",
            android.R.attr.background to "?attr/divider",
            android.R.attr.layout_marginTop to "10dp",
            android.R.attr.layout_marginBottom to "10dp"
        )
        val reference35 = android.view.View(reference22.context, reference35Attributes)
        val reference35LayoutParams = android.widget.LinearLayout.LayoutParams(reference22.context, reference35Attributes)
        finishReferenceConstruction(reference35)
        reference22.addView(reference35, reference35LayoutParams)
        val reference36Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.orientation to "horizontal",
            android.R.attr.gravity to "center_vertical"
        )
        val reference36 = android.widget.LinearLayout(reference22.context, reference36Attributes)
        val reference36LayoutParams = android.widget.LinearLayout.LayoutParams(reference22.context, reference36Attributes)
        val reference37Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "0dp",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_weight to "1",
            android.R.attr.orientation to "vertical"
        )
        val reference37 = android.widget.LinearLayout(reference36.context, reference37Attributes)
        val reference37LayoutParams = android.widget.LinearLayout.LayoutParams(reference36.context, reference37Attributes)
        val reference38Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_attach_proc_info",
            android.R.attr.textSize to "14sp",
            android.R.attr.textStyle to "bold",
            android.R.attr.fontFamily to "sans-serif-medium",
            android.R.attr.textColor to "?attr/primaryTextColor"
        )
        val reference38 = androidx.appcompat.widget.AppCompatTextView(reference37.context, reference38Attributes)
        val reference38LayoutParams = android.widget.LinearLayout.LayoutParams(reference37.context, reference38Attributes)
        finishReferenceConstruction(reference38)
        reference37.addView(reference38, reference38LayoutParams)
        val reference39Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_marginTop to "2dp",
            android.R.attr.text to "@string/support_attach_proc_info_desc",
            android.R.attr.textSize to "12sp",
            android.R.attr.textColor to "?attr/primaryLightColorText"
        )
        val reference39 = androidx.appcompat.widget.AppCompatTextView(reference37.context, reference39Attributes)
        val reference39LayoutParams = android.widget.LinearLayout.LayoutParams(reference37.context, reference39Attributes)
        finishReferenceConstruction(reference39)
        reference37.addView(reference39, reference39LayoutParams)
        finishReferenceConstruction(reference37)
        reference36.addView(reference37, reference37LayoutParams)
        val reference40Attributes = referenceAttributes("@style/CustomWidget.Material3.CompoundButton.MaterialSwitch",
            android.R.attr.id to "@id/switch_attach_proc_info",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.checked to "true"
        )
        val reference40 = com.google.android.material.materialswitch.MaterialSwitch(reference36.context, reference40Attributes)
        val reference40LayoutParams = android.widget.LinearLayout.LayoutParams(reference36.context, reference40Attributes)
        finishReferenceConstruction(reference40)
        reference36.addView(reference40, reference40LayoutParams)
        finishReferenceConstruction(reference36)
        reference22.addView(reference36, reference36LayoutParams)
        finishReferenceConstruction(reference22)
        reference21.addView(reference22, reference22LayoutParams)
        finishReferenceConstruction(reference21)
        reference6.addView(reference21, reference21LayoutParams)
        finishReferenceConstruction(reference6)
        reference5.addView(reference6, reference6LayoutParams)
        finishReferenceConstruction(reference5)
        reference0.addView(reference5, reference5LayoutParams)
        val reference41Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/layout_bottom_bar",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_gravity to "bottom",
            android.R.attr.orientation to "vertical",
            android.R.attr.background to "?attr/colorSurface",
            android.R.attr.elevation to "8dp"
        )
        val reference41 = android.widget.LinearLayout(reference0.context, reference41Attributes)
        val reference41LayoutParams = reference0.generateLayoutParams(reference41Attributes)
        val reference42Attributes = referenceAttributes("@style/Widget.Material3.Button.OutlinedButton",
            android.R.attr.id to "@id/btn_send_email",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "56dp",
            android.R.attr.text to "@string/about_bug_report_dialog_positive_btn",
            android.R.attr.textSize to "16sp",
            com.google.android.material.R.attr.cornerRadius to "28dp",
            android.R.attr.layout_marginStart to "24dp",
            android.R.attr.layout_marginEnd to "24dp",
            com.google.android.material.R.attr.iconGravity to "textStart",
            android.R.attr.textColor to "?attr/accentGood",
            com.google.android.material.R.attr.iconPadding to "8dp"
        )
        val reference42 = com.google.android.material.button.MaterialButton(reference41.context, reference42Attributes)
        val reference42LayoutParams = android.widget.LinearLayout.LayoutParams(reference41.context, reference42Attributes)
        finishReferenceConstruction(reference42)
        reference41.addView(reference42, reference42LayoutParams)
        val reference43Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_weight to "1",
            android.R.attr.layout_gravity to "center",
            android.R.attr.gravity to "center",
            android.R.attr.text to "@string/support_privacy_note",
            android.R.attr.textSize to "10sp",
            android.R.attr.alpha to "0.8",
            android.R.attr.layout_marginTop to "6dp",
            android.R.attr.paddingStart to "20dp",
            android.R.attr.paddingEnd to "20dp",
            android.R.attr.paddingBottom to "20dp",
            android.R.attr.textColor to "?attr/primaryLightColorText",
            android.R.attr.lineSpacingExtra to "3dp"
        )
        val reference43 = androidx.appcompat.widget.AppCompatTextView(reference41.context, reference43Attributes)
        val reference43LayoutParams = android.widget.LinearLayout.LayoutParams(reference41.context, reference43Attributes)
        finishReferenceConstruction(reference43)
        reference41.addView(reference43, reference43LayoutParams)
        val reference44Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/layout_loading",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.orientation to "horizontal",
            android.R.attr.gravity to "center",
            android.R.attr.padding to "8dp",
            android.R.attr.visibility to "gone"
        )
        val reference44 = android.widget.LinearLayout(reference41.context, reference44Attributes)
        val reference44LayoutParams = android.widget.LinearLayout.LayoutParams(reference41.context, reference44Attributes)
        val reference45Attributes = referenceAttributes("@android:style/Widget.ProgressBar.Small",
            android.R.attr.id to "@id/progress_bar",
            android.R.attr.layout_width to "24dp",
            android.R.attr.layout_height to "24dp",
            android.R.attr.layout_marginEnd to "12dp"
        )
        val reference45 = android.widget.ProgressBar(reference44.context, reference45Attributes)
        val reference45LayoutParams = android.widget.LinearLayout.LayoutParams(reference44.context, reference45Attributes)
        finishReferenceConstruction(reference45)
        reference44.addView(reference45, reference45LayoutParams)
        val reference46Attributes = referenceAttributes(null,
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.text to "@string/support_loading_data",
            android.R.attr.textSize to "14sp",
            android.R.attr.textColor to "?attr/primaryLightColorText"
        )
        val reference46 = androidx.appcompat.widget.AppCompatTextView(reference44.context, reference46Attributes)
        val reference46LayoutParams = android.widget.LinearLayout.LayoutParams(reference44.context, reference46Attributes)
        finishReferenceConstruction(reference46)
        reference44.addView(reference46, reference46LayoutParams)
        finishReferenceConstruction(reference44)
        reference41.addView(reference44, reference44LayoutParams)
        finishReferenceConstruction(reference41)
        reference0.addView(reference41, reference41LayoutParams)
        finishReferenceConstruction(reference0)
        reference2.title = title
        reference4.text = subtitle
        return BaselineSupportViews(reference0, reference2, reference4, reference7, reference26)
    }
}
