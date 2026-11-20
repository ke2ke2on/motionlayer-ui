import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

// ---- 폰 앱의 디자인을 그대로 가져다 쓴다 ------------------------------------------------------------
// 색·글자·간격 토큰과 공통 부품의 원본은 폰 앱(app) 한 곳에만 둔다. 복사본을 저장소에 올리지 않고,
// 빌드할 때마다 아래 파일을 build/generated 로 가져와 같이 컴파일한다. 그래서 두 쪽의 값이 어긋날 수 없다.
// 조건: 여기 적는 파일에는 안드로이드 전용 코드(R.xxx, Context 등)가 없어야 한다.
val appMain = rootProject.layout.projectDirectory.dir("app/src/main")

val sharedUiFiles = listOf(
    "theme/Color.kt",
    "theme/Dimens.kt",
    "theme/Effects.kt",
    "theme/Theme.kt",
    "theme/Type.kt",
    "components/Text.kt",
    "components/MlBadge.kt",
    "components/MlToggle.kt",
    "components/MlButton.kt",
    "util/Format.kt",
    "util/KoreanParticle.kt",
)

// PC 화면이 쓰는 아이콘. `ui/components/MlIcon.kt`(PC판)의 목록과 같아야 한다(테스트가 확인한다).
val sharedIcons = listOf("gesture", "image", "presentation", "smartphone")

val syncSharedUi = tasks.register<Sync>("syncSharedUi") {
    from(appMain.dir("java/com/uhi/motionlayer/ui")) { include(sharedUiFiles) }
    into(layout.buildDirectory.dir("generated/sharedUi"))
}

val syncSharedRes = tasks.register<Sync>("syncSharedRes") {
    from(appMain.dir("res/font")) { into("font") }
    from(appMain.dir("res/drawable")) {
        include(sharedIcons.map { "ic_$it.xml" })
        into("drawable")
    }
    into(layout.buildDirectory.dir("generated/sharedRes"))
}

kotlin.sourceSets.named("main") { kotlin.srcDir(syncSharedUi) }
sourceSets.named("main") { resources.srcDir(syncSharedRes) }
// ---------------------------------------------------------------------------------------------------

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.desktop.material3)
    testImplementation(libs.junit)
    testImplementation(libs.compose.desktop.ui.test)
}

compose.desktop {
    application {
        mainClass = "com.uhi.motionlayer.pc.MainKt"
        // 시연용: `gradlew :pc-agent:run -Pdemo=PC-07`처럼 주면 창이 뜬 뒤 이름에 그 글자가 든 시연용 동작을 실행한다.
        (findProperty("demo") as String?)?.let { jvmArgs("-Dmotionlayer.demo=$it") }
        // 속도 재기: `-Pfps`를 주면 오버레이가 1초에 몇 번 다시 그려지는지 콘솔에 적는다.
        if (hasProperty("fps")) jvmArgs("-Dmotionlayer.fps=true")
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Dmg)
            packageName = "MotionLayer"
            packageVersion = "1.0.0"
        }
    }
}
