package com.celzero.bravedns.ui.compose.rpn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.themeColor

@Composable
fun RpnRemovalContent(ui: RpnRemovalState) {
    Column(Modifier.fillMaxWidth().rpnMotion(ui.notificationCard).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp).rpnMotion(ui.iconContainer).background(themeColor(R.attr.chipBgColorPositive), CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_info_white), null, Modifier.size(32.dp).rpnMotion(ui.notificationIcon), tint = themeColor(R.attr.accentGood))
        }
        RpnText(ui.notificationTitle, Modifier.padding(top = 16.dp), MaterialTheme.typography.headlineSmall)
        RpnText(ui.notificationSubtitle, Modifier.padding(top = 8.dp), MaterialTheme.typography.bodyMedium)
        HorizontalDivider(Modifier.padding(vertical = 20.dp))
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            ui.rows.forEach { (server, row) -> key(server.id) {
                Row(Modifier.fillMaxWidth().rpnMotion(row).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(server.flagEmoji, fontSize = 28.sp)
                    Column(Modifier.padding(start = 16.dp)) { Text(server.countryName); Text(server.serverLocation, style = MaterialTheme.typography.bodySmall) }
                }
            } }
        }
        Button({ ui.btnUnderstand.performClick() }, Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp).rpnMotion(ui.btnUnderstand), enabled = ui.btnUnderstand.isEnabled) { RpnText(ui.btnUnderstand) }
    }
}
