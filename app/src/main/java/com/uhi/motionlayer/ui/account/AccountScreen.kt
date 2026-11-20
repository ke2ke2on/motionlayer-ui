package com.uhi.motionlayer.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.Account
import com.uhi.motionlayer.domain.model.AccountProvider
import com.uhi.motionlayer.ui.components.MlDividerThick
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** 프로필 원과 그 안의 사람 아이콘 크기(Figma ST-05). */
private val AvatarSize = 72.dp
private val AvatarIconSize = MotionIconSize.s36

/** "Google 계정으로 로그인됨"처럼 어떤 서비스로 로그인했는지 알리는 한 줄. 설정(ST-01)과 계정(ST-05)이 함께 쓴다. */
@Composable
fun Account.signedInLabel(): String = stringResource(
    when (provider) {
        AccountProvider.Google -> R.string.account_signed_in_google
    },
)

/**
 * 계정(Figma ST-05). 설정 맨 위의 계정 줄에서 열린다. 줄을 누르면 저장된 것(나만의 제스처·내 기기·앱별 설정)이 있는 곳으로 가고,
 * "로그아웃"은 바로 로그아웃해 설정으로 돌아간다. Figma의 "계정 삭제"는 범위 밖이라 두지 않았다.
 */
@Composable
fun AccountRoute(
    onBack: () -> Unit,
    onOpenGestures: () -> Unit,
    onOpenDevices: () -> Unit,
    onOpenApps: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    state?.let {
        AccountContent(
            state = it,
            onOpenGestures = onOpenGestures,
            onOpenDevices = onOpenDevices,
            onOpenApps = onOpenApps,
            onSignOut = {
                viewModel.signOut()
                onSignedOut()
            },
            onBack = onBack,
            modifier = modifier,
        )
    }
}

/** 계정 화면. 값을 받아 그리기만 한다. */
@Composable
fun AccountContent(
    state: AccountUiState,
    onOpenGestures: () -> Unit,
    onOpenDevices: () -> Unit,
    onOpenApps: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MotionTheme.colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = stringResource(R.string.st05_title), leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Profile(account = state.account)
            MlDividerThick()

            MlSectionHeader(title = stringResource(R.string.st05_saved_header))
            MlListRow(
                title = stringResource(R.string.st05_gestures),
                leading = MlListRowLeading.Icon(MlIcon.Gesture),
                trailing = MlListRowTrailing.Value(stringResource(R.string.st05_gestures_value, state.customGestureCount)),
                onClick = onOpenGestures,
            )
            MlListRow(
                title = stringResource(R.string.st05_devices),
                leading = MlListRowLeading.Icon(MlIcon.Laptop),
                trailing = MlListRowTrailing.Value(stringResource(R.string.st05_devices_value, state.deviceCount)),
                onClick = onOpenDevices,
            )
            MlListRow(
                title = stringResource(R.string.st05_apps),
                leading = MlListRowLeading.Icon(MlIcon.Apps),
                trailing = MlListRowTrailing.Value(stringResource(R.string.st05_apps_value, state.appCount)),
                onClick = onOpenApps,
            )

            MlSectionHeader(title = stringResource(R.string.st05_manage_header))
            MlListRow(
                title = stringResource(R.string.st05_sign_out),
                subtitle = stringResource(R.string.st05_sign_out_subtitle),
                leading = MlListRowLeading.Icon(MlIcon.Logout),
                trailing = MlListRowTrailing.Chevron,
                onClick = onSignOut,
            )
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

/** 맨 위의 프로필: 파란 원 안의 사람 아이콘, 이름, 로그인한 서비스. */
@Composable
private fun Profile(account: Account) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MotionSpacing.screenHorizontal,
                end = MotionSpacing.screenHorizontal,
                top = MotionSpacing.s8,
                bottom = MotionSpacing.s20,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
    ) {
        Box(
            modifier = Modifier
                .size(AvatarSize)
                .clip(CircleShape)
                .background(colors.brandWeak),
            contentAlignment = Alignment.Center,
        ) {
            MlIconView(icon = MlIcon.User, tint = colors.brandPrimary, size = AvatarIconSize)
        }
        Text(text = account.name, style = type.title3, color = colors.textPrimary)
        Text(text = account.signedInLabel(), style = type.caption1, color = colors.textTertiary)
    }
}

@Preview(name = "ST-05 계정", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun St05Preview() = MotionLayerTheme {
    AccountContent(
        state = AccountUiState(account = SampleData.account, customGestureCount = 1, deviceCount = 3, appCount = 2),
        onOpenGestures = {},
        onOpenDevices = {},
        onOpenApps = {},
        onSignOut = {},
        onBack = {},
    )
}
