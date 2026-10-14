/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.reference

/** Independent pinned-baseline widget reference; test only, never production UI.
 * These are explicit Android constructors and original scalar attributes, not XML inflation.
 * Review-time generator: docs/xml-layout-removal/test-dependencies-generate-factories.py.
 * Baseline: 23cb6dd98805445174bd83b7237ad5f5d2c24ac9:app/src/main/res/layout/bottom_sheet_summary_stats_filter.xml
 * Runtime/compilation remain unverified; the reviewed launcher is BLOCKED.
 */
internal data class BaselineStatisticsFilterViews(
    val group: com.google.android.material.chip.ChipGroup,
    val source: BaselineReferenceSource = BaselineReferenceSources.statistics
)

internal object BaselineStatisticsFilterReference {
    fun create(context: android.content.Context): BaselineStatisticsFilterViews {
        val reference0Attributes = referenceAttributes(null,
            android.R.attr.id to "@id/ssfc_chip_group",
            android.R.attr.layout_width to "match_parent",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.paddingTop to "6dp",
            com.google.android.material.R.attr.chipSpacingHorizontal to "6dp",
            com.google.android.material.R.attr.chipSpacingVertical to "4dp",
            com.google.android.material.R.attr.selectionRequired to "true",
            com.google.android.material.R.attr.singleLine to "false"
        )
        val reference0 = com.google.android.material.chip.ChipGroup(context, reference0Attributes)
        val reference1Attributes = referenceAttributes("@style/ThinnerChip.Large",
            android.R.attr.id to "@id/fss_filter_apps_chip",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_margin to "3dp",
            android.R.attr.padding to "2dp",
            android.R.attr.checkable to "true",
            android.R.attr.checked to "true",
            android.R.attr.text to "@string/apps_info_title",
            android.R.attr.textAlignment to "center",
            android.R.attr.textSize to "@dimen/default_font_text_view",
            com.google.android.material.R.attr.checkedIconVisible to "false",
            com.google.android.material.R.attr.chipBackgroundColor to "@drawable/bg_chip_state_list"
        )
        val reference1 = com.google.android.material.chip.Chip(reference0.context, reference1Attributes)
        val reference1LayoutParams = com.google.android.material.chip.ChipGroup.LayoutParams(reference0.context, reference1Attributes)
        finishReferenceConstruction(reference1)
        reference0.addView(reference1, reference1LayoutParams)
        val reference2Attributes = referenceAttributes("@style/ThinnerChip.Large",
            android.R.attr.id to "@id/fss_filter_countries_chip",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_margin to "3dp",
            android.R.attr.padding to "2dp",
            android.R.attr.checkable to "true",
            android.R.attr.checked to "true",
            android.R.attr.text to "@string/ssv_filter_countries",
            android.R.attr.textAlignment to "center",
            android.R.attr.textSize to "@dimen/default_font_text_view",
            com.google.android.material.R.attr.checkedIconVisible to "false",
            com.google.android.material.R.attr.chipBackgroundColor to "@drawable/bg_chip_state_list"
        )
        val reference2 = com.google.android.material.chip.Chip(reference0.context, reference2Attributes)
        val reference2LayoutParams = com.google.android.material.chip.ChipGroup.LayoutParams(reference0.context, reference2Attributes)
        finishReferenceConstruction(reference2)
        reference0.addView(reference2, reference2LayoutParams)
        val reference3Attributes = referenceAttributes("@style/ThinnerChip.Large",
            android.R.attr.id to "@id/fss_filter_providers_chip",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_margin to "3dp",
            android.R.attr.padding to "2dp",
            android.R.attr.checkable to "true",
            android.R.attr.checked to "true",
            android.R.attr.text to "@string/ssv_filter_providers",
            android.R.attr.textAlignment to "center",
            android.R.attr.textSize to "@dimen/default_font_text_view",
            com.google.android.material.R.attr.checkedIconVisible to "false",
            com.google.android.material.R.attr.chipBackgroundColor to "@drawable/bg_chip_state_list"
        )
        val reference3 = com.google.android.material.chip.Chip(reference0.context, reference3Attributes)
        val reference3LayoutParams = com.google.android.material.chip.ChipGroup.LayoutParams(reference0.context, reference3Attributes)
        finishReferenceConstruction(reference3)
        reference0.addView(reference3, reference3LayoutParams)
        val reference4Attributes = referenceAttributes("@style/ThinnerChip.Large",
            android.R.attr.id to "@id/fss_filter_blocklist_chip",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_margin to "3dp",
            android.R.attr.padding to "2dp",
            android.R.attr.checkable to "true",
            android.R.attr.checked to "true",
            android.R.attr.text to "@string/ssv_filter_blocklist",
            android.R.attr.textAlignment to "center",
            android.R.attr.textSize to "@dimen/default_font_text_view",
            com.google.android.material.R.attr.checkedIconVisible to "false",
            com.google.android.material.R.attr.chipBackgroundColor to "@drawable/bg_chip_state_list"
        )
        val reference4 = com.google.android.material.chip.Chip(reference0.context, reference4Attributes)
        val reference4LayoutParams = com.google.android.material.chip.ChipGroup.LayoutParams(reference0.context, reference4Attributes)
        finishReferenceConstruction(reference4)
        reference0.addView(reference4, reference4LayoutParams)
        val reference5Attributes = referenceAttributes("@style/ThinnerChip.Large",
            android.R.attr.id to "@id/fss_filter_domains_chip",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_margin to "3dp",
            android.R.attr.padding to "2dp",
            android.R.attr.checkable to "true",
            android.R.attr.checked to "true",
            android.R.attr.text to "@string/ssv_filter_domains",
            android.R.attr.textAlignment to "center",
            android.R.attr.textSize to "@dimen/default_font_text_view",
            com.google.android.material.R.attr.checkedIconVisible to "false",
            com.google.android.material.R.attr.chipBackgroundColor to "@drawable/bg_chip_state_list"
        )
        val reference5 = com.google.android.material.chip.Chip(reference0.context, reference5Attributes)
        val reference5LayoutParams = com.google.android.material.chip.ChipGroup.LayoutParams(reference0.context, reference5Attributes)
        finishReferenceConstruction(reference5)
        reference0.addView(reference5, reference5LayoutParams)
        val reference6Attributes = referenceAttributes("@style/ThinnerChip.Large",
            android.R.attr.id to "@id/fss_filter_ips_chip",
            android.R.attr.layout_width to "wrap_content",
            android.R.attr.layout_height to "wrap_content",
            android.R.attr.layout_margin to "3dp",
            android.R.attr.padding to "2dp",
            android.R.attr.checkable to "true",
            android.R.attr.checked to "true",
            android.R.attr.text to "@string/ssv_filter_ips",
            android.R.attr.textAlignment to "center",
            android.R.attr.textSize to "@dimen/default_font_text_view",
            com.google.android.material.R.attr.checkedIconVisible to "false",
            com.google.android.material.R.attr.chipBackgroundColor to "@drawable/bg_chip_state_list"
        )
        val reference6 = com.google.android.material.chip.Chip(reference0.context, reference6Attributes)
        val reference6LayoutParams = com.google.android.material.chip.ChipGroup.LayoutParams(reference0.context, reference6Attributes)
        finishReferenceConstruction(reference6)
        reference0.addView(reference6, reference6LayoutParams)
        finishReferenceConstruction(reference0)
        return BaselineStatisticsFilterViews(reference0)
    }
}
