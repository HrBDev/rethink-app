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
def binds_without_lifecycle_restarts(source):
    body = function(source, 'DnsEndpointRow')
    required = [
        'var description by remember(item) { mutableStateOf(item.initialDescription) }',
        'DisposableEffect(item, owner, ioDispatcher)',
        'val statusJob = owner.lifecycleScope.launch',
        'val readDescription = item.description ?: return@launch',
        'if (item.pollStatus && !owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) break',
        'launch {\n                    val value = withContext(ioDispatcher) { readDescription() }',
        'if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) description = value',
        'if (!item.pollStatus) break',
        'delay(1_000L)',
        'val flagJob = owner.lifecycleScope.launch',
        'val value = withContext(ioDispatcher) { item.flag() }',
        'if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) flag = value',
        'onDispose { statusJob.cancel(); flagJob.cancel() }',
    ]
    return (all(text in body for text in required)
            and 'repeatOnLifecycle(' not in body
            and body.count('readDescription()') == 1
            and body.count('item.flag()') == 1
            and 'DnsEndpointRow(rememberDnsEndpointRow(it, row))' in source
            and 'remember(DnsEndpointSourceIdentity(endpoint), row) { row(endpoint) }' in source
            and 'endpoint === other.endpoint' in source)

assert binds_without_lifecycle_restarts(screen), 'DNS row must keep bind-lifetime effects'
# These narrow controls reject resume-driven retries, incomplete bind keys, missing tick guards
# and an uncancelled outgoing row. They do not alter any protected controller comparison.
for original, replacement in [
    ('DisposableEffect(item, owner, ioDispatcher)', 'DisposableEffect(item.id, owner, ioDispatcher)'),
    ('if (item.pollStatus && !owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) break', 'if (false) break'),
    ('launch {\n                    val value = withContext(ioDispatcher) { readDescription() }', 'run {\n                    val value = withContext(ioDispatcher) { readDescription() }'),
    ('var description by remember(item) { mutableStateOf(item.initialDescription) }', 'var description by remember(item) { mutableStateOf<String?>(null) }'),
    ('val flagJob = owner.lifecycleScope.launch', 'val flagJob = owner.lifecycleScope.launch { owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) }; val unused = owner.lifecycleScope.launch'),
    ('onDispose { statusJob.cancel(); flagJob.cancel() }', 'onDispose { statusJob.cancel() }'),
    ('DnsEndpointRow(rememberDnsEndpointRow(it, row))', 'DnsEndpointRow(row(it))'),
    ('endpoint === other.endpoint', 'endpoint == other.endpoint'),
    ('remember(DnsEndpointSourceIdentity(endpoint), row)', 'remember(DnsEndpointSourceIdentity(endpoint))'),
]:
    mutated = screen.replace(original, replacement, 1)
    assert mutated != screen and not binds_without_lifecycle_restarts(mutated), 'DNS row lifecycle negative-control must fail'
# The six original protocol adapters fetch flags in displayDetails (bind), not on START.
for adapter in ['DohEndpointAdapter', 'DoTEndpointAdapter', 'ODoHEndpointAdapter',
                'DnsCryptEndpointAdapter', 'DnsCryptRelayEndpointAdapter', 'DnsProxyEndpointAdapter']:
    old = baseline(JAVA / f'adapter/{adapter}.kt')
    assert 'io { updateFlag(endpoint) }' in function(old, 'displayDetails'), adapter
    assert 'repeatOnLifecycle' not in old, adapter
    if adapter not in ['DnsCryptRelayEndpointAdapter', 'DnsProxyEndpointAdapter']:
        tick = function(old, 'updateSelectedStatus')
        assert '?.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED) == false' in tick, adapter
        assert 'statusCheckJob?.cancel()' in tick, adapter
row_presenter = (ROOT / JAVA / 'ui/compose/dns/DnsEndpointPresenter.kt').read_text()
def keeps_the_bound_description_branch(source):
    branch = function(source, 'bindDnsEndpointDescription')
    row_body = function(source, 'row')
    return (all(text in branch for text in [
                'if (!selected) return DnsEndpointDescriptionState()',
                'return if (canPoll()) DnsEndpointDescriptionState(read = status, poll = true)',
                'else DnsEndpointDescriptionState(initial = selectedLabel())'])
            and branch.count('canPoll()') == 1
            and all(text in row_body for text in [
                'val boundDescription = if (polling) bindDnsEndpointDescription(',
                'canPoll = { VpnController.hasTunnel() && !appConfig.isSmartDnsEnabled() }',
                'status = ::dnsStatus',
                ') else DnsEndpointDescriptionState(read = description)',
                'boundDescription.poll, relay', 'boundDescription.read,',
                'initialDescription = boundDescription.initial'])
            and row_body.count('VpnController.hasTunnel()') == 1
            and row_body.count('appConfig.isSmartDnsEnabled()') == 1)

assert keeps_the_bound_description_branch(row_presenter), 'DNS status/static branch must be captured at bind'
for original, replacement in [
    ('if (!selected) return DnsEndpointDescriptionState()', 'if (false) return DnsEndpointDescriptionState()'),
    ('DnsEndpointDescriptionState(read = status, poll = true)', 'DnsEndpointDescriptionState(read = { if (canPoll()) status() else selectedLabel() }, poll = true)'),
    ('status = ::dnsStatus', 'status = { if (VpnController.hasTunnel()) dnsStatus() else null }'),
]:
    mutated = row_presenter.replace(original, replacement, 1)
    assert mutated != row_presenter and not keeps_the_bound_description_branch(mutated), 'DNS bind-branch negative-control must fail'
assert 'relay = true, polling = false' in row_presenter
assert 'description = if (e.isSelected && !appConfig.isSmartDnsEnabled()) ::dnsStatus else null' in row_presenter
assert 'directIp = true, polling = false' in row_presenter
smart = (ROOT / JAVA / 'ui/compose/dns/SmartDnsContent.kt').read_text()
assert 'description = null' in smart and 'initialDescription = if (selected) selectedLabel else null' in smart
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
