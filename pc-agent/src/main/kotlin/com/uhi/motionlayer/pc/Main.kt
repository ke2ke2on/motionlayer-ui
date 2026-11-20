package com.uhi.motionlayer.pc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.isTraySupported
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import com.uhi.motionlayer.pc.ui.PcStrings
import com.uhi.motionlayer.pc.ui.overlay.OverlayRoute
import com.uhi.motionlayer.pc.ui.renderAppMark
import com.uhi.motionlayer.pc.ui.settings.SettingsRoute
import com.uhi.motionlayer.pc.ui.settings.SettingsTab
import com.uhi.motionlayer.pc.ui.theme.PcSize
import com.uhi.motionlayer.pc.ui.tray.AgentTray
import com.uhi.motionlayer.pc.ui.tray.connectedNotification
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import kotlinx.coroutines.delay
import java.awt.GraphicsEnvironment

/** 시작하자마자 실행할 시연용 동작의 이름 일부(예: "PC-07"). `gradlew :pc-agent:run -Pdemo=PC-07`로 준다. */
private const val DEMO_PROPERTY = "motionlayer.demo"
private const val DEMO_START_DELAY_MILLIS = 1_500L

/**
 * 설정 창의 X를 누르면 프로그램을 끝낼지.
 * - `false`(기본값): 창만 닫히고 트레이에 남는다. Figma 노트("평소에는 창 없이 트레이에만 있다")대로이고,
 *   발표 중에 창을 닫아도 연결이 끊기지 않는다. 끝낼 때는 트레이 메뉴의 "MotionLayer 종료"를 쓴다.
 * - `true`: 창을 닫으면 프로그램이 끝난다. 화면만 빨리 확인하고 싶을 때 잠깐 바꿔 쓸 수 있다.
 */
private const val CLOSE_WINDOW_QUITS = false

/**
 * PC 에이전트의 시작점.
 *
 * - **트레이 아이콘과 메뉴(PC-04)**: 작업 표시줄 오른쪽 아래의 아이콘. QR·설정 창·오버레이 미리 보기를 열고 "MotionLayer 종료"로 끝낸다.
 * - **설정 창(PC-01~03)**: 제목 줄은 운영체제가 그리고, 창 크기는 내용(758 x 458)에 맞춰진다. 직접 실행하면 연 채로 시작한다.
 * - **발표 오버레이(PC-06~09)**: 그릴 것이 있을 때만 뜨는, 화면 전체 크기의 투명한 창. 항상 맨 위에 있고 키보드를 가져가지 않는다.
 * - **연결 알림(PC-05)**: 폰이 연결되면 운영체제 알림을 띄운다.
 */
fun main() = application {
    val container = PcAppContainer
    val appMark = remember { BitmapPainter(renderAppMark()) }
    val trayState = rememberTrayState()
    // 트레이가 없는 환경에서는 창을 닫으면 프로그램을 끝낸다(다시 열 방법이 없으므로).
    val traySupported = remember { isTraySupported }

    var settingsOpen by remember { mutableStateOf(true) }
    var settingsTab by remember { mutableStateOf(SettingsTab.PhoneLink) }
    // 트레이에서 창을 열 때마다 하나씩 올린다. 이미 열린 창이면 앞으로 가져오는 신호로 쓴다.
    var openRequests by remember { mutableIntStateOf(0) }
    fun openSettings(tab: SettingsTab? = null) {
        if (tab != null) settingsTab = tab
        settingsOpen = true
        openRequests += 1
    }

    val phone by container.phoneLink.phone.collectAsState()

    if (traySupported) {
        AgentTray(
            icon = appMark,
            state = trayState,
            connected = phone != null,
            onShowQr = { openSettings(SettingsTab.PhoneLink) },
            onOpenSettings = { openSettings() },
            onPreviewOverlay = container.overlay::preview,
            onQuit = ::exitApplication,
        )
    }

    if (settingsOpen) {
        // 크기를 정하지 않으면 창이 내용 크기에 맞춰진다.
        val windowState = rememberWindowState(
            width = Dp.Unspecified,
            height = Dp.Unspecified,
            position = WindowPosition(Alignment.Center),
        )
        Window(
            onCloseRequest = { if (traySupported && !CLOSE_WINDOW_QUITS) settingsOpen = false else exitApplication() },
            title = PcStrings.APP_NAME,
            icon = appMark,
            state = windowState,
            resizable = false,
        ) {
            LaunchedEffect(openRequests) {
                if (openRequests > 0) {
                    windowState.isMinimized = false
                    window.toFront()
                }
            }
            MotionLayerTheme {
                Box(Modifier.size(width = PcSize.contentWidth, height = PcSize.contentHeight)) {
                    SettingsRoute(
                        tab = settingsTab,
                        onTabSelected = { settingsTab = it },
                        phoneLink = container.phoneLink,
                        settings = container.settings,
                        demoActions = container.demoActions,
                    )
                }
            }
        }
    }

    val overlay by container.overlay.state.collectAsState()
    if (!overlay.isEmpty) {
        val screen = remember {
            GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.bounds
        }
        Window(
            onCloseRequest = {},
            title = PcStrings.OVERLAY_WINDOW,
            icon = appMark,
            state = rememberWindowState(
                position = WindowPosition(screen.x.dp, screen.y.dp),
                size = DpSize(screen.width.dp, screen.height.dp),
            ),
            undecorated = true,
            transparent = true,
            alwaysOnTop = true,
            focusable = false,
            resizable = false,
        ) {
            MotionLayerTheme {
                OverlayRoute(overlay = container.overlay, settings = container.settings)
            }
        }
    }

    // 연결 알림(PC-05): 폰이 새로 연결됐을 때 한 번. 응답 시간이 바뀔 때마다 다시 띄우지 않으려고 이름으로만 본다.
    LaunchedEffect(phone?.name) {
        phone?.let { trayState.sendNotification(connectedNotification(it)) }
    }

    // 시연용: 이름을 주면 창이 뜬 뒤 그 동작을 한 번 실행한다. 실제 구현에서는 동작 목록이 비어 아무 일도 없다.
    LaunchedEffect(Unit) {
        val wanted = System.getProperty(DEMO_PROPERTY) ?: return@LaunchedEffect
        delay(DEMO_START_DELAY_MILLIS)
        container.demoActions.firstOrNull { wanted in it.label }?.run?.invoke()
    }
}
