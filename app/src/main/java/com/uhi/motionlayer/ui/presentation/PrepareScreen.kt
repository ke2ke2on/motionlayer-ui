package com.uhi.motionlayer.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.MlCard
import com.uhi.motionlayer.ui.components.MlCardTone
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.ConnectionRoute
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.PresentationSetup
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlBottomSheet
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlChip
import com.uhi.motionlayer.ui.components.MlChipRow
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconTile
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** 고를 수 있는 발표 시간(분). Figma PR-15의 칩 4개. */
private val DurationOptions = listOf(5, 10, 15, 20)

/** 발표 준비(Figma PR-06)의 연결 부분. 발표 시간과 진동은 [PresentingViewModel]이 들고 있다. */
@Composable
fun PrepareRoute(
    viewModel: PresentingViewModel,
    connection: PcConnectionState.Connected,
    onBack: () -> Unit,
    onOpenEffects: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val setup by viewModel.setup.collectAsStateWithLifecycle()
    val readyEffectCount by viewModel.readyEffectCount.collectAsStateWithLifecycle()
    PrepareContent(
        connection = connection,
        setup = setup,
        readyEffectCount = readyEffectCount,
        onBack = onBack,
        onSaveDuration = viewModel::setDurationMinutes,
        onChangeVibrate = viewModel::setVibrateBeforeEnd,
        onOpenEffects = onOpenEffects,
        onStart = onStart,
        modifier = modifier,
    )
}

/**
 * 연결된 PC, 발표 시간, 5분 전 진동, 미리 보낸 효과, 조작 방법과 "발표 시작" 버튼. 상태를 받아 그리기만 한다.
 * 발표 시간 줄을 누르면 시간 시트(PR-15)가, "미리 보낸 효과" 줄을 누르면 효과 관리(PR-14)가 열린다.
 *
 * @param readyEffectCount PC로 다 보낸 효과 수("2개 준비됨")
 */
@Composable
fun PrepareContent(
    connection: PcConnectionState.Connected,
    setup: PresentationSetup,
    readyEffectCount: Int,
    onBack: () -> Unit,
    onSaveDuration: (Int) -> Unit,
    onChangeVibrate: (Boolean) -> Unit,
    onOpenEffects: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyShowDurationSheet: Boolean = false,
) {
    val colors = MotionTheme.colors
    var showDurationSheet by rememberSaveable { mutableStateOf(initiallyShowDurationSheet) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(
            title = stringResource(R.string.pr06_title),
            leading = MlTopBarLeading.Back,
            onLeadingClick = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            ConnectedPcCard(
                connection = connection,
                modifier = Modifier.padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s4,
                ),
            )

            MlSectionHeader(title = stringResource(R.string.pr06_timer_header))
            MlListRow(
                title = stringResource(R.string.pr06_duration_title),
                leading = MlListRowLeading.Icon(MlIcon.Timer),
                trailing = MlListRowTrailing.Value(stringResource(R.string.pr06_duration_value, setup.durationMinutes)),
                onClick = { showDurationSheet = true },
            )
            MlListRow(
                title = stringResource(R.string.pr06_vibrate_title),
                subtitle = stringResource(R.string.pr06_vibrate_subtitle),
                leading = MlListRowLeading.Icon(MlIcon.Vibrate),
                trailing = MlListRowTrailing.Toggle(checked = setup.vibrateBeforeEnd, onCheckedChange = onChangeVibrate),
            )

            MlSectionHeader(title = stringResource(R.string.pr06_effects_header))
            MlListRow(
                title = stringResource(R.string.pr06_effects_title),
                leading = MlListRowLeading.Icon(MlIcon.Image),
                trailing = MlListRowTrailing.Value(stringResource(R.string.pr06_effects_value, readyEffectCount)),
                onClick = onOpenEffects,
            )

            MlSectionHeader(title = stringResource(R.string.pr06_how_header))
            HowToCard(
                modifier = Modifier.padding(horizontal = MotionSpacing.screenHorizontal),
            )
            Box(Modifier.height(MotionSpacing.s16))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s12,
                    bottom = MotionSpacing.s8,
                ),
        ) {
            MlButton(
                text = stringResource(R.string.pr06_start),
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showDurationSheet) {
        DurationSheet(
            selectedMinutes = setup.durationMinutes,
            onSave = { minutes ->
                onSaveDuration(minutes)
                showDurationSheet = false
            },
            onDismiss = { showDurationSheet = false },
        )
    }
}

/** 연결된 PC를 알려 주는 회색 카드. 이름, 연결 방식·응답 속도, "연결됨" 배지. */
@Composable
private fun ConnectedPcCard(connection: PcConnectionState.Connected, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MotionRadius.r16))
            .background(colors.bgGrouped)
            .padding(horizontal = MotionSpacing.s16, vertical = MotionSpacing.s14),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
    ) {
        MlIconTile(icon = MlIcon.Laptop, tone = MlTone.Blue)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
        ) {
            Text(text = connection.deviceName, style = type.body1Strong, color = colors.textPrimary, maxLines = 1)
            Text(
                text = stringResource(
                    if (connection.route == ConnectionRoute.Local) R.string.pr06_pc_subtitle_local else R.string.home_pc_connected_subtitle_server,
                    connection.latencyMs,
                ),
                style = type.caption1,
                color = colors.textSecondary,
            )
        }
        MlBadge(text = stringResource(R.string.home_badge_connected), tone = MlTone.Green)
    }
}

/** "이렇게 조작해요" 카드. 손가락을 댄 채로만 동작한다는 것과 휙·돌리기·두 번 톡톡(화면 가리기) 제스처를 짧게 알려 준다. */
@Composable
private fun HowToCard(modifier: Modifier = Modifier) {
    MlCard(
        modifier = modifier,
        tone = MlCardTone.Gray,
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
    ) {
        HowToRow(icon = MlIcon.Tap, text = stringResource(R.string.pr06_how_hold))
        HowToRow(icon = MlIcon.Swipe, text = stringResource(R.string.pr06_how_swipe))
        HowToRow(icon = MlIcon.Pointer, text = stringResource(R.string.pr06_how_pointer))
        HowToRow(icon = MlIcon.Tap, text = stringResource(R.string.pr06_how_blackout))
    }
}

@Composable
private fun HowToRow(icon: MlIcon, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
    ) {
        MlIconView(icon = icon, tint = MotionTheme.colors.textSecondary, size = MotionIconSize.s20)
        Text(
            text = text,
            style = MotionTheme.typography.body2,
            color = MotionTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * 발표 시간 시트(Figma PR-15). 5·10·15·20분 중 하나를 고르고 "저장"을 눌러야 바뀐다.
 * 시트를 그냥 닫으면 고른 것이 버려진다.
 */
@Composable
private fun DurationSheet(selectedMinutes: Int, onSave: (Int) -> Unit, onDismiss: () -> Unit) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    var picked by rememberSaveable { mutableIntStateOf(selectedMinutes) }

    MlBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s8,
                    bottom = MotionSpacing.s16,
                ),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
        ) {
            Text(text = stringResource(R.string.pr15_title), style = type.title3, color = colors.textPrimary)
            Text(text = stringResource(R.string.pr15_description), style = type.body2, color = colors.textSecondary)
        }
        MlChipRow(modifier = Modifier.padding(bottom = MotionSpacing.s16)) {
            DurationOptions.forEach { minutes ->
                MlChip(
                    text = stringResource(R.string.pr06_duration_value, minutes),
                    selected = minutes == picked,
                    onClick = { picked = minutes },
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s8),
        ) {
            MlButton(
                text = stringResource(R.string.pr15_save),
                onClick = { onSave(picked) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "PR-06 발표 준비", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr06Preview() = MotionLayerTheme {
    PrepareContent(
        connection = SampleData.classroomPc,
        setup = PresentationSetup(),
        readyEffectCount = 2,
        onBack = {},
        onSaveDuration = {},
        onChangeVibrate = {},
        onOpenEffects = {},
        onStart = {},
    )
}

@Preview(name = "PR-15 발표 시간 시트", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr15Preview() = MotionLayerTheme {
    PrepareContent(
        connection = SampleData.classroomPc,
        setup = PresentationSetup(),
        readyEffectCount = 2,
        onBack = {},
        onSaveDuration = {},
        onChangeVibrate = {},
        onOpenEffects = {},
        onStart = {},
        initiallyShowDurationSheet = true,
    )
}
