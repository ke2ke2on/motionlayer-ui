package com.uhi.motionlayer.domain.model

/** 발표 중 PC와의 연결 상태. 끊기면 3초 동안 다시 연결을 시도한다(분석서 UC-01 5c). */
sealed interface PresentationLink {
    data class Connected(val deviceName: String, val latencyMs: Int) : PresentationLink

    /** 끊겼다. 이 동안의 제스처는 버려진다(FR-03, 표 4-2). */
    data object Reconnecting : PresentationLink
}

/**
 * 발표를 시작하기 전에 정하는 것(PR-06 발표 준비, PR-15 발표 시간 시트).
 *
 * @param durationMinutes 발표 시간(분). 분석서 NFR-09가 10분을 기준으로 삼아 기본은 10분이다
 * @param vibrateBeforeEnd 끝나기 5분 전에 폰이 진동할지. PC 화면에는 아무것도 띄우지 않는다(UC-01 5d)
 */
data class PresentationSetup(
    val durationMinutes: Int = 10,
    val vibrateBeforeEnd: Boolean = true,
)

/**
 * 발표 중 쓰는 도구(Figma 아래 도구 줄). 한 번에 하나만 켤 수 있고, 켜진 도구를 한 번 더 고르면 꺼진다([None]).
 * "효과"(PR-14)는 켜 두는 도구가 아니라 효과 목록 화면을 여는 버튼이라 여기에 없다.
 */
enum class PresentationTool {
    /** 도구 없음. 휙으로 슬라이드를 넘기고 돌려서 포인터를 움직이는 기본 상태 */
    None,

    /** 그리기(PR-09). 손가락을 댄 채 폰을 움직이는 대로 PC 화면에 선이 그려지고, 손을 뗀 지 3초 뒤 저절로 지워진다(PC 쪽이 한다) */
    Draw,

    /** 화면 가리기(PR-10). PC 화면이 검게 바뀐다. 한 번 더 누르거나 "누른 채 두 번 톡톡"하면 돌아온다 */
    Blackout,
}

/** 발표에서 띄우는 효과 파일의 종류(PR-14). 사진·영상·소리. */
enum class EffectKind { Image, Video, Sound }

/**
 * 발표 전에 PC로 미리 보내 두는 효과 파일 하나(PR-14).
 *
 * @param sizeBytes 파일 크기(바이트). 화면이 "1.2MB"처럼 줄여서 보여 준다
 * @param uploadPercent 보내는 중이면 0~100, 다 보냈으면 null
 */
data class PresentationEffect(
    val id: String,
    val name: String,
    val kind: EffectKind,
    val sizeBytes: Long,
    val uploadPercent: Int? = null,
)

/**
 * 발표 중 화면(PR-07~12)이 그릴 값 전부. 값을 만드는 것은 제스처(A)와 PC 연결(C) 쪽이다.
 *
 * @param slideTotal 전체 슬라이드 수. PC 쪽 발표 프로그램에서 알 수 있을 때만 있다(모르면 null).
 *                   **미정: 슬라이드 번호가 PC에서 오는 값인지 폰이 세는 값인지** 분석서에 없어 팀에서 정해야 한다.
 * @param fiveMinutesLeft 남은 시간이 5분 이하가 된 상태(PR-11). 폰에만 알린다(UC-01 5d).
 * @param vibratedAtFiveMinutes 5분 남았을 때 진동으로 알렸는지(발표 준비에서 진동을 껐으면 false)
 * @param tool 지금 켜진 도구(PR-09). 도구를 켜고 끄는 것도 컨트롤러가 값으로 알린다
 */
data class PresentationState(
    val remainingSeconds: Int,
    val slideIndex: Int,
    val slideTotal: Int?,
    val link: PresentationLink,
    val fiveMinutesLeft: Boolean,
    val vibratedAtFiveMinutes: Boolean = true,
    val tool: PresentationTool = PresentationTool.None,
)

/**
 * 발표가 끝난 뒤 보여 주는 기록(PR-13). 분석서 UC-01 사후 조건: "발표 기록(시간·넘김·되돌림·오작동)이 폰에 남음".
 *
 * @param misfireGoalMet 오작동이 목표(10분 연속 0회, NFR-09)를 지켰는지. 판단은 기록을 가진 쪽이 한다.
 */
data class PresentationSummary(
    val durationSeconds: Int,
    val slideChanges: Int,
    val undoneCount: Int,
    val misfireCount: Int,
    val misfireGoalMet: Boolean,
)
