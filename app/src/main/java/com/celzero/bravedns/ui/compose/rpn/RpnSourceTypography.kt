package com.celzero.bravedns.ui.compose.rpn

import androidx.annotation.DimenRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit

/** Retains the selected resource qualifier and the user's font scale for XML sp dimensions. */
@Composable internal fun rpnDimensionSp(@DimenRes id: Int): TextUnit {
    val pixels = LocalContext.current.resources.getDimension(id)
    return with(LocalDensity.current) { pixels.toSp() }
}
