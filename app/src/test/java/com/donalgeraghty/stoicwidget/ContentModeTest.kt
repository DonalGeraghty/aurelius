package com.donalgeraghty.stoicwidget

import org.junit.Assert.assertEquals
import org.junit.Test

class ContentModeTest {
    @Test
    fun missingPreferenceDefaultsToStoicMode() {
        assertEquals(ContentMode.STOIC, ContentMode.fromStoredValue(null))
    }

    @Test
    fun unknownPreferenceDefaultsToStoicMode() {
        assertEquals(ContentMode.STOIC, ContentMode.fromStoredValue("UNKNOWN"))
    }

    @Test
    fun personalPreferenceIsRestored() {
        assertEquals(ContentMode.PERSONAL, ContentMode.fromStoredValue("PERSONAL"))
    }
}
