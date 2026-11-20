package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.selection.selectable
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.uhi.motionlayer.R
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** 하단 탭 4개. 이름과 순서는 Figma `BottomTab`의 Active 변형(홈·앱·제스처·발표)과 같다. */
enum class MlTab(val icon: MlIcon, val labelRes: Int) {
    Home(MlIcon.Home, R.string.tab_home),
    Apps(MlIcon.Apps, R.string.tab_apps),
    Gesture(MlIcon.Gesture, R.string.tab_gesture),
    Presentation(MlIcon.Presentation, R.string.tab_presentation),
}

/**
 * Figma `BottomTab`. 높이 60, 위쪽에 `line/divider` 1dp 선, 탭 4개가 같은 폭.
 * 선택된 탭은 아이콘·글자가 `text/primary`(글자는 Strong), 나머지는 `text/disabled`.
 *
 * 시스템 내비게이션 바 높이만큼 아래로 배경을 이어서 채우므로, 화면 맨 아래에 그대로 두면 된다.
 */
@Composable
fun MlBottomTab(
    selected: MlTab,
    onSelect: (MlTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bgBase)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // 60은 최소 높이다. 시스템 글자를 키우면 탭도 같이 커지고(보고서 6.3 "탭 높이 HUG"), 탭 항목은 그 높이를 나눠 쓴다.
                .heightIn(min = MotionSize.bottomTab)
                .height(IntrinsicSize.Min)
                .drawBehind {
                    drawLine(
                        color = colors.lineDivider,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = MotionBorder.thin.toPx(),
                    )
                },
        ) {
            MlTab.entries.forEach { tab ->
                TabItem(tab = tab, isSelected = tab == selected, onClick = { onSelect(tab) })
            }
        }
    }
}

@Composable
private fun RowScope.TabItem(tab: MlTab, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MotionTheme.colors
    val tint = if (isSelected) colors.textPrimary else colors.textDisabled
    val label = stringResource(tab.labelRes)

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2, Alignment.CenterVertically),
    ) {
        MlIconView(icon = tab.icon, tint = tint)
        Text(
            text = label,
            style = if (isSelected) MotionTheme.typography.caption2Strong else MotionTheme.typography.caption2,
            color = tint,
            // 글자를 키워도 라벨은 한 줄이다. 넘치면 말줄임(보고서 6.3).
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
