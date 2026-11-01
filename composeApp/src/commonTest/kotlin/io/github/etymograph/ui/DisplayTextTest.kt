package io.github.etymograph.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class DisplayTextTest {
    @Test
    fun replacesGlyphsMissingFromWebFallbackFonts() {
        val pie = displayTextOf("*ph₂tḗr")
        assertEquals("*ph2tḗr", pie.text)
        assertEquals(1, pie.spanStyles.size)

        assertEquals("*ḱr˳séti", displayTextOf("*ḱr̥séti").text)
        assertEquals("père", displayTextOf("père").text)
    }
}
