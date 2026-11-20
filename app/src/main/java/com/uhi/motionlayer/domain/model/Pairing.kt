package com.uhi.motionlayer.domain.model

import java.time.LocalDate

/**
 * PC에 연결하려고 가진 정보. 연결 방법은 세 가지다(분석서 UC-01).
 *  - QR을 찍어 읽은 토큰, QR을 못 찍을 때 입력하는 같은 토큰의 6자리 코드, 최근에 연결했던 PC 다시 연결.
 */
sealed interface PairingTarget {
    data class QrToken(val token: String) : PairingTarget

    /** 숫자 6자리. 화면이 자리 수만 확인하고, 맞는 코드인지는 연결 쪽(C)이 판단한다. */
    data class SixDigitCode(val code: String) : PairingTarget

    data class Recent(val id: String) : PairingTarget
}

/** 연결하지 못한 이유. PR-05 화면이 안내하는 원인(만료, 다른 와이파이, 기기 간 연결 차단)에 대응한다. */
enum class PairingFailure { TokenExpired, DifferentNetwork, Unknown }

sealed interface PairingResult {
    data class Connected(val connection: PcConnectionState.Connected) : PairingResult

    data class Failed(val reason: PairingFailure) : PairingResult
}

/**
 * 최근에 연결한 PC 한 줄(PR-01). 목록은 PC 연결 쪽(C)이 폰에 쌓아 둔 기록에서 준다.
 *
 * @param description 이름 아래 설명의 앞부분. "공학관 304", "내 기기"처럼 어디의 PC인지
 */
data class RecentPc(
    val id: String,
    val name: String,
    val description: String,
    val lastConnected: LocalDate,
)
