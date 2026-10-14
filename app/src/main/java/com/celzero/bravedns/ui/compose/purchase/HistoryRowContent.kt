/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.purchase
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.ui.compose.rpn.*
class HistoryRowState {
    val tvTimestamp = RpnControlState()
    val tvStateBadge = RpnControlState()
    val tvTransition = RpnControlState()
    val tvReason = RpnControlState()
    val ivStatusIcon = RpnControlState()
    val tvPurchaseToken = RpnControlState()
    val divider = RpnControlState()
    val tvTypeBadge = RpnControlState()
    val ivTypeIcon = RpnControlState()
    val tvStatusChip = RpnControlState()
    val tvProductName = RpnControlState()
    val tvToken = RpnControlState()
    val tvDatePrimary = RpnControlState()
    val tvDateSecondary = RpnControlState()
    val tvOrderId = RpnControlState()
    val tvTestBadge = RpnControlState()
}
@Composable private fun HistoryChip(control: RpnControlState) {
    if (!control.isVisible) return
    Surface(color = control.backgroundTintList?.defaultColor?.let(::Color) ?: MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
        RpnText(control, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), MaterialTheme.typography.labelSmall)
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable fun HistoryRowContent(state: HistoryRowState, server: Boolean) {
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val icon = if (server) state.ivTypeIcon else state.ivStatusIcon
        icon.imageRes?.let { Icon(painterResource(it), null, Modifier.size(28.dp), tint = icon.imageTintList?.defaultColor?.let(::Color) ?: MaterialTheme.colorScheme.onSurface) }
        if (server) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { HistoryChip(state.tvTypeBadge); HistoryChip(state.tvStatusChip); HistoryChip(state.tvTestBadge) }
            RpnText(state.tvProductName, style = MaterialTheme.typography.titleMedium)
            RpnText(state.tvToken, style = MaterialTheme.typography.bodySmall)
            RpnText(state.tvDatePrimary); RpnText(state.tvDateSecondary); RpnText(state.tvOrderId)
        } else {
            RpnText(state.tvTimestamp, style = MaterialTheme.typography.bodySmall)
            HistoryChip(state.tvStateBadge); RpnText(state.tvTransition); RpnText(state.tvReason)
        }
        if (state.divider.isVisible) HorizontalDivider()
    }
}
