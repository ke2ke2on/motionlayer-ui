package com.uhi.motionlayer.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.uhi.motionlayer.tv.R

/**
 * TV판 글꼴. 폰 앱과 같은 Noto Sans KR 가변 폰트 파일(app/src/main/res/font/noto_sans_kr.ttf)을
 * 빌드할 때 가져와 쓴다(`build.gradle.kts`의 `syncSharedRes`). 굵기 3종은 폰 앱의 `Font.kt`와 같다.
 *
 * 폰 앱의 `Font.kt`와 내용은 같고 리소스 번호(R)만 TV 앱 것이다. 글자 스타일(`Type.kt`)은 폰 앱 것을 그대로 쓴다.
 */
@OptIn(ExperimentalTextApi::class)
val NotoSansKr = FontFamily(
    Font(R.font.noto_sans_kr, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.noto_sans_kr, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.noto_sans_kr, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)
