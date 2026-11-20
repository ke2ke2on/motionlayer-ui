package com.uhi.motionlayer.pc

import com.uhi.motionlayer.pc.data.fake.FakePresentationOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 값이 계속 바뀌는 시연(움직이는 포인터, 그려지는 선)이 실제로 값을 바꿔 넣는지 본다. */
class LiveOverlayDemoTest {

    private fun withOverlay(block: suspend (FakePresentationOverlay) -> Unit) {
        val scope = CoroutineScope(Dispatchers.Default)
        try {
            runBlocking { block(FakePresentationOverlay(scope)) }
        } finally {
            scope.cancel()
        }
    }

    private fun FakePresentationOverlay.runDemo(tag: String) = demoActions.first { tag in it.label }.run()

    @Test
    fun movingPointerKeepsChangingPosition() = withOverlay { overlay ->
        overlay.runDemo("LIVE-06")
        val positions = withTimeout(3_000) {
            overlay.state.mapNotNull { it.pointer }.distinctUntilChanged().take(5).toList()
        }
        assertEquals("포인터 자리가 다섯 번 모두 달라야 한다", 5, positions.toSet().size)
        assertTrue("화면 안(0~1)에 있어야 한다", positions.all { it.x in 0f..1f && it.y in 0f..1f })
    }

    @Test
    fun strokeGrowsPointByPoint() = withOverlay { overlay ->
        overlay.runDemo("LIVE-07")
        val sizes = withTimeout(3_000) {
            overlay.state.mapNotNull { it.strokes.firstOrNull()?.points?.size }.distinctUntilChanged().take(4).toList()
        }
        assertEquals("선의 점은 늘기만 해야 한다", sizes.sorted(), sizes)
        assertTrue("선이 길어져야 한다: $sizes", sizes.last() > sizes.first())
    }
}
