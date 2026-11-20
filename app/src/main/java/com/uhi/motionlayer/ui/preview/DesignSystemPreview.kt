package com.uhi.motionlayer.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 디자인 시스템 확인용 화면. Figma "01 파운데이션"의 색 23개와 글자 스타일 13개를 같은 이름으로 보여 준다.
 * 개발 중 값이 Figma와 맞는지 눈으로 비교하는 용도이며, 앱의 실제 화면이 아니다.
 */
@Composable
fun DesignSystemPreview(modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s24),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
    ) {
        SectionTitle("색 — Color 변수 (Light)")
        listOf(
            "bg/base" to colors.bgBase,
            "bg/grouped" to colors.bgGrouped,
            "bg/subtle" to colors.bgSubtle,
            "fill/weak" to colors.fillWeak,
            "fill/strong" to colors.fillStrong,
            "text/primary" to colors.textPrimary,
            "text/secondary" to colors.textSecondary,
            "text/tertiary" to colors.textTertiary,
            "text/disabled" to colors.textDisabled,
            "text/inverse" to colors.textInverse,
            "line/divider" to colors.lineDivider,
            "line/strong" to colors.lineStrong,
            "brand/primary" to colors.brandPrimary,
            "brand/pressed" to colors.brandPressed,
            "brand/weak" to colors.brandWeak,
            "status/success" to colors.statusSuccess,
            "status/success-weak" to colors.statusSuccessWeak,
            "status/warning" to colors.statusWarning,
            "status/warning-weak" to colors.statusWarningWeak,
            "status/danger" to colors.statusDanger,
            "status/danger-weak" to colors.statusDangerWeak,
            "surface/toast" to colors.surfaceToast,
            "overlay/dim" to colors.overlayDim,
        ).forEach { (name, color) -> Swatch(name, color) }

        SectionTitle("글자 — Noto Sans KR 텍스트 스타일")
        listOf(
            "Display · 32/42" to type.display,
            "Title/1 · 26/36" to type.title1,
            "Title/2 · 22/31" to type.title2,
            "Title/3 · 20/28" to type.title3,
            "Headline · 17/25" to type.headline,
            "Body/1 · 16/24" to type.body1,
            "Body/1 Strong · 16/24" to type.body1Strong,
            "Body/2 · 15/22" to type.body2,
            "Body/2 Strong · 15/22" to type.body2Strong,
            "Caption/1 · 13/19" to type.caption1,
            "Caption/1 Strong · 13/19" to type.caption1Strong,
            "Caption/2 · 12/17" to type.caption2,
            "Caption/2 Strong · 12/17" to type.caption2Strong,
        ).forEach { (name, style) -> TypeSample(name, style) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, MotionTheme.typography.title3, MotionTheme.colors.textPrimary)
}

@Composable
private fun Swatch(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(MotionSpacing.s8))
                .border(1.dp, MotionTheme.colors.lineDivider, RoundedCornerShape(MotionSpacing.s8)),
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(name, MotionTheme.typography.body2Strong, MotionTheme.colors.textPrimary)
            Text(color.toHexString(), MotionTheme.typography.caption1, MotionTheme.colors.textTertiary)
        }
    }
}

@Composable
private fun TypeSample(name: String, style: TextStyle) {
    Column {
        Text(name, MotionTheme.typography.caption1, MotionTheme.colors.textTertiary)
        Text("폰을 움직여서 조작해 보세요", style, MotionTheme.colors.textPrimary)
    }
}

/** `style`을 첫 번째 인자 근처에 두는 짧은 도우미. 이 파일 안에서만 쓴다. */
@Composable
private fun Text(text: String, style: TextStyle, color: Color) {
    androidx.compose.material3.Text(text = text, style = style, color = color)
}

private fun Color.toHexString(): String {
    val argb = toArgb()
    val alpha = (argb ushr 24) and 0xFF
    val rgb = String.format("#%06X", argb and 0xFFFFFF)
    return if (alpha == 0xFF) rgb else "$rgb · ${(alpha * 100 + 127) / 255}%"
}

@Preview(name = "Design system", showBackground = true, widthDp = 360, heightDp = 1800)
@Composable
private fun DesignSystemPreviewPreview() {
    MotionLayerTheme { DesignSystemPreview() }
}
