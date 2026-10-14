/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.purchase
import com.celzero.bravedns.ui.compose.controls.OriginalChip
import com.celzero.bravedns.ui.compose.controls.OriginalButton
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.rpn.*
import com.celzero.bravedns.ui.compose.themeColor
internal class HistoryScreenState {
    val tvHeroSubtitle = RpnControlState()
    val tvEntryCount = RpnControlState()
    val shimmer = RpnControlState()
    val emptyState = RpnControlState().apply { isVisible = false }
    val errorState = RpnControlState().apply { isVisible = false }
    val rvHistory = RpnControlState().apply { isVisible = false }
    val rvOrders = RpnControlState().apply { isVisible = false }
    val tvEmptySubtitle = RpnControlState()
    val tvErrorMessage = RpnControlState()
    val btnRetry = RpnControlState()
    val chipEntitlement = RpnControlState()
    val chipPaymentHistory = RpnControlState()
    val dolphinSignature = RpnSignatureState()
    val fetchProgress = RpnControlState().apply { isVisible = false }
    var fetchTarget by mutableFloatStateOf(0f)
    var listEpoch by mutableIntStateOf(0)
    var shimmerRunning by mutableStateOf(false)
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable internal fun HistoryScreen(state: HistoryScreenState, server: Boolean, count: Int, key: (Int) -> Any,
    onBack: () -> Unit, onFetchHidden: () -> Unit, row: @Composable (Int) -> Unit) {
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val alpha = remember { Animatable(1f) }
    val fetchAlpha = remember { Animatable(0f) }
    LaunchedEffect(state.listEpoch) { if (state.listEpoch > 0) { alpha.snapTo(0f); alpha.animateTo(1f, tween(if (server) 280 else 300)) } }
    LaunchedEffect(state.fetchTarget) {
        fetchAlpha.animateTo(state.fetchTarget, tween(if (state.fetchTarget == 1f) 200 else 400))
        if (state.fetchTarget == 0f) onFetchHidden()
    }
    Scaffold(Modifier.fillMaxSize().nestedScroll(scroll.nestedScrollConnection), topBar = {
        Column {
            LargeTopAppBar(modifier = Modifier.rpnHeroBackground(), colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent, scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent), title = {
                Column {
                    Text(stringResource(if (server) R.string.server_order_title else R.string.payment_history_title))
                    if (scroll.state.collapsedFraction < .99f) RpnText(state.tvHeroSubtitle, Modifier.alpha(.7f * (1f - scroll.state.collapsedFraction)), style = MaterialTheme.typography.bodySmall)
                }
            }, expandedHeight = 180.dp, scrollBehavior = scroll)
            if (state.fetchProgress.isVisible) LinearProgressIndicator(Modifier.fillMaxWidth().alpha(fetchAlpha.value))
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(Modifier.fillMaxSize(), state = listState, contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item("summary") {
                    if (server) FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OriginalChip("activity_server_order_history:chip_entitlement", selected = false, onClick = { state.chipEntitlement.performClick() }, selectable = false, label = { Text(stringResource(R.string.entitlement)) })
                        OriginalChip("activity_server_order_history:chip_payment_history", selected = false, onClick = { state.chipPaymentHistory.performClick() }, selectable = false, label = { Text(stringResource(R.string.payment_history_title)) })
                    } else Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = themeColor(R.attr.chipBgColorPositive))) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(painterResource(R.drawable.ic_rethink_plus), null, Modifier.size(36.dp), tint = themeColor(R.attr.accentGood))
                            Text(stringResource(R.string.payment_history_summary_title), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.payment_history_summary_subtitle)); RpnText(state.tvEntryCount)
                        }
                    }
                }
                if (state.shimmer.isVisible) item("loading") { PurchaseShimmer(running = state.shimmerRunning, duration = 1500) }
                if (state.emptyState.isVisible) item("empty") {
                    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(painterResource(if (server) R.drawable.ic_rethink_plus_sparkle else R.drawable.ic_rethink_plus), null, Modifier.size(72.dp).align(Alignment.CenterHorizontally))
                        Text(stringResource(if (server) R.string.server_order_empty_title else R.string.payment_history_empty_title), style = MaterialTheme.typography.titleLarge)
                        Text(if (server && state.tvEmptySubtitle.text.isNotEmpty()) state.tvEmptySubtitle.text.toString() else stringResource(if (server) R.string.server_order_empty_subtitle else R.string.payment_history_empty_subtitle))
                    }
                }
                if (state.errorState.isVisible) item("error") {
                    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(painterResource(if (server) R.drawable.ic_cloud_error else R.drawable.ic_cross), null, Modifier.size(64.dp).align(Alignment.CenterHorizontally).alpha(.7f), tint = themeColor(R.attr.accentBad))
                        RpnText(state.tvErrorMessage, Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                        OriginalButton(if (server) "activity_server_order_history:btn_retry" else "activity_purchase_history:btn_retry", { state.btnRetry.performClick() }, Modifier.testTag("history_retry")) { Text(stringResource(R.string.ping_test_again)) }
                    }
                }
                if ((if (server) state.rvOrders else state.rvHistory).isVisible) items(count, key = key) { index -> Card(Modifier.fillMaxWidth().alpha(alpha.value)) { row(index) } }
            }
            RpnSignature(state.dolphinSignature, Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 12.dp).alpha(.8f))
        }
    }
}
