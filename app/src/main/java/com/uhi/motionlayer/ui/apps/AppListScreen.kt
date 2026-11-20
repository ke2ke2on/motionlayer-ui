package com.uhi.motionlayer.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import com.uhi.motionlayer.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uhi.motionlayer.R
import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.ControllableApp
import com.uhi.motionlayer.ui.components.MlBottomTab
import com.uhi.motionlayer.ui.components.MlEmptyState
import com.uhi.motionlayer.ui.components.MlIcon
import com.uhi.motionlayer.ui.components.MlIconView
import com.uhi.motionlayer.ui.components.MlListRow
import com.uhi.motionlayer.ui.components.MlListRowLeading
import com.uhi.motionlayer.ui.components.MlListRowTrailing
import com.uhi.motionlayer.ui.components.MlSectionHeader
import com.uhi.motionlayer.ui.components.MlSkeleton
import com.uhi.motionlayer.ui.components.MlTab
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionLayerTheme
import com.uhi.motionlayer.ui.theme.MotionRadius
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionSpacing
import com.uhi.motionlayer.ui.theme.MotionTheme
import com.uhi.motionlayer.ui.util.KoreanParticle

/** 앱 탭 첫 화면(Figma AP-01, 알림 접근이 없으면 AP-05). 하단 탭은 앱 틀에서 붙인다. */
@Composable
fun AppListRoute(
    onOpenApp: (appId: String) -> Unit,
    onOpenPhone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppListViewModel = viewModel(factory = AppListViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // 글자를 쓰는 중의 상태는 화면이 직접 들고(한글 조합 입력이 끊기지 않게), 바뀐 검색어만 ViewModel에 알린다.
    val queryState = rememberTextFieldState()
    LaunchedEffect(queryState) {
        snapshotFlow { queryState.text.toString() }.collect(viewModel::onQueryChange)
    }
    AppListContent(
        state = state,
        queryState = queryState,
        onToggle = viewModel::onToggle,
        onOpenApp = onOpenApp,
        onTogglePhone = viewModel::onTogglePhone,
        onOpenPhone = onOpenPhone,
        onOpenAccessSettings = viewModel::onOpenAccessSettings,
        modifier = modifier,
    )
}

/**
 * 제목, 검색창, "사용 중"과 "설치된 앱" 목록. 상태를 받아 그리기만 한다. 앱 줄을 누르면 앱별 동작 설정(AP-02)이 열린다.
 * 알림 접근이 허용돼 있지 않으면 목록 대신 권한 안내(AP-05)를 보여 준다.
 * "사용 중"과 "설치된 앱" 사이의 "전화" 구역은 줄을 누르면 전화 제어(AP-04)가 열리고 토글로 바로 켜고 끈다.
 * 검색어를 쓰면 전화 구역은 숨긴다.
 */
@Composable
fun AppListContent(
    state: AppListUiState,
    queryState: TextFieldState,
    onToggle: (appId: String, enabled: Boolean) -> Unit,
    onOpenApp: (appId: String) -> Unit,
    onTogglePhone: (Boolean) -> Unit,
    onOpenPhone: () -> Unit,
    onOpenAccessSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.accessGranted) {
        AccessRequired(onOpenSettings = onOpenAccessSettings, modifier = modifier)
        return
    }

    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val inUseTitle = stringResource(R.string.ap01_section_in_use, state.inUse.size)
    val installedTitle = stringResource(R.string.ap01_section_installed, state.installed.size)

    // 목록을 넘기기 시작하면 키보드를 내린다.
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) focusManager.clearFocus()
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = MotionSpacing.s16),
    ) {
        item(key = "header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MotionSpacing.screenHorizontal,
                        end = MotionSpacing.screenHorizontal,
                        top = MotionSpacing.s16,
                        bottom = MotionSpacing.s12,
                    ),
                verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
            ) {
                Text(text = stringResource(R.string.ap01_title), style = type.title2, color = colors.textPrimary)
                Text(text = stringResource(R.string.ap01_description), style = type.body2, color = colors.textSecondary)
            }
        }
        item(key = "search") {
            SearchField(
                queryState = queryState,
                modifier = Modifier.padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    bottom = MotionSpacing.s4,
                ),
            )
        }

        // 설치된 앱을 읽는 동안(AP-07)은 제목·검색창만 두고 목록 자리에 자리 표시 막대를 보여 준다.
        if (state.loading) {
            appLoadingItems()
            return@LazyColumn
        }

        appSection("header-in-use", inUseTitle, state.inUse, onToggle, onOpenApp)
        if (state.showPhone) {
            item(key = "header-phone") {
                MlSectionHeader(title = stringResource(R.string.ap01_section_phone), modifier = Modifier.animateItem())
            }
            item(key = "phone") {
                PhoneControlRow(
                    enabled = state.phoneEnabled,
                    onToggle = onTogglePhone,
                    onOpen = onOpenPhone,
                    modifier = Modifier.animateItem(),
                )
            }
        }
        appSection("header-installed", installedTitle, state.installed, onToggle, onOpenApp)

        if (state.inUse.isEmpty() && state.installed.isEmpty()) {
            item(key = "empty") {
                val query = queryState.text.toString().trim()
                if (query.isEmpty()) {
                    // 검색어 없이 목록이 비는 일(조작할 수 있는 앱이 하나도 없음)은 Figma에 시안이 없어 한 줄만 보여 준다.
                    Text(
                        text = stringResource(R.string.ap08_title),
                        style = type.body2,
                        color = colors.textTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s24),
                    )
                } else {
                    NoResults(query = query, onClear = { queryState.clearText() })
                }
            }
        }
    }
}

/** 설치된 앱을 읽는 중의 목록 자리(Figma AP-07): 안내 한 줄과 자리 표시 줄 여섯 개. 줄마다 이름 막대의 길이만 다르다. */
private fun LazyListScope.appLoadingItems() {
    item(key = "loading-caption") {
        Text(
            text = stringResource(R.string.ap07_caption),
            style = MotionTheme.typography.caption1,
            color = MotionTheme.colors.textTertiary,
            modifier = Modifier
                .fillMaxWidth()
                // 불러오는 중이라는 말을 스크린리더가 바로 읽어 준다(보고서 6.4 "스켈레톤 영역은 '불러오는 중' 라이브 리전").
                .semantics { liveRegion = LiveRegionMode.Polite }
                .padding(
                    start = MotionSpacing.screenHorizontal,
                    end = MotionSpacing.screenHorizontal,
                    top = MotionSpacing.s12,
                    bottom = MotionSpacing.s4,
                ),
        )
    }
    items(LoadingNameWidths) { nameWidth -> SkeletonAppRow(nameWidth = nameWidth) }
}

private val LoadingNameWidths = listOf(120.dp, 150.dp, 180.dp, 120.dp, 150.dp, 180.dp)

/** 앱 줄 자리 표시: 동그란 아이콘, 이름·설명 막대 두 개, 스위치 모양. 높이 68(위아래 14). */
@Composable
private fun SkeletonAppRow(nameWidth: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MotionSpacing.screenHorizontal, vertical = MotionSpacing.s14),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s14),
    ) {
        MlSkeleton(modifier = Modifier.size(MotionSize.iconTile), shape = CircleShape)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MotionSpacing.s8),
        ) {
            MlSkeleton(modifier = Modifier.size(width = nameWidth, height = 14.dp))
            MlSkeleton(modifier = Modifier.size(width = 90.dp, height = 12.dp))
        }
        MlSkeleton(modifier = Modifier.size(width = 50.dp, height = 30.dp), shape = CircleShape)
    }
}

/**
 * 검색어와 맞는 앱이 없을 때(Figma AP-08). "없음"만 말하지 않고 이유(이름이 비슷한 앱이 없음)와
 * 다음 행동(검색어 지우기)을 둔다. 검색어를 지우면 전체 목록(AP-01)으로 돌아온다.
 */
@Composable
private fun NoResults(query: String, onClear: () -> Unit) {
    MlEmptyState(
        icon = MlIcon.Search,
        title = stringResource(R.string.ap08_title),
        description = stringResource(R.string.ap08_description, query, KoreanParticle.with(query)),
        buttonText = stringResource(R.string.ap08_clear),
        onButtonClick = onClear,
    )
}

/** 제목 줄과 앱 줄들. 앱이 하나도 없으면 제목도 그리지 않는다. 켜고 끄면 앱 줄이 다른 구역으로 부드럽게 옮겨 간다. */
private fun LazyListScope.appSection(
    headerKey: String,
    title: String,
    apps: List<ControllableApp>,
    onToggle: (appId: String, enabled: Boolean) -> Unit,
    onOpenApp: (appId: String) -> Unit,
) {
    if (apps.isEmpty()) return
    item(key = headerKey) { MlSectionHeader(title = title, modifier = Modifier.animateItem()) }
    items(apps, key = { it.id }) { app ->
        AppRow(app = app, onToggle = onToggle, onOpen = { onOpenApp(app.id) }, modifier = Modifier.animateItem())
    }
}

@Composable
private fun AppRow(
    app: ControllableApp,
    onToggle: (appId: String, enabled: Boolean) -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MlListRow(
        modifier = modifier,
        title = app.name,
        subtitle = app.subtitle(),
        leading = MlListRowLeading.App(letter = app.iconLetter, color = app.accentColorArgb?.let { Color(it) }),
        trailing = MlListRowTrailing.Toggle(checked = app.enabled, onCheckedChange = { onToggle(app.id, it) }),
        onClick = onOpen,
    )
}

/**
 * 알림 접근이 없어 앱 목록을 보여 줄 수 없을 때의 안내(Figma AP-05). 제목만 두고, 가운데에 잠금 그림과 설명, 설정으로 가는 버튼이 있다.
 */
@Composable
private fun AccessRequired(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding(),
    ) {
        Text(
            text = stringResource(R.string.ap01_title),
            style = type.title2,
            color = colors.textPrimary,
            modifier = Modifier.padding(
                start = MotionSpacing.screenHorizontal,
                end = MotionSpacing.screenHorizontal,
                top = MotionSpacing.s16,
                bottom = MotionSpacing.s12,
            ),
        )
        MlEmptyState(
            icon = MlIcon.Lock,
            title = stringResource(R.string.ap05_title),
            description = stringResource(R.string.ap05_description),
            buttonText = stringResource(R.string.ap05_open_settings),
            onButtonClick = onOpenSettings,
        )
    }
}

/** 켜 둔 앱은 "기본 동작"(+ 나만의 제스처 수), 꺼 둔 앱은 앱의 종류를 보여 준다. */
@Composable
private fun ControllableApp.subtitle(): String = when {
    !enabled -> stringResource(category.labelRes)
    customGestureCount > 0 -> stringResource(R.string.ap01_subtitle_custom, customGestureCount)
    else -> stringResource(R.string.ap01_subtitle_default)
}

/** Figma `Search`. 높이 48, 모서리 14, `fill/weak` 바탕에 돋보기와 한 줄 입력. 비어 있으면 안내 글자를 보여 준다. */
@Composable
private fun SearchField(queryState: TextFieldState, modifier: Modifier = Modifier) {
    val colors = MotionTheme.colors
    val type = MotionTheme.typography
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MotionSize.searchField)
            .clip(RoundedCornerShape(MotionRadius.r14))
            .background(colors.fillWeak)
            .padding(horizontal = MotionSpacing.s16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotionSpacing.s10),
    ) {
        MlIconView(icon = MlIcon.Search, tint = colors.textTertiary, size = MotionIconSize.s20)
        BasicTextField(
            state = queryState,
            modifier = Modifier.weight(1f),
            textStyle = type.body1.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.brandPrimary),
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            onKeyboardAction = { focusManager.clearFocus() },
            decorator = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (queryState.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.ap01_search_hint),
                            style = type.body1,
                            color = colors.textDisabled,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Preview(name = "AP-01 앱 목록", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Ap01Preview() = MotionLayerTheme {
    Column(Modifier.fillMaxSize()) {
        AppListContent(
            state = AppListUiState(
                inUse = SampleData.apps.filter { it.enabled },
                installed = SampleData.apps.filterNot { it.enabled },
                showPhone = true,
            ),
            queryState = rememberTextFieldState(),
            onToggle = { _, _ -> },
            onOpenApp = {},
            onTogglePhone = {},
            onOpenPhone = {},
            onOpenAccessSettings = {},
            modifier = Modifier.weight(1f),
        )
        MlBottomTab(selected = MlTab.Apps, onSelect = {})
    }
}

@Preview(name = "AP-07 앱 목록 · 불러오는 중", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Ap07Preview() = MotionLayerTheme {
    Column(Modifier.fillMaxSize()) {
        AppListContent(
            state = AppListUiState(loading = true),
            queryState = rememberTextFieldState(),
            onToggle = { _, _ -> },
            onOpenApp = {},
            onTogglePhone = {},
            onOpenPhone = {},
            onOpenAccessSettings = {},
            modifier = Modifier.weight(1f),
        )
        MlBottomTab(selected = MlTab.Apps, onSelect = {})
    }
}

@Preview(name = "AP-08 앱 목록 · 검색 결과 없음", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Ap08Preview() = MotionLayerTheme {
    Column(Modifier.fillMaxSize()) {
        AppListContent(
            state = AppListUiState(),
            queryState = rememberTextFieldState(initialText = "냉동"),
            onToggle = { _, _ -> },
            onOpenApp = {},
            onTogglePhone = {},
            onOpenPhone = {},
            onOpenAccessSettings = {},
            modifier = Modifier.weight(1f),
        )
        MlBottomTab(selected = MlTab.Apps, onSelect = {})
    }
}

@Preview(name = "AP-05 권한 없음 상태", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Ap05Preview() = MotionLayerTheme {
    Column(Modifier.fillMaxSize()) {
        AppListContent(
            state = AppListUiState(accessGranted = false),
            queryState = rememberTextFieldState(),
            onToggle = { _, _ -> },
            onOpenApp = {},
            onTogglePhone = {},
            onOpenPhone = {},
            onOpenAccessSettings = {},
            modifier = Modifier.weight(1f),
        )
        MlBottomTab(selected = MlTab.Apps, onSelect = {})
    }
}
