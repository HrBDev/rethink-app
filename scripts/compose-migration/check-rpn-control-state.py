#!/usr/bin/env python3
"""Compiles actual RPN UI state and actual test bodies against isolated host stubs."""
from rpn_host import ROOT, args, run
sources={
 'View.kt':'package android.view\nclass View { companion object { const val VISIBLE=0; const val INVISIBLE=4; const val GONE=8 } }',
 'Drawable.kt':'package android.graphics.drawable\nopen class Drawable',
 'Colors.kt':'package android.content.res\nclass ColorStateList { companion object { fun valueOf(value:Int)=ColorStateList() } }',
 'Keep.kt':'package androidx.annotation\nannotation class Keep',
 'Compose.kt':'''package androidx.compose.runtime
import kotlin.reflect.KProperty
class MutableState<T>(var value:T)
fun <T> mutableStateOf(value:T)=MutableState(value)
operator fun <T> MutableState<T>.getValue(owner:Any?, property:KProperty<*>)=value
operator fun <T> MutableState<T>.setValue(owner:Any?, property:KProperty<*>, value:T) { this.value=value }
fun <T> mutableStateListOf(vararg values:T)=values.toMutableList()
''',
 'Motion.kt':'package com.celzero.bravedns.ui.compose.rpn\nclass RpnPropertyAnimator(state:RpnControlState)',
 'Junit.kt':'''package org.junit
annotation class Test
object Assert {
 fun assertEquals(expected:Any?, actual:Any?) { check(expected==actual) { "Expected $expected, got $actual" } }
 fun assertTrue(actual:Boolean) { check(actual) }
 fun assertFalse(actual:Boolean) { check(!actual) }
}
''',
 'Main.kt':'''import com.celzero.bravedns.ui.compose.rpn.RpnControlStateTest
fun main() {
 val test=RpnControlStateTest()
 val methods=test.javaClass.declaredMethods.filter { it.getAnnotation(org.junit.Test::class.java)!=null }.sortedBy { it.name }
 check(methods.size==8)
 methods.forEach { it.invoke(test) }
 println("PASS: 8 actual RPN state callback test bodies; JVM accessors compiled.")
}
'''
}
for name in ['RpnControlState','RpnChoiceState']:
 sources[name+'.kt']=(ROOT/f'app/src/main/java/com/celzero/bravedns/ui/compose/rpn/{name}.kt').read_text()
sources['Tests.kt']=(ROOT/'app/src/test/java/com/celzero/bravedns/ui/compose/rpn/RpnControlStateTest.kt').read_text()
run(sources,'MainKt',args())
