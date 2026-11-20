package com.uhi.motionlayer.pc.ui.tray

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.TrayState
import com.uhi.motionlayer.pc.domain.ConnectedPhone
import com.uhi.motionlayer.pc.ui.PcStrings

/**
 * 트레이 아이콘과 메뉴(PC-04). 평소에는 창 없이 여기에만 있고, QR과 설정 창을 여기서 연다.
 * 아이콘 자리와 메뉴 모양은 운영체제가 그린다. 여기서는 항목과 눌렀을 때 할 일만 정한다.
 *
 * 항목(시안 순서): 상태 줄(누를 수 없음) · QR 보기 · 설정 열기… · ― · 발표 오버레이 미리 보기 · ― · MotionLayer 종료
 *
 * @param onOpenSettings 메뉴의 "설정 열기…"와 아이콘을 직접 눌렀을 때.
 */
@Composable
fun ApplicationScope.AgentTray(
    icon: Painter,
    state: TrayState,
    connected: Boolean,
    onShowQr: () -> Unit,
    onOpenSettings: () -> Unit,
    onPreviewOverlay: () -> Unit,
    onQuit: () -> Unit,
) {
    Tray(
        icon = icon,
        state = state,
        tooltip = PcStrings.APP_NAME,
        onAction = onOpenSettings,
        menu = {
            Item(text = PcStrings.trayStatus(connected), enabled = false, onClick = {})
            Item(text = PcStrings.TRAY_SHOW_QR, onClick = onShowQr)
            Item(text = PcStrings.TRAY_OPEN_SETTINGS, onClick = onOpenSettings)
            Separator()
            Item(text = PcStrings.TRAY_PREVIEW_OVERLAY, onClick = onPreviewOverlay)
            Separator()
            Item(text = PcStrings.TRAY_QUIT, onClick = onQuit)
        },
    )
}

/** 폰이 연결됐을 때 띄울 알림(PC-05). 알림 모양은 운영체제가 그린다. */
fun connectedNotification(phone: ConnectedPhone): Notification = Notification(
    title = PcStrings.connectedTitle(phone.name),
    message = PcStrings.connectedDetail(phone),
)
