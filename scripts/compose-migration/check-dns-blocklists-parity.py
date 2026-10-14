#!/usr/bin/env python3
"""Compare reconstructed presentation controllers with the pinned baseline.

Source-only regression guards, not compilation or runtime/visual equivalence.
Only explicit render-property aliases and comments/formatting are normalized.
"""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
PREFIX = 'app/src/main/java/com/celzero/bravedns/'
TOKEN = re.compile(r'"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/')

def old(path):
    return subprocess.check_output(['git', 'show', BASE + ':' + PREFIX + path], cwd=ROOT, text=True)

def current(path):
    return (ROOT / PREFIX / path).read_text()

def masked(source):
    return TOKEN.sub(lambda m: ' ' * len(m[0]), source)

def braces(source, start):
    code = masked(source)
    opening = code.index('{', start)
    level, end = 1, opening + 1
    while level:
        level += (code[end] == '{') - (code[end] == '}')
        end += 1
    return source[opening + 1:end - 1]

def body(source, name):
    match = re.search(r'\bfun\s+' + re.escape(name) + r'\s*\(', source)
    assert match, 'Missing method ' + name
    return braces(source, match.end())

def normalized(source):
    source = re.sub(r'(b\.\w+)\.visible = (true|false)', lambda m: m[1] + '.visibility = View.' + ('VISIBLE' if m[2] == 'true' else 'GONE'), source)
    source = source.replace('!viewAvailable', '_binding == null').replace('requireView()', 'b.root')
    source = source.replace('_: DnsControl', '_: CompoundButton')
    parts, end = [], 0
    for m in TOKEN.finditer(source):
        parts.append(re.sub(r'\s+', '', source[end:m.start()]))
        if not m[0].startswith(('//', '/*')): parts.append(m[0])
        end = m.end()
    parts.append(re.sub(r'\s+', '', source[end:]))
    return ''.join(parts)

count = 0

def compare(path, names):
    global count
    before, after = old(path), current(path)
    for name in names:
        assert normalized(body(before, name)) == normalized(body(after, name)), path + ':' + name + ' changed'
        count += 1

compare('ui/fragment/DnsSettingsFragment.kt', [
    'handleProxyDnsUi', 'handlePreventDnsLeaksUi', 'updateAllowedRecordTypesUi', 'initObservers',
    'updateLatency', 'showSplitDnsUi', 'enableSplitDnsUi', 'disableSplitDnsUi', 'updateSpiltDns',
    'updateConnectedStatus', 'isSystemDns', 'isSmartDns', 'isRethinkDns', 'updateSelectedDns',
    'getConnectedDnsType', 'restoreDefaults', 'refreshUiAfterRestore', 'showBlockFreeDnsModeBottomSheet',
    'showDnsRecordTypesBottomSheet', 'showSmartDnsList', 'openLocalBlocklist', 'invokeRethinkActivity',
    'showCustomDns', 'updateBlockFreeDnsUi', 'setNetworkDns', 'enableAfterDelay', 'logEvent', 'io', 'uiCtx', 'onBtmSheetDismiss',
])
before, after = old('ui/fragment/DnsSettingsFragment.kt'), current('ui/fragment/DnsSettingsFragment.kt')
for control in ['dcCheckUpdateSwitch','dcAlgSwitch','dcFaviconSwitch','dcPreventDnsLeaksSwitch','dcDownloaderSwitch','dcEnableCacheSwitch','dcProxyDnsSwitch','dcSplitDnsSwitch','dcUndelegatedDomainsSwitch','dcBlockUnknownSwitch']:
    a = before.index('b.' + control + '.setOnCheckedChangeListener')
    b = after.index('b.' + control + '.onCheckedChange =')
    assert normalized(braces(before, a)) == normalized(braces(after, b)), control + ' callback changed'
    count += 1
compare('ui/activity/DnsListActivity.kt', ['invokeRethinkActivity','onResume','updateSelectedStatus','uiCtx','io'])
compare('ui/fragment/RethinkListFragment.kt', ['hideProgress','showUpdateCheckUi','isBlocklistUpdateAvailable','checkBlocklistUpdate','getDownloadTimeStamp','updateMaxSwitchUi','download','initObservers','showNewUpdateUi','showRedownloadUi','onRemoteDownloadFailure','enableChips','onDownloadSuccess','io','uiCtx'])
compare('ui/bottomsheet/LocalBlocklistsBottomSheet.kt', ['reflectOngoingDownloadUi','registerWorkObserversOnce','init','initializeObservers','showCheckDownloadProgressUi','onDownloadProgress','onDownloadSuccess','onDownloadFail','showCheckUpdateUi','showUpdateUi','showRedownloadUi','downloadLocalBlocklist','proceedWithDownload','deleteLocalBlocklist','handleDownloadStatus','updateLocalBlocklistUi','isBlocklistUpdateAvailable','enableBlocklist','invokeRethinkActivity','isLocalBlocklistStampAvailable','setBraveDnsLocal','removeBraveDnsLocal','observeWorkManager','ui','ioCtx','io','onDismiss'])
compare('ui/fragment/RethinkBlocklistFragment.kt', ['updateFileTagList','getSelectedFileTags','handleDownloadState','hasBlocklist','hasBlocklists','showDownloadUi','showConfigureUi','hideDownloadUi','hideConfigureUi','downloadType','cancelDownload','maybeResetStaleTerminalDownloadState','isRemoteDownloadActive','downloadBlocklist','proceedWithBlocklistDownload','handleDownloadStatus','setSimpleAdapter','onQueryTextSubmit','onQueryTextChange','filterObserver','applyFilter','openFilterBottomSheet','getAllList','onDownloadStart','onDownloadFail','uiCtx','ioCtx','io','go','ui'])
compare('ui/bottomsheet/RethinkPlusFilterBottomSheet.kt', ['isDarkThemeOn','applySubgroupFilter','removeSubgroupFilter'])
# The four actual production persistence bodies, not duplicated fake business logic.
presenter = current('ui/compose/blocklists/BlocklistSelectionPresenter.kt')
for adapter, method in [('LocalSimpleViewAdapter','setLocalPack'),('RemoteSimpleViewAdapter','setRemotePack'),('LocalAdvancedViewAdapter','setLocalTag'),('RemoteAdvancedViewAdapter','setRemoteTag')]:
    assert normalized(body(old('adapter/' + adapter + '.kt'), 'setFileTag')) == normalized(body(presenter, method)), adapter + ' persistence changed'
    count += 1
# Negative control detects a concrete write regression.
mutated = presenter.replace('updateFiletagsLocal(tagIds.toSet(), selected)', 'updateFiletagsLocal(tagIds.toSet(), 1 - selected)')
assert normalized(body(presenter, 'setLocalPack')) != normalized(body(mutated, 'setLocalPack'))
for path in ['ui/fragment/DnsSettingsFragment.kt','ui/fragment/RethinkListFragment.kt','ui/fragment/RethinkBlocklistFragment.kt','ui/bottomsheet/LocalBlocklistsBottomSheet.kt','ui/bottomsheet/RethinkListBottomSheet.kt','ui/bottomsheet/RethinkPlusFilterBottomSheet.kt']:
    code = current(path)
    for forbidden in ['databinding.', 'MaterialAlertDialogBuilder', 'AndroidView(', 'RecyclerView(', 'ViewHolder(']:
        assert forbidden not in code, path + ' retains ' + forbidden
renderer = current('ui/compose/blocklists/BlocklistConfigurationScreen.kt')
assert 'styleAttribute = com.google.android.material.R.attr.materialCardViewElevatedStyle' in renderer
assert 'containerColor = themeColor(if (checked) R.attr.selectedCardBg else R.attr.background)' in renderer
assert 'if (b.query.isNotBlank() && !onQuerySubmit(b.query))' in renderer
print(f'PASS: {count} reconstructed DNS/blocklist protected methods and callbacks match baseline after explicit UI aliases; negative control passed. Source-only.')
