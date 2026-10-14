/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.visual

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import com.celzero.bravedns.ui.compose.chrome.HomeBottomNavigation
import com.celzero.bravedns.ui.compose.home.HomeTourAnchors
import com.celzero.bravedns.ui.compose.networksheets.OrbotInfoContent
import com.celzero.bravedns.ui.compose.utilities.SponsorSuccessScreen
import com.celzero.bravedns.ui.compose.utilities.SponsorSuccessUiState
import com.celzero.bravedns.ui.compose.utilities.SupportScreen
import com.celzero.bravedns.ui.compose.utilities.SupportUiState
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Native rasterization of only pure content in ComponentActivity, with a plain Application. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w800dp-h1000dp-mdpi")
class ContentVisualParityTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val active = mutableStateOf(visualCases.first())
    private val failures = mutableListOf<String>()

    @Test fun supportCapturedAgainstRetainedXmlAcrossConfigurations() {
        compose.setContent { Scene { context ->
            SupportScreen(remember(context) { SupportUiState(context).apply {
                headerTitle = "Synthetic support"
                tvHeroSubtitle.text = "Synthetic subscription · ID: example"
                btnSendEmail.setOnClickListener { error("This visual fixture must never send anything") }
            } })
        } }
        visualCases.forEach { scenario ->
            val context = show(scenario)
            val baseline = inflateBaseline(context, R.layout.activity_customer_support)
            baseline.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar).title = "Synthetic support"
            baseline.findViewById<TextView>(R.id.tv_hero_subtitle).text = "Synthetic subscription · ID: example"
            compose.runOnUiThread {
                attachBaselineOffscreen(compose.activity, baseline, scenario.width, 800)
                layoutBaseline(baseline, scenario.width, 800)
            }
            compose.waitForIdle()
            compose.runOnUiThread { baseline.jumpDrawablesToCurrentState() }
            val evidence = VisualEvidence("support", scenario)
            evidence.capture(captureBaseline(baseline, context.color(R.attr.background)), capture())
            listOf(R.string.support_section_describe, R.string.support_issue_category_label,
                R.string.support_section_attachments, R.string.support_attachments_desc,
                R.string.support_attach_sub_status, R.string.support_attach_vpn_stats,
                R.string.support_attach_proc_info, R.string.support_privacy_note).forEach { id ->
                val label = context.getString(id)
                evidence.text(context.resources.getResourceEntryName(id), baseline.textView(label), text(label))
            }
            evidence.text("hero subtitle", baseline.findViewById(R.id.tv_hero_subtitle), text("Synthetic subscription · ID: example"))
            val send = compose.onNodeWithTag("activity_customer_support.btn_send_email")
            send.assertIsDisplayed().assertHasClickAction().assertIsEnabled()
            evidence.check("send touch target", send.fetchSemanticsNode().touchBoundsInRoot.height >= 48f, "minimum 48 dp")
            text(context.getString(R.string.support_section_describe)).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            compose.onNodeWithTag("activity_customer_support.switch_attach_status")
                .assertContentDescriptionEquals(context.getString(R.string.support_attach_sub_status))
            evidence.review("expanded header height", compose.onNodeWithTag("support.header").fetchSemanticsNode().boundsInRoot.height == 180f,
                "Expanded content header must remain 180 dp before interaction")
            failures += evidence.finish()
            if (baseline.parent != null) compose.runOnUiThread { detachBaseline(baseline) }
        }
        assertRequiredChecks()
    }

    @Test fun sponsorSuccessCapturedAgainstRetainedXmlAcrossConfigurations() {
        compose.setContent { Scene { context ->
            SponsorSuccessScreen(remember(context) { SponsorSuccessUiState(context).apply {
                continueButton.setOnClickListener { error("Capture cannot activate navigation") }
            } })
        } }
        visualCases.forEach { scenario ->
            val context = show(scenario)
            val baseline = inflateBaseline(context, R.layout.fragment_sponsor_success)
            compose.runOnUiThread {
                attachBaselineOffscreen(compose.activity, baseline, scenario.width, 800)
                layoutBaseline(baseline, scenario.width, 800)
            }
            compose.waitForIdle()
            compose.runOnUiThread { baseline.jumpDrawablesToCurrentState() }
            val evidence = VisualEvidence("sponsor-success", scenario)
            evidence.capture(captureBaseline(baseline, context.color(R.attr.background)), capture())
            listOf(R.id.success_title, R.id.success_message, R.id.continue_button).forEach { id ->
                val old = baseline.findViewById<TextView>(id)
                evidence.text(context.resources.getResourceEntryName(id), old, text(old.text.toString()), compareColor = true)
            }
            text(context.getString(R.string.sponsor_thank_you)).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            val action = compose.onNodeWithTag("fragment_sponsor_success.continue_button")
            action.performScrollTo().assertIsDisplayed().assertHasClickAction()
            evidence.check("continue touch target", action.fetchSemanticsNode().touchBoundsInRoot.height >= 48f, "minimum 48 dp, reachable after scroll")
            failures += evidence.finish()
            if (baseline.parent != null) compose.runOnUiThread { detachBaseline(baseline) }
        }
        assertRequiredChecks()
    }

    @Test fun orbotInfoCapturedAgainstRetainedXmlAcrossConfigurations() {
        val title = "Synthetic Orbot information"
        val description = "Synthetic connection information.\nSecond line of selectable description.\nThird line checks baseline spacing."
        compose.setContent { Scene(R.attr.colorSurfaceContainerHigh) { OrbotInfoContent(title, description) {
            error("Capture cannot dismiss the synthetic content")
        } } }
        visualCases.forEach { scenario ->
            val context = show(scenario)
            val baseline = inflateBaseline(context, R.layout.dialog_info_rules_layout)
            baseline.findViewById<TextView>(R.id.info_rules_dialog_rules_title).text = title
            baseline.findViewById<TextView>(R.id.info_rules_dialog_rules_desc).text = description
            compose.runOnUiThread {
                attachBaselineOffscreen(compose.activity, baseline, scenario.width, 800)
                layoutBaseline(baseline, scenario.width, 800)
            }
            compose.waitForIdle()
            compose.runOnUiThread { baseline.jumpDrawablesToCurrentState() }
            val evidence = VisualEvidence("orbot-info", scenario)
            evidence.capture(captureBaseline(baseline, context.color(R.attr.colorSurfaceContainerHigh)), capture())
            evidence.text("title", baseline.findViewById(R.id.info_rules_dialog_rules_title), compose.onNodeWithTag("orbot_info_title"))
            evidence.text("description", baseline.findViewById(R.id.info_rules_dialog_rules_desc), compose.onNodeWithTag("orbot_info_description"))
            val dismiss = compose.onNodeWithContentDescription(context.getString(R.string.lbl_dismiss))
            dismiss.assertIsDisplayed().assertHasClickAction()
            evidence.check("dismiss touch target", dismiss.fetchSemanticsNode().touchBoundsInRoot.height >= 48f, "minimum 48 dp (baseline icon is 30 dp)")
            // SelectionContainer exposes selection actions on Text; no links or external intents are installed.
            compose.onNodeWithTag("orbot_info_description").assert(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult))
            failures += evidence.finish()
            if (baseline.parent != null) compose.runOnUiThread { detachBaseline(baseline) }
        }
        assertRequiredChecks()
    }

    @Test fun rootNavigationCapturedAgainstBaselineWidgetAcrossConfigurations() {
        val ids = listOf(R.id.homeScreenFragment, R.id.summaryStatisticsFragment, R.id.rethinkPlus, R.id.configureFragment, R.id.aboutFragment)
        compose.setContent { Scene(height = null) {
            HomeBottomNavigation(R.id.homeScreenFragment, remember { HomeTourAnchors() }) { error("Capture cannot navigate") }
        } }
        visualCases.forEach { scenario ->
            val context = show(scenario)
            // Extract the baseline widget only: inflating activity_home_screen would create NavHostFragment.
            // Constructor defaults, labeled mode, drawable and menu match pinned XML lines 19–33.
            val baseline = BottomNavigationView(context).apply {
                setBackgroundResource(R.drawable.nav_menu_background)
                labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
                inflateMenu(R.menu.bottom_nav_menu)
                selectedItemId = R.id.homeScreenFragment
            }
            compose.runOnUiThread {
                layoutBaseline(baseline, scenario.width, null)
                attachBaselineOffscreen(compose.activity, baseline, scenario.width, baseline.height)
            }
            compose.waitForIdle()
            compose.runOnUiThread {
                baseline.jumpDrawablesToCurrentState()
                layoutBaseline(baseline, scenario.width, null)
            }
            val evidence = VisualEvidence("root-navigation", scenario)
            evidence.capture(captureBaseline(baseline, context.color(R.attr.background)), capture())
            val indicator = descendants(baseline).first { it.id == com.google.android.material.R.id.navigation_bar_item_active_indicator_view }
            evidence.check("reference selected indicator is measured", indicator.width > 0 && indicator.height > 0,
                "width=${indicator.width}, height=${indicator.height}, scaleX=${indicator.scaleX}, scaleY=${indicator.scaleY}, alpha=${indicator.alpha}, visibility=${indicator.visibility}")
            ids.forEachIndexed { index, id ->
                val item = compose.onNodeWithTag("root_nav_$id")
                item.assertIsDisplayed().assertHasClickAction()
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
                if (index == 0) item.assertIsSelected() else item.assertIsNotSelected()
                evidence.check("tab $index target", item.fetchSemanticsNode().touchBoundsInRoot.height >= 48f, "minimum 48 dp")
                val label = baseline.menu.findItem(id).title.toString()
                val oldLabels = descendants(baseline).filterIsInstance<TextView>().filter { it.text.toString() == label }
                val old = oldLabels.first { it.visibility == View.VISIBLE }
                evidence.text("tab $index label", old, text(label))
            }
            val tabCenters = ids.map { compose.onNodeWithTag("root_nav_$it").fetchSemanticsNode().boundsInRoot.center.x }
            evidence.check("tab direction", tabCenters.zipWithNext().all { (a, b) -> if (scenario.rtl) a > b else a < b },
                "RTL=${scenario.rtl}, physical centers=$tabCenters")
            evidence.review("bar height", baseline.height == capture().fetchSemanticsNode().boundsInRoot.height.toInt(),
                "XML=${baseline.height}px, Compose=${capture().fetchSemanticsNode().boundsInRoot.height}px")
            failures += evidence.finish()
            if (baseline.parent != null) compose.runOnUiThread { detachBaseline(baseline) }
        }
        assertRequiredChecks()
    }

    private fun show(scenario: VisualCase): Context {
        compose.runOnIdle { active.value = scenario }
        compose.waitForIdle()
        return scenario.context(compose.activity)
    }
    private fun capture() = compose.onNodeWithTag("visual.capture")
    private fun text(value: String): SemanticsNodeInteraction = compose.onAllNodes(
        hasText(value) and SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult), useUnmergedTree = true)[0]
    private fun assertRequiredChecks() = assertTrue("Required rendering/content/accessibility failures (cosmetic differences are recorded separately):\n" + failures.joinToString("\n"), failures.isEmpty())

    @Composable private fun Scene(background: Int = R.attr.background, height: Int? = 800, content: @Composable (Context) -> Unit) {
        val scenario = active.value
        val base = LocalContext.current
        val context = remember(base, scenario) { scenario.context(base) }
        key(scenario) {
            CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides context.resources.configuration,
                LocalDensity provides Density(context),
                LocalLayoutDirection provides if (scenario.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                RethinkTheme {
                    val size = if (height == null) Modifier.width(scenario.width.dp) else Modifier.requiredSize(scenario.width.dp, height.dp)
                    Box(size.background(Color(context.color(android.R.attr.colorBackground))).background(Color(context.color(background))).testTag("visual.capture")) { content(context) }
                }
            }
        }
    }
}
