package com.uhi.motionlayer.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlCard
import com.uhi.motionlayer.ui.components.MlCardTone
import com.uhi.motionlayer.ui.components.MlBottomTab
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlButtonSize
import com.uhi.motionlayer.ui.components.MlButtonStyle
import com.uhi.motionlayer.ui.components.MlChip
import com.uhi.motionlayer.ui.components.MlDestructiveRow
import com.uhi.motionlayer.ui.components.MlDividerInset
import com.uhi.motionlayer.ui.components.MlDividerThick
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconButton
import com.uhi.motionlayer.ui.components.MlIconTile
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlProgressBar
import com.uhi.motionlayer.ui.components.MlResultBadge
import com.uhi.motionlayer.ui.components.MlResultTone
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.components.MlSheetHandle
import com.uhi.motionlayer.ui.components.MlTab
import com.uhi.motionlayer.ui.components.MlToast
import com.uhi.motionlayer.ui.components.MlToggle
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 공통 컴포넌트 확인용 화면. Figma "01 파운데이션 > Components"와 같은 이름·같은 변형을 모아서 보여 준다.
 * 앱의 실제 화면이 아니라, 컴포넌트를 바꿀 때 눈으로 비교하는 용도다.
 *
 * 스튜디오 미리보기는 구역별(@Preview)로 나뉘어 있고, 앱을 실행하면 전체가 한 화면에서 스크롤된다.
 */
@Composable
fun ComponentsPreview(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MotionTheme.colors.bgBase)
            .verticalScroll(rememberScrollState()),
    ) {
        ButtonsSection()
        ControlsSection()
        FeedbackSection()
        ResultSection()
        BarsSection()
        ListSection()
        TabAndIconsSection()
    }
}

@Composable
private fun ButtonsSection() = Section {
    Label("Button · Style × Size (L / M / S)")
    listOf(
        MlButtonStyle.Primary,
        MlButtonStyle.Secondary,
        MlButtonStyle.Weak,
        MlButtonStyle.Ghost,
        MlButtonStyle.Danger,
    ).forEach { style ->
        Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8), verticalAlignment = Alignment.CenterVertically) {
            MlButton("확인", {}, style = style, size = MlButtonSize.L)
            MlButton("확인", {}, style = style, size = MlButtonSize.M)
            MlButton("확인", {}, style = style, size = MlButtonSize.S)
        }
    }
    Label("Button · Disabled")
    Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8), verticalAlignment = Alignment.CenterVertically) {
        MlButton("비활성", {}, size = MlButtonSize.L, enabled = false)
        MlButton("비활성", {}, size = MlButtonSize.M, enabled = false)
        MlButton("비활성", {}, size = MlButtonSize.S, enabled = false)
    }
    Label("Button · 풀폭 (CTA)")
    MlButton("풀폭 CTA", {}, modifier = Modifier.fillMaxWidth())
    MlButton("나중에 할게요", {}, modifier = Modifier.fillMaxWidth(), style = MlButtonStyle.Ghost, size = MlButtonSize.M)
}

@Composable
private fun ControlsSection() = Section {
    var toggleOn by remember { mutableStateOf(true) }
    var chipSelected by remember { mutableIntStateOf(0) }

    Label("Toggle · On / Off")
    Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s16)) {
        MlToggle(checked = toggleOn, onCheckedChange = { toggleOn = it })
        MlToggle(checked = !toggleOn, onCheckedChange = { toggleOn = !it })
    }

    Label("Chip · Selected True / False")
    Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
        listOf("휙", "톡톡", "기울임", "흔들기").forEachIndexed { i, text ->
            MlChip(text, selected = chipSelected == i, onClick = { chipSelected = i })
        }
    }

    Label("Badge · Tone")
    Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
        MlBadge("연결됨", tone = MlTone.Green)
        MlBadge("3번째 시도", tone = MlTone.Blue)
        MlBadge("대기", tone = MlTone.Gray)
        MlBadge("실패", tone = MlTone.Red)
        MlBadge("경고", tone = MlTone.Orange)
    }

    Label("IconTile · Tone (Gray / Blue / Green / Red / Orange)")
    Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
        MlIconTile(MlIcon.Gesture, tone = MlTone.Gray)
        MlIconTile(MlIcon.Gesture, tone = MlTone.Blue)
        MlIconTile(MlIcon.Check, tone = MlTone.Green)
        MlIconTile(MlIcon.Alert, tone = MlTone.Red)
        MlIconTile(MlIcon.Alert, tone = MlTone.Orange)
    }
}

@Composable
private fun FeedbackSection() = Section {
    Label("Banner · Tone (Info / Success / Warning / Danger / Neutral)")
    MlBanner("안내 문구입니다.", tone = MlBannerTone.Info, modifier = Modifier.fillMaxWidth())
    MlBanner("성공했어요.", tone = MlBannerTone.Success, modifier = Modifier.fillMaxWidth())
    MlBanner("주의가 필요해요.", tone = MlBannerTone.Warning, modifier = Modifier.fillMaxWidth())
    MlBanner("실패했어요.", tone = MlBannerTone.Danger, modifier = Modifier.fillMaxWidth())
    MlBanner("참고하세요. 문구가 길어지면 이렇게 두 줄로 넘어가요. 아이콘은 위쪽에 맞춰져요.", tone = MlBannerTone.Neutral, modifier = Modifier.fillMaxWidth())

    Label("Toast")
    MlToast("다음 곡으로 넘겼어요", actionText = "되돌리기", modifier = Modifier.fillMaxWidth())
    MlToast("강의실 PC와 연결됐어요", modifier = Modifier.fillMaxWidth())
}

@Composable
private fun ResultSection() = Section {
    val colors = MotionTheme.colors

    Label("ResultBadge · Tone (Success / Brand / Danger / Neutral)")
    Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12)) {
        MlResultBadge(icon = MlIcon.Check, tone = MlResultTone.Success)
        MlResultBadge(icon = MlIcon.Check, tone = MlResultTone.Brand)
        MlResultBadge(icon = MlIcon.Alert, tone = MlResultTone.Danger)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12)) {
        MlResultBadge(icon = MlIcon.Lock, tone = MlResultTone.Neutral)
        MlResultBadge(icon = MlIcon.Search, tone = MlResultTone.Neutral)
        MlResultBadge(icon = MlIcon.CloudOff, tone = MlResultTone.Neutral)
    }

    Label("IconButton (뒤로·설정 같은 둥근 아이콘 버튼, 누르는 영역 48)")
    Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
        MlIconButton(icon = MlIcon.ChevronLeft, onClick = {}, contentDescription = "뒤로")
        MlIconButton(icon = MlIcon.Settings, onClick = {}, contentDescription = "설정")
        MlIconButton(icon = MlIcon.Close, onClick = {}, contentDescription = "닫기")
    }

    Label("ProgressBar · 보내는 중(두께 6) / 성공률(두께 8)")
    MlProgressBar(fraction = 0.64f)
    MlProgressBar(fraction = 0.9f, thickness = MotionSize.rateBar, track = colors.fillWeak)
    MlProgressBar(fraction = 0.6f, thickness = MotionSize.rateBar, track = colors.fillWeak, fill = colors.statusWarning)

    Label("DestructiveRow (상세 화면 맨 아래의 삭제 행)")
    MlDestructiveRow(text = "제스처 삭제", onClick = {})

    Label("Card · White / Gray / Outlined (모서리 20, 안쪽 여백 20)")
    MlCard { Text("흰 카드", style = MotionTheme.typography.body2, color = colors.textPrimary) }
    MlCard(tone = MlCardTone.Gray) { Text("회색 카드", style = MotionTheme.typography.body2, color = colors.textPrimary) }
    MlCard(tone = MlCardTone.Outlined) { Text("선 카드", style = MotionTheme.typography.body2, color = colors.textPrimary) }
}

@Composable
private fun BarsSection() {
    Column(modifier = Modifier.background(MotionTheme.colors.bgBase)) {
        Label("TopBar · Back / Close(+action) / None(+action)", Modifier.padding(horizontal = MotionSpacing.screenHorizontal))
        MlTopBar(title = "제목", leading = MlTopBarLeading.Back)
        MlTopBar(title = "제목", leading = MlTopBarLeading.Close, actionIcon = MlIcon.Settings, actionDescription = "설정")
        MlTopBar(title = "제목", leading = MlTopBarLeading.None, actionIcon = MlIcon.Settings, actionDescription = "설정")

        Label("SectionHeader · Divider/Thick · SheetHandle", Modifier.padding(horizontal = MotionSpacing.screenHorizontal))
        MlSectionHeader("섹션 제목", actionText = "전체 보기")
        MlDividerThick()
        MlSheetHandle()
    }
}

@Composable
private fun ListSection() {
    var toggleOn by remember { mutableStateOf(true) }

    Column(modifier = Modifier.background(MotionTheme.colors.bgBase)) {
        Label("ListRow · Leading(Icon/App/None) × Trailing(Chevron/Toggle/Value/None)", Modifier.padding(horizontal = MotionSpacing.screenHorizontal))
        MlListRow("항목 이름", subtitle = "설명 한 줄", leading = MlListRowLeading.Icon(MlIcon.Gesture), trailing = MlListRowTrailing.Chevron, onClick = {})
        MlDividerInset()
        MlListRow("항목 이름", subtitle = "설명 한 줄", leading = MlListRowLeading.Icon(MlIcon.Vibrate), trailing = MlListRowTrailing.Toggle(toggleOn) { toggleOn = it })
        MlDividerInset()
        MlListRow("항목 이름", subtitle = "설명 한 줄", leading = MlListRowLeading.Icon(MlIcon.Bell), trailing = MlListRowTrailing.Value("보통"), onClick = {})
        MlDividerInset()
        MlListRow("YouTube Music", subtitle = "재생 중", leading = MlListRowLeading.App("Y", Color(0xFFD8303E)), trailing = MlListRowTrailing.Toggle(true, null))
        MlDividerInset()
        MlListRow("항목 이름", subtitle = "설명 한 줄")
        MlDividerInset()
        MlListRow("설명 없는 행", trailing = MlListRowTrailing.Chevron, onClick = {})
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TabAndIconsSection() {
    val colors = MotionTheme.colors
    var tab by remember { mutableStateOf(MlTab.Home) }

    Column(modifier = Modifier.background(colors.bgBase)) {
        Label("BottomTab · Active 4종 (눌러서 전환)", Modifier.padding(horizontal = MotionSpacing.screenHorizontal))
        MlBottomTab(selected = tab, onSelect = { tab = it })

        Label("Icons · 24px, 2px stroke (${MlIcon.entries.size}개)", Modifier.padding(horizontal = MotionSpacing.screenHorizontal))
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s8),
            horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
        ) {
            MlIcon.entries.forEach { icon ->
                Column(modifier = Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    MlIconView(icon = icon, tint = colors.textPrimary)
                    Text(icon.name, style = MotionTheme.typography.caption2, color = colors.textTertiary, maxLines = 1)
                }
            }
        }
    }
}

/** 좌우 여백 24, 요소 사이 12로 쌓는 구역 틀. */
@Composable
private fun Section(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MotionTheme.colors.bgBase)
            .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s12),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
    ) {
        content()
    }
}

@Composable
private fun Label(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MotionTheme.typography.caption1Strong,
        color = MotionTheme.colors.textTertiary,
        modifier = modifier.padding(top = MotionSpacing.s8),
    )
}

// ---- 스튜디오 미리보기: 구역별로 한 화면에 들어오게 나눴다 ----

@Preview(name = "1 Buttons", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun ButtonsPreview() = MotionLayerTheme { ButtonsSection() }

@Preview(name = "2 Controls", showBackground = true, widthDp = 360, heightDp = 360)
@Composable
private fun ControlsPreview() = MotionLayerTheme { ControlsSection() }

@Preview(name = "3 Feedback", showBackground = true, widthDp = 360, heightDp = 480)
@Composable
private fun FeedbackPreview() = MotionLayerTheme { FeedbackSection() }

@Preview(name = "4 Result and Progress", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ResultPreview() = MotionLayerTheme { ResultSection() }

@Preview(name = "5 Bars", showBackground = true, widthDp = 360, heightDp = 330)
@Composable
private fun BarsPreview() = MotionLayerTheme { BarsSection() }

@Preview(name = "6 ListRow", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun ListPreview() = MotionLayerTheme { ListSection() }

@Preview(name = "7 Tab and Icons", showBackground = true, widthDp = 360, heightDp = 1100)
@Composable
private fun TabAndIconsPreview() = MotionLayerTheme { TabAndIconsSection() }

@Preview(name = "All (scroll in app)", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun AllPreview() = MotionLayerTheme { ComponentsPreview() }
