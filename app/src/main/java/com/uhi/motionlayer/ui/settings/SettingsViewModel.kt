package com.uhi.motionlayer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.Account
import com.uhi.motionlayer.domain.model.VibrationFeedback
import com.uhi.motionlayer.domain.source.AccountSession
import com.uhi.motionlayer.domain.source.SettingsStore
import com.uhi.motionlayer.domain.source.VibrationPlayer
import kotlinx.coroutines.flow.StateFlow

/** 설정(ST-01)과 진동(ST-02) 화면이 함께 쓰는 ViewModel. 값은 [SettingsStore]에 있고 여기서는 전달만 한다. */
class SettingsViewModel(
    private val store: SettingsStore,
    private val vibration: VibrationPlayer,
    session: AccountSession,
) : ViewModel() {
    /** 로그인한 계정. 로그인하지 않았으면 null(ST-01 맨 위 줄이 "로그인"으로 바뀐다). */
    val account: StateFlow<Account?> = session.account
    val pauseGesturesWhileWalking: StateFlow<Boolean> = store.pauseGesturesWhileWalking
    val pocketGestureEnabled: StateFlow<Boolean> = store.pocketGestureEnabled
    val vibrationStrength: StateFlow<Float> = store.vibrationStrength

    fun setPauseGesturesWhileWalking(enabled: Boolean) = store.setPauseGesturesWhileWalking(enabled)

    fun setPocketGestureEnabled(enabled: Boolean) = store.setPocketGestureEnabled(enabled)

    fun setVibrationStrength(strength: Float) = store.setVibrationStrength(strength)

    fun feel(feedback: VibrationFeedback) = vibration.play(feedback)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    store = AppContainer.settingsStore,
                    vibration = AppContainer.vibrationPlayer,
                    session = AppContainer.accountSession,
                )
            }
        }
    }
}
