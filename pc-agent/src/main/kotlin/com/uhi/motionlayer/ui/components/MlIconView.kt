package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.uhi.motionlayer.ui.theme.MotionIconSize
import org.xml.sax.InputSource

/**
 * PC판 아이콘 그리기. 쓰는 방법은 폰 앱의 `MlIconView`와 같다(기본 24dp, 색은 [tint]).
 * 폰 앱은 안드로이드 리소스에서, 여기는 프로그램에 같이 든 같은 XML 파일에서 그림을 읽는다.
 */
@Composable
fun MlIconView(
    icon: MlIcon,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = MotionIconSize.s24,
    contentDescription: String? = null,
) {
    val density = LocalDensity.current
    val vector = remember(icon, density) { loadIcon(icon, density) }
    Icon(
        painter = rememberVectorPainter(vector),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}

internal fun loadIcon(icon: MlIcon, density: Density): ImageVector {
    val path = "/drawable/${icon.file}.xml"
    val stream = checkNotNull(MlIcon::class.java.getResourceAsStream(path)) { "아이콘 파일이 없다: $path" }
    return stream.use { loadXmlImageVector(InputSource(it), density) }
}
