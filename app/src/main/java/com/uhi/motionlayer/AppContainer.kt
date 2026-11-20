package com.uhi.motionlayer

import com.uhi.motionlayer.data.fake.FakeAccountSession
import com.uhi.motionlayer.data.fake.FakeAppCatalog
import com.uhi.motionlayer.data.fake.FakeAppEvents
import com.uhi.motionlayer.data.fake.FakeControlTarget
import com.uhi.motionlayer.data.fake.FakeCustomGestures
import com.uhi.motionlayer.data.fake.FakeDetectionController
import com.uhi.motionlayer.data.fake.FakeDeviceRegistry
import com.uhi.motionlayer.data.fake.FakeDiagnosticsSource
import com.uhi.motionlayer.data.fake.FakeEffectLibrary
import com.uhi.motionlayer.data.fake.FakeGestureCatalog
import com.uhi.motionlayer.data.fake.FakeGestureTrial
import com.uhi.motionlayer.data.fake.FakeDirectionPicker
import com.uhi.motionlayer.data.fake.FakeMediaSource
import com.uhi.motionlayer.data.fake.FakeNearbyDevices
import com.uhi.motionlayer.data.fake.FakeNetworkStatus
import com.uhi.motionlayer.data.fake.FakePcLink
import com.uhi.motionlayer.data.fake.FakePhoneControl
import com.uhi.motionlayer.data.fake.FakePracticeSession
import com.uhi.motionlayer.data.fake.FakePresentationController
import com.uhi.motionlayer.data.fake.FakeRemoteControl
import com.uhi.motionlayer.data.fake.FakeSettingsStore
import com.uhi.motionlayer.data.fake.FakeSystemPermissions
import com.uhi.motionlayer.data.fake.FakeUsageStats
import com.uhi.motionlayer.data.fake.FakeVibrationPlayer
import com.uhi.motionlayer.domain.model.AppEvent
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.PresentationLink
import com.uhi.motionlayer.domain.source.AccountSession
import com.uhi.motionlayer.domain.source.AppCatalog
import com.uhi.motionlayer.domain.source.AppEvents
import com.uhi.motionlayer.domain.source.ControlTargetSelector
import com.uhi.motionlayer.domain.source.CustomGestureLibrary
import com.uhi.motionlayer.domain.source.DetectionController
import com.uhi.motionlayer.domain.source.DeviceRegistry
import com.uhi.motionlayer.domain.source.DiagnosticsSource
import com.uhi.motionlayer.domain.source.DirectionPicker
import com.uhi.motionlayer.domain.source.EffectLibrary
import com.uhi.motionlayer.domain.source.GestureCatalog
import com.uhi.motionlayer.domain.source.GestureRecorder
import com.uhi.motionlayer.domain.source.GestureTrial
import com.uhi.motionlayer.domain.source.MediaSource
import com.uhi.motionlayer.domain.source.NearbyDevices
import com.uhi.motionlayer.domain.source.NetworkStatus
import com.uhi.motionlayer.domain.source.PcLink
import com.uhi.motionlayer.domain.source.PhoneControl
import com.uhi.motionlayer.domain.source.PracticeSession
import com.uhi.motionlayer.domain.source.PresentationController
import com.uhi.motionlayer.domain.source.RemoteControl
import com.uhi.motionlayer.domain.source.SettingsStore
import com.uhi.motionlayer.domain.source.SystemPermissions
import com.uhi.motionlayer.domain.source.UsageStats
import com.uhi.motionlayer.domain.source.VibrationPlayer
import com.uhi.motionlayer.ui.presentation.FakeQrCamera
import com.uhi.motionlayer.ui.presentation.QrCameraContent

/**
 * 화면이 쓰는 구현체를 고르는 **단 하나의 자리**.
 *
 * 지금은 모두 가짜 구현이다. A·B·C가 실제 구현을 만들면 아래 값만 바꾼다. 화면 코드는 고치지 않는다.
 */
object AppContainer {
    private val fakeDetection = FakeDetectionController(initiallyEnabled = true)
    val detectionController: DetectionController = fakeDetection
    val systemPermissions: SystemPermissions = FakeSystemPermissions()

    /** 계정(ON-10, ST-01·05)과 계정에 등록한 내 기기(DV). 기기는 로그인한 동안에만 보여서 계정과 이어 둔다. */
    private val fakeAccount = FakeAccountSession()
    val accountSession: AccountSession = fakeAccount

    /** 로그인 화면(ON-10)의 시연용 동작: 다음 로그인을 실패시키기. 실제 구현으로 바꾸면 빈 목록이 된다. */
    val accountDebugActions: List<DebugAction> = fakeAccount.debugActions

    /** 나만의 제스처 목록과 만들기(GS-03~08, GS-11). 목록과 만들기는 같은 가짜가 맡는다. 앱별 연결(AP-02)이 이 목록을 따라가서 먼저 만든다. */
    private val fakeCustomGestures = FakeCustomGestures()
    val customGestureLibrary: CustomGestureLibrary = fakeCustomGestures
    val gestureRecorder: GestureRecorder = fakeCustomGestures

    private val fakeAppCatalog = FakeAppCatalog(customGestures = fakeCustomGestures)
    val appCatalog: AppCatalog = fakeAppCatalog

    /** 전화 제어(AP-04). 켜려면 전화 권한이 필요해서 권한 가짜와 이어 둔다. */
    val phoneControl: PhoneControl = FakePhoneControl(permissions = systemPermissions)
    val mediaSource: MediaSource = FakeMediaSource(appCatalog)
    private val fakePcLink = FakePcLink()
    val pcLink: PcLink = fakePcLink
    private val fakeAppEvents = FakeAppEvents()
    val appEvents: AppEvents = fakeAppEvents

    /** 내 기기와 이번만 연결한 기기(DV). 기기를 이번만 연결하면 토스트("…와 연결됐어요")로 알리려고 이벤트와 이어 둔다. */
    private val fakeDevices = FakeDeviceRegistry(
        account = accountSession,
        onTemporaryConnected = { name -> fakeAppEvents.emit(AppEvent.PcConnected(name)) },
    )
    val deviceRegistry: DeviceRegistry = fakeDevices

    /** 기기 목록(DV-01)의 시연용 동작: 다음에 QR로 찍는 기기를 주인 없는 TV·남의 TV·공용 PC로 바꾸기. 실제 구현으로 바꾸면 빈 목록이 된다. */
    val deviceDebugActions: List<DebugAction> = fakeDevices.debugActions

    /** 사용자가 조작 대상을 바꾸면 토스트로 알리고, 토스트의 "되돌리기"는 조작 대상을 되돌린다. */
    private val fakeControlTarget = FakeControlTarget(
        pcLink = pcLink,
        onChanged = { name -> fakeAppEvents.emit(AppEvent.ControlTargetChanged(name)) },
    ).also { fakeAppEvents.undoControlTargetChangeHandler = it::undo }
    val controlTarget: ControlTargetSelector = fakeControlTarget

    /**
     * 주변 기기(DV-06), 방향으로 기기 고르기(DV-07), 원격 조작 리모컨(DV-08). 방향 고르기는 쓸 수 없는 기기를 골랐을 때 토스트로 알리고
     * PC를 고르면 조작 대상을 바꾸므로 이벤트·조작 대상 가짜 다음에 둔다.
     */
    val nearbyDevices: NearbyDevices = FakeNearbyDevices()
    val directionPicker: DirectionPicker = FakeDirectionPicker(
        registry = deviceRegistry,
        pcLink = pcLink,
        controlTarget = controlTarget,
        onUnavailable = { reason -> fakeAppEvents.emit(AppEvent.CommandFailed(reason)) },
    )
    val remoteControl: RemoteControl = FakeRemoteControl(registry = deviceRegistry)

    private val fakeNetwork = FakeNetworkStatus()
    val networkStatus: NetworkStatus = fakeNetwork

    val usageStats: UsageStats = FakeUsageStats(customGestures = fakeCustomGestures)

    /** 개발자 도구(ST-08)가 보여 주는 값. 디버그 빌드에서만 쓰인다. */
    val diagnostics: DiagnosticsSource = FakeDiagnosticsSource()
    val settingsStore: SettingsStore = FakeSettingsStore()
    val vibrationPlayer: VibrationPlayer = FakeVibrationPlayer()

    private val fakeGestureTrial = FakeGestureTrial()
    val gestureTrial: GestureTrial = fakeGestureTrial

    /** 첫 제스처 체험(ON-02)의 시연용 동작: 휙 인식시키기. 실제 구현으로 바꾸면 빈 목록이 된다. */
    val onboardingDebugActions: List<DebugAction> = fakeGestureTrial.debugActions

    private val fakePresentation = FakePresentationController(
        link = {
            // 연결된 PC의 이름·응답 시간을 그대로 쓴다. 연결이 끊겨 있으면 마지막으로 알려진 값이 없어 빈 값을 쓴다.
            val connected = pcLink.state.value as? PcConnectionState.Connected
            PresentationLink.Connected(connected?.deviceName.orEmpty(), connected?.latencyMs ?: 0)
        },
    )
    val presentationController: PresentationController = fakePresentation

    /** 에뮬레이터에서 상태를 바꿔 보는 시연용 동작. 실제 구현으로 바꾸면 빈 목록이 된다. */
    val presentationDebugActions: List<DebugAction> = fakePresentation.debugActions

    /** 발표 전에 PC로 미리 보내 두는 효과 파일들(PR-14). */
    val effectLibrary: EffectLibrary = FakeEffectLibrary()

    private val fakeGestureCatalog = FakeGestureCatalog()
    val gestureCatalog: GestureCatalog = fakeGestureCatalog

    /**
     * 홈 화면 제목을 길게 누르면 열리는 시연용 동작: 토스트 6가지, 연습 제안 시트, 인터넷 끊기, 홈 상태 확인 중·감지 멈춤,
     * 앱 목록·제스처 목록 불러오는 중, 나만의 제스처 동기화 대기, PC 연결 응답 없음·최근 PC 비움.
     * 실제 구현으로 바꾸면 빈 목록이 된다. (위에서 만든 가짜들을 쓰므로 이 값은 그 아래에 둔다.)
     */
    val homeDebugActions: List<DebugAction> =
        fakeAppEvents.debugActions + fakeNetwork.debugActions + fakeDetection.debugActions +
            fakeAppCatalog.debugActions + fakeGestureCatalog.debugActions + fakeCustomGestures.libraryDebugActions +
            fakePcLink.debugActions

    /** 녹화 화면에서 상태를 바꿔 보는 시연용 동작. 실제 구현으로 바꾸면 빈 목록이 된다. */
    val recordingDebugActions: List<DebugAction> = fakeCustomGestures.debugActions
    private val fakePractice = FakePracticeSession()
    val practiceSession: PracticeSession = fakePractice
    val practiceDebugActions: List<DebugAction> = fakePractice.debugActions

    /** QR 스캔 화면의 카메라(B가 실제 카메라와 QR 인식으로 바꾼다). 가짜는 인식 영역을 누르면 "읽은" 것으로 친다. */
    val qrCamera: QrCameraContent = { onScanned -> FakeQrCamera(onScanned) }
}
