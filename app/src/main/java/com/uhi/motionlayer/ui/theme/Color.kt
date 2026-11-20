package com.uhi.motionlayer.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Figma "01 파운데이션 > Color 변수 (Light)" 23개를 그대로 옮긴 색 토큰.
 * `text/tertiary`·`brand/primary`·`brand/pressed`·`status/danger`는 글자와 바탕의 명암비를 맞춘 값이다.
 *
 * 이름은 Figma 변수 이름(`bg/base`, `text/primary` ...)을 camelCase로 바꾼 것이다.
 * 화면 코드에서는 색을 직접 쓰지 말고 `MotionTheme.colors.xxx`로만 가져온다.
 * Figma에 다크 모드가 없으므로 라이트 한 벌만 둔다.
 */
@Immutable
data class MotionColors(
    // 배경
    val bgBase: Color,          // bg/base
    val bgGrouped: Color,       // bg/grouped
    val bgSubtle: Color,        // bg/subtle

    // 채움
    val fillWeak: Color,        // fill/weak
    val fillStrong: Color,      // fill/strong

    // 글자
    val textPrimary: Color,     // text/primary
    val textSecondary: Color,   // text/secondary
    val textTertiary: Color,    // text/tertiary
    val textDisabled: Color,    // text/disabled
    val textInverse: Color,     // text/inverse

    // 선
    val lineDivider: Color,     // line/divider
    val lineStrong: Color,      // line/strong

    // 브랜드
    val brandPrimary: Color,    // brand/primary
    val brandPressed: Color,    // brand/pressed
    val brandWeak: Color,       // brand/weak

    // 상태
    val statusSuccess: Color,       // status/success
    val statusSuccessWeak: Color,   // status/success-weak
    val statusWarning: Color,       // status/warning
    val statusWarningWeak: Color,   // status/warning-weak
    val statusDanger: Color,        // status/danger
    val statusDangerWeak: Color,    // status/danger-weak

    // 표면, 오버레이
    val surfaceToast: Color,    // surface/toast
    val overlayDim: Color,      // overlay/dim (#000000, 40%)
)

/** Figma 값 그대로의 라이트 팔레트. */
val LightMotionColors = MotionColors(
    bgBase = Color(0xFFFFFFFF),
    bgGrouped = Color(0xFFF2F4F6),
    bgSubtle = Color(0xFFF9FAFB),

    fillWeak = Color(0xFFF2F4F6),
    fillStrong = Color(0xFFE5E8EB),

    textPrimary = Color(0xFF191F28),
    textSecondary = Color(0xFF4E5968),
    textTertiary = Color(0xFF6B7684), // 원본 #8B95A1 (흰 바탕 명암비 3.0 → 4.6)
    textDisabled = Color(0xFFB0B8C1),
    textInverse = Color(0xFFFFFFFF),

    lineDivider = Color(0xFFE5E8EB),
    lineStrong = Color(0xFFD1D6DB),

    brandPrimary = Color(0xFF1F68D9), // 원본 #3182F6 (흰 글자 대비를 올림)
    brandPressed = Color(0xFF1A56C0), // 원본 #1B64DA
    brandWeak = Color(0xFFE8F3FF),

    statusSuccess = Color(0xFF03B26C),
    statusSuccessWeak = Color(0xFFE6F8F0),
    statusWarning = Color(0xFFFE9800),
    statusWarningWeak = Color(0xFFFFF4E5),
    statusDanger = Color(0xFFD8303E), // 원본 #F04452
    statusDangerWeak = Color(0xFFFFEEF0),

    surfaceToast = Color(0xFF333D4B),
    overlayDim = Color(0x66000000), // 0x66 = 40% (보고서 5장 "뒤 화면 어둡게 dim 40%")
)

internal val LocalMotionColors = staticCompositionLocalOf { LightMotionColors }
