package com.celzero.bravedns.ui.compose.appdetails
import android.app.Application
import android.view.ContextThemeWrapper
import androidx.appcompat.widget.SearchView
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Renderer/state fixtures only. No production Application, Activity, controller or manager is launched. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=Application::class)
class AppDetailsReconstructionTest {
    @get:Rule val compose=createComposeRule()
    @Composable private fun Theme(content:@Composable ()->Unit) {
        val context=ContextThemeWrapper(LocalContext.current,R.style.AppThemeWhite)
        CompositionLocalProvider(LocalContext provides context){RethinkTheme(content)}
    }
    @Test fun selfWarningGatesAllRulesAndProceedDispatchesOnce() {
        val state=AppDetailsState().apply{aadRethinkWarningContainer.visibility=android.view.View.VISIBLE;aadAppParentRl.visibility=android.view.View.GONE}
        val calls=mutableListOf<String>()
        state.aadRethinkWarningProceed.setOnClickListener{calls+="proceed"}
        compose.setContent{Theme{AppDetailsContent(state)}}
        compose.onNodeWithTag("app_rule_0").assertDoesNotExist()
        compose.onNodeWithTag("app_warning_proceed").performScrollTo().performClick()
        compose.runOnIdle{assertEquals(listOf("proceed"),calls)}
    }
    @Test fun sixFirewallActionsRemainDistinctAndCompositionIsSilent() {
        val state=AppDetailsState();val calls=mutableListOf<Int>()
        listOf(state.aadAppSettingsBlockWifi,state.aadAppSettingsBlockMd,state.aadAppSettingsIsolate,state.aadAppSettingsBypassDnsFirewall,state.aadAppSettingsBypassUniv,state.aadAppSettingsExclude).forEachIndexed{i,node->node.setOnClickListener{calls+=i}}
        compose.setContent{Theme{AppDetailsContent(state)}}
        compose.runOnIdle{assertTrue(calls.isEmpty());state.aadAppDetailName.text="Synthetic title"}
        (0..5).forEach{compose.onNodeWithTag("app_rule_$it").performScrollTo().performClick()}
        compose.runOnIdle{assertEquals((0..5).toList(),calls)}
    }
    @Test fun unavailablePackageDisablesExcludeAction() {
        val state=AppDetailsState();state.aadAppSettingsExclude.isEnabled=false
        state.aadAppSettingsExclude.setOnClickListener{error("Disabled action")}
        compose.setContent{Theme{AppDetailsContent(state)}}
        compose.onNodeWithTag("app_rule_5").performScrollTo().assertIsNotEnabled()
    }
    @Test fun notesTypingDoesNotSaveAndCancelOnlyCallsCancel() {
        val draft=AppNotesDraft("Synthetic draft");val calls=mutableListOf<String>()
        compose.setContent{Theme{AppNotesContent(draft,{calls+="save"},{calls+="cancel"})}}
        compose.onNodeWithTag("app_notes_input").performTextReplacement("New text")
        compose.runOnIdle{assertTrue(calls.isEmpty());assertEquals("New text",draft.text)}
        compose.onNodeWithTag("app_notes_cancel").performClick()
        compose.runOnIdle{assertEquals(listOf("cancel"),calls)}
    }
    @Test fun notesSaveOnlyDispatchesAtButton() {
        val draft=AppNotesDraft("fixture");val calls=mutableListOf<String>()
        compose.setContent{Theme{AppNotesContent(draft,{calls+=draft.text.toString()},{error("Unexpected cancel")})}}
        compose.runOnIdle{assertTrue(calls.isEmpty())}
        compose.onNodeWithTag("app_notes_save").performClick()
        compose.runOnIdle{assertEquals(listOf("fixture"),calls)}
    }
    @Test fun middlePasteLimitPreservesSuffixAndSurrogatePairs() {
        val old="a".repeat(249)+"Z".repeat(250)
        val result=filterAppNotes(TextFieldValue(old),TextFieldValue("a".repeat(249)+"PASTED"+"Z".repeat(250),TextRange(255)))
        assertEquals("a".repeat(249)+"P"+"Z".repeat(250),result.text)
        val prefix="a".repeat(499)
        assertEquals(prefix,filterAppNotes(TextFieldValue(prefix),TextFieldValue(prefix+"\uD83D\uDE00")).text)
    }
    @Test fun searchEqualityAndSubmissionMatchSearchViewCallbacks() {
        val state=AppSearchState();val calls=mutableListOf<String>()
        state.setOnQueryTextListener(object:SearchView.OnQueryTextListener {
            override fun onQueryTextChange(value:String):Boolean{calls+="change:$value";return true}
            override fun onQueryTextSubmit(value:String):Boolean{calls+="submit:$value";return true}
        })
        state.setQuery("",false);state.setQuery("draft",false);state.setQuery("draft",false);state.setQuery("draft",true)
        state.setQuery("",false);state.setQuery("",true)
        assertEquals(listOf("change:draft","submit:draft","change:"),calls)
    }
    @Test fun restorationDispatchesOnlyAfterListenerInstallationAndResumeClear() {
        val state=AppSearchState();val calls=mutableListOf<String>()
        calls+="filters initialized"
        state.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextChange(query:String):Boolean{calls+="query:$query";return true}
            override fun onQueryTextSubmit(query:String)=true
        })
        state.setQuery("restored",false);state.setQuery("",false)
        assertEquals(listOf("filters initialized","query:restored","query:"),calls)
    }
    @Test fun topFiveDonutKeepsLegacyNormalization() {
        assertEquals(listOf(240f,120f),appDonutSweeps(listOf(1f,.5f)))
        assertEquals(listOf(0f,0f),appDonutSweeps(listOf(0f,0f)))
    }
}
