package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * Figma `IconTile` (Tone 5종). 40×40, 모서리 12, 안에 24dp 아이콘.
 *
 * 아이콘 색은 Gray가 `text/secondary`, Blue가 `brand/primary`인 것을 Figma에서 확인했고,
 * 나머지 톤은 배너와 같은 규칙(톤의 진한 색)으로 맞췄다.
 */
@Composable
fun MlIconTile(
    icon: MlIcon,
    modifier: Modifier = Modifier,
    tone: MlTone = MlTone.Gray,
    contentDescription: String? = null,
) {
    val colors = MotionTheme.colors
    val (container, content) = when (tone) {
        MlTone.Gray -> colors.fillWeak to colors.textSecondary
        MlTone.Blue -> colors.brandWeak to colors.brandPrimary
        MlTone.Green -> colors.statusSuccessWeak to colors.statusSuccess
        MlTone.Red -> colors.statusDangerWeak to colors.statusDanger
        MlTone.Orange -> colors.statusWarningWeak to colors.statusWarning
    }

    Box(
        modifier = modifier
            .size(MotionSize.iconTile)
            .clip(RoundedCornerShape(MotionRadius.r12))
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        MlIconView(icon = icon, tint = content, contentDescription = contentDescription)
    }
}

/**
 * Figma `AppIcon`. 앱 아이콘 대신 한 글자를 색 타일 위에 보여 주는 자리 표시 아이콘이다.
 * 실제 앱 아이콘 이미지는 데이터가 오면 교체한다. 색은 앱마다 달라서 [color]로 받는다.
 *
 * @param large 앱 상세(AP-02) 맨 위의 큰 크기(52×52, 모서리 16)로 그린다. 아니면 40×40, 모서리 12다
 */
@Composable
fun MlAppIcon(
    letter: String,
    modifier: Modifier = Modifier,
    color: Color = MotionTheme.colors.brandPrimary,
    large: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(if (large) MotionSize.iconTileLarge else MotionSize.iconTile)
            .clip(RoundedCornerShape(if (large) MotionRadius.r16 else MotionRadius.r12))
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style = MotionTheme.typography.body1Strong,
            color = MotionTheme.colors.textInverse,
        )
    }
}
