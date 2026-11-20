package com.uhi.motionlayer.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class KoreanParticleTest {
    @Test
    fun subject_hangul() {
        assertEquals("이", KoreanParticle.subject("멜론"))
        assertEquals("이", KoreanParticle.subject("지니 뮤직"))
        assertEquals("가", KoreanParticle.subject("카카오"))
        assertEquals("가", KoreanParticle.subject("네이버"))
    }

    @Test
    fun subject_latin() {
        assertEquals("이", KoreanParticle.subject("YouTube Music")) // 뮤직
        assertEquals("가", KoreanParticle.subject("YouTube")) // 유튜브
        assertEquals("가", KoreanParticle.subject("Spotify")) // 스포티파이
    }

    @Test
    fun toward_hangul() {
        assertEquals("로", KoreanParticle.toward("카카오")) // 받침 없음
        assertEquals("로", KoreanParticle.toward("서울")) // ㄹ 받침
        assertEquals("으로", KoreanParticle.toward("멜론")) // ㄴ 받침
        assertEquals("으로", KoreanParticle.toward("윤의 노트북")) // ㄱ 받침
    }

    @Test
    fun toward_acronymsAndWords() {
        assertEquals("로", KoreanParticle.toward("강의실 PC")) // 피씨
        assertEquals("로", KoreanParticle.toward("거실 TV")) // 티브이
        assertEquals("으로", KoreanParticle.toward("MacBook")) // 맥북
        assertEquals("로", KoreanParticle.toward("Google")) // 구글(ㄹ)
    }

    @Test
    fun with_() {
        assertEquals("와", KoreanParticle.with("강의실 PC")) // 피씨
        assertEquals("과", KoreanParticle.with("멜론"))
        assertEquals("와", KoreanParticle.with("카카오"))
        assertEquals("과", KoreanParticle.with("윤의 MacBook")) // 맥북
    }

    @Test
    fun objective() {
        assertEquals("를", KoreanParticle.objective("강의실 PC")) // 피씨
        assertEquals("를", KoreanParticle.objective("카카오"))
        assertEquals("을", KoreanParticle.objective("멜론"))
        assertEquals("을", KoreanParticle.objective("서울")) // ㄹ 받침도 "을"
        assertEquals("을", KoreanParticle.objective("MacBook")) // 맥북
    }

    @Test
    fun or_() {
        assertEquals("나", KoreanParticle.or("카카오"))
        assertEquals("이나", KoreanParticle.or("멜론"))
        assertEquals("이나", KoreanParticle.or("YouTube Music")) // 뮤직
        assertEquals("나", KoreanParticle.or("Spotify")) // 스포티파이
        assertEquals("이나", KoreanParticle.or("서울")) // ㄹ 받침도 "이나"
    }

    @Test
    fun digits() {
        assertEquals("로", KoreanParticle.toward("PC 2")) // 이
        assertEquals("으로", KoreanParticle.toward("PC 3")) // 삼
        assertEquals("로", KoreanParticle.toward("PC 1")) // 일(ㄹ)
        assertEquals("이", KoreanParticle.subject("PC 6")) // 육
    }
}
