package com.uhi.motionlayer.domain.model

/** 사용자가 하는 동작. 분석서 FR-01·03·10의 제스처. 실제 인식은 제스처 코어(A)가 한다. */
enum class GestureType {
    SwipeRight,
    SwipeLeft,
    DoubleTap,
    Tilt,
    Shake,
    PressAndRotate,
}

/**
 * 제스처가 실행하는 명령. 음악·영상 조작(FR-01)과 발표 조작(FR-03·10).
 * [NextTrack]·[PreviousTrack]은 음악 앱에서는 곡, 영상 앱에서는 영상을 넘긴다. [Undo]는 방금 한 동작을 되돌린다.
 */
enum class GestureCommand {
    NextTrack,
    PreviousTrack,
    PlayPause,
    Volume,
    SkipForward,
    SkipBack,
    Undo,
    NextSlide,
    PreviousSlide,
    Pointer,
}

/** "이 제스처는 이 명령" 한 쌍. 홈 화면에는 `→ 다음 곡`처럼 보인다. */
data class GestureBinding(
    val gesture: GestureType,
    val command: GestureCommand,
)
