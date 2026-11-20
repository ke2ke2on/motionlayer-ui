package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** Figma Button의 Style. 비활성(Disabled)은 별도 스타일이 아니라 `enabled = false`로 표현한다. */
enum class MlButtonStyle { Primary, Secondary, Weak, Ghost, Danger }

/** Figma Button의 Size. L은 화면 하단 CTA, M은 카드 안, S는 작은 인라인 버튼. */
enum class MlButtonSize { L, M, S }

private class ButtonSpec(val height: Dp, val horizontalPadding: Dp, val radius: Dp, val textStyle: TextStyle)

/**
 * Figma `Button` (Style 6 × Size 3).
 *
 * 화면 하단 CTA처럼 가로를 꽉 채우려면 `Modifier.fillMaxWidth()`를 넘긴다(Figma에서는 FILL).
 * 눌렀을 때 색은 Figma에 변형이 없어서 Primary(`brand/pressed`)와 Secondary(`fill/strong`)만 토큰으로 바꾸고,
 * 나머지는 기본 물결 효과를 쓴다.
 */
@Composable
fun MlButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: MlButtonStyle = MlButtonStyle.Primary,
    size: MlButtonSize = MlButtonSize.L,
    enabled: Boolean = true,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val spec = when (size) {
        MlButtonSize.L -> ButtonSpec(MotionSize.buttonL, MotionSpacing.s20, MotionRadius.r16, type.body1Strong)
        MlButtonSize.M -> ButtonSpec(MotionSize.buttonM, MotionSpacing.s18, MotionRadius.r14, type.body2Strong)
        MlButtonSize.S -> ButtonSpec(MotionSize.buttonS, MotionSpacing.s14, MotionRadius.r10, type.caption1Strong)
    }

    val container: Color
    val content: Color
    if (!enabled) {
        container = colors.fillWeak
        content = colors.textDisabled
    } else {
        when (style) {
            MlButtonStyle.Primary -> {
                container = if (pressed) colors.brandPressed else colors.brandPrimary
                content = colors.textInverse
            }
            MlButtonStyle.Secondary -> {
                container = if (pressed) colors.fillStrong else colors.fillWeak
                content = colors.textSecondary
            }
            MlButtonStyle.Weak -> {
                container = colors.brandWeak
                content = colors.brandPrimary
            }
            MlButtonStyle.Ghost -> {
                container = Color.Transparent
                content = colors.brandPrimary
            }
            MlButtonStyle.Danger -> {
                container = colors.statusDanger
                content = colors.textInverse
            }
        }
    }

    Box(
        modifier = modifier
            // 높이는 Figma 값이 최소다. 시스템 글자를 키우면(보고서 6.3) 버튼도 같이 커져서 글자가 잘리지 않는다.
            // S(36)의 누르는 영역은 Compose 기본 최소 터치 크기가 위아래 6씩 넓혀 48이 된다(보고서 6.2, 접근성 정보로 확인).
            .heightIn(min = spec.height)
            .clip(RoundedCornerShape(spec.radius))
            .background(container)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = spec.horizontalPadding, vertical = MotionSpacing.s8),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = spec.textStyle,
            color = content,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
