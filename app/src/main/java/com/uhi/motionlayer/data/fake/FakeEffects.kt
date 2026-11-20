package com.uhi.motionlayer.data.fake

import com.uhi.motionlayer.domain.model.EffectKind
import com.uhi.motionlayer.domain.model.PresentationEffect
import com.uhi.motionlayer.domain.source.EffectLibrary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 가짜 효과 목록. 처음 모습은 Figma PR-14와 같다: 팀 로고(보냄)·시연 영상(64% 보내는 중)·박수 소리(보냄).
 * 시연 영상의 진행률은 그대로 멈춰 있다. "효과 추가"를 누르면 파일을 고르는 창 없이 가짜 파일 하나가
 * 약 3초 동안 0%에서 100%까지 올라가며 "보냄"이 된다.
 */
class FakeEffectLibrary(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : EffectLibrary {
    private val _effects = MutableStateFlow(
        listOf(
            PresentationEffect("logo", "팀 로고.png", EffectKind.Image, sizeBytes = 1_200_000),
            PresentationEffect("demo", "시연 영상.mp4", EffectKind.Video, sizeBytes = 38_000_000, uploadPercent = 64),
            PresentationEffect("clap", "박수 소리.mp3", EffectKind.Sound, sizeBytes = 400_000),
        ),
    )
    override val effects: StateFlow<List<PresentationEffect>> = _effects.asStateFlow()

    private val samples = listOf(
        PresentationEffect("", "발표 배경.png", EffectKind.Image, sizeBytes = 2_100_000),
        PresentationEffect("", "효과음.mp3", EffectKind.Sound, sizeBytes = 800_000),
        PresentationEffect("", "소개 영상.mp4", EffectKind.Video, sizeBytes = 24_000_000),
    )
    private var added = 0

    override fun add() {
        val sample = samples[added % samples.size]
        val effect = sample.copy(id = "added-${added++}", uploadPercent = 0)
        _effects.update { it + effect }
        scope.launch {
            for (percent in 5..100 step 5) {
                delay(150)
                _effects.update { list ->
                    list.map { if (it.id == effect.id) it.copy(uploadPercent = if (percent >= 100) null else percent) else it }
                }
            }
        }
    }
}
