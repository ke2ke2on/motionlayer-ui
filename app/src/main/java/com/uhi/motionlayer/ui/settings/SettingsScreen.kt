package com.uhi.motionlayer.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.Account
import com.uhi.motionlayer.ui.account.signedInLabel
import com.uhi.motionlayer.ui.components.MlDividerThick
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.debug.isDebuggable
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 진동 세기(0~1)를 "약하게·보통·강하게" 세 단계 이름으로 바꾼다. 설정 목록의 오른쪽 값에 쓴다.
 * 경계(1/3, 2/3)는 Figma에 없어서 직접 정했다. Figma 슬라이더 값 61%가 "보통"이 되도록 맞췄다.
 */
@Composable
fun vibrationStrengthLabel(strength: Float): String = stringResource(
    when {
        strength < 1f / 3f -> R.string.vibration_strength_weak
        strength < 2f / 3f -> R.string.vibration_strength_normal
        else -> R.string.vibration_strength_strong
    },
)

/** 설정(Figma ST-01). 하단 탭이 없는 하위 화면이다. */
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenAccount: () -> Unit,
    onSignIn: () -> Unit,
    onOpenVibration: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDevices: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenBatteryGuide: () -> Unit,
    onOpenDeveloperTools: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val pauseWalking by viewModel.pauseGesturesWhileWalking.collectAsStateWithLifecycle()
    val pocket by viewModel.pocketGestureEnabled.collectAsStateWithLifecycle()
    val strength by viewModel.vibrationStrength.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val versionName = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
        .getOrNull().orEmpty()
    // 개발자 도구는 디버그 빌드에서만 보인다(정식 배포판에서는 숨겨져요).
    val showDeveloperTools = isDebuggable()

    SettingsContent(
        account = account,
        pauseGesturesWhileWalking = pauseWalking,
        pocketGestureEnabled = pocket,
        vibrationStrength = strength,
        versionName = versionName,
        showDeveloperTools = showDeveloperTools,
        onOpenAccount = onOpenAccount,
        onSignIn = onSignIn,
        onPauseGesturesWhileWalking = viewModel::setPauseGesturesWhileWalking,
        onPocketGesture = viewModel::setPocketGestureEnabled,
        onOpenVibration = onOpenVibration,
        onOpenHistory = onOpenHistory,
        onOpenDevices = onOpenDevices,
        onOpenPermissions = onOpenPermissions,
        onOpenBatteryGuide = onOpenBatteryGuide,
        onOpenDeveloperTools = onOpenDeveloperTools,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * 설정 목록. 상태를 받아 그리기만 한다.
 *
 * 맨 위 줄은 로그인한 계정(허윤 · Google 계정으로 로그인됨)이고 누르면 계정 화면(ST-05)이 열린다.
 * 로그인하지 않았을 때는 Figma에 시안이 없어 같은 자리에 "로그인" 줄을 직접 만들어 두었다.
 *
 * @param account 로그인한 계정. 로그인하지 않았으면 null
 * @param showDeveloperTools 개발자 도구 줄을 보일지. 디버그 빌드에서만 true
 */
@Composable
fun SettingsContent(
    account: Account?,
    pauseGesturesWhileWalking: Boolean,
    pocketGestureEnabled: Boolean,
    vibrationStrength: Float,
    versionName: String,
    showDeveloperTools: Boolean,
    onOpenAccount: () -> Unit,
    onSignIn: () -> Unit,
    onPauseGesturesWhileWalking: (Boolean) -> Unit,
    onPocketGesture: (Boolean) -> Unit,
    onOpenVibration: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDevices: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenBatteryGuide: () -> Unit,
    onOpenDeveloperTools: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MotionTheme.colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = stringResource(R.string.st01_title), leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            if (account != null) {
                MlListRow(
                    title = account.name,
                    subtitle = account.signedInLabel(),
                    leading = MlListRowLeading.Icon(MlIcon.User, MlTone.Blue),
                    trailing = MlListRowTrailing.Chevron,
                    onClick = onOpenAccount,
                )
            } else {
                MlListRow(
                    title = stringResource(R.string.st01_sign_in),
                    subtitle = stringResource(R.string.st01_sign_in_subtitle),
                    leading = MlListRowLeading.Icon(MlIcon.User),
                    trailing = MlListRowTrailing.Chevron,
                    onClick = onSignIn,
                )
            }
            MlDividerThick()

            MlSectionHeader(title = stringResource(R.string.st01_detection_header))
            MlListRow(
                title = stringResource(R.string.st01_pause_walking_title),
                subtitle = stringResource(R.string.st01_pause_walking_subtitle),
                trailing = MlListRowTrailing.Toggle(pauseGesturesWhileWalking, onPauseGesturesWhileWalking),
            )
            MlListRow(
                title = stringResource(R.string.st01_pocket_title),
                subtitle = stringResource(R.string.st01_pocket_subtitle),
                trailing = MlListRowTrailing.Toggle(pocketGestureEnabled, onPocketGesture),
            )
            MlListRow(
                title = stringResource(R.string.st01_vibration),
                leading = MlListRowLeading.Icon(MlIcon.Vibrate),
                trailing = MlListRowTrailing.Value(vibrationStrengthLabel(vibrationStrength)),
                onClick = onOpenVibration,
            )

            MlSectionHeader(title = stringResource(R.string.st01_manage_header))
            MlListRow(
                title = stringResource(R.string.st01_history),
                leading = MlListRowLeading.Icon(MlIcon.Chart),
                trailing = MlListRowTrailing.Chevron,
                onClick = onOpenHistory,
            )
            MlListRow(
                title = stringResource(R.string.st01_devices),
                leading = MlListRowLeading.Icon(MlIcon.Laptop),
                trailing = MlListRowTrailing.Chevron,
                onClick = onOpenDevices,
            )
            MlListRow(
                title = stringResource(R.string.st01_permissions),
                leading = MlListRowLeading.Icon(MlIcon.Shield),
                trailing = MlListRowTrailing.Chevron,
                onClick = onOpenPermissions,
            )
            MlListRow(
                title = stringResource(R.string.st01_battery_guide),
                leading = MlListRowLeading.Icon(MlIcon.Battery),
                trailing = MlListRowTrailing.Chevron,
                onClick = onOpenBatteryGuide,
            )

            MlSectionHeader(title = stringResource(R.string.st01_info_header))
            MlListRow(
                title = stringResource(R.string.st01_version),
                // 누를 곳이 없는 줄이라 Figma의 오른쪽 화살표는 두지 않는다.
                trailing = MlListRowTrailing.Value(versionName, chevron = false),
            )
            if (showDeveloperTools) {
                MlListRow(
                    title = stringResource(R.string.st01_developer_tools),
                    subtitle = stringResource(R.string.st01_developer_tools_subtitle),
                    leading = MlListRowLeading.Icon(MlIcon.Code),
                    trailing = MlListRowTrailing.Chevron,
                    onClick = onOpenDeveloperTools,
                )
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Preview(name = "ST-01 설정", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun St01Preview() = MotionLayerTheme {
    SettingsContent(
        account = SampleData.account,
        pauseGesturesWhileWalking = true,
        pocketGestureEnabled = true,
        vibrationStrength = 0.61f,
        versionName = "0.3.0",
        showDeveloperTools = true,
        onOpenAccount = {},
        onSignIn = {},
        onPauseGesturesWhileWalking = {},
        onPocketGesture = {},
        onOpenVibration = {},
        onOpenHistory = {},
        onOpenDevices = {},
        onOpenPermissions = {},
        onOpenBatteryGuide = {},
        onOpenDeveloperTools = {},
        onBack = {},
    )
}
