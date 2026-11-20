package com.uhi.motionlayer.ui.devices

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.DeviceKind
import com.uhi.motionlayer.domain.model.DirectionCandidate
import com.uhi.motionlayer.domain.model.DirectionPick
import com.uhi.motionlayer.domain.source.DirectionPicker
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.theme.MotionShadows
import com.uhi.motionlayer.ui.theme.motionShadow
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.flow.StateFlow

/** 방향으로 기기 고르기(DV-07)가 쓰는 ViewModel. 후보와 폰이 향한 기기를 받아 올리고, 고르는 일을 전달한다. */
class DirectionPickerViewModel(private val picker: DirectionPicker) : ViewModel() {
    val candidates: StateFlow<List<DirectionCandidate>> = picker.candidates
    val facingId: StateFlow<String?> = picker.facingId

    fun face(deviceId: String) = picker.face(deviceId)

    fun pick(deviceId: String): DirectionPick = picker.pick(deviceId)

    companion object {
        val Factory = viewModelFactory {
            initializer { DirectionPickerViewModel(picker = AppContainer.directionPicker) }
        }
    }
}

/** 레이더 크기와 고리 지름(Figma DV-07: 300 · 200 · 110), 가운데 폰 원(44)과 그 안의 아이콘(22). */
private val RadarSize = 300.dp
private val RadarMiddleSize = 200.dp
private val RadarInnerSize = 110.dp
private val PhoneCircleSize = 44.dp
private val PhoneIconSize = MotionIconSize.s22

/** 기기 이름표(칩)의 아이콘 크기(18). */
private val ChipIconSize = MotionIconSize.s18

/** 폰이 향한 쪽을 파란 부채꼴로 보여 준다. Figma는 -2 ~ -1.14 라디안(약 49°)이고 농도는 눈으로 맞췄다. */
private const val CONE_SWEEP_DEGREES = 49.3f
private const val CONE_ALPHA = 0.14f

/** 위 제목과 레이더 사이(Figma Spacer 24), 레이더와 안내 글 사이(20), 안내 글과 배너 사이(12). */
private val HeadingGap = MotionSpacing.s24
private val RadarGap = MotionSpacing.s20

/**
 * 방향으로 기기 고르기(Figma DV-07). 폰을 기기 쪽으로 향하면 그쪽 기기가 파란 이름표로 강조되고, 이름을 누르면 강조가 바뀐다.
 * 앱은 후보만 보여 주고 **고르는 것은 사용자가 직접** 한다(자기장 간섭으로 실내에서는 방향이 틀릴 수 있다. FR-S-05):
 * 강조된 이름표를 한 번 더 누르면 그 기기를 고른다.
 *
 * @param onOpenRemote 켜진 TV를 골라 원격 조작 화면(DV-08)으로 가야 한다
 * @param onClose 닫기(X)를 눌렀거나, PC를 골라 조작 대상이 바뀌어 화면을 닫아야 한다
 */
@Composable
fun DirectionPickerRoute(
    onOpenRemote: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DirectionPickerViewModel = viewModel(factory = DirectionPickerViewModel.Factory),
) {
    val candidates by viewModel.candidates.collectAsStateWithLifecycle()
    val facingId by viewModel.facingId.collectAsStateWithLifecycle()

    DirectionPickerContent(
        candidates = candidates,
        facingId = facingId,
        onFace = viewModel::face,
        onPick = { id ->
            when (val result = viewModel.pick(id)) {
                is DirectionPick.OpenRemote -> onOpenRemote(result.deviceId)
                DirectionPick.TargetChanged -> onClose()
                DirectionPick.Unavailable -> Unit
            }
        },
        onClose = onClose,
        modifier = modifier,
    )
}

/** 방향 고르기 화면. 후보와 강조된 기기를 받아 그리기만 한다. */
@Composable
fun DirectionPickerContent(
    candidates: List<DirectionCandidate>,
    facingId: String?,
    onFace: (String) -> Unit,
    onPick: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = stringResource(R.string.dv07_title), leading = MlTopBarLeading.Close, onLeadingClick = onClose)

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, top = MotionSpacing.s4),
        ) {
            Text(text = stringResource(R.string.dv07_heading), style = type.title2, color = colors.textPrimary)
            Spacer(Modifier.height(HeadingGap))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Radar(candidates = candidates, facingId = facingId, onChipClick = { id -> if (id == facingId) onPick(id) else onFace(id) })
            }
            Spacer(Modifier.height(RadarGap))
            if (candidates.isEmpty()) {
                Text(text = stringResource(R.string.dv07_empty), style = type.body2, color = colors.textSecondary)
            } else {
                Text(text = stringResource(R.string.dv07_hint), style = type.body2, color = colors.textSecondary)
                Text(
                    text = stringResource(R.string.dv07_pick_hint),
                    style = type.caption1,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(top = MotionSpacing.s4),
                )
            }
            Spacer(Modifier.height(MotionSpacing.s12))
            MlBanner(
                message = stringResource(R.string.dv07_banner),
                tone = MlBannerTone.Neutral,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

/**
 * 레이더. 겹친 세 고리(바깥 `bg/grouped` · 가운데 `bg/subtle` · 안쪽 `bg/base`, 모두 `line/divider` 1dp 선) 한가운데에 파란 폰 원이 있고,
 * 후보 기기는 있는 방향·거리에 이름표로 놓인다. 강조된 기기 쪽으로 파란 부채꼴이 뻗는다.
 */
@Composable
private fun Radar(
    candidates: List<DirectionCandidate>,
    facingId: String?,
    onChipClick: (String) -> Unit,
) {
    val colors = MotionTheme.colors
    val description = stringResource(R.string.dv07_radar)
    val facing = candidates.firstOrNull { it.id == facingId }

    Box(modifier = Modifier.size(RadarSize).semantics { contentDescription = description }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val line = MotionBorder.thin.toPx()
            fun ring(diameter: Dp, fill: Color) {
                val d = diameter.toPx()
                drawCircle(color = fill, radius = d / 2f, center = center)
                drawCircle(color = colors.lineDivider, radius = d / 2f - line / 2f, center = center, style = Stroke(width = line))
            }
            ring(RadarSize, colors.bgGrouped)
            ring(RadarMiddleSize, colors.bgSubtle)
            ring(RadarInnerSize, colors.bgBase)
            if (facing != null) {
                // 부채꼴: 화면 각도는 3시 방향이 0°이고 시계 방향으로 커진다. 폰 앞쪽(위)이 0°인 방향각에서 90°를 뺀다.
                val sweep = CONE_SWEEP_DEGREES
                drawArc(
                    color = colors.brandPrimary.copy(alpha = CONE_ALPHA),
                    startAngle = facing.bearingDegrees - 90f - sweep / 2f,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(PhoneCircleSize)
                .clip(CircleShape)
                .background(colors.brandPrimary),
            contentAlignment = Alignment.Center,
        ) {
            MlIconView(icon = MlIcon.Smartphone, tint = colors.textInverse, size = PhoneIconSize)
        }

        candidates.forEach { candidate ->
            DeviceChip(
                candidate = candidate,
                highlighted = candidate.id == facingId,
                radarSize = RadarSize,
                onClick = { onChipClick(candidate.id) },
            )
        }
    }
}

/**
 * 기기 이름표 하나(Figma DV-07의 Device). 알약 모양(모서리 20)에 아이콘 18 + 이름이 들어 있고 그림자가 있다.
 * 강조된 것은 `brand/primary` 바탕에 흰 글자, 아닌 것은 `bg/base` 바탕에 `text/secondary` 글자다.
 */
@Composable
private fun DeviceChip(
    candidate: DirectionCandidate,
    highlighted: Boolean,
    radarSize: Dp,
    onClick: () -> Unit,
) {
    val colors = MotionTheme.colors
    val shape = RoundedCornerShape(MotionRadius.r20)
    val content = if (highlighted) colors.textInverse else colors.textSecondary
    val radius = radarSize / 2
    val bearing = Math.toRadians(candidate.bearingDegrees.toDouble())
    // 레이더 가운데를 기준으로 방향·거리만큼 떨어진 곳이 이름표의 가운데다.
    val centerX = radius + radius * candidate.distanceFraction * sin(bearing).toFloat()
    val centerY = radius - radius * candidate.distanceFraction * cos(bearing).toFloat()

    Row(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                layout(placeable.width, placeable.height) {
                    placeable.place(centerX.roundToPx() - placeable.width / 2, centerY.roundToPx() - placeable.height / 2)
                }
            }
            // Figma DV-07의 Device 이름표는 Shadow/Floating
            .motionShadow(MotionShadows.floating, shape)
            .clip(shape)
            .background(if (highlighted) colors.brandPrimary else colors.bgBase)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = MotionSpacing.s12, vertical = MotionSpacing.s8),
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s6),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MlIconView(icon = candidate.kind.icon, tint = content, size = ChipIconSize)
        Text(text = candidate.name, style = MotionTheme.typography.caption1Strong, color = content, maxLines = 1)
    }
}

/** Figma 예시 배치(거실 TV가 위쪽에서 강조, 윤의 MacBook은 왼쪽 아래, 강의실 PC는 오른쪽 아래). */
private val previewCandidates = listOf(
    DirectionCandidate("living_tv", "거실 TV", DeviceKind.Tv, 359.5f, 0.76f),
    DirectionCandidate("macbook", "윤의 MacBook", DeviceKind.Pc, 224f, 0.73f),
    DirectionCandidate("classroom", "강의실 PC", DeviceKind.Pc, 142f, 0.88f),
)

@Preview(name = "DV-07 방향으로 기기 고르기", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Dv07Preview() = MotionLayerTheme {
    DirectionPickerContent(
        candidates = previewCandidates,
        facingId = "living_tv",
        onFace = {},
        onPick = {},
        onClose = {},
    )
}
