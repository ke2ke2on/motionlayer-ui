# MotionLayer 화면 (UI)

폰을 만지지 않고 **폰의 움직임**으로 음악·볼륨·PC 슬라이드를 조작하는 앱 MotionLayer의 화면을 모은 저장소예요.
폰 앱, PC 에이전트, Android TV 앱의 화면을 UI 설계 보고서를 바탕으로 만들었어요.

> 제스처 인식, 시스템 연동, PC·TV 연결은 아직 붙어 있지 않아요. 화면은 `domain`의 계약(인터페이스) 뒤에 둔 **가짜 구현**으로 동작해요.

## 화면 범위

| 모듈 | 대상 | 화면 |
|---|---|---|
| `app` | 안드로이드 폰 (Android 8.0 이상) | 첫 실행 안내, 홈, 앱별 동작, 제스처·연습·나만의 제스처, PC 발표, 설정·기록·권한·계정, 기기 연결 |
| `pc-agent` | Windows·macOS | 설정 창(폰 연결·발표), 트레이 메뉴, 연결 알림, 발표 오버레이(포인터·그리기·화면 가리기·효과) |
| `tv-agent` | Android TV | 처음 설정, QR 대기, 연결 상태, 오버레이(조작한 사람·원격 조작 중·효과) |

**화면 수** (UI 설계 보고서의 화면 96개 기준)

| 모듈 | Figma 화면 | 앱에서 만든 화면 | 만들지 않은 화면 |
|---|---|---|---|
| 폰 (`app`) | 81 | 77 | 4 (시스템이 그리는 화면) |
| PC (`pc-agent`) | 9 | 9 | 0 |
| TV (`tv-agent`) | 6 | 6 | 0 |
| 합계 | 96 | 92 | 4 |

- 만들지 않은 4개는 안드로이드가 직접 그리는 화면이에요: 알림 허용 창(ON-06), 전화 권한 창(AP-06), 시스템 알림(SY-01·SY-02).
- PC의 트레이 메뉴(PC-04)와 연결 알림(PC-05)은 모양은 운영체제가 그리고, 앱은 항목과 문구를 정해요.
- 보고서에서 보류된 화면 3개(SY-04, SY-05, ST-06)는 96개에 들어가지 않고, 만들지도 않았어요.

## 기술 스택

- **언어**: Kotlin 2.2
- **폰·TV**: Jetpack Compose (Compose BOM 2026.02), Material 3, Navigation Compose, Lifecycle ViewModel
- **PC**: Compose Multiplatform 데스크톱 1.10 (Windows·macOS에서 같은 코드)
- **빌드**: Gradle 9.4 (wrapper), Android Gradle Plugin 9.2, 버전은 `gradle/libs.versions.toml` 한 곳에서 관리
- **SDK**: 최소 API 26(Android 8.0), target·compile API 36
- **글꼴**: Noto Sans KR (OFL)

## 폴더 구조

```
app/                         폰 앱
  src/main/java/com/uhi/motionlayer/
    ui/theme/                디자인 토큰 (색·글자·간격·모서리·그림자)
    ui/components/           공통 부품 (Figma 컴포넌트와 1:1, Ml*)
    ui/<기능>/               화면 (home, apps, gesture, presentation, settings, devices …)
    domain/                  화면이 쓰는 모델과 계약(인터페이스)
    data/fake/               가짜 구현 (실제 구현으로 바꿔 끼우는 곳은 AppContainer 한 곳)
pc-agent/                    PC 에이전트 (pc/domain, pc/data/fake, pc/ui)
tv-agent/                    TV 앱 (tv/domain, tv/data/fake, tv/ui)
docs/test-scenarios.md       화면 점검표
tools/icons/                 Figma 아이콘(SVG)을 안드로이드 벡터로 바꾸는 스크립트
```

PC·TV 모듈은 폰 앱의 디자인 토큰과 공통 부품을 **빌드할 때 가져다 써요**(각 모듈 `build.gradle.kts`의 `sharedUiFiles`). 원본은 `app`에만 있어요.

## 개발 환경

- **Android Studio**(최신)를 권해요. 폰·TV·PC를 모두 열고 실행할 수 있고, Compose 미리보기와 에뮬레이터 관리가 들어 있어요.
- **IntelliJ IDEA**(Community 같은 무료 버전으로도 돼요)도 쓸 수 있어요. PC 모듈(Compose 데스크톱)은 IntelliJ IDEA에서 가장 자연스럽게 돌아가요. 폰·TV 앱은 안드로이드 플러그인으로 열리지만, 새 Android Gradle Plugin 지원이 Android Studio보다 늦을 수 있어서 빌드가 안 되면 Android Studio를 써요.
- 두 편집기 모두 프로젝트 폴더를 열면 Gradle 설정을 알아서 불러와요. 안드로이드 앱을 빌드하려면 Android SDK가 필요해요(Android Studio를 설치하면 함께 들어와요).
- JDK 21을 써요. 편집기에 들어 있는 JDK를 그대로 써도 돼요.
- 편집기 없이 터미널에서 아래 `gradlew` 명령만으로도 빌드하고 실행할 수 있어요.

## 실행 방법

**폰**
- 편집기 위쪽 실행 목록에서 `app`을 고르고 에뮬레이터나 폰(USB 디버깅)을 골라 ▶
- 또는 `.\gradlew.bat :app:installDebug`

**PC** (Windows·macOS)
- 편집기 실행 목록에서 `pc-agent`를 고르고 ▶ (Android Studio와 IntelliJ IDEA 모두 이 실행 설정이 들어 있어요)
- 또는 `.\gradlew.bat :pc-agent:run` (macOS는 `./gradlew :pc-agent:run`)
- 창의 X를 누르면 트레이로 숨어요. 끝낼 때는 트레이 아이콘 메뉴의 "MotionLayer 종료"를 눌러요

**TV**
- Device Manager에서 TV 에뮬레이터(예: Television 1080p)를 만들고, 실행 목록에서 `tv-agent`를 골라 ▶
- 또는 TV 에뮬레이터를 켠 채 `.\gradlew.bat :tv-agent:installDebug`

## 테스트

```
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :pc-agent:test
.\gradlew.bat :tv-agent:testDebugUnitTest
```

PR을 올리거나 `main`에 합치면 깃허브가 폰·TV 앱 빌드와 위 테스트를 자동으로 돌려요(`.github/workflows/ci.yml`). 결과는 PR 화면에 ✅·❌로 보이고, 실패한 테스트 이름도 함께 나와요.

- 폰: 화면 코드에 색·숫자를 직접 쓰면 실패하는 디자인 규칙 검사(`DesignTokenRulesTest`), 한글 줄바꿈, 첫 실행 흐름 등
- PC: 화면을 PNG로 그려 내는 테스트가 있어 `pc-agent/build/renders/`에서 Figma 시안과 비교할 수 있어요
- TV: 어느 화면을 보일지, 연결된 사람이 많을 때 줄을 묶는 규칙 등

## 가짜 데이터와 시연용 동작

- 재생 중인 앱, 통계, PC, 기기, 연습 결과는 모두 Figma 예시 값을 쓴 **가짜**예요. 실제 구현은 `domain`의 같은 계약을 구현해 `AppContainer`(PC는 `PcAppContainer`, TV는 `TvAppContainer`)에서 바꿔 끼우면 돼요.
- 센서나 연결이 없는 에뮬레이터에서도 여러 상태를 볼 수 있게, **디버그 빌드에만** 시연용 동작 메뉴가 있어요.
  - 폰: 화면의 정해진 자리를 길게 누르기 (예: 홈 맨 위 "MotionLayer")
  - PC: 설정 창 왼쪽 메뉴를 마우스 오른쪽 버튼으로 누르기
  - TV: 리모컨 메뉴 키나 키보드 M 키
- 자리와 메뉴 내용은 점검표의 "0. 시작하기 전에"에 정리돼 있어요.

## 화면 점검

[docs/test-scenarios.md](docs/test-scenarios.md)에 폰·PC·TV 화면을 처음부터 끝까지 눌러 보는 점검표가 있어요. 버그를 적는 방법도 함께 있어요.
