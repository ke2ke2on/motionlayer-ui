package com.uhi.motionlayer.ui.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uhi.motionlayer.R
import com.uhi.motionlayer.ui.components.MlEmptyState
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 인터넷이 없어 기기를 추가할 수 없을 때(Figma DV-09). 안 되는 것(새 기기 등록·원격 조작·제스처 동기화)과 되는 것(같은 와이파이의
 * PC 조작)을 함께 말해 고장으로 오해하지 않게 한다(제안서 NFR-06). 인터넷이 다시 연결되면 바로 기기 추가로 이어진다.
 *
 * @param onOnline 인터넷이 (다시) 연결됐다. "다시 시도"를 눌렀을 때 이미 연결돼 있어도 부른다
 */
@Composable
fun AddDeviceOfflineRoute(
    deviceFlow: DeviceFlowViewModel,
    onOnline: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val online by deviceFlow.online.collectAsStateWithLifecycle()

    // 인터넷이 돌아오면 기다리지 않고 기기 추가로 이어 간다.
    LaunchedEffect(online) {
        if (online) onOnline()
    }

    AddDeviceOfflineContent(onRetry = { if (online) onOnline() }, onBack = onBack, modifier = modifier)
}

/** 인터넷 필요 화면. 눌렀을 때의 일만 받아 그리기만 한다. */
@Composable
fun AddDeviceOfflineContent(
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = stringResource(R.string.dv09_title), leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        MlEmptyState(
            icon = MlIcon.CloudOff,
            title = stringResource(R.string.dv09_heading),
            description = stringResource(R.string.dv09_description),
            buttonText = stringResource(R.string.dv09_retry),
            onButtonClick = onRetry,
        )
    }
}

@Preview(name = "DV-09 인터넷 필요", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Dv09Preview() = MotionLayerTheme {
    AddDeviceOfflineContent(onRetry = {}, onBack = {})
}
