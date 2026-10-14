#!/usr/bin/env python3
"""Verify the notification trampoline differs only in its native dialog and invisible Compose shell renderers."""
from pathlib import Path
import subprocess
root=Path(__file__).resolve().parents[2]
p='app/src/main/java/com/celzero/bravedns/ui/NotificationHandlerActivity.kt'
base=subprocess.check_output(['git','show','23cb6dd98805445174bd83b7237ad5f5d2c24ac9:'+p],cwd=root,text=True)
expected=base.replace('import com.google.android.material.dialog.MaterialAlertDialogBuilder','import com.celzero.bravedns.ui.compose.wireguard.WgAlertDialogBuilder').replace('MaterialAlertDialogBuilder(', 'WgAlertDialogBuilder(')
expected=expected.replace('import androidx.core.net.toUri', 'import androidx.compose.ui.platform.ComposeView\nimport androidx.compose.ui.platform.ViewCompositionStrategy\nimport androidx.core.net.toUri').replace('setContentView(android.R.layout.activity_list_item)', 'setContentView(ComposeView(this).apply {\n            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)\n            setContent {}\n        })')
actual=(root/p).read_text()
assert actual==expected, 'Notification trampoline changed beyond native renderer substitution'
assert expected.count('WgAlertDialogBuilder(')==2
assert actual.replace('VpnController.resumeApp()', 'VpnController.resumeApp(); error("negative-control")')!=expected
for before, after in [('window.decorView.alpha = 0f', 'window.decorView.alpha = 1f'), ('handleNotificationIntent(intent)', 'error("skipped dispatch")'), ('if (isAppLocked())', 'if (false)'), ('if (!VpnController.isOn())', 'if (false)'), ('ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed', 'ViewCompositionStrategy.DisposeOnDetachedFromWindow')]:
    assert before in actual
    assert actual.replace(before, after) != expected
assert 'R.layout.' not in actual
print('PASS: notification trampoline byte-identical except two dialog renderer substitutions and its invisible lifecycle-disposed Compose shell; all routing, security, permission and lifecycle code preserved. Runtime unrun.')
