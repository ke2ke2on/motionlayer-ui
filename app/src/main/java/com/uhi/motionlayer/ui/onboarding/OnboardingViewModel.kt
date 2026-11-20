package com.uhi.motionlayer.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.AppPermission
import com.uhi.motionlayer.domain.model.ControllableApp
import com.uhi.motionlayer.domain.model.VibrationFeedback
import com.uhi.motionlayer.domain.model.isGranted
import com.uhi.motionlayer.domain.source.AccountSession
import com.uhi.motionlayer.domain.source.AppCatalog
import com.uhi.motionlayer.domain.source.DetectionController
import com.uhi.motionlayer.domain.source.GestureTrial
import com.uhi.motionlayer.domain.source.PhoneControl
import com.uhi.motionlayer.domain.source.SettingsStore
import com.uhi.motionlayer.domain.source.SystemPermissions
import com.uhi.motionlayer.domain.source.VibrationPlayer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 첫 실행 안내(ON-01~11) 전체가 함께 쓰는 ViewModel. 앱 전체에서 하나만 둔다.
 * 어느 단계 다음에 어느 단계가 오는지, 이미 허용된 권한 단계와 이미 한 로그인 단계는 건너뛰는 것,
 * 마칠 때 감지를 켜는 것을 맡는다. 화면을 옮기는 일은 내비게이션이 한다.
 */
class OnboardingViewModel(
    private val settings: SettingsStore,
    private val detection: DetectionController,
    private val permissions: SystemPermissions,
    private val catalog: AppCatalog,
    private val trial: GestureTrial,
    private val vibration: VibrationPlayer,
    private val phone: PhoneControl,
    private val account: AccountSession,
) : ViewModel() {
    /** 앱을 켤 때 첫 실행 안내부터 보여 줘야 하는지. 시작 화면을 정할 때 한 번 읽는다. */
    val needed: Boolean
        get() = !settings.onboardingCompleted.value

    /** [step] 다음에 보여 줄 단계. 이미 허용된 권한을 알려 주는 단계와 이미 로그인했을 때의 로그인 단계는 건너뛴다. */
    fun stepAfter(step: OnboardingStep): OnboardingStep =
        nextOnboardingStep(step, permissions.statuses.value, signedIn = account.account.value != null)

    // ON-02 첫 제스처 체험
    val trialSwipeCount: StateFlow<Int> = trial.swipeCount

    fun startTrial() = trial.start()

    fun stopTrial() = trial.stop()

    /** 체험에서 휙이 인식됐다. 짧게 한 번 진동으로 알려 준다. */
    fun onTrialRecognized() = vibration.play(VibrationFeedback.Recognized)

    // ON-05 알림 허용
    suspend fun allowNotifications() = permissions.request(AppPermission.Notifications)

    // ON-08 쓸 앱 고르기
    val apps: StateFlow<List<ControllableApp>> = catalog.apps

    fun toggleApp(appId: String, enabled: Boolean) = catalog.setEnabled(appId, enabled)

    /** ON-08 맨 아래 "전화" 줄. 앱 목록(AP-01)과 같은 값을 바꾸고, 켜면 전화 권한을 요청한다. */
    val phoneEnabled: StateFlow<Boolean> = phone.enabled

    fun togglePhone(enabled: Boolean) {
        viewModelScope.launch { phone.setEnabled(enabled) }
    }

    // ON-09 알림 접근
    val accessGranted: StateFlow<Boolean> = permissions.statuses
        .map { it.isGranted(AppPermission.NotificationAccess) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = permissions.statuses.value.isGranted(AppPermission.NotificationAccess),
        )

    fun openAccessSettings() = permissions.openSettings(AppPermission.NotificationAccess)

    /** 안내를 마친다. 감지를 켜고, 다음부터는 안내가 나오지 않게 한다. */
    fun finish() {
        detection.setEnabled(true)
        settings.setOnboardingCompleted(true)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                OnboardingViewModel(
                    settings = AppContainer.settingsStore,
                    detection = AppContainer.detectionController,
                    permissions = AppContainer.systemPermissions,
                    catalog = AppContainer.appCatalog,
                    trial = AppContainer.gestureTrial,
                    vibration = AppContainer.vibrationPlayer,
                    phone = AppContainer.phoneControl,
                    account = AppContainer.accountSession,
                )
            }
        }
    }
}
