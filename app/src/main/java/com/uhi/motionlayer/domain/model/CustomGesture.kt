package com.uhi.motionlayer.domain.model

import java.time.LocalDate

/**
 * 나만의 제스처 하나(GS-01 줄, GS-11 상세). 만들고 세는 것은 제스처 인식(A)이다.
 *
 * @param successRatePercent 최근 7일 인식률(%)
 * @param undoneCount 최근 7일 동안 이 제스처로 한 동작을 되돌린 횟수
 * @param attemptCount 최근 7일 동안 이 제스처가 실행된 횟수("12번 중 4번을 되돌렸어요"의 12)
 * @param sensitivity 감도. 0.0(둔감하게)~1.0(민감하게)
 * @param syncPending 계정에 아직 올라가지 않았다(GS-15). 인터넷이 없어 이 폰에만 저장된 상태이고, 그래도 이 폰에서는 바로 쓸 수 있다
 *   (분석서 표 4-2). 온라인이 되면 저절로 올라가며 false가 된다
 */
data class CustomGesture(
    val id: String,
    val name: String,
    val createdOn: LocalDate,
    val successRatePercent: Int,
    val undoneCount: Int,
    val attemptCount: Int,
    val sensitivity: Float,
    val syncPending: Boolean = false,
)

/** 나만의 제스처를 쓰는 앱 하나와 그 앱에서 하는 동작(GS-11 "쓰는 곳"). */
data class CustomGestureUse(val app: ControllableApp, val command: GestureCommand)

/**
 * 나만의 제스처 [gestureId]를 쓰는 앱들과 그 앱에서 하는 동작. [apps]의 순서를 따르고, 쓰는 앱이 없으면 빈 목록이다.
 *
 * @param commands 앱 id → (제스처 id → 동작). [com.uhi.motionlayer.domain.source.AppCatalog.customGestureCommands]의 값
 */
fun customGestureUses(
    apps: List<ControllableApp>,
    commands: Map<String, Map<String, GestureCommand>>,
    gestureId: String,
): List<CustomGestureUse> =
    apps.mapNotNull { app -> commands[app.id]?.get(gestureId)?.let { CustomGestureUse(app, it) } }

/** 같은 동작을 녹화하는 횟수(GS-04 "5번 반복"). */
const val CUSTOM_RECORDING_TAKES = 5

/** 녹화 뒤 잘 인식되는지 확인하는 횟수와, 저장에 필요한 인식 횟수(GS-07 "3번 해 주세요 · 2번 이상 인식되면 저장"). */
const val CUSTOM_VERIFICATION_TRIES = 3
const val CUSTOM_VERIFICATION_REQUIRED = 2

/** 나만의 제스처를 만드는 흐름의 단계. 화면 번호는 Figma GS-xx다. */
enum class RecordingStep {
    /** GS-04 같은 동작을 5번 녹화하는 중 */
    Recording,

    /** GS-05 방금 동작이 이전과 너무 달라 저장하지 않았다 */
    Mismatch,

    /** GS-06 이미 있는 제스처와 너무 비슷해 저장할 수 없다 */
    TooSimilar,

    /**
     * GS-14 5번 녹화를 마치고 공통 패턴(템플릿)을 만드는 1~2초. 끝나면 [Verifying]으로 가고,
     * 동작이 서로 너무 달랐거나 기존 제스처와 겹치면 [Mismatch]·[TooSimilar]로 간다.
     */
    Analyzing,

    /** GS-07 만든 동작이 잘 인식되는지 3번 확인하는 중 */
    Verifying,

    /** GS-08 저장했다 */
    Done,
}

/** 확인(GS-07) 한 번의 결과. */
enum class VerificationResult { Recognized, Missed }

/**
 * 이미 있는 제스처와 너무 비슷할 때(GS-06) 비교하는 상대.
 *
 * @param isBasic 기본 제스처면 true, 내가 먼저 만든 제스처면 false(GS-06이 "(기본)"과 "(내 제스처)" 중 하나를 붙인다)
 */
data class SimilarGesture(
    val name: String,
    val curve: List<TrajectoryPoint>,
    val similarityPercent: Int,
    val isBasic: Boolean = true,
)

/**
 * 나만의 제스처를 만드는 중인 모습 전부(GS-04~08). 값을 만드는 것은 제스처 인식(A)이다.
 * 화면은 [step]에 맞는 모양을 그리고, 곡선은 0~1 좌표([TrajectoryPoint])라 크기에 맞춰 그린다.
 *
 * @param takes 지금까지 저장한 녹화 횟수(0~[CUSTOM_RECORDING_TAKES])
 * @param live 방금 한 동작의 곡선(파란 선, 달라요이면 주황, 겹침이면 파랑)
 * @param previous 이전에 저장한 동작들의 곡선(회색 선, 최대 두 개)
 * @param matchPercent 방금 동작이 이전 동작과 얼마나 같은지(%). 세 번째 녹화부터 값이 있다
 * @param similarTo [RecordingStep.TooSimilar]일 때 비슷한 기존 제스처
 * @param verification 확인한 결과들(0~[CUSTOM_VERIFICATION_TRIES]개)
 * @param saved [RecordingStep.Done]일 때 저장된 제스처
 */
data class RecordingState(
    val name: String,
    val step: RecordingStep,
    val takes: Int = 0,
    val live: List<TrajectoryPoint> = emptyList(),
    val previous: List<List<TrajectoryPoint>> = emptyList(),
    val matchPercent: Int? = null,
    val similarTo: SimilarGesture? = null,
    val verification: List<VerificationResult> = emptyList(),
    val saved: CustomGesture? = null,
)
