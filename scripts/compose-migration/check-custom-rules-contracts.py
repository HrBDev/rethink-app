#!/usr/bin/env python3
"""Source-only equality/rendering guard. Does not launch Android, services, or rule stores."""
from pathlib import Path
import subprocess, json, hashlib
ROOT = Path(__file__).resolve().parents[2]
BASE = '23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
P = 'app/src/main/java/com/celzero/bravedns/'
def original(path): return subprocess.check_output(['git', 'show', f'{BASE}:{path}'], cwd=ROOT, text=True)
def method(text, name):
    import re
    start = re.search(r'    private (?:suspend )?fun '+name+r'\(', text).start()
    cursor = text.index('{', start) + 1; depth = 1
    while depth:
        depth += (text[cursor] == '{') - (text[cursor] == '}'); cursor += 1
    return text[start:cursor]
checked = []
for kind, names in [('Ip', ['byPassUniversal','byPassAppRule','blockIp','noRuleIp']), ('Domain', ['whitelist','block','noRule'])]:
    path = P + f'ui/bottomsheet/Custom{kind}RulesBtmSheet.kt'
    before, after = original(path), (ROOT/path).read_text()
    for name in names:
        assert method(before,name) == method(after,name), name
        checked.append(path + '#' + name)
before = original(P+'ui/dialog/CustomLanIpDialog.kt')
after = (ROOT/(P+'ui/compose/rules/CustomLanIpState.kt')).read_text()
for name in ['saveAutoMode','saveManualMode','combineIpAndPrefix','validateIpv4WithPrefix','validateIpv6WithPrefix','isRfc1918Ipv4','isUlaIpv6','loadDefaultAutoValues','loadManualValues','resetManualFields','setupInitialState','setupClickListeners']:
    assert method(before,name) == method(after,name), name
    checked.append('CustomLanIpState#'+name)
protected = ['service/IpRulesManager.kt','service/DomainRulesManager.kt','ui/fragment/RulesImportHelper.kt','viewmodel/CustomIpViewModel.kt','viewmodel/CustomDomainViewModel.kt','database/CustomIpDao.kt','database/CustomDomainDAO.kt','database/CustomIp.kt','database/CustomDomain.kt']
for path in protected: assert original(P+path) == (ROOT/(P+path)).read_text(), path
hosts=['ui/activity/CustomRulesActivity.kt','ui/fragment/CustomIpFragment.kt','ui/fragment/CustomDomainFragment.kt','ui/bottomsheet/CustomIpRulesBtmSheet.kt','ui/bottomsheet/CustomDomainRulesBtmSheet.kt','ui/dialog/CustomLanIpDialog.kt']
files=[ROOT/(P+x) for x in hosts]+list((ROOT/(P+'ui/compose/rules')).glob('*.kt'))
for path in files:
    source=path.read_text()
    for bad in ['AndroidView(', 'AndroidViewBinding(', 'Binding.inflate(', 'MaterialAlertDialogBuilder(', 'RecyclerView(']: assert bad not in source, (path,bad)
for path in (ROOT/'app/src').rglob('*.kt'):
    source=path.read_text()
    assert 'CustomIpAdapter' not in source and 'CustomDomainAdapter' not in source, path
state=(ROOT/(P+'ui/compose/rules/RulesPageState.kt')).read_text()
assert 'save = { it.query }' in state
assert 'restore = { RulesPageState().apply { query = it } }' in state
assert 'rememberSaveable' not in (ROOT/(P+'ui/compose/rules/RuleDetailsContent.kt')).read_text()
report={'baseline':BASE,'methodCount':len(checked),'unchangedMethods':checked,'protectedRuleFiles':protected,'sourceHashes':{str(p.relative_to(ROOT)):hashlib.sha256(p.read_bytes()).hexdigest() for p in files},'limitation':'Source-only; not compilation, tests, or visual/runtime parity.'}
(ROOT/'docs/compose-migration/custom-rules-source-report.json').write_text(json.dumps(report,indent=2)+'\n')
print(f'PASS: {len(checked)} unchanged controller methods, {len(protected)} unchanged rule data/manager boundaries; native-source and transient-state guards passed.')
print(report['limitation'])
