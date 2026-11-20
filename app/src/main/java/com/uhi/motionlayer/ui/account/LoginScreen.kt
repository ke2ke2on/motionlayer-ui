package com.uhi.motionlayer.ui.account

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.R
import com.uhi.motionlayer.ui.components.MlCtaFrame
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.debug.DebugActionsDialog
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** 일러스트 안의 원(폰·노트북·TV)과 그 안의 아이콘, 원 사이 점 세 개의 크기(Figma ON-10). */
private val DeviceCircleSize = 64.dp
private val DeviceIconSize = 28.dp
private val ConnectorDotSize = 4.dp

/** 일러스트와 제목 사이의 간격(Figma Spacer 32). 간격 토큰에 없는 한 번만 쓰는 값이다. */
private val IllustrationGap = 32.dp

/**
 * 로그인(Figma ON-10). 첫 실행 안내의 마지막 단계에서 열리고, 설정의 "로그인" 줄에서도 열린다.
 * 로그인은 선택이라 "건너뛰기"로 지나갈 수 있다. 계정 고르는 창과 서버는 화면 밖(계정 구현)이 맡는다.
 *
 * @param signingIn 로그인하는 중이면 큰 버튼이 눌리지 않고 "로그인하는 중…"으로 바뀐다
 * @param failed 마지막 시도가 실패했으면 본문 아래에 알린다(Figma에 없어 직접 넣은 줄)
 * @param debugActions 일러스트를 길게 누르면(디버그 빌드) 열리는 시연용 동작
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(
    signingIn: Boolean,
    failed: Boolean,
    onGoogle: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    debugActions: List<DebugAction> = emptyList(),
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    var debugOpen by remember { mutableStateOf(false) }

    MlCtaFrame(
        modifier = modifier,
        topBar = { MlTopBar(title = null, leading = MlTopBarLeading.Back, onLeadingClick = onBack) },
        primaryContent = { GoogleButton(signingIn = signingIn, onClick = onGoogle) },
        secondaryText = stringResource(R.string.on10_skip),
        onSecondary = onSkip,
        contentTopPadding = MotionSpacing.s8,
    ) {
        DevicesIllustration(onLongClick = if (debugActions.isNotEmpty()) ({ debugOpen = true }) else null)
        Spacer(Modifier.height(IllustrationGap))
        Text(text = stringResource(R.string.on10_title), style = type.title2, color = colors.textPrimary)
        Text(
            text = stringResource(R.string.on10_description),
            style = type.body2,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = MotionSpacing.s8),
        )
        if (failed) {
            Text(
                text = stringResource(R.string.on10_failed),
                style = type.caption1,
                color = colors.statusDanger,
                modifier = Modifier.padding(top = MotionSpacing.s16),
            )
        }
    }

    if (debugOpen) DebugActionsDialog(actions = debugActions, onDismiss = { debugOpen = false })
}

/** 폰(파란 원) ··· 노트북 ··· TV를 점으로 이은 회색 카드. 폰에서 시작해 다른 기기로 넓힌다는 그림이다. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DevicesIllustration(onLongClick: (() -> Unit)?) {
    val colors = MotionTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MotionRadius.r20))
            .background(colors.bgGrouped)
            .then(if (onLongClick != null) Modifier.combinedClickable(onClick = {}, onLongClick = onLongClick) else Modifier)
            .padding(vertical = MotionSpacing.s28),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s10, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DeviceCircle(icon = MlIcon.Smartphone, circleColor = colors.brandWeak, iconColor = colors.brandPrimary)
        ConnectorDots()
        DeviceCircle(icon = MlIcon.Laptop, circleColor = colors.bgBase, iconColor = colors.textSecondary)
        ConnectorDots()
        DeviceCircle(icon = MlIcon.Tv, circleColor = colors.bgBase, iconColor = colors.textSecondary)
    }
}

@Composable
private fun DeviceCircle(icon: MlIcon, circleColor: Color, iconColor: Color) {
    Box(
        modifier = Modifier
            .size(DeviceCircleSize)
            .clip(CircleShape)
            .background(circleColor),
        contentAlignment = Alignment.Center,
    ) {
        MlIconView(icon = icon, tint = iconColor, size = DeviceIconSize)
    }
}

@Composable
private fun ConnectorDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s4)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(ConnectorDotSize)
                    .clip(CircleShape)
                    .background(MotionTheme.colors.textDisabled),
            )
        }
    }
}

/**
 * "Google로 계속하기" 윤곽선 버튼(Figma ON-10의 GoogleButton: 높이 56, 모서리 16, `line/strong` 1dp 테두리).
 * 파란 "G"와 글자를 가운데에 놓는다. 로그인하는 중에는 글자가 바뀌고 눌리지 않는다.
 */
@Composable
private fun GoogleButton(signingIn: Boolean, onClick: () -> Unit) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val shape = RoundedCornerShape(MotionRadius.r16)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MotionSize.buttonL)
            .clip(shape)
            .background(colors.bgBase)
            .border(width = MotionBorder.thin, color = colors.lineStrong, shape = shape)
            .clickable(enabled = !signingIn, role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s10, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(R.string.on10_google_mark), style = type.headline, color = colors.brandPrimary)
        Text(
            text = stringResource(if (signingIn) R.string.on10_google_loading else R.string.on10_google),
            style = type.body1Strong,
            color = if (signingIn) colors.textTertiary else colors.textPrimary,
        )
    }
}

@Preview(name = "ON-10 로그인", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun On10Preview() = MotionLayerTheme {
    LoginScreen(signingIn = false, failed = false, onGoogle = {}, onSkip = {}, onBack = {})
}

@Preview(name = "ON-10 로그인 · 하는 중", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun On10SigningInPreview() = MotionLayerTheme {
    LoginScreen(signingIn = true, failed = false, onGoogle = {}, onSkip = {}, onBack = {})
}

@Preview(name = "ON-10 로그인 · 실패", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun On10FailedPreview() = MotionLayerTheme {
    LoginScreen(signingIn = false, failed = true, onGoogle = {}, onSkip = {}, onBack = {})
}
