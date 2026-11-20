package com.uhi.motionlayer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.ui.components.MlCard
import com.uhi.motionlayer.ui.components.MlCardTone
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.ConnectionRoute
import com.uhi.motionlayer.domain.model.ControlTarget
import com.uhi.motionlayer.domain.model.DetectionStatus
import com.uhi.motionlayer.domain.model.DeviceKind
import com.uhi.motionlayer.domain.model.GestureBinding
import com.uhi.motionlayer.domain.model.GestureCommand
import com.uhi.motionlayer.domain.model.GestureType
import com.uhi.motionlayer.domain.model.MyDevice
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.PlayingApp
import com.uhi.motionlayer.domain.model.RecentPc
import com.uhi.motionlayer.ui.components.MlAppIcon
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlButtonSize
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconButton
import com.uhi.motionlayer.ui.components.MlIconTile
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlSkeleton
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlToggle
import com.uhi.motionlayer.ui.debug.DebugActionsDialog
import com.uhi.motionlayer.ui.debug.isDebuggable
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.KoreanParticle

/**
 * 홈 탭(Figma HM-01~05·07·08, SY-06). 상태에 따라 모양이 달라지는 **화면 하나**다.
 * 하단 탭은 포함하지 않는다(앱 틀에서 붙인다).
 */
@Composable
fun HomeRoute(
    onOpenSettings: () -> Unit,
    onOpenApps: () -> Unit,
    onConnectPc: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenPractice: () -> Unit,
    onTurnOnDetection: () -> Unit,
    onOpenRemote: (MyDevice) -> Unit,
    onPickByDirection: () -> Unit,
    debugActions: List<DebugAction>,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        onToggleDetection = { enabled -> if (enabled) onTurnOnDetection() else viewModel.onTurnOffDetection() },
        onOpenSettings = onOpenSettings,
        onOpenApps = onOpenApps,
        onConnectPc = onConnectPc,
        onDisconnectPc = viewModel::onDisconnectPc,
        onSelectControlTarget = viewModel::onSelectControlTarget,
        onOpenHistory = onOpenHistory,
        onOpenPractice = onOpenPractice,
        onOpenRemote = onOpenRemote,
        onPickByDirection = onPickByDirection,
        debugActions = if (isDebuggable()) debugActions else emptyList(),
        modifier = modifier,
    )
}

/** 상태를 받아 그리기만 하는 홈 화면. 미리보기와 테스트에서 직접 쓴다. */
@Composable
fun HomeContent(
    state: HomeUiState,
    onToggleDetection: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenApps: () -> Unit,
    onConnectPc: () -> Unit,
    onDisconnectPc: () -> Unit,
    onSelectControlTarget: (ControlTarget) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenPractice: () -> Unit,
    modifier: Modifier = Modifier,
    debugActions: List<DebugAction> = emptyList(),
    onOpenRemote: (MyDevice) -> Unit = {},
    onPickByDirection: () -> Unit = {},
) {
    val target = state.target()
    // 걷는 중(HM-04)은 감지가 켜져 있을 때만 뜻이 있다. 꺼졌거나 멈춘 화면에서는 알리지 않는다.
    val walking = state.detectionStatus == DetectionStatus.WalkingPaused && target !is Target.Off && target !is Target.Stopped
    var debugOpen by remember { mutableStateOf(false) }
    var targetSheetOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MotionTheme.colors.bgGrouped)
            .statusBarsPadding(),
    ) {
        HomeHeader(
            onOpenSettings = onOpenSettings,
            onTitleLongClick = if (debugActions.isNotEmpty()) ({ debugOpen = true }) else null,
        )

        if (state.detectionStatus == DetectionStatus.Checking) {
            // 상태를 확인하는 동안(HM-07)은 카드 자리에 막대만 두고, 헤더와 하단 탭은 바로 그려 둔다.
            HomeLoadingBody(modifier = Modifier.weight(1f))
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = MotionSpacing.s16, end = MotionSpacing.s16, top = MotionSpacing.s4, bottom = MotionSpacing.s16),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
            ) {
                if (target is Target.Stopped) {
                    MlBanner(
                        message = stringResource(R.string.home_stopped_banner),
                        tone = MlBannerTone.Warning,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (walking) {
                    MlBanner(
                        message = stringResource(R.string.home_walking_banner),
                        tone = MlBannerTone.Info,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (!state.online) {
                    MlBanner(
                        message = stringResource(R.string.home_offline_banner),
                        tone = MlBannerTone.Neutral,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                DetectionCard(state = state, target = target, walking = walking, onToggle = onToggleDetection)
                ControlTargetCard(
                    target = target,
                    walking = walking,
                    // 앱이나 PC를 조작 중이면 조작 대상 시트(HM-06)를, 꺼져 있거나 멈췄거나 재생 앱이 없으면 앱 탭을 연다.
                    onClick = { if (target is Target.App || target is Target.Pc) targetSheetOpen = true else onOpenApps() },
                )
                PcCard(
                    pc = state.pc,
                    offlineLastPc = if (state.online) null else state.lastPc,
                    onConnect = onConnectPc,
                    onDisconnect = onDisconnectPc,
                )
                // 감지가 멈춘 화면(HM-08)은 오류 안내에 집중하도록 통계·연습 카드를 두지 않는다(Figma).
                if (target !is Target.Stopped) {
                    StatsCard(state = state, onOpenHistory = onOpenHistory)
                    PracticeCard(onClick = onOpenPractice)
                }
            }
        }
    }

    if (targetSheetOpen) {
        ControlTargetSheet(
            selected = state.controlTarget,
            playingApp = state.playingApp,
            pc = state.pc,
            lastPc = state.lastPc,
            devices = state.myDevices.filter { it.kind == DeviceKind.Tv },
            onSelect = { selected ->
                onSelectControlTarget(selected)
                targetSheetOpen = false
            },
            onOpenRemote = { device ->
                targetSheetOpen = false
                onOpenRemote(device)
            },
            onPickByDirection = {
                targetSheetOpen = false
                onPickByDirection()
            },
            onDismiss = { targetSheetOpen = false },
        )
    }

    if (debugOpen) {
        DebugActionsDialog(actions = debugActions, onDismiss = { debugOpen = false })
    }
}

/** 지금 무엇을 조작하는 상태인지. 카드 두 장의 모양이 이것으로 정해진다. */
private sealed interface Target {
    /** 감지 서비스가 멈췄다(HM-08). 사용자가 끈 것([Off])과 달리 오류로 보여 주고 "다시 켜기"를 둔다. */
    data object Stopped : Target
    data object Off : Target
    data object NoApp : Target
    data class App(val app: PlayingApp) : Target
    data class Pc(val connection: PcConnectionState.Connected) : Target
}

private fun HomeUiState.target(): Target {
    val connection = pc
    val app = playingApp
    return when {
        detectionStatus == DetectionStatus.Stopped -> Target.Stopped
        !detectionEnabled -> Target.Off
        connection is PcConnectionState.Connected && controlTarget == ControlTarget.Pc -> Target.Pc(connection)
        app == null -> Target.NoApp
        else -> Target.App(app)
    }
}

/** @param onTitleLongClick 제목을 길게 눌렀을 때. 시연용 동작 창을 여는 데 쓰며, 없으면 길게 눌러도 아무 일 없다 */
@Composable
private fun HomeHeader(onOpenSettings: () -> Unit, onTitleLongClick: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MotionSize.topBar)
            .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MotionTheme.typography.title3,
            color = MotionTheme.colors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .then(
                    if (onTitleLongClick != null) {
                        Modifier.combinedClickable(onClick = {}, onLongClick = onTitleLongClick)
                    } else {
                        Modifier
                    },
                ),
        )
        MlIconButton(
            icon = MlIcon.Settings,
            onClick = onOpenSettings,
            contentDescription = stringResource(R.string.ml_settings),
        )
    }
}

/** 홈의 카드. 공용 카드 `MlCard`(흰색)에 홈의 기본 간격 12를 더한 것이다. */
@Composable
private fun HomeCard(
    modifier: Modifier = Modifier,
    verticalPadding: Dp = MotionSpacing.cardPadding,
    spacing: Dp = MotionSpacing.s12,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    MlCard(
        modifier = modifier,
        verticalPadding = verticalPadding,
        verticalArrangement = Arrangement.spacedBy(spacing),
        onClick = onClick,
        content = content,
    )
}

/**
 * @param walking 걷는 중이라 손 제스처를 멈춘 상태(HM-04). 스위치는 켜진 채로 두고 점과 설명만 주황·"주머니 두드림만 받아요"로 바꾼다
 */
@Composable
private fun DetectionCard(
    state: HomeUiState,
    target: Target,
    walking: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    val stopped = target is Target.Stopped
    val dotColor: Color
    val statusText: String
    when (target) {
        Target.Stopped -> {
            dotColor = colors.statusDanger
            statusText = stringResource(R.string.home_status_stopped)
        }
        Target.Off -> {
            dotColor = colors.textDisabled
            statusText = stringResource(R.string.home_status_off)
        }
        Target.NoApp -> {
            dotColor = colors.textDisabled
            statusText = stringResource(R.string.home_status_waiting)
        }
        is Target.App -> {
            dotColor = colors.statusSuccess
            statusText = stringResource(
                R.string.home_status_listening_app,
                target.app.name,
                KoreanParticle.subject(target.app.name),
            )
        }
        is Target.Pc -> {
            dotColor = colors.statusSuccess
            statusText = stringResource(
                R.string.home_status_sending_pc,
                target.connection.deviceName,
                KoreanParticle.toward(target.connection.deviceName),
            )
        }
    }
    // 걷는 중(HM-04)에는 어떤 대상이든 주머니 두드림만 받는다는 것을 먼저 말한다.
    val shownDotColor = if (walking) colors.statusWarning else dotColor
    val shownStatusText = if (walking) stringResource(R.string.home_status_walking) else statusText

    HomeCard {
        // 머리글 줄 전체가 스위치를 누르는 영역이다(보고서 6.2: 스위치만으로는 높이 30이라 모자라서 줄 전체를 눌리게 한다).
        // 스크린리더에는 "제스처 감지 · 켜져 있어요 · 스위치"가 한 덩어리로 읽힌다.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = state.detectionEnabled && !stopped,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Switch,
                    onValueChange = onToggle,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
            ) {
                Text(
                    text = stringResource(R.string.home_detection_label),
                    style = type.caption1Strong,
                    color = colors.textTertiary,
                )
                Text(
                    text = stringResource(
                        when {
                            stopped -> R.string.home_detection_stopped
                            state.detectionEnabled -> R.string.home_detection_on
                            else -> R.string.home_detection_off
                        },
                    ),
                    style = type.title3,
                    color = colors.textPrimary,
                )
            }
            // 멈춘 서비스는 켜 둔 값이 남아 있어도 스위치를 꺼 둬서 "앱이 꺼졌다"는 사실과 맞춘다(HM-08).
            MlToggle(checked = state.detectionEnabled && !stopped, onCheckedChange = null)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
        ) {
            Box(
                modifier = Modifier
                    .size(MotionSpacing.s8)
                    .background(shownDotColor, CircleShape),
            )
            Text(text = shownStatusText, style = type.caption1, color = colors.textSecondary)
        }
        if (stopped) {
            // 스위치를 켜는 것과 같다: 권한이 있으면 바로 켜고, 없으면 권한 안내가 먼저 나온다.
            MlButton(
                text = stringResource(R.string.home_detection_restart),
                onClick = { onToggle(true) },
                modifier = Modifier.fillMaxWidth(),
                size = MlButtonSize.M,
            )
        }
    }
}

/**
 * @param walking 걷는 중(HM-04). 재생 중인 앱의 제스처 칩이 손 제스처 대신 주머니 두드림 두 가지("두 번 두드림 다음 곡", "세 번 이전 곡")로 바뀐다.
 *   두드림 명령 배정은 Figma 노트가 "팀 확정 전 제안안"이라 한 고정 문구다. PC를 조작 중일 때의 모습은 시안이 없어 칩을 그대로 둔다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ControlTargetCard(target: Target, walking: Boolean, onClick: () -> Unit) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    val label = stringResource(if (target is Target.Pc) R.string.home_target_device_label else R.string.home_target_app_label)
    val title: String
    val subtitle: String
    when (target) {
        Target.Stopped -> {
            title = stringResource(R.string.home_target_stopped_title)
            subtitle = stringResource(R.string.home_target_stopped_subtitle)
        }
        Target.Off -> {
            title = stringResource(R.string.home_target_off_title)
            subtitle = stringResource(R.string.home_target_off_subtitle)
        }
        Target.NoApp -> {
            title = stringResource(R.string.home_target_none_title)
            subtitle = stringResource(R.string.home_target_none_subtitle)
        }
        is Target.App -> {
            title = target.app.name
            // 걷는 중(HM-04)에는 이 앱의 손 제스처 설정이 멈춰 있어서 "이 앱 설정 적용"을 빼고 "재생 중"만 말한다.
            subtitle = stringResource(if (walking) R.string.home_target_walking_subtitle else R.string.home_target_app_subtitle)
        }
        is Target.Pc -> {
            title = target.connection.deviceName
            subtitle = stringResource(
                if (target.connection.route == ConnectionRoute.Local) R.string.home_target_pc_subtitle_local else R.string.home_target_pc_subtitle_server,
                target.connection.latencyMs,
            )
        }
    }
    val bindings = when (target) {
        is Target.App -> target.app.bindings
        is Target.Pc -> target.connection.bindings
        else -> emptyList()
    }

    HomeCard(
        spacing = MotionSpacing.s14,
        onClick = onClick,
    ) {
        Text(text = label, style = type.caption1Strong, color = colors.textTertiary)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
        ) {
            when (target) {
                Target.Stopped, Target.Off -> MlIconTile(icon = MlIcon.Pause, tone = MlTone.Gray)
                Target.NoApp -> MlIconTile(icon = MlIcon.Music, tone = MlTone.Gray)
                is Target.App -> MlAppIcon(
                    letter = target.app.iconLetter,
                    color = target.app.accentColorArgb?.let { Color(it) } ?: colors.brandPrimary,
                )
                is Target.Pc -> MlIconTile(icon = MlIcon.Laptop, tone = MlTone.Blue)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
            ) {
                Text(text = title, style = type.body1Strong, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = subtitle, style = type.caption1, color = colors.textTertiary)
            }
            if (target is Target.Pc) {
                MlBadge(text = stringResource(R.string.home_badge_connected), tone = MlTone.Green)
            } else {
                MlIconView(icon = MlIcon.ChevronRight, tint = colors.textDisabled, size = MotionIconSize.s20)
            }
        }
        if (walking && target is Target.App) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
            ) {
                GestureChip(text = stringResource(R.string.home_pocket_next))
                GestureChip(text = stringResource(R.string.home_pocket_previous))
            }
        } else if (bindings.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
            ) {
                bindings.forEach { GestureChip(text = bindingLabel(it)) }
            }
        }
    }
}

/** 제스처-명령 한 쌍을 알려 주는 작은 칩("→ 다음 곡"). 누를 수 없는 표시용이다. */
@Composable
private fun GestureChip(text: String) {
    Text(
        text = text,
        style = MotionTheme.typography.caption1,
        color = MotionTheme.colors.textSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(MotionRadius.r10))
            .background(MotionTheme.colors.fillWeak)
            .padding(horizontal = MotionSpacing.s10, vertical = MotionSpacing.s6),
    )
}

@Composable
private fun bindingLabel(binding: GestureBinding): String {
    val gesture = stringResource(
        when (binding.gesture) {
            GestureType.SwipeRight -> R.string.gesture_swipe_right
            GestureType.SwipeLeft -> R.string.gesture_swipe_left
            GestureType.DoubleTap -> R.string.gesture_double_tap
            GestureType.Tilt -> R.string.gesture_tilt
            GestureType.Shake -> R.string.gesture_shake
            GestureType.PressAndRotate -> R.string.gesture_press_rotate
        },
    )
    val command = stringResource(
        when (binding.command) {
            GestureCommand.NextTrack -> R.string.command_next_track
            GestureCommand.PreviousTrack -> R.string.command_previous_track
            GestureCommand.PlayPause -> R.string.command_play_pause
            GestureCommand.Volume -> R.string.command_volume
            GestureCommand.SkipForward -> R.string.command_skip_forward
            GestureCommand.SkipBack -> R.string.command_skip_back
            GestureCommand.Undo -> R.string.command_undo
            GestureCommand.NextSlide -> R.string.command_next_slide
            GestureCommand.PreviousSlide -> R.string.command_previous_slide
            GestureCommand.Pointer -> R.string.command_pointer
        },
    )
    return stringResource(R.string.gesture_binding_format, gesture, command)
}

/**
 * @param offlineLastPc 인터넷이 끊겨 있고 PC가 연결되지 않았을 때 보여 줄 최근 PC(SY-06). 그렇지 않으면 null
 */
@Composable
private fun PcCard(pc: PcConnectionState, offlineLastPc: RecentPc?, onConnect: () -> Unit, onDisconnect: () -> Unit) {
    HomeCard(verticalPadding = MotionSpacing.s16, spacing = MotionSpacing.s14) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
        ) {
            when (pc) {
                PcConnectionState.Disconnected -> {
                    MlIconTile(icon = MlIcon.Laptop, tone = MlTone.Gray)
                    PcTexts(
                        title = offlineLastPc?.name ?: stringResource(R.string.home_pc_disconnected_title),
                        subtitle = stringResource(
                            if (offlineLastPc != null) R.string.home_pc_offline_subtitle else R.string.home_pc_disconnected_subtitle,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    MlButton(
                        text = stringResource(R.string.home_pc_connect),
                        onClick = onConnect,
                        style = MlButtonStyle.Weak,
                        size = MlButtonSize.S,
                    )
                }
                is PcConnectionState.Connected -> {
                    MlIconTile(icon = MlIcon.Laptop, tone = MlTone.Blue)
                    PcTexts(
                        title = pc.deviceName,
                        subtitle = stringResource(
                            if (pc.route == ConnectionRoute.Local) R.string.home_pc_connected_subtitle_local else R.string.home_pc_connected_subtitle_server,
                            pc.latencyMs,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    MlButton(
                        text = stringResource(R.string.home_pc_disconnect),
                        onClick = onDisconnect,
                        style = MlButtonStyle.Secondary,
                        size = MlButtonSize.S,
                    )
                }
            }
        }
    }
}

@Composable
private fun PcTexts(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2)) {
        Text(text = title, style = MotionTheme.typography.body1Strong, color = MotionTheme.colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = subtitle, style = MotionTheme.typography.caption1, color = MotionTheme.colors.textTertiary)
    }
}

@Composable
private fun StatsCard(state: HomeUiState, onOpenHistory: () -> Unit) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    HomeCard {
        Row {
            StatColumn(
                label = stringResource(R.string.home_stats_recognized),
                value = stringResource(R.string.home_stats_count, state.todayStats.recognizedCount),
                modifier = Modifier.weight(1f),
            )
            StatColumn(
                label = stringResource(R.string.home_stats_undone),
                value = stringResource(R.string.home_stats_count, state.todayStats.undoneCount),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onOpenHistory),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
        ) {
            Text(
                text = stringResource(R.string.home_stats_history),
                style = type.body2,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            MlIconView(icon = MlIcon.ChevronRight, tint = colors.textDisabled, size = MotionIconSize.s18)
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2)) {
        Text(text = label, style = MotionTheme.typography.caption1, color = MotionTheme.colors.textTertiary)
        Text(text = value, style = MotionTheme.typography.title3, color = MotionTheme.colors.textPrimary)
    }
}

@Composable
private fun PracticeCard(onClick: () -> Unit) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    HomeCard(verticalPadding = MotionSpacing.s16, spacing = MotionSpacing.s14, onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
        ) {
            MlIconTile(icon = MlIcon.Tap, tone = MlTone.Blue)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
            ) {
                Text(text = stringResource(R.string.home_practice_title), style = type.body1Strong, color = colors.textPrimary)
                Text(text = stringResource(R.string.home_practice_subtitle), style = type.caption1, color = colors.textTertiary)
            }
            MlIconView(icon = MlIcon.ChevronRight, tint = colors.textDisabled, size = MotionIconSize.s20)
        }
    }
}

/**
 * 감지 상태를 확인하는 동안의 홈 본문(Figma HM-07). 카드 네 장 자리에 회색 막대를 두고 아래에 안내 한 줄을 붙인다.
 * 막대의 크기는 Figma 값 그대로다(가로·세로 dp).
 */
@Composable
private fun HomeLoadingBody(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = MotionSpacing.s16, end = MotionSpacing.s16, top = MotionSpacing.s4),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
    ) {
        SkeletonCard(verticalPadding = MotionSpacing.s20, bars = listOf(70.dp to 14.dp, 120.dp to 24.dp, 230.dp to 14.dp))
        SkeletonCard(verticalPadding = MotionSpacing.s20, bars = listOf(100.dp to 14.dp, 200.dp to 40.dp, 260.dp to 24.dp))
        SkeletonCard(verticalPadding = MotionSpacing.s16, bars = listOf(160.dp to 40.dp))
        SkeletonCard(verticalPadding = MotionSpacing.s20, bars = listOf(90.dp to 14.dp, 180.dp to 24.dp))
        Text(
            text = stringResource(R.string.home_loading_caption),
            style = MotionTheme.typography.caption1,
            color = MotionTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            // 확인하는 중이라는 말을 스크린리더가 바로 읽어 준다(보고서 6.4).
            modifier = Modifier
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

/** 흰 카드 안에 회색 막대(가로 × 세로)를 세로로 쌓은 자리 표시. */
@Composable
private fun SkeletonCard(verticalPadding: Dp, bars: List<Pair<Dp, Dp>>) {
    HomeCard(verticalPadding = verticalPadding, spacing = MotionSpacing.s10) {
        bars.forEach { (width, height) -> MlSkeleton(modifier = Modifier.size(width = width, height = height)) }
    }
}
