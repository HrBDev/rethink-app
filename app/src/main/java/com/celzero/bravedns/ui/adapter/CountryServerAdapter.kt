package com.celzero.bravedns.ui.adapter

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.celzero.bravedns.R
import com.celzero.bravedns.database.CountryConfig
import com.celzero.bravedns.ui.compose.themeColor
import com.celzero.bravedns.ui.compose.rpn.*
import com.celzero.bravedns.util.SnackbarHelper.capitalizeWords
import java.util.Locale
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.cardview.R as CardR

/** Native presenter; the original grouping and callback contract is retained. */
class CountryServerAdapter(countries: List<CountryItem>, private val listener: CitySelectionListener) {
    interface CitySelectionListener {
        fun onCitySelected(server: CountryConfig, isEnabled: Boolean)
        /**
         * Called when a server item is tapped while the proxy is stopped.
         * The host should open the settings sheet so the user can restart the proxy.
         */
        fun onProxyStoppedItemTapped()
        /**
         * Called when the user taps the star icon on a country card.
         * [countryCode] identifies the country;
         * [countryName] is provided for user feedback purposes;
         * [isFavourite] is the new desired state;
         */
        fun onFavouriteToggled(countryCode: String, countryName: String, isFavourite: Boolean)
    }

    /**
     * Represents a merged group of servers sharing the same key.
     * Multiple servers with same key are grouped and shown as one selectable item.
     */
    data class ServerGroup(
        val key: String, // Common server key
        val servers: List<CountryConfig>, // All servers with this key
        val city: String, // Primary city name
        val avgLoad: Int, // Average load across servers
        val avgLink: Int, // Average link speed across servers (in Mbps)
        val isEnabled: Boolean // Selection state
    ) {
        val serverCount: Int get() = servers.size
    }

    data class CountryItem(
        val countryCode: String,
        val countryName: String,
        val flagEmoji: String,
        val serverGroups: List<ServerGroup>,  // Changed from cities to serverGroups
        var isFavourite: Boolean = false
    )


    private var countries by mutableStateOf(sortCountries(countries), neverEqualPolicy())
    private val expandedCountries = mutableStateMapOf<String, Boolean>()
    private var proxyStopped by mutableStateOf(false)
    fun setProxyStopped(stopped: Boolean) { if (proxyStopped != stopped) proxyStopped = stopped }
    fun updateCountries(newCountries: List<CountryItem>) {
        countries = sortCountries(newCountries)
        val newCodes = countries.map { it.countryCode }.toSet()
        expandedCountries.keys.retainAll(newCodes)
    }
    private fun sortCountries(list: List<CountryItem>): List<CountryItem> = list.sortedBy { it.countryName.lowercase(Locale.getDefault()) }
    private fun sectionLetter(name: String): String {
        val c = name.trim().uppercase(Locale.getDefault()).firstOrNull() ?: return "#"
        return if (c.isLetter()) c.toString() else "#"
    }
    @Composable
    fun Content(modifier: Modifier = Modifier) {
        Column(modifier) {
            var previous: String? = null
            countries.forEach { item ->
                val letter = sectionLetter(item.countryName)
                if (letter != previous) Text(letter, Modifier.padding(start = 8.dp, top = 14.dp, bottom = 8.dp), style = MaterialTheme.typography.labelSmall)
                previous = letter
                key(item.countryCode) { Country(item) }
            }
        }
    }
    @Composable
    private fun Country(item: CountryItem) {
        val expanded = expandedCountries[item.countryCode] == true
        val arrow by animateFloatAsState(if (expanded) 180f else 0f, label = "country expansion")
        val star = remember { RpnControlState() }
        Column(Modifier.fillMaxWidth().padding(bottom = 8.dp).background(themeColor(CardR.attr.cardBackgroundColor), RoundedCornerShape(16.dp)).border(1.dp, themeColor(R.attr.border), RoundedCornerShape(16.dp))) {
            Row(Modifier.fillMaxWidth().clickable { expandedCountries[item.countryCode] = !expanded }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(item.flagEmoji, fontSize = 26.sp); Text(item.countryCode.uppercase(Locale.getDefault()), fontSize = 10.sp) }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(item.countryName)
                    val total = item.serverGroups.sumOf { it.serverCount }
                    Text(pluralStringResource(R.plurals.server_count, total, total), style = MaterialTheme.typography.bodySmall)
                }
                IconButton({
                    val next = !item.isFavourite
                    star.animate().scaleX(1.35f).scaleY(1.35f).setDuration(120).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction {
                        star.animate().scaleX(1f).scaleY(1f).setDuration(100).setInterpolator(AccelerateDecelerateInterpolator()).start()
                    }.start()
                    item.isFavourite = next
                    countries = countries.toList()
                    listener.onFavouriteToggled(item.countryCode, item.countryName, next)
                }) { Image(painterResource(if (item.isFavourite) R.drawable.ic_star_filled else R.drawable.ic_star_outline), null, Modifier.size(20.dp).rpnMotion(star)) }
                Icon(painterResource(R.drawable.ic_arrow_down_small), null, Modifier.size(20.dp).rotate(arrow))
            }
            if (expanded) item.serverGroups.forEach { group -> key(group.key) {
                Row(Modifier.fillMaxWidth().clickable {
                    if (proxyStopped) listener.onProxyStoppedItemTapped() else listener.onCitySelected(group.servers.first(), !group.isEnabled)
                }.padding(start = 20.dp, end = 14.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(group.city.capitalizeWords())
                        if (group.serverCount > 1) Text(pluralStringResource(R.plurals.server_count, group.serverCount, group.serverCount), style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (group.avgLink > 0) {
                                val (label, attr) = speedInfo(group.avgLink)
                                Text(label, Modifier.background(themeColor(if (group.avgLink >= 10_000) attr else R.attr.chipColorBgNormal), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall)
                            }
                            if (group.avgLoad > 0) {
                                val (label, attr) = loadInfo(group.avgLoad)
                                Box(Modifier.size(5.dp).background(themeColor(attr), RoundedCornerShape(5.dp)))
                                Text(label, color = themeColor(attr), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Checkbox(group.isEnabled, null, Modifier.alpha(if (proxyStopped) .4f else 1f), enabled = !proxyStopped)
                }
            } }
        }
    }
    private fun speedInfo(linkMbps: Int): Pair<String, Int> {
        val formatted: String
        val attr: Int

        when {
            linkMbps >= 10_000 -> {
                val gbps = linkMbps / 1_000.0
                formatted = String.format(Locale.US, "%.0f Gbps", gbps)
                attr = R.attr.chipTextPositive
            }
            linkMbps >= 1_000 -> {
                val gbps = linkMbps / 1_000.0
                formatted = if (gbps == gbps.toLong().toDouble())
                    String.format(Locale.US, "%.0f Gbps", gbps)
                else
                    String.format(Locale.US, "%.1f Gbps", gbps)
                attr = R.attr.accentGood
            }
            linkMbps >= 100 -> {
                formatted = "$linkMbps Mbps"
                attr = R.attr.chipTextNeutral
            }
            linkMbps >= 10 -> {
                formatted = "$linkMbps Mbps"
                attr = R.attr.chipTextNeutral
            }
            else -> {
                formatted = "$linkMbps Mbps"
                attr = R.attr.chipTextNegative
            }
        }
        return Pair(formatted, attr)
    }

    /**
     * Returns (displayText, textColorAttr) for [loadPercent].
     *
     * Tier thresholds:
     *   ≤ 20 → Light
     *   ≤ 40 → Normal
     *   ≤ 60 → Busy
     *   ≤ 80 → Very Busy
     *   > 80 → Overloaded
     */
    private fun loadInfo(loadPercent: Int): Pair<String, Int> {
        val attr: Int

        when {
            loadPercent <= 20 -> {
                attr  = R.attr.chipTextPositive
            }
            loadPercent <= 40 -> {
                attr  = R.attr.accentGood
            }
            loadPercent <= 60 -> {
                attr  = R.attr.chipTextNeutral
            }
            loadPercent <= 80 -> {
                attr  = R.attr.chipTextNegative
            }
            else -> {
                attr  = R.attr.chipTextNegative
            }
        }
        val label = "$loadPercent%"
        return Pair(label, attr)
    }
}
