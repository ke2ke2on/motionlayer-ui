package com.uhi.motionlayer.pc.ui.theme

import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.pc.domain.PointerSize

// PC 화면에만 있는 크기. 색·글자·기본 간격은 폰 앱의 토큰(`MotionTheme`, `MotionSpacing` …)을 그대로 쓰고,
// 폰에 없는 값만 여기에 둔다. 화면 코드에는 숫자를 직접 쓰지 않는다(폰 앱의 규칙 1과 같다).
// 값은 Figma "20 PC 에이전트"에서 잰 것이다.

object PcSize {
    /** 설정 창의 내용 758 x 458. Figma의 창 760 x 500에서 제목 줄(40)과 테두리(1)를 뺀 크기다. 제목 줄은 운영체제가 그린다. */
    val contentWidth = 758.dp
    val contentHeight = 458.dp
    val sidebarWidth = 190.dp

    /** PC-01 QR: 흰 바탕 칸 150, 그 안의 QR 자리 116, 무늬 둘레의 빈 띠 8. */
    val qrBox = 150.dp
    val qrCode = 116.dp
    val qrQuietZone = 8.dp

    /** PC-02 연결된 폰 카드의 아이콘 칸. */
    val phoneTile = 44.dp

    /** PC-06 "○○ 조작 중" 이름표 앞의 파란 네모. */
    val operatorMark = 20.dp

    /** 앱 표시(트레이 아이콘·창 아이콘): 파란 둥근 네모 16, 모서리 4, 안의 아이콘 11 (PC-02 제목 줄·PC-04). */
    val appMark = 16.dp
    val appMarkRadius = 4.dp
    val appMarkGlyph = 11.dp

    /** 앱 표시를 그림으로 만들 때의 한 변(픽셀). 트레이와 창 제목 줄에서 줄여 쓴다. */
    const val APP_MARK_PIXELS = 64

    /** PC-06 포인터: 옅은 원 56, 가운데 점 18(크기 "보통"일 때). */
    val pointerHalo = 56.dp
    val pointerDot = 18.dp

    /** PC-09 효과 창 안의 그림 자리 360 x 203. */
    val pipMediaWidth = 360.dp
    val pipMediaHeight = 203.dp
}

object PcSpacing {
    /** 설정 창 내용의 좌우 여백. */
    val s32 = 32.dp

    /** 오버레이에서 화면 가장자리까지의 거리(1280 x 720 시안 기준). */
    val operatorTop = 24.dp
    val operatorEnd = 59.dp
    val hintBottom = 45.dp
    val blackoutNoteBottom = 38.dp
    val pipEnd = 20.dp
    val pipBottom = 30.dp
}

object PcIconSize {
    val s13 = 13.dp
    val s48 = 48.dp
}

object PcBorder {
    /** 포인터 가운데 점을 두르는 흰 테두리. */
    val pointerRing = 3.dp

    /** 폰으로 그린 선의 두께. */
    val stroke = 6.dp
}

object PcAlpha {
    /** 포인터 둘레의 옅은 원. */
    const val POINTER_HALO = 0.18f
}

/** 포인터 크기 설정에 따른 배율. Figma에는 "보통"만 있고 작게·크게는 직접 정했다. */
val PointerSize.scale: Float
    get() = when (this) {
        PointerSize.Small -> 0.75f
        PointerSize.Medium -> 1f
        PointerSize.Large -> 1.25f
    }
