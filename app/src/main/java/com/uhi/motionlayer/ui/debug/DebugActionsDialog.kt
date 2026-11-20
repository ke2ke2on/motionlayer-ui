package com.uhi.motionlayer.ui.debug

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.uhi.motionlayer.R

/** 지금 실행 중인 앱이 디버그 빌드인지. 시연용 동작은 디버그 빌드에서만 보여 준다. */
@Composable
fun isDebuggable(): Boolean {
    val context = LocalContext.current
    return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}

/**
 * 센서가 없는 에뮬레이터에서 화면 상태를 바꿔 보는 "시연용 동작" 목록 창.
 * 각 화면이 정한 자리를 길게 누르면 열린다. 실제 구현으로 바꾸면 동작 목록이 비어 아무 일도 없다.
 */
@Composable
fun DebugActionsDialog(actions: List<DebugAction>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.debug_title)) },
        text = {
            // 동작이 많아도 창 밖으로 넘치지 않게 목록만 스크롤한다.
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                actions.forEach { action ->
                    TextButton(
                        onClick = {
                            action.run()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = action.label, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.debug_close)) }
        },
    )
}
