package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** Figma `ListRow`의 Leading. 아이콘 타일 또는 앱 아이콘. 없으면 null. */
sealed interface MlListRowLeading {
    data class Icon(val icon: MlIcon, val tone: MlTone = MlTone.Gray) : MlListRowLeading
    data class App(val letter: String, val color: Color? = null) : MlListRowLeading
}

/** Figma `ListRow`의 Trailing. 오른쪽 화살표, 토글, 값(+화살표). 없으면 null. */
sealed interface MlListRowTrailing {
    data object Chevron : MlListRowTrailing

    /** [onCheckedChange]가 null이면 표시만 하고 누를 수 없다. */
    data class Toggle(val checked: Boolean, val onCheckedChange: ((Boolean) -> Unit)?) : MlListRowTrailing

    data class Value(val text: String, val chevron: Boolean = true) : MlListRowTrailing
}

/**
 * Figma `ListRow` (Leading 3 × Trailing 5). 좌우 여백 24, 위아래 12, 구성 요소 사이 14.
 * 제목은 Body/1 Strong, 설명은 Caption/1 `text/tertiary`.
 *
 * @param onClick 행 전체를 누르게 하려면 지정한다. 토글 행은 보통 토글만 누르므로 null.
 * @param subtitleColor 설명 글자색. 첫 실행 안내(ON-04·08)는 `text/secondary`로 덮어써서 쓴다. null이면 `text/tertiary`
 */
@Composable
fun MlListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: MlListRowLeading? = null,
    trailing: MlListRowTrailing? = null,
    onClick: (() -> Unit)? = null,
    subtitleColor: Color? = null,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            // Figma ListRow는 설명이 없어도 높이가 64 아래로 줄지 않는다(아이콘 타일 40 + 위아래 12).
            .heightIn(min = MotionSize.listRowMin)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
    ) {
        when (leading) {
            is MlListRowLeading.Icon -> MlIconTile(icon = leading.icon, tone = leading.tone)
            is MlListRowLeading.App ->
                MlAppIcon(letter = leading.letter, color = leading.color ?: colors.brandPrimary)
            null -> Unit
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
        ) {
            // 행은 최소 높이(64)만 정해 두고, 글자를 키우면(보고서 6.3) 두 줄까지 줄을 바꿔 늘어난다.
            Text(
                text = title,
                style = type.body1Strong,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = type.caption1,
                    color = subtitleColor ?: colors.textTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        when (trailing) {
            MlListRowTrailing.Chevron -> RowChevron()
            is MlListRowTrailing.Toggle -> MlToggle(checked = trailing.checked, onCheckedChange = trailing.onCheckedChange)
            is MlListRowTrailing.Value -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
            ) {
                Text(text = trailing.text, style = type.body2, color = colors.textTertiary, maxLines = 1)
                if (trailing.chevron) RowChevron()
            }
            null -> Unit
        }
    }
}

@Composable
private fun RowChevron() {
    MlIconView(icon = MlIcon.ChevronRight, tint = MotionTheme.colors.textDisabled, size = MotionIconSize.s20)
}
