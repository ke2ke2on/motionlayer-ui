package com.uhi.motionlayer.ui.components

/**
 * PC 화면이 쓰는 아이콘. 이름은 폰 앱의 `MlIcon`·Figma `icon/xxx`와 같다.
 * 그림 파일은 폰 앱의 `res/drawable/ic_*.xml`을 빌드할 때 가져온다(`build.gradle.kts`의 `sharedIcons`).
 * 아이콘을 더하려면 여기와 `sharedIcons` 두 곳에 같은 이름을 적는다.
 */
enum class MlIcon(val file: String) {
    Gesture("ic_gesture"),
    Image("ic_image"),
    Presentation("ic_presentation"),
    Smartphone("ic_smartphone"),
}
