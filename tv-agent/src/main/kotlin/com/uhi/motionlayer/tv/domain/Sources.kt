package com.uhi.motionlayer.tv.domain

import kotlinx.coroutines.flow.StateFlow

// TV 화면이 쓰는 계약. 연결을 맡은 C가 구현하고, 지금은 `data/fake`가 대신한다.
// 화면은 이 인터페이스만 보고, 어느 구현을 쓸지는 `TvAppContainer` 한 곳에서 고른다(폰 앱과 같은 방식).

/** 이 TV의 성격(TV-01). 아직 고르지 않았으면 null. */
interface TvSetup {
    val kind: StateFlow<TvKind?>

    /** 리모컨으로 카드를 골랐을 때. */
    fun choose(kind: TvKind)
}

/** 폰이 찍을 QR과 6자리 코드(TV-02). */
interface TvPairing {
    val code: StateFlow<TvPairingCode>
}

/** TV 이름과 연결된 사람들(TV-03). */
interface TvConnections {
    val tvName: StateFlow<String>
    /**
     * 연결된 사람. 화면에 보일 순서대로 준다(주인과 지금 조작 중인 사람을 앞에).
     * TV-03은 3줄까지만 그려서, 4명 이상이면 앞의 2명만 한 줄씩 보이고 나머지는 "그 밖에 N명"으로 묶인다.
     */
    val people: StateFlow<List<ConnectedPerson>>
}

/** 재생 중인 화면 위에 겹쳐 그릴 것(TV-04~06). */
interface TvOverlay {
    val state: StateFlow<TvOverlayState>
}
