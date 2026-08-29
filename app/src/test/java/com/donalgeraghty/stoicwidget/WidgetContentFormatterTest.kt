package com.donalgeraghty.stoicwidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetContentFormatterTest {
    @Test
    fun personalContentUsesTitleCaseAndDashedAttribution() {
        val content = WidgetContent(
            text = "Hello world",
            attribution = "Virgil",
            mode = ContentMode.PERSONAL,
        )

        assertEquals("“Hello World”", WidgetContentFormatter.quote(content))
        assertEquals("— Virgil", WidgetContentFormatter.attribution(content))
    }

    @Test
    fun blankPersonalAttributionRemainsHidden() {
        val content = WidgetContent(
            text = "Hello world",
            attribution = null,
            mode = ContentMode.PERSONAL,
        )

        assertNull(WidgetContentFormatter.attribution(content))
    }

    @Test
    fun stoicContentKeepsItsOriginalCasingAndAttribution() {
        val content = WidgetContent(
            text = "Do not explain your philosophy.",
            attribution = "Epictetus — adapted",
            mode = ContentMode.STOIC,
        )

        assertEquals("“Do not explain your philosophy.”", WidgetContentFormatter.quote(content))
        assertEquals("Epictetus — adapted", WidgetContentFormatter.attribution(content))
    }
}
