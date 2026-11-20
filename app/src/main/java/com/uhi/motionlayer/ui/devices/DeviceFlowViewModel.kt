package com.uhi.motionlayer.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.DeviceOwnership
import com.uhi.motionlayer.domain.model.NearbyDevice
import com.uhi.motionlayer.domain.model.ScannedDevice
import com.uhi.motionlayer.domain.source.AccountSession
import com.uhi.motionlayer.domain.source.DeviceRegistry
import com.uhi.motionlayer.domain.source.NetworkStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 기기 추가 흐름(DV-02 → QR·코드 → DV-03, DV-06 → QR)이 함께 쓰는 ViewModel. 앱 전체에서 하나만 둔다.
 * QR(코드)로 찍은 기기를 알아보고, 주인이 없는 기기면 어떻게 연결할지 묻는 시트(DV-03)에 올릴 기기를 들고 있는다.
 * 어느 화면으로 갈지는 내비게이션이 정한다.
 */
class DeviceFlowViewModel(
    private val registry: DeviceRegistry,
    account: AccountSession,
    network: NetworkStatus,
) : ViewModel() {
    /** 로그인했는지. 로그인하지 않았으면 "내 기기로 등록"에 "로그인 필요"가 붙고 누르면 로그인으로 보낸다. */
    val signedIn: StateFlow<Boolean> = account.account
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, account.account.value != null)

    /** 인터넷이 연결돼 있는지. 끊겨 있으면 기기를 추가할 수 없어 "인터넷 필요"(DV-09)를 보여 준다. */
    val online: StateFlow<Boolean> = network.online

    private val _pending = MutableStateFlow<ScannedDevice?>(null)

    /** 방금 찍은 기기 가운데 어떻게 연결할지 아직 묻지 않은 것. 있으면 기기 목록 위에 시트(DV-03)가 뜬다. */
    val pending: StateFlow<ScannedDevice?> = _pending.asStateFlow()

    private val _addSheetRequested = MutableStateFlow(false)

    /** 기기 추가 방법 시트(DV-02)를 열어 달라는 요청. "인터넷 필요"(DV-09)에서 인터넷이 돌아와 목록으로 돌아올 때 켜진다. */
    val addSheetRequested: StateFlow<Boolean> = _addSheetRequested.asStateFlow()

    private val _scanDone = Channel<Unit>(Channel.BUFFERED)

    /** 찍은 기기를 알아봤다. 찍는 화면을 닫고 기기 목록으로 돌아가라는 신호다. */
    val scanDone: Flow<Unit> = _scanDone.receiveAsFlow()

    private var identifying: Job? = null

    /** 주변 기기(DV-06)에서 고른 기기. 있으면 다음에 읽는 QR은 이 기기에 손님으로 연결하는 데 쓴다. */
    private var nearbyTarget: NearbyDevice? = null

    /** 주변 기기에서 [device]를 골랐다. 이어서 그 기기에 뜬 QR을 찍으면 이번만 연결한다. */
    fun selectNearby(device: NearbyDevice) {
        nearbyTarget = device
    }

    /** 기기 추가 방법 시트에서 QR·코드를 골라 시작한다. 이전에 골랐던 주변 기기는 잊는다. */
    fun clearNearby() {
        nearbyTarget = null
    }

    /** 기기 추가 시트를 열어 달라고 요청한다. */
    fun requestAddSheet() {
        _addSheetRequested.value = true
    }

    /** 기기 목록이 시트를 열었다. */
    fun consumeAddSheetRequest() {
        _addSheetRequested.value = false
    }

    /**
     * QR 토큰이나 6자리 코드를 읽었다. 주변 기기를 고르고 온 것이면 그 기기에 손님으로 바로 연결한다.
     * 아니면 어떤 기기인지 알아본 뒤, 주인이 없으면 시트(DV-03)에 올리고 남의 기기·공용 기기면 시트 없이 바로 이번만 연결한다
     * (분석서 FR-D-05·04). 이미 알아보는 중이면 무시한다.
     */
    fun onScanned(token: String) {
        if (identifying?.isActive == true) return
        identifying = viewModelScope.launch {
            val nearby = nearbyTarget
            if (nearby != null) {
                nearbyTarget = null
                registry.connectTemporarily(
                    ScannedDevice(
                        id = nearby.id,
                        name = nearby.name,
                        kind = nearby.kind,
                        ownership = if (nearby.owner == null) DeviceOwnership.Public else DeviceOwnership.OwnedByOther,
                    ),
                )
            } else {
                val device = registry.identify(token)
                when (device.ownership) {
                    DeviceOwnership.Unowned -> _pending.value = device
                    DeviceOwnership.OwnedByOther, DeviceOwnership.Public -> registry.connectTemporarily(device)
                }
            }
            _scanDone.send(Unit)
        }
    }

    /** 시트를 그냥 닫았다. 기기는 연결하지도 등록하지도 않는다. */
    fun dismissPending() {
        _pending.value = null
    }

    /**
     * "내 기기로 등록"을 눌렀다. 로그인하지 않았으면 등록하지 않고 false를 돌려준다(화면이 로그인으로 보내고,
     * 기기는 그대로 들고 있어 로그인하고 돌아오면 시트가 다시 뜬다).
     */
    fun register(): Boolean {
        val device = _pending.value ?: return true
        if (!signedIn.value) return false
        _pending.value = null
        viewModelScope.launch { registry.register(device) }
        return true
    }

    /** "이번만 연결"을 눌렀다. 로그인 없이 바로 연결하고, 기기는 이 연결을 끊으면 사라진다. */
    fun connectOnce() {
        val device = _pending.value ?: return
        _pending.value = null
        viewModelScope.launch { registry.connectTemporarily(device) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                DeviceFlowViewModel(
                    registry = AppContainer.deviceRegistry,
                    account = AppContainer.accountSession,
                    network = AppContainer.networkStatus,
                )
            }
        }
    }
}
