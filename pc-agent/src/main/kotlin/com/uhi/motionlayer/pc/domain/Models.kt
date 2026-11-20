package com.uhi.motionlayer.pc.domain

/**
 * QR 무늬. 가로세로 [size]칸이고 [isDark]가 참인 칸을 검게 칠한다.
 * 화면은 무늬를 만들지 않고 받은 대로 그린다(무늬를 만드는 일은 연결 쪽 C의 몫).
 */
class QrPattern(val size: Int, private val dark: BooleanArray) {
    init {
        require(dark.size == size * size) { "칸 수가 맞지 않는다: ${dark.size} != $size x $size" }
    }

    fun isDark(column: Int, row: Int): Boolean = dark[row * size + column]
}

/** PC가 지금 띄우고 있는 연결 정보. QR과 6자리 코드는 같은 토큰이고 60초마다 바뀐다(분석서 FR-03-1). */
class PairingCode(val qr: QrPattern, val digits: String, val secondsLeft: Int)

/** 폰과 이어진 길. */
enum class LinkPath { SameWifi, Relay }

/** 지금 연결된 폰. [oneTime]이 참이면 계정에 등록하지 않은 "이번만 연결"이다. */
data class ConnectedPhone(val name: String, val path: LinkPath, val latencyMs: Int, val oneTime: Boolean)

/** "폰 연결" 탭의 설정 값. */
data class ConnectionPreferences(
    val launchAtLogin: Boolean,
    val autoConnectMyPhone: Boolean,
    val pcName: String,
    val allowOtherPhones: Boolean,
    val showOperatorName: Boolean,
)

enum class PointerSize { Small, Medium, Large }

enum class PointerColor { Blue, Red }

/** "발표" 탭의 설정 값. 효과는 폰에서 미리 받아 둔 파일의 개수와 크기다. */
data class PresentationPreferences(
    val pointerSize: PointerSize,
    val pointerColor: PointerColor,
    val strokeFadeSeconds: Int,
    val effectCount: Int,
    val effectBytes: Long,
)

/** 화면 위의 한 점. 화면 왼쪽 위가 (0, 0), 오른쪽 아래가 (1, 1)이다. */
data class OverlayPoint(val x: Float, val y: Float)

/** 폰으로 그린 선 하나. */
class OverlayStroke(val points: List<OverlayPoint>)

/** 슬라이드 위에 띄운 효과(사진·영상). */
data class PlayingEffect(val name: String, val positionSeconds: Int, val durationSeconds: Int)

/**
 * 발표 중 슬라이드 위에 그릴 것. 모두 비어 있으면 오버레이 창을 띄우지 않는다.
 * 포인터 위치·선·가리기 여부는 폰에서 온 값을 C가 넣어 준다. 화면은 그리기만 한다.
 */
data class OverlayState(
    val operatorName: String? = null,
    val pointer: OverlayPoint? = null,
    val strokes: List<OverlayStroke> = emptyList(),
    val blackout: Boolean = false,
    val effect: PlayingEffect? = null,
) {
    val isEmpty: Boolean get() = pointer == null && strokes.isEmpty() && !blackout && effect == null
}

/**
 * 화면 시연·확인용 버튼 하나. 폰 없이 PC 화면의 상태를 바꿔 보게 해 준다(폰 앱의 `DebugAction`과 같은 역할).
 * 계약은 아니다. 실제 구현으로 바꾸면 빈 목록을 넘기면 되고, 그러면 시연용 메뉴가 열리지 않는다.
 */
class DemoAction(val label: String, val run: () -> Unit)
