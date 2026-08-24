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
}
