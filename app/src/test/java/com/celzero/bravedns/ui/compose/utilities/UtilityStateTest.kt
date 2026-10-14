package com.celzero.bravedns.ui.compose.utilities
import android.app.Application
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=Application::class)
class UtilityStateTest {
    private fun control(tag:String="fake")=UtilityControl(ApplicationProvider.getApplicationContext(),tag)
    @Test fun descriptionLimitPreservesSuffixAndDoesNotSplitSurrogatePairs() {
        assertEquals("abcd",utilityLengthLimitedInput("abcd","abXcd",4))
        assertEquals("abXcd",utilityLengthLimitedInput("abcd","abXYcd",5))
        assertEquals("abc",utilityLengthLimitedInput("","abc😀",4))
        assertEquals("abc😀",utilityLengthLimitedInput("","abc😀",5))
    }
    @Test fun invalidNativeFontWeightsHaveSemanticFallbacks() {
        assertEquals(400,utilityResolvedWeight(0,-1,"sans-serif",android.graphics.Typeface.NORMAL))
        assertEquals(800,utilityResolvedWeight(0,-1,"sans-serif-medium",android.graphics.Typeface.BOLD))
        assertEquals(600,utilityResolvedWeight(0,600,"sans-serif",android.graphics.Typeface.NORMAL))
        assertEquals(800,utilityResolvedWeight(800,-1,"sans-serif-medium",android.graphics.Typeface.BOLD))
    }
    @Test fun sameCheckedValueAndSynchronousReversalDoNotDuplicateCallback() {
        val c=control();val calls=mutableListOf<Boolean>()
        c.setOnCheckedChangeListener{row,value->calls+=value;row.isChecked=false}
        c.isChecked=true;c.isChecked=false;assertEquals(listOf(true),calls)
    }
    @Test fun deferredTcpReversalDoesBroadcastTheSecondFalseValue() {
        val c=control();val queue=ArrayDeque<()->Unit>();val calls=mutableListOf<Boolean>()
        c.setOnCheckedChangeListener{row,value->calls+=value;queue.add{if(value)row.isChecked=false}}
        c.isChecked=true;assertEquals(listOf(true),calls);queue.removeFirst().invoke();queue.removeFirst().invoke()
        assertEquals(listOf(true,false),calls);assertTrue(queue.isEmpty())
    }
    @Test fun sliderDistinguishesProgrammaticRestorationFromUserInput() {
        val c=control();val calls=mutableListOf<Pair<Float,Boolean>>()
        c.addOnChangeListener{_,value,user->calls+=value to user};c.value=2f;c.value=2f;c.userValue(3f)
        assertEquals(listOf(2f to false,3f to true),calls)
    }
    @Test fun fieldAndSwitchStateRestoresWithoutLosingTextOrSelection() {
        val first=control("saved");first.text=" synthetic text ";first.isChecked=true;first.value=4f
        val bundle=Bundle();saveUtilityControls(bundle,listOf(first));val restored=control("saved");restoreUtilityControls(bundle,listOf(restored))
        assertEquals(" synthetic text ",restored.text);assertTrue(restored.isChecked);assertEquals(4f,restored.value,0f)
    }
    @Test fun allowedExpiryAndBlockedTimeBoundariesMatchBaseline() {
        assertEquals(BubbleAllowedAge(false,14),bubbleAllowedAge(0,899_999));assertEquals(BubbleAllowedAge(true,15),bubbleAllowedAge(0,900_000))
        assertEquals(BubbleBlockedAge("now",0),bubbleBlockedAge(0,59_999));assertEquals(BubbleBlockedAge("minutes",1),bubbleBlockedAge(0,60_000))
        assertEquals(BubbleBlockedAge("hours",1),bubbleBlockedAge(0,3_600_000));assertEquals(BubbleBlockedAge("date",0),bubbleBlockedAge(0,86_400_000))
    }
}
