package dev.zanderp.opencfmoto

/** Diagnostic marker shared by media and control channels. */
object ShohamIsolation {
    @Volatile var firstFrameSent: Boolean = false
}
