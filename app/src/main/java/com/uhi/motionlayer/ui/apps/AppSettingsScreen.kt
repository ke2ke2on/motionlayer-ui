package com.uhi.motionlayer.ui.apps

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.AppCategory
import com.uhi.motionlayer.domain.model.AppGestureSettings
import com.uhi.motionlayer.domain.model.ControllableApp
import com.uhi.motionlayer.domain.model.CustomGesture
import com.uhi.motionlayer.domain.model.GestureCommand
import com.uhi.motionlayer.domain.model.GestureType
import com.uhi.motionlayer.ui.components.MlAppIcon
import com.uhi.motionlayer.ui.components.MlBottomSheet
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlButtonSize
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlDividerThick
import com.uhi.motionlayer.ui.components.MlIcon
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
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** 앱별 동작 설정(Figma AP-02). 앱 목록(AP-01)의 앱 줄을 누르면 열린다. */
@Composable
fun AppSettingsRoute(
    appId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppSettingsViewModel = viewModel(key = appId, factory = AppSettingsViewModel.factory(appId)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    state.app?.let { app ->
        AppSettingsContent(
            app = app,
            commands = state.commands,
            customGestures = state.customGestures,
            customCommands = state.customCommands,
            optionsFor = viewModel::optionsFor,
            customOptions = viewModel.customOptions(),
            onBack = onBack,
            onToggleEnabled = viewModel::onToggleEnabled,
            onSaveCommand = viewModel::onSaveCommand,
            onSaveCustomCommand = viewModel::onSaveCustomCommand,
            onReset = viewModel::onReset,
            modifier = modifier,
        )
    }
}

/**
 * 동작 고르기 시트(AP-03)가 어느 제스처를 위한 것인지. 시트가 다시 만들어져도 남도록 문자열 열쇠([key])로 저장한다.
 */
private sealed interface SheetTarget {
    val key: String

    data class Basic(val gesture: GestureType) : SheetTarget {
        override val key = "basic:${gesture.name}"
    }

    data class Custom(val gesture: CustomGesture) : SheetTarget {
        override val key = "custom:${gesture.id}"
    }
}

/**
 * 앱 이름과 "이 앱에서 제스처 사용" 토글, 제스처마다 하는 동작(기본 5가지 + 내가 만든 제스처), 아래 "기본값으로 되돌리기".
 * 상태를 받아 그리기만 한다. 제스처 줄을 누르면 동작을 고르는 시트(AP-03)가 열린다.
 * 나만의 제스처는 이 앱에서 쓰지 않으면 "사용 안 함"으로 보인다(Figma AP-02의 "원 그리기" 줄).
 *
 * Figma에 더 있는 "주머니 두 번 두드림" 줄은 이번 범위 밖(FR-09)이라 두지 않는다.
 *
 * @param customGestures 내가 만든 제스처 전부
 * @param customCommands 나만의 제스처 id → 이 앱에서 하는 동작. 없으면 "사용 안 함"
 * @param customOptions 나만의 제스처에 고를 수 있는 동작
 * @param initialGestureSheet 처음부터 열어 둘 동작 고르기 시트의 기본 제스처(미리보기용)
 */
@Composable
fun AppSettingsContent(
    app: ControllableApp,
    commands: Map<GestureType, GestureCommand?>,
    customGestures: List<CustomGesture>,
    customCommands: Map<String, GestureCommand>,
    optionsFor: (GestureType) -> List<GestureCommand>,
    customOptions: List<GestureCommand>,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSaveCommand: (GestureType, GestureCommand?) -> Unit,
    onSaveCustomCommand: (gestureId: String, GestureCommand?) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    initialGestureSheet: GestureType? = null,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    var sheetKey by rememberSaveable { mutableStateOf(initialGestureSheet?.let { SheetTarget.Basic(it).key }) }
    // 열쇠로 저장해 둔 시트를 지금 목록에서 찾는다. 그 사이 제스처가 지워졌으면 시트는 열지 않는다.
    val sheetTarget: SheetTarget? = sheetKey?.let { key ->
        AppGestureSettings.Gestures.map { SheetTarget.Basic(it) }.firstOrNull { it.key == key }
            ?: customGestures.map { SheetTarget.Custom(it) }.firstOrNull { it.key == key }
    }

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
                MlAppIcon(
                    letter = app.iconLetter,
                    color = app.accentColorArgb?.let { Color(it) } ?: colors.brandPrimary,
                    large = true,
                )
                Column(verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2)) {
                    Text(text = app.name, style = type.title3, color = colors.textPrimary)
                    Text(
                        text = stringResource(R.string.ap02_subtitle, stringResource(app.category.labelRes)),
                        style = type.caption1,
                        color = colors.textTertiary,
                    )
                }
            }

            MlListRow(
                title = stringResource(R.string.ap02_use_title),
                trailing = MlListRowTrailing.Toggle(checked = app.enabled, onCheckedChange = onToggleEnabled),
            )
            MlDividerThick()

            MlSectionHeader(title = stringResource(R.string.ap02_gestures_header))
            AppGestureSettings.Gestures.forEach { gesture ->
                val command = commands[gesture]
                MlListRow(
                    title = stringResource(gesture.nameRes),
                    leading = MlListRowLeading.Icon(gesture.icon, gesture.tone),
                    trailing = MlListRowTrailing.Value(
                        text = stringResource(command?.labelRes(app.category) ?: R.string.app_command_none),
                    ),
                    onClick = { sheetKey = SheetTarget.Basic(gesture).key },
                )
            }
            customGestures.forEach { gesture ->
                val command = customCommands[gesture.id]
                MlListRow(
                    title = gesture.name,
                    leading = MlListRowLeading.Icon(MlIcon.Gesture, MlTone.Gray),
                    trailing = MlListRowTrailing.Value(
                        text = stringResource(command?.labelRes(app.category) ?: R.string.app_command_none),
                    ),
                    onClick = { sheetKey = SheetTarget.Custom(gesture).key },
                )
            }
            Box(Modifier.height(MotionSpacing.s16))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s4,
                    bottom = MotionSpacing.s8,
                ),
        ) {
            MlButton(
                text = stringResource(R.string.ap02_reset),
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                style = MlButtonStyle.Ghost,
                size = MlButtonSize.M,
            )
        }
    }

    when (sheetTarget) {
        is SheetTarget.Basic -> CommandSheet(
            app = app,
            title = stringResource(sheetTarget.gesture.nameRes),
            options = optionsFor(sheetTarget.gesture),
            selected = commands[sheetTarget.gesture],
            onSave = { command ->
                onSaveCommand(sheetTarget.gesture, command)
                sheetKey = null
            },
            onDismiss = { sheetKey = null },
        )
        is SheetTarget.Custom -> CommandSheet(
            app = app,
            title = sheetTarget.gesture.name,
            options = customOptions,
            selected = customCommands[sheetTarget.gesture.id],
            onSave = { command ->
                onSaveCustomCommand(sheetTarget.gesture.id, command)
                sheetKey = null
            },
            onDismiss = { sheetKey = null },
        )
        null -> Unit
    }
}

/**
 * 동작 고르기 시트(Figma AP-03). 고른 것은 "저장"을 눌러야 바뀌고, 그냥 닫으면 버려진다.
 * 고를 수 있는 동작 아래에 "사용 안 함"을 붙인다.
 */
@Composable
private fun CommandSheet(
    app: ControllableApp,
    title: String,
    options: List<GestureCommand>,
    selected: GestureCommand?,
    onSave: (GestureCommand?) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    // 시트가 다시 만들어져도 고른 값이 남도록 이름으로 저장한다. 이름이 null이면 "사용 안 함"이다.
    var pickedName by rememberSaveable { mutableStateOf(selected?.name) }
    val picked = options.firstOrNull { it.name == pickedName }

    MlBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s8,
                    bottom = MotionSpacing.s8,
                ),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
        ) {
            Text(text = title, style = type.title3, color = colors.textPrimary)
            Text(
                text = stringResource(R.string.ap03_description, app.name),
                style = type.body2,
                color = colors.textSecondary,
            )
        }

        options.forEach { command ->
            OptionRow(
                label = stringResource(command.labelRes(app.category)),
                selected = picked == command,
                onClick = { pickedName = command.name },
            )
        }
        OptionRow(
            label = stringResource(R.string.app_command_none),
            selected = picked == null,
            onClick = { pickedName = null },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s8),
        ) {
            MlButton(
                text = stringResource(R.string.ap03_save),
                onClick = { onSave(picked) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** 시트의 한 줄. 고른 줄은 파란 굵은 글씨에 체크가 붙는다. */
@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s14),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
    ) {
        Text(
            text = label,
            style = if (selected) type.body1Strong else type.body1,
            color = if (selected) colors.brandPrimary else colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (selected) MlIconView(icon = MlIcon.Check, tint = colors.brandPrimary, size = MotionIconSize.s22)
    }
}

private val previewYoutube = SampleData.apps.first { it.id == "youtube" }.copy(enabled = true)

private val previewCommands = mapOf(
    GestureType.SwipeRight to GestureCommand.SkipForward,
    GestureType.SwipeLeft to GestureCommand.SkipBack,
    GestureType.DoubleTap to GestureCommand.PlayPause,
    GestureType.Tilt to GestureCommand.Volume,
    GestureType.Shake to GestureCommand.Undo,
)

private val previewOptions = listOf(
    GestureCommand.NextTrack,
    GestureCommand.SkipForward,
    GestureCommand.PreviousTrack,
    GestureCommand.SkipBack,
    GestureCommand.PlayPause,
)

@Preview(name = "AP-02 앱별 동작 설정", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Ap02Preview() = MotionLayerTheme {
    AppSettingsContent(
        app = previewYoutube,
        commands = previewCommands,
        customGestures = listOf(SampleData.customCircle),
        customCommands = emptyMap(),
        optionsFor = { previewOptions },
        customOptions = previewOptions,
        onBack = {},
        onToggleEnabled = {},
        onSaveCommand = { _, _ -> },
        onSaveCustomCommand = { _, _ -> },
        onReset = {},
    )
}

@Preview(name = "AP-03 동작 고르기 시트", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Ap03Preview() = MotionLayerTheme {
    AppSettingsContent(
        app = previewYoutube,
        commands = previewCommands,
        customGestures = listOf(SampleData.customCircle),
        customCommands = emptyMap(),
        optionsFor = { previewOptions },
        customOptions = previewOptions,
        onBack = {},
        onToggleEnabled = {},
        onSaveCommand = { _, _ -> },
        onSaveCustomCommand = { _, _ -> },
        onReset = {},
        initialGestureSheet = GestureType.SwipeRight,
    )
}

@Preview(name = "AP-02 음악 앱", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Ap02MusicPreview() = MotionLayerTheme {
    AppSettingsContent(
        app = SampleData.apps.first { it.category == AppCategory.Music },
        commands = previewCommands + (GestureType.SwipeRight to GestureCommand.NextTrack) + (GestureType.Shake to null),
        customGestures = listOf(SampleData.customCircle),
        customCommands = mapOf(SampleData.customCircle.id to GestureCommand.NextTrack),
        optionsFor = { previewOptions },
        customOptions = previewOptions,
        onBack = {},
        onToggleEnabled = {},
        onSaveCommand = { _, _ -> },
        onSaveCustomCommand = { _, _ -> },
        onReset = {},
    )
}
