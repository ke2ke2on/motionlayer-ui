package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.uhi.motionlayer.R
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** Figma `TopBar`의 Leading. 뒤로가기 화살표, 닫기(X), 없음. */
enum class MlTopBarLeading { Back, Close, None }

/**
 * Figma `TopBar`. 높이 56, 제목은 Headline.
 *
 * - 왼쪽 버튼(48×48)이 없는 [MlTopBarLeading.None]이면 제목이 왼쪽 여백 24에서 시작한다.
 * - [actionIcon]을 주면 오른쪽에 48×48 버튼이 생긴다. 안 주면 제목이 끝까지 차지한다.
 * - 상태 바 높이는 포함하지 않는다. 화면에서 `statusBarsPadding()` 등으로 처리한다.
 */
@Composable
fun MlTopBar(
    title: String?,
    modifier: Modifier = Modifier,
    leading: MlTopBarLeading = MlTopBarLeading.Back,
    onLeadingClick: () -> Unit = {},
    actionIcon: MlIcon? = null,
    actionDescription: String? = null,
    onActionClick: () -> Unit = {},
) {
    val colors = MotionTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MotionSize.topBar)
            .padding(
                start = if (leading == MlTopBarLeading.None) MotionSpacing.screenHorizontal else MotionSpacing.s8,
                end = MotionSpacing.s8,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s4),
    ) {
        when (leading) {
            MlTopBarLeading.Back -> TopBarButton(
                icon = MlIcon.ChevronLeft,
                description = stringResource(R.string.ml_back),
                onClick = onLeadingClick,
            )
            MlTopBarLeading.Close -> TopBarButton(
                icon = MlIcon.Close,
                description = stringResource(R.string.ml_close),
                onClick = onLeadingClick,
            )
            MlTopBarLeading.None -> Unit
        }

        if (title != null) {
            Text(
                text = title,
                style = MotionTheme.typography.headline,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }

        if (actionIcon != null) {
            TopBarButton(icon = actionIcon, description = actionDescription, onClick = onActionClick)
        }
    }
}

@Composable
private fun TopBarButton(icon: MlIcon, description: String?, onClick: () -> Unit) {
    MlIconButton(icon = icon, onClick = onClick, contentDescription = description)
}
