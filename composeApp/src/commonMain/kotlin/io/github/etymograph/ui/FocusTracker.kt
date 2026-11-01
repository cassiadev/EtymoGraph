package io.github.etymograph.ui

import io.github.etymograph.explorer.ExplorerState

/**
 * 지도 상태가 바뀔 때마다 화면에 보여야 할 노드를 고른다. 직전에 본 지도와 노드를 기억한다.
 * 새로 열린 지도(같은 단어로 다시 연 경우 포함)는 모든 노드가 새로 나타난 것으로 본다.
 */
internal class FocusTracker {
    /** [ids]가 보이도록 화면을 맞춘다. [allowZoomOut]은 새로 나타난 노드가 있을 때만 참이다. */
    data class Focus(val ids: Set<String>, val allowZoomOut: Boolean)

    private var mapNumber: Int? = null
    private var shownIds: Set<String> = emptySet()

    /** [state]를 기록하고, 이번 변화에서 보여야 할 노드를 돌려준다. 보여 줄 것이 없으면 null. */
    fun track(state: ExplorerState): Focus? {
        val added = if (state.mapNumber != mapNumber) state.positions.keys else state.positions.keys - shownIds
        mapNumber = state.mapNumber
        shownIds = state.positions.keys
        val ids = added + listOfNotNull(state.selectedId)
        return if (ids.isEmpty()) null else Focus(ids, allowZoomOut = added.isNotEmpty())
    }
}
