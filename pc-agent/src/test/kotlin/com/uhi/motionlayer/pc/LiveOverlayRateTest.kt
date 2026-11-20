package com.uhi.motionlayer.pc

import com.uhi.motionlayer.pc.data.fake.FakePresentationOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 움직이는 포인터 시연이 1초에 값을 몇 번 바꿔 넣는지 재서 `build/renders/live_rate.txt`에 적는다.
 * 오버레이가 다시 그려지는 횟수(`-Pfps`)와 견줘, 화면이 값을 다 따라가는지 볼 때 쓴다.
 */
class LiveOverlayRateTest {

    @Test
    fun movingPointerSendsManyValuesPerSecond() {
        val scope = CoroutineScope(Dispatchers.Default)
        try {
            val overlay = FakePresentationOverlay(scope)
            var changes = 0
            val seconds = 3
            runBlocking {
                overlay.demoActions.first { "LIVE-06" in it.label }.run()
                val counting = launch(Dispatchers.Default) {
                    overlay.state.mapNotNull { it.pointer }.distinctUntilChanged().collect { changes += 1 }
                }
                delay(seconds * 1_000L)
                counting.cancel()
            }
            val perSecond = changes / seconds
            File("build/renders").apply { mkdirs() }.resolve("live_rate.txt").writeText("values per second: $perSecond\n")
            assertTrue("1초에 20번은 넘게 바뀌어야 움직임으로 보인다(잰 값 $perSecond)", perSecond > 20)
        } finally {
            scope.cancel()
        }
    }
}
