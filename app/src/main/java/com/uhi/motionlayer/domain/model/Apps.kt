package com.uhi.motionlayer.domain.model

/**
 * 앱 하나의 제스처별 동작(AP-02). 동작이 null이면 그 제스처를 이 앱에서 쓰지 않는다("사용 안 함").
 *
 * @param commands [Gestures]에 있는 제스처마다 하나씩
 */
data class AppGestureSettings(
    val appId: String,
    val commands: Map<GestureType, GestureCommand?>,
) {
    companion object {
        /**
         * AP-02가 보여 주는 제스처와 순서. Figma에 더 있는 "주머니 두 번 두드림"(FR-09)·"원 그리기"(FR-07)는
         * 이번 범위 밖이라 뺐고, 발표 전용 "누른 채 돌리기"는 앱별 설정이 아니다.
         */
        val Gestures = listOf(
            GestureType.SwipeRight,
            GestureType.SwipeLeft,
            GestureType.DoubleTap,
            GestureType.Tilt,
            GestureType.Shake,
        )
    }
}

/** 앱의 종류. 앱 목록(AP-01)에서 꺼 둔 앱의 설명 줄에 "음악"·"동영상"으로 보여 준다. */
enum class AppCategory { Music, Video, Other }

/**
 * 앱 목록(AP-01)의 앱 한 개. 설치된 미디어 앱을 시스템 연동(B)이 알려 준다.
 *
 * @param id 앱을 구분하는 값(안드로이드에서는 패키지 이름)
 * @param iconLetter 앱 아이콘 이미지를 쓰기 전까지 타일에 보여 줄 한 글자
 * @param accentColorArgb 타일 색(ARGB). 없으면 화면 기본 색을 쓴다
 * @param enabled 이 앱이 재생 중일 때 제스처로 조작하는지
 * @param customGestureCount 이 앱에 만든 나만의 제스처 수(FR-07). 아직 없으면 0
 */
data class ControllableApp(
    val id: String,
    val name: String,
    val category: AppCategory,
    val iconLetter: String,
    val accentColorArgb: Long? = null,
    val enabled: Boolean = false,
    val customGestureCount: Int = 0,
)
