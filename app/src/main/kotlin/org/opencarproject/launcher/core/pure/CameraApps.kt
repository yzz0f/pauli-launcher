package org.opencarproject.launcher.core.pure

/**
 * Picks the head unit's camera app — the 360 around-view monitor (AVM) when the unit has one,
 * otherwise a DVR / reverse-view / plain camera app. Pure logic so it is unit-testable: the
 * caller provides (packageName, label) pairs.
 */
object CameraApps {

    /** AVM / camera apps shipped by common head units, best first. */
    val KNOWN_PACKAGES = listOf(
        "com.syu.avm", // FYT around-view monitor (360)
        "com.ts.avm", // Topway around-view monitor
        "com.hzbhd.avm",
        "com.syu.backview", // FYT reverse camera viewer
        "com.ts.dvr",
        "com.syu.dvr",
    )

    /** Around-view markers: a 360 app outranks a plain camera. */
    private val AVM = Regex("""(?i)(avm|panoram|surround|(?<![\p{L}\p{N}])360(?![\p{L}\p{N}]))""")

    /** Other camera-ish markers in the package name or label. */
    private val CAMERA = Regex("""(?i)(dvr|backview|rearview|dashcam|c[aá]mara|camera)""")

    /** `true` when the app looks like a camera viewer by its package name or label. */
    fun isCameraLike(packageName: String, label: String): Boolean =
        packageName in KNOWN_PACKAGES || AVM.containsMatchIn("$packageName $label") ||
            CAMERA.containsMatchIn("$packageName $label")

    /**
     * The best camera app: a known head-unit package first (in [KNOWN_PACKAGES] order), then
     * 360/AVM apps, then the rest (DVR, reverse view, plain camera) by label. Null when
     * nothing looks like a camera.
     */
    fun pick(apps: List<Pair<String, String>>): String? {
        KNOWN_PACKAGES.firstOrNull { known -> apps.any { it.first == known } }?.let { return it }
        val cameras = apps.filter { isCameraLike(it.first, it.second) }
        return (
            cameras.filter { AVM.containsMatchIn("${it.first} ${it.second}") } +
                cameras.sortedBy { it.second.lowercase() }
            ).firstOrNull()?.first
    }
}
