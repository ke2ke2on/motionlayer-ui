package com.uhi.motionlayer.ui.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.R
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlButtonSize
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 카메라 미리보기를 그리고 QR을 읽는 부분의 모양. **실제 구현은 시스템 연동 담당(B)이 채운다**
 * (카메라 권한 요청, 미리보기, QR 인식, 토큰 읽기). 화면 쪽은 읽은 토큰 문자열만 받는다.
 *
 * 구현이 지켜야 할 것: 카메라가 보이는 영역 전체를 채운다. 정중앙에 232dp 정사각형 인식 영역이 있다고 보고
 * 그 위에 모서리 표시([ScanCorners])가 겹쳐 그려진다. QR을 읽으면 [onScanned]를 부른다.
 */
typealias QrCameraContent = @Composable (onScanned: (String) -> Unit) -> Unit

/** Figma PR-02 QR 스캔. 상태 바 아래까지 카메라가 채우는 전체 화면이라 하단 탭이 없다. */
@Composable
fun QrScanScreen(
    camera: QrCameraContent,
    onScanned: (String) -> Unit,
    onEnterCode: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(modifier = modifier.fillMaxSize().background(colors.textDisabled)) {
        // 카메라가 보이는 영역
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            camera(onScanned)
            ScanCorners(modifier = Modifier.align(Alignment.Center))
            // 보이는 원은 40이고 누르는 영역은 48이다(보고서 6.2). 48 영역이 원을 가운데에 두고, 원은 Figma 자리(왼쪽 16·위 12)에 오도록 4씩 당겨 놓았다.
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = MotionSpacing.s12, top = MotionSpacing.s8)
                    .size(MotionSize.minTouchTarget)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(MotionSize.floatingCircle)
                        .clip(CircleShape)
                        .background(colors.bgBase),
                    contentAlignment = Alignment.Center,
                ) {
                    MlIconView(
                        icon = MlIcon.Close,
                        tint = colors.textPrimary,
                        size = MotionIconSize.s20,
                        contentDescription = stringResource(R.string.ml_close),
                    )
                }
            }
        }

        // 아래에서 올라온 안내 시트
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = MotionRadius.r24, topEnd = MotionRadius.r24))
                .background(colors.bgBase)
                .navigationBarsPadding()
                .padding(start = MotionSpacing.s24, end = MotionSpacing.s24, top = MotionSpacing.s24, bottom = MotionSpacing.s28),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
        ) {
            Text(text = title ?: stringResource(R.string.pr02_title), style = type.title3, color = colors.textPrimary)
            Text(
                text = description ?: stringResource(R.string.pr02_description),
                style = type.body2,
                color = colors.textSecondary,
            )
            Box(Modifier.padding(top = MotionSpacing.s4))
            MlButton(
                text = stringResource(R.string.pr02_enter_code),
                onClick = onEnterCode,
                modifier = Modifier.fillMaxWidth(),
                style = MlButtonStyle.Secondary,
                size = MlButtonSize.M,
            )
        }
    }
}

/**
 * QR을 맞춰 넣을 네모의 네 모서리 표시. Figma `Corners`: 252dp 정사각형, 선 5dp, 둥근 끝.
 * 카메라 구현(B)이 무엇이든 이 위에 겹쳐 그려진다.
 */
@Composable
private fun ScanCorners(modifier: Modifier = Modifier) {
    val color = MotionTheme.colors.bgBase
    Canvas(modifier = modifier.size(260.dp)) {
        val stroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val inset = 4.dp.toPx() + stroke.width / 2
        val side = size.width - inset * 2
        val radius = 28.dp.toPx()
        val arm = 48.dp.toPx()

        fun corner(origin: Offset, dx: Float, dy: Float) {
            // origin은 정사각형의 꼭짓점. (dx, dy)는 안쪽 방향(±1)
            val path = Path().apply {
                moveTo(origin.x, origin.y + dy * arm)
                lineTo(origin.x, origin.y + dy * radius)
                quadraticTo(origin.x, origin.y, origin.x + dx * radius, origin.y)
                lineTo(origin.x + dx * arm, origin.y)
            }
            drawPath(path, color, style = stroke)
        }
        corner(Offset(inset, inset), 1f, 1f)
        corner(Offset(inset + side, inset), -1f, 1f)
        corner(Offset(inset, inset + side), 1f, -1f)
        corner(Offset(inset + side, inset + side), -1f, -1f)
    }
}

/**
 * 실제 카메라가 없을 때 쓰는 가짜 카메라. Figma처럼 회색 바탕 가운데에 흰 인식 영역과 QR 그림을 그리고,
 * **그 영역을 누르면 QR을 읽은 것처럼** 가짜 토큰을 돌려준다.
 */
@Composable
fun FakeQrCamera(onScanned: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val description = stringResource(R.string.pr02_scan_area)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(232.dp)
                .clip(RoundedCornerShape(MotionRadius.r24))
                .background(colors.bgBase)
                .semantics { contentDescription = description }
                .clickable(role = Role.Button) { onScanned("demo-token") },
            contentAlignment = Alignment.Center,
        ) {
            QrPlaceholder(modifier = Modifier.size(120.dp))
        }
    }
}

/** QR 코드처럼 보이는 그림(모양만 흉내). 실제로 읽히지 않는다. */
@Composable
private fun QrPlaceholder(modifier: Modifier = Modifier) {
    val ink = MotionTheme.colors.textPrimary
    val paper = MotionTheme.colors.bgBase
    Canvas(modifier = modifier) {
        val cells = 21
        val cell = size.width / cells
        fun square(col: Int, row: Int, span: Int) =
            drawRect(ink, Offset(col * cell, row * cell), Size(span * cell, span * cell))
        fun finder(col: Int, row: Int) {
            square(col, row, 7)
            drawRect(
                color = paper,
                topLeft = Offset((col + 1) * cell, (row + 1) * cell),
                size = Size(5 * cell, 5 * cell),
            )
            square(col + 2, row + 2, 3)
        }
        finder(0, 0)
        finder(cells - 7, 0)
        finder(0, cells - 7)
        // 나머지 칸은 규칙적인 무늬로 채운다(같은 모양이 항상 나오게 계산식으로 정함)
        for (row in 0 until cells) {
            for (col in 0 until cells) {
                val inFinder = (col < 8 && row < 8) || (col >= cells - 8 && row < 8) || (col < 8 && row >= cells - 8)
                if (!inFinder && (col * 7 + row * 13 + col * row) % 3 == 0) square(col, row, 1)
            }
        }
    }
}

@Preview(name = "PR-02 QR 스캔", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr02Preview() = MotionLayerTheme {
    QrScanScreen(
        camera = { onScanned -> FakeQrCamera(onScanned) },
        onScanned = {},
        onEnterCode = {},
        onClose = {},
    )
}
