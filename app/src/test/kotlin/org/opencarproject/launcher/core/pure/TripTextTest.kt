package org.opencarproject.launcher.core.pure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TripTextTest {

    @Test
    fun `maps style summary with decimal comma`() {
        val r = TripText.parse("12 min · 5,4 km · 18:30")
        assertEquals(5400, r.meters)
        assertEquals(12, r.minutes)
    }

    @Test
    fun `hours plus minutes and decimal point`() {
        val r = TripText.parse("1 h 5 min · 60.2 km · ETA 9:05")
        assertEquals(60200, r.meters)
        assertEquals(65, r.minutes)
    }

    @Test
    fun `meters only, no time`() {
        val r = TripText.parse("Quedan 850 m")
        assertEquals(850, r.meters)
        assertNull(r.minutes)
    }

    @Test
    fun `miles convert and plain text yields nulls`() {
        assertEquals(3218, TripText.parse("2 mi restantes").meters)
        val none = TripText.parse("Sin ruta activa")
        assertNull(none.meters)
        assertNull(none.minutes)
    }
}
