package com.uhi.motionlayer.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.AppPermission
import com.uhi.motionlayer.domain.model.PermissionStatus
import com.uhi.motionlayer.domain.source.PhoneControl
import com.uhi.motionlayer.domain.source.SystemPermissions
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 전화 제어(AP-04)가 읽고 부탁하는 것. 스위치를 켜면 권한 요청은 [PhoneControl]이 한다. */
class PhoneControlViewModel(
    private val phone: PhoneControl,
    permissions: SystemPermissions,
) : ViewModel() {
    val enabled: StateFlow<Boolean> = phone.enabled

    /** 전화 권한의 지금 상태. 스위치 설명 줄이 이 값에 따라 바뀐다. */
    val permission: StateFlow<PermissionStatus> = permissions.statuses
        .map { it[AppPermission.Phone] ?: PermissionStatus.NotRequested }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = permissions.statuses.value[AppPermission.Phone] ?: PermissionStatus.NotRequested,
        )

    fun onToggle(enabled: Boolean) {
        viewModelScope.launch { phone.setEnabled(enabled) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                PhoneControlViewModel(phone = AppContainer.phoneControl, permissions = AppContainer.systemPermissions)
            }
        }
    }
}
