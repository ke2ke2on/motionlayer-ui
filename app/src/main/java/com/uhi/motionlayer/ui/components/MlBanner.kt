package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** Figma `Banner`의 Tone. 아이콘은 톤마다 정해져 있다. */
enum class MlBannerTone { Info, Success, Warning, Danger, Neutral }

/**
 * Figma `Banner` (Tone 5종). 안내 문구 한 덩어리. 좌우 16·위아래 14, 아이콘 20 + 문구, 모서리 14.
 * 가로 폭은 쓰는 곳에서 정한다(보통 화면 폭 - 좌우 여백 24씩 = 312).
 */
@Composable
fun MlBanner(
    message: String,
    modifier: Modifier = Modifier,
    tone: MlBannerTone = MlBannerTone.Info,
) {
    val colors = MotionTheme.colors
    val (container, accent, icon) = when (tone) {
        MlBannerTone.Info -> Triple(colors.brandWeak, colors.brandPrimary, MlIcon.Info)
        MlBannerTone.Success -> Triple(colors.statusSuccessWeak, colors.statusSuccess, MlIcon.Check)
        MlBannerTone.Warning -> Triple(colors.statusWarningWeak, colors.statusWarning, MlIcon.Alert)
        MlBannerTone.Danger -> Triple(colors.statusDangerWeak, colors.statusDanger, MlIcon.Alert)
        MlBannerTone.Neutral -> Triple(colors.fillWeak, colors.textSecondary, MlIcon.Info)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(MotionRadius.r14))
            .background(container)
            .padding(horizontal = MotionSpacing.s16, vertical = MotionSpacing.s14),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s10),
    ) {
        MlIconView(icon = icon, tint = accent, size = MotionIconSize.s20)
        Text(
            text = message,
            style = MotionTheme.typography.body2,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}
