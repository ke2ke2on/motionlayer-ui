package com.uhi.motionlayer.pc

import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toPixelMap
import com.uhi.motionlayer.pc.data.fake.FakePresentationOverlay
import com.uhi.motionlayer.pc.data.fake.PcSampleData
import com.uhi.motionlayer.pc.ui.PcStrings
import com.uhi.motionlayer.pc.ui.renderAppMark
import com.uhi.motionlayer.pc.ui.tray.connectedNotification
import com.uhi.motionlayer.ui.theme.LightMotionColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** 트레이 메뉴(PC-04)·연결 알림(PC-05)의 글자와, 트레이·창 아이콘으로 쓰는 앱 표시. */
class TrayAndMarkTest {

    @Test
    fun trayMenuTextsMatchFigma() {
        assertEquals("MotionLayer · 연결 대기", PcStrings.trayStatus(connected = false))
        assertEquals("MotionLayer · 연결됨", PcStrings.trayStatus(connected = true))
        assertEquals("QR 보기", PcStrings.TRAY_SHOW_QR)
        assertEquals("설정 열기…", PcStrings.TRAY_OPEN_SETTINGS)
        assertEquals("발표 오버레이 미리 보기", PcStrings.TRAY_PREVIEW_OVERLAY)
        assertEquals("MotionLayer 종료", PcStrings.TRAY_QUIT)
    }

    @Test
    fun connectedNotificationMatchesFigma() {
        val notification = connectedNotification(PcSampleData.phone)
        assertEquals("허윤의 Galaxy와 연결됐어요", notification.title)
        assertEquals("같은 와이파이 · 12ms", notification.message)
    }

    @Test
    fun notificationParticleFollowsThePhoneName() {
        assertEquals("민지의 폰과 연결됐어요", PcStrings.connectedTitle("민지의 폰"))
        assertEquals("내 갤럭시와 연결됐어요", PcStrings.connectedTitle("내 갤럭시"))
    }

    @Test
    fun overlayPreviewShowsThePointer() {
        val scope = CoroutineScope(Dispatchers.Default)
        try {
            val overlay = FakePresentationOverlay(scope)
            assertTrue(overlay.state.value.isEmpty)
            overlay.preview()
            val shown = runBlocking { withTimeout(3_000) { overlay.state.first { !it.isEmpty } } }
            assertEquals(PcSampleData.pointer, shown.pointer)
        } finally {
            scope.cancel()
        }
    }

    /** 앱 표시: 모서리는 비어 있고(둥근 네모), 바탕은 파랑, 가운데에 흰 아이콘이 있다. */
    @Test
    fun appMarkIsBlueRoundedSquareWithWhiteGlyph() {
        val mark = renderAppMark(pixels = 64)
        File("build/renders").apply { mkdirs() }.resolve("app_mark.png")
            .writeBytes(checkNotNull(Image.makeFromBitmap(mark.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)).bytes)

        assertEquals(64, mark.width)
        assertEquals(64, mark.height)
        val pixels = mark.toPixelMap()
        assertEquals("모서리는 투명해야 한다", 0f, pixels[0, 0].alpha, 0.01f)
        assertEquals("위쪽 가운데는 파란 바탕이어야 한다", LightMotionColors.brandPrimary, pixels[32, 3])
        var white = 0
        for (y in 0 until 64) for (x in 0 until 64) {
            val c = pixels[x, y]
            if (c.alpha > 0.9f && c.red > 0.9f && c.green > 0.9f && c.blue > 0.9f) white++
        }
        assertTrue("흰 아이콘이 보여야 한다(흰 칸 $white)", white > 40)
    }
}
