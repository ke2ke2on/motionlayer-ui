package com.uhi.motionlayer.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.PairingResult
import com.uhi.motionlayer.domain.model.PairingTarget
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.RecentPc
import com.uhi.motionlayer.domain.source.PcLink
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 발표 탭의 연결 흐름(PR-01~05)이 함께 쓰는 ViewModel. 앱 전체에서 하나만 둔다.
 * 연결을 시도하고, 끝나면 결과를 [results]로 한 번만 내보낸다. 어느 화면으로 갈지는 내비게이션이 정한다.
 */
class PairingViewModel(private val pcLink: PcLink) : ViewModel() {
    val connection: StateFlow<PcConnectionState> = pcLink.state
    val recentPcs: StateFlow<List<RecentPc>> = pcLink.recentPcs

    private val _isConnecting = MutableStateFlow(false)

    /**
     * 연결을 시도하는 중인지. true인 동안 앱 틀이 "PC에 연결하는 중"(PR-16)을 보여 주고, 결과가 나오면 연결됨(PR-04)이나
     * 실패(PR-05)로 바꾼다. 분석서 표 4-3: 결과는 3초 안에 나와야 하고, 넘으면 실패로 알리는 것은 연결 쪽(C)이다.
     */
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _connectingName = MutableStateFlow<String?>(null)

    /** 지금 연결을 시도하는 PC의 이름. 최근 PC를 골랐을 때만 알고, QR·코드로는 연결하기 전에는 모르므로 null이다. */
    val connectingName: StateFlow<String?> = _connectingName.asStateFlow()

    private val _results = Channel<PairingResult>(Channel.BUFFERED)
    val results: Flow<PairingResult> = _results.receiveAsFlow()

    private var attempt: Job? = null

    /** 이미 연결을 시도하는 중이면 무시한다(같은 QR을 연달아 인식해도 한 번만 연결). */
    fun connect(target: PairingTarget) {
        if (attempt?.isActive == true) return
        attempt = viewModelScope.launch {
            _connectingName.value = (target as? PairingTarget.Recent)
                ?.let { recent -> recentPcs.value.firstOrNull { it.id == recent.id }?.name }
            _isConnecting.value = true
            try {
                _results.send(pcLink.connect(target))
            } finally {
                _isConnecting.value = false
            }
        }
    }

    fun cancel() {
        attempt?.cancel()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { PairingViewModel(pcLink = AppContainer.pcLink) }
        }
    }
}
