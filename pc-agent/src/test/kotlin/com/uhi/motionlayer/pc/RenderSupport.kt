package com.uhi.motionlayer.pc

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File

/**
 * 화면을 창 없이 그림 파일로 그려 낸다. Figma 시안과 나란히 비교할 때 쓴다.
 * 결과는 `pc-agent/build/renders/<이름>.png`에 남는다.
 */
internal fun renderPng(name: String, width: Int, height: Int, content: @Composable () -> Unit): Image {
    val scene = ImageComposeScene(width = width, height = height, density = Density(1f), content = content)
    try {
        val image = scene.render()
        val bytes = checkNotNull(image.encodeToData(EncodedImageFormat.PNG)) { "PNG로 바꾸지 못했다" }.bytes
        File("build/renders").apply { mkdirs() }.resolve("$name.png").writeBytes(bytes)
        return image
    } finally {
        scene.close()
    }
}
