package com.uhi.motionlayer.pc.ui.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import com.uhi.motionlayer.pc.domain.AgentSettings
import com.uhi.motionlayer.pc.domain.OverlayState
import com.uhi.motionlayer.pc.domain.PlayingEffect
import com.uhi.motionlayer.pc.domain.PointerColor
import com.uhi.motionlayer.pc.domain.PointerSize
import com.uhi.motionlayer.pc.domain.PresentationOverlay
import com.uhi.motionlayer.pc.ui.PcStrings
import com.uhi.motionlayer.pc.ui.theme.PcAlpha
import com.uhi.motionlayer.pc.ui.theme.PcBorder
import com.uhi.motionlayer.pc.ui.theme.PcIconSize
import com.uhi.motionlayer.pc.ui.theme.PcSize
import com.uhi.motionlayer.pc.ui.theme.PcSpacing
import com.uhi.motionlayer.pc.ui.theme.scale
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.Text
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionShadows
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.theme.motionShadow

/** 발표 오버레이(PC-06~09). 값을 구독해 [OverlayContent]에 넘긴다. */
@Composable
fun OverlayRoute(overlay: PresentationOverlay, settings: AgentSettings) {
    val state by overlay.state.collectAsState()
    val connection by settings.connection.collectAsState()
    val presentation by settings.presentation.collectAsState()
    OverlayContent(
        state = state,
        showOperatorName = connection.showOperatorName,
        pointerSize = presentation.pointerSize,
        pointerColor = presentation.pointerColor,
        strokeFadeSeconds = presentation.strokeFadeSeconds,
    )
}

/**
 * 슬라이드 위에 그리는 것 전부. 바탕은 투명하고, 받은 값만 그린다.
 *
 * - PC-06 포인터: 옅은 원 + 가운데 점
 * - PC-07 그리기: 선 + 아래 가운데 안내
 * - PC-08 화면 가리기: 검은 화면 + 아래 안내(이때는 다른 것을 그리지 않는다)
 * - PC-09 효과: 오른쪽 아래 작은 창
 * - 조작한 사람 이름표: 오른쪽 위(설정에서 켰을 때만)
 *
 * @param effectMedia 효과 창 안에 실제 사진·영상을 그리는 자리. 재생은 C가 맡고, 비워 두면 자리 표시 그림이 나온다.
 */
@Composable
fun OverlayContent(
    state: OverlayState,
    showOperatorName: Boolean,
    pointerSize: PointerSize,
    pointerColor: PointerColor,
    strokeFadeSeconds: Int,
    modifier: Modifier = Modifier,
    effectMedia: (@Composable () -> Unit)? = null,
) {
    val colors = MotionTheme.colors
    Box(modifier.fillMaxSize()) {
        if (state.blackout) {
            Box(Modifier.fillMaxSize().background(colors.textPrimary)) {
                Text(
                    text = PcStrings.BLACKOUT_NOTE,
                    style = MotionTheme.typography.body2,
                    color = colors.textTertiary,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = PcSpacing.blackoutNoteBottom),
                )
            }
            return@Box
        }

        val strokeColor = colors.brandPrimary
        val pointerInk = when (pointerColor) {
            PointerColor.Blue -> colors.brandPrimary
            PointerColor.Red -> colors.statusDanger
        }
        val ringColor = colors.bgBase
        Canvas(Modifier.fillMaxSize()) {
            OverlayFrameMeter.tick()
            state.strokes.forEach { stroke ->
                if (stroke.points.size < 2) return@forEach
                val path = Path()
                stroke.points.forEachIndexed { index, point ->
                    val x = point.x * size.width
                    val y = point.y * size.height
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = strokeColor,
                    style = Stroke(width = PcBorder.stroke.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
            state.pointer?.let { point ->
                val center = Offset(point.x * size.width, point.y * size.height)
                val scale = pointerSize.scale
                val dotRadius = PcSize.pointerDot.toPx() / 2 * scale
                drawCircle(color = pointerInk.copy(alpha = PcAlpha.POINTER_HALO), radius = PcSize.pointerHalo.toPx() / 2 * scale, center = center)
                drawCircle(color = ringColor, radius = dotRadius, center = center)
                drawCircle(color = pointerInk, radius = dotRadius - PcBorder.pointerRing.toPx(), center = center)
            }
        }

        if (showOperatorName && state.operatorName != null) {
            OperatorPill(
                name = state.operatorName,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = PcSpacing.operatorTop, end = PcSpacing.operatorEnd),
            )
        }
        if (state.strokes.isNotEmpty()) {
            HintPill(
                text = PcStrings.strokeHint(strokeFadeSeconds),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = PcSpacing.hintBottom),
            )
        }
        state.effect?.let { effect ->
            EffectPip(
                effect = effect,
                media = effectMedia,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = PcSpacing.pipEnd, bottom = PcSpacing.pipBottom),
            )
        }
    }
}

/** "허윤 조작 중" 이름표. */
@Composable
private fun OperatorPill(name: String, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val shape = RoundedCornerShape(MotionRadius.r20)
    Row(
        modifier = modifier
            .motionShadow(MotionShadows.floating, shape)
            .background(colors.bgBase, shape)
            .padding(horizontal = MotionSpacing.s14, vertical = MotionSpacing.s8),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(PcSize.operatorMark)
                .background(colors.brandPrimary, RoundedCornerShape(MotionRadius.r6)),
            contentAlignment = Alignment.Center,
        ) {
            MlIconView(icon = MlIcon.Gesture, tint = colors.textInverse, size = PcIconSize.s13)
        }
        Text(text = PcStrings.operator(name), style = MotionTheme.typography.caption1Strong, color = colors.textSecondary)
    }
}

/** 그리는 동안 아래 가운데에 뜨는 안내. */
@Composable
private fun HintPill(text: String, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    Box(
        modifier
            .background(colors.surfaceToast, RoundedCornerShape(MotionRadius.r20))
            .padding(horizontal = MotionSpacing.s16, vertical = MotionSpacing.s8),
    ) {
        Text(text = text, style = MotionTheme.typography.caption1Strong, color = colors.textInverse)
    }
}

/** 슬라이드 위에 띄운 효과(사진·영상)의 작은 창. */
@Composable
private fun EffectPip(effect: PlayingEffect, media: (@Composable () -> Unit)?, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val shape = RoundedCornerShape(MotionRadius.r16)
    Column(
        modifier = modifier
            .motionShadow(MotionShadows.floating, shape)
            .background(colors.bgBase, shape)
            .padding(MotionSpacing.s10),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
    ) {
        Box(
            modifier = Modifier
                .size(width = PcSize.pipMediaWidth, height = PcSize.pipMediaHeight)
                .clip(RoundedCornerShape(MotionRadius.r10))
                .background(colors.bgGrouped),
            contentAlignment = Alignment.Center,
        ) {
            if (media != null) media() else MlIconView(icon = MlIcon.Image, tint = colors.textDisabled, size = PcIconSize.s48)
        }
        Row(
            modifier = Modifier.width(PcSize.pipMediaWidth).padding(horizontal = MotionSpacing.s4),
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = effect.name,
                style = MotionTheme.typography.caption1Strong,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(text = PcStrings.effectTime(effect), style = MotionTheme.typography.caption1, color = colors.textTertiary)
        }
    }
}
