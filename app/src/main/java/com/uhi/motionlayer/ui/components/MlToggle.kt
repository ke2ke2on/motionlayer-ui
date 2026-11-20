package com.uhi.motionlayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionShadows
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.theme.motionShadow

/** Figma Toggle의 틀 크기와 손잡이. 손잡이는 위·아래·바깥쪽에서 2씩 떨어져 있다. */
private val TrackWidth = 50.dp
private val TrackHeight = 30.dp
private val KnobSize = 26.dp
private val KnobInset = MotionSpacing.s2

/**
 * Figma `Toggle` (State On/Off). 50×30, 손잡이 26. 꺼진 상태는 연한 트랙에 `text/tertiary` 1.5 테두리를 둘러
 * 트랙이 바탕과 구분되게 한다.
 *
 * @param onCheckedChange null이면 누를 수 없는 표시용이다(ListRow 전체가 눌리는 경우 등).
 */
@Composable
fun MlToggle(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val track by animateColorAsState(
        targetValue = if (checked) colors.brandPrimary else colors.fillStrong,
        label = "toggleTrack",
    )
    val outline by animateColorAsState(
        targetValue = if (checked) Color.Transparent else colors.textTertiary,
        label = "toggleOutline",
    )
    val knobX by animateDpAsState(
        targetValue = if (checked) TrackWidth - KnobSize - KnobInset else KnobInset,
        label = "toggleKnob",
    )

    val shape = RoundedCornerShape(MotionRadius.r15)
    Box(
        modifier = modifier
            .size(width = TrackWidth, height = TrackHeight)
            .background(track, shape)
            .border(MotionBorder.toggle, outline, shape)
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(
                        value = checked,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Switch,
                        onValueChange = onCheckedChange,
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobX, y = KnobInset)
                .size(KnobSize)
                // Figma Shadow/Knob (y 2, 퍼짐 6, 16%)
                .motionShadow(MotionShadows.knob, CircleShape)
                .background(colors.bgBase, CircleShape),
        )
    }
}
