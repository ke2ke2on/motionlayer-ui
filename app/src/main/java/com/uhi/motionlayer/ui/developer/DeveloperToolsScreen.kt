package com.uhi.motionlayer.ui.developer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.DiagnosticsSnapshot
import com.uhi.motionlayer.domain.model.GestureThresholds
import com.uhi.motionlayer.domain.model.SensorSample
import com.uhi.motionlayer.ui.components.MlBadge
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.components.MlTone
import com.uhi.motionlayer.ui.components.MlTopBar
import com.uhi.motionlayer.ui.components.MlTopBarLeading
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.formatDecimal

/** 설정에서 열리는 개발자 도구(Figma ST-08). 디버그 빌드에서만 들어올 수 있다. */
@Composable
fun DeveloperToolsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeveloperToolsViewModel = viewModel(factory = DeveloperToolsViewModel.Factory),
) {
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    DeveloperToolsContent(snapshot = snapshot, onBack = onBack, modifier = modifier)
}

/**
 * 개발자 도구. 받은 값을 그대로 보여 주기만 한다. 값이 없으면 "—"로 둔다.
 *
 * Figma 목록의 오른쪽 화살표는 눌러서 갈 곳(기준값 조정 화면)이 없어서 두지 않는다.
 */
@Composable
fun DeveloperToolsContent(
    snapshot: DiagnosticsSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val none = stringResource(R.string.st08_value_none)
    val thresholds = snapshot.thresholds

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MotionTheme.colors.bgBase)
            .statusBarsPadding(),
    ) {
        MlTopBar(title = stringResource(R.string.st08_title), leading = MlTopBarLeading.Back, onLeadingClick = onBack)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            MlBadge(
                text = stringResource(R.string.st08_badge),
                tone = MlTone.Orange,
                modifier = Modifier.padding(start = MotionSpacing.screenHorizontal, top = MotionSpacing.s4),
            )

            MlSectionHeader(title = stringResource(R.string.st08_sensor_header))
            SensorCard(
                sensor = snapshot.sensor,
                modifier = Modifier.padding(horizontal = MotionSpacing.screenHorizontal),
            )

            MlSectionHeader(title = stringResource(R.string.st08_latency_header))
            ValueRow(
                title = stringResource(R.string.st08_last_command),
                value = snapshot.lastCommandMs?.let { stringResource(R.string.st08_value_milliseconds, it) } ?: none,
            )
            ValueRow(
                title = stringResource(R.string.st08_average_command),
                value = snapshot.averageCommandMs?.let { stringResource(R.string.st08_value_milliseconds, it) } ?: none,
            )
            ValueRow(
                title = stringResource(R.string.st08_battery),
                value = snapshot.batteryPercentPerHour
                    ?.let { stringResource(R.string.st08_value_percent, formatDecimal(it)) } ?: none,
            )

            MlSectionHeader(title = stringResource(R.string.st08_thresholds_header))
            ValueRow(
                title = stringResource(R.string.st08_swipe_peak),
                value = stringResource(R.string.st08_value_swipe_peak, thresholds.swipePeakMps2),
            )
            ValueRow(
                title = stringResource(R.string.st08_double_tap_interval),
                value = stringResource(R.string.st08_value_double_tap, thresholds.doubleTapMinMs, thresholds.doubleTapMaxMs),
            )
            ValueRow(
                title = stringResource(R.string.st08_tilt),
                value = stringResource(R.string.st08_value_tilt, thresholds.tiltDegrees, formatDecimal(thresholds.tiltHoldSeconds)),
            )
            ValueRow(
                title = stringResource(R.string.st08_shake),
                value = stringResource(
                    R.string.st08_value_shake,
                    formatDecimal(thresholds.shakeWithinSeconds),
                    thresholds.shakeCount,
                ),
            )
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun ValueRow(title: String, value: String) {
    MlListRow(title = title, trailing = MlListRowTrailing.Value(value, chevron = false))
}

/** 가속도(ax·ay·az)와 자이로(gx·gy·gz)를 세 칸씩 두 줄로 놓은 회색 판. 안쪽 여백 16, 줄 사이 10, 칸 사이 8. */
@Composable
private fun SensorCard(sensor: SensorSample, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MotionRadius.r16))
            .background(MotionTheme.colors.bgGrouped)
            .padding(MotionSpacing.s16),
        verticalArrangement = Arrangement.spacedBy(MotionSpacing.s10),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
            SensorCell(label = "ax", value = sensor.ax, modifier = Modifier.weight(1f))
            SensorCell(label = "ay", value = sensor.ay, modifier = Modifier.weight(1f))
            SensorCell(label = "az", value = sensor.az, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s8)) {
            SensorCell(label = "gx", value = sensor.gx, modifier = Modifier.weight(1f))
            SensorCell(label = "gy", value = sensor.gy, modifier = Modifier.weight(1f))
            SensorCell(label = "gz", value = sensor.gz, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SensorCell(label: String, value: Float, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(modifier = modifier) {
        Text(text = label, style = type.caption2, color = colors.textSecondary)
        Text(text = formatDecimal(value, digits = 2), style = type.body1Strong, color = colors.textPrimary)
    }
}

@Preview(name = "ST-08 개발자 도구", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun St08Preview() = MotionLayerTheme {
    DeveloperToolsContent(
        snapshot = DiagnosticsSnapshot(
            sensor = SensorSample(ax = 0.42f, ay = 9.71f, az = 0.88f, gx = 0.01f, gy = 0.03f, gz = 0.00f),
            lastCommandMs = 12,
            averageCommandMs = 18,
            batteryPercentPerHour = 1.6f,
            thresholds = GestureThresholds(
                swipePeakMps2 = 14,
                doubleTapMinMs = 120,
                doubleTapMaxMs = 400,
                tiltDegrees = 25,
                tiltHoldSeconds = 0.4f,
                shakeCount = 3,
                shakeWithinSeconds = 0.6f,
            ),
        ),
        onBack = {},
    )
}
