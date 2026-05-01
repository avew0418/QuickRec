package com.quickrec.recording

sealed class RecordingState {
    data object Idle : RecordingState()
    data class Recording(val startTimeMs: Long, val filePath: String) : RecordingState()
    data class Error(val message: String) : RecordingState()
}
