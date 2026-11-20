package com.uhi.motionlayer

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.uhi.motionlayer.ui.navigation.AppNavigation
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 앱은 항상 라이트 테마라서 상태바·내비게이션바 아이콘을 어둡게 고정한다.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            MotionLayerTheme {
                // 화면이 480dp보다 넓으면(태블릿 등) 내용 폭을 480으로 줄여 가운데에 둔다(보고서 6.3). 폰에서는 그대로 꽉 찬다.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MotionTheme.colors.bgGrouped),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Box(modifier = Modifier.widthIn(max = MotionSize.contentMaxWidth).fillMaxSize()) {
                        AppNavigation(
                            qrCamera = AppContainer.qrCamera,
                            debugActions = AppContainer.presentationDebugActions,
                            practiceDebugActions = AppContainer.practiceDebugActions,
                            homeDebugActions = AppContainer.homeDebugActions,
                            onboardingDebugActions = AppContainer.onboardingDebugActions,
                            accountDebugActions = AppContainer.accountDebugActions,
                            deviceDebugActions = AppContainer.deviceDebugActions,
                            recordingDebugActions = AppContainer.recordingDebugActions,
                        )
                    }
                }
            }
        }
    }
}
