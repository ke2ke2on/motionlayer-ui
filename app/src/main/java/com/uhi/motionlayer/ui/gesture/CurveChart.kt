package com.uhi.motionlayer.ui.gesture

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.domain.model.TrajectoryPoint

/** 곡선 하나: 0~1 좌표의 점들과 색. */
data class ChartCurve(val points: List<TrajectoryPoint>, val color: Color)

/**
 * 여러 곡선을 겹쳐 그리는 차트(Figma `Waveform`, 높이 110, 선 두께 2.5). 목록에 있는 순서대로 위에 쌓이므로
 * 흐린 이전 동작을 먼저 주고 방금 동작을 마지막에 준다. 점이 둘보다 적은 곡선은 그리지 않는다.
 */
@Composable
fun CurveChart(curves: List<ChartCurve>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(110.dp)) {
        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        curves.forEach { curve ->
            if (curve.points.size < 2) return@forEach
            val path = Path()
            curve.points.forEachIndexed { index, point ->
                val x = point.x * size.width
                val y = point.y * size.height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = curve.color, style = stroke)
        }
    }
}
