package com.celzero.bravedns.ui.compose.wireguard

import android.app.Application
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class WgFormRestoreTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    @Test fun interfaceDraftRestoresEmptyAndNonEmptyEditableFields() {
        val original = WgEditorState(context)
        original.interfaceNameText.setText("Synthetic tunnel")
        original.dnsServersText.setText("")
        original.mtuText.setText("1420")
        val bundle = Bundle(); original.saveForm(bundle, "editor.")
        val restored = WgEditorState(context)
        restored.dnsServersText.setText("example")
        restored.restoreForm(bundle, "editor.")
        assertEquals("Synthetic tunnel", restored.interfaceNameText.text)
        assertEquals("", restored.dnsServersText.text)
        assertEquals("1420", restored.mtuText.text)
    }
    @Test fun peerRestoreDispatchesKeepaliveWatcher() {
        val original = WgPeerState(context)
        original.peerPersistentKeepAlive.setText("25")
        val bundle = Bundle(); original.saveForm(bundle, "peer.")
        val restored = WgPeerState(context)
        var value = ""
        restored.peerPersistentKeepAlive.addTextChangedListener { value = it.toString() }
        restored.restoreForm(bundle, "peer.")
        assertEquals("25", value)
    }
    @Test fun absentStateDoesNotOverwriteInitializedValues() {
        val state = WgEditorState(context)
        state.interfaceNameText.setText("Initialized")
        state.restoreForm(Bundle(), "editor.")
        assertEquals("Initialized", state.interfaceNameText.text)
    }
}
