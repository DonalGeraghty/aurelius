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
    fun normalizesCollectionNames() {
        assertEquals("Morning Focus", MessageText.normalizeCollection("  Morning   Focus  "))
        assertNull(MessageText.normalizeCollection("   "))
    }

}
