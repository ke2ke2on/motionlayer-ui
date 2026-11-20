package com.uhi.motionlayer.tv

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.uhi.motionlayer.tv.domain.DemoAction
import com.uhi.motionlayer.tv.ui.TvApp
import com.uhi.motionlayer.ui.theme.MotionLayerTheme

/**
 * TV 앱의 화면 하나짜리 틀. 화면은 전부 [TvApp]이 그린다.
 *
 * 시연용 동작(디버그 빌드만):
 * - 리모컨 **메뉴 키**나 키보드 **M** 키를 누르면 목록이 열리고 닫힌다(에뮬레이터에서는 메뉴 키를 찾기 어려워 M도 받는다).
 * - 켤 때 `--es demo "TV-03,TV-04"`처럼 이름 일부를 주면 그 동작들을 차례로 실행한다(화면 확인을 자동으로 할 때 쓴다).
 */
class TvActivity : ComponentActivity() {

    private var demoOpen by mutableStateOf(false)

    private val demoActions: List<DemoAction> by lazy {
        val debuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (debuggable) TvAppContainer.demoActions else emptyList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TV에는 상태 막대가 없지만, 다른 기기에서 켜도 화면 전체를 쓰게 한다.
        WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())

        val container = TvAppContainer
        setContent {
            MotionLayerTheme {
                TvApp(
                    setup = container.setup,
                    pairing = container.pairing,
                    connections = container.connections,
                    overlay = container.overlay,
                    demoActions = demoActions,
                    demoOpen = demoOpen,
                    onDemoDismiss = { demoOpen = false },
                )
            }
        }
        runDemoFrom(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        runDemoFrom(intent)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val demoKey = keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_M
        if (demoKey && demoActions.isNotEmpty()) {
            demoOpen = !demoOpen
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun runDemoFrom(intent: Intent?) {
        val wanted = intent?.getStringExtra(EXTRA_DEMO) ?: return
        wanted.split(',').map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
            demoActions.firstOrNull { tag in it.label }?.run?.invoke()
        }
    }

    private companion object {
        const val EXTRA_DEMO = "demo"
    }
}
