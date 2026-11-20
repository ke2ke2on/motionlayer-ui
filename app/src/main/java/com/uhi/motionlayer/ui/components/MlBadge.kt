package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** 배지와 아이콘 타일, 배너가 같이 쓰는 색 묶음. Figma의 Tone 5종. */
enum class MlTone { Blue, Gray, Green, Red, Orange }

/** Figma `Badge` (Tone 5종). 작은 상태 표시. */
@Composable
fun MlBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: MlTone = MlTone.Blue,
) {
    val colors = MotionTheme.colors
    val (container, content) = when (tone) {
        MlTone.Blue -> colors.brandWeak to colors.brandPrimary
        MlTone.Gray -> colors.fillWeak to colors.textSecondary
        MlTone.Green -> colors.statusSuccessWeak to colors.statusSuccess
        MlTone.Red -> colors.statusDangerWeak to colors.statusDanger
        MlTone.Orange -> colors.statusWarningWeak to colors.statusWarning
    }

    Text(
        text = text,
        style = MotionTheme.typography.caption2Strong,
        color = content,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(MotionRadius.r6))
            .background(container)
            .padding(horizontal = MotionSpacing.s8, vertical = MotionSpacing.s3),
    )
}
