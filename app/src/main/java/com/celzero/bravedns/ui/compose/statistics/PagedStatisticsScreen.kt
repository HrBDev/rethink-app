/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.statistics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.appdetails.AppConnectionsPresenter

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PagedStatisticsScreen(title: String, subtitle: String?, empty: Boolean,
    presenter: AppConnectionsPresenter?, chips: @Composable () -> Unit = {},
    extraCount: Int = 0, extraRow: @Composable (Int) -> Unit = {}) {
    val listState = rememberLazyListState()
    Surface {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(Modifier.fillMaxWidth().padding(15.dp)) {
                Text(stringResource(R.string.app_name_small_case), Modifier.alpha(.5f).padding(horizontal = 7.dp), style = MaterialTheme.typography.headlineLarge)
                FlowRow(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    if (subtitle != null) Text(subtitle, Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyMedium)
                }
                chips()
            }
            if (empty) Column(Modifier.fillMaxWidth().testTag("statistics_empty").padding(30.dp)) {
                Text(stringResource(R.string.blocklist_update_check_failure), Modifier.padding(10.dp), style = MaterialTheme.typography.titleLarge)
                Image(painterResource(R.drawable.illustrations_no_record), null, Modifier.fillMaxWidth())
            } else LazyColumn(Modifier.fillMaxWidth().weight(1f).testTag("statistics_list"), state = listState) {
                // Pending pages have no header placeholders that could clamp a restored row index.
                presenter?.let { rows -> items(rows.itemCount) { rows.RowAt(it) } }
                items(extraCount) { extraRow(it) }
            }
        }
    }
}
