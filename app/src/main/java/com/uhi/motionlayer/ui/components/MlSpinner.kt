package com.uhi.motionlayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.rememberReduceMotion

/** 고리 두께가 지름에서 차지하는 비율. Figma Spinner는 안쪽 반지름이 0.82라 두께가 지름의 9%다. */
private const val RING_THICKNESS_RATIO = 0.09f

/** 진한 호가 도는 각도. Figma는 위(-90°)에서 시작해 252°만큼 그린다. */
private const val ARC_SWEEP_DEGREES = 252f

private const val ROTATION_MILLIS = 900

/**
 * 진행 중임을 알리는 고리(Figma GS-14·PR-16의 `Spinner`). 연한 파랑(`brand/weak`) 고리 위에서 진한 파랑(`brand/primary`) 호가 돈다.
 * Figma는 정지 그림이라 돌지 않는데, 멈춘 화면처럼 보이지 않게 천천히 돌린다.
 * 사용자가 "애니메이션 줄이기"를 켰으면 돌지 않고 Figma의 정지 그림 그대로 둔다(보고서 6.3).
 *
 * @param diameter 고리의 바깥 지름(GS-14는 48, PR-16은 44)
 */
@Composable
fun MlSpinner(diameter: Dp, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val reduceMotion = rememberReduceMotion()
    // "애니메이션 줄이기"이면 움직임 자체를 만들지 않는다(null). 값은 그리는 단계에서 읽어서 프레임마다 다시 짜지 않는다.
    val rotation: State<Float>? = if (reduceMotion) null else rememberSpinnerRotation()

    Canvas(modifier = modifier.size(diameter)) {
        val thickness = size.minDimension * RING_THICKNESS_RATIO
        // 선은 가운데 선을 따라 그려지므로 두께의 절반만큼 안쪽으로 들여 바깥 지름을 맞춘다.
        val topLeft = Offset(thickness / 2f, thickness / 2f)
        val ringSize = Size(size.width - thickness, size.height - thickness)
        drawArc(
            color = colors.brandWeak,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = ringSize,
            style = Stroke(width = thickness),
        )
        drawArc(
            color = colors.brandPrimary,
            startAngle = -90f + (rotation?.value ?: 0f),
            sweepAngle = ARC_SWEEP_DEGREES,
            useCenter = false,
            topLeft = topLeft,
            size = ringSize,
            style = Stroke(width = thickness),
        )
    }
}

@Composable
private fun rememberSpinnerRotation(): State<Float> {
    val transition = rememberInfiniteTransition(label = "spinner")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = ROTATION_MILLIS, easing = LinearEasing)),
        label = "spinnerRotation",
    )
}
