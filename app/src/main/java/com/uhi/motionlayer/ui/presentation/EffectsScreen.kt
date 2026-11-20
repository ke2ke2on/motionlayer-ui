package com.uhi.motionlayer.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.EffectKind
import com.uhi.motionlayer.domain.model.PresentationEffect
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlButton
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.KoreanParticle
import com.uhi.motionlayer.ui.util.formatFileSize
import com.uhi.motionlayer.ui.components.MlProgressBar
import com.uhi.motionlayer.ui.theme.MotionSize

/** 효과 관리(Figma PR-14). 발표 준비의 "미리 보낸 효과"와 발표 중 아래 "효과" 버튼에서 연다. */
@Composable
fun EffectsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EffectsViewModel = viewModel(factory = EffectsViewModel.Factory),
) {
    val effects by viewModel.effects.collectAsStateWithLifecycle()
    val pcName by viewModel.pcName.collectAsStateWithLifecycle()

    EffectsContent(
        pcName = pcName,
        effects = effects,
        onAdd = viewModel::onAdd,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * 효과 목록과 "효과 추가" 버튼. 상태를 받아 그리기만 한다.
 *
 * Figma 줄 오른쪽의 화살표는 눌러서 갈 곳(효과 하나의 상세)이 없어 두지 않는다.
 * 사진·소리·영상은 파일 크기를, 보내는 중인 파일은 "N% + 진행 막대"를 보여 준다.
 */
@Composable
fun EffectsContent(
    pcName: String?,
    effects: List<PresentationEffect>,
    onAdd: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = stringResource(R.string.pr14_title), leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.pr14_description),
                style = MotionTheme.typography.body2,
                color = colors.textSecondary,
                modifier = Modifier.padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s4,
                    bottom = MotionSpacing.s8,
                ),
            )

            MlSectionHeader(
                title = if (pcName != null) {
                    stringResource(R.string.pr14_header, pcName, KoreanParticle.toward(pcName))
                } else {
                    stringResource(R.string.pr14_header_default)
                },
            )
            effects.forEach { effect ->
                EffectRow(effect)
            }

            MlBanner(
                message = stringResource(R.string.pr14_note),
                tone = MlBannerTone.Neutral,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, top = MotionSpacing.s12),
            )
            Box(Modifier.height(MotionSpacing.s16))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s12,
                    bottom = MotionSpacing.s8,
                ),
        ) {
            MlButton(text = stringResource(R.string.pr14_add), onClick = onAdd, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** 효과 하나. 보내는 중이면 줄 아래에 진행 막대가 붙는다. */
@Composable
private fun EffectRow(effect: PresentationEffect) {
    val size = formatFileSize(effect.sizeBytes)
    val percent = effect.uploadPercent

    MlListRow(
        title = effect.name,
        subtitle = if (percent != null) stringResource(R.string.pr14_uploading, size) else size,
        leading = MlListRowLeading.Icon(effect.kind.icon, MlTone.Blue),
        trailing = MlListRowTrailing.Value(
            text = if (percent != null) stringResource(R.string.pr14_progress, percent) else stringResource(R.string.pr14_sent),
            chevron = false,
        ),
    )
    if (percent != null) {
        UploadProgress(
            percent = percent,
            modifier = Modifier.padding(
                // 줄의 글자와 맞춘다: 좌우 여백 24 + 아이콘 타일 40 + 사이 14
                start = MotionSpacing.screenHorizontal + MotionSize.iconTile + MotionSpacing.s14,
                end = MotionSpacing.screenHorizontal,
                bottom = MotionSpacing.s8,
            ),
        )
    }
}

/** 가로로 긴 회색 막대 위에 파란 막대가 [percent]%만큼 찬다(높이 6, 모서리 3). */
@Composable
private fun UploadProgress(percent: Int, modifier: Modifier = Modifier) {
    MlProgressBar(fraction = percent / 100f, modifier = modifier)
}

private val EffectKind.icon: MlIcon
    get() = when (this) {
        EffectKind.Image -> MlIcon.Image
        EffectKind.Video -> MlIcon.Video
        EffectKind.Sound -> MlIcon.Music
    }

@Preview(name = "PR-14 효과 관리", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Pr14Preview() = MotionLayerTheme {
    EffectsContent(
        pcName = "강의실 PC",
        effects = listOf(
            PresentationEffect("logo", "팀 로고.png", EffectKind.Image, 1_200_000),
            PresentationEffect("demo", "시연 영상.mp4", EffectKind.Video, 38_000_000, uploadPercent = 64),
            PresentationEffect("clap", "박수 소리.mp3", EffectKind.Sound, 400_000),
        ),
        onAdd = {},
        onBack = {},
    )
}
