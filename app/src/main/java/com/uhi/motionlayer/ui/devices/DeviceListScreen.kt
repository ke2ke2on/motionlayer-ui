package com.uhi.motionlayer.ui.devices

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.ConnectionRoute
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.domain.model.DeviceKind
import com.uhi.motionlayer.domain.model.MyDevice
import com.uhi.motionlayer.domain.model.MyDeviceStatus
import com.uhi.motionlayer.domain.model.PairingTarget
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.RecentPc
import com.uhi.motionlayer.domain.model.TemporaryDevice
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.debug.DebugActionsDialog
import com.uhi.motionlayer.ui.presentation.PairingViewModel
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** 기기 종류에 맞는 아이콘. */
internal val DeviceKind.icon: MlIcon
    get() = when (this) {
        DeviceKind.Pc -> MlIcon.Laptop
        DeviceKind.Tv -> MlIcon.Tv
    }

/**
 * 설정에서 열리는 기기 목록(Figma DV-01). 위에는 계정에 등록한 "내 기기"(로그인했을 때만), 아래에는 "이번만 연결"
 * (지금 연결한 PC, 전에 연결했던 PC, 이번만 연결한 TV 등)이 있다. 내 기기를 누르면 자세한 설정(DV-04)이 열리고,
 * 전에 연결했던 PC를 누르면 다시 연결한다. "기기 추가"는 방법을 고르는 시트(DV-02)를 연다.
 * 찍은 기기의 주인이 없으면 이 화면 위에 어떻게 연결할지 묻는 시트(DV-03)가 뜬다.
 *
 * @param debugActions 제목을 길게 누르면(디버그 빌드) 열리는 시연용 동작
 */
@Composable
fun DeviceListRoute(
    pairing: PairingViewModel,
    deviceFlow: DeviceFlowViewModel,
    onOpenDevice: (MyDevice) -> Unit,
    onScanQr: () -> Unit,
    onEnterCode: () -> Unit,
    onFindNearby: () -> Unit,
    onAddOffline: () -> Unit,
    onSignIn: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    debugActions: List<DebugAction> = emptyList(),
    viewModel: DeviceListViewModel = viewModel(factory = DeviceListViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pending by deviceFlow.pending.collectAsStateWithLifecycle()
    val signedIn by deviceFlow.signedIn.collectAsStateWithLifecycle()
    val online by deviceFlow.online.collectAsStateWithLifecycle()
    val addSheetRequested by deviceFlow.addSheetRequested.collectAsStateWithLifecycle()
    var addSheetOpen by rememberSaveable { mutableStateOf(false) }

    // "인터넷 필요"(DV-09)에서 인터넷이 돌아와 이 화면으로 왔으면 기다리지 않고 기기 추가 방법 시트를 연다.
    LaunchedEffect(addSheetRequested) {
        if (addSheetRequested) {
            addSheetOpen = true
            deviceFlow.consumeAddSheetRequest()
        }
    }

    DeviceListContent(
        state = state,
        onOpenMyDevice = onOpenDevice,
        onConnectRecent = { pairing.connect(PairingTarget.Recent(it.id)) },
        onSignIn = onSignIn,
        // 새 기기 등록·원격 조작은 인터넷이 있어야 해서, 끊겨 있으면 방법을 고르게 하지 않고 안내(DV-09)를 보여 준다.
        onAddDevice = { if (online) addSheetOpen = true else onAddOffline() },
        onBack = onBack,
        modifier = modifier,
        debugActions = debugActions,
    )

    if (addSheetOpen) {
        AddDeviceSheet(
            onScanQr = {
                addSheetOpen = false
                deviceFlow.clearNearby()
                onScanQr()
            },
            onEnterCode = {
                addSheetOpen = false
                deviceFlow.clearNearby()
                onEnterCode()
            },
            onFindNearby = {
                addSheetOpen = false
                onFindNearby()
            },
            onDismiss = { addSheetOpen = false },
        )
    }

    pending?.let { device ->
        RegisterDeviceSheet(
            device = device,
            signedIn = signedIn,
            onRegister = { if (!deviceFlow.register()) onSignIn() },
            onConnectOnce = deviceFlow::connectOnce,
            onDismiss = deviceFlow::dismissPending,
        )
    }
}

/** 기기 목록. 상태를 받아 그리기만 한다. 제목을 길게 누르면(디버그 빌드) 시연용 동작 창이 열린다. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceListContent(
    state: DeviceListUiState,
    onOpenMyDevice: (MyDevice) -> Unit,
    onConnectRecent: (RecentPc) -> Unit,
    onSignIn: () -> Unit,
    onAddDevice: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    debugActions: List<DebugAction> = emptyList(),
) {
    var debugOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MotionTheme.colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(
            title = stringResource(R.string.dv01_title),
            leading = MlTopBarLeading.Back,
            onLeadingClick = onBack,
            modifier = if (debugActions.isNotEmpty()) {
                Modifier.combinedClickable(onClick = {}, onLongClick = { debugOpen = true })
            } else {
                Modifier
            },
        )

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            if (state.signedIn) {
                if (state.myDevices.isNotEmpty()) {
                    MlSectionHeader(title = stringResource(R.string.dv01_my_header, state.myDevices.size))
                    state.myDevices.forEach { device ->
                        MlListRow(
                            title = device.name,
                            subtitle = myDeviceSubtitle(device.status),
                            leading = MlListRowLeading.Icon(
                                icon = device.kind.icon,
                                tone = if (device.status is MyDeviceStatus.Connected) MlTone.Blue else MlTone.Gray,
                            ),
                            trailing = MlListRowTrailing.Chevron,
                            onClick = { onOpenMyDevice(device) },
                        )
                    }
                }
            } else {
                // 로그인하지 않으면 "내 기기"가 없다. Figma에 없는 모습이라, 로그인하면 쓸 수 있다고 알리는 줄을 직접 두었다.
                MlSectionHeader(title = stringResource(R.string.dv01_my_header_signed_out))
                MlListRow(
                    title = stringResource(R.string.dv01_login_title),
                    subtitle = stringResource(R.string.dv01_login_subtitle),
                    leading = MlListRowLeading.Icon(MlIcon.User),
                    trailing = MlListRowTrailing.Chevron,
                    onClick = onSignIn,
                )
            }

            if (state.temporaryCount > 0) {
                MlSectionHeader(title = stringResource(R.string.dv01_once_header, state.temporaryCount))
            }
            state.connectedPc?.let { connected ->
                // 누를 곳이 없는 줄이라 오른쪽 화살표는 없다.
                MlListRow(
                    title = connected.deviceName,
                    subtitle = connectedSubtitle(connected),
                    leading = MlListRowLeading.Icon(MlIcon.Laptop, MlTone.Blue),
                )
            }
            state.temporaryDevices.forEach { device -> TemporaryDeviceRow(device) }
            state.recentPcs.forEach { pc ->
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

            MlButton(
                text = stringResource(R.string.dv01_add),
                onClick = onAddDevice,
                style = MlButtonStyle.Weak,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, top = MotionSpacing.s16),
            )
            Spacer(Modifier.navigationBarsPadding())
        }
    }

    if (debugOpen) DebugActionsDialog(actions = debugActions, onDismiss = { debugOpen = false })
}

/** 이번만 연결한 TV 등. 누를 곳이 없어 오른쪽 화살표는 없다. */
@Composable
private fun TemporaryDeviceRow(device: TemporaryDevice) {
    MlListRow(
        title = device.name,
        subtitle = device.description,
        leading = MlListRowLeading.Icon(device.kind.icon, MlTone.Blue),
    )
}

@Composable
private fun myDeviceSubtitle(status: MyDeviceStatus): String = when (status) {
    is MyDeviceStatus.Connected -> stringResource(R.string.dv01_connected_local, status.latencyMs)
    MyDeviceStatus.Off -> stringResource(R.string.dv01_status_off)
    MyDeviceStatus.RemoteReachable -> stringResource(R.string.dv01_status_remote)
}

@Composable
private fun connectedSubtitle(connected: PcConnectionState.Connected): String = stringResource(
    when (connected.route) {
        ConnectionRoute.Local -> R.string.dv01_connected_local
        ConnectionRoute.Server -> R.string.dv01_connected_server
    },
    connected.latencyMs,
)

@Preview(name = "DV-01 기기 목록 · 로그인함", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Dv01Preview() = MotionLayerTheme {
    DeviceListContent(
        state = deviceListState(
            signedIn = true,
            myDevices = SampleData.myDevices,
            temporaryDevices = emptyList(),
            pc = SampleData.classroomPc,
            recentPcs = SampleData.recentPcs,
        ),
        onOpenMyDevice = {},
        onConnectRecent = {},
        onSignIn = {},
        onAddDevice = {},
        onBack = {},
    )
}

@Preview(name = "DV-01 기기 목록 · 로그인 안 함", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Dv01SignedOutPreview() = MotionLayerTheme {
    DeviceListContent(
        state = deviceListState(
            signedIn = false,
            myDevices = emptyList(),
            temporaryDevices = emptyList(),
            pc = PcConnectionState.Disconnected,
            recentPcs = SampleData.recentPcs,
        ),
        onOpenMyDevice = {},
        onConnectRecent = {},
        onSignIn = {},
        onAddDevice = {},
        onBack = {},
    )
}
