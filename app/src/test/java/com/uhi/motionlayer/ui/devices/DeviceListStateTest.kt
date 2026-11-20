package com.uhi.motionlayer.ui.devices

import com.uhi.motionlayer.data.fake.SampleData
import com.uhi.motionlayer.domain.model.DeviceKind
import com.uhi.motionlayer.domain.model.PcConnectionState
import com.uhi.motionlayer.domain.model.TemporaryDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeviceListStateTest {
    private val guestTv = TemporaryDevice("jisu_tv", "지수네 TV", DeviceKind.Tv, "손님 · 같은 와이파이")

    /** 내 기기인 "윤의 MacBook"은 "이번만 연결"에 다시 나오지 않는다(Figma DV-01: 내 기기 3 + 이번만 연결 1). */
    @Test
    fun recentPcThatIsMyDevice_isNotListedAsTemporary() {
        val state = deviceListState(
            signedIn = true,
            myDevices = SampleData.myDevices,
            temporaryDevices = emptyList(),
            pc = PcConnectionState.Disconnected,
            recentPcs = SampleData.recentPcs,
        )

        assertEquals(listOf("강의실 PC"), state.recentPcs.map { it.name })
        assertEquals(3, state.myDevices.size)
    }

    /** 로그인하지 않으면 내 기기가 없으니 전에 연결했던 PC가 모두 "이번만 연결"에 남는다. */
    @Test
    fun signedOut_keepsEveryRecentPc() {
        val state = deviceListState(
            signedIn = false,
            myDevices = emptyList(),
            temporaryDevices = emptyList(),
            pc = PcConnectionState.Disconnected,
            recentPcs = SampleData.recentPcs,
        )

        assertEquals(listOf("강의실 PC", "윤의 MacBook"), state.recentPcs.map { it.name })
        assertEquals(2, state.temporaryCount)
    }

    /** 지금 연결된 PC가 내 기기이면 별도 줄로 또 나오지 않고(내 기기 줄이 연결됨으로 보인다), 아니면 맨 위에 나온다. */
    @Test
    fun connectedPc_isListedOnlyWhenItIsNotMyDevice() {
        val asMine = deviceListState(
            signedIn = true,
            myDevices = SampleData.myDevices,
            temporaryDevices = emptyList(),
            pc = SampleData.classroomPc.copy(deviceName = "윤의 MacBook"),
            recentPcs = emptyList(),
        )
        assertNull(asMine.connectedPc)

        val temporary = deviceListState(
            signedIn = true,
            myDevices = SampleData.myDevices,
            temporaryDevices = emptyList(),
            pc = SampleData.classroomPc,
            recentPcs = emptyList(),
        )
        assertEquals("강의실 PC", temporary.connectedPc?.deviceName)
    }

    /** 지금 연결된 PC는 "전에 연결했던 PC"에 같은 이름이 있어도 한 줄로만 보인다(연결된 줄). */
    @Test
    fun connectedPc_isNotRepeatedAmongRecentPcs() {
        val state = deviceListState(
            signedIn = false,
            myDevices = emptyList(),
            temporaryDevices = emptyList(),
            pc = SampleData.classroomPc,
            recentPcs = SampleData.recentPcs,
        )

        assertEquals("강의실 PC", state.connectedPc?.deviceName)
        assertEquals(listOf("윤의 MacBook"), state.recentPcs.map { it.name })
        assertEquals(2, state.temporaryCount)
    }

    /** "이번만 연결 N"의 N은 연결된 PC + 전에 연결했던 PC + 이번만 연결한 TV 등을 모두 센다. */
    @Test
    fun temporaryCount_addsConnectedRecentAndGuestDevices() {
        val state = deviceListState(
            signedIn = false,
            myDevices = emptyList(),
            temporaryDevices = listOf(guestTv),
            pc = SampleData.classroomPc,
            recentPcs = SampleData.recentPcs,
        )

        // 연결된 강의실 PC 1 + 전에 연결했던 PC(강의실 PC는 연결된 줄과 같아 제외, 윤의 MacBook) 1 + 지수네 TV 1
        assertEquals(3, state.temporaryCount)
    }

    @Test
    fun noTemporaryDevices_hasZeroCount() {
        val state = deviceListState(
            signedIn = true,
            myDevices = SampleData.myDevices,
            temporaryDevices = emptyList(),
            pc = PcConnectionState.Disconnected,
            recentPcs = emptyList(),
        )

        assertEquals(0, state.temporaryCount)
    }
}
