package com.uhi.motionlayer.pc.domain

import kotlinx.coroutines.flow.StateFlow

// PC 화면이 쓰는 계약. 연결을 맡은 C가 구현하고, 지금은 `data/fake`가 대신한다.
// 화면은 이 인터페이스만 보고, 어느 구현을 쓸지는 `PcAppContainer` 한 곳에서 고른다(폰 앱과 같은 방식).

/** 폰과의 연결(PC-01·02). */
interface PhoneLink {
    /** 지금 띄울 QR·6자리 코드·남은 초. */
    val pairing: StateFlow<PairingCode>

    /** 연결된 폰. 없으면 null(연결 대기). */
    val phone: StateFlow<ConnectedPhone?>

    /** "끊기" 버튼. */
    fun disconnect()
}

/** 설정 창의 값(PC-01~03). 값을 저장하고 실제로 적용하는 일은 구현이 한다. */
interface AgentSettings {
    val connection: StateFlow<ConnectionPreferences>
    val presentation: StateFlow<PresentationPreferences>

    fun setLaunchAtLogin(on: Boolean)
    fun setAutoConnectMyPhone(on: Boolean)
    fun setAllowOtherPhones(on: Boolean)
    fun setShowOperatorName(on: Boolean)
}

/** 발표 중 슬라이드 위에 그릴 것(PC-06~09). */
interface PresentationOverlay {
    val state: StateFlow<OverlayState>

    /**
     * 트레이 메뉴의 "발표 오버레이 미리 보기"(PC-04). 발표 전에 오버레이가 이 PC 화면에서 잘 보이는지 잠깐 띄워 본다.
     * 무엇을 얼마 동안 보여 줄지는 구현이 정하고, 결과는 [state]로 나온다.
     */
    fun preview()
}
