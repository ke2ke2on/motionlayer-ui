package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * Figma `Chip` (Selected True/False). 높이 36, 완전히 둥근 알약 모양.
 * 36은 최소 높이라서 시스템 글자를 키우면 칩도 같이 커진다(보고서 6.3). 글자는 줄바꿈하지 않는다.
 * 누르는 영역은 Compose 기본 최소 터치 크기가 위아래 6씩 넓혀 48이 된다(보고서 6.2, 접근성 정보로 확인).
 */
@Composable
fun MlChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val container = if (selected) colors.textPrimary else colors.fillWeak
    val content = if (selected) colors.textInverse else colors.textSecondary

    Box(
        modifier = modifier
            .heightIn(min = MotionSize.chip)
            .clip(RoundedCornerShape(MotionRadius.r18))
            .background(container)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = MotionSpacing.s14, vertical = MotionSpacing.s6),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MotionTheme.typography.body2Strong, color = content, maxLines = 1)
    }
}

/**
 * 칩이 한 줄로 늘어선 모양. 칩 사이 간격은 8이고, 칩은 줄을 바꾸지 않는다.
 * 글자를 키워 칩이 화면 폭을 넘으면 가로로 스크롤한다(보고서 6.3 "칩은 줄바꿈 금지 · 가로 스크롤").
 *
 * @param contentPadding 스크롤 안쪽 여백. 처음과 끝에서도 화면 가장자리에서 떨어져 있고, 밀어서 넘길 때는 칩이 가장자리까지 간다.
 */
@Composable
fun MlChipRow(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = MotionSpacing.screenHorizontal),
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
