package com.quickrec.hotkey

import android.util.Log

/**
 * Sequence-based state machine for detecting:
 * Audio: UP -> DOWN -> DOWN
 * Video: DOWN -> UP -> UP
 */
class VolumeKeyStateMachine(
    private val windowMs: Long = 1800L,
    private val cooldownMs: Long = 1000L,
    private val onAudioHotkey: () -> Unit = {},
    private val onVideoHotkey: () -> Unit = {}
) {
    private val sequence = mutableListOf<Int>()
    private var lastEventTimeMs: Long = 0L
    private var lastTriggerTimeMs: Long = 0L

    companion object {
        const val KEY_UP = 24
        const val KEY_DOWN = 25
        
        private val AUDIO_SEQ = listOf(KEY_UP, KEY_DOWN, KEY_DOWN)
        private val VIDEO_SEQ = listOf(KEY_DOWN, KEY_UP, KEY_UP)
    }

    fun onKeyDown(keyCode: Int, repeatCount: Int, eventTimeMs: Long): Boolean {
        // Ignore system repeats
        if (repeatCount > 0) return true

        val now = eventTimeMs

        // Cooldown check
        if (now - lastTriggerTimeMs < cooldownMs) {
            Log.d("VolumeKeyStateMachine", "Hotkey ignored due to cooldown")
            return true
        }

        // Reset sequence if time window exceeded
        if (now - lastEventTimeMs > windowMs) {
            sequence.clear()
        }

        sequence.add(keyCode)
        lastEventTimeMs = now
        
        Log.d("VolumeKeyStateMachine", "Volume key sequence updated: ${sequence.map { if (it == KEY_UP) "UP" else "DOWN" }}")

        // Keep only last 3 keys
        if (sequence.size > 3) {
            sequence.removeAt(0)
        }

        checkSequence(now)

        // Return true to consume events
        return true
    }

    private fun checkSequence(now: Long) {
        if (sequence == AUDIO_SEQ) {
            Log.i("VolumeKeyStateMachine", "Audio hotkey matched: UP_DOWN_DOWN")
            lastTriggerTimeMs = now
            sequence.clear()
            onAudioHotkey()
        } else if (sequence == VIDEO_SEQ) {
            Log.i("VolumeKeyStateMachine", "Video hotkey matched: DOWN_UP_UP")
            lastTriggerTimeMs = now
            sequence.clear()
            onVideoHotkey()
        }
    }

    fun onKeyUp(keyCode: Int, eventTimeMs: Long): Boolean {
        return keyCode == KEY_UP || keyCode == KEY_DOWN
    }

    fun reset() {
        Log.d("VolumeKeyStateMachine", "reset")
        sequence.clear()
        lastEventTimeMs = 0L
    }
}
