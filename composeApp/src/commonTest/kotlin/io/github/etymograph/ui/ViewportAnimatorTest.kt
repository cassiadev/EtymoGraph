package io.github.etymograph.ui

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

/**
 * [ViewportAnimator]의 회귀 테스트.
 *
 * 목적: 화면 움직임이 겹쳐 돌지 않는지 확인한다. 예전에는 "전체"를 더블클릭하거나 화면이 움직이는 도중 다시 누르면 애니메이션이 두 개 돌면서
 * 상대 변화량이 쌓여, 한 번 누를 때보다 더 확대됐다(배율 0.46이 아니라 0.49).
 * 브라우저 없이 재현하려고 가상 프레임 시계([BroadcastFrameClock])로 애니메이션을 한 프레임씩 진행한다.
 */
class ViewportAnimatorTest {
    private val view = Size(400f, 300f)
    private val margin = 16f
    private val group = Rect(-500f, 100f, 300f, 400f)

    // 축소 버튼을 네 번 누른 상태(0.8⁴ ≈ 0.41배)에서 시작한다.
    private val start = Viewport(scale = 0.41f)
    private val fit = { v: Viewport -> v.movementToFit(group, view, margin) }

    @Test
    fun pressingFitTwiceInARowEndsWhereASinglePressDoes() = runTest {
        val clock = BroadcastFrameClock()
        var viewport = start
        val animator = ViewportAnimator(current = { viewport }, update = { viewport = it(viewport) })
        var nanos = 0L
        fun frame(ms: Long) {
            nanos += ms * 1_000_000
            clock.sendFrame(nanos)
            testScheduler.runCurrent()
        }

        launch(clock) { animator.move(fit) }
        testScheduler.runCurrent()
        frame(0)
        frame(100) // 첫 번째 맞춤이 진행되는 도중에
        launch(clock) { animator.move(fit) } // 두 번째로 누른다
        testScheduler.runCurrent()
        repeat(40) { frame(16) }

        val single = fit(start).stepBetween(0f, 1f)(start)
        assertEquals(single.scale, viewport.scale, 0.0001f)
        assertEquals(single.pan.x, viewport.pan.x, 0.01f)
        assertEquals(single.pan.y, viewport.pan.y, 0.01f)
    }
}
