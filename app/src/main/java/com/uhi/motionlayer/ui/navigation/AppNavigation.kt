package com.uhi.motionlayer.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.uhi.motionlayer.R
import com.uhi.motionlayer.domain.model.BasicGesture
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.domain.model.PairingResult
import com.uhi.motionlayer.domain.model.PairingTarget
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.ui.account.AccountRoute
import com.uhi.motionlayer.ui.account.LoginScreen
import com.uhi.motionlayer.ui.account.LoginViewModel
import com.uhi.motionlayer.ui.apps.AppListRoute
import com.uhi.motionlayer.ui.apps.AppSettingsRoute
import com.uhi.motionlayer.ui.apps.PhoneControlRoute
import com.uhi.motionlayer.ui.developer.DeveloperToolsRoute
import com.uhi.motionlayer.ui.devices.AddDeviceOfflineRoute
import com.uhi.motionlayer.ui.devices.DeviceDetailRoute
import com.uhi.motionlayer.ui.devices.DeviceFlowViewModel
import com.uhi.motionlayer.ui.devices.DeviceListRoute
import com.uhi.motionlayer.ui.devices.DirectionPickerRoute
import com.uhi.motionlayer.ui.devices.NearbyDevicesRoute
import com.uhi.motionlayer.ui.devices.RemoteControlRoute
import com.uhi.motionlayer.ui.gesture.CustomGestureDetailRoute
import com.uhi.motionlayer.ui.gesture.CustomGestureNameScreen
import com.uhi.motionlayer.ui.gesture.CustomGestureRecordingRoute
import com.uhi.motionlayer.ui.gesture.GestureDetailRoute
import com.uhi.motionlayer.ui.gesture.GestureListRoute
import com.uhi.motionlayer.ui.gesture.PracticeDoneScreen
import com.uhi.motionlayer.ui.gesture.PracticeRoute
import com.uhi.motionlayer.ui.gesture.PracticeViewModel
import com.uhi.motionlayer.ui.gesture.RecordingViewModel
import com.uhi.motionlayer.ui.components.MlBottomTab
import com.uhi.motionlayer.ui.components.MlTab
import com.uhi.motionlayer.ui.debug.isDebuggable
import com.uhi.motionlayer.ui.home.HomeRoute
import com.uhi.motionlayer.ui.onboarding.BasicGesturesScreen
import com.uhi.motionlayer.ui.onboarding.FirstGestureScreen
import com.uhi.motionlayer.ui.onboarding.OnboardingStep
import com.uhi.motionlayer.ui.onboarding.OnboardingViewModel
import com.uhi.motionlayer.ui.onboarding.PickAppsScreen
import com.uhi.motionlayer.ui.onboarding.ReadyScreen
import com.uhi.motionlayer.ui.onboarding.TrialSuccessScreen
import com.uhi.motionlayer.ui.onboarding.WelcomeScreen
import com.uhi.motionlayer.ui.permission.BatteryGuideRoute
import com.uhi.motionlayer.ui.permission.NotificationAccessScreen
import com.uhi.motionlayer.ui.permission.NotificationPermissionScreen
import com.uhi.motionlayer.ui.permission.PermissionFlowEvent
import com.uhi.motionlayer.ui.permission.PermissionFlowViewModel
import com.uhi.motionlayer.ui.permission.PermissionListRoute
import com.uhi.motionlayer.ui.permission.PermissionStep
import com.uhi.motionlayer.ui.presentation.CodeEntryRoute
import com.uhi.motionlayer.ui.presentation.EffectsRoute
import com.uhi.motionlayer.ui.presentation.ConnectedScreen
import com.uhi.motionlayer.ui.presentation.ConnectingScreen
import com.uhi.motionlayer.ui.presentation.ConnectionFailedScreen
import com.uhi.motionlayer.ui.presentation.PairingViewModel
import com.uhi.motionlayer.ui.presentation.PrepareRoute
import com.uhi.motionlayer.ui.presentation.PresentationRoute
import com.uhi.motionlayer.ui.presentation.PresentingRoute
import com.uhi.motionlayer.ui.presentation.PresentingViewModel
import com.uhi.motionlayer.ui.presentation.QrCameraContent
import com.uhi.motionlayer.ui.presentation.QrScanScreen
import com.uhi.motionlayer.ui.presentation.SummaryScreen
import com.uhi.motionlayer.ui.report.ReportRoute
import com.uhi.motionlayer.ui.settings.SettingsRoute
import com.uhi.motionlayer.ui.settings.VibrationRoute
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.toast.ToastHost
import kotlinx.coroutines.launch

/** 화면 주소. 하단 탭 4개와, 탭 위에 열리는 하위 화면들. */
object Routes {
    const val Home = "home"
    const val Apps = "apps"
    const val Gesture = "gesture"
    const val Presentation = "presentation"

    const val Scan = "presentation/scan"
    const val Code = "presentation/code"
    const val Connecting = "presentation/connecting"
    const val Connected = "presentation/connected"
    const val Failed = "presentation/failed"
    const val Prepare = "presentation/prepare"
    const val Presenting = "presentation/presenting"
    const val Summary = "presentation/summary"
    const val Effects = "presentation/effects"

    const val PermissionNotifications = "permission/notifications"
    const val PermissionAccess = "permission/access"

    /** 첫 실행 안내(ON-01~11). 앱을 처음 켜면 홈 대신 여기서 시작한다. */
    const val OnboardingWelcome = "onboarding/welcome"
    const val OnboardingTrial = "onboarding/trial"
    const val OnboardingSuccess = "onboarding/success"
    const val OnboardingGestures = "onboarding/gestures"
    const val OnboardingNotifications = "onboarding/notifications"
    const val OnboardingBattery = "onboarding/battery"
    const val OnboardingApps = "onboarding/apps"
    const val OnboardingAccess = "onboarding/access"
    const val OnboardingLogin = "onboarding/login"
    const val OnboardingDone = "onboarding/done"

    /** 로그인(ON-10)을 첫 실행 안내가 아닌 곳(설정의 "로그인" 줄)에서 연 것. 끝나면 연 화면으로 돌아간다. */
    const val Login = "login"

    /** 계정(ST-05). 설정 맨 위의 계정 줄에서 열린다. */
    const val Account = "settings/account"

    /** 전화 제어(AP-04). 앱 목록의 "전화" 줄에서 열린다. */
    const val PhoneControl = "phone-control"

    /** 앱별 동작 설정(AP-02). `{appId}` 자리에 앱의 id가 들어간다. */
    const val AppSettings = "apps/{appId}"

    fun appSettings(appId: String) = "apps/$appId"

    /** 기본 제스처 상세(GS-02). `{gesture}` 자리에 [BasicGesture]의 이름이 들어간다. */
    const val GestureDetail = "gesture/detail/{gesture}"

    fun gestureDetail(gesture: BasicGesture) = "gesture/detail/${gesture.name}"

    /** 나만의 제스처 만들기: 이름(GS-03)과 녹화·확인·완료(GS-04~08). 녹화 단계는 화면 하나가 상태에 따라 모양을 바꾼다. */
    const val CreateGesture = "gesture/create"
    const val RecordGesture = "gesture/record"

    /** 나만의 제스처 상세(GS-11). `{id}` 자리에 제스처의 id가 들어간다. */
    const val CustomGestureDetail = "gesture/custom/{id}"

    fun customGestureDetail(id: String) = "gesture/custom/$id"

    const val Settings = "settings"
    const val Vibration = "settings/vibration"
    const val Devices = "settings/devices"

    /** 내 기기 상세(DV-04). `{deviceId}` 자리에 기기의 id가 들어간다. */
    const val DeviceDetail = "devices/detail/{deviceId}"

    fun deviceDetail(deviceId: String) = "devices/detail/$deviceId"

    /** 기기 추가의 QR 찍기·코드 입력. PC 연결(발표 탭)의 같은 화면과 달리, 찍은 기기가 어떤 기기인지 알아보는 데 쓴다. */
    const val DeviceScan = "devices/scan"
    const val DeviceCode = "devices/code"

    /** 기기 추가의 "주변 기기 찾기"(DV-06)와 인터넷이 없을 때의 안내(DV-09). */
    const val Nearby = "devices/nearby"
    const val DeviceOffline = "devices/offline"

    /** 방향으로 기기 고르기(DV-07). 조작 대상 시트(HM-06)의 "폰을 기기 쪽으로 향해서 고르기"에서 열린다. */
    const val DirectionPicker = "devices/direction"

    /** 원격 조작(DV-08). `{deviceId}` 자리에 기기의 id가 들어간다. */
    const val Remote = "devices/remote/{deviceId}"

    fun remote(deviceId: String) = "devices/remote/$deviceId"
    const val Permissions = "settings/permissions"
    const val BatteryGuide = "settings/battery"
    const val DeveloperTools = "settings/developer"
    const val History = "history"
    const val Practice = "practice"
    const val PracticeDone = "practice/done"
}

private val tabRoutes = mapOf(
    Routes.Home to MlTab.Home,
    Routes.Apps to MlTab.Apps,
    Routes.Gesture to MlTab.Gesture,
    Routes.Presentation to MlTab.Presentation,
)

private fun MlTab.route(): String = tabRoutes.entries.first { it.value == this }.key

/** 나만의 제스처를 만드는 화면들. 만들기를 시작한 화면(제스처 목록이나 제스처 상세) 위에 쌓인다. */
private val gestureCreationRoutes = setOf(Routes.CreateGesture, Routes.RecordGesture)

private fun OnboardingStep.route(): String = when (this) {
    OnboardingStep.Welcome -> Routes.OnboardingWelcome
    OnboardingStep.Trial -> Routes.OnboardingTrial
    OnboardingStep.Success -> Routes.OnboardingSuccess
    OnboardingStep.Gestures -> Routes.OnboardingGestures
    OnboardingStep.Notifications -> Routes.OnboardingNotifications
    OnboardingStep.Battery -> Routes.OnboardingBattery
    OnboardingStep.Apps -> Routes.OnboardingApps
    OnboardingStep.Access -> Routes.OnboardingAccess
    OnboardingStep.Login -> Routes.OnboardingLogin
    OnboardingStep.Done -> Routes.OnboardingDone
}

/**
 * 앱의 화면 이동 전부. 하단 탭은 탭 화면 네 곳에서만 보인다.
 *
 * @param qrCamera 발표 탭 QR 스캔 화면에 들어갈 카메라. 지금은 가짜 카메라, B가 만든 실제 카메라로 바꿔 끼운다.
 */
@Composable
fun AppNavigation(
    qrCamera: QrCameraContent,
    debugActions: List<DebugAction>,
    practiceDebugActions: List<DebugAction>,
    homeDebugActions: List<DebugAction>,
    onboardingDebugActions: List<DebugAction>,
    accountDebugActions: List<DebugAction>,
    deviceDebugActions: List<DebugAction>,
    recordingDebugActions: List<DebugAction>,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    pairing: PairingViewModel = viewModel(factory = PairingViewModel.Factory),
    presenting: PresentingViewModel = viewModel(factory = PresentingViewModel.Factory),
    practice: PracticeViewModel = viewModel(factory = PracticeViewModel.Factory),
    recording: RecordingViewModel = viewModel(factory = RecordingViewModel.Factory),
    permissionFlow: PermissionFlowViewModel = viewModel(factory = PermissionFlowViewModel.Factory),
    onboarding: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory),
    deviceFlow: DeviceFlowViewModel = viewModel(factory = DeviceFlowViewModel.Factory),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTab = tabRoutes[currentRoute]

    // 기기 추가에서 찍은 기기를 알아보면 찍는 화면들(QR·코드)을 닫고 기기 목록으로 돌아간다.
    LaunchedEffect(deviceFlow, navController) {
        deviceFlow.scanDone.collect { navController.popBackStack(Routes.Devices, inclusive = false) }
    }

    // 연결 시도가 끝나면 결과에 맞는 화면으로 간다. 연결 화면들은 흐름이 시작된 화면(홈이나 발표 탭) 위에 쌓는다.
    LaunchedEffect(pairing, navController) {
        pairing.results.collect { result ->
            when (result) {
                is PairingResult.Connected -> navController.navigate(Routes.Connected) {
                    popFlow(navController)
                    launchSingleTop = true
                }
                is PairingResult.Failed -> navController.navigate(Routes.Failed) {
                    popFlow(navController)
                    launchSingleTop = true
                }
            }
        }
    }

    // 연결을 시도하는 동안에는 "PC에 연결하는 중"(PR-16)을 흐름이 시작된 화면 위에 쌓는다. 결과가 나오면 위의 이동이
    // 흐름 화면들(이 화면 포함)을 걷어 내고 연결됨·실패로 간다.
    LaunchedEffect(pairing, navController) {
        pairing.isConnecting.collect { connecting ->
            if (connecting && navController.currentDestination?.route != Routes.Connecting) {
                navController.navigate(Routes.Connecting) { launchSingleTop = true }
            }
        }
    }

    // 권한 안내 흐름이 알려 주는 대로 화면을 옮긴다. 안내 화면은 홈 위에 쌓고, 끝나면 홈으로 돌아간다.
    // 두 번째 안내(ON-09)는 첫 번째(ON-05)를 대신해서 열어, 뒤로 가면 안내를 그만두고 홈으로 간다.
    LaunchedEffect(permissionFlow, navController) {
        permissionFlow.events.collect { event ->
            when (event) {
                is PermissionFlowEvent.Show -> when (event.step) {
                    PermissionStep.Notifications -> navController.navigate(Routes.PermissionNotifications) {
                        launchSingleTop = true
                    }
                    PermissionStep.NotificationAccess -> navController.navigate(Routes.PermissionAccess) {
                        popUpTo(Routes.PermissionNotifications) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                PermissionFlowEvent.Finished -> navController.popBackStack(Routes.Home, inclusive = false)
            }
        }
    }

    // 키보드가 올라오면 내용이 키보드 위로 밀려 오르고, 하단 탭은 키보드 뒤에 가리는 대신 숨긴다.
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    // 앱을 켤 때 첫 실행 안내를 아직 안 마쳤으면 홈 대신 안내부터 시작한다. 시작 화면은 처음 한 번만 정한다.
    val startDestination = remember { if (onboarding.needed) Routes.OnboardingWelcome else Routes.Home }
    val debugOnboardingActions = if (isDebuggable()) onboardingDebugActions else emptyList()
    val debugAccountActions = if (isDebuggable()) accountDebugActions else emptyList()
    val debugDeviceActions = if (isDebuggable()) deviceDebugActions else emptyList()

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        // 토스트는 내용 영역 맨 아래에 뜬다. 하단 탭이 없는 화면에서는 내비게이션 바 위로 올린다.
        Box(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
            ) {
                // 첫 실행 안내. 환영 → 첫 제스처 체험 → 체험 성공 → 기본 제스처 4가지 → 알림 허용 → 배터리 → 쓸 앱 고르기
                // → 알림 접근 → 준비 완료 → 홈. 이미 허용된 권한 단계는 건너뛴다.
                composable(Routes.OnboardingWelcome) {
                    val skipAction = DebugAction(stringResource(R.string.onboarding_debug_skip)) {
                        navController.finishOnboarding(onboarding)
                    }
                    WelcomeScreen(
                        onStart = { navController.onboardingNext(onboarding, OnboardingStep.Welcome) },
                        debugActions = if (debugOnboardingActions.isEmpty()) emptyList() else listOf(skipAction),
                    )
                }
                composable(Routes.OnboardingTrial) {
                    val swipeCount by onboarding.trialSwipeCount.collectAsStateWithLifecycle()
                    val countAtStart = remember { onboarding.trialSwipeCount.value }
                    // 체험 화면이 떠 있는 동안만 움직임을 듣는다.
                    DisposableEffect(onboarding) {
                        onboarding.startTrial()
                        onDispose { onboarding.stopTrial() }
                    }
                    LaunchedEffect(swipeCount) {
                        if (swipeCount > countAtStart) {
                            onboarding.onTrialRecognized()
                            navController.onboardingNext(onboarding, OnboardingStep.Trial)
                        }
                    }
                    FirstGestureScreen(
                        // 건너뛰면 체험 성공 안내도 건너뛰고 기본 제스처 소개로 간다.
                        onSkip = { navController.onboardingNext(onboarding, OnboardingStep.Success) },
                        debugActions = debugOnboardingActions,
                    )
                }
                composable(Routes.OnboardingSuccess) {
                    TrialSuccessScreen(onNext = { navController.onboardingNext(onboarding, OnboardingStep.Success) })
                }
                composable(Routes.OnboardingGestures) {
                    BasicGesturesScreen(
                        onNext = { navController.onboardingNext(onboarding, OnboardingStep.Gestures) },
                        onBack = { navController.goBack() },
                    )
                }
                composable(Routes.OnboardingNotifications) {
                    val scope = rememberCoroutineScope()
                    var requesting by remember { mutableStateOf(false) }
                    NotificationPermissionScreen(
                        onAllow = {
                            if (!requesting) {
                                requesting = true
                                scope.launch {
                                    try {
                                        onboarding.allowNotifications()
                                    } finally {
                                        requesting = false
                                    }
                                    navController.onboardingNext(onboarding, OnboardingStep.Notifications)
                                }
                            }
                        },
                        onLater = { navController.onboardingNext(onboarding, OnboardingStep.Notifications) },
                        onBack = { navController.goBack() },
                    )
                }
                composable(Routes.OnboardingBattery) {
                    BatteryGuideRoute(
                        onBack = { navController.goBack() },
                        onFinished = { navController.onboardingNext(onboarding, OnboardingStep.Battery) },
                    )
                }
                composable(Routes.OnboardingApps) {
                    val apps by onboarding.apps.collectAsStateWithLifecycle()
                    val phoneEnabled by onboarding.phoneEnabled.collectAsStateWithLifecycle()
                    PickAppsScreen(
                        apps = apps,
                        onToggle = onboarding::toggleApp,
                        phoneEnabled = phoneEnabled,
                        onTogglePhone = onboarding::togglePhone,
                        onNext = { navController.onboardingNext(onboarding, OnboardingStep.Apps) },
                        onBack = { navController.goBack() },
                    )
                }
                composable(Routes.OnboardingAccess) {
                    // 설정 앱에서 허용하고 돌아와 값이 바뀌면 다음으로 간다. 이 화면으로 되돌아왔을 때 바로 튕겨 나가지 않게
                    // 설정 앱을 연 뒤에만 본다.
                    val granted by onboarding.accessGranted.collectAsStateWithLifecycle()
                    var openedSettings by rememberSaveable { mutableStateOf(false) }
                    LaunchedEffect(granted, openedSettings) {
                        if (openedSettings && granted) navController.onboardingNext(onboarding, OnboardingStep.Access)
                    }
                    NotificationAccessScreen(
                        onOpenSettings = {
                            openedSettings = true
                            onboarding.openAccessSettings()
                        },
                        onLater = { navController.onboardingNext(onboarding, OnboardingStep.Access) },
                        onBack = { navController.goBack() },
                    )
                }
                composable(Routes.OnboardingLogin) {
                    val login: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
                    val signingIn by login.signingIn.collectAsStateWithLifecycle()
                    val failed by login.failed.collectAsStateWithLifecycle()
                    // 로그인은 선택이라 건너뛰어도 되고, 로그인이 되든 건너뛰든 다음은 준비 완료(ON-11)다.
                    val next = { navController.onboardingNext(onboarding, OnboardingStep.Login) }
                    LoginScreen(
                        signingIn = signingIn,
                        failed = failed,
                        onGoogle = { login.signIn(onSignedIn = next) },
                        onSkip = next,
                        onBack = { navController.goBack() },
                        debugActions = debugAccountActions,
                    )
                }
                composable(Routes.OnboardingDone) {
                    val apps by onboarding.apps.collectAsStateWithLifecycle()
                    ReadyScreen(
                        enabledApps = apps.filter { it.enabled },
                        onHome = { navController.finishOnboarding(onboarding) },
                    )
                }

                composable(Routes.Home) {
                    HomeRoute(
                        onOpenSettings = { navController.goTo(Routes.Settings) },
                        onOpenApps = { navController.switchTab(MlTab.Apps) },
                        onConnectPc = { navController.goTo(Routes.Scan) },
                        onOpenHistory = { navController.goTo(Routes.History) },
                        onOpenPractice = { navController.goTo(Routes.Practice) },
                        onTurnOnDetection = permissionFlow::start,
                        // 조작 대상 시트(HM-06)에서 켜진 TV를 고르면 원격 조작(DV-08), "향해서 고르기"는 레이더(DV-07)다.
                        onOpenRemote = { navController.goTo(Routes.remote(it.id)) },
                        onPickByDirection = { navController.goTo(Routes.DirectionPicker) },
                        debugActions = homeDebugActions,
                    )
                }
                composable(Routes.Apps) {
                    AppListRoute(
                        onOpenApp = { navController.goTo(Routes.appSettings(it)) },
                        onOpenPhone = { navController.goTo(Routes.PhoneControl) },
                    )
                }
                composable(Routes.PhoneControl) {
                    PhoneControlRoute(onBack = { navController.goBack() })
                }
                composable(
                    route = Routes.AppSettings,
                    arguments = listOf(navArgument("appId") { type = NavType.StringType }),
                ) { entry ->
                    val appId = entry.arguments?.getString("appId")
                    if (appId != null) AppSettingsRoute(appId = appId, onBack = { navController.goBack() })
                }
                composable(Routes.Gesture) {
                    GestureListRoute(
                        onOpenPractice = { navController.goTo(Routes.Practice) },
                        onOpenGesture = { navController.goTo(Routes.gestureDetail(it)) },
                        onCreateGesture = { navController.goTo(Routes.CreateGesture) },
                        onOpenCustomGesture = { navController.goTo(Routes.customGestureDetail(it)) },
                    )
                }
                composable(Routes.CreateGesture) {
                    CustomGestureNameScreen(
                        onClose = { navController.leaveGestureCreation() },
                        onNext = { name ->
                            recording.start(name)
                            navController.goTo(Routes.RecordGesture)
                        },
                    )
                }
                composable(Routes.RecordGesture) {
                    CustomGestureRecordingRoute(
                        viewModel = recording,
                        onClose = { navController.leaveGestureCreation() },
                        // 저장을 마친 제스처를 앱에 연결하러 가므로, 만들기 화면들을 걷어 낸 뒤 앱 탭으로 간다.
                        onConnectApps = {
                            navController.leaveGestureCreation()
                            navController.switchTab(MlTab.Apps)
                        },
                        onLater = { navController.leaveGestureCreation() },
                        debugActions = recordingDebugActions,
                    )
                }
                composable(
                    route = Routes.CustomGestureDetail,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val gestureId = entry.arguments?.getString("id")
                    if (gestureId != null) {
                        CustomGestureDetailRoute(
                            gestureId = gestureId,
                            onBack = { navController.goBack() },
                            onDeleted = { navController.popBackStack() },
                            onOpenApp = { navController.goTo(Routes.appSettings(it)) },
                            onRerecord = { gesture ->
                                // 이름은 그대로 두고 처음부터 다시 녹화한다(이름을 다시 묻지 않는다).
                                recording.start(gesture.name, replacingId = gesture.id)
                                navController.goTo(Routes.RecordGesture)
                            },
                        )
                    }
                }
                composable(
                    route = Routes.GestureDetail,
                    arguments = listOf(navArgument("gesture") { type = NavType.StringType }),
                ) { entry ->
                    val gesture = BasicGesture.entries.firstOrNull { it.name == entry.arguments?.getString("gesture") }
                    if (gesture != null) {
                        GestureDetailRoute(
                            gesture = gesture,
                            onBack = { navController.goBack() },
                            onPractice = {
                                practice.select(gesture)
                                navController.goTo(Routes.Practice)
                            },
                        )
                    }
                }
                composable(Routes.Presentation) {
                    PresentationRoute(
                        onConnectNew = { navController.goTo(Routes.Scan) },
                        pairing = pairing,
                    )
                }

                composable(Routes.Scan) {
                    QrScanScreen(
                        camera = qrCamera,
                        onScanned = { token -> pairing.connect(PairingTarget.QrToken(token)) },
                        onEnterCode = { navController.goTo(Routes.Code) },
                        onClose = { navController.goBack() },
                    )
                }
                composable(Routes.Code) {
                    CodeEntryRoute(pairing = pairing, onBack = { navController.goBack() })
                }
                composable(Routes.Connecting) {
                    val connectingName by pairing.connectingName.collectAsStateWithLifecycle()
                    // 취소하면 연결 시도를 멈추고 이 화면을 연 곳(QR·코드·발표 탭)으로 돌아간다. 시스템 뒤로 가기도 같다.
                    val cancel = {
                        pairing.cancel()
                        navController.goBack()
                    }
                    BackHandler(onBack = cancel)
                    ConnectingScreen(pcName = connectingName, onCancel = cancel)
                }
                composable(Routes.Connected) {
                    val connection by pairing.connection.collectAsStateWithLifecycle()
                    (connection as? PcConnectionState.Connected)?.let { connected ->
                        ConnectedScreen(
                            connection = connected,
                            onPrepare = { navController.goTo(Routes.Prepare) },
                            onConnectOther = { navController.goTo(Routes.Scan) { popFlow(navController) } },
                        )
                    }
                }
                composable(Routes.Failed) {
                    ConnectionFailedScreen(
                        onRescan = { navController.goTo(Routes.Scan) { popFlow(navController) } },
                        onEnterCode = { navController.goTo(Routes.Code) { popFlow(navController) } },
                        onClose = { navController.goBackToFlowOrigin() },
                    )
                }

                composable(Routes.Prepare) {
                    val connection by pairing.connection.collectAsStateWithLifecycle()
                    (connection as? PcConnectionState.Connected)?.let { connected ->
                        PrepareRoute(
                            viewModel = presenting,
                            connection = connected,
                            onBack = { navController.goBack() },
                            onOpenEffects = { navController.goTo(Routes.Effects) },
                            onStart = { navController.goTo(Routes.Presenting) { popFlow(navController) } },
                        )
                    }
                }
                composable(Routes.Presenting) {
                    PresentingRoute(
                        viewModel = presenting,
                        debugActions = debugActions,
                        onOpenEffects = { navController.goTo(Routes.Effects) },
                        onEnded = { navController.navigate(Routes.Summary) { popFlow(navController) } },
                    )
                }
                composable(Routes.Effects) {
                    EffectsRoute(onBack = { navController.goBack() })
                }
                composable(Routes.Summary) {
                    val summary by presenting.summary.collectAsStateWithLifecycle()
                    summary?.let {
                        SummaryScreen(
                            summary = it,
                            onConfirm = {
                                if (navController.goBackToFlowOrigin()) presenting.clearSummary()
                            },
                            onOpenDetails = { navController.goTo(Routes.History) },
                        )
                    }
                }

                composable(Routes.PermissionNotifications) {
                    NotificationPermissionScreen(
                        onAllow = permissionFlow::onAllowNotifications,
                        onLater = permissionFlow::onSkipNotifications,
                        onBack = { navController.goBack() },
                    )
                }
                composable(Routes.PermissionAccess) {
                    // 설정 앱에서 허용하고 돌아오면 값이 바뀌므로, 그때 흐름을 끝낸다.
                    val granted by permissionFlow.notificationAccessGranted.collectAsStateWithLifecycle()
                    LaunchedEffect(granted) {
                        if (granted) permissionFlow.onNotificationAccessGranted()
                    }
                    NotificationAccessScreen(
                        onOpenSettings = permissionFlow::onOpenNotificationAccessSettings,
                        onLater = permissionFlow::onSkipNotificationAccess,
                        onBack = { navController.goBack() },
                    )
                }

                composable(Routes.Settings) {
                    SettingsRoute(
                        onBack = { navController.goBack() },
                        onOpenAccount = { navController.goTo(Routes.Account) },
                        onSignIn = { navController.goTo(Routes.Login) },
                        onOpenVibration = { navController.goTo(Routes.Vibration) },
                        onOpenHistory = { navController.goTo(Routes.History) },
                        onOpenDevices = { navController.goTo(Routes.Devices) },
                        onOpenPermissions = { navController.goTo(Routes.Permissions) },
                        onOpenBatteryGuide = { navController.goTo(Routes.BatteryGuide) },
                        onOpenDeveloperTools = { navController.goTo(Routes.DeveloperTools) },
                    )
                }
                composable(Routes.Account) {
                    AccountRoute(
                        onBack = { navController.goBack() },
                        // 저장된 것이 있는 곳으로 간다. 제스처·앱은 탭이라 설정 화면들을 걷어 내고 그 탭으로 간다.
                        onOpenGestures = { navController.switchTab(MlTab.Gesture) },
                        onOpenDevices = { navController.goTo(Routes.Devices) },
                        onOpenApps = { navController.switchTab(MlTab.Apps) },
                        onSignedOut = { navController.popBackStack() },
                    )
                }
                composable(Routes.Login) {
                    val login: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
                    val signingIn by login.signingIn.collectAsStateWithLifecycle()
                    val failed by login.failed.collectAsStateWithLifecycle()
                    // 설정에서 열었으므로 로그인되든 건너뛰든 연 화면(설정)으로 돌아간다.
                    LoginScreen(
                        signingIn = signingIn,
                        failed = failed,
                        onGoogle = { login.signIn(onSignedIn = { navController.popBackStack() }) },
                        onSkip = { navController.goBack() },
                        onBack = { navController.goBack() },
                        debugActions = debugAccountActions,
                    )
                }
                composable(Routes.Vibration) {
                    VibrationRoute(onBack = { navController.goBack() })
                }
                composable(Routes.Devices) {
                    DeviceListRoute(
                        pairing = pairing,
                        deviceFlow = deviceFlow,
                        onOpenDevice = { navController.goTo(Routes.deviceDetail(it.id)) },
                        onScanQr = { navController.goTo(Routes.DeviceScan) },
                        onEnterCode = { navController.goTo(Routes.DeviceCode) },
                        onFindNearby = { navController.goTo(Routes.Nearby) },
                        onAddOffline = { navController.goTo(Routes.DeviceOffline) },
                        // 로그인하고 나면 로그인 화면이 닫히고 기기 목록으로 돌아온다. 등록하려던 기기는 그대로 남아 시트가 다시 뜬다.
                        onSignIn = { navController.goTo(Routes.Login) },
                        onBack = { navController.goBack() },
                        debugActions = debugDeviceActions,
                    )
                }
                composable(
                    route = Routes.DeviceDetail,
                    arguments = listOf(navArgument("deviceId") { type = NavType.StringType }),
                ) { entry ->
                    val deviceId = entry.arguments?.getString("deviceId")
                    if (deviceId != null) {
                        DeviceDetailRoute(
                            deviceId = deviceId,
                            onBack = { navController.goBack() },
                            onGone = { navController.popBackStack() },
                        )
                    }
                }
                // 기기 추가의 "QR 찍기"와 "코드 입력". 찍은 기기를 알아보면 기기 목록으로 돌아가고(아래 scanDone), 주인이 없으면
                // 거기서 어떻게 연결할지 묻는 시트(DV-03)가 뜬다.
                composable(Routes.DeviceScan) {
                    QrScanScreen(
                        camera = qrCamera,
                        onScanned = deviceFlow::onScanned,
                        onEnterCode = { navController.goTo(Routes.DeviceCode) },
                        onClose = { navController.goBack() },
                        title = stringResource(R.string.dv02_scan_title),
                        description = stringResource(R.string.dv02_scan_description),
                    )
                }
                composable(Routes.DeviceCode) {
                    CodeEntryRoute(
                        onSubmit = deviceFlow::onScanned,
                        onBack = { navController.goBack() },
                        title = stringResource(R.string.dv02_code_screen_title),
                    )
                }
                // 주변 기기(DV-06): 하나를 고르면 그 기기에 뜬 QR을 찍으러 간다. 찍으면 기기 목록으로 돌아와 손님으로 연결돼 있다.
                composable(Routes.Nearby) {
                    NearbyDevicesRoute(
                        onSelect = { device ->
                            deviceFlow.selectNearby(device)
                            navController.goTo(Routes.DeviceScan)
                        },
                        onBack = { navController.goBack() },
                    )
                }
                // 인터넷이 없어 기기를 추가할 수 없을 때(DV-09). 인터넷이 돌아오면 기기 목록으로 돌아가 기기 추가 시트를 연다.
                composable(Routes.DeviceOffline) {
                    AddDeviceOfflineRoute(
                        deviceFlow = deviceFlow,
                        onOnline = {
                            deviceFlow.requestAddSheet()
                            navController.popBackStack()
                        },
                        onBack = { navController.goBack() },
                    )
                }
                // 방향으로 기기 고르기(DV-07). 켜진 TV를 고르면 레이더를 걷어 내고 원격 조작(DV-08)으로 가서, 끝내면 홈으로 돌아간다.
                composable(Routes.DirectionPicker) {
                    DirectionPickerRoute(
                        onOpenRemote = { deviceId ->
                            navController.navigate(Routes.remote(deviceId)) {
                                popUpTo(Routes.DirectionPicker) { inclusive = true }
                            }
                        },
                        onClose = { navController.goBack() },
                    )
                }
                composable(
                    route = Routes.Remote,
                    arguments = listOf(navArgument("deviceId") { type = NavType.StringType }),
                ) { entry ->
                    val deviceId = entry.arguments?.getString("deviceId")
                    if (deviceId != null) {
                        RemoteControlRoute(deviceId = deviceId, onEnd = { navController.popBackStack() })
                    }
                }
                composable(Routes.Permissions) {
                    PermissionListRoute(
                        onOpenBatteryGuide = { navController.goTo(Routes.BatteryGuide) },
                        onBack = { navController.goBack() },
                    )
                }
                composable(Routes.BatteryGuide) {
                    BatteryGuideRoute(
                        onBack = { navController.goBack() },
                        onFinished = { navController.goBack() },
                    )
                }
                composable(Routes.DeveloperTools) {
                    DeveloperToolsRoute(onBack = { navController.goBack() })
                }
                composable(Routes.History) {
                    ReportRoute(onBack = { navController.goBack() })
                }
                composable(Routes.Practice) {
                    PracticeRoute(
                        viewModel = practice,
                        onBack = { navController.goBack() },
                        onCompleted = {
                            navController.navigate(Routes.PracticeDone) {
                                popUpTo(Routes.Practice) { inclusive = true }
                            }
                        },
                        debugActions = practiceDebugActions,
                    )
                }
                composable(Routes.PracticeDone) {
                    PracticeDoneScreen(onHome = { navController.goBackTo(Routes.Home) })
                }
            }
            ToastHost(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .then(if (currentTab == null) Modifier.navigationBarsPadding() else Modifier)
                    .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s16),
            )
        }

        if (currentTab != null && !imeVisible) {
            MlBottomTab(selected = currentTab, onSelect = { navController.switchTab(it) })
        }
    }
}

/**
 * 사용자가 누른 이동은 화면이 완전히 떠 있을 때만 한다. 같은 버튼을 빠르게 여러 번 눌러도
 * 같은 화면이 겹쳐 열리거나 뒤로 가기가 여러 번 먹히지 않게 하려는 것이다(전환 중인 화면은 아직 "떠 있는" 상태가 아니다).
 * 연결 결과나 권한 안내 끝처럼 앱이 스스로 하는 이동에는 쓰지 않는다.
 */
private fun NavHostController.isIdle(): Boolean =
    currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

private fun NavHostController.goTo(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
    if (isIdle()) navigate(route, builder)
}

private fun NavHostController.goBack() {
    if (isIdle()) popBackStack()
}

private fun NavHostController.goBackTo(route: String) {
    if (isIdle()) popBackStack(route, inclusive = false)
}

/**
 * 발표 흐름(QR·코드·연결 실패·연결됨·발표 준비·발표 중·요약)의 화면들. 이 화면들은 흐름이 시작된 화면 위에 쌓인다.
 * 흐름은 홈의 "PC 연결"에서도, 발표 탭에서도 시작할 수 있어서 시작 화면이 어디인지는 그때그때 찾는다.
 */
private val presentationFlowRoutes = setOf(
    Routes.Scan,
    Routes.Code,
    Routes.Connecting,
    Routes.Failed,
    Routes.Connected,
    Routes.Prepare,
    Routes.Presenting,
    Routes.Summary,
    Routes.Effects,
)

/** 흐름이 시작된 화면(홈이나 발표 탭)의 id. 흐름 화면들 바로 아래에 있다. */
private fun NavHostController.flowOriginId(): Int? = originIdOf(presentationFlowRoutes)

/** [flowRoutes]에 들지 않는 가장 위의 화면 id. 그 흐름이 시작된 화면이다. */
private fun NavHostController.originIdOf(flowRoutes: Set<String>): Int? = currentBackStack.value.lastOrNull { entry ->
    val route = entry.destination.route
    route != null && route !in flowRoutes
}?.destination?.id

/**
 * 나만의 제스처 만들기를 마치거나 그만두고 시작한 화면(제스처 목록이나 제스처 상세)으로 돌아간다.
 * 이미 돌아와 있으면 아무 일도 하지 않으므로 여러 번 불려도 한 번만 먹는다.
 */
private fun NavHostController.leaveGestureCreation() {
    val origin = originIdOf(gestureCreationRoutes) ?: return
    popBackStack(origin, inclusive = false)
}

/** 이 이동에서 쌓여 있던 흐름 화면들을 걷어 낸다. 흐름이 시작된 화면은 남는다. */
private fun NavOptionsBuilder.popFlow(navController: NavHostController) {
    navController.flowOriginId()?.let { origin -> popUpTo(origin) { inclusive = false } }
}

/** 흐름을 마치고 시작한 화면으로 돌아간다. 돌아갔으면 true. */
private fun NavHostController.goBackToFlowOrigin(): Boolean {
    val origin = flowOriginId()
    return isIdle() && origin != null && popBackStack(origin, inclusive = false)
}

/** 하단 탭 전환. 탭마다 보던 위치를 기억하고, 뒤로 가기는 홈으로 돌아간다. 첫 실행 안내로 시작한 경우에도 홈이 맨 아래라서 홈을 기준으로 한다. */
private fun NavHostController.switchTab(tab: MlTab) {
    navigate(tab.route()) {
        popUpTo(Routes.Home) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * 첫 실행 안내에서 [from] 다음 단계로 간다. 지나가면 다시 돌아올 이유가 없는 화면은 지운다:
 * 체험(ON-02)은 뒤로 가도 다시 기다리는 화면으로 돌아오지 않게, 알림 접근 안내(ON-09)는 설정 앱에서 허용하고 돌아온 화면이
 * 뒤로 가기를 누를 때마다 바로 앞으로 튕기지 않게, 로그인(ON-10)은 로그인한 뒤 뒤로 가도 로그인 화면이 다시 열리지 않게 한다.
 */
private fun NavHostController.onboardingNext(onboarding: OnboardingViewModel, from: OnboardingStep) {
    val next = onboarding.stepAfter(from)
    val leaving = currentDestination?.route?.takeIf { it in oneWayOnboardingRoutes }
    navigate(next.route()) {
        launchSingleTop = true
        if (leaving != null) popUpTo(leaving) { inclusive = true }
    }
}

/** 지나가면 뒤로 가기에서 지우는 첫 실행 안내 화면들. */
private val oneWayOnboardingRoutes = setOf(Routes.OnboardingTrial, Routes.OnboardingAccess, Routes.OnboardingLogin)

/** 첫 실행 안내를 마치고 홈으로 간다. 감지가 켜지고, 안내 화면들은 모두 걷어 낸다. */
private fun NavHostController.finishOnboarding(onboarding: OnboardingViewModel) {
    onboarding.finish()
    navigate(Routes.Home) { popUpTo(Routes.OnboardingWelcome) { inclusive = true } }
}
