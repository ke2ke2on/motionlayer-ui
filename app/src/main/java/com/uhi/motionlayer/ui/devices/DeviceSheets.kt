package com.uhi.motionlayer.ui.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.DeviceKind
import com.uhi.motionlayer.domain.model.ScannedDevice
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlBottomSheet
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.KoreanParticle

/**
 * 기기 추가 방법 시트(Figma DV-02). 진입은 하나이고 방법은 셋이다: QR 찍기, 코드 입력, 주변 기기 찾기(다른 사람 기기에 손님으로).
 * TV·PC 쪽에는 입력 화면이 없어서 어느 방법이든 폰에서 끝난다(분석서 FR-D-01). 줄을 누르면 시트가 닫히고 그 화면으로 간다.
 */
@Composable
fun AddDeviceSheet(
    onScanQr: () -> Unit,
    onEnterCode: () -> Unit,
    onFindNearby: () -> Unit,
    onDismiss: () -> Unit,
) {
    MlBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.dv02_title),
            style = MotionTheme.typography.title3,
            color = MotionTheme.colors.textPrimary,
            modifier = Modifier.padding(
                start = MotionSpacing.screenHorizontal,
                end = MotionSpacing.screenHorizontal,
                top = MotionSpacing.s8,
                bottom = MotionSpacing.s8,
            ),
        )
        MlListRow(
            title = stringResource(R.string.dv02_qr_title),
            subtitle = stringResource(R.string.dv02_qr_subtitle),
            leading = MlListRowLeading.Icon(MlIcon.Qr),
            trailing = MlListRowTrailing.Chevron,
            onClick = onScanQr,
        )
        MlListRow(
            title = stringResource(R.string.dv02_code_title),
            subtitle = stringResource(R.string.dv02_code_subtitle),
            leading = MlListRowLeading.Icon(MlIcon.Keyboard),
            trailing = MlListRowTrailing.Chevron,
            onClick = onEnterCode,
        )
        MlListRow(
            title = stringResource(R.string.dv02_nearby_title),
            subtitle = stringResource(R.string.dv02_nearby_subtitle),
            leading = MlListRowLeading.Icon(MlIcon.Users),
            trailing = MlListRowTrailing.Chevron,
            onClick = onFindNearby,
        )
        Spacer(Modifier.height(MotionSpacing.s8))
    }
}

/** DV-03에서 고를 수 있는 연결 방식. */
private enum class RegistrationChoice { Register, Once }

/**
 * 등록 방식 고르기 시트(Figma DV-03). 주인이 없는 기기를 찍었을 때 "내 기기로 등록"(계정에 저장, 집 밖에서도 조작)과
 * "이번만 연결"(로그인 없이 바로, 연결을 끊으면 사라짐) 중에서 고른다. 아래 큰 버튼은 고른 방식의 이름이다.
 * 처음에는 "내 기기로 등록"이 골라져 있다.
 *
 * @param signedIn 로그인했는지. 아니면 "내 기기로 등록"에 "로그인 필요"가 붙는다(누르면 로그인으로 보낸다)
 */
@Composable
fun RegisterDeviceSheet(
    device: ScannedDevice,
    signedIn: Boolean,
    onRegister: () -> Unit,
    onConnectOnce: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MotionTheme.colors
    var choice by rememberSaveable { mutableStateOf(RegistrationChoice.Register) }

    MlBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(
                start = MotionSpacing.screenHorizontal,
                end = MotionSpacing.screenHorizontal,
                top = MotionSpacing.s8,
                bottom = MotionSpacing.s8,
            ),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
        ) {
            Text(
                text = stringResource(R.string.dv03_title, device.name, KoreanParticle.objective(device.name)),
                style = MotionTheme.typography.title3,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(if (device.kind == DeviceKind.Tv) R.string.dv03_subtitle_tv else R.string.dv03_subtitle_pc),
                style = MotionTheme.typography.body2,
                color = colors.textSecondary,
            )
        }
        Column(
            modifier = Modifier.padding(
                start = MotionSpacing.screenHorizontal,
                end = MotionSpacing.screenHorizontal,
                top = MotionSpacing.s8,
                bottom = MotionSpacing.s8,
            ),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
        ) {
            ChoiceCard(
                title = stringResource(R.string.dv03_register_title),
                description = stringResource(R.string.dv03_register_description),
                selected = choice == RegistrationChoice.Register,
                badge = if (signedIn) null else stringResource(R.string.dv03_login_required),
                onSelect = { choice = RegistrationChoice.Register },
            )
            ChoiceCard(
                title = stringResource(R.string.dv03_once_title),
                description = stringResource(R.string.dv03_once_description),
                selected = choice == RegistrationChoice.Once,
                badge = null,
                onSelect = { choice = RegistrationChoice.Once },
            )
        }
        MlButton(
            text = stringResource(
                if (choice == RegistrationChoice.Register) R.string.dv03_register_title else R.string.dv03_once_title,
            ),
            onClick = if (choice == RegistrationChoice.Register) onRegister else onConnectOnce,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s12,
                    bottom = MotionSpacing.s8,
                ),
        )
    }
}

/**
 * 고를 수 있는 카드 한 장(Figma DV-03의 Option). 골라진 카드는 연한 파란 바탕에 파란 2dp 테두리와 파란 제목이고,
 * 아닌 카드는 흰 바탕에 `line/divider` 1dp 테두리와 검은 제목이다. 모서리 16, 안쪽 여백 16.
 */
@Composable
private fun ChoiceCard(
    title: String,
    description: String,
    selected: Boolean,
    badge: String?,
    onSelect: () -> Unit,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val shape = RoundedCornerShape(MotionRadius.r16)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) colors.brandWeak else colors.bgBase)
            .border(
                width = if (selected) MotionBorder.thick else MotionBorder.thin,
                color = if (selected) colors.brandPrimary else colors.lineDivider,
                shape = shape,
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(MotionSpacing.s16),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s4),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = type.body1Strong,
                color = if (selected) colors.brandPrimary else colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            if (badge != null) MlBadge(text = badge)
        }
        Text(text = description, style = type.caption1, color = colors.textSecondary)
    }
}
