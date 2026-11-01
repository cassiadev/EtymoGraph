package io.github.etymograph.graph

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraphLayoutTest {
    private val modern = 3
    private val ancient = 2

    @Test
    fun nodesInTheSameBandNeverOverlap() {
        val placed = GraphLayout.placeInBands(List(6) { Slot(modern, side = -1, preferredX = -260f) }, emptyList())

        for (i in placed.indices) for (j in i + 1 until placed.size) {
            assertTrue(abs(placed[i].x - placed[j].x) >= GraphLayout.MIN_DX, "겹침: ${placed[i]}, ${placed[j]}")
        }
    }

    @Test
    fun bandDecidesHeightAndFamilySideKeepsLanesApart() {
        val slots = listOf(Slot(0, side = 0, preferredX = 0f), Slot(1, side = -1, preferredX = 0f), Slot(ancient, side = 1, preferredX = -500f))

        val (pie, germanic, italic) = GraphLayout.placeInBands(slots, emptyList())

        assertEquals(GraphLayout.bandCenterY(0), pie.y)
        assertEquals(GraphLayout.bandCenterY(1), germanic.y)
        assertEquals(GraphLayout.bandCenterY(ancient), italic.y)
        // 바라는 위치가 다른 칸 쪽이어도 자기 어파 칸 안에 놓인다.
        assertTrue(germanic.x <= -GraphLayout.LANE_INNER)
        assertTrue(italic.x >= GraphLayout.LANE_INNER)
    }

    @Test
    fun ancestorGoesStraightAboveWhenTheSpotIsFree() {
        val anchor = Point(-260f, GraphLayout.bandCenterY(modern))

        val (above) = GraphLayout.placeInBands(listOf(Slot(ancient, side = -1, preferredX = anchor.x)), listOf(anchor))

        assertEquals(Point(-260f, GraphLayout.bandCenterY(ancient)), above)
    }

    @Test
    fun takenSpotsAreSkippedOutwardFirst() {
        val y = GraphLayout.bandCenterY(modern)
        val occupied = listOf(Point(-260f, y), Point(260f, y))

        val (left, right) = GraphLayout.placeInBands(
            listOf(Slot(modern, side = -1, preferredX = -260f), Slot(modern, side = 1, preferredX = 260f)),
            occupied,
        )

        assertEquals(Point(-260f - GraphLayout.MIN_DX, y), left)
        assertEquals(Point(260f + GraphLayout.MIN_DX, y), right)
    }

    @Test
    fun layoutIsDeterministic() {
        val slots = listOf(Slot(modern, -1, -260f), Slot(modern, -1, -260f), Slot(ancient, 1, 260f), Slot(0, 0, 0f))
        assertEquals(GraphLayout.placeInBands(slots, emptyList()), GraphLayout.placeInBands(slots, emptyList()))
    }
}
