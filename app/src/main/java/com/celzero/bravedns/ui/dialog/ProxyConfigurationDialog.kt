/*
 * Copyright 2026 RethinkDNS and its authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy at https://www.apache.org/licenses/LICENSE-2.0
 */
package com.celzero.bravedns.ui.dialog

import android.util.TypedValue
import androidx.appcompat.R.attr
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.celzero.bravedns.R
import com.google.android.material.color.MaterialColors

internal data class ProxyDialogValues(
    val appName: String,
    val host: String,
    val port: String = "",
    val userName: String = "",
    val password: String = "",
    val blockUdp: Boolean = false,
    val includeApps: Boolean,
)

internal fun filterProxyApps(appNames: List<String>, query: String): List<String> =
    appNames.filter { it.contains(query, ignoreCase = true) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProxyConfigurationDialog(
    socks5: Boolean,
    appNames: List<String>,
    initial: ProxyDialogValues,
    appSelectionLocked: Boolean,
    vpnLockdown: Boolean,
    onBlockUdpChanged: (Boolean) -> Unit,
    onIncludeAppsChanged: (Boolean) -> Unit,
    onApply: (ProxyDialogValues) -> Int?,
    onCancel: () -> Unit,
    onOpenVpnSettings: () -> Unit,
) {
    val context = LocalContext.current
    val titleFontSize = with(LocalDensity.current) {
        dimensionResource(R.dimen.large_font_text_view).toSp()
    }
    val colors = remember(context) {
        fun color(attr: Int) = Color(MaterialColors.getColor(context, attr, "Proxy dialog"))
        val themeValue = TypedValue()
        val isLightTheme = if (context.theme.resolveAttribute(attr.isLightTheme, themeValue, true)) {
            themeValue.data != 0
        } else {
            true
        }
        val base = if (isLightTheme) lightColorScheme() else darkColorScheme()
        base.copy(
            primary = color(attr.colorPrimary),
            onPrimary = color(com.google.android.material.R.attr.colorOnPrimary),
            surface = color(com.google.android.material.R.attr.colorSurface),
            surfaceContainer = color(com.google.android.material.R.attr.colorSurfaceContainer),
            surfaceTint = color(attr.colorPrimary),
            onSurface = color(com.google.android.material.R.attr.colorOnSurface),
            onSurfaceVariant = color(com.google.android.material.R.attr.colorOnSurfaceVariant),
            surfaceVariant = color(com.google.android.material.R.attr.colorSurfaceVariant),
            error = color(attr.colorError),
            outline = color(com.google.android.material.R.attr.colorOutline),
        )
    }
    // Credentials remain in memory only, never in saved instance state.
    var values by remember { mutableStateOf(if (appSelectionLocked) initial.copy(appName = appNames.first()) else initial) }
    var appInput by remember { mutableStateOf(TextFieldValue(values.appName)) }
    var appFocused by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var showAllApps by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<Int?>(null) }
    val appQuery = appInput.text
    val appValid = appQuery in appNames
    val suggestions = remember(appNames, appQuery, showAllApps) {
        if (showAllApps) appNames else filterProxyApps(appNames, appQuery)
    }
    LaunchedEffect(appFocused) {
        if (appFocused && !appSelectionLocked) {
            appInput = appInput.copy(selection = TextRange(0, appInput.text.length))
        }
    }

    MaterialTheme(colorScheme = colors) {
        AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
            title = {
                Text(
                    stringResource(if (socks5) R.string.settings_dns_proxy_dialog_header else R.string.http_proxy_dialog_heading),
                    fontSize = titleFontSize,
                )
            },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (!socks5) Text(stringResource(R.string.http_proxy_dialog_desc))
                    if (vpnLockdown) {
                        TextButton(onClick = onOpenVpnSettings) {
                            Text(stringResource(R.string.settings_lock_down_mode_desc))
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = expanded && !appSelectionLocked,
                        onExpandedChange = {
                            if (!appSelectionLocked) {
                                showAllApps = true
                                expanded = it
                            }
                        },
                    ) {
                        OutlinedTextField(
                            value = appInput,
                            onValueChange = {
                                if (it.text != appInput.text) {
                                    showAllApps = false
                                    expanded = true
                                    error = null
                                }
                                appInput = it
                            },
                            label = { Text(stringResource(R.string.settings_dns_proxy_dialog_app)) },
                            shape = MaterialTheme.shapes.large,
                            enabled = !appSelectionLocked,
                            singleLine = true,
                            isError = !appValid,
                            supportingText = {
                                when {
                                    appSelectionLocked -> Text(stringResource(R.string.lockdown_check_setting_disabled))
                                    !appValid -> Text(stringResource(R.string.proxy_app_select_from_list))
                                }
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expanded,
                                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable, enabled = !appSelectionLocked),
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { appFocused = it.isFocused }
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = !appSelectionLocked),
                        )
                        ExposedDropdownMenu(
                            expanded = expanded && !appSelectionLocked && suggestions.isNotEmpty(),
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.heightIn(max = 240.dp),
                        ) {
                            suggestions.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        appInput = TextFieldValue(name, selection = TextRange(name.length))
                                        values = values.copy(appName = name)
                                        expanded = false
                                        showAllApps = true
                                        error = null
                                    },
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = values.host,
                        onValueChange = { values = values.copy(host = it); error = null },
                        label = { Text(stringResource(R.string.settings_dns_proxy_dialog_hostname)) },
                        shape = MaterialTheme.shapes.large,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            imeAction = if (socks5) ImeAction.Next else ImeAction.Done,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (socks5) {
                        OutlinedTextField(
                            value = values.port,
                            onValueChange = {
                                values = values.copy(port = it.filter { character -> character in '0'..'9' })
                                error = null
                            },
                            label = { Text(stringResource(R.string.settings_dns_proxy_dialog_port_number)) },
                            shape = MaterialTheme.shapes.large,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = values.userName,
                            onValueChange = { values = values.copy(userName = it) },
                            label = { Text(stringResource(R.string.settings_socks5_dialog_hint_username)) },
                            shape = MaterialTheme.shapes.large,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = values.password,
                            onValueChange = { values = values.copy(password = it) },
                            label = { Text(stringResource(R.string.settings_socks5_dialog_hint_password)) },
                            shape = MaterialTheme.shapes.large,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        ProxyCheckbox(
                            values.blockUdp,
                            {
                                values = values.copy(blockUdp = it)
                                onBlockUdpChanged(it)
                            },
                            stringResource(R.string.univ_firewall_rule_4),
                        )
                    }
                    ProxyCheckbox(
                        values.includeApps,
                        {
                            values = values.copy(includeApps = it)
                            onIncludeAppsChanged(it)
                        },
                        stringResource(R.string.settings_exclude_proxy_apps_heading),
                        enabled = !vpnLockdown,
                    )
                    Text(stringResource(R.string.settings_exclude_proxy_apps_desc), style = MaterialTheme.typography.bodySmall)
                    error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                }
            },
            dismissButton = {
                TextButton(onClick = onCancel) { Text(stringResource(R.string.lbl_cancel)) }
            },
            confirmButton = {
                TextButton(
                    enabled = appValid,
                    onClick = { error = onApply(values.copy(appName = appQuery)) },
                ) { Text(stringResource(R.string.settings_dns_proxy_dialog_positive_btn)) }
            },
        )
    }
}

@Composable
private fun ProxyCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String, enabled: Boolean = true) {
    Row(
        Modifier.fillMaxWidth().toggleable(checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f).padding(end = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f))
        Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}
