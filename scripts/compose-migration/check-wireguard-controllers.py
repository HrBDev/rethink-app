#!/usr/bin/env python3
"""Selected baseline controller-body equivalence. This is not Android/type/runtime validation."""
import argparse, hashlib, json, pathlib, re, subprocess, sys
ROOT=pathlib.Path(__file__).resolve().parents[2]
BASELINE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
MANIFEST=ROOT/'docs/compose-migration/wireguard-controller-selection.json'

def lex(source):
    """Keep literals exact, ignore whitespace/comments, and mask literal/comment braces for parsing."""
    tokens=[]; mask=list(source); i=0
    while i<len(source):
        start=i
        if source[i].isspace():i+=1;continue
        if source.startswith('//',i):
            i=source.find('\n',i)
            if i<0:i=len(source)
            mask[start:i]=' '*(i-start);continue
        if source.startswith('/*',i):
            level=1;i+=2
            while level and i<len(source):
                if source.startswith('/*',i):level+=1;i+=2
                elif source.startswith('*/',i):level-=1;i+=2
                else:i+=1
            mask[start:i]=' '*(i-start);continue
        if source.startswith('"""',i):
            end=source.find('"""',i+3)
            if end<0:raise ValueError('unterminated raw literal')
            i=end+3;tokens.append(source[start:i]);mask[start:i]=' '*(i-start);continue
        if source[i] in '\"\'':
            quote=source[i];i+=1
            while i<len(source):
                if source[i]=='\\':i+=2;continue
                if source[i]==quote:i+=1;break
                i+=1
            tokens.append(source[start:i]);mask[start:i]=' '*(i-start);continue
        m=re.match(r'[A-Za-z_][\w]*|\d+(?:\.\d+)?',source[i:])
        if m:i+=len(m[0])
        else:i+=1
        tokens.append(source[start:i])
    return tokens,''.join(mask)

def bodies(source):
    _,mask=lex(source); result={};counts={}
    for m in re.finditer(r'\bfun\s+(?:\w+\.)?(\w+)\s*\(',mask):
        name=m[1];i=m.end();depth=1
        while depth and i<len(mask):
            if mask[i]=='(':depth+=1
            elif mask[i]==')':depth-=1
            i+=1
        head=re.search(r'[={}]',mask[i:])
        if head is None or head[0]!='{':continue
        start=i+head.start();i=start+1;depth=1
        while depth and i<len(mask):
            if mask[i]=='{':depth+=1
            elif mask[i]=='}':depth-=1
            i+=1
        if depth:raise ValueError('unterminated body '+name)
        counts[name]=counts.get(name,0)+1
        key=name if counts[name]==1 else f'{name}#{counts[name]}'
        result[key]=lex(source[start:i])[0]
    return result

def baseline(path):return subprocess.check_output(['git','show',f'{BASELINE}:{path}'],cwd=ROOT,text=True)
def digest(tokens):return hashlib.sha256(json.dumps(tokens,ensure_ascii=False).encode()).hexdigest()
def self_test():
    a='fun a() { val text="brace } and spaces"; /* { */ save(1) }'
    b='fun a() {\n val text = "brace } and spaces"; /* other } */ save ( 1 )\n}'
    assert bodies(a)['a']==bodies(b)['a']
    assert bodies(a)['a']!=bodies(a.replace('save(1)','save(2)'))['a']
    assert bodies(a)['a']!=bodies(a.replace('and spaces','and  spaces'))['a']
    assert 'missing' not in bodies(a)
    assert 'abstractCallback' not in bodies('interface X { fun abstractCallback() } class Y { fun real() { save(1) } }')
    print('PASS: extractor whitespace/comment/braces, literal-space, mutation and missing-method controls')

def main():
    p=argparse.ArgumentParser();p.add_argument('--self-test',action='store_true');p.add_argument('--report',type=pathlib.Path);a=p.parse_args()
    if a.self_test:self_test()
    manifest=json.loads(MANIFEST.read_text());results=[];failed=[]
    for path,names in manifest.items():
        old=bodies(baseline(path));new=bodies((ROOT/path).read_text())
        for name in names:
            passed=name in old and name in new and old[name]==new[name]
            results.append({'path':path,'method':name,'baselineSha256':digest(old.get(name,[])),'currentSha256':digest(new.get(name,[])),'passed':passed})
            if not passed:failed.append(path+'::'+name)
    report={'baseline':BASELINE,'kind':'selected-controller-token-equality','passed':not failed,'count':len(results),'methods':results}
    if a.report:a.report.write_text(json.dumps(report,indent=2)+'\n')
    if failed:
        print('FAIL: '+'\n'.join(failed));return 1
    print(f'PASS: {len(results)} unchanged selected WireGuard controller bodies. Not typechecking, Android compilation, lifecycle/rendering or service tests.')
    return 0
if __name__=='__main__':sys.exit(main())
