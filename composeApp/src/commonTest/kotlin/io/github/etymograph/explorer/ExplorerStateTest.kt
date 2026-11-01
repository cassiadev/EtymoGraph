package io.github.etymograph.explorer

import io.github.etymograph.data.SampleLexicon
import io.github.etymograph.graph.GraphLayout
import io.github.etymograph.graph.Point
import io.github.etymograph.model.Era
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ExplorerStateTest {
    private val lexicon = SampleLexicon

    @Test
    fun newRootSitsInItsEraBandAndFamilyLaneWithItsNeighbors() {
        val state = ExplorerState().withNewRoot(lexicon, "en:father")

        // father는 현대 영어라 현대어 층, 게르만어파(왼쪽) 칸의 기본 위치에서 시작한다.
        assertEquals(Point(GraphLayout.homeX(-1), GraphLayout.bandCenterY(Era.MODERN.ordinal)), state.positions["en:father"])
        assertEquals(setOf("en:father", "ang:faeder", "de:vater", "la:pater"), state.positions.keys)
        // 조상 fæder는 같은 칸 바로 위 층, 라틴어 pater는 오른쪽 칸에 놓인다.
        assertEquals(Point(GraphLayout.homeX(-1), GraphLayout.bandCenterY(Era.ANCIENT.ordinal)), state.positions["ang:faeder"])
        assertTrue(state.positions.getValue("la:pater").x >= GraphLayout.LANE_INNER)
        assertEquals("en:father", state.selectedId)
        assertTrue("en:father" in state.expandedIds)
    }

    @Test
    fun expandingAddsOnlyNewNodesAndKeepsExistingPositions() {
        val before = ExplorerState().withNewRoot(lexicon, "en:father")
        val after = before.withExpanded(lexicon, "la:pater")

        before.positions.forEach { (id, p) -> assertEquals(p, after.positions[id], "$id 위치가 바뀌었다") }
        assertEquals(
            setOf("ine-pro:ph2ter", "fr:pere", "es:padre", "la:paternalis"),
            after.positions.keys - before.positions.keys,
        )
        assertEquals(after.positions, after.withExpanded(lexicon, "la:pater").positions)
    }

    @Test
    fun visibleRelationsNeedBothEndsOnScreen() {
        val state = ExplorerState().withNewRoot(lexicon, "en:father")

        val visible = state.visibleRelations(lexicon)
        assertTrue(visible.isNotEmpty())
        assertTrue(visible.all { it.fromId in state.positions && it.toId in state.positions })
    }

    @Test
    fun startingANewMapKeepsThePreviousOneForGoingBack() {
        val father = ExplorerState().withNewRoot(lexicon, "en:father").withExpanded(lexicon, "la:pater")
        val courier = father.withNewRoot(lexicon, "en:courier")

        assertEquals("en:courier", courier.rootId)
        val back = courier.withPreviousMap()
        assertEquals(father.positions, back.positions)
        assertEquals(father.expandedIds, back.expandedIds)
        assertTrue(back.history.isEmpty())
        assertSame(back, back.withPreviousMap())
    }

    @Test
    fun reopeningTheSameWordOrGoingBackIsStillANewMap() {
        val first = ExplorerState().withNewRoot(lexicon, "en:father")
        val again = first.withNewRoot(lexicon, "en:father")

        assertEquals(first.positions, again.positions) // 배치는 결정적이라 내용이 똑같다
        assertTrue(again.mapNumber != first.mapNumber)
        assertTrue(again.withPreviousMap().mapNumber != again.mapNumber)
    }

    @Test
    fun revealingAHiddenNeighborExpandsTheSelectedNodeAndSelectsIt() {
        val state = ExplorerState().withNewRoot(lexicon, "en:father").withSelection("la:pater")

        val revealed = state.withRevealed(lexicon, "fr:pere")

        assertEquals("fr:pere", revealed.selectedId)
        assertTrue("la:pater" in revealed.expandedIds)
    }
}
