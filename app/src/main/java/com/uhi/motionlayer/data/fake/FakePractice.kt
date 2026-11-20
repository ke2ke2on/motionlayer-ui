package com.uhi.motionlayer.data.fake

import com.uhi.motionlayer.domain.model.BasicGesture
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.domain.model.GestureDetail
import com.uhi.motionlayer.domain.model.GestureSummary
import com.uhi.motionlayer.domain.model.PRACTICE_SUCCESSES_REQUIRED
import com.uhi.motionlayer.domain.model.PracticeState
import com.uhi.motionlayer.domain.model.TrajectoryPoint
import com.uhi.motionlayer.domain.source.GestureCatalog
import com.uhi.motionlayer.domain.source.PracticeSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Figma GS-01에 적힌 성공률(97·95·99·92%)을 그대로 쓴 가짜 목록. [debugActions]로 목록을 불러오는 중(GS-16)을 볼 수 있다.
 */
class FakeGestureCatalog(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : GestureCatalog {
    private val rates = mapOf(
        BasicGesture.Swipe to 97,
        BasicGesture.DoubleTap to 95,
        BasicGesture.Tilt to 99,
        BasicGesture.Shake to 92,
    )

    override val basicGestures: StateFlow<List<GestureSummary>> = MutableStateFlow(
        BasicGesture.entries.map { GestureSummary(it, rates.getValue(it)) },
    ).asStateFlow()

    private val _loading = MutableStateFlow(false)
    override val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /** 시연용: 제스처 목록을 불러오는 중(GS-16)을 2초 동안 보여 준다. */
    val debugActions: List<DebugAction> = listOf(
        DebugAction("제스처 목록 · 불러오는 중 2초 (GS-16)") {
            scope.launch {
                _loading.value = true
                delay(LOADING_MILLIS)
                _loading.value = false
            }
        },
    )

    /** 휙은 Figma GS-02의 값(97%, 되돌림 2회, 감도 61%), 흔들기는 GS-17의 값(92%, 되돌림 4회)이고, 나머지는 그럴듯하게 만든 가짜다. */
    private val details = mapOf(
        BasicGesture.Swipe to MutableStateFlow(GestureDetail(BasicGesture.Swipe, 97, 2, 0.61f)),
        BasicGesture.DoubleTap to MutableStateFlow(GestureDetail(BasicGesture.DoubleTap, 95, 1, 0.5f)),
        BasicGesture.Tilt to MutableStateFlow(GestureDetail(BasicGesture.Tilt, 99, 0, 0.5f)),
        BasicGesture.Shake to MutableStateFlow(GestureDetail(BasicGesture.Shake, 92, 4, 0.5f)),
    )

    override fun detail(gesture: BasicGesture): StateFlow<GestureDetail> = details.getValue(gesture).asStateFlow()

    override fun setSensitivity(gesture: BasicGesture, sensitivity: Float) {
        details.getValue(gesture).update { it.copy(sensitivity = sensitivity.coerceIn(0f, 1f)) }
    }

    private companion object {
        const val LOADING_MILLIS = 2_000L
    }
}

/**
 * 가짜 연습 모드. 센서 없이 **목표 곡선을 따라 파란 선이 계속 그려지는 모습**을 흉내 낸다(약 30fps).
 *
 * 성공 횟수는 저절로 늘지 않는다. 센서가 없으므로 시연용 동작([debugActions])으로 올린다.
 * 처음 값은 Figma GS-09와 같다: 휙 2, 톡톡 3, 기울임 3, 흔들기 0.
 */
class FakePracticeSession(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : PracticeSession {

    private val counts = MutableStateFlow(
        mapOf(
            BasicGesture.Swipe to 2,
            BasicGesture.DoubleTap to 3,
            BasicGesture.Tilt to 3,
            BasicGesture.Shake to 0,
        ),
    )

    private val _state = MutableStateFlow(
        PracticeState(
            selected = BasicGesture.Swipe,
            attemptNumber = 3,
            target = targetFor(BasicGesture.Swipe),
            live = emptyList(),
            matchPercent = null,
            successCounts = counts.value,
        ),
    )
    override val state: StateFlow<PracticeState> = _state.asStateFlow()

    private var drawing: Job? = null

    override fun enter() {
        drawing?.cancel()
        drawing = scope.launch { drawForever() }
    }

    override fun leave() {
        drawing?.cancel()
    }

    override fun select(gesture: BasicGesture) {
        _state.update {
            it.copy(
                selected = gesture,
                attemptNumber = (counts.value[gesture] ?: 0) + 1,
                target = targetFor(gesture),
                live = emptyList(),
                matchPercent = null,
            )
        }
        // 그리던 중이면 새 제스처로 처음부터 다시 그린다.
        if (drawing?.isActive == true) enter()
    }

    private suspend fun drawForever() {
        while (true) {
            val current = _state.value
            val gesture = current.selected
            val target = current.target
            val noise = noiseAmplitude(current.attemptNumber)
            val actual = target.mapIndexed { i, p ->
                TrajectoryPoint(p.x, (p.y + noise * sin(i * 0.45f + current.attemptNumber)).coerceIn(0f, 1f))
            }
            for (end in 1..actual.size) {
                _state.update { it.copy(live = actual.subList(0, end), matchPercent = null) }
                delay(FRAME_MILLIS)
            }
            _state.update { it.copy(matchPercent = matchPercent(target, actual)) }
            delay(HOLD_MILLIS)
            _state.update {
                it.copy(
                    attemptNumber = it.attemptNumber + 1,
                    live = emptyList(),
                    matchPercent = null,
                    selected = gesture,
                )
            }
        }
    }

    /** 시연용 동작 모음. 센서 없이 성공 횟수를 올려 연습 완료까지 가 볼 수 있다. */
    val debugActions: List<DebugAction> = listOf(
        DebugAction("선택한 제스처 성공 +1") { addSuccess(_state.value.selected) },
        DebugAction("모든 제스처를 2회로 맞추기") { setAll(PRACTICE_SUCCESSES_REQUIRED - 1) },
        DebugAction("모든 제스처 0회로 되돌리기") { setAll(0) },
    )

    private fun addSuccess(gesture: BasicGesture) {
        counts.update { it + (gesture to ((it[gesture] ?: 0) + 1).coerceAtMost(PRACTICE_SUCCESSES_REQUIRED)) }
        _state.update { it.copy(successCounts = counts.value) }
    }

    private fun setAll(value: Int) {
        counts.value = BasicGesture.entries.associateWith { value }
        _state.update { it.copy(successCounts = counts.value) }
    }

    private companion object {
        const val FRAME_MILLIS = 33L // 약 30fps
        const val HOLD_MILLIS = 900L
        const val POINT_COUNT = 48

        /** 시도마다 곡선이 조금씩 다르게 흔들리도록 정한 크기(0.01~0.07). */
        fun noiseAmplitude(attempt: Int): Float = 0.01f + (attempt % 4) * 0.02f

        fun matchPercent(target: List<TrajectoryPoint>, actual: List<TrajectoryPoint>): Int {
            val diff = target.indices.map { abs(target[it].y - actual[it].y) }.average()
            return (100 - diff * 160).roundToInt().coerceIn(50, 99)
        }

        /** 제스처마다 목표 곡선의 모양을 정한다. 실제 모양은 제스처 코어(A)가 준다. */
        fun targetFor(gesture: BasicGesture): List<TrajectoryPoint> = List(POINT_COUNT) { i ->
            val t = i / (POINT_COUNT - 1f)
            val y = when (gesture) {
                // 휙: 낮게 시작해 부드럽게 올라간다
                BasicGesture.Swipe -> 0.8f - 0.55f * smoothStep(0.1f, 0.9f, t)
                // 톡톡: 짧은 두 번의 뾰족한 움직임
                BasicGesture.DoubleTap -> 0.8f - 0.6f * bump(t, 0.3f) - 0.6f * bump(t, 0.65f)
                // 기울임: 기울어졌다가 그 자리에서 유지
                BasicGesture.Tilt -> 0.8f - 0.6f * smoothStep(0.2f, 0.5f, t)
                // 흔들기: 빠르게 좌우로 반복
                BasicGesture.Shake -> 0.5f + 0.35f * sin(t * 4f * PI.toFloat()) * (1f - 0.4f * t)
            }
            TrajectoryPoint(x = t, y = y.coerceIn(0f, 1f))
        }

        fun smoothStep(from: Float, to: Float, x: Float): Float {
            val t = ((x - from) / (to - from)).coerceIn(0f, 1f)
            return t * t * (3f - 2f * t)
        }

        fun bump(x: Float, center: Float): Float = exp(-(((x - center) / 0.05f).pow(2)))
    }
}
