package com.uhi.motionlayer.domain.model

/**
 * 지금 제스처로 조작하는 미디어 앱. 알림 접근(미디어 세션)으로 알아낸다(B).
 *
 * @param iconLetter 앱 아이콘 이미지를 쓰기 전까지 타일에 보여 줄 한 글자
 * @param accentColorArgb 타일 색(ARGB). 없으면 화면 기본 색을 쓴다
 * @param bindings 이 앱에 적용된 제스처-명령 목록(앱별 설정, FR-05·06)
 * @param appId 앱 목록(AP-01)의 앱과 같은 값. 앱별 동작 설정을 찾을 때 쓴다
 */
data class PlayingApp(
    val name: String,
    val iconLetter: String,
    val accentColorArgb: Long? = null,
    val bindings: List<GestureBinding> = emptyList(),
    val appId: String? = null,
)

/** 제스처 명령을 보내는 곳(HM-06): 이 폰의 미디어 앱, 또는 연결된 PC. */
enum class ControlTarget { Phone, Pc }
