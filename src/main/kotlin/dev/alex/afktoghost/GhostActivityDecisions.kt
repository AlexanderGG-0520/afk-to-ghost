package dev.alex.afktoghost

import kotlin.math.abs

object GhostActivityDecisions {
    private const val LOOK_THRESHOLD = 0.1f

    @JvmStatic
    fun hasIntentionalMovementInput(xxa: Float, zza: Float, jumping: Boolean, shiftKeyDown: Boolean): Boolean {
        return xxa != 0.0f || zza != 0.0f || jumping || shiftKeyDown
    }

    @JvmStatic
    fun hasIntentionalLookChange(hasRotation: Boolean, yaw: Float, pitch: Float, currentYaw: Float, currentPitch: Float): Boolean {
        return hasRotation &&
            (angleDelta(yaw, currentYaw) > LOOK_THRESHOLD || angleDelta(pitch, currentPitch) > LOOK_THRESHOLD)
    }

    @JvmStatic
    fun hasIntentionalBoatPaddleInput(left: Boolean, right: Boolean): Boolean {
        return left || right
    }

    private fun angleDelta(a: Float, b: Float): Float {
        var delta = abs(a - b) % 360.0f
        if (delta > 180.0f) {
            delta = 360.0f - delta
        }
        return delta
    }
}
