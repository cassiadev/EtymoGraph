package io.github.etymograph.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.abs

/** 화면 좌표의 3차 베지어 곡선 하나. 연결선과 그 라벨·화살촉 위치를 함께 정한다. */
internal class Curve(val start: Offset, val c1: Offset, val c2: Offset, val end: Offset) {
    fun pointAt(t: Float): Offset {
        val u = 1 - t
        return start * (u * u * u) + c1 * (3 * u * u * t) + c2 * (3 * u * t * t) + end * (t * t * t)
    }

    /** 끝점에서 선이 나아가는 방향(단위 벡터). 화살촉 방향에 쓴다. */
    fun endDirection(): Offset {
        val tangent = end - c2
        val v = if (tangent.getDistance() > 0.01f) tangent else end - start
        val length = v.getDistance()
        return if (length > 0f) v / length else Offset(0f, 1f)
    }
}

/**
 * 두 노드 상자를 잇는 곡선. [from]이 선의 시작(조상·출처)이다.
 * 층이 다르면 위아래 면의 가운데를 S자로 잇는다. 같은 층이면 두 단어 위로 넘어가는 호로 이어,
 * 사이에 있는 단어를 가로지르거나 그 단어와 이어진 것처럼 보이지 않게 하고 라벨 둘 자리도 남긴다.
 */
internal fun curveBetween(from: Rect, to: Rect): Curve {
    val sameRow = abs(from.center.y - to.center.y) < from.height / 2
    if (!sameRow) {
        val down = to.center.y > from.center.y
        val start = Offset(from.center.x, if (down) from.bottom else from.top)
        val end = Offset(to.center.x, if (down) to.top else to.bottom)
        val bend = (end.y - start.y) * 0.55f
        return Curve(start, Offset(start.x, start.y + bend), Offset(end.x, end.y - bend), end)
    }
    // 윗면 가운데는 윗 층에서 내려오는 선이 닿는 자리라, 마주 보는 쪽으로 너비의 1/4만큼 비켜 잇는다.
    val toward = if (to.center.x > from.center.x) 1f else -1f
    val start = Offset(from.center.x + toward * from.width / 4, from.top)
    val end = Offset(to.center.x - toward * to.width / 4, to.top)
    val dx = end.x - start.x
    // 멀리 떨어진 단어끼리일수록 높이 띄우되, 층 안(노드 위 여백)을 넘지 않게 제한한다.
    val lift = minOf(abs(dx) * 0.35f, from.height * 1.1f)
    return Curve(start, Offset(start.x + dx * 0.15f, start.y - lift), Offset(end.x - dx * 0.15f, end.y - lift), end)
}
