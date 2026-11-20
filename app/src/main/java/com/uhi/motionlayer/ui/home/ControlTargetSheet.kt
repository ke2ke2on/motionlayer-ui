package com.uhi.motionlayer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.ConnectionRoute
import com.uhi.motionlayer.domain.model.ControlTarget
import com.uhi.motionlayer.domain.model.MyDevice
import com.uhi.motionlayer.domain.model.MyDeviceStatus
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.PlayingApp
import com.uhi.motionlayer.domain.model.RecentPc
import com.uhi.motionlayer.ui.components.MlBottomSheet
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconTile
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 조작 대상 바꾸기 시트(Figma HM-06). "이 폰"과 "PC" 중 제스처 명령을 보낼 곳을 고른다. 고르면 바로 바뀌고 시트가 닫힌다.
 * 그 아래에 내 TV 줄(켜진 TV를 누르면 원격 조작 DV-08, 꺼진 TV는 흐리게 "꺼져 있어요")과 "폰을 기기 쪽으로 향해서 고르기"(DV-07)가 있다.
 *
 * PC가 연결돼 있지 않으면 PC 줄은 흐리게 "연결 안 됨"으로 보이고 누를 수 없다(Figma의 꺼진 TV 줄의 흐린 모양을 따랐다).
 *
 * @param selected 지금 명령을 보내는 곳
 * @param playingApp 이 폰에서 재생 중인 앱. 없으면 "재생 중인 앱이 없어요"를 보여 준다
 * @param lastPc 가장 최근에 연결한 PC. 지금 연결돼 있지 않을 때 줄의 이름으로 쓴다
 * @param devices 내 기기 가운데 TV. 로그인하지 않았거나 TV가 없으면 비어 있어 TV 줄이 없다
 */
@Composable
fun ControlTargetSheet(
    selected: ControlTarget,
    playingApp: PlayingApp?,
    pc: PcConnectionState,
    lastPc: RecentPc?,
    devices: List<MyDevice>,
    onSelect: (ControlTarget) -> Unit,
    onOpenRemote: (MyDevice) -> Unit,
    onPickByDirection: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val connected = pc as? PcConnectionState.Connected

    MlBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s8,
                    bottom = MotionSpacing.s12,
                ),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
        ) {
            Text(text = stringResource(R.string.hm06_title), style = type.title3, color = colors.textPrimary)
            Text(text = stringResource(R.string.hm06_description), style = type.body2, color = colors.textSecondary)
        }

        TargetOption(
            icon = MlIcon.Smartphone,
            title = stringResource(R.string.hm06_phone),
            subtitle = if (playingApp != null) {
                stringResource(R.string.hm06_phone_playing, playingApp.name)
            } else {
                stringResource(R.string.hm06_phone_idle)
            },
            selected = selected == ControlTarget.Phone,
            enabled = true,
            onClick = { onSelect(ControlTarget.Phone) },
        )
        TargetOption(
            icon = MlIcon.Laptop,
            title = connected?.deviceName ?: lastPc?.name ?: stringResource(R.string.hm06_pc_default_name),
            subtitle = when {
                connected == null -> stringResource(R.string.hm06_pc_disconnected)
                connected.route == ConnectionRoute.Local -> stringResource(R.string.hm06_pc_connected_local, connected.latencyMs)
                else -> stringResource(R.string.hm06_pc_connected_server, connected.latencyMs)
            },
            selected = connected != null && selected == ControlTarget.Pc,
            enabled = connected != null,
            onClick = { onSelect(ControlTarget.Pc) },
        )
        devices.forEach { device ->
            TargetOption(
                icon = MlIcon.Tv,
                title = device.name,
                subtitle = when (val status = device.status) {
                    is MyDeviceStatus.Connected -> stringResource(R.string.dv01_connected_local, status.latencyMs)
                    MyDeviceStatus.Off -> stringResource(R.string.dv01_status_off)
                    MyDeviceStatus.RemoteReachable -> stringResource(R.string.dv01_status_remote)
                },
                selected = false,
                enabled = device.status !is MyDeviceStatus.Off,
                onClick = { onOpenRemote(device) },
            )
        }
        DirectionButton(onClick = onPickByDirection)
        Box(Modifier.height(MotionSpacing.s12))
    }
}

/** "폰을 기기 쪽으로 향해서 고르기"(Figma HM-06): 연한 회색 바탕의 넓은 버튼에 나침반 아이콘 20 + 글자(Body/2)를 가운데에 둔다. */
@Composable
private fun DirectionButton(onClick: () -> Unit) {
    val colors = MotionTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MotionSpacing.screenHorizontal,
                end = MotionSpacing.screenHorizontal,
                top = MotionSpacing.s8,
                bottom = MotionSpacing.s4,
            )
            .clip(RoundedCornerShape(MotionRadius.r14))
            .background(colors.fillWeak)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = MotionSpacing.s14),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MlIconView(icon = MlIcon.Compass, tint = colors.textSecondary, size = MotionIconSize.s20)
        Text(text = stringResource(R.string.hm06_direction), style = MotionTheme.typography.body2, color = colors.textSecondary)
    }
}

/** 시트의 한 줄. 고른 줄은 파란 아이콘 타일에 체크가 붙고, 고를 수 없는 줄은 글자가 흐리다. */
@Composable
private fun TargetOption(
    icon: MlIcon,
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
    ) {
        MlIconTile(icon = icon, tone = if (selected) MlTone.Blue else MlTone.Gray)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
        ) {
            Text(
                text = title,
                style = type.body1Strong,
                color = if (enabled) colors.textPrimary else colors.textDisabled,
            )
            Text(
                text = subtitle,
                style = type.caption1,
                color = if (enabled) colors.textTertiary else colors.textDisabled,
            )
        }
        if (selected) MlIconView(icon = MlIcon.Check, tint = colors.brandPrimary)
    }
}
