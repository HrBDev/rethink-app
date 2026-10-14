/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.statistics

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.celzero.bravedns.R
import com.celzero.bravedns.data.AppConnection
import com.celzero.bravedns.ui.compose.appdetails.DrawableImage
import com.celzero.bravedns.ui.compose.themeColor
import com.celzero.bravedns.ui.fragment.SummaryStatisticsFragment.SummaryStatisticsType as Type
import com.celzero.bravedns.util.UIUtils.getCountryNameFromFlag

@Composable
internal fun StatisticsRow(title: String, subtitle: String?, flag: String, count: String,
    percentage: Int? = null, color: Color = MaterialTheme.colorScheme.primary, icon: Drawable? = null,
    downArrow: Boolean = false, arrowRotation: Float = 0f, tag: String = "", onClick: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().testTag(tag).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = 8.dp, horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                if (icon != null) DrawableImage(icon, Modifier.size(22.dp)) else Text(flag, fontSize = 18.sp)
            }
            Column(Modifier.weight(1f).padding(start = 12.dp, end = 10.dp)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = themeColor(R.attr.primaryLightColorText))
                if (percentage != null) LinearProgressIndicator(progress = { percentage.coerceIn(0, 100) / 100f },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(3.dp).alpha(.9f), color = color,
                    trackColor = themeColor(R.attr.background), gapSize = 0.dp, drawStopIndicator = {})
            }
            Text(count, Modifier.alpha(.85f), style = MaterialTheme.typography.bodyMedium)
            if (onClick != null) Icon(painterResource(if (downArrow) R.drawable.ic_arrow_down_small else R.drawable.ic_right_arrow_white), null,
                Modifier.padding(start = 6.dp).size(14.dp).alpha(.35f).rotate(arrowRotation))
        }
    }
}
internal fun statisticsLabel(context: Context, item: AppConnection, type: Type): String = when (type) {
    Type.TOP_ACTIVE_CONNS, Type.MOST_CONNECTED_APPS, Type.MOST_BLOCKED_APPS -> item.appOrDnsName?.takeIf { it.isNotEmpty() }
        ?: context.getString(R.string.network_log_app_name_unnamed, item.uid.toString())
    Type.MOST_CONNECTED_ASN, Type.MOST_BLOCKED_ASN -> item.appOrDnsName.orEmpty()
    Type.MOST_CONTACTED_DOMAINS, Type.MOST_BLOCKED_DOMAINS -> item.appOrDnsName?.dropLastWhile { it == '.' }.orEmpty()
    Type.MOST_BLOCKED_BLOCKLISTS -> item.appOrDnsName.orEmpty()
    Type.MOST_CONTACTED_IPS, Type.MOST_BLOCKED_IPS -> item.ipAddress
    Type.MOST_CONTACTED_COUNTRIES -> {
        val name = getCountryNameFromFlag(item.flag)
        if (name.isNotEmpty() && !name.all { it == '-' }) name else context.getString(R.string.two_argument_space,
            context.getString(R.string.network_log_app_name_unknown), item.flag)
    }
}
