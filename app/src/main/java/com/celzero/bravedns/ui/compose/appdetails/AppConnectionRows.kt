package com.celzero.bravedns.ui.compose.appdetails
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.themeColor
import com.celzero.bravedns.ui.compose.wireguard.*

@Composable fun AppIpsRowContent(b: AppIpsRowState) = AppConnectionRow(b.acdContainer, b.acdFlag, null, b.acdIpAddress, b.acdDomainName, b.acdCount, b.progress, b.acdDownArrowIv)
@Composable fun AppDomainsRowContent(b: AppDomainsRowState) = AppConnectionRow(b.acdContainer, b.acdFlag, b.acdIcon, b.acdDomain, b.acdIpAddress, b.acdCount, b.progress, null)
@Composable fun AppCountriesRowContent(b: AppCountriesRowState) = AppConnectionRow(null, b.accFlag, null, b.accCountryName, null, b.accCount, b.accProgress, null, true)

@Composable private fun AppConnectionRow(container: WgControlState?, flag: WgControlState, icon: WgControlState?, title: WgControlState, subtitle: WgControlState?, count: WgControlState, progress: WgControlState, arrow: WgControlState?, country: Boolean = false) {
    Column(Modifier.fillMaxWidth().then(if (container != null) Modifier.clickable { container.performClick() } else Modifier)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 50.dp).padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon?.isVisible == true && icon.drawable != null) DrawableImage(icon.drawable, Modifier.size(32.dp))
            else if (flag.isVisible) Text(flag.text.toString(), Modifier.width(32.dp).alpha(.75f), fontSize = 26.sp)
            Column(Modifier.weight(1f).padding(start = 10.dp, end = 5.dp)) {
                Text(wgAnnotatedText(title.text), color = themeColor(R.attr.primaryTextColor), fontSize = 14.sp)
                if (subtitle?.isVisible == true) Text(wgAnnotatedText(subtitle.text), fontSize = 12.sp)
                if (progress.isVisible) LinearProgressIndicator(progress = { progress.progress.coerceIn(0, 100) / 100f },
                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp).height(3.dp).alpha(if (country) .9f else .75f),
                    color = progress.indicatorColor?.let(::Color) ?: MaterialTheme.colorScheme.primary, trackColor = themeColor(R.attr.background), gapSize = 0.dp, drawStopIndicator = {})
            }
            Text(count.text.toString(), Modifier.padding(5.dp), fontSize = 16.sp)
            if (!country) {
                if (arrow == null || arrow.isVisible) Icon(painterResource(R.drawable.ic_arrow_down_small), null, Modifier.padding(5.dp).alpha(.3f))
                else Spacer(Modifier.width(34.dp))
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 3.dp), thickness = .75.dp, color = themeColor(R.attr.divider))
    }
}
