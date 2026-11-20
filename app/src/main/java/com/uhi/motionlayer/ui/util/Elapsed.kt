package com.uhi.motionlayer.ui.util

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

/**
 * 이 화면이 그려지기 시작한 뒤 흐른 시간(초). 0.1초마다 바뀐다. "분석 중 · 1.3초", "연결하는 중 · 1.2초"처럼
 * 진행 중임을 알리는 글자에 쓴다. 실제 걸린 시간이 아니라 화면이 떠 있던 시간을 센 것이다(PR-03의 "남음"과 같은 방식).
 */
@Composable
fun rememberElapsedSeconds(): State<Float> {
    val seconds = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = SystemClock.elapsedRealtime()
        while (true) {
            delay(TICK_MILLIS)
            seconds.floatValue = (SystemClock.elapsedRealtime() - start) / 1000f
        }
    }
    return seconds
}

private const val TICK_MILLIS = 100L
