/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.wireguard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R

@Composable fun WgToggle(node: WgControlState, modifier: Modifier = Modifier) {
    if (!node.isVisible) return
    Button({ node.performClick() }, modifier, enabled = node.isEnabled,
        colors = ButtonDefaults.buttonColors(containerColor = node.backgroundTintList?.defaultColor?.let(::Color) ?: MaterialTheme.colorScheme.surfaceContainer,
            contentColor = node.textColor?.let(::Color) ?: MaterialTheme.colorScheme.onSurface)) { Text(node.text.toString()) }
}

@Composable fun WgMainContent(b: WgMainState) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center) { WgToggle(b.oneWgToggleBtn); WgToggle(b.wgGeneralToggleBtn) }
                WgText(b.wgWireguardDisclaimer, Modifier.padding(horizontal = 16.dp))
                if (b.wgGeneralInterfaceList.isVisible) b.wgGeneralInterfaceList.adapter?.Content(Modifier.weight(1f))
                if (b.oneWgInterfaceList.isVisible) b.oneWgInterfaceList.adapter?.Content(Modifier.weight(1f))
            }
            if (b.wgEmptyView.isVisible) Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                WgText(b.wgNoConfigText)
                Image(painterResource(R.drawable.illustrations_no_record), null, Modifier.sizeIn(maxWidth = 280.dp, maxHeight = 280.dp))
            }
            Column(Modifier.align(Alignment.BottomEnd).padding(16.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(b.deleteAllFab, b.qrCodeFab, b.importFab, b.createFab).forEach { node ->
                    if (node.isVisible) ExtendedFloatingActionButton({ node.performClick() }) { Text(node.text.toString()) }
                }
                FloatingActionButton({ b.wgAddFab.performClick() }, Modifier.testTag("wg_add_fab")) {
                    Icon(painterResource(R.drawable.ic_fab_without_border), stringResource(R.string.lbl_add))
                }
            }
        }
    }
}
