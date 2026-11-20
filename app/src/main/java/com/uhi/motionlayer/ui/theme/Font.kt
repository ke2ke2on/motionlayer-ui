package com.uhi.motionlayer.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.uhi.motionlayer.R

/**
 * Noto Sans KR 가변 폰트 하나(res/font/noto_sans_kr.ttf)에서 굵기 3종(Regular 400, Medium 500, Bold 700)을 뽑아 쓴다.
 * 가변 폰트의 굵기 지정은 API 26 이상에서 동작한다(이 앱의 minSdk가 26).
 * 이 지정 API가 아직 "실험적"으로 표시돼 있어서 OptIn을 붙였다.
 *
 * 글꼴을 불러오는 방법만 기기마다 달라서 이 파일을 따로 두었다. 폰 앱은 여기서(res/font),
 * PC 에이전트는 `pc-agent`의 같은 이름 파일에서 같은 글꼴 파일을 불러온다. 글자 스타일(`Type.kt`)은 둘이 같이 쓴다.
 */
@OptIn(ExperimentalTextApi::class)
val NotoSansKr = FontFamily(
    Font(R.font.noto_sans_kr, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.noto_sans_kr, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.noto_sans_kr, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)
