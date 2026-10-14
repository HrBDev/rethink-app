/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.dns

import com.celzero.bravedns.ui.compose.controls.*
import com.celzero.bravedns.ui.compose.fields.OriginalSpinner
import com.celzero.bravedns.ui.compose.fields.OriginalTextField
import com.celzero.bravedns.ui.compose.fields.OriginalFieldStyle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.NoDimDialogWindow
import com.celzero.bravedns.ui.compose.themeColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal enum class DnsServerKind { DOH, DOT, ODOH }

/** Transient dialog state, deliberately not saved across Activity recreation. */
@Composable internal fun DnsServerEditor(
    kind: DnsServerKind, suggestedName: suspend () -> String,
    onApply: (String, String, String, String, Boolean) -> String?, onDismiss: () -> Unit
) {
    val defaultName = stringResource(when(kind) {
        DnsServerKind.DOH -> R.string.cd_custom_doh_url_name_default
        DnsServerKind.DOT -> R.string.lbl_dot
        DnsServerKind.ODOH -> R.string.lbl_odoh
    })
    var name by remember { mutableStateOf(defaultName) }
    var url by remember { mutableStateOf(if (kind == DnsServerKind.DOT) "" else "https://") }
    var ip by remember { mutableStateOf("") }
    var proxy by remember { mutableStateOf("") }
    var insecure by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { name = withContext(Dispatchers.IO) { suggestedName() } }
    val title = if (kind == DnsServerKind.DOH) stringResource(R.string.cd_doh_dialog_heading) else
        stringResource(R.string.two_argument_space, stringResource(R.string.lbl_add).replaceFirstChar(Char::titlecase), defaultName)
    DnsEditorDialog(title, onDismiss, { error = onApply(name, url, ip.trim(), proxy, !insecure) }) {
        DnsField(name, { name = it }, stringResource(R.string.cd_doh_dialog_resolver_name), "dns_name")
        if (kind == DnsServerKind.ODOH) DnsField(proxy, { proxy = it }, stringResource(R.string.settings_proxy_header).replaceFirstChar(Char::uppercase) + stringResource(R.string.lbl_optional), "dns_proxy", KeyboardType.Uri)
        DnsField(url, { url = it }, stringResource(R.string.cd_doh_dialog_resolver_url), "dns_url", KeyboardType.Uri)
        if (kind != DnsServerKind.ODOH) {
            // The original DoT form showed this field too, but did not persist its value.
            DnsField(ip, { ip = it }, stringResource(R.string.cd_doh_dialog_resolver_ip), "dns_ip")
            DnsCheck(stringResource(R.string.cd_doh_dialog_checkbox_desc), insecure, { insecure = it }, "dns_insecure")
        }
        DnsError(error)
    }
}

@Composable internal fun DnsCryptEditor(nextIndices: suspend () -> Pair<Int, Int>, onApply: (String, String, String, Boolean) -> String?, onDismiss: () -> Unit) {
    val resources = LocalResources.current
    val defaultName = stringResource(R.string.cd_dns_crypt_name_default)
    var name by remember { mutableStateOf(defaultName) }
    var stamp by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var relay by remember { mutableStateOf(false) }
    var serverIndex by remember { mutableIntStateOf(0) }
    var relayIndex by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val indices = withContext(Dispatchers.IO) { nextIndices() }
        serverIndex = indices.first; relayIndex = indices.second
        name = resources.getString(R.string.cd_dns_crypt_name, serverIndex.toString())
    }
    DnsEditorDialog(stringResource(R.string.cd_dns_crypt_dialog_heading), onDismiss, { error = onApply(name, stamp, description, relay) }) {
        Column(Modifier.selectableGroup()) {
            DnsRadio(stringResource(R.string.cd_dns_crypt_resolver_heading), !relay) { relay = false; name = resources.getString(R.string.cd_dns_crypt_name, serverIndex.toString()) }
            DnsRadio(stringResource(R.string.cd_dns_crypt_relay_heading), relay) { relay = true; name = resources.getString(R.string.cd_dns_crypt_relay_name, relayIndex.toString()) }
        }
        DnsField(name, { name = it }, stringResource(R.string.cd_dns_crypt_dialog_name), "dns_name")
        DnsField(stamp, { stamp = it }, stringResource(R.string.cd_dns_crypt_dialog_stamp), "dns_stamp", singleLine = false)
        DnsField(description, { description = it }, stringResource(R.string.cd_dns_crypt_dialog_desc), "dns_description", singleLine = false)
        DnsError(error)
    }
}

@Composable internal fun DnsProxyEditor(
    appNames: List<String>, initialName: String, initialForwardApps: Boolean, lockdown: Boolean,
    onVpnSettings: () -> Unit, onApply: (String, String, String, String, Boolean) -> String?, onDismiss: () -> Unit
) {
    require(appNames.isNotEmpty()) { "DNS proxy app choices include the default-app sentinel" }
    var name by remember { mutableStateOf(initialName) }
    var appIndex by remember { mutableIntStateOf(0) }
    val defaultIp = stringResource(R.string.cd_custom_dns_proxy_default_ip)
    var ip by remember { mutableStateOf(defaultIp) }
    var port by remember { mutableStateOf("") }
    var forwardApps by remember { mutableStateOf(initialForwardApps) }
    var showApps by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    DnsEditorDialog(stringResource(R.string.dns_proxy_dialog_header_dns), onDismiss, { error = onApply(name, appNames[appIndex], ip, port, forwardApps) }) {
        if (lockdown) OriginalButton("dialog_set_dns_proxy:dialog_dns_proxy_lockdown_desc", onVpnSettings) { Text(stringResource(R.string.settings_lock_down_mode_desc)) }
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.settings_dns_proxy_dialog_app), Modifier.weight(.3f),
                style = MaterialTheme.typography.bodyLarge, color = themeColor(R.attr.primaryTextColor))
            OriginalSpinner(appNames[appIndex], { showApps = true }, Modifier.weight(.7f).testTag("dns_app"))
        }
        DnsField(name, { name = it }, stringResource(R.string.dns_proxy_name), "dns_name", fieldStyle = OriginalFieldStyle.DnsProxy)
        DnsField(ip, { ip = it }, stringResource(R.string.dns_proxy_ip_address), "dns_ip", fieldStyle = OriginalFieldStyle.DnsProxy)
        DnsField(port, { port = it }, stringResource(R.string.dns_proxy_port), "dns_port", KeyboardType.Number, fieldStyle = OriginalFieldStyle.DnsProxy)
        DnsError(error)
        DnsCheck(stringResource(R.string.settings_exclude_proxy_apps_heading), forwardApps, { forwardApps = it }, "dns_forward_apps", !lockdown)
    }
    if (showApps) AlertDialog(onDismissRequest = { showApps = false },
        title = { Text(stringResource(R.string.settings_dns_proxy_dialog_app)) },
        text = { LazyColumn(Modifier.fillMaxWidth().heightIn(max = 400.dp).selectableGroup()) {
            itemsIndexed(appNames) { index, label -> DnsRadio(label, index == appIndex) { appIndex = index; showApps = false } }
        } }, confirmButton = { OriginalButton("@dialog_positive", { showApps = false }) { Text(stringResource(R.string.lbl_cancel)) } })
}

@Composable private fun DnsEditorDialog(title: String, onDismiss: () -> Unit, onApply: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        NoDimDialogWindow()
        Surface(Modifier.imePadding().padding(16.dp).widthIn(max = 600.dp).fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text(title, Modifier.padding(bottom = 12.dp), style = MaterialTheme.typography.titleMedium)
                Column(Modifier.weight(1f, false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OriginalButton("@dialog_negative", onDismiss, Modifier.testTag("dns_editor_cancel")) { Text(stringResource(R.string.lbl_cancel), color = themeColor(R.attr.accentGood)) }
                    OriginalButton("@dialog_positive", onApply, Modifier.testTag("dns_editor_add")) { Text(stringResource(R.string.lbl_add), color = themeColor(R.attr.accentGood)) }
                }
            }
        }
    }
}
@Composable private fun DnsField(value: String, onValue: (String) -> Unit, label: String, tag: String, keyboard: KeyboardType = KeyboardType.Text, singleLine: Boolean = true, fieldStyle: OriginalFieldStyle = OriginalFieldStyle.Dns) {
    val editor: @Composable (Modifier) -> Unit = { modifier ->
        OriginalTextField(value, onValue, modifier.testTag(tag), style = fieldStyle, label = { Text(label) }, singleLine = singleLine, maxLines = if (singleLine) 1 else 3, keyboardOptions = KeyboardOptions(keyboardType = keyboard, autoCorrectEnabled = false))
    }
    if (fieldStyle == OriginalFieldStyle.DnsProxy) Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(.3f), style = MaterialTheme.typography.bodyLarge, color = themeColor(R.attr.primaryTextColor))
        editor(Modifier.weight(.7f))
    } else editor(Modifier.fillMaxWidth())
}
@Composable private fun DnsCheck(label: String, checked: Boolean, onCheck: (Boolean) -> Unit, tag: String, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth().testTag(tag).toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheck).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked, null, enabled = enabled)
        Text(label, Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else .5f))
    }
}
@Composable private fun DnsRadio(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().selectable(selected, role = Role.RadioButton, onClick = onClick).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected, null); Text(label, Modifier.padding(start = 8.dp))
    }
}
@Composable private fun DnsError(error: String?) {
    if (!error.isNullOrEmpty()) Text(error, Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
}
