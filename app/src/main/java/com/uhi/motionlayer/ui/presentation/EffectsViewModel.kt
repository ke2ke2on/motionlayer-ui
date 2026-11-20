package com.uhi.motionlayer.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.PresentationEffect
import com.uhi.motionlayer.domain.source.EffectLibrary
import com.uhi.motionlayer.domain.source.PcLink
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 효과 관리(PR-14)가 그릴 값: 지금 연결한 PC 이름과 효과 목록. 값은 받아서 보여 주기만 한다. */
class EffectsViewModel(
    private val library: EffectLibrary,
    pcLink: PcLink,
) : ViewModel() {
    val effects: StateFlow<List<PresentationEffect>> = library.effects

    /** 연결된 PC의 이름. 연결돼 있지 않으면 null. */
    val pcName: StateFlow<String?> = pcLink.state
        .map { (it as? PcConnectionState.Connected)?.deviceName }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = (pcLink.state.value as? PcConnectionState.Connected)?.deviceName,
        )

    /** "효과 추가". 파일을 고르는 창은 시스템이 띄운다. */
    fun onAdd() = library.add()

    companion object {
        val Factory = viewModelFactory {
            initializer {
                EffectsViewModel(library = AppContainer.effectLibrary, pcLink = AppContainer.pcLink)
            }
        }
    }
}
