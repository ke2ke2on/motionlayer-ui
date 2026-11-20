package com.uhi.motionlayer.ui.components

import androidx.annotation.DrawableRes
import com.uhi.motionlayer.tv.R

/**
 * TV 화면이 쓰는 아이콘. 이름은 폰 앱의 `MlIcon`·Figma `icon/xxx`와 같다.
 * 그림 파일은 폰 앱의 `res/drawable/ic_*.xml`을 빌드할 때 가져온다(`build.gradle.kts`의 `sharedIcons`).
 * 아이콘을 더하려면 여기와 `sharedIcons` 두 곳에 같은 이름을 적는다. 그리는 코드(`MlIconView`)는 폰 앱 것을 그대로 쓴다.
 */
enum class MlIcon(@param:DrawableRes val res: Int) {
    Cloud(R.drawable.ic_cloud),
    Gesture(R.drawable.ic_gesture),
    Image(R.drawable.ic_image),
    User(R.drawable.ic_user),
    Users(R.drawable.ic_users),
}
