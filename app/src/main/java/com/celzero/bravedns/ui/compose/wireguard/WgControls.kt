/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.wireguard

import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.BackgroundColorSpan
import android.text.style.UnderlineSpan
import android.text.style.StrikethroughSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.themeColor

private fun Modifier.wgState(node: WgControlState): Modifier = alpha(if (node.visibility == View.INVISIBLE) 0f else node.alpha)
    .then(if (node.visibility == View.INVISIBLE) Modifier.clearAndSetSemantics { } else Modifier)

@Composable fun WgText(node: WgControlState, modifier: Modifier = Modifier) {
    if (node.visibility == View.GONE) return
    Text(wgAnnotatedText(if (node.isAllCaps) node.text.toString().uppercase() else node.text), modifier.wgState(node).then(if (node.hasClickListener) Modifier.clickable(enabled = node.isEnabled && node.isClickable) { node.performClick() } else Modifier),
        color = node.textColor?.let(::Color) ?: themeColor(R.attr.primaryTextColor),
        fontSize = node.textSizeSp?.sp ?: MaterialTheme.typography.bodyMedium.fontSize, maxLines = node.maxLines,
        overflow = if (node.ellipsize) TextOverflow.Ellipsis else TextOverflow.Clip,
        fontWeight = if (node.textStyle and android.graphics.Typeface.BOLD != 0) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (node.textStyle and android.graphics.Typeface.ITALIC != 0) FontStyle.Italic else FontStyle.Normal)
}

fun wgAnnotatedText(value: CharSequence?): AnnotatedString {
    val text = value ?: ""
    if (text !is Spanned) return AnnotatedString(text.toString())
    return AnnotatedString.Builder(text.toString()).apply {
        text.getSpans(0, text.length, Any::class.java).forEach { span ->
            val style = when (span) {
                is ForegroundColorSpan -> SpanStyle(color = Color(span.foregroundColor))
                is RelativeSizeSpan -> SpanStyle(fontSize = span.sizeChange.em)
                is StyleSpan -> SpanStyle(
                    fontWeight = if (span.style and android.graphics.Typeface.BOLD != 0) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (span.style and android.graphics.Typeface.ITALIC != 0) FontStyle.Italic else FontStyle.Normal)
                is TypefaceSpan -> SpanStyle(fontFamily = FontFamily(android.graphics.Typeface.create(span.family, android.graphics.Typeface.NORMAL)))
                is BackgroundColorSpan -> SpanStyle(background = Color(span.backgroundColor))
                is UnderlineSpan -> SpanStyle(textDecoration = TextDecoration.Underline)
                is StrikethroughSpan -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                else -> null
            }
            style?.let { addStyle(it, text.getSpanStart(span), text.getSpanEnd(span)) }
        }
    }.toAnnotatedString()
}

@Composable fun WgAction(node: WgControlState, modifier: Modifier = Modifier) {
    if (node.visibility == View.GONE) return
    TextButton(onClick = { node.performClick() }, enabled = node.isEnabled && node.isClickable,
        modifier = modifier.wgState(node), colors = ButtonDefaults.textButtonColors(contentColor = node.textColor?.let(::Color) ?: MaterialTheme.colorScheme.primary)) { Text(if (node.isAllCaps) node.text.toString().uppercase() else node.text.toString()) }
}

@Composable fun WgCheck(node: WgControlState, label: String? = null, switch: Boolean = false, modifier: Modifier = Modifier) {
    if (node.visibility == View.GONE) return
    val m = modifier.wgState(node).then(if (label != null) Modifier.semantics { contentDescription = label } else Modifier)
    if (switch) Switch(node.isChecked, { node.performClick() }, m, enabled = node.isEnabled && node.isClickable, colors = wgSwitchColors(node))
    else {
        val colors = node.buttonTint?.let { tint ->
            CheckboxDefaults.colors(checkedColor = Color(tint), uncheckedColor = Color(tint), disabledCheckedColor = Color(tint), disabledUncheckedColor = Color(tint))
        } ?: CheckboxDefaults.colors()
        Checkbox(node.isChecked, { node.performClick() }, m, enabled = node.isEnabled && node.isClickable, colors = colors)
    }
}

@Composable private fun wgSwitchColors(node: WgControlState): SwitchColors {
    val overlay = node.switchThemeOverlay ?: return SwitchDefaults.colors()
    val context = LocalContext.current
    val tints = remember(context, overlay) {
        val themed = androidx.appcompat.view.ContextThemeWrapper(context, overlay)
        listOf(com.google.android.material.R.color.mtrl_switch_thumb_tint,
            com.google.android.material.R.color.mtrl_switch_track_tint,
            com.google.android.material.R.color.mtrl_switch_track_decoration_tint).map {
            requireNotNull(androidx.appcompat.content.res.AppCompatResources.getColorStateList(themed, it))
        }
    }
    fun tint(index: Int, enabled: Boolean, checked: Boolean): Color {
        val states = intArrayOf(if (enabled) android.R.attr.state_enabled else -android.R.attr.state_enabled,
            if (checked) android.R.attr.state_checked else -android.R.attr.state_checked)
        return Color(tints[index].getColorForState(states, tints[index].defaultColor))
    }
    return SwitchDefaults.colors(
        checkedThumbColor = tint(0, true, true), checkedTrackColor = tint(1, true, true), checkedBorderColor = tint(2, true, true),
        uncheckedThumbColor = tint(0, true, false), uncheckedTrackColor = tint(1, true, false), uncheckedBorderColor = tint(2, true, false),
        disabledCheckedThumbColor = tint(0, false, true), disabledCheckedTrackColor = tint(1, false, true), disabledCheckedBorderColor = tint(2, false, true),
        disabledUncheckedThumbColor = tint(0, false, false), disabledUncheckedTrackColor = tint(1, false, false), disabledUncheckedBorderColor = tint(2, false, false)
    )
}

@Composable fun WgIcon(node: WgControlState, label: String? = null, modifier: Modifier = Modifier) {
    if (node.visibility == View.GONE) return
    val m = modifier.wgState(node)
    val content: @Composable () -> Unit = {
        if (node.drawable != null) {
            val bitmap = remember(node.drawable) { node.drawable!!.toBitmap(width = 96, height = 96).asImageBitmap() }
            Image(bitmap, label, Modifier.size(40.dp))
        } else node.imageResource?.let { Icon(painterResource(it), label, Modifier.size(24.dp), tint = node.textColor?.let(::Color) ?: LocalContentColor.current) }
    }
    if (node.hasClickListener) IconButton({ node.performClick() }, m, enabled = node.isEnabled && node.isClickable) { content() }
    else Box(m) { content() }
}

@Composable fun WgProgress(node: WgControlState, modifier: Modifier = Modifier) {
    if (node.visibility == View.GONE) return
    if (node.isIndeterminate) LinearProgressIndicator(modifier.wgState(node))
    else LinearProgressIndicator(progress = { (node.progress.toFloat() / node.max.coerceAtLeast(1)).coerceIn(0f, 1f) }, modifier = modifier.wgState(node), color = node.indicatorColor?.let(::Color) ?: MaterialTheme.colorScheme.primary)
}

@Composable fun WgCard(node: WgControlState? = null, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    if (node?.visibility == View.GONE) return
    val context = LocalContext.current
    val values = remember(context) {
        context.obtainStyledAttributes(null, intArrayOf(androidx.cardview.R.attr.cardBackgroundColor, androidx.cardview.R.attr.cardElevation), com.google.android.material.R.attr.materialCardViewStyle, 0).let {
            val color = if (it.hasValue(0)) it.getColor(0, 0) else null
            val elevation = it.getDimension(1, 0f) / context.resources.displayMetrics.density
            it.recycle(); color to elevation
        }
    }
    val width = with(LocalDensity.current) { (node?.strokeWidth ?: 0).toDp() }
    val m = modifier.fillMaxWidth().then(if (node != null) Modifier.wgState(node) else Modifier)
        .then(if (node?.hasClickListener == true) Modifier.clickable(enabled = node.isEnabled && node.isClickable) { node.performClick() } else Modifier)
    Card(m, shape = RoundedCornerShape((node?.radiusDp ?: 8f).dp),
        border = if (width.value > 0) BorderStroke(width, Color(node!!.strokeColor)) else null,
        colors = CardDefaults.cardColors(containerColor = (node?.backgroundColor ?: values.first)?.let(::Color) ?: MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = (node?.elevationDp ?: values.second).dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable fun WgField(node: WgControlState, label: CharSequence, modifier: Modifier = Modifier, readOnly: Boolean = false,
                        keyboard: KeyboardType = KeyboardType.Text, maxLines: Int = 4, onDone: (() -> Unit)? = node.onEditorDone, trailing: (@Composable () -> Unit)? = null) {
    if (node.visibility == View.GONE) return
    OutlinedTextField(node.text.toString(), { node.setText(it) }, modifier.fillMaxWidth().wgState(node), enabled = node.isEnabled,
        readOnly = readOnly, label = { Text(label.toString()) }, placeholder = { Text(node.hint.toString()) },
        visualTransformation = if (node.isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        maxLines = maxLines, shape = RoundedCornerShape(8.dp), trailingIcon = trailing,
        keyboardOptions = KeyboardOptions(keyboardType = if (node.isPassword) KeyboardType.Password else keyboard, autoCorrectEnabled = !node.noSuggestions, imeAction = if (onDone != null) ImeAction.Done else if (node.imeNext) ImeAction.Next else ImeAction.Default),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }))
}
