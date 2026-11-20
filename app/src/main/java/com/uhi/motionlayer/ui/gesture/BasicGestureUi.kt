package com.uhi.motionlayer.ui.gesture

import androidx.annotation.StringRes
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.BasicGesture
import com.uhi.motionlayer.ui.components.MlIcon

/** 기본 제스처 4가지를 화면에 보여 줄 때 필요한 이름·아이콘·설명. 목록과 연습 화면이 같이 쓴다. */
@get:StringRes
val BasicGesture.nameRes: Int
    get() = when (this) {
        BasicGesture.Swipe -> R.string.gesture_name_swipe
        BasicGesture.DoubleTap -> R.string.gesture_name_double_tap
        BasicGesture.Tilt -> R.string.gesture_name_tilt
        BasicGesture.Shake -> R.string.gesture_name_shake
    }

/** 이 제스처가 하는 일("이전 · 다음"). */
@get:StringRes
val BasicGesture.doesRes: Int
    get() = when (this) {
        BasicGesture.Swipe -> R.string.gesture_does_swipe
        BasicGesture.DoubleTap -> R.string.gesture_does_double_tap
        BasicGesture.Tilt -> R.string.gesture_does_tilt
        BasicGesture.Shake -> R.string.gesture_does_shake
    }

/** 연습 모드의 작은 선택 칩 글자("휙"). */
@get:StringRes
val BasicGesture.chipRes: Int
    get() = when (this) {
        BasicGesture.Swipe -> R.string.gs09_chip_swipe
        BasicGesture.DoubleTap -> R.string.gs09_chip_double_tap
        BasicGesture.Tilt -> R.string.gs09_chip_tilt
        BasicGesture.Shake -> R.string.gs09_chip_shake
    }

/** 연습할 때 위에 보여 주는 안내 문구. 휙만 Figma 문구이고 나머지는 같은 말투로 정했다. */
@get:StringRes
val BasicGesture.promptRes: Int
    get() = when (this) {
        BasicGesture.Swipe -> R.string.gs09_prompt_swipe
        BasicGesture.DoubleTap -> R.string.gs09_prompt_double_tap
        BasicGesture.Tilt -> R.string.gs09_prompt_tilt
        BasicGesture.Shake -> R.string.gs09_prompt_shake
    }

val BasicGesture.icon: MlIcon
    get() = when (this) {
        BasicGesture.Swipe -> MlIcon.Swipe
        BasicGesture.DoubleTap -> MlIcon.Tap
        BasicGesture.Tilt -> MlIcon.Tilt
        BasicGesture.Shake -> MlIcon.Shake
    }
