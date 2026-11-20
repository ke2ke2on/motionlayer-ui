package com.uhi.motionlayer.domain.model

/**
 * 제스처 감지 서비스의 지금 상태(HM-01·02·07·08). 사용자가 켠 것(`DetectionController.enabled`)과는 따로,
 * 서비스가 실제로 돌고 있는지를 알려 준다. 제스처 인식(A)이 채운다.
 */
enum class DetectionStatus {
    /** 앱을 켠 직후 상태를 확인하는 중(HM-07). 콜드 스타트 2초 안에 끝난다(NFR-12). */
    Checking,

    /** 사용자가 정한 대로 돌고 있다. 켜 두었으면 감지 중, 꺼 두었으면 꺼짐(HM-01·02). */
    Normal,

    /**
     * 감지는 켜져 있지만 걷는 중이라 손 제스처를 잠시 멈춘 상태(HM-04, 설정의 "걷는 중엔 손 제스처 멈추기"). 주머니 두드림은 계속 받는다.
     * 걷고 있는지 알아보는 것은 제스처 인식(A)이다.
     */
    WalkingPaused,

    /**
     * 켜 두었는데 서비스가 멈췄다(HM-08). 배터리 최적화 등으로 시스템이 앱을 끈 경우다.
     * 감지를 다시 켜면(`setEnabled(true)`) 서비스가 다시 시작되고 [Normal]로 돌아온다.
     */
    Stopped,
}
