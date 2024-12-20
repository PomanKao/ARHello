package com.poman.arhello.common.helpers

import android.app.Activity
import android.view.WindowManager
import com.google.ar.core.Camera
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState

/** Gets human readibly tracking failure reasons and suggested actions.  */
class TrackingStateHelper(private val activity: Activity) {
    private var previousTrackingState: TrackingState? = null

    /** Keep the screen unlocked while tracking, but allow it to lock when tracking stops.  */
    fun updateKeepScreenOnFlag(trackingState: TrackingState) {
        if (trackingState == previousTrackingState) {
            return
        }
        previousTrackingState = trackingState
        when (trackingState) {
            TrackingState.PAUSED, TrackingState.STOPPED -> activity.runOnUiThread {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            TrackingState.TRACKING -> activity.runOnUiThread {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    /** Returns a message based on the tracking state and failure reason.  */
    fun getTrackingFailureReasonString(camera: Camera): String {
        return when (camera.trackingState) {
            TrackingState.PAUSED -> when (camera.trackingFailureReason) {
                TrackingFailureReason.NONE -> ""
                TrackingFailureReason.BAD_STATE -> "Bad State. Try restarting the app."
                TrackingFailureReason.INSUFFICIENT_LIGHT -> "Too dark. Try moving to a well-lit area."
                TrackingFailureReason.EXCESSIVE_MOTION -> "Too much motion. Try moving the device more slowly."
                TrackingFailureReason.INSUFFICIENT_FEATURES -> "Not enough surface detail. Try pointing at a different surface."
                TrackingFailureReason.CAMERA_UNAVAILABLE -> "Camera unavailable. Try restarting the app."
            }
            TrackingState.TRACKING -> ""
            TrackingState.STOPPED -> "Tracking stopped"
        }
    }
}