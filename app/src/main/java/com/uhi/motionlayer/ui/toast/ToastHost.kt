package com.uhi.motionlayer.ui.toast

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.AppEvent
import com.uhi.motionlayer.domain.model.GestureCommand
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlToast
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.KoreanParticle
import kotlinx.coroutines.delay

/** 토스트가 떠 있는 시간. 행동 글자("되돌리기")가 있는 토스트는 누를 시간을 더 준다(Figma에 시간이 없어 직접 정함). */
private const val TOAST_MILLIS = 3_000L
private const val ACTION_TOAST_MILLIS = 5_000L

/**
 * 앱 전체 위에 토스트를 띄우는 자리(Figma SY-03). 앱 화면이 켜져 있는 동안에만 일을 받아 보여 준다.
 * 놓는 위치와 가로 여백은 쓰는 곳에서 [modifier]로 정한다.
 */
@Composable
fun ToastHost(
    modifier: Modifier = Modifier,
    viewModel: ToastViewModel = viewModel(factory = ToastViewModel.Factory),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.collectEvents() }
    }
    val item by viewModel.current.collectAsStateWithLifecycle()
    ToastHostContent(item = item, onAction = viewModel::onAction, onTimeout = viewModel::dismiss, modifier = modifier)
}

/** 토스트 한 개를 아래에서 올라오며 보여 주고, 시간이 지나면 [onTimeout]으로 치우게 한다. 상태를 받아 그리기만 한다. */
@Composable
fun ToastHostContent(
    item: ToastItem?,
    onAction: (ToastItem) -> Unit,
    onTimeout: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(item?.id) {
        if (item != null) {
            delay(if (item.event.hasAction()) ACTION_TOAST_MILLIS else TOAST_MILLIS)
            onTimeout(item.id)
        }
    }

    AnimatedContent(
        targetState = item,
        modifier = modifier,
        transitionSpec = {
            (fadeIn() + slideInVertically { it / 2 }) togetherWith (fadeOut() + slideOutVertically { it / 2 })
        },
        contentKey = { it?.id },
        label = "toast",
    ) { shown ->
        if (shown != null) ToastBubble(item = shown, onAction = onAction)
    }
}

private fun AppEvent.hasAction(): Boolean = this is AppEvent.ControlTargetChanged

@Composable
private fun ToastBubble(item: ToastItem, onAction: (ToastItem) -> Unit, modifier: Modifier = Modifier) {
    val content = item.event.toastContent()
    MlToast(
        message = content.message,
        modifier = modifier.fillMaxWidth(),
        icon = content.icon,
        actionText = content.actionText,
        onActionClick = { onAction(item) },
    )
}

private class ToastContent(val message: String, val icon: MlIcon?, val actionText: String? = null)

/** 일마다 문구와 아이콘이 정해져 있다. 성공은 체크 아이콘, 실패·되돌릴 동작 없음·대상 변경은 아이콘 없이 문구만. */
@Composable
private fun AppEvent.toastContent(): ToastContent = when (this) {
    is AppEvent.CommandExecuted -> ToastContent(stringResource(command.executedRes), MlIcon.Check)
    AppEvent.CommandUndone -> ToastContent(stringResource(R.string.toast_undone), MlIcon.Check)
    AppEvent.NothingToUndo -> ToastContent(stringResource(R.string.toast_nothing_to_undo), icon = null)
    is AppEvent.CommandFailed -> ToastContent(
        message = if (reason == null) {
            stringResource(R.string.toast_failed)
        } else {
            stringResource(R.string.toast_failed_reason, reason)
        },
        icon = null,
    )
    is AppEvent.PcConnected -> ToastContent(
        stringResource(R.string.toast_pc_connected, deviceName, KoreanParticle.with(deviceName)),
        MlIcon.Check,
    )
    is AppEvent.ControlTargetChanged -> ToastContent(
        message = stringResource(R.string.toast_target_changed, targetName, KoreanParticle.objective(targetName)),
        icon = null,
        actionText = stringResource(R.string.toast_undo),
    )
}

private val GestureCommand.executedRes: Int
    get() = when (this) {
        GestureCommand.NextTrack -> R.string.toast_executed_next_track
        GestureCommand.PreviousTrack -> R.string.toast_executed_previous_track
        GestureCommand.PlayPause -> R.string.toast_executed_play_pause
        GestureCommand.Volume -> R.string.toast_executed_volume
        GestureCommand.SkipForward -> R.string.toast_executed_skip_forward
        GestureCommand.SkipBack -> R.string.toast_executed_skip_back
        GestureCommand.Undo -> R.string.toast_undone
        GestureCommand.NextSlide -> R.string.toast_executed_next_slide
        GestureCommand.PreviousSlide -> R.string.toast_executed_previous_slide
        GestureCommand.Pointer -> R.string.toast_executed_pointer
    }

@Preview(name = "SY-03 토스트 6가지", showBackground = true, widthDp = 360)
@Composable
private fun Sy03Preview() = MotionLayerTheme {
    val events = listOf(
        AppEvent.CommandExecuted(GestureCommand.NextTrack),
        AppEvent.CommandUndone,
        AppEvent.CommandFailed(reason = "거실 TV가 꺼져 있어"),
        AppEvent.PcConnected(deviceName = "강의실 PC"),
        AppEvent.ControlTargetChanged(targetName = "강의실 PC"),
        AppEvent.NothingToUndo,
    )
    Column(
        modifier = Modifier
            .background(MotionTheme.colors.bgGrouped)
            .padding(MotionSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s20),
    ) {
        events.forEachIndexed { index, event ->
            ToastBubble(item = ToastItem(id = index.toLong(), event = event), onAction = {})
        }
    }
}
