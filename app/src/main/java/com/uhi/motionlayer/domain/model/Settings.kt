package com.uhi.motionlayer.domain.model

/**
 * 진동 3종(분석서 FR-02). 진동 설정 화면(ST-02)에서 "느껴 보기"로 미리 울려 볼 수 있다.
 * 각 진동이 어떤 박자인지는 시스템 연동(B)이 정한다. 화면은 이름만 안다.
 */
enum class VibrationFeedback {
    /** 제스처를 인식했을 때: 짧게 1번 */
    Recognized,

    /** 명령이 실행되었을 때: 짧게 2번 */
    Executed,

    /** 실행하지 못했을 때: 길고 거칠게 */
    Failed,
}
