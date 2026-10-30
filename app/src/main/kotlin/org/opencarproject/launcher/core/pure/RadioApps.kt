package org.opencarproject.launcher.core.pure

/**
 * Picks the head unit's FM radio app among the installed launchable apps.
 * Pure logic so it is unit-testable: the caller provides (packageName, label) pairs.
 */
object RadioApps {

    /** FM apps shipped by common head units and AOSP builds, best first. */
    val KNOWN_PACKAGES = listOf(
        "com.syu.radio", // FYT units
        "com.ts.fmradio", // Topway units
        "com.android.fmradio", // AOSP (MediaTek)
        "com.caf.fmradio", // AOSP (Qualcomm)
        "com.quicinc.fmradio",
    )

    /** "Radio", "FM" or "AM" as a whole word in the label ("Radiohead" doesn't count). */
    private val LABEL_WORD = Regex("""(?i)(?<![\p{L}\p{N}])(radio|fm|am)(?![\p{L}\p{N}])""")

    /** `fmradio`, an `fm` segment, or a final/inner `radio` segment in the package name. */
    private val PACKAGE_SEGMENT = Regex("""(?i)(fmradio|(^|\.)fm(\.|$)|(^|\.)radio(\.|$))""")

    /** `true` when the app looks like a radio tuner/player by its package name or label. */
    fun isRadioLike(packageName: String, label: String): Boolean =
        packageName in KNOWN_PACKAGES ||
            PACKAGE_SEGMENT.containsMatchIn(packageName) ||
            LABEL_WORD.containsMatchIn(label)

    /**
     * The best radio app: a known head-unit package first (in [KNOWN_PACKAGES] order), then the
     * radio-looking apps by label. Null when nothing looks like a radio.
     */
    fun pick(apps: List<Pair<String, String>>): String? {
        KNOWN_PACKAGES.firstOrNull { known -> apps.any { it.first == known } }?.let { return it }
        return apps.filter { isRadioLike(it.first, it.second) }.minByOrNull { it.second.lowercase() }?.first
    }
}
