package com.uhi.motionlayer.data.fake

import com.uhi.motionlayer.domain.model.DiagnosticsSnapshot
import com.uhi.motionlayer.domain.model.GestureThresholds
import com.uhi.motionlayer.domain.model.SensorSample
import com.uhi.motionlayer.domain.source.DiagnosticsSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.math.sin

/**
 * 가짜 개발자 도구 값. 센서 원시값은 폰을 세워 든 것처럼 중력(ay 약 9.7)에 작은 떨림을 얹어 0.2초마다 바꾼다.
 * 지연·배터리·판정 기준은 Figma 예시값에 고정이다. 화면을 보고 있는 동안에만 값을 만든다.
 */
class FakeDiagnosticsSource(
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val tickMillis: Long = 200,
) : DiagnosticsSource {
    override val snapshot: StateFlow<DiagnosticsSnapshot> = flow {
        var tick = 0
        while (true) {
            emit(sample(tick++))
            delay(tickMillis)
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), sample(0))

    private fun sample(tick: Int): DiagnosticsSnapshot {
        val t = tick * tickMillis / 1000.0
        fun wave(base: Double, amplitude: Double, speed: Double, phase: Double) =
            (base + amplitude * sin(t * speed + phase)).toFloat()

        return DiagnosticsSnapshot(
            sensor = SensorSample(
                ax = wave(0.42, 0.35, 0.9, 0.0),
                ay = wave(9.71, 0.12, 0.5, 0.0),
                az = wave(0.88, 0.30, 1.3, 1.0),
                gx = wave(0.01, 0.04, 1.7, 0.0),
                gy = wave(0.03, 0.04, 1.1, 2.0),
                gz = wave(0.00, 0.03, 0.7, 4.0),
            ),
            lastCommandMs = 12,
            averageCommandMs = 18,
            batteryPercentPerHour = 1.6f,
            thresholds = GestureThresholds(
                swipePeakMps2 = 14,
                doubleTapMinMs = 120,
                doubleTapMaxMs = 400,
                tiltDegrees = 25,
                tiltHoldSeconds = 0.4f,
                shakeCount = 3,
                shakeWithinSeconds = 0.6f,
            ),
        )
    }
}
