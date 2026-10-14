/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.reference

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import com.celzero.bravedns.R
import kotlin.math.roundToInt

/** Independent native-widget oracles transcribed from baseline
 * 23cb6dd98805445174bd83b7237ad5f5d2c24ac9. These factories do not depend on any
 * application layout resource or any production Compose renderer/style resolver.
 * The source guard verifies baseline attributes and pins this complete source. */
internal object RuleDialogReferenceFactories {
    /** list_item_custom_domain#custom_domain_status_icon and rectangle_border_background. */
    fun listBadge(context: Context) = AppCompatTextView(context).apply {
        layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            marginStart = dp(context, 5f)
            marginEnd = dp(context, 10f)
            addRule(RelativeLayout.CENTER_VERTICAL)
        }
        minimumWidth = dp(context, 25f)
        val inset = dp(context, 5f)
        setPadding(inset, inset, inset, inset)
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        val border = context.obtainStyledAttributes(intArrayOf(R.attr.border))
        try {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f * context.resources.displayMetrics.density
                // Android dimensionPixelSize rounds nonzero subpixel widths up to at least 1px.
                setStroke(dp(context, .75f).coerceAtLeast(1), border.getColorStateList(0))
                // No setColor: the original frame has no solid element.
            }
        } finally { border.recycle() }
    }

    /** dialog_set_custom_doh / dialog_set_custom_odoh#dialog_custom_url_failure_text. */
    fun dohError(context: Context) = AppCompatTextView(context).apply {
        sourceBodyMedium(context, this)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            val margin = dp(context, 10f)
            setMargins(margin, margin, margin, margin)
        }
        val inset = dp(context, 5f)
        setPadding(inset, inset, inset, inset)
        setText(R.string.settings_http_proxy_error_text4)
        visibility = View.INVISIBLE
    }

    /** dialog_set_dns_crypt#dialog_dns_crypt_error_txt: visible, empty, no margins. */
    fun cryptError(context: Context) = AppCompatTextView(context).apply {
        sourceBodyMedium(context, this)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val inset = dp(context, 2f)
        setPadding(inset, inset, inset, inset)
        text = ""
        visibility = View.VISIBLE
    }

    private fun sourceBodyMedium(context: Context, view: AppCompatTextView) {
        val appearance = TypedValue()
        check(context.theme.resolveAttribute(com.google.android.material.R.attr.textAppearanceBodyMedium, appearance, true))
        view.setTextAppearance(context, appearance.resourceId)
        val color = context.obtainStyledAttributes(intArrayOf(androidx.appcompat.R.attr.colorError))
        try { view.setTextColor(checkNotNull(color.getColorStateList(0))) } finally { color.recycle() }
    }
    private fun dp(context: Context, value: Float) = (value * context.resources.displayMetrics.density).roundToInt()
}
