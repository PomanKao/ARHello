package com.poman.arhello.common.helpers

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar

/**
 * Helper to manage the sample snackbar. Hides the Android boilerplate code, and exposes simpler
 * methods.
 */
class SnackbarHelper {
    private var messageSnackbar: Snackbar? = null

    private enum class DismissBehavior {
        HIDE,
        SHOW,
        FINISH
    }

    /** Shows a snackbar with a given message.  */
    fun showMessage(activity: Activity, message: String) {
        show(activity, message, DismissBehavior.HIDE)
    }

    /** Shows a snackbar with a given message, and a dismiss button.  */
    fun showMessageWithDismiss(activity: Activity, message: String) {
        show(activity, message, DismissBehavior.SHOW)
    }

    /**
     * Shows a snackbar with a given error message. When dismissed, will finish the activity. Useful
     * for notifying errors, where no further interaction with the activity is possible.
     */
    fun showError(activity: Activity, errorMessage: String) {
        show(activity, errorMessage, DismissBehavior.FINISH)
    }

    /**
     * Hides the currently showing snackbar, if there is one. Safe to call from any thread. Safe to
     * call even if snackbar is not currently showing, or if activity is no longer active.
     */
    fun hide(activity: Activity) {
        activity.runOnUiThread {
            messageSnackbar?.dismiss()
            messageSnackbar = null
        }
    }

    private fun show(
        activity: Activity, message: String, dismissBehavior: DismissBehavior
    ) {
        activity.runOnUiThread {
            messageSnackbar?.dismiss()
            messageSnackbar = Snackbar.make(
                activity.findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_INDEFINITE
            )
            messageSnackbar?.view?.setBackgroundColor(BACKGROUND_COLOR)
            if (dismissBehavior != DismissBehavior.HIDE) {
                messageSnackbar?.setAction(
                    "Dismiss"
                ) { messageSnackbar?.dismiss() }
                if (dismissBehavior == DismissBehavior.FINISH) {
                    messageSnackbar?.addCallback(
                        object : BaseCallback<Snackbar?>() {
                            override fun onDismissed(
                                transientBottomBar: Snackbar?,
                                event: Int
                            ) {
                                super.onDismissed(transientBottomBar, event)
                                activity.finish()
                            }
                        })
                }
            }
            (messageSnackbar
                ?.view
                ?.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView)
                .maxLines = 4
            messageSnackbar?.show()
        }
    }

    companion object {
        private const val BACKGROUND_COLOR = -0x40cdcdce
    }
}