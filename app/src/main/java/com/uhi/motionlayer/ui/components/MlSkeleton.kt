package com.uhi.motionlayer.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.rememberReduceMotion

/**
 * 불러오는 동안 내용이 들어올 자리를 알려 주는 회색 모양(HM-07·AP-07·GS-16의 스켈레톤). `fill/strong` 색에 보통은 모서리 6의 막대다.
 * 크기는 쓰는 곳에서 정한다. Figma는 정지 그림이라 움직임이 없는데, 멈춘 화면처럼 보이지 않게 천천히 옅어졌다 진해진다.
 * 사용자가 "애니메이션 줄이기"를 켰으면 움직이지 않고 정지 그림으로 둔다(보고서 6.3).
 */
@Composable
fun MlSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(MotionRadius.r6),
) {
    val reduceMotion = rememberReduceMotion()
    // "애니메이션 줄이기"이면 움직임 자체를 만들지 않는다(null). 값은 그리는 단계에서 읽어서 프레임마다 다시 짜지 않는다.
    val pulse: State<Float>? = if (reduceMotion) null else rememberSkeletonPulse()
    Box(
        modifier = modifier
            .graphicsLayer { alpha = pulse?.value ?: 1f }
            .background(MotionTheme.colors.fillStrong, shape),
    )
}

@Composable
private fun rememberSkeletonPulse(): State<Float> {
    val transition = rememberInfiniteTransition(label = "skeleton")
    return transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeletonPulse",
    )
}
