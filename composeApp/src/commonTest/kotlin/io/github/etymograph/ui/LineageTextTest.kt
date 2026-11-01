package io.github.etymograph.ui

import io.github.etymograph.data.SampleLexicon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LineageTextTest {
    @Test
    fun uncertainLinkStaysVisibleInTheLineage() {
        assertEquals("horse(영어) ←? *ḱers-(인도유럽 조어)", lineageTextOf(SampleLexicon, "en:horse"))
    }

    @Test
    fun courtierShowsBorrowingAndDerivationAsSeparateSteps() {
        assertEquals(
            "courtier(영어) ← corteour(앵글로노르만어) ← cort(고대 프랑스어) ← cohors(라틴어)",
            lineageTextOf(SampleLexicon, "en:courtier"),
        )
    }

    @Test
    fun certainLinksUsePlainArrowsAndRootsHaveNoLineage() {
        assertEquals("cour(프랑스어) ← cort(고대 프랑스어) ← cohors(라틴어)", lineageTextOf(SampleLexicon, "fr:cour"))
        assertNull(lineageTextOf(SampleLexicon, "ine-pro:kers"))
    }
}
