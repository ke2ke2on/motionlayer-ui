package com.uhi.motionlayer.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.PairingTarget
import com.uhi.motionlayer.ui.components.MlNumPad
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import kotlinx.coroutines.delay

const val PAIRING_CODE_LENGTH = 6

/** 화면이 열린 뒤 코드가 바뀌기까지 보여 주는 초. 분석서: 코드(QR 토큰)는 60초마다 바뀐다. */
private const val CODE_LIFETIME_SECONDS = 60

/**
 * Figma PR-03 코드 입력. 6자리를 다 누르면 자동으로 연결을 시도한다.
 *
 * 남은 시간은 **이 화면이 열린 때부터 센 값**이다. PC의 실제 코드 만료 시각은 폰이 알 수 없어서,
 * 연결 쪽(C)이 실제 만료 시각을 알려 주면 그 값으로 바꾼다.
 */
@Composable
fun CodeEntryRoute(
    pairing: PairingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CodeEntryRoute(
        onSubmit = { code -> pairing.connect(PairingTarget.SixDigitCode(code)) },
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * 같은 코드 입력 화면을 PC 연결이 아닌 쪽(기기 추가의 "코드 입력")에서도 쓰는 형태. 6자리를 다 누르면 [onSubmit]을 부른다.
 *
 * @param title 맨 위 제목. null이면 PC 연결의 "PC에 보이는 6자리 코드를 입력해 주세요"
 */
@Composable
fun CodeEntryRoute(
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    var code by rememberSaveable { mutableStateOf("") }
    val secondsLeft by produceCountdown(CODE_LIFETIME_SECONDS)

    CodeEntryContent(
        code = code,
        secondsLeft = secondsLeft,
        title = title,
        onDigit = { digit ->
            if (code.length < PAIRING_CODE_LENGTH) {
                code += digit
                if (code.length == PAIRING_CODE_LENGTH) onSubmit(code)
            }
        },
        onBackspace = { code = code.dropLast(1) },
        onBack = onBack,
        modifier = modifier,
    )
}

/** 1초마다 1씩 줄어 0에서 멈추는 값. */
@Composable
private fun produceCountdown(start: Int): State<Int> = produceState(initialValue = start) {
    while (value > 0) {
        delay(1_000)
        value -= 1
    }
}

@Composable
fun CodeEntryContent(
    code: String,
    secondsLeft: Int,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = null, leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, top = MotionSpacing.s4),
        ) {
            Text(text = title ?: stringResource(R.string.pr03_title), style = type.title2, color = colors.textPrimary)
            Box(Modifier.height(MotionSpacing.s28))
            CodeCells(code = code)
            Box(Modifier.height(MotionSpacing.s12))
            Text(
                text = stringResource(R.string.pr03_countdown, secondsLeft / 60, secondsLeft % 60),
                style = type.caption1,
                color = colors.textTertiary,
            )
        }

        MlNumPad(onDigit = onDigit, onBackspace = onBackspace)
    }
}

/** 6칸짜리 코드 입력 칸. 다음에 입력할 칸은 파란 테두리와 깜빡이는 막대(커서 자리)로 보여 준다. */
@Composable
private fun CodeCells(code: String) {
    val colors = MotionTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
    ) {
        repeat(PAIRING_CODE_LENGTH) { index ->
            val digit = code.getOrNull(index)
            val isActive = index == code.length
            val shape = RoundedCornerShape(MotionRadius.r12)
            val description = stringResource(
                R.string.pr03_code_cell,
                index + 1,
                digit?.toString() ?: stringResource(R.string.pr03_code_cell_empty),
            )
            Box(
                modifier = Modifier
                    // 칸 폭은 전체 폭을 6으로 나눈 값이라 글자를 키워도(보고서 6.3) 칸이 화면 밖으로 밀리지 않는다. 높이는 최소값이다.
                    .weight(1f)
                    .heightIn(min = MotionSize.codeCell)
                    .background(if (isActive) colors.bgBase else colors.fillWeak, shape)
                    .then(if (isActive) Modifier.border(MotionBorder.thick, colors.brandPrimary, shape) else Modifier)
                    .semantics { contentDescription = description },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    digit != null -> Text(
                        text = digit.toString(),
                        style = MotionTheme.typography.title2,
                        color = colors.textPrimary,
                        modifier = Modifier.clearAndSetSemantics { },
                    )
                    isActive -> Box(Modifier.size(width = 2.dp, height = 24.dp).background(colors.brandPrimary))
                }
            }
        }
    }
}

@Preview(name = "PR-03 코드 입력 (482 입력 중)", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr03Preview() = MotionLayerTheme {
    CodeEntryContent(code = "482", secondsLeft = 42, onDigit = {}, onBackspace = {}, onBack = {})
}

@Preview(name = "PR-03 코드 입력 (비어 있음)", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr03EmptyPreview() = MotionLayerTheme {
    CodeEntryContent(code = "", secondsLeft = 60, onDigit = {}, onBackspace = {}, onBack = {})
}
