package com.uhi.motionlayer.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.AppPermission
import com.uhi.motionlayer.domain.model.ControllableApp
import com.uhi.motionlayer.domain.model.isGranted
import com.uhi.motionlayer.domain.source.AppCatalog
import com.uhi.motionlayer.domain.source.PhoneControl
import com.uhi.motionlayer.domain.source.SystemPermissions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 앱 목록(AP-01)이 그릴 값. 검색어에 맞는 앱만 들어 있고, 켜 둔 앱과 나머지가 나뉘어 있다.
 *
 * @param accessGranted 알림 접근이 허용돼 있는지. 아니면 목록 대신 권한 안내(AP-05)를 보여 준다
 * @param loading 설치된 앱을 읽는 중인지. true면 목록 자리에 자리 표시 막대를 보여 준다(AP-07)
 * @param showPhone 전화 제어 줄(AP-04로 가는 "전화" 구역)을 보여 줄지. 검색어가 있으면 숨긴다
 * @param phoneEnabled 전화 제어가 켜져 있는지
 */
data class AppListUiState(
    val inUse: List<ControllableApp> = emptyList(),
    val installed: List<ControllableApp> = emptyList(),
    val accessGranted: Boolean = true,
    val loading: Boolean = false,
    val showPhone: Boolean = false,
    val phoneEnabled: Boolean = false,
)

class AppListViewModel(
    private val catalog: AppCatalog,
    private val permissions: SystemPermissions,
    private val phone: PhoneControl,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val uiState: StateFlow<AppListUiState> = combine(
        catalog.apps,
        query,
        permissions.statuses.map { it.isGranted(AppPermission.NotificationAccess) },
        phone.enabled,
        catalog.loading,
        ::filter,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = filter(
            catalog.apps.value,
            query.value,
            permissions.statuses.value.isGranted(AppPermission.NotificationAccess),
            phone.enabled.value,
            catalog.loading.value,
        ),
    )

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun onToggle(appId: String, enabled: Boolean) {
        catalog.setEnabled(appId, enabled)
    }

    /** "전화" 줄의 토글. 켜면 전화 권한을 요청한다. */
    fun onTogglePhone(enabled: Boolean) {
        viewModelScope.launch { phone.setEnabled(enabled) }
    }

    /** AP-05 "권한 설정으로 이동". 허용하고 돌아오면 목록이 나온다. */
    fun onOpenAccessSettings() = permissions.openSettings(AppPermission.NotificationAccess)

    private fun filter(
        apps: List<ControllableApp>,
        query: String,
        accessGranted: Boolean,
        phoneEnabled: Boolean,
        loading: Boolean,
    ): AppListUiState {
        val shown = apps.filter { it.name.contains(query.trim(), ignoreCase = true) }
        return AppListUiState(
            inUse = shown.filter { it.enabled },
            installed = shown.filterNot { it.enabled },
            accessGranted = accessGranted,
            loading = loading,
            showPhone = query.isBlank(),
            phoneEnabled = phoneEnabled,
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                AppListViewModel(
                    catalog = AppContainer.appCatalog,
                    permissions = AppContainer.systemPermissions,
                    phone = AppContainer.phoneControl,
                )
            }
        }
    }
}
