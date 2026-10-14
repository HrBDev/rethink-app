/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.celzero.bravedns.ui.compose.wireguard

import androidx.compose.ui.graphics.Color
import com.celzero.bravedns.ui.compose.fields.OriginalTextField
import com.celzero.bravedns.ui.compose.fields.OriginalFieldStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.data.SsidItem
import com.celzero.bravedns.ui.compose.themeColor
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class WgSsidPresenter(private val items: MutableList<SsidItem>, val onDelete: (SsidItem) -> Unit) {
    private var revision by mutableIntStateOf(0)
    fun notifyDataSetChanged() { revision++ }
    fun getSsidItems(): List<SsidItem> { revision; return items.toList() }
    fun addSsidItem(item: SsidItem) {
        if (items.none { it.name == item.name && it.type == item.type }) { items.add(item); revision++ }
    }
    fun removeSsidItem(item: SsidItem) {
        val index = items.indexOf(item)
        if (index != -1) { items.removeAt(index); revision++ }
    }
}

@Composable private fun WgRadio(node: WgControlState) {
    Row(Modifier.clickable(enabled = node.isEnabled && node.isClickable) { node.isChecked = true }, verticalAlignment = Alignment.CenterVertically) {
        RadioButton(node.isChecked, { node.isChecked = true }, enabled = node.isEnabled && node.isClickable)
        WgText(node)
    }
}

@Composable fun WgSsidContent(b: WgSsidState, presenter: WgSsidPresenter?) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val done = b.ssidEditText.onEditorDone
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.background) {
        Column(Modifier.safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.wg_setting_ssid_title), style = MaterialTheme.typography.titleLarge)
            WgText(b.descriptionTextView)
            if (b.ssidRecyclerView.isVisible) presenter?.getSsidItems()?.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(item.name); Text(item.type.getDisplayName(context), color = themeColor(R.attr.accentBad)) }
                    IconButton({ presenter.onDelete(item) }) { Icon(painterResource(R.drawable.ic_delete), stringResource(R.string.lbl_delete), tint = Color.Unspecified) }
                }
            }
            WgText(b.emptyStateView)
            WgText(b.ssidConditionHeading)
            FlowRow { WgRadio(b.radioEqual); WgRadio(b.radioNotEqual) }
            Text(stringResource(R.string.lbl_criteria))
            FlowRow { WgRadio(b.radioExact); WgRadio(b.radioWildcard) }
            WgField(b.ssidEditText, b.ssidTextInputLayout.hint, fieldStyle = OriginalFieldStyle.WgSsid, onDone = { keyboard?.hide(); scope.launch { yield(); focus.clearFocus(); done?.invoke() } })
            WgAction(b.addSsidBtn, source = "dialog_wg_ssid:addSsidBtn")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { WgAction(b.cancelBtn, source = "dialog_wg_ssid:cancelBtn"); WgAction(b.saveBtn, source = "dialog_wg_ssid:saveBtn") }
        }
    }
}
