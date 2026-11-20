package com.uhi.motionlayer.data.fake

import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.domain.model.PresentationLink
import com.uhi.motionlayer.domain.model.PresentationSetup
import com.uhi.motionlayer.domain.model.PresentationState
import com.uhi.motionlayer.domain.model.PresentationSummary
import com.uhi.motionlayer.domain.model.PresentationTool
import com.uhi.motionlayer.domain.source.PresentationController
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

/**
 * 가짜 발표 진행. 시간만 실제로 1초씩 흐르고, 나머지(슬라이드 넘김·5분 남음·연결 끊김)는 [debugActions]로 일으킨다.
 * 터치는 기록하지 않는다(진짜 제스처 인식이 없으므로).
 */
class FakePresentationController(
    private val link: () -> PresentationLink.Connected,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : PresentationController {

    private val _state = MutableStateFlow<PresentationState?>(null)
    override val state: StateFlow<PresentationState?> = _state.asStateFlow()

    private var totalSeconds = 0
    private var slideChanges = 0
    private var undone = 0
    private var ticker: Job? = null
    private var reconnect: Job? = null

    override fun start(setup: PresentationSetup) {
        ticker?.cancel()
        reconnect?.cancel()
        val durationSeconds = setup.durationMinutes * 60
        totalSeconds = durationSeconds
        slideChanges = 0
        undone = 0
        _state.value = PresentationState(
            remainingSeconds = durationSeconds,
            slideIndex = 1,
            slideTotal = SLIDE_TOTAL,
            link = link(),
            fiveMinutesLeft = durationSeconds <= FIVE_MINUTES,
            vibratedAtFiveMinutes = setup.vibrateBeforeEnd,
        )
        ticker = scope.launch {
            while (true) {
                delay(1_000)
                _state.update { current ->
                    current?.let {
                        val remaining = (it.remainingSeconds - 1).coerceAtLeast(0)
                        it.copy(remainingSeconds = remaining, fiveMinutesLeft = remaining <= FIVE_MINUTES)
                    }
                }
            }
        }
    }

    override fun setTouching(touching: Boolean) = Unit

    override fun selectTool(tool: PresentationTool) {
        _state.update { current ->
            current?.copy(tool = if (current.tool == tool) PresentationTool.None else tool)
        }
    }

    override fun end(): PresentationSummary? {
        val current = _state.value ?: return null
        ticker?.cancel()
        reconnect?.cancel()
        _state.value = null
        return PresentationSummary(
            durationSeconds = totalSeconds - current.remainingSeconds,
            slideChanges = slideChanges,
            undoneCount = undone,
            misfireCount = 0,
            misfireGoalMet = true,
        )
    }

    /** 시연용 동작 모음. [AppContainer]에서 디버그 빌드일 때만 화면에 넘긴다. */
    val debugActions: List<DebugAction> = listOf(
        DebugAction("다음 슬라이드 (휙)") { moveSlide(+1) },
        DebugAction("이전 슬라이드") { moveSlide(-1) },
        DebugAction("되돌림 1회 추가") { undone += 1 },
        DebugAction("누른 채 두 번 톡톡 (화면 가리기 켜고 끄기)") { selectTool(PresentationTool.Blackout) },
        DebugAction("남은 시간 5분으로") {
            _state.update { it?.copy(remainingSeconds = FIVE_MINUTES, fiveMinutesLeft = true) }
        },
        DebugAction("연결 끊김 (3초 뒤 복구)") { disconnectForAWhile() },
    )

    private fun moveSlide(delta: Int) {
        _state.update { current ->
            current?.let {
                val moved = (it.slideIndex + delta).coerceIn(1, it.slideTotal ?: Int.MAX_VALUE)
                if (moved != it.slideIndex) slideChanges += 1
                it.copy(slideIndex = moved)
            }
        }
    }

    private fun disconnectForAWhile() {
        if (_state.value == null) return
        reconnect?.cancel()
        _state.update { it?.copy(link = PresentationLink.Reconnecting) }
        reconnect = scope.launch {
            delay(3_000)
            _state.update { it?.copy(link = link()) }
        }
    }

    private companion object {
        const val FIVE_MINUTES = 300
        const val SLIDE_TOTAL = 18
    }
}
