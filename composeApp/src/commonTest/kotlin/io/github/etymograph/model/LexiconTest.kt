package io.github.etymograph.model

import io.github.etymograph.data.SampleLexicon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LexiconTest {
    private val lexicon = SampleLexicon

    @Test
    fun sampleDataCitesASourceForEveryWordAndRelation() {
        lexicon.words.forEach { w -> assertTrue(w.sourceUrls.isNotEmpty() && w.sourceUrls.all { it.startsWith("https://") }, w.id) }
        lexicon.relations.forEach { r -> assertTrue(r.sourceUrls.isNotEmpty() && r.sourceUrls.all { it.startsWith("https://") }, "$r") }
    }

    @Test
    fun wordsAreSearchableAndReachTheirFamilyRoots() {
        val expectedRoots = mapOf(
            "verse" to "la:vertere",
            "versed" to "la:vertere",
            "versatile" to "la:vertere",
            "weave" to "gem-pro:webana",
            "web" to "gem-pro:webana",
            "wedge" to "ang:wecg",
            "potato wedges" to "ang:wecg",
            "prescient" to "la:scire",
            "science" to "la:scire",
            "omniscient" to "la:scire",
        )
        expectedRoots.forEach { (word, root) ->
            val result = assertNotNull(lexicon.search(word).firstOrNull(), word)
            assertEquals(word, result.word)
            assertEquals(Language.ENGLISH, result.language, word)
            assertEquals(root, lexicon.ancestryOf(result.id).lastOrNull()?.toId, word)
        }
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
