package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 아이콘 하나만 있는 동그란 버튼. 누르는 영역은 48dp이고 아이콘은 24dp다(Figma TopBar의 뒤로·닫기, 홈의 설정 톱니).
 * 위 막대와 홈 머리글이 같은 버튼을 쓰도록 한 곳에서 만든다.
 *
 * @param contentDescription 스크린리더가 읽을 이름. 아이콘만 있는 버튼이라 꼭 정해 준다
 */
@Composable
fun MlIconButton(
    icon: MlIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = MotionTheme.colors.textPrimary,
) {
    Box(
        modifier = modifier
            .size(MotionSize.iconButton)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        MlIconView(icon = icon, tint = tint, contentDescription = contentDescription)
    }
}
