package com.celzero.bravedns.ui.compose.rpn

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.themeColor
import androidx.cardview.R as CardR

/** All keys share one composition scope, so moving a tile to another row does not restart its jobs. */
@Composable
fun RpnServerGrid(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content, modifier) { measurables, constraints ->
        val gap = 12.dp.roundToPx()
        val cell = ((constraints.maxWidth - gap) / 2).coerceAtLeast(0)
        val placeables = measurables.map { it.measure(Constraints.fixedWidth(cell)) }
        val heights = placeables.chunked(2).map { row -> row.maxOf { it.height } }
        layout(constraints.maxWidth, heights.sum() + gap * (heights.size - 1).coerceAtLeast(0)) {
            var y = 0
            placeables.chunked(2).forEachIndexed { index, row ->
                row.forEachIndexed { column, child -> child.placeRelative(column * (cell + gap), y) }
                y += heights[index] + gap
            }
        }
    }
}

@Composable
fun RpnServerTile(ui: RpnServerRowState, isAuto: Boolean) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(themeColor(CardR.attr.cardBackgroundColor)).border(1.dp, themeColor(R.attr.border), RoundedCornerShape(16.dp)).clickable { ui.serverCard.performClick() }.padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            RpnText(ui.tvCountryName, Modifier.weight(1f).padding(end = 4.dp), MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp))
            Box(Modifier.padding(top = 4.dp).size(28.dp).rpnMotion(ui.refreshStopIcon).clickable { ui.refreshStopIcon.performClick() }.padding(7.dp)) { RpnImage(ui.refreshStopIcon, Modifier.fillMaxSize()) }
        }
        Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (ui.ivStatusCheck.isVisible) Icon(painterResource(R.drawable.ic_check_circle), null, Modifier.padding(end = 4.dp).size(10.dp).alpha(.75f), tint = themeColor(R.attr.chipBgColorPositive))
            RpnText(ui.tvServerStatus, Modifier.padding(end = 4.dp), MaterialTheme.typography.bodySmall)
            RpnText(ui.tvServerIp, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
        }
        if (isAuto) Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Apps(ui); Recent(ui) }
        else Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) { Apps(ui); Recent(ui) }
        if (ui.relayActionContainer.visibility != android.view.View.GONE) RpnTileChip(ui.relayActionContainer, Modifier.padding(top = 10.dp)) {
            RpnText(ui.relayAction, style = MaterialTheme.typography.bodySmall)
            if (ui.relayIcon.visibility != android.view.View.GONE) Icon(painterResource(R.drawable.ic_check_circle), null, Modifier.padding(start = 4.dp).size(10.dp).rpnVisibility(ui.relayIcon), tint = themeColor(R.attr.serverChipTextColor))
        }
    }
}
@Composable private fun Apps(ui: RpnServerRowState) {
    RpnTileChip(ui.appsActionContainer) {
        RpnText(ui.appsAction, Modifier.width(44.dp), MaterialTheme.typography.bodySmall)
        Icon(painterResource(R.drawable.ic_right_arrow_small), null, Modifier.padding(start = 4.dp).size(11.dp), tint = themeColor(R.attr.serverChipTextColor))
    }
}
@Composable private fun Recent(ui: RpnServerRowState) {
    RpnTileChip(ui.lastRoutedAppContainer, compact = true) {
        if (ui.lastRoutedAppIconStack.isVisible) Box(Modifier.size(36.dp, 20.dp)) {
            listOf(ui.routedAppIcon2 to 22, ui.routedAppIcon1 to 11, ui.routedAppIcon0 to 0).forEach { (state, x) ->
                if (state.isVisible) RpnImage(state, Modifier.align(Alignment.CenterStart).offset(x.dp).size(16.dp).padding(1.dp))
            }
        }
        if (ui.lastRoutedAppFallbackIcon.isVisible) Icon(painterResource(R.drawable.ic_timer), null, Modifier.size(12.dp))
        RpnText(ui.lastRoutedAppAction, Modifier.padding(start = 4.dp).width(32.dp).basicMarquee(iterations = Int.MAX_VALUE), MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp))
    }
}
@Composable private fun RpnTileChip(state: RpnControlState, modifier: Modifier = Modifier, compact: Boolean = false, content: @Composable RowScope.() -> Unit) {
    if (state.visibility == android.view.View.GONE) return
    val positive = state.backgroundRes == R.drawable.bg_vpn_server_chip_positive
    Row(modifier.rpnVisibility(state).clip(RoundedCornerShape(14.dp)).background(if (positive) themeColor(R.attr.chipBgColorPositive) else Color.Transparent)
        .border(1.dp, themeColor(if (positive) R.attr.chipTextPositive else R.attr.serverChipStrokeColor), RoundedCornerShape(14.dp))
        .semantics { state.contentDescription?.let { contentDescription = it.toString() } }
        .clickable(enabled = state.isEnabled && state.isClickable) { state.performClick() }
        .padding(start = if (compact) 6.dp else 12.dp, end = if (compact) 6.dp else 9.dp, top = if (compact) 3.dp else 6.dp, bottom = if (compact) 3.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically, content = content)
}
@Composable internal fun RpnImage(state: RpnControlState, modifier: Modifier = Modifier) {
    val drawable = state.drawableValue
    val description = state.contentDescription?.toString()
    val filter = state.imageTintList?.defaultColor?.let { androidx.compose.ui.graphics.ColorFilter.tint(Color(it)) }
    if (drawable != null) {
        val bitmap = remember(drawable) { drawable.toBitmap(width = drawable.intrinsicWidth.coerceAtLeast(1), height = drawable.intrinsicHeight.coerceAtLeast(1)).asImageBitmap() }
        Image(bitmap, description, modifier, colorFilter = filter)
    } else if (state.imageRes != 0) Image(painterResource(state.imageRes), description, modifier, colorFilter = filter)
}
@Composable fun RpnAddServerTile(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.fillMaxWidth().heightIn(min = 80.dp).padding(6.dp).border(1.dp, themeColor(R.attr.border), RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(painterResource(R.drawable.ic_add), null, Modifier.size(26.dp).alpha(.8f), tint = themeColor(R.attr.primaryLightColorText))
        Text(stringResource(R.string.server_selection_add_tile), Modifier.padding(top = 6.dp).alpha(.8f), fontSize = 11.sp)
    }
}
