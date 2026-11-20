package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionShadows
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.theme.motionShadow

/** Figma Slider의 손잡이 지름이자 슬라이더 전체 높이. */
private val ThumbSize = 28.dp

/**
 * Figma `Slider`. 높이 28, 선(트랙) 두께 6에 손잡이 지름 28(흰색, 얇은 테두리와 그림자).
 * 채워진 부분은 `brand/primary`, 나머지는 `fill/strong`.
 *
 * 누르고 끄는 동작과 스크린리더 조작은 Material 슬라이더가 처리하고, 모양만 Figma에 맞춘다.
 * 모양을 직접 그리는 `thumb`·`track` 지정이 아직 "실험적"으로 표시돼 있어 OptIn을 붙였다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MlSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val colors = MotionTheme.colors

    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        modifier = modifier
            .height(ThumbSize)
            // Material 슬라이더는 손잡이 지름(28)만큼 선을 안쪽으로 줄여 그린다. Figma는 선이 가로 전체(312)를 쓰고
            // 손잡이가 끝에서 선 밖으로 반쯤 나가므로, 슬라이더를 28만큼 넓게 만들어 양쪽으로 14씩 내보낸다.
            .layout { measurable, constraints ->
                val extra = ThumbSize.roundToPx()
                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = constraints.maxWidth + extra,
                        maxWidth = constraints.maxWidth + extra,
                    ),
                )
                layout(constraints.maxWidth, placeable.height) { placeable.place(-extra / 2, 0) }
            },
        thumb = {
            Box(
                modifier = Modifier
                    .size(ThumbSize)
                    // Figma Shadow/Knob (y 2, 퍼짐 6, 16%)
                    .motionShadow(MotionShadows.knob, CircleShape)
                    .background(colors.bgBase, CircleShape)
                    .border(MotionBorder.thin, colors.lineDivider, CircleShape),
            )
        },
        track = { state ->
            val fraction = (state.value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MotionSize.progressBar)
                    .clip(RoundedCornerShape(MotionRadius.r3))
                    .background(colors.fillStrong),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .background(colors.brandPrimary),
                )
            }
        },
    )
}
