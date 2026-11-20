package com.uhi.motionlayer.tv.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.ui.theme.MotionShadow

// TV 화면에만 있는 크기. 색·기본 간격·모서리는 폰 앱의 토큰(`MotionTheme`, `MotionSpacing`, `MotionRadius` …)을 그대로 쓰고,
// 폰에 없는 값만 여기에 둔다. 화면 코드에는 숫자를 직접 쓰지 않는다(폰 앱의 규칙 1과 같다).
//
// 값은 Figma "21 TV"(1280 x 720)에서 잰 것 그대로다. 실제 TV 크기에 맞추는 일은 `TvCanvas`가 한다:
// 그 안에서는 1dp가 "시안의 1"이 되어, 어떤 TV에서도 시안과 같은 비율로 보인다.

object TvSize {
    /** 시안 한 장의 크기. */
    const val CANVAS_WIDTH = 1280f
    const val CANVAS_HEIGHT = 720f
    val canvasWidth = 1280.dp
    val canvasHeight = 720.dp

    /** 왼쪽 위 "MotionLayer" 앞의 파란 네모. */
    val brandMark = 36.dp

    /** TV-01 선택 카드와 그 안의 아이콘 칸. */
    val choiceWidth = 520.dp
    val choiceHeight = 240.dp
    val choiceTile = 56.dp

    /** TV-02 왼쪽 글 영역의 폭, 순서 번호 동그라미, QR 칸(흰 바탕 320 · QR 자리 280 · 무늬 둘레 빈 띠 19). */
    val pairingTextWidth = 600.dp
    val stepNumber = 32.dp
    val qrBox = 320.dp
    val qrCode = 280.dp
    val qrQuietZone = 19.dp

    /** TV-03 내용의 폭과 사람 아이콘 동그라미. */
    val connectionsWidth = 760.dp
    val avatar = 56.dp

    /** TV-04 이름표 앞의 동그라미. */
    val operatorMark = 36.dp

    /** TV-06 효과 창 안의 그림 자리. */
    val pipMediaWidth = 400.dp
    val pipMediaHeight = 225.dp

    /** 시연용 동작 목록의 폭(시안에 없음, 직접 정함). */
    val demoMenuWidth = 640.dp
}

object TvSpacing {
    val s32 = 32.dp
    val s36 = 36.dp

    /** 화면 왼쪽 여백과 "MotionLayer" 표시의 위 여백. */
    val screenStart = 64.dp
    val brandTop = 48.dp

    /** TV-01·03 제목이 시작하는 높이. */
    val contentTop = 150.dp

    /** TV-01 카드 줄과 아래 안내 글이 시작하는 높이. */
    val choicesTop = 330.dp
    val hintTop = 630.dp

    /** TV-02 왼쪽 글과 오른쪽 QR 카드의 자리. */
    val pairingTextTop = 180.dp
    val qrCardStart = 780.dp
    val qrCardTop = 60.dp

    /** 오버레이: 위 여백, 이름표의 오른쪽 여백, 효과 창의 오른쪽·아래 여백. */
    val overlayTop = 48.dp
    val operatorEnd = 141.dp
    val pipEnd = 36.dp
    val pipBottom = 17.dp
}

object TvRadius {
    val r32 = 32.dp
}

object TvIconSize {
    val s26 = 26.dp
    val s30 = 30.dp
    val s56 = 56.dp
}

object TvBorder {
    /** 리모컨으로 고른 카드를 두르는 굵은 테두리. */
    val focus = 4.dp
}

object TvShadows {
    /**
     * Figma `Shadow/Floating`의 TV판. 폰의 값(높이 8 · 검정 60%)은 폰 화면 배율에 맞춘 것이라
     * TV에서 그대로 쓰면 시안보다 좁고 진하게 나온다. TV 에뮬레이터 화면을 시안과 나란히 놓고 눈으로 맞췄다.
     */
    val floating = MotionShadow(elevation = 16.dp, color = Color.Black.copy(alpha = 0.3f))
}
