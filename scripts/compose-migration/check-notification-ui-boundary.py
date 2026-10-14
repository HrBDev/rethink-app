#!/usr/bin/env python3
"""Verify the notification trampoline differs only in its native dialog renderer type."""
from pathlib import Path
import subprocess
root=Path(__file__).resolve().parents[2]
p='app/src/main/java/com/celzero/bravedns/ui/NotificationHandlerActivity.kt'
base=subprocess.check_output(['git','show','23cb6dd98805445174bd83b7237ad5f5d2c24ac9:'+p],cwd=root,text=True)
expected=base.replace('import com.google.android.material.dialog.MaterialAlertDialogBuilder','import com.celzero.bravedns.ui.compose.wireguard.WgAlertDialogBuilder').replace('MaterialAlertDialogBuilder(', 'WgAlertDialogBuilder(')
actual=(root/p).read_text()
assert actual==expected, 'Notification trampoline changed beyond native renderer substitution'
assert expected.count('WgAlertDialogBuilder(')==2
assert actual.replace('VpnController.resumeApp()', 'VpnController.resumeApp(); error("negative-control")')!=expected
print('PASS: notification trampoline byte-identical except two dialog renderer substitutions; all routing, security, permission and lifecycle code preserved.')
