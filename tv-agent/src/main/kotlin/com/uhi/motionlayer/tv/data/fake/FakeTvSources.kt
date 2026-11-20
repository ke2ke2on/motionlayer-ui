package com.uhi.motionlayer.tv.data.fake

import com.uhi.motionlayer.tv.domain.ConnectedPerson
import com.uhi.motionlayer.tv.domain.DemoAction
import com.uhi.motionlayer.tv.domain.EffectKind
import com.uhi.motionlayer.tv.domain.OperatorNotice
import com.uhi.motionlayer.tv.domain.PersonRole
import com.uhi.motionlayer.tv.domain.PersonStatus
import com.uhi.motionlayer.tv.domain.QrPattern
import com.uhi.motionlayer.tv.domain.TvCommand
import com.uhi.motionlayer.tv.domain.TvConnections
import com.uhi.motionlayer.tv.domain.TvEffect
import com.uhi.motionlayer.tv.domain.TvKind
import com.uhi.motionlayer.tv.domain.TvOverlay
import com.uhi.motionlayer.tv.domain.TvOverlayState
import com.uhi.motionlayer.tv.domain.TvPairing
import com.uhi.motionlayer.tv.domain.TvPairingCode
import com.uhi.motionlayer.tv.domain.TvSetup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

/** Figma "21 TV" 예시에 나온 값. 가짜 구현과 테스트가 같이 쓴다. */
object TvSampleData {
    const val CODE_SECONDS = 60

    /** TV-02의 예시: 코드 482 913. */
    val pairing = TvPairingCode(qr = fakeQrPattern(seed = 482913), digits = "482913")

    /** TV-03의 예시. */
    const val TV_NAME = "거실 TV"
    val people = listOf(
        ConnectedPerson(name = "허윤", role = PersonRole.Owner, status = PersonStatus.Operating),
        ConnectedPerson(name = "민수", role = PersonRole.Guest, status = PersonStatus.SameWifi),
    )

    /** 공용 TV에 손님이 많이 붙은 모습(직접 만든 값, 시안에 없음). 주인이 없고 다섯 명이라 TV-03이 줄을 묶는다. */
    val crowd = listOf(
        ConnectedPerson(name = "민수", role = PersonRole.Guest, status = PersonStatus.Operating),
        ConnectedPerson(name = "지수", role = PersonRole.Guest, status = PersonStatus.SameWifi),
        ConnectedPerson(name = "서연", role = PersonRole.Guest, status = PersonStatus.SameWifi),
        ConnectedPerson(name = "도윤", role = PersonRole.Guest, status = PersonStatus.SameWifi),
        ConnectedPerson(name = "하준", role = PersonRole.Guest, status = PersonStatus.SameWifi),
    )

    /** TV-04의 예시: "민수 ▸ 다음 채널". */
    val operator = OperatorNotice(name = "민수", command = TvCommand.NextChannel)

    /** TV-05의 예시: "허윤 님이 밖에서 조작하고 있어요". */
    const val REMOTE_OPERATOR = "허윤"

    /** TV-06의 예시: "지수가 보낸 사진", "3초 뒤 닫혀요". */
    val effect = TvEffect(senderName = "지수", kind = EffectKind.Photo, secondsLeft = 3)
}

/**
 * QR처럼 보이는 가짜 무늬. 모서리 세 곳의 네모만 진짜와 같고 나머지 칸은 [seed]로 정한 무작위라서 **폰으로 찍히지 않는다.**
 * 진짜 무늬는 연결 쪽(C)이 토큰으로 만들어 준다. PC 모듈의 것과 같은 코드다.
 */
fun fakeQrPattern(seed: Int, size: Int = 21): QrPattern {
    val random = Random(seed)
    val dark = BooleanArray(size * size) { random.nextBoolean() }
    fun finder(left: Int, top: Int) {
        for (row in -1..7) for (column in -1..7) {
            val x = left + column
            val y = top + row
            if (x !in 0 until size || y !in 0 until size) continue
            val ring = maxOf(abs(column - 3), abs(row - 3))
            // 가운데 3x3과 바깥 테두리는 검정, 그 사이와 둘레 한 칸은 흰색
            dark[y * size + x] = ring <= 1 || ring == 3
        }
    }
    finder(0, 0)
    finder(size - 7, 0)
    finder(0, size - 7)
    return QrPattern(size, dark)
}

/** 가짜 설정. 앱을 켤 때마다 "아직 안 고름"으로 시작해서 TV-01이 매번 나온다(실제 구현은 고른 값을 저장한다). */
class FakeTvSetup : TvSetup {
    private val _kind = MutableStateFlow<TvKind?>(null)
    override val kind: StateFlow<TvKind?> = _kind.asStateFlow()

    override fun choose(kind: TvKind) {
        _kind.value = kind
    }

    val demoActions: List<DemoAction> = listOf(
        DemoAction("처음 화면으로 되돌리기 (TV-01)") { _kind.value = null },
        DemoAction("공용 TV로 고른 것으로 하기 (TV-02)") { _kind.value = TvKind.Public },
    )
}

/** 가짜 QR. 60초마다 새 코드로 바뀐다. */
class FakeTvPairing(scope: CoroutineScope) : TvPairing {
    private var generation = 0
    private val _code = MutableStateFlow(TvSampleData.pairing)
    override val code: StateFlow<TvPairingCode> = _code.asStateFlow()

    init {
        scope.launch {
            while (true) {
                delay(TvSampleData.CODE_SECONDS * 1_000L)
                _code.value = nextCode()
            }
        }
    }

    private fun nextCode(): TvPairingCode {
        generation += 1
        val number = Random(generation).nextInt(0, 1_000_000)
        return TvPairingCode(qr = fakeQrPattern(seed = number), digits = number.toString().padStart(6, '0'))
    }

    val demoActions: List<DemoAction> = listOf(
        DemoAction("코드를 지금 새로 바꾸기 (TV-02)") { _code.value = nextCode() },
    )
}

/** 가짜 연결. 처음에는 아무도 없고, 시연용 동작으로 Figma 예시의 두 사람이 연결된다. */
class FakeTvConnections : TvConnections {
    private val _people = MutableStateFlow<List<ConnectedPerson>>(emptyList())
    override val tvName: StateFlow<String> = MutableStateFlow(TvSampleData.TV_NAME).asStateFlow()
    override val people: StateFlow<List<ConnectedPerson>> = _people.asStateFlow()

    val demoActions: List<DemoAction> = listOf(
        DemoAction("사람 둘 연결됨으로 바꾸기 (TV-03)") { _people.value = TvSampleData.people },
        DemoAction("손님 다섯 명 연결됨 (TV-03, 많을 때)") { _people.value = TvSampleData.crowd },
        DemoAction("모두 연결 끊기 (TV-02)") { _people.value = emptyList() },
    )
}

/**
 * 가짜 오버레이. 시연용 동작을 누르면 그 모습을 띄웠다가 스스로 지운다.
 * 이름표는 Figma 노트대로 2초, 원격 조작 띠는 6초, 효과는 "3초 뒤 닫혀요"가 3 → 2 → 1로 줄다가 닫힌다.
 */
class FakeTvOverlay(private val scope: CoroutineScope) : TvOverlay {
    private val _state = MutableStateFlow(TvOverlayState())
    override val state: StateFlow<TvOverlayState> = _state.asStateFlow()

    private var operatorJob: Job? = null
    private var remoteJob: Job? = null
    private var effectJob: Job? = null

    private fun showOperator() {
        operatorJob?.cancel()
        operatorJob = scope.launch {
            _state.update { it.copy(operator = TvSampleData.operator) }
            delay(OPERATOR_MILLIS)
            _state.update { it.copy(operator = null) }
        }
    }

    private fun showRemote() {
        remoteJob?.cancel()
        remoteJob = scope.launch {
            _state.update { it.copy(remoteOperator = TvSampleData.REMOTE_OPERATOR) }
            delay(REMOTE_MILLIS)
            _state.update { it.copy(remoteOperator = null) }
        }
    }

    private fun showEffect() {
        effectJob?.cancel()
        effectJob = scope.launch {
            for (seconds in TvSampleData.effect.secondsLeft downTo 1) {
                _state.update { it.copy(effect = TvSampleData.effect.copy(secondsLeft = seconds)) }
                delay(SECOND_MILLIS)
            }
            _state.update { it.copy(effect = null) }
        }
    }

    val demoActions: List<DemoAction> = listOf(
        DemoAction("오버레이 · 조작한 사람 2초 (TV-04)") { showOperator() },
        DemoAction("오버레이 · 원격 조작 중 6초 (TV-05)") { showRemote() },
        DemoAction("오버레이 · 효과 3초 (TV-06)") { showEffect() },
    )

    private companion object {
        const val OPERATOR_MILLIS = 2_000L
        const val REMOTE_MILLIS = 6_000L
        const val SECOND_MILLIS = 1_000L
    }
}
