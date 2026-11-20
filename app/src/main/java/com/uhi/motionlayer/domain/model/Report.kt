package com.uhi.motionlayer.domain.model

/** 기록과 리포트(ST-03)의 기간. 위쪽 칩 "오늘 · 7일 · 30일". */
enum class ReportRange { Today, Week, Month }

/**
 * 한 기간의 사용 기록. 이벤트 기록(B)과 제스처 인식 결과(A)로 센다.
 *
 * @param undoneCount 흔들어 되돌린 횟수. 분석서 NFR-02는 이 횟수로 "의도하지 않은 실행"을 센다
 * @param averageMisfiresPerDay 하루 평균 오작동 횟수
 * @param misfireGoalMet 오작동이 목표(하루 1회 이하, NFR-02)를 지켰는지. 판단은 기록을 가진 쪽이 한다
 * @param gestureRates 기본 제스처별 성공률
 * @param customGestureRates 나만의 제스처별 성공률(만든 순서). 기본 제스처 줄 아래에 이어서 보여 준다
 * @param sensitivitySuggestion 감도를 낮춰 보라고 권할 제스처. 권할 것이 없으면 null
 */
data class UsageReport(
    val range: ReportRange,
    val recognizedCount: Int,
    val undoneCount: Int,
    val averageMisfiresPerDay: Float,
    val misfireGoalMet: Boolean,
    val gestureRates: List<GestureSummary>,
    val customGestureRates: List<CustomGestureRate> = emptyList(),
    val sensitivitySuggestion: SensitivitySuggestion? = null,
)

/**
 * 나만의 제스처 하나의 성공률(ST-03 막대 한 줄).
 *
 * @param needsAttention true면 성공률이 목표에 못 미쳐 주황으로 보여 준다. 판단은 기록을 가진 쪽(A)이 한다
 */
data class CustomGestureRate(
    val gestureId: String,
    val name: String,
    val successRatePercent: Int,
    val needsAttention: Boolean,
)

/**
 * "'원 그리기'를 자주 되돌렸어요 · 감도를 낮춰 볼까요?"(ST-03 카드)와 그 시트(ST-04)의 대상.
 * 최근 7일 동안 [attemptCount]번 중 [undoneCount]번을 되돌렸다. 언제 권할지는 기록을 가진 쪽(A)이 정한다.
 *
 * @param sensitivity 지금 감도. 0.0(둔감하게)~1.0(민감하게). 시트의 슬라이더가 이 값에서 시작한다
 */
data class SensitivitySuggestion(
    val gestureId: String,
    val gestureName: String,
    val attemptCount: Int,
    val undoneCount: Int,
    val sensitivity: Float,
)
