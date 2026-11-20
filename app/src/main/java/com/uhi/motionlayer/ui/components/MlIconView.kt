package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.uhi.motionlayer.ui.theme.MotionIconSize

/**
 * Figma 아이콘(`icon/xxx`) 하나를 그린다. 기본 크기는 Figma와 같은 24dp.
 * 아이콘 파일은 검정 선으로만 만들어 두었고 색은 [tint]로 정한다.
 *
 * @param contentDescription 스크린리더가 읽을 설명. 글자와 함께 쓰는 장식용 아이콘이면 null.
 */
@Composable
fun MlIconView(
    icon: MlIcon,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = MotionIconSize.s24,
    contentDescription: String? = null,
) {
    Icon(
        painter = painterResource(icon.res),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}
