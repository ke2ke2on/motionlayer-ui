package com.uhi.motionlayer.ui.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.PresentationLink
import com.uhi.motionlayer.domain.model.PresentationState
import com.uhi.motionlayer.domain.model.PresentationTool
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.debug.DebugActionsDialog
import com.uhi.motionlayer.ui.debug.isDebuggable
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import kotlin.math.roundToInt

/**
 * 손가락이 닿은 자리를 따라다니는 원(Figma PR-08 `TouchPad`의 원). 크기 88, `brand/primary` 색을 투명도 16%로 얹는다.
 * 눌린 터치 영역의 파란 테두리·옅은 파란 바탕과 같은 색 계열이다.
 */
private val TouchMarkerSize = 88.dp
private const val TOUCH_MARKER_ALPHA = 0.16f

/**
 * 발표 중 화면(Figma PR-07 대기 · PR-08 누르는 중 · PR-11 5분 남음 · PR-12 연결 끊김). 상태에 따라 모양이 바뀌는 **화면 하나**다.
 *
 * 화면이 열리면 발표를 시작하고, "끝내기"를 누르면 기록을 만들어 [onEnded]로 알린다.
 * 발표 도중 실수로 나가지 않도록 시스템 뒤로 가기는 막아 둔다.
 */
@Composable
fun PresentingRoute(
    viewModel: PresentingViewModel,
    onEnded: () -> Unit,
    onOpenEffects: () -> Unit,
    debugActions: List<DebugAction>,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(viewModel) { viewModel.startIfNeeded() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = true) { /* 발표 중에는 "끝내기"로만 나간다 */ }

    state?.let { current ->
        PresentingContent(
            state = current,
            onTouching = viewModel::onTouching,
            onSelectTool = viewModel::selectTool,
            onOpenEffects = onOpenEffects,
            onEnd = {
                viewModel.end()
                onEnded()
            },
            debugActions = if (isDebuggable()) debugActions else emptyList(),
            modifier = modifier,
        )
    }
}

/**
 * 상태를 받아 그리기만 하는 발표 중 화면.
 *
 * 아래 도구 줄(Figma)은 "그리기"(PR-09)·"화면 가리기"(PR-10) 두 도구와 효과 목록을 여는 "효과"(PR-14) 버튼이다.
 * 위쪽 안내는 연결 끊김 > 5분 남음 > 화면 가리기 순서로 하나만 보여 준다.
 *
 * @param onSelectTool 도구 타일을 눌렀다. 켜진 도구를 다시 누르면 끄는 것은 컨트롤러가 한다
 * @param onOpenEffects "효과" 버튼을 눌렀다(효과 관리 PR-14를 연다)
 * @param debugActions 디버그 빌드에서 "남은 시간"·"슬라이드" 줄을 **길게 누르면** 열리는 시연용 동작. 비어 있으면 아무 일도 없다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresentingContent(
    state: PresentationState,
    onTouching: (Boolean) -> Unit,
    onSelectTool: (PresentationTool) -> Unit,
    onOpenEffects: () -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
    debugActions: List<DebugAction> = emptyList(),
) {
    val colors = MotionTheme.colors
    val reconnecting = state.link is PresentationLink.Reconnecting
    var debugOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        PresentingHeader(link = state.link, onEnd = onEnd)

        when {
            reconnecting -> NoticeBanner(
                text = stringResource(R.string.pr07_disconnected_notice),
                tone = MlBannerTone.Danger,
            )
            state.fiveMinutesLeft -> NoticeBanner(
                text = stringResource(
                    if (state.vibratedAtFiveMinutes) R.string.pr07_five_minutes_notice else R.string.pr07_five_minutes_notice_silent,
                ),
                tone = MlBannerTone.Warning,
            )
            state.tool == PresentationTool.Blackout -> NoticeBanner(
                text = stringResource(R.string.pr10_notice),
                tone = MlBannerTone.Info,
            )
        }

        PresentingInfo(
            state = state,
            modifier = if (debugActions.isNotEmpty()) {
                Modifier.combinedClickable(onClick = {}, onLongClick = { debugOpen = true })
            } else {
                Modifier
            },
        )

        TouchPad(
            enabled = !reconnecting,
            tool = state.tool,
            onTouching = onTouching,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = MotionSpacing.screenHorizontal),
        )

        ToolRow(selected = state.tool, enabled = !reconnecting, onSelect = onSelectTool, onOpenEffects = onOpenEffects)
    }

    if (debugOpen) {
        DebugActionsDialog(actions = debugActions, onDismiss = { debugOpen = false })
    }
}

@Composable
private fun PresentingHeader(link: PresentationLink, onEnd: () -> Unit) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MotionSize.topBar)
            .padding(horizontal = MotionSpacing.screenHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
    ) {
        when (link) {
            is PresentationLink.Connected -> {
                MlBadge(text = stringResource(R.string.pr07_badge_connected), tone = MlTone.Green)
                Text(
                    text = stringResource(R.string.pr07_link_status, link.deviceName, link.latencyMs),
                    style = type.caption1,
                    color = colors.textTertiary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
            PresentationLink.Reconnecting -> {
                MlBadge(text = stringResource(R.string.pr07_badge_disconnected), tone = MlTone.Red)
                Text(
                    text = stringResource(R.string.pr07_reconnecting),
                    style = type.caption1,
                    color = colors.textTertiary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        // 글자는 작지만 누르는 영역은 48dp 높이로 넉넉하게 잡는다.
        Box(
            modifier = Modifier
                .heightIn(min = MotionSize.iconButton)
                .clickable(role = Role.Button, onClick = onEnd),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.pr07_end),
                style = type.body2Strong,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun NoticeBanner(text: String, tone: MlBannerTone) {
    MlBanner(
        message = text,
        tone = tone,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, bottom = MotionSpacing.s8),
    )
}

@Composable
private fun PresentingInfo(state: PresentationState, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val timeColor = if (state.fiveMinutesLeft) colors.statusWarning else colors.textPrimary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, bottom = MotionSpacing.s16),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.pr07_remaining_label), style = type.caption1, color = colors.textTertiary)
            Text(
                text = stringResource(R.string.pr07_time_format, state.remainingSeconds / 60, state.remainingSeconds % 60),
                style = type.display,
                color = timeColor,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = stringResource(R.string.pr07_slide_label), style = type.caption1, color = colors.textTertiary)
            Text(
                text = state.slideTotal
                    ?.let { stringResource(R.string.pr07_slide_progress, state.slideIndex, it) }
                    ?: stringResource(R.string.pr07_slide_only, state.slideIndex),
                style = type.title2,
                color = colors.textPrimary,
            )
        }
    }
}

/**
 * 아래 도구 줄(Figma `Tools`). 타일은 가로를 똑같이 나눠 쓰고, 켜진 도구는 파란색이다.
 * 연결이 끊긴 동안(PR-12)은 흐리게 하고 누를 수 없다.
 */
@Composable
private fun ToolRow(
    selected: PresentationTool,
    enabled: Boolean,
    onSelect: (PresentationTool) -> Unit,
    onOpenEffects: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 나란한 타일은 가장 큰 타일에 맞춰 같은 높이로 맞춘다(보고서 6.3. 글자를 키우면 한 타일만 두 줄이 될 수 있어서).
            .height(IntrinsicSize.Min)
            .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s16),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
    ) {
        ToolTile(
            icon = MlIcon.Pen,
            label = stringResource(R.string.pr07_tool_draw),
            selected = selected == PresentationTool.Draw,
            enabled = enabled,
            onClick = { onSelect(PresentationTool.Draw) },
            modifier = Modifier.weight(1f),
        )
        ToolTile(
            icon = MlIcon.Blackout,
            label = stringResource(R.string.pr07_tool_blackout),
            selected = selected == PresentationTool.Blackout,
            enabled = enabled,
            onClick = { onSelect(PresentationTool.Blackout) },
            modifier = Modifier.weight(1f),
        )
        // 켜 두는 도구가 아니라 효과 목록 화면을 여는 버튼이라 파랗게 켜지지 않는다.
        ToolTile(
            icon = MlIcon.Image,
            label = stringResource(R.string.pr07_tool_effects),
            selected = false,
            enabled = enabled,
            onClick = onOpenEffects,
            modifier = Modifier.weight(1f),
        )
    }
}

/** 도구 하나: 위에 아이콘(22), 아래에 이름. 모서리 16, 위아래 14. 꺼져 있으면 회색 칸, 켜져 있으면 파란 칸에 흰 글씨다. */
@Composable
private fun ToolTile(
    icon: MlIcon,
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val content = if (selected) colors.textInverse else colors.textSecondary

    Column(
        modifier = modifier
            .fillMaxHeight()
            .alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(MotionRadius.r16))
            .background(if (selected) colors.brandPrimary else colors.fillWeak)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = MotionSpacing.s14),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s6, Alignment.CenterVertically),
    ) {
        MlIconView(icon = icon, tint = content, size = MotionIconSize.s22)
        Text(text = label, style = MotionTheme.typography.caption1Strong, color = content, textAlign = TextAlign.Center)
    }
}

/**
 * 손가락을 댄 채로 제스처를 하는 큰 터치 영역(Figma `TouchPad`, 모서리 28).
 * 손가락이 닿으면 파란 테두리로 바뀌고(PR-08), 닿은 자리를 따라 검정 16% 원(88dp)이 움직인다.
 * 그리기 도구가 켜져 있으면(PR-09) 닿았을 때 펜 아이콘과 "그리는 중이에요"가 나오고, 닿지 않았을 때는 안내 문구만 바뀐다.
 * [enabled]가 false(연결 끊김)이면 닿아도 반응하지 않고 안내만 흐리게 보여 준다(PR-12).
 */
@Composable
private fun TouchPad(
    enabled: Boolean,
    tool: PresentationTool,
    onTouching: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    var finger by remember { mutableStateOf<Offset?>(null) }
    val pressed = enabled && finger != null
    val drawing = tool == PresentationTool.Draw
    val shape = RoundedCornerShape(MotionRadius.r28)

    // 연결이 끊기는 순간 손가락이 닿은 채여도 닿지 않은 것으로 돌려놓는다.
    LaunchedEffect(enabled) {
        if (!enabled && finger != null) {
            finger = null
            onTouching(false)
        }
    }
    DisposableEffect(Unit) { onDispose { onTouching(false) } }

    val padDescription = stringResource(R.string.pr07_pad_description)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (pressed) colors.brandWeak else if (enabled) colors.bgGrouped else colors.fillWeak)
            .then(if (pressed) Modifier.border(MotionBorder.thick, colors.brandPrimary, shape) else Modifier)
            // 스크린리더에는 이 영역이 무엇인지 한 마디로 알려 준다(보고서 6.4 "터치패드에 '누르고 있는 동안 제스처 인식'").
            .semantics { contentDescription = padDescription }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    finger = down.position
                    onTouching(true)
                    // 손을 뗄 때(또는 취소될 때)까지 닿은 자리를 따라간다.
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        event.changes.firstOrNull()?.let { if (it.pressed) finger = it.position }
                    } while (event.changes.any { it.pressed })
                    finger = null
                    onTouching(false)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        finger?.let { position ->
            Box(
                modifier = Modifier
                    .offset {
                        val half = (TouchMarkerSize / 2).toPx()
                        IntOffset((position.x - half).roundToInt(), (position.y - half).roundToInt())
                    }
                    .size(TouchMarkerSize)
                    .background(colors.brandPrimary.copy(alpha = TOUCH_MARKER_ALPHA), CircleShape)
                    .align(Alignment.TopStart),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.bgBase),
                contentAlignment = Alignment.Center,
            ) {
                MlIconView(
                    icon = when {
                        pressed && drawing -> MlIcon.Pen
                        pressed -> MlIcon.Pointer
                        else -> MlIcon.Tap
                    },
                    tint = if (pressed) colors.brandPrimary else if (enabled) colors.textSecondary else colors.textDisabled,
                    size = MotionIconSize.s32,
                )
            }
            val title: Int
            val hint: Int
            val titleColor: Color
            val hintColor: Color
            when {
                pressed -> {
                    title = if (drawing) R.string.pr09_pad_title else R.string.pr08_pad_pressed_title
                    hint = if (drawing) R.string.pr09_pad_hint else R.string.pr08_pad_pressed_hint
                    titleColor = colors.brandPrimary
                    hintColor = colors.textTertiary
                }
                enabled -> {
                    title = R.string.pr07_pad_idle_title
                    hint = when (tool) {
                        PresentationTool.Draw -> R.string.pr09_pad_idle_hint
                        PresentationTool.Blackout -> R.string.pr10_pad_hint
                        PresentationTool.None -> R.string.pr07_pad_idle_hint
                    }
                    titleColor = colors.textSecondary
                    hintColor = colors.textSecondary
                }
                else -> {
                    title = R.string.pr12_pad_title
                    hint = R.string.pr12_pad_hint
                    titleColor = colors.textDisabled
                    hintColor = colors.textDisabled
                }
            }
            Text(
                text = stringResource(title),
                style = type.body1Strong,
                color = titleColor,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(hint),
                style = type.caption1,
                color = hintColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val previewConnected = PresentationLink.Connected(deviceName = "강의실 PC", latencyMs = 12)

@Preview(name = "PR-07 발표 중 · 대기", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr07Preview() = MotionLayerTheme {
    PresentingContent(
        state = PresentationState(522, 5, 18, previewConnected, fiveMinutesLeft = false),
        onTouching = {},
        onSelectTool = {},
        onOpenEffects = {},
        onEnd = {},
    )
}

@Preview(name = "PR-09 발표 중 · 그리기", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr09Preview() = MotionLayerTheme {
    PresentingContent(
        state = PresentationState(522, 6, 18, previewConnected, fiveMinutesLeft = false, tool = PresentationTool.Draw),
        onTouching = {},
        onSelectTool = {},
        onOpenEffects = {},
        onEnd = {},
    )
}

@Preview(name = "PR-10 발표 중 · 화면 가리기", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr10Preview() = MotionLayerTheme {
    PresentingContent(
        state = PresentationState(522, 5, 18, previewConnected, fiveMinutesLeft = false, tool = PresentationTool.Blackout),
        onTouching = {},
        onSelectTool = {},
        onOpenEffects = {},
        onEnd = {},
    )
}

@Preview(name = "PR-11 발표 중 · 5분 남음", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr11Preview() = MotionLayerTheme {
    PresentingContent(
        state = PresentationState(300, 14, 18, previewConnected, fiveMinutesLeft = true),
        onTouching = {},
        onSelectTool = {},
        onOpenEffects = {},
        onEnd = {},
    )
}

@Preview(name = "PR-12 발표 중 · 연결 끊김", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr12Preview() = MotionLayerTheme {
    PresentingContent(
        state = PresentationState(522, 9, 18, PresentationLink.Reconnecting, fiveMinutesLeft = false),
        onTouching = {},
        onSelectTool = {},
        onOpenEffects = {},
        onEnd = {},
    )
}
