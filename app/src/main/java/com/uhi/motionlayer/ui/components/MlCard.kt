package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** 카드의 바탕. 흰 카드는 회색 화면 위에, 회색 카드는 흰 화면 위에 놓고, 선 카드는 바탕 없이 테두리만 있다. */
enum class MlCardTone { White, Gray, Outlined }

/**
 * 보고서 공통 부품의 `Card`. 가로를 꽉 채우고 모서리 20, 좌우 안쪽 여백 20(`MotionSpacing.cardPadding`)이며 안은 세로로 쌓인다.
 * Figma에서는 카드가 컴포넌트가 아니라 같은 규격의 프레임으로 그려져 있어서, 화면마다 다시 그리던 모양을 앱에서 한 곳에 모았다
 * (같은 모양은 한 부품에서만 그린다).
 *
 * @param tone 바탕. [MlCardTone.White]는 `bg/base`, [MlCardTone.Gray]는 `bg/grouped`, [MlCardTone.Outlined]는 `line/divider` 테두리 1
 * @param verticalPadding 위아래 안쪽 여백. 기본은 좌우와 같은 20
 * @param onClick 카드 전체를 누르게 하려면 지정한다
 */
@Composable
fun MlCard(
    modifier: Modifier = Modifier,
    tone: MlCardTone = MlCardTone.White,
    verticalPadding: Dp = MotionSpacing.cardPadding,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MotionTheme.colors
    val shape = RoundedCornerShape(MotionRadius.r20)
    val surface = when (tone) {
        MlCardTone.White -> Modifier.clip(shape).background(colors.bgBase)
        MlCardTone.Gray -> Modifier.clip(shape).background(colors.bgGrouped)
        MlCardTone.Outlined -> Modifier.border(MotionBorder.thin, colors.lineDivider, shape)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(surface)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = MotionSpacing.cardPadding, vertical = verticalPadding),
        verticalArrangement = verticalArrangement,
        content = content,
    )
}
