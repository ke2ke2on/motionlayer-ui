package com.uhi.motionlayer.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.PairingTarget
import com.uhi.motionlayer.domain.model.RecentPc
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** 발표 탭 첫 화면(Figma PR-01 발표 · 연결 전). 하단 탭은 앱 틀에서 붙인다. */
@Composable
fun PresentationRoute(
    onConnectNew: () -> Unit,
    pairing: PairingViewModel,
    modifier: Modifier = Modifier,
) {
    val recent by pairing.recentPcs.collectAsStateWithLifecycle()
    PresentationContent(
        recentPcs = recent,
        onConnectNew = onConnectNew,
        onConnectRecent = { pairing.connect(PairingTarget.Recent(it.id)) },
        modifier = modifier,
    )
}

@Composable
fun PresentationContent(
    recentPcs: List<RecentPc>,
    onConnectNew: () -> Unit,
    onConnectRecent: (RecentPc) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        // 제목과 설명 (위·아래 16)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s16,
                    bottom = MotionSpacing.s16,
                ),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
        ) {
            Text(text = stringResource(R.string.pr01_title), style = type.title2, color = colors.textPrimary)
            Text(text = stringResource(R.string.pr01_description), style = type.body2, color = colors.textSecondary)
        }

        // 폰 ··· PC 그림과 연결 버튼
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MotionSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
        ) {
            ConnectIllustration()
            MlButton(
                text = stringResource(R.string.pr01_connect),
                onClick = onConnectNew,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        MlSectionHeader(title = stringResource(R.string.pr01_recent_header))
        if (recentPcs.isEmpty()) {
            EmptyRecentPcs()
        }
        recentPcs.forEach { pc ->
            MlListRow(
                title = pc.name,
                subtitle = stringResource(
                    R.string.pr01_recent_subtitle,
                    pc.description,
                    stringResource(R.string.date_month_day, pc.lastConnected.monthValue, pc.lastConnected.dayOfMonth),
                ),
                leading = MlListRowLeading.Icon(MlIcon.Laptop),
                trailing = MlListRowTrailing.Chevron,
                onClick = { onConnectRecent(pc) },
            )
        }

        MlBanner(
            message = stringResource(R.string.pr01_install_notice),
            tone = MlBannerTone.Neutral,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, top = MotionSpacing.s8),
        )
        Box(Modifier.padding(bottom = MotionSpacing.s16))
    }
}

/**
 * 아직 연결한 PC가 하나도 없을 때의 안내(Figma PR-17). "없음"만 말하지 않고 다음 행동(위의 "PC 연결하기")과 조건(PC에 설치)을
 * 함께 알려 준다. 큰 버튼은 그대로 두어 처음 쓰는 사람의 첫 행동이 하나로 보이게 한다.
 */
@Composable
private fun EmptyRecentPcs() {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MotionSpacing.screenHorizontal,
                end = MotionSpacing.screenHorizontal,
                top = MotionSpacing.s12,
                bottom = MotionSpacing.s12,
            ),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
    ) {
        Text(text = stringResource(R.string.pr17_empty_title), style = type.body1Strong, color = colors.textPrimary)
        Text(text = stringResource(R.string.pr17_empty_description), style = type.caption1, color = colors.textTertiary)
    }
}

/** 회색 판 위에 폰(파랑) ··· 와이파이 ··· PC가 놓인 장식 그림. 높이 128, 모서리 20. */
@Composable
private fun ConnectIllustration() {
    val colors = MotionTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MotionRadius.r20))
            .background(colors.bgGrouped)
            .padding(vertical = MotionSpacing.s28),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IllustrationCircle(icon = MlIcon.Smartphone, tint = colors.brandPrimary)
        MlIconView(icon = MlIcon.Wifi, tint = colors.textDisabled)
        IllustrationCircle(icon = MlIcon.Laptop, tint = colors.textSecondary)
    }
}

@Composable
private fun IllustrationCircle(icon: MlIcon, tint: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MotionTheme.colors.bgBase),
        contentAlignment = Alignment.Center,
    ) {
        MlIconView(icon = icon, tint = tint, size = MotionIconSize.s32)
    }
}
