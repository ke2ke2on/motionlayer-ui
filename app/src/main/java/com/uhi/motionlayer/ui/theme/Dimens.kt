package com.uhi.motionlayer.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 간격·모서리 값. Figma 홈·앱·제스처·발표 페이지의 auto layout 통계에서 가장 많이 쓰인 값이다.
 * 이름은 Figma 변수 `spacing/N`·`radius/N`·`size/…`·`icon/N`·`border/…`와 짝이 되게 지었다(`s20` ↔ `spacing/20`).
 * 새 값이 필요하면 화면 코드에 숫자를 쓰지 말고 여기에 먼저 추가한다.
 */
object MotionSpacing {
    /** 화면 좌우 여백 (Scroll/Body/CTA의 padding-left가 24인 경우가 가장 많음) */
    val screenHorizontal = 24.dp

    /** 카드 안쪽 여백 */
    val cardPadding = 20.dp

    val s2 = 2.dp
    val s3 = 3.dp
    val s4 = 4.dp
    val s6 = 6.dp
    val s8 = 8.dp
    val s10 = 10.dp
    val s12 = 12.dp
    val s14 = 14.dp
    val s15 = 15.dp
    val s16 = 16.dp
    val s18 = 18.dp
    val s20 = 20.dp
    val s24 = 24.dp
    val s28 = 28.dp
}

object MotionRadius {
    /** 바텀시트 손잡이(높이 4) */
    val r2 = 2.dp

    /** 슬라이더 선·진행 막대(높이 6) */
    val r3 = 3.dp

    val r6 = 6.dp
    val r10 = 10.dp
    val r12 = 12.dp
    val r14 = 14.dp

    /** 스위치(높이 30) */
    val r15 = 15.dp
    val r16 = 16.dp

    /** 칩(높이 36) */
    val r18 = 18.dp

    /** 카드 (홈의 카드 29개가 모두 20) */
    val r20 = 20.dp
    val r24 = 24.dp
    val r28 = 28.dp
    val r36 = 36.dp

    /** 바텀시트 윗부분 등 큰 모서리 */
    val r44 = 44.dp
}

/** 터치 영역 등 공통 크기. Button 크기는 컴포넌트에서 정한다. */
object MotionSize {
    /** Button Size=L 높이 */
    val buttonL = 56.dp
    val buttonM = 48.dp
    val buttonS = 36.dp

    /** BottomTab 높이 */
    val bottomTab = 60.dp

    /** TopBar 높이 */
    val topBar = 56.dp

    /** 검색창(앱 목록의 "앱 이름 검색") 높이 */
    val searchField = 48.dp

    /** TextField의 입력 칸 높이 */
    val textField = 56.dp

    /** Chip 높이 */
    val chip = 36.dp

    /** 위 막대·홈 머리글의 동그란 아이콘 버튼(누르는 영역 48) */
    val iconButton = 48.dp

    /** 보고서 6.2: Android 터치 타깃 최소 48×48dp. 눈에 보이는 크기가 이보다 작아도 누르는 영역은 이만큼 둔다 */
    val minTouchTarget = 48.dp

    /** 카메라 화면 위에 뜬 닫기 원(PR-02)의 보이는 크기. 누르는 영역은 `minTouchTarget` */
    val floatingCircle = 40.dp

    /** PR-03 코드 입력 한 칸의 높이. 글자가 커지면 늘어난다 */
    val codeCell = 58.dp

    /** 보고서 6.3: 화면이 600dp 이상(태블릿)이면 내용 폭을 이 값으로 줄여 가운데에 둔다 */
    val contentMaxWidth = 480.dp

    /** ListRow가 설명이 없어도 줄어들지 않는 최소 높이(아이콘 타일 40 + 위아래 12) */
    val listRowMin = 64.dp

    /** IconTile·AppIcon 크기와, 앱 상세 맨 위의 큰 AppIcon */
    val iconTile = 40.dp
    val iconTileLarge = 52.dp

    /** 결과·빈 화면 가운데의 큰 원(Figma 88×88, 모서리 44) */
    val resultCircle = 88.dp

    /** 빈 화면(`Empty`)에서 맨 위부터 큰 원까지의 간격 */
    val emptyStateTop = 96.dp

    /** 진행 막대의 기본 두께 */
    val progressBar = 6.dp

    /** 기록(ST-03)의 제스처별 성공률 막대 두께 */
    val rateBar = 8.dp

    /** 바텀시트 손잡이 너비(높이는 `MotionSpacing.s4`) */
    val sheetHandleWidth = 36.dp
}

/**
 * 아이콘 크기. Figma 아이콘은 24px 기준이고, 줄 끝의 작은 화살표나 큰 그림 속 아이콘은 이 값들로 줄이거나 키운다.
 * 같은 모양의 아이콘이 화면마다 다른 크기로 나오지 않게 값을 여기서만 고른다.
 */
object MotionIconSize {
    val s18 = 18.dp
    val s20 = 20.dp
    val s22 = 22.dp
    val s24 = 24.dp
    val s28 = 28.dp
    val s32 = 32.dp
    val s36 = 36.dp
    val s40 = 40.dp
    val s44 = 44.dp
}

/** 선 두께. 테두리·구분선에 쓴다. */
object MotionBorder {
    /** 구분선·얇은 테두리 */
    val thin = 1.dp

    /** 꺼진 스위치의 테두리 */
    val toggle = 1.5.dp

    /** 입력 중·눌린 칸을 강조하는 굵은 테두리 */
    val thick = 2.dp
}
