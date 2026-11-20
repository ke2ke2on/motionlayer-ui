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
import com.uhi.motionlayer.domain.model.GestureSummary
import com.uhi.motionlayer.domain.model.customGestureUses
import com.uhi.motionlayer.domain.source.AppCatalog
import com.uhi.motionlayer.domain.source.CustomGestureLibrary
import com.uhi.motionlayer.domain.source.GestureCatalog
import com.uhi.motionlayer.domain.source.PracticeSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * 나만의 제스처 한 줄(GS-01).
 *
 * @param uses 이 제스처를 쓰는 앱과 동작. 쓰는 앱이 없으면 빈 목록
 */
data class CustomGestureRow(
    val gesture: CustomGesture,
    val uses: List<CustomGestureUse> = emptyList(),
)

/** 제스처 탭(GS-01)이 그릴 값. */
data class GestureListUiState(
    val gestures: List<GestureSummary>,
    /** 연습 모드에서 3번씩 성공해 익힌 제스처 수. */
    val learnedCount: Int,
    /** 내가 만든 제스처. 만든 순서대로다. */
    val customGestures: List<CustomGestureRow> = emptyList(),
    /** 최근 7일 인식률을 모으는 중인지. true면 목록 자리에 자리 표시 줄을 보여 준다(GS-16). */
    val loading: Boolean = false,
)

class GestureListViewModel(
    private val catalog: GestureCatalog,
    private val practice: PracticeSession,
    private val customLibrary: CustomGestureLibrary,
    private val appCatalog: AppCatalog,
) : ViewModel() {
    private fun customRows(
        gestures: List<CustomGesture>,
        apps: List<ControllableApp>,
        commands: Map<String, Map<String, GestureCommand>>,
    ) = gestures.map { gesture -> CustomGestureRow(gesture, customGestureUses(apps, commands, gesture.id)) }

    /** 한 번에 합치는 흐름이 다섯 개까지라서 제스처 목록과 불러오는 중인지를 먼저 묶는다. */
    private val catalogInfo = combine(catalog.basicGestures, catalog.loading) { gestures, loading -> gestures to loading }

    val uiState: StateFlow<GestureListUiState> = combine(
        catalogInfo,
        practice.state,
        customLibrary.gestures,
        appCatalog.apps,
        appCatalog.customGestureCommands,
    ) { (gestures, loading), practiceState, custom, apps, commands ->
        GestureListUiState(
            gestures = gestures,
            learnedCount = practiceState.learnedCount,
            customGestures = customRows(custom, apps, commands),
            loading = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GestureListUiState(
            gestures = catalog.basicGestures.value,
            learnedCount = practice.state.value.learnedCount,
            customGestures = customRows(
                customLibrary.gestures.value,
                appCatalog.apps.value,
                appCatalog.customGestureCommands.value,
            ),
            loading = catalog.loading.value,
        ),
    )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                GestureListViewModel(
                    catalog = AppContainer.gestureCatalog,
                    practice = AppContainer.practiceSession,
                    customLibrary = AppContainer.customGestureLibrary,
                    appCatalog = AppContainer.appCatalog,
                )
            }
        }
    }
}
