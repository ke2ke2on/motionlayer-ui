package com.uhi.motionlayer.domain.model

/**
 * 기본 제스처 4가지. 목록(GS-01)과 연습 모드(GS-09·10)가 같이 쓴다.
 * "좌우로 휙"은 오른쪽·왼쪽을 한 가지로 센다(분석서 FR-08: 기본 제스처 4종).
 */
enum class BasicGesture { Swipe, DoubleTap, Tilt, Shake }

/** 분석서 FR-08-1: 기본 제스처 4종을 3번씩 성공하면 연습 완료. */
const val PRACTICE_SUCCESSES_REQUIRED = 3

/**
 * 제스처 목록 한 줄(GS-01).
 *
 * @param successRatePercent 최근 인식 성공률(%). 이벤트 기록(B)과 인식 결과(A)로 센다.
 */
data class GestureSummary(
    val gesture: BasicGesture,
    val successRatePercent: Int,
)

/**
 * 기본 제스처 하나의 자세한 값(GS-02). 인식 결과(A)와 이벤트 기록(B)으로 센다.
 *
 * @param successRatePercent 최근 7일 인식률(%)
 * @param undoneCount 최근 7일 동안 이 제스처로 한 동작을 되돌린 횟수
 * @param sensitivity 감도. 0.0(둔감하게)~1.0(민감하게). 폰에 저장하고 인식에 반영하는 것은 A가 한다
 */
data class GestureDetail(
    val gesture: BasicGesture,
    val successRatePercent: Int,
    val undoneCount: Int,
    val sensitivity: Float,
)

/** 센서 움직임을 그린 곡선 위의 한 점. 둘 다 0.0~1.0이고, x는 왼쪽→오른쪽, y는 위(0)→아래(1)다. */
data class TrajectoryPoint(val x: Float, val y: Float)

/**
 * 연습 모드(GS-09)가 그릴 값 전부. 값을 만드는 것은 제스처 인식(A)이다.
 *
 * 분석서 4.3: 궤적은 **30fps**로 갱신하고 느린 프레임은 5% 이하여야 한다. 그래서 [live]는 자주 바뀐다.
 *
 * @param attemptNumber 이번 제스처의 몇 번째 시도인지(1부터)
 * @param target 따라 해야 할 목표 곡선(점선)
 * @param live 지금까지 움직인 곡선(파란 선). 아직 움직이지 않았으면 비어 있다
 * @param matchPercent 목표와 얼마나 같은지(%). 시도를 마치기 전에는 null
 * @param successCounts 제스처별로 성공한 횟수(0~[PRACTICE_SUCCESSES_REQUIRED])
 */
data class PracticeState(
    val selected: BasicGesture,
    val attemptNumber: Int,
    val target: List<TrajectoryPoint>,
    val live: List<TrajectoryPoint>,
    val matchPercent: Int?,
    val successCounts: Map<BasicGesture, Int>,
) {
    /** 4가지를 모두 [PRACTICE_SUCCESSES_REQUIRED]번씩 성공했다. */
    val completed: Boolean
        get() = BasicGesture.entries.all { (successCounts[it] ?: 0) >= PRACTICE_SUCCESSES_REQUIRED }

    /** 3번 이상 성공해 "익힌" 제스처 수. */
    val learnedCount: Int
        get() = BasicGesture.entries.count { (successCounts[it] ?: 0) >= PRACTICE_SUCCESSES_REQUIRED }
}
