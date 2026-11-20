package com.uhi.motionlayer.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.uhi.motionlayer.tv.R
import com.uhi.motionlayer.tv.domain.DemoAction
import com.uhi.motionlayer.tv.domain.EffectKind
import com.uhi.motionlayer.tv.domain.OperatorNotice
import com.uhi.motionlayer.tv.domain.TvCommand
import com.uhi.motionlayer.tv.domain.TvEffect
import com.uhi.motionlayer.tv.domain.TvOverlayState
import com.uhi.motionlayer.tv.ui.theme.TvIconSize
import com.uhi.motionlayer.tv.ui.theme.TvShadows
import com.uhi.motionlayer.tv.ui.theme.TvSize
import com.uhi.motionlayer.tv.ui.theme.TvSpacing
import com.uhi.motionlayer.tv.ui.theme.TvType
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.Text
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.theme.motionShadow
import com.uhi.motionlayer.ui.util.KoreanParticle

/**
 * 재생 중인 화면 위에 겹쳐 그리는 것(TV-04~06). 바탕은 투명하고 받은 값만 그린다.
 *
 * - TV-04 조작한 사람: 오른쪽 위 이름표 "민수 ▸ 다음 채널"
 * - TV-05 원격 조작 중: 위 가운데 파란 띠 "허윤 님이 밖에서 조작하고 있어요"
 * - TV-06 효과: 오른쪽 아래 작은 창
 *
 * @param effectMedia 효과 창 안에 실제 사진·영상을 그리는 자리. 재생은 C가 맡고, 비워 두면 자리 표시 그림이 나온다.
 */
@Composable
fun TvOverlayLayer(
    state: TvOverlayState,
    modifier: Modifier = Modifier,
    effectMedia: (@Composable () -> Unit)? = null,
) {
    Box(modifier.fillMaxSize()) {
        state.remoteOperator?.let { name ->
            RemoteBanner(name = name, modifier = Modifier.align(Alignment.TopCenter).padding(top = TvSpacing.overlayTop))
        }
        state.operator?.let { notice ->
            OperatorToast(
                notice = notice,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = TvSpacing.overlayTop, end = TvSpacing.operatorEnd),
            )
        }
        state.effect?.let { effect ->
            EffectPip(
                effect = effect,
                media = effectMedia,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = TvSpacing.pipEnd, bottom = TvSpacing.pipBottom),
            )
        }
    }
}

/** TV-04: 방금 누가 무엇을 했는지. */
@Composable
private fun OperatorToast(notice: OperatorNotice, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val shape = RoundedCornerShape(MotionRadius.r28)
    val command = stringResource(
        when (notice.command) {
            TvCommand.NextChannel -> R.string.tv_command_next_channel
            TvCommand.PreviousChannel -> R.string.tv_command_previous_channel
            TvCommand.VolumeUp -> R.string.tv_command_volume_up
            TvCommand.VolumeDown -> R.string.tv_command_volume_down
            TvCommand.InputSwitch -> R.string.tv_command_input_switch
        },
    )
    Row(
        modifier = modifier
            .motionShadow(TvShadows.floating, shape)
            .background(colors.bgBase, shape)
            .padding(horizontal = MotionSpacing.s24, vertical = MotionSpacing.s14),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(TvSize.operatorMark).background(colors.brandWeak, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            MlIconView(icon = MlIcon.User, tint = colors.brandPrimary, size = MotionIconSize.s20)
        }
        Text(
            text = stringResource(R.string.tv04_operator, notice.name, command),
            style = TvType.bodyStrong,
            color = colors.textPrimary,
        )
    }
}

/** TV-05: 집 밖에서 조작하는 동안 계속 보이는 띠. */
@Composable
private fun RemoteBanner(name: String, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val shape = RoundedCornerShape(MotionRadius.r28)
    Row(
        modifier = modifier
            .motionShadow(TvShadows.floating, shape)
            .background(colors.brandWeak, shape)
            .padding(horizontal = MotionSpacing.s28, vertical = MotionSpacing.s16),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MlIconView(icon = MlIcon.Cloud, tint = colors.brandPrimary, size = TvIconSize.s26)
        Text(text = stringResource(R.string.tv05_remote, name), style = TvType.bodyStrong, color = colors.brandPrimary)
    }
}

/** TV-06: 폰에서 보낸 사진·영상의 작은 창. */
@Composable
private fun EffectPip(effect: TvEffect, media: (@Composable () -> Unit)?, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val shape = RoundedCornerShape(MotionRadius.r24)
    val sentBy = stringResource(
        when (effect.kind) {
            EffectKind.Photo -> R.string.tv06_sent_photo
            EffectKind.Video -> R.string.tv06_sent_video
        },
        effect.senderName,
        KoreanParticle.subject(effect.senderName),
    )
    Column(
        modifier = modifier
            .motionShadow(TvShadows.floating, shape)
            .background(colors.bgBase, shape)
            .padding(MotionSpacing.s12),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s10),
    ) {
        Box(
            modifier = Modifier
                .size(width = TvSize.pipMediaWidth, height = TvSize.pipMediaHeight)
                .clip(RoundedCornerShape(MotionRadius.r16))
                .background(colors.bgGrouped),
            contentAlignment = Alignment.Center,
        ) {
            if (media != null) media() else MlIconView(icon = MlIcon.Image, tint = colors.textDisabled, size = TvIconSize.s56)
        }
        Row(
            modifier = Modifier.width(TvSize.pipMediaWidth).padding(horizontal = MotionSpacing.s6),
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = sentBy,
                style = TvType.captionStrong,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.tv06_closes_in, effect.secondsLeft),
                style = TvType.small,
                color = colors.textTertiary,
            )
        }
    }
}

/**
 * 시연용 동작 목록(디버그 빌드에서 리모컨 메뉴 키). 방향키로 옮기고 확인으로 실행하면 목록이 닫힌다.
 * 시안에 없는 개발용 화면이라 모양은 직접 정했다.
 */
@Composable
fun TvDemoMenu(actions: List<DemoAction>, onDismiss: () -> Unit) {
    val colors = MotionTheme.colors
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestFocus() }
    BackHandler(onBack = onDismiss)

    Box(modifier = Modifier.fillMaxSize().background(colors.overlayDim), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .width(TvSize.demoMenuWidth)
                .background(colors.bgBase, RoundedCornerShape(MotionRadius.r28))
                .padding(MotionSpacing.s24),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s4),
        ) {
            Text(
                text = stringResource(R.string.tv_demo_title),
                style = TvType.bodyStrong,
                color = colors.textTertiary,
                modifier = Modifier.padding(horizontal = MotionSpacing.s16, vertical = MotionSpacing.s8),
            )
            actions.forEachIndexed { index, action ->
                DemoMenuItem(
                    label = action.label,
                    onClick = {
                        action.run()
                        onDismiss()
                    },
                    modifier = if (index == 0) Modifier.focusRequester(first) else Modifier,
                )
            }
        }
    }
}

@Composable
private fun DemoMenuItem(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    Text(
        text = label,
        style = TvType.detail,
        color = if (focused) colors.brandPrimary else colors.textPrimary,
        modifier = modifier
            .fillMaxWidth()
            .background(if (focused) colors.brandWeak else Color.Transparent, RoundedCornerShape(MotionRadius.r16))
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = MotionSpacing.s16, vertical = MotionSpacing.s10),
    )
}
