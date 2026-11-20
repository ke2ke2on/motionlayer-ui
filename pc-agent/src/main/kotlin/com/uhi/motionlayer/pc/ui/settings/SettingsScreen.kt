package com.uhi.motionlayer.pc.ui.settings

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.uhi.motionlayer.pc.domain.AgentSettings
import com.uhi.motionlayer.pc.domain.ConnectedPhone
import com.uhi.motionlayer.pc.domain.ConnectionPreferences
import com.uhi.motionlayer.pc.domain.DemoAction
import com.uhi.motionlayer.pc.domain.PairingCode
import com.uhi.motionlayer.pc.domain.PhoneLink
import com.uhi.motionlayer.pc.domain.PresentationPreferences
import com.uhi.motionlayer.pc.ui.PcStrings
import com.uhi.motionlayer.pc.ui.theme.PcSize
import com.uhi.motionlayer.pc.ui.theme.PcSpacing
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.Text
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 설정 창의 왼쪽 메뉴. Figma에는 "일반"·"정보"도 있지만 그 화면의 시안이 없어 메뉴에 두지 않는다
 * (눌러도 갈 곳이 없는 버튼을 만들지 않는다).
 */
enum class SettingsTab(val label: String, val icon: MlIcon) {
    PhoneLink(PcStrings.TAB_PHONE_LINK, MlIcon.Smartphone),
    Presentation(PcStrings.TAB_PRESENTATION, MlIcon.Presentation),
}

/**
 * 설정 창(PC-01~03). 값을 구독해 [SettingsContent]에 넘긴다.
 * 어느 메뉴를 보고 있는지([tab])는 밖에서 들고 있다: 트레이의 "QR 보기"가 "폰 연결"을 골라서 창을 열기 때문이다.
 */
@Composable
fun SettingsRoute(
    tab: SettingsTab,
    onTabSelected: (SettingsTab) -> Unit,
    phoneLink: PhoneLink,
    settings: AgentSettings,
    demoActions: List<DemoAction> = emptyList(),
) {
    val pairing by phoneLink.pairing.collectAsState()
    val phone by phoneLink.phone.collectAsState()
    val connection by settings.connection.collectAsState()
    val presentation by settings.presentation.collectAsState()

    SettingsContent(
        tab = tab,
        onTabSelected = onTabSelected,
        pairing = pairing,
        phone = phone,
        connection = connection,
        presentation = presentation,
        onDisconnect = phoneLink::disconnect,
        onLaunchAtLoginChange = settings::setLaunchAtLogin,
        onAutoConnectChange = settings::setAutoConnectMyPhone,
        onAllowOtherPhonesChange = settings::setAllowOtherPhones,
        onShowOperatorNameChange = settings::setShowOperatorName,
        demoActions = demoActions,
    )
}

/**
 * 설정 창의 내용(제목 줄 아래 758 x 458). 제목 줄은 운영체제가 그린다.
 * 값을 받아 그리기만 한다.
 */
@Composable
fun SettingsContent(
    tab: SettingsTab,
    onTabSelected: (SettingsTab) -> Unit,
    pairing: PairingCode,
    phone: ConnectedPhone?,
    connection: ConnectionPreferences,
    presentation: PresentationPreferences,
    onDisconnect: () -> Unit,
    onLaunchAtLoginChange: (Boolean) -> Unit,
    onAutoConnectChange: (Boolean) -> Unit,
    onAllowOtherPhonesChange: (Boolean) -> Unit,
    onShowOperatorNameChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    demoActions: List<DemoAction> = emptyList(),
) {
    val colors = MotionTheme.colors
    Row(modifier.fillMaxSize().background(colors.bgBase)) {
        Sidebar(selected = tab, onSelected = onTabSelected, demoActions = demoActions)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PcSpacing.s32, vertical = MotionSpacing.s28),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s16),
        ) {
            when (tab) {
                SettingsTab.PhoneLink ->
                    if (phone == null) {
                        PhoneLinkWaiting(
                            pairing = pairing,
                            connection = connection,
                            onLaunchAtLoginChange = onLaunchAtLoginChange,
                            onAutoConnectChange = onAutoConnectChange,
                        )
                    } else {
                        PhoneLinkConnected(
                            phone = phone,
                            connection = connection,
                            onDisconnect = onDisconnect,
                            onAllowOtherPhonesChange = onAllowOtherPhonesChange,
                            onShowOperatorNameChange = onShowOperatorNameChange,
                        )
                    }

                SettingsTab.Presentation -> PresentationTab(presentation)
            }
        }
    }
}

@Composable
private fun Sidebar(
    selected: SettingsTab,
    onSelected: (SettingsTab) -> Unit,
    demoActions: List<DemoAction>,
) {
    // 시연용 동작이 있으면(가짜 구현일 때) 왼쪽 메뉴를 마우스 오른쪽 버튼으로 눌러 고른다.
    ContextMenuArea(items = { demoActions.map { action -> ContextMenuItem(action.label, action.run) } }) {
        Column(
            modifier = Modifier
                .width(PcSize.sidebarWidth)
                .fillMaxHeight()
                .background(MotionTheme.colors.bgSubtle)
                .padding(horizontal = MotionSpacing.s12, vertical = MotionSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s4),
        ) {
            SettingsTab.entries.forEach { tab ->
                SidebarItem(tab = tab, selected = tab == selected, onClick = { onSelected(tab) })
            }
        }
    }
}

@Composable
private fun SidebarItem(tab: SettingsTab, selected: Boolean, onClick: () -> Unit) {
    val colors = MotionTheme.colors
    val tint = if (selected) colors.brandPrimary else colors.textSecondary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MotionRadius.r10))
            .background(if (selected) colors.brandWeak else Color.Transparent)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = MotionSpacing.s12, vertical = MotionSpacing.s10),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MlIconView(icon = tab.icon, tint = tint, size = MotionIconSize.s18)
        Text(
            text = tab.label,
            style = if (selected) MotionTheme.typography.body2Strong else MotionTheme.typography.body2,
            color = tint,
        )
    }
}
