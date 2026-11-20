package com.uhi.motionlayer.ui.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.DiagnosticsSnapshot
import com.uhi.motionlayer.domain.source.DiagnosticsSource
import kotlinx.coroutines.flow.StateFlow

/** 개발자 도구(ST-08)가 그릴 값. 받은 값을 그대로 보여 주기만 한다. */
class DeveloperToolsViewModel(diagnostics: DiagnosticsSource) : ViewModel() {
    val snapshot: StateFlow<DiagnosticsSnapshot> = diagnostics.snapshot

    companion object {
        val Factory = viewModelFactory {
            initializer { DeveloperToolsViewModel(diagnostics = AppContainer.diagnostics) }
        }
    }
}
