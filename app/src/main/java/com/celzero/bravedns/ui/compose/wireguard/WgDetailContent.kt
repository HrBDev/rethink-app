/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.celzero.bravedns.ui.compose.wireguard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R

@Composable private fun WgSetting(row: WgControlState, title: WgControlState, description: String, check: WgControlState) {
    if (!row.isVisible) return
    Row(Modifier.fillMaxWidth().clickable(enabled = row.isEnabled && row.isClickable) { row.performClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { WgText(title); Text(description, style = MaterialTheme.typography.bodyMedium) }
        WgCheck(check, title.text.toString(), switch = true)
    }
}

@Composable private fun WgDetailAction(node: WgControlState, icon: Int, label: @Composable () -> Unit) {
    if (!node.isVisible) return
    Column(Modifier.clickable(enabled = node.isEnabled && node.isClickable) { node.performClick() }.alpha(node.alpha).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(painterResource(icon), null, Modifier.size(28.dp)); label()
    }
}

@Composable private fun WgClientShimmer() {
    val transition = rememberInfiniteTransition(label = "client_info_loading")
    val alpha by transition.animateFloat(.35f, .75f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "client_info_alpha")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(110.dp, 220.dp, 160.dp).forEach { width -> Box(Modifier.widthIn(max = width).fillMaxWidth().height(10.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))) }
    }
}

@Composable fun WgDetailContent(b: WgDetailState) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).padding(bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                WgCard(b.interfaceDetailCard) {
                    Row(verticalAlignment = Alignment.CenterVertically) { WgText(b.interfaceFlagText); WgText(b.configNameText, Modifier.weight(1f)); WgText(b.configIdText) }
                    WgText(b.statusText)
                    WgText(b.publicKeyLabel); WgText(b.publicKeyText)
                    WgText(b.addressesLabel); WgText(b.addressesText)
                    WgText(b.dnsServersLabel); WgText(b.dnsServersText)
                    WgText(b.dnsSearchDomainsLabel); WgText(b.dnsSearchDomainsText)
                    WgText(b.listenPortLabel); WgText(b.listenPortText)
                    WgText(b.mtuLabel); WgText(b.mtuText)
                    if (b.rowClientInfo.isVisible) {
                        WgText(b.labelClientInfo)
                        if (b.shimmerClientInfo.isVisible) WgClientShimmer()
                        WgText(b.valueIpv4); WgText(b.valueIpv6)
                    }
                    FlowRow(horizontalArrangement = Arrangement.End) { WgAction(b.editBtn); WgAction(b.deleteBtn) }
                }
                WgCard(b.actionsCard) {
                    Text(stringResource(R.string.lbl_actions))
                    FlowRow {
                        WgDetailAction(b.applicationsBtnX, R.drawable.ic_app_info_accent) { WgText(b.appsLabel) }
                        WgDetailAction(b.hopBtnX, R.drawable.ic_network_tunnel) {
                            Row(verticalAlignment = Alignment.CenterVertically) { WgText(b.hopLabel); if (b.hopBadge.isVisible) Badge() }
                        }
                        WgDetailAction(b.logsBtnX, R.drawable.ic_logs) { Text("Logs") }
                    }
                }
                WgCard(b.otherSettingsCard) {
                    WgText(b.wgHeaderTv)
                    WgSetting(b.lockdownRl, b.lockdownTitleTv, stringResource(R.string.lockdown_wg_dialog_desc), b.lockdownCheck)
                    WgSetting(b.catchAllRl, b.catchAllTitleTv, stringResource(R.string.catch_all_wg_dialog_desc), b.catchAllCheck)
                    WgText(b.oneWgInfoTv)
                }
                WgCard(b.mobileSsidSettingsCard) {
                    WgText(b.msWgHeaderTv)
                    WgSetting(b.useMobileRl, b.useMobileTitleTv, stringResource(R.string.wg_setting_use_on_mobile_desc), b.useMobileCheck)
                    WgSetting(b.ssidFilterRl, b.ssidTitleTv, b.ssidDescTv.text.toString(), b.ssidCheck)
                    if (b.ssidEditGroup.isVisible) Row {
                        WgField(b.ssidEditText, stringResource(R.string.lbl_ssid), Modifier.weight(1f))
                        WgIcon(b.ssidOkBtn, stringResource(R.string.lbl_save))
                    }
                    if (b.ssidDisplayGroup.isVisible) Row(verticalAlignment = Alignment.CenterVertically) {
                        WgText(b.ssidValueTv, Modifier.weight(1f)); WgIcon(b.ssidEditBtn, stringResource(R.string.rt_edit_dialog_positive))
                    }
                    if (b.ssidPermissionErrorLayout.isVisible) { WgText(b.ssidPermissionErrorText); WgAction(b.ssidPermissionErrorAction) }
                    if (b.ssidLocationErrorLayout.isVisible) { WgText(b.ssidLocationErrorText); WgAction(b.ssidLocationErrorAction) }
                }
                b.peersList.adapter?.Content(Modifier.fillMaxWidth())
            }
            ExtendedFloatingActionButton({ b.addPeerFab.performClick() }, Modifier.align(Alignment.BottomEnd).padding(16.dp)) { Text(b.addPeerFab.text.toString()) }
        }
    }
}
