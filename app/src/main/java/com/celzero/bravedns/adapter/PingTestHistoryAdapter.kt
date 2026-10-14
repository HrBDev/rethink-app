/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.adapter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.rpnproxy.RpnProxyManager.PingTestHistoryEntry
import com.celzero.bravedns.rpnproxy.RpnProxyManager.PingTestOutcome
import com.celzero.bravedns.ui.compose.themeColor
import com.celzero.bravedns.ui.compose.utilities.*
import com.celzero.bravedns.util.UIUtils

class PingTestHistoryAdapter {
    var entries by mutableStateOf<List<PingTestHistoryEntry>>(emptyList()); private set
    fun submitList(values: List<PingTestHistoryEntry>) { entries = values.toList() }
}
@Composable fun PingHistoryRow(entry: PingTestHistoryEntry) {
    val outcome = entry.outcomeEnum()
    val color = when(outcome) { PingTestOutcome.SUCCESS -> colorResource(R.color.accentGood); PingTestOutcome.PARTIAL -> themeColor(R.attr.accentWarning); PingTestOutcome.FAILURE -> colorResource(R.color.accentBad) }
    val text = stringResource(when(outcome) { PingTestOutcome.SUCCESS -> R.string.ping_reach_reachable; PingTestOutcome.PARTIAL -> R.string.ping_partial_title; PingTestOutcome.FAILURE -> R.string.ping_failure_title })
    val large = LocalDensity.current.fontScale > 1.3f
    Row(Modifier.fillMaxWidth().padding(16.dp).testTag("ping.history.${entry.timestamp}").semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
        Surface(color = themeColor(R.attr.colorSurfaceVariant), shape = MaterialTheme.shapes.small) {
            utilityImage(if (outcome == PingTestOutcome.SUCCESS) R.drawable.ic_tick else R.drawable.ic_cross_accent, Modifier.size(32.dp).padding(6.dp), tint = color)
        }
        Column(Modifier.weight(1f).padding(start = 13.dp)) {
            Text(if(entry.isAuto()) stringResource(R.string.ping_history_auto_targets) else entry.targets, style = utilityTextStyle(size = 14.5f), color = themeColor(R.attr.primaryTextColor))
            val time = UIUtils.getRelativeTimeSpan(entry.timestamp)?.toString().orEmpty()
            Text(if(entry.total > 1) stringResource(R.string.ping_history_meta_passed, time, entry.passed, entry.total) else time, style = utilityTextStyle(size = 11.5f), color = themeColor(R.attr.primaryLightColorText))
            if (large) { Text(stringResource(R.string.ping_total_latency, entry.latencyMs), style = utilityTextStyle(size = 13f)); Text(text, color = color, style = utilityTextStyle(size = 11.5f)) }
        }
        if (!large) Column(Modifier.padding(start = 12.dp), horizontalAlignment = Alignment.End) {
            Text(stringResource(R.string.ping_total_latency, entry.latencyMs), style = utilityTextStyle(size = 13f), color = themeColor(R.attr.primaryTextColor))
            Text(text, color = color, style = utilityTextStyle(size = 11.5f))
        }
    }
}
