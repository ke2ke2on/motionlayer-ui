package com.uhi.motionlayer.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/** 한글 낱말 단위 줄바꿈(보고서 6.3)을 위한 WORD JOINER 넣기가 낱말 안에만 들어가는지 확인한다. */
class KeepKoreanWordsTest {
    private val wj = '⁠'

    @Test
    fun joinsSyllablesInsideAWord() {
        assertEquals("나${wj}와${wj}요.", "나와요.".keepKoreanWordsTogether())
    }

    @Test
    fun doesNotJoinAcrossSpaces() {
        assertEquals("고${wj}르${wj}면 나${wj}와${wj}요", "고르면 나와요".keepKoreanWordsTogether())
    }

    @Test
    fun joinsHangulWithAdjacentLatinAndDigits() {
        assertEquals("PC${wj}의 12${wj}개", "PC의 12개".keepKoreanWordsTogether())
    }

    @Test
    fun leavesTextWithoutHangulAlone() {
        val text = "YouTube Music 12ms"
        assertSame(text, text.keepKoreanWordsTogether())
    }

    @Test
    fun leavesSingleCharactersAndEmptyAlone() {
        assertEquals("", "".keepKoreanWordsTogether())
        assertEquals("가", "가".keepKoreanWordsTogether())
    }
}
