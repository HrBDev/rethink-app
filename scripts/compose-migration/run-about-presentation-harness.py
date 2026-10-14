#!/usr/bin/env python3
"""Runs exact side-effect-free presentation classes against tiny framework fakes.
No Gradle, Android application, production controller, profile, file export or DB is started.
This complements (does not replace) Robolectric/Compose validation.
"""
from pathlib import Path
import importlib.util,subprocess,tempfile,os
ROOT=Path(__file__).resolve().parents[2]
CACHE=ROOT.parent/'toolchain/gradle-user-home/caches/modules-2/files-2.1'
JAVA=ROOT.parent/'toolchain/java/jdk-17.0.20.1+1/bin/java'
PRESENTATION=ROOT/'app/src/main/java/com/celzero/bravedns/ui/compose/about'
spec=importlib.util.spec_from_file_location('guard',Path(__file__).with_name('check-wireguard-controllers.py'));g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g)
def extract(file,name):
 source=(PRESENTATION/file).read_text();_,mask=g.lex(source)
 start=mask.index('class '+name);body=mask.index('{',start);i=body+1;level=1
 while level:
  if mask[i]=='{':level+=1
  elif mask[i]=='}':level-=1
  i+=1
 return source[start:i]
def jar(group,name,version):return next((CACHE/group/name/version).glob('*/*.jar'))
stdlib=jar('org.jetbrains.kotlin','kotlin-stdlib','2.4.10')
cp=[jar('org.jetbrains.kotlin',n,'2.4.10') for n in ['kotlin-compiler-embeddable','kotlin-build-tools-api','kotlin-script-runtime','kotlin-daemon-embeddable']]
cp += [stdlib,jar('org.jetbrains.kotlin','kotlin-reflect','1.6.10'),jar('org.jetbrains.kotlinx','kotlinx-coroutines-core-jvm','1.8.0'),jar('org.jetbrains','annotations','23.0.0')]
with tempfile.TemporaryDirectory(prefix='about-presentation-') as temp:
 d=Path(temp)
 (d/'Runtime.kt').write_text('''package androidx.compose.runtime
import kotlin.reflect.KProperty
class MutableState<T>(var value:T)
fun <T> mutableStateOf(value:T)=MutableState(value)
fun mutableIntStateOf(value:Int)=MutableState(value)
operator fun <T> MutableState<T>.getValue(owner:Any?,p:KProperty<*>)=value
operator fun <T> MutableState<T>.setValue(owner:Any?,p:KProperty<*>,v:T){ value=v }
fun <T> mutableStateListOf()=mutableListOf<T>()
''')
 (d/'Dialog.kt').write_text('package android.content\ninterface DialogInterface { fun dismiss(); fun cancel() }\n')
 classes=[('AboutDiagnosticsContent.kt','DiagnosticTabsState'),('AboutDiagnosticsContent.kt','DiagnosticSelectionState'),('AboutDiagnosticsContent.kt','DatabaseDumpState'),('DnsRecordTypesContent.kt','DiagnosticToggleGroup'),('AboutDialog.kt','AboutDialogActionGate')]
 (d/'Models.kt').write_text('package harness\nimport androidx.compose.runtime.*\nimport android.content.DialogInterface\n'+'\n\n'.join(extract(*c) for c in classes))
 (d/'Main.kt').write_text('''package harness
import android.content.DialogInterface
fun main() {
    val tabs=DiagnosticTabsState(listOf("JVM Stack" to "jvm", "Go Stack" to "go"))
    check(tabs.selectedTab==0); tabs.select(1); tabs.select(1)
    check(tabs.selectedTab==1 && tabs.selectionEpoch==2)
    val selected=DiagnosticSelectionState(listOf("first","second","third"),"all")
    check(selected.textToCopy()=="all"); selected.toggle(2); selected.toggle(0)
    check(selected.textToCopy()=="first\\nthird"); selected.toggle(2); selected.toggle(0)
    check(selected.textToCopy()=="all")
    val db=DatabaseDumpState(); db.append("\\nLoading fake ...\\n")
    db.text=db.text.toString().replace("Loading fake ...",""); db.append("fake result")
    check(db.text.endsWith("fake result") && !db.text.contains("Loading"))
    val trace=mutableListOf<String>(); val toggle=DiagnosticToggleGroup(1)
    toggle.addOnButtonCheckedListener { _,id,checked -> trace += "$id:$checked" }
    toggle.check(2);toggle.check(2);toggle.check(1)
    check(trace==listOf("1:false","2:true","2:false","1:true"));trace.clear()
    val dialog=object:DialogInterface { override fun dismiss(){trace += "dismiss"};override fun cancel(){trace += "cancel"} }
    val gate=AboutDialogActionGate()
    gate.dispatch(dialog,-1){_,which->trace += "action:$which"}
    gate.dispatch(dialog,-2){_,_->error("repeated terminal callback")}
    check(trace==listOf("action:-1","dismiss"));trace.clear()
    AboutDialogActionGate().dispatch(dialog,-3){d,_->d.dismiss();trace += "refresh"}
    check(trace==listOf("dismiss","refresh","dismiss"))
    println("PASS: 6 exact-source fake presentation scenarios; no Android, services, DB, profiles or filesystem work")
}
''')
 cmd=[str(JAVA),'-Xmx512m','-cp',os.pathsep.join(map(str,cp)),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-classpath',str(stdlib),'-d',str(d/'out')]+[str(p) for p in d.glob('*.kt')]
 subprocess.run(cmd,check=True,cwd=ROOT)
 subprocess.run([str(JAVA),'-cp',str(d/'out')+os.pathsep+str(stdlib),'harness.MainKt'],check=True,cwd=ROOT)
