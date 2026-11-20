package com.uhi.motionlayer.ui.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.uhi.motionlayer.ui.components.MlCard
import com.uhi.motionlayer.ui.components.MlCardTone
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlCtaFrame
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 배터리 설정 안내(Figma ON-07). 설정과 권한 목록에서도, 첫 실행 안내에서도 열린다.
 * "설정 열기"를 눌러 설정 앱으로 가서 바꾸고 돌아오면 설정이 적용된 것이 확인되는 순간 [onFinished]를 부른다.
 * 이미 적용돼 있는 채로 열었을 때는 부르지 않는다.
 *
 * @param onBack 위쪽 뒤로 가기 화살표
 * @param onFinished "나중에 할게요"를 눌렀거나 설정이 적용된 것이 확인됐다. 보통 안내를 닫고 다음으로 간다
 */
@Composable
fun BatteryGuideRoute(
    onBack: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BatteryGuideViewModel = viewModel(factory = BatteryGuideViewModel.Factory),
) {
    val applied by viewModel.applied.collectAsStateWithLifecycle()
    var openedSettings by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(applied, openedSettings) {
        if (openedSettings && applied) onFinished()
    }

    BatteryGuideScreen(
        onOpenSettings = {
            openedSettings = true
            viewModel.onOpenSettings()
        },
        onLater = onFinished,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * Figma ON-07 배터리 설정 안내. 삼성 폰이 화면이 꺼진 앱을 재우는 것을 막으려면 어떻게 바꾸는지 세 단계로 알려 준다.
 * 설정 화면은 제조사가 그리므로 만들지 않고, "설정 열기"로 설정 앱을 연다.
 */
@Composable
fun BatteryGuideScreen(
    onOpenSettings: () -> Unit,
    onLater: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    MlCtaFrame(
        modifier = modifier,
        topBar = { MlTopBar(title = null, leading = MlTopBarLeading.Back, onLeadingClick = onBack) },
        primaryText = stringResource(R.string.on07_open_settings),
        onPrimary = onOpenSettings,
        secondaryText = stringResource(R.string.permission_later),
        onSecondary = onLater,
        contentTopPadding = MotionSpacing.s8,
    ) {
        // 본문 요소 사이는 8이고, 단계 카드는 위로 20, 안내 문구는 위로 8이 더 떨어져 있다.
        Column(verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
            Text(text = stringResource(R.string.on07_title), style = type.title2, color = colors.textPrimary)
            Text(text = stringResource(R.string.on07_description), style = type.body2, color = colors.textSecondary)
            StepsCard(
                steps = listOf(
                    stringResource(R.string.on07_step_1),
                    stringResource(R.string.on07_step_2),
                    stringResource(R.string.on07_step_3),
                ),
                modifier = Modifier.padding(top = MotionSpacing.s20),
            )
            MlBanner(
                message = stringResource(R.string.on07_note),
                tone = MlBannerTone.Neutral,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MotionSpacing.s8),
            )
        }
    }
}

/** 번호 붙은 단계들을 회색 판에 담은 카드. 번호는 파란 동그라미 안에 있다. 카드 안 여백 20, 단계 사이 18. */
@Composable
private fun StepsCard(steps: List<String>, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    MlCard(
        modifier = modifier,
        tone = MlCardTone.Gray,
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s18),
    ) {
        steps.forEachIndexed { index, step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colors.brandWeak),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "${index + 1}", style = type.caption2Strong, color = colors.brandPressed)
                }
                Text(
                    text = step,
                    style = type.body2,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(name = "ON-07 배터리 설정 안내", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun On07Preview() = MotionLayerTheme {
    BatteryGuideScreen(onOpenSettings = {}, onLater = {}, onBack = {})
}
