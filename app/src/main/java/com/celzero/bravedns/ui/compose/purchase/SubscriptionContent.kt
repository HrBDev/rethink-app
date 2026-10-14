/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.purchase
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.chrome.homeNavigationClearance
import com.celzero.bravedns.ui.compose.rpn.RpnControlState
import com.celzero.bravedns.ui.compose.rpn.RpnText
import com.celzero.bravedns.ui.compose.themeColor

internal class SubscriptionScreenState {
    val btnContactSupport = RpnControlState()
    val btnContactSupportError = RpnControlState()
    val btnOneTime = RpnControlState()
    val btnSubscription = RpnControlState()
    val cancelPolicy = RpnControlState()
    val connectionInfoCard = RpnControlState().apply { isVisible = false }
    val connectionIp = RpnControlState()
    val connectionIsp = RpnControlState()
    val connectionLocation = RpnControlState()
    val ctaContainer = RpnControlState()
    val errorContainer = RpnControlState().apply { isVisible = false }
    val errorIcon = RpnControlState().apply { imageRes = R.drawable.ic_error_state }
    val errorMessage = RpnControlState()
    val errorReason = RpnControlState().apply { isVisible = false }
    val errorTitle = RpnControlState()
    val extendModeBanner = RpnControlState().apply { isVisible = false }
    val ispContainer = RpnControlState().apply { isVisible = false }
    val loadingContainer = RpnControlState().apply { isVisible = false }
    val productTypeToggleContainer = RpnControlState()
    val retryButton = RpnControlState()
    val scrollView = RpnControlState()
    val subscribeButton = RpnControlState()
    val termsText = RpnControlState()
    val vDivider = RpnControlState().apply { isVisible = false }
    var hosted by mutableStateOf(false)
    var shimmerRunning by mutableStateOf(false)
    var oceanRunning by mutableStateOf(false)
    var oceanEpoch by mutableIntStateOf(0)
    var contentEpoch by mutableIntStateOf(0)
    var pulseEpoch by mutableIntStateOf(0)
    var pulseTarget by mutableStateOf<RpnControlState?>(null)
    var moneyBackDays by mutableIntStateOf(7)
    var subscriptionSelected by mutableStateOf(false)
}
@Composable private fun PurchaseButton(state: RpnControlState, label: Int, tag: String, scale: Float = 1f) {
    if (!state.isVisible) return
    Button(onClick = { state.performClick() }, enabled = state.isEnabled,
        colors = ButtonDefaults.buttonColors(containerColor = state.backgroundTintList?.defaultColor?.let(::Color) ?: MaterialTheme.colorScheme.primary,
            contentColor = state.foregroundColor?.let(::Color) ?: MaterialTheme.colorScheme.onPrimary),
        modifier = Modifier.fillMaxWidth().testTag(tag).graphicsLayer { scaleX = scale; scaleY = scale }) {
        Text(if (state.text.isNotEmpty()) state.text.toString() else stringResource(label))
    }
}
@Composable internal fun SubscriptionContent(state: SubscriptionScreenState, plans: @Composable () -> Unit) {
    val ocean = rememberPurchaseOcean(state.oceanRunning, state.oceanEpoch)
    val entrance = remember { Animatable(1f) }
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(state.contentEpoch) { if (state.contentEpoch > 0) { entrance.snapTo(0f); entrance.animateTo(1f, tween(300)) } }
    LaunchedEffect(state.pulseEpoch) {
        if (state.pulseEpoch > 0) {
            pulse.snapTo(1f)
            pulse.animateTo(.95f, tween(50, easing = Easing { android.view.animation.AnticipateOvershootInterpolator().getInterpolation(it) }))
            pulse.animateTo(1f, tween(50, easing = Easing { android.view.animation.AnticipateOvershootInterpolator().getInterpolation(it) }))
        }
    }
    fun scale(control: RpnControlState) = if (state.pulseTarget === control) pulse.value else 1f
    Surface(Modifier.fillMaxSize()) {
        Scaffold(containerColor = themeColor(R.attr.background), bottomBar = {
            if (state.ctaContainer.isVisible) Surface(Modifier.alpha(entrance.value)) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = if (state.hosted) 0.dp else homeNavigationClearance(60.dp)), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RpnText(state.cancelPolicy, style = MaterialTheme.typography.bodySmall)
                    PurchaseButton(state.subscribeButton, R.string.get_rpn, "purchase_subscribe", scale(state.subscribeButton))
                    TextButton({ state.btnContactSupport.performClick() }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.contact_support_title)) }
                    Text(AnnotatedString.fromHtml(state.termsText.text.toString(), linkStyles = TextLinkStyles(style = SpanStyle(color = themeColor(R.attr.primaryLightColorText)))), Modifier.fillMaxWidth().padding(bottom = 8.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }) { padding ->
            if (state.scrollView.isVisible) Column(Modifier.fillMaxSize().padding(padding).alpha(entrance.value).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PurchaseOcean(ocean, Modifier.fillMaxWidth().height(150.dp).background(Brush.verticalGradient(listOf(themeColor(R.attr.vpnHeaderGradientStart), themeColor(R.attr.vpnHeaderGradientCenter), themeColor(R.attr.background)))))
                if (state.connectionInfoCard.isVisible) Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("${stringResource(R.string.symbol_infinity)}  ${stringResource(R.string.rpn_desc_one)}", style = MaterialTheme.typography.bodySmall)
                        Text("${stringResource(R.string.symbol_globe)}  ${stringResource(R.string.rpn_desc_two)}", style = MaterialTheme.typography.bodySmall)
                        Text("${stringResource(R.string.symbol_bunny)}  ${stringResource(R.string.rpn_desc_four)}", style = MaterialTheme.typography.bodySmall)
                        MoneyBackBadge(state.moneyBackDays, Modifier.align(Alignment.End))
                        if (state.vDivider.isVisible) HorizontalDivider()
                        if (state.ispContainer.isVisible) { RpnText(state.connectionIp); RpnText(state.connectionLocation); RpnText(state.connectionIsp) }
                    }
                }
                if (state.extendModeBanner.isVisible) Card(Modifier.padding(horizontal = 16.dp)) {
                    Column(Modifier.padding(16.dp)) { Text(stringResource(R.string.extend_mode_banner_title), style = MaterialTheme.typography.titleMedium); Text(stringResource(R.string.extend_mode_banner_desc)) }
                }
                if (state.productTypeToggleContainer.isVisible) Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(state.subscriptionSelected, { state.btnSubscription.performClick() }, label = { Text(stringResource(R.string.subscribe_title)) }, modifier = Modifier.weight(1f).graphicsLayer { scaleX = scale(state.btnSubscription); scaleY = scale(state.btnSubscription) })
                    FilterChip(!state.subscriptionSelected, { state.btnOneTime.performClick() }, label = { Text(stringResource(R.string.one_time_label)) }, modifier = Modifier.weight(1f).graphicsLayer { scaleX = scale(state.btnOneTime); scaleY = scale(state.btnOneTime) })
                }
                Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) { plans() }
            }
            if (state.loadingContainer.isVisible) Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) { PurchaseShimmer(running = state.shimmerRunning) }
            if (state.errorContainer.isVisible) Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                state.errorIcon.imageRes?.let { Icon(painterResource(it), null, Modifier.size(64.dp)) }
                RpnText(state.errorTitle, style = MaterialTheme.typography.headlineSmall)
                RpnText(state.errorMessage); RpnText(state.errorReason)
                PurchaseButton(state.retryButton, R.string.retry, "purchase_retry", scale(state.retryButton))
                TextButton({ state.btnContactSupportError.performClick() }) { Text(stringResource(R.string.contact_support_title)) }
            }
        }
    }
}
