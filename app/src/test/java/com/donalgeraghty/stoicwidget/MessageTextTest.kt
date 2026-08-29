package com.donalgeraghty.stoicwidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MessageTextTest {
    @Test
    fun trimsMessageText() {
        assertEquals("Keep going", MessageText.normalize("  Keep going  "))
    }

    @Test
    fun rejectsBlankText() {
        assertNull(MessageText.normalize("   "))
    }

    @Test
    fun rejectsTextOverMaximumLength() {
        assertNull(MessageText.normalize("a".repeat(MessageText.MAX_LENGTH + 1)))
    }

    @Test
    fun titleCasesPersonalCardTextForDisplay() {
        assertEquals("Hello World", MessageText.titleCaseForDisplay("Hello world"))
        assertEquals("Keep API Working", MessageText.titleCaseForDisplay("keep API working"))
        assertEquals("Well-Known Idea", MessageText.titleCaseForDisplay("well-known idea"))
    }

    @Test
    fun normalizesSourceAndAddsDisplayDash() {
        assertEquals("Ovid", MessageText.normalizeSource(" - Ovid "))
        assertEquals("— Ovid", MessageText.attributionForDisplay("Ovid"))
        assertNull(MessageText.normalizeSource("   "))
    }

    @Test
    fun validatesAndNormalizesOptionalColors() {
        assertEquals("#AABBCC", MessageText.normalizeColor(" #aabbcc "))
        assertEquals("#80AABBCC", MessageText.normalizeColor("#80aabbcc"))
        assertNull(MessageText.normalizeColor("blue"))
        assertEquals(true, MessageText.isValidOptionalColor(""))
        assertEquals(false, MessageText.isValidOptionalColor("#12345"))
    }
}
