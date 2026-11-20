package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 가운데 정렬 안내(Figma `Empty`): 회색 큰 원 안의 아이콘, 제목, 설명, 버튼 하나. 위에서 96 떨어져 있다.
 * 보여 줄 것이 없거나 막혔을 때 쓴다(AP-05 권한 없음, AP-08 검색 결과 없음, DV-09 인터넷 필요).
 * 그냥 안 된다고만 하지 않고, 다음에 할 일을 버튼으로 함께 준다.
 */
@Composable
fun MlEmptyState(
    icon: MlIcon,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = MotionSpacing.screenHorizontal,
                end = MotionSpacing.screenHorizontal,
                top = MotionSize.emptyStateTop,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s16),
    ) {
        MlResultBadge(icon = icon, tone = MlResultTone.Neutral)
        Text(
            text = title,
            style = type.title3,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            style = type.body2,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        MlButton(
            text = buttonText,
            onClick = onButtonClick,
            style = MlButtonStyle.Weak,
            size = MlButtonSize.M,
        )
    }
}
