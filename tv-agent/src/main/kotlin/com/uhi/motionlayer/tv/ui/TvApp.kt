package com.uhi.motionlayer.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uhi.motionlayer.tv.domain.ConnectedPerson
import com.uhi.motionlayer.tv.domain.DemoAction
import com.uhi.motionlayer.tv.domain.TvConnections
import com.uhi.motionlayer.tv.domain.TvKind
import com.uhi.motionlayer.tv.domain.TvOverlay
import com.uhi.motionlayer.tv.domain.TvPairing
import com.uhi.motionlayer.tv.domain.TvSetup
import com.uhi.motionlayer.tv.ui.theme.TvSize
import com.uhi.motionlayer.ui.theme.MotionTheme
import kotlin.math.min

/** 지금 보여 줄 화면. */
enum class TvScreen { Setup, Pairing, Connections }

/**
 * 어느 화면을 보여 줄지: 성격을 아직 안 골랐으면 TV-01, 연결된 사람이 없으면 TV-02(QR 대기), 있으면 TV-03(연결 상태).
 */
fun tvScreenFor(kind: TvKind?, people: List<ConnectedPerson>): TvScreen = when {
    kind == null -> TvScreen.Setup
    people.isEmpty() -> TvScreen.Pairing
    else -> TvScreen.Connections
}

/**
 * TV 앱 전체. 값을 구독해 화면 하나를 고르고, 그 위에 오버레이(TV-04~06)와 시연용 메뉴를 겹쳐 그린다.
 *
 * 오버레이는 지금 이 앱 화면 위에만 뜬다. 다른 앱(재생 중인 영상) 위에 띄우는 시스템 창은 연결할 때 C·B가 더한다.
 */
@Composable
fun TvApp(
    setup: TvSetup,
    pairing: TvPairing,
    connections: TvConnections,
    overlay: TvOverlay,
    demoActions: List<DemoAction>,
    demoOpen: Boolean,
    onDemoDismiss: () -> Unit,
) {
    val kind by setup.kind.collectAsStateWithLifecycle()
    val code by pairing.code.collectAsStateWithLifecycle()
    val tvName by connections.tvName.collectAsStateWithLifecycle()
    val people by connections.people.collectAsStateWithLifecycle()
    val overlayState by overlay.state.collectAsStateWithLifecycle()

    TvCanvas {
        when (tvScreenFor(kind, people)) {
            TvScreen.Setup -> TvSetupScreen(onChoose = setup::choose)
            TvScreen.Pairing -> TvPairingScreen(code = code, kind = checkNotNull(kind))
            TvScreen.Connections -> TvConnectionsScreen(tvName = tvName, people = people, kind = checkNotNull(kind))
        }
        TvOverlayLayer(state = overlayState)
        if (demoOpen && demoActions.isNotEmpty()) {
            TvDemoMenu(actions = demoActions, onDismiss = onDemoDismiss)
        }
    }
}

/**
 * 시안(1280 x 720)을 실제 화면에 맞춰 늘이는 틀.
 *
 * 이 안에서는 1dp가 "시안의 1"이다. 그래서 화면 코드는 Figma 숫자를 그대로 쓰고,
 * 720p·1080p·4K 어느 TV에서도 시안과 같은 비율로 보인다. 화면 비율이 16:9가 아니면 가운데에 두고 남는 곳은 바탕색으로 채운다.
 * 글자 크기도 같은 비율로 맞추려고 시스템의 글자 크기 설정은 따르지 않는다(TV는 멀리서 보는 고정 화면이라서).
 */
@Composable
fun TvCanvas(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(MotionTheme.colors.bgGrouped),
        contentAlignment = Alignment.Center,
    ) {
        val scale = min(constraints.maxWidth / TvSize.CANVAS_WIDTH, constraints.maxHeight / TvSize.CANVAS_HEIGHT)
        CompositionLocalProvider(LocalDensity provides Density(density = scale, fontScale = 1f)) {
            Box(modifier = Modifier.size(TvSize.canvasWidth, TvSize.canvasHeight), content = content)
        }
    }
}
