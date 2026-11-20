package com.uhi.motionlayer.tv

import com.uhi.motionlayer.tv.data.fake.FakeTvConnections
import com.uhi.motionlayer.tv.data.fake.FakeTvOverlay
import com.uhi.motionlayer.tv.data.fake.FakeTvPairing
import com.uhi.motionlayer.tv.data.fake.FakeTvSetup
import com.uhi.motionlayer.tv.domain.DemoAction
import com.uhi.motionlayer.tv.domain.TvConnections
import com.uhi.motionlayer.tv.domain.TvOverlay
import com.uhi.motionlayer.tv.domain.TvPairing
import com.uhi.motionlayer.tv.domain.TvSetup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 화면이 쓸 구현을 고르는 한 곳(폰 앱의 `AppContainer`와 같은 역할).
 * 지금은 전부 가짜다. C가 실제 구현을 만들면 여기서만 바꿔 끼운다.
 */
object TvAppContainer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val fakeSetup = FakeTvSetup()
    private val fakePairing = FakeTvPairing(scope)
    private val fakeConnections = FakeTvConnections()
    private val fakeOverlay = FakeTvOverlay(scope)

    val setup: TvSetup = fakeSetup
    val pairing: TvPairing = fakePairing
    val connections: TvConnections = fakeConnections
    val overlay: TvOverlay = fakeOverlay

    /** 시연용 동작. 디버그 빌드에서 리모컨 메뉴 키를 누르면 나온다. 실제 구현에서는 빈 목록. */
    val demoActions: List<DemoAction> =
        fakeSetup.demoActions + fakePairing.demoActions + fakeConnections.demoActions + fakeOverlay.demoActions
}
