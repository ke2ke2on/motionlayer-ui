package com.uhi.motionlayer.data.fake

import com.uhi.motionlayer.domain.model.Account
import com.uhi.motionlayer.domain.model.DebugAction
import com.uhi.motionlayer.domain.model.SignInResult
import com.uhi.motionlayer.domain.source.AccountSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 가짜 계정. 앱을 켤 때마다 로그인하지 않은 상태로 시작하고, 로그인하면 0.6초 뒤 Figma 예시의 "허윤"으로 로그인된다.
 * 계정 선택 창(시스템)과 서버는 없다. 실패하는 모습은 시연용 동작([debugActions])으로 만든다.
 */
class FakeAccountSession(initiallySignedIn: Boolean = false) : AccountSession {
    private val _account = MutableStateFlow(if (initiallySignedIn) SampleData.account else null)
    override val account: StateFlow<Account?> = _account.asStateFlow()

    /** 다음 로그인 시도를 실패시킬지(시연용). 한 번 실패시키면 다시 꺼진다. */
    private var failNext = false

    override suspend fun signIn(): SignInResult {
        delay(SIGN_IN_DELAY_MILLIS)
        if (failNext) {
            failNext = false
            return SignInResult.Failed
        }
        _account.value = SampleData.account
        return SignInResult.SignedIn
    }

    override fun signOut() {
        _account.value = null
    }

    /** 로그인 화면(ON-10)의 일러스트를 길게 누르면(디버그 빌드) 열리는 창에 나온다. */
    val debugActions: List<DebugAction> = listOf(
        DebugAction("로그인 · 다음 시도 실패시키기 (ON-10)") { failNext = true },
    )

    private companion object {
        const val SIGN_IN_DELAY_MILLIS = 600L
    }
}
