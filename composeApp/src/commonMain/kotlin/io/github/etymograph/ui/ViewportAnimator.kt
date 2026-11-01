package io.github.etymograph.ui

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatorMutex

/**
 * 지도 화면 위치([Viewport])를 바꾸는 애니메이션을 한 번에 하나만 실행한다.
 *
 * 존재 이유: 자동 화면 이동(선택, 펼치기, 지도 전환)과 "전체" 버튼이 같은 화면을 움직인다. 각 움직임은
 * "지금보다 2.4배 확대" 같은 구간별 상대 변화량이라, 두 움직임이 겹쳐 돌면 변화량이 쌓여 목표를 지나친다.
 * "전체"를 더블클릭하거나 화면이 움직이는 도중 다시 누르면 과하게 확대되던 버그가 이것이었다. 그래서 새 움직임이 시작되면 진행 중인 것을
 * 취소하고, 그 시점의 화면 위치에서 움직임을 다시 계산한다.
 *
 * 화면(Compose) 코드와 따로 둔 것은 이 규칙을 단위 테스트(`ViewportAnimatorTest`)로 확인하기 위해서다.
 * [current]로 지금 화면 위치를 읽고, [update]로 변화량을 적용한다.
 */
internal class ViewportAnimator(
    private val current: () -> Viewport,
    private val update: ((Viewport) -> Viewport) -> Unit,
) {
    private val mutex = MutatorMutex()

    suspend fun move(movementFrom: (Viewport) -> ViewportMovement) = mutex.mutate {
        val movement = movementFrom(current())
        if (movement.isNone) return@mutate
        var progress = 0f
        animate(0f, 1f, animationSpec = tween(durationMillis = 300)) { t, _ ->
            update(movement.stepBetween(progress, t))
            progress = t
        }
    }
}
