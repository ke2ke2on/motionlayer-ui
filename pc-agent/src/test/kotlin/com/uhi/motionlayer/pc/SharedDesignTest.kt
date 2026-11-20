package com.uhi.motionlayer.pc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.Text
import com.uhi.motionlayer.ui.components.loadIcon
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import org.junit.Assert.assertTrue
import org.junit.Test

/** 폰 앱에서 가져다 쓰는 디자인(글꼴·아이콘)이 PC에서도 제대로 읽히는지 본다. */
class SharedDesignTest {

    @Test
    fun everyIconFileIsBundled() {
        MlIcon.entries.forEach { icon -> loadIcon(icon, Density(1f)) }
    }

    /** 가변 글꼴 하나에서 굵기 3종이 실제로 다르게 그려지는지: 굵을수록 글자가 칠해진 칸이 많아야 한다. */
    @Test
    fun koreanFontWeightsRenderDifferently() {
        val regular = inkOf("weight_regular") { MotionTheme.typography.body1 }
        val medium = inkOf("weight_medium") { MotionTheme.typography.body1Strong }
        val bold = inkOf("weight_bold") { MotionTheme.typography.body1.copy(fontWeight = MotionTheme.typography.headline.fontWeight) }
        assertTrue("Regular $regular < Medium $medium < Bold $bold 이어야 한다", regular < medium && medium < bold)
    }

    @Test
    fun rendersSampleSheet() {
        renderPng("shared_design_sample", 420, 190) {
            MotionLayerTheme {
                Column(Modifier.fillMaxSize().background(MotionTheme.colors.bgBase).padding(MotionSpacing.s16)) {
                    Text("폰 연결 가나다 Regular", style = MotionTheme.typography.body1, color = MotionTheme.colors.textPrimary)
                    Text("폰 연결 가나다 Medium", style = MotionTheme.typography.body1Strong, color = MotionTheme.colors.textPrimary)
                    Text("폰 연결 가나다 Bold", style = MotionTheme.typography.headline, color = MotionTheme.colors.textPrimary)
                    Text("482 913", style = MotionTheme.typography.title1, color = MotionTheme.colors.textPrimary)
                    Row { MlIcon.entries.forEach { MlIconView(it, tint = MotionTheme.colors.brandPrimary) } }
                }
            }
        }
    }

    private fun inkOf(name: String, style: @androidx.compose.runtime.Composable () -> androidx.compose.ui.text.TextStyle): Int {
        val image = renderPng(name, 320, 40) {
            MotionLayerTheme {
                Column(Modifier.fillMaxSize().background(MotionTheme.colors.bgBase)) {
                    Text("폰을 움직여서 조작해 보세요", style = style(), color = MotionTheme.colors.textPrimary)
                }
            }
        }
        return darkPixels(image)
    }

    private fun darkPixels(image: Image): Int {
        val bitmap = Bitmap.makeFromImage(image)
        var count = 0
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            val c = bitmap.getColor(x, y)
            val luminance = ((c shr 16 and 0xFF) + (c shr 8 and 0xFF) + (c and 0xFF)) / 3
            if (luminance < 128) count++
        }
        return count
    }
}
