package com.uhi.motionlayer.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.PresentationSetup
import com.uhi.motionlayer.domain.model.PresentationState
import com.uhi.motionlayer.domain.model.PresentationSummary
import com.uhi.motionlayer.domain.model.PresentationTool
import com.uhi.motionlayer.domain.source.EffectLibrary
import com.uhi.motionlayer.domain.source.PcLink
import com.uhi.motionlayer.domain.source.PresentationController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * 발표 한 번(시작 → 진행 → 끝내기 → 요약)을 다루는 ViewModel. 앱 전체에서 하나만 둔다.
 * 요약(PR-13)은 발표 중 화면이 사라진 뒤에도 보여야 해서 여기에 들고 있는다.
 */
class PresentingViewModel(
    private val controller: PresentationController,
    private val pcLink: PcLink,
    effects: EffectLibrary,
) : ViewModel() {
    val state: StateFlow<PresentationState?> = controller.state

    /** 발표 준비(PR-06)의 "미리 보낸 효과 N개 준비됨"에 쓰는 수. 다 보낸 효과만 센다(보내는 중인 것은 뺀다). */
    val readyEffectCount: StateFlow<Int> = effects.effects
        .map { list -> list.count { it.uploadPercent == null } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = effects.effects.value.count { it.uploadPercent == null },
        )

    private val _summary = MutableStateFlow<PresentationSummary?>(null)
    val summary: StateFlow<PresentationSummary?> = _summary.asStateFlow()

    /** 발표 준비(PR-06·15)에서 고른 값. 발표를 시작할 때 쓰고, 한 번 정하면 다음 발표에도 그대로 남는다. */
    private val _setup = MutableStateFlow(PresentationSetup())
    val setup: StateFlow<PresentationSetup> = _setup.asStateFlow()

    fun setDurationMinutes(minutes: Int) = _setup.update { it.copy(durationMinutes = minutes) }

    fun setVibrateBeforeEnd(enabled: Boolean) = _setup.update { it.copy(vibrateBeforeEnd = enabled) }

    /** 발표를 시작한다. 이미 진행 중이면 이어서 쓴다(화면이 다시 만들어져도 시간이 다시 시작되지 않게). */
    fun startIfNeeded() {
        if (controller.state.value == null) {
            _summary.value = null
            controller.start(_setup.value)
        }
    }

    fun onTouching(touching: Boolean) = controller.setTouching(touching)

    /** 아래 도구 줄에서 도구를 눌렀다(PR-09). 켜진 도구를 다시 누르면 꺼진다. */
    fun selectTool(tool: PresentationTool) = controller.selectTool(tool)

    /** 끝내기: 기록을 만들고 PC 연결을 끝낸다(UC-01 6). */
    fun end() {
        _summary.value = controller.end()
        pcLink.disconnect()
    }

    fun clearSummary() {
        _summary.value = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                PresentingViewModel(
                    controller = AppContainer.presentationController,
                    pcLink = AppContainer.pcLink,
                    effects = AppContainer.effectLibrary,
                )
            }
        }
    }
}
