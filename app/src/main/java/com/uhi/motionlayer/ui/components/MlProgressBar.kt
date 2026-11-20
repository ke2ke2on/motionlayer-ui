package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 가로로 긴 진행 막대. 바탕 위에 [fill] 색이 [fraction](0~1)만큼 찬다. 모서리는 두께의 절반이라 끝이 완전히 둥글다
 * (보내는 중 PR-14는 두께 6·모서리 3, 기록의 성공률 막대 ST-03은 두께 8·모서리 4).
 *
 * @param thickness 막대 두께
 * @param track 채워지지 않은 부분의 색. 기본은 `fill/strong`
 * @param fill 채워진 부분의 색. 기본은 `brand/primary`
 */
@Composable
fun MlProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    thickness: Dp = MotionSize.progressBar,
    track: Color = MotionTheme.colors.fillStrong,
    fill: Color = MotionTheme.colors.brandPrimary,
) {
    // 두께가 얼마든 양 끝이 완전히 둥글다(모서리 = 두께의 절반)
    val shape = CircleShape
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .clip(shape)
            .background(track),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = fraction.coerceIn(0f, 1f))
                .clip(shape)
                .background(fill),
        )
    }
}
