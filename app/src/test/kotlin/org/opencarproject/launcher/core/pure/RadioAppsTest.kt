package org.opencarproject.launcher.core.pure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RadioAppsTest {

    @Test
    fun `known head-unit packages are radio-like`() {
        assertTrue(RadioApps.isRadioLike("com.syu.radio", "Radio"))
        assertTrue(RadioApps.isRadioLike("com.android.fmradio", "FM"))
    }

    @Test
    fun `package segments and label words match`() {
        assertTrue(RadioApps.isRadioLike("com.vendor.fm", "Tuner"))
        assertTrue(RadioApps.isRadioLike("com.vendor.radio.app", "Tuner"))
        assertTrue(RadioApps.isRadioLike("com.example.tuner", "FM Radio"))
        assertTrue(RadioApps.isRadioLike("tunein.player", "TuneIn Radio"))
    }

    @Test
    fun `music apps and lookalike words do not match`() {
        assertFalse(RadioApps.isRadioLike("com.spotify.music", "Spotify"))
        assertFalse(RadioApps.isRadioLike("com.band.app", "Radiohead Fan"))
        assertFalse(RadioApps.isRadioLike("com.firmware.tool", "Firmware"))
    }

    @Test
    fun `pick prefers known packages over label matches`() {
        val best = RadioApps.pick(
            listOf(
                "tunein.player" to "TuneIn Radio",
                "com.syu.radio" to "Radio",
            ),
        )
        assertEquals("com.syu.radio", best)
    }

    @Test
    fun `pick falls back to the first radio-looking label and null without one`() {
        val apps = listOf(
            "com.spotify.music" to "Spotify",
            "tunein.player" to "TuneIn Radio",
        )
        assertEquals("tunein.player", RadioApps.pick(apps))
        assertNull(RadioApps.pick(listOf("com.spotify.music" to "Spotify")))
    }
}
