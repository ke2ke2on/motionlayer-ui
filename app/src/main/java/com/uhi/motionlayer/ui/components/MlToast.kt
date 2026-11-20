package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionShadows
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.theme.motionShadow

/**
 * Figma `Toast`. 어두운 바탕(`surface/toast`)에 아이콘 + 한 줄 문구 + 선택적 행동 글자("되돌리기").
 * 좌우 18·위아래 15, 모서리 16. 앱 화면이 켜져 있을 때만 결과를 알려 준다(SY-03).
 * 화면에 띄우고 사라지게 하는 것은 쓰는 곳에서 처리하고, 여기서는 모양만 그린다.
 *
 * @param icon 문구 앞 아이콘. null이면 아이콘 없이 문구만 보여 준다(Figma `Show icon` 끔: 실패·대상 변경 토스트)
 * @param iconTint 아이콘 색. SY-03의 모든 토스트가 `text/inverse`(흰색)라 기본값이 그것이다.
 */
@Composable
fun MlToast(
    message: String,
    modifier: Modifier = Modifier,
    icon: MlIcon? = MlIcon.Check,
    iconTint: Color = MotionTheme.colors.textInverse,
    actionText: String? = null,
    onActionClick: () -> Unit = {},
) {
    val colors = MotionTheme.colors
    val shape = RoundedCornerShape(MotionRadius.r16)

    Row(
        modifier = modifier
            // Figma Shadow/Floating (y 8, 퍼짐 24, 10%)
            .motionShadow(MotionShadows.floating, shape)
            .background(colors.surfaceToast, shape)
            // 토스트가 뜨면 스크린리더가 문구를 읽어 준다(보고서 6.4 "토스트는 접근성 이벤트로도 읽힘").
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(horizontal = MotionSpacing.s18, vertical = MotionSpacing.s15),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s10),
    ) {
        if (icon != null) MlIconView(icon = icon, tint = iconTint, size = MotionIconSize.s20)
        Text(
            text = message,
            style = MotionTheme.typography.body2,
            color = colors.textInverse,
            modifier = Modifier.weight(1f),
        )
        if (actionText != null) {
            Text(
                text = actionText,
                style = MotionTheme.typography.body2Strong,
                color = colors.brandPrimary,
                modifier = Modifier.clickable(role = Role.Button, onClick = onActionClick),
            )
        }
    }
}
