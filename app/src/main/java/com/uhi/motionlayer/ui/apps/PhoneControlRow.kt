package com.uhi.motionlayer.ui.apps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.uhi.motionlayer.R
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlTone

/**
 * "전화" 줄(Figma AP-01·ON-08): 회색 전화 아이콘 타일, "전화", "휙으로 받기 · 흔들어 거절", 오른쪽 토글.
 * 앱 목록(AP-01)에서는 줄을 누르면 전화 제어(AP-04)가 열리고([onOpen]), 첫 실행 안내(ON-08)에서는 토글만 누른다.
 * 토글을 켜면 전화 권한을 요청한다(전화 제어가 한다).
 *
 * @param subtitleColor 설명 글자색. null이면 `text/tertiary`(ON-08은 `text/secondary`)
 */
@Composable
internal fun PhoneControlRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onOpen: (() -> Unit)? = null,
    subtitleColor: Color? = null,
) {
    MlListRow(
        modifier = modifier,
        title = stringResource(R.string.ap01_phone_title),
        subtitle = stringResource(R.string.ap01_phone_subtitle),
        leading = MlListRowLeading.Icon(MlIcon.PhoneCall, MlTone.Gray),
        trailing = MlListRowTrailing.Toggle(checked = enabled, onCheckedChange = onToggle),
        onClick = onOpen,
        subtitleColor = subtitleColor,
    )
}
