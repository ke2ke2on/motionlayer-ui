package com.uhi.motionlayer.pc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.uhi.motionlayer.pc.data.fake.PcSampleData
import com.uhi.motionlayer.pc.domain.ConnectedPhone
import com.uhi.motionlayer.pc.domain.OverlayState
import com.uhi.motionlayer.pc.ui.overlay.OverlayContent
import com.uhi.motionlayer.pc.ui.settings.SettingsContent
import com.uhi.motionlayer.pc.ui.settings.SettingsTab
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionTheme
import org.junit.Test

/**
 * 화면 일곱 개를 Figma 예시 값으로 그려 `build/renders/PC-0x.png`에 남긴다.
 * 시안(`docs/ui-report/figma_snapshots/PC-0x_*.png`)과 나란히 놓고 눈으로 비교하는 용도다.
 * 설정 창은 제목 줄을 뺀 내용(758 x 458), 오버레이는 시안과 같은 1280 x 720으로 그린다.
 */
class ScreenRenderTest {

    @Test
    fun pc01_phoneLinkWaiting() = settings("PC-01", SettingsTab.PhoneLink, phone = null)

    @Test
    fun pc02_phoneLinkConnected() = settings("PC-02", SettingsTab.PhoneLink, phone = PcSampleData.phone)

    @Test
    fun pc03_presentation() = settings("PC-03", SettingsTab.Presentation, phone = null)

    @Test
    fun pc06_pointer() = overlay("PC-06", OverlayState(operatorName = PcSampleData.OPERATOR, pointer = PcSampleData.pointer))

    @Test
    fun pc07_drawing() = overlay("PC-07", OverlayState(operatorName = PcSampleData.OPERATOR, strokes = listOf(PcSampleData.stroke)))

    @Test
    fun pc08_blackout() = overlay("PC-08", OverlayState(operatorName = PcSampleData.OPERATOR, blackout = true))

    @Test
    fun pc09_effect() = overlay("PC-09", OverlayState(operatorName = PcSampleData.OPERATOR, effect = PcSampleData.effect))

    private fun settings(name: String, tab: SettingsTab, phone: ConnectedPhone?) {
        renderPng(name, width = 758, height = 458) {
            MotionLayerTheme {
                SettingsContent(
                    tab = tab,
                    onTabSelected = {},
                    pairing = PcSampleData.pairing,
                    phone = phone,
                    connection = PcSampleData.connection,
                    presentation = PcSampleData.presentation,
                    onDisconnect = {},
                    onLaunchAtLoginChange = {},
                    onAutoConnectChange = {},
                    onAllowOtherPhonesChange = {},
                    onShowOperatorNameChange = {},
                )
            }
        }
    }

    private fun overlay(name: String, state: OverlayState) {
        renderPng(name, width = 1280, height = 720) {
            MotionLayerTheme {
                // 실제 오버레이는 투명하다. 시안과 비교하기 쉽게 여기서만 흰 바탕(슬라이드 자리)을 깐다.
                Box(Modifier.fillMaxSize().background(MotionTheme.colors.bgBase)) {
                    OverlayContent(
                        state = state,
                        showOperatorName = PcSampleData.connection.showOperatorName,
                        pointerSize = PcSampleData.presentation.pointerSize,
                        pointerColor = PcSampleData.presentation.pointerColor,
                        strokeFadeSeconds = PcSampleData.presentation.strokeFadeSeconds,
                    )
                }
            }
        }
    }
}
