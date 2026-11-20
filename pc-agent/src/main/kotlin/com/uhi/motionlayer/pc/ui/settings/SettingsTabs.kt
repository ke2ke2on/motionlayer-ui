package com.uhi.motionlayer.pc.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.uhi.motionlayer.pc.domain.ConnectedPhone
import com.uhi.motionlayer.pc.domain.ConnectionPreferences
import com.uhi.motionlayer.pc.domain.PairingCode
import com.uhi.motionlayer.pc.domain.PresentationPreferences
import com.uhi.motionlayer.pc.domain.QrPattern
import com.uhi.motionlayer.pc.ui.PcStrings
import com.uhi.motionlayer.pc.ui.theme.PcSize
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlButtonSize
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlToggle
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.Text
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/** PC-01 폰 연결 · 연결 대기: QR 카드, "연결된 폰 없음", 스위치 두 줄. */
@Composable
internal fun PhoneLinkWaiting(
    pairing: PairingCode,
    connection: ConnectionPreferences,
    onLaunchAtLoginChange: (Boolean) -> Unit,
    onAutoConnectChange: (Boolean) -> Unit,
) {
    TabTitle(PcStrings.TAB_PHONE_LINK)
    QrCard(pairing)
    Row { MlBadge(text = PcStrings.NO_PHONE, tone = MlTone.Gray) }
    SettingRow(title = PcStrings.LAUNCH_AT_LOGIN) {
        MlToggle(checked = connection.launchAtLogin, onCheckedChange = onLaunchAtLoginChange)
    }
    SettingRow(title = PcStrings.AUTO_CONNECT, note = PcStrings.AUTO_CONNECT_NOTE) {
        MlToggle(checked = connection.autoConnectMyPhone, onCheckedChange = onAutoConnectChange)
    }
}

/** PC-02 폰 연결 · 연결됨: 연결된 폰 카드, PC 이름, 스위치 두 줄. */
@Composable
internal fun PhoneLinkConnected(
    phone: ConnectedPhone,
    connection: ConnectionPreferences,
    onDisconnect: () -> Unit,
    onAllowOtherPhonesChange: (Boolean) -> Unit,
    onShowOperatorNameChange: (Boolean) -> Unit,
) {
    TabTitle(PcStrings.TAB_PHONE_LINK)
    ConnectedPhoneCard(phone = phone, onDisconnect = onDisconnect)
    SettingRow(title = PcStrings.PC_NAME, note = PcStrings.PC_NAME_NOTE) { RowValue(connection.pcName) }
    SettingRow(title = PcStrings.ALLOW_OTHER_PHONES) {
        MlToggle(checked = connection.allowOtherPhones, onCheckedChange = onAllowOtherPhonesChange)
    }
    SettingRow(title = PcStrings.SHOW_OPERATOR, note = PcStrings.SHOW_OPERATOR_NOTE) {
        MlToggle(checked = connection.showOperatorName, onCheckedChange = onShowOperatorNameChange)
    }
}

/** PC-03 발표: 값만 보여 주는 네 줄. 값을 고르는 화면은 시안에 없어 줄을 누를 수 없다. */
@Composable
internal fun PresentationTab(preferences: PresentationPreferences) {
    TabTitle(PcStrings.TAB_PRESENTATION)
    Text(
        text = PcStrings.PRESENTATION_NOTE,
        style = MotionTheme.typography.body2,
        color = MotionTheme.colors.textSecondary,
    )
    SettingRow(title = PcStrings.POINTER_SIZE) { RowValue(PcStrings.pointerSize(preferences.pointerSize)) }
    SettingRow(title = PcStrings.POINTER_COLOR) { RowValue(PcStrings.pointerColor(preferences.pointerColor)) }
    SettingRow(title = PcStrings.STROKE_FADE) { RowValue(PcStrings.strokeFade(preferences.strokeFadeSeconds)) }
    SettingRow(title = PcStrings.EFFECTS, note = PcStrings.EFFECTS_NOTE) {
        RowValue(PcStrings.effects(preferences.effectCount, preferences.effectBytes))
    }
}

@Composable
private fun TabTitle(text: String) {
    Text(text = text, style = MotionTheme.typography.title3, color = MotionTheme.colors.textPrimary)
}

@Composable
private fun QrCard(pairing: PairingCode) {
    val colors = MotionTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgGrouped, RoundedCornerShape(MotionRadius.r16))
            .padding(MotionSpacing.s24),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s24),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(PcSize.qrBox)
                .background(colors.bgBase, RoundedCornerShape(MotionRadius.r12)),
            contentAlignment = Alignment.Center,
        ) {
            QrCodeView(pattern = pairing.qr, modifier = Modifier.size(PcSize.qrCode))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
            Text(text = PcStrings.QR_GUIDE, style = MotionTheme.typography.body2, color = colors.textSecondary)
            Text(
                text = PcStrings.pairingDigits(pairing.digits),
                style = MotionTheme.typography.title1,
                color = colors.textPrimary,
            )
            Text(
                text = PcStrings.codeHint(pairing.secondsLeft),
                style = MotionTheme.typography.caption1,
                color = colors.textTertiary,
            )
        }
    }
}

/** 받은 QR 무늬를 칸마다 네모로 그린다. 무늬를 만들지는 않는다. */
@Composable
private fun QrCodeView(pattern: QrPattern, modifier: Modifier = Modifier) {
    val ink = MotionTheme.colors.textPrimary
    Canvas(modifier.padding(PcSize.qrQuietZone).semantics { contentDescription = PcStrings.QR_DESCRIPTION }) {
        val cell = size.minDimension / pattern.size
        for (row in 0 until pattern.size) {
            for (column in 0 until pattern.size) {
                if (pattern.isDark(column, row)) {
                    // 칸 사이에 실금이 보이지 않게 아주 조금 겹쳐 칠한다.
                    drawRect(color = ink, topLeft = Offset(column * cell, row * cell), size = Size(cell + 0.5f, cell + 0.5f))
                }
            }
        }
    }
}

@Composable
private fun ConnectedPhoneCard(phone: ConnectedPhone, onDisconnect: () -> Unit) {
    val colors = MotionTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgGrouped, RoundedCornerShape(MotionRadius.r16))
            .padding(horizontal = MotionSpacing.s20, vertical = MotionSpacing.s18),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(PcSize.phoneTile)
                .background(colors.brandWeak, RoundedCornerShape(MotionRadius.r12)),
            contentAlignment = Alignment.Center,
        ) {
            MlIconView(icon = MlIcon.Smartphone, tint = colors.brandPrimary, size = MotionIconSize.s22)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2)) {
            Text(text = phone.name, style = MotionTheme.typography.body1Strong, color = colors.textPrimary)
            Text(
                text = PcStrings.phoneDetail(phone),
                style = MotionTheme.typography.caption1,
                color = colors.textTertiary,
            )
        }
        MlBadge(text = PcStrings.CONNECTED, tone = MlTone.Green)
        MlButton(
            text = PcStrings.DISCONNECT,
            onClick = onDisconnect,
            style = MlButtonStyle.Secondary,
            size = MlButtonSize.S,
        )
    }
}

/**
 * 설정 한 줄: 위에 가는 선, 왼쪽에 이름(과 설명), 오른쪽에 스위치나 값.
 * 폰의 `MlListRow`와 여백·선 위치가 달라(Figma PC 시안) 따로 두었다.
 */
@Composable
private fun SettingRow(title: String, note: String? = null, trailing: @Composable () -> Unit) {
    val colors = MotionTheme.colors
    Column {
        Box(Modifier.fillMaxWidth().height(MotionBorder.thin).background(colors.lineDivider))
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = MotionSpacing.s12),
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(MotionSpacing.s2)) {
                Text(text = title, style = MotionTheme.typography.body2Strong, color = colors.textPrimary)
                if (note != null) {
                    Text(text = note, style = MotionTheme.typography.caption1, color = colors.textTertiary)
                }
            }
            trailing()
        }
    }
}

@Composable
private fun RowValue(text: String) {
    Text(text = text, style = MotionTheme.typography.body2, color = MotionTheme.colors.textTertiary)
}
