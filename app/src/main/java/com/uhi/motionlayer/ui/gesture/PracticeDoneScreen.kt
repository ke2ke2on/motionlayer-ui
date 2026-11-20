package com.uhi.motionlayer.ui.gesture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import com.uhi.motionlayer.ui.components.MlCard
import com.uhi.motionlayer.ui.components.MlCardTone
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.BasicGesture
import com.uhi.motionlayer.domain.model.PRACTICE_SUCCESSES_REQUIRED
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlResultBadge
import com.uhi.motionlayer.ui.components.MlResultTone
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** Figma GS-10 연습 완료. 4가지를 모두 익혔다고 알려 주고 홈으로 보낸다. */
@Composable
fun PracticeDoneScreen(
    onHome: () -> Unit,
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
            Text(text = stringResource(R.string.gs10_title), style = type.title2, color = colors.textPrimary)
            Box(Modifier.height(MotionSpacing.s8))
            Text(text = stringResource(R.string.gs10_description), style = type.body2, color = colors.textSecondary)
            Box(Modifier.height(MotionSpacing.s24))
            // 모든 제스처가 다 익힌 상태이므로 점 3개가 모두 채워진다.
            MlCard(tone = MlCardTone.Gray, verticalPadding = MotionSpacing.s8) {
                BasicGesture.entries.forEach { gesture ->
                    ProgressRow(gesture = gesture, successes = PRACTICE_SUCCESSES_REQUIRED)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, top = MotionSpacing.s12, bottom = MotionSpacing.s8),
        ) {
            MlButton(text = stringResource(R.string.gs10_home), onClick = onHome, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(name = "GS-10 연습 완료", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs10Preview() = MotionLayerTheme { PracticeDoneScreen(onHome = {}) }
