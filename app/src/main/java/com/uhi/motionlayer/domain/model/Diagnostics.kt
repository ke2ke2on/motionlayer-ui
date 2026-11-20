package com.uhi.motionlayer.domain.model

/**
 * 센서 원시값 한 번(ST-08). 가속도 ax·ay·az는 m/s², 자이로 gx·gy·gz는 rad/s로 센서가 준 그대로다.
 */
data class SensorSample(
    val ax: Float,
    val ay: Float,
    val az: Float,
    val gx: Float,
    val gy: Float,
    val gz: Float,
)

/**
 * 제스처를 알아보는 기준(ST-08 "판정 기준"). 값은 제스처 인식(A)이 정하고 화면은 그대로 보여 준다.
 *
 * @param swipePeakMps2 휙으로 치는 가속도 최고점(m/s²)
 * @param doubleTapMinMs 톡톡 사이 간격의 아래 한계(ms)
 * @param doubleTapMaxMs 톡톡 사이 간격의 위 한계(ms)
 * @param tiltDegrees 기울임으로 치는 각도(도)
 * @param tiltHoldSeconds 그 각도를 유지해야 하는 시간(초)
 * @param shakeCount 흔들기로 치는 흔듦 횟수
 * @param shakeWithinSeconds 그 횟수를 채워야 하는 시간(초)
 */
data class GestureThresholds(
    val swipePeakMps2: Int,
    val doubleTapMinMs: Int,
    val doubleTapMaxMs: Int,
    val tiltDegrees: Int,
    val tiltHoldSeconds: Float,
    val shakeCount: Int,
    val shakeWithinSeconds: Float,
)

/**
 * 개발자 도구(ST-08)가 보여 줄 값 한 묶음. 값이 아직 없으면 null이고, 화면은 "—"로 보여 준다.
 *
 * @param lastCommandMs 마지막 명령이 인식된 뒤 실행되기까지 걸린 시간(ms)
 * @param averageCommandMs 최근 50번 명령의 평균 시간(ms)
 * @param batteryPercentPerHour 감지를 켠 뒤 1시간 동안 쓴 배터리(%)
 */
data class DiagnosticsSnapshot(
    val sensor: SensorSample,
    val lastCommandMs: Int?,
    val averageCommandMs: Int?,
    val batteryPercentPerHour: Float?,
    val thresholds: GestureThresholds,
)
