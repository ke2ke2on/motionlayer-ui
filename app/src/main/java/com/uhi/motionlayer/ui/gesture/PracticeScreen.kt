package com.uhi.motionlayer.ui.gesture

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.ui.components.MlCard
import com.uhi.motionlayer.ui.components.MlCardTone
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.BasicGesture
import com.uhi.motionlayer.domain.model.PRACTICE_SUCCESSES_REQUIRED
import com.uhi.motionlayer.domain.model.PracticeState
import com.uhi.motionlayer.domain.model.TrajectoryPoint
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlChip
import com.uhi.motionlayer.ui.components.MlChipRow
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.debug.DebugActionsDialog
import com.uhi.motionlayer.ui.debug.isDebuggable
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 연습 모드(Figma GS-09). 화면에 들어오면 연습을 시작하고 나가면 멈춘다.
 * 이 화면에 **들어온 뒤에** 4가지를 모두 익히면 [onCompleted]로 연습 완료 화면(GS-10)으로 간다.
 * (이미 다 익힌 채 들어왔을 때는 바로 넘어가지 않는다.)
 */
@Composable
fun PracticeRoute(
    viewModel: PracticeViewModel,
    onBack: () -> Unit,
    onCompleted: () -> Unit,
    debugActions: List<DebugAction>,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val completedAtEntry = remember { state.completed }

    DisposableEffect(viewModel) {
        viewModel.enter()
        onDispose { viewModel.leave() }
    }
    LaunchedEffect(state.completed) {
        if (state.completed && !completedAtEntry) onCompleted()
    }

    PracticeContent(
        state = state,
        onSelect = viewModel::select,
        onBack = onBack,
        debugActions = if (isDebuggable()) debugActions else emptyList(),
        modifier = modifier,
    )
}

/**
 * 상태를 받아 그리기만 하는 연습 화면. 궤적 카드를 **길게 누르면**(디버그 빌드) 시연용 동작 창이 열린다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PracticeContent(
    state: PracticeState,
    onSelect: (BasicGesture) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    debugActions: List<DebugAction> = emptyList(),
) {
    val colors = MotionTheme.colors
    var debugOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = stringResource(R.string.gs09_title), leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            MlChipRow(modifier = Modifier.padding(top = MotionSpacing.s4, bottom = MotionSpacing.s16)) {
                BasicGesture.entries.forEach { gesture ->
                    MlChip(
                        text = stringResource(gesture.chipRes),
                        selected = gesture == state.selected,
                        onClick = { onSelect(gesture) },
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = MotionSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
            ) {
                TrajectoryCard(
                    state = state,
                    modifier = if (debugActions.isNotEmpty()) {
                        Modifier.combinedClickable(onClick = {}, onLongClick = { debugOpen = true })
                    } else {
                        Modifier
                    },
                )
                ProgressCard(counts = state.successCounts)
                Text(
                    text = stringResource(R.string.gs09_footer),
                    style = MotionTheme.typography.caption1,
                    color = colors.textTertiary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Box(Modifier.height(MotionSpacing.s16))
            }
        }
    }

    if (debugOpen) DebugActionsDialog(actions = debugActions, onDismiss = { debugOpen = false })
}

/** 지금 하는 동작의 궤적과 목표와의 일치도를 보여 주는 회색 카드(모서리 20, 안쪽 20). */
@Composable
private fun TrajectoryCard(state: PracticeState, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    MlCard(
        modifier = modifier,
        tone = MlCardTone.Gray,
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(state.selected.promptRes),
                style = type.body2Strong,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            MlBadge(text = stringResource(R.string.gs09_attempt, state.attemptNumber), tone = MlTone.Blue)
        }
        TrajectoryChart(
            target = state.target,
            live = state.live,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(R.string.gs09_match_label),
                style = type.body2,
                color = colors.textSecondary,
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = MotionSpacing.s4),
            )
            Text(
                text = state.matchPercent?.let { stringResource(R.string.gs09_match_value, it) } ?: "–",
                style = type.title1,
                color = colors.brandPrimary,
            )
        }
    }
}

/**
 * 목표 곡선(회색 점선)과 지금까지 움직인 곡선(파란 선, 끝에 파란 점)을 겹쳐 그린다.
 * 점이 바뀔 때마다 다시 그려져서 30fps(분석서 4.3)로 갱신된다. 점 좌표는 0~1이고 가장자리에서 10dp 안쪽에 그린다.
 */
@Composable
private fun TrajectoryChart(
    target: List<TrajectoryPoint>,
    live: List<TrajectoryPoint>,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val description = stringResource(R.string.gs09_match_label)
    Canvas(modifier = modifier.semantics { contentDescription = description }) {
        val inset = 10.dp.toPx()
        val area = Size(size.width - inset * 2, size.height - inset * 2)

        drawLine(
            points = target,
            area = area,
            inset = inset,
            color = colors.textDisabled,
            width = 3.dp.toPx(),
            dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx())),
        )
        drawLine(
            points = live,
            area = area,
            inset = inset,
            color = colors.brandPrimary,
            width = 4.dp.toPx(),
            dash = null,
        )
        live.lastOrNull()?.let { last ->
            drawCircle(
                color = colors.brandPrimary,
                radius = 7.dp.toPx(),
                center = Offset(inset + last.x * area.width, inset + last.y * area.height),
            )
        }
    }
}

private fun DrawScope.drawLine(
    points: List<TrajectoryPoint>,
    area: Size,
    inset: Float,
    color: androidx.compose.ui.graphics.Color,
    width: Float,
    dash: PathEffect?,
) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(inset + points.first().x * area.width, inset + points.first().y * area.height)
        for (i in 1 until points.size) {
            lineTo(inset + points[i].x * area.width, inset + points[i].y * area.height)
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = dash),
    )
}

/** 제스처마다 성공한 횟수를 점 3개로 보여 주는 흰 카드(테두리 1, 모서리 20). */
@Composable
private fun ProgressCard(counts: Map<BasicGesture, Int>) {
    MlCard(tone = MlCardTone.Outlined, verticalPadding = MotionSpacing.s8) {
        BasicGesture.entries.forEach { gesture ->
            ProgressRow(gesture = gesture, successes = counts[gesture] ?: 0)
        }
    }
}

@Composable
internal fun ProgressRow(gesture: BasicGesture, successes: Int) {
    val colors = MotionTheme.colors
    val name = stringResource(gesture.nameRes)
    val description = stringResource(R.string.gs09_progress_dots, name, successes)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MotionSpacing.s10)
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MotionTheme.typography.body2,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s6)) {
            repeat(PRACTICE_SUCCESSES_REQUIRED) { index ->
                Box(
                    modifier = Modifier
                        .size(MotionSpacing.s10)
                        .background(if (index < successes) colors.statusSuccess else colors.fillStrong, CircleShape),
                )
            }
        }
    }
}

private val previewTarget = List(48) { i ->
    val t = i / 47f
    val s = ((t - 0.1f) / 0.8f).coerceIn(0f, 1f)
    TrajectoryPoint(t, 0.8f - 0.55f * (s * s * (3f - 2f * s)))
}

@Preview(name = "GS-09 연습 모드", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs09Preview() = MotionLayerTheme {
    PracticeContent(
        state = PracticeState(
            selected = BasicGesture.Swipe,
            attemptNumber = 3,
            target = previewTarget,
            live = previewTarget.map { it.copy(y = (it.y + 0.03f).coerceAtMost(1f)) },
            matchPercent = 92,
            successCounts = mapOf(
                BasicGesture.Swipe to 2,
                BasicGesture.DoubleTap to 3,
                BasicGesture.Tilt to 3,
                BasicGesture.Shake to 0,
            ),
        ),
        onSelect = {},
        onBack = {},
    )
}
