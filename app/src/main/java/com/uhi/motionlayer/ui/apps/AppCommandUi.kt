package com.uhi.motionlayer.ui.apps

import androidx.annotation.StringRes
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.AppCategory
import com.uhi.motionlayer.domain.model.GestureCommand
import com.uhi.motionlayer.domain.model.GestureType
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlTone

/*
 * 앱별 동작 설정(AP-02·03)이 제스처와 동작을 화면에 보여 줄 때 쓰는 이름·아이콘·색.
 * 동작 이름은 앱 종류에 따라 다르다: 같은 "다음"이 음악 앱에서는 "다음 곡", 영상 앱에서는 "다음 영상"이다.
 */

@get:StringRes
val GestureType.nameRes: Int
    get() = when (this) {
        GestureType.SwipeRight -> R.string.ap02_gesture_swipe_right
        GestureType.SwipeLeft -> R.string.ap02_gesture_swipe_left
        GestureType.DoubleTap -> R.string.gesture_name_double_tap
        GestureType.Tilt -> R.string.gesture_name_tilt
        GestureType.Shake -> R.string.gesture_name_shake
        GestureType.PressAndRotate -> R.string.gesture_press_rotate
    }

val GestureType.icon: MlIcon
    get() = when (this) {
        GestureType.SwipeRight, GestureType.SwipeLeft -> MlIcon.Swipe
        GestureType.DoubleTap -> MlIcon.Tap
        GestureType.Tilt -> MlIcon.Tilt
        GestureType.Shake -> MlIcon.Shake
        GestureType.PressAndRotate -> MlIcon.Pointer
    }

/** 손으로 하는 기본 동작은 파랑, 되돌리기(흔들기)처럼 따로 쓰는 동작은 회색(Figma AP-02). */
val GestureType.tone: MlTone
    get() = when (this) {
        GestureType.Shake, GestureType.PressAndRotate -> MlTone.Gray
        else -> MlTone.Blue
    }

@StringRes
fun GestureCommand.labelRes(category: AppCategory): Int = when (this) {
    GestureCommand.NextTrack ->
        if (category == AppCategory.Video) R.string.app_command_next_video else R.string.app_command_next_music
    GestureCommand.PreviousTrack ->
        if (category == AppCategory.Video) R.string.app_command_previous_video else R.string.app_command_previous_music
    GestureCommand.PlayPause -> R.string.app_command_play_pause
    GestureCommand.Volume -> R.string.app_command_volume
    GestureCommand.SkipForward -> R.string.app_command_skip_forward
    GestureCommand.SkipBack -> R.string.app_command_skip_back
    GestureCommand.Undo -> R.string.app_command_undo
    GestureCommand.NextSlide -> R.string.app_command_next_slide
    GestureCommand.PreviousSlide -> R.string.app_command_previous_slide
    GestureCommand.Pointer -> R.string.app_command_pointer
}

@get:StringRes
val AppCategory.labelRes: Int
    get() = when (this) {
        AppCategory.Music -> R.string.app_category_music
        AppCategory.Video -> R.string.app_category_video
        AppCategory.Other -> R.string.app_category_other
    }
