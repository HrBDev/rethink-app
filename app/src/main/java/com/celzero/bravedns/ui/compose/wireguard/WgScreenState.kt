/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.wireguard

import android.content.Context
import android.os.Bundle
import android.view.View
import com.celzero.bravedns.R
import com.celzero.bravedns.util.UIUtils

open class WgFormState {
    protected val editableFields = linkedMapOf<String, WgControlState>()
    fun saveForm(bundle: Bundle, prefix: String) { editableFields.forEach { (id, node) -> bundle.putString(prefix + id, node.text.toString()) } }
    fun restoreForm(bundle: Bundle?, prefix: String) { editableFields.forEach { (id, node) -> if (bundle?.containsKey(prefix + id) == true) node.setText(bundle.getString(prefix + id)) } }
}

class WgMainState(context: Context) : WgFormState() {
    val wgEmptyView = WgControlState(context).apply {
        visibility = View.GONE
    }
    val wgNoConfigText = WgControlState(context).apply {
        text = context.getString(R.string.wireguard_no_config_msg)
        textSizeSp = context.resources.getDimension(R.dimen.heading_font_text_view) / context.resources.displayMetrics.scaledDensity
    }
    val wgListToggleGroup = WgControlState(context).apply {
    }
    val oneWgToggleBtn = WgControlState(context).apply {
        text = context.getString(R.string.rt_list_simple_btn_txt)
        textColor = UIUtils.fetchColor(context, R.attr.defaultToggleBtnTxt)
    }
    val wgGeneralToggleBtn = WgControlState(context).apply {
        text = context.getString(R.string.lbl_advanced)
        textColor = UIUtils.fetchColor(context, R.attr.defaultToggleBtnTxt)
    }
    val wgWireguardDisclaimer = WgControlState(context).apply {
        text = context.getString(R.string.wireguard_disclaimer)
        alpha = 0.7f
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val wgGeneralInterfaceList = WgControlState(context).apply {
        visibility = View.GONE
    }
    val oneWgInterfaceList = WgControlState(context).apply {
        visibility = View.GONE
    }
    val createFab = WgControlState(context).apply {
        text = context.getString(R.string.lbl_create)
        isClickable = true
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
        imageResource = R.drawable.ic_fab_without_border
        isFocusable = true
    }
    val qrCodeFab = WgControlState(context).apply {
        text = context.getString(R.string.lbl_qr_code)
        isClickable = true
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
        isFocusable = true
    }
    val importFab = WgControlState(context).apply {
        text = context.getString(R.string.lbl_import)
        isClickable = true
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
        isFocusable = true
    }
    val deleteAllFab = WgControlState(context).apply {
        text = context.getString(R.string.config_delete_all_dialog_title)
        isClickable = true
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
        isFocusable = true
    }
    val wgAddFab = WgControlState(context).apply {
        isClickable = true
        imageResource = R.drawable.ic_fab_without_border
        isFocusable = true
    }
}

class WgEditorState(context: Context) : WgFormState() {
    val wgConfigLayout = WgControlState(context).apply {
    }
    val interfaceTitle = WgControlState(context).apply {
        applyTextAppearance(context, this, com.google.android.material.R.style.TextAppearance_Material3_LabelLarge)
        text = context.getString(R.string.lbl_configure)
        textColor = UIUtils.fetchColor(context, androidx.appcompat.R.attr.colorPrimary)
    }
    val interfaceNameLayout = WgControlState(context).apply {
        hint = context.getString(R.string.cd_dns_crypt_dialog_name)
    }
    val interfaceNameText = WgControlState(context).apply {
        noSuggestions = true
        imeNext = true
    }
    val privateKeyTextLayout = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_private_key)
    }
    val privateKeyText = WgControlState(context).apply {
        isPassword = true
        noSuggestions = true
        imeNext = true
    }
    val publicKeyLabelLayout = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_public_key)
    }
    val publicKeyText = WgControlState(context).apply {
        maxLines = 1
        ellipsize = true
        hint = context.getString(R.string.lbl_generated)
        isFocusable = false
        imeNext = true
    }
    val addressesLabelLayout = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_addresses)
    }
    val addressesLabelText = WgControlState(context).apply {
        noSuggestions = true
        imeNext = true
    }
    val listenPortLabelLayout = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_listen_port)
    }
    val listenPortText = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_random)
        imeNext = true
    }
    val dnsServersLabelLayout = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_dns_servers)
    }
    val dnsServersText = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_auto)
        noSuggestions = true
        imeNext = true
    }
    val mtuLabelLayout = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_mtu)
    }
    val mtuText = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_auto)
    }
    val amzProps = WgControlState(context).apply {
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val lbActionContainer = WgControlState(context).apply {
    }
    val dismissBtn = WgControlState(context).apply {
        applyTextAppearance(context, this, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
        text = context.getString(R.string.lbl_dismiss)
        alpha = 0.7f
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val saveTunnel = WgControlState(context).apply {
        applyTextAppearance(context, this, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
        text = context.getString(R.string.lbl_save)
        textColor = UIUtils.fetchColor(context, androidx.appcompat.R.attr.colorPrimary)
    }
    init {
        editableFields["interfaceNameText"] = interfaceNameText
        editableFields["privateKeyText"] = privateKeyText
        editableFields["publicKeyText"] = publicKeyText
        editableFields["addressesLabelText"] = addressesLabelText
        editableFields["listenPortText"] = listenPortText
        editableFields["dnsServersText"] = dnsServersText
        editableFields["mtuText"] = mtuText
    }
}

class WgDetailState(context: Context) : WgFormState() {
    val interfaceDetailCard = WgControlState(context).apply {
        strokeWidth = (0f * context.resources.displayMetrics.density).toInt()
        radiusDp = 18f
    }
    val configTitleLayout = WgControlState(context).apply {
    }
    val interfaceFlagText = WgControlState(context).apply {
        maxLines = 1
        applyTextAppearance(context, this, androidx.appcompat.R.style.TextAppearance_AppCompat_Headline)
        visibility = View.GONE
        textSizeSp = 25f
    }
    val configNameText = WgControlState(context).apply {
        applyTextAppearanceAttribute(context, this, com.google.android.material.R.attr.textAppearanceHeadline6)
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val configIdText = WgControlState(context).apply {
        maxLines = 1
        ellipsize = true
        alpha = 0.7f
    }
    val statusText = WgControlState(context).apply {
        textStyle = 2
    }
    val publicKeyLabel = WgControlState(context).apply {
        text = context.getString(R.string.lbl_public_key)
        visibility = View.GONE
    }
    val publicKeyText = WgControlState(context).apply {
        maxLines = 1
        ellipsize = true
        visibility = View.GONE
    }
    val addressesLabel = WgControlState(context).apply {
        text = context.getString(R.string.lbl_addresses)
        visibility = View.GONE
    }
    val addressesText = WgControlState(context).apply {
        visibility = View.GONE
    }
    val dnsServersLabel = WgControlState(context).apply {
        text = context.getString(R.string.lbl_dns_servers)
        visibility = View.GONE
    }
    val dnsServersText = WgControlState(context).apply {
        visibility = View.GONE
    }
    val dnsSearchDomainsLabel = WgControlState(context).apply {
        text = ""
        visibility = View.GONE
    }
    val dnsSearchDomainsText = WgControlState(context).apply {
        visibility = View.GONE
    }
    val listenPortLabel = WgControlState(context).apply {
        text = ""
        visibility = View.GONE
    }
    val listenPortText = WgControlState(context).apply {
        visibility = View.GONE
    }
    val mtuLabel = WgControlState(context).apply {
        text = ""
        visibility = View.GONE
    }
    val mtuText = WgControlState(context).apply {
        visibility = View.GONE
    }
    val rowClientInfo = WgControlState(context).apply {
    }
    val labelClientInfo = WgControlState(context).apply {
        text = context.getString(R.string.lbl_client_ipv4)
    }
    val shimmerClientInfo = WgControlState(context).apply {
        visibility = View.VISIBLE
    }
    val valueIpv4 = WgControlState(context).apply {
        visibility = View.GONE
    }
    val valueIpv6 = WgControlState(context).apply {
        visibility = View.GONE
    }
    val editDeleteLayout = WgControlState(context).apply {
    }
    val editBtn = WgControlState(context).apply {
        text = context.getString(R.string.rt_edit_dialog_positive)
        textSizeSp = context.resources.getDimension(R.dimen.heading_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val deleteBtn = WgControlState(context).apply {
        text = context.getString(R.string.ci_delete)
        textSizeSp = context.resources.getDimension(R.dimen.heading_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.accentBad)
    }
    val actionsCard = WgControlState(context).apply {
        strokeWidth = (0f * context.resources.displayMetrics.density).toInt()
        radiusDp = 18f
    }
    val applicationsBtnX = WgControlState(context).apply {
    }
    val appsLabel = WgControlState(context).apply {
        maxLines = 1
        ellipsize = true
        text = "Apps(0)"
        textSizeSp = 12f
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
        isAllCaps = false
    }
    val hopBtnX = WgControlState(context).apply {
    }
    val hopIcon = WgControlState(context).apply {
        alpha = 0.5f
        imageResource = R.drawable.ic_network_tunnel
    }
    val hopLabel = WgControlState(context).apply {
        maxLines = 1
        text = "Relay"
        textSizeSp = 12f
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
        isAllCaps = false
    }
    val hopBadge = WgControlState(context).apply {
        visibility = View.GONE
    }
    val logsBtnX = WgControlState(context).apply {
    }
    val otherSettingsCard = WgControlState(context).apply {
        strokeWidth = (0f * context.resources.displayMetrics.density).toInt()
        radiusDp = 18f
    }
    val wgHeaderTv = WgControlState(context).apply {
        applyTextAppearanceAttribute(context, this, com.google.android.material.R.attr.textAppearanceHeadline6)
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val lockdownRl = WgControlState(context).apply {
    }
    val lockdownTitleTv = WgControlState(context).apply {
        applyTextAppearance(context, this, androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead)
        text = context.getString(R.string.firewall_rule_global_lockdown)
        textSizeSp = context.resources.getDimension(R.dimen.large_font_text_view) / context.resources.displayMetrics.scaledDensity
    }
    val lockdownCheck = WgControlState(context, checkable = true).apply {
    }
    val catchAllRl = WgControlState(context).apply {
    }
    val catchAllTitleTv = WgControlState(context).apply {
        applyTextAppearance(context, this, androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead)
        text = context.getString(R.string.catch_all_wg_dialog_title)
        textSizeSp = context.resources.getDimension(R.dimen.large_font_text_view) / context.resources.displayMetrics.scaledDensity
    }
    val catchAllCheck = WgControlState(context, checkable = true).apply {
    }
    val oneWgInfoTv = WgControlState(context).apply {
        text = context.getString(R.string.one_wg_dialog_desc)
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val mobileSsidSettingsCard = WgControlState(context).apply {
        strokeWidth = (0f * context.resources.displayMetrics.density).toInt()
        radiusDp = 18f
    }
    val msWgHeaderTv = WgControlState(context).apply {
        applyTextAppearanceAttribute(context, this, com.google.android.material.R.attr.textAppearanceHeadline6)
        text = context.getString(R.string.adv_taster_title)
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val useMobileRl = WgControlState(context).apply {
    }
    val useMobileTitleTv = WgControlState(context).apply {
        applyTextAppearance(context, this, androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead)
        text = context.getString(R.string.wg_setting_use_on_mobile)
        textSizeSp = context.resources.getDimension(R.dimen.large_font_text_view) / context.resources.displayMetrics.scaledDensity
    }
    val useMobileCheck = WgControlState(context, checkable = true).apply {
    }
    val ssidFilterRl = WgControlState(context).apply {
    }
    val ssidTitleTv = WgControlState(context).apply {
        applyTextAppearance(context, this, androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead)
        text = context.getString(R.string.wg_setting_ssid_title)
        textSizeSp = context.resources.getDimension(R.dimen.large_font_text_view) / context.resources.displayMetrics.scaledDensity
    }
    val ssidDescTv = WgControlState(context).apply {
        text = context.getString(R.string.wg_setting_ssid_desc)
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val ssidCheck = WgControlState(context, checkable = true).apply {
    }
    val ssidEditGroup = WgControlState(context).apply {
        visibility = View.VISIBLE
    }
    val ssidEditText = WgControlState(context).apply {
        maxLines = 4
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val ssidOkBtn = WgControlState(context).apply {
        imageResource = R.drawable.ic_tick
    }
    val ssidDisplayGroup = WgControlState(context).apply {
        visibility = View.GONE
    }
    val ssidValueTv = WgControlState(context).apply {
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val ssidEditBtn = WgControlState(context).apply {
        alpha = 0.5f
        imageResource = R.drawable.ic_edit_icon_grey
    }
    val ssidPermissionErrorLayout = WgControlState(context).apply {
        visibility = View.GONE
    }
    val ssidPermissionErrorText = WgControlState(context).apply {
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.accentBad)
    }
    val ssidPermissionErrorAction = WgControlState(context).apply {
        text = context.getString(R.string.ssid_permission_error_action)
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.accentGood)
        textStyle = 1
    }
    val ssidLocationErrorLayout = WgControlState(context).apply {
        visibility = View.GONE
    }
    val ssidLocationErrorText = WgControlState(context).apply {
        text = context.getString(R.string.ssid_location_error)
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.accentBad)
    }
    val ssidLocationErrorAction = WgControlState(context).apply {
        text = context.getString(R.string.ssid_location_error_action)
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.accentGood)
        textStyle = 1
    }
    val peersList = WgControlState(context).apply {
    }
    val addPeerFab = WgControlState(context).apply {
        text = context.getString(R.string.add_peer)
        isClickable = true
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
        isFocusable = true
    }
    init {
        editableFields["ssidEditText"] = ssidEditText
    }
}

class WgPeerState(context: Context) : WgFormState() {
    val peerInputContainer = WgControlState(context).apply {
    }
    val peerTitle = WgControlState(context).apply {
        text = context.getString(R.string.add_peer)
        textSizeSp = context.resources.getDimension(R.dimen.extra_large_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.secondaryTextColor)
    }
    val publicKeyLabelLayout = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_public_key)
    }
    val peerPublicKey = WgControlState(context).apply {
        noSuggestions = true
        imeNext = true
    }
    val preSharedKeyLabelLayout = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_preshared_key)
    }
    val peerPresharedKey = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_optional)
        isPassword = true
        noSuggestions = true
        imeNext = true
    }
    val persistentKeepaliveLabelLayout = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_persistent_keepalive)
    }
    val peerPersistentKeepAlive = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_optional)
        imeNext = true
    }
    val keepAliveHint = WgControlState(context).apply {
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val endpointLabelLayout = WgControlState(context).apply {
        hint = context.getString(R.string.parse_error_inet_endpoint)
    }
    val peerEndpoint = WgControlState(context).apply {
        noSuggestions = true
        imeNext = true
    }
    val allowedIpsLabelLayout = WgControlState(context).apply {
        hint = context.getString(R.string.lbl_allowed_ips)
    }
    val peerAllowedIps = WgControlState(context).apply {
        maxLines = 4
        noSuggestions = true
    }
    val lbActionContainer = WgControlState(context).apply {
    }
    val customDialogDismissButton = WgControlState(context).apply {
        applyTextAppearance(context, this, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
        text = context.getString(R.string.lbl_dismiss)
        alpha = 0.7f
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val customDialogOkButton = WgControlState(context).apply {
        applyTextAppearance(context, this, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
        text = context.getString(R.string.lbl_save)
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val customDialogOkProgress = WgControlState(context).apply {
        visibility = View.GONE
    }
    init {
        editableFields["peerPublicKey"] = peerPublicKey
        editableFields["peerPresharedKey"] = peerPresharedKey
        editableFields["peerPersistentKeepAlive"] = peerPersistentKeepAlive
        editableFields["peerEndpoint"] = peerEndpoint
        editableFields["peerAllowedIps"] = peerAllowedIps
    }
}

class WgSsidState(context: Context) : WgFormState() {
    val descriptionTextView = WgControlState(context).apply {
        textSizeSp = 14f
    }
    val ssidRecyclerView = WgControlState(context).apply {
        visibility = View.GONE
    }
    val emptyStateView = WgControlState(context).apply {
        text = context.getString(R.string.country_ssid_empty_state)
        visibility = View.VISIBLE
        alpha = 0.6f
        textSizeSp = 14f
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val ssidConditionHeading = WgControlState(context).apply {
        text = context.getString(R.string.lbl_action)
        textSizeSp = 12f
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val ssidConditionRadioGroup = WgControlState(context).apply {
    }
    val radioEqual = WgControlState(context, checkable = true).apply {
        text = context.getString(R.string.lbl_connect)
        isChecked = true
    }
    val radioNotEqual = WgControlState(context, checkable = true).apply {
        text = context.getString(R.string.notification_action_pause_vpn)
    }
    val ssidMatchTypeRadioGroup = WgControlState(context).apply {
    }
    val radioExact = WgControlState(context, checkable = true).apply {
        text = context.getString(R.string.wg_ssid_type_exact)
    }
    val radioWildcard = WgControlState(context, checkable = true).apply {
        text = context.getString(R.string.wg_ssid_type_wildcard)
        isChecked = true
    }
    val ssidTextInputLayout = WgControlState(context).apply {
    }
    val ssidEditText = WgControlState(context).apply {
    }
    val addSsidBtn = WgControlState(context).apply {
        text = context.getString(R.string.lbl_add)
        textColor = UIUtils.fetchColor(context, R.attr.secondaryTextColor)
    }
    val cancelBtn = WgControlState(context).apply {
        text = context.getString(R.string.lbl_cancel)
        visibility = View.GONE
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val saveBtn = WgControlState(context).apply {
        text = context.getString(R.string.fapps_info_dialog_positive_btn)
        textColor = UIUtils.fetchColor(context, R.attr.secondaryTextColor)
    }
    init {
        editableFields["ssidEditText"] = ssidEditText
    }
    init {
        radioEqual.setOnCheckedChangeListener { _, checked -> if (checked) { radioNotEqual.isChecked = false; ssidConditionRadioGroup.callOnClick() } }
        radioNotEqual.setOnCheckedChangeListener { _, checked -> if (checked) { radioEqual.isChecked = false; ssidConditionRadioGroup.callOnClick() } }
        radioExact.setOnCheckedChangeListener { _, checked -> if (checked) { radioWildcard.isChecked = false; ssidMatchTypeRadioGroup.callOnClick() } }
        radioWildcard.setOnCheckedChangeListener { _, checked -> if (checked) { radioExact.isChecked = false; ssidMatchTypeRadioGroup.callOnClick() } }
    }
}

class WgHopState(context: Context) : WgFormState() {
    val wgHopDialogHeading = WgControlState(context).apply {
        applyTextAppearance(context, this, androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead)
        text = context.getString(R.string.hop_add_remove_title)
        textSizeSp = context.resources.getDimension(R.dimen.extra_large_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.accentGood)
    }
    val wgHopRecyclerView = WgControlState(context).apply {
        isClickable = true
        isFocusable = true
    }
    val wgHopDialogOkButton = WgControlState(context).apply {
        text = context.getString(R.string.ada_noapp_dialog_positive)
        textSizeSp = context.resources.getDimension(R.dimen.large_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.accentGood)
    }
}

class WgIncludeAppsState(context: Context) : WgFormState() {
    val filters = androidx.compose.runtime.mutableStateListOf<WgControlState>()
    val wgIncludeAppDialogHeading = WgControlState(context).apply {
        applyTextAppearance(context, this, androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead)
        text = context.getString(R.string.add_remove_apps)
        textSizeSp = context.resources.getDimension(R.dimen.large_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.accentGood)
    }
    val wgIncludeAppDialogSearchLayout = WgControlState(context).apply {
    }
    val wgIncludeAppDialogSearchView = WgControlState(context).apply {
        isFocusable = false
    }
    val wgIncludeAppDialogSearchFilter = WgControlState(context).apply {
        visibility = View.GONE
        imageResource = R.drawable.ic_filter
    }
    val wgRefreshList = WgControlState(context).apply {
        imageResource = R.drawable.ic_refresh_white
    }
    val wgIncludeAppDialogChipGroup = WgControlState(context).apply {
    }
    val wgIncludeAppSelectAll = WgControlState(context).apply {
    }
    val wgIncludeAppBulkCheck = WgControlState(context, checkable = true).apply {
        text = context.getString(R.string.lbl_select_all)
        textSizeSp = 13f
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val wgIncludeAppDeselectAllCheck = WgControlState(context, checkable = true).apply {
        text = context.getString(R.string.lbl_unselect_all)
        textSizeSp = 13f
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val wgIncludeAppRecyclerViewDialog = WgControlState(context).apply {
        isClickable = true
        isFocusable = true
    }
}

class WgConfigRowState(context: Context) : WgFormState() {
    val interfaceDetailCard = WgControlState(context).apply {
        isClickable = true
        radiusDp = 16f
        isFocusable = true
    }
    val configTitleLayout = WgControlState(context).apply {
    }
    val interfaceNameLayout = WgControlState(context).apply {
    }
    val interfaceFlagText = WgControlState(context).apply {
        maxLines = 1
        applyTextAppearance(context, this, androidx.appcompat.R.style.TextAppearance_AppCompat_Headline)
        visibility = View.GONE
        textSizeSp = 25f
    }
    val interfaceIdText = WgControlState(context).apply {
        maxLength = 5
        alpha = 0.7f
    }
    val interfaceNameText = WgControlState(context).apply {
        maxLines = 1
        ellipsize = true
        applyTextAppearanceAttribute(context, this, com.google.android.material.R.attr.textAppearanceHeadline6)
    }
    val protocolInfoChipGroup = WgControlState(context).apply {
        visibility = View.GONE
    }
    val protocolInfoChipIpv4 = WgControlState(context).apply {
        text = context.getString(R.string.settings_ip_text_ipv4)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val protocolInfoChipIpv6 = WgControlState(context).apply {
        text = context.getString(R.string.settings_ip_text_ipv6)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val chipSplitTunnel = WgControlState(context).apply {
        text = context.getString(R.string.lbl_split)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val chipAmnezia = WgControlState(context).apply {
        text = context.getString(R.string.lbl_amnezia)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val chipHopSrc = WgControlState(context).apply {
        text = context.getString(R.string.lbl_hopping)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val chipHopping = WgControlState(context).apply {
        text = context.getString(R.string.lbl_hop)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val chipProperties = WgControlState(context).apply {
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val interfaceStatus = WgControlState(context).apply {
        visibility = View.GONE
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
        textStyle = 2
    }
    val interfaceAppsCount = WgControlState(context).apply {
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val interfaceActiveLayout = WgControlState(context).apply {
        visibility = View.GONE
    }
    val interfaceActiveUptime = WgControlState(context).apply {
        visibility = View.VISIBLE
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val interfaceActiveRxTx = WgControlState(context).apply {
        ellipsize = true
        visibility = View.VISIBLE
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val interfaceSwitch = WgControlState(context, checkable = true).apply {
        isClickable = true
        textSizeSp = 16f
        textColor = UIUtils.fetchColor(context, androidx.appcompat.R.attr.colorPrimary)
        isFocusable = true
    }
}

class WgOneConfigRowState(context: Context) : WgFormState() {
    val interfaceDetailCard = WgControlState(context).apply {
        isClickable = true
        isFocusable = true
    }
    val configTitleLayout = WgControlState(context).apply {
    }
    val interfaceNameLayout = WgControlState(context).apply {
    }
    val interfaceFlagText = WgControlState(context).apply {
        maxLines = 1
        applyTextAppearance(context, this, androidx.appcompat.R.style.TextAppearance_AppCompat_Headline)
        visibility = View.GONE
        textSizeSp = 25f
    }
    val interfaceIdText = WgControlState(context).apply {
        maxLength = 5
        alpha = 0.7f
    }
    val interfaceNameText = WgControlState(context).apply {
        maxLines = 1
        ellipsize = true
        applyTextAppearanceAttribute(context, this, com.google.android.material.R.attr.textAppearanceHeadline6)
    }
    val protocolInfoChipGroup = WgControlState(context).apply {
        visibility = View.GONE
    }
    val protocolInfoChipIpv4 = WgControlState(context).apply {
        text = context.getString(R.string.settings_ip_text_ipv4)
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val protocolInfoChipIpv6 = WgControlState(context).apply {
        text = context.getString(R.string.settings_ip_text_ipv6)
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val chipSplitTunnel = WgControlState(context).apply {
        text = context.getString(R.string.lbl_split)
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val interfaceStatus = WgControlState(context).apply {
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
        textStyle = 2
    }
    val interfaceAppsCount = WgControlState(context).apply {
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val interfaceActiveLayout = WgControlState(context).apply {
        visibility = View.VISIBLE
    }
    val interfaceActiveUptime = WgControlState(context).apply {
        visibility = View.VISIBLE
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val interfaceActiveRxTx = WgControlState(context).apply {
        ellipsize = true
        visibility = View.VISIBLE
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val oneWgCheck = WgControlState(context, checkable = true).apply {
        isClickable = true
        textSizeSp = 16f
        textColor = UIUtils.fetchColor(context, androidx.appcompat.R.attr.colorPrimary)
        isFocusable = true
    }
}

class WgAppRowState(context: Context) : WgFormState() {
    val wgIncludeCard = WgControlState(context).apply {
        strokeWidth = (1f * context.resources.displayMetrics.density).toInt()
        radiusDp = 16f
        backgroundColor = UIUtils.fetchColor(context, R.attr.background)
        strokeColor = UIUtils.fetchColor(context, R.attr.divider)
        elevationDp = 0f
    }
    val wgIncludeAppListContainer = WgControlState(context).apply {
    }
    val wgIncludeAppListApkIconIv = WgControlState(context).apply {
    }
    val wgIncludeAppListTextContainer = WgControlState(context).apply {
    }
    val wgIncludeAppListApkLabelTv = WgControlState(context).apply {
        maxLines = 1
        ellipsize = true
        textSizeSp = 16f
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val wgIncludeAppAppDescTv = WgControlState(context).apply {
        maxLines = 1
        ellipsize = true
        visibility = View.GONE
        alpha = 0.75f
        textSizeSp = 12f
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val wgIncludeAppListCheckbox = WgControlState(context, checkable = true).apply {
    }
}

class WgPeerRowState(context: Context) : WgFormState() {
    val peerTitle = WgControlState(context).apply {
        applyTextAppearanceAttribute(context, this, com.google.android.material.R.attr.textAppearanceHeadline6)
        text = context.getString(R.string.lbl_peer)
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val peerEdit = WgControlState(context).apply {
        imageResource = R.drawable.ic_edit_icon_grey
    }
    val peerDelete = WgControlState(context).apply {
        imageResource = R.drawable.ic_delete
    }
    val publicKeyLabel = WgControlState(context).apply {
        text = context.getString(R.string.lbl_public_key)
    }
    val publicKeyText = WgControlState(context).apply {
        maxLines = 1
        ellipsize = true
    }
    val preSharedKeyLabel = WgControlState(context).apply {
        text = context.getString(R.string.lbl_preshared_key)
        visibility = View.GONE
    }
    val preSharedKeyText = WgControlState(context).apply {
        maxLines = 1
        ellipsize = true
        visibility = View.GONE
    }
    val allowedIpsLabel = WgControlState(context).apply {
        text = context.getString(R.string.lbl_allowed_ips)
    }
    val allowedIpsText = WgControlState(context).apply {
    }
    val endpointLabel = WgControlState(context).apply {
        text = context.getString(R.string.parse_error_inet_endpoint)
    }
    val endpointText = WgControlState(context).apply {
    }
    val persistentKeepaliveLabel = WgControlState(context).apply {
        text = context.getString(R.string.lbl_persistent_keepalive)
    }
    val persistentKeepaliveText = WgControlState(context).apply {
    }
}

class WgHopRowState(context: Context) : WgFormState() {
    val wgHopListCard = WgControlState(context).apply {
    }
    val wgHopListContainer = WgControlState(context).apply {
    }
    val wgHopListNameTv = WgControlState(context).apply {
        ellipsize = true
        textSizeSp = context.resources.getDimension(R.dimen.extra_large_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val wgHopListDescTv = WgControlState(context).apply {
        ellipsize = true
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val chipGroup = WgControlState(context).apply {
        visibility = View.VISIBLE
    }
    val chipIpv4 = WgControlState(context).apply {
        text = context.getString(R.string.settings_ip_text_ipv4)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val chipIpv6 = WgControlState(context).apply {
        text = context.getString(R.string.settings_ip_text_ipv6)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val chipSplitTunnel = WgControlState(context).apply {
        text = context.getString(R.string.lbl_split)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val chipAmnezia = WgControlState(context).apply {
        text = context.getString(R.string.lbl_amnezia)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val chipHopSrc = WgControlState(context).apply {
        text = context.getString(R.string.lbl_hopping)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val chipHopping = WgControlState(context).apply {
        text = context.getString(R.string.cd_dns_crypt_relay_heading)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val chipProperties = WgControlState(context).apply {
        text = context.getString(R.string.settings_ip_text_ipv4)
        visibility = View.GONE
        isChecked = true
        alpha = 0.8f
        textSizeSp = context.resources.getDimension(R.dimen.small_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val wgHopListCheckbox = WgControlState(context, checkable = true).apply {
    }
    val wgHopListProgress = WgControlState(context).apply {
        visibility = View.GONE
    }
}

class WgStatsState(context: Context) : WgFormState() {
    val toggleGroup = WgControlState(context).apply {
    }
    val tbRecentToggleBtn = WgControlState(context).apply {
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.defaultToggleBtnTxt)
        isAllCaps = false
    }
    val tbDailyToggleBtn = WgControlState(context).apply {
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.defaultToggleBtnTxt)
        isAllCaps = false
    }
    val tbWeeklyToggleBtn = WgControlState(context).apply {
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.defaultToggleBtnTxt)
        isAllCaps = false
    }
    val tbLogsDisabledTv = WgControlState(context).apply {
        text = context.getString(R.string.no_stats_available)
        visibility = View.VISIBLE
        textSizeSp = context.resources.getDimension(R.dimen.large_font_text_view) / context.resources.displayMetrics.scaledDensity
    }
    val tbStatsCard = WgControlState(context).apply {
        strokeWidth = (1f * context.resources.displayMetrics.density).toInt()
        strokeColor = UIUtils.fetchColor(context, R.attr.secondaryTextColor)
    }
    val fssProgressBar = WgControlState(context).apply {
        max = 100
        progress = 50
        secondaryProgress = 80
    }
    val fssUnmeteredDataUsage = WgControlState(context).apply {
        applyTextAppearance(context, this, androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead)
        textSizeSp = context.resources.getDimension(R.dimen.small_font_subheading_text) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val fssTotalDataUsage = WgControlState(context).apply {
        applyTextAppearance(context, this, androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead)
        textSizeSp = context.resources.getDimension(R.dimen.small_font_subheading_text) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val fssMeteredDataUsage = WgControlState(context).apply {
        applyTextAppearance(context, this, androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead)
        textSizeSp = context.resources.getDimension(R.dimen.small_font_subheading_text) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val statsRecyclerView = WgControlState(context).apply {
    }
}

class WgStatsRowState(context: Context) : WgFormState() {
    val ssContainer = WgControlState(context).apply {
    }
    val ssIcon = WgControlState(context).apply {
        visibility = View.GONE
    }
    val ssFlag = WgControlState(context).apply {
        maxLines = 1
        textSizeSp = 18f
    }
    val ssDataUsage = WgControlState(context).apply {
        maxLines = 1
        ellipsize = true
        textSizeSp = context.resources.getDimension(R.dimen.large_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
        textStyle = 1
    }
    val ssName = WgControlState(context).apply {
        maxLines = 1
        ellipsize = true
        textSizeSp = context.resources.getDimension(R.dimen.small_font_subheading_text) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryLightColorText)
    }
    val ssProgress = WgControlState(context).apply {
        alpha = 0.9f
    }
    val ssCount = WgControlState(context).apply {
        alpha = 0.85f
        textSizeSp = context.resources.getDimension(R.dimen.default_font_text_view) / context.resources.displayMetrics.scaledDensity
        textColor = UIUtils.fetchColor(context, R.attr.primaryTextColor)
    }
    val ssIndicator = WgControlState(context).apply {
        alpha = 0.35f
        imageResource = R.drawable.ic_right_arrow_white
    }
}

private fun applyTextAppearanceAttribute(context: Context, node: WgControlState, attribute: Int) {
    val value = android.util.TypedValue()
    if (context.theme.resolveAttribute(attribute, value, true) && value.resourceId != 0) applyTextAppearance(context, node, value.resourceId)
}
private fun applyTextAppearance(context: Context, node: WgControlState, style: Int) {
    context.obtainStyledAttributes(style, intArrayOf(android.R.attr.textSize, android.R.attr.textColor, android.R.attr.textStyle)).let {
        if (it.hasValue(0)) node.textSizeSp = it.getDimension(0, 14f) / context.resources.displayMetrics.scaledDensity
        if (it.hasValue(1)) node.textColor = it.getColor(1, 0)
        if (it.hasValue(2)) node.textStyle = it.getInt(2, 0)
        it.recycle()
    }
}
