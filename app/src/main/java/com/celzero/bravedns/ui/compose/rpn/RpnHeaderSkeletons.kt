package com.celzero.bravedns.ui.compose.rpn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.themeColor
import com.facebook.shimmer.Shimmer

/** fragment_server_selection: small left bar and end-aligned action placeholder. */
@Composable internal fun RpnHeaderLoadingSkeleton(running: Boolean) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp)
        .then(sourceShimmerModifier(running, 1200, .12f, .35f)), verticalAlignment = Alignment.CenterVertically) {
        HeaderPlaceholder(Modifier.width(80.dp).height(10.dp))
        Spacer(Modifier.weight(1f).height(10.dp))
        HeaderPlaceholder(Modifier.width(60.dp).height(26.dp))
    }
}

/** This source ShimmerFrameLayout has no XML overrides: read the already-pinned library defaults. */
@Composable internal fun RpnSubscriptionLoadingSkeleton(running: Boolean) {
    val defaults = remember { Shimmer.AlphaHighlightBuilder().build() }
    val baseAlpha = (defaults.baseColor ushr 24 and 0xff) / 255f
    val highlightAlpha = (defaults.highlightColor ushr 24 and 0xff) / 255f
    Row(Modifier.fillMaxWidth().padding(top = 8.dp).height(40.dp)
        .then(sourceShimmerModifier(running, defaults.animationDuration.toInt(), baseAlpha, highlightAlpha))
        .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        HeaderPlaceholder(Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        HeaderPlaceholder(Modifier.weight(1f).height(12.dp))
    }
}

@Composable private fun HeaderPlaceholder(modifier: Modifier) {
    Box(modifier.background(themeColor(R.attr.primaryTextColor), RoundedCornerShape(12.dp)))
}
