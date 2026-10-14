@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.celzero.bravedns.ui.compose.rpn

import android.content.res.Configuration
import android.view.View
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.celzero.bravedns.ui.compose.home.HomeTourAnchors
import com.celzero.bravedns.ui.compose.home.homeTourAnchor
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.themeColor
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.*

@Composable
fun RpnSelectionScreen(ui: RpnSelectionState, anchors: HomeTourAnchors) {
    val density = LocalDensity.current
    val scroll = rememberScrollState()
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var searchY by remember { mutableIntStateOf(0) }
    var restoredQuery by rememberSaveable { mutableStateOf(ui.searchBar.query) }
    LaunchedEffect(ui) { if (restoredQuery != ui.searchBar.query) ui.searchBar.setQuery(restoredQuery) }
    LaunchedEffect(ui.searchBar.query) { restoredQuery = ui.searchBar.query }
    LaunchedEffect(ui.focusSearchRequest) {
        if (ui.focusSearchRequest == 0) return@LaunchedEffect
        scroll.animateScrollTo(searchY)
        focus.requestFocus()
        keyboard?.show()
    }
    RpnPullRefresh(ui.swipeRefresh, Modifier.fillMaxSize().background(themeColor(R.attr.background))) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(bottom = with(density) { ui.scrollBottomPx.toDp() } + 72.dp)) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(top = 4.dp).homeTourAnchor(R.id.status_card, anchors).rpnMotion(ui.statusCard).clip(RoundedCornerShape(24.dp)).rpnHeroBackground().padding(start = 18.dp, end = 12.dp, top = 16.dp, bottom = 16.dp)) {
                        if (ui.locationContent.isVisible) Column(Modifier.rpnMotion(ui.locationContent)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                RpnAvatars(ui)
                                Row(Modifier.weight(1f).padding(start = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.padding(end = 5.dp).size(6.dp).rpnMotion(ui.statusIndicator).background(ui.statusIndicator.backgroundTintList?.defaultColor?.let(::Color) ?: themeColor(R.attr.accentGood), CircleShape))
                                    RpnText(ui.tvConnectionStatus, Modifier.widthIn(max = 110.dp), MaterialTheme.typography.bodySmall)
                                    RpnText(ui.tvActiveDuration, Modifier.padding(start = 4.dp).widthIn(max = 120.dp), MaterialTheme.typography.bodySmall)
                                }
                                RpnIconButton(ui.supportBtn, R.drawable.ic_user_icon, R.string.contact_support_title, 32.dp)
                                RpnIconButton(ui.settingsBtn, R.drawable.ic_settings, R.string.server_settings_open, 32.dp, Modifier.homeTourAnchor(R.id.settings_btn, anchors))
                            }
                            Column(Modifier.fillMaxWidth().padding(top = 8.dp).rpnMotion(ui.statusChip).clickable { ui.statusChip.performClick() }.padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 2.dp)) {
                                Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                                    Text("-24H", Modifier.weight(1f).alpha(.55f), fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                                    Text("-12H", Modifier.weight(2f).alpha(.55f), textAlign = TextAlign.Center, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                                    Text("now", Modifier.weight(1f).alpha(.55f), textAlign = TextAlign.End, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                                }
                                RpnHeatmap(ui)
                            }
                        }
                        if (ui.shimmerHeader.isVisible) RpnShimmer(ui.shimmerHeader, 46.dp, listOf(.48f to 10.dp, .8f to 26.dp))
                        if (ui.subscriptionBanner.isVisible) Row(Modifier.fillMaxWidth().rpnMotion(ui.subscriptionBanner).padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RpnText(ui.tvExpiryLabel, style = MaterialTheme.typography.bodySmall)
                            RpnText(ui.tvExpiryDate, Modifier.weight(1f).padding(start = 6.dp), MaterialTheme.typography.bodySmall)
                            RpnText(ui.tvDaysRemaining, style = MaterialTheme.typography.labelSmall)
                        }
                        if (ui.shimmerSubscriptionBanner.isVisible) RpnShimmer(ui.shimmerSubscriptionBanner, 40.dp, listOf(.45f to 20.dp, .25f to 12.dp))
                    }
                    if (ui.registrationProgressBar.isVisible) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 4.dp))
                    if (ui.quickSettingsRow.isVisible) BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp).rpnMotion(ui.quickSettingsRow)) {
                        Row(Modifier.horizontalScroll(rememberScrollState()).widthIn(min = maxWidth), horizontalArrangement = Arrangement.Center) {
                            QuickPill(ui.qsRelayTile, R.drawable.ic_hop, stringResource(R.string.cd_dns_crypt_relay_heading), ui.qsRelayState, ui.qsRelayIcon, ui.qsRelayLabel, Modifier.homeTourAnchor(R.id.qs_relay_tile, anchors))
                            QuickPill(ui.qsBypassAppsTile, R.drawable.ic_bypass, stringResource(R.string.fapps_bypass_block_dialog_title), ui.qsBypassAppsState, modifier = Modifier.homeTourAnchor(R.id.qs_bypass_apps_tile, anchors))
                            QuickPill(ui.qsStatsTile, R.drawable.ic_statistics, stringResource(R.string.title_statistics), ui.qsStatsState)
                        }
                    }
                }
                // Keep the native selected presenters composed while GONE so retained jobs match the old attached nested list.
                Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).homeTourAnchor(R.id.rv_selected_servers, anchors).rpnVisibility(ui.rvSelectedServers).rpnMotion(ui.rvSelectedServers)) {
                    ui.selectedAdapter?.Content(Modifier.fillMaxWidth(), visible = ui.rvSelectedServers.isVisible, anchors = anchors)
                }
                if (ui.frequentCountriesSection.isVisible) Column(Modifier.padding(vertical = 12.dp).rpnMotion(ui.frequentCountriesSection)) {
                    Text(stringResource(R.string.server_selection_frequent_label), Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.labelSmall)
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ui.frequentChipGroup.children.forEach { state -> AssistChip({ state.performClick() }, { RpnText(state, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)) }, Modifier.heightIn(min = 40.dp), colors = AssistChipDefaults.assistChipColors(containerColor = themeColor(R.attr.colorSurfaceVariant))) }
                    }
                }
                if (ui.emptySelectionCard.isVisible) RpnSettingsCard { Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painterResource(R.drawable.ic_vpn), null, Modifier.size(28.dp).alpha(.6f))
                    Text(stringResource(R.string.server_selection_tap_to_select), Modifier.padding(top = 12.dp), fontSize = 16.sp)
                    Text(stringResource(R.string.server_selection_max_info), Modifier.padding(top = 6.dp).alpha(.7f), fontSize = 11.sp)
                } }
                if (ui.locationCapacityIndicator.isVisible) Row(Modifier.fillMaxWidth().rpnMotion(ui.locationCapacityIndicator).padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)) {
                    listOf(ui.capacityDotOne, ui.capacityDotTwo, ui.capacityDotThree, ui.capacityDotFour, ui.capacityDotFive).forEach { dot ->
                        Box(Modifier.size(16.dp, 6.dp).rpnMotion(dot).background(dot.backgroundTintList?.defaultColor?.let(::Color) ?: themeColor(R.attr.accentGood), RoundedCornerShape(3.dp)))
                    }
                }
                if (ui.searchCard.isVisible) Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 10.dp).onGloballyPositioned { searchY = it.positionInParent().y.toInt() }.rpnMotion(ui.searchCard).clip(RoundedCornerShape(16.dp)).background(themeColor(R.attr.colorSurfaceVariant)).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_search), null, Modifier.size(18.dp))
                    BasicTextField(ui.searchBar.query, { restoredQuery = it; ui.searchBar.setQuery(it) }, Modifier.weight(1f).padding(horizontal = 8.dp).heightIn(min = 40.dp).focusRequester(focus).onFocusChanged { ui.searchBar.onFocusChanged(it.hasFocus) },
                        enabled = ui.searchBar.isEnabled, singleLine = true, textStyle = TextStyle(fontSize = 14.sp, color = themeColor(R.attr.primaryTextColor)),
                        decorationBox = { editor -> Box(contentAlignment = Alignment.CenterStart) { if (ui.searchBar.query.isEmpty()) Text(stringResource(R.string.server_selection_search_hint), fontSize = 14.sp, color = themeColor(R.attr.primaryLightColorText)); editor() } })
                    if (ui.searchBar.query.isNotEmpty()) IconButton({ ui.searchBar.setQuery("") }, Modifier.size(32.dp), enabled = ui.searchBar.isEnabled) { Icon(painterResource(R.drawable.ic_cross), stringResource(R.string.lbl_cancel), Modifier.size(16.dp)) }
                    RpnIconButton(ui.searchFilterBtn, R.drawable.ic_filter, R.string.server_selection_filter_locations, 36.dp)
                }
                if (ui.serverCountLayout.isVisible) Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RpnText(ui.tvServerCount, Modifier.weight(1f), MaterialTheme.typography.titleMedium)
                    if (ui.tvActiveFilterSummary.isVisible) RpnText(ui.tvActiveFilterSummary, Modifier.clip(RoundedCornerShape(16.dp)).background(ui.tvActiveFilterSummary.backgroundTintList?.defaultColor?.let(::Color) ?: Color.Transparent).clickable { ui.tvActiveFilterSummary.performClick() }.padding(8.dp), MaterialTheme.typography.bodySmall)
                }
                if (ui.rvServers.isVisible) ui.countryAdapter?.Content(Modifier.fillMaxWidth().padding(horizontal = 16.dp).rpnMotion(ui.rvServers))
                if (ui.shimmerServerList.isVisible) Column(Modifier.padding(horizontal = 12.dp)) { repeat(5) { RpnShimmer(ui.shimmerServerList, 80.dp, listOf(.5f to 16.dp, .7f to 12.dp)) } }
                if (ui.errorStateContainer.isVisible) RpnErrorContent(ui)
            }
            Box(Modifier.matchParentSize().rpnMotion(ui.fxOverlay)) {
                ui.fxOverlay.children.forEach { child -> key(child) {
                    val width = with(density) { child.width.toDp() }; val height = with(density) { child.height.toDp() }
                    if (child.imageRes != 0) RpnImage(child, Modifier.size(width, height).rpnMotion(child))
                    else Box(Modifier.size(width, height).rpnMotion(child).background(child.backgroundTintList?.defaultColor?.let(::Color) ?: themeColor(R.attr.accentGood), CircleShape))
                } }
            }
            if (ui.fabStopProxy.isVisible) FloatingActionButton({ ui.fabStopProxy.performClick() }, Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = with(density) { ui.fabBottomPx.toDp() }).rpnMotion(ui.fabStopProxy)) {
                Icon(painterResource(R.drawable.ic_stop), ui.fabStopProxy.contentDescription?.toString())
            }
            if (ui.fabStartProxy.isVisible) ExtendedFloatingActionButton({ ui.fabStartProxy.performClick() }, Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = with(density) { ui.fabBottomPx.toDp() }).rpnMotion(ui.fabStartProxy)) {
                Text(ui.fabStartProxy.text.toString())
            }
        }
    }
}

@Composable private fun RpnAvatars(ui: RpnSelectionState) {
    val entries = ui.avatars.filter { it.cc.isNotBlank() }
    if (entries.isEmpty()) return
    Box(Modifier.width((38 + (entries.size - 1) * 28).dp).height(38.dp)) {
        entries.forEachIndexed { index, config -> Box(Modifier.offset((index * 28).dp).size(38.dp).background(themeColor(R.attr.chipBgColorNeutral), CircleShape), contentAlignment = Alignment.Center) {
            Text(config.flagEmoji, Modifier.alpha(.75f), fontSize = 22.sp)
            Text(config.cc.uppercase(Locale.US), color = Color.White, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, shadow = Shadow(Color.Black.copy(alpha = .5f), Offset(0f, 1f), 2f)))
        } }
    }
}

@Composable private fun RpnHeatmap(ui: RpnSelectionState) {
    val base = themeColor(R.attr.primaryLightColorText)
    val light = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK != Configuration.UI_MODE_NIGHT_YES
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val alphas = if (light) intArrayOf(0x40, 0x80, 0x80, 0xB3, 0xE6) else intArrayOf(0x24, 0x52, 0x52, 0x85, 0xCC)
    val tap by rememberUpdatedState(ui.onHeatmapTap)
    Canvas(Modifier.fillMaxWidth().height(54.dp).rpnMotion(ui.rpnHeatmapGrid).pointerInput(Unit) { detectTapGestures { tap(it.x, it.y) } }) {
        val fractions = floatArrayOf(.30f, .78f, .78f, 1f, 1f)
        for (i in 0 until 144) {
            val count = ui.heatmap.getOrElse(i) { 0L }
            val level = if (count <= 0L) 0 else min(4, log10(count.toDouble()).toInt() + 1)
            val col = if (rtl) 23 - i / 6 else i / 6
            val row = i % 6
            val diameter = (5.dp.toPx() * fractions[level]).toInt().toFloat()
            drawCircle(base.copy(alpha = alphas[level] / 255f), diameter / 2, Offset((col + .5f) * size.width / 24, (row + .5f) * size.height / 6))
        }
    }
}

@Composable private fun RpnIconButton(state: RpnControlState, icon: Int, description: Int, size: Dp, modifier: Modifier = Modifier) {
    if (!state.isVisible) return
    IconButton({ state.performClick() }, modifier.size(size).rpnMotion(state).background(state.backgroundTintList?.defaultColor?.let(::Color) ?: Color.Transparent, CircleShape), enabled = state.isEnabled && state.isClickable) {
        Icon(painterResource(icon), state.contentDescription?.toString() ?: stringResource(description), Modifier.size(22.dp), tint = state.iconTint?.defaultColor?.let(::Color) ?: themeColor(R.attr.primaryLightColorText))
    }
}
@Composable private fun QuickPill(state: RpnControlState, icon: Int, label: String, caption: RpnControlState, iconState: RpnControlState? = null, labelState: RpnControlState? = null, modifier: Modifier = Modifier) {
    Row(modifier.padding(end = 6.dp).rpnMotion(state).clip(RoundedCornerShape(20.dp)).background(themeColor(R.attr.colorSurfaceVariant)).clickable(enabled = state.isClickable && state.isEnabled) { state.performClick() }.padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), null, Modifier.size(14.dp).alpha(iconState?.alpha ?: .5f), tint = iconState?.imageTintList?.defaultColor?.let(::Color) ?: themeColor(R.attr.primaryLightColorText))
        Text(label, Modifier.padding(start = 5.dp), fontSize = 11.sp, color = labelState?.foregroundColor?.let(::Color) ?: themeColor(R.attr.primaryLightColorText))
        RpnText(caption, Modifier.padding(start = 5.dp), MaterialTheme.typography.labelSmall)
    }
}
@Composable private fun RpnErrorContent(ui: RpnSelectionState) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 24.dp).rpnMotion(ui.errorStateContainer)) {
        RpnSettingsCard { Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            RpnImage(ui.errorIllustration, Modifier.size(80.dp, 64.dp).rpnMotion(ui.errorIllustration))
            RpnText(ui.errorTitle, Modifier.padding(top = 20.dp), MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp))
            RpnText(ui.errorHint, Modifier.padding(top = 6.dp))
            if (ui.errorRetryBtn.isVisible) Button({ ui.errorRetryBtn.performClick() }, Modifier.fillMaxWidth().padding(top = 24.dp).height(48.dp), enabled = ui.errorRetryBtn.isEnabled) { RpnText(ui.errorRetryBtn) }
            if (ui.errorResetBtn.isVisible) OutlinedButton({ ui.errorResetBtn.performClick() }, Modifier.fillMaxWidth().padding(top = 10.dp).height(44.dp), enabled = ui.errorResetBtn.isEnabled) { RpnText(ui.errorResetBtn) }
            if (ui.errorReportBtn.isVisible) TextButton({ ui.errorReportBtn.performClick() }, Modifier.padding(top = 6.dp), enabled = ui.errorReportBtn.isEnabled) { RpnText(ui.errorReportBtn) }
        } }
    }
}
@Composable private fun RpnShimmer(state: RpnControlState, height: Dp, lines: List<Pair<Float, Dp>>) {
    val transition = rememberInfiniteTransition(label = "RPN loading shimmer")
    val offset by transition.animateFloat(-1f, 2f, infiniteRepeatable(tween(1000, easing = LinearEasing)), label = "shimmer highlight")
    val base = themeColor(R.attr.primaryLightColorText).copy(alpha = .12f)
    Column(Modifier.fillMaxWidth().height(height).padding(vertical = 8.dp), verticalArrangement = Arrangement.SpaceBetween) {
        lines.forEach { (width, h) -> Canvas(Modifier.fillMaxWidth(width).height(h)) {
            val x = if (state.shimmerRunning) offset * size.width else 0f
            drawRoundRect(Brush.linearGradient(listOf(base, base.copy(alpha = .3f), base), Offset(x - size.width, 0f), Offset(x + size.width, size.height)), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
        } }
    }
}

/** Nested-scroll pull distance is unscaled; SwipeRefreshLayout's .5 drag rate applies only to its touch fallback. */
internal fun rpnPullSpinnerOffset(distance: Float, trigger: Float, slingshot: Float, diameter: Float): Float {
    val drag = min(1f, abs(distance / trigger))
    val extra = abs(distance) - trigger
    val tensionSlingshot = max(0f, min(extra, slingshot * 2) / slingshot)
    val tension = (tensionSlingshot / 4 - (tensionSlingshot / 4).pow(2)) * 2
    return -diameter + slingshot * drag + slingshot * tension * 2
}

@Composable
private fun RpnPullRefresh(state: RpnRefreshState, modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    val density = LocalDensity.current
    val trigger = with(density) { 220.dp.toPx() }
    val diameter = with(density) { 40.dp.toPx() }
    var distance by remember { mutableFloatStateOf(0f) }
    var settling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val current by rememberUpdatedState(state)
    val connection = remember(trigger) { object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.UserInput || distance <= 0f || available.y >= 0f) return Offset.Zero
            val consumed = max(available.y, -distance)
            distance += consumed
            return Offset(0f, consumed)
        }
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.UserInput || !current.isEnabled || current.isRefreshing || settling || available.y <= 0f) return Offset.Zero
            distance += available.y
            return Offset(0f, available.y)
        }
        override suspend fun onPreFling(available: Velocity): Velocity {
            if (distance <= 0f) return Velocity.Zero
            val refresh = distance > trigger && current.isEnabled && !current.isRefreshing && !settling
            if (refresh) {
                settling = true
                scope.launch {
                    animate(distance, trigger, animationSpec = tween(200)) { value, _ -> distance = value }
                    current.onRefresh()
                    settling = false
                    distance = 0f
                }
            } else {
                scope.launch { animate(distance, 0f, animationSpec = tween(200)) { value, _ -> distance = value } }
            }
            return Velocity(0f, available.y)
        }
    } }
    Box(modifier.nestedScroll(connection)) {
        content()
        if (distance > 0f || settling || state.isRefreshing) {
            val offset = if (state.isRefreshing && !settling) trigger - diameter else rpnPullSpinnerOffset(distance, trigger, trigger, diameter)
            Surface(Modifier.align(Alignment.TopCenter).offset { IntOffset(0, offset.roundToInt()) }.size(40.dp), shape = CircleShape, shadowElevation = 4.dp) {
                if (state.isRefreshing || settling) CircularProgressIndicator(Modifier.padding(8.dp), strokeWidth = 3.dp)
                else CircularProgressIndicator(progress = { (distance / trigger).coerceIn(0f, 1f) }, modifier = Modifier.padding(8.dp), strokeWidth = 3.dp)
            }
        }
    }
}
