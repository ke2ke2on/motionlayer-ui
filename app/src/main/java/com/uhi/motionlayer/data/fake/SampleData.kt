package com.uhi.motionlayer.data.fake

import com.uhi.motionlayer.domain.model.Account
import com.uhi.motionlayer.domain.model.AccountProvider
import com.uhi.motionlayer.domain.model.AppCategory
import com.uhi.motionlayer.domain.model.BasicGesture
import com.uhi.motionlayer.domain.model.ConnectedPerson
import com.uhi.motionlayer.domain.model.ConnectedPersonRole
import com.uhi.motionlayer.domain.model.ConnectionRoute
import com.uhi.motionlayer.domain.model.ControllableApp
import com.uhi.motionlayer.domain.model.CustomGesture
import com.uhi.motionlayer.domain.model.CustomGestureRate
import com.uhi.motionlayer.domain.model.DailyStats
import com.uhi.motionlayer.domain.model.DeviceKind
import com.uhi.motionlayer.domain.model.GestureBinding
import com.uhi.motionlayer.domain.model.GestureSummary
import com.uhi.motionlayer.domain.model.GestureCommand
import com.uhi.motionlayer.domain.model.GestureType
import com.uhi.motionlayer.domain.model.MyDevice
import com.uhi.motionlayer.domain.model.MyDeviceDetail
import com.uhi.motionlayer.domain.model.MyDeviceStatus
import com.uhi.motionlayer.domain.model.NearbyDevice
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.PersonPlace
import com.uhi.motionlayer.domain.model.PlayingApp
import com.uhi.motionlayer.domain.model.RecentPc
import com.uhi.motionlayer.domain.model.ReportRange
import com.uhi.motionlayer.domain.model.SensitivitySuggestion
import com.uhi.motionlayer.domain.model.UsageReport
import java.time.LocalDate

/** Figma 화면(HM-01~05)에 적힌 예시 값을 그대로 옮긴 가짜 데이터. */
object SampleData {
    // Figma `AppIcon` 타일 색: status/danger, status/success, text/primary, brand/primary
    private const val RED = 0xFFD8303E
    private const val GREEN = 0xFF03B26C
    private const val DARK = 0xFF191F28
    private const val BLUE = 0xFF1F68D9

    /** HM-01의 앱. 타일 색은 Figma에서 `status/danger`(#D8303E)다. */
    val youtubeMusic = PlayingApp(
        name = "YouTube Music",
        iconLetter = "Y",
        accentColorArgb = RED,
        appId = "youtube_music",
        bindings = listOf(
            GestureBinding(GestureType.SwipeRight, GestureCommand.NextTrack),
            GestureBinding(GestureType.DoubleTap, GestureCommand.PlayPause),
            GestureBinding(GestureType.Tilt, GestureCommand.Volume),
        ),
    )

    /** HM-05의 PC 연결. */
    /** Figma GS-01·GS-11의 나만의 제스처 "원 그리기"(10월 9일에 만듦, 인식률 91%, 되돌림 1회, 감도 보통). */
    val customCircle = CustomGesture(
        id = "circle",
        name = "원 그리기",
        createdOn = LocalDate.of(2026, 10, 9),
        successRatePercent = 91,
        undoneCount = 1,
        attemptCount = 12,
        sensitivity = 0.61f,
    )

    val classroomPc = PcConnectionState.Connected(
        deviceName = "강의실 PC",
        route = ConnectionRoute.Local,
        latencyMs = 12,
        bindings = listOf(
            GestureBinding(GestureType.SwipeRight, GestureCommand.NextSlide),
            GestureBinding(GestureType.PressAndRotate, GestureCommand.Pointer),
        ),
    )

    /** HM-01·05의 통계(오늘 인식 23회, 되돌림 1회). */
    val todayStats = DailyStats(recognizedCount = 23, undoneCount = 1)

    /**
     * AP-01 앱 목록. 앞의 여섯 개는 Figma 값(사용 중 2개, 설치된 앱 4개)이고,
     * 나머지는 목록이 스크롤되도록 더한 가짜다(Figma는 "설치된 앱 24").
     */
    val apps = listOf(
        ControllableApp("youtube_music", "YouTube Music", AppCategory.Music, "Y", RED, enabled = true, customGestureCount = 1),
        ControllableApp("melon", "멜론", AppCategory.Music, "멜", GREEN, enabled = true),
        ControllableApp("youtube", "YouTube", AppCategory.Video, "Y", RED),
        ControllableApp("spotify", "Spotify", AppCategory.Music, "S", DARK),
        ControllableApp("genie", "지니 뮤직", AppCategory.Music, "지", BLUE),
        ControllableApp("netflix", "Netflix", AppCategory.Video, "N", RED),
        ControllableApp("flo", "FLO", AppCategory.Music, "F", BLUE),
        ControllableApp("vibe", "VIBE", AppCategory.Music, "V", GREEN),
        ControllableApp("bugs", "벅스", AppCategory.Music, "벅", RED),
        ControllableApp("tving", "티빙", AppCategory.Video, "티", RED),
        ControllableApp("wavve", "웨이브", AppCategory.Video, "웨", BLUE),
        ControllableApp("coupang_play", "쿠팡플레이", AppCategory.Video, "쿠", DARK),
        ControllableApp("podbbang", "팟빵", AppCategory.Other, "팟", GREEN),
    )

    /**
     * ST-03 기록과 리포트. "7일"은 Figma 값(142회, 되돌림 4회, 하루 평균 오작동 0.6회, 성공률 97·95·99·92%,
     * 나만의 제스처 "원 그리기" 88%, "12번 중 4번 되돌림" 제안). "오늘"과 "30일"은 Figma에 값이 없어 그럴듯하게 만든
     * 가짜다(오늘은 홈의 23회·1회와 맞춤).
     *
     * "원 그리기"의 숫자는 Figma 화면마다 다르다(GS-01·11은 91%·되돌림 1회, ST-03·04는 88%·12번 중 4번).
     * 가짜는 화면마다 Figma 값을 그대로 둔다. 실제 구현은 같은 기록에서 나오므로 한 값이 된다.
     */
    val reports = listOf(
        UsageReport(
            range = ReportRange.Today,
            recognizedCount = 23, undoneCount = 1,
            averageMisfiresPerDay = 1.0f, misfireGoalMet = true,
            gestureRates = rates(96, 94, 100, 90),
            customGestureRates = listOf(circleRate(90)),
        ),
        UsageReport(
            range = ReportRange.Week,
            recognizedCount = 142, undoneCount = 4,
            averageMisfiresPerDay = 0.6f, misfireGoalMet = true,
            gestureRates = rates(97, 95, 99, 92),
            customGestureRates = listOf(circleRate(88)),
            sensitivitySuggestion = SensitivitySuggestion(
                gestureId = customCircle.id,
                gestureName = customCircle.name,
                attemptCount = 12,
                undoneCount = 4,
                sensitivity = customCircle.sensitivity,
            ),
        ),
        UsageReport(
            range = ReportRange.Month,
            recognizedCount = 612, undoneCount = 17,
            averageMisfiresPerDay = 0.6f, misfireGoalMet = true,
            gestureRates = rates(96, 94, 98, 91),
            customGestureRates = listOf(circleRate(89)),
        ),
    )

    /** 목표(90%)에 못 미치면 주황으로 보여 준다. 판단은 기록을 가진 쪽이 하므로 가짜가 값에 같이 담는다. */
    private fun circleRate(percent: Int) = CustomGestureRate(
        gestureId = customCircle.id,
        name = customCircle.name,
        successRatePercent = percent,
        needsAttention = percent < 90,
    )

    private fun rates(swipe: Int, doubleTap: Int, tilt: Int, shake: Int) = listOf(
        GestureSummary(BasicGesture.Swipe, swipe),
        GestureSummary(BasicGesture.DoubleTap, doubleTap),
        GestureSummary(BasicGesture.Tilt, tilt),
        GestureSummary(BasicGesture.Shake, shake),
    )

    /** PR-01의 "최근 연결한 PC" 두 줄. */
    val recentPcs = listOf(
        RecentPc(id = "classroom", name = "강의실 PC", description = "공학관 304", lastConnected = LocalDate.of(2026, 10, 2)),
        RecentPc(id = "macbook", name = "윤의 MacBook", description = "내 기기", lastConnected = LocalDate.of(2026, 10, 8)),
    )

    /** ST-01·05의 계정(허윤 · Google). */
    val account = Account(name = "허윤", provider = AccountProvider.Google)

    /** DV-01·ST-05의 "내 기기 3"(윤의 MacBook 연결됨, 거실 TV 꺼짐, 본가 TV 밖에서 조작 가능). */
    val macbook = MyDevice("macbook", "윤의 MacBook", DeviceKind.Pc, "macOS", MyDeviceStatus.Connected(latencyMs = 12))
    val livingRoomTv = MyDevice("living_tv", "거실 TV", DeviceKind.Tv, "Android TV", MyDeviceStatus.Off)
    val homeTv = MyDevice("home_tv", "본가 TV", DeviceKind.Tv, "Android TV", MyDeviceStatus.RemoteReachable)
    val myDevices = listOf(macbook, livingRoomTv, homeTv)

    /** DV-06의 주변 기기 두 줄(지수네 TV는 주인이 지수, 스터디룸 PC는 공용 기기). */
    val nearbyDevices = listOf(
        NearbyDevice(id = "jisu_tv", name = "지수네 TV", kind = DeviceKind.Tv, owner = "지수"),
        NearbyDevice(id = "study_pc", name = "스터디룸 PC", kind = DeviceKind.Pc, owner = null),
    )

    val me = ConnectedPerson("me", "나", ConnectedPersonRole.Me, PersonPlace.ThisPhone, controlling = true)

    /** DV-04. 거실 TV는 Figma 값 그대로이고(손님·동시 조작·집 밖 허용, 연결된 사람 나·민수, 방향 2곳) 나머지 둘은 직접 만든 값이다. */
    val deviceDetails: Map<String, MyDeviceDetail> = listOf(
        MyDeviceDetail(
            device = livingRoomTv,
            allowGuests = true,
            allowSimultaneous = false,
            allowRemote = true,
            connectedPeople = listOf(
                me,
                ConnectedPerson("minsu", "민수", ConnectedPersonRole.Guest, PersonPlace.SameWifi, controlling = false),
            ),
            selectVibrationLabel = "짧게-길게",
            registeredDirections = 2,
        ),
        MyDeviceDetail(
            device = macbook,
            allowGuests = false,
            allowSimultaneous = false,
            allowRemote = false,
            connectedPeople = listOf(me),
            selectVibrationLabel = "짧게",
            registeredDirections = 1,
        ),
        MyDeviceDetail(
            device = homeTv,
            allowGuests = false,
            allowSimultaneous = false,
            allowRemote = true,
            connectedPeople = emptyList(),
            selectVibrationLabel = "길게",
            registeredDirections = 1,
        ),
    ).associateBy { it.device.id }
}
