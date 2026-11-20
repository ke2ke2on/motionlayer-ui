package com.uhi.motionlayer.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * 이 앱의 모든 글자는 Material `Text` 대신 이 `Text`로 그린다(쓰는 쪽은 `import com.uhi.motionlayer.ui.components.Text`).
 * 하는 일은 하나다: **한글을 낱말(띄어쓰기) 단위로 줄바꿈**한다(보고서 6.3 "글자 낱말 단위 줄바꿈").
 *
 * 안드로이드는 한글 글자 사이 어디서나 줄을 바꿀 수 있어서 "나 / 와요"처럼 낱말이 두 줄로 갈라지고,
 * 시스템 글자를 키우면(1.3배·2배) 더 자주 그렇다. 안드로이드에는 CSS의 `word-break: keep-all`에 해당하는 설정이 없어
 * (`LineBreak.WordBreak.Phrase`는 일본어에만 듣는다) 낱말 안의 글자 사이에 보이지 않는 WORD JOINER(U+2060)를 넣어 끊지 못하게 한다.
 * 한 낱말이 한 줄보다 길 때만 어쩔 수 없이 글자 사이에서 끊긴다.
 *
 * 스크린리더에는 WORD JOINER가 없는 원래 글이 읽히게 한다.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current,
) {
    val joined = remember(text) { text.keepKoreanWordsTogether() }
    androidx.compose.material3.Text(
        text = joined,
        modifier = if (joined === text) modifier else modifier.semantics { this.text = AnnotatedString(text) },
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        style = style,
    )
}

private const val WORD_JOINER = '⁠'

/**
 * 한글 낱말 안의 글자 사이(한글–한글, 한글–영문·숫자)에 WORD JOINER를 넣은 글을 돌려준다.
 * 넣을 곳이 없으면 같은 글(같은 객체)을 그대로 돌려준다.
 */
internal fun String.keepKoreanWordsTogether(): String {
    var builder: StringBuilder? = null
    for (index in 1 until length) {
        val before = this[index - 1]
        val after = this[index]
        if (isKoreanWordPair(before, after)) {
            if (builder == null) builder = StringBuilder(length + 8).append(this, 0, index)
            builder.append(WORD_JOINER)
        }
        builder?.append(after)
    }
    return builder?.toString() ?: this
}

private fun isKoreanWordPair(before: Char, after: Char): Boolean =
    (isHangul(before) && (isHangul(after) || isLatinOrDigit(after))) ||
        (isHangul(after) && isLatinOrDigit(before))

private fun isHangul(char: Char): Boolean =
    char in '가'..'힣' || char in '㄰'..'㆏' || char in 'ᄀ'..'ᇿ'

private fun isLatinOrDigit(char: Char): Boolean =
    char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9'
