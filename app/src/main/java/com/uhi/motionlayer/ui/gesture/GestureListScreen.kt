package com.uhi.motionlayer.ui.gesture

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.FakeGestureCatalog
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.BasicGesture
import com.uhi.motionlayer.domain.model.CUSTOM_RECORDING_TAKES
import com.uhi.motionlayer.domain.model.CustomGestureUse
import com.uhi.motionlayer.domain.model.GestureCommand
import com.uhi.motionlayer.ui.apps.labelRes
import com.uhi.motionlayer.ui.components.MlBottomTab
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlButtonSize
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconTile
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.components.MlSkeleton
import com.uhi.motionlayer.ui.components.MlTab
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** 제스처 탭 첫 화면(Figma GS-01). 하단 탭은 앱 틀에서 붙인다. */
@Composable
fun GestureListRoute(
    onOpenPractice: () -> Unit,
    onOpenGesture: (BasicGesture) -> Unit,
    onCreateGesture: () -> Unit,
    onOpenCustomGesture: (gestureId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GestureListViewModel = viewModel(factory = GestureListViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GestureListContent(
        state = state,
        onOpenPractice = onOpenPractice,
        onOpenGesture = onOpenGesture,
        onCreateGesture = onCreateGesture,
        onOpenCustomGesture = onOpenCustomGesture,
        modifier = modifier,
    )
}

/**
 * 기본 제스처 4가지, 연습 모드 입구, 나만의 제스처. 상태를 받아 그리기만 한다.
 * 기본 제스처 줄을 누르면 상세(GS-02), 나만의 제스처 줄을 누르면 상세(GS-11), "+ 만들기"는 만들기(GS-03)가 열린다.
 *
 * Figma에 있는 "주머니 제스처"(FR-09)는 Should라 이번에는 만들지 않는다.
 */
@Composable
fun GestureListContent(
    state: GestureListUiState,
    onOpenPractice: () -> Unit,
    onOpenGesture: (BasicGesture) -> Unit,
    onCreateGesture: () -> Unit,
    onOpenCustomGesture: (gestureId: String) -> Unit,
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
            Text(text = stringResource(R.string.gs01_title), style = type.title2, color = colors.textPrimary)
            Text(text = stringResource(R.string.gs01_description), style = type.body2, color = colors.textSecondary)
        }

        // 최근 7일 인식률을 모으는 동안(GS-16)은 제목만 두고 연습 카드·목록 자리에 자리 표시 모양을 보여 준다.
        if (state.loading) {
            GestureListLoadingBody()
            return@Column
        }

        PracticeCard(
            learnedCount = state.learnedCount,
            onClick = onOpenPractice,
            modifier = Modifier.padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, bottom = MotionSpacing.s4),
        )

        MlSectionHeader(title = stringResource(R.string.gs01_basic_header))
        state.gestures.forEach { summary ->
            MlListRow(
                title = stringResource(summary.gesture.nameRes),
                subtitle = stringResource(summary.gesture.doesRes),
                leading = MlListRowLeading.Icon(summary.gesture.icon, MlTone.Blue),
                trailing = MlListRowTrailing.Value(
                    text = stringResource(R.string.gesture_success_rate, summary.successRatePercent),
                ),
                onClick = { onOpenGesture(summary.gesture) },
            )
        }

        MlSectionHeader(
            title = stringResource(R.string.gs01_custom_header),
            actionText = stringResource(R.string.gs01_custom_create),
            onActionClick = onCreateGesture,
        )
        if (state.customGestures.isEmpty()) {
            EmptyCustomGestures(onCreate = onCreateGesture)
        }
        state.customGestures.forEach { row ->
            MlListRow(
                title = row.gesture.name,
                subtitle = row.subtitle(),
                leading = MlListRowLeading.Icon(MlIcon.Gesture, MlTone.Gray),
                trailing = MlListRowTrailing.Value(
                    text = stringResource(R.string.gesture_success_rate, row.gesture.successRatePercent),
                ),
                onClick = { onOpenCustomGesture(row.gesture.id) },
            )
        }
        Box(Modifier.padding(bottom = MotionSpacing.s16))
    }
}

/**
 * 아직 만든 제스처가 없을 때의 안내(Figma GS-13). "없음"을 공백으로 두지 않고, 만들 수 있다는 것과 드는 수고(5번 반복)를
 * 알려 주며 바로 만들 버튼을 둔다. 기본 제스처 4가지는 항상 있어서 목록 전체가 비는 일은 없다.
 */
@Composable
private fun EmptyCustomGestures(onCreate: () -> Unit) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

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
        Text(text = stringResource(R.string.gs01_custom_empty), style = type.body1Strong, color = colors.textPrimary)
        Text(
            text = stringResource(R.string.gs13_empty_description, CUSTOM_RECORDING_TAKES),
            style = type.caption1,
            color = colors.textTertiary,
        )
        MlButton(
            text = stringResource(R.string.gs13_create),
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth(),
            style = MlButtonStyle.Weak,
            size = MlButtonSize.M,
        )
    }
}

/**
 * 최근 7일 인식률을 모으는 동안의 목록 자리(Figma GS-16): 안내 한 줄, 연습 카드 자리, 줄 자리 다섯 개.
 * 줄마다 이름 막대의 길이만 다르다.
 */
@Composable
private fun GestureListLoadingBody() {
    Text(
        text = stringResource(R.string.gs16_caption),
        style = MotionTheme.typography.caption1,
        color = MotionTheme.colors.textTertiary,
        modifier = Modifier
            .fillMaxWidth()
            // 계산하는 중이라는 말을 스크린리더가 바로 읽어 준다(보고서 6.4).
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(
                start = MotionSpacing.screenHorizontal,
                end = MotionSpacing.screenHorizontal,
                top = MotionSpacing.s8,
                bottom = MotionSpacing.s4,
            ),
    )
    MlSkeleton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s8)
            .height(73.dp),
        shape = RoundedCornerShape(MotionRadius.r16),
    )
    listOf(110.dp, 150.dp, 110.dp, 150.dp, 110.dp).forEach { nameWidth -> SkeletonGestureRow(nameWidth = nameWidth) }
}

/** 제스처 줄 자리 표시: 네모난 아이콘, 이름·설명 막대 두 개, 오른쪽 값 막대. 높이 68(위아래 14). */
@Composable
private fun SkeletonGestureRow(nameWidth: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s14),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
    ) {
        MlSkeleton(modifier = Modifier.size(MotionSize.iconTile), shape = RoundedCornerShape(MotionRadius.r12))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
        ) {
            MlSkeleton(modifier = Modifier.size(width = nameWidth, height = 14.dp))
            MlSkeleton(modifier = Modifier.size(width = 80.dp, height = 12.dp))
        }
        MlSkeleton(modifier = Modifier.size(width = 36.dp, height = 14.dp))
    }
}

/**
 * 나만의 제스처 줄의 설명: 한 앱에서만 쓰면 "앱 이름 · 동작", 여러 앱이면 "앱 이름 외 N곳"(직접 정함),
 * 쓰는 앱이 없으면 그 사실(직접 정함).
 */
@Composable
private fun CustomGestureRow.subtitle(): String {
    return when (uses.size) {
        0 -> stringResource(R.string.gs01_custom_subtitle_none)
        1 -> {
            val use = uses.first()
            stringResource(
                R.string.gs01_custom_subtitle_one,
                use.app.name,
                stringResource(use.command.labelRes(use.app.category)),
            )
        }
        else -> stringResource(R.string.gs01_custom_subtitle_many, uses.first().app.name, uses.size - 1)
    }
}

/** 연습 모드로 가는 연한 파란 카드. "4가지 중 N가지를 익혔어요". */
@Composable
private fun PracticeCard(learnedCount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MotionRadius.r16))
            .background(colors.brandWeak)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = MotionSpacing.s16, vertical = MotionSpacing.s14),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
    ) {
        MlIconTile(icon = MlIcon.Tap, tone = MlTone.Blue)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2),
        ) {
            Text(text = stringResource(R.string.gs01_practice_title), style = type.body1Strong, color = colors.textPrimary)
            Text(
                text = stringResource(R.string.gs01_practice_progress, BasicGesture.entries.size, learnedCount),
                style = type.caption1,
                color = colors.textSecondary,
            )
        }
        MlIconView(icon = MlIcon.ChevronRight, tint = colors.brandPrimary, size = MotionIconSize.s20)
    }
}

@Preview(name = "GS-13 제스처 목록 · 나만의 제스처 없음", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs13Preview() = MotionLayerTheme {
    Column(Modifier.fillMaxSize()) {
        GestureListContent(
            state = GestureListUiState(
                gestures = FakeGestureCatalog().basicGestures.value,
                learnedCount = 3,
            ),
            onOpenPractice = {},
            onOpenGesture = {},
            onCreateGesture = {},
            onOpenCustomGesture = {},
            modifier = Modifier.weight(1f),
        )
        MlBottomTab(selected = MlTab.Gesture, onSelect = {})
    }
}

@Preview(name = "GS-16 제스처 목록 · 불러오는 중", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs16Preview() = MotionLayerTheme {
    Column(Modifier.fillMaxSize()) {
        GestureListContent(
            state = GestureListUiState(
                gestures = FakeGestureCatalog().basicGestures.value,
                learnedCount = 3,
                loading = true,
            ),
            onOpenPractice = {},
            onOpenGesture = {},
            onCreateGesture = {},
            onOpenCustomGesture = {},
            modifier = Modifier.weight(1f),
        )
        MlBottomTab(selected = MlTab.Gesture, onSelect = {})
    }
}

@Preview(name = "GS-01 제스처 목록", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Gs01Preview() = MotionLayerTheme {
    Column(Modifier.fillMaxSize()) {
        GestureListContent(
            state = GestureListUiState(
                gestures = FakeGestureCatalog().basicGestures.value,
                learnedCount = 3,
                customGestures = listOf(
                    CustomGestureRow(
                        gesture = SampleData.customCircle,
                        uses = listOf(CustomGestureUse(SampleData.apps.first { it.id == "youtube_music" }, GestureCommand.NextTrack)),
                    ),
                ),
            ),
            onOpenPractice = {},
            onOpenGesture = {},
            onCreateGesture = {},
            onOpenCustomGesture = {},
            modifier = Modifier.weight(1f),
        )
        MlBottomTab(selected = MlTab.Gesture, onSelect = {})
    }
}
