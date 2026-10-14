/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.celzero.bravedns.ui.compose.about

import com.celzero.bravedns.ui.compose.controls.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.chrome.homeNavigationClearance
import com.celzero.bravedns.ui.compose.themeColor
import com.celzero.bravedns.ui.compose.wireguard.WgCard
import com.celzero.bravedns.ui.compose.wireguard.WgControlState

@Composable
fun AboutContent(b: AboutState) {
    Surface(color = themeColor(R.attr.background)) {
        LazyColumn(Modifier.fillMaxSize().testTag("about"), contentPadding = PaddingValues(bottom = homeNavigationClearance(60.dp))) {
            item {
                Column(Modifier.padding(15.dp)) {
                    Text(if (b.fhsTitleRethink.isAllCaps) b.fhsTitleRethink.text.toString().uppercase() else b.fhsTitleRethink.text.toString(),
                        Modifier.fillMaxWidth().alpha(.5f).clickable { b.fhsTitleRethink.performClick() }.padding(horizontal = 7.dp).testTag("about.title"),
                        fontSize = dimensionResource(R.dimen.rethink_header_text).value.sp, letterSpacing = 7.sp,
                        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = themeColor(R.attr.colorOnSurface))
                    Text(stringResource(R.string.about_title_desc), Modifier.alpha(.5f).padding(horizontal = 10.dp),
                        fontSize = dimensionResource(R.dimen.small_font_subheading_text).value.sp, color = themeColor(R.attr.colorOnSurface))
                }
            }
            item {
                WgCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // AppCompat.Subhead resolves to primaryTextColor, including Frost and Plus.
                    AboutDescription(stringResource(R.string.about_bravedns_explantion))
                    AboutDescription(stringResource(R.string.about_bravedns_whoarewe))
                    if (b.sponsorInfoUsage.isVisible) AboutDescription(b.sponsorInfoUsage.text.toString())
                    AboutRow(b.aboutSponsor, framed = true, tint = themeColor(R.attr.secondaryTextColor))
                    AboutRow(b.aboutManageRpn, framed = true, tint = themeColor(R.attr.secondaryTextColor))
                    if (b.aboutCommunitySponsor.isVisible) Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(painterResource(R.drawable.ic_sponsor_badge), null, Modifier.size(48.dp).alpha(.7f), tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.sponsor_community_sponsor), Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        Text(stringResource(R.string.sponsor_about_thanks), Modifier.padding(top = 8.dp), color = themeColor(R.attr.colorOnSurfaceVariant), textAlign = TextAlign.Center)
                        Text(b.aboutSponsorSince.text.toString(), Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall, color = themeColor(R.attr.colorOnSurfaceVariant))
                        OriginalButton("fragment_about:about_sponsor_again", { b.aboutSponsorAgain.performClick() }) { Text(b.aboutSponsorAgain.text.toString()) }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 32.dp).padding(top = 20.dp, bottom = 10.dp)) {
                    AboutRow(b.aboutTelegram, framed = true)
                    AboutDescription(stringResource(R.string.about_bravedns_toreport_text), centered = true)
                    if (b.progressLayout.isVisible) Row(Modifier.align(Alignment.CenterHorizontally).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                        Text(stringResource(R.string.collecting_logs_progress_text), Modifier.padding(4.dp), color = themeColor(com.google.android.material.R.attr.colorSecondary))
                    }
                    AboutRow(b.aboutCrashLog, framed = true)
                    AboutDescription(stringResource(R.string.about_bug_report_desc), centered = true)
                }
            }
            item { AboutSection(R.string.about_app) }
            item { AboutRow(b.aboutAppUpdate) }
            item { AboutRow(b.aboutAppContributors) }
            item { AboutRow(b.aboutAppTranslate) }
            item { AboutSection(R.string.about_web) }
            item { AboutRow(b.aboutWebsite) }
            item { AboutRow(b.aboutGithub) }
            item { AboutRow(b.aboutFaq) }
            item { AboutRow(b.aboutBlog) }
            item { AboutRow(b.aboutPrivacyPolicy) }
            item { AboutRow(b.aboutTermsOfService) }
            item { AboutSection(R.string.about_connect) }
            item { AboutRow(b.aboutTwitter) }
            item { AboutRow(b.aboutMail) }
            item { AboutRow(b.aboutReddit) }
            item { AboutRow(b.aboutElement) }
            item { AboutRow(b.aboutMastodon) }
            item { AboutSection(R.string.about_settings) }
            item { AboutRow(b.aboutAppInfo) }
            item { AboutRow(b.aboutVpnProfile) }
            item { AboutRow(b.aboutAppNotification) }
            item { AboutSection(R.string.title_statistics) }
            item { AboutRow(b.aboutStats) }
            item { AboutRow(b.aboutProc) }
            item { AboutRow(b.aboutStackTrace) }
            item { AboutRow(b.aboutMemoryProfile) }
            item { AboutRow(b.aboutDbStats) }
            item { AboutRow(b.aboutConsoleLogs) }
            item { AboutRow(b.aboutEventLogs) }
            item { AboutFooter(b) }
        }
    }
}

@Composable
private fun AboutDescription(text: String, centered: Boolean = false) {
    Text(text, Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 10.dp),
        fontSize = 14.sp, color = themeColor(R.attr.primaryTextColor),
        textAlign = if (centered) TextAlign.Center else TextAlign.Start)
}

@Composable
private fun AboutSection(label: Int) {
    Text(stringResource(label), Modifier.padding(start = 60.dp, top = 16.dp, bottom = 8.dp).padding(5.dp).semantics { heading() },
        style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun AboutRow(node: WgControlState, framed: Boolean = false, tint: Color = themeColor(R.attr.primaryTextColor)) {
    if (!node.isVisible) return
    Row(Modifier.fillMaxWidth().then(if (!framed) Modifier.padding(horizontal = 16.dp) else Modifier)
        .then(if (framed) Modifier.border(1.dp, themeColor(R.attr.border), RoundedCornerShape(4.dp)) else Modifier)
        .clickable(enabled = node.isEnabled) { node.performClick() }.heightIn(min = 48.dp).padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        node.imageResource?.let { Icon(painterResource(it), null, Modifier.size(24.dp), tint = tint) }
        Text(node.text.toString(), Modifier.weight(1f).padding(start = if (framed) 8.dp else 20.dp, end = if (framed) 24.dp else 0.dp),
            fontSize = 14.sp, color = tint, textAlign = if (framed) TextAlign.Center else TextAlign.Start)
    }
}

@Composable
private fun AboutFooter(b: AboutState) {
    Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, themeColor(R.attr.colorSurfaceContainer).copy(alpha = .55f), Color.Transparent)))
        .padding(top = 36.dp, bottom = 64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.about_mozilla), Modifier.padding(horizontal = 24.dp).alpha(.6f), fontSize = 10.sp,
            color = themeColor(R.attr.primaryLightColorText), textAlign = TextAlign.Center)
        Row(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(b.mozillaImg, b.fossImg, b.flossFundsImg).forEach { node ->
                Image(painterResource(requireNotNull(node.imageResource)), stringResource(R.string.about_mozilla),
                    Modifier.size(80.dp, 24.dp).alpha(.75f).clickable { node.performClick() })
            }
        }
        SelectionContainer {
            Text(b.aboutAppVersion.text.toString(), Modifier.fillMaxWidth().padding(top = 28.dp, start = 16.dp, end = 16.dp).alpha(.75f),
                color = themeColor(R.attr.primaryLightColorText), fontSize = 10.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
        }
        if (b.tokenTextView.isVisible) AboutTokenContent(b)
        Text(b.aboutLicense.text.toString(), Modifier.fillMaxWidth().padding(horizontal = 16.dp).alpha(.75f).clickable { b.aboutLicense.performClick() }.padding(top = 8.dp),
            fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = themeColor(R.attr.primaryLightColorText), textAlign = TextAlign.Center)
    }
}
