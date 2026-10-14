/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.networksheets

import android.app.Application
import android.view.ContextThemeWrapper
import android.view.View
import android.view.LayoutInflater
import android.content.res.Configuration
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Compose test host + synthetic state and callback records. No controller/service/files/traffic. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class NetworkSheetContentTest {
    @get:Rule val compose = createComposeRule()
    private val app get() = RuntimeEnvironment.getApplication()
    @Test fun orbotRowAndRadioKeepDistinctCallbacksAndHiddenBoth() {
        val state = OrbotSheetState(app)
        val calls = mutableListOf<String>()
        state.bsSocks5OrbotRl.setOnClickListener { calls += "row:${state.bsOrbotRadioSocks5.isChecked}" }
        state.bsOrbotRadioSocks5.setOnClickListener { calls += "radio:${it.isChecked}" }
        compose.setContent { Theme { OrbotSheetContent(state) } }
        compose.onNodeWithTag("orbot_row_${R.string.orbot_both}").assertDoesNotExist()
        compose.onNodeWithTag("orbot_row_${R.string.orbot_socks5}").performScrollTo().performClick()
        compose.onNodeWithTag("orbot_radio_${R.string.orbot_socks5}").performClick().assertIsSelected()
        compose.onNodeWithTag("orbot_radio_${R.string.orbot_socks5}").performClick()
        compose.runOnIdle { assertEquals(listOf("row:false", "radio:true", "radio:true"), calls) }
    }
    @Test fun loadingAndPreQVisibilityPreventInputWithoutHidingStatus() {
        val state = OrbotSheetState(app).apply {
            bsOrbotHttpRl.visibility = View.GONE
            bsSocks5OrbotRl.isClickable = false
            bsOrbotRadioSocks5.isClickable = false
            orbotStatus.text = "Synthetic connecting"
        }
        state.bsOrbotRadioSocks5.setOnClickListener { fail("disabled input dispatched") }
        compose.setContent { Theme { OrbotSheetContent(state) } }
        compose.onNodeWithTag("orbot_row_${R.string.orbot_http}").assertDoesNotExist()
        // Loading removes the radio's action, so its semantics correctly merge into the
        // disabled parent. Inspect the decorative child only in the unmerged tree.
        compose.onNodeWithTag("orbot_row_${R.string.orbot_socks5}").assertIsNotEnabled()
        compose.onNodeWithTag("orbot_radio_${R.string.orbot_socks5}", useUnmergedTree = true).assertIsNotEnabled().assertHasNoClickAction()
        compose.onNodeWithText("Synthetic connecting").assertIsDisplayed()
    }
    @Test fun backupAndRestoreEmitOnlyExplicitSeparateCallbacks() {
        val state = BackupRestoreSheetState(app).apply { brbsAppVersion.text = "Synthetic version" }
        val calls = mutableListOf<String>()
        state.brbsBackup.setOnClickListener { calls += "backup-dialog" }
        state.brbsRestore.setOnClickListener { calls += "restore-dialog" }
        compose.setContent { Theme { BackupRestoreSheetContent(state) } }
        compose.runOnIdle { assertTrue(calls.isEmpty()) }
        compose.onNodeWithTag("backup_row_${R.string.brbs_backup_title}").performScrollTo().performClick()
        compose.onNodeWithTag("backup_row_${R.string.brbs_restore_title}").performScrollTo().performClick()
        compose.onNodeWithText("Synthetic version").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(listOf("backup-dialog", "restore-dialog"), calls) }
    }
    @Test fun warningChoicesRemainDistinctUnderRtlLargeTextAndFrost() {
        val state = RethinkWarningSheetState(app).apply { tvDescription.text = "Synthetic loopback explanation." }
        val calls = mutableListOf<String>()
        state.btnExemptRethink.setOnClickListener { calls += "proceed" }
        state.btnUnderstand.setOnClickListener { calls += "understand" }
        compose.setContent { Theme(R.style.AppThemeTrueBlackFrost, true, 2f) { RethinkWarningSheetContent(state) } }
        compose.onNodeWithText(state.btnExemptRethink.text.toString()).performScrollTo().performClick()
        compose.onNodeWithText(state.btnUnderstand.text.toString()).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("proceed", "understand"), calls) }
    }
    @Test fun countryRowAndRadioDispatchOnceEachAndRenderSelectionFromSource() {
        val state = ProxyCountriesSheetState(app)
        var selection = "AA"
        val calls = mutableListOf<String>()
        state.rows = listOf("AA", "BB"); state.supportsCountryDetails = true
        state.flagFor = { "x" }; state.selection = { selection }
        state.onClick = { calls += it; selection = it }
        compose.setContent { Theme { ProxyCountriesSheetContent(state) } }
        compose.onNodeWithTag("country_radio_AA").assertIsSelected()
        compose.onNodeWithTag("country_row_BB").performClick()
        compose.onNodeWithTag("country_radio_BB").assertIsSelected().performClick()
        compose.runOnIdle { assertEquals(listOf("BB", "BB"), calls) }
    }
    @Test fun informationUsesXmlTypographyAndCloseIsOnlyCallback() {
        var dismiss = 0
        compose.setContent { Theme { OrbotInfoContent("Synthetic title", "Synthetic <not HTML> body") { dismiss++ } } }
        compose.onNodeWithTag("orbot_info_title").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { read ->
            val result = mutableListOf<TextLayoutResult>(); assertTrue(read(result))
            assertEquals(18f, result.single().layoutInput.style.fontSize.value, .01f)
        }
        compose.onNodeWithTag("orbot_info_description").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { read ->
            val result = mutableListOf<TextLayoutResult>(); assertTrue(read(result))
            assertEquals(14f, result.single().layoutInput.style.fontSize.value, .01f)
        }
        compose.runOnIdle { assertEquals(0, dismiss) }
        compose.onNodeWithContentDescription(app.getString(R.string.lbl_dismiss)).performClick()
        compose.runOnIdle { assertEquals(1, dismiss) }
    }
    @Test fun infoLongTextCanScrollWithoutTruncationUnderRtlLargeText() {
        val body = "A long synthetic explanation. ".repeat(70)
        compose.setContent { Theme(R.style.AppTheme, true, 2f) { OrbotInfoContent("Synthetic title", body) {} } }
        compose.onNodeWithTag("orbot_info_description").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { read ->
            val result = mutableListOf<TextLayoutResult>(); assertTrue(read(result)); assertFalse(result.single().hasVisualOverflow)
        }
    }
    @Test fun infoUsesBaselineLineStepAndFirstStrongDirectionAtNonlinearFontScale() {
        val configuration = Configuration(app.resources.configuration).apply {
            fontScale = 2f; setLocale(Locale.ENGLISH); setLayoutDirection(Locale("ar"))
        }
        val context = ContextThemeWrapper(app.createConfigurationContext(configuration), R.style.AppTheme)
        val description = "Synthetic connection information.\nSecond line of selectable description.\nThird line checks baseline spacing."
        val baseline = LayoutInflater.from(context).inflate(R.layout.dialog_info_rules_layout, null, false)
        baseline.layoutDirection = View.LAYOUT_DIRECTION_RTL
        val oldText = baseline.findViewById<TextView>(R.id.info_rules_dialog_rules_desc).apply { text = description }
        val density = context.resources.displayMetrics.density
        val width = (360 * density).toInt()
        val height = (800 * density).toInt()
        baseline.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
        baseline.layout(0, 0, width, height)
        compose.setContent {
            CompositionLocalProvider(LocalContext provides context, LocalDensity provides Density(context), LocalLayoutDirection provides LayoutDirection.Rtl) {
                RethinkTheme { Box(Modifier.width(360.dp)) { OrbotInfoContent("Synthetic Orbot information", description) {} } }
            }
        }
        compose.onNodeWithTag("orbot_info_description").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { read ->
            val results = mutableListOf<TextLayoutResult>(); assertTrue(read(results))
            val result = results.single()
            assertEquals(oldText.layout.getLineBaseline(1) - oldText.layout.getLineBaseline(0),
                (result.getLineBaseline(1) - result.getLineBaseline(0)).toInt())
            assertEquals(oldText.layout.lineCount, result.lineCount)
            assertEquals(androidx.compose.ui.text.style.ResolvedTextDirection.Ltr, result.getParagraphDirection(0))
            assertFalse(result.hasVisualOverflow)
        }
    }
    @Composable private fun Theme(style: Int = R.style.AppThemeWhite, rtl: Boolean = false, scale: Float = 1f, content: @Composable () -> Unit) {
        val base = LocalContext.current
        val context = remember(base, style) { ContextThemeWrapper(base, style) }
        CompositionLocalProvider(LocalContext provides context, LocalDensity provides Density(LocalDensity.current.density, scale),
            LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) { RethinkTheme(content) }
    }
}
