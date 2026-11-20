package com.uhi.motionlayer.ui.gesture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.BasicGesture
import com.uhi.motionlayer.domain.model.PracticeState
import com.uhi.motionlayer.domain.source.PracticeSession
import kotlinx.coroutines.flow.StateFlow

/**
 * 연습 모드(GS-09)·연습 완료(GS-10)가 함께 쓰는 ViewModel. 앱 전체에서 하나만 둔다.
 * 연습 완료 화면이 같은 값(성공 횟수)을 보여 줘야 해서, 연습 화면과 같은 인스턴스를 쓴다.
 */
class PracticeViewModel(private val session: PracticeSession) : ViewModel() {
    val state: StateFlow<PracticeState> = session.state

    fun enter() = session.enter()

    fun leave() = session.leave()

    fun select(gesture: BasicGesture) = session.select(gesture)

    companion object {
        val Factory = viewModelFactory {
            initializer { PracticeViewModel(session = AppContainer.practiceSession) }
        }
    }
}
