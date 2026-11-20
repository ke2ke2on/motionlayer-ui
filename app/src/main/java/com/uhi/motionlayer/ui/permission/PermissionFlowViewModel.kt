package com.uhi.motionlayer.ui.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.AppPermission
import com.uhi.motionlayer.domain.model.isGranted
import com.uhi.motionlayer.domain.source.DetectionController
import com.uhi.motionlayer.domain.source.SystemPermissions
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 권한 안내의 단계. 허용되지 않은 것만 이 순서대로 보여 준다. */
enum class PermissionStep { Notifications, NotificationAccess }

/** 앱 틀이 받아서 화면을 옮기는 신호. */
sealed interface PermissionFlowEvent {
    data class Show(val step: PermissionStep) : PermissionFlowEvent

    /** 흐름이 끝났다. 감지는 이미 켜졌고, 홈으로 돌아가면 된다. */
    data object Finished : PermissionFlowEvent
}

/**
 * 홈에서 감지를 켤 때의 권한 안내 흐름(ON-05 알림 → ON-09 알림 접근).
 * 아직 허용되지 않은 것만 안내하고, 끝나면 감지를 켠다. 안내에서 "나중에 할게요"를 눌러도 감지는 켠다.
 * 안내 화면에서 뒤로 가면 감지는 켜지 않고 그대로 끝난다.
 */
class PermissionFlowViewModel(
    private val detection: DetectionController,
    private val permissions: SystemPermissions,
) : ViewModel() {
    private val _events = Channel<PermissionFlowEvent>(Channel.BUFFERED)
    val events: Flow<PermissionFlowEvent> = _events.receiveAsFlow()

    /** 알림 접근이 허용됐는지. 안내 화면이 설정 앱에서 돌아온 것을 알아채는 데 쓴다. */
    val notificationAccessGranted: StateFlow<Boolean> = permissions.statuses
        .map { it.isGranted(AppPermission.NotificationAccess) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = permissions.statuses.value.isGranted(AppPermission.NotificationAccess),
        )

    private var requestingNotifications = false

    /** 홈에서 감지를 켜려 한다. 필요한 권한이 없으면 안내를 시작하고, 다 있으면 바로 켠다. */
    fun start() {
        val statuses = permissions.statuses.value
        when {
            !statuses.isGranted(AppPermission.Notifications) -> show(PermissionStep.Notifications)
            !statuses.isGranted(AppPermission.NotificationAccess) -> show(PermissionStep.NotificationAccess)
            else -> finish()
        }
    }

    /** ON-05 "알림 허용하기". 사용자가 답하면(허용이든 아니든) 다음 단계로 간다. */
    fun onAllowNotifications() {
        if (requestingNotifications) return
        requestingNotifications = true
        viewModelScope.launch {
            try {
                permissions.request(AppPermission.Notifications)
            } finally {
                requestingNotifications = false
            }
            afterNotifications()
        }
    }

    /** ON-05 "나중에 할게요". */
    fun onSkipNotifications() = afterNotifications()

    /** ON-09 "설정에서 허용하기". 돌아와서 허용된 것이 확인되면 [onNotificationAccessGranted]가 불린다. */
    fun onOpenNotificationAccessSettings() = permissions.openSettings(AppPermission.NotificationAccess)

    /** ON-09에서 알림 접근이 허용된 것이 확인됐다. */
    fun onNotificationAccessGranted() = finish()

    /** ON-09 "나중에 할게요". */
    fun onSkipNotificationAccess() = finish()

    private fun afterNotifications() {
        if (permissions.statuses.value.isGranted(AppPermission.NotificationAccess)) finish() else show(PermissionStep.NotificationAccess)
    }

    private fun show(step: PermissionStep) {
        _events.trySend(PermissionFlowEvent.Show(step))
    }

    private fun finish() {
        detection.setEnabled(true)
        _events.trySend(PermissionFlowEvent.Finished)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                PermissionFlowViewModel(
                    detection = AppContainer.detectionController,
                    permissions = AppContainer.systemPermissions,
                )
            }
        }
    }
}
