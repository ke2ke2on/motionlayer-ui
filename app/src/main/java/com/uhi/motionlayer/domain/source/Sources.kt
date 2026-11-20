package com.uhi.motionlayer.domain.source

import com.uhi.motionlayer.domain.model.Account
import com.uhi.motionlayer.domain.model.AppEvent
import com.uhi.motionlayer.domain.model.AppGestureSettings
import com.uhi.motionlayer.domain.model.AppPermission
import com.uhi.motionlayer.domain.model.BasicGesture
import com.uhi.motionlayer.domain.model.ControlTarget
import com.uhi.motionlayer.domain.model.ControllableApp
import com.uhi.motionlayer.domain.model.CustomGesture
import com.uhi.motionlayer.domain.model.DailyStats
import com.uhi.motionlayer.domain.model.DetectionStatus
import com.uhi.motionlayer.domain.model.DiagnosticsSnapshot
import com.uhi.motionlayer.domain.model.DirectionCandidate
import com.uhi.motionlayer.domain.model.DirectionPick
import com.uhi.motionlayer.domain.model.GestureCommand
import com.uhi.motionlayer.domain.model.GestureDetail
import com.uhi.motionlayer.domain.model.GestureSummary
import com.uhi.motionlayer.domain.model.GestureType
import com.uhi.motionlayer.domain.model.MyDevice
import com.uhi.motionlayer.domain.model.MyDeviceDetail
import com.uhi.motionlayer.domain.model.NearbyDevice
import com.uhi.motionlayer.domain.model.PairingResult
import com.uhi.motionlayer.domain.model.PairingTarget
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.PermissionStatus
import com.uhi.motionlayer.domain.model.PlayingApp
import com.uhi.motionlayer.domain.model.PracticeState
import com.uhi.motionlayer.domain.model.PresentationEffect
import com.uhi.motionlayer.domain.model.PresentationSetup
import com.uhi.motionlayer.domain.model.PresentationState
import com.uhi.motionlayer.domain.model.PresentationTool
import com.uhi.motionlayer.domain.model.PresentationSummary
import com.uhi.motionlayer.domain.model.RecentPc
import com.uhi.motionlayer.domain.model.RecordingState
import com.uhi.motionlayer.domain.model.RemoteKey
import com.uhi.motionlayer.domain.model.RemoteSession
import com.uhi.motionlayer.domain.model.ReportRange
import com.uhi.motionlayer.domain.model.ScannedDevice
import com.uhi.motionlayer.domain.model.SignInResult
import com.uhi.motionlayer.domain.model.TemporaryDevice
import com.uhi.motionlayer.domain.model.UsageReport
import com.uhi.motionlayer.domain.model.VibrationFeedback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/*
 * 화면이 의존하는 "계약". 화면은 이 인터페이스만 알고, 실제 구현은 팀원이 만든다.
 * 지금은 data/fake 의 가짜 구현이 들어 있고, 실제 구현이 생기면 AppContainer 한 곳에서 바꿔 끼운다.
 *
 *  - DetectionController : 제스처 인식 담당(A)
 *  - DiagnosticsSource : 제스처 인식(A)과 시스템 연동(B)이 함께
 *  - MediaSource, AppCatalog, SystemPermissions, NetworkStatus, UsageStats : 시스템 연동 담당(B)
 *  - PcLink : PC 연결 담당(C)
 *  - AccountSession, DeviceRegistry : 계정·서버가 필요한 부분이라 C가 (Google 로그인 창은 B와 함께)
 *  - ControlTargetSelector : 연결(C)과 시스템 연동(B)이 함께
 *  - AppEvents : 일에 따라 A·B·C가 함께 채운다
 */

/** 제스처 감지 켜기·끄기와 현재 상태. */
interface DetectionController {
    /** 사용자가 감지를 켜 두었는지. */
    val enabled: StateFlow<Boolean>

    /**
     * 감지 서비스가 실제로 어떤 상태인지(HM-07·08). [enabled]가 켜져 있어도 [DetectionStatus.Stopped]일 수 있다.
     * 멈춘 서비스는 [setEnabled]로 다시 켠다.
     */
    val status: StateFlow<DetectionStatus>

    /** 켜면 서비스를 (다시) 시작하고, 끄면 멈춘다. */
    fun setEnabled(enabled: Boolean)
}

/**
 * 토스트로 알려 줄 일들(SY-03). 화면이 켜져 있는 동안에만 받아서 보여 주고, 꺼져 있을 때 생긴 일은 버린다.
 * 제스처 실행·되돌림·실패는 A·B가, PC 연결과 조작 대상 바뀜은 C가 알린다.
 */
interface AppEvents {
    val events: Flow<AppEvent>

    /** 조작 대상이 바뀐 것을 되돌린다(토스트의 "되돌리기"를 눌렀다). */
    fun undoControlTargetChange()
}

/** 인터넷에 연결돼 있는지(SY-06). 확인은 시스템 연동(B)이 한다. 폰 안 기능과 같은 와이파이 PC 연결은 인터넷 없이도 된다. */
interface NetworkStatus {
    val online: StateFlow<Boolean>
}

/**
 * 앱이 쓰는 안드로이드 권한·설정(ON-05·07·09, AP-05, ST-07). 확인과 요청은 시스템 연동(B)이 한다.
 * 화면은 값을 읽고 요청을 부탁할 뿐이고, 시스템 허용 창과 설정 앱은 구현이 띄운다.
 */
interface SystemPermissions {
    /**
     * 권한마다 지금 상태. [AppPermission]의 모든 값이 들어 있다. 설정 앱에서 바꾸고 돌아오면 이 값이 바뀐다.
     * 알림이 허용돼야 "감지 중" 알림이 보이고(안드로이드 13 이상), 알림 접근이 허용돼야 재생 중인 앱을 알 수 있다.
     */
    val statuses: StateFlow<Map<AppPermission, PermissionStatus>>

    /**
     * [permission]을 요청한다. 시스템 허용 창으로 물을 수 있는 권한(알림·카메라)은 사용자가 답할 때까지 기다리고
     * 결과를 [statuses]로 알린다. 설정 앱에서만 바꿀 수 있는 권한(알림 접근·배터리)은 설정 앱의 그 화면을 열고 바로 돌아온다.
     */
    suspend fun request(permission: AppPermission)

    /** 설정 앱의 [permission] 화면을 연다(이미 허용했거나 거부한 권한을 바꿀 때). 사용자가 바꾸고 돌아오면 [statuses]가 바뀐다. */
    fun openSettings(permission: AppPermission)
}

/**
 * 개발자 도구(ST-08)가 보여 줄 값. 디버그 빌드에서만 쓰인다. 센서·지연·판정 기준은 제스처 인식(A)이,
 * 배터리는 시스템 연동(B)이 채운다.
 */
interface DiagnosticsSource {
    /** 값이 바뀔 때마다 새 묶음이 온다. 센서 원시값은 자주(초당 여러 번) 바뀔 수 있다. */
    val snapshot: StateFlow<DiagnosticsSnapshot>
}

/**
 * 제스처 명령을 보내는 곳(HM-06). PC가 연결되면 PC로 바뀌고 끊기면 폰으로 돌아오는 것은 구현이 하고,
 * 사용자가 홈에서 고르면 그곳이 된다. PC 연결(C)과 시스템 연동(B)이 함께 채운다.
 */
interface ControlTargetSelector {
    val selected: StateFlow<ControlTarget>

    /** 사용자가 [target]을 골랐다. */
    fun select(target: ControlTarget)
}

/** 지금 재생 중인 앱. 없으면 null. */
interface MediaSource {
    val playingApp: StateFlow<PlayingApp?>
}

/**
 * 앱 목록(AP-01). 어떤 앱을 제스처로 조작할지 고른다. 설치된 앱을 찾는 일은 시스템 연동(B)이 한다.
 * 전화는 목록에 넣지 않는다(전화 제어 AP-04는 이번 범위 밖).
 */
interface AppCatalog {
    /** 제스처로 조작할 수 있는 앱 전부(켜 둔 앱 포함). 보여 줄 순서는 구현이 정한다. */
    val apps: StateFlow<List<ControllableApp>>

    /**
     * 설치된 앱과 재생 중인 미디어를 읽는 중인지(AP-07). 앱 탭을 열 때 목표 1초 안에 끝난다(분석서 표 4-3).
     * true인 동안 화면은 목록 자리에 자리 표시 막대를 보여 주고, 끝나는 대로 [apps]로 바뀐다.
     */
    val loading: StateFlow<Boolean>

    fun setEnabled(appId: String, enabled: Boolean)

    /** 앱 하나의 제스처별 동작(AP-02). */
    fun gestureSettings(appId: String): StateFlow<AppGestureSettings>

    /** 이 앱에서 [gesture]에 고를 수 있는 동작(AP-03). "사용 안 함"은 들어 있지 않고 화면이 맨 끝에 붙인다. */
    fun commandOptions(appId: String, gesture: GestureType): List<GestureCommand>

    /** [gesture]가 [command]를 하게 한다. null이면 이 앱에서 그 제스처를 쓰지 않는다. */
    fun setCommand(appId: String, gesture: GestureType, command: GestureCommand?)

    /**
     * 이 앱의 제스처별 동작을 기본값으로 되돌린다. 기본 제스처는 앱 종류에 맞는 기본 동작으로,
     * 나만의 제스처는 이 앱에서 "사용 안 함"으로 돌아간다(Figma AP-02의 기본 모습).
     */
    fun resetGestureSettings(appId: String)

    /**
     * 나만의 제스처를 앱마다 어떤 동작에 연결했는지(AP-02, GS-11 "쓰는 곳"). 앱 id → (제스처 id → 동작).
     * 연결하지 않은 앱·제스처는 들어 있지 않다. 제스처를 지우면([CustomGestureLibrary.delete]) 그 연결도 사라진다.
     */
    val customGestureCommands: StateFlow<Map<String, Map<String, GestureCommand>>>

    /** 이 앱에서 나만의 제스처에 고를 수 있는 동작(AP-03). "사용 안 함"은 들어 있지 않고 화면이 맨 끝에 붙인다. */
    fun customCommandOptions(appId: String): List<GestureCommand>

    /** 나만의 제스처 [gestureId]가 이 앱에서 [command]를 하게 한다. null이면 이 앱에서 그 제스처를 쓰지 않는다. */
    fun setCustomCommand(appId: String, gestureId: String, command: GestureCommand?)
}

/**
 * 전화 제어(AP-04): 전화가 올 때 폰을 꺼내지 않고 제스처로 받거나 거절한다. 어떤 제스처가 무엇을 하는지는 정해져 있다
 * (오른쪽 휙 받기 · 세게 흔들기 거절 · 두 번 톡톡 벨소리 끄기, 통화 중에는 두 번 톡톡 끊기). 시스템 연동(B)과 제스처(A)가 채운다.
 */
interface PhoneControl {
    /** 전화 제어가 켜져 있는지. 전화 권한이 허용돼 있어야 켜진다. 권한이 거두어지면 꺼진 것으로 본다. */
    val enabled: StateFlow<Boolean>

    /**
     * 켜거나 끈다. 켜려면 전화 권한이 필요해서(NFR-07) 아직 허용되지 않았으면 권한을 요청하고, 허용돼야 켜진다.
     * 사용자가 허용하지 않으면 켜지지 않는다.
     */
    suspend fun setEnabled(enabled: Boolean)
}

/**
 * PC 연결. 연결 방법·규격은 C가 정하고, 화면은 결과만 받는다.
 *
 * 분석서 4.3: QR 인식·6자리 입력 → 연결 결과는 3초 안에 보여 줘야 한다. 3초가 넘으면 진행률과 취소가 필요하다.
 */
interface PcLink {
    val state: StateFlow<PcConnectionState>

    /** PR-01 "최근 연결한 PC". 최근에 연결한 것이 먼저 온다. */
    val recentPcs: StateFlow<List<RecentPc>>

    /**
     * [target]으로 연결을 시도하고 결과를 돌려준다. 성공하면 [state]도 Connected로 바뀐다.
     * 호출한 쪽이 코루틴을 취소하면 연결 시도도 멈춰야 한다.
     */
    suspend fun connect(target: PairingTarget): PairingResult

    fun disconnect()
}

/** 오늘 인식·되돌림 횟수(홈)와 기간별 기록 리포트(ST-03). */
interface UsageStats {
    val today: StateFlow<DailyStats>

    /** 기간별 리포트. 같은 기간을 다시 요청하면 같은 흐름을 돌려줘도 된다. */
    fun report(range: ReportRange): StateFlow<UsageReport>
}

/**
 * 설정 화면(ST-01·02)이 읽고 쓰는 사용자 설정. 폰에 저장한다(분석서 표 4-2: 폰 안 기능은 인터넷 없이 동작).
 * 저장과 실제 동작에 반영하는 것은 시스템 연동(B)과 제스처(A)가 한다.
 */
interface SettingsStore {
    /** 걷는 중에는 손 제스처를 멈춘다(주머니 두드림은 계속 동작). */
    val pauseGesturesWhileWalking: StateFlow<Boolean>

    /** 주머니 위를 두드려 조작하는 제스처(FR-09). */
    val pocketGestureEnabled: StateFlow<Boolean>

    /** 진동 세기. 0.0(약하게)~1.0(강하게). */
    val vibrationStrength: StateFlow<Float>

    /** 첫 실행 안내(ON-01~11)를 마쳤는지. 마치기 전에는 앱을 켜면 안내가 먼저 나온다. */
    val onboardingCompleted: StateFlow<Boolean>

    fun setPauseGesturesWhileWalking(enabled: Boolean)

    fun setPocketGestureEnabled(enabled: Boolean)

    fun setVibrationStrength(strength: Float)

    fun setOnboardingCompleted(completed: Boolean)
}

/**
 * 첫 제스처 체험(ON-02). 권한 없이 센서만으로 "오른쪽 휙"이 인식되길 기다린다. 제스처 인식(A)이 채운다.
 * 체험 중의 제스처는 체험으로만 쓰고 실제 앱에 명령을 보내지 않는다(연습 모드와 같다).
 */
interface GestureTrial {
    /** 체험하는 동안 "오른쪽 휙"이 인식될 때마다 1씩 늘어난다. 화면은 시작할 때의 값과 비교해 새로 인식된 것을 알아챈다. */
    val swipeCount: StateFlow<Int>

    /** 체험을 시작한다. 화면이 떠 있는 동안만 센서를 듣는다. */
    fun start()

    /** 체험을 마쳤다. 센서 듣기를 멈춘다. */
    fun stop()
}

/** 진동을 한 번 울려 보여 준다(ST-02 "느껴 보기"). 박자와 세기는 구현이 정한다. */
interface VibrationPlayer {
    fun play(feedback: VibrationFeedback)
}

/**
 * 만들어 둔 나만의 제스처들(GS-01 목록, GS-11 상세). 제스처 인식(A)이 채운다.
 * 새로 만드는 일은 [GestureRecorder]가 하고, 저장이 끝나면 여기 목록에 들어온다.
 */
interface CustomGestureLibrary {
    /** 만든 순서대로. 지우면 목록에서 빠진다. */
    val gestures: StateFlow<List<CustomGesture>>

    /** 감도를 바꾼다. 0.0(둔감하게)~1.0(민감하게). */
    fun setSensitivity(id: String, sensitivity: Float)

    /** 제스처를 지운다. 이 제스처로 하던 앱별 동작도 함께 사라진다. */
    fun delete(id: String)
}

/**
 * 나만의 제스처를 만드는 녹화·확인(GS-03~08). 제스처 인식(A)이 센서로 동작을 알아본다.
 * 녹화·확인 중에는 **실제 앱에 명령을 보내지 않는다**(GS-04 안내문 "등록 중에는 실제 앱을 제어하지 않아요").
 */
interface GestureRecorder {
    /** 만드는 중이 아니면 null. */
    val state: StateFlow<RecordingState?>

    /** 새 제스처를 [name]으로 만들기 시작한다. [replacingId]가 있으면 그 제스처를 같은 이름으로 다시 녹화한다(GS-11 "다시 녹화하기"). */
    fun start(name: String, replacingId: String? = null)

    /** "이번 동작 다시 하기"(GS-05). 저장하지 않은 동작은 버리고 같은 자리에서 다시 녹화한다. */
    fun retryTake()

    /** "처음부터 다시"(GS-04·07)·"다른 동작으로 다시 녹화"(GS-06). 이름은 그대로 두고 녹화를 처음부터 한다. */
    fun restart()

    /**
     * 만들기를 그만둔다(닫기·취소). 저장하지 않은 것은 버리고 [state]를 null로 되돌린다.
     * 저장을 마친 뒤 화면을 떠날 때도 불러 상태를 비운다(이미 저장된 제스처는 [CustomGestureLibrary]에 그대로 남는다).
     */
    fun cancel()
}

/**
 * 발표 전에 PC로 미리 보내 두는 효과 파일들(PR-14, PR-06의 "미리 보낸 효과"). PC 연결(C)이 채운다.
 * 발표 중에는 파일을 보내지 않는다.
 */
interface EffectLibrary {
    /** 지금 연결한 PC로 보냈거나 보내는 중인 효과들. 먼저 보낸 것이 위에 온다. */
    val effects: StateFlow<List<PresentationEffect>>

    /**
     * 효과로 쓸 파일을 골라 PC로 미리 보낸다. 파일을 고르는 창은 시스템이 띄우고,
     * 보내는 동안 [effects]의 진행률(`uploadPercent`)이 오른다.
     */
    fun add()
}

/** 기본 제스처 목록과 제스처별 성공률(GS-01), 제스처 하나의 자세한 값과 감도(GS-02). 제스처 인식(A)이 센다. */
interface GestureCatalog {
    val basicGestures: StateFlow<List<GestureSummary>>

    /**
     * 제스처별 최근 7일 인식률을 기록에서 모으는 중인지(GS-16). 제스처 탭을 열 때 목표 1초 안에 끝난다(분석서 표 4-3).
     * true인 동안 화면은 목록 자리에 자리 표시 줄을 보여 주고, 끝나는 대로 [basicGestures]로 바뀐다.
     */
    val loading: StateFlow<Boolean>

    /** 제스처 하나의 최근 7일 인식률·되돌림 횟수와 감도. */
    fun detail(gesture: BasicGesture): StateFlow<GestureDetail>

    /** 감도를 바꾼다. 0.0(둔감하게)~1.0(민감하게). */
    fun setSensitivity(gesture: BasicGesture, sensitivity: Float)
}

/**
 * 연습 모드(GS-09·10). 제스처 인식(A)이 만든다.
 *
 * 연습 중에는 **실제 앱에 명령을 보내지 않는다**(FR-08-1: 미디어 명령 0건). 그 약속은 [enter]~[leave] 사이에 구현이 지킨다.
 */
interface PracticeSession {
    val state: StateFlow<PracticeState>

    /** 연습 모드에 들어왔다. 이 동안의 제스처는 연습으로만 센다. */
    fun enter()

    /** 연습 모드를 나갔다. */
    fun leave()

    /** 연습할 제스처를 바꾼다. */
    fun select(gesture: BasicGesture)
}

/**
 * 발표 한 번의 진행(PR-07~13). 제스처 인식(A)과 PC 연결(C)이 함께 만든다.
 *
 * 화면이 하는 일은 둘뿐이다: 손가락이 화면에 닿았는지 알려 주기, 상태를 받아 그리기.
 * 손을 댄 채일 때만 제스처가 슬라이드를 넘기고, 뗀 채의 움직임은 버려진다(FR-03-2·03-3).
 */
interface PresentationController {
    /** 발표 중이 아니면 null. */
    val state: StateFlow<PresentationState?>

    /** 발표를 시작한다. 연결된 PC가 있어야 한다. */
    fun start(setup: PresentationSetup)

    /** 손가락이 발표 화면의 터치 영역에 닿았는지(true)·뗐는지(false). */
    fun setTouching(touching: Boolean)

    /** 도구를 고른다. 이미 켜진 도구를 다시 고르면 끈다. 켜고 끈 결과는 [state]의 `tool`로 알린다(PR-09). */
    fun selectTool(tool: PresentationTool)

    /** 발표를 끝내고 기록을 돌려준다. 발표 중이 아니었다면 null. */
    fun end(): PresentationSummary?
}

/**
 * 계정(ON-10, ST-01·05). 기기 등록과 제스처 동기화를 쓰려는 사람만 로그인한다.
 * 로그인은 선택이라 로그인하지 않아도 이 폰 안의 기능과 "이번만 연결"은 모두 쓸 수 있다.
 * Google 계정을 고르는 시스템 창은 구현이 띄우고, 서버와 주고받는 일은 C가 한다.
 *
 * 계정을 지우는 일(ST-06 계정 삭제)은 이번 범위 밖이라 두지 않았다.
 */
interface AccountSession {
    /** 로그인한 계정. 로그인하지 않았으면 null. */
    val account: StateFlow<Account?>

    /** Google 계정으로 로그인한다. 끝나면 결과를 돌려준다. 시스템 창이 떠 있는 동안 기다린다. */
    suspend fun signIn(): SignInResult

    /** 로그아웃한다. 이 폰의 설정은 그대로 남는다(ST-05). */
    fun signOut()
}

/**
 * 기기 목록과 내 기기 관리(DV-01~05). 내 기기는 계정에 저장되고 설정·연결된 사람은 서버를 거치므로 C가 채운다.
 * 이번만 연결하는 PC는 [PcLink]가 따로 맡고, 여기에는 TV 같은 그 밖의 기기가 든다.
 * 설정의 유일한 창구는 폰이다(TV·PC 쪽에는 입력 화면이 없다. FR-D-01).
 */
interface DeviceRegistry {
    /** 등록한 기기. 로그인하지 않았으면 비어 있다. */
    val myDevices: StateFlow<List<MyDevice>>

    /** 등록하지 않고 이번만 연결한 기기(PC 제외). 연결을 끊으면 사라진다. */
    val temporaryDevices: StateFlow<List<TemporaryDevice>>

    /** 내 기기 하나의 자세한 값(DV-04). 없는 기기(이미 삭제 등)면 null. */
    fun detail(deviceId: String): StateFlow<MyDeviceDetail?>

    /** 주변 사람이 이 기기를 찾아 손님으로 연결할 수 있게 할지. */
    fun setAllowGuests(deviceId: String, allowed: Boolean)

    /** 여러 사람이 함께 조작할 수 있게 할지. 끄면 한 사람만 조작한다. */
    fun setAllowSimultaneous(deviceId: String, allowed: Boolean)

    /** 집 밖에서 서버를 거쳐 원격으로 조작할 수 있게 할지. */
    fun setAllowRemote(deviceId: String, allowed: Boolean)

    /** 연결된 사람 한 명의 연결을 끊는다. 해제는 즉시이고, 손님에게 알림이 간다(DV-05). */
    fun disconnectPerson(deviceId: String, personId: String)

    /** 조작 권한을 이 폰으로 되찾는다("조작 권한 되찾기"). */
    fun reclaimControl(deviceId: String)

    /** 내 기기에서 지운다. 기기는 다음에 QR을 새로 찍어야 연결된다. */
    fun remove(deviceId: String)

    /** QR(코드)로 찍은 기기가 어떤 기기인지 알아본다. 주인이 있는지에 따라 이어지는 화면이 갈린다(DV-03). */
    suspend fun identify(qrToken: String): ScannedDevice

    /** 찍은 기기를 내 기기로 등록한다. 로그인해 있어야 한다. */
    suspend fun register(device: ScannedDevice)

    /** 찍은 기기를 등록하지 않고 이번만 연결한다. */
    suspend fun connectTemporarily(device: ScannedDevice)
}

/**
 * 주변에서 찾을 수 있는 다른 사람의 기기(DV-06). 같은 와이파이에서 찾고, 주인이 손님 연결을 허용한 기기만 나온다.
 * 화면이 [devices]를 구독하는 동안에만 찾고, 구독을 멈추면 찾기를 멈춘다. C가 채운다.
 *
 * 하나를 고르면 그 기기에 QR이 뜨고, 그 QR을 찍으면 이번만(손님으로) 연결된다. 그 자리에 있었다는 증명이다(design §8).
 */
interface NearbyDevices {
    val devices: StateFlow<List<NearbyDevice>>
}

/**
 * 폰이 향한 방향으로 기기를 고르기(DV-07, 분석서 FR-S-05). 방향 센서 값으로 후보를 **강조만** 하고 고르는 것은 사용자가 한다
 * (실내에서는 자기장 간섭으로 방향이 틀릴 수 있다). 제스처 인식(A)이 센서로, 연결(C)이 기기 목록으로 채운다.
 */
interface DirectionPicker {
    /** 방향으로 고를 수 있는 기기들. 같은 와이파이에 있는 내 기기와 연결된 PC이다. */
    val candidates: StateFlow<List<DirectionCandidate>>

    /** 지금 폰이 향한 후보의 id. 없으면 null. */
    val facingId: StateFlow<String?>

    /** 이름을 눌러(또는 폰을 가볍게 흔들어) 강조를 바꾼다. */
    fun face(deviceId: String)

    /** 강조된 기기를 골랐다. 이어서 무엇을 할지 돌려준다. 쓸 수 없는 기기면 이유를 토스트로 알린다. */
    fun pick(deviceId: String): DirectionPick
}

/**
 * 내 기기를 원격으로 조작하는 리모컨(DV-08). 서버가 중계하고(분석서 FR-S-06) 대상 화면에 조작하는 사람의 이름이 뜬다. C가 채운다.
 */
interface RemoteControl {
    /** [deviceId] 기기의 원격 조작 연결 상태. 조작할 수 없는 기기(없음·꺼짐)면 null. 화면이 구독하는 동안 연결을 유지한다. */
    fun session(deviceId: String): StateFlow<RemoteSession?>

    /** 리모컨 버튼을 눌렀다. */
    fun press(deviceId: String, key: RemoteKey)
}
