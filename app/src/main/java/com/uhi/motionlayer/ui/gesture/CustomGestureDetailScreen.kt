package com.uhi.motionlayer.ui.gesture

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.CustomGesture
import com.uhi.motionlayer.domain.model.CustomGestureUse
import com.uhi.motionlayer.domain.model.GestureCommand
import com.uhi.motionlayer.ui.apps.labelRes
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlDialog
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconTile
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.KoreanParticle
import com.uhi.motionlayer.ui.components.MlDestructiveRow

/** 감도 값 줄에 보여 줄 말의 경계. 1/3 미만은 "둔감하게", 2/3 미만은 "보통", 그 이상은 "민감하게". */
private const val SENSITIVITY_LOW_BELOW = 1f / 3f
private const val SENSITIVITY_HIGH_FROM = 2f / 3f

/**
 * 나만의 제스처 상세(Figma GS-11)와 삭제 확인(GS-12). 제스처 목록(GS-01)의 나만의 제스처 줄을 누르면 열린다.
 *
 * @param onDeleted 제스처가 지워져 이 화면이 더 이상 그릴 것이 없을 때(삭제를 확인한 뒤)
 * @param onOpenApp "쓰는 곳"의 앱 줄을 눌러 그 앱의 동작 설정(AP-02)을 연다
 * @param onRerecord "다시 녹화하기"
 */
@Composable
fun CustomGestureDetailRoute(
    gestureId: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onOpenApp: (appId: String) -> Unit,
    onRerecord: (CustomGesture) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomGestureDetailViewModel = viewModel(
        key = "custom-$gestureId",
        factory = CustomGestureDetailViewModel.factory(gestureId),
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gesture = state.gesture

    // 지워졌으면(삭제 확인 뒤) 화면을 닫는다.
    LaunchedEffect(gesture == null) { if (gesture == null) onDeleted() }
    if (gesture == null) {
        Box(modifier.fillMaxSize().background(MotionTheme.colors.bgBase))
        return
    }

    CustomGestureDetailContent(
        gesture = gesture,
        uses = state.uses,
        onBack = onBack,
        onOpenApp = onOpenApp,
        onRerecord = { onRerecord(gesture) },
        onSensitivityChange = viewModel::onSensitivityChange,
        onDelete = viewModel::onDelete,
        modifier = modifier,
    )
}

/**
 * 이름·만든 날, 최근 7일 인식률·되돌림, 쓰는 곳, 다시 녹화하기·감도, 맨 아래 "제스처 삭제". 상태를 받아 그리기만 한다.
 * "감도"를 누르면 슬라이더 시트가, "제스처 삭제"를 누르면 확인 창이 뜬다(둘 다 이 화면이 연 채로 들고 있다).
 *
 * @param uses 이 제스처를 쓰는 앱과 동작. 쓰는 앱이 없으면 빈 목록
 */
@Composable
fun CustomGestureDetailContent(
    gesture: CustomGesture,
    uses: List<CustomGestureUse>,
    onBack: () -> Unit,
    onOpenApp: (appId: String) -> Unit,
    onRerecord: () -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    var sensitivitySheetOpen by remember { mutableStateOf(false) }
    var deleteDialogOpen by remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState()),
        ) {
            // 계정에 아직 올라가지 않았으면(GS-15) 기능은 막지 않고 어디까지 저장됐는지만 배너와 배지로 알린다.
            if (gesture.syncPending) {
                MlBanner(
                    message = stringResource(R.string.gs15_banner),
                    tone = MlBannerTone.Neutral,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MotionSpacing.screenHorizontal,
                            end = MotionSpacing.screenHorizontal,
                            top = MotionSpacing.s8,
                            bottom = MotionSpacing.s8,
                        ),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MotionSpacing.screenHorizontal,
                        end = MotionSpacing.screenHorizontal,
                        top = MotionSpacing.s4,
                        bottom = MotionSpacing.s16,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
            ) {
                MlIconTile(icon = MlIcon.Gesture, tone = MlTone.Blue)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
                ) {
                    Text(text = gesture.name, style = type.title3, color = colors.textPrimary)
                    Text(
                        text = stringResource(
                            if (gesture.syncPending) R.string.gs15_created_pending else R.string.gs11_created,
                            gesture.createdOn.monthValue,
                            gesture.createdOn.dayOfMonth,
                        ),
                        style = type.caption1,
                        color = colors.textTertiary,
                    )
                }
                if (gesture.syncPending) {
                    MlBadge(text = stringResource(R.string.gs15_badge), tone = MlTone.Orange)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, bottom = MotionSpacing.s8),
                horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
            ) {
                StatCard(
                    label = stringResource(R.string.gs02_stat_rate),
                    value = stringResource(R.string.gesture_success_rate, gesture.successRatePercent),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.gs02_stat_undone),
                    value = stringResource(R.string.gs02_stat_count, gesture.undoneCount),
                    modifier = Modifier.weight(1f),
                )
            }

            MlSectionHeader(title = stringResource(R.string.gs11_uses_header))
            if (uses.isEmpty()) {
                // 쓰는 앱이 없다는 안내는 Figma에 없어 직접 정한 문구다.
                Text(
                    text = stringResource(R.string.gs11_uses_empty),
                    style = type.body2,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s4),
                )
            } else {
                uses.forEach { use ->
                    MlListRow(
                        title = use.app.name,
                        leading = MlListRowLeading.App(letter = use.app.iconLetter, color = use.app.accentColorArgb?.let { Color(it) }),
                        trailing = MlListRowTrailing.Value(text = stringResource(use.command.labelRes(use.app.category))),
                        onClick = { onOpenApp(use.app.id) },
                    )
                }
            }

            MlSectionHeader(title = stringResource(R.string.gs11_manage_header))
            MlListRow(
                title = stringResource(R.string.gs11_rerecord),
                leading = MlListRowLeading.Icon(MlIcon.Refresh, MlTone.Gray),
                trailing = MlListRowTrailing.Chevron,
                onClick = onRerecord,
            )
            MlListRow(
                title = stringResource(R.string.gs11_sensitivity),
                leading = MlListRowLeading.Icon(MlIcon.Tilt, MlTone.Gray),
                trailing = MlListRowTrailing.Value(text = stringResource(sensitivityLabelRes(gesture.sensitivity))),
                onClick = { sensitivitySheetOpen = true },
            )
            MlDestructiveRow(text = stringResource(R.string.gs11_delete), onClick = { deleteDialogOpen = true })
            Box(Modifier.height(MotionSpacing.s16))
        }
    }

    if (sensitivitySheetOpen) {
        SensitivitySheet(
            title = stringResource(R.string.gs11_sheet_title),
            message = stringResource(R.string.gs11_sheet_message),
            sensitivity = gesture.sensitivity,
            confirmText = stringResource(R.string.gs11_sheet_save),
            onConfirm = { value ->
                onSensitivityChange(value)
                sensitivitySheetOpen = false
            },
            onDismiss = { sensitivitySheetOpen = false },
        )
    }

    if (deleteDialogOpen) {
        MlDialog(
            title = stringResource(R.string.gs12_title, gesture.name, KoreanParticle.objective(gesture.name)),
            message = deleteMessage(uses),
            dismissText = stringResource(R.string.gs12_cancel),
            onDismiss = { deleteDialogOpen = false },
            confirmText = stringResource(R.string.gs12_delete),
            onConfirm = {
                deleteDialogOpen = false
                onDelete()
            },
            confirmStyle = MlButtonStyle.Danger,
        )
    }
}

/**
 * 삭제 확인 창의 설명. 한 앱에서 쓰면 그 앱과 동작을 말하고(Figma), 여러 앱이면 "○○ 외 N곳"으로 줄이고,
 * 쓰는 앱이 없으면 다시 쓸 수 없다고만 알린다(뒤의 둘은 Figma에 없어 직접 정한 문구).
 */
@Composable
private fun deleteMessage(uses: List<CustomGestureUse>): String = when {
    uses.isEmpty() -> stringResource(R.string.gs12_message_none)
    uses.size == 1 -> {
        val use = uses.first()
        val command = stringResource(use.command.labelRes(use.app.category))
        stringResource(R.string.gs12_message_one, use.app.name, command, KoreanParticle.subject(command))
    }
    else -> stringResource(R.string.gs12_message_many, uses.first().app.name, uses.size - 1)
}

@StringRes
private fun sensitivityLabelRes(sensitivity: Float): Int = when {
    sensitivity < SENSITIVITY_LOW_BELOW -> R.string.gs02_dull
    sensitivity < SENSITIVITY_HIGH_FROM -> R.string.gs11_sensitivity_normal
    else -> R.string.gs02_sensitive
}

@Preview(name = "GS-11 나만의 제스처 상세", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs11Preview() = MotionLayerTheme {
    CustomGestureDetailContent(
        gesture = SampleData.customCircle,
        uses = listOf(CustomGestureUse(SampleData.apps.first { it.id == "youtube_music" }, GestureCommand.NextTrack)),
        onBack = {},
        onOpenApp = {},
        onRerecord = {},
        onSensitivityChange = {},
        onDelete = {},
    )
}

@Preview(name = "GS-15 나만의 제스처 상세 · 동기화 대기", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs15Preview() = MotionLayerTheme {
    CustomGestureDetailContent(
        gesture = SampleData.customCircle.copy(syncPending = true),
        uses = listOf(CustomGestureUse(SampleData.apps.first { it.id == "youtube_music" }, GestureCommand.NextTrack)),
        onBack = {},
        onOpenApp = {},
        onRerecord = {},
        onSensitivityChange = {},
        onDelete = {},
    )
}

@Preview(name = "GS-11 쓰는 앱 없음", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs11NoUsesPreview() = MotionLayerTheme {
    CustomGestureDetailContent(
        gesture = SampleData.customCircle,
        uses = emptyList(),
        onBack = {},
        onOpenApp = {},
        onRerecord = {},
        onSensitivityChange = {},
        onDelete = {},
    )
}
