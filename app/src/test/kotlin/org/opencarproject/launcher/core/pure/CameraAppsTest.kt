package org.opencarproject.launcher.core.pure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraAppsTest {

    @Test
    fun `known head-unit packages and markers are camera-like`() {
        assertTrue(CameraApps.isCameraLike("com.syu.avm", "AVM"))
        assertTrue(CameraApps.isCameraLike("com.vendor.view", "Cámara 360"))
        assertTrue(CameraApps.isCameraLike("com.vendor.dashcam", "DVR"))
        assertTrue(CameraApps.isCameraLike("com.android.camera2", "Camera"))
    }

    @Test
    fun `unrelated apps do not match`() {
        assertFalse(CameraApps.isCameraLike("com.spotify.music", "Spotify"))
        // "360" only as a whole token: a resolution label must not match.
        assertFalse(CameraApps.isCameraLike("com.video.player", "Player x360p"))
    }

    @Test
    fun `pick prefers known packages, then 360 apps, then plain cameras`() {
        assertEquals(
            "com.syu.avm",
            CameraApps.pick(
                listOf(
                    "com.android.camera2" to "Camera",
                    "com.syu.avm" to "AVM",
                ),
            ),
        )
        assertEquals(
            "com.vendor.surround",
            CameraApps.pick(
                listOf(
                    "com.android.camera2" to "Camera",
                    "com.vendor.surround" to "Vista 360",
                ),
            ),
        )
        assertEquals(
            "com.android.camera2",
            CameraApps.pick(listOf("com.android.camera2" to "Camera", "com.spotify.music" to "Spotify")),
        )
        assertNull(CameraApps.pick(listOf("com.spotify.music" to "Spotify")))
    }
}
