package com.uhi.motionlayer.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.MyDeviceDetail
import com.uhi.motionlayer.domain.source.DeviceRegistry
import kotlinx.coroutines.flow.StateFlow

/** 내 기기 하나의 상세(DV-04)가 쓰는 ViewModel. 허용 설정을 바꾸고, 연결된 사람을 끊고, 기기를 지우는 부탁을 전달한다. */
class DeviceDetailViewModel(
    private val deviceId: String,
    private val registry: DeviceRegistry,
) : ViewModel() {
    /** 기기의 자세한 값. 없는 기기(지웠거나 처음부터 없음)면 null. */
    val detail: StateFlow<MyDeviceDetail?> = registry.detail(deviceId)

    fun setAllowGuests(allowed: Boolean) = registry.setAllowGuests(deviceId, allowed)

    fun setAllowSimultaneous(allowed: Boolean) = registry.setAllowSimultaneous(deviceId, allowed)

    fun setAllowRemote(allowed: Boolean) = registry.setAllowRemote(deviceId, allowed)

    fun disconnectPerson(personId: String) = registry.disconnectPerson(deviceId, personId)

    fun reclaimControl() = registry.reclaimControl(deviceId)

    fun remove() = registry.remove(deviceId)

    companion object {
        fun factory(deviceId: String) = viewModelFactory {
            initializer { DeviceDetailViewModel(deviceId = deviceId, registry = AppContainer.deviceRegistry) }
        }
    }
}
