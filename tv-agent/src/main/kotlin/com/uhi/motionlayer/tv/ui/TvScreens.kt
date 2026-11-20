package com.uhi.motionlayer.tv.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.uhi.motionlayer.tv.R
import com.uhi.motionlayer.tv.domain.ConnectedPerson
import com.uhi.motionlayer.tv.domain.PersonRole
import com.uhi.motionlayer.tv.domain.PersonStatus
import com.uhi.motionlayer.tv.domain.QrPattern
import com.uhi.motionlayer.tv.domain.TvKind
import com.uhi.motionlayer.tv.domain.TvPairingCode
import com.uhi.motionlayer.tv.ui.theme.TvBorder
import com.uhi.motionlayer.tv.ui.theme.TvIconSize
import com.uhi.motionlayer.tv.ui.theme.TvRadius
import com.uhi.motionlayer.tv.ui.theme.TvShadows
import com.uhi.motionlayer.tv.ui.theme.TvSize
import com.uhi.motionlayer.tv.ui.theme.TvSpacing
import com.uhi.motionlayer.tv.ui.theme.TvType
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.Text
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.theme.motionShadow

/** 모든 TV 화면 왼쪽 위의 "MotionLayer" 표시. */
@Composable
private fun TvBrand() {
    val colors = MotionTheme.colors
    Row(
        modifier = Modifier.padding(start = TvSpacing.screenStart, top = TvSpacing.brandTop),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(TvSize.brandMark)
                .background(colors.brandPrimary, RoundedCornerShape(MotionRadius.r10)),
            contentAlignment = Alignment.Center,
        ) {
            MlIconView(icon = MlIcon.Gesture, tint = colors.textInverse, size = MotionIconSize.s20)
        }
        Text(text = stringResource(R.string.app_name), style = TvType.brand, color = colors.textPrimary)
    }
}

/**
 * TV-01 처음 실행: 이 TV를 개인용으로 쓸지 공용으로 쓸지 리모컨으로 고른다.
 * 방향키로 카드 사이를 옮기고 확인을 누르면 [onChoose]가 불린다. 처음에는 "개인 TV"에 포커스가 있다.
 */
@Composable
fun TvSetupScreen(onChoose: (TvKind) -> Unit) {
    val colors = MotionTheme.colors
    val firstCard = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstCard.requestFocus() }

    Box(Modifier.fillMaxSize()) {
        TvBrand()
        Column(
            modifier = Modifier.padding(start = TvSpacing.screenStart, top = TvSpacing.contentTop),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s16),
        ) {
            Text(text = stringResource(R.string.tv01_title), style = TvType.title, color = colors.textPrimary)
            Text(text = stringResource(R.string.tv01_subtitle), style = TvType.body, color = colors.textSecondary)
        }
        Row(
            modifier = Modifier.padding(start = TvSpacing.screenStart, top = TvSpacing.choicesTop),
            horizontalArrangement = Arrangement.spacedBy(TvSpacing.s32),
        ) {
            ChoiceCard(
                icon = MlIcon.User,
                title = stringResource(R.string.tv01_personal_title),
                body = stringResource(R.string.tv01_personal_body),
                onClick = { onChoose(TvKind.Personal) },
                modifier = Modifier.focusRequester(firstCard),
            )
            ChoiceCard(
                icon = MlIcon.Users,
                title = stringResource(R.string.tv01_public_title),
                body = stringResource(R.string.tv01_public_body),
                onClick = { onChoose(TvKind.Public) },
            )
        }
        Text(
            text = stringResource(R.string.tv01_hint),
            style = TvType.detail,
            color = colors.textTertiary,
            modifier = Modifier.padding(start = TvSpacing.screenStart, top = TvSpacing.hintTop),
        )
    }
}

/**
 * 고를 수 있는 카드. 리모컨 포커스가 오면 시안대로 파란 굵은 테두리와 그림자가 생기고 아이콘 칸이 파래진다.
 * 테두리는 안쪽에 그려서 포커스가 오갈 때 글자 자리가 움직이지 않는다.
 */
@Composable
private fun ChoiceCard(icon: MlIcon, title: String, body: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val shape = RoundedCornerShape(MotionRadius.r28)
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .size(width = TvSize.choiceWidth, height = TvSize.choiceHeight)
            .then(if (focused) Modifier.motionShadow(TvShadows.floating, shape) else Modifier)
            .background(colors.bgBase, shape)
            .then(if (focused) Modifier.border(TvBorder.focus, colors.brandPrimary, shape) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            // 포커스 표시는 위의 테두리로 하므로 기본 눌림·포커스 효과는 끈다.
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            // 아래 여백은 두지 않는다: 시안도 카드 높이(240)가 고정이라 글 두 줄이 아래 여백 자리까지 내려온다.
            .padding(start = TvSpacing.s36, top = TvSpacing.s36, end = TvSpacing.s36),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s16),
    ) {
        Box(
            modifier = Modifier
                .size(TvSize.choiceTile)
                .background(if (focused) colors.brandWeak else colors.fillWeak, RoundedCornerShape(MotionRadius.r16)),
            contentAlignment = Alignment.Center,
        ) {
            MlIconView(icon = icon, tint = if (focused) colors.brandPrimary else colors.textSecondary, size = TvIconSize.s30)
        }
        Text(text = title, style = TvType.cardTitle, color = colors.textPrimary)
        Text(text = body, style = TvType.detail, color = colors.textSecondary)
    }
}

/**
 * TV-02 QR 대기: 왼쪽에 순서 세 줄, 오른쪽에 QR 카드와 6자리 코드. 누를 것은 없다.
 * 공용 TV는 주인으로 등록할 수 없어서 3번 글이 다르다(시안은 개인 TV 한 벌뿐).
 */
@Composable
fun TvPairingScreen(code: TvPairingCode, kind: TvKind) {
    val colors = MotionTheme.colors
    Box(Modifier.fillMaxSize()) {
        TvBrand()
        Column(
            modifier = Modifier
                .padding(start = TvSpacing.screenStart, top = TvSpacing.pairingTextTop)
                .width(TvSize.pairingTextWidth),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s20),
        ) {
            Text(text = stringResource(R.string.tv02_title), style = TvType.display, color = colors.textPrimary)
            PairingStep(number = "1", text = stringResource(R.string.tv02_step_1))
            PairingStep(number = "2", text = stringResource(R.string.tv02_step_2))
            PairingStep(
                number = "3",
                text = stringResource(if (kind == TvKind.Public) R.string.tv02_step_3_public else R.string.tv02_step_3),
            )
        }

        val cardShape = RoundedCornerShape(TvRadius.r32)
        Column(
            modifier = Modifier
                .padding(start = TvSpacing.qrCardStart, top = TvSpacing.qrCardTop)
                .motionShadow(TvShadows.floating, cardShape)
                .background(colors.bgBase, cardShape)
                .padding(TvSpacing.s36),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.size(TvSize.qrBox), contentAlignment = Alignment.Center) {
                QrCodeView(pattern = code.qr, modifier = Modifier.size(TvSize.qrCode))
            }
            Text(text = code.digits.chunked(3).joinToString(" "), style = TvType.code, color = colors.textPrimary)
            Text(text = stringResource(R.string.tv02_code_note), style = TvType.caption, color = colors.textTertiary)
        }
    }
}

@Composable
private fun PairingStep(number: String, text: String) {
    val colors = MotionTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(TvSize.stepNumber).background(colors.brandWeak, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = number, style = MotionTheme.typography.body1Strong, color = colors.brandPrimary)
        }
        Text(text = text, style = TvType.body, color = colors.textSecondary)
    }
}

/** 받은 QR 무늬를 칸마다 네모로 그린다. 무늬를 만들지는 않는다. PC 모듈의 것과 같은 코드다. */
@Composable
private fun QrCodeView(pattern: QrPattern, modifier: Modifier = Modifier) {
    val ink = MotionTheme.colors.textPrimary
    val description = stringResource(R.string.tv02_qr_description)
    Canvas(modifier.padding(TvSize.qrQuietZone).semantics { contentDescription = description }) {
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

/**
 * TV-03 연결 상태: TV 이름과 연결된 사람 목록. 보기 전용이고, 끊거나 바꾸는 일은 주인의 폰(공용 TV는 각자의 폰)에서 한다.
 * 줄은 [MAX_PERSON_ROWS]개까지만 그리고, 넘치는 사람은 마지막 줄 하나로 묶는다([personRows]).
 */
@Composable
fun TvConnectionsScreen(tvName: String, people: List<ConnectedPerson>, kind: TvKind) {
    val colors = MotionTheme.colors
    Box(Modifier.fillMaxSize()) {
        TvBrand()
        Column(
            modifier = Modifier
                .padding(start = TvSpacing.screenStart, top = TvSpacing.contentTop)
                .width(TvSize.connectionsWidth),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s24),
        ) {
            Text(text = tvName, style = TvType.title, color = colors.textPrimary)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgBase, RoundedCornerShape(MotionRadius.r28))
                    .padding(horizontal = TvSpacing.s32, vertical = MotionSpacing.s12),
            ) {
                val rows = personRows(people)
                rows.shown.forEach { person -> PersonRow(person) }
                if (rows.rest.isNotEmpty()) MorePeopleRow(rows.rest)
            }
            Text(
                text = stringResource(if (kind == TvKind.Public) R.string.tv03_footer_public else R.string.tv03_footer),
                style = TvType.detail,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun PersonRow(person: ConnectedPerson) {
    val isOwner = person.role == PersonRole.Owner
    val role = stringResource(if (isOwner) R.string.tv03_role_owner else R.string.tv03_role_guest)
    val status = stringResource(
        when (person.status) {
            PersonStatus.Operating -> R.string.tv03_status_operating
            PersonStatus.SameWifi -> R.string.tv03_status_same_wifi
            PersonStatus.Remote -> R.string.tv03_status_remote
        },
    )
    PersonRowFrame(
        icon = MlIcon.User,
        title = person.name,
        detail = stringResource(R.string.tv03_person_detail, role, status),
    ) {
        MlBadge(text = role, tone = if (isOwner) MlTone.Blue else MlTone.Gray)
    }
}

/** 줄에 다 그리지 못한 사람들을 묶은 줄: "그 밖에 3명"과 그 아래 이름들. 이름이 많으면 한 줄에서 줄여 보인다. */
@Composable
private fun MorePeopleRow(rest: List<ConnectedPerson>) {
    PersonRowFrame(
        icon = MlIcon.Users,
        title = stringResource(R.string.tv03_more_people, rest.size),
        detail = rest.joinToString(separator = " · ") { it.name },
    )
}

/** TV-03 한 줄의 틀: 동그란 아이콘 · 이름과 아래 글 · 오른쪽 끝(배지). 시안의 줄 높이 106 그대로다. */
@Composable
private fun PersonRowFrame(icon: MlIcon, title: String, detail: String, trailing: (@Composable () -> Unit)? = null) {
    val colors = MotionTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = MotionSpacing.s20),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s20),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(TvSize.avatar).background(colors.fillWeak, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            MlIconView(icon = icon, tint = colors.textSecondary, size = MotionIconSize.s28)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(MotionSpacing.s4)) {
            Text(text = title, style = TvType.name, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = detail, style = TvType.detail, color = colors.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        trailing?.invoke()
    }
}

/** TV-03에 한 번에 그리는 줄 수. 4줄부터는 아래 안내 글이 화면(높이 720) 밖으로 나간다(시안 예시는 2명). */
internal const val MAX_PERSON_ROWS = 3

/** 한 줄씩 그릴 사람([shown])과 "그 밖에 N명" 한 줄로 묶을 사람([rest]). */
internal class PersonRows(val shown: List<ConnectedPerson>, val rest: List<ConnectedPerson>)

/**
 * 연결된 사람을 [maxRows]줄에 나눈다. 다 들어가면 모두 한 줄씩 그리고, 넘치면 마지막 줄을 묶음 줄로 쓴다.
 * 순서는 받은 그대로다(주인과 지금 조작 중인 사람을 앞에 두는 일은 구현이 한다 — `TvConnections.people`).
 */
internal fun personRows(people: List<ConnectedPerson>, maxRows: Int = MAX_PERSON_ROWS): PersonRows =
    if (people.size <= maxRows) {
        PersonRows(shown = people, rest = emptyList())
    } else {
        PersonRows(shown = people.take(maxRows - 1), rest = people.drop(maxRows - 1))
    }
