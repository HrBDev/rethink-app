/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.celzero.bravedns.ui.compose.wireguard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R

@Composable fun WgIncludeAppsContent(b: WgIncludeAppsState, onSubmit: (String) -> Boolean) {
    val requester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            WgText(b.wgIncludeAppDialogHeading, Modifier.padding(16.dp))
            Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(b.wgIncludeAppDialogSearchView.text.toString(), { b.wgIncludeAppDialogSearchView.setText(it) },
                    Modifier.weight(1f).focusRequester(requester), singleLine = true,
                    placeholder = { Text(stringResource(R.string.search_proxy_add_apps)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        val query = b.wgIncludeAppDialogSearchView.text.toString()
                        // SearchView uses String.trim(), which trims ASCII <= U+0020.
                        if (query.trim { it <= ' ' }.isNotEmpty()) { onSubmit(query); keyboard?.hide() }
                    }), trailingIcon = {
                        if (b.wgIncludeAppDialogSearchView.text.isNotEmpty()) IconButton({
                            b.wgIncludeAppDialogSearchView.setText("")
                            requester.requestFocus(); keyboard?.show()
                        }) { Icon(painterResource(android.R.drawable.ic_menu_close_clear_cancel), stringResource(R.string.fapps_filter_clear_btn)) }
                    })
                if (b.wgRefreshList.isIndeterminate) CircularProgressIndicator(Modifier.padding(12.dp).size(24.dp))
                else WgIcon(b.wgRefreshList, stringResource(R.string.wg_compose_refresh))
            }
            FlowRow(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                b.filters.forEach { chip -> FilterChip(chip.isChecked, { chip.isChecked = true }, label = { Text(chip.text.toString()) }) }
            }
            FlowRow(Modifier.padding(horizontal = 12.dp)) {
                listOf(b.wgIncludeAppBulkCheck, b.wgIncludeAppDeselectAllCheck).forEach { node ->
                    Row(verticalAlignment = Alignment.CenterVertically) { WgCheck(node, node.text.toString()); WgText(node) }
                }
            }
            b.wgIncludeAppRecyclerViewDialog.adapter?.Content(Modifier.weight(1f))
        }
    }
}
