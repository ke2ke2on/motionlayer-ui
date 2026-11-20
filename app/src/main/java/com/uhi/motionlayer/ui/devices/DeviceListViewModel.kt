package com.uhi.motionlayer.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.MyDevice
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.RecentPc
import com.uhi.motionlayer.domain.model.TemporaryDevice
import com.uhi.motionlayer.domain.source.AccountSession
import com.uhi.motionlayer.domain.source.DeviceRegistry
import com.uhi.motionlayer.domain.source.PcLink
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * 기기 목록(DV-01)이 보여 주는 값.
 *
 * @param connectedPc 지금 연결돼 있는 PC 가운데 내 기기가 아닌 것(이번만 연결). 내 기기인 PC는 "내 기기" 줄이 연결됨으로 보여 준다
 * @param recentPcs 전에 연결했던 PC 가운데 내 기기가 아닌 것. 다시 연결하는 줄이다
 */
data class DeviceListUiState(
    val signedIn: Boolean,
    val myDevices: List<MyDevice>,
    val connectedPc: PcConnectionState.Connected?,
    val recentPcs: List<RecentPc>,
    val temporaryDevices: List<TemporaryDevice>,
) {
    /** "이번만 연결" 구역에 보일 줄의 수. 0이면 구역 제목도 그리지 않는다. */
    val temporaryCount: Int
        get() = (if (connectedPc != null) 1 else 0) + recentPcs.size + temporaryDevices.size
}

/**
 * 내 기기(DV-01 "내 기기")와 이번만 연결한 기기(PC는 연결 기록, 그 밖의 기기는 등록부)를 한 화면에 맞춘다.
 * 같은 기기가 두 줄로 겹쳐 보이지 않게, 내 기기와 이름이 같은 PC는 "이번만 연결"에서 빼고, 지금 연결된 PC는
 * "전에 연결했던 PC" 줄에서도 뺀다(연결된 줄 하나로 보인다).
 */
internal fun deviceListState(
    signedIn: Boolean,
    myDevices: List<MyDevice>,
    temporaryDevices: List<TemporaryDevice>,
    pc: PcConnectionState,
    recentPcs: List<RecentPc>,
): DeviceListUiState {
    val mine = myDevices.map { it.name }.toSet()
    val connected = (pc as? PcConnectionState.Connected)?.takeIf { it.deviceName !in mine }
    return DeviceListUiState(
        signedIn = signedIn,
        myDevices = myDevices,
        connectedPc = connected,
        recentPcs = recentPcs.filter { it.name !in mine && it.name != connected?.deviceName },
        temporaryDevices = temporaryDevices,
    )
}

/** 기기 목록(DV-01)이 쓰는 ViewModel. 계정·기기 등록부·PC 연결에서 값을 모아 한 덩어리로 올린다. */
class DeviceListViewModel(
    account: AccountSession,
    registry: DeviceRegistry,
    pcLink: PcLink,
) : ViewModel() {
    val state: StateFlow<DeviceListUiState> = combine(
        account.account,
        registry.myDevices,
        registry.temporaryDevices,
        pcLink.state,
        pcLink.recentPcs,
    ) { signedIn, mine, temporary, pc, recent -> deviceListState(signedIn != null, mine, temporary, pc, recent) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = deviceListState(
                signedIn = account.account.value != null,
                myDevices = registry.myDevices.value,
                temporaryDevices = registry.temporaryDevices.value,
                pc = pcLink.state.value,
                recentPcs = pcLink.recentPcs.value,
            ),
        )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                DeviceListViewModel(
                    account = AppContainer.accountSession,
                    registry = AppContainer.deviceRegistry,
                    pcLink = AppContainer.pcLink,
                )
            }
        }
    }
}
