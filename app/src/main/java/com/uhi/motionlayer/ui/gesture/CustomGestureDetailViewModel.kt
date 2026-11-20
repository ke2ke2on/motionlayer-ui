package com.uhi.motionlayer.ui.gesture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.ControllableApp
import com.uhi.motionlayer.domain.model.CustomGesture
import com.uhi.motionlayer.domain.model.CustomGestureUse
import com.uhi.motionlayer.domain.model.GestureCommand
import com.uhi.motionlayer.domain.model.customGestureUses
import com.uhi.motionlayer.domain.source.AppCatalog
import com.uhi.motionlayer.domain.source.CustomGestureLibrary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * 나만의 제스처 상세(GS-11)가 그릴 값.
 *
 * @param gesture 지워졌으면 null이다. 화면은 곧바로 닫힌다
 * @param uses 이 제스처를 쓰는 앱과 동작. 쓰는 앱이 없으면 빈 목록
 */
data class CustomGestureDetailUiState(
    val gesture: CustomGesture?,
    val uses: List<CustomGestureUse> = emptyList(),
)

/** 나만의 제스처 하나의 상세와 감도·삭제(GS-11·12). 제스처마다 하나씩 만든다. */
class CustomGestureDetailViewModel(
    private val gestureId: String,
    private val library: CustomGestureLibrary,
    catalog: AppCatalog,
) : ViewModel() {
    private fun stateOf(
        gestures: List<CustomGesture>,
        apps: List<ControllableApp>,
        commands: Map<String, Map<String, GestureCommand>>,
    ) = CustomGestureDetailUiState(
        gesture = gestures.firstOrNull { it.id == gestureId },
        uses = customGestureUses(apps, commands, gestureId),
    )

    val uiState: StateFlow<CustomGestureDetailUiState> = combine(
        library.gestures,
        catalog.apps,
        catalog.customGestureCommands,
        ::stateOf,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = stateOf(library.gestures.value, catalog.apps.value, catalog.customGestureCommands.value),
    )

    fun onSensitivityChange(sensitivity: Float) = library.setSensitivity(gestureId, sensitivity)

    fun onDelete() = library.delete(gestureId)

    companion object {
        fun factory(gestureId: String) = viewModelFactory {
            initializer {
                CustomGestureDetailViewModel(
                    gestureId = gestureId,
                    library = AppContainer.customGestureLibrary,
                    catalog = AppContainer.appCatalog,
                )
            }
        }
    }
}
