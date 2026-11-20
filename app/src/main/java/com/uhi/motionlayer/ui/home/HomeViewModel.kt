package com.uhi.motionlayer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.ControlTarget
import com.uhi.motionlayer.domain.model.DailyStats
import com.uhi.motionlayer.domain.model.DetectionStatus
import com.uhi.motionlayer.domain.model.MyDevice
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.PlayingApp
import com.uhi.motionlayer.domain.model.RecentPc
import com.uhi.motionlayer.domain.source.ControlTargetSelector
import com.uhi.motionlayer.domain.source.DetectionController
import com.uhi.motionlayer.domain.source.DeviceRegistry
import com.uhi.motionlayer.domain.source.MediaSource
import com.uhi.motionlayer.domain.source.NetworkStatus
import com.uhi.motionlayer.domain.source.PcLink
import com.uhi.motionlayer.domain.source.UsageStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * 홈 화면이 그릴 값 전부. 값을 계산하지 않고 각 담당이 준 값을 그대로 모은다.
 *
 * @param detectionStatus 감지 서비스가 실제로 어떤 상태인지. 확인 중이면 자리 표시 막대(HM-07), 멈췄으면 오류 안내(HM-08)를 보여 준다
 * @param online 인터넷에 연결돼 있는지. 끊겨 있으면 안내 배너를 보여 준다(SY-06)
 * @param lastPc 가장 최근에 연결한 PC. 인터넷이 끊겨 있을 때 PC 카드에 이름을 보여 준다
 * @param controlTarget 제스처 명령을 보내는 곳(HM-06). PC가 연결돼 있으면 기본으로 PC다
 * @param myDevices 계정에 등록한 내 기기들. HM-06 시트가 TV 줄을 보여 주는 데 쓴다(로그인하지 않으면 비어 있다)
 */
data class HomeUiState(
    val detectionEnabled: Boolean,
    val playingApp: PlayingApp?,
    val pc: PcConnectionState,
    val todayStats: DailyStats,
    val detectionStatus: DetectionStatus = DetectionStatus.Normal,
    val online: Boolean = true,
    val lastPc: RecentPc? = null,
    val controlTarget: ControlTarget = ControlTarget.Pc,
    val myDevices: List<MyDevice> = emptyList(),
)

class HomeViewModel(
    private val detection: DetectionController,
    media: MediaSource,
    private val pcLink: PcLink,
    stats: UsageStats,
    network: NetworkStatus,
    private val controlTarget: ControlTargetSelector,
    private val registry: DeviceRegistry,
) : ViewModel() {

    /** 한 번에 합치는 흐름이 다섯 개까지라서 PC·기기에 관한 값과 감지에 관한 값을 먼저 각각 묶는다. */
    private data class PcInfo(
        val state: PcConnectionState,
        val lastPc: RecentPc?,
        val target: ControlTarget,
        val myDevices: List<MyDevice>,
    )

    private data class DetectionInfo(val enabled: Boolean, val status: DetectionStatus)

    private val pcInfo = combine(pcLink.state, pcLink.recentPcs, controlTarget.selected, registry.myDevices) { state, recent, target, devices ->
        PcInfo(state, recent.firstOrNull(), target, devices)
    }

    private val detectionInfo = combine(detection.enabled, detection.status) { enabled, status ->
        DetectionInfo(enabled, status)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        detectionInfo,
        media.playingApp,
        pcInfo,
        stats.today,
        network.online,
    ) { detectionNow, app, pc, today, online ->
        HomeUiState(
            detectionEnabled = detectionNow.enabled,
            detectionStatus = detectionNow.status,
            playingApp = app,
            pc = pc.state,
            todayStats = today,
            online = online,
            lastPc = pc.lastPc,
            controlTarget = pc.target,
            myDevices = pc.myDevices,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(
            detectionEnabled = detection.enabled.value,
            detectionStatus = detection.status.value,
            playingApp = media.playingApp.value,
            pc = pcLink.state.value,
            todayStats = stats.today.value,
            online = network.online.value,
            lastPc = pcLink.recentPcs.value.firstOrNull(),
            controlTarget = controlTarget.selected.value,
            myDevices = registry.myDevices.value,
        ),
    )

    /** 감지 토글을 껐다. 켜는 것은 권한을 먼저 확인해야 해서 권한 안내 흐름(`PermissionFlowViewModel`)이 맡는다. */
    fun onTurnOffDetection() {
        detection.setEnabled(false)
    }

    fun onDisconnectPc() {
        pcLink.disconnect()
    }

    /** HM-06에서 조작 대상을 골랐다. */
    fun onSelectControlTarget(target: ControlTarget) = controlTarget.select(target)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    detection = AppContainer.detectionController,
                    media = AppContainer.mediaSource,
                    pcLink = AppContainer.pcLink,
                    stats = AppContainer.usageStats,
                    network = AppContainer.networkStatus,
                    controlTarget = AppContainer.controlTarget,
                    registry = AppContainer.deviceRegistry,
                )
            }
        }
    }
}
