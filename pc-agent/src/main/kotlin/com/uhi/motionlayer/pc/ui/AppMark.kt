package com.uhi.motionlayer.pc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.Density
import com.uhi.motionlayer.pc.ui.theme.PcSize
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * MotionLayer 표시: 파란 둥근 네모 안의 흰 제스처 아이콘(Figma PC-02 제목 줄, PC-04 트레이 아이콘).
 */
@Composable
fun AppMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(PcSize.appMark)
            .background(MotionTheme.colors.brandPrimary, RoundedCornerShape(PcSize.appMarkRadius)),
        contentAlignment = Alignment.Center,
    ) {
        MlIconView(icon = MlIcon.Gesture, tint = MotionTheme.colors.textInverse, size = PcSize.appMarkGlyph)
    }
}

/**
 * [AppMark]를 그림 한 장으로 만든다. 트레이 아이콘과 창 아이콘은 화면 밖(운영체제)에 그려지므로 그림으로 넘겨야 한다.
 */
fun renderAppMark(pixels: Int = PcSize.APP_MARK_PIXELS): ImageBitmap {
    val density = Density(pixels / PcSize.appMark.value)
    val scene = ImageComposeScene(width = pixels, height = pixels, density = density) {
        MotionLayerTheme { AppMark() }
    }
    try {
        return scene.render().toComposeImageBitmap()
    } finally {
        scene.close()
    }
}
