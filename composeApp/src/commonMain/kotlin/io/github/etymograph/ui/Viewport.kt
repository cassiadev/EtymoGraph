package io.github.etymograph.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.pow

/**
 * 그래프 화면의 이동·확대 상태. 화면 좌표 = 캔버스 중심 + [pan] + 월드 좌표(px) × [scale].
 * 지도 데이터(ExplorerState)와 분리해 둔다.
 */
data class Viewport(val pan: Offset = Offset.Zero, val scale: Float = 1f) {
    fun withPanBy(delta: Offset): Viewport = copy(pan = pan + delta)

    /** [focus](캔버스 중심 기준 화면 좌표) 아래의 지점이 확대·축소 후에도 같은 자리에 남도록 [pan]을 보정한다. */
    fun withZoom(factor: Float, focus: Offset): Viewport {
        val newScale = (scale * factor).coerceIn(MIN_SCALE, MAX_SCALE)
        return Viewport(pan = focus - (focus - pan) * (newScale / scale), scale = newScale)
    }

    /**
     * detectTransformGestures 한 번의 변화량을 적용한다. Compose는 이전 프레임의 중심점을 넘기므로
     * 그 점을 기준으로 먼저 확대하고, 손가락 이동량은 확대 배율과 무관하게 더한다.
     */
    fun withTransformGesture(previousCentroid: Offset, pan: Offset, zoom: Float): Viewport =
        withZoom(zoom, previousCentroid).withPanBy(pan)

    /**
     * 월드 좌표(px)의 [bounds]가 화면 안쪽([margin]만큼 띄움)에 들어오도록 [pan]만 최소한으로 옮긴다.
     * 이미 보이면 그대로 두고, 화면보다 크면 가운데를 맞춘다. 배율은 바꾸지 않는다.
     */
    fun withBoundsInView(bounds: Rect, viewSize: Size, margin: Float): Viewport {
        fun shiftOf(start: Float, end: Float, halfView: Float): Float {
            val min = -halfView + margin
            val max = halfView - margin
            return when {
                end - start > max - min -> -(start + end) / 2
                start < min -> min - start
                end > max -> max - end
                else -> 0f
            }
        }
        val dx = shiftOf(pan.x + bounds.left * scale, pan.x + bounds.right * scale, viewSize.width / 2)
        val dy = shiftOf(pan.y + bounds.top * scale, pan.y + bounds.bottom * scale, viewSize.height / 2)
        return if (dx == 0f && dy == 0f) this else withPanBy(Offset(dx, dy))
    }

    /**
     * [group]이 화면에 보이도록 하는 움직임. 화면보다 크고 [allowZoomOut]이면 다 들어갈 만큼만 축소하고(확대는 하지 않음),
     * 최소 배율로도 넘치면 묶음 가운데를 맞춘다. 어느 경우든 [keepVisible]은 화면 안에 둔다.
     */
    fun movementToShow(group: Rect, keepVisible: Rect?, viewSize: Size, margin: Float, allowZoomOut: Boolean): ViewportMovement {
        val fitScale = minOf((viewSize.width - 2 * margin) / group.width, (viewSize.height - 2 * margin) / group.height)
        val newScale = if (allowZoomOut) minOf(scale, fitScale).coerceAtLeast(MIN_SCALE) else scale
        val focus = pan + group.center * scale
        val zoomed = withZoom(newScale / scale, focus)
        val shown = zoomed.withBoundsInView(group, viewSize, margin)
            .let { v -> keepVisible?.let { v.withBoundsInView(it, viewSize, margin) } ?: v }
        return ViewportMovement(newScale / scale, focus, shown.pan - zoomed.pan)
    }

    /** "전체 보기": [group] 전체가 화면 가운데에 오도록 맞춘다. 작은 묶음을 기본 배율(1배)보다 크게 확대하지는 않는다. */
    fun movementToFit(group: Rect, viewSize: Size, margin: Float): ViewportMovement {
        val fitScale = minOf((viewSize.width - 2 * margin) / group.width, (viewSize.height - 2 * margin) / group.height)
        val newScale = fitScale.coerceIn(MIN_SCALE, 1f)
        val focus = pan + group.center * scale
        // focus 기준으로 배율을 바꾸면 묶음 가운데가 그 자리에 남으므로, 그만큼 되돌려 화면 가운데로 옮긴다.
        return ViewportMovement(newScale / scale, focus, -focus)
    }

    companion object {
        const val MIN_SCALE = 0.3f
        const val MAX_SCALE = 3f
    }
}

/**
 * 화면 하나의 움직임을 "[focus] 기준 [zoom]배 확대 후 [pan]만큼 이동"으로 나타낸 것.
 * 애니메이션은 구간별 변화량만 적용하므로, 그 사이 사용자 드래그가 있어도 서로 덮어쓰지 않는다.
 */
class ViewportMovement(private val zoom: Float, private val focus: Offset, private val pan: Offset) {
    val isNone: Boolean get() = zoom == 1f && pan == Offset.Zero

    /** 진행률 [from]→[to] 구간의 변화량. 0에서 1까지 구간을 나눠 차례로 모두 적용하면 전체 움직임과 같다. */
    fun stepBetween(from: Float, to: Float): (Viewport) -> Viewport = { v ->
        // 이미 적용된 이동량(pan × from)만큼 확대 기준점도 함께 옮겨야 구간을 나눠도 결과가 같다.
        v.withZoom(zoom.pow(to - from), focus + pan * from).withPanBy(pan * (to - from))
    }
}
