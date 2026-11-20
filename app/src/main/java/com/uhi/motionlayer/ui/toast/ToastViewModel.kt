package com.uhi.motionlayer.ui.toast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.AppEvent
import com.uhi.motionlayer.domain.source.AppEvents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 토스트로 보여 줄 일 한 건. [id]가 달라야 같은 일이 연달아 와도 새 토스트로 친다. */
data class ToastItem(val id: Long, val event: AppEvent)

/**
 * 지금 떠 있는 토스트 하나를 들고 있다. 새 일이 오면 떠 있던 토스트를 바로 대신한다.
 * 일은 앱 화면이 켜져 있는 동안에만 받는다(SY-03: "앱 화면이 켜져 있을 때만 토스트로 결과를 보여 줘요").
 */
class ToastViewModel(private val appEvents: AppEvents) : ViewModel() {
    private val _current = MutableStateFlow<ToastItem?>(null)
    val current: StateFlow<ToastItem?> = _current.asStateFlow()

    private var nextId = 0L

    /** 앱 화면이 켜진 동안 계속 불러 두면 일을 받아 토스트로 바꾼다. 화면이 꺼지면 취소된다. */
    suspend fun collectEvents() {
        appEvents.events.collect { event ->
            _current.value = ToastItem(nextId++, event)
        }
    }

    /** 시간이 지나 토스트를 치운다. 그 사이 새 토스트로 바뀌었다면 건드리지 않는다. */
    fun dismiss(id: Long) {
        _current.update { if (it?.id == id) null else it }
    }

    /** 토스트의 행동 글자("되돌리기")를 눌렀다. */
    fun onAction(item: ToastItem) {
        if (item.event is AppEvent.ControlTargetChanged) appEvents.undoControlTargetChange()
        dismiss(item.id)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ToastViewModel(appEvents = AppContainer.appEvents) }
        }
    }
}
