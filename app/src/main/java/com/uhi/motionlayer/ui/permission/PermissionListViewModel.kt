package com.uhi.motionlayer.ui.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.AppPermission
import com.uhi.motionlayer.domain.model.PermissionStatus
import com.uhi.motionlayer.domain.source.SystemPermissions
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 권한 목록(ST-07)이 읽고 부탁하는 것. 값은 받아서 보여 주기만 한다. */
class PermissionListViewModel(private val permissions: SystemPermissions) : ViewModel() {
    val statuses: StateFlow<Map<AppPermission, PermissionStatus>> = permissions.statuses

    /** 한 줄을 눌렀다. 아직 묻지 않은 권한은 요청하고, 그 밖에는 설정 앱의 그 화면을 연다. */
    fun onOpen(permission: AppPermission) {
        if (permissions.statuses.value[permission] == PermissionStatus.NotRequested) {
            viewModelScope.launch { permissions.request(permission) }
        } else {
            permissions.openSettings(permission)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { PermissionListViewModel(permissions = AppContainer.systemPermissions) }
        }
    }
}
