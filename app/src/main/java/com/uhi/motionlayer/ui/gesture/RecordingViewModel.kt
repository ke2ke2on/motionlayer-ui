package com.uhi.motionlayer.ui.gesture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.RecordingState
import com.uhi.motionlayer.domain.source.GestureRecorder
import kotlinx.coroutines.flow.StateFlow

/**
 * 나만의 제스처를 만드는 중의 상태(GS-04~08). 앱 틀이 하나만 만들어 이름 화면(GS-03)과 녹화 화면이 함께 쓴다.
 * 곡선·일치도·확인 결과는 [GestureRecorder]가 알려 주는 대로 화면에 그리고, 화면이 하는 일은 사용자의 동작을 전하는 것뿐이다.
 */
class RecordingViewModel(private val recorder: GestureRecorder) : ViewModel() {
    /** 만드는 중이 아니면 null. */
    val state: StateFlow<RecordingState?> = recorder.state

    /** 새 제스처를 [name]으로 만들기 시작한다. [replacingId]가 있으면 그 제스처를 다시 녹화한다. */
    fun start(name: String, replacingId: String? = null) = recorder.start(name, replacingId)

    fun retryTake() = recorder.retryTake()

    fun restart() = recorder.restart()

    /** 만들기를 그만둔다. 저장을 마친 뒤 화면을 떠날 때도 불러 상태를 비운다(저장된 제스처는 그대로 남는다). */
    fun cancel() = recorder.cancel()

    companion object {
        val Factory = viewModelFactory {
            initializer { RecordingViewModel(recorder = AppContainer.gestureRecorder) }
        }
    }
}
