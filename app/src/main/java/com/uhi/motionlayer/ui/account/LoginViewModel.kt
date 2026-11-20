package com.uhi.motionlayer.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uhi.motionlayer.AppContainer
import com.uhi.motionlayer.domain.model.SignInResult
import com.uhi.motionlayer.domain.source.AccountSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 로그인 화면(ON-10)이 쓰는 ViewModel. 로그인을 부탁하고, 시도 중인지·실패했는지만 들고 있다.
 * 로그인되면 어디로 갈지는 화면을 연 쪽(첫 실행 안내나 설정)이 정해서 [signIn]에 넘긴다.
 */
class LoginViewModel(private val session: AccountSession) : ViewModel() {
    private val _signingIn = MutableStateFlow(false)

    /** 로그인하는 중인지. 계정 선택 창이 떠 있거나 서버를 기다리는 동안 true. */
    val signingIn: StateFlow<Boolean> = _signingIn.asStateFlow()

    private val _failed = MutableStateFlow(false)

    /** 마지막 시도가 실패했는지. 다시 시도하면 지워진다. */
    val failed: StateFlow<Boolean> = _failed.asStateFlow()

    /** "Google로 계속하기"를 눌렀다. 로그인되면 [onSignedIn]을 부른다. 이미 시도 중이면 무시한다. */
    fun signIn(onSignedIn: () -> Unit) {
        if (_signingIn.value) return
        _signingIn.value = true
        _failed.value = false
        viewModelScope.launch {
            val result = try {
                session.signIn()
            } finally {
                _signingIn.value = false
            }
            when (result) {
                SignInResult.SignedIn -> onSignedIn()
                SignInResult.Failed -> _failed.value = true
                SignInResult.Cancelled -> Unit
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { LoginViewModel(session = AppContainer.accountSession) }
        }
    }
}
