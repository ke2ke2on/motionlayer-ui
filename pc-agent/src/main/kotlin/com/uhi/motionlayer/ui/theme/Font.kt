package com.uhi.motionlayer.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

/**
 * PC판 글꼴. 폰 앱과 같은 Noto Sans KR 가변 폰트 파일(app/src/main/res/font/noto_sans_kr.ttf)을
 * 빌드할 때 가져와 쓴다(`build.gradle.kts`의 `syncSharedRes`). 굵기 3종은 폰 앱의 `Font.kt`와 같다.
 *
 * 이 파일만 PC 전용이고, 글자 스타일(`Type.kt`)은 폰 앱 것을 그대로 쓴다.
 *
 * 굵기를 `variationSettings`로 직접 지정해야 한다. 지정하지 않으면 가변 폰트의 기본값(가장 가는 굵기)으로 그려진다
 * (`SharedDesignTest`가 지킨다).
 */
private const val FONT_PATH = "/font/noto_sans_kr.ttf"

private val fontBytes: ByteArray by lazy {
    val stream = checkNotNull(FontVariation::class.java.getResourceAsStream(FONT_PATH)) { "글꼴 파일이 없다: $FONT_PATH" }
    stream.use { it.readBytes() }
}

@OptIn(ExperimentalTextApi::class)
private fun notoSansKr(weight: FontWeight) = Font(
    identity = "NotoSansKR-${weight.weight}",
    getData = { fontBytes },
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val NotoSansKr = FontFamily(
    notoSansKr(FontWeight.Normal),
    notoSansKr(FontWeight.Medium),
    notoSansKr(FontWeight.Bold),
)
