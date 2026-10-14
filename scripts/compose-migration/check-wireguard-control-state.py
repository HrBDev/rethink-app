#!/usr/bin/env python3
"""Compile actual WG control model/test bodies against minimal host stubs; no Android execution."""
import argparse, pathlib, re, subprocess, tempfile
p=argparse.ArgumentParser()
p.add_argument('--gradle-cache', required=True, type=pathlib.Path)
p.add_argument('--java-home', required=True, type=pathlib.Path)
a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
cache=a.gradle_cache/'modules-2/files-2.1'
def jar(group, artifact, version):
    matches=list((cache/group/artifact/version).glob('*/*.jar'))
    if len(matches)!=1: raise SystemExit(f'Expected one existing {artifact}:{version} JAR, got {matches}; no downloads attempted')
    return matches[0]
stdlib=jar('org.jetbrains.kotlin','kotlin-stdlib','2.4.10')
jars=[jar('org.jetbrains.kotlin','kotlin-compiler-embeddable','2.4.10'), stdlib,
      jar('org.jetbrains.kotlin','kotlin-script-runtime','2.4.10'),
      jar('org.jetbrains.kotlin','kotlin-reflect','1.6.10'),
      jar('org.jetbrains.kotlinx','kotlinx-coroutines-core-jvm','1.8.0'),
      jar('org.jetbrains','annotations','13.0')]
java=str(a.java_home/'bin/java')
model=root/'app/src/main/java/com/celzero/bravedns/ui/compose/wireguard/WgControlState.kt'
tests=(root/'app/src/test/java/com/celzero/bravedns/ui/compose/wireguard/WgControlStateTest.kt').read_text()
names=re.findall(r'@Test fun (\w+)\(\)',tests)
stubs={
'Context.kt':'package android.content; open class Context { fun getText(id:Int):CharSequence = error("Android resources are outside host harness scope") }',
'Handler.kt':'package android.os; class Looper { companion object { fun getMainLooper()=Looper() } }; class Handler(looper:Looper) { fun postDelayed(action:() -> Unit, delay:Long):Boolean = error("Delayed Android callbacks are outside host harness scope") }',
'Color.kt':'package android.content.res; class ColorStateList',
'Drawable.kt':'package android.graphics.drawable; open class Drawable',
'View.kt':'package android.view; object View { const val VISIBLE=0; const val INVISIBLE=4; const val GONE=8 }',
'Runtime.kt':'''package androidx.compose.runtime
import kotlin.reflect.KProperty
class MutableState<T>(var value:T)
fun <T> mutableStateOf(value:T)=MutableState(value)
operator fun <T> MutableState<T>.getValue(receiver:Any?, property:KProperty<*>)=value
operator fun <T> MutableState<T>.setValue(receiver:Any?, property:KProperty<*>, value:T) { this.value=value }
''',
'WgList.kt':'package com.celzero.bravedns.ui.compose.wireguard; interface WgListContent',
'Checks.kt':'''package com.celzero.bravedns.ui.compose.wireguard
fun assertEquals(expected:Any?, actual:Any?) { check(expected==actual) { "expected=$expected actual=$actual" } }
fun assertTrue(value:Boolean) { check(value) }
fun assertFalse(value:Boolean) { check(!value) }
''',
'Tests.kt':re.sub(r'import org.junit.[^\n]+\n','',tests).replace('@Test ',''),
'Runner.kt':'package com.celzero.bravedns.ui.compose.wireguard\nfun main() { val t=WgControlStateTest(); '+ '; '.join('t.'+n+'()' for n in names)+f'; println("PASS: {len(names)} host-only WG control tests (Kotlin 2.4.10)")'+' }'
}
with tempfile.TemporaryDirectory(prefix='wg-control-host-') as tmp:
    tmp=pathlib.Path(tmp)
    for name,code in stubs.items():(tmp/name).write_text(code)
    target=tmp/'classes'
    result=subprocess.run([java,'-cp',':'.join(map(str,jars)),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler',
        '-no-stdlib','-no-reflect','-classpath',str(stdlib),'-jvm-target','17','-d',str(target),str(model),*[str(f) for f in tmp.glob('*.kt')]],text=True,capture_output=True)
    if result.returncode:
        print(result.stdout+result.stderr);raise SystemExit(result.returncode)
    subprocess.run([java,'-cp',str(target)+':'+str(stdlib),'com.celzero.bravedns.ui.compose.wireguard.RunnerKt'],check=True)
print('LIMIT: minimal Android/Compose-state stubs; not real snapshots, Android compilation, Gradle tests or service tests.')
