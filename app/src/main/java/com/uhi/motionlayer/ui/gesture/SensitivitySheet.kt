package com.uhi.motionlayer.ui.gesture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.uhi.motionlayer.R
import com.uhi.motionlayer.ui.components.MlBottomSheet
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlButtonSize
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlSlider
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 감도를 고르는 시트(Figma ST-04 "감도를 낮출까요?", GS-11의 "감도" 줄이 같이 쓴다).
 * 맨 위에 제목과 설명, 가운데에 슬라이더(왼쪽 "둔감하게", 오른쪽 "민감하게"), 아래에 큰 버튼과 (있으면) 글자만 있는 작은 버튼이 있다.
 * 슬라이더는 시트 안에서만 움직이고, 큰 버튼을 눌러야 고른 값이 [onConfirm]으로 넘어간다.
 * 그냥 닫으면(막·아래로 밀기·뒤로 가기·작은 버튼) 고른 값은 버려진다.
 *
 * @param sensitivity 시트를 열 때의 감도. 0.0(둔감하게)~1.0(민감하게)
 * @param secondaryText 글자만 있는 작은 버튼. null이면 큰 버튼 하나만 둔다. 누르면 [onDismiss]와 같다
 */
@Composable
fun SensitivitySheet(
    title: String,
    message: String,
    sensitivity: Float,
    confirmText: String,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String? = null,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    var draft by remember { mutableFloatStateOf(sensitivity.coerceIn(0f, 1f)) }

    MlBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier.padding(
                start = MotionSpacing.screenHorizontal,
                end = MotionSpacing.screenHorizontal,
                top = MotionSpacing.s8,
                bottom = MotionSpacing.s16,
            ),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
        ) {
            Text(text = title, style = type.title3, color = colors.textPrimary)
            Text(text = message, style = type.body2, color = colors.textSecondary)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, bottom = MotionSpacing.s8),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
        ) {
            MlSlider(value = draft, onValueChange = { draft = it }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.gs02_dull),
                    style = type.caption1,
                    color = colors.textTertiary,
                    modifier = Modifier.weight(1f),
                )
                Text(text = stringResource(R.string.gs02_sensitive), style = type.caption1, color = colors.textTertiary)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s8),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s4),
        ) {
            MlButton(text = confirmText, onClick = { onConfirm(draft) }, modifier = Modifier.fillMaxWidth())
            if (secondaryText != null) {
                MlButton(
                    text = secondaryText,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    style = MlButtonStyle.Ghost,
                    size = MlButtonSize.M,
                )
            }
        }
    }
}
