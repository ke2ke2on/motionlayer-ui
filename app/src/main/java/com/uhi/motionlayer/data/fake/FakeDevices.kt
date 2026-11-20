package com.uhi.motionlayer.data.fake

import com.uhi.motionlayer.domain.model.ConnectedPersonRole
import com.uhi.motionlayer.domain.model.ConnectionRoute
import com.uhi.motionlayer.domain.model.ControlTarget
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.domain.model.DeviceKind
import com.uhi.motionlayer.domain.model.DeviceOwnership
import com.uhi.motionlayer.domain.model.DirectionCandidate
import com.uhi.motionlayer.domain.model.DirectionPick
import com.uhi.motionlayer.domain.model.MyDevice
import com.uhi.motionlayer.domain.model.MyDeviceDetail
import com.uhi.motionlayer.domain.model.MyDeviceStatus
import com.uhi.motionlayer.domain.model.NearbyDevice
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.RemoteKey
import com.uhi.motionlayer.domain.model.RemoteSession
import com.uhi.motionlayer.domain.model.ScannedDevice
import com.uhi.motionlayer.domain.model.TemporaryDevice
import com.uhi.motionlayer.domain.source.AccountSession
import com.uhi.motionlayer.domain.source.ControlTargetSelector
import com.uhi.motionlayer.domain.source.DeviceRegistry
import com.uhi.motionlayer.domain.source.DirectionPicker
import com.uhi.motionlayer.domain.source.NearbyDevices
import com.uhi.motionlayer.domain.source.PcLink
import com.uhi.motionlayer.domain.source.RemoteControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * 가짜 기기 등록부. 메모리에만 든다(앱을 껐다 켜면 처음 값).
 * 내 기기는 로그인한 동안에만 보이고(Figma 예시의 기기 3대), 로그아웃하면 비워 보인다. 기기는 계정에 저장되기 때문이다.
 *
 * 기기를 QR로 찍으면 [identify]는 처음에는 "주인 없는 TV"를 알려 준다(어떻게 연결할지 묻는 시트 DV-03).
 * 남의 기기·공용 기기를 찍은 경우는 시연용 동작([debugActions])으로 바꿔 볼 수 있다.
 */
class FakeDeviceRegistry(
    account: AccountSession,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val onTemporaryConnected: (deviceName: String) -> Unit = {},
) : DeviceRegistry {
    private val devices = MutableStateFlow(SampleData.myDevices)
    private val details = MutableStateFlow(SampleData.deviceDetails)
    private val _temporary = MutableStateFlow<List<TemporaryDevice>>(emptyList())

    override val myDevices: StateFlow<List<MyDevice>> = combine(account.account, devices) { signedIn, list ->
        if (signedIn == null) emptyList() else list
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val temporaryDevices: StateFlow<List<TemporaryDevice>> = _temporary.asStateFlow()

    override fun detail(deviceId: String): StateFlow<MyDeviceDetail?> =
        details.map { it[deviceId] }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), details.value[deviceId])

    private fun updateDetail(deviceId: String, change: (MyDeviceDetail) -> MyDeviceDetail) {
        details.update { all -> all[deviceId]?.let { all + (deviceId to change(it)) } ?: all }
    }

    override fun setAllowGuests(deviceId: String, allowed: Boolean) =
        updateDetail(deviceId) { it.copy(allowGuests = allowed) }

    override fun setAllowSimultaneous(deviceId: String, allowed: Boolean) =
        updateDetail(deviceId) { it.copy(allowSimultaneous = allowed) }

    override fun setAllowRemote(deviceId: String, allowed: Boolean) =
        updateDetail(deviceId) { it.copy(allowRemote = allowed) }

    override fun disconnectPerson(deviceId: String, personId: String) =
        updateDetail(deviceId) { detail -> detail.copy(connectedPeople = detail.connectedPeople.filterNot { it.id == personId }) }

    /** 조작하는 사람을 이 폰의 주인(나)으로 되돌린다. */
    override fun reclaimControl(deviceId: String) = updateDetail(deviceId) { detail ->
        detail.copy(connectedPeople = detail.connectedPeople.map { it.copy(controlling = it.role == ConnectedPersonRole.Me) })
    }

    override fun remove(deviceId: String) {
        devices.update { list -> list.filterNot { it.id == deviceId } }
        details.update { it - deviceId }
    }

    /** 다음에 찍는 기기의 주인(시연용). 처음에는 주인이 없는 TV다. */
    private var nextOwnership = DeviceOwnership.Unowned

    override suspend fun identify(qrToken: String): ScannedDevice {
        delay(IDENTIFY_DELAY_MILLIS)
        return when (nextOwnership) {
            DeviceOwnership.Unowned -> ScannedDevice("bedroom_tv", "안방 TV", DeviceKind.Tv, DeviceOwnership.Unowned)
            DeviceOwnership.OwnedByOther -> ScannedDevice("jisu_tv", "지수네 TV", DeviceKind.Tv, DeviceOwnership.OwnedByOther)
            DeviceOwnership.Public -> ScannedDevice("study_pc", "스터디룸 PC", DeviceKind.Pc, DeviceOwnership.Public)
        }
    }

    override suspend fun register(device: ScannedDevice) {
        delay(REGISTER_DELAY_MILLIS)
        if (devices.value.any { it.id == device.id }) return
        val registered = MyDevice(
            id = device.id,
            name = device.name,
            kind = device.kind,
            platform = if (device.kind == DeviceKind.Tv) "Android TV" else "PC",
            status = MyDeviceStatus.Connected(latencyMs = 12),
        )
        devices.update { it + registered }
        details.update {
            it + (
                registered.id to MyDeviceDetail(
                    device = registered,
                    allowGuests = false,
                    allowSimultaneous = false,
                    allowRemote = false,
                    connectedPeople = listOf(SampleData.me),
                    selectVibrationLabel = "짧게",
                    registeredDirections = 0,
                )
                )
        }
    }

    override suspend fun connectTemporarily(device: ScannedDevice) {
        delay(REGISTER_DELAY_MILLIS)
        _temporary.update { list ->
            if (list.any { it.id == device.id }) list else list + TemporaryDevice(device.id, device.name, device.kind, "손님 · 같은 와이파이")
        }
        onTemporaryConnected(device.name)
    }

    /** 기기 목록(DV-01) 제목을 길게 누르면(디버그 빌드) 열리는 창에 나온다. */
    val debugActions: List<DebugAction> = listOf(
        DebugAction("기기 추가 · 다음에 찍는 기기 바꾸기 (주인 없는 TV → 남의 TV → 공용 PC)") {
            nextOwnership = DeviceOwnership.entries[(nextOwnership.ordinal + 1) % DeviceOwnership.entries.size]
        },
    )

    private companion object {
        const val IDENTIFY_DELAY_MILLIS = 600L
        const val REGISTER_DELAY_MILLIS = 400L
    }
}

/**
 * 가짜 주변 기기. 화면이 열려 구독하는 동안 같은 와이파이에서 찾는 척하다가 1.2초 뒤에 Figma의 두 기기(지수네 TV·스터디룸 PC)를
 * 찾는다. 화면을 닫았다 다시 열면 다시 찾는다.
 */
class FakeNearbyDevices(
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : NearbyDevices {
    override val devices: StateFlow<List<NearbyDevice>> = flow {
        emit(emptyList())
        delay(SEARCH_MILLIS)
        emit(SampleData.nearbyDevices)
    }.stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    private companion object {
        const val SEARCH_MILLIS = 1_200L
    }
}

/**
 * 가짜 방향 고르기. 방향 센서가 없으므로 후보의 방향은 정해 둔 값이다(Figma 배치: 거실 TV는 위쪽, 윤의 MacBook은 왼쪽 아래,
 * 강의실 PC는 오른쪽 아래). 후보는 내 기기 가운데 집 밖에서만 닿는 기기를 뺀 것과 연결된 PC이고, 처음에는 위쪽에 가장 가까운 것이 강조돼 있다.
 *
 * 고르면: 켜진 TV는 원격 조작 화면으로 가고, 꺼진 TV는 "기기가 꺼져 있어 실행하지 못했어요" 토스트, PC는 연결돼 있으면 조작 대상을
 * 그 PC로 바꾸고 아니면 "연결된 PC가 없어 실행하지 못했어요" 토스트다.
 */
class FakeDirectionPicker(
    private val registry: DeviceRegistry,
    private val pcLink: PcLink,
    private val controlTarget: ControlTargetSelector,
    private val onUnavailable: (reason: String) -> Unit,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : DirectionPicker {
    override val candidates: StateFlow<List<DirectionCandidate>> = combine(registry.myDevices, pcLink.state) { mine, pc ->
        val connected = (pc as? PcConnectionState.Connected)?.takeIf { c -> mine.none { it.name == c.deviceName } }
        val nearby = mine.filter { it.status !is MyDeviceStatus.RemoteReachable }
            .map { it.id to (it.name to it.kind) } + listOfNotNull(connected?.let { it.deviceName to (it.deviceName to DeviceKind.Pc) })
        nearby.mapIndexed { index, (id, nameAndKind) ->
            val (bearing, distance) = PRESET_POSITIONS[id] ?: FALLBACK_POSITIONS[index % FALLBACK_POSITIONS.size]
            DirectionCandidate(id, nameAndKind.first, nameAndKind.second, bearing, distance)
        }.sortedBy { minOf(it.bearingDegrees, 360f - it.bearingDegrees) }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val faced = MutableStateFlow<String?>(null)

    override val facingId: StateFlow<String?> = combine(candidates, faced) { list, chosen ->
        chosen?.takeIf { id -> list.any { it.id == id } } ?: list.firstOrNull()?.id
    }.stateIn(scope, SharingStarted.Eagerly, null)

    override fun face(deviceId: String) {
        faced.value = deviceId
    }

    override fun pick(deviceId: String): DirectionPick {
        val candidate = candidates.value.firstOrNull { it.id == deviceId } ?: return DirectionPick.Unavailable
        return when (candidate.kind) {
            DeviceKind.Tv -> {
                val status = registry.myDevices.value.firstOrNull { it.id == deviceId }?.status
                if (status is MyDeviceStatus.Off) {
                    onUnavailable("기기가 꺼져 있어")
                    DirectionPick.Unavailable
                } else {
                    DirectionPick.OpenRemote(deviceId)
                }
            }
            DeviceKind.Pc -> if (pcLink.state.value is PcConnectionState.Connected) {
                controlTarget.select(ControlTarget.Pc)
                DirectionPick.TargetChanged
            } else {
                onUnavailable("연결된 PC가 없어")
                DirectionPick.Unavailable
            }
        }
    }

    private companion object {
        /** 기기마다 정해 둔 자리(방향 °, 거리 비율). 거실 TV·윤의 MacBook·강의실 PC는 Figma DV-07 배치에서 잰 값이고 안방 TV는 직접 정했다. */
        val PRESET_POSITIONS = mapOf(
            "living_tv" to (359.5f to 0.76f),
            "macbook" to (224f to 0.73f),
            "강의실 PC" to (142f to 0.88f),
            "bedroom_tv" to (300f to 0.85f),
        )

        /** 위에 없는 기기(그때그때 연결한 PC 등)가 놓일 자리. */
        val FALLBACK_POSITIONS = listOf(60f to 0.8f, 190f to 0.82f, 270f to 0.9f)
    }
}

/**
 * 가짜 리모컨. 내 기기의 상태로 연결을 정한다: 꺼진 기기는 조작할 수 없고(null), 같은 와이파이로 연결된 기기는 로컬 응답 시간,
 * 집 밖에서만 닿는 기기는 서버 경유 180ms(Figma DV-08)다. 버튼을 눌러도 실제로 하는 일은 없다.
 */
class FakeRemoteControl(
    private val registry: DeviceRegistry,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : RemoteControl {
    override fun session(deviceId: String): StateFlow<RemoteSession?> =
        registry.myDevices.map { sessionOf(it, deviceId) }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), sessionOf(registry.myDevices.value, deviceId))

    override fun press(deviceId: String, key: RemoteKey) = Unit

    private fun sessionOf(devices: List<MyDevice>, deviceId: String): RemoteSession? =
        devices.firstOrNull { it.id == deviceId }?.let { device ->
            when (val status = device.status) {
                is MyDeviceStatus.Off -> null
                is MyDeviceStatus.Connected -> RemoteSession(device.name, ConnectionRoute.Local, status.latencyMs)
                is MyDeviceStatus.RemoteReachable -> RemoteSession(device.name, ConnectionRoute.Server, REMOTE_LATENCY_MILLIS)
            }
        }

    private companion object {
        const val REMOTE_LATENCY_MILLIS = 180
    }
}
