package com.quickrec.playback

import android.media.MediaPlayer
import android.util.Log
import java.io.File

class AudioPlayerManager(
    private val onCompletion: () -> Unit = {},
    private val onError: (String) -> Unit = {}
) {
    companion object {
        private const val TAG = "AudioPlayerManager"
    }

    private var mediaPlayer: MediaPlayer? = null
    var currentPlayingFile: String? = null
        private set

    fun play(file: File) {
        Log.d(TAG, "Playback requested: ${file.name}")
        
        // If already playing the same file, stop it (toggle behavior)
        if (currentPlayingFile == file.absolutePath && isPlaying()) {
            stop()
            return
        }

        // If playing something else, stop it first
        stop()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    Log.d(TAG, "Playback completed: ${file.name}")
                    this@AudioPlayerManager.currentPlayingFile = null
                    onCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    val msg = "MediaPlayer error: what=$what, extra=$extra"
                    Log.e(TAG, "Playback error: $msg")
                    onError(msg)
                    true
                }
            }
            currentPlayingFile = file.absolutePath
            Log.i(TAG, "Playback started: ${file.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Playback error: ${e.message}")
            onError(e.message ?: "Unknown playback error")
        }
    }

    fun stop() {
        if (mediaPlayer != null) {
            Log.d(TAG, "Playback stopped: $currentPlayingFile")
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            currentPlayingFile = null
        }
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun release() {
        stop()
    }
}
