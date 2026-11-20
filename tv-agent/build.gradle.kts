plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// ---- 폰 앱의 디자인을 그대로 가져다 쓴다 (pc-agent와 같은 방식) ----------------------------------------
// 색·글자·간격 토큰과 공통 부품의 원본은 폰 앱(app) 한 곳에만 둔다. 복사본을 저장소에 올리지 않고,
// 빌드할 때마다 아래 파일을 build/generated 로 가져와 같이 컴파일한다. 그래서 값이 어긋날 수 없다.
// 조건: 여기 적는 Kotlin 파일은 폰 앱의 R(리소스 번호)을 직접 쓰지 않아야 한다(TV 앱의 R은 다른 것이라서).
val appMain = rootProject.layout.projectDirectory.dir("app/src/main")

val sharedUiFiles = listOf(
    "theme/Color.kt",
    "theme/Dimens.kt",
    "theme/Effects.kt",
    "theme/Theme.kt",
    "theme/Type.kt",
    "components/Text.kt",
    "components/MlBadge.kt",
    "components/MlIconView.kt",
    "util/KoreanParticle.kt",
)

// TV 화면이 쓰는 아이콘. `ui/components/MlIcon.kt`(TV판)의 목록과 같아야 한다(없으면 컴파일이 안 된다).
val sharedIcons = listOf("cloud", "gesture", "image", "user", "users")

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
// ---------------------------------------------------------------------------------------------------

android {
    namespace = "com.uhi.motionlayer.tv"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.uhi.motionlayer.tv"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    // 가져온 파일이 놓이는 폴더를 소스·리소스 폴더로 더한다. AGP 9는 여기에 Provider를 받지 않아서 경로 글자로 준다.
    sourceSets {
        getByName("main") {
            kotlin.directories.add(layout.buildDirectory.dir("generated/sharedUi").get().asFile.path)
            res.directories.add(layout.buildDirectory.dir("generated/sharedRes").get().asFile.path)
        }
    }
}

// 가져오기가 컴파일·리소스 처리보다 먼저 끝나게 한다.
tasks.named("preBuild") { dependsOn(syncSharedUi, syncSharedRes) }

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    testImplementation(libs.junit)
}
