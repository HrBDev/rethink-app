/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.utilities
import android.app.Application
import android.graphics.Typeface
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.em
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnitType
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.reference.BaselineSponsorSuccessReference
import com.celzero.bravedns.ui.compose.reference.BaselineSupportReference
import com.celzero.bravedns.ui.compose.reference.BaselineReachabilityReference
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Offscreen programmatic baseline widgets are reference measurements only. No production host or side effects. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UtilityTypographyTest {
    @get:Rule val compose=createComposeRule()
    @Test fun sponsorSuccessTitleMatchesIndependentBaselineTextView() {
        val context=ContextThemeWrapper(ApplicationProvider.getApplicationContext(),R.style.AppThemeWhite)
        val reference=BaselineSponsorSuccessReference.create(context).title
        val ui=SponsorSuccessUiState(context)
        lateinit var rendered:TextStyle
        compose.setContent {CompositionLocalProvider(LocalContext provides context) {
            rendered=utilityTextStyle(ui.successTitle.textAppearance,ui.successTitle.fontSize,ui.successTitle.fontFamily,ui.successTitle.fontStyle,ui.successTitle.letterSpacing)
            Text(reference.text.toString(),style=rendered)
        }}
        compose.runOnIdle {assertTypography(reference,rendered)}
    }
    @Test fun sponsorSuccessBodyMatchesBaselineBodySizeAndSpacing() {
        val context=ContextThemeWrapper(ApplicationProvider.getApplicationContext(),R.style.AppThemeTrueBlackFrost)
        val reference=BaselineSponsorSuccessReference.create(context).message
        val ui=SponsorSuccessUiState(context)
        lateinit var rendered:TextStyle
        compose.setContent {CompositionLocalProvider(LocalContext provides context) {rendered=utilityTextStyle(ui.successMessage.textAppearance,lineExtraDp=6f);Text(reference.text.toString(),style=rendered)}}
        compose.runOnIdle {
            assertTypography(reference,rendered)
            val metrics=reference.paint.fontMetricsInt
            val expected=(metrics.descent-metrics.ascent+Math.round(reference.lineSpacingExtra))/reference.textSize
            assertEquals(TextUnitType.Em,rendered.lineHeight.type)
            assertEquals(expected,rendered.lineHeight.value,.002f)
        }
    }
    @Test fun supportSectionUsesBaselineAppearanceBoldAndLetterSpacing() {
        val context=ContextThemeWrapper(ApplicationProvider.getApplicationContext(),R.style.AppThemeTrueBlackFrost)
        val root=BaselineSupportReference.create(context).root
        val reference=allText(root).single{it.text.toString()==context.getString(R.string.support_section_describe)}
        lateinit var rendered:TextStyle
        compose.setContent {CompositionLocalProvider(LocalContext provides context) {rendered=utilityTextStyle(com.google.android.material.R.style.TextAppearance_Material3_TitleSmall,weight=Typeface.BOLD,spacing=.08f);Text(reference.text.toString(),style=rendered)}}
        compose.runOnIdle {assertTypography(reference,rendered)}
    }
    @Test fun supportUnstyledAttachmentTitleUsesThemeTextViewDefault() {
        val context=ContextThemeWrapper(ApplicationProvider.getApplicationContext(),R.style.AppThemeWhite)
        val root=BaselineSupportReference.create(context).root
        val reference=allText(root).single{it.text.toString()==context.getString(R.string.support_attach_sub_status)}
        lateinit var rendered:TextStyle
        compose.setContent {CompositionLocalProvider(LocalContext provides context) {rendered=utilityTextStyle(weight=Typeface.BOLD);Text(reference.text.toString(),style=rendered)}}
        compose.runOnIdle {assertTypography(reference,rendered)}
    }
    @Test fun reachabilityInputMatchesBaselineEditTextDefaultAppearance() {
        val context=ContextThemeWrapper(ApplicationProvider.getApplicationContext(),R.style.AppThemeWhite)
        val reference=BaselineReachabilityReference.create(context).ipv4EditText
        lateinit var rendered:TextStyle
        compose.setContent {CompositionLocalProvider(LocalContext provides context) {rendered=utilityTextStyle(defaultStyleAttribute=android.R.attr.editTextStyle);Text("192.0.2.1",style=rendered)}}
        compose.runOnIdle {assertTypography(reference,rendered)}
    }
    @Test fun sponsorLargeFontPreservesRawSpAndRenderedLineStep() {
        val base=ApplicationProvider.getApplicationContext<Application>()
        val context=ContextThemeWrapper(base,R.style.AppThemeWhite).apply {
            applyOverrideConfiguration(android.content.res.Configuration(base.resources.configuration).apply {fontScale=2f})
        }
        val reference=BaselineSponsorSuccessReference.create(context)
        val root=reference.root
        val title=reference.title
        val message=reference.message
        root.measure(View.MeasureSpec.makeMeasureSpec(800,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(800,View.MeasureSpec.EXACTLY))
        root.layout(0,0,800,800)
        val density=Density(context)
        lateinit var titleStyle:TextStyle
        lateinit var bodyLayout:TextLayoutResult
        compose.setContent {CompositionLocalProvider(LocalContext provides context,LocalDensity provides density) {
            Column {
                titleStyle=utilityTextStyle(com.google.android.material.R.style.TextAppearance_Material3_HeadlineLarge,weight=Typeface.BOLD)
                Text(title.text.toString(),style=titleStyle)
                Text(message.text.toString(),style=utilityTextStyle(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge,lineExtraDp=6f),onTextLayout={bodyLayout=it})
            }
        }}
        compose.runOnIdle {
            // TextAppearance rounds pixels while Compose retains subpixels; cosmetic rounding is accepted.
            assertEquals(title.textSize,with(density){titleStyle.fontSize.toPx()},.51f)
            val expected=message.layout.getLineBaseline(1)-message.layout.getLineBaseline(0)
            assertEquals(expected.toFloat(),bodyLayout.getLineBaseline(1)-bodyLayout.getLineBaseline(0),1f)
        }
    }
    private fun assertTypography(reference:TextView,actual:TextStyle) {
        assertEquals(reference.textSize/reference.resources.displayMetrics.scaledDensity,actual.fontSize.value,.01f)
        assertEquals(TextUnitType.Em,actual.letterSpacing.type)
        assertEquals(reference.letterSpacing,actual.letterSpacing.value,.0001f)
        assertEquals(reference.typeface.weight,actual.fontWeight!!.weight)
        assertEquals(reference.typeface.isItalic,actual.fontStyle==androidx.compose.ui.text.font.FontStyle.Italic)
    }
    private fun allText(view:View):List<TextView> = buildList {
        if(view is TextView)add(view)
        if(view is ViewGroup)for(i in 0 until view.childCount)addAll(allText(view.getChildAt(i)))
    }
}
