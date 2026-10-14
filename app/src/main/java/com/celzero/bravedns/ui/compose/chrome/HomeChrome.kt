/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.chrome

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.NoDimDialogWindow
import com.celzero.bravedns.ui.compose.themeColor
import com.celzero.bravedns.ui.compose.home.HomeTourAnchors
import com.celzero.bravedns.ui.compose.home.homeTourAnchor

internal data class RootNavigationItem(val id: Int, val icon: Int, val label: Int)
internal val rootNavigationItems = listOf(
    RootNavigationItem(R.id.homeScreenFragment, R.drawable.ic_home_black_24dp, R.string.txt_home),
    RootNavigationItem(R.id.summaryStatisticsFragment, R.drawable.ic_statistics, R.string.title_statistics),
    RootNavigationItem(R.id.rethinkPlus, R.drawable.ic_rethink_plus_sparkle, R.string.rpn_title),
    RootNavigationItem(R.id.configureFragment, R.drawable.ic_settings, R.string.lbl_configure),
    RootNavigationItem(R.id.aboutFragment, R.drawable.ic_about, R.string.title_about),
)

@Composable
fun HomeBottomNavigation(selectedItem: Int, anchors: HomeTourAnchors, onSelect: (Int) -> Unit) {
    NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = themeColor(R.attr.navBarColor),
        tonalElevation = 0.dp,
        // The same-ID Activity host still applies the original platform bottom inset.
        windowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        rootNavigationItems.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item.id,
                onClick = { onSelect(item.id) },
                icon = { Icon(painterResource(item.icon), null, Modifier.size(24.dp)) },
                label = { Text(stringResource(item.label), maxLines = 1) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = themeColor(R.attr.colorOnSecondaryContainer),
                    selectedTextColor = themeColor(com.google.android.material.R.attr.colorOnSurface),
                    indicatorColor = themeColor(R.attr.colorSecondaryContainer),
                    unselectedIconColor = themeColor(R.attr.colorOnSurfaceVariant),
                    unselectedTextColor = themeColor(R.attr.colorOnSurfaceVariant),
                ),
                modifier = Modifier.testTag("root_nav_${item.id}").homeTourAnchor(item.id, anchors),
            )
        }
    }
}

/** Local window state: the original AlertDialogs were not persisted through recreation. */
class HomeChromeDialog(
    val title: String,
    val message: String,
    val positiveLabel: String,
    val negativeLabel: String? = null,
    val dismissible: Boolean = true,
    private val onPositive: () -> Unit = {},
    private val onNegative: () -> Unit = {},
) {
    private var closed = false
    fun positive(onDismiss: () -> Unit) = finish(onPositive, onDismiss)
    fun negative(onDismiss: () -> Unit) = finish(onNegative, onDismiss)
    fun dismiss(onDismiss: () -> Unit) { if (dismissible) finish({}, onDismiss) }
    private fun finish(action: () -> Unit, onDismiss: () -> Unit) {
        if (closed) return
        closed = true
        action() // Preserve the original button listener before dismissal.
        onDismiss()
    }
}

@Composable
fun HomeChromeDialogContent(dialog: HomeChromeDialog, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { dialog.dismiss(onDismiss) },
        title = { Text(dialog.title) },
        text = { NoDimDialogWindow(); Text(dialog.message) },
        confirmButton = {
            TextButton(onClick = { dialog.positive(onDismiss) }, modifier = Modifier.testTag("root_dialog_positive")) {
                Text(dialog.positiveLabel)
            }
        },
        dismissButton = if (dialog.negativeLabel == null) null else ({
            TextButton(onClick = { dialog.negative(onDismiss) }, modifier = Modifier.testTag("root_dialog_negative")) {
                Text(dialog.negativeLabel)
            }
        }),
        properties = DialogProperties(dismissOnBackPress = dialog.dismissible, dismissOnClickOutside = dialog.dismissible),
    )
}
