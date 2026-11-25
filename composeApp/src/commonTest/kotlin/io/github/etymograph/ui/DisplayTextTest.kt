package io.github.etymograph.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class DisplayTextTest {
    @Test
    fun showsTheCorrectNameForEachDictionarySource() {
        assertEquals("American Heritage Dictionary · wedge", sourceLabelOf("https://www.ahdictionary.com/word/search.html?q=wedge"))
        assertEquals("Dictionary.com · courtier", sourceLabelOf("https://www.dictionary.com/browse/courtier"))
        assertEquals("Merriam-Webster · versed", sourceLabelOf("https://www.merriam-webster.com/dictionary/versed"))
    }

    @Test
    fun replacesGlyphsMissingFromWebFallbackFonts() {
        val pie = displayTextOf("*ph₂tḗr")
        assertEquals("*ph2tḗr", pie.text)
        assertEquals(1, pie.spanStyles.size)

        assertEquals("*ḱr˳séti", displayTextOf("*ḱr̥séti").text)
        assertEquals("père", displayTextOf("père").text)
    }
}
