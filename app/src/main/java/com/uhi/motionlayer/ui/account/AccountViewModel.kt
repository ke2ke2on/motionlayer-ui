package com.uhi.motionlayer.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.Account
import com.uhi.motionlayer.domain.model.ControllableApp
import com.uhi.motionlayer.domain.model.CustomGesture
import com.uhi.motionlayer.domain.model.MyDevice
import com.uhi.motionlayer.domain.source.AccountSession
import com.uhi.motionlayer.domain.source.AppCatalog
import com.uhi.motionlayer.domain.source.CustomGestureLibrary
import com.uhi.motionlayer.domain.source.DeviceRegistry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn

/** 계정 화면(ST-05)이 보여 주는 값. "이 계정에 저장된 것" 세 줄의 개수까지 한 덩어리다. */
data class AccountUiState(
    val account: Account,
    val customGestureCount: Int,
    val deviceCount: Int,
    val appCount: Int,
)

/**
 * 계정 화면(ST-05)이 쓰는 ViewModel. 로그인한 계정과, 저장된 것의 개수(나만의 제스처·내 기기·켜 둔 앱)를
 * 각각의 목록에서 세어 올릴 뿐이다.
 */
class AccountViewModel(
    private val session: AccountSession,
    customGestures: CustomGestureLibrary,
    devices: DeviceRegistry,
    apps: AppCatalog,
) : ViewModel() {
    /** 로그아웃을 부른 뒤. 화면이 닫히는 동안 계정이 비며 숫자가 0으로 바뀌어 깜박이지 않게, 이때부터는 값을 갱신하지 않는다. */
    @Volatile
    private var signedOut = false

    /** 로그인하지 않았으면 값이 없다(이 화면은 로그인한 뒤에만 열린다). */
    val state: StateFlow<AccountUiState?> = combine(
        session.account,
        customGestures.gestures,
        devices.myDevices,
        apps.apps,
    ) { account, gestures, myDevices, installed -> stateOf(account, gestures, myDevices, installed) }
        .filter { !signedOut }
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = stateOf(
                session.account.value,
                customGestures.gestures.value,
                devices.myDevices.value,
                apps.apps.value,
            ),
        )

    /** 로그아웃한다. 이 폰의 설정은 그대로 남는다. */
    fun signOut() {
        signedOut = true
        session.signOut()
    }

    private fun stateOf(
        account: Account?,
        gestures: List<CustomGesture>,
        myDevices: List<MyDevice>,
        installed: List<ControllableApp>,
    ): AccountUiState? = account?.let {
        AccountUiState(
            account = it,
            customGestureCount = gestures.size,
            deviceCount = myDevices.size,
            appCount = installed.count { app -> app.enabled },
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                AccountViewModel(
                    session = AppContainer.accountSession,
                    customGestures = AppContainer.customGestureLibrary,
                    devices = AppContainer.deviceRegistry,
                    apps = AppContainer.appCatalog,
                )
            }
        }
    }
}
