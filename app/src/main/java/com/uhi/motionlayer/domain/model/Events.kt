package com.uhi.motionlayer.domain.model

/**
 * 앱 화면이 켜져 있을 때 토스트(SY-03)로 알려 주는 일 여섯 가지. 화면이 꺼져 있을 때의 일은 알림이 맡는다.
 * 제스처 실행·되돌림·실패는 제스처 인식(A)과 시스템 연동(B)이, PC 연결과 조작 대상 바뀜은 PC 연결(C)이 알린다.
 * 연속 실패 때 연습을 권하는 시트(SY-05)는 보고서·Figma에서 보류된 화면이라 두지 않는다.
 */
sealed interface AppEvent {
    /** 제스처가 명령을 실행했다("다음 곡으로 넘겼어요"). */
    data class CommandExecuted(val command: GestureCommand) : AppEvent

    /** 방금 동작을 되돌렸다. */
    data object CommandUndone : AppEvent

    /**
     * 되돌리려 했지만 되돌릴 동작이 없다("10초가 지나 되돌릴 동작이 없어요"). 세게 흔들기는 마지막 명령만 10초 안에 되돌린다.
     * 실행 실패([CommandFailed])와 따로, 전용 토스트로 알린다.
     */
    data object NothingToUndo : AppEvent

    /**
     * 명령을 실행하지 못했다.
     *
     * @param reason "거실 TV가 꺼져 있어"처럼 이유를 말하는 구절. 없으면 이유 없이 "실행하지 못했어요"만 알린다
     */
    data class CommandFailed(val reason: String? = null) : AppEvent

    /** PC와 연결됐다. */
    data class PcConnected(val deviceName: String) : AppEvent

    /** 조작하는 대상이 바뀌었다("이제 강의실 PC를 조작해요"). 토스트의 "되돌리기"로 되돌릴 수 있다. */
    data class ControlTargetChanged(val targetName: String) : AppEvent
}

