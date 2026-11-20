package com.uhi.motionlayer.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.R
import com.uhi.motionlayer.ui.components.MlCtaFrame
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.debug.DebugActionsDialog
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.components.MlResultBadge
import com.uhi.motionlayer.ui.components.MlResultTone

/*
 * 첫 실행 안내 앞부분: ON-01 환영, ON-02 첫 제스처 체험, ON-03 체험 성공.
 * Figma에만 있는 정확한 치수는 아래 상수로 두었다(간격 토큰에 없는 한 번만 쓰는 값).
 */

/** ON-01 본문이 상태 막대 아래에서 시작하는 위 여백. */
private val WelcomeTopPadding = 120.dp
private val AppMarkSize = 64.dp

/** ON-02 설명 아래에서 일러스트까지의 추가 여백과, 일러스트에서 "기다리고 있어요"까지의 추가 여백. */
private val TrialIllustrationGap = 34.dp
private val TrialListeningGap = 22.dp
private val TrialCircleSize = 240.dp
private val TrialPhoneWidth = 76.dp
private val TrialPhoneHeight = 136.dp
private val TrialPhoneArrowGap = 14.dp
private val TrialArrowSize = 40.dp
private val ListeningDotSize = 8.dp

/** ON-03·11 본문이 상태 막대 아래에서 시작하는 위 여백(Figma 56 + 24). */
private val ResultTopPadding = 80.dp

/** 첫 화면(Figma ON-01 환영). 앱을 처음 켜면 가장 먼저 나온다. 앱 아이콘을 길게 누르면(디버그 빌드) 시연용 동작 창이 열린다. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    debugActions: List<DebugAction> = emptyList(),
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    var debugOpen by remember { mutableStateOf(false) }

    MlCtaFrame(
        modifier = modifier,
        topBar = {},
        primaryText = stringResource(R.string.on01_start),
        onPrimary = onStart,
        contentTopPadding = WelcomeTopPadding,
        footnote = stringResource(R.string.on01_login_later),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
            Box(
                modifier = Modifier
                    .size(AppMarkSize)
                    .clip(RoundedCornerShape(MotionRadius.r20))
                    .background(colors.brandPrimary)
                    .then(
                        if (debugActions.isNotEmpty()) {
                            Modifier.combinedClickable(onClick = {}, onLongClick = { debugOpen = true })
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                MlIconView(icon = MlIcon.Gesture, tint = colors.textInverse, size = MotionIconSize.s32)
            }
            Text(
                text = stringResource(R.string.on01_title),
                style = type.title1,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = MotionSpacing.s20),
            )
            Text(text = stringResource(R.string.on01_description), style = type.body2, color = colors.textSecondary)
        }
    }

    if (debugOpen) DebugActionsDialog(actions = debugActions, onDismiss = { debugOpen = false })
}

/**
 * 첫 제스처 체험(Figma ON-02). 폰을 오른쪽으로 휙 움직이면 다음 화면으로 넘어간다. 움직임을 알아보는 것은 화면 밖(제스처 인식)이 하고,
 * 화면은 기다리는 모습만 보여 준다. 일러스트를 길게 누르면(디버그 빌드) 휙을 인식시키는 시연용 동작 창이 열린다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FirstGestureScreen(
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    debugActions: List<DebugAction> = emptyList(),
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    var debugOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding(),
    ) {
        // 오른쪽 끝의 "건너뛰기". 글자 자리는 그대로 두고 누를 수 있는 영역만 넓혔다.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MotionSize.topBar)
                .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.s16),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.on02_skip),
                style = type.body2,
                color = colors.textSecondary,
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onSkip)
                    .padding(horizontal = MotionSpacing.s8, vertical = MotionSpacing.s12),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, top = MotionSpacing.s8),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
        ) {
            Text(text = stringResource(R.string.on02_eyebrow), style = type.caption1Strong, color = colors.brandPressed)
            Text(text = stringResource(R.string.on02_title), style = type.title2, color = colors.textPrimary)
            Text(
                text = stringResource(R.string.on02_description),
                style = type.body2,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = MotionSpacing.s2),
            )
            TrialIllustration(
                modifier = Modifier.padding(top = TrialIllustrationGap),
                onLongClick = if (debugActions.isNotEmpty()) ({ debugOpen = true }) else null,
            )
            ListeningRow(modifier = Modifier.padding(top = TrialListeningGap))
        }

        Text(
            text = stringResource(R.string.on02_caption),
            style = type.caption1,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = MotionSpacing.s16),
        )
    }

    if (debugOpen) DebugActionsDialog(actions = debugActions, onDismiss = { debugOpen = false })
}

/** 회색 원 안에 폰 윤곽과 오른쪽 화살표. 폰을 오른쪽으로 움직이라는 그림이다. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrialIllustration(onLongClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Row(
            modifier = Modifier
                .size(TrialCircleSize)
                .clip(CircleShape)
                .background(colors.bgGrouped)
                .then(if (onLongClick != null) Modifier.combinedClickable(onClick = {}, onLongClick = onLongClick) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(TrialPhoneArrowGap, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val phoneShape = RoundedCornerShape(MotionRadius.r16)
            Box(
                modifier = Modifier
                    .size(width = TrialPhoneWidth, height = TrialPhoneHeight)
                    .clip(phoneShape)
                    .background(colors.bgBase)
                    .border(width = MotionBorder.thick, color = colors.lineStrong, shape = phoneShape),
            )
            MlIconView(icon = MlIcon.ArrowRight, tint = colors.brandPrimary, size = TrialArrowSize)
        }
    }
}

/** 파란 점과 "움직임을 기다리고 있어요". */
@Composable
private fun ListeningRow(modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(ListeningDotSize)
                .clip(CircleShape)
                .background(colors.brandPrimary),
        )
        Text(
            text = stringResource(R.string.on02_listening),
            style = MotionTheme.typography.caption1Strong,
            color = colors.brandPressed,
        )
    }
}

/** 체험 성공(Figma ON-03). 진동 세 가지가 어떤 뜻인지 알려 준다. */
@Composable
fun TrialSuccessScreen(onNext: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    MlCtaFrame(
        modifier = modifier,
        topBar = {},
        primaryText = stringResource(R.string.on03_next),
        onPrimary = onNext,
        contentTopPadding = ResultTopPadding,
    ) {
        // 본문 요소 사이는 8이고, 제목과 진동 안내 카드는 위로 20이 더 떨어져 있다.
        Column(verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
            MlResultBadge(icon = MlIcon.Check, tone = MlResultTone.Success)
            Text(
                text = stringResource(R.string.on03_title),
                style = type.title2,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = MotionSpacing.s20),
            )
            Text(text = stringResource(R.string.on03_description), style = type.body2, color = colors.textSecondary)
            MlCard(
                modifier = Modifier.padding(top = MotionSpacing.s20),
                tone = MlCardTone.Gray,
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s18),
            ) {
                VibrationLegendRow(
                    tint = colors.brandPrimary,
                    label = stringResource(R.string.on03_recognized),
                    value = stringResource(R.string.on03_recognized_value),
                )
                VibrationLegendRow(
                    tint = colors.statusSuccess,
                    label = stringResource(R.string.on03_executed),
                    value = stringResource(R.string.on03_executed_value),
                )
                VibrationLegendRow(
                    tint = colors.statusDanger,
                    label = stringResource(R.string.on03_failed),
                    value = stringResource(R.string.on03_failed_value),
                )
            }
        }
    }
}

/** 진동 아이콘 + 언제 + 어떻게. 아이콘 색이 진동 종류(인식 파랑·실행 초록·실패 빨강)를 나타낸다. */
@Composable
private fun VibrationLegendRow(tint: Color, label: String, value: String) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
    ) {
        MlIconView(icon = MlIcon.Vibrate, tint = tint, size = MotionIconSize.s20)
        Text(text = label, style = type.body2Strong, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Text(text = value, style = type.body2, color = colors.textSecondary)
    }
}

@Preview(name = "ON-01 환영", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun On01Preview() = MotionLayerTheme { WelcomeScreen(onStart = {}) }

@Preview(name = "ON-02 첫 제스처 체험", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun On02Preview() = MotionLayerTheme { FirstGestureScreen(onSkip = {}) }

@Preview(name = "ON-03 체험 성공", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun On03Preview() = MotionLayerTheme { TrialSuccessScreen(onNext = {}) }
