package com.celzero.bravedns.ui.compose.dnssettings

import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme

/** Content fixture only. Execution still requires the separately reviewed isolated runner. */
@Composable fun DnsUiTestTheme(content: @Composable () -> Unit) {
    val base = LocalContext.current
    remember(base) {
        generateSequence(base) { (it as? android.content.ContextWrapper)?.baseContext }
            .filterIsInstance<android.app.Activity>().first().setTheme(R.style.AppThemeWhite)
    }
    val context = remember(base) { ContextThemeWrapper(base, R.style.AppThemeWhite) }
    CompositionLocalProvider(LocalContext provides context) { RethinkTheme(content) }
}
