package com.uhi.motionlayer.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.uhi.motionlayer.ui.components.MlCard
import com.uhi.motionlayer.ui.components.MlCardTone
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.AppCategory
import com.uhi.motionlayer.domain.model.BasicGesture
import com.uhi.motionlayer.domain.model.ControllableApp
import com.uhi.motionlayer.ui.apps.PhoneControlRow
import com.uhi.motionlayer.ui.apps.labelRes
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlCtaFrame
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.gesture.icon
import com.uhi.motionlayer.ui.gesture.nameRes
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.KoreanParticle
import com.uhi.motionlayer.ui.components.MlResultBadge
import com.uhi.motionlayer.ui.components.MlResultTone

/** ON-11 본문이 상태 막대 아래에서 시작하는 위 여백(ON-03과 같다). */
private val ReadyTopPadding = 80.dp

/**
 * 기본 제스처 4가지(Figma ON-04). 이름 아래 설명은 이 화면 전용 문구이고, 글자색이 목록 기본(`text/tertiary`)보다 진한 `text/secondary`다.
 * 아래 안내 문구의 "주머니 위를 두드려서"는 주머니 제스처(FR-09) 이야기다.
 */
@Composable
fun BasicGesturesScreen(onNext: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors

    MlCtaFrame(
        modifier = modifier,
        topBar = { MlTopBar(title = null, leading = MlTopBarLeading.Back, onLeadingClick = onBack) },
        primaryText = stringResource(R.string.on04_next),
        onPrimary = onNext,
        contentTopPadding = MotionSpacing.s8,
        contentHorizontalPadding = 0.dp,
    ) {
        TitleBlock(
            title = stringResource(R.string.on04_title),
            description = stringResource(R.string.on04_description),
            bottomPadding = MotionSpacing.s16,
        )
        BasicGesture.entries.forEach { gesture ->
            MlListRow(
                title = stringResource(gesture.nameRes),
                subtitle = stringResource(gesture.onboardingSubtitleRes),
                leading = MlListRowLeading.Icon(gesture.icon, MlTone.Blue),
                subtitleColor = colors.textSecondary,
            )
        }
        MlBanner(
            message = stringResource(R.string.on04_pocket_note),
            tone = MlBannerTone.Neutral,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, top = MotionSpacing.s16),
        )
    }
}

private val BasicGesture.onboardingSubtitleRes: Int
    get() = when (this) {
        BasicGesture.Swipe -> R.string.on04_swipe_subtitle
        BasicGesture.DoubleTap -> R.string.on04_double_tap_subtitle
        BasicGesture.Tilt -> R.string.on04_tilt_subtitle
        BasicGesture.Shake -> R.string.on04_shake_subtitle
    }

/**
 * 쓸 앱 고르기(Figma ON-08). 앱마다 토글로 켜고 끄고, 맨 아래 "전화" 줄로 전화 제어도 켤 수 있다.
 * 아래 큰 버튼에는 켠 **앱** 수가 나온다(전화는 세지 않는다). 앱 목록(AP-01)과 같은 값을 바꾼다.
 */
@Composable
fun PickAppsScreen(
    apps: List<ControllableApp>,
    onToggle: (appId: String, enabled: Boolean) -> Unit,
    phoneEnabled: Boolean,
    onTogglePhone: (Boolean) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val enabledCount = apps.count { it.enabled }

    MlCtaFrame(
        modifier = modifier,
        topBar = { MlTopBar(title = null, leading = MlTopBarLeading.Back, onLeadingClick = onBack) },
        primaryText = if (enabledCount > 0) {
            stringResource(R.string.on08_cta_count, enabledCount)
        } else {
            stringResource(R.string.on08_cta_none)
        },
        onPrimary = onNext,
        contentTopPadding = MotionSpacing.s8,
        contentHorizontalPadding = 0.dp,
    ) {
        TitleBlock(
            title = stringResource(R.string.on08_title),
            description = stringResource(R.string.on08_description),
            bottomPadding = MotionSpacing.s12,
        )
        apps.forEach { app ->
            MlListRow(
                title = app.name,
                subtitle = stringResource(app.category.labelRes),
                leading = MlListRowLeading.App(letter = app.iconLetter, color = app.accentColorArgb?.let { Color(it) }),
                trailing = MlListRowTrailing.Toggle(checked = app.enabled, onCheckedChange = { onToggle(app.id, it) }),
                subtitleColor = colors.textSecondary,
            )
        }
        PhoneControlRow(enabled = phoneEnabled, onToggle = onTogglePhone, subtitleColor = colors.textSecondary)
    }
}

/**
 * 준비 완료(Figma ON-11). 방금 고른 앱 이름으로 "이 앱에서 노래를 틀고 휙 움직여 보세요"라고 안내한다.
 * 앱을 하나도 안 골랐으면 "앱 탭에서 고르라"고 알려 준다.
 */
@Composable
fun ReadyScreen(enabledApps: List<ControllableApp>, onHome: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    MlCtaFrame(
        modifier = modifier,
        topBar = {},
        primaryText = stringResource(R.string.on11_home),
        onPrimary = onHome,
        contentTopPadding = ReadyTopPadding,
    ) {
        // 본문 요소 사이는 8이고, 제목과 안내 카드는 위로 20이 더 떨어져 있다.
        Column(verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
            MlResultBadge(icon = MlIcon.Check, tone = MlResultTone.Brand)
            Text(
                text = stringResource(R.string.on11_title),
                style = type.title2,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = MotionSpacing.s20),
            )
            Text(text = readyDescription(enabledApps), style = type.body2, color = colors.textSecondary)
            MlCard(
                modifier = Modifier.padding(top = MotionSpacing.s20),
                tone = MlCardTone.Gray,
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s16),
            ) {
                TipRow(icon = MlIcon.Home, text = stringResource(R.string.on11_tip_home))
                TipRow(icon = MlIcon.Gesture, text = stringResource(R.string.on11_tip_practice))
                TipRow(icon = MlIcon.Shake, text = stringResource(R.string.on11_tip_undo))
            }
        }
    }
}

/**
 * 고른 앱 이름으로 만든 안내 문장. 앞의 두 앱만 이름을 부르고(둘이면 "A이나 B"), 두 앱의 종류에 맞춰
 * "노래를"·"영상을"·"음악이나 영상을" 가운데 알맞은 말을 쓴다.
 */
@Composable
private fun readyDescription(enabledApps: List<ControllableApp>): String {
    val named = enabledApps.take(2)
    if (named.isEmpty()) return stringResource(R.string.on11_description_none)

    val names = if (named.size == 2) {
        "${named[0].name}${KoreanParticle.or(named[0].name)} ${named[1].name}"
    } else {
        named[0].name
    }
    val media = stringResource(
        when {
            named.all { it.category == AppCategory.Music } -> R.string.on11_media_music
            named.all { it.category == AppCategory.Video } -> R.string.on11_media_video
            else -> R.string.on11_media_mixed
        },
    )
    return stringResource(R.string.on11_description_apps, names, media)
}

@Composable
private fun TipRow(icon: MlIcon, text: String) {
    val colors = MotionTheme.colors

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
    ) {
        MlIconView(icon = icon, tint = colors.textSecondary, size = MotionIconSize.s20)
        Text(text = text, style = MotionTheme.typography.body2, color = colors.textPrimary, modifier = Modifier.weight(1f))
    }
}

/** 큰 제목과 설명. 목록 줄이 위아래로 이어지는 화면(ON-04·08)에서 줄 위에 놓는다. 좌우 24, 아래 여백은 화면마다 다르다. */
@Composable
private fun TitleBlock(title: String, description: String, bottomPadding: Dp) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
    ) {
        Text(text = title, style = type.title2, color = colors.textPrimary)
        Text(text = description, style = type.body2, color = colors.textSecondary)
    }
}

@Preview(name = "ON-04 기본 제스처 4가지", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun On04Preview() = MotionLayerTheme { BasicGesturesScreen(onNext = {}, onBack = {}) }

@Preview(name = "ON-08 쓸 앱 고르기", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun On08Preview() = MotionLayerTheme {
    PickAppsScreen(
        apps = SampleData.apps.take(5),
        onToggle = { _, _ -> },
        phoneEnabled = false,
        onTogglePhone = {},
        onNext = {},
        onBack = {},
    )
}

@Preview(name = "ON-11 준비 완료", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun On11Preview() = MotionLayerTheme {
    ReadyScreen(enabledApps = SampleData.apps.filter { it.enabled }, onHome = {})
}
