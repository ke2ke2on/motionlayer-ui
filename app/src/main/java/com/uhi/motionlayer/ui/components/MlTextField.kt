package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * Figma `TextField` (State: Placeholder · Filled · Focused). 위에 작은 라벨, 아래에 높이 56의 입력 칸.
 * 평소에는 `fill/weak` 바탕이고, 입력 중(포커스)이면 `bg/base` 바탕에 `brand/primary` 두께 2 테두리가 둘린다.
 * 비어 있으면 [placeholder]를 `text/disabled` 색으로 보여 준다.
 *
 * 글자 상태는 호출한 쪽이 [TextFieldState]로 들고 있다(한글 조합 입력이 중간에 끊기지 않게).
 *
 * @param fieldModifier 입력 칸에 붙일 수정자. 처음부터 포커스를 주려면 `focusRequester`를 여기에 붙인다
 * @param inputTransformation 글자 수 제한처럼 입력을 걸러내는 규칙
 */
@Composable
fun MlTextField(
    state: TextFieldState,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    inputTransformation: InputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(MotionRadius.r14)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
        Text(text = label, style = type.caption1Strong, color = colors.textSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MotionSize.textField)
                .clip(shape)
                .background(if (focused) colors.bgBase else colors.fillWeak)
                .then(if (focused) Modifier.border(MotionBorder.thick, colors.brandPrimary, shape) else Modifier)
                .padding(horizontal = MotionSpacing.s16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                state = state,
                modifier = fieldModifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused },
                textStyle = type.body1.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.brandPrimary),
                lineLimits = TextFieldLineLimits.SingleLine,
                inputTransformation = inputTransformation,
                keyboardOptions = keyboardOptions,
                onKeyboardAction = onKeyboardAction,
                decorator = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (state.text.isEmpty()) {
                            Text(text = placeholder, style = type.body1, color = colors.textDisabled)
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}

@Preview(name = "TextField · Placeholder / Filled", showBackground = true, widthDp = 360)
@Composable
private fun MlTextFieldPreview() = MotionLayerTheme {
    Column(
        modifier = Modifier.padding(MotionSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s16),
    ) {
        MlTextField(state = rememberTextFieldState(), label = "라벨", placeholder = "입력하세요")
        MlTextField(state = rememberTextFieldState(initialText = "원 그리기"), label = "라벨", placeholder = "입력하세요")
    }
}
