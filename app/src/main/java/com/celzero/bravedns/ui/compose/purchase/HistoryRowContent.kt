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
import com.celzero.bravedns.ui.compose.controls.OriginalLabel
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
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
@Composable private fun HistoryChip(source: String, control: RpnControlState) {
    if (!control.isVisible) return
    OriginalLabel(source, backgroundTint = control.backgroundTintList,
        contentColor = control.foregroundColor?.let(::Color)) {
        val inherited = LocalTextStyle.current
        Text(control.text.toString(), maxLines = control.maxLines, overflow = TextOverflow.Ellipsis,
            style = inherited.copy(fontSize = control.textSizeSp?.sp ?: inherited.fontSize,
                fontWeight = if (control.bold) FontWeight.Bold else inherited.fontWeight,
                fontFamily = if (control.monospace) FontFamily.Monospace else inherited.fontFamily))
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable fun HistoryRowContent(state: HistoryRowState, server: Boolean) {
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val icon = if (server) state.ivTypeIcon else state.ivStatusIcon
        icon.imageRes?.let { Icon(painterResource(it), null, Modifier.size(28.dp), tint = icon.imageTintList?.defaultColor?.let(::Color) ?: MaterialTheme.colorScheme.onSurface) }
        if (server) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { HistoryChip("list_item_server_order:tv_type_badge", state.tvTypeBadge); HistoryChip("list_item_server_order:tv_status_chip", state.tvStatusChip); HistoryChip("list_item_server_order:tv_test_badge", state.tvTestBadge) }
            RpnText(state.tvProductName, style = MaterialTheme.typography.titleMedium)
            RpnText(state.tvToken, style = MaterialTheme.typography.bodySmall)
            RpnText(state.tvDatePrimary); RpnText(state.tvDateSecondary); RpnText(state.tvOrderId)
        } else {
            RpnText(state.tvTimestamp, style = MaterialTheme.typography.bodySmall)
            HistoryChip("list_item_purchase_history:tv_state_badge", state.tvStateBadge); RpnText(state.tvTransition); RpnText(state.tvReason)
        }
        if (state.divider.isVisible) HorizontalDivider()
    }
}
