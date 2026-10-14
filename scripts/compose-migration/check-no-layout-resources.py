#!/usr/bin/env python3
"""Independent app-owned XML-layout/resource-identity removal gate.

Default is final acceptance and fails until every app-owned layout and executable
layout reference is gone, including tests, unknown flavors/qualifiers, alternate
XML fixture locations, generated resource directories and direct reflection.
This is source-only: no Android/Gradle/network/runtime operation is performed.
"""
from __future__ import annotations
import argparse, collections, hashlib, io, json, re, subprocess, sys, tarfile
from pathlib import Path
import xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2]
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
IDS='app/src/main/res/values/compose_legacy_ids.xml'
NOTIFICATION='app/src/main/java/com/celzero/bravedns/ui/NotificationHandlerActivity.kt'
PREVIOUSLY_REMOVED={
 'app/src/full/res/layout/activity_home_screen.xml',
 'app/src/full/res/layout/fragment_rethink_basic.xml',
 'app/src/main/res/layout/activity_fragment_host.xml',
 'app/src/main/res/layout/dialog_subscription_anim.xml',
}
LAYOUT_PATH=re.compile(r'(?:^|/)res/layout[^/]*(?:/|$)')
LAYOUT_REF=re.compile(r'@(?:(?:[\w.]+):)?layout/[\w.]+')
VIEW_TAG=re.compile(r'<\s*(?:[\w.$]+\.)?(?:\w*(?:Layout|View|ViewGroup)|merge|include|layout|fragment|TextView|ImageView|Button|EditText|CheckBox|RadioButton|Switch|ProgressBar|Space)\b')
BUILD_SUFFIX=('.gradle','.gradle.kts','.bp')

def digest(data):return hashlib.sha256(data).hexdigest()
def code_only(text):
    """Independent position-preserving comment masker, retaining string literals."""
    chars=list(text);i=0
    while i<len(text):
        if text.startswith('//',i):
            end=text.find('\n',i);end=len(text) if end<0 else end
            chars[i:end]=' '*(end-i);i=end
        elif text.startswith('/*',i):
            start=i;depth=1;i+=2
            while i<len(text) and depth:
                if text.startswith('/*',i):depth+=1;i+=2
                elif text.startswith('*/',i):depth-=1;i+=2
                else:i+=1
            chars[start:i]=['\n' if c=='\n' else ' ' for c in text[start:i]]
        elif text.startswith('"""',i):
            end=text.find('"""',i+3);i=len(text) if end<0 else end+3
        elif text[i] in '\"\'':
            q=text[i];i+=1
            while i<len(text):
                if text[i]=='\\':i+=2
                elif text[i]==q:i+=1;break
                else:i+=1
        else:i+=1
    return ''.join(chars)
def issue(path,text,offset,rule,detail):
    return {'path':path,'line':text.count('\n',0,offset)+1,'rule':rule,'detail':detail}
def resource_set(path):
    parts=Path(path).parts
    try:index=parts.index('src');return '/'.join(parts[:index+2])
    except ValueError:return 'other_repository_location'
def resource_ids(path,text):
    found=[]
    try:root=ET.fromstring(text)
    except ET.ParseError:return found
    for node in root.iter():
        if (node.tag=='item' and node.get('type')=='id') or node.tag=='id':
            if node.get('name'):found.append(node.get('name'))
        for value in node.attrib.values():
            if value.startswith('@+id/'):found.append(value[5:])
    return found

def call_arguments(code,start):
    if start>=len(code) or code[start]!='(':return []
    parts=[];begin=start+1;depth=1;i=begin
    while i<len(code):
        if code.startswith('"""',i):
            end=code.find('"""',i+3);i=len(code) if end<0 else end+3;continue
        if code[i] in '\"\'':
            quote=code[i];i+=1
            while i<len(code):
                if code[i]=='\\':i+=2
                elif code[i]==quote:i+=1;break
                else:i+=1
            continue
        if code[i] in '({[':depth+=1
        elif code[i] in ')}]':
            depth-=1
            if depth==0:return parts+[code[begin:i]]
        elif code[i]==',' and depth==1:parts.append(code[begin:i]);begin=i+1
        i+=1
    return []

def analyze_source(path,text):
    code=code_only(text);out=[]
    patterns=[
      (r'\b(?:[\w]+\s*\.\s*)?R\s*\.\s*layout\b','layout-resource-or-import'),
      (r'\b(?:inflate|inflateAsync|getLayout)\s*(?:<[^>\n]+>)?\s*\(','layout-inflation-entry'),
      (r'::\s*(?:inflate|inflateAsync|getLayout)\b','layout-inflation-callable-reference'),
      (r'\b(?:ViewBinding|ViewDataBinding|DataBindingUtil|AndroidViewBinding)\b|\.databinding\b','binding-layout-escape'),
      (r'\bR(?:\\?\$|\\u0024)layout\b','reflective-layout-class'),
      (r'\b(?:XmlPullParserFactory|newPullParser|XmlResourceParser)\b','xml-layout-parser-escape'),
      (r'[\"\'][^\"\'\n]*(?:LayoutInflater|R\$layout)[^\"\'\n]*[\"\']','reflective-layout-inflater'),
    ]
    for pattern,rule in patterns:
        for m in re.finditer(pattern,code):
            out.append(issue(path,code,m.start(),rule,m[0]))
    for m in re.finditer(r'@(?:[\w.]+:)?layout/\w+',code):out.append(issue(path,code,m.start(),'literal-xml-layout-reference',m[0]))
    for literal in re.finditer(r'"""[\s\S]*?"""|"(?:\\.|[^"\\])*"',code):
        for m in VIEW_TAG.finditer(literal[0]):out.append(issue(path,code,literal.start()+m.start(),'embedded-layout-xml',m[0]))
    fixture_path=re.search(r'(?:res|resources|assets)/layout(?:[-/][^\"\'\s]*)?',code)
    if fixture_path and re.search(r'\b(?:getResourceAsStream|getResource|openRawResource|File|readText|readBytes|readString|newInputStream)\s*\(|assets\s*\.\s*open\s*\(',code):
        out.append(issue(path,code,fixture_path.start(),'executable-layout-fixture-path',fixture_path[0]))
    for m in re.finditer(r'\b(?:setContentView|addContentView)\s*\(',code):
        arguments=call_arguments(code,m.end()-1)
        first=arguments[0].strip() if arguments else ''
        known=first.startswith('ComposeView(')
        known |= path in {'app/src/main/java/com/celzero/bravedns/ui/activity/SmartDnsListActivity.kt','app/src/main/java/com/celzero/bravedns/ui/activity/ConfigureOtherDnsActivity.kt'} and first.startswith('dnsFragmentHost(')
        named={'app/src/main/java/com/celzero/bravedns/ui/activity/RpnConfigDetailActivity.kt':'composeRoot','app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgDialogContent.kt':'view'}
        if first==named.get(path):known |= bool(re.search(r'\b'+first+r'\s*(?:=\s*ComposeView\s*\(|:\s*ComposeView\b)',code))
        # Four pre-existing isolated lifecycle fixtures construct genuine View
        # containers programmatically. They do not inflate or load any XML.
        compact_first=re.sub(r'\s+',' ',first)
        if path=='app/src/test/java/com/celzero/bravedns/ui/compose/chrome/SnackbarContractTest.kt':
            known |= first=='root' and bool(re.search(r'\broot\s*=\s*FrameLayout\(activity\)',code)) and len(re.findall(r'\broot\s*=',code))==1
        if path=='app/src/test/java/com/celzero/bravedns/ui/compose/hosts/ActivityFragmentContainerTest.kt':
            known |= first=='container' and 'val container = FragmentContainerView(this).apply { id = R.id.fragment_container }' in re.sub(r'\s+',' ',code)
        if path=='app/src/test/java/com/celzero/bravedns/ui/compose/settings/SingleSettingsFragmentHostTest.kt':
            known |= compact_first.startswith('ViewPager2(this).apply {') and 'id = android.R.id.list' in compact_first
        if path=='app/src/test/java/com/celzero/bravedns/ui/compose/trafficlogs/TrafficLogFragmentControllerTest.kt':
            known |= compact_first=='FrameLayout(host.get()).apply { id = containerId }'
        if not known:out.append(issue(path,code,m.start(),'opaque-layout-capable-content-root',first[:120]))
    if path!='app/src/main/java/com/celzero/bravedns/ui/BaseActivity.kt':
        for m in re.finditer(r'\b(?:BaseActivity|AppCompatActivity|Fragment|DialogFragment)\s*\(\s*[^)\s]',code):out.append(issue(path,code,m.start(),'layout-capable-constructor',m[0]))
    # Both R-class aliases and direct layout-member aliases are caught at the
    # import; this also catches Java static layout imports before usage changes.
    for m in re.finditer(r'^\s*import\s+(?:static\s+)?([\w.]+)(?:\s+as\s+(\w+))?',code,re.M):
        target,alias=m[1],m[2]
        if '.R.layout' in target:out.append(issue(path,code,m.start(),'layout-import-alias',m[0].strip()))
        if alias and target.endswith('.R'):
            for hit in re.finditer(r'\b'+re.escape(alias)+r'\s*\.\s*layout\b',code):out.append(issue(path,code,hit.start(),'aliased-layout-resource',hit[0]))
    # All current allowed dynamic lookup kinds are non-layout. A non-literal,
    # multiline or assembled type is rejected unless this bounded call proves it.
    for m in re.finditer(r'\bgetIdentifier\b',code):
        start=re.match(r'\s*\(',code[m.end():]);args=call_arguments(code,m.end()+start.end()-1) if start else []
        if len(args)!=3 or not re.fullmatch(r'"(?:string|drawable|color|id)"',args[1].strip()):
            out.append(issue(path,code,m.start(),'unproven-dynamic-resource-kind',m[0]))
    return out

def analyze_build(path,text):
    code=code_only(text);out=[]
    allowed={
      'app/build.gradle.kts':{'getByName("main").res.directories.add("src/full/res")','getByName("full").res.directories.clear()'},
      'Android.bp':{'resource_dirs: ["app/src/main/res"],'},
    }
    for hit in re.finditer(r'\b(?:srcDir|srcDirs|setSrcDirs)\b\s*(?:\(|=|\[|[\"\'])|\b(?:addGeneratedSourceDirectory|addStaticSourceDirectory|registerGeneratedResFolders)\b',code):
        line_start=code.rfind('\n',0,hit.start())+1
        if re.search(r'\b(?:val|var)\s*$',code[line_start:hit.start()]):continue
        out.append(issue(path,code,hit.start(),'unreviewed-resource-source-or-generation',hit[0]))
    for num,line in enumerate(code.splitlines(),1):
        if re.search(r'addGeneratedSourceDirectory|registerGeneratedRes|generatedResFolders|\bres\s*\.\s*(?:srcDir|srcDirs|setSrcDirs|directories)|resource_dirs\s*:|resources\s*\.\s*(?:srcDir|srcDirs|setSrcDirs)|sourceSets[^\n]*resources',line):
            if line.strip() not in allowed.get(path,set()):out.append({'path':path,'line':num,'rule':'unreviewed-resource-source-or-generation','detail':line.strip()})
        if re.search(r'\bresValue\s*\(\s*["\'](?:layout|id)["\']|(?:res|resources|assets)/layout|["\']layout(?:-[^/"\']+)?/',line):
            out.append({'path':path,'line':num,'rule':'generated-layout-or-id-escape','detail':line.strip()})
    return out

def analyze_files(files,expected_ids=None,require_ids=True):
    errors=[];layouts=[];declared=collections.defaultdict(list);hashes={};sets=collections.Counter();references=[]
    for path,data in sorted(files.items()):
        if isinstance(data,str):data=data.encode()
        hashes[path]=digest(data)
        if LAYOUT_PATH.search(path):
            layouts.append(path);sets[resource_set(path)]+=1
            errors.append({'path':path,'line':1,'rule':'app-owned-layout-file','detail':'Default final gate requires zero files in every res/layout* directory'})
        if b'\x00' in data:continue
        try:text=data.decode('utf8')
        except UnicodeDecodeError:continue
        executable=path.endswith(('.kt','.java')) and ('/src/' in path or path.startswith('benchmark/'))
        if executable:errors.extend(analyze_source(path,text))
        if path.endswith(BUILD_SUFFIX):errors.extend(analyze_build(path,text))
        if path.endswith('.xml'):
            try:root=ET.fromstring(text)
            except ET.ParseError as exc:
                errors.append({'path':path,'line':1,'rule':'invalid-xml','detail':str(exc)});continue
            ids=resource_ids(path,text)
            if not LAYOUT_PATH.search(path):
                for name in ids:declared[name].append(path)
            if not LAYOUT_PATH.search(path) and (VIEW_TAG.match('<'+root.tag) or root.tag=='layout'):
                errors.append({'path':path,'line':1,'rule':'layout-xml-outside-layout-directory','detail':root.tag})
            for node in root.iter():
                if node.get('type')=='layout':errors.append({'path':path,'line':1,'rule':'layout-resource-alias','detail':ET.tostring(node,encoding='unicode')[:250]})
                for value in list(node.attrib.values())+([node.text] if node.text else []):
                    for m in LAYOUT_REF.finditer(value):
                        errors.append({'path':path,'line':1,'rule':'xml-layout-reference','detail':m[0]})
        # A renamed fixture/asset is still executable input. Archived evidence in
        # docs JSON is not an Android/test input and is intentionally outside this.
        if '/src/' in path and re.search(r'/(?:assets|resources|res/raw)/',path) and VIEW_TAG.search(text):
            errors.append({'path':path,'line':1,'rule':'renamed-xml-layout-fixture','detail':'Layout XML detected in packaged/test input regardless of extension'})
    if require_ids:
        if IDS not in files:errors.append({'path':IDS,'line':1,'rule':'missing-identity-resource','detail':'Pinned baseline IDs must remain declared independently of XML layouts'})
        else:
            raw=files[IDS].decode() if isinstance(files[IDS],bytes) else files[IDS]
            try:
                tree=ET.fromstring(raw);items=list(tree);actual=[n.get('name') for n in items]
                if tree.tag!='resources' or any(n.tag!='item' or n.get('type')!='id' or set(n.attrib)!={'name','type'} or list(n) or (n.text or '').strip() for n in items):
                    errors.append({'path':IDS,'line':1,'rule':'identity-resource-shape','detail':'Only empty <item name=... type="id"/> declarations are permitted'})
                if len(actual)!=len(set(actual)):errors.append({'path':IDS,'line':1,'rule':'duplicate-identity-name','detail':'Each baseline ID must be declared once'})
                if expected_ids is not None:
                    missing=sorted(set(expected_ids)-set(actual));extra=sorted(set(actual)-set(expected_ids))
                    if missing or extra:errors.append({'path':IDS,'line':1,'rule':'identity-baseline-mismatch','detail':json.dumps({'missing':missing,'extra':extra})})
                    for name in sorted(expected_ids):
                        if declared.get(name)!=[IDS]:errors.append({'path':IDS,'line':1,'rule':'duplicate-or-missing-external-id','detail':name+': '+repr(declared.get(name,[]))})
            except ET.ParseError:pass # invalid-xml already recorded
    return {'errors':sorted(errors,key=lambda e:(e['path'],e['line'],e['rule'],e['detail'])),'layout_files':layouts,'layout_counts_by_source_set':dict(sorted(sets.items())),'nonlayout_id_declarations':len(declared),'scanned_file_sha256':hashes}

def baseline_ids(repo):
    data=subprocess.check_output(['git','archive',BASE,'app/src'],cwd=repo)
    identities=set();sources={}
    with tarfile.open(fileobj=io.BytesIO(data)) as archive:
        for member in archive:
            if not member.isfile() or not LAYOUT_PATH.search(member.name) or member.name in PREVIOUSLY_REMOVED:continue
            raw=archive.extractfile(member).read();sources[member.name]=digest(raw)
            identities.update(resource_ids(member.name,raw.decode()))
    if len(sources)!=186 or len(identities)!=2516:raise ValueError('Pinned original inventory count differs from 186 layouts / 2516 identities')
    return identities,sources

def self_test():
    negatives={
      'default layout':{'app/src/main/res/layout/old.xml':'<FrameLayout/>'},
      'qualified test layout':{'app/src/testWebsite/res/layout-w720dp-night/old.xml':'<TextView/>'},
      'unknown flavor layout':{'app/src/future/res/layout-ldrtl/old.xml':'<merge/>'},
      'other module layout':{'module/src/debug/res/layout/old.xml':'<LinearLayout/>'},
      'renamed layout XML':{'app/src/test/resources/old.xml':'<androidx.constraintlayout.widget.ConstraintLayout/>'},
      'renamed raw fixture':{'app/src/main/assets/old.fixture':'<TextView/>'},
      'alias values':{'app/src/main/res/values/old.xml':'<resources><item type="layout" name="old">@android:layout/simple_list_item_1</item></resources>'},
      'tools reference':{'app/src/main/res/xml/old.xml':'<x xmlns:tools="http://schemas.android.com/tools" tools:layout="@layout/old"/>'},
    }
    code={
      'R reference':'val x = R.layout.old',
      'spaced qualified R':'val x = com.example.R \n . layout . old',
      'R alias':'import com.example.R as Assets\nval x=Assets.layout.old',
      'member alias':'import com.example.R.layout.old as Screen\nval x=Screen',
      'Java static':'import static com.example.R.layout.old;',
      'inflater call':'inflater\n.inflate\n(old, parent)',
      'numeric inflation':'LayoutInflater.from(ctx).inflate(1234,null)',
      'opaque content root':'setContentView(layoutProvider())',
      'numeric Activity constructor':'class Foo : Fragment(123)',
      'framework layout no longer accepted':'setContentView(android.R.layout.activity_list_item)',
      'generic inflation':'fun f()=loader.inflate<View>(id,parent)',
      'callable inflation':'val f=inflater::inflate',
      'getLayout':'resources.getLayout(1)',
      'reflective class':'Class.forName("com.example.R$layout")',
      'reflective inflater':'Class.forName("android.view.LayoutInflater")',
      'dynamic layout kind':'resources.getIdentifier(name,"layout",packageName)',
      'dynamic unknown kind':'resources.getIdentifier(name,type,packageName)',
      'nested misleading lookup kind':'resources.getIdentifier(helper(name, "string", x),type,packageName)',
      'dynamic multiline kind':'resources.getIdentifier(\nname,\n"layout",\npackageName)',
      'raw parser':'XmlPullParserFactory.newInstance().newPullParser()',
      'embedded XML':'val old = """<LinearLayout><TextView/></LinearLayout>"""',
      'binding':'DataBindingUtil.setContentView(this,1)',
      'AndroidViewBinding':'AndroidViewBinding(OldBinding::inflate)',
      'fixture path':'val old=javaClass.getResourceAsStream("/resources/layout/old.xml")',
      'Kotlin interpolation':'val old="${inflater.inflate(1,null)}"',
      'XML literal reference':'val old="@layout/old"',
    }
    for label,text in code.items():negatives[label]={'app/src/testFresh/java/example/Probe.kt':text}
    for label,text in {
      'new resource source':'android.sourceSets["test"].res.srcDirs("fixtures")',
      'generated resource source':'sources.res.addGeneratedSourceDirectory(task, gen)',
      'new static resource source':'sources.res.addStaticSourceDirectory("fixtures")',
      'multiline resource source':'res\n .srcDir("fixtures")',
      'nested resource source':'sourceSets { test { res { srcDirs("fixtures") } } }',
      'resource generation':'resValue("layout","old","@layout/fixture")',
      'fixture copy':'val target="src/main/res/layout/new.xml"',
      'unexpected Soong resources':'resource_dirs: ["app/src/test/resources"],',
    }.items():negatives[label]={'app/build.gradle.kts':text}
    for label,files in negatives.items():assert analyze_files(files,require_ids=False)['errors'],label+' escaped'
    positive={
      'app/src/main/res/values/ids.xml':'<resources><item name="one" type="id"/></resources>',
      'app/src/main/res/drawable/icon.xml':'<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp"/>',
      'app/src/main/AndroidManifest.xml':'<manifest><application/></manifest>',
      'app/src/test/java/example/Probe.kt':'/* outer /* R.layout.bad */ inflater.inflate(1) */\n//R.layout.old\nval t="https://site"\nval id=resources.getIdentifier(name, "string", packageName)\nval id2=resources.getIdentifier(name.substringAfter("R.string."), "string", packageName)\nval typeOnly = mutableListOf<View>()\nval provenance="app/src/main/res/layout/old.xml"',
      'app/build.gradle.kts':'getByName("main").res.directories.add("src/full/res")\ngetByName("full").res.directories.clear()\nval srcDir = file("src")\nval hasSource = srcDir.listFiles()',
    }
    assert not analyze_files(positive,require_ids=False)['errors'],'harmless source rejected'
    valid={IDS:'<resources><item name="one" type="id"/></resources>'}
    assert not analyze_files(valid,{'one'})['errors']
    synthetic={'app/src/test/java/com/celzero/bravedns/ui/compose/chrome/SnackbarContractTest.kt':'root = FrameLayout(activity)\nsetContentView(root)'}
    assert not analyze_files(synthetic,require_ids=False)['errors']
    assert analyze_files({p:t+'\nroot = oldResourceId' for p,t in synthetic.items()},require_ids=False)['errors']
    id_mutations=[{}, {IDS:'<resources/>'},{IDS:'<resources><item name="two" type="id"/></resources>'},{IDS:'<resources><item name="one" type="string"/></resources>'},{IDS:'<resources><item name="one" type="id"/><item name="one" type="id"/></resources>'},{**valid,'app/src/main/res/values/other.xml':'<resources><item name="one" type="id"/></resources>'}]
    for files in id_mutations:assert analyze_files(files,{'one'})['errors'],'identity mutation escaped'
    return len(negatives)+4+len(id_mutations)

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--repo',type=Path,default=ROOT);parser.add_argument('--report',type=Path);parser.add_argument('--self-test',action='store_true');args=parser.parse_args()
    repo=args.repo.resolve();controls=self_test() if args.self_test else None
    if controls:print('PASS:',controls,'in-memory negative/positive controls')
    files={};symlinks=[]
    for p in sorted(repo.rglob('*')):
        rel=p.relative_to(repo);parts=rel.parts
        if any(x in ('.git','.gradle','build','__pycache__') for x in parts):continue
        if p.is_symlink():
            symlinks.append({'path':str(rel),'line':1,'rule':'unreviewed-source-symlink','detail':'Symlinks may hide resource/fixture input outside the inspected tree; no target read performed'})
            continue
        if not p.is_file():continue
        # Docs are non-executable evidence, except XML which may not hide copies.
        if parts[0]=='docs' and p.suffix!='.xml':continue
        if p.suffix in ('.xml','.kt','.java','.gradle','.kts','.bp','.properties','.pro','.toml') or '/src/' in str(rel) and any(x in parts for x in ('assets','resources','raw')) or LAYOUT_PATH.search(str(rel)):
            files[str(rel)]=p.read_bytes()
    try:expected,baseline=baseline_ids(repo)
    except Exception as exc:print('FAIL: cannot verify immutable baseline:',exc);return 1
    report=analyze_files(files,expected);report['errors'].extend(symlinks);report.update({'status':'FAIL' if report['errors'] else 'PASS','source_only':True,'baseline_commit':BASE,'baseline_resource_sha256':baseline,'expected_preserved_ids':len(expected),'guard_sha256':digest(Path(__file__).read_bytes()),'self_test_controls':controls,'limits':['No compilation, resource linker, APK/dependency-bytecode inspection, device or visual testing.','Known direct/resource/parser/source-generation escapes are rejected; arbitrary computed reflection cannot be proved absent by lexical source checks.','Framework and dependency-internal resources cannot be deleted here, but app/test source may not request any R.layout; notification now requires empty Compose presentation.']})
    if args.report:
        out=args.report if args.report.is_absolute() else repo/args.report;out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(report,indent=2)+'\n')
    print(report['status']+': '+str(len(report['layout_files']))+' app-owned layout files; '+str(len(report['errors']))+' violations; '+str(len(expected))+' pinned ID symbols')
    for error in report['errors'][:30]:print(error['path']+':'+str(error['line'])+': '+error['rule']+': '+error['detail'][:240])
    if len(report['errors'])>30:print('Remaining violations recorded in report:',len(report['errors'])-30)
    return 1 if report['errors'] else 0
if __name__=='__main__':sys.exit(main())
