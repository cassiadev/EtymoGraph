package io.github.etymograph.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViewportTest {
    private fun Viewport.screenOf(world: Offset) = pan + world * scale

    @Test
    fun zoomKeepsTheWorldPointUnderTheFocusInPlace() {
        val before = Viewport(pan = Offset(40f, -25f), scale = 1.2f)
        val focus = Offset(130f, 70f)
        val worldUnderFocus = (focus - before.pan) / before.scale

        val after = before.withZoom(1.5f, focus)

        val moved = after.screenOf(worldUnderFocus) - focus
        assertTrue(abs(moved.x) < 0.01f && abs(moved.y) < 0.01f, "초점이 $moved 만큼 밀렸다")
        assertEquals(1.8f, after.scale, 0.0001f)
    }

    @Test
    fun pinchKeepsThePointUnderTheFingersWhileMovingAndZooming() {
        val before = Viewport(pan = Offset(5f, -8f), scale = 1f)
        val previousCentroid = Offset(50f, 20f)
        val pan = Offset(10f, 0f)
        val worldUnderFingers = (previousCentroid - before.pan) / before.scale

        val after = before.withTransformGesture(previousCentroid, pan, zoom = 2f)

        // 손가락 아래 지점은 2배 확대와 무관하게 손가락이 움직인 만큼(10)만 움직여야 한다.
        val moved = after.screenOf(worldUnderFingers) - (previousCentroid + pan)
        assertTrue(abs(moved.x) < 0.01f && abs(moved.y) < 0.01f, "손가락에서 $moved 만큼 밀렸다")
    }

    private val view = Size(400f, 300f)
    private val margin = 16f

    private fun Viewport.screenRectOf(world: Rect) =
        Rect(screenOf(world.topLeft), screenOf(world.bottomRight))

    private fun assertInsideView(r: Rect) {
        val half = Offset(view.width / 2, view.height / 2)
        assertTrue(
            r.left >= -half.x + margin - 0.01f && r.right <= half.x - margin + 0.01f &&
                r.top >= -half.y + margin - 0.01f && r.bottom <= half.y - margin + 0.01f,
            "화면 밖: $r",
        )
    }

    @Test
    fun visibleBoundsDoNotMoveTheMap() {
        val before = Viewport(pan = Offset(30f, -10f), scale = 1.5f)
        assertEquals(before, before.withBoundsInView(Rect(-40f, -20f, 40f, 20f), view, margin))
    }

    @Test
    fun offscreenBoundsArePannedJustInsideTheEdge() {
        val before = Viewport(pan = Offset(-120f, 40f), scale = 2f)
        val bounds = Rect(100f, 30f, 166f, 58f) // 화면 오른쪽 아래 밖

        val after = before.withBoundsInView(bounds, view, margin)

        assertEquals(2f, after.scale)
        val r = after.screenRectOf(bounds)
        assertInsideView(r)
        // 최소 이동: 넘친 쪽 가장자리가 여백 경계에 딱 닿는다.
        assertEquals(view.width / 2 - margin, r.right, 0.01f)
        assertEquals(view.height / 2 - margin, r.bottom, 0.01f)
    }

    @Test
    fun boundsLargerThanTheViewAreCentered() {
        val bounds = Rect(-500f, -10f, 300f, 10f)
        val r = Viewport().withBoundsInView(bounds, view, margin).screenRectOf(bounds)
        assertEquals(0f, (r.left + r.right) / 2, 0.01f)
    }

    private fun Viewport.movedToShow(group: Rect, keepVisible: Rect? = null, allowZoomOut: Boolean = true) =
        movementToShow(group, keepVisible, view, margin, allowZoomOut).stepBetween(0f, 1f)(this)

    @Test
    fun zoomsOutJustEnoughToFitAGroupWiderThanTheView() {
        val group = Rect(-500f, -10f, 300f, 10f)

        val after = Viewport().movedToShow(group)

        assertEquals((view.width - 2 * margin) / group.width, after.scale, 0.0001f)
        assertInsideView(after.screenRectOf(group))
    }

    @Test
    fun neverZoomsInAndOnlyPansWhenZoomOutIsNotAllowed() {
        val small = Rect(-10f, -10f, 10f, 10f)
        assertEquals(0.5f, Viewport(scale = 0.5f).movedToShow(small).scale)

        val wide = Rect(-500f, -10f, 300f, 10f)
        assertEquals(1f, Viewport().movedToShow(wide, allowZoomOut = false).scale)
    }

    @Test
    fun keepsTheSelectedNodeVisibleWhenEvenTheMinimumScaleIsTooLarge() {
        val huge = Rect(-5000f, -10f, 5000f, 10f)
        val selected = Rect(4800f, -10f, 4900f, 10f)

        val after = Viewport().movedToShow(huge, keepVisible = selected)

        assertEquals(Viewport.MIN_SCALE, after.scale)
        assertInsideView(after.screenRectOf(selected))
    }

    private fun Viewport.movedToFit(group: Rect) = movementToFit(group, view, margin).stepBetween(0f, 1f)(this)

    @Test
    fun fitAllCentersTheWholeGroupAndZoomsOutWhenItIsLarger() {
        val group = Rect(-500f, 100f, 300f, 400f)

        val after = Viewport(pan = Offset(70f, -30f), scale = 1.3f).movedToFit(group)

        assertEquals((view.width - 2 * margin) / group.width, after.scale, 0.0001f)
        val r = after.screenRectOf(group)
        assertInsideView(r)
        assertEquals(0f, r.center.x, 0.01f)
        assertEquals(0f, r.center.y, 0.01f)
    }

    @Test
    fun fitAllDoesNotMagnifyASmallGroupBeyondTheDefaultScale() {
        val small = Rect(-10f, -10f, 10f, 10f)

        val after = Viewport(pan = Offset(-200f, 90f), scale = 2.5f).movedToFit(small)

        assertEquals(1f, after.scale, 0.0001f)
        assertEquals(0f, after.screenRectOf(small).center.x, 0.01f)
        assertEquals(0f, after.screenRectOf(small).center.y, 0.01f)
    }

    @Test
    fun animationStepsAddUpToTheWholeMovement() {
        val start = Viewport(pan = Offset(20f, 5f), scale = 1.2f)
        val movement = start.movementToShow(Rect(-500f, 100f, 300f, 400f), null, view, margin, allowZoomOut = true)

        val whole = movement.stepBetween(0f, 1f)(start)
        val stepped = listOf(0f, 0.3f, 0.7f, 1f).zipWithNext().fold(start) { v, (a, b) -> movement.stepBetween(a, b)(v) }

        assertEquals(whole.scale, stepped.scale, 0.0001f)
        assertEquals(whole.pan.x, stepped.pan.x, 0.01f)
        assertEquals(whole.pan.y, stepped.pan.y, 0.01f)
    }

    @Test
    fun zoomIsClampedAndPanAccumulates() {
        assertEquals(Viewport.MAX_SCALE, Viewport().withZoom(100f, Offset.Zero).scale)
        assertEquals(Viewport.MIN_SCALE, Viewport().withZoom(0.001f, Offset.Zero).scale)
        assertEquals(Offset(3f, 4f), Viewport().withPanBy(Offset(1f, 1f)).withPanBy(Offset(2f, 3f)).pan)
    }
}
