/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.celzero.bravedns.ui.compose.wireguard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R

@Composable private fun WgChips(group: WgControlState, vararg nodes: WgControlState) {
    if (!group.isVisible) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        nodes.filter { it.isVisible }.forEach { node ->
            Surface(Modifier.alpha(node.alpha).heightIn(min = 24.dp), shape = RoundedCornerShape(12.dp),
                color = node.backgroundColor?.let(::Color) ?: MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, Color(node.strokeColor))) {
                Text(wgAnnotatedText(node.text), Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    color = node.textColor?.let(::Color) ?: MaterialTheme.colorScheme.onSurface,
                    fontSize = node.textSizeSp?.sp ?: 11.sp)
            }
        }
    }
}

@Composable fun WgConfigRow(b: WgConfigRowState) {
    WgCard(b.interfaceDetailCard) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { WgText(b.interfaceFlagText); WgText(b.interfaceNameText, Modifier.weight(1f)); WgText(b.interfaceIdText) }
                WgChips(b.protocolInfoChipGroup, b.protocolInfoChipIpv4, b.protocolInfoChipIpv6, b.chipSplitTunnel, b.chipAmnezia, b.chipHopSrc, b.chipHopping, b.chipProperties)
                WgText(b.interfaceStatus)
                WgText(b.interfaceAppsCount)
                if (b.interfaceActiveLayout.isVisible) { WgText(b.interfaceActiveUptime); WgText(b.interfaceActiveRxTx) }
            }
            WgCheck(b.interfaceSwitch, b.interfaceNameText.text.toString(), switch = true)
        }
    }
}

@Composable fun WgOneConfigRow(b: WgOneConfigRowState) {
    WgCard(b.interfaceDetailCard) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { WgText(b.interfaceFlagText); WgText(b.interfaceNameText, Modifier.weight(1f)); WgText(b.interfaceIdText) }
                WgChips(b.protocolInfoChipGroup, b.protocolInfoChipIpv4, b.protocolInfoChipIpv6, b.chipSplitTunnel)
                WgText(b.interfaceStatus)
                WgText(b.interfaceAppsCount)
                if (b.interfaceActiveLayout.isVisible) { WgText(b.interfaceActiveUptime); WgText(b.interfaceActiveRxTx) }
            }
            WgCheck(b.oneWgCheck, b.interfaceNameText.text.toString())
        }
    }
}

@Composable fun WgAppRow(b: WgAppRowState) {
    WgCard(b.wgIncludeCard) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WgIcon(b.wgIncludeAppListApkIconIv)
            Column(Modifier.weight(1f)) { WgText(b.wgIncludeAppListApkLabelTv); WgText(b.wgIncludeAppAppDescTv) }
            WgCheck(b.wgIncludeAppListCheckbox, b.wgIncludeAppListApkLabelTv.text.toString())
        }
    }
}

@Composable fun WgHopRow(b: WgHopRowState) {
    WgCard(b.wgHopListCard) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WgText(b.wgHopListNameTv); WgText(b.wgHopListDescTv)
                WgChips(b.chipGroup, b.chipIpv4, b.chipIpv6, b.chipSplitTunnel, b.chipAmnezia, b.chipHopSrc, b.chipHopping, b.chipProperties)
            }
            WgCheck(b.wgHopListCheckbox, b.wgHopListNameTv.text.toString())
        }
        if (b.wgHopListProgress.isVisible) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}

@Composable fun WgPeerRow(b: WgPeerRowState) {
    WgCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WgText(b.peerTitle, Modifier.weight(1f))
            WgIcon(b.peerEdit, stringResource(R.string.rt_edit_dialog_positive))
            WgIcon(b.peerDelete, stringResource(R.string.lbl_delete))
        }
        WgText(b.publicKeyLabel); WgText(b.publicKeyText)
        WgText(b.preSharedKeyLabel); WgText(b.preSharedKeyText)
        WgText(b.allowedIpsLabel); WgText(b.allowedIpsText)
        WgText(b.endpointLabel); WgText(b.endpointText)
        WgText(b.persistentKeepaliveLabel); WgText(b.persistentKeepaliveText)
    }
}

@Composable fun WgStatsRow(b: WgStatsRowState) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WgIcon(b.ssIcon); WgText(b.ssFlag)
            Column(Modifier.weight(1f)) { WgText(b.ssName); WgText(b.ssDataUsage) }
            WgText(b.ssCount)
        }
        WgProgress(b.ssProgress, Modifier.fillMaxWidth())
    }
}
