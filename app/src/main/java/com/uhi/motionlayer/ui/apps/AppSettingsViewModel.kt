package com.uhi.motionlayer.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.ControllableApp
import com.uhi.motionlayer.domain.model.CustomGesture
import com.uhi.motionlayer.domain.model.GestureCommand
import com.uhi.motionlayer.domain.model.GestureType
import com.uhi.motionlayer.domain.source.AppCatalog
import com.uhi.motionlayer.domain.source.CustomGestureLibrary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * 앱별 동작 설정(AP-02)이 그릴 값.
 *
 * @param app 이 앱. 목록에 없는 앱이면 null
 * @param commands 기본 제스처마다 하는 동작. null이면 "사용 안 함"
 * @param customGestures 내가 만든 제스처 전부(만든 순서). 이 앱에서 쓰지 않는 것도 "사용 안 함"으로 보여 준다
 * @param customCommands 나만의 제스처 id → 이 앱에서 하는 동작. 없으면 "사용 안 함"
 */
data class AppSettingsUiState(
    val app: ControllableApp?,
    val commands: Map<GestureType, GestureCommand?>,
    val customGestures: List<CustomGesture> = emptyList(),
    val customCommands: Map<String, GestureCommand> = emptyMap(),
)

/** 앱 하나의 제스처별 동작(AP-02)과 동작 고르기(AP-03). 앱마다 하나씩 만든다. */
class AppSettingsViewModel(
    private val appId: String,
    private val catalog: AppCatalog,
    private val customLibrary: CustomGestureLibrary,
) : ViewModel() {
    val uiState: StateFlow<AppSettingsUiState> = combine(
        catalog.apps,
        catalog.gestureSettings(appId),
        customLibrary.gestures,
        catalog.customGestureCommands,
    ) { apps, settings, customGestures, customCommands ->
        AppSettingsUiState(
            app = apps.firstOrNull { it.id == appId },
            commands = settings.commands,
            customGestures = customGestures,
            customCommands = customCommands[appId].orEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettingsUiState(
            app = catalog.apps.value.firstOrNull { it.id == appId },
            commands = catalog.gestureSettings(appId).value.commands,
            customGestures = customLibrary.gestures.value,
            customCommands = catalog.customGestureCommands.value[appId].orEmpty(),
        ),
    )

    fun optionsFor(gesture: GestureType): List<GestureCommand> = catalog.commandOptions(appId, gesture)

    fun customOptions(): List<GestureCommand> = catalog.customCommandOptions(appId)

    fun onToggleEnabled(enabled: Boolean) = catalog.setEnabled(appId, enabled)

    fun onSaveCommand(gesture: GestureType, command: GestureCommand?) = catalog.setCommand(appId, gesture, command)

    fun onSaveCustomCommand(gestureId: String, command: GestureCommand?) =
        catalog.setCustomCommand(appId, gestureId, command)

    fun onReset() = catalog.resetGestureSettings(appId)

    companion object {
        fun factory(appId: String) = viewModelFactory {
            initializer {
                AppSettingsViewModel(
                    appId = appId,
                    catalog = AppContainer.appCatalog,
                    customLibrary = AppContainer.customGestureLibrary,
                )
            }
        }
    }
}
