package com.uhi.motionlayer.ui.devices

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.NearbyDevice
import com.uhi.motionlayer.domain.source.NearbyDevices
import com.uhi.motionlayer.ui.components.MlBanner
import com.uhi.motionlayer.ui.components.MlBannerTone
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionBorder
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import kotlinx.coroutines.flow.StateFlow

/** 주변 기기(DV-06)가 쓰는 ViewModel. 화면이 열려 있는 동안만 주변을 찾는다. */
class NearbyDevicesViewModel(nearby: NearbyDevices) : ViewModel() {
    val devices: StateFlow<List<NearbyDevice>> = nearby.devices

    companion object {
        val Factory = viewModelFactory {
            initializer { NearbyDevicesViewModel(nearby = AppContainer.nearbyDevices) }
        }
    }
}

/** "같은 와이파이에서 찾는 중" 앞의 작은 고리(Figma: 14×14, 선 2, 270° 호). 찾는 중임이 보이게 천천히 돈다. */
private val SearchingDotSize = 14.dp
private val SearchingDotStroke = MotionBorder.thick
private const val SEARCHING_SWEEP_DEGREES = 270f
private const val SEARCHING_ROTATION_MILLIS = 1_000

/**
 * 주변 기기(Figma DV-06). 다른 사람의 기기에 손님으로 연결하려고 고르는 목록이다. 주인이 손님 연결을 허용한 기기만 나오고,
 * 하나를 고르면 그 기기에 뜬 QR을 찍어 이번만 연결한다(그 자리에 있었다는 증명).
 */
@Composable
fun NearbyDevicesRoute(
    onSelect: (NearbyDevice) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NearbyDevicesViewModel = viewModel(factory = NearbyDevicesViewModel.Factory),
) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    NearbyDevicesContent(devices = devices, onSelect = onSelect, onBack = onBack, modifier = modifier)
}

/** 주변 기기 목록. 값을 받아 그리기만 한다. 찾는 중 표시는 이 화면이 열려 있는 동안 계속 보인다. */
@Composable
fun NearbyDevicesContent(
    devices: List<NearbyDevice>,
    onSelect: (NearbyDevice) -> Unit,
    onBack: () -> Unit,
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
        MlTopBar(title = stringResource(R.string.dv06_title), leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MotionSpacing.screenHorizontal,
                        end = MotionSpacing.screenHorizontal,
                        top = MotionSpacing.s4,
                        bottom = MotionSpacing.s12,
                    ),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s12),
            ) {
                Text(text = stringResource(R.string.dv06_description), style = type.body2, color = colors.textSecondary)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SearchingDot()
                    Text(text = stringResource(R.string.dv06_searching), style = type.caption1Strong, color = colors.brandPrimary)
                }
            }

            devices.forEach { device ->
                MlListRow(
                    title = device.name,
                    subtitle = if (device.owner != null) {
                        stringResource(R.string.dv06_owner_subtitle, device.owner)
                    } else {
                        stringResource(R.string.dv06_public_subtitle)
                    },
                    leading = MlListRowLeading.Icon(device.kind.icon),
                    trailing = MlListRowTrailing.Chevron,
                    onClick = { onSelect(device) },
                )
            }

            MlBanner(
                message = stringResource(R.string.dv06_banner),
                tone = MlBannerTone.Neutral,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = MotionSpacing.screenHorizontal, end = MotionSpacing.screenHorizontal, top = MotionSpacing.s16),
            )
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

/** 파란 270° 호가 도는 작은 고리. */
@Composable
private fun SearchingDot() {
    val color = MotionTheme.colors.brandPrimary
    val transition = rememberInfiniteTransition(label = "searching")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = SEARCHING_ROTATION_MILLIS, easing = LinearEasing)),
        label = "searchingRotation",
    )
    Canvas(modifier = Modifier.size(SearchingDotSize).graphicsLayer { rotationZ = rotation }) {
        val stroke = SearchingDotStroke.toPx()
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = SEARCHING_SWEEP_DEGREES,
            useCenter = false,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(size.width - stroke, size.height - stroke),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}

@Preview(name = "DV-06 주변 기기", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Dv06Preview() = MotionLayerTheme {
    NearbyDevicesContent(devices = SampleData.nearbyDevices, onSelect = {}, onBack = {})
}
