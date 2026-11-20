package com.uhi.motionlayer.data.fake

import com.uhi.motionlayer.domain.model.AppPermission
import com.uhi.motionlayer.domain.model.isGranted
import com.uhi.motionlayer.domain.source.PhoneControl
import com.uhi.motionlayer.domain.source.SystemPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * 가짜 전화 제어. 처음에는 꺼져 있고, 켜려고 하면 [permissions]에 전화 권한을 요청한다(가짜는 곧 허용한다).
 * 켠 뒤에 권한 목록(ST-07)에서 전화를 "허용 안 됨"으로 바꾸면 꺼진 것으로 보인다.
 * 실제로 전화를 받거나 끊는 일은 하지 않는다.
 */
class FakePhoneControl(
    private val permissions: SystemPermissions,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : PhoneControl {
    /** 사용자가 스위치를 켜 둔 상태. 실제로 켜졌는지는 권한과 함께 [enabled]가 정한다. */
    private val switchedOn = MutableStateFlow(false)

    override val enabled: StateFlow<Boolean> = combine(switchedOn, permissions.statuses) { on, statuses ->
        on && statuses.isGranted(AppPermission.Phone)
    }.stateIn(scope, SharingStarted.Eagerly, false)

    override suspend fun setEnabled(enabled: Boolean) {
        if (enabled && !permissions.statuses.value.isGranted(AppPermission.Phone)) {
            permissions.request(AppPermission.Phone)
        }
        // 허용되지 않았으면 켜지 않는다.
        switchedOn.value = enabled && permissions.statuses.value.isGranted(AppPermission.Phone)
    }
}
