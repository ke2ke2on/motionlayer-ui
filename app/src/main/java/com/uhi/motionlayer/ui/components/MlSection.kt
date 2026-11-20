package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * Figma `SectionHeader`. 좌우 24, 위 20, 아래 8. 제목은 Headline, 오른쪽 작은 글씨는 선택 사항("전체 보기" 등).
 */
@Composable
fun MlSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: () -> Unit = {},
) {
    val colors = MotionTheme.colors
    // 위 20·아래 8 여백은 제목과 오른쪽 글자 칸 각각에 둔다. 오른쪽 글자 칸이 머리글 높이만큼 길어져서
    // 글자는 작아도 누르는 영역이 48 이상이 되고(보고서 6.2), 머리글 높이와 글자 자리는 그대로다.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = MotionSpacing.screenHorizontal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MotionTheme.typography.headline,
            color = colors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .padding(top = MotionSpacing.s20, bottom = MotionSpacing.s8),
        )
        if (actionText != null) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .defaultMinSize(minWidth = MotionSize.minTouchTarget, minHeight = MotionSize.minTouchTarget)
                    .clickable(role = Role.Button, onClick = onActionClick)
                    .padding(top = MotionSpacing.s20, bottom = MotionSpacing.s8),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = actionText,
                    style = MotionTheme.typography.body2,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

/** Figma `Divider/Thick`. 섹션 사이를 가르는 12dp 두꺼운 회색 띠. */
@Composable
fun MlDividerThick(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MotionSpacing.s12)
            .background(MotionTheme.colors.bgGrouped),
    )
}

/** Figma `Divider/Inset`. 목록 행 사이의 1dp 선(좌우 여백 24). */
@Composable
fun MlDividerInset(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MotionSpacing.screenHorizontal)
            .height(MotionBorder.thin)
            .background(MotionTheme.colors.lineDivider),
    )
}

/** Figma `SheetHandle`. 바텀시트 맨 위의 작은 손잡이(36×4). */
@Composable
fun MlSheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MotionSpacing.s24),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = MotionSize.sheetHandleWidth, height = MotionSpacing.s4)
                .clip(RoundedCornerShape(MotionRadius.r2))
                .background(MotionTheme.colors.fillStrong),
        )
    }
}
