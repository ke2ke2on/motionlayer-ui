package com.uhi.motionlayer.pc.ui

import com.uhi.motionlayer.pc.domain.ConnectedPhone
import com.uhi.motionlayer.pc.domain.LinkPath
import com.uhi.motionlayer.pc.domain.PlayingEffect
import com.uhi.motionlayer.pc.domain.PointerColor
import com.uhi.motionlayer.pc.domain.PointerSize
import com.uhi.motionlayer.ui.util.KoreanParticle
import com.uhi.motionlayer.ui.util.formatFileSize

/**
 * PC 화면의 글자. 한 곳에 모아 둔다(폰 앱의 strings.xml과 같은 역할).
 * 따옴표 안의 글은 Figma "20 PC 에이전트" 그대로이고, "직접 정함"이라고 적은 것만 시안에 없다.
 */
object PcStrings {
    const val APP_NAME = "MotionLayer"
    const val OVERLAY_WINDOW = "MotionLayer 발표 오버레이"

    // 왼쪽 메뉴
    const val TAB_PHONE_LINK = "폰 연결"
    const val TAB_PRESENTATION = "발표"
    const val DEMO_MENU_HINT = "시연용 동작"

    // PC-01 연결 대기
    const val QR_GUIDE = "폰에서 MotionLayer → 발표 → 'PC 연결하기'로 찍어 주세요"
    const val QR_DESCRIPTION = "연결용 QR 코드"
    const val NO_PHONE = "연결된 폰 없음"
    const val LAUNCH_AT_LOGIN = "로그인할 때 자동으로 켜기"
    const val AUTO_CONNECT = "내 폰이 같은 와이파이에 있으면 자동 연결"
    const val AUTO_CONNECT_NOTE = "로그인한 계정의 내 기기만"

    fun codeHint(secondsLeft: Int) = "QR을 못 찍으면 이 코드를 입력하세요 · ${clock(secondsLeft)} 뒤 바뀌어요"

    /** "482913" → "482 913" */
    fun pairingDigits(digits: String) = digits.chunked(3).joinToString(" ")

    // PC-02 연결됨
    const val CONNECTED = "연결됨"
    const val DISCONNECT = "끊기"
    const val PC_NAME = "이 PC 이름"
    const val PC_NAME_NOTE = "폰의 기기 목록에 이렇게 보여요"
    const val ALLOW_OTHER_PHONES = "다른 폰도 함께 연결 허용"
    const val SHOW_OPERATOR = "조작한 사람 이름 표시"
    const val SHOW_OPERATOR_NOTE = "화면 오른쪽 위에 잠깐 보여 줘요"

    /** "같은 와이파이 · 12ms · 이번만 연결". "서버 경유"와 "내 기기"는 시안에 없어 직접 정함. */
    fun phoneDetail(phone: ConnectedPhone): String {
        val kind = if (phone.oneTime) "이번만 연결" else "내 기기"
        return "${pathAndLatency(phone)} · $kind"
    }

    private fun pathAndLatency(phone: ConnectedPhone): String {
        val path = when (phone.path) {
            LinkPath.SameWifi -> "같은 와이파이"
            LinkPath.Relay -> "서버 경유"
        }
        return "$path · ${phone.latencyMs}ms"
    }

    // PC-04 트레이 메뉴 (메뉴 모양은 운영체제가 그리고, 항목 글자만 정한다)
    const val TRAY_SHOW_QR = "QR 보기"
    const val TRAY_OPEN_SETTINGS = "설정 열기…"
    const val TRAY_PREVIEW_OVERLAY = "발표 오버레이 미리 보기"
    const val TRAY_QUIT = "MotionLayer 종료"

    /** 메뉴 맨 위의 상태 줄. "MotionLayer · 연결 대기"가 시안이고 "연결됨"은 직접 정함. */
    fun trayStatus(connected: Boolean) = "$APP_NAME · " + if (connected) "연결됨" else "연결 대기"

    // PC-05 연결 알림 (알림 모양은 운영체제가 그린다)
    /** "허윤의 Galaxy와 연결됐어요" */
    fun connectedTitle(phoneName: String) = "$phoneName${KoreanParticle.with(phoneName)} 연결됐어요"

    /** "같은 와이파이 · 12ms" */
    fun connectedDetail(phone: ConnectedPhone) = pathAndLatency(phone)

    // PC-03 발표
    const val PRESENTATION_NOTE = "폰에서 누르고 있는 동안만 이 화면에 표시돼요."
    const val POINTER_SIZE = "포인터 크기"
    const val POINTER_COLOR = "포인터 색"
    const val STROKE_FADE = "그린 선 지우기"
    const val EFFECTS = "폰에서 미리 받은 효과"
    const val EFFECTS_NOTE = "발표 중에는 새 파일을 받지 않아요"

    /** "보통"만 시안에 있고 나머지는 직접 정함. */
    fun pointerSize(size: PointerSize) = when (size) {
        PointerSize.Small -> "작게"
        PointerSize.Medium -> "보통"
        PointerSize.Large -> "크게"
    }

    /** "파랑"만 시안에 있고 "빨강"은 직접 정함. */
    fun pointerColor(color: PointerColor) = when (color) {
        PointerColor.Blue -> "파랑"
        PointerColor.Red -> "빨강"
    }

    fun strokeFade(seconds: Int) = "${seconds}초 뒤"

    /** "3개 · 40MB" */
    fun effects(count: Int, bytes: Long) = "${count}개 · ${formatFileSize(bytes)}"

    // PC-06~09 오버레이
    fun operator(name: String) = "$name 조작 중"
    fun strokeHint(seconds: Int) = "손을 떼면 ${seconds}초 뒤 지워져요"
    const val BLACKOUT_NOTE = "화면을 가렸어요 · 폰에서 한 번 더 누르면 돌아와요"

    /** "0:12 / 0:40" */
    fun effectTime(effect: PlayingEffect) = "${clock(effect.positionSeconds)} / ${clock(effect.durationSeconds)}"

    /** 초를 "0:42"처럼 분:초로. */
    private fun clock(seconds: Int): String {
        val safe = seconds.coerceAtLeast(0)
        return "${safe / 60}:${(safe % 60).toString().padStart(2, '0')}"
    }
}
