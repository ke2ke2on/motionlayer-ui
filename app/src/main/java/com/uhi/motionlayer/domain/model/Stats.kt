package com.uhi.motionlayer.domain.model

/** 오늘의 사용 통계. 이벤트 기록(B)에서 센다. 되돌림은 흔들어 되돌린 횟수(NFR-02). */
data class DailyStats(
    val recognizedCount: Int,
    val undoneCount: Int,
)
