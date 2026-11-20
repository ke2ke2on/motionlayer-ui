package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * Figma 바텀시트(PR-15, AP-03, HM-06 …). 어두운 막(`overlay/dim`) 위에 흰 시트가 올라오고,
 * 위쪽 모서리는 24, 맨 위에 손잡이(`SheetHandle`)가 있다. 내용은 시트 맨 아래의 내비게이션 바 위까지 채운다.
 *
 * 막을 누르거나 시트를 아래로 밀거나 뒤로 가기를 누르면 [onDismissRequest]가 불린다.
 * 시트는 처음부터 끝까지 펼쳐진 채로만 열린다(반쯤 열리는 중간 단계가 없다).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MlBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        // 화면 내용과 같은 폭 상한(480). 폰에서는 화면보다 좁아질 일이 없다(보고서 6.3).
        sheetMaxWidth = MotionSize.contentMaxWidth,
        shape = RoundedCornerShape(topStart = MotionRadius.r24, topEnd = MotionRadius.r24),
        containerColor = MotionTheme.colors.bgBase,
        tonalElevation = 0.dp,
        scrimColor = MotionTheme.colors.overlayDim,
        dragHandle = { MlSheetHandle() },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Column(modifier = Modifier.navigationBarsPadding(), content = content)
    }
}
