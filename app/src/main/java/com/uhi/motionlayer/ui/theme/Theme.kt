package com.uhi.motionlayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * 화면 코드에서 색과 글자 스타일을 꺼내는 입구.
 *
 *     Text("안녕", style = MotionTheme.typography.body1, color = MotionTheme.colors.textPrimary)
 *
 * 색·글자 크기를 숫자나 `Color(0xFF...)`로 직접 쓰지 않는다. 값이 필요하면 Color.kt / Type.kt에 먼저 추가한다.
 */
object MotionTheme {
    val colors: MotionColors
        @Composable @ReadOnlyComposable get() = LocalMotionColors.current

    val typography: MotionTypography
        @Composable @ReadOnlyComposable get() = LocalMotionTypography.current
}

/**
 * 앱 전체 테마.
 *
 * - Figma에 다크 모드가 없어서 항상 라이트로 그린다(시스템 다크 모드·동적 색을 따르지 않는다).
 * - Material 컴포넌트(바텀시트, 대화상자 등)가 우리 색·글꼴을 쓰도록 MaterialTheme도 같은 값으로 맞춘다.
 */
@Composable
fun MotionLayerTheme(content: @Composable () -> Unit) {
    val colors = LightMotionColors
    val typography = MotionTypography()

    val materialColors = lightColorScheme(
        primary = colors.brandPrimary,
        onPrimary = colors.textInverse,
        primaryContainer = colors.brandWeak,
        onPrimaryContainer = colors.brandPressed,
        secondary = colors.textSecondary,
        onSecondary = colors.textInverse,
        background = colors.bgBase,
        onBackground = colors.textPrimary,
        surface = colors.bgBase,
        onSurface = colors.textPrimary,
        surfaceVariant = colors.bgGrouped,
        onSurfaceVariant = colors.textSecondary,
        surfaceTint = Color.Transparent,
        surfaceContainerLowest = colors.bgBase,
        surfaceContainerLow = colors.bgBase,
        surfaceContainer = colors.bgBase,
        surfaceContainerHigh = colors.bgBase,
        surfaceContainerHighest = colors.bgGrouped,
        outline = colors.lineStrong,
        outlineVariant = colors.lineDivider,
        error = colors.statusDanger,
        onError = colors.textInverse,
        errorContainer = colors.statusDangerWeak,
        onErrorContainer = colors.statusDanger,
        scrim = Color.Black,
    )

    CompositionLocalProvider(
        LocalMotionColors provides colors,
        LocalMotionTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = typography.toMaterial(),
            content = content,
        )
    }
}
