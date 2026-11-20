package com.uhi.motionlayer.ui.onboarding

import com.uhi.motionlayer.domain.model.AppPermission
import com.uhi.motionlayer.domain.model.PermissionStatus
import com.uhi.motionlayer.domain.model.isGranted

/** 첫 실행 안내의 단계. 화면이 나오는 순서대로이고, 화면 번호는 Figma의 ON-xx다. */
enum class OnboardingStep {
    /** ON-01 환영 */
    Welcome,

    /** ON-02 첫 제스처 체험 */
    Trial,

    /** ON-03 체험 성공 */
    Success,

    /** ON-04 기본 제스처 4가지 */
    Gestures,

    /** ON-05 상시 감지 켜기(알림 허용) */
    Notifications,

    /** ON-07 배터리 설정 안내 */
    Battery,

    /** ON-08 쓸 앱 고르기 */
    Apps,

    /** ON-09 알림 접근 권한 안내 */
    Access,

    /** ON-10 로그인(선택) */
    Login,

    /** ON-11 준비 완료 */
    Done,
}

/**
 * [step] 다음에 보여 줄 단계. 이미 허용된 권한을 알려 주는 단계(알림·배터리·알림 접근)와,
 * 이미 로그인한 경우의 로그인 단계(ON-10)는 건너뛴다.
 *
 * @param signedIn 이미 로그인한 계정이 있는지. 설정에서 먼저 로그인했다가 안내를 다시 보는 경우 등
 */
fun nextOnboardingStep(
    step: OnboardingStep,
    statuses: Map<AppPermission, PermissionStatus>,
    signedIn: Boolean = false,
): OnboardingStep = when (step) {
    OnboardingStep.Welcome -> OnboardingStep.Trial
    OnboardingStep.Trial -> OnboardingStep.Success
    OnboardingStep.Success -> OnboardingStep.Gestures
    OnboardingStep.Gestures -> firstNeededStep(OnboardingStep.Notifications, statuses, signedIn)
    OnboardingStep.Notifications -> firstNeededStep(OnboardingStep.Battery, statuses, signedIn)
    OnboardingStep.Battery -> OnboardingStep.Apps
    OnboardingStep.Apps -> firstNeededStep(OnboardingStep.Access, statuses, signedIn)
    OnboardingStep.Access -> firstNeededStep(OnboardingStep.Login, statuses, signedIn)
    OnboardingStep.Login, OnboardingStep.Done -> OnboardingStep.Done
}

/** [step]부터 따라가며 아직 보여 줘야 하는 첫 단계. 허용돼 있는 권한 단계와 이미 한 로그인 단계는 지나간다. */
private fun firstNeededStep(
    step: OnboardingStep,
    statuses: Map<AppPermission, PermissionStatus>,
    signedIn: Boolean,
): OnboardingStep = when {
    step == OnboardingStep.Notifications && statuses.isGranted(AppPermission.Notifications) ->
        firstNeededStep(OnboardingStep.Battery, statuses, signedIn)
    step == OnboardingStep.Battery && statuses.isGranted(AppPermission.BatteryOptimization) ->
        OnboardingStep.Apps
    step == OnboardingStep.Access && statuses.isGranted(AppPermission.NotificationAccess) ->
        firstNeededStep(OnboardingStep.Login, statuses, signedIn)
    step == OnboardingStep.Login && signedIn ->
        OnboardingStep.Done
    else -> step
}
