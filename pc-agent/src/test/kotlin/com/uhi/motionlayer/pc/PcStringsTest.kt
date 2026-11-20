package com.uhi.motionlayer.pc

import com.uhi.motionlayer.pc.data.fake.PcSampleData
import com.uhi.motionlayer.pc.data.fake.fakeQrPattern
import com.uhi.motionlayer.pc.domain.LinkPath
import com.uhi.motionlayer.pc.domain.OverlayState
import com.uhi.motionlayer.pc.ui.PcStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 값이 Figma 문구 모양으로 바뀌는지 본다. */
class PcStringsTest {

    @Test
    fun pairingCodeIsSplitInThrees() {
        assertEquals("482 913", PcStrings.pairingDigits("482913"))
    }

    @Test
    fun codeHintShowsMinutesAndSeconds() {
        assertEquals("QR을 못 찍으면 이 코드를 입력하세요 · 0:42 뒤 바뀌어요", PcStrings.codeHint(42))
        assertEquals("QR을 못 찍으면 이 코드를 입력하세요 · 1:00 뒤 바뀌어요", PcStrings.codeHint(60))
        assertEquals("QR을 못 찍으면 이 코드를 입력하세요 · 0:05 뒤 바뀌어요", PcStrings.codeHint(5))
    }

    @Test
    fun connectedPhoneDetailMatchesFigma() {
        assertEquals("같은 와이파이 · 12ms · 이번만 연결", PcStrings.phoneDetail(PcSampleData.phone))
        assertEquals(
            "서버 경유 · 180ms · 내 기기",
            PcStrings.phoneDetail(PcSampleData.phone.copy(path = LinkPath.Relay, latencyMs = 180, oneTime = false)),
        )
    }

    @Test
    fun presentationValuesMatchFigma() {
        val sample = PcSampleData.presentation
        assertEquals("보통", PcStrings.pointerSize(sample.pointerSize))
        assertEquals("파랑", PcStrings.pointerColor(sample.pointerColor))
        assertEquals("3초 뒤", PcStrings.strokeFade(sample.strokeFadeSeconds))
        assertEquals("3개 · 40MB", PcStrings.effects(sample.effectCount, sample.effectBytes))
    }

    @Test
    fun overlayTextsMatchFigma() {
        assertEquals("허윤 조작 중", PcStrings.operator(PcSampleData.OPERATOR))
        assertEquals("손을 떼면 3초 뒤 지워져요", PcStrings.strokeHint(3))
        assertEquals("0:12 / 0:40", PcStrings.effectTime(PcSampleData.effect))
    }

    @Test
    fun overlayWindowShowsOnlyWhenThereIsSomethingToDraw() {
        assertTrue(OverlayState().isEmpty)
        assertTrue(OverlayState(operatorName = "허윤").isEmpty)
        assertFalse(OverlayState(pointer = PcSampleData.pointer).isEmpty)
        assertFalse(OverlayState(blackout = true).isEmpty)
    }

    @Test
    fun fakeQrHasThreeCornerSquares() {
        val qr = fakeQrPattern(seed = 1)
        val last = qr.size - 1
        // 세 모서리 네모의 바깥 귀퉁이와 한가운데는 검고, 네 번째 모서리 쪽에는 네모가 없다.
        assertTrue(qr.isDark(0, 0) && qr.isDark(3, 3))
        assertTrue(qr.isDark(last, 0) && qr.isDark(last - 3, 3))
        assertTrue(qr.isDark(0, last) && qr.isDark(3, last - 3))
        assertFalse(qr.isDark(1, 1))
    }
}
