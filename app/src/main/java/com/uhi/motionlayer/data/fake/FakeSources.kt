package com.uhi.motionlayer.data.fake

import com.uhi.motionlayer.domain.model.AppCategory
import com.uhi.motionlayer.domain.model.AppEvent
import com.uhi.motionlayer.domain.model.AppGestureSettings
import com.uhi.motionlayer.domain.model.AppPermission
import com.uhi.motionlayer.domain.model.ControlTarget
import com.uhi.motionlayer.domain.model.ControllableApp
import com.uhi.motionlayer.domain.model.CustomGesture
import com.uhi.motionlayer.domain.model.CustomGestureRate
import com.uhi.motionlayer.domain.model.DailyStats
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.domain.model.DetectionStatus
import com.uhi.motionlayer.domain.model.GestureBinding
import com.uhi.motionlayer.domain.model.GestureCommand
import com.uhi.motionlayer.domain.model.GestureType
import com.uhi.motionlayer.domain.model.PairingFailure
import com.uhi.motionlayer.domain.model.PairingResult
import com.uhi.motionlayer.domain.model.PairingTarget
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.PermissionStatus
import com.uhi.motionlayer.domain.model.PlayingApp
import com.uhi.motionlayer.domain.model.RecentPc
import com.uhi.motionlayer.domain.model.ReportRange
import com.uhi.motionlayer.domain.model.UsageReport
import com.uhi.motionlayer.domain.model.VibrationFeedback
import com.uhi.motionlayer.domain.source.AppCatalog
import com.uhi.motionlayer.domain.source.AppEvents
import com.uhi.motionlayer.domain.source.ControlTargetSelector
import com.uhi.motionlayer.domain.source.CustomGestureLibrary
import com.uhi.motionlayer.domain.source.DetectionController
import com.uhi.motionlayer.domain.source.GestureTrial
import com.uhi.motionlayer.domain.source.MediaSource
import com.uhi.motionlayer.domain.source.NetworkStatus
import com.uhi.motionlayer.domain.source.PcLink
import com.uhi.motionlayer.domain.source.SettingsStore
import com.uhi.motionlayer.domain.source.SystemPermissions
import com.uhi.motionlayer.domain.source.UsageStats
import com.uhi.motionlayer.domain.source.VibrationPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * 실제 구현이 없을 때 화면을 돌려 보기 위한 가짜 구현들.
 * 상태를 메모리에만 들고 있고, 센서·권한·네트워크는 전혀 쓰지 않는다.
 */

/**
 * 가짜 감지 서비스. 처음에는 정상이고, [debugActions]로 HM-07(상태 확인 중)과 HM-08(서비스 멈춤)을 만들어 볼 수 있다.
 * 감지를 켜고 끄면 멈춰 있던 서비스도 다시 정상으로 돌아온다.
 */
class FakeDetectionController(
    initiallyEnabled: Boolean = false,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : DetectionController {
    private val _enabled = MutableStateFlow(initiallyEnabled)
    override val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _status = MutableStateFlow(DetectionStatus.Normal)
    override val status: StateFlow<DetectionStatus> = _status.asStateFlow()

    override fun setEnabled(enabled: Boolean) {
        _enabled.value = enabled
        _status.value = DetectionStatus.Normal
    }

    val debugActions: List<DebugAction> = listOf(
        DebugAction("홈 · 상태 확인 중 2초 (HM-07)") {
            scope.launch {
                _status.value = DetectionStatus.Checking
                delay(CHECKING_MILLIS)
                _status.value = DetectionStatus.Normal
            }
        },
        DebugAction("홈 · 감지 서비스 멈춤 (HM-08)") { _status.value = DetectionStatus.Stopped },
        DebugAction("홈 · 걷는 중 켜고 끄기 (HM-04)") {
            _status.update { if (it == DetectionStatus.WalkingPaused) DetectionStatus.Normal else DetectionStatus.WalkingPaused }
        },
    )

    private companion object {
        const val CHECKING_MILLIS = 2_000L
    }
}

/**
 * 가짜 앱 신호. 제스처·연결 신호가 없는 에뮬레이터에서 토스트 5가지와 연습 제안 시트를 눌러 볼 수 있게 [debugActions]로 일으킨다.
 * 받는 쪽이 없을 때(앱 화면이 꺼져 있을 때) 보낸 일은 버려진다.
 */
class FakeAppEvents : AppEvents {
    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 8)
    override val events: Flow<AppEvent> = _events.asSharedFlow()

    /** 조작 대상을 되돌리는 일은 [FakeControlTarget]이 한다. 앱 시작에서 이어 준다. */
    var undoControlTargetChangeHandler: () -> Unit = {}

    override fun undoControlTargetChange() = undoControlTargetChangeHandler()

    fun emit(event: AppEvent) {
        _events.tryEmit(event)
    }

    val debugActions: List<DebugAction> = listOf(
        DebugAction("토스트 · 다음 곡으로 넘김") { _events.tryEmit(AppEvent.CommandExecuted(GestureCommand.NextTrack)) },
        DebugAction("토스트 · 동작 되돌림") { _events.tryEmit(AppEvent.CommandUndone) },
        DebugAction("토스트 · 되돌릴 동작 없음") { _events.tryEmit(AppEvent.NothingToUndo) },
        DebugAction("토스트 · 실행 실패") { _events.tryEmit(AppEvent.CommandFailed(reason = "거실 TV가 꺼져 있어")) },
        DebugAction("토스트 · PC 연결됨") { _events.tryEmit(AppEvent.PcConnected(SampleData.classroomPc.deviceName)) },
        DebugAction("토스트 · 조작 대상 바뀜") {
            _events.tryEmit(AppEvent.ControlTargetChanged(SampleData.classroomPc.deviceName))
        },
    )
}

/** 가짜 인터넷 상태. 처음에는 연결돼 있고, [debugActions]로 끊어 SY-06 홈을 볼 수 있다. */
class FakeNetworkStatus(initiallyOnline: Boolean = true) : NetworkStatus {
    private val _online = MutableStateFlow(initiallyOnline)
    override val online: StateFlow<Boolean> = _online.asStateFlow()

    val debugActions: List<DebugAction> = listOf(
        DebugAction("인터넷 끊기 (SY-06 홈)") { _online.value = false },
        DebugAction("인터넷 다시 연결") { _online.value = true },
    )
}

/**
 * 가짜 안드로이드 권한. 시스템 허용 창과 설정 앱은 띄우지 않는다. 처음에는 전부 "요청 전"이라,
 * 홈에서 감지를 껐다 켜면 권한 안내(ON-05·09)가 나온다. 요청하면 곧 허용된 것으로 친다(거부는 흉내 내지 않는다).
 * "설정 앱을 연다"는 사용자가 거기서 값을 뒤집은 것으로 친다: 허용돼 있으면 끄고, 아니면 켠다.
 * 앱을 껐다 켜면 처음으로 돌아간다.
 */
class FakeSystemPermissions(private val answerMillis: Long = 400) : SystemPermissions {
    private val _statuses = MutableStateFlow(AppPermission.entries.associateWith { PermissionStatus.NotRequested })
    override val statuses: StateFlow<Map<AppPermission, PermissionStatus>> = _statuses.asStateFlow()

    override suspend fun request(permission: AppPermission) {
        delay(answerMillis)
        set(permission, PermissionStatus.Granted)
    }

    override fun openSettings(permission: AppPermission) {
        val granted = _statuses.value[permission] == PermissionStatus.Granted
        set(permission, if (granted) PermissionStatus.Denied else PermissionStatus.Granted)
    }

    private fun set(permission: AppPermission, status: PermissionStatus) {
        _statuses.update { it + (permission to status) }
    }
}

/**
 * 가짜 재생 앱. 처음에는 YouTube Music이 재생 중이다. 앱별 동작 설정(AP-02)에서 고친 값이 홈의 "이 앱 설정 적용" 칩에
 * 그대로 나타나게 [catalog]와 이어 두었다. 앱 목록에서 앱을 꺼도 재생 중으로 알리는 것은 그대로다
 * (꺼 둔 앱이 재생될 때 홈에 무엇을 보여 줄지는 미정).
 */
class FakeMediaSource(
    catalog: AppCatalog,
    initialApp: PlayingApp? = SampleData.youtubeMusic,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MediaSource {
    private val playing = MutableStateFlow(initialApp)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val playingApp: StateFlow<PlayingApp?> = playing
        .flatMapLatest { app ->
            val id = app?.appId
            if (app == null || id == null) {
                flowOf(app)
            } else {
                catalog.gestureSettings(id).map { settings -> app.copy(bindings = settings.homeBindings()) }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = initialApp?.let { app ->
                app.appId?.let { id -> app.copy(bindings = catalog.gestureSettings(id).value.homeBindings()) } ?: app
            },
        )

    fun play(app: PlayingApp?) {
        playing.value = app
    }

    /** 홈 카드에 칩으로 보여 주는 제스처: 오른쪽 휙, 두 번 톡톡, 기울임. */
    private fun AppGestureSettings.homeBindings(): List<GestureBinding> =
        listOf(GestureType.SwipeRight, GestureType.DoubleTap, GestureType.Tilt)
            .mapNotNull { gesture -> commands[gesture]?.let { GestureBinding(gesture, it) } }
}

/**
 * 가짜 조작 대상. PC가 연결되면 PC로, 끊기면 폰으로 저절로(소리 없이) 바뀌고, 사용자가 고르면(HM-06) 그곳이 된다.
 * 사용자가 바꾼 것은 [onChanged]로 알려 토스트("이제 강의실 PC를 조작해요")를 띄우게 하고, [undo]로 한 단계 되돌릴 수 있다.
 */
class FakeControlTarget(
    private val pcLink: PcLink,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val onChanged: (targetName: String) -> Unit = {},
) : ControlTargetSelector {
    private val _selected = MutableStateFlow(ControlTarget.Phone)
    override val selected: StateFlow<ControlTarget> = _selected.asStateFlow()

    private var previous: ControlTarget? = null

    init {
        scope.launch {
            pcLink.state.collect { state ->
                _selected.value = if (state is PcConnectionState.Connected) ControlTarget.Pc else ControlTarget.Phone
                previous = null
            }
        }
    }

    override fun select(target: ControlTarget) {
        val current = _selected.value
        if (target == current) return
        previous = current
        _selected.value = target
        onChanged(nameOf(target))
    }

    /** 방금 사용자가 바꾼 것을 되돌린다. 바꾼 적이 없으면 아무 일도 하지 않는다. */
    fun undo() {
        previous?.let { _selected.value = it }
        previous = null
    }

    private fun nameOf(target: ControlTarget): String = when (target) {
        ControlTarget.Phone -> "이 폰"
        ControlTarget.Pc -> (pcLink.state.value as? PcConnectionState.Connected)?.deviceName ?: "PC"
    }
}

/**
 * 메모리에만 들고 있는 가짜 앱 목록. 켜고 끈 상태는 앱을 껐다 켜면 처음 값으로 돌아간다.
 * 다른 가짜 구현과 이어 두지 않았다: 여기서 끈 앱도 [FakeMediaSource]는 계속 재생 중으로 알린다.
 *
 * 나만의 제스처를 앱마다 어떤 동작에 연결했는지만 [customGestures]와 이어 두었다: 제스처를 지우면 그 제스처의 연결도 곧 사라지고,
 * 앱 줄의 "나만의 제스처 N개"는 연결 수를 따라간다.
 */
class FakeAppCatalog(
    customGestures: CustomGestureLibrary,
    initial: List<ControllableApp> = SampleData.apps,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : AppCatalog {
    private val _apps = MutableStateFlow(initial)
    override val apps: StateFlow<List<ControllableApp>> = _apps.asStateFlow()

    private val _loading = MutableStateFlow(false)
    override val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /** 시연용: 앱 목록을 불러오는 중(AP-07)을 2초 동안 보여 준다. */
    val debugActions: List<DebugAction> = listOf(
        DebugAction("앱 목록 · 불러오는 중 2초 (AP-07)") {
            scope.launch {
                _loading.value = true
                delay(LOADING_MILLIS)
                _loading.value = false
            }
        },
    )

    private val settings = mutableMapOf<String, MutableStateFlow<AppGestureSettings>>()

    /** 처음에는 Figma GS-11처럼 YouTube Music에서 "원 그리기"가 다음 곡이다. */
    private val _customCommands = MutableStateFlow(
        mapOf("youtube_music" to mapOf(SampleData.customCircle.id to GestureCommand.NextTrack)),
    )
    override val customGestureCommands: StateFlow<Map<String, Map<String, GestureCommand>>> = _customCommands.asStateFlow()

    init {
        // 제스처를 지우면 그 제스처로 하던 앱별 동작도 함께 지운다.
        scope.launch {
            customGestures.gestures.collect { gestures ->
                val existing = gestures.mapTo(mutableSetOf()) { it.id }
                updateCustomCommands { all -> all.mapValues { (_, byGesture) -> byGesture.filterKeys { it in existing } } }
            }
        }
    }

    /** 나만의 제스처에 고를 수 있는 동작은 휙과 같다(Figma에 나만의 제스처의 목록이 없어 직접 정했다). */
    override fun customCommandOptions(appId: String): List<GestureCommand> = commandOptions(appId, GestureType.SwipeRight)

    override fun setCustomCommand(appId: String, gestureId: String, command: GestureCommand?) {
        updateCustomCommands { all ->
            val byGesture = all[appId].orEmpty()
            all + (appId to if (command == null) byGesture - gestureId else byGesture + (gestureId to command))
        }
    }

    /** 연결을 바꾸고, 연결이 하나도 없어진 앱은 빼고, 앱 줄의 나만의 제스처 수를 새로 센다. */
    private fun updateCustomCommands(
        change: (Map<String, Map<String, GestureCommand>>) -> Map<String, Map<String, GestureCommand>>,
    ) {
        _customCommands.update { all -> change(all).filterValues { it.isNotEmpty() } }
        _apps.update { list -> list.map { it.copy(customGestureCount = _customCommands.value[it.id]?.size ?: 0) } }
    }

    override fun setEnabled(appId: String, enabled: Boolean) {
        _apps.update { list -> list.map { if (it.id == appId) it.copy(enabled = enabled) else it } }
    }

    override fun gestureSettings(appId: String): StateFlow<AppGestureSettings> = settingsFlow(appId).asStateFlow()

    /**
     * 휙 두 가지는 음악 앱이면 곡 넘기기, 영상 앱이면 10초 이동이다(Figma AP-02의 YouTube 값).
     * 고를 수 있는 동작은 제스처마다 직접 정했다: 휙은 다섯 가지(Figma), 톡톡은 재생·이전·다음, 기울임은 볼륨, 흔들기는 되돌리기.
     */
    override fun commandOptions(appId: String, gesture: GestureType): List<GestureCommand> = when (gesture) {
        GestureType.SwipeRight, GestureType.SwipeLeft -> listOf(
            GestureCommand.NextTrack,
            GestureCommand.SkipForward,
            GestureCommand.PreviousTrack,
            GestureCommand.SkipBack,
            GestureCommand.PlayPause,
        )
        GestureType.DoubleTap -> listOf(GestureCommand.PlayPause, GestureCommand.NextTrack, GestureCommand.PreviousTrack)
        GestureType.Tilt -> listOf(GestureCommand.Volume)
        GestureType.Shake -> listOf(GestureCommand.Undo)
        GestureType.PressAndRotate -> listOf(GestureCommand.Pointer)
    }

    override fun setCommand(appId: String, gesture: GestureType, command: GestureCommand?) {
        settingsFlow(appId).update { it.copy(commands = it.commands + (gesture to command)) }
    }

    override fun resetGestureSettings(appId: String) {
        settingsFlow(appId).value = defaultSettings(appId)
        updateCustomCommands { all -> all - appId }
    }

    private fun settingsFlow(appId: String): MutableStateFlow<AppGestureSettings> =
        synchronized(settings) { settings.getOrPut(appId) { MutableStateFlow(defaultSettings(appId)) } }

    private fun defaultSettings(appId: String): AppGestureSettings {
        val video = _apps.value.firstOrNull { it.id == appId }?.category == AppCategory.Video
        return AppGestureSettings(
            appId = appId,
            commands = mapOf(
                GestureType.SwipeRight to if (video) GestureCommand.SkipForward else GestureCommand.NextTrack,
                GestureType.SwipeLeft to if (video) GestureCommand.SkipBack else GestureCommand.PreviousTrack,
                GestureType.DoubleTap to GestureCommand.PlayPause,
                GestureType.Tilt to GestureCommand.Volume,
                GestureType.Shake to GestureCommand.Undo,
            ),
        )
    }

    private companion object {
        const val LOADING_MILLIS = 2_000L
    }
}

/**
 * 가짜 PC 연결. 네트워크를 쓰지 않고 잠깐 기다린 뒤 결과를 돌려준다.
 *
 * 시연용 약속: 6자리 코드를 **000000**으로 입력하면 "만료"로 실패한다(PR-05 확인용).
 * 그 밖의 QR·코드·최근 PC는 모두 성공하고, 결과가 나오기까지 1.5초 걸린다("PC에 연결하는 중" PR-16이 그동안 보인다).
 * [debugActions]로 "응답 없음"(3초 뒤 실패)과 "최근 PC 비움"(PR-17)을 켜 볼 수 있다.
 */
class FakePcLink(private val latencyMillis: Long = 1_500) : PcLink {
    private val _state = MutableStateFlow<PcConnectionState>(PcConnectionState.Disconnected)
    override val state: StateFlow<PcConnectionState> = _state.asStateFlow()

    private val _recentPcs = MutableStateFlow(SampleData.recentPcs)
    override val recentPcs: StateFlow<List<RecentPc>> = _recentPcs.asStateFlow()

    /** 켜 두면 PC가 답하지 않는 것처럼 3초 기다렸다 실패한다(분석서 표 4-3: 3초가 넘으면 실패 PR-05). 취소 버튼도 이때 눌러 볼 수 있다. */
    private var unresponsive = false

    val debugActions: List<DebugAction> = listOf(
        DebugAction("PC 연결 · 응답 없게 켜고 끄기 (3초 뒤 실패)") { unresponsive = !unresponsive },
        DebugAction("발표 탭 · 최근 PC 비우기·되돌리기 (PR-17)") {
            _recentPcs.value = if (_recentPcs.value.isEmpty()) SampleData.recentPcs else emptyList()
        },
    )

    override suspend fun connect(target: PairingTarget): PairingResult {
        if (unresponsive) {
            delay(TIMEOUT_MILLIS)
            return PairingResult.Failed(PairingFailure.Unknown)
        }
        delay(latencyMillis)
        if (target is PairingTarget.SixDigitCode && target.code == FAIL_CODE) {
            return PairingResult.Failed(PairingFailure.TokenExpired)
        }
        val name = (target as? PairingTarget.Recent)
            ?.let { recent -> _recentPcs.value.firstOrNull { it.id == recent.id }?.name }
            ?: SampleData.classroomPc.deviceName
        val connected = SampleData.classroomPc.copy(deviceName = name)
        _state.value = connected
        return PairingResult.Connected(connected)
    }

    override fun disconnect() {
        _state.value = PcConnectionState.Disconnected
    }

    companion object {
        const val FAIL_CODE = "000000"
        private const val TIMEOUT_MILLIS = 3_000L
    }
}

/**
 * 가짜 사용 기록. 나만의 제스처는 [customGestures]를 따라간다: 만든 제스처마다 성공률 줄이 하나 생기고
 * (Figma 샘플이 있는 "원 그리기"는 그 값, 새로 만든 것은 자기 인식률), 감도를 충분히 낮추면 "감도를 낮춰 볼까요?" 제안이 사라진다.
 */
class FakeUsageStats(
    customGestures: CustomGestureLibrary,
    initial: DailyStats = SampleData.todayStats,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : UsageStats {
    private val _today = MutableStateFlow(initial)
    override val today: StateFlow<DailyStats> = _today.asStateFlow()

    private val reports: Map<ReportRange, StateFlow<UsageReport>> = SampleData.reports.associate { base ->
        base.range to customGestures.gestures
            .map { gestures -> withCustomGestures(base, gestures) }
            .stateIn(scope, SharingStarted.Eagerly, withCustomGestures(base, customGestures.gestures.value))
    }

    override fun report(range: ReportRange): StateFlow<UsageReport> = reports.getValue(range)

    /** 목표(90%)에 못 미치면 주황으로, 감도가 0.6 이상일 때만 감도를 낮추라고 권한다(실제 기준은 A가 정한다). */
    private fun withCustomGestures(base: UsageReport, gestures: List<CustomGesture>): UsageReport {
        val samples = base.customGestureRates.associateBy { it.gestureId }
        val rates = gestures.map { gesture ->
            samples[gesture.id]?.copy(name = gesture.name)
                ?: CustomGestureRate(
                    gestureId = gesture.id,
                    name = gesture.name,
                    successRatePercent = gesture.successRatePercent,
                    needsAttention = gesture.successRatePercent < ATTENTION_BELOW_PERCENT,
                )
        }
        val suggestion = base.sensitivitySuggestion?.let { sample ->
            gestures.firstOrNull { it.id == sample.gestureId }
                ?.takeIf { it.sensitivity >= SUGGEST_FROM_SENSITIVITY }
                ?.let { gesture -> sample.copy(gestureName = gesture.name, sensitivity = gesture.sensitivity) }
        }
        return base.copy(customGestureRates = rates, sensitivitySuggestion = suggestion)
    }

    private companion object {
        const val ATTENTION_BELOW_PERCENT = 90
        const val SUGGEST_FROM_SENSITIVITY = 0.6f
    }
}

/** 메모리에만 저장하는 가짜 설정. 앱을 껐다 켜면 처음 값으로 돌아간다. 처음 값은 Figma ST-01·02와 같다. */
class FakeSettingsStore(onboardingCompleted: Boolean = false) : SettingsStore {
    /** 가짜는 앱을 켤 때마다 처음 상태라, 기본으로 첫 실행 안내가 먼저 나온다. 실제 구현은 마친 뒤로 폰에 저장해 다시 나오지 않게 한다. */
    private val _onboardingCompleted = MutableStateFlow(onboardingCompleted)
    override val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _pauseWhileWalking = MutableStateFlow(true)
    override val pauseGesturesWhileWalking: StateFlow<Boolean> = _pauseWhileWalking.asStateFlow()

    private val _pocketGesture = MutableStateFlow(true)
    override val pocketGestureEnabled: StateFlow<Boolean> = _pocketGesture.asStateFlow()

    /** Figma 슬라이더가 약 61% 지점이고, ST-01에 "보통"으로 적혀 있다. */
    private val _vibrationStrength = MutableStateFlow(0.61f)
    override val vibrationStrength: StateFlow<Float> = _vibrationStrength.asStateFlow()

    override fun setPauseGesturesWhileWalking(enabled: Boolean) {
        _pauseWhileWalking.value = enabled
    }

    override fun setPocketGestureEnabled(enabled: Boolean) {
        _pocketGesture.value = enabled
    }

    override fun setVibrationStrength(strength: Float) {
        _vibrationStrength.value = strength.coerceIn(0f, 1f)
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        _onboardingCompleted.value = completed
    }
}

/**
 * 가짜 첫 제스처 체험. 센서가 없으므로 휙이 인식된 것은 시연용 동작([debugActions])으로 만든다.
 * 체험 화면의 일러스트를 길게 누르면(디버그 빌드) 열리는 창에 나온다.
 */
class FakeGestureTrial : GestureTrial {
    private val _swipeCount = MutableStateFlow(0)
    override val swipeCount: StateFlow<Int> = _swipeCount.asStateFlow()

    override fun start() = Unit

    override fun stop() = Unit

    val debugActions: List<DebugAction> = listOf(
        DebugAction("오른쪽 휙 인식시키기") { _swipeCount.update { it + 1 } },
    )
}

/** 진동을 실제로 울리지 않는 가짜. 실제 진동 박자는 시스템 연동(B)이 만든다. */
class FakeVibrationPlayer : VibrationPlayer {
    override fun play(feedback: VibrationFeedback) = Unit
}
