package com.uhi.motionlayer.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Figma 효과 스타일(그림자)을 Compose 그림자로 옮긴 값. 안드로이드는 퍼짐(blur)을 직접 쓸 수 없어서
 * 같은 느낌이 나는 높이(elevation)와 그림자 색으로 근사한다. 색의 투명도는 Figma의 투명도(16%·10%) 그대로가 아니다:
 * 안드로이드는 그림자 색의 투명도에 시스템 기본 투명도를 또 곱해서, 그대로 쓰면 Figma보다 훨씬 옅게 나온다.
 * 그래서 에뮬레이터에서 그림자 바로 아래의 밝기를 Figma 화면과 재 보고 맞춘 값을 쓴다.
 */
@Immutable
class MotionShadow(val elevation: Dp, val color: Color)

object MotionShadows {
    /** Figma `Shadow/Knob`: y 2, 퍼짐 6, 검정 16%. 스위치·슬라이더 손잡이 */
    val knob = MotionShadow(elevation = 4.dp, color = Color.Black)

    /** Figma `Shadow/Floating`: y 8, 퍼짐 24, 검정 10%. 토스트·알림 미리보기·기기 이름표처럼 떠 있는 것 */
    val floating = MotionShadow(elevation = 8.dp, color = Color.Black.copy(alpha = 0.6f))
}

/** [spec]의 그림자를 [shape] 모양으로 드리운다. */
fun Modifier.motionShadow(spec: MotionShadow, shape: Shape): Modifier = shadow(
    elevation = spec.elevation,
    shape = shape,
    ambientColor = spec.color,
    spotColor = spec.color,
)
