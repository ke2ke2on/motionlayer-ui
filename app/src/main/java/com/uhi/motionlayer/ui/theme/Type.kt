package com.uhi.motionlayer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// 글꼴(`NotoSansKr`)은 `Font.kt`에 있다. 이 파일은 안드로이드 전용 코드 없이 두어 PC 에이전트(pc-agent)가 그대로 가져다 쓴다.

/** Figma는 줄 높이를 글자 가운데에 맞추므로 같은 방식으로 맞춘다. */
private val FigmaLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun figmaStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacingPercent: Int,
): TextStyle = TextStyle(
    fontFamily = NotoSansKr,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    // Figma의 자간은 % 단위(-2%, -1%). Compose에서는 em 비율로 쓴다.
    letterSpacing = (letterSpacingPercent / 100f).em,
    lineHeightStyle = FigmaLineHeightStyle,
)

/**
 * Figma "글자 — Noto Sans KR 텍스트 스타일" 13개.
 * 이름 뒤 숫자는 크기/줄 높이(px). 색은 스타일에 넣지 않고 쓰는 곳에서 `MotionTheme.colors`로 지정한다.
 * PC·TV용 스타일(PC/…, TV/…)은 폰 앱에서 쓰지 않아 옮기지 않았다.
 */
@Immutable
data class MotionTypography(
    val display: TextStyle = figmaStyle(32, 42, FontWeight.Bold, -2),        // Display · 32/42
    val title1: TextStyle = figmaStyle(26, 36, FontWeight.Bold, -2),         // Title/1 · 26/36
    val title2: TextStyle = figmaStyle(22, 31, FontWeight.Bold, -2),         // Title/2 · 22/31
    val title3: TextStyle = figmaStyle(20, 28, FontWeight.Bold, -2),         // Title/3 · 20/28
    val headline: TextStyle = figmaStyle(17, 25, FontWeight.Bold, -1),       // Headline · 17/25
    val body1: TextStyle = figmaStyle(16, 24, FontWeight.Normal, -1),        // Body/1 · 16/24
    val body1Strong: TextStyle = figmaStyle(16, 24, FontWeight.Medium, -1),  // Body/1 Strong · 16/24
    val body2: TextStyle = figmaStyle(15, 22, FontWeight.Normal, -1),        // Body/2 · 15/22
    val body2Strong: TextStyle = figmaStyle(15, 22, FontWeight.Medium, -1),  // Body/2 Strong · 15/22
    val caption1: TextStyle = figmaStyle(13, 19, FontWeight.Normal, -1),     // Caption/1 · 13/19
    val caption1Strong: TextStyle = figmaStyle(13, 19, FontWeight.Medium, -1), // Caption/1 Strong · 13/19
    val caption2: TextStyle = figmaStyle(12, 17, FontWeight.Normal, -1),     // Caption/2 · 12/17
    val caption2Strong: TextStyle = figmaStyle(12, 17, FontWeight.Medium, -1), // Caption/2 Strong · 12/17
)

internal val LocalMotionTypography = staticCompositionLocalOf { MotionTypography() }

/** Material 컴포넌트(시트, 대화상자 등)가 기본으로 쓰는 글자 스타일도 같은 폰트로 맞춘다. */
internal fun MotionTypography.toMaterial(): Typography = Typography(
    displayLarge = display,
    headlineLarge = title1,
    headlineMedium = title2,
    headlineSmall = title3,
    titleLarge = headline,
    titleMedium = body1Strong,
    titleSmall = body2Strong,
    bodyLarge = body1,
    bodyMedium = body2,
    bodySmall = caption1,
    labelLarge = body2Strong,
    labelMedium = caption1Strong,
    labelSmall = caption2Strong,
)
