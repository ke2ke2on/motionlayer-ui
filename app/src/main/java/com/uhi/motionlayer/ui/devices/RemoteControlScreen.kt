package com.uhi.motionlayer.ui.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.ConnectionRoute
import com.uhi.motionlayer.domain.model.RemoteKey
import com.uhi.motionlayer.domain.model.RemoteSession
import com.uhi.motionlayer.domain.source.AccountSession
import com.uhi.motionlayer.domain.source.RemoteControl
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlCtaFrame
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 원격 조작(DV-08)이 쓰는 ViewModel. 연결 상태를 올리고, 버튼 누름을 전달한다. */
class RemoteControlViewModel(
    private val deviceId: String,
    private val remote: RemoteControl,
    account: AccountSession,
) : ViewModel() {
    /** 원격 연결 상태. 조작할 수 없는 기기(꺼짐·없음)면 null. */
    val session: StateFlow<RemoteSession?> = remote.session(deviceId)

    /** 대상 화면에 "○○ 님이 조작 중"으로 뜨는 조작하는 사람의 이름. 내 계정 이름이다. */
    val controllerName: StateFlow<String> = account.account
        .map { it?.name.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, account.account.value?.name.orEmpty())

    fun press(key: RemoteKey) = remote.press(deviceId, key)

    companion object {
        fun factory(deviceId: String) = viewModelFactory {
            initializer {
                RemoteControlViewModel(
                    deviceId = deviceId,
                    remote = AppContainer.remoteControl,
                    account = AppContainer.accountSession,
                )
            }
        }
    }
}

/**
 * 원격 조작 중(Figma DV-08). 내 기기를 서버를 거쳐(또는 같은 와이파이로) 리모컨처럼 조작한다. 대상 화면에는 조작하는 사람의 이름이
 * 뜬다고 알리고, 어떻게 연결됐는지(서버 경유·응답 시간)를 보여 준다. 꺼져 있어 조작할 수 없는 기기면 바로 돌아간다.
 */
@Composable
fun RemoteControlRoute(
    deviceId: String,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RemoteControlViewModel = viewModel(key = "remote-$deviceId", factory = RemoteControlViewModel.factory(deviceId)),
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val controllerName by viewModel.controllerName.collectAsStateWithLifecycle()

    LaunchedEffect(session == null) {
        if (session == null) onEnd()
    }

    session?.let {
        RemoteControlContent(
            session = it,
            controllerName = controllerName,
            onKey = viewModel::press,
            onEnd = onEnd,
            modifier = modifier,
        )
    }
}

/** 원격 조작 화면. 값을 받아 그리고 버튼 누름을 올릴 뿐이다. */
@Composable
fun RemoteControlContent(
    session: RemoteSession,
    controllerName: String,
    onKey: (RemoteKey) -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    MlCtaFrame(
        modifier = modifier,
        topBar = { MlTopBar(title = session.deviceName, leading = MlTopBarLeading.Back, onLeadingClick = onEnd) },
        primaryText = stringResource(R.string.dv08_end),
        onPrimary = onEnd,
        primaryStyle = MlButtonStyle.Secondary,
        contentTopPadding = MotionSpacing.s4,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(MotionSpacing.s12)) {
            MlBanner(
                message = stringResource(R.string.dv08_banner, controllerName),
                tone = MlBannerTone.Info,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MlBadge(
                    text = stringResource(
                        if (session.route == ConnectionRoute.Server) R.string.dv08_route_server else R.string.dv08_route_local,
                    ),
                )
                Text(
                    text = stringResource(R.string.dv08_latency, session.latencyMs),
                    style = type.caption1,
                    color = colors.textTertiary,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
                KeyRow(
                    left = KeySpec(MlIcon.Refresh, R.string.dv08_key_input, R.string.dv08_key_input, RemoteKey.InputSwitch),
                    right = KeySpec(MlIcon.Blackout, R.string.dv08_key_power, R.string.dv08_key_power, RemoteKey.Power),
                    onKey = onKey,
                )
                KeyRow(
                    left = KeySpec(MlIcon.Minus, R.string.dv08_key_volume, R.string.dv08_key_volume_down, RemoteKey.VolumeDown),
                    right = KeySpec(MlIcon.Plus, R.string.dv08_key_volume, R.string.dv08_key_volume_up, RemoteKey.VolumeUp),
                    onKey = onKey,
                )
                KeyRow(
                    left = KeySpec(MlIcon.Minus, R.string.dv08_key_channel, R.string.dv08_key_channel_down, RemoteKey.ChannelDown),
                    right = KeySpec(MlIcon.Plus, R.string.dv08_key_channel, R.string.dv08_key_channel_up, RemoteKey.ChannelUp),
                    onKey = onKey,
                )
            }
            Text(text = stringResource(R.string.dv08_gesture_hint), style = type.caption1, color = colors.textTertiary)
        }
    }
}

/** 리모컨 버튼 하나의 그림과 글자, 눌렀을 때 보낼 키. [description]은 스크린리더가 읽는 말이다("음량 줄이기"). */
private class KeySpec(
    val icon: MlIcon,
    val labelRes: Int,
    val descriptionRes: Int,
    val key: RemoteKey,
)

/** 버튼 두 개를 같은 폭으로 나란히 놓은 한 줄(Figma: 가로 152씩, 사이 8). */
@Composable
private fun KeyRow(left: KeySpec, right: KeySpec, onKey: (RemoteKey) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
        KeyTile(spec = left, onClick = { onKey(left.key) }, modifier = Modifier.weight(1f))
        KeyTile(spec = right, onClick = { onKey(right.key) }, modifier = Modifier.weight(1f))
    }
}

/** 리모컨 버튼(Figma `Key/…`): `fill/weak` 바탕, 모서리 20, 위아래 18, 아이콘 24 아래에 Caption/1 Strong 글자. */
@Composable
private fun KeyTile(spec: KeySpec, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val description = stringResource(spec.descriptionRes)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(MotionRadius.r20))
            .background(colors.fillWeak)
            .semantics { contentDescription = description }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = MotionSpacing.s18),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
    ) {
        MlIconView(icon = spec.icon, tint = colors.textSecondary)
        Text(text = stringResource(spec.labelRes), style = MotionTheme.typography.caption1Strong, color = colors.textSecondary)
    }
}

@Preview(name = "DV-08 원격 조작 중 (본가 TV)", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Dv08Preview() = MotionLayerTheme {
    RemoteControlContent(
        session = RemoteSession("본가 TV", ConnectionRoute.Server, 180),
        controllerName = "윤",
        onKey = {},
        onEnd = {},
    )
}
