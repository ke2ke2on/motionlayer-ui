package com.uhi.motionlayer.ui.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.AppPermission
import com.uhi.motionlayer.domain.model.isGranted
import com.uhi.motionlayer.domain.source.SystemPermissions
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 배터리 설정 안내(ON-07)가 읽고 부탁하는 것: 배터리 최적화에서 빠졌는지, 설정 앱 열기. */
class BatteryGuideViewModel(private val permissions: SystemPermissions) : ViewModel() {
    val applied: StateFlow<Boolean> = permissions.statuses
        .map { it.isGranted(AppPermission.BatteryOptimization) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = permissions.statuses.value.isGranted(AppPermission.BatteryOptimization),
        )

    fun onOpenSettings() = permissions.openSettings(AppPermission.BatteryOptimization)

    companion object {
        val Factory = viewModelFactory {
            initializer { BatteryGuideViewModel(permissions = AppContainer.systemPermissions) }
        }
    }
}
