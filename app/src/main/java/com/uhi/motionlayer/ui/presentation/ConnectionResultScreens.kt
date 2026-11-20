package com.uhi.motionlayer.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.uhi.motionlayer.ui.components.MlCard
import com.uhi.motionlayer.ui.components.MlCardTone
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.ConnectionRoute
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.ui.components.MlCtaFrame
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlResultBadge
import com.uhi.motionlayer.ui.components.MlResultTone
import com.uhi.motionlayer.ui.components.MlSpinner
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.KoreanParticle
import com.uhi.motionlayer.ui.util.formatDecimal
import com.uhi.motionlayer.ui.util.rememberElapsedSeconds

/** Figma PR-04 연결됨. 연결에 성공한 PC와 연결 방식·응답 속도를 알려 주고 발표 준비로 넘긴다. */
@Composable
fun ConnectedScreen(
    connection: PcConnectionState.Connected,
    onPrepare: () -> Unit,
    onConnectOther: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val isLocal = connection.route == ConnectionRoute.Local

    MlCtaFrame(
        modifier = modifier,
        topBar = { Box(Modifier.height(MotionSize.topBar)) },
        primaryText = stringResource(R.string.pr04_prepare),
        onPrimary = onPrepare,
        secondaryText = stringResource(R.string.pr04_other_pc),
        onSecondary = onConnectOther,
    ) {
        MlResultBadge(icon = MlIcon.Check, tone = MlResultTone.Success)
        Box(Modifier.height(MotionSpacing.s28))
        Text(
            text = stringResource(
                R.string.pr04_title,
                connection.deviceName,
                KoreanParticle.with(connection.deviceName),
            ),
            style = type.title2,
            color = colors.textPrimary,
        )
        Box(Modifier.height(MotionSpacing.s8))
        Text(
            text = stringResource(if (isLocal) R.string.pr04_description_local else R.string.pr04_description_server),
            style = type.body2,
            color = colors.textSecondary,
        )
        Box(Modifier.height(MotionSpacing.s24))
        InfoCard {
            InfoRow(
                label = stringResource(R.string.pr04_method_label),
                value = stringResource(if (isLocal) R.string.pr04_method_local else R.string.pr04_method_server),
            )
            InfoRow(
                label = stringResource(R.string.pr04_latency_label),
                value = stringResource(R.string.pr04_latency_value, connection.latencyMs),
            )
            InfoRow(
                label = stringResource(R.string.pr04_internet_label),
                value = stringResource(if (isLocal) R.string.pr04_internet_not_needed else R.string.pr04_internet_needed),
            )
            // 계정 기능이 범위 밖이라 연결 종류는 항상 "이번만 연결"이다.
            InfoRow(
                label = stringResource(R.string.pr04_kind_label),
                value = stringResource(R.string.pr04_kind_one_time),
            )
        }
    }
}

/**
 * Figma PR-16 PC에 연결하는 중. QR·코드·최근 PC를 고른 뒤 결과(연결됨 PR-04 또는 실패 PR-05)가 나올 때까지 보이는 대기 화면이다.
 * 어떤 길로 시도하는 중인지와 자동 전환 규칙을 글로 알려 주고, 취소는 항상 열어 둔다. 위 막대는 없고 뒤로 가기도 취소와 같다.
 * 응답 속도·인터넷은 아직 모르는 값이라 "측정 중…"·"확인 중"으로 고정해서 보여 준다.
 *
 * @param pcName 연결하려는 PC의 이름. 최근 PC를 골랐을 때만 알고, 모르면 "PC에 연결하는 중이에요"로 말한다
 * @param onCancel 연결 시도를 멈추고 이전 화면으로 돌아간다
 */
@Composable
fun ConnectingScreen(
    pcName: String?,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    // "연결하는 중 · 1.2초"는 이 화면이 열린 뒤 흐른 시간이다.
    val elapsed by rememberElapsedSeconds()

    MlCtaFrame(
        modifier = modifier,
        topBar = { Box(Modifier.height(MotionSize.topBar)) },
        primaryText = stringResource(R.string.pr16_button, formatDecimal(elapsed)),
        onPrimary = {},
        primaryEnabled = false,
        secondaryText = stringResource(R.string.pr16_cancel),
        onSecondary = onCancel,
    ) {
        MlResultBadge(container = colors.bgBase) {
            MlSpinner(diameter = MotionIconSize.s44)
        }
        Box(Modifier.height(MotionSpacing.s28))
        Text(
            text = if (pcName != null) stringResource(R.string.pr16_title, pcName) else stringResource(R.string.pr16_title_unknown),
            style = type.title2,
            color = colors.textPrimary,
        )
        Box(Modifier.height(MotionSpacing.s8))
        Text(text = stringResource(R.string.pr16_description), style = type.body2, color = colors.textSecondary)
        Box(Modifier.height(MotionSpacing.s24))
        InfoCard {
            InfoRow(label = stringResource(R.string.pr04_method_label), value = stringResource(R.string.pr16_method_trying))
            InfoRow(label = stringResource(R.string.pr04_latency_label), value = stringResource(R.string.pr16_latency_measuring))
            InfoRow(label = stringResource(R.string.pr04_internet_label), value = stringResource(R.string.pr16_internet_checking))
            InfoRow(label = stringResource(R.string.pr04_kind_label), value = stringResource(R.string.pr04_kind_one_time))
        }
    }
}

/** Figma PR-05 연결 실패. 왜 안 됐는지 짐작되는 원인 세 가지를 번호로 알려 주고 다시 시도하게 한다. */
@Composable
fun ConnectionFailedScreen(
    onRescan: () -> Unit,
    onEnterCode: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    MlCtaFrame(
        modifier = modifier,
        topBar = { MlTopBar(title = null, leading = MlTopBarLeading.Close, onLeadingClick = onClose) },
        primaryText = stringResource(R.string.pr05_rescan),
        onPrimary = onRescan,
        secondaryText = stringResource(R.string.pr05_enter_code),
        onSecondary = onEnterCode,
        contentTopPadding = MotionSpacing.s16,
    ) {
        MlResultBadge(icon = MlIcon.Alert, tone = MlResultTone.Danger)
        Box(Modifier.height(MotionSpacing.s28))
        Text(text = stringResource(R.string.pr05_title), style = type.title2, color = colors.textPrimary)
        Box(Modifier.height(MotionSpacing.s8))
        Text(text = stringResource(R.string.pr05_description), style = type.body2, color = colors.textSecondary)
        Box(Modifier.height(MotionSpacing.s24))
        MlCard(
            tone = MlCardTone.Gray,
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s16),
        ) {
            ChecklistItem(number = 1, text = stringResource(R.string.pr05_check_1))
            ChecklistItem(number = 2, text = stringResource(R.string.pr05_check_2))
            ChecklistItem(number = 3, text = stringResource(R.string.pr05_check_3))
        }
    }
}

/** 회색 둥근 판 안에 [InfoRow]를 쌓은 정보 카드(PR-04 연결됨, PR-16 연결하는 중). */
@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    MlCard(tone = MlCardTone.Gray, verticalPadding = MotionSpacing.s8, content = content)
}

/**
 * 정보 카드의 한 줄: 왼쪽에 이름, 오른쪽에 값. 글자를 키워 한 줄에 다 들어가지 않으면(보고서 6.3)
 * 이름 위 · 값 아래로 쌓아서 "연결 / 방식"처럼 이름이 두 줄로 갈라지는 일이 없게 한다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoRow(label: String, value: String) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MotionSpacing.s10),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MotionTheme.typography.body2,
            color = MotionTheme.colors.textSecondary,
        )
        Text(
            text = value,
            style = MotionTheme.typography.body2Strong,
            color = MotionTheme.colors.textPrimary,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun ChecklistItem(number: Int, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12)) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MotionTheme.colors.bgBase),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                style = MotionTheme.typography.caption2Strong,
                color = MotionTheme.colors.textSecondary,
            )
        }
        Text(
            text = text,
            style = MotionTheme.typography.body2,
            color = MotionTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(name = "PR-16 PC에 연결하는 중", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr16Preview() = MotionLayerTheme {
    ConnectingScreen(pcName = SampleData.classroomPc.deviceName, onCancel = {})
}

@Preview(name = "PR-04 연결됨", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr04Preview() = MotionLayerTheme {
    ConnectedScreen(connection = SampleData.classroomPc, onPrepare = {}, onConnectOther = {})
}

@Preview(name = "PR-05 연결 실패", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr05Preview() = MotionLayerTheme {
    ConnectionFailedScreen(onRescan = {}, onEnterCode = {}, onClose = {})
}
