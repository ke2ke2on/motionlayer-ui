package com.uhi.motionlayer.ui.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.ConnectedPerson
import com.uhi.motionlayer.domain.model.ConnectedPersonRole
import com.uhi.motionlayer.domain.model.DeviceKind
import com.uhi.motionlayer.domain.model.MyDeviceDetail
import com.uhi.motionlayer.domain.model.PersonPlace
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

/**
 * 내 기기 상세(Figma DV-04). 이름 아래에 "Android TV · 내 기기", 연결 허용 토글 셋(손님 · 동시 조작 · 집 밖), 연결된 사람 목록
 * ("조작 권한 되찾기", 손님 줄의 "끊기" → 확인 창 DV-05), 기타(진동 · 방향 등록 값), 아래에 "내 기기에서 삭제"가 있다.
 * 기기를 지우면(또는 없는 기기면) 목록으로 돌아간다.
 */
@Composable
fun DeviceDetailRoute(
    deviceId: String,
    onBack: () -> Unit,
    onGone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeviceDetailViewModel = viewModel(key = deviceId, factory = DeviceDetailViewModel.factory(deviceId)),
) {
    val detail by viewModel.detail.collectAsStateWithLifecycle()

    // 기기를 지우면 값이 사라지므로, 그때(처음부터 없는 기기일 때도) 목록으로 돌아간다.
    LaunchedEffect(detail == null) {
        if (detail == null) onGone()
    }

    detail?.let {
        DeviceDetailContent(
            detail = it,
            onAllowGuests = viewModel::setAllowGuests,
            onAllowSimultaneous = viewModel::setAllowSimultaneous,
            onAllowRemote = viewModel::setAllowRemote,
            onReclaimControl = viewModel::reclaimControl,
            onDisconnectPerson = viewModel::disconnectPerson,
            onRemove = viewModel::remove,
            onBack = onBack,
            modifier = modifier,
        )
    }
}

/** 기기 상세. 값을 받아 그리고, 끊기·삭제 확인 창을 띄우는 일만 한다. */
@Composable
fun DeviceDetailContent(
    detail: MyDeviceDetail,
    onAllowGuests: (Boolean) -> Unit,
    onAllowSimultaneous: (Boolean) -> Unit,
    onAllowRemote: (Boolean) -> Unit,
    onReclaimControl: () -> Unit,
    onDisconnectPerson: (String) -> Unit,
    onRemove: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val device = detail.device
    // 어느 사람의 연결을 끊을지 확인 중이면 그 사람의 id. 화면을 돌려도 남게 id만 저장한다.
    var disconnectingId by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmRemove by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = null, leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MotionSpacing.screenHorizontal,
                        end = MotionSpacing.screenHorizontal,
                        top = MotionSpacing.s4,
                        bottom = MotionSpacing.s8,
                    ),
                horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MlIconTile(icon = device.kind.icon, tone = MlTone.Blue)
                Column(verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2)) {
                    Text(text = device.name, style = type.title3, color = colors.textPrimary)
                    Text(
                        text = stringResource(R.string.dv04_subtitle, device.platform),
                        style = type.caption1,
                        color = colors.textTertiary,
                    )
                }
            }

            MlSectionHeader(title = stringResource(R.string.dv04_allow_header))
            MlListRow(
                title = stringResource(R.string.dv04_guests_title),
                subtitle = stringResource(
                    if (device.kind == DeviceKind.Tv) R.string.dv04_guests_subtitle_tv else R.string.dv04_guests_subtitle_pc,
                ),
                trailing = MlListRowTrailing.Toggle(detail.allowGuests, onAllowGuests),
            )
            MlListRow(
                title = stringResource(R.string.dv04_simultaneous_title),
                subtitle = stringResource(R.string.dv04_simultaneous_subtitle),
                trailing = MlListRowTrailing.Toggle(detail.allowSimultaneous, onAllowSimultaneous),
            )
            MlListRow(
                title = stringResource(R.string.dv04_remote_title),
                subtitle = stringResource(R.string.dv04_remote_subtitle),
                trailing = MlListRowTrailing.Toggle(detail.allowRemote, onAllowRemote),
            )

            MlSectionHeader(
                title = stringResource(R.string.dv04_people_header, detail.connectedPeople.size),
                actionText = stringResource(R.string.dv04_reclaim),
                onActionClick = onReclaimControl,
            )
            if (detail.connectedPeople.isEmpty()) {
                Text(
                    text = stringResource(R.string.dv04_no_people),
                    style = type.body2,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(
                        start = MotionSpacing.screenHorizontal,
                        end = MotionSpacing.screenHorizontal,
                        top = MotionSpacing.s4,
                        bottom = MotionSpacing.s12,
                    ),
                )
            }
            detail.connectedPeople.forEach { person ->
                val isMe = person.role == ConnectedPersonRole.Me
                MlListRow(
                    title = person.name,
                    subtitle = personSubtitle(person),
                    leading = MlListRowLeading.Icon(MlIcon.User, if (isMe) MlTone.Blue else MlTone.Gray),
                    // 나를 끊을 수는 없다. 손님 줄만 "끊기"가 있다.
                    trailing = if (isMe) null else MlListRowTrailing.Value(stringResource(R.string.dv04_disconnect)),
                    onClick = if (isMe) null else ({ disconnectingId = person.id }),
                )
            }

            MlSectionHeader(title = stringResource(R.string.dv04_other_header))
            // 진동 종류를 고르는 화면과 방향을 등록하는 화면은 Figma에 없어서, 눌러도 갈 곳이 없는 값만 보여 준다(화살표 없음).
            MlListRow(
                title = stringResource(R.string.dv04_vibration),
                leading = MlListRowLeading.Icon(MlIcon.Vibrate),
                trailing = MlListRowTrailing.Value(detail.selectVibrationLabel, chevron = false),
            )
            MlListRow(
                title = stringResource(R.string.dv04_directions),
                leading = MlListRowLeading.Icon(MlIcon.Compass),
                trailing = MlListRowTrailing.Value(
                    stringResource(R.string.dv04_directions_value, detail.registeredDirections),
                    chevron = false,
                ),
            )

            MlDestructiveRow(text = stringResource(R.string.dv04_remove), onClick = { confirmRemove = true })
            Spacer(Modifier.navigationBarsPadding())
        }
    }

    // DV-05: 상대에게 영향이 가는 동작(손님 폰에 연결이 끊겼다고 알려요)이라 한 번 확인한다.
    detail.connectedPeople.firstOrNull { it.id == disconnectingId }?.let { person ->
        MlDialog(
            title = stringResource(R.string.dv05_title, person.name),
            message = stringResource(R.string.dv05_message, person.name),
            dismissText = stringResource(R.string.dv05_cancel),
            onDismiss = { disconnectingId = null },
            confirmText = stringResource(R.string.dv05_confirm),
            onConfirm = {
                disconnectingId = null
                onDisconnectPerson(person.id)
            },
            confirmStyle = MlButtonStyle.Danger,
        )
    }

    // 삭제 확인(Figma에 없어 직접 둔 창): 내 기기에서 지우면 연결 허용과 방향 설정이 사라진다.
    if (confirmRemove) {
        MlDialog(
            title = stringResource(R.string.dv04_remove_title, device.name, KoreanParticle.objective(device.name)),
            message = stringResource(R.string.dv04_remove_message),
            dismissText = stringResource(R.string.dv04_remove_cancel),
            onDismiss = { confirmRemove = false },
            confirmText = stringResource(R.string.dv04_remove_confirm),
            onConfirm = {
                confirmRemove = false
                onRemove()
            },
            confirmStyle = MlButtonStyle.Danger,
        )
    }
}

/** "이 폰 · 조작 중", "손님 · 같은 와이파이"처럼 사람 줄 아래에 붙는 설명. */
@Composable
private fun personSubtitle(person: ConnectedPerson): String {
    val place = stringResource(
        when (person.place) {
            PersonPlace.ThisPhone -> R.string.dv04_place_this_phone
            PersonPlace.SameWifi -> R.string.dv04_place_same_wifi
            PersonPlace.Remote -> R.string.dv04_place_remote
        },
    )
    val parts = buildList {
        if (person.role == ConnectedPersonRole.Guest) add(stringResource(R.string.dv04_role_guest))
        add(place)
        if (person.controlling) add(stringResource(R.string.dv04_controlling))
    }
    return parts.joinToString(" · ")
}

@Preview(name = "DV-04 기기 상세 (거실 TV)", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Dv04Preview() = MotionLayerTheme {
    DeviceDetailContent(
        detail = SampleData.deviceDetails.getValue("living_tv"),
        onAllowGuests = {},
        onAllowSimultaneous = {},
        onAllowRemote = {},
        onReclaimControl = {},
        onDisconnectPerson = {},
        onRemove = {},
        onBack = {},
    )
}
