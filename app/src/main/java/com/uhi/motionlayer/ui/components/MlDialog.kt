package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 앱이 그리는 확인 창(Figma GS-12 `Dialog`). 어두운 막(`overlay/dim`) 위에 가로 312, 모서리 24의 흰 창이 가운데 뜨고,
 * 제목(Title/3)·설명(Body/2) 아래에 같은 너비의 버튼 두 개(작은 크기)가 나란히 있다. 시스템이 그리는 권한 창과는 다르다.
 *
 * 막을 누르거나 뒤로 가기를 누르면 [onDismiss]가 불린다(버튼 "취소"와 같다).
 *
 * @param confirmStyle 오른쪽 확인 버튼의 모양. 되돌릴 수 없는 일(삭제)은 [MlButtonStyle.Danger]
 */
@Composable
fun MlDialog(
    title: String,
    message: String,
    dismissText: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmStyle: MlButtonStyle = MlButtonStyle.Primary,
) {
    val dim = MotionTheme.colors.overlayDim.alpha

    Dialog(
        onDismissRequest = onDismiss,
        // 가로를 화면 폭에 맞추고, 양옆 여백 24는 아래 내용이 만든다.
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // 창 뒤의 막 진하기를 Figma `overlay/dim`(45%)에 맞춘다. 기본값은 더 진하다.
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(dim) }

        MlDialogContent(
            title = title,
            message = message,
            dismissText = dismissText,
            onDismiss = onDismiss,
            confirmText = confirmText,
            onConfirm = onConfirm,
            confirmStyle = confirmStyle,
            modifier = modifier,
        )
    }
}

/** 창 안쪽 모양. 미리보기는 `Dialog` 창을 그리지 못해서 안쪽만 따로 둔다. */
@Composable
private fun MlDialogContent(
    title: String,
    message: String,
    dismissText: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    confirmStyle: MlButtonStyle,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MotionSpacing.screenHorizontal)
            .clip(RoundedCornerShape(MotionRadius.r24))
            .background(colors.bgBase)
            .padding(
                start = MotionSpacing.screenHorizontal,
                end = MotionSpacing.screenHorizontal,
                top = MotionSpacing.s28,
                bottom = MotionSpacing.s20,
            ),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
    ) {
        Text(text = title, style = type.title3, color = colors.textPrimary)
        Text(text = message, style = type.body2, color = colors.textSecondary)
        // Figma는 설명과 버튼 사이에 높이 16의 빈 칸이 있고, 그 위아래에도 간격 8이 붙는다.
        Spacer(Modifier.height(MotionSpacing.s16))
        Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
            MlButton(
                text = dismissText,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                style = MlButtonStyle.Secondary,
                size = MlButtonSize.M,
            )
            MlButton(
                text = confirmText,
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                style = confirmStyle,
                size = MlButtonSize.M,
            )
        }
    }
}

@Preview(name = "Dialog · 삭제 확인", showBackground = true, widthDp = 360, heightDp = 300)
@Composable
private fun MlDialogPreview() = MotionLayerTheme {
    MlDialogContent(
        title = "'원 그리기'를 삭제할까요?",
        message = "YouTube Music에서 이 제스처로 하던 '다음 곡'이 더 이상 동작하지 않아요.",
        dismissText = "취소",
        onDismiss = {},
        confirmText = "삭제",
        onConfirm = {},
        confirmStyle = MlButtonStyle.Danger,
        modifier = Modifier.padding(vertical = MotionSpacing.s24),
    )
}
