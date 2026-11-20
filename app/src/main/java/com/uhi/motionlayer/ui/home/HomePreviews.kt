package com.uhi.motionlayer.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.DailyStats
import com.uhi.motionlayer.domain.model.DetectionStatus
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.ui.components.MlBottomTab
import com.uhi.motionlayer.ui.components.MlTab
import com.uhi.motionlayer.ui.theme.MotionLayerTheme

/*
 * 홈 화면 상태별 미리보기. Figma 화면 이름과 맞춰 두었으니 같은 이름의 화면과 나란히 비교한다.
 * 하단 탭은 앱 틀에서 붙는 것이라 여기서는 Figma와 같은 모양을 보려고 같이 그린다.
 */

@Composable
private fun HomePreviewFrame(state: HomeUiState) {
    MotionLayerTheme {
        Column(Modifier.fillMaxSize()) {
            HomeContent(
                state = state,
                onToggleDetection = {},
                onOpenSettings = {},
                onOpenApps = {},
                onConnectPc = {},
                onDisconnectPc = {},
                onSelectControlTarget = {},
                onOpenHistory = {},
                onOpenPractice = {},
                modifier = Modifier.weight(1f),
            )
            MlBottomTab(selected = MlTab.Home, onSelect = {})
        }
    }
}

@Preview(name = "HM-01 홈 · 감지 중", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun Hm01Preview() = HomePreviewFrame(
    HomeUiState(
        detectionEnabled = true,
        playingApp = SampleData.youtubeMusic,
        pc = PcConnectionState.Disconnected,
        todayStats = SampleData.todayStats,
    ),
)

@Preview(name = "HM-02 홈 · 감지 꺼짐", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun Hm02Preview() = HomePreviewFrame(
    HomeUiState(
        detectionEnabled = false,
        playingApp = null,
        pc = PcConnectionState.Disconnected,
        todayStats = DailyStats(recognizedCount = 0, undoneCount = 0),
    ),
)

@Preview(name = "HM-03 홈 · 재생 중인 앱 없음", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun Hm03Preview() = HomePreviewFrame(
    HomeUiState(
        detectionEnabled = true,
        playingApp = null,
        pc = PcConnectionState.Disconnected,
        todayStats = SampleData.todayStats,
    ),
)

@Preview(name = "SY-06 홈 · 인터넷 없음", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun Sy06Preview() = HomePreviewFrame(
    HomeUiState(
        detectionEnabled = true,
        playingApp = SampleData.youtubeMusic,
        pc = PcConnectionState.Disconnected,
        todayStats = SampleData.todayStats,
        online = false,
        lastPc = SampleData.recentPcs.first(),
    ),
)

@Preview(name = "HM-05 홈 · PC 연결됨", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun Hm05Preview() = HomePreviewFrame(
    HomeUiState(
        detectionEnabled = true,
        playingApp = SampleData.youtubeMusic,
        pc = SampleData.classroomPc,
        todayStats = SampleData.todayStats,
    ),
)

@Preview(name = "HM-04 홈 · 걷는 중", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun Hm04Preview() = HomePreviewFrame(
    HomeUiState(
        detectionEnabled = true,
        playingApp = SampleData.youtubeMusic,
        pc = PcConnectionState.Disconnected,
        todayStats = SampleData.todayStats,
        detectionStatus = DetectionStatus.WalkingPaused,
    ),
)

@Preview(name = "HM-07 홈 · 불러오는 중", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun Hm07Preview() = HomePreviewFrame(
    HomeUiState(
        detectionEnabled = true,
        playingApp = null,
        pc = PcConnectionState.Disconnected,
        todayStats = DailyStats(recognizedCount = 0, undoneCount = 0),
        detectionStatus = DetectionStatus.Checking,
    ),
)

@Preview(name = "HM-08 홈 · 감지 멈춤 (오류)", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun Hm08Preview() = HomePreviewFrame(
    HomeUiState(
        detectionEnabled = true,
        playingApp = SampleData.youtubeMusic,
        pc = PcConnectionState.Disconnected,
        todayStats = SampleData.todayStats,
        detectionStatus = DetectionStatus.Stopped,
    ),
)
