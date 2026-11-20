package com.uhi.motionlayer.domain.model

/** 로그인한 계정(ON-10, ST-01·05). 화면에는 이름과 로그인에 쓴 서비스만 보여 준다. */
data class Account(
    val name: String,
    val provider: AccountProvider,
)

/** 로그인에 쓴 서비스. Figma에는 Google 하나뿐이다. */
enum class AccountProvider { Google }

/** 로그인 시도의 결과(ON-10). 화면은 이 값에 따라 다음으로 가거나, 그대로 있거나, 실패를 알린다. */
enum class SignInResult {
    /** 로그인됐다. */
    SignedIn,

    /** 사용자가 계정을 고르는 시스템 창을 닫았다. 화면은 아무 말 없이 그대로 둔다. */
    Cancelled,

    /** 인터넷이 없거나 서버가 응답하지 않았다. */
    Failed,
}
