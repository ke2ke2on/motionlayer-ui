package com.uhi.motionlayer.domain.model

/** 연결 경로. 같은 와이파이로 바로(로컬) 또는 서버를 거쳐서(FR-03-1, 표 4-2). */
enum class ConnectionRoute { Local, Server }

/** PC 연결 상태. 연결과 응답 시간 측정은 PC 연결 담당(C)이 한다. */
sealed interface PcConnectionState {
    data object Disconnected : PcConnectionState

    /**
     * @param latencyMs 제스처를 보내고 응답(ack)을 받기까지의 시간(ms)
     * @param bindings 연결된 동안 적용되는 제스처-명령 목록(발표 모드)
     */
    data class Connected(
        val deviceName: String,
        val route: ConnectionRoute,
        val latencyMs: Int,
        val bindings: List<GestureBinding> = emptyList(),
    ) : PcConnectionState
}
