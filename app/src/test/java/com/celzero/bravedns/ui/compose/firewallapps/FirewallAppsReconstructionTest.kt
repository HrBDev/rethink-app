package com.celzero.bravedns.ui.compose.firewallapps
import android.app.Application
import android.view.ContextThemeWrapper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.activity.AppListActivity
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** No production Activity/Application, adapter, Koin or service is constructed. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=Application::class)
class FirewallAppsReconstructionTest {
    @get:Rule val compose=createComposeRule()
    @Composable private fun Theme(content:@Composable ()->Unit){
        val context=ContextThemeWrapper(LocalContext.current,R.style.AppThemeWhite)
        CompositionLocalProvider(LocalContext provides context){RethinkTheme(content)}
    }
    @Test fun filterDraftChangesWaitForApply() {
        val state=FirewallFilterState().apply{categories=listOf("Tools","Games")};val calls=mutableListOf<String>()
        state.onParent={calls+="parent:$it"};state.onCategory={v,on->calls+="category:$v:$on"};state.onSort={calls+="sort:${it.name}"}
        state.fsApply.setOnClickListener{calls+="apply"}
        compose.setContent{Theme{FirewallFilterContent(state)}}
        compose.runOnIdle{assertTrue(calls.isEmpty())}
        compose.onNodeWithTag("firewall_category_Games").performScrollTo().performClick()
        compose.onNodeWithTag("firewall_parent_2").performScrollTo().performClick()
        compose.onNodeWithTag("firewall_sort_UID").performScrollTo().performClick()
        compose.runOnIdle{assertEquals(listOf("category:Games:true","parent:2","sort:UID"),calls)}
        compose.onNodeWithTag("firewall_filter_apply").performScrollTo().performClick()
        compose.runOnIdle{assertEquals("apply",calls.last())}
    }
    @Test fun unchangedFilterAndSortReselectionIsSilent() {
        val state=FirewallFilterState();val calls=mutableListOf<String>()
        state.onParent={calls+="parent"};state.onSort={calls+="sort"};state.onCategory={_,_->calls+="category"}
        state.selectParent(0);state.selectSort(AppListActivity.SortOption.NAME)
        state.selectCategory("Fixture",true);state.selectCategory("Fixture",true);state.selectCategory("Fixture",false)
        assertEquals(listOf("category","category"),calls)
    }
    @Test fun firewallChipReselectionDoesNotRefilter() {
        val state=FirewallAppsState();val calls=mutableListOf<Int>();state.onFirewall={calls+=it}
        state.selectFirewall(0);state.selectFirewall(3);state.selectFirewall(3)
        assertEquals(listOf(3),calls)
    }
    @Test fun rowWifiAndMobileHaveSeparateDisabledStates() {
        val state=FirewallAppRowState();val calls=mutableListOf<String>()
        state.firewallAppToggleWifi.setOnClickListener{calls+="wifi";state.firewallAppToggleWifi.isEnabled=false}
        state.firewallAppToggleMobileData.setOnClickListener{calls+="mobile"}
        compose.setContent{Theme{FirewallAppRowContent(state)}}
        compose.runOnIdle{assertTrue(calls.isEmpty())}
        compose.onNodeWithTag("firewall_row_network_0").performClick().assertIsNotEnabled()
        compose.onNodeWithTag("firewall_row_network_1").performClick()
        compose.runOnIdle{assertEquals(listOf("wifi","mobile"),calls)}
    }
    @Test fun ownUidPresentationOmitsBothNetworkControls() {
        val state=FirewallAppRowState().apply{firewallAppToggleWifi.visibility=android.view.View.GONE;firewallAppToggleMobileData.visibility=android.view.View.GONE}
        compose.setContent{Theme{FirewallAppRowContent(state)}}
        compose.onNodeWithTag("firewall_row_network_0").assertDoesNotExist()
        compose.onNodeWithTag("firewall_row_network_1").assertDoesNotExist()
    }
    @Test fun clearRemainsItsOwnExplicitCallback() {
        val state=FirewallFilterState();val calls=mutableListOf<String>()
        state.fsClear.setOnClickListener{calls+="clear"};state.fsApply.setOnClickListener{calls+="apply"}
        compose.setContent{Theme{FirewallFilterContent(state)}}
        compose.onNodeWithTag("firewall_filter_clear").performScrollTo().performClick()
        compose.runOnIdle{assertEquals(listOf("clear"),calls)}
    }
}
