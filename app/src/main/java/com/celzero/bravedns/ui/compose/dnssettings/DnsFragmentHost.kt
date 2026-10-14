/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.dnssettings

import android.content.Context
import android.widget.LinearLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentContainerView
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.celzero.bravedns.ui.compose.themeColor
import com.celzero.bravedns.util.UIUtils.fetchColor

/** Framework-only lifecycle/navigation host. No legacy screen is embedded in Compose. */
fun dnsFragmentHost(context: Context, rootId: Int, headingId: Int, containerId: Int, title: String, description: String? = null): LinearLayout =
    LinearLayout(context).apply {
        id = rootId
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(fetchColor(context, R.attr.background))
        addView(ComposeView(context).apply {
            id = headingId
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { RethinkTheme { DnsFragmentHeader(title, description) } }
        }, LinearLayout.LayoutParams(-1, -2))
        addView(FragmentContainerView(context).apply { id = containerId }, LinearLayout.LayoutParams(-1, -1))
    }

@Composable private fun DnsFragmentHeader(title: String, description: String?) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(title, Modifier.padding(top = 10.dp).padding(5.dp).semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = themeColor(androidx.appcompat.R.attr.colorPrimary))
        description?.let { Text(it, Modifier.padding(5.dp), fontSize = 14.sp, color = themeColor(R.attr.primaryLightColorText)) }
    }
}
