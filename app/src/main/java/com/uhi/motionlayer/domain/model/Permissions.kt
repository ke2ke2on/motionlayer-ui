package com.uhi.motionlayer.domain.model

/**
 * 앱이 쓰는 안드로이드 권한·설정 한 가지(ST-07). 근처 기기(DV)는 해당 기능이 이번 범위 밖이라 넣지 않았다.
 */
enum class AppPermission {
    /** 알림 허용. "감지 중" 알림을 보여 준다(ON-05). */
    Notifications,

    /** 알림 접근. 지금 재생 중인 앱을 알아낸다(ON-09). 설정 앱에서만 켤 수 있다. */
    NotificationAccess,

    /** 배터리 최적화 제외. 화면이 꺼져도 감지가 이어지게 한다(ON-07). 설정 앱에서만 바꿀 수 있다. */
    BatteryOptimization,

    /** 카메라. PC 연결의 QR을 찍는 데 쓴다(PR-02). */
    Camera,

    /** 전화. 전화 제어(AP-04)가 전화를 받고 끊는 데 쓴다. 전화 제어를 켤 때 요청한다. */
    Phone,
}

/** 권한 하나의 지금 상태. 아직 묻지 않은 것과 허용하지 않은 것은 다르게 보여 준다(ST-07). */
enum class PermissionStatus {
    Granted,

    /** 사용자가 허용하지 않았다(거부했거나 설정에서 껐다). */
    Denied,

    /** 아직 한 번도 묻지 않았다. */
    NotRequested,
}

/** [permission]이 허용돼 있는지. */
fun Map<AppPermission, PermissionStatus>.isGranted(permission: AppPermission): Boolean =
    this[permission] == PermissionStatus.Granted
