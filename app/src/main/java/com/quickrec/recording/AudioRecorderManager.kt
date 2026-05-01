package com.quickrec.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.quickrec.storage.RecordingFileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AudioRecorderManager(
    private val context: Context,
    private val fileManager: RecordingFileManager
) {
    companion object {
        private const val TAG = "AudioRecorderManager"
        private const val SAMPLE_RATE = 44100
        private const val BIT_RATE = 128_000
    }

    private var recorder: MediaRecorder? = null
    private val _state = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val state: StateFlow<RecordingState> = _state.asStateFlow()

    fun startRecording() {
        if (_state.value is RecordingState.Recording) {
            Log.w(TAG, "Already recording, ignoring start request")
            return
        }

        val outputFile = fileManager.createNewRecordingFile()

        try {
            recorder = createRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(BIT_RATE)
                setAudioSamplingRate(SAMPLE_RATE)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            _state.value = RecordingState.Recording(
                startTimeMs = System.currentTimeMillis(),
                filePath = outputFile.absolutePath
            )
            Log.i(TAG, "Recording started: ${outputFile.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            recorder?.release()
            recorder = null
            outputFile.delete()
            _state.value = RecordingState.Error("錄音啟動失敗: ${e.message}")
        }
    }

    fun stopRecording(): File? {
        val currentState = _state.value
        if (currentState !is RecordingState.Recording) {
            Log.w(TAG, "Not recording, ignoring stop request")
            return null
        }

        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            _state.value = RecordingState.Idle
            val file = File(currentState.filePath)
            Log.i(TAG, "Recording stopped: ${file.name} (${file.length()} bytes)")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            recorder?.release()
            recorder = null
            _state.value = RecordingState.Error("錄音停止失敗: ${e.message}")
            File(currentState.filePath).delete()
            null
        }
    }

    fun release() {
        if (_state.value is RecordingState.Recording) {
            stopRecording()
        }
        recorder?.release()
        recorder = null
    }

    fun clearError() {
        if (_state.value is RecordingState.Error) {
            _state.value = RecordingState.Idle
        }
    }

    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }
}
