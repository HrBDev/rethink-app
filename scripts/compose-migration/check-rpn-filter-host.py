#!/usr/bin/env python3
"""Runs extracted actual pure filter/format bodies with plain data, never real managers."""
from rpn_host import ROOT,args,run
from rpn_source import functions
source=(ROOT/'app/src/main/java/com/celzero/bravedns/ui/fragment/ServerSelectionFragment.kt').read_text()
methods=functions(source)
fixture='''import java.util.Locale
class CountryConfig(val countryName:String="United States", val serverLocation:String="New York", val cc:String="US", val load:Int=20, val link:Int=1000, val isFavourite:Boolean=false)
enum class LoadFilter { ALL, LOW, MEDIUM, HIGH }
class FilterFixture {
 var loadFilter=LoadFilter.ALL
 var speedFilter=0
 var favouritesOnly=false
'''+methods['matchesFilters'][0].replace('private fun','fun')+'\n'+methods['formatLinkSpeed'][0].replace('private fun','fun')+'\n}\n'
main='''fun main() {
 val f=FilterFixture(); var cases=0
 fun result(expected:Boolean, server:CountryConfig=CountryConfig(), query:String="") {
  val actual=f.matchesFilters(server,query); check(actual==expected) { "case ${cases+1}: expected $expected got $actual" }; cases++
 }
 for ((q, expected) in listOf("" to true,"united" to true,"york" to true,"us" to true,"nowhere" to false)) result(expected,query=q)
 val loadCases=listOf(Triple(LoadFilter.ALL,0,true), Triple(LoadFilter.LOW,0,false), Triple(LoadFilter.LOW,1,true), Triple(LoadFilter.LOW,40,true), Triple(LoadFilter.LOW,41,false), Triple(LoadFilter.MEDIUM,40,false), Triple(LoadFilter.MEDIUM,41,true), Triple(LoadFilter.MEDIUM,80,true), Triple(LoadFilter.MEDIUM,81,false), Triple(LoadFilter.HIGH,0,false), Triple(LoadFilter.HIGH,80,false), Triple(LoadFilter.HIGH,81,true))
 for ((tier,load,expected) in loadCases) { f.loadFilter=tier; result(expected,CountryConfig(load=load)) }
 f.loadFilter=LoadFilter.ALL
 for ((speed,link,expected) in listOf(Triple(0,0,true),Triple(0,1000,true),Triple(1000,0,false),Triple(1000,1000,true),Triple(1000,10000,false))) { f.speedFilter=speed;result(expected,CountryConfig(link=link)) }
 f.speedFilter=0;f.favouritesOnly=true
 result(false,CountryConfig(isFavourite=false));result(true,CountryConfig(isFavourite=true))
 f.loadFilter=LoadFilter.LOW;result(false,CountryConfig(load=90,isFavourite=true))
 for ((speed,expected) in listOf(0 to "0 Mbps",100 to "100 Mbps",1000 to "1 Gbps",2500 to "2.5 Gbps",10000 to "10 Gbps")) { check(f.formatLinkSpeed(speed)==expected);cases++ }
 check(cases==30);println("PASS: 30 cases against actual RPN filter and link-format method bodies.")
}
'''
run({'Fixture.kt':fixture,'Main.kt':main},'MainKt',args())
