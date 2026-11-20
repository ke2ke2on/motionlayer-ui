package com.uhi.motionlayer.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.VibrationFeedback
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.components.MlSlider
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** 진동(Figma ST-02). 세기를 정하고, 세 가지 진동을 눌러서 미리 느껴 볼 수 있다. */
@Composable
fun VibrationRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val strength by viewModel.vibrationStrength.collectAsStateWithLifecycle()
    VibrationContent(
        strength = strength,
        onStrengthChange = viewModel::setVibrationStrength,
        onFeel = viewModel::feel,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * 진동 설정. 상태를 받아 그리기만 한다.
 *
 * Figma 아래쪽의 "기기를 바꿀 때"(기기별 진동 박자, DV-07)와 그 안내 배너는 기기 기능(Could)이라 두지 않는다.
 */
@Composable
fun VibrationContent(
    strength: Float,
    onStrengthChange: (Float) -> Unit,
    onFeel: (VibrationFeedback) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val sliderDescription = stringResource(R.string.st02_slider_description)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = stringResource(R.string.st02_title), leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            MlSectionHeader(title = stringResource(R.string.st02_strength_header))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MotionSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
            ) {
                MlSlider(
                    value = strength,
                    onValueChange = onStrengthChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = sliderDescription },
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.vibration_strength_weak),
                        style = type.caption1,
                        color = colors.textTertiary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(R.string.vibration_strength_strong),
                        style = type.caption1,
                        color = colors.textTertiary,
                    )
                }
            }

            MlSectionHeader(title = stringResource(R.string.st02_feel_header))
            FeelRow(
                title = stringResource(R.string.st02_recognized_title),
                subtitle = stringResource(R.string.st02_recognized_subtitle),
                tone = MlTone.Blue,
                onClick = { onFeel(VibrationFeedback.Recognized) },
            )
            FeelRow(
                title = stringResource(R.string.st02_executed_title),
                subtitle = stringResource(R.string.st02_executed_subtitle),
                tone = MlTone.Green,
                onClick = { onFeel(VibrationFeedback.Executed) },
            )
            FeelRow(
                title = stringResource(R.string.st02_failed_title),
                subtitle = stringResource(R.string.st02_failed_subtitle),
                tone = MlTone.Red,
                onClick = { onFeel(VibrationFeedback.Failed) },
            )
        }
    }
}

@Composable
private fun FeelRow(title: String, subtitle: String, tone: MlTone, onClick: () -> Unit) {
    MlListRow(
        title = title,
        subtitle = subtitle,
        leading = MlListRowLeading.Icon(MlIcon.Vibrate, tone),
        trailing = MlListRowTrailing.Value(stringResource(R.string.st02_feel_action)),
        onClick = onClick,
    )
}

@Preview(name = "ST-02 진동", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun St02Preview() = MotionLayerTheme {
    VibrationContent(strength = 0.61f, onStrengthChange = {}, onFeel = {}, onBack = {})
}
