package com.uhi.motionlayer.pc.data.fake

import com.uhi.motionlayer.pc.domain.AgentSettings
import com.uhi.motionlayer.pc.domain.ConnectedPhone
import com.uhi.motionlayer.pc.domain.ConnectionPreferences
import com.uhi.motionlayer.pc.domain.DemoAction
import com.uhi.motionlayer.pc.domain.LinkPath
import com.uhi.motionlayer.pc.domain.OverlayPoint
import com.uhi.motionlayer.pc.domain.OverlayState
import com.uhi.motionlayer.pc.domain.OverlayStroke
import com.uhi.motionlayer.pc.domain.PairingCode
import com.uhi.motionlayer.pc.domain.PhoneLink
import com.uhi.motionlayer.pc.domain.PlayingEffect
import com.uhi.motionlayer.pc.domain.PointerColor
import com.uhi.motionlayer.pc.domain.PointerSize
import com.uhi.motionlayer.pc.domain.PresentationOverlay
import com.uhi.motionlayer.pc.domain.PresentationPreferences
import com.uhi.motionlayer.pc.domain.QrPattern
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Figma 예시에 나온 값. 가짜 구현과 화면 비교용 테스트가 같이 쓴다. */
object PcSampleData {
    const val CODE_SECONDS = 60

    /** PC-01의 예시: 코드 482 913, 0:42 남음. */
    val pairing = PairingCode(qr = fakeQrPattern(seed = 482913), digits = "482913", secondsLeft = 42)

    /** PC-02의 예시. */
    val phone = ConnectedPhone(name = "허윤의 Galaxy", path = LinkPath.SameWifi, latencyMs = 12, oneTime = true)

    val connection = ConnectionPreferences(
        launchAtLogin = true,
        autoConnectMyPhone = true,
        pcName = "강의실 PC",
        allowOtherPhones = false,
        showOperatorName = true,
    )

    /** PC-03의 예시: 보통 · 파랑 · 3초 뒤 · 3개 40MB. */
    val presentation = PresentationPreferences(
        pointerSize = PointerSize.Medium,
        pointerColor = PointerColor.Blue,
        strokeFadeSeconds = 3,
        effectCount = 3,
        effectBytes = 40_000_000,
    )

    const val OPERATOR = "허윤"

    /** PC-06의 포인터 자리(1280x720 시안에서 잰 값). */
    val pointer = OverlayPoint(x = 730f / 1280f, y = 350f / 720f)

    /** PC-07의 선: 시안의 물결 모양을 점으로 옮겼다. */
    val stroke = OverlayStroke(
        points = (0..40).map { i ->
            val t = i / 40f
            val wave = cos(2f * Math.PI.toFloat() * (1.3f * t + 0.08f))
            OverlayPoint(x = (190f + 400f * t) / 1280f, y = (342f + 36f * wave) / 720f)
        },
    )

    /** PC-09의 예시. */
    val effect = PlayingEffect(name = "시연 영상.mp4", positionSeconds = 12, durationSeconds = 40)
}

/**
 * QR처럼 보이는 가짜 무늬. 모서리 세 곳의 네모만 진짜와 같고 나머지 칸은 [seed]로 정한 무작위라서 **폰으로 찍히지 않는다.**
 * 진짜 무늬는 연결 쪽(C)이 토큰으로 만들어 준다.
 */
fun fakeQrPattern(seed: Int, size: Int = 21): QrPattern {
    val random = Random(seed)
    val dark = BooleanArray(size * size) { random.nextBoolean() }
    fun finder(left: Int, top: Int) {
        for (row in -1..7) for (column in -1..7) {
            val x = left + column
            val y = top + row
            if (x !in 0 until size || y !in 0 until size) continue
            val ring = maxOf(kotlin.math.abs(column - 3), kotlin.math.abs(row - 3))
            // 가운데 3x3과 바깥 테두리는 검정, 그 사이와 둘레 한 칸은 흰색
            dark[y * size + x] = ring <= 1 || ring == 3
        }
    }
    finder(0, 0)
    finder(size - 7, 0)
    finder(0, size - 7)
    return QrPattern(size, dark)
}

/**
 * 가짜 폰 연결. 코드는 1초씩 줄다가 0이 되면 새 코드로 바뀐다. 폰은 시연용 동작으로만 연결된다.
 */
class FakePhoneLink(scope: CoroutineScope) : PhoneLink {
    private var generation = 0
    private val _pairing = MutableStateFlow(PcSampleData.pairing)
    private val _phone = MutableStateFlow<ConnectedPhone?>(null)

    override val pairing: StateFlow<PairingCode> = _pairing.asStateFlow()
    override val phone: StateFlow<ConnectedPhone?> = _phone.asStateFlow()

    init {
        scope.launch {
            while (true) {
                delay(1_000)
                _pairing.update { current ->
                    if (current.secondsLeft <= 1) nextCode() else PairingCode(current.qr, current.digits, current.secondsLeft - 1)
                }
            }
        }
    }

    override fun disconnect() {
        _phone.value = null
    }

    private fun nextCode(): PairingCode {
        generation += 1
        val number = Random(generation).nextInt(0, 1_000_000)
        return PairingCode(
            qr = fakeQrPattern(seed = number),
            digits = number.toString().padStart(6, '0'),
            secondsLeft = PcSampleData.CODE_SECONDS,
        )
    }

    val demoActions: List<DemoAction> = listOf(
        DemoAction("폰 연결됨으로 바꾸기 (PC-02)") { _phone.value = PcSampleData.phone },
        DemoAction("폰 연결 끊기 (PC-01)") { _phone.value = null },
        DemoAction("코드를 지금 새로 바꾸기") { _pairing.value = nextCode() },
    )
}

/** 가짜 설정. 값은 프로그램을 끄면 처음으로 돌아가고, 스위치를 눌러도 실제로 적용되는 것은 없다. */
class FakeAgentSettings : AgentSettings {
    private val _connection = MutableStateFlow(PcSampleData.connection)
    private val _presentation = MutableStateFlow(PcSampleData.presentation)

    override val connection: StateFlow<ConnectionPreferences> = _connection.asStateFlow()
    override val presentation: StateFlow<PresentationPreferences> = _presentation.asStateFlow()

    override fun setLaunchAtLogin(on: Boolean) = _connection.update { it.copy(launchAtLogin = on) }
    override fun setAutoConnectMyPhone(on: Boolean) = _connection.update { it.copy(autoConnectMyPhone = on) }
    override fun setAllowOtherPhones(on: Boolean) = _connection.update { it.copy(allowOtherPhones = on) }
    override fun setShowOperatorName(on: Boolean) = _connection.update { it.copy(showOperatorName = on) }
}

/**
 * 가짜 오버레이. 시연용 동작을 누르면 그 모습을 [SHOW_MILLIS] 동안 띄웠다가 스스로 지운다
 * (오버레이가 화면 전체를 덮으므로, 끄는 버튼을 누를 수 없게 되는 일을 막으려고).
 */
class FakePresentationOverlay(private val scope: CoroutineScope) : PresentationOverlay {
    private val _state = MutableStateFlow(OverlayState())
    private var showing: Job? = null

    override val state: StateFlow<OverlayState> = _state.asStateFlow()

    /** 미리 보기는 포인터와 이름표(PC-06 모습)를 띄운다. */
    override fun preview() {
        show(OverlayState(operatorName = PcSampleData.OPERATOR, pointer = PcSampleData.pointer))
    }

    private fun show(state: OverlayState) {
        showing?.cancel()
        showing = scope.launch {
            _state.value = state
            delay(SHOW_MILLIS)
            _state.value = OverlayState()
        }
    }

    /**
     * 실제 폰처럼 값을 계속 바꿔 넣는다. [FRAME_MILLIS]마다 [frame]이 만든 모습으로 바꾸고,
     * [durationMillis]가 지나면 지운다.
     */
    private fun animate(durationMillis: Long, frame: (elapsedMillis: Long) -> OverlayState) {
        showing?.cancel()
        showing = scope.launch {
            val start = System.nanoTime()
            while (true) {
                val elapsed = (System.nanoTime() - start) / 1_000_000
                if (elapsed >= durationMillis) break
                _state.value = frame(elapsed)
                delay(FRAME_MILLIS)
            }
            _state.value = OverlayState()
        }
    }

    /** 폰을 돌리는 것처럼 포인터가 화면 가운데 둘레를 부드럽게 돈다. */
    private fun movingPointer(elapsedMillis: Long): OverlayState {
        val seconds = elapsedMillis / 1000f
        val turn = 2f * Math.PI.toFloat()
        return OverlayState(
            operatorName = PcSampleData.OPERATOR,
            pointer = OverlayPoint(
                x = 0.5f + 0.30f * sin(turn * seconds / 4f),
                y = 0.5f + 0.22f * sin(turn * seconds / 2.7f),
            ),
        )
    }

    /** 3초 동안 선이 왼쪽에서 오른쪽으로 그려지고, 손을 뗀 것처럼 멈췄다가 "지우는 시간" 뒤에 사라진다. */
    private val liveStrokePoints: List<OverlayPoint> = (0 until LIVE_STROKE_POINTS).map { i ->
        val t = i / (LIVE_STROKE_POINTS - 1f)
        val turn = 2f * Math.PI.toFloat()
        OverlayPoint(x = 0.2f + 0.6f * t, y = 0.5f + 0.10f * sin(turn * 2f * t) + 0.04f * sin(turn * 5f * t))
    }
    private val finishedStroke = OverlayState(
        operatorName = PcSampleData.OPERATOR,
        strokes = listOf(OverlayStroke(liveStrokePoints)),
    )

    private fun growingStroke(elapsedMillis: Long): OverlayState {
        if (elapsedMillis >= LIVE_DRAW_MILLIS) return finishedStroke
        val count = maxOf(2, (elapsedMillis * LIVE_STROKE_POINTS / LIVE_DRAW_MILLIS).toInt())
        return OverlayState(
            operatorName = PcSampleData.OPERATOR,
            strokes = listOf(OverlayStroke(liveStrokePoints.take(count))),
        )
    }

    val demoActions: List<DemoAction> = listOf(
        DemoAction("오버레이 · 포인터 6초 (PC-06)") {
            show(OverlayState(operatorName = PcSampleData.OPERATOR, pointer = PcSampleData.pointer))
        },
        DemoAction("오버레이 · 그리기 6초 (PC-07)") {
            show(OverlayState(operatorName = PcSampleData.OPERATOR, strokes = listOf(PcSampleData.stroke)))
        },
        DemoAction("오버레이 · 화면 가리기 6초 (PC-08)") {
            show(OverlayState(operatorName = PcSampleData.OPERATOR, blackout = true))
        },
        DemoAction("오버레이 · 효과 6초 (PC-09)") {
            show(OverlayState(operatorName = PcSampleData.OPERATOR, effect = PcSampleData.effect))
        },
        // 아래 둘은 멈춘 그림이 아니라 값이 계속 바뀌는 모습이다(실제 폰이 붙었을 때처럼).
        DemoAction("오버레이 · 움직이는 포인터 8초 (LIVE-06)") {
            animate(LIVE_POINTER_MILLIS, ::movingPointer)
        },
        DemoAction("오버레이 · 그려지는 선, 3초 뒤 지워짐 (LIVE-07)") {
            animate(LIVE_DRAW_MILLIS + PcSampleData.presentation.strokeFadeSeconds * 1_000L, ::growingStroke)
        },
    )

    private companion object {
        const val SHOW_MILLIS = 6_000L

        /**
         * 값을 바꿔 넣는 간격. Windows에서는 기다리는 시간이 약 15.6ms 단위로 올림돼서,
         * 15로 두면 1초에 약 60번, 20으로 두면 약 32번 바뀐다(Windows에서 잰 값). 분석서의 센서 주기(50Hz)보다 조금 빠르게 둔다.
         */
        const val FRAME_MILLIS = 15L
        const val LIVE_POINTER_MILLIS = 8_000L
        const val LIVE_DRAW_MILLIS = 3_000L
        const val LIVE_STROKE_POINTS = 150
    }
}
