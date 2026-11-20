package com.uhi.motionlayer.pc

import com.uhi.motionlayer.pc.data.fake.FakeAgentSettings
import com.uhi.motionlayer.pc.data.fake.FakePhoneLink
import com.uhi.motionlayer.pc.data.fake.FakePresentationOverlay
import com.uhi.motionlayer.pc.domain.AgentSettings
import com.uhi.motionlayer.pc.domain.DemoAction
import com.uhi.motionlayer.pc.domain.PhoneLink
import com.uhi.motionlayer.pc.domain.PresentationOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 화면이 쓸 구현을 고르는 한 곳(폰 앱의 `AppContainer`와 같은 역할).
 * 지금은 전부 가짜다. C가 실제 구현을 만들면 여기서만 바꿔 끼운다.
 */
object PcAppContainer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val fakePhoneLink = FakePhoneLink(scope)
    private val fakeSettings = FakeAgentSettings()
    private val fakeOverlay = FakePresentationOverlay(scope)

    val phoneLink: PhoneLink = fakePhoneLink
    val settings: AgentSettings = fakeSettings
    val overlay: PresentationOverlay = fakeOverlay

    /** 시연용 동작. 설정 창의 왼쪽 메뉴를 마우스 오른쪽 버튼으로 누르면 나온다. 실제 구현에서는 빈 목록. */
    val demoActions: List<DemoAction> = fakePhoneLink.demoActions + fakeOverlay.demoActions
}
