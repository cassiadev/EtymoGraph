package io.github.etymograph.model

import io.github.etymograph.data.SampleLexicon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LexiconTest {
    private val lexicon = SampleLexicon

    @Test
    fun sampleDataCitesASourceForEveryWordAndRelation() {
        assertTrue(lexicon.words.size in 15..30)
        lexicon.words.forEach { w -> assertTrue(w.sourceUrls.isNotEmpty() && w.sourceUrls.all { it.startsWith("https://") }, w.id) }
        lexicon.relations.forEach { r -> assertTrue(r.sourceUrls.isNotEmpty() && r.sourceUrls.all { it.startsWith("https://") }, "$r") }
    }

    @Test
    fun rejectsRelationsToUnknownWords() {
        val word = lexicon.word("en:father")
        assertFailsWith<IllegalArgumentException> {
            Lexicon(listOf(word), listOf(RelationEdge(word.id, "xx:missing", RelationType.COGNATE_OF, "", emptyList())))
        }
    }

    @Test
    fun ancestryFollowsInheritanceToTheReconstructedRoot() {
        assertEquals(
            listOf("ang:faeder", "gem-pro:fader", "ine-pro:ph2ter"),
            lexicon.ancestryOf("en:father").map { it.toId },
        )
    }

    @Test
    fun courtierIsBorrowedFromADerivativeNotFromCortItself() {
        val (borrowed, derived) = lexicon.ancestryOf("en:courtier")
        assertEquals(RelationType.BORROWED_FROM to "xno:corteour", borrowed.type to borrowed.toId)
        assertEquals(RelationType.DERIVED_FROM to "fro:cort", derived.type to derived.toId)
    }

    @Test
    fun ancestryKeepsTheUncertaintyOfEachLink() {
        val horse = lexicon.ancestryOf("en:horse")
        assertEquals(listOf("ine-pro:kers"), horse.map { it.toId })
        assertTrue(horse.single().uncertain)
        assertTrue(lexicon.ancestryOf("en:father").none { it.uncertain })
    }

    @Test
    fun courtAndCourierAreOnlySimilarInFormAndShareNoAncestor() {
        val link = lexicon.relationsOf("en:court").single { it.otherEnd("en:court") == "en:courier" }
        assertEquals(RelationType.SIMILAR_FORM, link.type)
        assertFalse(link.type.etymological)

        val courtLine = lexicon.ancestryOf("en:court").map { it.toId }
        val courierLine = lexicon.ancestryOf("en:courier").map { it.toId }
        assertEquals("la:cohors", courtLine.last())
        assertTrue("la:currere" in courierLine)
        assertTrue(courtLine.intersect(courierLine.toSet()).isEmpty())
    }

    @Test
    fun searchRanksExactMatchesFirstAndIgnoresCase() {
        assertEquals("en:father", lexicon.search("Father").first().id)
        assertEquals(listOf("fr:cour", "en:court", "en:courtier"), lexicon.search("cour").take(3).map { it.id })
        assertTrue(lexicon.search("  ").isEmpty())
    }

    @Test
    fun reconstructedFormsAreFoundWithOrWithoutTheAsterisk() {
        assertEquals("ine-pro:kers", lexicon.search("*ḱers-").first().id)
        assertEquals("ine-pro:kers", lexicon.search("ḱers-").first().id)
        assertTrue(lexicon.search("*").isEmpty())
    }
}
