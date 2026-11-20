package com.uhi.motionlayer.ui.onboarding

import com.uhi.motionlayer.domain.model.AppPermission
import com.uhi.motionlayer.domain.model.PermissionStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingFlowTest {
    private fun statuses(vararg granted: AppPermission): Map<AppPermission, PermissionStatus> =
        AppPermission.entries.associateWith {
            if (it in granted) PermissionStatus.Granted else PermissionStatus.NotRequested
        }

    /** 처음 켰을 때처럼 아무 권한도 없고 로그인도 안 했으면 모든 안내가 차례대로 나온다. */
    @Test
    fun noPermissions_showsEveryStepInOrder() {
        val none = statuses()
        val order = generateSequence(OnboardingStep.Welcome) { step ->
            nextOnboardingStep(step, none).takeIf { it != step }
        }.toList()

        assertEquals(
            listOf(
                OnboardingStep.Welcome,
                OnboardingStep.Trial,
                OnboardingStep.Success,
                OnboardingStep.Gestures,
                OnboardingStep.Notifications,
                OnboardingStep.Battery,
                OnboardingStep.Apps,
                OnboardingStep.Access,
                OnboardingStep.Login,
                OnboardingStep.Done,
            ),
            order,
        )
    }

    @Test
    fun grantedNotifications_skipsNotificationStep() {
        val s = statuses(AppPermission.Notifications)
        assertEquals(OnboardingStep.Battery, nextOnboardingStep(OnboardingStep.Gestures, s))
    }

    @Test
    fun grantedNotificationsAndBattery_skipsToApps() {
        val s = statuses(AppPermission.Notifications, AppPermission.BatteryOptimization)
        assertEquals(OnboardingStep.Apps, nextOnboardingStep(OnboardingStep.Gestures, s))
    }

    @Test
    fun grantedBattery_isSkippedAfterNotifications() {
        val s = statuses(AppPermission.BatteryOptimization)
        assertEquals(OnboardingStep.Apps, nextOnboardingStep(OnboardingStep.Notifications, s))
    }

    /** 알림 접근이 이미 허용돼 있으면 ON-09는 건너뛰고 로그인(ON-10)으로 간다. */
    @Test
    fun grantedAccess_skipsAccessStep() {
        val s = statuses(AppPermission.NotificationAccess)
        assertEquals(OnboardingStep.Login, nextOnboardingStep(OnboardingStep.Apps, s))
    }

    @Test
    fun allGranted_goesStraightThroughTheGuides() {
        val all = statuses(*AppPermission.entries.toTypedArray())
        assertEquals(OnboardingStep.Apps, nextOnboardingStep(OnboardingStep.Gestures, all))
        assertEquals(OnboardingStep.Login, nextOnboardingStep(OnboardingStep.Apps, all))
    }

    /** 거부했거나 설정에서 끈 권한도 허용된 것이 아니므로 안내를 보여 준다. */
    @Test
    fun deniedPermission_isStillShown() {
        val denied = AppPermission.entries.associateWith { PermissionStatus.Denied }
        assertEquals(OnboardingStep.Notifications, nextOnboardingStep(OnboardingStep.Gestures, denied))
        assertEquals(OnboardingStep.Access, nextOnboardingStep(OnboardingStep.Apps, denied))
    }

    /** 알림 접근 안내(ON-09) 다음에는 로그인(ON-10), 로그인 다음에는 준비 완료(ON-11)다. */
    @Test
    fun loginStep_comesAfterAccessAndBeforeDone() {
        val none = statuses()
        assertEquals(OnboardingStep.Login, nextOnboardingStep(OnboardingStep.Access, none))
        assertEquals(OnboardingStep.Done, nextOnboardingStep(OnboardingStep.Login, none))
    }

    /** 이미 로그인했으면(설정에서 먼저 했거나 안내를 다시 보는 경우) 로그인 단계는 건너뛴다. */
    @Test
    fun signedIn_skipsLoginStep() {
        val none = statuses()
        assertEquals(OnboardingStep.Done, nextOnboardingStep(OnboardingStep.Access, none, signedIn = true))
        assertEquals(OnboardingStep.Access, nextOnboardingStep(OnboardingStep.Apps, none, signedIn = true))

        val accessGranted = statuses(AppPermission.NotificationAccess)
        assertEquals(OnboardingStep.Done, nextOnboardingStep(OnboardingStep.Apps, accessGranted, signedIn = true))
    }

    /** 로그인하지 않은 채로 "건너뛰기"를 눌러도 다음은 준비 완료다(로그인은 선택). */
    @Test
    fun skippingLogin_stillReachesDone() {
        assertEquals(OnboardingStep.Done, nextOnboardingStep(OnboardingStep.Login, statuses(), signedIn = false))
    }
}
