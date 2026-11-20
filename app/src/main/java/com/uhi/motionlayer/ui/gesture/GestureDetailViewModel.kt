package com.uhi.motionlayer.ui.gesture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.BasicGesture
import com.uhi.motionlayer.domain.model.GestureDetail
import com.uhi.motionlayer.domain.source.GestureCatalog
import kotlinx.coroutines.flow.StateFlow

/** 기본 제스처 하나의 상세(GS-02). 제스처마다 하나씩 만든다. */
class GestureDetailViewModel(
    private val gesture: BasicGesture,
    private val catalog: GestureCatalog,
) : ViewModel() {
    val detail: StateFlow<GestureDetail> = catalog.detail(gesture)

    fun onSensitivityChange(sensitivity: Float) = catalog.setSensitivity(gesture, sensitivity)

    companion object {
        fun factory(gesture: BasicGesture) = viewModelFactory {
            initializer { GestureDetailViewModel(gesture = gesture, catalog = AppContainer.gestureCatalog) }
        }
    }
}
