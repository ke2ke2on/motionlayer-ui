package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 상세 화면 맨 아래의 되돌리기 어려운 행("제스처 삭제", "내 기기에서 삭제"). 가운데 정렬 빨간 글자(`status/danger`,
 * Body/2 Strong), 위아래 20. 눌러도 바로 지우지 않고 확인 창을 먼저 띄우는 것은 쓰는 곳에서 한다.
 */
@Composable
fun MlDestructiveRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = MotionSpacing.s20),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MotionTheme.typography.body2Strong, color = MotionTheme.colors.statusDanger)
    }
}
