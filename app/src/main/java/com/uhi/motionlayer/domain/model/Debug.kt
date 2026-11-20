package com.uhi.motionlayer.domain.model

/**
 * 화면 시연·확인용으로 쓰는 버튼 하나(디버그 빌드에서만 보인다). 센서가 없는 에뮬레이터에서 상태를 바꿔 보게 해 준다.
 *
 * A·B·C가 구현할 계약은 아니다. 가짜 구현(`data/fake`)이 목록을 만들고 `AppContainer`가 화면에 넘긴다.
 * 실제 구현으로 바꾸면 빈 목록을 넘기면 되고, 그러면 시연용 창은 열리지 않는다.
 * 화면이 `data/fake`를 가져다 쓰지 않게 하려고 여기에 둔다.
 */
class DebugAction(val label: String, val run: () -> Unit)
