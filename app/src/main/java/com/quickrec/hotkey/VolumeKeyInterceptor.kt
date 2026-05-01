package com.quickrec.hotkey

import android.view.KeyEvent

/**
 * Bridges Android KeyEvent to the pure-logic [VolumeKeyStateMachine].
 * Call [dispatchKeyEvent] from Activity.dispatchKeyEvent().
 */
class VolumeKeyInterceptor(
    private val stateMachine: VolumeKeyStateMachine
) {
    /**
     * @return true if the event was consumed by the hotkey system
     */
    fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode

        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return false
        }

        return when (event.action) {
            KeyEvent.ACTION_DOWN -> stateMachine.onKeyDown(
                keyCode = keyCode,
                repeatCount = event.repeatCount,
                eventTimeMs = event.eventTime
            )
            KeyEvent.ACTION_UP -> stateMachine.onKeyUp(
                keyCode = keyCode,
                eventTimeMs = event.eventTime
            )
            else -> false
        }
    }
}
