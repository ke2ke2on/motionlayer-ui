package com.uhi.motionlayer.tv

import com.uhi.motionlayer.tv.data.fake.FakeTvConnections
import com.uhi.motionlayer.tv.data.fake.FakeTvOverlay
import com.uhi.motionlayer.tv.data.fake.FakeTvSetup
import com.uhi.motionlayer.tv.data.fake.TvSampleData
import com.uhi.motionlayer.tv.data.fake.fakeQrPattern
import com.uhi.motionlayer.tv.domain.TvKind
import com.uhi.motionlayer.tv.ui.MAX_PERSON_ROWS
import com.uhi.motionlayer.tv.ui.TvScreen
import com.uhi.motionlayer.tv.ui.personRows
import com.uhi.motionlayer.tv.ui.tvScreenFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** TV 화면이 어느 것을 보여 줄지와, 가짜 구현이 시안대로 움직이는지 본다. */
class TvLogicTest {

    @Test
    fun showsSetupUntilTheKindIsChosen() {
        assertEquals(TvScreen.Setup, tvScreenFor(kind = null, people = emptyList()))
        // 성격을 안 골랐으면 사람이 연결돼 있어도 먼저 고르게 한다
        assertEquals(TvScreen.Setup, tvScreenFor(kind = null, people = TvSampleData.people))
    }

    @Test
    fun showsQrWhenNobodyIsConnectedAndPeopleWhenSomeoneIs() {
        assertEquals(TvScreen.Pairing, tvScreenFor(kind = TvKind.Personal, people = emptyList()))
        assertEquals(TvScreen.Pairing, tvScreenFor(kind = TvKind.Public, people = emptyList()))
        assertEquals(TvScreen.Connections, tvScreenFor(kind = TvKind.Personal, people = TvSampleData.people))
    }

    @Test
    fun choosingAKindLeavesTheSetupScreen() {
        val setup = FakeTvSetup()
        assertNull(setup.kind.value)
        setup.choose(TvKind.Public)
        assertEquals(TvKind.Public, setup.kind.value)
        assertEquals(TvScreen.Pairing, tvScreenFor(setup.kind.value, emptyList()))
    }

    @Test
    fun demoConnectsTheTwoPeopleFromFigma() {
        val connections = FakeTvConnections()
        assertTrue(connections.people.value.isEmpty())
        connections.demoActions.first { "TV-03" in it.label }.run()
        assertEquals(listOf("허윤", "민수"), connections.people.value.map { it.name })
        connections.demoActions.first { "모두" in it.label }.run()
        assertTrue(connections.people.value.isEmpty())
    }

    @Test
    fun drawsEveryoneWhenTheyFitInThreeRows() {
        val rows = personRows(TvSampleData.crowd.take(MAX_PERSON_ROWS))
        assertEquals(3, rows.shown.size)
        assertTrue(rows.rest.isEmpty())
        assertTrue(personRows(emptyList()).shown.isEmpty())
    }

    @Test
    fun groupsTheRestIntoTheLastRowWhenThereAreTooMany() {
        val rows = personRows(TvSampleData.crowd)
        // 줄은 3개: 두 사람 + "그 밖에 3명"
        assertEquals(listOf("민수", "지수"), rows.shown.map { it.name })
        assertEquals(listOf("서연", "도윤", "하준"), rows.rest.map { it.name })
        assertEquals(MAX_PERSON_ROWS, rows.shown.size + 1)
    }

    @Test
    fun effectCountsDownFromThreeAndCloses() {
        val scope = CoroutineScope(Dispatchers.Default)
        try {
            val overlay = FakeTvOverlay(scope)
            overlay.demoActions.first { "TV-06" in it.label }.run()
            val seconds = runBlocking {
                withTimeout(6_000) {
                    overlay.state.map { it.effect?.secondsLeft }
                        .distinctUntilChanged()
                        .dropWhile { it == null } // 띄우기 전의 "없음"은 빼고 센다
                        .transformWhile { emit(it); it != null } // 닫힐 때(null)까지
                        .toList()
                }
            }
            // 3 → 2 → 1 → 닫힘
            assertEquals(listOf(3, 2, 1, null), seconds)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun operatorNoticeDisappearsByItself() {
        val scope = CoroutineScope(Dispatchers.Default)
        try {
            val overlay = FakeTvOverlay(scope)
            overlay.demoActions.first { "TV-04" in it.label }.run()
            runBlocking {
                withTimeout(1_000) { overlay.state.first { it.operator != null } }
                withTimeout(4_000) { overlay.state.first { it.operator == null } }
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun fakeQrHasThreeCornerSquares() {
        val qr = fakeQrPattern(seed = 1)
        val last = qr.size - 1
        assertTrue(qr.isDark(0, 0) && qr.isDark(3, 3))
        assertTrue(qr.isDark(last, 0) && qr.isDark(last - 3, 3))
        assertTrue(qr.isDark(0, last) && qr.isDark(3, last - 3))
        assertFalse(qr.isDark(1, 1))
    }
}
