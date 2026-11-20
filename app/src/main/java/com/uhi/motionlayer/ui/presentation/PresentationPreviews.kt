package com.uhi.motionlayer.ui.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.ui.components.MlBottomTab
import com.uhi.motionlayer.ui.components.MlTab
import com.uhi.motionlayer.ui.theme.MotionLayerTheme

/* 발표 탭 화면 미리보기. PR-02·03·04·05는 각 화면 파일에 있다. Figma 화면 이름과 같게 맞췄다. */

@Preview(name = "PR-01 발표 · 연결 전", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr01Preview() = MotionLayerTheme {
    Column(Modifier.fillMaxSize()) {
        PresentationContent(
            recentPcs = SampleData.recentPcs,
            onConnectNew = {},
            onConnectRecent = {},
            modifier = Modifier.weight(1f),
        )
        MlBottomTab(selected = MlTab.Presentation, onSelect = {})
    }
}

@Preview(name = "PR-17 발표 · 연결한 PC 없음", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr17Preview() = MotionLayerTheme {
    Column(Modifier.fillMaxSize()) {
        PresentationContent(
            recentPcs = emptyList(),
            onConnectNew = {},
            onConnectRecent = {},
            modifier = Modifier.weight(1f),
        )
        MlBottomTab(selected = MlTab.Presentation, onSelect = {})
    }
}
