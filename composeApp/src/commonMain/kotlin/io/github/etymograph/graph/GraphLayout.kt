package io.github.etymograph.graph

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** 월드 좌표(단위 dp). 화면 좌표 변환은 UI 쪽 Viewport가 맡는다. */
data class Point(val x: Float, val y: Float)

/**
 * 새 노드 하나를 놓을 자리 요청. [band]는 시대 층 번호(0이 가장 오래됨), [side]는 어파 칸
 * (-1 왼쪽, 1 오른쪽, 0 어느 어파에도 속하지 않음), [preferredX]는 가장 바라는 가로 위치다.
 */
data class Slot(val band: Int, val side: Int, val preferredX: Float)

/**
 * 시대 층(세로)과 어파 칸(가로)에 맞춘 결정적 증분 배치. 세로 위치는 층이 정하고, 가로 위치는 바라는 위치에서
 * 가장 가까운 빈자리를 찾는다. 이미 놓인 노드는 움직이지 않고, 같은 입력에는 항상 같은 좌표를 돌려준다.
 */
object GraphLayout {
    /** 같은 층의 두 노드 중심이 이만큼 떨어지면 겹치지 않는다(노드 폭 136dp + 여백). */
    const val MIN_DX = 156f

    /** 어파 칸에 속한 노드의 중심이 가운데 기준선(x = 0)에서 떨어져야 하는 최소 거리. 왼쪽·오른쪽 칸이 겹치지 않게 한다. */
    const val LANE_INNER = 90f

    /** 다른 어파 칸으로 넘어가는 노드를 처음 놓아 보는 가로 위치(칸 쪽 부호를 곱해 쓴다). */
    const val LANE_HOME = 260f

    /** 층의 위·아래 경계(월드 y). 층 i는 BAND_EDGES[i]..BAND_EDGES[i + 1]이다. 층 전체의 가운데가 y = 0 근처에 오도록 잡았다. */
    val BAND_EDGES = floatArrayOf(-415f, -225f, -35f, 185f, 415f)

    fun bandCenterY(band: Int): Float = (BAND_EDGES[band] + BAND_EDGES[band + 1]) / 2

    fun homeX(side: Int): Float = side * LANE_HOME

    fun placeInBands(slots: List<Slot>, occupied: Collection<Point>): List<Point> {
        val taken = occupied.toMutableList()
        return slots.map { slot -> freeSpotOf(slot, taken).also { taken += it } }
    }

    private fun freeSpotOf(slot: Slot, taken: List<Point>): Point {
        val y = bandCenterY(slot.band)
        val start = when {
            slot.side < 0 -> min(slot.preferredX, -LANE_INNER)
            slot.side > 0 -> max(slot.preferredX, LANE_INNER)
            else -> slot.preferredX
        }
        // 바깥쪽(가운데 기준선에서 먼 쪽)부터 찾아, 가운데의 공통 조상 자리와 다른 어파 칸 쪽을 비워 둔다.
        val outward = if (slot.side < 0) -1f else 1f
        val inSameBand = taken.filter { abs(it.y - y) < 1f }
        // 노드 하나는 후보 자리를 많아야 두 개 막으므로, 바깥쪽으로 이만큼 가면 반드시 빈자리가 있다.
        for (k in 0..(2 * inSameBand.size + 1)) {
            for (sign in if (k == 0) listOf(1f) else listOf(outward, -outward)) {
                val x = start + sign * k * MIN_DX
                val insideLane = when {
                    slot.side < 0 -> x <= -LANE_INNER
                    slot.side > 0 -> x >= LANE_INNER
                    else -> true
                }
                if (insideLane && inSameBand.none { abs(it.x - x) < MIN_DX }) return Point(x, y)
            }
        }
        error("바깥쪽에 빈자리가 반드시 있어야 한다")
    }
}
