/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.dns

import android.app.Application
import android.view.ContextThemeWrapper
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.reference.RuleDialogReferenceFactories
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt

/** Authored UNRUN. Plain Application plus localized resource/native TextView oracles and fake
 * callbacks only. No production Activity/Application, service, database or traffic is started. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h800dp")
class DnsEditorSourceStyleTest {
    @get:Rule val compose = createComposeRule()

    @Test fun dohInvisibleErrorKeepsNativeMeasurementWithoutAnnouncingPlaceholder() {
        val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppThemeWhite)
        val native = RuleDialogReferenceFactories.dohError(context)
        val placeholder = context.getString(R.string.settings_http_proxy_error_text4)
        assertEquals(View.INVISIBLE, native.visibility)
        val error = mutableStateOf<String?>(null)
        compose.setContent {
            CompositionLocalProvider(LocalContext provides context) {
                RethinkTheme { Box(Modifier.width(280.dp).testTag("reservation")) { DnsError(error.value, DnsErrorLayout.Doh) } }
            }
        }
        compose.onNodeWithText(placeholder, useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("dns_error").assertDoesNotExist()
        val hiddenHeight = compose.onNodeWithTag("reservation").fetchSemanticsNode().boundsInRoot.height
        val density = context.resources.displayMetrics.density
        native.measure(View.MeasureSpec.makeMeasureSpec((260 * density).roundToInt(), View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        assertEquals(native.measuredHeight + 20 * density, hiddenHeight, 1f)
        compose.runOnIdle { error.value = placeholder }
        compose.onNodeWithTag("dns_error").assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        assertEquals(hiddenHeight, compose.onNodeWithTag("reservation").fetchSemanticsNode().boundsInRoot.height, 0f)
        compose.runOnIdle { error.value = null }
        compose.onNodeWithText(placeholder, useUnmergedTree = true).assertDoesNotExist()
        assertEquals(hiddenHeight, compose.onNodeWithTag("reservation").fetchSemanticsNode().boundsInRoot.height, 0f)
    }

    @Test fun cryptEmptyErrorRetainsNativeTwoDpPaddingAndLineMeasurement() {
        val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppThemeWhite)
        val native = RuleDialogReferenceFactories.cryptError(context)
        assertEquals(View.VISIBLE, native.visibility)
        assertEquals("", native.text.toString())
        compose.setContent {
            CompositionLocalProvider(LocalContext provides context) {
                RethinkTheme { Box(Modifier.width(280.dp).testTag("reservation")) { DnsError(null, DnsErrorLayout.Crypt) } }
            }
        }
        val density = context.resources.displayMetrics.density
        native.measure(View.MeasureSpec.makeMeasureSpec((280 * density).roundToInt(), View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        assertEquals(native.measuredHeight.toFloat(), compose.onNodeWithTag("reservation").fetchSemanticsNode().boundsInRoot.height, 1f)
        compose.onNodeWithTag("dns_error").assertDoesNotExist()
    }

    @Test fun odohErrorAndCancelKeepTheSameFakeApplyContract() {
        var calls = 0; var dismissed = 0
        var open by mutableStateOf(true)
        compose.setContent {
            val context = ContextThemeWrapper(LocalContext.current, R.style.AppThemeWhite)
            CompositionLocalProvider(LocalContext provides context) {
                RethinkTheme {
                    if (open) DnsServerEditor(DnsServerKind.ODOH, { "Fixture resolver" },
                        { _, _, _, _, _ -> calls++; "Fixture validation error" }, { dismissed++; open = false })
                }
            }
        }
        compose.onNodeWithTag("dns_error").assertDoesNotExist()
        compose.onNodeWithTag("dns_editor_add").performClick()
        compose.onNodeWithTag("dns_error").performScrollTo().assertTextEquals("Fixture validation error")
        compose.onNodeWithTag("dns_editor_cancel").performClick()
        compose.runOnIdle { assertEquals(1, calls); assertEquals(1, dismissed) }
        compose.onNodeWithTag("dns_name").assertDoesNotExist()
    }
}
