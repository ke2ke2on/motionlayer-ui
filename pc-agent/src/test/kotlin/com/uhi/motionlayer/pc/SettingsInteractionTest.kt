package com.uhi.motionlayer.pc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import com.uhi.motionlayer.pc.data.fake.PcSampleData
import com.uhi.motionlayer.pc.domain.ConnectedPhone
import com.uhi.motionlayer.pc.domain.DemoAction
import com.uhi.motionlayer.pc.ui.PcStrings
import com.uhi.motionlayer.pc.ui.settings.SettingsContent
import com.uhi.motionlayer.pc.ui.settings.SettingsTab
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import org.junit.Rule
import org.junit.Test

/** 설정 창을 실제로 눌러 본다: 메뉴 이동, 시연용 메뉴(마우스 오른쪽 버튼), 끊기. */
@OptIn(ExperimentalTestApi::class)
class SettingsInteractionTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(demoActions: (connect: () -> Unit) -> List<DemoAction> = { emptyList() }) {
        rule.setContent {
            var tab by remember { mutableStateOf(SettingsTab.PhoneLink) }
            var phone by remember { mutableStateOf<ConnectedPhone?>(null) }
            MotionLayerTheme {
                SettingsContent(
                    tab = tab,
                    onTabSelected = { tab = it },
                    pairing = PcSampleData.pairing,
                    phone = phone,
                    connection = PcSampleData.connection,
                    presentation = PcSampleData.presentation,
                    onDisconnect = { phone = null },
                    onLaunchAtLoginChange = {},
                    onAutoConnectChange = {},
                    onAllowOtherPhonesChange = {},
                    onShowOperatorNameChange = {},
                    demoActions = demoActions { phone = PcSampleData.phone },
                )
            }
        }
    }

    @Test
    fun startsOnPhoneLinkWaiting() {
        show()
        rule.onNodeWithText(PcStrings.NO_PHONE).assertExists()
        rule.onNodeWithText("482 913").assertExists()
    }

    @Test
    fun presentationMenuShowsPc03() {
        show()
        rule.onNodeWithText(PcStrings.TAB_PRESENTATION).performClick()
        rule.onNodeWithText(PcStrings.POINTER_SIZE).assertExists()
        rule.onNodeWithText("3개 · 40MB").assertExists()
    }

    @Test
    fun rightClickOnSidebarOpensDemoActionsAndConnectingShowsPc02() {
        show { connect -> listOf(DemoAction("폰 연결됨으로 바꾸기") { connect() }) }
        rule.onNodeWithText(PcStrings.TAB_PRESENTATION).performMouseInput { rightClick() }
        rule.onNodeWithText("폰 연결됨으로 바꾸기").assertExists().performClick()
        rule.onNodeWithText("허윤의 Galaxy").assertExists()
        rule.onNodeWithText(PcStrings.CONNECTED).assertExists()

        // "끊기"를 누르면 연결 대기로 돌아간다
        rule.onNodeWithText(PcStrings.DISCONNECT).performClick()
        rule.onNodeWithText(PcStrings.NO_PHONE).assertExists()
    }
}
