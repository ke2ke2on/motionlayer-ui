package com.uhi.motionlayer.ui.apps

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.GestureType
import com.uhi.motionlayer.domain.model.PermissionStatus
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlDividerThick
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

/** 전화 제어(Figma AP-04). 앱 목록(AP-01)의 "전화" 줄을 누르면 열린다. */
@Composable
fun PhoneControlRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhoneControlViewModel = viewModel(factory = PhoneControlViewModel.Factory),
) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val permission by viewModel.permission.collectAsStateWithLifecycle()
    PhoneControlContent(
        enabled = enabled,
        permission = permission,
        onToggle = viewModel::onToggle,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * 전화 제어 사용 스위치와 "전화가 올 때"·"통화 중"에 제스처가 하는 일, 아래 안내. 상태를 받아 그리기만 한다.
 * 어떤 제스처가 무엇을 하는지는 정해져 있어서(Figma에 동작을 고르는 시트가 없다) 줄에는 오른쪽 화살표가 없고 눌러도 아무 일이 없다.
 * 전화 권한은 스위치를 켤 때 요청한다.
 *
 * @param permission 전화 권한의 지금 상태. 스위치 아래 설명이 이에 따라 바뀐다
 */
@Composable
fun PhoneControlContent(
    enabled: Boolean,
    permission: PermissionStatus,
    onToggle: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = null, leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MotionSpacing.screenHorizontal,
                        end = MotionSpacing.screenHorizontal,
                        top = MotionSpacing.s4,
                        bottom = MotionSpacing.s12,
                    ),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
            ) {
                Text(text = stringResource(R.string.ap04_title), style = type.title2, color = colors.textPrimary)
                Text(text = stringResource(R.string.ap04_description), style = type.body2, color = colors.textSecondary)
            }

            MlListRow(
                title = stringResource(R.string.ap04_use_title),
                subtitle = stringResource(permission.subtitleRes),
                trailing = MlListRowTrailing.Toggle(checked = enabled, onCheckedChange = onToggle),
            )
            MlDividerThick()

            MlSectionHeader(title = stringResource(R.string.ap04_incoming_header))
            PhoneGestureRow(GestureType.SwipeRight, R.string.ap04_action_answer)
            PhoneGestureRow(GestureType.Shake, R.string.ap04_action_reject)
            PhoneGestureRow(GestureType.DoubleTap, R.string.ap04_action_silence)

            MlSectionHeader(title = stringResource(R.string.ap04_in_call_header))
            PhoneGestureRow(GestureType.DoubleTap, R.string.ap04_action_hang_up)

            Box(
                modifier = Modifier.padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s12,
                ),
            ) {
                MlBanner(
                    message = stringResource(R.string.ap04_banner),
                    tone = MlBannerTone.Neutral,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(Modifier.height(MotionSpacing.s16))
        }
    }
}

/** 제스처 이름과 그 제스처가 하는 일(화살표 없음). */
@Composable
private fun PhoneGestureRow(gesture: GestureType, @StringRes actionRes: Int) {
    MlListRow(
        title = stringResource(gesture.nameRes),
        leading = MlListRowLeading.Icon(gesture.icon, MlTone.Blue),
        trailing = MlListRowTrailing.Value(text = stringResource(actionRes), chevron = false),
    )
}

/** 스위치 아래 설명. 허용돼 있으면 Figma 문구이고, 아직 묻지 않았거나 허용하지 않았을 때의 문구는 직접 정했다. */
@get:StringRes
private val PermissionStatus.subtitleRes: Int
    get() = when (this) {
        PermissionStatus.Granted -> R.string.ap04_permission_granted
        PermissionStatus.NotRequested -> R.string.ap04_permission_not_requested
        PermissionStatus.Denied -> R.string.ap04_permission_denied
    }

@Preview(name = "AP-04 전화 제어", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Ap04Preview() = MotionLayerTheme {
    PhoneControlContent(enabled = true, permission = PermissionStatus.Granted, onToggle = {}, onBack = {})
}

@Preview(name = "AP-04 전화 제어 · 꺼짐", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Ap04OffPreview() = MotionLayerTheme {
    PhoneControlContent(enabled = false, permission = PermissionStatus.NotRequested, onToggle = {}, onBack = {})
}
