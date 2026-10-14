/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.statistics

import com.celzero.bravedns.ui.compose.controls.*
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.chrome.homeNavigationClearance
import com.celzero.bravedns.data.AppConnection
import com.celzero.bravedns.data.DataUsageSummary
import com.celzero.bravedns.ui.compose.NoDimDialogWindow
import com.celzero.bravedns.ui.compose.appdetails.AppCountryMap
import com.celzero.bravedns.ui.compose.appdetails.appDonutSweeps
import com.celzero.bravedns.ui.compose.themeColor
import com.celzero.bravedns.ui.fragment.SummaryStatisticsFragment.SummaryStatisticsType as Type
import com.celzero.bravedns.ui.stats.StatsInsightsMath
import com.celzero.bravedns.ui.stats.StatsViewMode
import com.celzero.bravedns.util.Utilities
import com.celzero.bravedns.viewmodel.SummaryStatisticsViewModel.TimeCategory

@Composable
internal fun SummaryStatisticsScreen(state: SummaryStatisticsState) {
    Surface {
        LazyColumn(Modifier.fillMaxSize().testTag("summary_statistics"), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 15.dp, bottom = homeNavigationClearance(90.dp, contentGap = 10.dp))) {
            item {
                Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).alpha(.5f)) {
                        Text(stringResource(if (state.alpha) R.string.app_name_alpha else R.string.app_name_small_case), style = MaterialTheme.typography.headlineLarge)
                        Text(stringResource(R.string.about_title_desc), style = MaterialTheme.typography.bodySmall)
                    }
                    StatsModeButton(state.mode == StatsViewMode.LIST, R.drawable.ic_view_list, R.string.cd_stats_switch_to_list, "stats_mode_list") { state.onMode(StatsViewMode.LIST) }
                    StatsModeButton(state.mode == StatsViewMode.INSIGHTS, R.drawable.ic_view_chart, R.string.cd_stats_switch_to_insights, "stats_mode_insights") { state.onMode(StatsViewMode.INSIGHTS) }
                    Box {
                        val filterDescription = stringResource(R.string.ssv_filter_heading)
                        OriginalButton("fragment_summary_statistics:fss_filter_btn", onClick = state.onFilter,
                            modifier = Modifier.testTag("stats_filter").semantics { contentDescription = filterDescription }) { }
                        statisticsFilterBadge(state.filters, state.providersAvailable)?.let { count ->
                            Badge(Modifier.align(Alignment.BottomEnd).animateContentSize()) { Text(count.toString()) }
                        }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy((-1).dp)) {
                    items(TimeCategory.entries.size) { index ->
                        val tc = TimeCategory.entries[index]
                        val amount = when (tc) { TimeCategory.ONE_HOUR -> "1"; TimeCategory.TWENTY_FOUR_HOUR -> "24"; TimeCategory.SEVEN_DAYS -> "7" }
                        OriginalChip("fragment_summary_statistics:" + when (tc) { TimeCategory.TWENTY_FOUR_HOUR -> "tb_daily_toggle_btn"; TimeCategory.SEVEN_DAYS -> "tb_weekly_toggle_btn"; else -> "tb_recent_toggle_btn" }, segmentIndex = index, segmentCount = 3, containerColor = themeColor(if (state.timeCategory == tc) R.attr.accentGood else R.attr.defaultToggleBtnBg), contentColor = themeColor(if (state.timeCategory == tc) R.attr.homeScreenHeaderTextColor else R.attr.primaryTextColor), selected = state.timeCategory == tc, onClick = { state.onTimeCategory(tc) }, modifier = Modifier.widthIn(min = 100.dp).testTag("stats_time_${tc.value}"), label = { Text(stringResource(R.string.ci_desc, amount, stringResource(if (tc == TimeCategory.SEVEN_DAYS) R.string.lbl_day else R.string.lbl_hour))) })
                    }
                }
            }
            item { TrafficUsage(if (state.mode == StatsViewMode.INSIGHTS) state.insightUsage else state.listUsage, state.mode == StatsViewMode.INSIGHTS) }
            summarySectionOrder.forEach { type ->
                val section = state.sections.getValue(type)
                val visible = state.sectionAvailable(type) && if (state.mode == StatsViewMode.INSIGHTS) section.items.isNotEmpty() else section.visible
                if (visible) {
                    item(key = "header_${type.tid}") {
                        Row(Modifier.padding(top = 20.dp).testTag("stats_section_${type.tid}"), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(statisticsTitle(type)), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            if (type == Type.TOP_ACTIVE_CONNS) OriginalButton("fragment_summary_statistics:fss_close_conns_chip", onClick = state.onClose, modifier = Modifier.testTag("stats_close_all")) { Text(stringResource(R.string.close_all)) }
                            OriginalButton("fragment_summary_statistics:fss_active_apps_chip", onClick = { state.onDetail(type) }, modifier = Modifier.testTag("stats_detail_${type.tid}")) { Text(stringResource(R.string.ssv_see_more)) }
                        }
                    }
                    if (state.mode == StatsViewMode.INSIGHTS) item(key = "insights_${type.tid}") { SummaryInsights(type, section.items) }
                    else section.presenter?.let { presenter -> items(presenter.itemCount, key = { "row_${type.tid}_$it" }) { presenter.RowAt(it) } }
                }
            }
        }
    }
    if (state.closeDialog) AlertDialog(onDismissRequest = { state.closeDialog = false },
        title = { NoDimDialogWindow(); Text(stringResource(R.string.close_conns_dialog_title)) },
        text = { Text(stringResource(R.string.close_conns_dialog_desc, stringResource(R.string.lbl_all_apps).lowercase())) },
        confirmButton = { OriginalButton("@dialog_positive", onClick = { if (state.closeDialog) { state.closeDialog = false; state.onConfirmClose() } }, modifier = Modifier.testTag("stats_confirm_close")) { Text(stringResource(R.string.lbl_proceed)) } },
        dismissButton = { OriginalButton("@dialog_negative", onClick = { state.closeDialog = false }, modifier = Modifier.testTag("stats_cancel_close")) { Text(stringResource(R.string.lbl_cancel)) } })
}

@Composable
private fun StatsModeButton(selected: Boolean, icon: Int, description: Int, tag: String, onClick: () -> Unit) {
    val label = stringResource(description)
    OriginalButton("fragment_summary_statistics:" + if (tag == "stats_mode_list") "fss_view_mode_list_btn" else "fss_view_mode_insights_btn",
        onClick = { if (!selected) onClick() }, modifier = Modifier.testTag(tag).semantics { contentDescription = label },
        selected = selected, segmentIndex = if (tag == "stats_mode_list") 0 else 1, segmentCount = 2,
        containerColor = themeColor(if (selected) R.attr.accentGood else R.attr.defaultToggleBtnBg),
        leadingIcon = { Icon(painterResource(icon), null, Modifier.size(22.dp), tint = themeColor(if (selected) R.attr.homeScreenHeaderTextColor else R.attr.defaultToggleBtnTxt)) }) { }

}
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrafficUsage(usage: DataUsageSummary?, insights: Boolean) {
    if (usage == null) return
    val total = usage.totalDownload + usage.totalUpload
    val unmetered = total - usage.meteredDataUsage
    val metered = usage.meteredDataUsage
    val entries = listOf(R.string.ada_app_unmetered to unmetered, R.string.ada_app_metered to metered, R.string.lbl_overall to total)
    if (insights) FlowRow(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        entries.forEach { (label, bytes) -> Surface(shape = MaterialTheme.shapes.medium, color = themeColor(R.attr.colorSurfaceContainer)) {
            Column(Modifier.padding(12.dp)) {
                Text(Utilities.humanReadableByteCount(bytes, true), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(label), style = MaterialTheme.typography.labelSmall)
            }
        } }
    } else Surface(Modifier.fillMaxWidth().padding(top = 12.dp), shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, themeColor(R.attr.secondaryTextColor))) {
        Column(Modifier.padding(14.dp)) {
            val ump = if (total == 0L) 0 else (unmetered * 100 / total).toInt()
            val mp = if (total == 0L) 0 else (metered * 100 / total).toInt()
            LinearProgressIndicator(progress = { if (ump + mp == 0) 0f else ump.toFloat() / (ump + mp) }, modifier = Modifier.fillMaxWidth(),
                color = themeColor(R.attr.accentGood), trackColor = themeColor(R.attr.accentGood).copy(alpha = .5f), gapSize = 0.dp, drawStopIndicator = {})
            entries.forEach { (label, bytes) -> Text(stringResource(R.string.two_argument_colon, stringResource(label), Utilities.humanReadableByteCount(bytes, true)), Modifier.padding(top = 8.dp)) }
        }
    }
}
@Composable
internal fun SummaryInsights(type: Type, items: List<AppConnection>) {
    val fractions = StatsInsightsMath.normalizeFractions(items.map { statisticsMetric(it, type) })
    val positive = themeColor(R.attr.accentGood)
    val negative = themeColor(R.attr.accentBad)
    val track = themeColor(R.attr.colorSurfaceContainerHighest)
    val blocked = type in listOf(Type.MOST_BLOCKED_APPS, Type.MOST_BLOCKED_ASN, Type.MOST_BLOCKED_DOMAINS, Type.MOST_BLOCKED_BLOCKLISTS, Type.MOST_BLOCKED_IPS)
    val base = if (blocked) negative else positive
    val context = LocalContext.current
    val bytes = type == Type.MOST_CONNECTED_APPS
    val total = items.sumOf { statisticsMetric(it, type) }.let { if (bytes) Utilities.humanReadableByteCount(it, true) else it.toString() }
    val rows: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEachIndexed { index, item ->
                val label = if (type == Type.MOST_CONNECTED_ASN || type == Type.MOST_BLOCKED_ASN) stringResource(R.string.two_argument_space, item.flag, item.appOrDnsName.orEmpty()) else statisticsLabel(context, item, type)
                val metric = statisticsMetric(item, type).let { if (bytes) Utilities.humanReadableByteCount(it, true) else it.toString() }
                val description = stringResource(R.string.ci_desc, label, metric)
                Column(Modifier.semantics(mergeDescendants = true) { contentDescription = description }) {
                    Row { Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall); Text(metric, Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodySmall) }
                    LinearProgressIndicator(progress = { (fractions[index] * 100).toInt() / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 5.dp).height(3.dp),
                        color = if (item.blocked) negative else positive, trackColor = track, gapSize = 0.dp, drawStopIndicator = {})
                }
            }
        }
    }
    if (type == Type.MOST_CONTACTED_COUNTRIES) Column { AppCountryMap(items); rows() }
    else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        val sweeps = appDonutSweeps(fractions.take(5))
        val label = stringResource(statisticsTitle(type))
        Box(Modifier.size(96.dp).semantics(mergeDescendants = true) { contentDescription = "$label: $total" }, contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val ring = minOf(size.width, size.height) * .14f
                val diameter = minOf(size.width, size.height) - ring
                val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                val area = Size(diameter, diameter)
                drawArc(track, 0f, 360f, false, topLeft, area, style = Stroke(ring))
                var start = -90f
                val alphas = listOf(255, 190, 140, 100, 70)
                sweeps.forEachIndexed { index, sweep ->
                    if (sweep > 2f) drawArc(base.copy(alpha = alphas[index] / 255f), start, sweep - 1.5f, false, topLeft, area, style = Stroke(ring))
                    start += sweep
                }
            }
            Text(total, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.width(14.dp)); Box(Modifier.weight(1f)) { rows() }
    }
}
