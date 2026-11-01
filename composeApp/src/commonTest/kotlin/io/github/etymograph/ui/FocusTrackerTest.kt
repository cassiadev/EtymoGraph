package io.github.etymograph.ui

import io.github.etymograph.data.SampleLexicon
import io.github.etymograph.explorer.ExplorerState
import io.github.etymograph.explorer.withExpanded
import io.github.etymograph.explorer.withNewRoot
import io.github.etymograph.explorer.withPreviousMap
import io.github.etymograph.explorer.withSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FocusTrackerTest {
    private val lexicon = SampleLexicon

    @Test
    fun searchingTheSameWordAgainRefitsTheWholeMap() {
        val tracker = FocusTracker()
        val first = ExplorerState().withNewRoot(lexicon, "en:father")
        assertEquals(Focus(first.positions.keys, allowZoomOut = true), tracker.track(first))

        // 같은 단어를 다시 검색하면 App이 화면을 1배로 되돌리므로, 내용이 같아도 다시 맞춰야 한다.
        val again = first.withNewRoot(lexicon, "en:father")
        assertEquals(Focus(again.positions.keys, allowZoomOut = true), tracker.track(again))
    }

    @Test
    fun expandingFocusesOnlyTheNewNodesAndTheSelection() {
        val tracker = FocusTracker()
        val map = ExplorerState().withNewRoot(lexicon, "en:father")
        tracker.track(map)

        val expanded = map.withExpanded(lexicon, "la:pater").withSelection("la:pater")
        val focus = tracker.track(expanded)!!

        assertEquals(expanded.positions.keys - map.positions.keys + "la:pater", focus.ids)
        assertTrue(focus.allowZoomOut)
    }

    @Test
    fun selectionAloneOnlyPansAndUnchangedStateDoesNothing() {
        val tracker = FocusTracker()
        val map = ExplorerState().withNewRoot(lexicon, "en:father")
        tracker.track(map)

        assertEquals(Focus(setOf("de:vater"), allowZoomOut = false), tracker.track(map.withSelection("de:vater")))
        assertNull(tracker.track(map.withSelection(null)))
    }

    @Test
    fun goingBackRefitsTheRestoredMap() {
        val tracker = FocusTracker()
        val father = ExplorerState().withNewRoot(lexicon, "en:father")
        tracker.track(father)
        tracker.track(father.withNewRoot(lexicon, "en:court"))

        val back = father.withNewRoot(lexicon, "en:court").withPreviousMap()
        val focus = tracker.track(back)!!
        assertEquals(back.positions.keys, focus.ids)
        assertTrue(focus.allowZoomOut)
    }
}

private typealias Focus = FocusTracker.Focus
