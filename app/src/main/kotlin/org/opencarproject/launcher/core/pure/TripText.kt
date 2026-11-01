package org.opencarproject.launcher.core.pure

/**
 * Parses the remaining trip out of a navigation notification's summary line — the
 * "12 min · 5,4 km · 18:30" style text Google Maps and Waze publish. Locale-tolerant:
 * decimal comma or point, km/m/mi, hours+minutes.
 */
object TripText {

    /** What is left of the trip; null fields when the text doesn't say. */
    data class Remaining(val meters: Int?, val minutes: Int?)

    private val DISTANCE = Regex("""(?i)(\d+(?:[.,]\d+)?)\s*(km|mi|m)(?![\p{L}])""")
    private val HOURS = Regex("""(?i)(\d+)\s*h(?:r|ora)?s?(?![\p{L}])""")
    private val MINUTES = Regex("""(?i)(\d+)\s*min""")

    fun parse(text: String): Remaining {
        val meters = DISTANCE.find(text)?.let { m ->
            val value = m.groupValues[1].replace(',', '.').toDouble()
            when (m.groupValues[2].lowercase()) {
                "km" -> value * 1000
                "mi" -> value * 1609.34
                else -> value
            }.toInt()
        }
        val minutes = (HOURS.find(text)?.groupValues?.get(1)?.toInt() ?: 0) * 60 +
            (MINUTES.find(text)?.groupValues?.get(1)?.toInt() ?: 0)
        return Remaining(meters, minutes.takeIf { HOURS.containsMatchIn(text) || MINUTES.containsMatchIn(text) })
    }
}
