package com.uhi.motionlayer.pc.ui.overlay

/**
 * 오버레이가 1초에 몇 번 다시 그려지는지 세어 콘솔에 "overlay fps=58"처럼 적는다.
 * 값이 빠르게 바뀔 때 화면이 따라오는지 재는 용도다(분석서 표 4-3: 손가락 닿음 → PC 포인터 150ms).
 *
 * `gradlew :pc-agent:run -Pfps`로 켰을 때만 동작하고, 평소에는 아무 일도 하지 않는다.
 */
internal object OverlayFrameMeter {
    private const val PROPERTY = "motionlayer.fps"
    private const val SECOND_NANOS = 1_000_000_000L

    private val enabled = System.getProperty(PROPERTY) != null
    private var windowStart = 0L
    private var frames = 0

    /** 오버레이를 한 번 그릴 때마다 부른다(화면을 그리는 스레드에서만 불린다). */
    fun tick() {
        if (!enabled) return
        val now = System.nanoTime()
        if (windowStart == 0L) windowStart = now
        frames += 1
        if (now - windowStart >= SECOND_NANOS) {
            println("overlay fps=$frames")
            frames = 0
            windowStart = now
        }
    }
}
