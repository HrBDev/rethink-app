/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.utilities
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.data.AllowedAppInfo
import com.celzero.bravedns.data.BlockedAppInfo
import com.celzero.bravedns.ui.compose.themeColor
import com.celzero.bravedns.util.Logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BubbleQuickFix(val drawable: Int, val label: Int, val active: Boolean, val onClick: () -> Unit)
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun BubbleScreen(ui: BubbleUiState) {
    Surface(Modifier.fillMaxSize(), color = themeColor(R.attr.background)) {
        LazyColumn(Modifier.fillMaxSize().safeDrawingPadding().testTag("bubble.list"), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item("title") {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(stringResource(R.string.firewall_bubble_title), style = utilityTextStyle(androidx.appcompat.R.style.TextAppearance_AppCompat_Title, weight = android.graphics.Typeface.BOLD), color = themeColor(R.attr.primaryTextColor), modifier = Modifier.semantics { heading() })
                    Text(stringResource(R.string.firewall_bubble_subtitle), style = utilityTextStyle(androidx.appcompat.R.style.TextAppearance_AppCompat_Body2), color = themeColor(R.attr.primaryLightColorText))
                }
            }
            item("quick-fixes") { LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(ui.quickFixes, key = { it.label }) { fix ->
                    val label = stringResource(fix.label)
                    TooltipBox(TooltipDefaults.rememberPlainTooltipPositionProvider(), { PlainTooltip { Text(label) } }, rememberTooltipState()) {
                        IconButton(fix.onClick, Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).testTag("bubble.quick.${fix.label}").semantics { selected = fix.active }) {
                            utilityImage(fix.drawable, Modifier.size(32.dp).alpha(if (fix.active) 1f else .4f), label, themeColor(if (fix.active) R.attr.accentGood else R.attr.primaryLightColorText))
                        }
                    }
                }
            } }
            if (ui.bubbleAllowedAppsLl.isVisible) {
                item("allowed-title") { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    BubbleHeading(R.string.bubble_allowed_title, Modifier.weight(1f)); UtilityText(ui.bubbleAllowedCount)
                } }
                ui.allowedAdapter?.let { adapter -> items(adapter.itemCount, key = { "allowed-${adapter.peek(it)?.uid ?: it}" }) { index -> adapter.getItem(index)?.let { adapter.Row(it) } } }
            }
            item("blocked-title") { BubbleHeading(R.string.bubble_activity_title, Modifier.padding(12.dp)) }
            if (ui.bubbleProgressCard.isVisible) item("loading") { Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (ui.bubbleProgressBar.isVisible) CircularProgressIndicator(Modifier.size(36.dp).testTag("bubble.loading"))
                Text(stringResource(R.string.loading), Modifier.padding(top = 10.dp), style = utilityTextStyle(androidx.appcompat.R.style.TextAppearance_AppCompat_Body2))
            } }
            if (ui.bubbleRecyclerView.isVisible) ui.blockedAdapter?.let { adapter -> items(adapter.itemCount, key = { "blocked-${adapter.peek(it)?.uid ?: it}" }) { index -> adapter.getItem(index)?.let { adapter.Row(it) } } }
            if (ui.bubbleEmptyState.isVisible) item("empty") { Column(Modifier.fillMaxWidth().padding(24.dp).testTag("bubble.empty"), horizontalAlignment = Alignment.CenterHorizontally) {
                utilityImage(R.drawable.ic_firewall_shield, Modifier.size(44.dp), tint = themeColor(androidx.appcompat.R.attr.colorPrimary))
                UtilityText(ui.bubbleEmptyTitle, Modifier.padding(top = 12.dp))
                Text(stringResource(R.string.bubble_empty_state_desc), Modifier.padding(top = 6.dp), style = utilityTextStyle(androidx.appcompat.R.style.TextAppearance_AppCompat_Body2), textAlign = TextAlign.Center, color = themeColor(R.attr.primaryLightColorText))
            } }
        }
    }
}
@Composable private fun BubbleHeading(id: Int, modifier: Modifier) { Text(stringResource(id), modifier.semantics { heading() }, style = utilityTextStyle(androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead, weight = android.graphics.Typeface.BOLD), color = themeColor(R.attr.primaryLightColorText)) }
@Composable private fun AppIcon(packageName: String, blocked: Boolean) {
    val context = LocalContext.current
    val icon = remember(packageName, context) {
        val fallback = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.default_app_icon)
        val drawable = if (blocked) {
            try { if (packageName == "Unknown") fallback else context.packageManager.getApplicationIcon(packageName) }
            catch (_: android.content.pm.PackageManager.NameNotFoundException) { Logger.e("BubbleBlockedAppsAdapter", "App icon not found for $packageName"); fallback }
        } else { try { context.packageManager.getApplicationIcon(packageName) } catch (_: Exception) { fallback } }
        UtilityControl(context).apply { setImageDrawable(drawable) }
    }
    UtilityImage(icon, Modifier.size(32.dp))
}
@Composable fun BubbleAllowedRow(app: AllowedAppInfo, action: (AllowedAppInfo) -> Unit, now: Long = remember(app) { System.currentTimeMillis() }) {
    val age = bubbleAllowedAge(app.allowedAt, now)
    val label = when { age.expired -> stringResource(R.string.lbl_expired); age.minutesAgo <= 0 -> stringResource(R.string.bubble_time_just_now); else -> stringResource(R.string.bubble_time_minutes_ago, age.minutesAgo.toInt()) }
    BubbleRow(app.appName, app.packageName, label, null, false, stringResource(R.string.lbl_remove), "bubble.remove.${app.uid}", { action(app) }, age.expired)
}
@Composable fun BubbleBlockedRow(app: BlockedAppInfo, action: (BlockedAppInfo) -> Unit, now: Long = remember(app) { System.currentTimeMillis() }) {
    val age = bubbleBlockedAge(app.lastBlocked, now)
    val ago = when (age.unit) { "now" -> stringResource(R.string.bubble_time_just_now); "minutes" -> stringResource(R.string.bubble_time_minutes_ago, age.value); "hours" -> stringResource(R.string.bubble_time_hours_ago, age.value); else -> SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(app.lastBlocked)) }
    BubbleRow(app.appName, app.packageName, stringResource(R.string.bubble_blocked_count, app.count), ago, true, stringResource(R.string.bubble_allow_btn), "bubble.allow.${app.uid}", { action(app) }, true)
}
@Composable private fun BubbleRow(name: String, pkg: String, line: String, time: String?, blocked: Boolean, action: String, tag: String, click: () -> Unit, bad: Boolean) {
    val large = LocalDensity.current.fontScale > 1.3f
    Card(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 3.dp), colors = CardDefaults.cardColors(containerColor = themeColor(R.attr.background))) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(pkg, blocked)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(name, style = utilityTextStyle(androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead, weight = android.graphics.Typeface.BOLD), color = themeColor(R.attr.primaryTextColor))
                    if (blocked) Text(pkg, style = utilityTextStyle(androidx.appcompat.R.style.TextAppearance_AppCompat_Caption), color = themeColor(R.attr.primaryLightColorText))
                    Text(line, style = utilityTextStyle(androidx.appcompat.R.style.TextAppearance_AppCompat_Caption), color = themeColor(if (bad) R.attr.accentBad else R.attr.accentGood))
                    if (time != null) Text(time, style = utilityTextStyle(androidx.appcompat.R.style.TextAppearance_AppCompat_Caption), color = themeColor(R.attr.primaryLightColorText))
                }
                if (!large) TextButton(click, Modifier.testTag(tag).semantics { contentDescription = "$action $name" }) { Text(action) }
            }
            if (large) TextButton(click, Modifier.align(Alignment.End).testTag(tag).semantics { contentDescription = "$action $name" }) { Text(action) }
        }
    }
}
