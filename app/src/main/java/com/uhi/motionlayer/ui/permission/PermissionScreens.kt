package com.uhi.motionlayer.ui.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.MlCard
import com.uhi.motionlayer.ui.components.MlCardTone
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.R
import com.uhi.motionlayer.ui.components.MlCtaFrame
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlToggle
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionShadows
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.theme.motionShadow

/**
 * Figma ON-05 상시 감지 켜기. 화면이 꺼져도 동작하려면 "감지 중" 알림이 필요한 이유를 알려 주고 알림 허용을 부탁한다.
 * 시스템 허용 창(ON-06)은 안드로이드가 그리므로 만들지 않는다.
 */
@Composable
fun NotificationPermissionScreen(
    onAllow: () -> Unit,
    onLater: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PermissionGuideFrame(
        modifier = modifier,
        onBack = onBack,
        preview = { NotificationPreview() },
        title = stringResource(R.string.on05_title),
        description = stringResource(R.string.on05_description),
        primaryText = stringResource(R.string.on05_allow),
        onPrimary = onAllow,
        onLater = onLater,
    )
}

/** Figma ON-09 알림 접근 권한 안내. 재생 중인 앱을 알아내려면 설정 앱에서 알림 접근을 켜야 한다고 알려 준다. */
@Composable
fun NotificationAccessScreen(
    onOpenSettings: () -> Unit,
    onLater: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PermissionGuideFrame(
        modifier = modifier,
        onBack = onBack,
        preview = { SettingsPreview() },
        title = stringResource(R.string.on09_title),
        description = stringResource(R.string.on09_description),
        primaryText = stringResource(R.string.on09_open_settings),
        onPrimary = onOpenSettings,
        onLater = onLater,
        note = stringResource(R.string.on09_note),
    )
}

/**
 * ON-05·09가 같이 쓰는 틀: 뒤로 가기 막대, 미리보기 카드, 큰 제목, 설명, (선택) 안심 문구, 아래 버튼 두 개.
 * 본문 요소 사이는 8이고, 제목은 위로 24, 안심 문구는 위로 8이 더 떨어져 있다.
 */
@Composable
private fun PermissionGuideFrame(
    onBack: () -> Unit,
    preview: @Composable () -> Unit,
    title: String,
    description: String,
    primaryText: String,
    onPrimary: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
    note: String? = null,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    MlCtaFrame(
        modifier = modifier,
        topBar = { MlTopBar(title = null, leading = MlTopBarLeading.Back, onLeadingClick = onBack) },
        primaryText = primaryText,
        onPrimary = onPrimary,
        secondaryText = stringResource(R.string.permission_later),
        onSecondary = onLater,
        contentTopPadding = MotionSpacing.s8,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
            preview()
            Text(
                text = title,
                style = type.title2,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = MotionSpacing.s24),
            )
            Text(text = description, style = type.body2, color = colors.textSecondary)
            if (note != null) {
                Row(
                    modifier = Modifier.padding(top = MotionSpacing.s8),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
                ) {
                    MlIconView(icon = MlIcon.Shield, tint = colors.statusSuccess, size = MotionIconSize.s18)
                    Text(text = note, style = type.caption1, color = colors.textSecondary)
                }
            }
        }
    }
}

/** ON-05 위쪽 그림: 알림창에 "MotionLayer · 감지 중"이 이렇게 보인다는 예시. */
@Composable
private fun NotificationPreview() {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    MlCard(tone = MlCardTone.Gray) {
        val shape = RoundedCornerShape(MotionRadius.r16)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Figma ON-05 미리보기 카드는 떠 있는 그림자(Shadow/Floating)가 있다
                .motionShadow(MotionShadows.floating, shape)
                .background(colors.bgBase, shape)
                .padding(MotionSpacing.s16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(MotionRadius.r10))
                    .background(colors.brandPrimary),
                contentAlignment = Alignment.Center,
            ) {
                MlIconView(icon = MlIcon.Gesture, tint = colors.textInverse, size = MotionIconSize.s18)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
            ) {
                Text(
                    text = stringResource(R.string.on05_preview_title, stringResource(R.string.app_name)),
                    style = type.caption1Strong,
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.on05_preview_subtitle),
                    style = type.caption1,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

/** ON-09 위쪽 그림: 설정 앱의 "알림 접근 허용" 목록에서 MotionLayer만 켠다는 예시(눌러 볼 수 없는 그림). */
@Composable
private fun SettingsPreview() {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MotionRadius.r20))
            .background(colors.bgGrouped)
            .padding(MotionSpacing.s16),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(MotionRadius.r16))
                .background(colors.bgBase)
                .padding(vertical = MotionSpacing.s8),
        ) {
            Text(
                text = stringResource(R.string.on09_preview_caption),
                style = type.caption1Strong,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = MotionSpacing.s16, vertical = MotionSpacing.s8),
            )
            PreviewToggleRow(label = stringResource(R.string.app_name), checked = true)
            PreviewToggleRow(label = stringResource(R.string.on09_preview_other_app), checked = false)
        }
    }
}

@Composable
private fun PreviewToggleRow(label: String, checked: Boolean) {
    val colors = MotionTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MotionSpacing.s16, vertical = MotionSpacing.s10),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
    ) {
        Text(
            text = label,
            style = MotionTheme.typography.body2Strong,
            color = if (checked) colors.textPrimary else colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        MlToggle(checked = checked, onCheckedChange = null)
    }
}

@Preview(name = "ON-05 상시 감지 켜기", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun On05Preview() = MotionLayerTheme {
    NotificationPermissionScreen(onAllow = {}, onLater = {}, onBack = {})
}

@Preview(name = "ON-09 알림 접근 권한 안내", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun On09Preview() = MotionLayerTheme {
    NotificationAccessScreen(onOpenSettings = {}, onLater = {}, onBack = {})
}
