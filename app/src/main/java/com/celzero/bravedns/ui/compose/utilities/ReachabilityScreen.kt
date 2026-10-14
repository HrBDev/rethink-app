/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.utilities
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.themeColor

@Composable fun ReachabilityScreen(ui: ReachabilityUiState) {
    Surface(color = themeColor(R.attr.background)) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReachabilityMode(ui.autoToggleBtn, Modifier.weight(1f))
                ReachabilityMode(ui.manualToggleBtn, Modifier.weight(1f))
            }
            Text(stringResource(R.string.bypasses_network_restrictions), Modifier.padding(5.dp), style = utilityTextStyle(size = 14f), color = themeColor(R.attr.primaryLightColorText))
            Row(verticalAlignment = Alignment.CenterVertically) {
                UtilityImage(ui.protocolV4, Modifier.size(16.dp), stringResource(R.string.settings_ip_text_ipv4))
                Text(stringResource(R.string.settings_ip_text_ipv4), Modifier.weight(1f).padding(8.dp).semantics { heading() }, style = utilityTextStyle(size = 18f), color = themeColor(R.attr.secondaryTextColor))
                UtilityButton(ui.resetChip)
            }
            ProbeField(ui.ipv4Address1, ui.progressIpv41, ui.statusIpv41, "IPv4 1")
            ProbeField(ui.ipv4Address2, ui.progressIpv42, ui.statusIpv42, "IPv4 2")
            if (ui.urlV4Layout1.isVisible) ProbeField(ui.urlV4Address1, ui.progressUrlV41, ui.statusUrlV41, "URL IPv4 1")
            if (ui.urlV4Layout2.isVisible) ProbeField(ui.urlV4Address2, ui.progressUrlV42, ui.statusUrlV42, "URL IPv4 2")
            Row(verticalAlignment = Alignment.CenterVertically) {
                UtilityImage(ui.protocolV6, Modifier.size(16.dp), stringResource(R.string.settings_ip_text_ipv6))
                Text(stringResource(R.string.settings_ip_text_ipv6), Modifier.padding(8.dp).semantics { heading() }, style = utilityTextStyle(size = 18f), color = themeColor(R.attr.secondaryTextColor))
            }
            ProbeField(ui.ipv6Address1, ui.progressIpv61, ui.statusIpv61, "IPv6 1")
            ProbeField(ui.ipv6Address2, ui.progressIpv62, ui.statusIpv62, "IPv6 2")
            if (ui.urlV6Layout1.isVisible) ProbeField(ui.urlV6Address1, ui.progressUrlV61, ui.statusUrlV61, "URL IPv6 1")
            if (ui.urlV6Layout2.isVisible) ProbeField(ui.urlV6Address2, ui.progressUrlV62, ui.statusUrlV62, "URL IPv6 2")
            UtilityText(ui.errorMessage, Modifier.padding(5.dp).semantics { liveRegion = LiveRegionMode.Polite })
            Spacer(Modifier.height(25.dp))
            Row(Modifier.fillMaxWidth()) { UtilityButton(ui.saveButton, Modifier.weight(1f)); UtilityButton(ui.testButton, Modifier.weight(1f)) }
        }
    }
}
@Composable private fun ReachabilityMode(control: UtilityControl, modifier: Modifier) {
    Button({ control.performClick() }, modifier = modifier.testTag(control.tag), colors = ButtonDefaults.buttonColors(
        containerColor = control.backgroundTintList?.defaultColor?.let(::Color) ?: themeColor(R.attr.background),
        contentColor = control.textColor?.let(::Color) ?: themeColor(R.attr.primaryTextColor))) {
        Text(control.text.toString(), style = utilityTextStyle(size = control.fontSize))
    }
}
@Composable private fun ProbeField(field: UtilityControl, progress: UtilityControl, status: UtilityControl, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        OutlinedTextField(field.text.toString(), { field.text = it }, Modifier.weight(1f).testTag(field.tag).semantics { contentDescription = label }
            .drawBehind { field.background?.let { background -> drawIntoCanvas { canvas -> background.setBounds(0, 0, size.width.toInt(), size.height.toInt()); background.draw(canvas.nativeCanvas) } } },
            enabled = field.isEnabled, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri), textStyle = utilityTextStyle())
        if (progress.isVisible) CircularProgressIndicator(Modifier.padding(start = 8.dp).size(24.dp).testTag(progress.tag))
        UtilityImage(status, Modifier.padding(start = 8.dp).size(16.dp), stringResource(R.string.lbl_status))
    }
}
