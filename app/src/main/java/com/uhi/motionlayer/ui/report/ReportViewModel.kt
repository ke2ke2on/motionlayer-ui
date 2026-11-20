package com.uhi.motionlayer.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.ReportRange
import com.uhi.motionlayer.domain.model.UsageReport
import com.uhi.motionlayer.domain.source.CustomGestureLibrary
import com.uhi.motionlayer.domain.source.UsageStats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * 기록과 리포트(ST-03)와 감도 낮추기 제안(ST-04). 고른 기간이 바뀌면 그 기간의 리포트로 바꿔 보여 준다. 기본은 7일.
 * 제안 시트에서 고른 감도는 나만의 제스처 목록([CustomGestureLibrary])에 저장한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModel(
    private val stats: UsageStats,
    private val customLibrary: CustomGestureLibrary,
) : ViewModel() {
    private val _range = MutableStateFlow(ReportRange.Week)
    val range: StateFlow<ReportRange> = _range.asStateFlow()

    val report: StateFlow<UsageReport> = _range
        .flatMapLatest { stats.report(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = stats.report(_range.value).value,
        )

    fun select(range: ReportRange) {
        _range.value = range
    }

    /** 제안 시트에서 "낮추기"를 눌렀다. 고른 감도를 그 제스처에 저장한다. */
    fun onLowerSensitivity(gestureId: String, sensitivity: Float) = customLibrary.setSensitivity(gestureId, sensitivity)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ReportViewModel(stats = AppContainer.usageStats, customLibrary = AppContainer.customGestureLibrary)
            }
        }
    }
}
