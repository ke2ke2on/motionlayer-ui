package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.R
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * Figma `NumPad`. 숫자 키 3열 × 4행(마지막 줄은 빈칸·0·지우기). 키 높이 56, 좌우 여백 12, 아래 8.
 * 시스템 키보드 대신 쓰는 화면 안 키패드라서 내비게이션 바 높이만큼 아래를 띄운다.
 */
@Composable
fun MlNumPad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MotionTheme.colors.bgBase)
            .navigationBarsPadding()
            .padding(start = MotionSpacing.s12, end = MotionSpacing.s12, bottom = MotionSpacing.s8),
    ) {
        listOf("123", "456", "789").forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { digit -> DigitKey(digit = digit, onClick = { onDigit(digit) }) }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f).heightIn(min = KEY_HEIGHT))
            DigitKey(digit = '0', onClick = { onDigit('0') })
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = KEY_HEIGHT)
                    .clickable(role = Role.Button, onClick = onBackspace),
                contentAlignment = Alignment.Center,
            ) {
                MlIconView(
                    icon = MlIcon.ArrowLeft,
                    tint = MotionTheme.colors.textPrimary,
                    contentDescription = stringResource(R.string.ml_backspace),
                )
            }
        }
    }
}

private val KEY_HEIGHT = 56.dp

@Composable
private fun RowScope.DigitKey(digit: Char, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            // 56은 최소 높이다. 시스템 글자를 키워도 숫자가 잘리지 않게 키가 같이 커진다(보고서 6.3).
            .heightIn(min = KEY_HEIGHT)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = digit.toString(), style = MotionTheme.typography.title2, color = MotionTheme.colors.textPrimary)
    }
}
