package com.celzero.bravedns.ui.compose.rpn

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.themeColor
import androidx.cardview.R as CardR

@Composable
fun RpnSettingsContent(ui: RpnSettingsState) {
    Column(Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        Box(Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp, bottom = 10.dp).size(40.dp, 4.dp).alpha(.5f).background(themeColor(R.attr.border)))
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 6.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_settings), null, Modifier.size(48.dp).background(themeColor(R.attr.chipBgColorPositive), RoundedCornerShape(14.dp)).padding(12.dp), tint = themeColor(R.attr.accentGood))
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(stringResource(R.string.title_settings), fontSize = 20.sp)
                Text(stringResource(R.string.server_settings_sheet_subtitle), Modifier.alpha(.6f), fontSize = 12.sp)
            }
            IconButton({ ui.refreshBtn.performClick() }, Modifier.size(38.dp), enabled = ui.refreshBtn.isEnabled && ui.refreshBtn.isClickable) {
                Icon(painterResource(R.drawable.ic_refresh_white), stringResource(R.string.server_settings_refresh_servers), Modifier.size(22.dp).rpnMotion(ui.refreshBtn), tint = themeColor(R.attr.accentGood))
            }
        }
        Column(Modifier.verticalScroll(rememberScrollState())) {
            RpnSectionLabel(R.string.server_settings_dns_section)
            RpnSettingsCard {
                RpnText(ui.tvDnsFilterDesc, Modifier.padding(horizontal = 16.dp, vertical = 12.dp), MaterialTheme.typography.bodySmall)
                if (ui.splitDnsBanner.isVisible) Row(Modifier.padding(12.dp).rpnMotion(ui.splitDnsBanner).background(themeColor(R.attr.chipBgColorNeutral), RoundedCornerShape(10.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.server_settings_split_dns_disabled), Modifier.weight(1f), fontSize = 12.sp)
                    TextButton({ ui.splitDnsEnableBtn.performClick() }) { RpnText(ui.splitDnsEnableBtn) }
                }
                Column(Modifier.rpnMotion(ui.dnsCheckboxContainer)) {
                    RpnCheckRow(R.string.rbl_privacy, ui.dnsRowPrivacy, ui.cbDnsPrivacy)
                    RpnSettingsDivider()
                    RpnCheckRow(R.string.server_settings_dns_family, ui.dnsRowFamily, ui.cbDnsFamily)
                    RpnSettingsDivider()
                    RpnCheckRow(R.string.rbl_security, ui.dnsRowSecurity, ui.cbDnsSecurity)
                }
            }
            RpnSectionLabel(R.string.server_settings_config_handling_section)
            RpnSettingsCard {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RpnText(ui.tvConfigModeHint, Modifier.weight(1f).padding(end = 10.dp), MaterialTheme.typography.bodySmall)
                    listOf(R.id.btn_config_auto to ui.btnConfigAuto, R.id.btn_config_manual to ui.btnConfigManual).forEach { (id, state) ->
                        val selected = ui.configModeToggle.checkedId == id
                        TextButton({ ui.configModeToggle.check(id) }, colors = ButtonDefaults.textButtonColors(containerColor = if (selected) themeColor(R.attr.accentGood) else androidx.compose.ui.graphics.Color.Transparent)) { RpnText(state, style = MaterialTheme.typography.labelMedium) }
                    }
                }
                RpnSettingsDivider()
                RpnSwitchRow(R.string.server_settings_identity_title, R.string.server_settings_identity_desc, ui.identityRow, ui.identitySwitch)
                RpnSettingsDivider()
                Row(Modifier.fillMaxWidth().rpnMotion(ui.portRow).clickable(enabled = ui.portRow.isClickable) { ui.portRow.performClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(stringResource(R.string.server_settings_port_title)); Text(stringResource(R.string.server_settings_port_desc), style = MaterialTheme.typography.bodySmall) }
                    RpnText(ui.tvPortValue)
                    Icon(painterResource(R.drawable.ic_arrow_down_small), null, Modifier.size(16.dp))
                }
                if (ui.permanentConfigRow.isVisible) { RpnSettingsDivider(); RpnSwitchRow(R.string.server_settings_permanent_config_title, R.string.server_settings_permanent_config_desc, ui.permanentConfigRow, ui.permanentConfigSwitch) }
            }
            RpnSectionLabel(R.string.auto_exclude_cc_title)
            RpnSettingsCard {
                Row(Modifier.fillMaxWidth().rpnMotion(ui.excludeCountriesRow).clickable { ui.excludeCountriesRow.performClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_filter), null, Modifier.size(38.dp).padding(8.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(stringResource(R.string.auto_exclude_cc_row_title)); RpnText(ui.tvExcludeCcDesc, style = MaterialTheme.typography.bodySmall) }
                    Icon(painterResource(R.drawable.ic_right_arrow_small), null, Modifier.size(17.dp))
                }
            }
            Button({ ui.btnResetRpn.performClick() }, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(48.dp).rpnMotion(ui.btnResetRpn), enabled = ui.btnResetRpn.isEnabled) { Text(ui.btnResetRpn.text.toString()) }
            Text(stringResource(R.string.rpn_restore_confirm_message), Modifier.padding(horizontal = 20.dp).alpha(.6f), style = MaterialTheme.typography.bodySmall)
            RpnSignature(ui.dolphinSignature, Modifier.fillMaxWidth())
        }
    }
}

@Composable internal fun RpnSectionLabel(resource: Int) { Text(stringResource(resource), Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp), color = themeColor(R.attr.primaryLightColorText), style = MaterialTheme.typography.labelSmall) }
@Composable internal fun RpnSettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clip(RoundedCornerShape(16.dp)).background(themeColor(CardR.attr.cardBackgroundColor)).border(1.dp, themeColor(R.attr.border), RoundedCornerShape(16.dp)), content = content)
}
@Composable internal fun RpnSettingsDivider() { HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = themeColor(R.attr.primaryTextColor).copy(alpha = .08f)) }
@Composable private fun RpnCheckRow(label: Int, row: RpnControlState, checkbox: RpnControlState) {
    Row(Modifier.fillMaxWidth().clickable(enabled = row.isClickable) { row.performClick() }.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(label), Modifier.weight(1f)); Checkbox(checkbox.isChecked, { checkbox.isChecked = it }, enabled = checkbox.isEnabled)
    }
}
@Composable private fun RpnSwitchRow(title: Int, desc: Int, row: RpnControlState, toggle: RpnControlState) {
    Row(Modifier.fillMaxWidth().rpnMotion(row).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(stringResource(title)); Text(stringResource(desc), Modifier.alpha(.6f), style = MaterialTheme.typography.bodySmall) }
        Switch(toggle.isChecked, { toggle.isChecked = it }, enabled = toggle.isEnabled)
    }
}
