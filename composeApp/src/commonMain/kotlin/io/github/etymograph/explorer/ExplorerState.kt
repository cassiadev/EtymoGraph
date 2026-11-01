package io.github.etymograph.explorer

import io.github.etymograph.graph.GraphLayout
import io.github.etymograph.graph.Point
import io.github.etymograph.graph.Slot
import io.github.etymograph.model.LanguageFamily
import io.github.etymograph.model.Lexicon
import io.github.etymograph.model.RelationEdge
import io.github.etymograph.model.WordNode

/**
 * 사용자가 펼친 지도. 화면 이동·확대(Viewport)와 학습 상태는 여기에 두지 않는다.
 * 모든 변경 함수는 새 상태를 돌려준다.
 */
data class ExplorerState(
    /** 화면에 펼친 단어와 월드 좌표. 펼친 순서를 유지한다. */
    val positions: Map<String, Point> = emptyMap(),
    val expandedIds: Set<String> = emptySet(),
    val selectedId: String? = null,
    val rootId: String? = null,
    /** 새 출발점으로 바꾸기 전의 지도들. */
    val history: List<ExplorerState> = emptyList(),
    /**
     * 지도를 열 때마다 바뀌는 번호. 같은 단어로 다시 열어 내용이 똑같아도 "새로 열린 지도"임을 화면 쪽이 알 수 있게 한다.
     * 이전 지도로 돌아가면 그 지도의 번호가 되돌아오므로 역시 바뀐다.
     */
    val mapNumber: Int = 0,
)

fun ExplorerState.visibleRelations(lexicon: Lexicon): List<RelationEdge> =
    lexicon.relations.filter { it.fromId in positions && it.toId in positions }

fun ExplorerState.hiddenNeighborCount(lexicon: Lexicon, id: String): Int =
    lexicon.relationsOf(id).map { it.otherEnd(id) }.distinct().count { it !in positions }

/** 지도에서 어파 칸이 놓이는 쪽. 게르만어파는 왼쪽, 이탈리아어파는 오른쪽, 어파 이전 단계는 가운데다. */
fun sideOf(word: WordNode): Int = when (word.language.family) {
    LanguageFamily.GERMANIC -> -1
    LanguageFamily.ITALIC -> 1
    null -> 0
}

/** [id]를 출발점으로 새 지도를 연다. 지금 지도는 [ExplorerState.history]에 남긴다. */
fun ExplorerState.withNewRoot(lexicon: Lexicon, id: String): ExplorerState {
    val saved = if (rootId == null) history else history + copy(history = emptyList())
    val root = lexicon.word(id)
    val start = Point(GraphLayout.homeX(sideOf(root)), GraphLayout.bandCenterY(root.language.era.ordinal))
    return ExplorerState(
        positions = mapOf(id to start),
        selectedId = id,
        rootId = id,
        history = saved,
        mapNumber = mapNumber + 1,
    ).withExpanded(lexicon, id)
}

/**
 * [id]와 연결된 단어 중 아직 없는 것만 추가한다. 세로 위치는 단어의 시대 층, 가로 위치는 어파 칸이 정한다.
 * 같은 칸이면 기준 단어 바로 위·아래를, 다른 칸이면 그 칸의 기본 위치를 먼저 찾는다. 기존 노드 위치는 그대로 둔다.
 */
fun ExplorerState.withExpanded(lexicon: Lexicon, id: String): ExplorerState {
    val anchor = positions[id] ?: return this
    val anchorSide = sideOf(lexicon.word(id))
    val newcomers = lexicon.relationsOf(id).map { it.otherEnd(id) }.distinct().filter { it !in positions }
    val slots = newcomers.map {
        val word = lexicon.word(it)
        val side = sideOf(word)
        Slot(band = word.language.era.ordinal, side = side, preferredX = if (side == anchorSide) anchor.x else GraphLayout.homeX(side))
    }
    val placed = GraphLayout.placeInBands(slots, positions.values)
    return copy(
        positions = positions + newcomers.zip(placed),
        expandedIds = expandedIds + id,
    )
}

fun ExplorerState.withSelection(id: String?): ExplorerState =
    if (id == null || id in positions) copy(selectedId = id) else this

/** 화면에 없는 단어라면, 그 단어와 연결된 화면 위 노드(선택한 노드 우선)를 펼친 뒤 선택한다. */
fun ExplorerState.withRevealed(lexicon: Lexicon, id: String): ExplorerState {
    if (id in positions) return withSelection(id)
    val neighbor = (listOfNotNull(selectedId) + positions.keys)
        .firstOrNull { visible -> lexicon.relationsOf(visible).any { it.otherEnd(visible) == id } }
        ?: return this
    return withExpanded(lexicon, neighbor).withSelection(id)
}

fun ExplorerState.withPreviousMap(): ExplorerState =
    history.lastOrNull()?.copy(history = history.dropLast(1)) ?: this
