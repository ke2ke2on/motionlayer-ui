package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 위 막대, 본문(좌우 24), 아래 고정 버튼으로 이루어진 화면 틀(PR-04·05, ON-01·03·04·05·07·08·09·11).
 * 아래 버튼은 큰 파란 버튼 + (있으면) 글자만 있는 작은 버튼이고, 둘 다 가로를 꽉 채운다.
 * 본문이 화면보다 길어지면(글자 크기를 키웠을 때 등) 본문만 스크롤되고 버튼은 그대로 아래에 남는다.
 *
 * @param primaryStyle 큰 버튼의 모양. 보통 파란 [MlButtonStyle.Primary]이고, "처음부터 다시"처럼 덜 눈에 띄는 큰 버튼은 Secondary
 * @param primaryEnabled 큰 버튼을 누를 수 있는지(이름이 비어 있을 때 등)
 * @param secondaryText 글자만 있는 작은 버튼. null이면 큰 버튼 하나만 둔다
 * @param contentHorizontalPadding 본문 좌우 여백. 목록 줄처럼 줄이 스스로 좌우 여백을 가진 본문은 0으로 쓴다
 * @param footnote 버튼 아래에 가운데로 놓는 작은 안내 글(ON-01 "로그인은 나중에 해도 돼요"). null이면 두지 않는다
 */
@Composable
fun MlCtaFrame(
    topBar: @Composable () -> Unit,
    primaryText: String,
    onPrimary: () -> Unit,
    primaryStyle: MlButtonStyle = MlButtonStyle.Primary,
    primaryEnabled: Boolean = true,
    secondaryText: String? = null,
    onSecondary: () -> Unit = {},
    modifier: Modifier = Modifier,
    contentTopPadding: Dp = MotionSpacing.s24,
    contentHorizontalPadding: Dp = MotionSpacing.screenHorizontal,
    footnote: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    MlCtaFrame(
        topBar = topBar,
        primaryContent = {
            MlButton(
                text = primaryText,
                onClick = onPrimary,
                modifier = Modifier.fillMaxWidth(),
                style = primaryStyle,
                enabled = primaryEnabled,
            )
        },
        secondaryText = secondaryText,
        onSecondary = onSecondary,
        modifier = modifier,
        contentTopPadding = contentTopPadding,
        contentHorizontalPadding = contentHorizontalPadding,
        footnote = footnote,
        content = content,
    )
}

/**
 * 위의 틀에서 큰 버튼 자리에 버튼 모양 [MlButton] 말고 다른 것을 놓는 형태(ON-10의 "Google로 계속하기" 윤곽선 버튼).
 * 나머지 모양과 간격은 같다. [primaryContent]는 가로를 꽉 채우게 그려야 한다.
 */
@Composable
fun MlCtaFrame(
    topBar: @Composable () -> Unit,
    primaryContent: @Composable () -> Unit,
    secondaryText: String? = null,
    onSecondary: () -> Unit = {},
    modifier: Modifier = Modifier,
    contentTopPadding: Dp = MotionSpacing.s24,
    contentHorizontalPadding: Dp = MotionSpacing.screenHorizontal,
    footnote: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MotionTheme.colors.bgBase)
            .statusBarsPadding(),
    ) {
        topBar()
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = contentHorizontalPadding, end = contentHorizontalPadding, top = contentTopPadding),
            content = content,
        )
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MotionSpacing.screenHorizontal,
                        end = MotionSpacing.screenHorizontal,
                        top = MotionSpacing.s12,
                        bottom = MotionSpacing.s8,
                    ),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s4),
            ) {
                primaryContent()
                if (secondaryText != null) {
                    MlButton(
                        text = secondaryText,
                        onClick = onSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        style = MlButtonStyle.Ghost,
                        size = MlButtonSize.M,
                    )
                }
            }
            if (footnote != null) {
                Text(
                    text = footnote,
                    style = MotionTheme.typography.caption1,
                    color = MotionTheme.colors.textTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, bottom = MotionSpacing.s12),
                )
            }
        }
    }
}
