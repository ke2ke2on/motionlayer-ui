package com.uhi.motionlayer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomGestureUsesTest {
    private val music = ControllableApp("music", "음악 앱", AppCategory.Music, "음")
    private val video = ControllableApp("video", "영상 앱", AppCategory.Video, "영")
    private val apps = listOf(music, video)

    @Test
    fun `제스처를 쓰는 앱만 앱 목록의 순서대로 돌려준다`() {
        val commands = mapOf(
            "video" to mapOf("circle" to GestureCommand.SkipForward),
            "music" to mapOf("circle" to GestureCommand.NextTrack, "star" to GestureCommand.PlayPause),
        )

        val uses = customGestureUses(apps, commands, gestureId = "circle")

        assertEquals(
            listOf(
                CustomGestureUse(music, GestureCommand.NextTrack),
                CustomGestureUse(video, GestureCommand.SkipForward),
            ),
            uses,
        )
    }

    @Test
    fun `아무 앱도 쓰지 않으면 빈 목록이다`() {
        val commands = mapOf("music" to mapOf("star" to GestureCommand.PlayPause))

        assertTrue(customGestureUses(apps, commands, gestureId = "circle").isEmpty())
    }

    @Test
    fun `앱 목록에 없는 앱의 연결은 무시한다`() {
        val commands = mapOf("removed-app" to mapOf("circle" to GestureCommand.NextTrack))

        assertTrue(customGestureUses(apps, commands, gestureId = "circle").isEmpty())
    }
}
