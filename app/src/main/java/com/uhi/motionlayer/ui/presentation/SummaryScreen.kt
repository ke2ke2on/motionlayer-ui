package com.uhi.motionlayer.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.PresentationSummary
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlButtonSize
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlResultBadge
import com.uhi.motionlayer.ui.components.MlResultTone
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** Figma PR-13 발표 종료 요약. 이번 발표의 시간·슬라이드 넘김·되돌림·오작동을 네 칸으로 보여 준다. */
@Composable
fun SummaryScreen(
    summary: PresentationSummary,
    onConfirm: () -> Unit,
    onOpenDetails: () -> Unit,
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
        Box(Modifier.height(MotionSize.topBar))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, top = MotionSpacing.s24),
        ) {
            MlResultBadge(icon = MlIcon.Check, tone = MlResultTone.Brand)
            Box(Modifier.height(MotionSpacing.s28))
            Text(text = stringResource(R.string.pr13_title), style = type.title2, color = colors.textPrimary)
            Box(Modifier.height(MotionSpacing.s8))
            Text(text = stringResource(R.string.pr13_description), style = type.body2, color = colors.textSecondary)
            Box(Modifier.height(MotionSpacing.s24))

            Column(verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
                ) {
                    StatCard(
                        label = stringResource(R.string.pr13_duration_label),
                        value = stringResource(
                            R.string.pr13_duration_value,
                            summary.durationSeconds / 60,
                            summary.durationSeconds % 60,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(R.string.pr13_slides_label),
                        value = stringResource(R.string.pr13_count, summary.slideChanges),
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
                ) {
                    StatCard(
                        label = stringResource(R.string.pr13_undone_label),
                        value = stringResource(R.string.pr13_count, summary.undoneCount),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(R.string.pr13_misfire_label),
                        value = stringResource(R.string.pr13_count, summary.misfireCount),
                        badge = if (summary.misfireGoalMet) stringResource(R.string.pr13_goal_met) else null,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, top = MotionSpacing.s12, bottom = MotionSpacing.s8),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s4),
        ) {
            MlButton(text = stringResource(R.string.pr13_confirm), onClick = onConfirm, modifier = Modifier.fillMaxWidth())
            MlButton(
                text = stringResource(R.string.pr13_details),
                onClick = onOpenDetails,
                modifier = Modifier.fillMaxWidth(),
                style = MlButtonStyle.Ghost,
                size = MlButtonSize.M,
            )
        }
    }
}

/** 회색 칸 하나. 위에 이름(과 선택적 배지), 아래에 값. 안쪽 여백 16, 모서리 16. */
@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(MotionRadius.r16))
            .background(MotionTheme.colors.bgGrouped)
            .padding(MotionSpacing.s16),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s4),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
        ) {
            Text(text = label, style = MotionTheme.typography.caption1, color = MotionTheme.colors.textSecondary)
            if (badge != null) MlBadge(text = badge, tone = MlTone.Green)
        }
        Text(text = value, style = MotionTheme.typography.title3, color = MotionTheme.colors.textPrimary)
    }
}

@Preview(name = "PR-13 발표 종료 요약", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr13Preview() = MotionLayerTheme {
    SummaryScreen(
        summary = PresentationSummary(
            durationSeconds = 9 * 60 + 48,
            slideChanges = 18,
            undoneCount = 0,
            misfireCount = 0,
            misfireGoalMet = true,
        ),
        onConfirm = {},
        onOpenDetails = {},
    )
}
