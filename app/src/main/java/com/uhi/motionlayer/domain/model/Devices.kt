package com.uhi.motionlayer.domain.model

/** 기기의 종류. 아이콘과 설명 문구를 정한다(DV-01·04). */
enum class DeviceKind { Pc, Tv }

/** 내 기기의 지금 상태. DV-01 줄의 설명과 아이콘 타일 색(연결됨만 파랑)을 정한다. */
sealed interface MyDeviceStatus {
    /** 같은 와이파이로 연결돼 있다("연결됨 · 로컬 12ms"). */
    data class Connected(val latencyMs: Int) : MyDeviceStatus

    /** 꺼져 있다. 조작할 수 없다. */
    data object Off : MyDeviceStatus

    /** 꺼져 있지는 않고, 집 밖에서도 서버를 거쳐 조작할 수 있다("밖에서 조작 가능 · 서버 경유"). */
    data object RemoteReachable : MyDeviceStatus
}

/**
 * 계정에 등록한 "내 기기"(DV-01 "내 기기", ST-05 "내 기기 N대").
 * 등록하지 않고 그때만 연결하는 PC는 이 목록에 들지 않는다(`PcLink`가 맡는 "이번만 연결").
 *
 * @param platform 기기 종류 이름("Android TV", "macOS" 등). DV-04 이름 아래 "Android TV · 내 기기"에 쓴다
 */
data class MyDevice(
    val id: String,
    val name: String,
    val kind: DeviceKind,
    val platform: String,
    val status: MyDeviceStatus,
)

/** 기기에 연결된 사람이 누구인지. [Me]는 이 폰의 주인, [Guest]는 손님으로 들어온 다른 사람. */
enum class ConnectedPersonRole { Me, Guest }

/** 연결된 사람이 어디서 연결했는지. */
enum class PersonPlace { ThisPhone, SameWifi, Remote }

/**
 * 기기에 연결된 사람 한 줄(DV-04 "연결된 사람 N").
 *
 * @param controlling 지금 이 기기를 조작하는 사람인지. 동시 조작을 허용하지 않으면 한 사람만 true다
 */
data class ConnectedPerson(
    val id: String,
    val name: String,
    val role: ConnectedPersonRole,
    val place: PersonPlace,
    val controlling: Boolean,
)

/**
 * 내 기기 하나의 자세한 값(DV-04). 서버에 저장되는 값이라 C가 채운다.
 *
 * @param selectVibrationLabel 이 기기를 방향으로 고를 때 울리는 진동의 이름("짧게-길게"). 진동을 만드는 것은 B가 한다
 * @param registeredDirections 방향으로 고르려고 등록해 둔 방향 수("2곳")
 */
data class MyDeviceDetail(
    val device: MyDevice,
    val allowGuests: Boolean,
    val allowSimultaneous: Boolean,
    val allowRemote: Boolean,
    val connectedPeople: List<ConnectedPerson>,
    val selectVibrationLabel: String,
    val registeredDirections: Int,
)

/** QR(코드)로 찍은 기기의 주인이 누구인지. 어떻게 연결할지 묻는 시트(DV-03)를 띄울지가 여기서 갈린다. */
enum class DeviceOwnership {
    /** 주인이 없다. 내 기기로 등록하거나 이번만 연결한다(DV-03). */
    Unowned,

    /** 다른 사람의 기기다. 시트 없이 바로 이번만(손님으로) 연결한다. */
    OwnedByOther,

    /** 공용 기기다. "내 기기로 등록"은 아예 나오지 않고 바로 이번만 연결한다. */
    Public,
}

/** QR(코드)로 찍어서 알아낸 기기. 아직 연결하거나 등록하지 않았다. */
data class ScannedDevice(
    val id: String,
    val name: String,
    val kind: DeviceKind,
    val ownership: DeviceOwnership,
)

/**
 * 등록하지 않고 이번만 연결한 기기(DV-01 "이번만 연결"). PC는 `PcLink`가 따로 세므로 여기에는 TV 같은 것만 든다.
 *
 * @param description 이름 아래 설명("손님 · 같은 와이파이")
 */
data class TemporaryDevice(
    val id: String,
    val name: String,
    val kind: DeviceKind,
    val description: String,
)

/**
 * 주변에서 찾은, 손님으로 연결할 수 있는 다른 사람의 기기(DV-06). 주인이 손님 연결을 허용해 둔 기기만 보인다.
 *
 * @param owner 주인의 이름("지수"). null이면 공용 기기다
 */
data class NearbyDevice(
    val id: String,
    val name: String,
    val kind: DeviceKind,
    val owner: String?,
)

/**
 * 방향으로 고를 수 있는 후보 한 줄(DV-07).
 *
 * @param bearingDegrees 폰 앞쪽을 0°로 시계 방향으로 잰 기기가 있는 방향(0~360)
 * @param distanceFraction 레이더 바깥 고리까지를 1로 본 거리(0~1). 가까울수록 작다. 정확하지 않아도 되는 어림값이다
 */
data class DirectionCandidate(
    val id: String,
    val name: String,
    val kind: DeviceKind,
    val bearingDegrees: Float,
    val distanceFraction: Float,
)

/** 방향으로 후보를 골랐을 때(DV-07) 이어질 일. */
sealed interface DirectionPick {
    /** 원격 조작 화면(DV-08)을 연다. */
    data class OpenRemote(val deviceId: String) : DirectionPick

    /** 조작 대상이 바뀌었다(홈에 이미 반영됨). 화면은 그냥 닫는다. */
    data object TargetChanged : DirectionPick

    /** 골랐지만 쓸 수 없다(꺼져 있음·연결 안 됨). 이유는 토스트로 알렸고 화면은 그대로 둔다. */
    data object Unavailable : DirectionPick
}

/** 리모컨 버튼(DV-08). 입력 전환·전원, 음량 −·+, 채널 −·+. */
enum class RemoteKey { InputSwitch, Power, VolumeDown, VolumeUp, ChannelDown, ChannelUp }

/**
 * 원격으로 조작하는 중인 기기의 연결 상태(DV-08).
 *
 * @param route 같은 와이파이로 바로(로컬)인지 서버를 거치는지. 서버 경유는 응답이 느리다(분석서 NFR-01: 500ms 이내)
 * @param latencyMs 버튼을 누르고 기기가 응답하기까지의 시간(ms)
 */
data class RemoteSession(
    val deviceName: String,
    val route: ConnectionRoute,
    val latencyMs: Int,
)
