package com.uhi.motionlayer.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.CustomGestureRate
import com.uhi.motionlayer.domain.model.GestureSummary
import com.uhi.motionlayer.domain.model.ReportRange
import com.uhi.motionlayer.domain.model.SensitivitySuggestion
import com.uhi.motionlayer.domain.model.UsageReport
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlButtonSize
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlChip
import com.uhi.motionlayer.ui.components.MlChipRow
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconTile
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.gesture.SensitivitySheet
import com.uhi.motionlayer.ui.gesture.nameRes
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.KoreanParticle
import com.uhi.motionlayer.ui.util.formatDecimal
import com.uhi.motionlayer.ui.components.MlProgressBar
import com.uhi.motionlayer.ui.theme.MotionSize

/** 기록과 리포트(Figma ST-03). 홈의 "기록 보기", 설정, 발표 요약에서 들어온다. */
@Composable
fun ReportRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel(factory = ReportViewModel.Factory),
) {
    val range by viewModel.range.collectAsStateWithLifecycle()
    val report by viewModel.report.collectAsStateWithLifecycle()
    ReportContent(
        range = range,
        report = report,
        onSelectRange = viewModel::select,
        onLowerSensitivity = viewModel::onLowerSensitivity,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * 기간 칩, 인식·되돌림·오작동 카드, 제스처별 성공률 막대(기본 제스처 + 나만의 제스처), 맨 아래 "감도를 낮춰 볼까요?" 카드.
 * 상태를 받아 그리기만 한다. 카드의 "조정"을 누르면 감도를 고르는 시트(ST-04)가 열린다.
 *
 * @param onLowerSensitivity 시트에서 "낮추기"를 눌렀다. 제스처 id와 고른 감도(0~1)
 * @param initialSuggestionSheet 처음부터 열어 둘 감도 시트(미리보기용)
 */
@Composable
fun ReportContent(
    range: ReportRange,
    report: UsageReport,
    onSelectRange: (ReportRange) -> Unit,
    onLowerSensitivity: (gestureId: String, sensitivity: Float) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialSuggestionSheet: Boolean = false,
) {
    var suggestionSheetOpen by remember { mutableStateOf(initialSuggestionSheet) }
    val suggestion = report.sensitivitySuggestion

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MotionTheme.colors.bgGrouped)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = stringResource(R.string.st03_title), leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MotionSpacing.s16, vertical = MotionSpacing.s4),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
        ) {
            MlChipRow(contentPadding = PaddingValues(start = MotionSpacing.s8)) {
                ReportRange.entries.forEach { option ->
                    MlChip(
                        text = stringResource(option.labelRes),
                        selected = option == range,
                        onClick = { onSelectRange(option) },
                    )
                }
            }
            SummaryCard(report = report)
            RatesCard(rates = report.gestureRates, customRates = report.customGestureRates)
            if (suggestion != null) {
                SuggestionCard(suggestion = suggestion, onAdjust = { suggestionSheetOpen = true })
            }
            Box(Modifier.height(MotionSpacing.s16))
        }
    }

    // 제안이 사라지면(감도를 충분히 낮춘 뒤 등) 시트도 함께 닫힌다.
    if (suggestion != null && suggestionSheetOpen) {
        SensitivitySheet(
            title = stringResource(R.string.st04_title, suggestion.gestureName),
            message = stringResource(R.string.st04_message, suggestion.attemptCount, suggestion.undoneCount),
            sensitivity = suggestion.sensitivity,
            confirmText = stringResource(R.string.st04_confirm),
            onConfirm = { value ->
                onLowerSensitivity(suggestion.gestureId, value)
                suggestionSheetOpen = false
            },
            onDismiss = { suggestionSheetOpen = false },
            secondaryText = stringResource(R.string.st04_dismiss),
        )
    }
}

private val ReportRange.labelRes: Int
    get() = when (this) {
        ReportRange.Today -> R.string.st03_range_today
        ReportRange.Week -> R.string.st03_range_week
        ReportRange.Month -> R.string.st03_range_month
    }

private val ReportRange.recognizedLabelRes: Int
    get() = when (this) {
        ReportRange.Today -> R.string.st03_recognized_today
        ReportRange.Week -> R.string.st03_recognized_week
        ReportRange.Month -> R.string.st03_recognized_month
    }

/** 흰 카드 틀. 안쪽 여백 20, 모서리 20. */
@Composable
private fun ReportCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    MlCard(modifier = modifier, content = content)
}

/** 큰 숫자(인식 횟수)와 그 아래 작은 칸 두 개(되돌림, 하루 평균 오작동). */
@Composable
private fun SummaryCard(report: UsageReport) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    ReportCard {
        Text(text = stringResource(report.range.recognizedLabelRes), style = type.caption1, color = colors.textTertiary)
        Text(
            text = stringResource(R.string.st03_count, report.recognizedCount),
            style = type.display,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = MotionSpacing.s4, bottom = MotionSpacing.s8),
        )
        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
        ) {
            MiniStat(
                label = stringResource(R.string.st03_undone),
                value = stringResource(R.string.st03_count, report.undoneCount),
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            MiniStat(
                label = stringResource(R.string.st03_misfire),
                value = stringResource(R.string.st03_misfire_value, formatDecimal(report.averageMisfiresPerDay)),
                badge = if (report.misfireGoalMet) stringResource(R.string.st03_goal_met) else null,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier, badge: String? = null) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(MotionRadius.r14))
            .background(MotionTheme.colors.bgGrouped)
            .padding(horizontal = MotionSpacing.s14, vertical = MotionSpacing.s12),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s4),
    ) {
        Text(text = label, style = MotionTheme.typography.caption1, color = MotionTheme.colors.textSecondary)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
        ) {
            Text(text = value, style = MotionTheme.typography.headline, color = MotionTheme.colors.textPrimary)
            if (badge != null) MlBadge(text = badge, tone = MlTone.Green)
        }
    }
}

/**
 * 기본 제스처에 이어 내가 만든 제스처까지, 제스처마다 이름·성공률 막대·숫자(%)를 한 줄씩 보여 준다.
 * 목표에 못 미치는 나만의 제스처는 막대와 숫자가 주황이다.
 */
@Composable
private fun RatesCard(rates: List<GestureSummary>, customRates: List<CustomGestureRate>) {
    ReportCard {
        Text(
            text = stringResource(R.string.st03_rates_title),
            style = MotionTheme.typography.headline,
            color = MotionTheme.colors.textPrimary,
        )
        Column(
            modifier = Modifier.padding(top = MotionSpacing.s14),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
        ) {
            rates.forEach { RateRow(name = stringResource(it.gesture.nameRes), percent = it.successRatePercent) }
            customRates.forEach {
                RateRow(name = it.name, percent = it.successRatePercent, needsAttention = it.needsAttention)
            }
        }
    }
}

@Composable
private fun RateRow(name: String, percent: Int, needsAttention: Boolean = false) {
    val colors = MotionTheme.colors
    val percentText = stringResource(R.string.gesture_success_rate, percent)
    val accent = if (needsAttention) colors.statusWarning else colors.brandPrimary

    Row(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "$name $percentText" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s10),
    ) {
        Text(
            text = name,
            style = MotionTheme.typography.body2,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(86.dp),
        )
        MlProgressBar(
            fraction = percent / 100f,
            modifier = Modifier.weight(1f),
            thickness = MotionSize.rateBar,
            track = colors.fillWeak,
            fill = accent,
        )
        Text(
            text = percentText,
            style = MotionTheme.typography.body2Strong,
            color = if (needsAttention) colors.statusWarning else colors.textPrimary,
        )
    }
}

/**
 * "'원 그리기'를 자주 되돌렸어요 · 감도를 낮춰 볼까요?" 카드. 주황 아이콘 타일, 두 줄 글, 오른쪽 "조정" 버튼.
 */
@Composable
private fun SuggestionCard(suggestion: SensitivitySuggestion, onAdjust: () -> Unit) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    MlCard(verticalPadding = MotionSpacing.s16) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
        ) {
            MlIconTile(icon = MlIcon.Alert, tone = MlTone.Orange)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
            ) {
                Text(
                    text = stringResource(
                        R.string.st03_suggestion_title,
                        suggestion.gestureName,
                        KoreanParticle.objective(suggestion.gestureName),
                    ),
                    style = type.body2Strong,
                    color = colors.textPrimary,
                )
                Text(text = stringResource(R.string.st03_suggestion_subtitle), style = type.caption1, color = colors.textTertiary)
            }
            MlButton(
                text = stringResource(R.string.st03_suggestion_action),
                onClick = onAdjust,
                style = MlButtonStyle.Weak,
                size = MlButtonSize.S,
            )
        }
    }
}

@Preview(name = "ST-03 기록과 리포트 (7일)", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun St03Preview() = MotionLayerTheme {
    ReportContent(
        range = ReportRange.Week,
        report = SampleData.reports.first { it.range == ReportRange.Week },
        onSelectRange = {},
        onLowerSensitivity = { _, _ -> },
        onBack = {},
    )
}

@Preview(name = "ST-04 감도 낮추기 제안 시트", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun St04Preview() = MotionLayerTheme {
    ReportContent(
        range = ReportRange.Week,
        report = SampleData.reports.first { it.range == ReportRange.Week },
        onSelectRange = {},
        onLowerSensitivity = { _, _ -> },
        onBack = {},
        initialSuggestionSheet = true,
    )
}
