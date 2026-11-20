package com.uhi.motionlayer.ui.gesture

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.rememberTextFieldState
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.ui.components.MlCard
import com.uhi.motionlayer.ui.components.MlCardTone
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.CUSTOM_RECORDING_TAKES
import com.uhi.motionlayer.domain.model.CUSTOM_VERIFICATION_REQUIRED
import com.uhi.motionlayer.domain.model.CUSTOM_VERIFICATION_TRIES
import com.uhi.motionlayer.domain.model.RecordingState
import com.uhi.motionlayer.domain.model.RecordingStep
import com.uhi.motionlayer.domain.model.SimilarGesture
import com.uhi.motionlayer.domain.model.TrajectoryPoint
import com.uhi.motionlayer.domain.model.VerificationResult
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlCtaFrame
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlSpinner
import com.uhi.motionlayer.ui.components.MlTextField
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.debug.DebugActionsDialog
import com.uhi.motionlayer.ui.debug.isDebuggable
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.KoreanParticle
import com.uhi.motionlayer.ui.util.formatDecimal
import com.uhi.motionlayer.ui.util.rememberElapsedSeconds
import com.uhi.motionlayer.ui.components.MlResultBadge
import com.uhi.motionlayer.ui.components.MlResultTone
import kotlin.math.PI
import kotlin.math.sin

/** 나만의 제스처 만들기는 이름 → 녹화 → 확인의 세 단계다("1 / 3"). */
private const val CREATE_STEPS = 3

/** 이름 입력의 최대 글자 수. Figma에 없어 직접 정했다. */
private const val MAX_NAME_LENGTH = 20

/** 확인 칸 안의 아이콘·점선 원 지름(Figma 28). */
private val TryIconSize = MotionIconSize.s28

/** 분석 중(GS-14)의 회전 고리 지름(Figma 48). */
private val SpinnerSize = 48.dp

/** 이전 동작을 겹쳐 그리는 흐린 곡선의 투명도(Figma GS-05·06의 0.55). GS-04는 투명하지 않다. */
private const val FADED_CURVE_ALPHA = 0.55f

/** 입력이 최대 글자 수를 넘으면 그 입력을 되돌린다. */
private val NameLimit = InputTransformation {
    if (length > MAX_NAME_LENGTH) revertAllChanges()
}

/**
 * 나만의 제스처 만들기 1/3(Figma GS-03): 제스처 이름을 정한다. 이름이 비어 있으면 "다음"을 누를 수 없다.
 * 키보드는 시스템이 그리므로 만들지 않고, 키보드가 올라오면 아래 버튼이 그 위로 올라온다(앱 틀이 처리).
 */
@Composable
fun CustomGestureNameScreen(
    onClose: () -> Unit,
    onNext: (name: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val nameState = rememberTextFieldState()
    val focusRequester = remember { FocusRequester() }
    val name = nameState.text.toString().trim()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    MlCtaFrame(
        modifier = modifier,
        topBar = { MlTopBar(title = null, leading = MlTopBarLeading.Close, onLeadingClick = onClose) },
        primaryText = stringResource(R.string.gs03_next),
        onPrimary = { onNext(name) },
        primaryEnabled = name.isNotEmpty(),
        contentTopPadding = MotionSpacing.s4,
    ) {
        StepLabel(step = 1)
        Spacer(Modifier.height(MotionSpacing.s6))
        Text(text = stringResource(R.string.gs03_title), style = type.title2, color = colors.textPrimary)
        Spacer(Modifier.height(MotionSpacing.s24))
        MlTextField(
            state = nameState,
            label = stringResource(R.string.gs03_label),
            placeholder = stringResource(R.string.gs03_placeholder),
            fieldModifier = Modifier.focusRequester(focusRequester),
            inputTransformation = NameLimit,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = { if (name.isNotEmpty()) onNext(name) },
        )
        Spacer(Modifier.height(MotionSpacing.s8))
        Text(text = stringResource(R.string.gs03_hint), style = type.caption1, color = colors.textTertiary)
    }
}

/**
 * 녹화·확인·완료 화면(GS-04~08)의 연결부. [RecordingViewModel]의 상태를 그리고 사용자의 동작을 알린다.
 * 닫기·취소·"앱에 연결하기"·"나중에 할게요"는 모두 먼저 만들기 상태를 비운 뒤(저장된 제스처는 그대로 남는다) 바깥에 알린다.
 * 시스템 뒤로 가기는 저장을 마친 뒤에는 "나중에 할게요", 그 전에는 닫기와 같다.
 *
 * @param onClose 만들기를 그만두고 나간다
 * @param onConnectApps 저장을 마치고 앱 탭으로 간다
 * @param onLater 저장을 마치고 처음 있던 화면으로 돌아간다
 */
@Composable
fun CustomGestureRecordingRoute(
    viewModel: RecordingViewModel,
    onClose: () -> Unit,
    onConnectApps: () -> Unit,
    onLater: () -> Unit,
    debugActions: List<DebugAction>,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // 상태를 비운 뒤 화면이 사라지는 동안에도 마지막 모습을 그대로 보여 준다(빈 화면이 번쩍이지 않게).
    var shown by remember { mutableStateOf(state) }
    if (state != null) shown = state

    // 만들고 있는 것이 없는데 이 화면만 열렸다면(앱이 다시 시작된 직후 등) 바로 나간다.
    LaunchedEffect(Unit) { if (viewModel.state.value == null) onClose() }

    val current = shown
    if (current == null) {
        Box(modifier.fillMaxSize().background(MotionTheme.colors.bgBase))
        return
    }

    val close = { viewModel.cancel(); onClose() }
    val later = { viewModel.cancel(); onLater() }
    BackHandler { if (current.step == RecordingStep.Done) later() else close() }

    CustomGestureRecordingScreen(
        state = current,
        onClose = close,
        onRestart = viewModel::restart,
        onRetryTake = viewModel::retryTake,
        onConnectApps = { viewModel.cancel(); onConnectApps() },
        onLater = later,
        modifier = modifier,
        debugActions = if (isDebuggable()) debugActions else emptyList(),
    )
}

/**
 * 녹화·확인·완료(Figma GS-04~08). [RecordingState]의 단계에 따라 모양이 바뀌는 **화면 하나**다.
 * 곡선과 일치도는 제스처 인식(A)이 주는 값을 그대로 그린다. 제목을 **길게 누르면**(디버그 빌드) 시연용 동작 창이 열린다.
 *
 * @param onClose 닫기(X)·"취소". 만들기를 그만둔다
 * @param onRestart "처음부터 다시"·"다른 동작으로 다시 녹화"
 * @param onRetryTake "이번 동작 다시 하기"
 * @param onConnectApps "앱에 연결하기"
 * @param onLater "나중에 할게요"
 */
@Composable
fun CustomGestureRecordingScreen(
    state: RecordingState,
    onClose: () -> Unit,
    onRestart: () -> Unit,
    onRetryTake: () -> Unit,
    onConnectApps: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
    debugActions: List<DebugAction> = emptyList(),
) {
    var debugOpen by remember { mutableStateOf(false) }
    val onTitleLongClick: (() -> Unit)? = if (debugActions.isNotEmpty()) ({ debugOpen = true }) else null

    when (state.step) {
        RecordingStep.Recording -> RecordingContent(state, onClose, onRestart, onTitleLongClick, modifier)
        RecordingStep.Mismatch -> MismatchContent(state, onClose, onRetryTake, onTitleLongClick, modifier)
        RecordingStep.TooSimilar -> TooSimilarContent(state, onClose, onRestart, onTitleLongClick, modifier)
        RecordingStep.Analyzing -> AnalyzingContent(state, onClose, onTitleLongClick, modifier)
        RecordingStep.Verifying -> VerifyingContent(state, onClose, onRestart, onTitleLongClick, modifier)
        RecordingStep.Done -> DoneContent(state, onConnectApps, onLater, modifier)
    }

    if (debugOpen) DebugActionsDialog(actions = debugActions, onDismiss = { debugOpen = false })
}

/** GS-04 같은 동작을 5번 반복하는 중. */
@Composable
private fun RecordingContent(
    state: RecordingState,
    onClose: () -> Unit,
    onRestart: () -> Unit,
    onTitleLongClick: (() -> Unit)?,
    modifier: Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    RecordingFrame(
        name = state.name,
        onClose = onClose,
        primaryText = stringResource(R.string.gs04_restart),
        onPrimary = onRestart,
        primaryStyle = MlButtonStyle.Secondary,
        modifier = modifier,
    ) {
        StepLabel(step = 2)
        Spacer(Modifier.height(MotionSpacing.s6))
        StepTitle(text = stringResource(R.string.gs04_title, CUSTOM_RECORDING_TAKES), onLongClick = onTitleLongClick)
        Spacer(Modifier.height(MotionSpacing.s16))
        TakeProgress(takes = state.takes)
        Spacer(Modifier.height(MotionSpacing.s6))
        Text(
            text = stringResource(R.string.gs04_count, state.takes, CUSTOM_RECORDING_TAKES),
            style = type.caption1,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(MotionSpacing.s20))
        MatchCard(state = state, liveColor = colors.brandPrimary, previousAlpha = 1f, showHeader = true)
        Spacer(Modifier.height(MotionSpacing.s12))
        Text(text = stringResource(R.string.gs04_note), style = type.caption1, color = colors.textTertiary)
    }
}

/** GS-05 방금 동작이 이전과 너무 달라 저장하지 않았다. */
@Composable
private fun MismatchContent(
    state: RecordingState,
    onClose: () -> Unit,
    onRetryTake: () -> Unit,
    onTitleLongClick: (() -> Unit)?,
    modifier: Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    RecordingFrame(
        name = state.name,
        onClose = onClose,
        primaryText = stringResource(R.string.gs05_retry),
        onPrimary = onRetryTake,
        primaryStyle = MlButtonStyle.Primary,
        modifier = modifier,
    ) {
        StepLabel(step = 2)
        Spacer(Modifier.height(MotionSpacing.s6))
        StepTitle(text = stringResource(R.string.gs05_title), onLongClick = onTitleLongClick)
        Spacer(Modifier.height(MotionSpacing.s16))
        TakeProgress(takes = state.takes)
        Spacer(Modifier.height(MotionSpacing.s6))
        Text(
            text = stringResource(R.string.gs05_count, state.takes, CUSTOM_RECORDING_TAKES),
            style = type.caption1,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(MotionSpacing.s20))
        MatchCard(state = state, liveColor = colors.statusWarning, previousAlpha = FADED_CURVE_ALPHA, showHeader = false)
        Spacer(Modifier.height(MotionSpacing.s12))
        MlBanner(
            message = stringResource(R.string.gs05_banner),
            tone = MlBannerTone.Warning,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** GS-06 이미 있는 제스처와 너무 비슷해 저장할 수 없다. 단계 번호("2 / 3")는 없다. */
@Composable
private fun TooSimilarContent(
    state: RecordingState,
    onClose: () -> Unit,
    onRestart: () -> Unit,
    onTitleLongClick: (() -> Unit)?,
    modifier: Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val other = state.similarTo ?: return

    RecordingFrame(
        name = state.name,
        onClose = onClose,
        primaryText = stringResource(R.string.gs06_rerecord),
        onPrimary = onRestart,
        primaryStyle = MlButtonStyle.Primary,
        secondaryText = stringResource(R.string.gs06_cancel),
        onSecondary = onClose,
        modifier = modifier,
    ) {
        StepTitle(text = stringResource(R.string.gs06_title), onLongClick = onTitleLongClick)
        Spacer(Modifier.height(MotionSpacing.s8))
        Text(text = stringResource(R.string.gs06_description), style = type.body2, color = colors.textSecondary)
        Spacer(Modifier.height(MotionSpacing.s20))
        MlCard(
            tone = MlCardTone.Gray,
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
        ) {
            CurveChart(
                curves = listOf(
                    ChartCurve(other.curve, colors.statusDanger.copy(alpha = FADED_CURVE_ALPHA)),
                    ChartCurve(state.live, colors.brandPrimary),
                ),
            )
            LegendRow(dot = colors.brandPrimary, text = stringResource(R.string.gs06_new, state.name))
            LegendRow(
                dot = colors.statusDanger,
                text = stringResource(if (other.isBasic) R.string.gs06_other_basic else R.string.gs06_other_custom, other.name),
                trailing = stringResource(R.string.gs06_similarity, other.similarityPercent),
            )
        }
        Spacer(Modifier.height(MotionSpacing.s12))
        MlBanner(
            message = stringResource(R.string.gs06_banner, other.name, KoreanParticle.with(other.name)),
            tone = MlBannerTone.Danger,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * GS-14 5번 녹화한 동작에서 공통 패턴을 뽑는 중(보통 2초 안). 아래 버튼은 눌리지 않아 중복 저장을 막는다.
 * "분석 중 · 1.3초"는 이 화면이 열린 뒤 흐른 시간이다. 분석이 끝나면 확인(GS-07)으로, 실패하면 GS-05·06 모양으로 바뀐다.
 */
@Composable
private fun AnalyzingContent(
    state: RecordingState,
    onClose: () -> Unit,
    onTitleLongClick: (() -> Unit)?,
    modifier: Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val elapsed by rememberElapsedSeconds()

    RecordingFrame(
        name = state.name,
        onClose = onClose,
        primaryText = stringResource(R.string.gs14_button),
        onPrimary = {},
        primaryStyle = MlButtonStyle.Primary,
        primaryEnabled = false,
        modifier = modifier,
    ) {
        StepLabel(step = 2)
        Spacer(Modifier.height(MotionSpacing.s6))
        StepTitle(text = stringResource(R.string.gs14_title), onLongClick = onTitleLongClick)
        Spacer(Modifier.height(MotionSpacing.s8))
        Text(
            text = stringResource(R.string.gs14_description, CUSTOM_RECORDING_TAKES),
            style = type.body2,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(MotionSpacing.s28))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(MotionRadius.r16))
                .background(colors.bgGrouped)
                .padding(vertical = MotionSpacing.s28),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
        ) {
            MlSpinner(diameter = SpinnerSize)
            Text(
                text = stringResource(R.string.gs14_elapsed, formatDecimal(elapsed)),
                style = type.caption1,
                color = colors.textSecondary,
            )
        }
        Spacer(Modifier.height(MotionSpacing.s16))
        Text(text = stringResource(R.string.gs14_hint), style = type.body2, color = colors.textSecondary)
    }
}

/** GS-07 만든 동작이 잘 인식되는지 3번 확인하는 중. */
@Composable
private fun VerifyingContent(
    state: RecordingState,
    onClose: () -> Unit,
    onRestart: () -> Unit,
    onTitleLongClick: (() -> Unit)?,
    modifier: Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val results = state.verification
    val recognized = results.count { it == VerificationResult.Recognized }
    // "2 / 3회 · 성공 1회": 앞의 숫자는 지금 하는 차례다(1회를 마치고 2회를 기다리는 중이면 2).
    val currentTry = (results.size + 1).coerceAtMost(CUSTOM_VERIFICATION_TRIES)

    RecordingFrame(
        name = state.name,
        onClose = onClose,
        primaryText = stringResource(R.string.gs04_restart),
        onPrimary = onRestart,
        primaryStyle = MlButtonStyle.Secondary,
        modifier = modifier,
    ) {
        StepLabel(step = 3)
        Spacer(Modifier.height(MotionSpacing.s6))
        StepTitle(text = stringResource(R.string.gs07_title), onLongClick = onTitleLongClick)
        Spacer(Modifier.height(MotionSpacing.s8))
        Text(
            text = stringResource(R.string.gs07_description, CUSTOM_VERIFICATION_TRIES, CUSTOM_VERIFICATION_REQUIRED),
            style = type.body2,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(MotionSpacing.s28))
        Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
            for (index in 0 until CUSTOM_VERIFICATION_TRIES) {
                TryTile(
                    number = index + 1,
                    result = results.getOrNull(index),
                    isCurrent = index == results.size,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(MotionSpacing.s16))
        Text(
            text = stringResource(R.string.gs07_progress, currentTry, CUSTOM_VERIFICATION_TRIES, recognized),
            style = type.body2,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** GS-08 저장했다. 위 막대 없이 초록 체크와 안내만 있다. */
@Composable
private fun DoneContent(
    state: RecordingState,
    onConnectApps: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val recognized = state.verification.count { it == VerificationResult.Recognized }

    MlCtaFrame(
        modifier = modifier,
        // 위 막대 자리는 비워 두고 높이만 같게 둔다(Figma `Spacer` 56).
        topBar = { Spacer(Modifier.height(MotionSize.topBar)) },
        primaryText = stringResource(R.string.gs08_connect),
        onPrimary = onConnectApps,
        secondaryText = stringResource(R.string.gs08_later),
        onSecondary = onLater,
    ) {
        MlResultBadge(icon = MlIcon.Check, tone = MlResultTone.Success)
        Spacer(Modifier.height(MotionSpacing.s28))
        Text(
            text = stringResource(R.string.gs08_title, state.name, KoreanParticle.objective(state.name)),
            style = type.title2,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(MotionSpacing.s8))
        Text(
            text = stringResource(R.string.gs08_description, CUSTOM_VERIFICATION_TRIES, recognized),
            style = type.body2,
            color = colors.textSecondary,
        )
    }
}

/** 닫기(X)와 제스처 이름이 있는 위 막대 + 본문 + 아래 버튼 틀(GS-04~07). */
@Composable
private fun RecordingFrame(
    name: String,
    onClose: () -> Unit,
    primaryText: String,
    onPrimary: () -> Unit,
    primaryStyle: MlButtonStyle,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    secondaryText: String? = null,
    onSecondary: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    MlCtaFrame(
        modifier = modifier,
        topBar = { MlTopBar(title = name, leading = MlTopBarLeading.Close, onLeadingClick = onClose) },
        primaryText = primaryText,
        onPrimary = onPrimary,
        primaryStyle = primaryStyle,
        primaryEnabled = primaryEnabled,
        secondaryText = secondaryText,
        onSecondary = onSecondary,
        contentTopPadding = MotionSpacing.s4,
        content = content,
    )
}

/** "2 / 3". 지금이 만들기의 몇 단계인지. */
@Composable
private fun StepLabel(step: Int) {
    Text(
        text = stringResource(R.string.gs_create_step, step, CREATE_STEPS),
        style = MotionTheme.typography.caption1Strong,
        color = MotionTheme.colors.brandPrimary,
    )
}

/** 큰 제목. [onLongClick]이 있으면 길게 눌러 시연용 동작 창을 열 수 있다(디버그 빌드). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StepTitle(text: String, onLongClick: (() -> Unit)?) {
    Text(
        text = text,
        style = MotionTheme.typography.title2,
        color = MotionTheme.colors.textPrimary,
        modifier = if (onLongClick != null) Modifier.combinedClickable(onClick = {}, onLongClick = onLongClick) else Modifier,
    )
}

/** 녹화 횟수 막대 다섯 칸. 저장한 횟수만큼 파랗다(높이 6, 칸 사이 6). */
@Composable
private fun TakeProgress(takes: Int) {
    val colors = MotionTheme.colors

    Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s6)) {
        for (index in 0 until CUSTOM_RECORDING_TAKES) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(MotionSpacing.s6)
                    .clip(CircleShape)
                    .background(if (index < takes) colors.brandPrimary else colors.fillStrong),
            )
        }
    }
}

/**
 * 방금 동작의 곡선과 이전 동작(회색)을 겹쳐 보여 주는 회색 카드와 "이전 동작과 일치도".
 * 일치도가 아직 없으면(세 번째 녹화 전) "—"로 둔다. [liveColor]는 방금 동작 선과 일치도 글자의 색이다(달라요이면 주황).
 *
 * @param showHeader "방금 동작" 제목과 "녹화 중" 배지(GS-04에만 있다)
 * @param previousAlpha 이전 동작 선의 투명도
 */
@Composable
private fun MatchCard(state: RecordingState, liveColor: Color, previousAlpha: Float, showHeader: Boolean) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    MlCard(
        tone = MlCardTone.Gray,
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s16),
    ) {
        if (showHeader) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.gs04_label),
                    style = type.caption1Strong,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                MlBadge(text = stringResource(R.string.gs04_badge), tone = MlTone.Red)
            }
        }
        CurveChart(
            curves = state.previous.map { ChartCurve(it, colors.textDisabled.copy(alpha = previousAlpha)) } +
                ChartCurve(state.live, liveColor),
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(R.string.gs04_match),
                style = type.body2,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            val match = state.matchPercent
            Text(
                text = if (match != null) stringResource(R.string.gesture_success_rate, match) else stringResource(R.string.gs04_match_none),
                style = type.title1,
                color = if (match != null) liveColor else colors.textTertiary,
            )
        }
    }
}

/** 색 점 + 이름 한 줄. 오른쪽 끝에 값을 둘 수 있다(GS-06 비교 목록). */
@Composable
private fun LegendRow(dot: Color, text: String, trailing: String? = null) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
    ) {
        Box(Modifier.size(MotionSpacing.s8).clip(CircleShape).background(dot))
        Text(text = text, style = type.body2, color = colors.textPrimary, modifier = Modifier.weight(1f))
        if (trailing != null) Text(text = trailing, style = type.body2Strong, color = dot)
    }
}

/** 확인 한 번(GS-07). 인식됨(초록 체크)·인식 안 됨(빨강 X)·지금 해 보세요(파란 손)·대기(점선 원) 네 모양이다. */
@Composable
private fun TryTile(number: Int, result: VerificationResult?, isCurrent: Boolean, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val container: Color
    val labelColor: Color
    val label: Int
    when {
        result == VerificationResult.Recognized -> {
            container = colors.statusSuccessWeak
            labelColor = colors.statusSuccess
            label = R.string.gs07_recognized
        }
        result == VerificationResult.Missed -> {
            container = colors.statusDangerWeak
            labelColor = colors.statusDanger
            label = R.string.gs07_missed
        }
        isCurrent -> {
            container = colors.brandWeak
            labelColor = colors.brandPrimary
            label = R.string.gs07_current
        }
        else -> {
            container = colors.fillWeak
            labelColor = colors.textSecondary
            label = R.string.gs07_waiting
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(MotionRadius.r16))
            .background(container)
            .padding(vertical = MotionSpacing.s20),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s10),
    ) {
        Text(text = stringResource(R.string.gs07_try, number), style = type.caption1Strong, color = colors.textSecondary)
        when {
            result == VerificationResult.Recognized -> MlIconView(icon = MlIcon.Check, tint = labelColor, size = TryIconSize)
            result == VerificationResult.Missed -> MlIconView(icon = MlIcon.Close, tint = labelColor, size = TryIconSize)
            isCurrent -> MlIconView(icon = MlIcon.Tap, tint = labelColor, size = TryIconSize)
            else -> DashedCircle(color = colors.lineStrong)
        }
        Text(text = stringResource(label), style = type.caption1Strong, color = labelColor)
    }
}

/** 대기 중인 칸에 쓰는 점선 원(지름 28, 선 두께 2, 4·4 점선). */
@Composable
private fun DashedCircle(color: Color) {
    Canvas(Modifier.size(TryIconSize)) {
        val stroke = 2.dp.toPx()
        drawCircle(
            color = color,
            radius = size.minDimension / 2f - stroke / 2f,
            style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))),
        )
    }
}

private fun previewWave(phase: Float, amplitude: Float = 0.3f): List<TrajectoryPoint> = List(48) { i ->
    val x = i / 47f
    TrajectoryPoint(x, (0.5f - amplitude * sin(x * 2f * PI.toFloat() * 1.6f + phase)).coerceIn(0f, 1f))
}

private fun previewState(step: RecordingStep): RecordingState {
    val base = RecordingState(
        name = "원 그리기",
        step = step,
        takes = 3,
        live = previewWave(1.05f),
        previous = listOf(previewWave(0.35f), previewWave(0.7f)),
        matchPercent = 86,
    )
    return when (step) {
        RecordingStep.Mismatch -> base.copy(matchPercent = 48, live = previewWave(2.2f, 0.38f))
        RecordingStep.TooSimilar -> base.copy(similarTo = SimilarGesture("오른쪽 휙", previewWave(0.3f), 88))
        RecordingStep.Analyzing -> base.copy(takes = 5)
        RecordingStep.Verifying -> base.copy(takes = 5, verification = listOf(VerificationResult.Recognized))
        RecordingStep.Done -> base.copy(takes = 5, verification = List(3) { VerificationResult.Recognized })
        RecordingStep.Recording -> base
    }
}

@Composable
private fun PreviewRecording(step: RecordingStep) = MotionLayerTheme {
    CustomGestureRecordingScreen(
        state = previewState(step),
        onClose = {},
        onRestart = {},
        onRetryTake = {},
        onConnectApps = {},
        onLater = {},
    )
}

@Preview(name = "GS-03 제스처 만들기 · 이름", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs03Preview() = MotionLayerTheme { CustomGestureNameScreen(onClose = {}, onNext = {}) }

@Preview(name = "GS-04 제스처 만들기 · 녹화", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs04Preview() = PreviewRecording(RecordingStep.Recording)

@Preview(name = "GS-05 녹화 · 동작이 달라요", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs05Preview() = PreviewRecording(RecordingStep.Mismatch)

@Preview(name = "GS-06 녹화 · 기존 제스처와 겹침", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs06Preview() = PreviewRecording(RecordingStep.TooSimilar)

@Preview(name = "GS-14 제스처 만들기 · 분석 중", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs14Preview() = PreviewRecording(RecordingStep.Analyzing)

@Preview(name = "GS-07 제스처 만들기 · 검증", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs07Preview() = PreviewRecording(RecordingStep.Verifying)

@Preview(name = "GS-08 제스처 만들기 · 완료", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs08Preview() = PreviewRecording(RecordingStep.Done)
