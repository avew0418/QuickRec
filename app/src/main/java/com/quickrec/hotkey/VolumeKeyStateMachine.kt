package com.quickrec.hotkey

/**
 * Pure-logic state machine for detecting "hold Volume Up + triple-tap Volume Down".
 *
 * No Android dependency — fully unit-testable.
 *
 * States:
 *   IDLE → VOL_UP down → WAITING_HOLD
 *   WAITING_HOLD → held ≥ holdThresholdMs → ARMED
 *   ARMED → VOL_DOWN tapped [requiredTaps] times within tapWindowMs → TRIGGERED → callback
 *   Any state → VOL_UP released → IDLE
 */
class VolumeKeyStateMachine(
    private val holdThresholdMs: Long = 400L,
    private val tapWindowMs: Long = 800L,
    private val requiredTaps: Int = 3,
    private val onTriggered: () -> Unit = {}
) {
    enum class State { IDLE, WAITING_HOLD, ARMED }

    var currentState: State = State.IDLE
        private set

    private var volumeUpDownTimeMs: Long = 0L
    private var tapCount: Int = 0
    private var firstTapTimeMs: Long = 0L

    /**
     * @param keyCode  KEYCODE_VOLUME_UP (24) or KEYCODE_VOLUME_DOWN (25)
     * @param repeatCount  from KeyEvent.getRepeatCount()
     * @param eventTimeMs  SystemClock.uptimeMillis() from the KeyEvent
     * @return true if the event was consumed (caller should not propagate)
     */
    fun onKeyDown(keyCode: Int, repeatCount: Int, eventTimeMs: Long): Boolean {
        return when (currentState) {
            State.IDLE -> handleIdle(keyCode, repeatCount, eventTimeMs)
            State.WAITING_HOLD -> handleWaiting(keyCode, repeatCount, eventTimeMs)
            State.ARMED -> handleArmed(keyCode, repeatCount, eventTimeMs)
        }
    }

    fun onKeyUp(keyCode: Int, @Suppress("UNUSED_PARAMETER") eventTimeMs: Long): Boolean {
        if (keyCode == KEY_VOLUME_UP && currentState != State.IDLE) {
            reset()
            return true
        }
        return currentState != State.IDLE
    }

    fun reset() {
        currentState = State.IDLE
        volumeUpDownTimeMs = 0L
        tapCount = 0
        firstTapTimeMs = 0L
    }

    // ── State handlers ──────────────────────────────────────────────

    private fun handleIdle(keyCode: Int, repeatCount: Int, eventTimeMs: Long): Boolean {
        if (keyCode == KEY_VOLUME_UP && repeatCount == 0) {
            currentState = State.WAITING_HOLD
            volumeUpDownTimeMs = eventTimeMs
            return true
        }
        return false
    }

    private fun handleWaiting(keyCode: Int, repeatCount: Int, eventTimeMs: Long): Boolean {
        if (keyCode == KEY_VOLUME_UP && repeatCount > 0) {
            if (eventTimeMs - volumeUpDownTimeMs >= holdThresholdMs) {
                currentState = State.ARMED
                tapCount = 0
                firstTapTimeMs = 0L
            }
            return true
        }
        if (keyCode == KEY_VOLUME_DOWN) return true   // consume to block volume change
        return false
    }

    private fun handleArmed(keyCode: Int, repeatCount: Int, eventTimeMs: Long): Boolean {
        if (keyCode == KEY_VOLUME_UP) return true      // consume vol-up repeats

        if (keyCode == KEY_VOLUME_DOWN) {
            if (repeatCount > 0) return true           // ignore vol-down long-press

            // Fresh tap
            if (tapCount == 0) {
                firstTapTimeMs = eventTimeMs
            } else if (eventTimeMs - firstTapTimeMs > tapWindowMs) {
                tapCount = 0                           // window expired, restart
                firstTapTimeMs = eventTimeMs
            }

            tapCount++

            if (tapCount >= requiredTaps) {
                onTriggered()
                reset()
            }
            return true
        }
        return false
    }

    companion object {
        const val KEY_VOLUME_UP = 24    // android.view.KeyEvent.KEYCODE_VOLUME_UP
        const val KEY_VOLUME_DOWN = 25  // android.view.KeyEvent.KEYCODE_VOLUME_DOWN
    }
}
