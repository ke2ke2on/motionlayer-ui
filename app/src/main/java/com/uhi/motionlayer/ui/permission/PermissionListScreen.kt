package com.uhi.motionlayer.ui.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.uhi.motionlayer.domain.model.AppPermission
import com.uhi.motionlayer.domain.model.PermissionStatus
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 설정에서 열리는 권한 목록(Figma ST-07). 줄을 누르면 아직 묻지 않은 권한은 요청하고, 그 밖에는 설정 앱의 그 화면을 연다.
 * 배터리 줄은 어떻게 바꾸는지 알려 주는 배터리 설정 안내(ON-07)로 간다.
 */
@Composable
fun PermissionListRoute(
    onOpenBatteryGuide: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PermissionListViewModel = viewModel(factory = PermissionListViewModel.Factory),
) {
    val statuses by viewModel.statuses.collectAsStateWithLifecycle()

    PermissionListContent(
        statuses = statuses,
        onOpen = { permission ->
            if (permission == AppPermission.BatteryOptimization) onOpenBatteryGuide() else viewModel.onOpen(permission)
        },
        onBack = onBack,
        modifier = modifier,
    )
}

/** 화면에 보여 주는 권한과 순서. Figma에 더 있는 근처 기기(DV)는 해당 기능이 이번 범위 밖이라 두지 않는다. */
private val shownPermissions = listOf(
    AppPermission.Notifications,
    AppPermission.NotificationAccess,
    AppPermission.BatteryOptimization,
    AppPermission.Camera,
    AppPermission.Phone,
)

/** 권한 목록. 상태를 받아 그리기만 한다. */
@Composable
fun PermissionListContent(
    statuses: Map<AppPermission, PermissionStatus>,
    onOpen: (AppPermission) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MotionTheme.colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = stringResource(R.string.st07_title), leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                text = stringResource(R.string.st07_description),
                style = MotionTheme.typography.body2,
                color = MotionTheme.colors.textSecondary,
                modifier = Modifier.padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s4,
                    bottom = MotionSpacing.s8,
                ),
            )
            shownPermissions.forEach { permission ->
                val status = statuses[permission] ?: PermissionStatus.NotRequested
                MlListRow(
                    title = stringResource(permission.titleRes),
                    subtitle = stringResource(permission.subtitleRes),
                    leading = MlListRowLeading.Icon(permission.icon, status.tone),
                    trailing = MlListRowTrailing.Value(stringResource(statusLabelRes(permission, status))),
                    onClick = { onOpen(permission) },
                )
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

private val AppPermission.titleRes: Int
    get() = when (this) {
        AppPermission.Notifications -> R.string.st07_notifications_title
        AppPermission.NotificationAccess -> R.string.st07_access_title
        AppPermission.BatteryOptimization -> R.string.st07_battery_title
        AppPermission.Camera -> R.string.st07_camera_title
        AppPermission.Phone -> R.string.st07_phone_title
    }

private val AppPermission.subtitleRes: Int
    get() = when (this) {
        AppPermission.Notifications -> R.string.st07_notifications_subtitle
        AppPermission.NotificationAccess -> R.string.st07_access_subtitle
        AppPermission.BatteryOptimization -> R.string.st07_battery_subtitle
        AppPermission.Camera -> R.string.st07_camera_subtitle
        AppPermission.Phone -> R.string.st07_phone_subtitle
    }

private val AppPermission.icon: MlIcon
    get() = when (this) {
        AppPermission.Notifications -> MlIcon.Bell
        AppPermission.NotificationAccess -> MlIcon.Apps
        AppPermission.BatteryOptimization -> MlIcon.Battery
        AppPermission.Camera -> MlIcon.Camera
        AppPermission.Phone -> MlIcon.PhoneCall
    }

/** 아이콘 타일 색: 허용은 초록, 허용하지 않음은 빨강, 아직 묻지 않음은 회색(Figma ST-07). */
private val PermissionStatus.tone: MlTone
    get() = when (this) {
        PermissionStatus.Granted -> MlTone.Green
        PermissionStatus.Denied -> MlTone.Red
        PermissionStatus.NotRequested -> MlTone.Gray
    }

/**
 * 오른쪽 값의 글자. 배터리는 허용·거부가 아니라 "적용됨"·"적용 안 됨"이라 하고(묻는 방식이 아니라서 요청 전도 안 됨으로 본다),
 * 나머지는 "허용됨"·"허용 안 됨"·"요청 전"이다. 배터리의 "적용 안 됨"은 Figma에 없어 직접 정했다.
 */
private fun statusLabelRes(permission: AppPermission, status: PermissionStatus): Int {
    val battery = permission == AppPermission.BatteryOptimization
    return when (status) {
        PermissionStatus.Granted -> if (battery) R.string.st07_status_applied else R.string.st07_status_granted
        PermissionStatus.Denied -> if (battery) R.string.st07_status_not_applied else R.string.st07_status_denied
        PermissionStatus.NotRequested -> if (battery) R.string.st07_status_not_applied else R.string.st07_status_not_requested
    }
}

@Preview(name = "ST-07 권한", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun St07Preview() = MotionLayerTheme {
    PermissionListContent(
        statuses = mapOf(
            AppPermission.Notifications to PermissionStatus.Granted,
            AppPermission.NotificationAccess to PermissionStatus.Granted,
            AppPermission.BatteryOptimization to PermissionStatus.Granted,
            AppPermission.Camera to PermissionStatus.NotRequested,
            AppPermission.Phone to PermissionStatus.Granted,
        ),
        onOpen = {},
        onBack = {},
    )
}

@Preview(name = "ST-07 권한 · 허용 안 됨", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun St07DeniedPreview() = MotionLayerTheme {
    PermissionListContent(
        statuses = mapOf(
            AppPermission.Notifications to PermissionStatus.Granted,
            AppPermission.NotificationAccess to PermissionStatus.Denied,
            AppPermission.BatteryOptimization to PermissionStatus.NotRequested,
            AppPermission.Camera to PermissionStatus.Denied,
            AppPermission.Phone to PermissionStatus.Denied,
        ),
        onOpen = {},
        onBack = {},
    )
}
