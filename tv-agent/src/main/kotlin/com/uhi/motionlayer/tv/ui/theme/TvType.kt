package com.uhi.motionlayer.tv.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.uhi.motionlayer.ui.theme.NotoSansKr

/**
 * TV 화면의 글자 스타일. Figma "21 TV"에는 이름 붙은 스타일이 없고 크기만 있어서 쓰임새로 이름을 붙였다.
 * 줄 높이는 모두 글자 크기의 135%, 자간은 큰 굵은 글자(26 이상)가 -2%, 나머지가 -1%다.
 * 순서 번호(16 Medium)와 배지는 폰 앱의 글자 스타일·부품을 그대로 쓴다.
 * 색은 스타일에 넣지 않고 쓰는 곳에서 `MotionTheme.colors`로 지정한다.
 */
object TvType {
    /** 52 Bold · TV-02 "폰으로 QR을 찍어 주세요" */
    val display = tvStyle(52, FontWeight.Bold, -2)

    /** 44 Bold · TV-01·03 화면 제목 */
    val title = tvStyle(44, FontWeight.Bold, -2)

    /** 36 Bold · 6자리 코드 */
    val code = tvStyle(36, FontWeight.Bold, -2)

    /** 30 Bold · 선택 카드 제목 */
    val cardTitle = tvStyle(30, FontWeight.Bold, -2)

    /** 26 Bold · 연결된 사람 이름 */
    val name = tvStyle(26, FontWeight.Bold, -2)

    /** 22 Bold · 왼쪽 위 "MotionLayer" */
    val brand = tvStyle(22, FontWeight.Bold, -1)

    /** 22 Regular · 설명과 순서 글 */
    val body = tvStyle(22, FontWeight.Normal, -1)

    /** 22 Medium · 오버레이의 이름표와 띠 */
    val bodyStrong = tvStyle(22, FontWeight.Medium, -1)

    /** 20 Regular · 카드 설명, 사람 상태, 아래 안내 */
    val detail = tvStyle(20, FontWeight.Normal, -1)

    /** 18 Regular · "60초마다 바뀌어요" */
    val caption = tvStyle(18, FontWeight.Normal, -1)

    /** 18 Medium · 효과 창의 "지수가 보낸 사진" */
    val captionStrong = tvStyle(18, FontWeight.Medium, -1)

    /** 16 Regular · 효과 창의 "3초 뒤 닫혀요" */
    val small = tvStyle(16, FontWeight.Normal, -1)
}

private const val LINE_HEIGHT_RATIO = 1.35f

private fun tvStyle(size: Int, weight: FontWeight, letterSpacingPercent: Int): TextStyle = TextStyle(
    fontFamily = NotoSansKr,
    fontSize = size.sp,
    lineHeight = (size * LINE_HEIGHT_RATIO).sp,
    fontWeight = weight,
    letterSpacing = (letterSpacingPercent / 100f).em,
    lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None),
)
