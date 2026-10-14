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

@Composable private fun WgChips(group: WgControlState, source: String, vararg nodes: WgControlState) {
    if (!group.isVisible) return
    val ids = if (source == "list_item_wg_hop") listOf("chip_ipv4", "chip_ipv6", "chip_split_tunnel", "chip_amnezia", "chip_hop_src", "chip_hopping", "chip_properties")
        else listOf("protocol_info_chip_ipv4", "protocol_info_chip_ipv6", "chip_split_tunnel", "chip_amnezia", "chip_hop_src", "chip_hopping", "chip_properties")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        nodes.forEachIndexed { index, node -> if (node.isVisible) {
            com.celzero.bravedns.ui.compose.controls.OriginalLabel("$source:${ids[index]}", Modifier.alpha(node.alpha),
                selected = node.isChecked, contentColor = node.textColor?.let(::Color),
                containerColor = node.backgroundColor?.let(::Color), backgroundTint = node.backgroundTintList) {
                Text(wgAnnotatedText(node.text))
            }
        } }
    }
}

@Composable fun WgConfigRow(b: WgConfigRowState) {
    WgCard(b.interfaceDetailCard) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { WgText(b.interfaceFlagText); WgText(b.interfaceNameText, Modifier.weight(1f)); WgText(b.interfaceIdText) }
                WgChips(b.protocolInfoChipGroup, "list_item_wg_general_interface", b.protocolInfoChipIpv4, b.protocolInfoChipIpv6, b.chipSplitTunnel, b.chipAmnezia, b.chipHopSrc, b.chipHopping, b.chipProperties)
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
                WgChips(b.protocolInfoChipGroup, "list_item_wg_one_interface", b.protocolInfoChipIpv4, b.protocolInfoChipIpv6, b.chipSplitTunnel)
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
                WgChips(b.chipGroup, "list_item_wg_hop", b.chipIpv4, b.chipIpv6, b.chipSplitTunnel, b.chipAmnezia, b.chipHopSrc, b.chipHopping, b.chipProperties)
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
