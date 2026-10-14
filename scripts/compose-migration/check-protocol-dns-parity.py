#!/usr/bin/env python3
"""Pinned protocol DNS controller and presentation-only reconstruction guard.

Source-level equivalence checks are not an Android runtime or rendering test.
"""
from pathlib import Path
from collections import Counter
import re
import subprocess

ROOT = Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
JAVA = Path('app/src/main/java/com/celzero/bravedns')

def baseline(path):
    return subprocess.check_output(['git', 'show', f'{BASE}:{path.as_posix()}'], cwd=ROOT, text=True)

def function(source, name):
    match = re.search(r'\b(?:private |public |internal |override |suspend )*fun\s+' + re.escape(name) + r'\s*\(', source)
    assert match, name
    start = source.index('{', match.end()); depth = 1; end = start + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}'); end += 1
    return source[match.start():end]

def same(before, after, names):
    return all(function(before, name) == function(after, name) for name in names)

checks = {
    'ui/fragment/DohListFragment.kt': ['insertDoHEndpoint', 'checkUrl', 'io'],
    'ui/fragment/DoTListFragment.kt': ['insert', 'io'],
    'ui/fragment/ODoHListFragment.kt': ['insert', 'checkUrl', 'io'],
    'ui/fragment/DnsCryptListFragment.kt': ['insertDNSCryptServer', 'insertDNSCryptRelay', 'isDarkThemeOn', 'io'],
    'ui/fragment/DnsProxyListFragment.kt': ['insertDNSProxyEndpointDB', 'io'],
    'ui/activity/BlockFreeDnsActivity.kt': ['setupFilterChips', 'observeViewModel', 'onDnsSelected', 'updateSelectedCard', 'onResume'],
}
for relative, names in checks.items():
    path = JAVA / relative
    before, after = baseline(path), (ROOT / path).read_text()
    assert same(before, after, names), f'Protected method changed: {relative}'
    original = function(after, names[0])
    mutated = after.replace(original, original.replace('{', '{ /* negative-control */', 1), 1)
    assert not same(before, mutated, names), 'negative-control must fail'


retired = ['DohEndpointAdapter','DoTEndpointAdapter','ODoHEndpointAdapter','DnsProxyEndpointAdapter',
    'DnsCryptEndpointAdapter','DnsCryptRelayEndpointAdapter','SmartDnsEndpointAdapter','BlockFreeDnsAdapter','DnsCryptRelaysDialog']
for path in (ROOT / 'app/src').rglob('*.kt'):
    text = path.read_text()
    for name in retired:
        assert path.stem != name and not re.search(r'\b' + name + r'\s*\(', text), f'{name} still reachable in {path}'
        assert not re.search(r'import\s+[\w.]+\.' + name + r'\b', text), f'{name} import in {path}'

hosts = [JAVA / name for name in checks] + [JAVA / 'ui/fragment/SmartDnsListFragment.kt']
for path in hosts:
    text = (ROOT / path).read_text()
    assert 'DisposeOnViewTreeLifecycleDestroyed' in text and 'ComposeView' in text, path
    for forbidden in ['viewBinding(', 'Binding.inflate(', 'AndroidView(', 'MaterialAlertDialogBuilder(']:
        assert forbidden not in text, f'{path}: {forbidden}'
    if 'Fragment' in path.name:
        assert 'rememberSaveable' not in text, f'Transient editor/relay state was persisted in {path}'

editors = (ROOT / JAVA / 'ui/compose/dns/DnsEndpointEditors.kt').read_text()
assert 'rememberSaveable' not in editors and 'NoDimDialogWindow()' in editors
assert 'ip.trim()' in editors and '!insecure' in editors
screen = (ROOT / JAVA / 'ui/compose/dns/DnsEndpointScreen.kt').read_text()
assert 'repeatOnLifecycle(Lifecycle.State.STARTED)' in screen and 'delay(1_000L)' in screen
assert 'flowWithLifecycle(owner.lifecycle, Lifecycle.State.STARTED)' in screen
presenter = (ROOT / JAVA / 'ui/compose/dns/DnsEndpointPresenter.kt').read_text()
for name in ['handleDoHChanges','handleDoTChanges','handleODoHChanges','handleDnscryptChanges','handleDnsrelayChanges','handleDnsProxyChanges',
             'deleteDohEndpoint','deleteDoTEndpoint','deleteODoHEndpoint','deleteDnscryptEndpoint','deleteDnscryptRelayEndpoint','deleteDnsProxyEndpoint']:
    assert presenter.count('appConfig.' + name + '(') == 1, name
assert 'selected && !appConfig.isDnscryptRelaySelectable()' in presenter
assert 'persistentState?.wgGlobalLockdown == true' in presenter
proxy = (ROOT / JAVA / 'ui/fragment/DnsProxyListFragment.kt').read_text()
for text in ['ipInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }', 'IPAddressString(ip).isIPAddress',
             'Utilities.isLanIpv4(ip)', '!Utilities.isValidLocalPort(port)', 'portInput.toInt()',
             'validIps.joinToString(",")', 'persistentState.excludeAppsInProxy = !forwardApps',
             'rdb.refresh(RefreshDatabase.ACTION_REFRESH_FORCE)']:
    assert text in proxy, text
blockfree = (ROOT / JAVA / 'ui/compose/dns/BlockFreeDnsContent.kt').read_text()
ids = re.search(r'val ids = listOf\((.*?)\)', blockfree, re.S).group(1)
assert re.findall(r'R.id.(\w+)', ids) == ['bfd_chip_all','bfd_chip_rethink','bfd_chip_doh','bfd_chip_dot','bfd_chip_dnscrypt','bfd_chip_odoh','bfd_chip_dns_proxy']
assert 'b.bfdFilterChipGroup.restore(savedInstanceState.getInt("block_free_filter_id"))' in (ROOT / JAVA / 'ui/activity/BlockFreeDnsActivity.kt').read_text()
for path in (ROOT / 'app/src/test/java/com/celzero/bravedns/ui/compose/dns').glob('*ComposeTest.kt'):
    text = path.read_text()
    assert 'application = Application::class' in text, path
    for forbidden in ['ActivityScenario', 'VpnController.', 'AppConfig(', 'PersistentState(', '.enableSmartDns(', '.handleDoHChanges(']:
        assert forbidden not in text, f'Production effect in isolated test: {path}: {forbidden}'
print('PASS: protocol insert/URL validators and BlockFree controller bodies exactly match pinned base; transient dialogs, lifecycle sources, action entry points, chip restoration, retired renderers and isolated test scope checked.')
