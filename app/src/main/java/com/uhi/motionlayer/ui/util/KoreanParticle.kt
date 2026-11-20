package com.uhi.motionlayer.ui.util

/**
 * 앱·기기 이름 뒤에 붙는 조사를 알맞게 고른다. "멜론이", "Spotify가", "강의실 PC로", "MacBook으로"처럼.
 *
 * - 한글은 마지막 글자의 받침으로 정확히 판단한다.
 * - 숫자는 우리말 읽기(영·일·이…)로 판단한다.
 * - 영어는 **발음을 정확히 알 수 없어서 어림짐작**이다. 대문자 약어(PC, TV)는 알파벳 이름으로 읽고
 *   (PC→피씨), 그 밖의 낱말은 마지막 글자가 모음(a, e, i, o, u, y)이면 받침 없음으로 본다.
 *   틀릴 수 있는 이름은 테스트에 예시를 추가하며 고친다.
 */
object KoreanParticle {
    /** 주격 조사: 받침이 있으면 "이", 없으면 "가". */
    fun subject(word: String): String = if (ending(word) == Ending.Vowel) "가" else "이"

    /** 방향·수단 조사: 받침이 없거나 ㄹ 받침이면 "로", 그 밖의 받침이면 "으로". */
    fun toward(word: String): String = if (ending(word) == Ending.Consonant) "으로" else "로"

    /** 함께 조사: 받침이 없으면 "와", 있으면 "과". "강의실 PC와", "멜론과". */
    fun with(word: String): String = if (ending(word) == Ending.Vowel) "와" else "과"

    /** 목적격 조사: 받침이 없으면 "를", 있으면 "을". "강의실 PC를", "멜론을". */
    fun objective(word: String): String = if (ending(word) == Ending.Vowel) "를" else "을"

    /** 또는 조사: 받침이 없으면 "나", 있으면 "이나". "카카오나", "YouTube Music이나". */
    fun or(word: String): String = if (ending(word) == Ending.Vowel) "나" else "이나"

    private enum class Ending { Vowel, Consonant, Rieul }

    private fun ending(word: String): Ending {
        val trimmed = word.trimEnd()
        val last = trimmed.lastOrNull() ?: return Ending.Vowel
        return when {
            last in '가'..'힣' -> when ((last - '가') % 28) {
                0 -> Ending.Vowel
                8 -> Ending.Rieul // ㄹ 받침
                else -> Ending.Consonant
            }
            last.isDigit() -> digitEnding(last)
            last.isLetter() -> latinEnding(trimmed)
            else -> Ending.Vowel
        }
    }

    /** 0 영, 1 일, 2 이, 3 삼, 4 사, 5 오, 6 육, 7 칠, 8 팔, 9 구 */
    private fun digitEnding(digit: Char): Ending = when (digit) {
        '1', '7', '8' -> Ending.Rieul
        '0', '3', '6' -> Ending.Consonant
        else -> Ending.Vowel
    }

    private fun latinEnding(text: String): Ending {
        val lastWord = text.substringAfterLast(' ')
        val isAcronym = lastWord.length in 2..5 && lastWord.all { it in 'A'..'Z' }
        val last = lastWord.last()
        return if (isAcronym) {
            // 알파벳 이름: 에프·에이치·엠·엔·에스·엑스는 받침이 있고, 엘·알은 ㄹ 받침.
            when (last) {
                'L', 'R' -> Ending.Rieul
                'F', 'H', 'M', 'N', 'S', 'X' -> Ending.Consonant
                else -> Ending.Vowel
            }
        } else {
            val lower = last.lowercaseChar()
            when {
                lower == 'l' -> Ending.Rieul
                lower in "aeiouy" -> Ending.Vowel
                else -> Ending.Consonant
            }
        }
    }
}
