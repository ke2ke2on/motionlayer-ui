package com.uhi.motionlayer.data.fake

import com.uhi.motionlayer.domain.model.CUSTOM_RECORDING_TAKES
import com.uhi.motionlayer.domain.model.CUSTOM_VERIFICATION_REQUIRED
import com.uhi.motionlayer.domain.model.CUSTOM_VERIFICATION_TRIES
import com.uhi.motionlayer.domain.model.CustomGesture
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.domain.model.RecordingState
import com.uhi.motionlayer.domain.model.RecordingStep
import com.uhi.motionlayer.domain.model.SimilarGesture
import com.uhi.motionlayer.domain.model.TrajectoryPoint
import com.uhi.motionlayer.domain.model.VerificationResult
import com.uhi.motionlayer.domain.source.CustomGestureLibrary
import com.uhi.motionlayer.domain.source.GestureRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.sin

/**
 * 가짜 나만의 제스처 목록과 만들기. 센서가 없으므로 녹화·확인은 시연용 동작([debugActions])으로 진행한다.
 * 목록에는 처음부터 Figma의 "원 그리기" 하나가 들어 있다(10월 9일에 만듦, 감도 보통).
 *
 * 녹화한 동작마다 곡선은 같은 물결에 위상만 조금씩 다르게 만든 가짜다. 세 번째 녹화부터 이전 동작과 비교한 일치도(86%)가 나온다.
 * 5번째 녹화를 마치면 1.5초 동안 "분석 중"(GS-14)을 보여 준 뒤 확인(GS-07)으로 넘어간다.
 * 확인에서 3번 중 2번 이상 인식되면 저장하고, 모자라면 확인을 처음부터 다시 한다.
 */
class FakeCustomGestures(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CustomGestureLibrary, GestureRecorder {
    private val _gestures = MutableStateFlow(listOf(SampleData.customCircle))
    override val gestures: StateFlow<List<CustomGesture>> = _gestures.asStateFlow()

    private val _state = MutableStateFlow<RecordingState?>(null)
    override val state: StateFlow<RecordingState?> = _state.asStateFlow()

    private var replacingId: String? = null
    private var created = 0

    override fun setSensitivity(id: String, sensitivity: Float) {
        _gestures.update { list -> list.map { if (it.id == id) it.copy(sensitivity = sensitivity.coerceIn(0f, 1f)) else it } }
    }

    override fun delete(id: String) {
        _gestures.update { list -> list.filterNot { it.id == id } }
    }

    override fun start(name: String, replacingId: String?) {
        this.replacingId = replacingId
        _state.value = RecordingState(name = name, step = RecordingStep.Recording)
    }

    override fun retryTake() {
        _state.update { it?.copy(step = RecordingStep.Recording, live = emptyList(), matchPercent = null) }
    }

    override fun restart() {
        _state.value?.let { start(it.name, replacingId) }
    }

    override fun cancel() {
        _state.value = null
    }

    /** 시연용: 나만의 제스처 "원 그리기"를 계정에 올리기 전(동기화 대기, GS-15)으로 만들고 되돌린다. 홈 제목을 길게 누르면 나온다. */
    val libraryDebugActions: List<DebugAction> = listOf(
        DebugAction("나만의 제스처 · 동기화 대기 켜고 끄기 (GS-15)") {
            _gestures.update { list ->
                list.map { if (it.id == SampleData.customCircle.id) it.copy(syncPending = !it.syncPending) else it }
            }
        },
    )

    /** 시연용 동작 모음. 녹화 카드를 길게 누르면(디버그 빌드) 나온다. */
    val debugActions: List<DebugAction> = listOf(
        DebugAction("동작 한 번 녹화 (GS-04)") { recordTake() },
        DebugAction("동작이 달라요 (GS-05)") { mismatch() },
        DebugAction("기존 제스처와 겹침 (GS-06)") { tooSimilar() },
        DebugAction("확인: 인식됨 (GS-07)") { verify(VerificationResult.Recognized) },
        DebugAction("확인: 인식 안 됨 (GS-07)") { verify(VerificationResult.Missed) },
    )

    private fun recordTake() {
        var analysisStarted = false
        _state.update { current ->
            if (current == null || current.step != RecordingStep.Recording) return@update current
            val takes = current.takes + 1
            val curve = wave(phase = takes * 0.35f)
            val previous = (current.previous + listOfNotNull(current.live.takeIf { it.isNotEmpty() })).takeLast(2)
            analysisStarted = takes >= CUSTOM_RECORDING_TAKES
            current.copy(
                step = if (analysisStarted) RecordingStep.Analyzing else RecordingStep.Recording,
                takes = takes,
                live = curve,
                previous = previous,
                matchPercent = if (takes >= 3) MATCH_PERCENT else null,
            )
        }
        if (analysisStarted) {
            // 5번째 녹화를 마치면 공통 패턴을 만드는 척 잠깐 기다린 뒤 확인으로 넘어간다(GS-14 → GS-07).
            scope.launch {
                delay(ANALYZING_MILLIS)
                _state.update { current ->
                    if (current?.step == RecordingStep.Analyzing) current.copy(step = RecordingStep.Verifying) else current
                }
            }
        }
    }

    private fun mismatch() {
        _state.update { current ->
            if (current == null || current.step != RecordingStep.Recording) return@update current
            current.copy(
                step = RecordingStep.Mismatch,
                live = wave(phase = 2.2f, amplitude = 0.38f),
                previous = (current.previous + listOfNotNull(current.live.takeIf { it.isNotEmpty() })).takeLast(2),
                matchPercent = MISMATCH_PERCENT,
            )
        }
    }

    private fun tooSimilar() {
        _state.update { current ->
            if (current == null || current.step != RecordingStep.Recording) return@update current
            current.copy(
                step = RecordingStep.TooSimilar,
                live = wave(phase = 0f),
                similarTo = SimilarGesture(name = "오른쪽 휙", curve = wave(phase = 0.3f), similarityPercent = SIMILARITY_PERCENT),
            )
        }
    }

    private fun verify(result: VerificationResult) {
        _state.update { current ->
            if (current == null || current.step != RecordingStep.Verifying) return@update current
            val results = current.verification + result
            if (results.size < CUSTOM_VERIFICATION_TRIES) return@update current.copy(verification = results)

            val recognized = results.count { it == VerificationResult.Recognized }
            if (recognized < CUSTOM_VERIFICATION_REQUIRED) {
                // 인식이 모자라면 확인을 처음부터 다시 한다.
                return@update current.copy(verification = emptyList())
            }
            val saved = save(current.name, recognized)
            current.copy(step = RecordingStep.Done, verification = results, saved = saved)
        }
    }

    /** 확인을 통과한 제스처를 목록에 넣는다. 다시 녹화였으면 그 제스처를 갱신한다. */
    private fun save(name: String, recognized: Int): CustomGesture {
        val existing = _gestures.value.firstOrNull { it.id == replacingId }
        val gesture = existing?.copy(successRatePercent = recognized * 100 / CUSTOM_VERIFICATION_TRIES)
            ?: CustomGesture(
                id = "custom-${created++}",
                name = name,
                createdOn = LocalDate.now(),
                successRatePercent = recognized * 100 / CUSTOM_VERIFICATION_TRIES,
                undoneCount = 0,
                attemptCount = 0,
                sensitivity = DEFAULT_SENSITIVITY,
            )
        _gestures.update { list -> if (existing != null) list.map { if (it.id == gesture.id) gesture else it } else list + gesture }
        return gesture
    }

    /** 가로로 한 번 반쯤 도는 물결 모양 곡선. [phase]만 달리 해서 매번 조금씩 다른 동작처럼 보이게 한다. */
    private fun wave(phase: Float, amplitude: Float = 0.3f, points: Int = 48): List<TrajectoryPoint> =
        List(points) { i ->
            val x = i / (points - 1f)
            TrajectoryPoint(x, (0.5f - amplitude * sin(x * 2f * PI.toFloat() * 1.6f + phase)).coerceIn(0f, 1f))
        }

    private companion object {
        const val ANALYZING_MILLIS = 1_500L
        const val MATCH_PERCENT = 86
        const val MISMATCH_PERCENT = 48
        const val SIMILARITY_PERCENT = 88
        const val DEFAULT_SENSITIVITY = 0.61f
    }
}
