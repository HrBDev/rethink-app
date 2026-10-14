#!/usr/bin/env python3
"""Compile exact safe state helpers with tiny host stubs. No Android, database, VPN or Gradle task."""
import re
from rpn_host import ROOT,args,run

def declaration(file,start):
 s=(ROOT/file).read_text();a=s.index(start);brace=s.index('{',a);end=brace+1;depth=1
 while depth:
  depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[a:end]
base='app/src/main/java/com/celzero/bravedns/ui/compose/appdetails/'
sources={
'Runtime.kt':'''package androidx.compose.runtime
import kotlin.reflect.KProperty
class MutableState<T>(var value:T)
fun <T> mutableStateOf(value:T)=MutableState(value)
fun mutableIntStateOf(value:Int)=MutableState(value)
operator fun <T> MutableState<T>.getValue(owner:Any?,property:KProperty<*>)=value
operator fun <T> MutableState<T>.setValue(owner:Any?,property:KProperty<*>,value:T){this.value=value}
''',
'Search.kt':'''package androidx.appcompat.widget
class SearchView { interface OnQueryTextListener { fun onQueryTextChange(value:String):Boolean; fun onQueryTextSubmit(value:String):Boolean } }
''',
'Text.kt':'package androidx.compose.ui.text\ndata class TextRange(val start:Int,val end:Int=start)',
'TextValue.kt':'package androidx.compose.ui.text.input\nimport androidx.compose.ui.text.TextRange\ndata class TextFieldValue(val text:String="",val selection:TextRange=TextRange(0),val composition:TextRange?=null)',
'States.kt':'''package com.celzero.bravedns.ui.compose.appdetails
import androidx.compose.runtime.*
import androidx.appcompat.widget.SearchView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
'''+declaration(base+'AppLogsContent.kt','class AppSearchState')+'\n'+declaration(base+'AppDetailsDialogs.kt','class AppNotesDraft')+'\n'+declaration(base+'AppDetailsDialogs.kt','fun filterAppNotes'),
'Main.kt':'''import com.celzero.bravedns.ui.compose.appdetails.*
import androidx.appcompat.widget.SearchView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
fun main(){
 val calls=mutableListOf<String>();val search=AppSearchState()
 search.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
  override fun onQueryTextChange(value:String):Boolean{calls+="change:$value";return true}
  override fun onQueryTextSubmit(value:String):Boolean{calls+="submit:$value";return true}
 })
 search.setQuery("",false);check(calls.isEmpty())
 search.setQuery("draft",false);check(calls==listOf("change:draft"))
 search.setQuery("draft",false);check(calls.size==1)
 search.setQuery("draft",true);check(calls.last()=="submit:draft")
 search.setQuery("",false);check(calls.last()=="change:")
 search.setQuery("",true);check(calls.size==3)
 val old="a".repeat(249)+"Z".repeat(250)
 val filtered=filterAppNotes(TextFieldValue(old),TextFieldValue("a".repeat(249)+"PASTED"+"Z".repeat(250),TextRange(255)))
 check(filtered.text=="a".repeat(249)+"P"+"Z".repeat(250));check(filtered.selection==TextRange(250))
 val prefix="a".repeat(499);check(filterAppNotes(TextFieldValue(prefix),TextFieldValue(prefix+"\\uD83D\\uDE00")).text==prefix)
 val draft=AppNotesDraft("fixture");check(draft.value.selection==TextRange(7))
 draft.setText("new");draft.setSelection(3);check(draft.value==TextFieldValue("new",TextRange(3)))
 val proposed=TextFieldValue("text",TextRange(2),TextRange(0,4));check(filterAppNotes(TextFieldValue(),proposed)==proposed)
 println("PASS: 12 isolated app search/notes callback and edit checks using exact current helper bodies; not Android runtime parity.")
}
'''
}
run(sources,'MainKt',args())
