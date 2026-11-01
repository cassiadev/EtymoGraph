package io.github.etymograph.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EdgeGeometryTest {
    private fun box(cx: Float, cy: Float) = Rect(Offset(cx - 68f, cy - 31f), Offset(cx + 68f, cy + 31f))

    @Test
    fun ancestorAboveIsJoinedFromItsBottomToTheDescendantsTop() {
        val ancestor = box(0f, 0f)
        val descendant = box(200f, 300f)

        val c = curveBetween(ancestor, descendant)

        assertEquals(Offset(0f, ancestor.bottom), c.start)
        assertEquals(Offset(200f, descendant.top), c.end)
        assertTrue(c.endDirection().y > 0.9f, "화살촉이 아래를 향해야 한다: ${c.endDirection()}")
    }

    @Test
    fun wordsInTheSameRowAreJoinedByAnArcOverTheirTops() {
        val left = box(0f, 0f)
        val right = box(400f, 0f)

        val c = curveBetween(right, left)

        // 윗면 가운데(윗 층에서 오는 선이 닿는 자리)에서 서로 마주 보는 쪽으로 너비의 1/4만큼 비킨다.
        assertEquals(Offset(400f - 34f, right.top), c.start)
        assertEquals(Offset(34f, left.top), c.end)
        // 사이(x = 200)에 다른 단어가 있어도 그 상자 위를 넘어가야 한다.
        assertTrue(c.pointAt(0.5f).y < right.top - 31f, "사이에 있는 단어 위로 넘어가야 한다: ${c.pointAt(0.5f)}")
        assertTrue(c.endDirection().y > 0.5f, "화살촉은 위에서 단어로 들어가야 한다: ${c.endDirection()}")
    }
}
