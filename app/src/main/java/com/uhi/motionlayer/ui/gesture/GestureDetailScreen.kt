package com.uhi.motionlayer.ui.gesture

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.BasicGesture
import com.uhi.motionlayer.domain.model.GestureDetail
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.components.MlSlider
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** 기본 제스처 상세(Figma GS-02). 제스처 목록(GS-01)의 줄을 누르면 열린다. */
@Composable
fun GestureDetailRoute(
    gesture: BasicGesture,
    onBack: () -> Unit,
    onPractice: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GestureDetailViewModel = viewModel(key = gesture.name, factory = GestureDetailViewModel.factory(gesture)),
) {
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    GestureDetailContent(
        detail = detail,
        onBack = onBack,
        onSensitivityChange = viewModel::onSensitivityChange,
        onPractice = onPractice,
        modifier = modifier,
    )
}

/**
 * 제스처 이름과 설명, 그림, 최근 7일 인식률·되돌림, 감도 슬라이더, 아래 "연습하기" 버튼. 상태를 받아 그리기만 한다.
 *
 * Figma에는 "좌우로 휙"(GS-02)과 "세게 흔들기"(GS-17)가 그려져 있다. 두 번 톡톡·기울임의 설명 문구와 그림(휙은 좌우 화살표,
 * 나머지는 제스처 아이콘 한 쌍)은 같은 모양으로 직접 정했다. 흔들기만 되돌린 횟수 이름과 감도 안내가 따로 있다.
 */
@Composable
fun GestureDetailContent(
    detail: GestureDetail,
    onBack: () -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onPractice: () -> Unit,
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
        MlTopBar(title = null, leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier.padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s4,
                ),
            ) {
                Text(text = stringResource(detail.gesture.nameRes), style = type.title2, color = colors.textPrimary)
                Text(
                    text = stringResource(detail.gesture.descriptionRes),
                    style = type.body2,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = MotionSpacing.s8),
                )
                GestureIllustration(
                    gesture = detail.gesture,
                    modifier = Modifier.padding(top = MotionSpacing.s20),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MotionSpacing.s12),
                    horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
                ) {
                    StatCard(
                        label = stringResource(R.string.gs02_stat_rate),
                        value = stringResource(R.string.gesture_success_rate, detail.successRatePercent),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(detail.gesture.undoneLabelRes),
                        value = stringResource(R.string.gs02_stat_count, detail.undoneCount),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            MlSectionHeader(title = stringResource(R.string.gs02_sensitivity_header))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MotionSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
            ) {
                MlSlider(
                    value = detail.sensitivity,
                    onValueChange = onSensitivityChange,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.gs02_dull),
                        style = type.caption1,
                        color = colors.textTertiary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(text = stringResource(R.string.gs02_sensitive), style = type.caption1, color = colors.textTertiary)
                }
                Text(
                    text = stringResource(detail.gesture.sensitivityHintRes),
                    style = type.caption1,
                    color = colors.textSecondary,
                )
            }
            Box(Modifier.height(MotionSpacing.s16))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s12,
                    bottom = MotionSpacing.s8,
                ),
        ) {
            MlButton(
                text = stringResource(R.string.gs02_practice),
                onClick = onPractice,
                modifier = Modifier.fillMaxWidth(),
                style = MlButtonStyle.Weak,
            )
        }
    }
}

/** 폰 그림 양옆에 아이콘 하나씩. 왼쪽은 흐리게, 오른쪽은 파랗게 해서 "이쪽으로"를 보여 준다. */
@Composable
private fun GestureIllustration(gesture: BasicGesture, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val (left, right) = when (gesture) {
        BasicGesture.Swipe -> MlIcon.ArrowLeft to MlIcon.ArrowRight
        else -> gesture.icon to gesture.icon
    }
    val phoneShape = RoundedCornerShape(MotionRadius.r14)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(RoundedCornerShape(MotionRadius.r20))
            .background(colors.bgGrouped)
            .padding(vertical = MotionSpacing.s28),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s20, Alignment.CenterHorizontally),
    ) {
        MlIconView(icon = left, tint = colors.textDisabled, size = MotionIconSize.s32)
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 112.dp)
                .background(colors.bgBase, phoneShape)
                .border(MotionBorder.thick, colors.lineStrong, phoneShape),
        )
        MlIconView(icon = right, tint = colors.brandPrimary, size = MotionIconSize.s32)
    }
}

/** 테두리만 있는 작은 카드. 위에 설명, 아래에 큰 값. 나만의 제스처 상세(GS-11)도 같이 쓴다. */
@Composable
internal fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = modifier
            .border(MotionBorder.thin, colors.lineDivider, RoundedCornerShape(MotionRadius.r16))
            .padding(horizontal = MotionSpacing.s16, vertical = MotionSpacing.s14),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
    ) {
        Text(text = label, style = type.caption1, color = colors.textTertiary)
        Text(text = value, style = type.title3, color = colors.textPrimary)
    }
}

@get:StringRes
private val BasicGesture.descriptionRes: Int
    get() = when (this) {
        BasicGesture.Swipe -> R.string.gs02_desc_swipe
        BasicGesture.DoubleTap -> R.string.gs02_desc_double_tap
        BasicGesture.Tilt -> R.string.gs02_desc_tilt
        BasicGesture.Shake -> R.string.gs02_desc_shake
    }

/** 되돌린 횟수 카드의 이름. 흔들기(되돌리기)는 "되돌린 횟수", 나머지는 "되돌림"이다. */
@get:StringRes
private val BasicGesture.undoneLabelRes: Int
    get() = if (this == BasicGesture.Shake) R.string.gs17_stat_undone else R.string.gs02_stat_undone

/** 감도 아래 안내 글. 흔들기는 걷다가 저절로 되돌려지는 경우를 말하고, 나머지는 잘못 넘어가는 경우를 말한다. */
@get:StringRes
private val BasicGesture.sensitivityHintRes: Int
    get() = if (this == BasicGesture.Shake) R.string.gs17_sensitivity_hint else R.string.gs02_sensitivity_hint

@Preview(name = "GS-02 기본 제스처 상세", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs02Preview() = MotionLayerTheme {
    GestureDetailContent(
        detail = GestureDetail(BasicGesture.Swipe, successRatePercent = 97, undoneCount = 2, sensitivity = 0.61f),
        onBack = {},
        onSensitivityChange = {},
        onPractice = {},
    )
}

@Preview(name = "GS-17 기본 제스처 상세 · 세게 흔들기", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs17Preview() = MotionLayerTheme {
    GestureDetailContent(
        detail = GestureDetail(BasicGesture.Shake, successRatePercent = 92, undoneCount = 4, sensitivity = 0.5f),
        onBack = {},
        onSensitivityChange = {},
        onPractice = {},
    )
}
