/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.statistics

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.adapter.SummaryStatisticsAdapter
import com.celzero.bravedns.data.AppConfig
import com.celzero.bravedns.data.AppConnection
import com.celzero.bravedns.service.PersistentState
import com.celzero.bravedns.ui.activity.AppInfoActivity
import com.celzero.bravedns.ui.activity.DomainConnectionsActivity as Destination
import com.celzero.bravedns.ui.fragment.SummaryStatisticsFragment.SummaryStatisticsType as Type
import com.celzero.bravedns.viewmodel.SummaryStatisticsViewModel.TimeCategory
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Captures Intents without launching a component. Strict mocks reject any store/config access. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class SummaryStatisticsRouteTest {
    private val context = RecordingContext(ApplicationProvider.getApplicationContext())
    private val persistent = mockk<PersistentState>()
    private val config = mockk<AppConfig>()
    private val item = AppConnection(10001, "192.0.2.7", 443, 4, "🇳🇿", false, "example.test")
    private fun adapter(type: Type) = SummaryStatisticsAdapter(context, persistent, config, type)
    private fun click(adapter: SummaryStatisticsAdapter) {
        SummaryStatisticsAdapter::class.java.getDeclaredMethod("onRowClick", AppConnection::class.java)
            .apply { isAccessible = true }.invoke(adapter, item)
    }
    private fun verifyNoDependencies() { verify { listOf(persistent, config) wasNot Called } }

    @Test fun allAggregateRowTypesPreserveDestinationExtraNamesValuesAndTimeCategory() {
        data class Route(val type: Type, val input: Destination.InputType, val key: String, val value: String, val blocked: Boolean?)
        val routes = listOf(
            Route(Type.MOST_CONNECTED_ASN, Destination.InputType.ASN, Destination.INTENT_EXTRA_ASN, "example.test", false),
            Route(Type.MOST_BLOCKED_ASN, Destination.InputType.ASN, Destination.INTENT_EXTRA_ASN, "example.test", true),
            Route(Type.MOST_CONTACTED_DOMAINS, Destination.InputType.DOMAIN, Destination.INTENT_EXTRA_DOMAIN, "example.test", false),
            Route(Type.MOST_BLOCKED_DOMAINS, Destination.InputType.DOMAIN, Destination.INTENT_EXTRA_DOMAIN, "example.test", true),
            Route(Type.MOST_BLOCKED_BLOCKLISTS, Destination.InputType.BLOCKLIST, Destination.INTENT_EXTRA_BLOCKLIST, "example.test", true),
            Route(Type.MOST_CONTACTED_IPS, Destination.InputType.IP, Destination.INTENT_EXTRA_IP, "192.0.2.7", false),
            Route(Type.MOST_BLOCKED_IPS, Destination.InputType.IP, Destination.INTENT_EXTRA_IP, "192.0.2.7", true),
            Route(Type.MOST_CONTACTED_COUNTRIES, Destination.InputType.FLAG, Destination.INTENT_EXTRA_FLAG, "🇳🇿", null)
        )
        for (time in TimeCategory.entries) for (route in routes) {
            context.intents.clear()
            click(adapter(route.type).apply { setTimeCategory(time) })
            assertEquals(1, context.intents.size)
            val intent = context.intents.single()
            assertEquals(Destination::class.java.name, intent.component?.className)
            assertEquals(route.input.type, intent.getIntExtra(Destination.INTENT_EXTRA_TYPE, -1))
            assertEquals(route.value, intent.getStringExtra(route.key))
            assertEquals(time.value, intent.getIntExtra(Destination.INTENT_EXTRA_TIME_CATEGORY, -1))
            val expectedKeys = mutableSetOf(Destination.INTENT_EXTRA_TYPE, route.key, Destination.INTENT_EXTRA_TIME_CATEGORY)
            if (route.blocked != null) {
                expectedKeys += Destination.INTENT_EXTRA_IS_BLOCKED
                assertEquals(route.blocked, intent.getBooleanExtra(Destination.INTENT_EXTRA_IS_BLOCKED, !route.blocked))
            }
            assertEquals(expectedKeys, intent.extras!!.keySet())
        }
        verifyNoDependencies()
    }
    @Test fun scopedBlocklistIncludesSelectedUidRatherThanRowUid() {
        click(adapter(Type.MOST_BLOCKED_BLOCKLISTS).apply { setUid(10002); setTimeCategory(TimeCategory.SEVEN_DAYS) })
        assertEquals(1, context.intents.size)
        val intent = context.intents.single()
        assertEquals(10002, intent.getIntExtra(Destination.INTENT_EXTRA_UID, -1))
        assertEquals(2, intent.getIntExtra(Destination.INTENT_EXTRA_TIME_CATEGORY, -1))
        assertEquals(5, intent.extras!!.size())
        verifyNoDependencies()
    }
    @Test fun topActiveConnectionRoutesDirectlyWithOnlyItsUid() {
        click(adapter(Type.TOP_ACTIVE_CONNS))
        assertEquals(1, context.intents.size)
        val intent = context.intents.single()
        assertEquals(AppInfoActivity::class.java.name, intent.component?.className)
        assertEquals(setOf(AppInfoActivity.INTENT_UID), intent.extras!!.keySet())
        assertEquals(10001, intent.getIntExtra(AppInfoActivity.INTENT_UID, -1))
        verifyNoDependencies()
    }
    private class RecordingContext(base: Context) : ContextWrapper(base) {
        val intents = mutableListOf<Intent>()
        override fun startActivity(intent: Intent) { intents += Intent(intent) }
    }
}
