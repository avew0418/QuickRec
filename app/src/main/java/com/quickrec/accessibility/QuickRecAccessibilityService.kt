package com.quickrec.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import com.quickrec.QuickRecApp
import com.quickrec.hotkey.VolumeKeyStateMachine
import com.quickrec.recording.RecordingState
import com.quickrec.service.RecordingForegroundService

class QuickRecAccessibilityService : AccessibilityService() {

    private lateinit var hotkeyStateMachine: VolumeKeyStateMachine

    override fun onCreate() {
        super.onCreate()
        hotkeyStateMachine = VolumeKeyStateMachine(
            onAudioHotkey = {
                Log.i("AccessibilityService", "Background Audio hotkey triggered!")
                toggleRecording()
            },
            onVideoHotkey = {
                Log.i("AccessibilityService", "Background Video hotkey triggered but not implemented")
            }
        )
    }

    private fun toggleRecording() {
        val recorderManager = QuickRecApp.instance.recorderManager
        if (recorderManager.state.value is RecordingState.Recording) {
            RecordingForegroundService.stopService(this)
        } else {
            RecordingForegroundService.startService(this)
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return false
        }

        return if (event.action == KeyEvent.ACTION_DOWN) {
            hotkeyStateMachine.onKeyDown(
                keyCode = keyCode,
                repeatCount = event.repeatCount,
                eventTimeMs = event.eventTime
            )
        } else if (event.action == KeyEvent.ACTION_UP) {
            hotkeyStateMachine.onKeyUp(
                keyCode = keyCode,
                eventTimeMs = event.eventTime
            )
        } else {
            false
        }
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i("AccessibilityService", "Service connected")
    }
}
