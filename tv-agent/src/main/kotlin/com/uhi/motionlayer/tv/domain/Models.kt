package com.uhi.motionlayer.tv.domain

/** 이 TV의 성격. 처음 켰을 때 한 번 고른다(Figma 노트 FR-D-04). 공용이면 주인으로 등록할 수 없다. */
enum class TvKind { Personal, Public }

/**
 * QR 무늬. 가로세로 [size]칸이고 [isDark]가 참인 칸을 검게 칠한다.
 * 화면은 무늬를 만들지 않고 받은 대로 그린다(무늬를 만드는 일은 연결 쪽 C의 몫).
 * PC 모듈(pc-agent)의 `QrPattern`과 같은 코드다. 공용 모듈이 생기면 한 곳으로 합친다.
 */
class QrPattern(val size: Int, private val dark: BooleanArray) {
    init {
        require(dark.size == size * size) { "칸 수가 맞지 않는다: ${dark.size} != $size x $size" }
    }

    fun isDark(column: Int, row: Int): Boolean = dark[row * size + column]
}

/** TV가 지금 띄우고 있는 연결 정보. QR과 6자리 코드는 같은 토큰이고 60초마다 바뀐다. */
class TvPairingCode(val qr: QrPattern, val digits: String)

enum class PersonRole { Owner, Guest }

/** 연결된 사람이 지금 어떤 상태인지. */
enum class PersonStatus { Operating, SameWifi, Remote }

/** 이 TV에 연결된 사람(TV-03). 보기 전용이고, 끊는 일은 주인의 폰(공용 TV는 각자의 폰)에서 한다. */
data class ConnectedPerson(val name: String, val role: PersonRole, val status: PersonStatus)

/** TV에 실행된 명령. 이름표(TV-04)에 "민수 ▸ 다음 채널"처럼 보인다. */
enum class TvCommand { NextChannel, PreviousChannel, VolumeUp, VolumeDown, InputSwitch }

/** 방금 누가 무엇을 했는지(TV-04). 실행된 뒤 2초쯤 보였다 사라진다 — 언제 지울지는 구현이 정한다. */
data class OperatorNotice(val name: String, val command: TvCommand)

enum class EffectKind { Photo, Video }

/** 화면 위에 띄운 효과(TV-06). [secondsLeft]는 닫히기까지 남은 초. */
data class TvEffect(val senderName: String, val kind: EffectKind, val secondsLeft: Int)

/**
 * 재생 중인 화면 위에 겹쳐 그릴 것. 셋은 따로따로 켜지고 꺼진다.
 * - [operator]: 방금 조작한 사람과 명령(TV-04)
 * - [remoteOperator]: 집 밖에서(서버 경유로) 조작 중인 사람의 이름. 조작하는 동안 계속 보인다(TV-05)
 * - [effect]: 폰에서 보낸 사진·영상(TV-06)
 */
data class TvOverlayState(
    val operator: OperatorNotice? = null,
    val remoteOperator: String? = null,
    val effect: TvEffect? = null,
)

/**
 * 화면 시연·확인용 버튼 하나. 폰 없이 TV 화면의 상태를 바꿔 보게 해 준다(폰 앱의 `DebugAction`과 같은 역할).
 * 계약은 아니다. 실제 구현으로 바꾸면 빈 목록을 넘기면 되고, 그러면 시연용 메뉴가 열리지 않는다.
 */
class DemoAction(val label: String, val run: () -> Unit)
